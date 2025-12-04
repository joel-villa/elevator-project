package ElevatorController.Processes;

import Bus.SoftwareBusCodes;
import ElevatorController.LowerLevel.*;
import ElevatorController.Util.FloorNDirection;
import ElevatorController.Util.State;

/**
 * Normal mode is the default mode that the system starts in.
 * The initial state of each elevator is on the first floor with the doors open.
 * In this mode, no messages from the supervisor are expected, other than a change in mode;
 * The movement is determined solely by button presses.
 * This mode provides the typical elevator functionality: handling requests and navigating floors.
 */
public class Normal {
    /**
     *
     * @param mode the mode lower level object
     * @param buttons the buttons lower level object
     * @param cabin the cabin lower level object
     * @param doorAssembly the door assembly lower level object
     * @param notifier the notifier lower level object
     * @return the mode that caused the process to end.
     */
    public static State normal(Mode mode, Buttons buttons, Cabin cabin,
                               DoorAssembly doorAssembly, Notifier notifier){

        //Check if normal
        if (mode.getMode() != State.NORMAL) return mode.getMode();

        //Prepare for use
        buttons.enableCalls();
        buttons.enableAllRequests();
        ProcessesUtil.doorClose(doorAssembly,notifier);
        FloorNDirection currentService = null;
        FloorNDirection currentStatus = null;
        cabin.gotoFloor(cabin.currentStatus().getFloor()); // go to closest floor

        //TODO: TEST CODE, CURRENTLY HAUNTED OOHOHHHH (call ghost busters)
//        cabin.gotoFloor(cabin.getID()*2);
//        currentService = new FloorNDirection(cabin.getID()*2,null);



        //Process Requests until state changes
        while (mode.getMode() == State.NORMAL) {

            // Update Commmand Center of Cabin floor and direction
            FloorNDirection newStatus = cabin.currentStatus();
            if (newStatus != null && !newStatus.equals(currentStatus)){
                currentStatus = newStatus;
                notifier.elevatorStatus(currentStatus);
            }

            //get next service
             currentService = buttons.nextService(cabin.currentStatus());
             //TODO: I think nextService may have scheduling bug (not overwriting to go to closer location)

            //go to floor of current service
            if (currentService != null) {
                // go to the currentService floor
                cabin.gotoFloor(currentService.floor());
            }
            //arrive (open doors, wait, close doors)
            if (cabin.arrived() && currentService != null) {
                //System.out.println("the humble 'whatchu doin queen'");
                System.out.println("****** CABIN ARRIVED *****");
                notifier.arrivedAtFloor(cabin.currentStatus());
                ProcessesUtil.arriveProcess(buttons,doorAssembly,notifier,currentService);
                currentService = null;
            }
        }
        //Exit mode
        return mode.getMode();
    }
}
