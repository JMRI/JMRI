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
public class Z21SystemConnectionMemoTest {

    @Test
    public void testCtor() {
        Z21SystemConnectionMemo a = new Z21SystemConnectionMemo();
        Assert.assertNotNull(a);
    }

    @Test
    @Ignore("Not Ready Yet")
    public void testConfigureManagers(){
        Z21SystemConnectionMemo a = new Z21SystemConnectionMemo();
        a.setTrafficController(new Z21InterfaceScaffold());
        a.configureManagers();
        Assert.assertNotNull(a);
    }

    @Test
    public void testProvidesReporterManager() {
        Z21SystemConnectionMemo a = new Z21SystemConnectionMemo();
        Assert.assertTrue(a.provides(jmri.ReporterManager.class));
    }

    @Test
    @Ignore("needs more setup")
    public void testProvidesAddressedProgrammerManager() {
        Z21SystemConnectionMemo a = new Z21SystemConnectionMemo();
        Assert.assertTrue(a.provides(jmri.AddressedProgrammerManager.class));
    }

    @Test
    @Ignore("needs more setup")
    public void testProvidesGlobalProgrammerManager() {
        Z21SystemConnectionMemo a = new Z21SystemConnectionMemo();
        Assert.assertTrue(a.provides(jmri.GlobalProgrammerManager.class));
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
