package jmri.managers;

import jmri.Throttle;
import jmri.DccThrottle;
import jmri.ThrottleManager;
import jmri.ThrottleListener;
import jmri.DccLocoAddress;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Base for ThrottleManager tests in specific jmrix.packages
 * <p>
 * This is not itself a test class, e.g. should not be added to a suite.
 * Instead, this forms the base for test classes, including providing some
 * common tests
 *
 * @author   Bob Jacobsen 
 * @author   Paul Bender Copyright (C) 2016
 */
public abstract class AbstractThrottleManagerTestBase {

    /**
     * Overload to load l with actual object; create scaffolds as needed
     */
    @Before
    abstract public void setUp(); 

    protected ThrottleManager tm = null; // holds objects under test

    protected boolean throttleFoundResult = false;
    protected boolean throttleNotFoundResult = false;

    protected class ThrottleListen implements ThrottleListener {

       @Override
       public void notifyThrottleFound(DccThrottle t){
             throttleFoundResult = true;
       }

       @Override
       public void notifyFailedThrottleRequest(DccLocoAddress address, String reason){
             throttleFoundResult = true;
       }

    }

    // start of common tests
    // test creation - real work is in the setup() routine
    @Test
    public void testCreate() {
        Assert.assertNotNull(tm);
    }

}
