package ElevatorController.Util;

public record FloorNDirection(int floor, Direction direction) {

    public int getFloor() {
        return floor;
    }

    public Direction getDirection() {
        return direction;
    }

    /**
     * Is this equal to that?
     * @param obj   the reference object with which to compare.
     * @return
     */
    @Override
    public boolean equals(Object obj){
        if (obj == null || !(obj instanceof FloorNDirection)){
            return false;
        }
        FloorNDirection that = (FloorNDirection) obj;
        return this.floor == that.floor && this.direction == that.direction;

    }

    @Override
    public String toString() {
        return "Floor: " + floor + " , Direction: " + direction;
    }


}
