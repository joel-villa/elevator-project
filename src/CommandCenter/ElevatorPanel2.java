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
 * TODO: IMPLEMENT BUTTONS FOR CONTROL MODE
 */
public class ElevatorPanel2 extends VBox {

    private final CommandCenter commandCenter;
    private final int elevatorId;


    private int currentFloor = 1;
    private boolean isDoorOpen = false;
    private boolean isEnabled = true;         // RUN/STOP
    private boolean autoMode = false;
    private boolean isFireMode = false;


    //Since they use 'diffenet' directions then us, im just traping them in
    //here TODO: FIX THIS
    private enum GUIDIRECTIONCHGME { UP, DOWN, IDLE }
    private GUIDIRECTIONCHGME currentDirection =GUIDIRECTIONCHGME.IDLE;

    //Start stop button, into one now
    private Button mainControlButton;
    private StackPane shaftPane;
    private VBox floorsColumn;
    private Pane carPane;
    private VBox movingCar;
    private Label carFloorLabel;
    private TranslateTransition animation;

    //Floor to indicators
    private HashMap<Integer, DualDotIndicatorPanel> floorCallIndicators =
            new HashMap<>();
    private DirectionIndicatorPanel directionIndicator;
    private Label currentFloorDisplay;


    private static final double FLOOR_HEIGHT = 30;
    private static final double SPACING = 3;
    private static final double TOTAL_HEIGHT = FLOOR_HEIGHT + SPACING;
    private static final double SPEED_PER_FLOOR = 400; // ms

    //TODO: make this actually in tune
    private class DualDotIndicatorPanel extends VBox {
        private final Circle upDot = new Circle(3, Color.web("#505050"));
        private final Circle downDot = new Circle(3, Color.web("#505050"));

        DualDotIndicatorPanel() {
            super(6);
            getChildren().addAll(upDot, downDot);
            setAlignment(Pos.CENTER);
            setPadding(new Insets(0, 4, 0, 4));
        }

        void setDotLit(GUIDIRECTIONCHGME dir, boolean lit) {
            Color c = lit ? Color.WHITE : Color.web("#505050");
            if (dir == GUIDIRECTIONCHGME.UP) upDot.setFill(c);
            if (dir == GUIDIRECTIONCHGME.DOWN) downDot.setFill(c);
        }
    }
    //TODO: Same issue here
    private class DirectionIndicatorPanel extends VBox {
        private final Polygon upTri;
        private final Polygon downTri;
        private final Color OFF = Color.BLACK;

        DirectionIndicatorPanel() {
            super(6);
            upTri = new Polygon(6,0, 0,8, 12,8);
            downTri = new Polygon(6,8, 0,0, 12,0);
            setAlignment(Pos.CENTER);
            setPadding(new Insets(5));
            setDirection(GUIDIRECTIONCHGME.IDLE);
            getChildren().addAll(upTri, downTri);
        }

        void setDirection(GUIDIRECTIONCHGME d) {
            upTri.setFill(d == GUIDIRECTIONCHGME.UP ? Color.WHITE : OFF);
            downTri.setFill(d == GUIDIRECTIONCHGME.DOWN ? Color.WHITE : OFF);
        }
    }

    //rewrite part 2 !!!!! how fun!!!!

    public ElevatorPanel2(int id, CommandCenter cc) {
        super(1);
        this.elevatorId = id;
        this.commandCenter = cc;

        setAlignment(Pos.TOP_CENTER);
        setStyle("-fx-background-color: #333;");
        setPrefWidth(110);

        Label title = new Label("Elevator " + id);
        title.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        // START/STOP button now in one button!!!
        mainControlButton = new Button("OFF");
        mainControlButton.setStyle("-fx-background-color: #B22222; -fx-text-fill: white;");
        mainControlButton.setPrefWidth(90);
        mainControlButton.setOnAction(e -> toggleLocalRunStop());
        HBox statusRow = new HBox(5);
        statusRow.setAlignment(Pos.CENTER);
        currentFloorDisplay = new Label("1");
        currentFloorDisplay.setPrefSize(30, 30);
        currentFloorDisplay.setAlignment(Pos.CENTER);
        currentFloorDisplay.setStyle(
                "-fx-background-color: white; -fx-text-fill: black; " +
                        "-fx-font-weight: bold; -fx-font-size: 18px;");

        directionIndicator = new DirectionIndicatorPanel();
        statusRow.getChildren().addAll(currentFloorDisplay, directionIndicator);


        shaftPane = new StackPane();
        floorsColumn = new VBox(SPACING);
        carPane = new Pane();
        carPane.setMouseTransparent(true);

        for (int floor = 10; floor >= 1; floor--) {
            floorsColumn.getChildren().add(createFloorRow(floor));
        }

        carFloorLabel = new Label("1");
        carFloorLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        movingCar = new VBox(carFloorLabel);
        movingCar.setAlignment(Pos.CENTER);
        movingCar.setPrefSize(40, FLOOR_HEIGHT);
        movingCar.setStyle("-fx-background-color: #606060; -fx-border-color:black; -fx-border-width:0 2 0 2;");
        movingCar.setLayoutX(42);

        carPane.getChildren().add(movingCar);
        shaftPane.getChildren().addAll(floorsColumn, carPane);

        getChildren().addAll(title, mainControlButton, statusRow, shaftPane);
        animation = new TranslateTransition();
        animation.setNode(movingCar);

        updateCarPosition(1, false);
        startGuiUpdateThread(); //zz replaces bus call
    }


