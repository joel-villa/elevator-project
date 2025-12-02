package Motion.Tests;

import Motion.Util.Constants;

public class SignificantLocs {
    // First position to start decelerating at (3rd floor - 0 based indexing)
    public static double FIRST_STOP = (Constants.HEIGHT + Constants.FLOOR_THICKNESS)*3 + Constants.FLOOR_THICKNESS + Constants.ACCELERATION_DIST +.9;

    // Time to wait at a stop
    public static double TIME_TO_WAIT = 1;

    // Start the elevator going downward

    // Second position to start decelerating at (2nd floor)
    public static double SECOND_STOP = (Constants.HEIGHT + Constants.FLOOR_THICKNESS)*3 - Constants.ACCELERATION_DIST;

    // Start elevator going up

    // Stop on the top floor
    public static double FINAL_STOP = (Constants.HEIGHT + Constants.FLOOR_THICKNESS)*9 + Constants.FLOOR_THICKNESS + Constants.ACCELERATION;
}
