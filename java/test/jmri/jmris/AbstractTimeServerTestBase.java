package jmri.jmris;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * Common tests for classes derived from jmri.jmris.AbstractTimeServer class
 *
 * @author Paul Bender Copyright (C) 2017
 */
abstract public class AbstractTimeServerTestBase {

    protected AbstractTimeServer a = null;

    @Test
    public void testCtor() {
        assertThat(a).isNotNull();
    }

    @Test
    public void addAndRemoveListener() {
        jmri.Timebase t = jmri.InstanceManager.getDefault(jmri.Timebase.class);
        int n = t.getMinuteChangeListeners().length;
        a.listenToTimebase(true);
        assertThat(t.getMinuteChangeListeners().length).withFailMessage("added listener").isEqualTo(n + 1);
        a.listenToTimebase(false);
        assertThat(t.getMinuteChangeListeners().length).withFailMessage("removed listener").isEqualTo(n);
    }

    @Test
    public void sendErrorStatusTest() {
        Throwable thrown = catchThrowable( () -> a.sendErrorStatus());
        assertThat(thrown).withFailMessage("failed sending status").isNull();
        confirmErrorStatusSent();
    }

    /**
     * confirm the error status was forwarded to the client.
     */
    abstract public void confirmErrorStatusSent();

    @Test
    public void sendStatusTest() {
        Throwable thrown = catchThrowable( () -> a.sendErrorStatus());
        assertThat(thrown).withFailMessage("failed sending status").isNull();
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
        assertThat(jmri.InstanceManager.getDefault(jmri.Timebase.class).getRun()).withFailMessage("Timebase started").isTrue();
    }

    /**
     * confirm the timebase was stoped; class under test may send client status
     */
    public void confirmTimeStopped() {
        assertThat(jmri.InstanceManager.getDefault(jmri.Timebase.class).getRun()).withFailMessage("Timebase stopped").isFalse();
    }

    @BeforeEach
    // derived classes must configure the TimeServer variable (a)
    abstract public void setUp();

    @AfterEach
    // derived classes must clean up the TimeServer variable (a)
    abstract public void tearDown();

}
