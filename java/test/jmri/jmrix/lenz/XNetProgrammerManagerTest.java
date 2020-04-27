package jmri.jmrix.lenz;

import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * XNetProgrammerManagerTest.java
 *
 * Test for the jmri.jmrix.lenz.XNetProgrammerManager class
 *
 * @author Paul Bender Copyright (C) 2012,2018
 */
public class XNetProgrammerManagerTest {

    private XNetInterfaceScaffold tc;
    private XNetSystemConnectionMemo memo;
    private XNetProgrammer prog;
 
    @Test
    public void testCtor() {
        XNetProgrammerManager t = new XNetProgrammerManager(prog,memo);
        Assert.assertNotNull(t);
    }

    @Test
    public void testIsAddressedModePossible() {
        XNetProgrammerManager t = new XNetProgrammerManager(prog,memo);
        Assert.assertTrue(t.isAddressedModePossible());
    }

    @Test
    public void testGetAddressedProgrammer() {
        XNetProgrammerManager t = new XNetProgrammerManager(prog,memo);
        Assert.assertNotNull(t.getAddressedProgrammer(false,42));
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        tc = new XNetInterfaceScaffold(new LenzCommandStation());
        memo = new XNetSystemConnectionMemo(tc);
        prog = new XNetProgrammer(tc);
    }

    @After
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

}
