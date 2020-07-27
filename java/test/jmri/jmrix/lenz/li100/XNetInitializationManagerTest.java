package jmri.jmrix.lenz.li100;

import jmri.implementation.NmraConsistManager;
import jmri.jmrix.lenz.*;
import jmri.jmrix.roco.RocoXNetThrottleManager;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Tests for XNetInitializationManager when configured for the LI100.
 *
 * @author Paul Bender Copyright (C) 2020
 */
public class XNetInitializationManagerTest {

    private XNetTrafficController tc;
    private XNetSystemConnectionMemo memo;
    private LenzCommandStation cs;

    @Test
    public void testNoCSVersionResponse() {
        Mockito.when(cs.getCommandStationSoftwareVersion()).thenReturn(-1.0f);
        Mockito.when(cs.getCommandStationSoftwareVersionBCD()).thenReturn(-1.0f);
        Mockito.when(cs.getCommandStationType()).thenReturn(-1);
        Mockito.when(cs.isOpsModePossible()).thenReturn(true);
        new XNetInitializationManager().memo(memo).setDefaults().versionCheck().setTimeout(50).programmer(LI100XNetProgrammer.class).init();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(memo.getCommandStation()).isEqualTo(cs);
        softly.assertThat(memo.getPowerManager()).isExactlyInstanceOf((XNetPowerManager.class));
        softly.assertThat(memo.getThrottleManager()).isExactlyInstanceOf(XNetThrottleManager.class);
        softly.assertThat(memo.getProgrammerManager()).isExactlyInstanceOf(XNetProgrammerManager.class);
        softly.assertThat(memo.getProgrammerManager().getGlobalProgrammer()).isExactlyInstanceOf(LI100XNetProgrammer.class);
        softly.assertThat(memo.getProgrammerManager().getAddressedProgrammer(false, 42)).isExactlyInstanceOf(XNetOpsModeProgrammer.class);
        softly.assertThat(memo.getTurnoutManager()).isExactlyInstanceOf(XNetTurnoutManager.class);
        softly.assertThat(memo.getSensorManager()).isExactlyInstanceOf(XNetSensorManager.class);
        softly.assertThat(memo.getLightManager()).isExactlyInstanceOf(XNetLightManager.class);
        softly.assertThat(memo.getConsistManager()).isExactlyInstanceOf(XNetConsistManager.class);
        softly.assertAll();
        jmri.util.JUnitAppender.assertWarnMessage("Command Station disconnected, or powered down assuming LZ100/LZV100 V3.x");
    }

    @Test
    public void testUnsupportedSoftwareVersionResponse() {
        Mockito.when(cs.getCommandStationSoftwareVersion()).thenReturn(2.0f);
        Mockito.when(cs.getCommandStationSoftwareVersionBCD()).thenReturn(2.0f);
        Mockito.when(cs.getCommandStationType()).thenReturn(0x00);
        Mockito.when(cs.isOpsModePossible()).thenReturn(true);
        new XNetInitializationManager().memo(memo).setDefaults().versionCheck().setTimeout(50).programmer(LI100XNetProgrammer.class).init();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(memo.getCommandStation()).isNull();
        softly.assertThat(memo.getPowerManager()).isExactlyInstanceOf((XNetPowerManager.class));
        softly.assertThat(memo.getThrottleManager()).isExactlyInstanceOf(XNetThrottleManager.class);
        softly.assertThat(memo.getProgrammerManager()).isNull();
        softly.assertThat(memo.getTurnoutManager()).isNull();
        softly.assertThat(memo.getSensorManager()).isNull();
        softly.assertThat(memo.getLightManager()).isNull();
        softly.assertThat(memo.getConsistManager()).isNull();
        softly.assertAll();
        jmri.util.JUnitAppender.assertWarnMessage("Command Station does not support XpressNet Version 3 Command Set");
    }

