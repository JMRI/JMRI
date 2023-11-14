package jmri.jmris;

import org.junit.jupiter.api.Test;

import jmri.DccLocoAddress;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.jupiter.api.AfterEach;

/**
 * Common tests for classes derived from jmri.jmris.AbstractThrottleServer class
 *
 * @author Paul Bender Copyright (C) 2017 
 */
abstract public class AbstractThrottleServerTestBase {

    protected AbstractThrottleServer ats = null;

    @Test
    public void testCtor() {
        assertThat(ats).isNotNull();
    }

    @Test
    public void requestThrottleTest(){
       ats.requestThrottle(new DccLocoAddress(42,false));
       confirmThrottleRequestSucceeded();
    }

    /**
     * confirm the throttle request succeeded and an appropirate response
     * was forwarded to the client.
     */
    abstract public void confirmThrottleRequestSucceeded();

    @Test
    public void sendErrorStatusTest(){
        Throwable thrown = catchThrowable( () ->  ats.sendErrorStatus());
        assertThat(thrown).withFailMessage("failed sending status").isNull();
        confirmThrottleErrorStatusSent();
    }

    /**
     * confirm the throttle status was forwarded to the client.
     */
    abstract public void confirmThrottleErrorStatusSent();

    @Test
    public void sendStatusTest(){
       DccLocoAddress address = new DccLocoAddress(42,false);
       ats.requestThrottle(address);
       Throwable thrown = catchThrowable( () -> ats.sendStatus(address));
       assertThat(thrown).withFailMessage("failed sending status").isNull();
       confirmThrottleStatusSent();
    }

    /**
     * confirm the throttle status was forwarded to the client.
     */
    abstract public void confirmThrottleStatusSent();

    // derived classes must configure the ThrottleServer variable (ats)
    // and should also install a throttle manager.
    abstract public void setUp();

    @AfterEach
    public void postTestReset(){
       ats = null;
    }

}
