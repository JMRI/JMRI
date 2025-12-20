package jmri.util;

import java.util.TimerTask;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Bob Jacobsen Copyright (C) 2018
 */
public class TimerUtilTest {

    // no testCtor as tested class only supplied static methods

    @Test
    public void testScheduleDate() {
        TimerUtil.schedule(task, 10);
        assertTrue(JUnitUtil.waitFor(() -> {return taskRan;}));
    }

    @Test
    public void testScheduleDateOnLayout() {
        TimerUtil.scheduleOnLayoutThread(task, 10);
        assertTrue(JUnitUtil.waitFor(() -> {return taskRan;}));
        assertTrue(taskRanOnLayout);
    }

    @Test
    public void testScheduleDateOnGUI() {
        TimerUtil.scheduleOnGUIThread(task, 10);
        assertTrue(JUnitUtil.waitFor(() -> {return taskRan;}));
        assertTrue(taskRanOnGUI);
    }

    private boolean taskRan = false;
    private boolean taskRanOnGUI = false;
    private boolean taskRanOnLayout = false;

    final TimerTask task = new TimerTask(){
        @Override
        public void run() {
            taskRanOnLayout = ThreadingUtil.isGUIThread();
            taskRanOnGUI = ThreadingUtil.isLayoutThread();
            taskRan = true;
        }
    };

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        taskRan = false;
        taskRanOnGUI = false;
        taskRanOnLayout = false;        
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TimerUtilTest.class);

}
