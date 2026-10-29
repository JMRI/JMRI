package jmri.jmrix.marklin.swing.configure;

import jmri.jmrix.marklin.McanDevice;
import jmri.jmrix.marklin.McanDeviceIdentity;
import jmri.jmrix.marklin.MarklinSystemConnectionMemo;
import jmri.jmrix.marklin.MarklinTrafficControlScaffold;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link ReporterConfigPanel}.
 *
 * @author Ralf Lang (ralf.lang@ralf-lang.de)
 */
class ReporterConfigPanelTest {

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
        McanDeviceIdentity.CdB id = McanDeviceIdentity.forCdB(0x0D, 1);
        McanDevice dev = new McanDevice("M", id);
        ReporterConfigPanel panel = new ReporterConfigPanel(dev, memo);
        assertNotNull(panel);
    }

    @Test
    void testFreezeRowHiddenForGleisReporter() {
        McanDeviceIdentity.CdB id = McanDeviceIdentity.forCdB(0x0D, 1);
        McanDevice dev = new McanDevice("M", id);
        ReporterConfigPanel panel = new ReporterConfigPanel(dev, null);
        assertFalse(panel.isFreezeRowVisible());
    }

    @Test
    void testFreezeRowVisibleForStromSniffer() {
        McanDeviceIdentity.CdB id = McanDeviceIdentity.forCdB(0x0B, 2);
        McanDevice dev = new McanDevice("M", id);
        ReporterConfigPanel panel = new ReporterConfigPanel(dev, null);
        assertTrue(panel.isFreezeRowVisible());
    }
}
