package ElevatorController.Processes;

import ElevatorController.LowerLevel.*;
import ElevatorController.Util.ConstantsElevatorControl;
import ElevatorController.Util.Direction;
import ElevatorController.Util.FloorNDirection;
import ElevatorController.Util.State;

/**
 * In fire mode, the elevator only listens to request buttons in the cabin if the fire key has been inserted.
 * Only one service button can be lit up at a time.
 * If two buttons are pressed, the most recently pressed button is the only service request.
 */
public class Fire {
    /**
     * Fire Process
     * @param mode the mode lower level object
     * @param buttons the buttons lower level object
     * @param cabin the cabin lower level object
     * @param doorAssembly the door assembly lower level object
     * @param notifier the notifier lower level object
     * @return
     */
    public static State fire(Mode mode, Buttons buttons, Cabin cabin,
                             DoorAssembly doorAssembly, Notifier notifier){
        System.out.println("IN FIRE"); // TODO: delete this (for debugging)

        //Buttons gets fire key message

        //buttons handles fire key
        //if single calls are enable then the fire key is inserted
        buttons.disableCalls();

        buttons.enableSingleRequest();

        //Close doors
        ProcessesUtil.doorClose(doorAssembly,notifier);

        FloorNDirection fireKeyService = null;

        //TODO: this is just normal mode's code, not fully working
        // TODO: button presses are not overriding
        // - Expected behavior: pressing a button in the cabin while in fire mode should overwrite any presses
        // - is this an issue in Buttons?
        while (mode.getMode() == State.FIRE){
            if (fireKeyService == null) fireKeyService = buttons.nextService(cabin.currentStatus());
            //go to floor of current service
            if (fireKeyService != null) {
                // update current service in the case of better destinations
                // TODO: not over-writing destination for some reason
                fireKeyService = buttons.nextService(cabin.currentStatus());

                // go to the fireKeyService floor
                cabin.gotoFloor(fireKeyService.floor());
            }
            //arrive (open doors, wait, close doors)
            if (cabin.arrived() && fireKeyService != null) {
                //System.out.println("the humble 'whatchu doin queen'");
                ProcessesUtil.arriveProcess(buttons,doorAssembly,notifier,fireKeyService);
                fireKeyService = null;
            }
        }

// NOTE: the code bellow is not working as intended
//        while (mode.getMode() == State.FIRE){
//            System.out.println("cabin.getTargetFloor(): " + cabin.getTargetFloor() + ", cabin.arrived(): " + cabin.arrived());
//            //Listen to requests if fire key is inserted, otherwise go to first floor
//            while (cabin.getTargetFloor() != 1 && !cabin.arrived()) {
//                //Get any services enabled by fire key
//                if (fireKeyService == null) fireKeyService = buttons.nextService(cabin.currentStatus());
//                //Process services enabled by fire key
//                if (fireKeyService != null) {
//                    fireKeyService = buttons.nextService(cabin.currentStatus());
//                    cabin.gotoFloor(fireKeyService.floor());
//                }
//                //Go to floor 1 if no requests
//                else if (cabin.getTargetFloor() != 1)
//                    cabin.gotoFloor(1);
//                //Arrival process (open doors, wait, close doors)
//                if (cabin.arrived()) {
//                    ProcessesUtil.arriveProcess(buttons, doorAssembly, notifier, fireKeyService);
//                    fireKeyService = null;
//                }
//                System.out.println("LOOPING"); //TODO: delete this (debugging)
//            }
//        }

        // TODO: when you use the commented out code below, fire alarm plays noise, currently does not

//        //Listen to requests if fire key is inserted, otherwise go to first floor
//        while (cabin.getTargetFloor() != 1 && !cabin.arrived() && mode.getMode() == State.FIRE) {
//            //Get any services enabled by fire key
//            if (fireKeyService == null) fireKeyService = buttons.nextService(cabin.currentStatus());
//            //Process services enabled by fire key
//            if (fireKeyService != null)
//                cabin.gotoFloor(fireKeyService.floor());
//            //Go to floor 1 if no requests
//            else if (cabin.getTargetFloor() != 1)
//                cabin.gotoFloor(1);
//            //Arrival process (open doors, wait, close doors)
//            if (cabin.arrived()) {
//                ProcessesUtil.arriveProcess(buttons, doorAssembly, notifier, fireKeyService);
//                fireKeyService = null;
//            }
//        }

//        Return exit mode
        return mode.getMode();
    }


}
