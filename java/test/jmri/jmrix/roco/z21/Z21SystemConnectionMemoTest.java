package jmri.jmrix.roco.z21;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for the jmri.jmrix.roco.z21.z21SystemConnectionMemo class
 *
 * @author	Paul Bender
 */
public class Z21SystemConnectionMemoTest extends jmri.jmrix.SystemConnectionMemoTestBase {

    @Test
    public void testProvidesReporterManager() {
        Z21SystemConnectionMemo a = (Z21SystemConnectionMemo)scm;
        Assert.assertTrue(a.provides(jmri.ReporterManager.class));
    }

    @Test
    @Ignore("needs more setup")
    public void testProvidesAddressedProgrammerManager() {
        Z21SystemConnectionMemo a = (Z21SystemConnectionMemo)scm;
        Assert.assertTrue(a.provides(jmri.AddressedProgrammerManager.class));
    }

    @Test
    @Ignore("needs more setup")
    public void testProvidesGlobalProgrammerManager() {
        Z21SystemConnectionMemo a = (Z21SystemConnectionMemo)scm;
        Assert.assertTrue(a.provides(jmri.GlobalProgrammerManager.class));
    }

    @Override
    @Test
    public void testProvidesConsistManager(){
       // there is a consist manager, but it is provided by delegation to 
       // the XPressNet tunnel, which setUp doesn't currently enable.
       Assert.assertFalse("Provides ConsistManager",scm.provides(jmri.ConsistManager.class));
    }

    @Test
    public void testProvidesMultiMeter(){
       Assert.assertTrue("Provides MultiMeter",scm.provides(jmri.MultiMeter.class));
    }

    @Test
    public void testGetMultiMeter(){
       Assert.assertNotNull("Get MultiMeter",scm.get(jmri.MultiMeter.class));
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        Z21InterfaceScaffold tc = new Z21InterfaceScaffold();
        Z21SystemConnectionMemo memo = new Z21SystemConnectionMemo();
        memo.setTrafficController(tc);
        memo.setRocoZ21CommandStation(new RocoZ21CommandStation());
        //memo.configureManagers();
        scm = memo;
    }

    @Override
    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
