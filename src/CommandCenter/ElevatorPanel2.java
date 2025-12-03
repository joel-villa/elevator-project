package CommandCenter;

import ElevatorController.Util.Direction;
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

import java.util.HashMap;

/**
 * ElevatorPanel2 - Enhanced elevator control panel with interactive floor selection
 *
 * This is a revised version of ElevatorPanel with improvements:
 * - Clickable floor buttons for manual service requests
 * - Single unified START/STOP button
 * - Direct integration with CommandCenter for control
 * - Cleaner layout with better spacing
 *
 * TODO: IMPLEMENT BUTTONS FOR CONTROL MODE (Auto/Manual/Fire)
 * TODO: Fix direction enum naming (currently using GUIDIRECTIONCHGME as workaround)
 * TODO: Implement indicator lights to show active hall calls
 * TODO: Display door status visually
 * TODO: Make direction indicators sync with actual elevator movement
 */
public class ElevatorPanel2 extends VBox {

    // ========== CORE REFERENCES ==========
    private final CommandCenter commandCenter;    // Central control system reference
    private final int elevatorId;                 // Unique identifier for this elevator

    // ========== STATE VARIABLES ==========
    private int currentFloor = 1;                 // Current floor position (1-10)
    private boolean isDoorOpen = false;           // Door status (true = open)
    private boolean isEnabled = true;             // System state (true = running)
    private boolean autoMode = false;             // Control mode (true = automatic)
    private boolean isFireMode = false;           // Fire recall mode flag

    // ========== DIRECTION ENUM WORKAROUND ==========
    /**
     * Local direction enum for UI purposes
     *
     * TODO: FIX THIS - Using different enum than the main controller's Direction
     * This is a temporary workaround because the controller uses its own Direction enum
     * Should standardize on one Direction enum across the entire system
     */
    private enum GUIDIRECTIONCHGME { UP, DOWN, IDLE }
    private GUIDIRECTIONCHGME currentDirection = GUIDIRECTIONCHGME.IDLE;

    // ========== UI COMPONENTS ==========
    private Button mainControlButton;             // Combined START/STOP button
    private StackPane shaftPane;                  // Container for elevator shaft
    private VBox floorsColumn;                    // Vertical column of floor rows
    private Pane carPane;                         // Pane containing animated car
    private VBox movingCar;                       // The elevator car visual element
    private Label carFloorLabel;                  // Floor number displayed inside car
    private TranslateTransition animation;        // Animation controller for car movement

    // Map of floor number -> call indicator panel
    private HashMap<Integer, DualDotIndicatorPanel> floorCallIndicators = new HashMap<>();

    private DirectionIndicatorPanel directionIndicator;  // Up/down triangle display
    private Label currentFloorDisplay;                   // Large floor number in status area

    // ========== LAYOUT CONSTANTS ==========
    private static final double FLOOR_HEIGHT = 30;       // Height of each floor row (pixels)
    private static final double SPACING = 3;             // Gap between floor rows (pixels)
    private static final double TOTAL_HEIGHT = FLOOR_HEIGHT + SPACING;  // Total row height
    private static final double SPEED_PER_FLOOR = 400;   // Animation speed (milliseconds per floor)

    // ========== INNER CLASSES ==========

    /**
     * DualDotIndicatorPanel - Visual indicator for hall call requests
     *
     * Displays two colored dots (circles) stacked vertically:
     * - Upper dot = UP call indicator
     * - Lower dot = DOWN call indicator
     *
     * Dots light up (white) when there's an active call in that direction,
     * and dim (dark gray) when no call is pending.
     *
     * TODO: Make this actually sync with real hall call data from CommandCenter
     */
    private class DualDotIndicatorPanel extends VBox {
        private final Circle upDot = new Circle(3, Color.web("#505050"));      // Top dot (UP calls)
        private final Circle downDot = new Circle(3, Color.web("#505050"));    // Bottom dot (DOWN calls)

        /**
         * Creates a new dual-dot indicator panel
         * Initially both dots are unlit (dark gray)
         */
        DualDotIndicatorPanel() {
            super(6);  // 6px spacing between dots
            getChildren().addAll(upDot, downDot);
            setAlignment(Pos.CENTER);
            setPadding(new Insets(0, 4, 0, 4));
        }

