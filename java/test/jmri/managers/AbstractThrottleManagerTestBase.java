package jmri.managers;

import jmri.LocoAddress;
import jmri.DccThrottle;
import jmri.ThrottleListener;
import jmri.ThrottleManager;
import org.junit.After;
import org.junit.Assert;
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
    protected boolean throttleStealResult = false;

    protected class ThrottleListen implements ThrottleListener {

       @Override
       public void notifyThrottleFound(DccThrottle t){
             throttleFoundResult = true;
       }

       @Override
       public void notifyFailedThrottleRequest(LocoAddress address, String reason){
             throttleNotFoundResult = true;
       }

       @Override
       public void notifyStealThrottleRequired(LocoAddress address){
            throttleStealResult = true;
       }
    }

    @After
    public void postTestReset(){
       throttleFoundResult = false;
       throttleNotFoundResult = false;
       throttleStealResult = false;
    }

    // start of common tests
    // test creation - real work is in the setup() routine
    @Test
    public void testCreate() {
        Assert.assertNotNull(tm);
    }

    @Test
    public void getUserName() {
        Assert.assertNotNull(tm.getUserName());
    }

    @Test
    public void hasDispatchFunction() {
        Assert.assertNotNull(tm.hasDispatchFunction());
    }

    @Test
    public void addressTypeUnique() {
        Assert.assertNotNull(tm.addressTypeUnique());
    }

    @Test
    public void canBeLongAddress() {
       Assert.assertNotNull(tm.canBeLongAddress(50));
    }

    @Test
    public void canBeShortAddress() {
       Assert.assertNotNull(tm.canBeShortAddress(50));
    }

    @Test
    public void supportedSpeedModes() {
        Assert.assertNotNull(tm.supportedSpeedModes());
    }

    @Test
    public void getAddressTypes() {
        Assert.assertNotNull(tm.getAddressTypes());
    }

    @Test
    public void getAddressProtocolTypes() {
        Assert.assertNotNull(tm.getAddressProtocolTypes());
    }


}
