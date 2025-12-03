package CommandCenter;

import ElevatorController.Util.FloorNDirection;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.util.Duration;

import java.util.concurrent.ConcurrentHashMap;

/**
 * ElevatorPanel - Visual representation and control panel for a single elevator
 *
 * This class provides a JavaFX UI component that displays:
 * - Current floor and direction indicator
 * - Animated elevator car moving through floors
 * - Call indicators (up/down dots) for each floor
 * - Start/Stop control button
 *
 * TODO: IMPLEMENT BUTTONS FOR CONTROL MODE
 * TODO: Implement indicator lights for hall calls
 * TODO: Display door open/close status
 * TODO: Add floor selection buttons for manual control
 */
public class ElevatorPanel extends VBox {

    private CommandCenter commandCenter;

    // Local direction enum for UI purposes
    public enum Direction { UP, DOWN, IDLE }

    // ========== STATE FLAGS ==========
    private int currentFloor = 1;                                    // Current floor position
    private ElevatorPanel.Direction currentDirection = Direction.IDLE; // Current travel direction
    private boolean isDoorOpen = false;                              // Door status
    private boolean isEnabled = true;                                // Running state (true = running)
    private boolean autoMode = false;                                // Control mode (true = auto/independent)
    private boolean isFireMode = false;                              // Fire recall mode flag

    // ========== IDENTIFIERS ==========
    private final int elevatorId;                                    // Unique elevator identifier

    // ========== UI COMPONENTS ==========
    private Button mainControlButton;                                // Start/Stop button
    private final String btnText_START = "START";
    private final String btnColor_START = "-fx-background-color: #228B22;";
    private final String btnText_STOP  = "STOP";
    private final String btnColor_STOP = "-fx-background-color: #B22222;";

    private StackPane shaftPane;                                     // Container for elevator shaft
    private VBox floorButtonColumn;                                  // Column of floor indicators
    private Pane carPane;                                            // Pane for animated car
    private VBox movingCar;                                          // The elevator car visual
    private Label carFloorLabel;                                     // Floor label inside car
    private TranslateTransition elevatorAnimation;                   // Animation for car movement

    // Map of floor number -> call indicator (up/down dots)
    private final ConcurrentHashMap<Integer, DualDotIndicatorPanel> floorCallIndicators = new ConcurrentHashMap<>();

    private final DirectionIndicatorPanel directionIndicator;        // Up/down triangle indicator
    private final Label currentFloorDisplay;                         // Large floor number display

    // ========== ANIMATION CONSTANTS ==========
    private static final double FLOOR_HEIGHT = 20.0;                 // Height of each floor row in pixels
    private static final double FLOOR_SPACING = 3.0;                 // Spacing between floors
    private static final double TOTAL_FLOOR_HEIGHT = FLOOR_HEIGHT + FLOOR_SPACING;
    private static final double ANIMATION_SPEED_PER_FLOOR = 400.0;  // Milliseconds per floor traveled

    // ========== INNER CLASSES ==========

    /**
     * DualDotIndicatorPanel - Displays up/down call indicators for a single floor
     * Shows two dots (circles) that light up to indicate pending hall calls
     */
    private class DualDotIndicatorPanel extends VBox {
        private final Circle upDot = new Circle(3, Color.web("#505050"));     // Upper dot (up calls)
        private final Circle downDot = new Circle(3, Color.web("#505050"));   // Lower dot (down calls)

        /**
         * Creates a dual-dot indicator for a specific floor
         * @param floor The floor number this indicator represents
         * @param parentPanel Reference to the parent ElevatorPanel
         */
        DualDotIndicatorPanel(int floor, ElevatorPanel parentPanel) {
            super(6);
            getChildren().addAll(upDot, downDot);
            setAlignment(Pos.CENTER);
            setPadding(new Insets(0, 5, 0, 5));
        }

        /**
         * Sets whether a direction dot should be lit (indicating an active call)
         * @param direction Which direction dot to update (UP or DOWN)
         * @param lit True to light up (white), false to dim (dark gray)
         */
        void setDotLit(ElevatorPanel.Direction direction, boolean lit) {
            Color color = lit ? Color.WHITE : Color.web("#505050");
            if (direction == ElevatorPanel.Direction.UP)   upDot.setFill(color);
            if (direction == ElevatorPanel.Direction.DOWN) downDot.setFill(color);
        }
    }

