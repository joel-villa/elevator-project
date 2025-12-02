package Bus;

/**
 * All the topics, subtopics, and bodies
 */
public class SoftwareBusCodes {
    // List of topics
    public static final int systemStop = 1;
    public static final int systemStart = 2;
    public static final int systemReset = 3;
    public static final int clearFire = 4;
    public static final int elevatorMode = 5;
    public static final int startElevator = 6;
    public static final int stopElevator = 7;

    // Elevator Controller to MUX
    public static final int doorControl = 100;
    public static final int carDispatch = 102;
    public static final int carStop = 103;
    public static final int resetFloorSelection = 109;
    public static final int resetCall = 110;
    public static final int displayFloor = 111;
    public static final int displayDirection = 112;

    public static final int callsEnable = 113;
    public static final int selectionsEnable = 114;
    public static final int selectionsType = 115;
    public static final int playSound = 116;

    public static final int fireAlarm = 120;

    // MUX to elevator controller
    public static final int hallCall = 200;

    public static final int cabinSelect = 201;
    public static final int cabinPosition = 202;
    public static final int doorSensor = 203;
    public static final int doorStatus = 204;
    public static final int cabinLoad = 205;
    public static final int fireKey = 206;
    public static final int currDirection = 207;
    public static final int currMovement = 208;
    public static final int fireAlarmActive = 209;
    public static final int topSensor = 210;
    public static final int bottomSensor = 211;

    // Command Center to Elevator Controller
    public static final int setMode = 300;
    public static final int setDestination = 301;
    public static final int elevatorOnOff = 303;



    // Elevator Controller to Command Center
    public static final int fireMode = 400;
    public static final int elevatorStatus = 401;
    public static final int doorStatusCC = 402;
    public static final int ccElevatorDirection = 403;

    // List of Subtopics
    public static final int allElevators = 0;
    public static final int elevatorOne = 1;
    public static final int elevatorTwo = 2;
    public static final int elevatorThree = 3;
    public static final int elevatorFour = 4;
    public static final int buildingMUX = 5;

    public static final int floorOne = 1;
    public static final int floorTwo = 2;
    public static final int floorThree = 3;
    public static final int floorFour = 4;
    public static final int floorFive = 5;
    public static final int floorSix = 6;
    public static final int floorSeven = 7;
    public static final int floorEight = 8;
    public static final int floorNine = 9;
    public static final int floorTen = 10;

    // List of bodies
    public static final int emptyBody = 0;
    public static final int centralized = 1000;
    public static final int independent = 1100;
    public static final int fire = 1110;

    public static final int doorOpen = 0;
    public static final int doorClose = 1;

    public static final int deviceFire = 0;
    public static final int deviceIndependent = 1;
    public static final int deviceCentralized = 2;

    public static final int deviceFloorOne = 1;
    public static final int deviceFloorTwo = 2;
    public static final int deviceFloorThree = 3;
    public static final int deviceFloorFour = 4;
    public static final int deviceFloorFive = 5;
    public static final int deviceFloorSix = 6;
    public static final int deviceFloorSeven = 7;
    public static final int deviceFloorEight = 8;
    public static final int deviceFloorNine = 9;
    public static final int deviceFloorTen = 10;

    public static final int up = 0;
    public static final int down = 1;
    public static final int none = 2;

    public static final int pulled = 1;
    public static final int idle = 0;

    public static final int obstructed = 0;
    public static final int clear = 1;

    public final static int inactive = 0;
    public final static int active = 1;

    public static final int normal = 0;
    public static final int overloaded = 1;

    public static final int arrivalNoise = 0;
    public static final int overloadNoise = 1;

    public static final int on = 1;
    public static final int off = 0;

    public static final int single = 0;
    public static final int multiple = 1;

    public static final int downOffset = 0; // for down call button body:  1 - 10
    public static final int upOffset = 100; // for up call button body: 101 - 110

    //BODIES for TOPIC: resetCall
    public static final int reset1Up = 00;
    public static final int reset2Up = 10;
    public static final int reset3Up = 20;
    public static final int reset4Up = 30;
    public static final int reset5Up = 40;
    public static final int reset6Up = 50;
    public static final int reset7Up = 60;
    public static final int reset8Up = 70;
    public static final int reset9Up = 80;
    public static final int reset10Up = 90;

    public static final int reset1Down= 01;
    public static final int reset2Down = 11;
    public static final int reset3Down = 21;
    public static final int reset4Down = 31;
    public static final int reset5Down = 41;
    public static final int reset6Down = 51;
    public static final int reset7Down = 61;
    public static final int reset8Down = 71;
    public static final int reset9Down = 81;
    public static final int reset10Down = 91;
}
