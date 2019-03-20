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
 *
 * @author	Bob Jacobsen Copyright 2014
 * 
 */
public class TwoIndexTcsProgrammerFacadeTest {

    int readValue = -2;
    boolean replied = false;

    @Test
    public void testWriteReadDirect() throws jmri.ProgrammerException, InterruptedException {

        ProgDebugger dp = new ProgDebugger();
        Programmer p = new TwoIndexTcsProgrammerFacade(dp);
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

    @Test
    public void testWriteReadDoubleIndexed() throws jmri.ProgrammerException, InterruptedException {

        ProgDebugger dp = new ProgDebugger();
        Programmer p = new TwoIndexTcsProgrammerFacade(dp);
        ProgListener l = new ProgListener() {
            @Override
            public void programmingOpReply(int value, int status) {
                log.debug("callback value=" + value + " status=" + status);
                replied = true;
                readValue = value;
            }
        };

        p.writeCV("T2CV.10.20", 12 * 256 + 13, l);
        waitReply();
        Assert.assertEquals("index 1 written", 10, dp.getCvVal(201));
        Assert.assertEquals("index 2 written", 20, dp.getCvVal(202));
        Assert.assertEquals("value MSB written", 12, dp.getCvVal(203));
        Assert.assertEquals("value LSB written", 13, dp.getCvVal(204));

        dp.clearHasBeenWritten(201);
        dp.clearHasBeenWritten(202);
        dp.clearHasBeenWritten(203);
        dp.clearHasBeenWritten(204);
        dp.resetCv(203, 12);
        dp.resetCv(204, 20);

        p.readCV("T2CV.10.20", l);
        waitReply();
        Assert.assertEquals("index 1 written", 100 + 10, dp.getCvVal(201));
        Assert.assertEquals("index 2 written", 20, dp.getCvVal(202));
        Assert.assertEquals("dummy 204 written", 100, dp.getCvVal(204)); // TCS says this is arbitrary, so
        // we write the offset value

        Assert.assertEquals("read back", 12 * 256 + 100, readValue);         // We get 100 from the LSB
        // because we wrote the offset
        // and the test Programmer remembers that
    }

    @Test
    public void testWriteReadTripleIndexed() throws jmri.ProgrammerException, InterruptedException {

        ProgDebugger dp = new ProgDebugger();
        Programmer p = new TwoIndexTcsProgrammerFacade(dp);
        ProgListener l = new ProgListener() {
            @Override
            public void programmingOpReply(int value, int status) {
                log.debug("callback value=" + value + " status=" + status);
                replied = true;
                readValue = value;
            }
        };

        p.writeCV("T3CV.10.20.30", 13, l);
        waitReply();
        Assert.assertEquals("index 1 written", 10, dp.getCvVal(201));
        Assert.assertEquals("value written", 13, dp.getCvVal(202));
        Assert.assertEquals("index written to MSB", 20, dp.getCvVal(203));
        Assert.assertEquals("index written to LSB", 30, dp.getCvVal(204));

        dp.clearHasBeenWritten(201);
        dp.resetCv(202, 13);
        dp.clearHasBeenWritten(203);
        dp.clearHasBeenWritten(204);

        p.readCV("T3CV.10.20.30", l);
        waitReply();
        Assert.assertEquals("index 1 written", 100 + 10, dp.getCvVal(201));
        Assert.assertEquals("SI not written, left at start value", 13, dp.getCvVal(202));
        Assert.assertEquals("index written to MSB", 20, dp.getCvVal(203));
        Assert.assertEquals("index written to LSB", 30, dp.getCvVal(204));

        Assert.assertEquals("read back", 13, readValue);
    }

    @Test
    public void testCvLimit() {
        ProgDebugger dp = new ProgDebugger();
        dp.setTestReadLimit(1024);
        dp.setTestWriteLimit(1024);

        Programmer p = new TwoIndexTcsProgrammerFacade(dp);

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

    private final static Logger log = LoggerFactory.getLogger(TwoIndexTcsProgrammerFacadeTest.class);

}