    // Floor row builder (NOW WITH BUTTONS!(WOAW))

    private HBox createFloorRow(int floor) {
        HBox row = new HBox(4);
        row.setAlignment(Pos.CENTER);
        row.setPrefSize(95, FLOOR_HEIGHT);

        DualDotIndicatorPanel ind = new DualDotIndicatorPanel();
        floorCallIndicators.put(floor, ind);

        // Floor number button
        Button floorBtn = new Button(String.valueOf(floor));
        floorBtn.setStyle("-fx-background-color:#404040; -fx-text-fill:white;");
        floorBtn.setPrefSize(40, 25);
        floorBtn.setOnAction(e -> {

            commandCenter.sendServiceMessage(elevatorId, floor);   // woaw
        });

        row.getChildren().addAll(ind, floorBtn);
        return row;
    }


    // START/STOP button local handler
    private void toggleLocalRunStop() {
        isEnabled = !isEnabled;
        updateRunStopUI();
    }
    // Flips button and lets command center know about it
    private void updateRunStopUI() {
        if (isEnabled) {
            mainControlButton.setText("OFF");
            mainControlButton.setStyle("-fx-background-color:#B22222; -fx-text-fill:white;");
            commandCenter.disableSingleElevator(elevatorId);
        } else {
            mainControlButton.setText("ON");
            mainControlButton.setStyle("-fx-background-color:#228B22; -fx-text-fill:white;");
            commandCenter.enableSingleElevator(elevatorId);
        }
    }

   //REPLACE THE BUS UPDATE zz
    private void startGuiUpdateThread() {
        Thread t = new Thread(() -> {
            while (true) {
                //my bad guys im just silly
//                boolean systemOn = commandCenter.elevatorOn(elevatorId);
//                if (systemOn != isEnabled) {
//                    Platform.runLater(() -> {
//                        isEnabled = systemOn;
//                        updateRunStopUI();
//                    });
//                }

                FloorNDirection f = commandCenter.getFloorNDirection(elevatorId);
                if (f != null && f.direction() == Direction.STOPPED) {
                    Platform.runLater(() -> {
                        updateCarPosition(f.getFloor(), true);
                        setDirection(GUIDIRECTIONCHGME.IDLE);
                    });
                }

                // TODO: DO INDICATOR LIGHTS
                // TODO: display floor, hall floors and door info

                try { Thread.sleep(20); }
                catch (InterruptedException ignored) {}
            }
        });
        t.setDaemon(true);
        t.start();
    }

    //only once it reaches the floor
    private void updateCarPosition(int floor, boolean animateFlag) {
        int diff = Math.abs(floor - currentFloor);
        currentFloor = floor;

        currentFloorDisplay.setText(String.valueOf(floor));
        carFloorLabel.setText(String.valueOf(floor));

        double targetY = (10 - floor) * TOTAL_HEIGHT;

        if (animateFlag) {
            animation.stop();
            animation.setDuration(Duration.millis(Math.max(1, diff) * SPEED_PER_FLOOR));
            animation.setToY(targetY);
            animation.playFromStart();
        } else {
            movingCar.setTranslateY(targetY);
        }
    }

    private void setDirection(GUIDIRECTIONCHGME dir) {
        currentDirection = dir;
        directionIndicator.setDirection(dir);
    }

    //TODO: Hook this up
    private void setDoorStatus(boolean open) {
        isDoorOpen = open;
        String color = open ? "white" : "black";
        movingCar.setStyle("-fx-background-color:#606060; -fx-border-color:" +
                color + "; -fx-border-width:0 2 0 2;");
    }

    //huh
    public int getCurrentFloor() { return currentFloor; }
    public boolean isDoorOpen() { return isDoorOpen; }
    public boolean isAutoMode() { return autoMode; }
    public boolean isFireMode() { return isFireMode; }
    public boolean isEnabled() { return isEnabled; }
}

