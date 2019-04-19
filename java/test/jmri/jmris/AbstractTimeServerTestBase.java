package jmri.jmris;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Common tests for classes derived from jmri.jmris.AbstractTimeServer class
 *
 * @author Paul Bender Copyright (C) 2017
 */
abstract public class AbstractTimeServerTestBase {

    protected AbstractTimeServer a = null;

    @Test
    public void testCtor() {
        Assert.assertNotNull(a);
    }

    @Test
    public void addAndRemoveListener() {
        jmri.Timebase t = jmri.InstanceManager.getDefault(jmri.Timebase.class);
        int n = t.getMinuteChangeListeners().length;
        a.listenToTimebase(true);
        Assert.assertEquals("added listener", n + 1, t.getMinuteChangeListeners().length);
        a.listenToTimebase(false);
        Assert.assertEquals("removed listener", n, t.getMinuteChangeListeners().length);
    }

    @Test
    public void sendErrorStatusTest() {
        try {
            a.sendErrorStatus();
        } catch (java.io.IOException ioe) {
            Assert.fail("failed sending status");
        }
        confirmErrorStatusSent();
    }

    /**
     * confirm the error status was forwarded to the client.
     */
    abstract public void confirmErrorStatusSent();

    @Test
    public void sendStatusTest() {
        try {
            a.sendErrorStatus();
        } catch (java.io.IOException ioe) {
            Assert.fail("failed sending status");
        }
        confirmStatusSent();
    }

    /**
     * confirm the status was forwarded to the client.
     */
    abstract public void confirmStatusSent();

    @Test
    public void sendStartAndStopTimebase() {
        a.listenToTimebase(true);
        a.startTime();
        confirmTimeStarted();
        a.stopTime();
        confirmTimeStopped();
    }

    /**
     * confirm the timebase was started; class under test may send client status
     */
    public void confirmTimeStarted() {
        Assert.assertTrue("Timebase started", jmri.InstanceManager.getDefault(jmri.Timebase.class).getRun());
    }

    /**
     * confirm the timebase was stoped; class under test may send client status
     */
    public void confirmTimeStopped() {
        Assert.assertFalse("Timebase stopped", jmri.InstanceManager.getDefault(jmri.Timebase.class).getRun());
    }

    @Before
    // derived classes must configure the TimeServer variable (a)
    abstract public void setUp();

    @After
    // derived classes must clean up the TimeServer variable (a)
    abstract public void tearDown();

}
