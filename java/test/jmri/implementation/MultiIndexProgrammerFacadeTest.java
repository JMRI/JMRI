package jmri.implementation;

import jmri.ProgListener;
import jmri.Programmer;
import jmri.progdebugger.ProgDebugger;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test the MultiIndexProgrammerFacade class.
 *
 * @author	Bob Jacobsen Copyright 2013
 * 
 */
public class MultiIndexProgrammerFacadeTest extends TestCase {

    int readValue = -2;
    boolean replied = false;

    public void testWriteReadDirect() throws jmri.ProgrammerException, InterruptedException {

        ProgDebugger dp = new ProgDebugger();
        Programmer p = new MultiIndexProgrammerFacade(dp, "81", null, true, false);
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
        Assert.assertEquals("target written", 12, dp.getCvVal(4));
        Assert.assertTrue("index not written", !dp.hasBeenWritten(81));

        p.readCV("4", l);
        waitReply();
        Assert.assertEquals("read back", 12, readValue);
        Assert.assertTrue("index not written", !dp.hasBeenWritten(81));
    }

    public void testWriteReadDirectSkip() throws jmri.ProgrammerException, InterruptedException {

        ProgDebugger dp = new ProgDebugger();
        Programmer p = new MultiIndexProgrammerFacade(dp, "81", null, true, true);
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
        Assert.assertEquals("target written", 12, dp.getCvVal(4));
        Assert.assertTrue("index not written", !dp.hasBeenWritten(81));

        p.readCV("4", l);
        waitReply();
        Assert.assertEquals("read back", 12, readValue);
        Assert.assertTrue("index not written", !dp.hasBeenWritten(81));
    }

    public void testWriteReadSingleIndexed() throws jmri.ProgrammerException, InterruptedException {

        ProgDebugger dp = new ProgDebugger();
        Programmer p = new MultiIndexProgrammerFacade(dp, "81", "82", true, false);
        ProgListener l = new ProgListener() {
            @Override
            public void programmingOpReply(int value, int status) {
                log.debug("callback value=" + value + " status=" + status);
                replied = true;
                readValue = value;
            }
        };

        p.writeCV("123.45", 12, l);
        waitReply();
        Assert.assertEquals("index 1 written", 45, dp.getCvVal(81));
        Assert.assertTrue("index 2 not written", !dp.hasBeenWritten(82));
        Assert.assertEquals("value written", 12, dp.getCvVal(123));

        dp.clearHasBeenWritten(81);
        dp.clearHasBeenWritten(82);

        p.readCV("123.45", l);
        waitReply();
        Assert.assertEquals("read back", 12, readValue);
        Assert.assertEquals("index 1 written", 45, dp.getCvVal(81));
        Assert.assertTrue("index 2 not written", !dp.hasBeenWritten(82));
        Assert.assertEquals("index written", 45, dp.getCvVal(81));
    }

    public void testWriteReadSingleIndexedSkip() throws jmri.ProgrammerException, InterruptedException {

        ProgDebugger dp = new ProgDebugger();
        Programmer p = new MultiIndexProgrammerFacade(dp, "81", "82", true, true);
        ProgListener l = new ProgListener() {
            @Override
            public void programmingOpReply(int value, int status) {
                log.debug("callback value=" + value + " status=" + status);
                replied = true;
                readValue = value;
            }
        };

        p.writeCV("123.45", 12, l);
        waitReply();
        Assert.assertEquals("index 1 written", 45, dp.getCvVal(81));
        Assert.assertTrue("index 2 not written", !dp.hasBeenWritten(82));
        Assert.assertEquals("value written", 12, dp.getCvVal(123));

        dp.clearHasBeenWritten(81);
        dp.clearHasBeenWritten(82);

        p.readCV("123.45", l);
        waitReply();
        Assert.assertEquals("read back", 12, readValue);
        Assert.assertTrue("index 1 not written", !dp.hasBeenWritten(81));
        Assert.assertTrue("index 2 not written", !dp.hasBeenWritten(82));
    }

    public void testWriteReadSingleIndexedCvLast() throws jmri.ProgrammerException, InterruptedException {

        ProgDebugger dp = new ProgDebugger();
        Programmer p = new MultiIndexProgrammerFacade(dp, "81", "82", false, false);
        ProgListener l = new ProgListener() {
            @Override
            public void programmingOpReply(int value, int status) {
                log.debug("callback value=" + value + " status=" + status);
                replied = true;
                readValue = value;
            }
        };

        p.writeCV("45.123", 12, l);
        waitReply();
        Assert.assertEquals("index 1 written", 45, dp.getCvVal(81));
        Assert.assertTrue("index 2 not written", !dp.hasBeenWritten(82));
        Assert.assertEquals("value written", 12, dp.getCvVal(123));

        dp.clearHasBeenWritten(81);
        dp.clearHasBeenWritten(82);

        p.readCV("45.123", l);
        waitReply();
        Assert.assertEquals("read back", 12, readValue);
        Assert.assertEquals("index 1 written", 45, dp.getCvVal(81));
        Assert.assertTrue("index 2 not written", !dp.hasBeenWritten(82));
        Assert.assertEquals("index written", 45, dp.getCvVal(81));
    }

