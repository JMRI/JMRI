package jmri.jmrix.dccpp;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * DCCppOpsModeProgrammerTest.java
 * <p>
 * Test for the jmri.jmrix.dccpp.DCCppOpsModeProgrammer class
 *
 * @author Paul Bender
 * @author Mark Underwood (C) 2015
 */
public class DCCppOpsModeProgrammerTest extends jmri.jmrix.AbstractOpsModeProgrammerTestBase {

    @Override
    @Test
    public void testGetCanRead() {
        // DccPP supports railcom?
        Assert.assertTrue("can read", programmer.getCanRead());
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        // infrastructure objects
        DCCppInterfaceScaffold tc = new DCCppInterfaceScaffold(new DCCppCommandStation());

        DCCppOpsModeProgrammer t = new DCCppOpsModeProgrammer(5, tc);
        programmer = t;
    }

    @Override
    @AfterEach
    public void tearDown() {
        programmer = null;
        JUnitUtil.resetWindows(false, false);
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

}
