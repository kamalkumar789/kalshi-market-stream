package com.kamal.kalshi_market_stream.utils;

import java.util.ArrayDeque;
import java.util.Deque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Signals {

    public enum Trend {
        UP, DOWN, FLAT
    }

    private final int fastWindow = 5;
    private final int slowWindow = 10;
    private static final Logger log = LoggerFactory.getLogger(Signals.class);

    private final Deque<Integer> fastQueue = new ArrayDeque<>();
    private final Deque<Integer> slowQueue = new ArrayDeque<>();

    private long fastSum = 0;
    private long slowSum = 0;

    public Trend update(int value) {
        log.debug("Signals update called with value={}", value);

        // --- fast window update ---
        fastQueue.addLast(value);
        fastSum += value;
        if (fastQueue.size() > fastWindow) {
            fastSum -= fastQueue.removeFirst();
        }

        // --- slow window update ---
        slowQueue.addLast(value);
        slowSum += value;
        if (slowQueue.size() > slowWindow) {
            slowSum -= slowQueue.removeFirst();
        }

        if (fastQueue.size() < fastWindow || slowQueue.size() < slowWindow) {
            log.debug("Windows not full yet: fast={}, slow={}",
                    fastQueue.size(), slowQueue.size());
            return Trend.FLAT;
        }

        double fastAvg = fastSum / (double) fastWindow;
        double slowAvg = slowSum / (double) slowWindow;
        double diff = fastAvg - slowAvg;

        Trend trend;
        if (Math.abs(diff) == 0.0) {
            trend = Trend.FLAT;
        } else {
            trend = diff > 0.0 ? Trend.UP : Trend.DOWN;
        }

        log.debug("Trend computed: fastAvg={}, slowAvg={}, diff={}, trend={}",
                fastAvg, slowAvg, diff, trend);

        return trend;
    }
}
