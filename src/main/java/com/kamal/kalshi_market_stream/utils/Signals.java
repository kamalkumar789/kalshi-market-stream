package com.kamal.kalshi_market_stream.utils;

import java.util.ArrayDeque;
import java.util.Deque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maintains two sliding windows:
 * fast window = 5 values
 * slow window = 10 values
 *
 * Each new value is added to both windows.
 * If a window exceeds its size limit, the oldest value is removed.
 *
 * Example:
 * incoming values → 1 2 3 4 5 6
 *
 * fast window step-by-step:
 * [1 2 3 4 5]  → full
 * add 6 → [2 3 4 5 6] (1 removed, 6 added)
 *
 * slow window behaves the same but keeps the last 10 values.
 *
 * After updating both windows, their averages are compared:
 * UP   → fast average > slow average
 * DOWN → fast average < slow average
 * FLAT → not enough data or equal averages
 *
 * This detects short-term momentum vs long-term trend.
 */

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


        return trend;
    }
}
