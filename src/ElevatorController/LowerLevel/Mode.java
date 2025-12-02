package ElevatorController.LowerLevel;

import Bus.SoftwareBus;
import Bus.SoftwareBusCodes;
import ElevatorController.Util.FloorNDirection;
import ElevatorController.Util.State;
import Message.Message;
import Message.MessageHelper;

/**
 * The mode serves as a means for the Elevator Controller to be put into and track its current mode.
 * The mode is indirectly being updated by the Control Room, a separate entity outside of the Elevator Controller system.
 * Additionally, the mode is responsible for taking in demands from the Control Room when the elevator is being remotely controlled.
 * The mode object receives messages via the software bus but does not post messages to the software bus.
 */
public class Mode {
    private final int ELEVATOR_ID;
    private final SoftwareBus softwareBus;
    private State currentMode;
    private FloorNDirection currDestination;

    // *** Topic Constants ***
    // From Command Center to Mode
    private static final int TOPIC_ON_OFF = SoftwareBusCodes.elevatorOnOff;
    //TODO: do we need to handle a SYSTEM_RESET message?
    private static final int TOPIC_MODE = SoftwareBusCodes.setMode;
    private static final int TOPIC_DESTINATION =
            SoftwareBusCodes.setDestination;
    // From Mode to Command Center
    private static final int TOPIC_FIRE_MODE = SoftwareBusCodes.fireMode;

    // From MUX to Mode
    private static final int TOPIC_FIRE_ALARM =
            SoftwareBusCodes.fireAlarmActive;
    // From Mode to MUX
    private static final int TOPIC_SET_FIRE = SoftwareBusCodes.fireAlarm;

    // Body for mode changes
    private static final int BODY_CENTRALIZED_MODE  = SoftwareBusCodes.centralized;
    private static final int BODY_NORMAL_MODE = SoftwareBusCodes.normal;
    private static final int BODY_FIRE_MODE = SoftwareBusCodes.fire; //TODO: safe to delete this?

    /**
     * Instantiate a Mode object
     * @param elevatorID which elevator this Mode object is associated with
     *                   (for software bus messages)
     * @param softwareBus the means of communication
     */
    public Mode(int elevatorID, SoftwareBus softwareBus) {
        this.softwareBus = softwareBus;
        this.ELEVATOR_ID = elevatorID;

        this.currDestination = null;

        // Initially in Normal mode
        this.currentMode = State.NORMAL;

        // Subscribe to relevant topics, subtopic is elevatorID
        softwareBus.subscribe(TOPIC_ON_OFF, elevatorID);
        softwareBus.subscribe(TOPIC_MODE, elevatorID);
        softwareBus.subscribe(TOPIC_DESTINATION, elevatorID);
        softwareBus.subscribe(TOPIC_FIRE_ALARM, elevatorID);
    }

    /**
     * Call get() on softwareBus w/ appropriate topic/subtopic, until NULL is returned (only care about most recent mode
     * set), store last valid mode in currentMode, return currentMode
     * @return the currentMode this elevator is in
     */
    public State getMode(){
        setCurrentMode();
        return currentMode;
    }

    /**
     * Pulls all related messages from softwareBUs until null and
     * sets current mode equal to the last relevant message
     */
    private void setCurrentMode(){
        // From the Command Center
        Message modeMessage =  MessageHelper.pullAllMessages(softwareBus, ELEVATOR_ID, TOPIC_MODE);

        // From the MUX, was the fire alarm pulled?
        Message fireMessage =  MessageHelper.pullAllMessages(softwareBus, ELEVATOR_ID, TOPIC_FIRE_ALARM);

        // From the Command Center, On/Off
        Message onOffMessage = MessageHelper.pullAllMessages(softwareBus, ELEVATOR_ID, TOPIC_ON_OFF);


        int state;
        if(modeMessage!=null){
            state = modeMessage.getBody();
            switch (state){
                case BODY_CENTRALIZED_MODE -> currentMode = State.CONTROL;
                case BODY_NORMAL_MODE -> currentMode = State.NORMAL;
            }
        }


        //TODO: do we have to send the following message to the MUX?
        //Jackie: Also, don't worry about sending Fire Alarm to each elevator
        // when fire mode is activated by the pfd GUI specifically. The building
        // mux handles that automatically. They will need to be sent if you're doing
        // it via the control center, however @Val
        if(fireMessage!=null){
            state = fireMessage.getBody();
            if (state == SoftwareBusCodes.pulled){
                softwareBus.publish(new Message(TOPIC_FIRE_MODE, ELEVATOR_ID,
                        SoftwareBusCodes.emptyBody));
                currentMode = State.FIRE;
            }
        }

        if(onOffMessage!=null){
            state = onOffMessage.getBody();
            if (state == SoftwareBusCodes.off){
                currentMode = State.OFF;
            }
        }


        // Notify the MUX that the fire is active
        if (currentMode == State.FIRE){
            softwareBus.publish(new Message(TOPIC_SET_FIRE, ELEVATOR_ID,
                    SoftwareBusCodes.emptyBody));
            softwareBus.publish(new Message(TOPIC_SET_FIRE, SoftwareBusCodes.buildingMUX, SoftwareBusCodes.emptyBody));
        }
    }

    /**
     * Call get() on softwareBus w/ appropriate topic/subtopic,
     * @return next floor to travel to (no direction)
     */
    public FloorNDirection nextService(){
        int floor;


        Message message = MessageHelper.pullAllMessages(softwareBus, ELEVATOR_ID, TOPIC_DESTINATION);

        if (message != null){

            // Update next floor service based on message
            floor = message.getBody();
            System.out.println("Please take me to floor "+floor);
        } else {
            return currDestination;
        }

        // Update currDestination based on Message
        currDestination = new FloorNDirection(floor, null);
        return currDestination;
    }
}
