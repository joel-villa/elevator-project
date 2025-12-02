package ElevatorController.LowerLevel;

import Bus.SoftwareBus;
import Bus.SoftwareBusCodes;
import ElevatorController.Util.Direction;
import ElevatorController.Util.FloorNDirection;
import Message.*;

import java.util.ArrayList;
import java.util.List;

/**
 * The buttons object enables the Elevator Controller to track and schedule its destinations. The buttons object
 * indirectly receives floor requests via the physical buttons on the panel inside the cabin, as well as the call
 * buttons on each level. These button events are being received via the software bus.
 * The buttons object does not post any messages to the Software Bus.
 */
public class Buttons {
    private boolean callEnabled;
//    private boolean multipleRequests;
    private final int ELEVATOR_ID;
    private final List<FloorNDirection> destinations;
    private final SoftwareBus softwareBus;
    private int currFloor;
    private Direction currDirection;
    private boolean fireKey = false;
    private RequestType requestType;

    //  *** Software Bus Topics ***
    // Receiving from MUX
    private final static int TOPIC_HALL_CALL = SoftwareBusCodes.hallCall; // buttons in the halls
    private final static int TOPIC_CABIN_SELECT = SoftwareBusCodes.cabinSelect; // button events in the cabin
    private final static int TOPIC_FIRE_KEY = SoftwareBusCodes.fireKey;

    //Sending to MUX
    private final static int TOPIC_RESET_CALL = SoftwareBusCodes.resetCall;
    private final static int RESET_FLOOR_SELECTION = SoftwareBusCodes.resetFloorSelection;
    private final static int TOPIC_CALLS_ENABLED = SoftwareBusCodes.callsEnable;
    private final static int TOPIC_REQS_ENABLED =
            SoftwareBusCodes.selectionsEnable;
    private final static int TOPIC_SELECTION_TYPE =
            SoftwareBusCodes.selectionsType;

    //Subtopic for sending to Building MUX
    private final static int SUBTOPIC_BUILD_MUX = SoftwareBusCodes.buildingMUX;

    // Bodies for the fire key
    private final static int BODY_F_KEY_ACTIVE   = SoftwareBusCodes.active;
    private final static int BODY_F_KEY_INACTIVE = SoftwareBusCodes.inactive;


    /**
     * Instantiate a Buttons Object
     * @param elevatorID the elevator number associated with this Buttons Object
     *                   (for software bus messages)
     * @param softwareBus the means of communication
     */
    public Buttons(int elevatorID, SoftwareBus softwareBus) {
        switch (elevatorID) {
            case 1, 2, 3, 4:
                break;
            default:
                System.out.println("ERROR: Invalid elevator ID");
        }

        // Assuming normal mode settings initially
        this.callEnabled = true;
//        this.multipleRequests = true;
        this.requestType = RequestType.MULTI_SELECT;

        this.destinations = new ArrayList<>();
        this.softwareBus = softwareBus;
        this.ELEVATOR_ID = elevatorID;

        // Subscribing
        softwareBus.subscribe(TOPIC_CABIN_SELECT, elevatorID);
        softwareBus.subscribe(TOPIC_HALL_CALL, elevatorID);
        softwareBus.subscribe(TOPIC_FIRE_KEY, elevatorID);


    }

