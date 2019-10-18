package jmri.jmrix.roco.z21;

import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Z21XNetProgrammerManagerTest.java
 *
 * Description:	tests for the jmri.jmrix.roco.z21.Z21XNetProgrammerManager class
 *
 * @author	Paul Bender Copyright (C) 2012,2018
 */
public class Z21XNetProgrammerManagerTest {

    private jmri.jmrix.lenz.XNetInterfaceScaffold tc;
    private jmri.jmrix.lenz.XNetSystemConnectionMemo memo;
    private Z21XNetProgrammer prog;
 
    @Test
    public void testCtor() {
        Z21XNetProgrammerManager t = new Z21XNetProgrammerManager(prog,memo);
        Assert.assertNotNull(t);
    }

    @Test
    public void testIsAddressedModePossible() {
        Z21XNetProgrammerManager t = new Z21XNetProgrammerManager(prog,memo);
        Assert.assertTrue(t.isAddressedModePossible());
    }

    @Test
    public void testGetAddressedProgrammer() {
        Z21XNetProgrammerManager t = new Z21XNetProgrammerManager(prog,memo);
        Assert.assertNotNull(t.getAddressedProgrammer(false,42));
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        tc = new jmri.jmrix.lenz.XNetInterfaceScaffold(new RocoZ21CommandStation());
        memo = new jmri.jmrix.lenz.XNetSystemConnectionMemo(tc);
        prog = new Z21XNetProgrammer(tc);
    }

    @After
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

}
