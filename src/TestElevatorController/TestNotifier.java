package TestElevatorController;

import Bus.SoftwareBus;
import ElevatorController.LowerLevel.Notifier;
import ElevatorController.Util.Direction;
import ElevatorController.Util.FloorNDirection;
import Message.Message;
import Bus.SoftwareBusCodes;

/**
 * For testing the Notifier object in ElevatorController/LowerLevel
 */
public class TestNotifier {
    // Topic for updating car info
    private static final int TOPIC_DISPLAY_FLOOR = SoftwareBusCodes.displayFloor;
    private static final int TOPIC_DISPLAY_DIREC = SoftwareBusCodes.displayDirection;

    // SoftwareBusCodess for playing sounds
    private static final int TOPIC_SPEAKER = SoftwareBusCodes.playSound;

    // Body for play capacity noise
    private static final int BODY_CAPACITY = SoftwareBusCodes.overloaded;
    private static final int BODY_ARRIVAL  = SoftwareBusCodes.emptyBody;

    // software bus
    private static final SoftwareBus  BUS =  new SoftwareBus(true);
    private static final SoftwareBus  CLIENT_BUS =  new SoftwareBus(false);

    // notifier objects
    private final static Notifier NOTIFIER_1 = new Notifier(1, BUS);
    private static final Notifier NOTIFIER_2 = new Notifier(2, BUS);

    /**
     * For testing Notifier.arrivedAtFloor() and Notifier.elevatorStatus()
     * @return true if passed, false if failed
     */
    private static boolean test3Or4(boolean test3){
        int elevator = 2;
        if(test3){
            elevator = 1;
        }
        boolean passedTestsFlag = true;

        for (int i = 1; i < 33; i++) {
            Direction direction;
            int floor;
            int intDirection;

            // Setting direction based off mod 3
            if (i % 3 == 0){
                direction = Direction.DOWN;
            } else if (i % 3 == 1){
                direction = Direction.UP;
            } else {
                direction = Direction.STOPPED;
            }

            // Set floor number
            floor = i / 3;

            NOTIFIER_2.arrivedAtFloor(new FloorNDirection(floor, direction));
            Message message1 = BUS.get(TOPIC_DISPLAY_DIREC, elevator);
            Message message2 = BUS.get(TOPIC_DISPLAY_FLOOR, elevator);
            Message message3 = BUS.get(TOPIC_SPEAKER, elevator);

            // ding message
            if (!test3){
                // test four does not send ding message
            } else if (message3 != null) {
                if (message3.getBody() != BODY_ARRIVAL){
                    passedTestsFlag = false;
                    System.out.println("ERROR: sending wrong body for arrival ding, " +
                            "recieved body = " + message3.getBody() +
                            ", expecting body = " + BODY_ARRIVAL);
                    break; // done testing
                }
            } else {
                passedTestsFlag = false;
                System.out.println("ERROR: arrivedAtFloor() not sending correct " +
                        "topic/subtopic for dinging the elevator");
                break; // done testing
            }

            // direction display message
            if (message1 != null) {
                if (message1.getBody() != direction.getIntegerVersion()){
                    passedTestsFlag = false;
                    System.out.println("ERROR: arrivedAtFloor() not sending " +
                            "correct body for displaying direction, expecting body = "
                            +  direction.getIntegerVersion() + ", recieved body = "
                            + message1.getBody());
                    break; // done testing
                }
            } else {
                passedTestsFlag = false;
                System.out.println("ERROR: arrivedAtFloor() not sending correct" +
                        " topic/subtopic for updating cab direction display");
                break; // done testing
            }

            // floor display message
            if (message2 != null) {
                if (message2.getBody() != floor){
                    System.out.println("ERROR: arrivedAtFloor() not sending correct " +
                            "body for updating cab location display, recieved body = " +
                            message2.getBody() + ", expected body = " + floor);
                }
            } else {
                passedTestsFlag = false;
                System.out.println("ERROR: arrivedAtFloor() not sending correct " +
                        "topic/subtopic for updating cab location display");
                break; //done testing
            }
        }
        return passedTestsFlag;
    }

    /**
     * Run to test Notifier behavior
     */
    static void main() {
        Message message;
        boolean passedTestsFlag;
        int elevator;
        int testsPassed = 0;

        // TEST 1 playCapacityNoise()
        System.out.println("Test 1: playCapacityNoise()");
        elevator = 2;
        NOTIFIER_2.playCapacityNoise();
        message = BUS.get(TOPIC_SPEAKER, elevator);
        if (message != null) {
            passedTestsFlag = true;
            if (message.getTopic() != TOPIC_SPEAKER){
                System.out.println("ERROR: in software bus? getting from topic = "
                        + TOPIC_SPEAKER +", receiving topic = " + message.getTopic());
                passedTestsFlag = false;
            }
            if (message.getSubTopic() != elevator){
                System.out.println("ERROR: in software bus? getting from subtopic = "
                        + elevator + ", recieving from subtopic = " + message.getSubTopic());
                passedTestsFlag = false;
            }
            if (message.getBody() != BODY_CAPACITY){
                System.out.println("ERROR: sending wrong body, sending body = " +
                        message.getBody() + ", expecting body = " + BODY_CAPACITY);
                passedTestsFlag = false;
            }
            if (passedTestsFlag){
                // Passed test for playCapacity
                System.out.println("Test 1: passed");
                testsPassed++;
            }
        } else {
            System.out.println("ERROR: playCapacityNoise() not sending correct" +
                    " topic subtopic, expecting topic = "  + TOPIC_SPEAKER +
                    ", subtopic = " + elevator);
        }
        System.out.println();

        // TEST 2 stopCapacityNoise()
        System.out.println("Test 2: stopCapacityNoise()");
        elevator = 1;
        NOTIFIER_1.stopCapacityNoise();
        message =  BUS.get(TOPIC_SPEAKER, elevator);
        if (message != null) {
            passedTestsFlag = true;

            if (message.getTopic() != TOPIC_SPEAKER){
                System.out.println("Error in software bus? getting from topic = "
                        + TOPIC_SPEAKER +", receiving topic = " + message.getTopic());
                passedTestsFlag = false;
            }
            if (message.getSubTopic() != elevator){
                System.out.println("Error in software bux? getting from subtopic = "
                        + elevator + ", recieving from subtopic = " + message.getSubTopic());
                passedTestsFlag = false;
            }
            // TODO no known body for stopping the overweight buzzer
        } else  {
            System.out.println("ERROR: playCapacityNoise() not sending correct" +
                    " topic subtopic, expecting topic = "  + TOPIC_SPEAKER +
                    ", subtopic = " + elevator);
        }
        System.out.println();

        // Test 3: arrivedAtFloor()
        System.out.println("Test 3: arrivedAtFloor()");
        passedTestsFlag = test3Or4(true);
        if (passedTestsFlag){
            System.out.println("Test 3: passed");
            testsPassed++;
        }
        System.out.println();

        // Test 4: elevatorStatus()
        System.out.println("Test 4: elevatorStatus()");
        elevator = 1;
        passedTestsFlag = test3Or4(false);
        if (passedTestsFlag){
            System.out.println("Test 4: passed");
            testsPassed += 1;
        }
        System.out.println();

        System.out.println("testsPassed: " + testsPassed);
    }
}