    /**
     * Call publish on the softwareBus with a message that the call button of the given floor, and given direction can be
     * turned off
     * Remove that floor from destinations
     * @param floorNDirection The call button and direction which is no longer relevant
     */
    public void callReset(FloorNDirection floorNDirection) {
        if (floorNDirection == null) return;
        if(!destinations.contains(floorNDirection)) return;

        //Cabin Button Reset
        if (floorNDirection.direction() == null) {
            softwareBus.publish(new Message(RESET_FLOOR_SELECTION, ELEVATOR_ID, floorNDirection.floor()));
            destinations.remove(floorNDirection);
            return;
        }

        //Floor Button Reset

        switch(floorNDirection.floor()){
//            case UP -> softwareBus.publish(new Message(TOPIC_RESET_CALL, SUBTOPIC_BUILD_MUX, 0));
//            case DOWN -> softwareBus.publish(new Message(TOPIC_RESET_CALL, SUBTOPIC_BUILD_MUX, 1));
//            // if direction is not up or down handle with grace!
//            default -> throw new IllegalStateException("Unexpected value: " + floorNDirection.direction());
            case 1 -> {
                switch(floorNDirection.direction()) {
                    case UP -> softwareBus.publish(new Message(TOPIC_RESET_CALL, SUBTOPIC_BUILD_MUX, SoftwareBusCodes.reset1Up));
                    case DOWN -> softwareBus.publish(new Message(TOPIC_RESET_CALL, SUBTOPIC_BUILD_MUX, SoftwareBusCodes.reset1Down));
                    // if direction is not up or down handle with grace!
                    default -> throw new IllegalStateException("Unexpected value: " + floorNDirection.direction());
                }
            }
            case 2 -> {
                switch(floorNDirection.direction()) {
                    case UP -> softwareBus.publish(new Message(TOPIC_RESET_CALL, SUBTOPIC_BUILD_MUX, SoftwareBusCodes.reset2Up));
                    case DOWN -> softwareBus.publish(new Message(TOPIC_RESET_CALL, SUBTOPIC_BUILD_MUX, SoftwareBusCodes.reset2Down));
                    // if direction is not up or down handle with grace!
                    default -> throw new IllegalStateException("Unexpected value: " + floorNDirection.direction());
                }
            }
            case 3 -> {
                switch(floorNDirection.direction()) {
                    case UP -> softwareBus.publish(new Message(TOPIC_RESET_CALL, SUBTOPIC_BUILD_MUX, SoftwareBusCodes.reset3Up));
                    case DOWN -> softwareBus.publish(new Message(TOPIC_RESET_CALL, SUBTOPIC_BUILD_MUX, SoftwareBusCodes.reset3Down));
                    // if direction is not up or down handle with grace!
                    default -> throw new IllegalStateException("Unexpected value: " + floorNDirection.direction());
                }
            }
            case 4 -> {
                switch(floorNDirection.direction()) {
                    case UP -> softwareBus.publish(new Message(TOPIC_RESET_CALL, SUBTOPIC_BUILD_MUX, SoftwareBusCodes.reset4Up));
                    case DOWN -> softwareBus.publish(new Message(TOPIC_RESET_CALL, SUBTOPIC_BUILD_MUX, SoftwareBusCodes.reset4Down));
                    // if direction is not up or down handle with grace!
                    default -> throw new IllegalStateException("Unexpected value: " + floorNDirection.direction());
                }
            }
            case 5 -> {
                switch(floorNDirection.direction()) {
                    case UP -> softwareBus.publish(new Message(TOPIC_RESET_CALL, SUBTOPIC_BUILD_MUX, SoftwareBusCodes.reset5Up));
                    case DOWN -> softwareBus.publish(new Message(TOPIC_RESET_CALL, SUBTOPIC_BUILD_MUX, SoftwareBusCodes.reset5Down));
                    // if direction is not up or down handle with grace!
                    default -> throw new IllegalStateException("Unexpected value: " + floorNDirection.direction());
                }
            }
            case 6 -> {
                switch(floorNDirection.direction()) {
                    case UP -> softwareBus.publish(new Message(TOPIC_RESET_CALL, SUBTOPIC_BUILD_MUX, SoftwareBusCodes.reset6Up));
                    case DOWN -> softwareBus.publish(new Message(TOPIC_RESET_CALL, SUBTOPIC_BUILD_MUX, SoftwareBusCodes.reset6Down));
                    // if direction is not up or down handle with grace!
                    default -> throw new IllegalStateException("Unexpected value: " + floorNDirection.direction());
                }
            }
            case 7 -> {
                switch(floorNDirection.direction()) {
                    case UP -> softwareBus.publish(new Message(TOPIC_RESET_CALL, SUBTOPIC_BUILD_MUX, SoftwareBusCodes.reset7Up));
                    case DOWN -> softwareBus.publish(new Message(TOPIC_RESET_CALL, SUBTOPIC_BUILD_MUX, SoftwareBusCodes.reset7Down));
                    // if direction is not up or down handle with grace!
                    default -> throw new IllegalStateException("Unexpected value: " + floorNDirection.direction());
                }
            }
            case 8 -> {
                switch(floorNDirection.direction()) {
                    case UP -> softwareBus.publish(new Message(TOPIC_RESET_CALL, SUBTOPIC_BUILD_MUX, SoftwareBusCodes.reset8Up));
                    case DOWN -> softwareBus.publish(new Message(TOPIC_RESET_CALL, SUBTOPIC_BUILD_MUX, SoftwareBusCodes.reset8Down));
                    // if direction is not up or down handle with grace!
                    default -> throw new IllegalStateException("Unexpected value: " + floorNDirection.direction());
                }
            }
            case 9 -> {
                switch(floorNDirection.direction()) {
                    case UP -> softwareBus.publish(new Message(TOPIC_RESET_CALL, SUBTOPIC_BUILD_MUX, SoftwareBusCodes.reset9Up));
                    case DOWN -> softwareBus.publish(new Message(TOPIC_RESET_CALL, SUBTOPIC_BUILD_MUX, SoftwareBusCodes.reset9Down));
                    // if direction is not up or down handle with grace!
                    default -> throw new IllegalStateException("Unexpected value: " + floorNDirection.direction());
                }
            }
            case 10 -> {
                switch(floorNDirection.direction()) {
                    case UP -> softwareBus.publish(new Message(TOPIC_RESET_CALL, SUBTOPIC_BUILD_MUX, SoftwareBusCodes.reset10Up));
                    case DOWN -> softwareBus.publish(new Message(TOPIC_RESET_CALL, SUBTOPIC_BUILD_MUX, SoftwareBusCodes.reset10Down));
                    // if direction is not up or down handle with grace!
                    default -> throw new IllegalStateException("Unexpected value: " + floorNDirection.direction());
                }
            }
            default -> throw new IllegalStateException("Unexpected value: " + floorNDirection.direction());
        }
        destinations.remove(floorNDirection);
    }

