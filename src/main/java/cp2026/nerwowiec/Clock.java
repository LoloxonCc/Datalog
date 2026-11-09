package cp2026.nerwowiec;

import java.util.concurrent.atomic.AtomicLong;

public class Clock {
    private static final AtomicLong clock = new AtomicLong(0);

    /**
     * @return a simulated time in nanoseconds since arbitrary epoch. Like {@link System#nanoTime()}, but
     * additionally guarantees that the values are globally monotonic irrespective of the thread that calls
     * the function.
     */
    public static long nanoTime() {
        return clock.incrementAndGet();
    }
}
