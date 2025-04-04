package jmri.jmrix.loconet.hexfile;

import jmri.jmrix.loconet.LocoNetMessage;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class LnHexFilePortTest {

    @Test
    public void testCTor() {
        LnHexFilePort t = new LnHexFilePort();
        Assertions.assertNotNull(t, "exists");
    }

    @Test
    public void testSimReplySetting() {
        LnHexFilePort t = new LnHexFilePort();
        Assertions.assertFalse(t.simReply(), "SimReply off at start");
        t.simReply(true);
        Assertions.assertTrue(t.simReply(), "SimReply turned on");
    }

    @Test
    public void testGenerateLncvReply() {
        Assertions.assertEquals(LnHexFilePort.generateReply(new LocoNetMessage(new int[]{0xED, 0x0F, 0x01, 0x05, 0x00, 0x21, 0x41, 0x29, 0x13, 0x00, 0x00, 0x01, 0x00, 0x00, 0x42})).toMonitorString(),
                "(LNCV) READ_CV_REPLY from module (Article #5033):\n" + "\tCV0 value = 1\n");
    }

    @Test
    public void testGenerateLnsv1Reply() {
        Assertions.assertEquals(LnHexFilePort.generateReply(new LocoNetMessage(new int[]{0xE5, 0x10, 0x50, 0x04, 0x01, 0x00, 0x02, 0x4A, 0x00, 0x00, 0x10, 0x02, 0x00, 0x00, 0x00, 0x05})).toMonitorString(),
                "(LNSV1) LocoIO@4/2 => LocoBuffer: Report SV74 (0x4A) = 76 (0x4C) Firmware rev 1.2.0.\n");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(LnHexFilePortTest.class);

}