    /**
     * Call publish on softwareBus with a message that the call button on the given floor, and given direction can be
     * turned off
     * Remove that floor from destinations
     * @param floor the floor request button that is no longer relevant
     */
    public void requestReset(int floor) {
        // CALL BUTTONS: are the hall buttons
        // we may want to consider keeping track of what buttons are on with an array of booleans
        // this could reduce clutter so we only call publish if the array contains true at the index of the floor
        softwareBus.publish(new Message(TOPIC_RESET_CALL, ELEVATOR_ID, floor));
    }

    /**
     * In normal mode, level call buttons are enabled
     */
    public void enableCalls(){
        // ignore this call if calls are already enabled
        if (callEnabled){
            return;
        }

        // Publish to MUX
        softwareBus.publish(new Message(TOPIC_CALLS_ENABLED,
                SoftwareBusCodes.buildingMUX,
                SoftwareBusCodes.on));

        // set local variable
        this.callEnabled = true;
    }

    /**
     * In fire mode, and controlled mode call buttons are disabled
     */
    public void disableCalls(){
        // Do nothing if calls already enabled
        if (!callEnabled){
            return;
        }

        // Notify MUX
        softwareBus.publish(new Message(TOPIC_CALLS_ENABLED, SoftwareBusCodes.buildingMUX,
                SoftwareBusCodes.off));

        // Update local
        this.callEnabled = false;
    }

    /**
     * In Normal mode, all request buttons are enabled
     */
    public void enableAllRequests(){
        //if request type is already all, do nothing
        if (requestType == RequestType.MULTI_SELECT){
            return;
        }

        // Notify MUX
        softwareBus.publish(new Message(TOPIC_REQS_ENABLED, ELEVATOR_ID,
                SoftwareBusCodes.on));
        softwareBus.publish(new Message(TOPIC_SELECTION_TYPE, ELEVATOR_ID,
                SoftwareBusCodes.multiple));

        // Set local variable
//        this.multipleRequests = true;
        requestType = RequestType.MULTI_SELECT;
    }