        /**
         * Lights or dims a specific direction dot
         *
         * @param dir Which direction to update (UP or DOWN)
         * @param lit True to light up (white), false to dim (dark gray)
         */
        void setDotLit(GUIDIRECTIONCHGME dir, boolean lit) {
            Color c = lit ? Color.WHITE : Color.web("#505050");
            if (dir == GUIDIRECTIONCHGME.UP) upDot.setFill(c);
            if (dir == GUIDIRECTIONCHGME.DOWN) downDot.setFill(c);
        }
    }

    /**
     * DirectionIndicatorPanel - Displays elevator's current travel direction
     *
     * Shows two triangles stacked vertically:
     * - Upper triangle (pointing up) = elevator moving UP
     * - Lower triangle (pointing down) = elevator moving DOWN
     *
     * Active triangle lights up white, inactive stays black.
     *
     * TODO: Same direction enum issue - should use standard Direction enum
     */
    private class DirectionIndicatorPanel extends VBox {
        private final Polygon upTri;              // Triangle pointing upward
        private final Polygon downTri;            // Triangle pointing downward
        private final Color OFF = Color.BLACK;    // Color when not active

        /**
         * Creates the direction indicator with both triangles
         * Initially set to IDLE (both triangles dark)
         */
        DirectionIndicatorPanel() {
            super(6);  // 6px spacing between triangles
            upTri = new Polygon(6,0, 0,8, 12,8);       // Points: center-top, left-bottom, right-bottom
            downTri = new Polygon(6,8, 0,0, 12,0);     // Points: center-bottom, left-top, right-top
            setAlignment(Pos.CENTER);
            setPadding(new Insets(5));
            setDirection(GUIDIRECTIONCHGME.IDLE);
            getChildren().addAll(upTri, downTri);
        }

        /**
         * Updates which triangle is lit based on current direction
         *
         * @param d Direction to display (UP, DOWN, or IDLE)
         *          IDLE = both triangles dark
         */
        void setDirection(GUIDIRECTIONCHGME d) {
            upTri.setFill(d == GUIDIRECTIONCHGME.UP ? Color.WHITE : OFF);
            downTri.setFill(d == GUIDIRECTIONCHGME.DOWN ? Color.WHITE : OFF);
        }
    }

    // ========== CONSTRUCTOR ==========

    /**
     * Constructs a new ElevatorPanel2 with full UI and control integration
     *
     * Creates the complete interface including:
     * - Title label with elevator ID
     * - Single ON/OFF control button
     * - Current floor display and direction indicator
     * - Interactive elevator shaft with clickable floor buttons
     * - Animated elevator car
     * - Hall call indicators for each floor
     *
     * Key improvements over ElevatorPanel:
     * - Floor buttons are now clickable to request service
     * - Simplified START/STOP into single toggle button
     * - Better spacing and layout
     *
     * @param id Unique identifier for this elevator (1-based)
     * @param cc Reference to CommandCenter for state management and control
     */
    public ElevatorPanel2(int id, CommandCenter cc) {
        super(1);  // 1px spacing between main VBox children
        this.elevatorId = id;
        this.commandCenter = cc;

        setAlignment(Pos.TOP_CENTER);
        setStyle("-fx-background-color: #333;");
        setPrefWidth(110);

        // ========== TITLE SECTION ==========
        Label title = new Label("Elevator " + id);
        title.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        // ========== CONTROL BUTTON (ON/OFF) ==========
        // NOW UNIFIED: Single button that toggles between ON and OFF
        mainControlButton = new Button("ON");
        mainControlButton.setStyle("-fx-background-color: #B22222; -fx-text-fill: white;");
        mainControlButton.setPrefWidth(90);
        mainControlButton.setOnAction(e -> toggleLocalRunStop());
        updateRunStopUI();

        // ========== STATUS ROW ==========
        // Contains current floor display and direction indicator
        HBox statusRow = new HBox(5);
        statusRow.setAlignment(Pos.CENTER);

        // Large white box showing current floor number
        currentFloorDisplay = new Label("1");
        currentFloorDisplay.setPrefSize(30, 30);
        currentFloorDisplay.setAlignment(Pos.CENTER);
        currentFloorDisplay.setStyle(
                "-fx-background-color: white; -fx-text-fill: black; " +
                        "-fx-font-weight: bold; -fx-font-size: 18px;");

        directionIndicator = new DirectionIndicatorPanel();
        statusRow.getChildren().addAll(currentFloorDisplay, directionIndicator);

        // ========== ELEVATOR SHAFT SECTION ==========
        shaftPane = new StackPane();
        floorsColumn = new VBox(SPACING);
        carPane = new Pane();
        carPane.setMouseTransparent(true);  // Clicks pass through to floor buttons

        // Build floor rows from top (10) to bottom (1)
        for (int floor = 10; floor >= 1; floor--) {
            floorsColumn.getChildren().add(createFloorRow(floor));
        }

        // ========== ELEVATOR CAR ==========
        carFloorLabel = new Label("1");
        carFloorLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        movingCar = new VBox(carFloorLabel);
        movingCar.setAlignment(Pos.CENTER);
        movingCar.setPrefSize(40, FLOOR_HEIGHT);
        movingCar.setStyle("-fx-background-color: #606060; -fx-border-color:black; -fx-border-width:0 2 0 2;");
        movingCar.setLayoutX(42);  // Position over floor buttons

        carPane.getChildren().add(movingCar);
        shaftPane.getChildren().addAll(floorsColumn, carPane);

        // ========== ASSEMBLE PANEL ==========
        getChildren().addAll(title, mainControlButton, statusRow, shaftPane);

        // Setup animation controller
        animation = new TranslateTransition();
        animation.setNode(movingCar);

        // Initialize car at floor 1
        updateCarPosition(1, false);

        // Start background thread for state synchronization
        startGuiUpdateThread();
    }