    public void testWriteReadSingleIndexedCvLastSkip() throws jmri.ProgrammerException, InterruptedException {

        ProgDebugger dp = new ProgDebugger();
        Programmer p = new MultiIndexProgrammerFacade(dp, "81", "82", false, true);
        ProgListener l = new ProgListener() {
            @Override
            public void programmingOpReply(int value, int status) {
                log.debug("callback value=" + value + " status=" + status);
                replied = true;
                readValue = value;
            }
        };

        p.writeCV("45.123", 12, l);
        waitReply();
        Assert.assertEquals("index 1 written", 45, dp.getCvVal(81));
        Assert.assertTrue("index 2 not written", !dp.hasBeenWritten(82));
        Assert.assertEquals("value written", 12, dp.getCvVal(123));

        dp.clearHasBeenWritten(81);
        dp.clearHasBeenWritten(82);

        p.readCV("45.123", l);
        waitReply();
        Assert.assertEquals("read back", 12, readValue);
        Assert.assertTrue("index 1 not written", !dp.hasBeenWritten(81));
        Assert.assertTrue("index 2 not written", !dp.hasBeenWritten(82));
    }

    public void testWriteReadDoubleIndexed() throws jmri.ProgrammerException, InterruptedException {

        ProgDebugger dp = new ProgDebugger();
        Programmer p = new MultiIndexProgrammerFacade(dp, "81", "82", true, false);
        ProgListener l = new ProgListener() {
            @Override
            public void programmingOpReply(int value, int status) {
                log.debug("callback value=" + value + " status=" + status);
                replied = true;
                readValue = value;
            }
        };

        p.writeCV("123.45.46", 12, l);
        waitReply();
        Assert.assertEquals("index 1 written", 45, dp.getCvVal(81));
        Assert.assertEquals("index 2 written", 46, dp.getCvVal(82));
        Assert.assertEquals("value written", 12, dp.getCvVal(123));

        dp.clearHasBeenWritten(81);
        dp.clearHasBeenWritten(82);

        p.readCV("123.45.46", l);
        waitReply();
        Assert.assertEquals("read back", 12, readValue);
        Assert.assertEquals("index 1 written", 45, dp.getCvVal(81));
        Assert.assertEquals("index 2 written", 46, dp.getCvVal(82));
    }

    public void testWriteReadDoubleIndexedSkip() throws jmri.ProgrammerException, InterruptedException {

        ProgDebugger dp = new ProgDebugger();
        Programmer p = new MultiIndexProgrammerFacade(dp, "81", "82", true, true);
        ProgListener l = new ProgListener() {
            @Override
            public void programmingOpReply(int value, int status) {
                log.debug("callback value=" + value + " status=" + status);
                replied = true;
                readValue = value;
            }
        };

        p.writeCV("123.45.46", 12, l);
        waitReply();
        Assert.assertEquals("index 1 written", 45, dp.getCvVal(81));
        Assert.assertEquals("index 2 written", 46, dp.getCvVal(82));
        Assert.assertEquals("value written", 12, dp.getCvVal(123));

        dp.clearHasBeenWritten(81);
        dp.clearHasBeenWritten(82);

        p.readCV("123.45.46", l);
        waitReply();
        Assert.assertEquals("read back", 12, readValue);
        Assert.assertTrue("index 1 not written", !dp.hasBeenWritten(81));
        Assert.assertTrue("index 2 not written", !dp.hasBeenWritten(82));
    }

    public void testWriteReadDoubleIndexedCvList() throws jmri.ProgrammerException, InterruptedException {

        ProgDebugger dp = new ProgDebugger();
        Programmer p = new MultiIndexProgrammerFacade(dp, "81", "82", false, false);
        ProgListener l = new ProgListener() {
            @Override
            public void programmingOpReply(int value, int status) {
                log.debug("callback value=" + value + " status=" + status);
                replied = true;
                readValue = value;
            }
        };

        p.writeCV("45.46.123", 12, l);
        waitReply();
        Assert.assertEquals("index 1 written", 45, dp.getCvVal(81));
        Assert.assertEquals("index 2 written", 46, dp.getCvVal(82));
        Assert.assertEquals("value written", 12, dp.getCvVal(123));

        dp.clearHasBeenWritten(81);
        dp.clearHasBeenWritten(82);

        p.readCV("45.46.123", l);
        waitReply();
        Assert.assertEquals("read back", 12, readValue);
        Assert.assertEquals("index 1 written", 45, dp.getCvVal(81));
        Assert.assertEquals("index 2 written", 46, dp.getCvVal(82));

        dp.clearHasBeenWritten(81);
        dp.clearHasBeenWritten(82);

        p.readCV("45.46.123", l);
        waitReply();
        Assert.assertEquals("read back", 12, readValue);
        Assert.assertEquals("index 1 written", 45, dp.getCvVal(81));
        Assert.assertEquals("index 2 written", 46, dp.getCvVal(82));

        dp.clearHasBeenWritten(81);
        dp.clearHasBeenWritten(82);

        p.readCV("45.46.123", l);
        waitReply();
        Assert.assertEquals("read back", 12, readValue);
        Assert.assertEquals("index 1 written", 45, dp.getCvVal(81));
        Assert.assertEquals("index 2 written", 46, dp.getCvVal(82));

    }

