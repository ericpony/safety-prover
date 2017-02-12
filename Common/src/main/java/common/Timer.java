package common;

public class Timer {

    public static class TimeoutException extends Exception {
    }

    private static long nanoTimeout = 0L;
    private static long startTime = 0L;
    private static long elapsedTime = 0L;

    public static void start() {
        reset();
        startTime = System.nanoTime();
    }

    public static void reset() {
        elapsedTime = 0L;
        startTime = 0L;
    }

    public static void stop() {
        elapsedTime = getElapsedTime();
        startTime = 0L;
    }

    public static void tick() throws TimeoutException {
        if (nanoTimeout == 0L)
            return;
        if (startTime == 0L)
            throw new IllegalStateException("Should call start() before calling tick()");
        if (System.nanoTime() > startTime + nanoTimeout) {
            throw new Timer.TimeoutException();
        }
    }

    public static void setMilliTimeout(int timeMilliseconds) {
        setNanoTimeout(timeMilliseconds * 1000000L);
    }

    public static void setNanoTimeout(long timeout) {
        if (startTime != 0L)
            throw new IllegalStateException("Cannot set timeout when the timer is counting!");
        nanoTimeout = timeout;
    }

    public static void clearTimeout() {
        nanoTimeout = 0L;
    }

    public static long getElapsedTime() {
        if (elapsedTime != 0L)
            return elapsedTime;
        if (startTime == 0L)
            throw new IllegalStateException("Should call start() before calling this method!");
        return System.nanoTime() - startTime;
    }
}