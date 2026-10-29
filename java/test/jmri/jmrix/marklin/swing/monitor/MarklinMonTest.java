package jmri.jmrix.marklin.swing.monitor;

import jmri.jmrix.marklin.MarklinReply;
import jmri.jmrix.marklin.mcan.cs2.Cs2SystemCommands;
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
        int[] raw = Cs2SystemCommands.buildSystemStop();
        MarklinReply r = new MarklinReply(raw);
        String result = MarklinMon.displayReply(r);
        Assertions.assertTrue(result.contains("System Stop"), "Expected 'System Stop' in: " + result);
        Assertions.assertTrue(result.contains("Priority"), "Expected priority label in: " + result);
        Assertions.assertTrue(result.contains("Broadcast"), "Expected 'Broadcast' in: " + result);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private static final Logger log = LoggerFactory.getLogger(MarklinMonTest.class);

}
