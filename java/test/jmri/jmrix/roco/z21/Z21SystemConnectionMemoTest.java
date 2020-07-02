package jmri.jmrix.roco.z21;

import jmri.jmrix.SystemConnectionMemoTestBase;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.jmrix.roco.z21.z21SystemConnectionMemo class
 *
 * @author Paul Bender
 */
public class Z21SystemConnectionMemoTest extends SystemConnectionMemoTestBase<Z21SystemConnectionMemo> {

    @Test
    public void testProvidesReporterManager() {
        Assert.assertTrue(scm.provides(jmri.ReporterManager.class));
    }

    @Test
    public void testProvidesAddressedProgrammerManager() {
        // there is a an addressed program manager, but it is provided
        // by delegation to the XPressNet tunnel, which setUp doesn't 
        // currently enable.
        Assert.assertFalse("Provides Addressed programmer", scm.provides(jmri.AddressedProgrammerManager.class));
    }

    @Test
    public void testProvidesGlobalProgrammerManager() {
        // there is a an global program manager, but it is provided
        // by delegation to the XPressNet tunnel, which setUp doesn't 
        // currently enable.
        Assert.assertFalse("provides golbal programmer", scm.provides(jmri.GlobalProgrammerManager.class));
    }

    @Override
    @Test
    public void testProvidesConsistManager() {
        // there is a consist manager, but it is provided by delegation to 
        // the XPressNet tunnel, which setUp doesn't currently enable.
        Assert.assertFalse("Provides ConsistManager", scm.provides(jmri.ConsistManager.class));
    }

    @Test
    public void testProvidesMultiMeter() {
        Assert.assertTrue("Provides MultiMeter", scm.provides(jmri.MultiMeter.class));
    }

    @Test
    public void testGetMultiMeter() {
        Assert.assertNotNull("Get MultiMeter", scm.get(jmri.MultiMeter.class));
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        scm = new Z21SystemConnectionMemo();
        scm.setTrafficController(new Z21InterfaceScaffold());
        scm.setRocoZ21CommandStation(new RocoZ21CommandStation());
    }

    @Override
    @AfterEach
    public void tearDown() {
        scm.getTrafficController().terminateThreads();
        scm.dispose();
        scm = null;
        JUnitUtil.tearDown();
    }

}
