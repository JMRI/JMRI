package jmri.progdebugger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.ProgListener;
import jmri.Programmer;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test the DebugProgrammer class.
 *
 * @author Bob Jacobsen Copyright 2013
 */
public class DebugProgrammerTest {

    private int readValue = -2;
    private boolean replied = false;

    @Test
    public void testWriteRead() throws jmri.ProgrammerException, InterruptedException {
        Programmer p = new ProgDebugger();
        ProgListener l = new ProgListener() {
            @Override
            public void programmingOpReply(int value, int status) {
                log.debug("callback value={} status={}", value, status);
                replied = true;
                readValue = value;
            }
        };
        p.writeCV("4", 12, l);
        waitReply();
        log.debug("readValue is {}", readValue);
        p.readCV("4", l);
        waitReply();
        log.debug("readValue is {}", readValue);
        assertEquals( 12, readValue, "read back");
    }

    @Test
    public void testCvLimit() {
        Programmer p = new jmri.progdebugger.ProgDebugger();
        assertTrue( p.getCanRead("256"), "CV limit read");
        assertTrue( p.getCanWrite("256"), "CV limit write");
        assertFalse( p.getCanRead("257"), "CV limit read");
        assertFalse( p.getCanWrite("257"), "CV limit write");
    }

    @Test
    public void testKnowsWrite() throws jmri.ProgrammerException {
        ProgDebugger p = new ProgDebugger();
        ProgListener l = new ProgListener() {
            @Override
            public void programmingOpReply(int value, int status) {
                log.debug("callback value={} status={}", value, status);
                replied = true;
                readValue = value;
            }
        };

        assertFalse( p.hasBeenWritten(4), "initially not written");
        p.writeCV("4", 12, l);
        assertTrue( p.hasBeenWritten(4), "after 1st write");
        p.clearHasBeenWritten(4);
        assertFalse( p.hasBeenWritten(4), "now longer written");
        p.writeCV("4", 12, l);
        assertTrue( p.hasBeenWritten(4), "after 2nd write");

    }

    // from here down is testing infrastructure
    private void waitReply() {
        JUnitUtil.waitFor(() -> {
            return replied;
        }, "reply received");
        replied = false;
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DebugProgrammerTest.class);

}
