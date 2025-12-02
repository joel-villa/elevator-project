package ElevatorController.Processes;

import ElevatorController.LowerLevel.*;
import ElevatorController.Util.State;

import static ElevatorController.Processes.Control.control;
import static ElevatorController.Processes.Fire.fire;
import static ElevatorController.Processes.Normal.normal;

/**
 * The elevator controller is a light-weight object responsible for switching
 * between the elevator’s various modes.
 */
public class ElevatorController {
    public static boolean onOff = true;
    /**
     * Create an instance of Elevator Controller
     * @param normal the normal mode procedure
     * @param fire the fire mode procedure
     * @param control the controlled mode procedure
     */

    /**
     * Switch between the modes using their return values
     */
    public static void elevatorController(Mode mode, Buttons buttons, Cabin cabin,
                                          DoorAssembly doorAssembly, Notifier notifier){
        //The first thing that normal checks is if the mode is in normal.
        //If it's not in normal it will return the current mode
        State currentMode = normal(mode, buttons, cabin, doorAssembly,
                notifier);
        while(onOff){
            boolean on = true;
            if (on) {
                switch (currentMode){
                    case NORMAL -> currentMode = normal(mode, buttons, cabin,
                            doorAssembly, notifier);
                    case FIRE -> currentMode = fire(mode, buttons, cabin,
                            doorAssembly, notifier);
                    case CONTROL -> currentMode = control(mode, buttons, cabin,
                            doorAssembly, notifier);
                    case OFF -> on = false;
                }
            } else {
                if (mode.getMode() != State.OFF) on = true;
            }
        }
    }
}
