package ElevatorController.LowerLevel;

import Bus.SoftwareBus;
import Bus.SoftwareBusCodes;
import ElevatorController.Util.ConstantsElevatorControl;
import ElevatorController.Util.Direction;
import ElevatorController.Util.FloorNDirection;
import ElevatorController.Util.Timer;
import Message.Message;
import Message.MessageHelper;

/**
 * The cabin provides a means for the elevator controller to send the elevator to a destination.
 * The cabin indirectly controls the motor by sending messages to the Software Bus.
 * Additionally, the cabin indirectly receives messages from physical sensors through the Software Bus.
 */
public class Cabin implements Runnable {
    private int currDest;
    private Direction currDirection;
    private int ELEVATOR_ID;
    private int currFloor;
    private int topAlign;
    private int botAlign;
    private boolean motor;
    private SoftwareBus softwareBus;

    // *** Constants for cabin topic ***
    // Sending to the MUX
    private static final int TOPIC_CAR_STOP = SoftwareBusCodes.carStop;
    private static final int TOPIC_CAR_DISPATCH = SoftwareBusCodes.carDispatch;

    // Recieving from the MUX
    private static final int TOPIC_TOP_FLOOR_SENSOR = SoftwareBusCodes.topSensor;
    private static final int TOPIC_BOTTOM_SENSOR = SoftwareBusCodes.bottomSensor;

    //Constants for cabin bodies
    private static final int STOP_MOTOR = 0;
    private static final int START_MOTOR = 1;

    /**
     * Instanciate the Cabin
     * @param elevatorID the elevator associated with this cabin (1 to 4)
     * @param softwareBus the softwareBus associated with the entire system
     */
    public Cabin(int elevatorID, SoftwareBus softwareBus){
        switch (elevatorID) {
            case 1, 2, 3, 4:
                break;
            default:
                System.out.println("ERRROR: Invalid elevator ID");
        }
        this.softwareBus = softwareBus;
        this.currDest = 0;
        this.currDirection = Direction.STOPPED;
        this.ELEVATOR_ID = elevatorID;

        //Subscribing
        softwareBus.subscribe(TOPIC_TOP_FLOOR_SENSOR, ELEVATOR_ID);
        softwareBus.subscribe(TOPIC_BOTTOM_SENSOR, ELEVATOR_ID);

        //Start Cabin Thread
        Thread thread = new Thread(this);
        thread.start();
    }

    /**
     * Run the Cabin
     */
    @Override
    public void run() {
        while (true) {
            stepTowardsDest();
            System.out.printf(""); // <- why?
        }
    }

    /**
     * sets the direction and the destination
     * @param floor the target floor
     */
    public void gotoFloor(int floor){
        if(floor > currFloor) currDirection = Direction.UP;
        else if (floor < currFloor) currDirection = Direction.DOWN;
        else currDirection = Direction.STOPPED;
        currDest = floor;
    }

    /**
     * @return the floor and direction of the elevator
     */
    public FloorNDirection currentStatus(){
        return new FloorNDirection(currFloor,currDirection);}

    /**
     * @return true if the elevator has arrived at its destination
     */
    public boolean arrived(){return currFloor == currDest;}

    /**
     * @return the current target floor
     */
    public int getTargetFloor(){return currDest;}


    // Internal methods

    private Timer timeToStop;

    /**
     * Main thread method, used to step towards a target floor
     */
    private synchronized void stepTowardsDest() {
        //Update alignment
        topAlignment();
        bottomAlignment();
        updateCurrFloor();
        //Last sensor before stop
        boolean almostThere;
        if (currDirection == Direction.DOWN) almostThere = sensorToFloor(topAlign) == currDest;
        else almostThere = sensorToFloor(botAlign) == currDest;
        //System.out.println(motor + " "+almostThere);
        //Should time stop
        if (motor && almostThere) {
            //Time to stop!
            if (timeToStop != null && timeToStop.timeout()) stopMotor();
            //Determine time to stop
            else if (timeToStop == null) timeToStop = timeStop();
        } else if (!motor && currFloor != currDest){
            //Turn motor on if needed
            if (currFloor > currDest) currDirection = Direction.DOWN;
            else if (currFloor!=currDest) currDirection = Direction.UP;
            startMotor(currDirection);
        } else {
            //Reset time to stop
            timeToStop = null;
        }
    }

    /**
     * Updates the correct time to stop the motor based on constants file
     * @return a timer that times out when it's time to stop
     */
    private Timer timeStop() {
        return new Timer(ConstantsElevatorControl.TIME_TO_STOP);
    }


    /**
     * Translates sensor to floor
     * @param sensorPos a sensor position to a floor #
     * @return a floor number 1-20
     */
    private int sensorToFloor(int sensorPos) {
        return sensorPos/2 + 1;
    }


    //Wrapper methods for software bus messages
    private void startMotor(Direction direction) {
        //We set the direction number based on current mux 11/23/2025
        motor = true;
        int dir = -1;
        switch (direction) {
            case UP -> dir = SoftwareBusCodes.up;
            case DOWN -> dir = SoftwareBusCodes.down;
            default -> {
                System.out.println("ERROR: Cabin " + ELEVATOR_ID +
                        " calling startMotor() with invalid direction");
            }
        }

        softwareBus.publish(new Message(TOPIC_CAR_DISPATCH, ELEVATOR_ID, dir));
    }

    /**
     * Send message to MUX to stop the motor
     */
    private void stopMotor() {
//        System.out.println("AHAHA IM STOPPJINGNGGDSLKAKLDSAJKDSAJKDKASJKJDASKJDASKJDJAKJASDKJDSALKJghsajkdsb");
        motor = false;
        softwareBus.publish(new Message(TOPIC_CAR_STOP, ELEVATOR_ID, STOP_MOTOR));
    }

    /**
     * Update topAlign variable via messages from the MUX
     */
    private void topAlignment() {
       Message message = MessageHelper.pullAllMessages(softwareBus, ELEVATOR_ID, TOPIC_TOP_FLOOR_SENSOR);
       if (message != null) topAlign = message.getBody();
    }

    /**
     * Update botAlign variable via messages from the MUX
     */
    private void bottomAlignment() {
        Message message = MessageHelper.pullAllMessages(softwareBus, ELEVATOR_ID, TOPIC_BOTTOM_SENSOR);
        if (message != null) botAlign = message.getBody();
    }

    /**
     * Estimates current floor based on sensors
     * Rounds to next floor
     */
    private void updateCurrFloor() {
        switch (currDirection){
            case UP -> {
                // Going up -> current floor based on top of floor sensors
                currFloor = botAlign / 2 + 1;
            }
            case DOWN -> {
                // Going down -> current floor based on bottom of floor sensors
                currFloor = topAlign / 2 + 1;
            }
        }
    }
}