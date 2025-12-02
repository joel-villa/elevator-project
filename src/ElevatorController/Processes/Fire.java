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
        //In fire mode, move all elevators down to floor one
        //Mode gets fire key message

        //buttons handles fire key
        //if single calls are enable then the fire key is inserted
        buttons.disableCalls();

        buttons.enableSingleRequest();

        //Close doors
        ProcessesUtil.doorClose(doorAssembly,notifier);

        FloorNDirection fireKeyService = null;

        //Listen to requests if fire key is inserted, otherwise go to first floor
        while (cabin.getTargetFloor() != 1 && !cabin.arrived() && mode.getMode() == State.FIRE) {
            //Get any services enabled by fire key
            if (fireKeyService == null) fireKeyService = buttons.nextService(cabin.currentStatus());
            //Process services enabled by fire key
            if (fireKeyService != null)
                cabin.gotoFloor(fireKeyService.floor());
            //Go to floor 1 if no requests
            else if (cabin.getTargetFloor() != 1)
                cabin.gotoFloor(1);
            //Arrival process (open doors, wait, close doors)
            if (cabin.arrived()) {
                ProcessesUtil.arriveProcess(buttons, doorAssembly, notifier, fireKeyService);
                fireKeyService = null;
            }
        }
        //Return exit mode
        return mode.getMode();
    }


}
