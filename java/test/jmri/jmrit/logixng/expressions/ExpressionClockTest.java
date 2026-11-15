package jmri.jmrit.logixng.expressions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mockStatic;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import jmri.InstanceManager;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.util.JUnitUtil;
import jmri.util.TimerUtil;

import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

/**
 * Tests for {@link ExpressionClock}.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class ExpressionClockTest {

    /**
     * On the system clock, the expression is checked at the start of each
     * minute, so that a minute's change is seen as it happens.
     */
    @Test
    public void testSystemClockCheckedAtTheStartOfEachMinute() {
        List<long[]> calls = new ArrayList<>(); // {first delay, period, time of the call}
        try (MockedStatic<TimerUtil> timer = mockStatic(TimerUtil.class)) {
            timer.when(() -> TimerUtil.schedule(any(TimerTask.class), anyLong(), anyLong())).thenAnswer(
                    i -> calls.add(new long[]{i.getArgument(1), i.getArgument(2), System.currentTimeMillis()}));
            timer.when(() -> TimerUtil.scheduleAtFixedRate(any(TimerTask.class), anyLong(), anyLong())).thenAnswer(
                    i -> calls.add(new long[]{i.getArgument(1), i.getArgument(2), System.currentTimeMillis()}));

            ExpressionClock e = new ExpressionClock(InstanceManager.getDefault(DigitalExpressionManager.class).getAutoSystemName(), null);
            e.setType(ExpressionClock.Type.SystemClock);
            e.registerListenersForThisClass();
            e.unregisterListenersForThisClass();
        }
        assertEquals(1, calls.size(), "one timer");
        long[] c = calls.get(0);
        assertEquals(60000, c[1], "every minute");
        assertTrue(c[0] > 0 && c[0] <= 60000, "first check within the minute: " + c[0]);
        long secondOfFirstCheck = (c[2] + c[0]) % 60000;
        assertTrue(secondOfFirstCheck < 250 || secondOfFirstCheck > 59750,
                "first check at the start of a minute, not " + secondOfFirstCheck + " ms after it");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initLogixNGManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