    /**
     * DirectionIndicatorPanel - Displays current travel direction using triangles
     * Shows up/down triangles that light up based on elevator direction
     */
    private class DirectionIndicatorPanel extends VBox {
        private final Polygon upTriangle, downTriangle;
        private final Color UNLIT_COLOR = Color.BLACK;

        /**
         * Creates the direction indicator with up and down triangles
         */
        DirectionIndicatorPanel() {
            super(6);
            upTriangle   = new Polygon(6.0, 0.0, 0.0, 8.0, 12.0, 8.0);    // Triangle pointing up
            downTriangle = new Polygon(6.0, 8.0, 0.0, 0.0, 12.0, 0.0);    // Triangle pointing down
            setDirection(ElevatorPanel.Direction.IDLE);
            getChildren().addAll(upTriangle, downTriangle);
            setAlignment(Pos.CENTER);
            setPadding(new Insets(5));
        }

        /**
         * Updates which triangle is lit based on current direction
         * @param newDirection The direction to display (UP, DOWN, or IDLE)
         */
        void setDirection(ElevatorPanel.Direction newDirection) {
            upTriangle.setFill(newDirection == ElevatorPanel.Direction.UP   ? Color.WHITE : UNLIT_COLOR);
            downTriangle.setFill(newDirection == ElevatorPanel.Direction.DOWN ? Color.WHITE : UNLIT_COLOR);
        }
    }

    // ========== CONSTRUCTOR ==========

    /**
     * Constructs a new ElevatorPanel UI component
     *
     * Creates the complete visual interface including:
     * - Title label with elevator ID
     * - Start/Stop control button
     * - Current floor display and direction indicator
     * - Animated elevator shaft with 10 floors
     * - Floor indicators with call lights
     * - Animated elevator car
     *
     * @param id The unique identifier for this elevator
     * @param commandCenter Reference to the CommandCenter for state synchronization
     */
    public ElevatorPanel(int id, CommandCenter commandCenter) {
        super(3);
        this.elevatorId = id;
        this.commandCenter = commandCenter;

        setAlignment(Pos.CENTER);
        setStyle("-fx-background-color: #333333;");
        setPrefWidth(100);

        // Title label
        Label title = new Label("Elevator " + id);
        title.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        // Start/Stop button
        mainControlButton = new Button(btnText_STOP);
        mainControlButton.setStyle(
                btnColor_STOP + " -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 0;");
        mainControlButton.setPrefWidth(90);
        mainControlButton.setOnAction(e -> toggleEnabledState());

        // Status row: current floor display + direction indicator
        HBox statusRow = new HBox(5);
        statusRow.setAlignment(Pos.CENTER_RIGHT);
        statusRow.setPrefWidth(90);

        currentFloorDisplay = new Label(String.valueOf(this.currentFloor));
        currentFloorDisplay.setStyle(
                "-fx-background-color: white; -fx-text-fill: black; " +
                        "-fx-font-size: 18px; -fx-font-weight: bold; -fx-alignment: center;");
        currentFloorDisplay.setPrefSize(30, 30);

        directionIndicator = new DirectionIndicatorPanel();
        statusRow.getChildren().addAll(currentFloorDisplay, directionIndicator);

        getChildren().addAll(title, mainControlButton, statusRow);

        // Shaft + car layout
        shaftPane = new StackPane();
        floorButtonColumn = new VBox(FLOOR_SPACING);
        carPane = new Pane();
        carPane.setMouseTransparent(true);

        // Create floor rows from top (10) to bottom (1)
        for (int i = 10; i >= 1; i--) {
            floorButtonColumn.getChildren().add(createFloorRow(i));
        }

        // Create the elevator car visual
        carFloorLabel = new Label(String.valueOf(this.currentFloor));
        carFloorLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white; -fx-font-weight: bold;");

        movingCar = new VBox(carFloorLabel);
        movingCar.setAlignment(Pos.CENTER);
        movingCar.setPrefSize(40, FLOOR_HEIGHT);
        movingCar.setStyle("-fx-background-color: #606060;-fx-border-color: black;-fx-border-width: 0 2 0 2;");
        carPane.getChildren().add(movingCar);
        movingCar.setLayoutX(40.5);

        shaftPane.getChildren().addAll(floorButtonColumn, carPane);
        getChildren().add(shaftPane);

        // Setup animation for car movement
        elevatorAnimation = new TranslateTransition();
        elevatorAnimation.setNode(movingCar);

        // Initialize position at starting floor
        updateElevatorPosition(this.currentFloor, false);

        // Start background thread to sync with CommandCenter
        updateGUI();
    }

