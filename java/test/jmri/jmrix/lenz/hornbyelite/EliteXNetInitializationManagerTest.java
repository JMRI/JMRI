package jmri.jmrix.lenz.hornbyelite;

import jmri.implementation.NmraConsistManager;
import jmri.jmrix.lenz.*;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * EliteXNetInitializationManagerTest.java
 *
 * Test for the jmri.jmrix.lenz.EliteXNetInitializationManager
 * class
 *
 * @author Paul Bender
 */
@SuppressWarnings("deprecation")
public class EliteXNetInitializationManagerTest {

    private XNetTrafficController tc;
    private XNetSystemConnectionMemo memo;
    private HornbyEliteCommandStation cs;

    @Test
    public void testCtor() {
        EliteXNetInitializationManager m = new EliteXNetInitializationManager(memo) {
//             {
//                 LI100_INIT_TIMEOUT = 50; // shorten, because this will fail & delay test
//             }
        };
        assertThat(m).withFailMessage("exists").isNotNull();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(memo.getCommandStation()).isEqualTo(cs);
        softly.assertThat(memo.getPowerManager()).isExactlyInstanceOf((XNetPowerManager.class));
        softly.assertThat(memo.getThrottleManager()).isExactlyInstanceOf(EliteXNetThrottleManager.class);
        softly.assertThat(memo.getProgrammerManager()).isExactlyInstanceOf(XNetProgrammerManager.class);
        softly.assertThat(memo.getProgrammerManager().getGlobalProgrammer()).isExactlyInstanceOf(EliteXNetProgrammer.class);
        softly.assertThat(memo.getProgrammerManager().getAddressedProgrammer(false,42)).isExactlyInstanceOf(XNetOpsModeProgrammer.class);
        softly.assertThat(memo.getTurnoutManager()).isExactlyInstanceOf(EliteXNetTurnoutManager.class);
        softly.assertThat(memo.getSensorManager()).isNull();
        softly.assertThat(memo.getLightManager()).isExactlyInstanceOf(XNetLightManager.class);
        softly.assertThat(memo.getConsistManager()).isExactlyInstanceOf(NmraConsistManager.class);
        softly.assertAll();
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        tc = Mockito.mock(XNetTrafficController.class);
        cs = Mockito.mock(HornbyEliteCommandStation.class);
        Mockito.when(cs.isOpsModePossible()).thenReturn(true);
        Mockito.when(tc.getCommandStation()).thenReturn(cs);
        memo = new EliteXNetSystemConnectionMemo(tc);
    }

    @AfterEach
    public void tearDown() {
        memo = null;
        tc = null;
        cs = null;
        jmri.util.JUnitUtil.tearDown();
    }
}
