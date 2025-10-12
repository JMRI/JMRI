package jmri.jmrix.roco.z21;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import jmri.implementation.DccConsistManager;
import jmri.jmrix.lenz.*;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;


/**
 * Test for the XNetInitializationManager  when configured for the Roco Z21
 * class
 *
 * @author Paul Bender Copyright (C) 2015,2020
 *
 */
public class XNetInitializationManagerTest {

    private XNetTrafficController tc;
    private XNetSystemConnectionMemo memo;
    private RocoZ21CommandStation cs;

    @Test
    public void testCtor() {
        new XNetInitializationManager()
                .memo(memo)
                .setDefaults()
                .throttleManager(Z21XNetThrottleManager.class)
                .programmer(Z21XNetProgrammer.class)
                .programmerManager(Z21XNetProgrammerManager.class)
                .turnoutManager(Z21XNetTurnoutManager.class)
                .consistManager(null)
                .noCommandStation()
                .init();
        assertNull( memo.getCommandStation());
        assertInstanceOf( XNetPowerManager.class, memo.getPowerManager());
        assertInstanceOf( Z21XNetThrottleManager.class, memo.getThrottleManager());
        assertInstanceOf( Z21XNetProgrammerManager.class, memo.getProgrammerManager());
        assertInstanceOf( XNetProgrammer.XNetConfigurator.class, memo.getProgrammerManager().getGlobalProgrammer().getConfigurator());
        assertInstanceOf( XNetOpsModeProgrammer.XNetOpsConfigurator.class, memo.getProgrammerManager().getAddressedProgrammer(false,42).getConfigurator());
        assertInstanceOf( Z21XNetTurnoutManager.class, memo.getTurnoutManager());
        assertInstanceOf( XNetSensorManager.class, memo.getSensorManager());
        assertInstanceOf( XNetLightManager.class, memo.getLightManager());
        assertInstanceOf( DccConsistManager.class, memo.getConsistManager());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initConnectionConfigManager();
        tc = Mockito.mock(XNetTrafficController.class);
        cs = Mockito.mock(RocoZ21CommandStation.class);
        Mockito.when(cs.isOpsModePossible()).thenReturn(true);
        Mockito.when(tc.getCommandStation()).thenReturn(cs);
        memo = new XNetSystemConnectionMemo(tc);
    }

    @AfterEach
    public void tearDown() {
        memo.dispose();
        memo = null;
        JUnitUtil.tearDown();
    }

}
