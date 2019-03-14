package jmri.implementation;

import jmri.ProgListener;
import jmri.Programmer;
import jmri.ProgrammerException;
import jmri.progdebugger.ProgDebugger;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test the VerifyWriteProgrammerFacade class.
 *
 * @author	Bob Jacobsen Copyright 2013
 * 
 */
public class VerifyWriteProgrammerFacadeTest {

    int readValue = -2;
    int readCount = 0;
    boolean replied = false;

    @Test
    public void testWriteReadDirect() throws jmri.ProgrammerException, InterruptedException {

        readCount = 0;
        ProgDebugger dp = new ProgDebugger() {
            @Override
            public boolean getCanRead(String cv) { return false; }
            @Override
            public boolean getCanRead() { return false; }
            @Override
            public void readCV(String cv, ProgListener p) throws ProgrammerException { readCount++; super.readCV(cv, p); }
        };
        Programmer p = new VerifyWriteProgrammerFacade(dp);
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
        Assert.assertEquals("reads", 0, readCount);
        
        p.readCV("4", l);
        waitReply();
        Assert.assertEquals("read back", 12, readValue);
    }

    @Test
    public void testWriteReadVerify() throws jmri.ProgrammerException, InterruptedException {

        readCount = 0;
        ProgDebugger dp = new ProgDebugger() {
            @Override
            public boolean getCanRead(String cv) { return true; }
            @Override
            public boolean getCanRead() { return true; }
            @Override
            public void readCV(String cv, ProgListener p) throws ProgrammerException { readCount++; super.readCV(cv, p); }
        };
        Programmer p = new VerifyWriteProgrammerFacade(dp);
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
        Assert.assertEquals("reads", 1, readCount);

        p.readCV("4", l);
        waitReply();
        Assert.assertEquals("read back", 12, readValue);
    }

    @Test
    public void testCvLimit() {
        ProgDebugger dp = new ProgDebugger();
        dp.setTestReadLimit(1024);
        dp.setTestWriteLimit(1024);

        Programmer p = new VerifyWriteProgrammerFacade(dp);

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

    private final static Logger log = LoggerFactory.getLogger(VerifyWriteProgrammerFacadeTest.class);

}
