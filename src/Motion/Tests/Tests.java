package Motion.Tests;

import Motion.Elevator_Controler.MotionController;
import Motion.GUI.ElevatorGUI;
import Motion.Util.Direction;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Demonstrates the elevator moving up and down several times,
 * stopping at each floor, without freezing the GUI.
 */
public class Tests extends Application {

    @Override
    public void start(Stage primaryStage) {
        MotionController motionController=new MotionController();
        motionController.set_direction(Direction.UP);
        motionController.start();


                ElevatorGUI gui = new ElevatorGUI(
                motionController.motionSimulation.getSensors(),
                motionController.motionSimulation.get_sensor_pos_HashMap(),
                motionController.motionSimulation.getElevator(),
                motionController.motionSimulation.getMotor()
        );

        Stage stage=gui.getPrimaryStage();
        stage.show();

        Controller controller=new Controller(motionController);
        controller.start();



//        int flors = 10;
//        List<Integer> floorSequence = new ArrayList<>();
//        for (int i = 0; i < flors; i++) floorSequence.add(i);
//        for (int i = flors - 2; i > 0; i--) floorSequence.add(i);
//        Timeline timeline = new Timeline();
//        double time_at_Floor = 2.05;
//        double time = 10;
//        Direction dir = Direction.UP;
//        for (int i = 0; i < floorSequence.size(); i++) {
//            int floor = floorSequence.get(i);
//            Direction direction = (i < flors - 1) ? Direction.UP : Direction.DOWN;
//            //System.out.println(i+" < "+flors+ "Floor "+floor);
//            timeline.getKeyFrames().add(new KeyFrame(
//                    Duration.seconds(time),
//                    e -> {
//                        motionController.set_direction(direction);
//                        motionController.start();
//                    }
//            ));
//
//            time += time_at_Floor; //this is the time it took to reach the floor
//            timeline.getKeyFrames().add(new KeyFrame(
//                    Duration.seconds(time),
//                    e -> motionController.stop()
//
//            ));
//        }
//
//        timeline.setCycleCount(Timeline.INDEFINITE); //so liek a while true that doesnt break everything!!!
//        timeline.play();
    }

    private static class Controller extends Thread{
        MotionController motionController;
        Controller(MotionController motionController){
            this.motionController=motionController;
        }
        @Override
        public void run() {

            try {
                motionController.set_direction(Direction.UP);
                motionController.start();
                Thread.sleep(10000);
                System.out.println("Stopping");
                motionController.stop();
                Thread.sleep(10000);
                motionController.set_direction(Direction.DOWN);
                motionController.start();
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}
