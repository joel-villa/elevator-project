package CommandCenter;

import Bus.SoftwareBus;
import Bus.SoftwareBusCodes;
import ElevatorController.Util.Direction;
import ElevatorController.Util.FloorNDirection;
import ElevatorController.Util.State;
import Message.MessageHelper;
import Message.Message;

import java.util.ArrayList;
import java.util.Arrays;

public class CommandCenter {

    private boolean[] elevatorEnabled={true,true,true,true};
    private final FloorNDirection startingFND = new FloorNDirection(1, Direction.STOPPED);
    private FloorNDirection[] floorNDirections={startingFND,startingFND,startingFND,startingFND};

    //Given to us by a startup
    public SoftwareBus bus;

    /**
     * Sent by us to elevator control
     */

    private static final int SET_MODE= SoftwareBusCodes.setMode;


    private static final int TURN_ELEVATOR_ON_OFF=SoftwareBusCodes.elevatorOnOff;


    private static final int SERVICE_MESSAGE = SoftwareBusCodes.setDestination;


    /**
     * Sent to us by elevator controller
     */

    private static final int GET_ELEVATOR_STATUS = SoftwareBusCodes.elevatorStatus;


    public static final int GET_MODE = SoftwareBusCodes.fireMode;
    public static final int GET_FIRE_ALARM_STATUS = SoftwareBusCodes.fireAlarmActive;

    private static final int GET_DOOR_STATUS = SoftwareBusCodes.doorStatusCC;

    private static final int GET_DIRECTION = SoftwareBusCodes.ccElevatorDirection;


    private State currMode = State.NORMAL;

    public CommandCenter(SoftwareBus bus){
        this.bus=bus;
        bus.subscribe(GET_MODE,1);
        bus.subscribe(GET_FIRE_ALARM_STATUS, 0);
        bus.subscribe(GET_ELEVATOR_STATUS, 1);
        bus.subscribe(GET_DOOR_STATUS,1);
        bus.subscribe(GET_DIRECTION, 1);
        bus.subscribe(GET_MODE,2);
        bus.subscribe(GET_FIRE_ALARM_STATUS, 2);
        bus.subscribe(GET_ELEVATOR_STATUS, 2);
        bus.subscribe(GET_DOOR_STATUS,2);
        bus.subscribe(GET_DIRECTION, 2);
        bus.subscribe(GET_MODE,3);
        bus.subscribe(GET_FIRE_ALARM_STATUS, 3);
        bus.subscribe(GET_ELEVATOR_STATUS, 3);
        bus.subscribe(GET_DOOR_STATUS,3);
        bus.subscribe(GET_DIRECTION, 3);
        bus.subscribe(GET_MODE,4);
        bus.subscribe(GET_FIRE_ALARM_STATUS, 4);
        bus.subscribe(GET_ELEVATOR_STATUS, 4);
        bus.subscribe(GET_DOOR_STATUS,4);
        bus.subscribe(GET_DIRECTION, 4);

    }

    /**
     * Publishes starts single elevator
     */
    public void enableSingleElevator(int elevatorId){
        System.out.println("In command center; Turning on elevator: "+elevatorId);
        elevatorEnabled[elevatorId-1]=true;
        // TODO: make this message actually work
        bus.publish(new Message(TURN_ELEVATOR_ON_OFF,elevatorId,1)); //turn elevator on
    }

    /**
     * Returns if this elevator is enabled
     * @param elevatorId the elevator in questions
     * @return true or false
     */

    public boolean elevatorOn(int elevatorId){
        return elevatorEnabled[elevatorId-1];
    }

    /**
     * Publishes start all elevators
     */
    public void enableElevator(){
        if (currMode == State.CONTROL) {
            Arrays.fill(elevatorEnabled, true);
            System.out.println("In command center; Turning on elevators");
            bus.publish(new Message(TURN_ELEVATOR_ON_OFF, 0, 1)); //Turn all elevators on
        } else {
            System.out.println("Hey now you're not in control mode!");
        }
    }

