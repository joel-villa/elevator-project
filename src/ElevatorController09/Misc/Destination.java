package ElevatorController09.Misc;

public record Destination(int floor, int direction) {
    int getFloor() {
        return floor;
    }
    int getDirection() {
        return direction;
    }

}