    // ========== FLOOR ROW CREATION ==========

    /**
     * Creates a single interactive floor row for the elevator shaft
     *
     * NEW FEATURE: Floor buttons are now CLICKABLE!
     * Clicking a floor button sends a service request to that floor
     *
     * Each row contains:
     * - DualDotIndicatorPanel (up/down call lights)
     * - Clickable floor number button
     *
     * @param floor The floor number for this row (1-10)
     * @return HBox containing the complete floor row with interactive button
     */
    private HBox createFloorRow(int floor) {
        HBox row = new HBox(4);
        row.setAlignment(Pos.CENTER);
        row.setPrefSize(95, FLOOR_HEIGHT);

        // Create and register call indicator
        DualDotIndicatorPanel ind = new DualDotIndicatorPanel();
        floorCallIndicators.put(floor, ind);

        // ========== CLICKABLE FLOOR BUTTON ==========
        // This is the key improvement - floors are now interactive!
        Button floorBtn = new Button(String.valueOf(floor));
        floorBtn.setStyle("-fx-background-color:#404040; -fx-text-fill:white;");
        floorBtn.setPrefSize(40, 25);
        floorBtn.setOnAction(e -> {
            // Send service request to this floor through CommandCenter
            commandCenter.sendServiceMessage(elevatorId, floor);
        });

        row.getChildren().addAll(ind, floorBtn);
        return row;
    }

    // ========== CONTROL BUTTON HANDLERS ==========

    /**
     * Toggles the elevator between running (ON) and stopped (OFF) states
     *
     * This is a LOCAL toggle that:
     * 1. Flips the isEnabled flag
     * 2. Updates the button appearance
     * 3. Notifies CommandCenter to enable/disable this elevator
     */
    private void toggleLocalRunStop() {
        isEnabled = !isEnabled;
        updateRunStopUI();
    }

    /**
     * Updates the control button's appearance and notifies CommandCenter
     *
     * Button states:
     * - Enabled  → "OFF" button (red) - clicking will turn off
     * - Disabled → "ON" button (green) - clicking will turn on
     *
     * Also sends corresponding enable/disable commands to CommandCenter
     */
    private void updateRunStopUI() {
        if (isEnabled) {
            mainControlButton.setText("ON");
            mainControlButton.setStyle("-fx-background-color:#228B22; -fx-text-fill:white;");
            commandCenter.enableSingleElevator(elevatorId);
        } else {
            // Elevator is running - show OFF button (red)
            mainControlButton.setText("OFF");
            mainControlButton.setStyle("-fx-background-color:#B22222; -fx-text-fill:white;");
            commandCenter.disableSingleElevator(elevatorId);
        }
    }

