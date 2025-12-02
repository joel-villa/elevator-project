package ElevatorController.Util;

public class Timer {
    private final long timeout;
    public Timer(long timeoutMillis) {
        this.timeout = System.currentTimeMillis() + timeoutMillis;
    }
    public boolean timeout() {
        return System.currentTimeMillis() > timeout;
    }
}
