package ElevatorController.Processes;

import ElevatorController.LowerLevel.*;
import ElevatorController.Util.FloorNDirection;
import ElevatorController.Util.State;

/**
 * Control mode is one where movement is controlled by the Control Room. Using
 * the Software Bus. The Control Room can give commands to the elevators and
 * assumes full control over the system.
 */
public class Control {
    private Mode mode;
    private Buttons buttons;
    private Cabin cabin;
    private DoorAssembly doorAssembly;
    private Notifier notifier;


    /**
     * Create an instance of the Fire Procedure
     * @param mode the mode lower level object
     * @param buttons the buttons lower level object
     * @param cabin the cabin lower level object
     * @param doorAssembly the door assembly lower level object
     * @param notifier the notifier lower level object
     */

    /**
     * Control mode implementation
     * @return The state to switch too (normal or fire)
     */
    public static State control(Mode mode, Buttons buttons, Cabin cabin,
                                DoorAssembly doorAssembly, Notifier notifier){
        buttons.disableCalls();
        buttons.requestReset(0); //NEHEHEHEEH
        //TODO need to implement some API for resetting Request?
        buttons.enableSingleRequest();

        //Close doors
        ProcessesUtil.doorClose(doorAssembly,notifier);
        FloorNDirection currentStatus = null;
        FloorNDirection nextSer = null;
        boolean playSound = false;
        while(mode.getMode() == State.CONTROL){
            nextSer = mode.nextService();
            if(nextSer != null && cabin.getTargetFloor() != nextSer.floor()){
                playSound = true;
                System.out.println("next service isnt null");
                cabin.gotoFloor(nextSer.floor());
            }

            // Update Commmand Center of Cabin floor and direction
            FloorNDirection newStatus = cabin.currentStatus();
            if (newStatus != null && !newStatus.equals(currentStatus)){
                currentStatus = newStatus;
                notifier.elevatorStatus(currentStatus);
            }
//            Arrival process (open doors, wait, close doors)
            if (cabin.arrived() && playSound) {
                playSound = false;
                System.out.println("****** CABIN ARRIVED *****");
                notifier.arrivedAtFloor(cabin.currentStatus());
//                ProcessesUtil.arriveProcess(buttons, doorAssembly, notifier,
//                        nextSer); //TODO: call something other than arrive procees. Do we even
                //need opening doors in control mode?
                nextSer = null;
            }
        }
        return mode.getMode();
    }
}
