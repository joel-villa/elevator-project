package ElevatorController.LowerLevel;

import Bus.SoftwareBus;
import Bus.SoftwareBusCodes;
import Message.*;

/**
 * The door assembly is a virtualization of the physical interfaces which
 * comprise the doors: fully open sensors, fully closed sensors, door
 * obstruction sensors, the scale, and the door motor. The door assembly posts
 * and receives messages from its physical counterparts via the software bus;
 * posting to the motor; receiving from the fully closed sensors, fully open
 * sensors, the scale, and the door obstruction sensors.
 */
public class DoorAssembly {
    private boolean obstructed;
    private boolean fullyClosed;
    private boolean fullyOpen;
    private boolean overCapacity;
    private final int ELEVATOR_ID;
    private final SoftwareBus softwareBus;

    // *** Constants for TOPIC codes ***
    // Sent to MUX
    private static final int TOPIC_DOOR_CONTROL = SoftwareBusCodes.doorControl;

    // Received from MUX
    private static final int TOPIC_DOOR_SENSOR = SoftwareBusCodes.doorSensor;
    private static final int TOPIC_CABIN_LOAD = SoftwareBusCodes.cabinLoad;
    private static final int TOPIC_DOOR_STATUS = SoftwareBusCodes.doorStatus;

    //Constants for body codes
    private static final int OPEN_CODE = SoftwareBusCodes.doorOpen;
    private static final int CLOSE_CODE = SoftwareBusCodes.doorClose;
    private static final int OBSTRUCTED_CODE = SoftwareBusCodes.obstructed;
    private static final int NOT_OBSTRUCTED_CODE = SoftwareBusCodes.clear;
    private static final int OVER_CAPACITY_CODE = SoftwareBusCodes.overloaded;
    private static final int NOT_OVER_CAPACITY_CODE = SoftwareBusCodes.normal;

    /**
     * Instantiate a DoorAssembly object, and run its thread
     * @param elevatorID For software bus messages
     * @param softwareBus The means of communication
     */
    public DoorAssembly(int elevatorID, SoftwareBus  softwareBus) {
        this.obstructed = false;
        this.fullyClosed = false;
        this.fullyOpen = true;
        this.overCapacity = false;
        this.softwareBus = softwareBus;
        this.ELEVATOR_ID = elevatorID;

        softwareBus.subscribe(TOPIC_DOOR_SENSOR, elevatorID);
        softwareBus.subscribe(TOPIC_CABIN_LOAD, elevatorID);
        softwareBus.subscribe(TOPIC_DOOR_STATUS, elevatorID);
    }

    /**
     * Send message to softwareBus to open the doors (which sends the message
     * to the MUX)
     */
    public void open(){
        // correct body for current mux
        softwareBus.publish(new Message(TOPIC_DOOR_CONTROL, ELEVATOR_ID, OPEN_CODE));
    }

    /**
     * Send message to softwareBus to close the doors (which sends the message
     * to the MUX)
     */
    public void close(){
        // correct body for current mux 11/23/2025
        softwareBus.publish(new Message(TOPIC_DOOR_CONTROL, ELEVATOR_ID, CLOSE_CODE));

    }

    /**
     * @return true if obstruction sensor triggered, false otherwise
     */
    public boolean obstructed(){
        Message message = MessageHelper.pullAllMessages(softwareBus, ELEVATOR_ID, TOPIC_DOOR_SENSOR);
        if (message != null ) {
            if (message.getBody() == OBSTRUCTED_CODE) obstructed = true;
            if (message.getBody() == NOT_OBSTRUCTED_CODE) obstructed = false;
        }
        return obstructed;
    }

    /**
     * @return true if fully closed sensor triggered, false otherwise
     */
    public boolean fullyClosed(){
        Message message =  MessageHelper.pullAllMessages(softwareBus, ELEVATOR_ID, TOPIC_DOOR_STATUS);
        if (message != null ) {
            if (message.getBody() == OPEN_CODE) fullyClosed = false;
            if (message.getBody() == CLOSE_CODE) fullyClosed = true;
            else System.out.println("Unexpected body in SoftwareBusCodes.doorStatus Message in DoorAssembly: body = " + message.getBody());
        }
        return fullyClosed;
    }

    /**
     * @return true if fully open sensor triggered, false otherwise
     */
    public boolean fullyOpen(){
        Message message =  MessageHelper.pullAllMessages(softwareBus, ELEVATOR_ID, TOPIC_DOOR_STATUS);
        if (message != null ) {
            if (message.getBody() == OPEN_CODE) fullyOpen = true;
            if (message.getBody() == OPEN_CODE) fullyOpen = false;
        }
        return fullyOpen;
    }

    /**
     * @return true if an over capacity message was received, false if an under
     *         capacity message was received, true initially
     */
    public boolean overCapacity(){
        Message message = MessageHelper.pullAllMessages(softwareBus, ELEVATOR_ID, TOPIC_CABIN_LOAD);
        if (message != null ) {
            if (message.getBody() == OVER_CAPACITY_CODE) overCapacity = true;
            if (message.getBody() == NOT_OVER_CAPACITY_CODE) overCapacity = false;
        }
        return overCapacity;
    }

}