    /**
     * In Fire mode, the request buttons in the cabin are mutually exclusive
     */
    public void enableSingleRequest(){
        // If request type is already mutually exclusive, do nothing
        if(requestType == RequestType.MUTUALLY_EXCLUSIVE){
            return;
        }

        // Notify MUX
        softwareBus.publish(new Message(TOPIC_REQS_ENABLED, ELEVATOR_ID,
                SoftwareBusCodes.on));
        softwareBus.publish(new Message(TOPIC_SELECTION_TYPE, ELEVATOR_ID,
                SoftwareBusCodes.single));

        // Set local variable
//        this.multipleRequests = false;
        requestType = RequestType.MUTUALLY_EXCLUSIVE;
    }

    /**
     * Disable request buttons in the cabin
     * Note: should be called when...
     *       - elevator gets turned off
     *       - in fire mode, before the fire key is inserted (or when the fire
     *         key is removed)
     *       - in command center mode
     */
    public void disableRequests(){
        // If request buttons are already disabled, do nothing
        if (requestType == RequestType.DISABLED){
            return;
        }

        // Send message to MUX
        softwareBus.publish(new Message(TOPIC_REQS_ENABLED, ELEVATOR_ID, SoftwareBusCodes.off));

        // Set local variable
        requestType = RequestType.DISABLED;
    }

    /**
     * Is the fire key inserted?
     * @return True if it is, False otherwise
     */
    public boolean fireKeyInserted(){
        handleFireKey();
        return fireKey;
    }

    /*
     * Note; call events have associated directions, and request events do not
     *
     * If multipleRequests enabled, keep calling get() on software bus, add them to associated lists, use given
     * floorNDirection to determine which is best.
     *
     * If multipleRequests are disabled, keep calling get() on software bus, ignore all but the most recent.
     *
     * If calls are disabled, ignore all call button presses (those associated with level)
     *
     * @param floorNDirection record holding the floor and direction
     * @return next service direction and floor (direction non-null for call buttons, null for requests)
     */
    public FloorNDirection nextService(FloorNDirection floorNDirection) {
        // Helper calls
        handleCabinSelect();
        handleHallCall();
//        handleFireKey();

        currDirection = floorNDirection.direction();
        currFloor = floorNDirection.floor();

        // Calls disabled case
        if (!callEnabled && !fireKey) return null;

        // Requests disabled case
        if (requestType == RequestType.DISABLED) {
            //TODO: is there ever a time when requests (in cabin) are disabled,
            // but call buttons are enabled? I don't think so...
            return null;
        }

        if (requestType == RequestType.MUTUALLY_EXCLUSIVE) {
            // Get the next service if the request type is mutually exclusive
            if (!destinations.isEmpty()){
                // Can only get first element of non-empty list
                FloorNDirection nextService = destinations.getFirst();
                destinations.clear();
                destinations.add(nextService); //TODO: this seems incorrect?
                return nextService;
            }
            // no nextService -> return null
            return null;
        }

        //Determine floors not on the way
        List<FloorNDirection> unreachable = new ArrayList<>();
        if(destinations.isEmpty()){
            return null;
        }
        int currServiceFloor = destinations.getFirst().getFloor();

        for (FloorNDirection fd : destinations) {
            //Service incompatible
            boolean belowDown =
                    fd.floor() < currServiceFloor && fd.direction() != null && fd.direction() == Direction.DOWN;
            boolean aboveUp =
                    fd.floor() > currServiceFloor && fd.direction() != null && fd.direction() == Direction.UP;
            //Floor incompatible
            boolean belowUp =
                    fd.floor() < currFloor && fd.direction() == Direction.UP;
            boolean aboveDown =
                    fd.floor() > currFloor && fd.direction() == Direction.DOWN;
            if (belowDown || aboveUp || belowUp || aboveDown) {
                unreachable.add(fd);
            }
        }
        //Remove unreachable floors from queue temporarily
        for (FloorNDirection fd : unreachable) {
            destinations.remove(fd);
        }
        // Determines if list is sorted increasing or decreasing
        int inticator = 0;
        // sort increasing
        if (currDirection == Direction.UP) inticator = 1;
        // sort decreasing
        else if (currDirection == Direction.DOWN) inticator = -1;

        //If not moving, go to the most recently called floor
        if (destinations.isEmpty()){
            //Don't getFirst() on empty destinations
            return null;
        }
        if (inticator == 0) return destinations.getFirst();
        //The humble bubble sort glorious! <- so hot! wowowowow!!
        for (int i = 0; i < destinations.size(); i++) {
            for (int j = 0; j < destinations.size(); j++) {
                if (i == j) continue;
                if (destinations.get(i).floor() * inticator > destinations.get(j).floor() * inticator) {
                    FloorNDirection temp = destinations.get(i);
                    destinations.set(i,destinations.get(j));
                    destinations.set(j,temp);
                }
            }
        }

        //re-add unreachable destinations
        destinations.addAll(unreachable);

        return destinations.getFirst();
    }
    /**
     * Get the fire key message from the MUX
     */
    private void handleFireKey() {
        // Most recent fire key message
        Message message = MessageHelper.pullAllMessages(softwareBus, ELEVATOR_ID, TOPIC_FIRE_KEY);
        if (message != null) {
            // If a message exist, use it to update the local fire key variable
            switch (message.getBody()){
                case BODY_F_KEY_ACTIVE -> {
                    fireKey = true;
                    System.out.println("fire key active in Buttons of Elevator " + ELEVATOR_ID ); //TODO delete (for debugging)
                }
                case BODY_F_KEY_INACTIVE -> {
                    fireKey = false;
                    System.out.println("fire key inactive in Buttons of Elevator " + ELEVATOR_ID ); //TODO delete (for debugging)
                }
                default -> {
                    fireKey = false;
                    System.out.println("Unexpected Body in Buttons, TOPIC_FIRE_KEY, body = " + message.getBody());
                }
            }
        }
    }

