package ElevatorController.Util;

import Bus.SoftwareBusCodes;

public enum Direction {
    UP(SoftwareBusCodes.up),
    DOWN(SoftwareBusCodes.down),
    STOPPED(SoftwareBusCodes.none);

    // Directions associated with numbers from MUX's body handling
    private int integerVersion;

    public int getIntegerVersion() {
        return integerVersion;
    }
    private Direction(int integerVersion){
        this.integerVersion=integerVersion;
    }
}
