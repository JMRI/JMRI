package jmri.jmrix.can.cbus;

import jmri.jmrix.can.TestTrafficController;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.jmrix.can.cbus.CbusProgrammerManager class.
 *
 * @author Bob Jacobsen Copyright 2008
 */
public class CbusProgrammerManagerTest {

    @Test
    public void testGlobal() {
        CbusProgrammerManager m = new CbusProgrammerManager(new TestTrafficController());
        Assert.assertTrue("no global mode", !m.isGlobalModePossible());
    }

    public void testAddressed() {
        CbusProgrammerManager m = new CbusProgrammerManager(new TestTrafficController());
        Assert.assertTrue("addressed mode ok", m.isAddressedModePossible());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

}