    // ========== UI CREATION METHODS ==========

    /**
     * Creates a single floor row in the elevator shaft
     *
     * Each row contains:
     * - Dual-dot call indicator (up/down)
     * - Floor number label
     *
     * @param floor The floor number for this row
     * @return HBox containing the complete floor row
     */
    private HBox createFloorRow(int floor) {
        HBox row = new HBox(5);
        row.setAlignment(Pos.CENTER);
        row.setPrefSize(90, FLOOR_HEIGHT);

        // Create and register call indicator
        DualDotIndicatorPanel callIndicator = new DualDotIndicatorPanel(floor, this);
        floorCallIndicators.put(floor, callIndicator);

        // Floor number label
        Label floorLabel = new Label(String.valueOf(floor));
        floorLabel.setStyle("-fx-background-color: #404040; -fx-text-fill: white;");
        floorLabel.setPrefSize(40, 25);
        floorLabel.setAlignment(Pos.CENTER);

        row.getChildren().addAll(callIndicator, floorLabel);
        return row;
    }

    // ========== STATE CONTROL METHODS ==========

    /**
     * Toggles the elevator between enabled (running) and disabled (stopped) states
     * This is a local UI toggle - actual control comes from CommandCenter
     */
    private void toggleEnabledState() {
        isEnabled = !isEnabled;
        applyEnabledUI();
    }

    /**
     * Updates the UI to reflect the current enabled/disabled state
     * Changes the main control button appearance and text
     */
    private void applyEnabledUI() {
        if (isEnabled) {
            mainControlButton.setText(btnText_STOP);
            mainControlButton.setStyle(btnColor_STOP
                    + " -fx-text-fill: white; -fx-font-weight: bold;");
        } else {
            mainControlButton.setText(btnText_START);
            mainControlButton.setStyle(btnColor_START
                    + " -fx-text-fill: white; -fx-font-weight: bold;");
        }
    }

    // ========== BACKGROUND UPDATE THREAD ==========

    /**
     * Starts a background daemon thread that continuously syncs UI with CommandCenter state
     *
     * This thread:
     * - Polls the CommandCenter for elevator on/off state
     * - Updates the elevator position when stopped
     * - Runs continuously at 10ms intervals
     *
     * TODO: Implement indicator lights for hall calls
     * TODO: Display door open/close status
     * TODO: Show active floor selections
     */
    private void updateGUI() {
        Thread t = new Thread(() -> {
            while (true) {
                // Check if elevator is enabled/disabled from CommandCenter
                if (!commandCenter.elevatorOn(elevatorId)) {
                    isEnabled = false;
                    applyEnabledUI();
                    logState("System Stop");
                } else {
                    isEnabled = true;
                    applyEnabledUI();
                    logState("System Start");
                }

                // Update position when elevator is stopped
                FloorNDirection floorNDirection = commandCenter.getFloorNDirection(elevatorId);
                if (floorNDirection != null &&
                        floorNDirection.direction() == ElevatorController.Util.Direction.STOPPED) {
                    Platform.runLater(() ->
                            updateElevatorPosition(floorNDirection.getFloor(), true));
                    setDirection(ElevatorPanel.Direction.IDLE);
                }

                // TODO: Implement indicator lights for pending hall calls
                // TODO: Display door status (open/closed)
                // TODO: Show which floors are selected

                try {
                    Thread.sleep(10);
                } catch (InterruptedException ignored) {}
            }
        });
        t.setDaemon(true);
        t.start();
    }

    // ========== ANIMATION METHODS ==========

    /**
     * Updates the elevator car's position to a new floor
     *
     * Calculates the vertical position based on floor number and either
     * animates the movement or instantly moves the car
     *
     * @param newFloor The target floor number (1-10)
     * @param animate True to animate the movement, false for instant positioning
     */
    private void updateElevatorPosition(int newFloor, boolean animate) {
        // Calculate Y position (floor 10 at top = Y:0, floor 1 at bottom)
        double targetY = (10 - newFloor) * TOTAL_FLOOR_HEIGHT;
        int floorsToTravel = Math.abs(newFloor - this.currentFloor);
        this.currentFloor = newFloor;

        // Update floor displays
        this.currentFloorDisplay.setText(String.valueOf(newFloor));
        this.carFloorLabel.setText(String.valueOf(newFloor));

        if (animate) {
            // Animate movement with speed proportional to distance
            elevatorAnimation.stop();
            elevatorAnimation.setDuration(
                    Duration.millis(Math.max(1, floorsToTravel) * ANIMATION_SPEED_PER_FLOOR));
            elevatorAnimation.setToY(targetY);
            elevatorAnimation.playFromStart();
        } else {
            // Instant positioning (no animation)
            movingCar.setTranslateY(targetY);
        }
        setDirection(ElevatorPanel.Direction.IDLE);
    }

