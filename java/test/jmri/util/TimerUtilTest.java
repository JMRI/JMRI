package jmri.util;

import java.util.TimerTask;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Bob Jacobsen Copyright (C) 2018
 */
public class TimerUtilTest {

    @Test
    public void testCTor() {
        TimerUtil t = new TimerUtil();
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testScheduleDate() {
        TimerUtil.schedule(task, 10);
        Assert.assertTrue(JUnitUtil.waitFor(() -> {return taskRan;}));
    }

    @Test
    public void testScheduleDateOnLayout() {
        TimerUtil.scheduleOnLayoutThread(task, 10);
        Assert.assertTrue(JUnitUtil.waitFor(() -> {return taskRan;}));
        Assert.assertTrue(taskRanOnLayout);
    }

    @Test
    public void testScheduleDateOnGUI() {
        TimerUtil.scheduleOnGUIThread(task, 10);
        Assert.assertTrue(JUnitUtil.waitFor(() -> {return taskRan;}));
        Assert.assertTrue(taskRanOnGUI);
    }

    boolean taskRan = false;
    boolean taskRanOnGUI = false;
    boolean taskRanOnLayout = false;

    final TimerTask task = new TimerTask(){
        @Override
        public void run() {
            taskRanOnLayout = ThreadingUtil.isGUIThread();
            taskRanOnGUI = ThreadingUtil.isLayoutThread();
            taskRan = true;
        }
    };
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        taskRan = false;
        taskRanOnGUI = false;
        taskRanOnLayout = false;        
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TimerUtilTest.class);

}
