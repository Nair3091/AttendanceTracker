package com.example.attendancetracker;

import static org.junit.Assert.assertEquals;

import com.example.attendancetracker.utils.AttendanceCalculator;

import org.junit.Test;

public class AttendanceCalculatorTest {

    @Test
    public void percentage_workedExample() {
        assertEquals(85.0, AttendanceCalculator.percentage(34, 40), 0.001);
    }

    @Test
    public void percentage_noClasses() {
        assertEquals(-1.0, AttendanceCalculator.percentage(0, 0), 0.001);
    }

    @Test
    public void safeLeaves_workedExample() {
        assertEquals(5, AttendanceCalculator.safeLeaves(34, 40, 75));
    }

    @Test
    public void recovery_workedExample() {
        assertEquals(12, AttendanceCalculator.classesToRecover(27, 40, 75));
    }

    @Test
    public void recovery_notPossibleAt100() {
        assertEquals(-1, AttendanceCalculator.classesToRecover(39, 40, 100));
    }
}