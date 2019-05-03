package jmri.jmris;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import jmri.LocoAddress;
import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.Throttle;
import jmri.ThrottleListener;

/**
 * Common tests for classes derived from jmri.jmris.AbstractThrottleServer class
 *
 * @author Paul Bender Copyright (C) 2017 
 */
abstract public class AbstractThrottleServerTestBase {

    protected AbstractThrottleServer ats = null;

    @Test
    public void testCtor() {
        Assert.assertNotNull(ats);
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
       try {
          ats.sendErrorStatus();
       } catch (java.io.IOException ioe) {
          Assert.fail("failed sending status");
       }
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
       try {
          ats.sendStatus(address);
       } catch (java.io.IOException ioe) {
          Assert.fail("failed sending status");
       }
       confirmThrottleStatusSent();
    }

    /**
     * confirm the throttle status was forwarded to the client.
     */
    abstract public void confirmThrottleStatusSent();

    @Before
    // derived classes must configure the ThrottleServer variable (ats)
    // and should also install a throttle manager.
    abstract public void setUp();

    @After
    public void postTestReset(){
       ats = null;
    }


}
