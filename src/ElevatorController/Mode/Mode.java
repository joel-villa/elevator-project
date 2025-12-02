package ElevatorController.Mode;

import Bus.SoftwareBus;
import Bus.SoftwareBusCodes;
import ElevatorController.Misc.Destination;
import ElevatorController.Misc.ModeEnum;
import Message.Message;

public class Mode {
    private SoftwareBus softwareBus;
    private ModeEnum currMode;
    private Destination currDest;
    private int currentElevator;


    public Mode(int currentElevator, SoftwareBus softwareBus) {
        this.softwareBus = softwareBus;
        this.currentElevator = currentElevator;
        this.currMode = ModeEnum.NORMAL;
        this.currDest = null;

        softwareBus.subscribe(SoftwareBusCodes.elevatorState, currentElevator);
        softwareBus.subscribe(SoftwareBusCodes.setMode, currentElevator);
        softwareBus.subscribe(SoftwareBusCodes.setDestination, currentElevator);
        softwareBus.subscribe(SoftwareBusCodes.fireAlarm, currentElevator);

    }

    public ModeEnum getMode(){
        // check software bus for new mode
        return currMode;
    }

    public Destination getDestination(){
        int nextFloor = -1;
        Message msg = softwareBus.get(SoftwareBusCodes.setDestination, currentElevator);

        while(msg != null){
            nextFloor = msg.getBody();
            msg = softwareBus.get(SoftwareBusCodes.setDestination, currentElevator);
        }

        Destination nextDest = new Destination(nextFloor, -1);
        return nextDest;
    }
}
