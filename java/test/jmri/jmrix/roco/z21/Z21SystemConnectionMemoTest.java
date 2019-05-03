package jmri.jmrix.roco.z21;

import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Tests for the jmri.jmrix.roco.z21.z21SystemConnectionMemo class
 *
 * @author	Paul Bender
 */
public class Z21SystemConnectionMemoTest extends jmri.jmrix.SystemConnectionMemoTestBase {

    private Z21SystemConnectionMemo memo;
    private Z21InterfaceScaffold tc;

    @Test
    public void testProvidesReporterManager() {
        Z21SystemConnectionMemo a = (Z21SystemConnectionMemo)scm;
        Assert.assertTrue(a.provides(jmri.ReporterManager.class));
    }

    @Test
    public void testProvidesAddressedProgrammerManager() {
       Z21SystemConnectionMemo a = (Z21SystemConnectionMemo)scm;
       // there is a an addressed program manager, but it is provided
       // by delegation to the XPressNet tunnel, which setUp doesn't 
       // currently enable.
       Assert.assertFalse("Provides Addressed programmer",a.provides(jmri.AddressedProgrammerManager.class));
    }

    @Test
    public void testProvidesGlobalProgrammerManager() {
        Z21SystemConnectionMemo a = (Z21SystemConnectionMemo)scm;
       // there is a an global program manager, but it is provided
       // by delegation to the XPressNet tunnel, which setUp doesn't 
       // currently enable.
        Assert.assertFalse("provides golbal programmer",a.provides(jmri.GlobalProgrammerManager.class));
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
        tc = new Z21InterfaceScaffold();
        memo = new Z21SystemConnectionMemo();
        memo.setTrafficController(tc);
        memo.setRocoZ21CommandStation(new RocoZ21CommandStation());
        //memo.configureManagers();
        scm = memo;
    }

    @Override
    @After
    public void tearDown() {
        scm = null;
        memo = null;
        tc.terminateThreads();
        tc = null;
        JUnitUtil.tearDown();
    }

}