    @Test
    public void testVersion35LZ100Response() {
        Mockito.when(cs.getCommandStationSoftwareVersion()).thenReturn(3.5f);
        Mockito.when(cs.getCommandStationSoftwareVersionBCD()).thenReturn(3.5f);
        Mockito.when(cs.getCommandStationType()).thenReturn(0x00);
        Mockito.when(cs.isOpsModePossible()).thenReturn(true);
        new XNetInitializationManager().memo(memo).setDefaults().versionCheck().setTimeout(50).programmer(LI100XNetProgrammer.class).init();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(memo.getCommandStation()).isEqualTo(cs);
        softly.assertThat(memo.getPowerManager()).isExactlyInstanceOf((XNetPowerManager.class));
        softly.assertThat(memo.getThrottleManager()).isExactlyInstanceOf(XNetThrottleManager.class);
        softly.assertThat(memo.getProgrammerManager()).isExactlyInstanceOf(XNetProgrammerManager.class);
        softly.assertThat(memo.getProgrammerManager().getGlobalProgrammer()).isExactlyInstanceOf(LI100XNetProgrammer.class);
        softly.assertThat(memo.getProgrammerManager().getAddressedProgrammer(false, 42)).isExactlyInstanceOf(XNetOpsModeProgrammer.class);
        softly.assertThat(memo.getTurnoutManager()).isExactlyInstanceOf(XNetTurnoutManager.class);
        softly.assertThat(memo.getSensorManager()).isExactlyInstanceOf(XNetSensorManager.class);
        softly.assertThat(memo.getLightManager()).isExactlyInstanceOf(XNetLightManager.class);
        softly.assertThat(memo.getConsistManager()).isExactlyInstanceOf(XNetConsistManager.class);
        softly.assertAll();
    }

    @Test
    public void testVersion3LH200Response() {
        Mockito.when(cs.getCommandStationSoftwareVersion()).thenReturn(3.0f);
        Mockito.when(cs.getCommandStationSoftwareVersionBCD()).thenReturn(3.0f);
        Mockito.when(cs.getCommandStationType()).thenReturn(0x01);
        Mockito.when(cs.isOpsModePossible()).thenReturn(false);
        new XNetInitializationManager().memo(memo).setDefaults().versionCheck().setTimeout(50).programmer(LI100XNetProgrammer.class).init();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(memo.getCommandStation()).isNull();
        softly.assertThat(memo.getPowerManager()).isExactlyInstanceOf((XNetPowerManager.class));
        softly.assertThat(memo.getThrottleManager()).isExactlyInstanceOf(XNetThrottleManager.class);
        softly.assertThat(memo.getProgrammerManager()).isNull();
        softly.assertThat(memo.getTurnoutManager()).isNull();
        softly.assertThat(memo.getSensorManager()).isNull();
        softly.assertThat(memo.getLightManager()).isNull();
        softly.assertThat(memo.getConsistManager()).isNull();
        softly.assertAll();
    }

    @Test
    public void testVersion32CompactResponse() {
        Mockito.when(cs.getCommandStationSoftwareVersion()).thenReturn(3.2f);
        Mockito.when(cs.getCommandStationSoftwareVersionBCD()).thenReturn(3.2f);
        Mockito.when(cs.getCommandStationType()).thenReturn(0x02);
        Mockito.when(cs.isOpsModePossible()).thenReturn(false);
        new XNetInitializationManager().memo(memo).setDefaults().versionCheck().setTimeout(50).programmer(LI100XNetProgrammer.class).init();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(memo.getCommandStation()).isNull();
        softly.assertThat(memo.getPowerManager()).isExactlyInstanceOf((XNetPowerManager.class));
        softly.assertThat(memo.getThrottleManager()).isExactlyInstanceOf(XNetThrottleManager.class);
        softly.assertThat(memo.getProgrammerManager()).isNull();
        softly.assertThat(memo.getTurnoutManager()).isExactlyInstanceOf(XNetTurnoutManager.class);
        softly.assertThat(memo.getSensorManager()).isNull();
        softly.assertThat(memo.getLightManager()).isExactlyInstanceOf(XNetLightManager.class);
        softly.assertThat(memo.getConsistManager()).isExactlyInstanceOf(XNetConsistManager.class);
        softly.assertAll();
    }

    @Test
    public void testVersion3LokMausResponse() {
        Mockito.when(cs.getCommandStationSoftwareVersion()).thenReturn(3.0f);
        Mockito.when(cs.getCommandStationSoftwareVersionBCD()).thenReturn(3.0f);
        Mockito.when(cs.getCommandStationType()).thenReturn(0x04);
        Mockito.when(cs.isOpsModePossible()).thenReturn(true);
        new XNetInitializationManager().memo(memo).setDefaults().versionCheck().setTimeout(50).programmer(LI100XNetProgrammer.class).init();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(memo.getCommandStation()).isEqualTo(cs);
        softly.assertThat(memo.getPowerManager()).isExactlyInstanceOf((XNetPowerManager.class));
        softly.assertThat(memo.getThrottleManager()).isExactlyInstanceOf(RocoXNetThrottleManager.class);
        softly.assertThat(memo.getProgrammerManager()).isExactlyInstanceOf(XNetProgrammerManager.class);
        softly.assertThat(memo.getProgrammerManager().getGlobalProgrammer()).isExactlyInstanceOf(LI100XNetProgrammer.class);
        softly.assertThat(memo.getProgrammerManager().getAddressedProgrammer(false, 42)).isExactlyInstanceOf(XNetOpsModeProgrammer.class);
        softly.assertThat(memo.getTurnoutManager()).isExactlyInstanceOf(XNetTurnoutManager.class);
        softly.assertThat(memo.getSensorManager()).isExactlyInstanceOf(XNetSensorManager.class);
        softly.assertThat(memo.getLightManager()).isExactlyInstanceOf(XNetLightManager.class);
        softly.assertThat(memo.getConsistManager()).isExactlyInstanceOf(NmraConsistManager.class);
        softly.assertAll();
    }