    /**
     *  Get the cabin selection button presses from MUX
     */
    private void handleCabinSelect() {
        Message message = softwareBus.get(TOPIC_CABIN_SELECT,ELEVATOR_ID);
        while(message!=null){
            int floor = message.getBody();
            System.out.println("adding to destinations: "+floor);
            destinations.add(new FloorNDirection(floor, null));
            message=softwareBus.get(TOPIC_CABIN_SELECT,ELEVATOR_ID);
        }
    }

    /**
     *  get the hall call button presses from MUX
     */
    private void handleHallCall() {
        Message message = MessageHelper.pullAllMessages(softwareBus, ELEVATOR_ID, TOPIC_HALL_CALL);
        if(message==null){
            return;
        }
        int floor;
        int destCode = message.getBody();
        FloorNDirection fd;

        /*
         *  destCode = 1 to 10    -> down calls on that level
         *  destCode = 101 to 110 -> up calls on that level
         */
        if (destCode >=100) {
            // Subtract 100 to get floor
            floor = destCode - SoftwareBusCodes.upOffset;

            // Direction is UP
            fd = new FloorNDirection(floor, Direction.UP);
        } else {
            // Subtracting 0
            floor = destCode - SoftwareBusCodes.downOffset;

            // Direction is DOWN
            fd = new FloorNDirection(floor, Direction.DOWN);
        }
        if (floor < 1 || floor > 10) {
            // Unexpected floor, print error message
            System.out.println("ERROR in Buttons of Elevator " + ELEVATOR_ID +
                    ", floor = " + floor + ", destCode = " + destCode);
        }
        destinations.add(fd);
    }

    /**
     * Request buttons in the cabin can be in one of three logical states
     */
    private enum RequestType {
        DISABLED,
        MUTUALLY_EXCLUSIVE,
        MULTI_SELECT
    }
}