    /**
     * Updates the visual door status
     * Changes the border color of the elevator car to indicate open/closed doors
     *
     * @param open True if doors are open, false if closed
     */
    private void setDoorStatus(boolean open) {
        this.isDoorOpen = open;
        String borderColor = open ? "white" : "black";
        movingCar.setStyle(
                "-fx-background-color: #606060;-fx-border-color: "
                        + borderColor + ";-fx-border-width: 0 2 0 2;");
    }

    /**
     * Closes the elevator doors
     * Convenience method that calls setDoorStatus(false)
     */
    private void closeDoor() {
        setDoorStatus(false);
    }

    /**
     * Sets the current direction of travel and updates the direction indicator
     *
     * @param d The new direction (UP, DOWN, or IDLE)
     */
    private void setDirection(ElevatorPanel.Direction d) {
        this.currentDirection = d;
        directionIndicator.setDirection(d);
    }

    // ========== UTILITY METHODS ==========

    /**
     * Clears all call indicators (up/down dots) on all floors
     * Used primarily when clearing fire mode or resetting the system
     */
    private void clearAllCallIndicators() {
        for (int floor = 1; floor <= 10; floor++) {
            DualDotIndicatorPanel indicator = floorCallIndicators.get(floor);
            if (indicator != null) {
                indicator.setDotLit(ElevatorPanel.Direction.UP, false);
                indicator.setDotLit(ElevatorPanel.Direction.DOWN, false);
            }
        }
        System.out.println("Elevator " + elevatorId + " - ALL call indicators cleared");
    }

    /**
     * Debug logging helper - prints current state to console
     *
     * @param action Description of the action that triggered this log
     */
    private void logState(String action) {
        System.out.println("Elevator " + elevatorId + " [" + action +
                "] - Floor: " + currentFloor +
                ", Enabled: " + isEnabled +
                ", FireMode: " + isFireMode +
                ", AutoMode: " + autoMode);
    }

    // ========== GETTERS ==========

    /**
     * @return The current floor number the elevator is on
     */
    public int getCurrentFloor() {
        return currentFloor;
    }

    /**
     * @return True if the doors are currently open
     */
    public boolean isDoorOpen() {
        return isDoorOpen;
    }

    /**
     * @return True if elevator is in automatic/independent mode
     */
    public boolean isAutoMode() {
        return autoMode;
    }

    /**
     * @return True if elevator is in fire recall mode
     */
    public boolean isFireMode() {
        return isFireMode;
    }

    /**
     * @return True if elevator is enabled (running)
     */
    public boolean isEnabled() {
        return isEnabled;
    }
}

/*
 * ========== TODO SUMMARY ==========
 *
 * HIGH PRIORITY:
 * 1. Implement hall call indicator lights
 *    - Connect to CommandCenter to get active hall calls
 *    - Light up appropriate up/down dots when calls exist
 *
 * 2. Display door status
 *    - Get door state from CommandCenter
 *    - Update visual representation (currently has setDoorStatus method but not used)
 *
 * 3. Show floor selections (car calls)
 *    - Add visual indicators for which floors are selected from inside the car
 *    - Possibly add clickable floor buttons for manual control
 *
 * MEDIUM PRIORITY:
 * 4. Implement control mode buttons
 *    - Add UI for switching between Auto/Manual/Fire modes
 *    - Connect to CommandCenter control logic
 *
 * 5. Add manual floor selection
 *    - Create buttons to select destination floors
 *    - Only active in manual control mode
 *
 * LOW PRIORITY:
 * 6. Improve animation smoothness
 *    - Currently animates floor-to-floor, could interpolate better
 *
 * 7. Add sound effects or additional visual feedback
 *
 * 8. Display queue/pending requests
 *    - Show which floors are in the service queue
 */