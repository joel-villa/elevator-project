package ElevatorController.Processes;

import ElevatorController.LowerLevel.*;
import ElevatorController.Util.FloorNDirection;
import ElevatorController.Util.State;

//TODO: wherever "Fully closed: true over capacity false obstructed false", is printing please remove it
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
     * @return the mode to switch to
     */
    public static State fire(Mode mode, Buttons buttons, Cabin cabin,
                             DoorAssembly doorAssembly, Notifier notifier){
        //Buttons gets fire key message

        //buttons handles fire key
        //if single calls are enable then the fire key is inserted
        buttons.disableCalls();

        buttons.disableRequests();

        //Close doors
        ProcessesUtil.doorClose(doorAssembly,notifier);

        FloorNDirection fireKeyService = null;

        while (mode.getMode() == State.FIRE){
            // While in fire mode do the following:
            if (buttons.fireKeyInserted()){
                // Fire key inserted -> can accept request button events in the cabin
                buttons.enableSingleRequest();

                // update fireService TODO: not over-writing destination for some reason
                // TODO: I think next service potentially has bugs
                fireKeyService = buttons.nextService(cabin.currentStatus());

                //go to floor of current service
                if (fireKeyService != null) {
                    // close doors
                    ProcessesUtil.doorClose(doorAssembly, notifier);

                    // go to the fireKeyService floor
                    cabin.gotoFloor(fireKeyService.floor());
                }

                //arrive (open doors, wait, close doors)
                //TODO this is normal mode logic for arriving, should be different for fire mode
                if (cabin.arrived() && fireKeyService != null) {
                    notifier.arrivedAtFloor(cabin.currentStatus());
                    ProcessesUtil.arriveProcess(buttons,doorAssembly,notifier,fireKeyService);
                    fireKeyService = null;
                }
            } else {
                // Fire key not inserted -> go to floor one, wait with the doors open
                fireKeyService = null;

                // Request buttons disabled
                buttons.disableRequests();

                // Close doors
                ProcessesUtil.doorClose(doorAssembly,notifier);

                // Go to first floor
                cabin.gotoFloor(1);

                // If the cabin is at the first floor, wait with the doors open
                // TODO change this to waiting with doors open
                if(cabin.arrived() && doorAssembly.fullyClosed()){
//                    notifier.arrivedAtFloor(cabin.currentStatus());
//                    ProcessesUtil.arriveProcess(buttons,doorAssembly,notifier,fireKeyService);
                    // Busy wait for the doors to open
//                    while (!ProcessesUtil.tryDoorOpen(doorAssembly)) ;
                }
            }
        }

//      Return exit mode
        return mode.getMode();
    }


}
