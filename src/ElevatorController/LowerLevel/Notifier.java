package ElevatorController.LowerLevel;

import Bus.SoftwareBus;
import Bus.SoftwareBusCodes;
import ElevatorController.Util.FloorNDirection;
import Message.Message;

/**
 * The notifier object is used to communicate all necessary visual and audio
 * information. The notifier sends messages to the speakers, button lights, and
 * floor display (up/down arrows and LEDs for displaying the floor number). The
 * notifier object does not receive any messages from the Software Bus.
 */
public class Notifier {
    private final int ELEVATOR_ID;
    private final SoftwareBus softwareBus;

    // ***Topics ***
    // To MUX
    private static final int TOPIC_CABIN_POSITION = SoftwareBusCodes.cabinPosition;
    private static final int TOPIC_DISPLAY_DIRECTION =SoftwareBusCodes.displayDirection;
    private static final int TOPIC_PLAY_SOUND = SoftwareBusCodes.playSound;

    // To Command Center
    private static final int TOPIC_COMMAND_CENTER =
            SoftwareBusCodes.elevatorStatus;
    private static final int TOPIC_COMMAND_DIRECTION =
            SoftwareBusCodes.ccElevatorDirection;

    //bodies
    private static final int BODY_ARRIVAL = SoftwareBusCodes.arrivalNoise;
    private static final int BODY_OVERLOAD = SoftwareBusCodes.overloadNoise;

    public  Notifier(int elevatorID, SoftwareBus softwareBus){
        this.ELEVATOR_ID = elevatorID;
        this.softwareBus = softwareBus;
    }

    /**
     * Notify Control Center and MUX of elevator status (arrived => play arrival
     * chime)
     * @param floorNDirection This elevator's current floor and direction
     */
    public void arrivedAtFloor(FloorNDirection floorNDirection){
        softwareBus.publish(new Message(TOPIC_PLAY_SOUND, ELEVATOR_ID, BODY_ARRIVAL));
    }

    /**
     * Notify Control Center and MUX of this elevator's status
     * @param floorNDirection This elevator's floor and direction
     */
    public void elevatorStatus(FloorNDirection floorNDirection){
        //Tell display direction
        int direction = floorNDirection.getDirection().getIntegerVersion();

//        // Tell mux what direction we're going
//        softwareBus.publish(new Message(TOPIC_DISPLAY_DIRECTION, ELEVATOR_ID,direction));
//
//        //Tell mux where we are
//        softwareBus.publish(new Message(TOPIC_CABIN_POSITION, ELEVATOR_ID, floorNDirection.floor()));

        //Tell CC where we are
        softwareBus.publish(new Message(TOPIC_COMMAND_CENTER, ELEVATOR_ID,
                floorNDirection.floor()));

        softwareBus.publish(new Message(TOPIC_COMMAND_DIRECTION, ELEVATOR_ID,
                direction));
    }

    /**
     * Notify the MUX to play the capacity buzzer
     */
    public void playCapacityNoise(){
        softwareBus.publish(new Message(TOPIC_PLAY_SOUND, ELEVATOR_ID,
                BODY_OVERLOAD));
    }

    /**
     * Notify the MUX to stop playing the capacity buzzer
     */
    public void stopCapacityNoise(){
        // NEVER HAPPENS, capacity nose plays for a set duration and stops on its own
    }

}
