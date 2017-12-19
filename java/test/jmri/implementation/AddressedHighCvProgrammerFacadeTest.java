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
 * Test the AddressedHighCvProgrammerFacade class.
 *
 * @author	Bob Jacobsen Copyright 2013
 * 
 */
public class AddressedHighCvProgrammerFacadeTest extends TestCase {

    int readValue = -2;
    boolean replied = false;

    public void testWriteReadDirect() throws jmri.ProgrammerException, InterruptedException {

        ProgDebugger dp = new ProgDebugger();
        dp.setTestReadLimit(256);
        dp.setTestWriteLimit(256);

        Programmer p = new AddressedHighCvProgrammerFacade(dp, "256", "253", "254", "255", "100");
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
        Assert.assertTrue("index H not written", !dp.hasBeenWritten(253));
        Assert.assertTrue("index L not written", !dp.hasBeenWritten(254));
        Assert.assertTrue("index val not written", !dp.hasBeenWritten(255));

        p.readCV(4, l);
        waitReply();
        Assert.assertEquals("read back", 12, readValue);
    }

    public void testWriteReadDirectHighCV() throws jmri.ProgrammerException, InterruptedException {

        ProgDebugger dp = new ProgDebugger();
        dp.setTestReadLimit(1024);
        dp.setTestWriteLimit(1024);

        Programmer p = new AddressedHighCvProgrammerFacade(dp, "256", "253", "254", "255", "100");
        ProgListener l = new ProgListener() {
            @Override
            public void programmingOpReply(int value, int status) {
                log.debug("callback value=" + value + " status=" + status);
                replied = true;
                readValue = value;
            }
        };
        p.writeCV("258", 12, l);
        waitReply();
        Assert.assertEquals("target written", 12, dp.getCvVal(258));
        Assert.assertTrue("index H not written", !dp.hasBeenWritten(253));
        Assert.assertTrue("index L not written", !dp.hasBeenWritten(254));
        Assert.assertTrue("index val not written", !dp.hasBeenWritten(255));

        p.readCV("258", l);
        waitReply();
        Assert.assertEquals("read back", 12, readValue);
    }

    public void testWriteReadDirectHighCVRightSide() throws jmri.ProgrammerException, InterruptedException {

        ProgDebugger dp = new ProgDebugger();
        dp.setTestReadLimit(256);
        dp.setTestWriteLimit(1024);

        Programmer p = new AddressedHighCvProgrammerFacade(dp, "256", "253", "254", "255", "100");
        ProgListener l = new ProgListener() {
            @Override
            public void programmingOpReply(int value, int status) {
                log.debug("callback value=" + value + " status=" + status);
                replied = true;
                readValue = value;
            }
        };
        p.writeCV("258", 12, l);
        waitReply();
        Assert.assertEquals("target written", 12, dp.getCvVal(258));
        Assert.assertTrue("index H not written", !dp.hasBeenWritten(253));
        Assert.assertTrue("index L not written", !dp.hasBeenWritten(254));
        Assert.assertTrue("index val not written", !dp.hasBeenWritten(255));

        dp.setTestReadLimit(1024);
        dp.setTestWriteLimit(256);

        p.readCV("258", l);
        waitReply();
        Assert.assertEquals("read back", 12, readValue);
    }

    public void testWriteReadIndexed() throws jmri.ProgrammerException, InterruptedException {

        ProgDebugger dp = new ProgDebugger();
        dp.setTestReadLimit(256);
        dp.setTestWriteLimit(256);
        Programmer p = new AddressedHighCvProgrammerFacade(dp, "256", "253", "254", "255", "100");
        ProgListener l = new ProgListener() {
            @Override
            public void programmingOpReply(int value, int status) {
                log.debug("callback value=" + value + " status=" + status);
                replied = true;
                readValue = value;
            }
        };
        p.writeCV("258", 12, l);
        waitReply();
        Assert.assertTrue("target not written", !dp.hasBeenWritten(258));
        Assert.assertEquals("index H written", 2, dp.getCvVal(253));
        Assert.assertEquals("index L written", 58, dp.getCvVal(254));
        Assert.assertEquals("value written", 12, dp.getCvVal(255));

        p.readCV("258", l);
        waitReply();
        Assert.assertEquals("read back", 12, readValue);
    }

    public void testCvLimit() {
        ProgDebugger dp = new ProgDebugger();
        dp.setTestReadLimit(256);
        dp.setTestWriteLimit(256);
        Programmer p = new AddressedHighCvProgrammerFacade(dp, "256", "253", "254", "255", "100");
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
    public AddressedHighCvProgrammerFacadeTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {AddressedHighCvProgrammerFacadeTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite(AddressedHighCvProgrammerFacadeTest.class);
        return suite;
    }

    private final static Logger log = LoggerFactory.getLogger(AddressedHighCvProgrammerFacadeTest.class);

}
