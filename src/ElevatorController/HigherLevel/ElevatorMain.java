package ElevatorController.HigherLevel;

import Bus.SoftwareBus;
import ElevatorController.LowerLevel.*;

import static ElevatorController.Processes.ElevatorController.elevatorController;
import static ElevatorController.Processes.ElevatorController.onOff;

/**
 * Main is a lightweight object, which instantiates Elevator Controller, Mode,
 * Buttons, Cabin, Door Assembly and Notifier.
 */
public class ElevatorMain implements Runnable{
    /**
     * Instantiate Everything
     * @param elevatorID the number associated with this elevator
     * @param softwareBus the means of communication
     */
    private Buttons buttons;
    private Cabin cabin;
    private DoorAssembly doorAssembly;
    private Notifier notifier;
    private Mode mode;


    public ElevatorMain(int elevatorID, SoftwareBus softwareBus){
        buttons = new Buttons(elevatorID, softwareBus);
        cabin = new Cabin(elevatorID, softwareBus);
        doorAssembly = new DoorAssembly(elevatorID, softwareBus);
        notifier = new Notifier(elevatorID, softwareBus);
        mode = new Mode(elevatorID, softwareBus);
        onOff = true;

    }

    @Override
    public void run() {
        elevatorController(mode,buttons,cabin,doorAssembly,notifier);
    }
}
