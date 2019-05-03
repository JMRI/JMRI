package jmri.implementation;

import jmri.ProgListener;
import jmri.Programmer;
import jmri.progdebugger.ProgDebugger;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test the OffsetHighCvProgrammerFacade class.
 *
 * @author	Bob Jacobsen Copyright 2013
 * 
 */
public class OffsetHighCvProgrammerFacadeTest {

    int readValue = -2;
    boolean replied = false;

    @Test
    public void testWriteReadDirect() throws jmri.ProgrammerException, InterruptedException {

        ProgDebugger dp = new ProgDebugger();
        dp.setTestReadLimit(256);
        dp.setTestWriteLimit(256);

        Programmer p = new OffsetHighCvProgrammerFacade(dp, "256", "7", "10", "100");
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
        Assert.assertTrue("index not written", !dp.hasBeenWritten(7));

        p.readCV("4", l);
        waitReply();
        Assert.assertEquals("read back", 12, readValue);
    }

    @Test
    public void testWriteReadDirectHighCV() throws jmri.ProgrammerException, InterruptedException {

        ProgDebugger dp = new ProgDebugger();
        dp.setTestReadLimit(1024);
        dp.setTestWriteLimit(1024);

        Programmer p = new OffsetHighCvProgrammerFacade(dp, "256", "7", "10", "100");
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
        Assert.assertTrue("index not written", !dp.hasBeenWritten(7));

        p.readCV("258", l);
        waitReply();
        Assert.assertEquals("read back", 12, readValue);
    }

    @Test
    public void testWriteReadIndexed() throws jmri.ProgrammerException, InterruptedException {

        ProgDebugger dp = new ProgDebugger();
        dp.setTestReadLimit(256);
        dp.setTestWriteLimit(256);
        Programmer p = new OffsetHighCvProgrammerFacade(dp, "256", "7", "10", "100");
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
        Assert.assertEquals("index written", 20, dp.getCvVal(7));
        Assert.assertEquals("value written", 12, dp.getCvVal(58));

        p.readCV("258", l);
        waitReply();
        Assert.assertEquals("read back", 12, readValue);
    }

    @Test
    public void testCvLimit() {
        ProgDebugger dp = new ProgDebugger();
        dp.setTestReadLimit(256);
        dp.setTestWriteLimit(256);
        Programmer p = new OffsetHighCvProgrammerFacade(dp, "256", "7", "10", "100");
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

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown(){
        jmri.util.JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(OffsetHighCvProgrammerFacadeTest.class);

}
