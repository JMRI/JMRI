package jmri.managers;

import jmri.LocoAddress;
import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.Throttle;
import jmri.ThrottleListener;
import jmri.ThrottleManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

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
     * Overload to load tm with actual object; create scaffolds as needed
     */
    abstract public void setUp(); 

    protected ThrottleManager tm = null; // holds objects under test

    protected boolean throttleFoundResult = false;
    protected boolean throttleNotFoundResult = false;
    protected boolean throttleStealResult = false;

    public class ThrottleListen implements ThrottleListener {

        private DccThrottle foundThrottle = null;
        private String failedReason;
        public int flagGotStealRequest = -1;

        @Override
        public void notifyThrottleFound(DccThrottle t){
            throttleFoundResult = true;
            foundThrottle = t;
        }

        @Override
        public void notifyFailedThrottleRequest(LocoAddress address, String reason){
            throttleNotFoundResult = true;
            failedReason = reason;
        }

        @Override
        public void notifyDecisionRequired(LocoAddress address, DecisionType question) {
            if ( question == DecisionType.STEAL ){
                throttleStealResult = true;
                flagGotStealRequest = address.getNumber();
            }
        }

        public DccThrottle getThrottle() {
            return foundThrottle;
        }

        public String getFailedReason() {
            return failedReason;
        }

        public int getFlagGotStealRequest() {
            return flagGotStealRequest;
        }

    }

    @AfterEach
    public void postTestReset(){
       throttleFoundResult = false;
       throttleNotFoundResult = false;
       throttleStealResult = false;
    }

    // start of common tests
    // test creation - real work is in the setup() routine
    @Test
    public void testCreate() {
        assertNotNull(tm);
    }

    @Test
    public void getUserName() {
        assertNotNull(tm.getUserName());
    }

    @Test
    public void hasDispatchFunction() {
        assertNotNull(tm.hasDispatchFunction());
    }

    @Test
    public void addressTypeUnique() {
        assertNotNull(tm.addressTypeUnique());
    }

    @Test
    public void canBeLongAddress() {
        assertNotNull(tm.canBeLongAddress(50));
    }

    @Test
    public void canBeShortAddress() {
        assertNotNull(tm.canBeShortAddress(50));
    }

    @Test
    public void supportedSpeedModes() {
        assertNotNull(tm.supportedSpeedModes());
    }

    @Test
    public void getAddressTypes() {
        assertNotNull(tm.getAddressTypes());
    }

    @Test
    public void getAddressProtocolTypes() {
        assertNotNull(tm.getAddressProtocolTypes());
    }

    @Test
    public void testGetAddressNullValue() {
        assertNull( tm.getAddress(null, ""), "null address value");
    }

    @Test
    public void testGetAddressNullProtocol() {
        assertNull( tm.getAddress("42", (String)null), "null protocol");
    }

    @Test
    public void testGetAddressShort() {
        assertNotNull( tm.getAddress("42", LocoAddress.Protocol.DCC), "short address value");
    }

    @Test
    public void testGetAddressLong() {
        assertNotNull( tm.getAddress("4200", LocoAddress.Protocol.DCC), "long address value");
    }

    @Test
    public void testGetAddressShortString() {
        assertNotNull( tm.getAddress("42", "DCC"), "short address value from strings");
    }

    @Test
    public void testGetAddressLongString() {
        assertNotNull( tm.getAddress("4200", "DCC"), "long address value from strings");
    }

    @Test
    @SuppressWarnings({"deprecation"})
    public void testGetThrottleInfo() {
        DccLocoAddress addr = new DccLocoAddress(42, false);
        assertEquals( 0, tm.getThrottleUsageCount(addr), "throttle use 0");
        assertEquals( 0, tm.getThrottleUsageCount(42, false), "throttle use 0");
        assertNull( tm.getThrottleInfo(addr, Throttle.F28), "NULL");
        ThrottleListen throtListen = new ThrottleListen();
        tm.requestThrottle(addr, throtListen, true);
        JUnitUtil.waitFor(()-> (tm.getThrottleInfo(addr, Throttle.ISFORWARD) != null), "reply didn't arrive");

        assertTrue(throttleFoundResult);
        assertFalse( throttleNotFoundResult );
        assertFalse( throttleStealResult );

        assertNotNull( tm.getThrottleInfo(addr, Throttle.ISFORWARD), "is forward");
        assertNotNull( tm.getThrottleInfo(addr, Throttle.SPEEDSETTING), "speed setting");
        assertNotNull( tm.getThrottleInfo(addr, Throttle.SPEEDINCREMENT), "speed increment");
        assertNotNull( tm.getThrottleInfo(addr, Throttle.SPEEDSTEPMODE), "speed step mode");
        assertNotNull( tm.getThrottleInfo(addr, Throttle.F0), "F0");
        assertNotNull( tm.getThrottleInfo(addr, Throttle.F1), "F1");
        assertNotNull( tm.getThrottleInfo(addr, Throttle.F2), "F2");
        assertNotNull( tm.getThrottleInfo(addr, Throttle.F3), "F3");
        assertNotNull( tm.getThrottleInfo(addr, Throttle.F4), "F4");
        assertNotNull( tm.getThrottleInfo(addr, Throttle.F5), "F5");
        assertNotNull( tm.getThrottleInfo(addr, Throttle.F6), "F6");
        assertNotNull( tm.getThrottleInfo(addr, Throttle.F7), "F7");
        assertNotNull( tm.getThrottleInfo(addr, Throttle.F8), "F8");
        assertNotNull( tm.getThrottleInfo(addr, Throttle.F9), "F9");
        assertNotNull( tm.getThrottleInfo(addr, Throttle.F10), "F10");
        assertNotNull( tm.getThrottleInfo(addr, Throttle.F11), "F11");
        assertNotNull( tm.getThrottleInfo(addr, Throttle.F12), "F12");
        assertNotNull( tm.getThrottleInfo(addr, Throttle.F13), "F13");
        assertNotNull( tm.getThrottleInfo(addr, Throttle.F14), "F14");
        assertNotNull( tm.getThrottleInfo(addr, Throttle.F15), "F15");
        assertNotNull( tm.getThrottleInfo(addr, Throttle.F16), "F16");
        assertNotNull( tm.getThrottleInfo(addr, Throttle.F17), "F17");
        assertNotNull( tm.getThrottleInfo(addr, Throttle.F18), "F18");
        assertNotNull( tm.getThrottleInfo(addr, Throttle.F19), "F19");
        assertNotNull( tm.getThrottleInfo(addr, Throttle.F20), "F20");
        assertNotNull( tm.getThrottleInfo(addr, Throttle.F21), "F21");
        assertNotNull( tm.getThrottleInfo(addr, Throttle.F22), "F22");
        assertNotNull( tm.getThrottleInfo(addr, Throttle.F23), "F23");
        assertNotNull( tm.getThrottleInfo(addr, Throttle.F24), "F24");
        assertNotNull( tm.getThrottleInfo(addr, Throttle.F25), "F25");
        assertNotNull( tm.getThrottleInfo(addr, Throttle.F26), "F26");
        assertNotNull( tm.getThrottleInfo(addr, Throttle.F27), "F27");
        assertNotNull( tm.getThrottleInfo(addr, Throttle.F28), "F28");
        assertNull( tm.getThrottleInfo(addr, "NOT A VARIABLE"), "NULL");
        assertEquals( 1, tm.getThrottleUsageCount(addr), "throttle use 1 addr");
        assertEquals( 1, tm.getThrottleUsageCount(42, false), "throttle use 1 int b");
        assertEquals( 0, tm.getThrottleUsageCount(77, true), "throttle use 0");

        // remove listener on throttle created in process
        DccThrottle throttle = throtListen.getThrottle();
        assertNotNull(throttle);
        tm.releaseThrottle(throttle, throtListen);
        JUnitUtil.waitFor(()-> (tm.getThrottleUsageCount(addr) == 0), "throttle still in use after release");
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractThrottleManagerTestBase.class);
}