    /**
     * Due to the way the command center GUI is set up, we added the functionality
     * to stop/start single elevators
     * @param elevatorId the elevator to be stoped
     */
    public void disableSingleElevator(int elevatorId){
        if (currMode == State.CONTROL) {
            System.out.println("In command center; Turning off elevator: " + elevatorId);
            elevatorEnabled[elevatorId - 1] = false;
            bus.publish(new Message(TURN_ELEVATOR_ON_OFF, elevatorId, 0)); //turns elevator off
        } else {
            System.out.println("Hey now you're not in control mode!");
        }
    }

    /**
     * Stops all elevators
     */

    public void disableElevator(){
        if (currMode == State.CONTROL) {
            System.out.println("In command center; Turning off elevator: ");
            Arrays.fill(elevatorEnabled, false);
            bus.publish(new Message(TURN_ELEVATOR_ON_OFF, 0, 0)); //Turns all elevators off
        } else {
            System.out.println("Hey now you're not in control mode!");
        }
    }

    /**
     * Turns off fire message, sent to all elevators, with a dummy body
     */
    public void clearFireMessage() {
        System.out.println("In command center: clear fire message sent");
        bus.publish(new Message(SoftwareBusCodes.clearFire,0,0));
    }



    /**
     * Send mode message
     */
    public void sendModeMessage(int modeMessage){
        //currMode =
        System.out.println("In command center; mode message sent "+modeMessage);
        bus.publish(new Message(SET_MODE,1,modeMessage));
        bus.publish(new Message(SET_MODE,2,modeMessage));
        bus.publish(new Message(SET_MODE,3,modeMessage));
        bus.publish(new Message(SET_MODE,4,modeMessage));

    }

    /**
     * Send Service Message
     */
    public void sendServiceMessage(int elevatorID, int floor) {
        System.out.println("Sent service message for elevator: "+ elevatorID+ " to floor "+ floor);
        bus.publish(new Message(SERVICE_MESSAGE,elevatorID,floor));
    }
    /**
     * Gets a new mode from software bus if there is one, otherwise returns current mode
     */
    public State getMode() {
        //TODO: hard coded for fire alarm, would need to handle other cases if we are told to switch to other modes
        Message message=bus.get(GET_MODE,0);
        if (message != null) {
            currMode = State.FIRE;
        }
        if (bus.get(GET_FIRE_ALARM_STATUS, 0) != null) {
            System.out.println("hit aoiuwdhaoihwd");
            currMode = State.NORMAL;
        }
        return currMode;
    }

    public void toggleModes(){
        if (currMode == State.FIRE){
            return;
        }
        if(currMode==State.CONTROL){
            currMode=State.NORMAL;
        }else{
            currMode=State.CONTROL;
        }
    }

    /**
     * Returns the current elevator status
     * @param id of the elevator
     * @return current floor and motion of the elevator
     */
    public FloorNDirection getElevatorStatus(int id) {
        int floor;
        Direction dir = null;
        FloorNDirection floorNDirection;

        // Setting floor
        Message m = MessageHelper.pullAllMessages(bus, id, GET_ELEVATOR_STATUS);
        if (m == null) {
            // No change
            floor = floorNDirections[id - 1].getFloor();
        } else {
            System.out.println("litty changing  da floor");
            // New floor based off messages from sbus
            floor = m.getBody();
        }

        // Setting direction
        m = MessageHelper.pullAllMessages(bus, id, GET_DIRECTION);
        if (m == null) {
            // No change
            dir = floorNDirections[id-1].getDirection();
        } else {
            System.out.println("litty changing  da dir");
            // New direction from software bus
            int direction = m.getBody();

            switch (direction) {
                case SoftwareBusCodes.up -> dir = Direction.UP;
                case SoftwareBusCodes.down -> dir = Direction.DOWN;
                case SoftwareBusCodes.none -> dir = Direction.STOPPED;
                default -> System.out.println("unexpected body in command center");
            }
        }

        floorNDirection = new FloorNDirection(floor, dir);
        floorNDirections[id-1]=floorNDirection;
        return floorNDirection;
    }

    /**
     * Gets the door status of specified elevator
     * @param id of the elevator
     * @return 0 for open, 1 for closed, -1 error
     */
    public int getDoorStatus(int id) {
        int message = bus.get(GET_DOOR_STATUS,id).getBody();
        if (message == 0 || message == 1) return message;
        return -1;
    }

    public FloorNDirection getFloorNDirection(int id){
        return getElevatorStatus(id);
    }

}