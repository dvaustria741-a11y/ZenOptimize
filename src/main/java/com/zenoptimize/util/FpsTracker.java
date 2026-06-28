package com.zenoptimize.util;

public class FpsTracker {

    private static final int SAMPLE_SIZE = 60;
    private static final long[] frameTimes = new long[SAMPLE_SIZE];
    private static int index = 0;
    private static long lastFrameNano = 0L;
    private static double cachedAvgFps = 60.0;
    private static int frameCount = 0;

    public static void recordFrame() {
        long now = System.nanoTime();
        if (lastFrameNano == 0L) {
            lastFrameNano = now;
            return;
        }
        long delta = now - lastFrameNano;
        lastFrameNano = now;
        frameTimes[index % SAMPLE_SIZE] = delta;
        index++;
        frameCount++;
        if (frameCount >= SAMPLE_SIZE) {
            cachedAvgFps = computeAvg();
            frameCount = 0;
        }
    }

    private static double computeAvg() {
        long total = 0;
        int filled = Math.min(index, SAMPLE_SIZE);
        if (filled == 0) return 60.0;
        for (int i = 0; i < filled; i++) total += frameTimes[i];
        double avgNs = (double) total / filled;
        return avgNs > 0 ? 1_000_000_000.0 / avgNs : 60.0;
    }

    public static double getAverageFps() {
        return cachedAvgFps;
    }

    public static boolean isBelowTarget(int target) {
        return cachedAvgFps < target * 0.75;
    }

    public static boolean isAboveTarget(int target) {
        return cachedAvgFps >= target;
    }
}