    // ========== BACKGROUND UPDATE THREAD ==========

    /**
     * Starts a background daemon thread to continuously sync UI with CommandCenter
     *
     * This replaces the old "bus update" mechanism with direct CommandCenter polling
     *
     * The thread continuously:
     * 1. Checks if elevator state changed (commented out - was causing issues)
     * 2. Updates elevator position when it reaches a floor (STOPPED state)
     * 3. Updates direction indicator to IDLE when stopped
     *
     * Polling interval: 20ms (50 times per second)
     *
     * TODO: Implement indicator lights for active hall calls
     * TODO: Display door open/closed status
     * TODO: Show which floors have active service requests
     * TODO: Sync direction indicator with actual movement (not just IDLE)
     */
    private void startGuiUpdateThread() {
        Thread t = new Thread(() -> {
            while (true) {
                // ========== SYSTEM ON/OFF SYNC (DISABLED) ==========
                // NOTE: Commented out because it was causing synchronization issues
                // The local toggle button now controls state directly
                /*
                boolean systemOn = commandCenter.elevatorOn(elevatorId);
                if (systemOn != isEnabled) {
                    Platform.runLater(() -> {
                        isEnabled = systemOn;
                        updateRunStopUI();
                    });
                }
                */

                // ========== POSITION UPDATE ==========
                // When elevator reaches a floor and stops, update the visual position
                FloorNDirection f = commandCenter.getFloorNDirection(elevatorId);
                GUIDIRECTIONCHGME direction = null;
                switch (f.direction()){
                    case Direction.UP -> direction = GUIDIRECTIONCHGME.UP;
                    case Direction.DOWN -> direction = GUIDIRECTIONCHGME.DOWN;
                    case Direction.STOPPED -> direction = GUIDIRECTIONCHGME.IDLE;
                    default -> System.out.println("weirdness in starGuiUpdateThread() in ElevatorPanel2");
                }
                if (f != null) {
                    GUIDIRECTIONCHGME d = direction;
                    Platform.runLater(() -> {
                        updateCarPosition(f.getFloor(), false);  // Animate to new position
                        setDirection(d);   // Show idle (no direction)
                    });
                }

                // TODO: Implement call indicator lights
                //       - Get active hall calls from CommandCenter
                //       - Light up appropriate dots using setDotLit()

                // TODO: Display door status
                //       - Get door state from CommandCenter
                //       - Call setDoorStatus() to update visual

                // TODO: Display active car calls (floor selections)
                //       - Show which floors are in the service queue
                //       - Could highlight floor buttons or add indicators

                try {
                    Thread.sleep(20);  // Poll 50 times per second
                } catch (InterruptedException ignored) {}
            }
        });
        t.setDaemon(true);  // Thread dies when application closes
        t.start();
    }

    // ========== POSITION AND ANIMATION ==========

    /**
     * Updates the elevator car's visual position to a specific floor
     *
     * This method:
     * 1. Calculates the vertical position based on floor number
     * 2. Updates both display labels (status and car)
     * 3. Either animates the movement or instantly moves the car
     *
     * Floor positioning:
     * - Floor 10 (top) = Y position 0
     * - Floor 1 (bottom) = Y position (9 * TOTAL_HEIGHT)
     *
     * Animation speed is proportional to distance traveled
     *
     * @param floor The target floor number (1-10)
     * @param animateFlag True = smooth animation, False = instant jump
     */
    private void updateCarPosition(int floor, boolean animateFlag) {
        int diff = Math.abs(floor - currentFloor);  // Distance to travel
        currentFloor = floor;

        // Update both floor displays
        currentFloorDisplay.setText(String.valueOf(floor));
        carFloorLabel.setText(String.valueOf(floor));

        // Calculate Y position (floor 10 at top = 0, floor 1 at bottom)
        double targetY = (10 - floor) * TOTAL_HEIGHT;

        if (animateFlag) {
            // Smooth animation - duration based on floors traveled
            animation.stop();  // Stop any ongoing animation
            animation.setDuration(Duration.millis(Math.max(1, diff) * SPEED_PER_FLOOR));
            animation.setToY(targetY);
            animation.playFromStart();
        } else {
            // Instant positioning (no animation)
            movingCar.setTranslateY(targetY);
        }
    }