    @Test
    public void testVersion3MultiMausResponse() {
        Mockito.when(cs.getCommandStationSoftwareVersion()).thenReturn(3.0f);
        Mockito.when(cs.getCommandStationSoftwareVersionBCD()).thenReturn(3.0f);
        Mockito.when(cs.getCommandStationType()).thenReturn(0x10);
        Mockito.when(cs.isOpsModePossible()).thenReturn(true);
        new XNetInitializationManager().memo(memo).setDefaults().versionCheck().setTimeout(50).programmer(LI100XNetProgrammer.class).init();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(memo.getCommandStation()).isEqualTo(cs);
        softly.assertThat(memo.getPowerManager()).isExactlyInstanceOf((XNetPowerManager.class));
        softly.assertThat(memo.getThrottleManager()).isExactlyInstanceOf(RocoXNetThrottleManager.class);
        softly.assertThat(memo.getProgrammerManager()).isExactlyInstanceOf(XNetProgrammerManager.class);
        softly.assertThat(memo.getProgrammerManager().getGlobalProgrammer()).isExactlyInstanceOf(LI100XNetProgrammer.class);
        softly.assertThat(memo.getProgrammerManager().getAddressedProgrammer(false, 42)).isExactlyInstanceOf(XNetOpsModeProgrammer.class);
        softly.assertThat(memo.getTurnoutManager()).isExactlyInstanceOf(XNetTurnoutManager.class);
        softly.assertThat(memo.getSensorManager()).isExactlyInstanceOf(XNetSensorManager.class);
        softly.assertThat(memo.getLightManager()).isExactlyInstanceOf(XNetLightManager.class);
        softly.assertThat(memo.getConsistManager()).isExactlyInstanceOf(NmraConsistManager.class);
        softly.assertAll();
    }

    @Test
    public void testVersion35UnknownCSTypeResponse() {
        Mockito.when(cs.getCommandStationSoftwareVersion()).thenReturn(3.5f);
        Mockito.when(cs.getCommandStationSoftwareVersionBCD()).thenReturn(3.5f);
        Mockito.when(cs.getCommandStationType()).thenReturn(0x42);
        Mockito.when(cs.isOpsModePossible()).thenReturn(true);
        new XNetInitializationManager().memo(memo).setDefaults().versionCheck().setTimeout(50).programmer(LI100XNetProgrammer.class).init();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(memo.getCommandStation()).isEqualTo(cs);
        softly.assertThat(memo.getPowerManager()).isExactlyInstanceOf((XNetPowerManager.class));
        softly.assertThat(memo.getThrottleManager()).isExactlyInstanceOf(XNetThrottleManager.class);
        softly.assertThat(memo.getProgrammerManager()).isExactlyInstanceOf(XNetProgrammerManager.class);
        softly.assertThat(memo.getProgrammerManager().getGlobalProgrammer()).isExactlyInstanceOf(LI100XNetProgrammer.class);
        softly.assertThat(memo.getProgrammerManager().getAddressedProgrammer(false, 42)).isExactlyInstanceOf(XNetOpsModeProgrammer.class);
        softly.assertThat(memo.getTurnoutManager()).isExactlyInstanceOf(XNetTurnoutManager.class);
        softly.assertThat(memo.getSensorManager()).isExactlyInstanceOf(XNetSensorManager.class);
        softly.assertThat(memo.getLightManager()).isExactlyInstanceOf(XNetLightManager.class);
        softly.assertThat(memo.getConsistManager()).isExactlyInstanceOf(XNetConsistManager.class);
        softly.assertAll();
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        tc = Mockito.mock(XNetTrafficController.class);
        cs = Mockito.mock(LenzCommandStation.class);
        Mockito.when(tc.getCommandStation()).thenReturn(cs);
        memo = new XNetSystemConnectionMemo(tc);
    }

    @AfterEach
    public void tearDown() {
        memo = null;
        tc = null;
        cs = null;
        jmri.util.JUnitUtil.tearDown();
    }

}