    public void testWriteReadDoubleIndexedCvListSkip() throws jmri.ProgrammerException, InterruptedException {

        ProgDebugger dp = new ProgDebugger();
        MultiIndexProgrammerFacade p = new MultiIndexProgrammerFacade(dp, "81", "82", false, true);
        ProgListener l = new ProgListener() {
            @Override
            public void programmingOpReply(int value, int status) {
                log.debug("callback value=" + value + " status=" + status);
                replied = true;
                readValue = value;
            }
        };

        p.writeCV("45.46.123", 12, l);
        waitReply();
        Assert.assertEquals("index 1 written", 45, dp.getCvVal(81));
        Assert.assertEquals("index 2 written", 46, dp.getCvVal(82));
        Assert.assertEquals("value written", 12, dp.getCvVal(123));

        dp.clearHasBeenWritten(81);
        dp.clearHasBeenWritten(82);

        p.readCV("45.46.123", l);
        waitReply();
        Assert.assertEquals("read back", 12, readValue);
        Assert.assertTrue("index 1 not written", !dp.hasBeenWritten(81));
        Assert.assertTrue("index 2 not written", !dp.hasBeenWritten(82));

        dp.clearHasBeenWritten(81);
        dp.clearHasBeenWritten(82);

        p.readCV("45.46.123", l);
        waitReply();
        Assert.assertEquals("read back", 12, readValue);
        Assert.assertTrue("index 1 not written", !dp.hasBeenWritten(81));
        Assert.assertTrue("index 2 not written", !dp.hasBeenWritten(82));

        dp.clearHasBeenWritten(81);
        dp.clearHasBeenWritten(82);

        p.readCV("45.46.123", l);
        waitReply();
        Assert.assertEquals("read back", 12, readValue);
        Assert.assertTrue("index 1 not written", !dp.hasBeenWritten(81));
        Assert.assertTrue("index 2 not written", !dp.hasBeenWritten(82));

        // add timeout
        p.lastOpTime = p.lastOpTime - 2*p.maxDelay; 
        
        dp.clearHasBeenWritten(81);
        dp.clearHasBeenWritten(82);

        p.readCV("45.46.123", l);
        waitReply();
        Assert.assertEquals("read back", 12, readValue);
        Assert.assertEquals("index 1 written", 45, dp.getCvVal(81));
        Assert.assertEquals("index 2 written", 46, dp.getCvVal(82));
    }

    public void testWriteReadDoubleIndexedCvListDelayedSkip() throws jmri.ProgrammerException, InterruptedException {

        ProgDebugger dp = new ProgDebugger();
        MultiIndexProgrammerFacade p = new MultiIndexProgrammerFacade(dp, "81", "82", false, true);
        ProgListener l = new ProgListener() {
            @Override
            public void programmingOpReply(int value, int status) {
                log.debug("callback value=" + value + " status=" + status);
                replied = true;
                readValue = value;
            }
        };

        p.writeCV("45.46.123", 12, l);
        waitReply();
        Assert.assertEquals("index 1 written", 45, dp.getCvVal(81));
        Assert.assertEquals("index 2 written", 46, dp.getCvVal(82));
        Assert.assertEquals("value written", 12, dp.getCvVal(123));

        dp.clearHasBeenWritten(81);
        dp.clearHasBeenWritten(82);
        
        // pretend too long has elapsed, so should still program
        p.lastOpTime = p.lastOpTime - 2*p.maxDelay; 

        p.readCV("45.46.123", l);
        waitReply();
        Assert.assertEquals("read back", 12, readValue);
        Assert.assertEquals("index 1 written", 45, dp.getCvVal(81));
        Assert.assertEquals("index 2 written", 46, dp.getCvVal(82));
    }

    public void testCvLimit() {
        ProgDebugger dp = new ProgDebugger();
        dp.setTestReadLimit(1024);
        dp.setTestWriteLimit(1024);

        Programmer p = new MultiIndexProgrammerFacade(dp, "81", null, true, false);

        Assert.assertTrue("CV limit read OK", p.getCanRead("1024"));
        Assert.assertTrue("CV limit write OK", p.getCanWrite("1024"));
        Assert.assertTrue("CV limit read fail", !p.getCanRead("1025"));
        Assert.assertTrue("CV limit write fail", !p.getCanWrite("1025"));
    }

    // from here down is testing infrastructure
    synchronized void waitReply() throws InterruptedException {
        while (!replied) {
            wait(200);
        }
        replied = false;
    }

    // from here down is testing infrastructure
    public MultiIndexProgrammerFacadeTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {MultiIndexProgrammerFacadeTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite(MultiIndexProgrammerFacadeTest.class);
        return suite;
    }

    private final static Logger log = LoggerFactory.getLogger(MultiIndexProgrammerFacadeTest.class);

}
