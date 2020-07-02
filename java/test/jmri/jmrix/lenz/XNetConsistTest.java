package jmri.jmrix.lenz;

import jmri.InstanceManager;
import jmri.jmrit.consisttool.ConsistPreferencesManager;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.*;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * XNetConsistTest.java
 * <p>
 * Test for the jmri.jmrix.lenz.XNetConsist class
 *
 * @author Paul Bender Copyright (C) 2010,2016,2017
 */
public class XNetConsistTest extends jmri.implementation.AbstractConsistTestBase {

    // infrastructure objects, populated by setUp.
    private XNetInterfaceScaffold tc = null;
    private XNetSystemConnectionMemo memo = null;

    @Test
    public void dccLocoAddressConstructorTest() {
        jmri.DccLocoAddress addr = new jmri.DccLocoAddress(5, false);

        XNetConsist ac = new XNetConsist(addr, tc, memo);
        Assert.assertNotNull(ac);
    }

    @Override
    @Test
    public void checkDisposeMethod() {
        // verify that c has been added to the traffic controller's 
        // list of listeners.
        int listeners = tc.numListeners();

        jmri.DccLocoAddress A = new jmri.DccLocoAddress(200, true);
        jmri.DccLocoAddress B = new jmri.DccLocoAddress(250, true);
        c.restore(A, true); // use restore here, as it does not send
        // any data to the command station
        c.restore(B, false); // use restore here, as it does not send
        // any data to the command station
        // before dispose, this should succeed.
        Assert.assertTrue("Advanced Consist Contains", c.contains(A));
        Assert.assertTrue("Advanced Consist Contains", c.contains(B));

        c.dispose();
        Assert.assertEquals("dispose check", listeners - 1, tc.numListeners());

        // after dispose, this should fail
        Assert.assertThrows("Advanced Consist is null", NullPointerException.class, () -> c.contains(A));
        Assert.assertThrows("Advanced Consist is null", NullPointerException.class, () -> c.contains(B));

    }

    @Override
    @Test
    public void testSetConsistTypeCS() {
        c.setConsistType(jmri.Consist.CS_CONSIST);
        Assert.assertEquals("CS consist type", jmri.Consist.CS_CONSIST, c.getConsistType());
    }

    @Test
    public void checkSizeLimitCS() {
        c.setConsistType(jmri.Consist.CS_CONSIST);
        Assert.assertEquals("CS Consist Limit", 2, c.sizeLimit());
    }

    @Test
    public void checkContainsCS() {
        c.setConsistType(jmri.Consist.CS_CONSIST);
        jmri.DccLocoAddress A = new jmri.DccLocoAddress(200, true);
        jmri.DccLocoAddress B = new jmri.DccLocoAddress(250, true);
        // nothing added, should be false for all.
        Assert.assertFalse("CS Consist Contains", c.contains(A));
        Assert.assertFalse("CS Consist Contains", c.contains(B));
        // add just A
        c.restore(A, true); // use restore here, as it does not send
        // any data to the command station
        Assert.assertTrue("CS Consist Contains", c.contains(A));
        Assert.assertFalse("CS Consist Contains", c.contains(B));
        // then add B
        c.restore(B, false);
        Assert.assertTrue("CS Consist Contains", c.contains(A));
        Assert.assertTrue("CS Consist Contains", c.contains(B));
    }

    @Test
    public void checkGetLocoDirectionCS() {
        c.setConsistType(jmri.Consist.CS_CONSIST);
        jmri.DccLocoAddress A = new jmri.DccLocoAddress(200, true);
        jmri.DccLocoAddress B = new jmri.DccLocoAddress(250, true);
        c.restore(A, true); // use restore here, as it does not send
        // any data to the command station
        c.restore(B, false); // revese direction.
        Assert.assertTrue("Direction in CS Consist", c.getLocoDirection(A));
        Assert.assertFalse("Direction in CS Consist", c.getLocoDirection(B));
    }

    @Test
    @Disabled("Remove requires response from command station")
    @ToDo("re-write parent class test here and include simulated command station response")
    @Override
    public void checkRemoveWithGetRosterIDAdvanced() {
    }

    @Test
    @Disabled("Remove requires response from command station")
    @ToDo("re-write parent class test here and include simulated command station response")
    @Override
    public void checkAddRemoveWithRosterUpdateAdvanced() {
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        InstanceManager.setDefault(ConsistPreferencesManager.class, new ConsistPreferencesManager());
        tc = new XNetInterfaceScaffold(new LenzCommandStation());
        memo = new XNetSystemConnectionMemo(tc);
        c = new XNetConsist(5, tc, memo);
    }

    @AfterEach
    @Override
    public void tearDown() {
        tc.terminateThreads();
        tc = null;
        memo.dispose();
        memo = null;
        JUnitUtil.tearDown();
    }

}