    /**
     * Sets the current direction and updates the direction indicator
     *
     * @param dir New direction to display (UP, DOWN, or IDLE)
     */
    private void setDirection(GUIDIRECTIONCHGME dir) {
        currentDirection = dir;
        directionIndicator.setDirection(dir);
    }

    // ========== DOOR STATUS ==========

    /**
     * Updates the visual representation of door status
     *
     * Door status is shown by changing the elevator car's border color:
     * - Open doors = WHITE border (more visible)
     * - Closed doors = BLACK border (blends in)
     *
     * TODO: Hook this up to actual door state from CommandCenter
     *       Currently this method exists but is never called
     *
     * @param open True if doors are open, false if closed
     */
    private void setDoorStatus(boolean open) {
        isDoorOpen = open;
        String color = open ? "white" : "black";
        movingCar.setStyle("-fx-background-color:#606060; -fx-border-color:" +
                color + "; -fx-border-width:0 2 0 2;");
    }

    // ========== GETTERS ==========

    /**
     * @return Current floor number the elevator is on (1-10)
     */
    public int getCurrentFloor() {
        return currentFloor;
    }

    /**
     * @return True if doors are currently open
     */
    public boolean isDoorOpen() {
        return isDoorOpen;
    }

    /**
     * @return True if elevator is in automatic mode
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
     * @return True if elevator is enabled/running
     */
    public boolean isEnabled() {
        return isEnabled;
    }
}

/*
 * ========== COMPREHENSIVE TODO LIST ==========
 *
 * CRITICAL FIXES:
 * 1. Fix Direction Enum Issue
 *    - Currently using GUIDIRECTIONCHGME as a workaround
 *    - Should standardize on one Direction enum system-wide
 *    - Need to map between ElevatorController.Util.Direction and UI direction
 *
 * HIGH PRIORITY:
 * 2. Implement Hall Call Indicators
 *    - Get active hall calls from CommandCenter
 *    - Light up corresponding dots (UP/DOWN) for each floor
 *    - Should update in real-time as calls are made/serviced
 *
 * 3. Connect Door Status Display
 *    - Get door state from CommandCenter (open/closed)
 *    - Call setDoorStatus() to update visual (white/black border)
 *    - Show door status in real-time
 *
 * 4. Display Active Service Requests
 *    - Show which floors are selected (car calls)
 *    - Could highlight floor buttons differently
 *    - Could add separate indicator lights
 *
 * 5. Sync Direction Indicator with Movement
 *    - Currently only shows IDLE when stopped
 *    - Should show UP/DOWN while elevator is moving
 *    - Need to get direction from CommandCenter during movement
 *
 * MEDIUM PRIORITY:
 * 6. Implement Control Mode Buttons
 *    - Add UI for Auto/Manual/Fire mode selection
 *    - Show current mode visually
 *    - Connect to CommandCenter mode control
 *
 * 7. Fix ON/OFF Sync Issue
 *    - Currently commented out because it caused issues
 *    - Need proper bidirectional sync between UI and CommandCenter
 *    - Prevent conflicts between button toggle and system state
 *
 * 8. Add Visual Feedback for Service Requests
 *    - When floor button is clicked, show confirmation
 *    - Maybe briefly highlight the button or show a message
 *    - Help user know their click was registered
 *
 * LOW PRIORITY:
 * 9. Improve Animation
 *    - Could add easing functions for smoother movement
 *    - Add door opening/closing animations
 *    - Consider adding sound effects
 *
 * 10. Add Queue Display
 *     - Show upcoming stops in order
 *     - Display algorithm being used
 *     - Show estimated time to each floor
 *
 * 11. Better Error Handling
 *     - What if CommandCenter returns null?
 *     - Handle disconnection gracefully
 *     - Show error states visually
 *
 * ARCHITECTURAL IMPROVEMENTS:
 * 12. Reduce Polling Frequency
 *     - 20ms polling (50Hz) might be excessive
 *     - Consider event-based updates instead
 *     - Use listeners/callbacks from CommandCenter
 *
 * 13. Separate UI from Logic
 *     - Consider MVVM or MVC pattern
 *     - Make the panel more testable
 *     - Reduce coupling with CommandCenter
 */