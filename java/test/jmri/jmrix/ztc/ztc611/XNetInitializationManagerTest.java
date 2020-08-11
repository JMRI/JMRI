package jmri.jmrix.ztc.ztc611;

import jmri.jmrix.lenz.*;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;


/**
 * Test for the XNetInitializationManager when configured for the ZTC611.
 *
 * @author Paul Bender
 */
public class XNetInitializationManagerTest {

    private XNetTrafficController tc;
    private XNetSystemConnectionMemo memo;
    private LenzCommandStation cs;

    @Test
    public void testCtor() {
        new XNetInitializationManager()
                .memo(memo)
                .setDefaults()
                .turnoutManager(ZTC611XNetTurnoutManager.class)
                .init();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(memo.getCommandStation()).isEqualTo(cs);
        softly.assertThat(memo.getPowerManager()).isExactlyInstanceOf((XNetPowerManager.class));
        softly.assertThat(memo.getThrottleManager()).isExactlyInstanceOf(XNetThrottleManager.class);
        softly.assertThat(memo.getProgrammerManager()).isExactlyInstanceOf(XNetProgrammerManager.class);
        softly.assertThat(memo.getProgrammerManager().getGlobalProgrammer()).isExactlyInstanceOf(XNetProgrammer.class);
        softly.assertThat(memo.getProgrammerManager().getAddressedProgrammer(false,42)).isExactlyInstanceOf(XNetOpsModeProgrammer.class);
        softly.assertThat(memo.getTurnoutManager()).isExactlyInstanceOf(ZTC611XNetTurnoutManager.class);
        softly.assertThat(memo.getSensorManager()).isExactlyInstanceOf(XNetSensorManager.class);
        softly.assertThat(memo.getLightManager()).isExactlyInstanceOf(XNetLightManager.class);
        softly.assertThat(memo.getConsistManager()).isExactlyInstanceOf(XNetConsistManager.class);
        softly.assertAll();
    }

    @BeforeEach
    public void setUp() throws Exception {
        jmri.util.JUnitUtil.setUp();
        tc = Mockito.mock(XNetTrafficController.class);
        cs = Mockito.mock(LenzCommandStation.class);
        Mockito.when(cs.isOpsModePossible()).thenReturn(true);
        Mockito.when(tc.getCommandStation()).thenReturn(cs);
        memo = new XNetSystemConnectionMemo(tc);
    }

    @AfterEach
    public void tearDown() throws Exception {
        memo = null;
        tc = null;
        cs = null;
        jmri.util.JUnitUtil.tearDown();
    }

}
