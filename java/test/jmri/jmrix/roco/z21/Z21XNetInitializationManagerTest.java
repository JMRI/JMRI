package jmri.jmrix.roco.z21;

import jmri.implementation.DccConsistManager;
import jmri.jmrix.lenz.*;
import jmri.util.JUnitUtil;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Z21XNetInitializationManagerTest.java
 *
 * Test for the jmri.jmrix.roco.z21.z21XNetInitializationManager
 * class
 *
 * @author Paul Bender Copyright (C) 2015
 *
 */
public class Z21XNetInitializationManagerTest {

    private XNetTrafficController tc;
    private XNetSystemConnectionMemo memo;
    private RocoZ21CommandStation cs;

    @Test
    public void testCtor() {
        Z21XNetInitializationManager m = new Z21XNetInitializationManager(memo) {
            @Override
            protected int getInitTimeout() {
                return 50;   // shorten, because this will fail & delay test
            }
        };
        assertThat(m).withFailMessage("exists").isNotNull();
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
