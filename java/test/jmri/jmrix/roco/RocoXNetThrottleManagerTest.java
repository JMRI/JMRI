package jmri.jmrix.roco;

import jmri.jmrix.lenz.XNetSystemConnectionMemo;
import jmri.jmrix.lenz.XNetThrottleManagerTest;
import jmri.jmrix.lenz.XNetTrafficController;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.mockito.Mockito;


/**
 * Tests for the jmri.jmrix.roco.RocoXNetThrottleManager class
 *
 * @author Paul Bender Copyright (C) 2015,2016
 */
public class RocoXNetThrottleManagerTest extends XNetThrottleManagerTest {

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        XNetTrafficController tc = Mockito.mock(XNetTrafficController.class);
        RocoCommandStation cs = Mockito.mock(RocoCommandStation.class);
        Mockito.when(tc.getCommandStation()).thenReturn(cs);
        XNetSystemConnectionMemo memo = Mockito.mock(XNetSystemConnectionMemo.class);
        Mockito.when(memo.getXNetTrafficController()).thenReturn(tc);
        Mockito.when(memo.getUserName()).thenReturn("Roco");
        tm = new RocoXNetThrottleManager(memo);
    }

    @AfterEach
    @Override
    public void tearDown() {
        tm = null;
        JUnitUtil.tearDown();
    }

}
