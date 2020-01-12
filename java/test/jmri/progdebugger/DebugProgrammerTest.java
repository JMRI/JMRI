package jmri.progdebugger;

import jmri.ProgListener;
import jmri.Programmer;
import org.junit.Test;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test the DebugProgrammer class.
 *
 * @author Bob Jacobsen Copyright 2013
 */
public class DebugProgrammerTest {

    int readValue = -2;
    boolean replied = false;

    @Test
    public void testWriteRead() throws jmri.ProgrammerException, InterruptedException {
        Programmer p = new ProgDebugger();
        ProgListener l = new ProgListener() {
            @Override
            public void programmingOpReply(int value, int status) {
                log.debug("callback value=" + value + " status=" + status);
                replied = true;
                readValue = value;
            }
        };
        p.writeCV("4", 12, l);
        waitReply();
        log.debug("readValue is " + readValue);
        p.readCV("4", l);
        waitReply();
        log.debug("readValue is " + readValue);
        Assert.assertEquals("read back", 12, readValue);
    }

    @Test
    public void testCvLimit() {
        Programmer p = new jmri.progdebugger.ProgDebugger();
        Assert.assertTrue("CV limit read", p.getCanRead("256"));
        Assert.assertTrue("CV limit write", p.getCanWrite("256"));
        Assert.assertTrue("CV limit read", !p.getCanRead("257"));
        Assert.assertTrue("CV limit write", !p.getCanWrite("257"));
    }

    @Test
    public void testKnowsWrite() throws jmri.ProgrammerException {
        ProgDebugger p = new ProgDebugger();
        ProgListener l = new ProgListener() {
            @Override
            public void programmingOpReply(int value, int status) {
                log.debug("callback value=" + value + " status=" + status);
                replied = true;
                readValue = value;
            }
        };

        Assert.assertTrue("initially not written", !p.hasBeenWritten(4));
        p.writeCV("4", 12, l);
        Assert.assertTrue("after 1st write", p.hasBeenWritten(4));
        p.clearHasBeenWritten(4);
        Assert.assertTrue("now longer written", !p.hasBeenWritten(4));
        p.writeCV("4", 12, l);
        Assert.assertTrue("after 2nd write", p.hasBeenWritten(4));

    }

    // from here down is testing infrastructure
    synchronized void waitReply() throws InterruptedException {
        jmri.util.JUnitUtil.waitFor(() -> {
            return replied;
        }, "reply received");
        replied = false;
    }

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(DebugProgrammerTest.class);

}
