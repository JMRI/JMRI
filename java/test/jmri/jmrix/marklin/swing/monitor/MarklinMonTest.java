package jmri.jmrix.marklin.swing.monitor;

import jmri.jmrix.marklin.MarklinReply;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2024
 */
public class MarklinMonTest {

    @Test
    public void testDisplayReply() {
        MarklinReply r = new MarklinReply();
        Assertions.assertEquals(
            "Priority 1, Stop/Go/Short Command: System Request Message Broadcast0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0"
            , MarklinMon.displayReply(r));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(MarklinMonTest.class);

}
