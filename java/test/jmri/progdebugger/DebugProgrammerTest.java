package jmri.progdebugger;

import jmri.ProgListener;
import jmri.Programmer;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test the DebugProgrammer class.
 *
 * @author	Bob Jacobsen Copyright 2013
 */
public class DebugProgrammerTest extends TestCase {

    int readValue = -2;
    boolean replied = false;

    public void testWriteRead() throws jmri.ProgrammerException, InterruptedException {
        Programmer p = new ProgDebugger();
        ProgListener l = new ProgListener() {
            public void programmingOpReply(int value, int status) {
                log.debug("callback value=" + value + " status=" + status);
                replied = true;
                readValue = value;
            }
        };
        p.writeCV(4, 12, l);
        waitReply();
        log.debug("readValue is " + readValue);
        p.readCV(4, l);
        waitReply();
        log.debug("readValue is " + readValue);
        Assert.assertEquals("read back", 12, readValue);
    }

    // Test names ending with "String" are for the new writeCV(String, ...) 
    // etc methods.  If you remove the older writeCV(int, ...) tests, 
    // you can rename these. Note that not all (int,...) tests may have a 
    // String(String, ...) test defined, in which case you should create those.
    public void testWriteReadString() throws jmri.ProgrammerException, InterruptedException {
        Programmer p = new ProgDebugger();
        ProgListener l = new ProgListener() {
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

    public void testCvLimit() {
        Programmer p = new jmri.progdebugger.ProgDebugger();
        Assert.assertTrue("CV limit read", p.getCanRead("256"));
        Assert.assertTrue("CV limit write", p.getCanWrite("256"));
        Assert.assertTrue("CV limit read", !p.getCanRead("257"));
        Assert.assertTrue("CV limit write", !p.getCanWrite("257"));
    }

    public void testKnowsWrite() throws jmri.ProgrammerException {
        ProgDebugger p = new ProgDebugger();
        ProgListener l = new ProgListener() {
            public void programmingOpReply(int value, int status) {
                log.debug("callback value=" + value + " status=" + status);
                replied = true;
                readValue = value;
            }
        };

        Assert.assertTrue("initially not written", !p.hasBeenWritten(4));
        p.writeCV(4, 12, l);
        Assert.assertTrue("after 1st write", p.hasBeenWritten(4));
        p.clearHasBeenWritten(4);
        Assert.assertTrue("now longer written", !p.hasBeenWritten(4));
        p.writeCV(4, 12, l);
        Assert.assertTrue("after 2nd write", p.hasBeenWritten(4));

    }

    public void testKnowsWriteString() throws jmri.ProgrammerException {
        ProgDebugger p = new ProgDebugger();
        ProgListener l = new ProgListener() {
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
        jmri.util.JUnitUtil.waitFor(()->{return replied;}, "reply received");
        replied = false;
    }

    // from here down is testing infrastructure
    public DebugProgrammerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {DebugProgrammerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite(DebugProgrammerTest.class);
        return suite;
    }

    private final static Logger log = LoggerFactory.getLogger(DebugProgrammerTest.class.getName());

}
