package jmri.jmrix.lenz;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

/**
 * XNetProgrammerManagerTest.java
 *
 * Test for the jmri.jmrix.lenz.XNetProgrammerManager class
 *
 * @author Paul Bender Copyright (C) 2012,2018
 */
public class XNetProgrammerManagerTest {

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
        LenzCommandStation commandStation = memo.getXNetTrafficController().getCommandStation();
        Mockito.when(commandStation.isOpsModePossible()).thenReturn(true).thenReturn(false);
        Assert.assertTrue(t.isAddressedModePossible());
        Assert.assertFalse(t.isAddressedModePossible());
    }

    @Test
    public void testGetAddressedProgrammer() {
        XNetProgrammerManager t = new XNetProgrammerManager(prog,memo);
        Assert.assertNotNull(t.getAddressedProgrammer(false,42));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        XNetTrafficController trafficController = Mockito.mock(XNetTrafficController.class);
        LenzCommandStation commandStation = Mockito.mock(LenzCommandStation.class);
        Mockito.when(trafficController.getCommandStation()).thenReturn(commandStation);
        memo = Mockito.mock(XNetSystemConnectionMemo.class);
        Mockito.when(memo.getXNetTrafficController()).thenReturn(trafficController);
        prog = new XNetProgrammer(trafficController);
    }

    @AfterEach
    public void tearDown() {
        memo = null;
        prog = null;
        JUnitUtil.tearDown();
    }

}
