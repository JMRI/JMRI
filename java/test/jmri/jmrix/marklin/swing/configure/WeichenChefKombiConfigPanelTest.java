package jmri.jmrix.marklin.swing.configure;

import jmri.jmrix.marklin.McanDevice;
import jmri.jmrix.marklin.McanDeviceIdentity;
import jmri.jmrix.marklin.MarklinSystemConnectionMemo;
import jmri.jmrix.marklin.MarklinTrafficControlScaffold;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link WeichenChefKombiConfigPanel}.
 *
 * @author Ralf Lang (ralf.lang@ralf-lang.de)
 */
class WeichenChefKombiConfigPanelTest {

    private MarklinSystemConnectionMemo memo;
    private MarklinTrafficControlScaffold tc;

    @BeforeEach
    void setUp() {
        JUnitUtil.setUp();
        tc = new MarklinTrafficControlScaffold();
        memo = new MarklinSystemConnectionMemo(tc);
        memo.configureManagers();
    }

    @AfterEach
    void tearDown() {
        tc.dispose();
        memo.dispose();
        JUnitUtil.tearDown();
    }

    @Test
    @jmri.util.junit.annotations.DisabledIfHeadless
    void testPanelCreatesWithoutError() {
        McanDeviceIdentity.CdB id = McanDeviceIdentity.forCdB(0x27, 3);
        McanDevice dev = new McanDevice("M", id);
        WeichenChefKombiConfigPanel panel = new WeichenChefKombiConfigPanel(dev, memo);
        assertNotNull(panel);
    }
}
