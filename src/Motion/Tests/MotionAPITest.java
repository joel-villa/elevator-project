package Motion.Tests;

import Motion.Elevator_Controler.MotionController;
import Motion.Util.Direction;

public class MotionAPITest {


    public static void main(String[] args) {

        MotionController motionController=new MotionController();
        motionController.set_direction(Direction.UP);
        motionController.start();
        //Uncommit this if you want to see the GUI, also make the motion sim
        // in motion controller public
//        ElevatorGUI gui = new ElevatorGUI(
//                motionController.motionSimulation.getSensors(),
//                motionController.motionSimulation.get_sensor_pos_HashMap(),
//                motionController.motionSimulation.getElevator(),
//                motionController.motionSimulation.getMotor()
//        );
//        //gui.getPrimaryStage(primaryStage);

    }
}
