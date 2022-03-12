package jmri.jmrix.roco.z21;

import jmri.implementation.DccConsistManager;
import jmri.jmrix.lenz.*;
import jmri.util.JUnitUtil;

import org.assertj.core.api.SoftAssertions;
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
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(memo.getCommandStation()).isNull();
        softly.assertThat(memo.getPowerManager()).isExactlyInstanceOf((XNetPowerManager.class));
        softly.assertThat(memo.getThrottleManager()).isExactlyInstanceOf(Z21XNetThrottleManager.class);
        softly.assertThat(memo.getProgrammerManager()).isExactlyInstanceOf(Z21XNetProgrammerManager.class);
        softly.assertThat(memo.getProgrammerManager().getGlobalProgrammer()).isExactlyInstanceOf(Z21XNetProgrammer.class);
        softly.assertThat(memo.getProgrammerManager().getAddressedProgrammer(false,42)).isExactlyInstanceOf(Z21XNetOpsModeProgrammer.class);
        softly.assertThat(memo.getTurnoutManager()).isExactlyInstanceOf(Z21XNetTurnoutManager.class);
        softly.assertThat(memo.getSensorManager()).isExactlyInstanceOf(XNetSensorManager.class);
        softly.assertThat(memo.getLightManager()).isExactlyInstanceOf(XNetLightManager.class);
        softly.assertThat(memo.getConsistManager()).isExactlyInstanceOf(DccConsistManager.class);
        softly.assertAll();
    }

    @BeforeEach
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.initConnectionConfigManager();
        tc = Mockito.mock(XNetTrafficController.class);
        cs = Mockito.mock(RocoZ21CommandStation.class);
        Mockito.when(cs.isOpsModePossible()).thenReturn(true);
        Mockito.when(tc.getCommandStation()).thenReturn(cs);
        memo = new XNetSystemConnectionMemo(tc);
    }

    @AfterEach
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }

}
