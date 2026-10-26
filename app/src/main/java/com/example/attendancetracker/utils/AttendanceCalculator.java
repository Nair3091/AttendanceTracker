package com.example.attendancetracker.utils;

public class AttendanceCalculator {

    /**
     * Calculates attendance percentage. Cancelled classes (C) are not held and never enter the formula.
     * held = P + A, attended = P[cite: 4].
     */
    public static double calculatePercentage(int present, int absent) {
        int held = present + absent;
        if (held == 0) {
            return 0.0; // Edge case: handled in UI as "No classes yet"[cite: 5]
        }
        return ((double) present / held) * 100.0;
    }

    /**
     * Calculates how many more classes can be safely skipped (x) while staying at or above target[cite: 4].
     * Formula: x = floor(a / t - h)[cite: 4]
     */
    public static int calculateSafeLeaves(int attended, int held, double targetPercent) {
        if (targetPercent <= 0) return 0;
        double target = targetPercent / 100.0;
        double maxHeld = attended / target;
        int safeSkips = (int) Math.floor(maxHeld - held);
        return Math.max(0, safeSkips);
    }

    /**
     * Calculates how many classes in a row (y) must be attended to recover to target[cite: 5].
     * Formula: y = ceil((t * h - a) / (1 - t))[cite: 5]
     */
    public static int calculateRecoveryClasses(int attended, int held, double targetPercent) {
        if (targetPercent >= 100) {
            return -1; // Recovery not possible for 100% target once an absence exists[cite: 5]
        }
        double target = targetPercent / 100.0;
        double numerator = (target * held) - attended;
        double denominator = 1.0 - target;
        if (numerator <= 0) {
            return 0; // Already at or above target
        }
        return (int) Math.ceil(numerator / denominator);
    }
}