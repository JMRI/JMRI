package jmri.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.ProgListener;
import jmri.Programmer;
import jmri.ProgrammerException;
import jmri.progdebugger.ProgDebugger;

import org.junit.jupiter.api.*;

/**
 * Test the VerifyWriteProgrammerFacade class.
 *
 * @author Bob Jacobsen Copyright 2013
 * 
 */
public class VerifyWriteProgrammerFacadeTest {

    private int readValue = -2;
    private boolean replied = false;

    @Test
    public void testWriteReadDirect() throws jmri.ProgrammerException, InterruptedException {

        
        var dp = new ProgDebugger() {
            private int readCount = 0;
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
                log.debug("callback value={} status={}", value, status);
                replied = true;
                readValue = value;
            }
        };

        p.writeCV("4", 12, l);
        waitReply();
        assertEquals( 12, dp.getCvVal(4), "target written");
        assertEquals( 0, dp.readCount, "reads");

        p.readCV("4", l);
        waitReply();
        assertEquals( 12, readValue, "read back");
    }

    @Test
    public void testWriteReadVerify() throws jmri.ProgrammerException, InterruptedException {

        var dp = new ProgDebugger() {
            private int readCount = 0;
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
                log.debug("callback value={} status={}", value, status);
                replied = true;
                readValue = value;
            }
        };

        p.writeCV("4", 12, l);
        waitReply();
        assertEquals( 12, dp.getCvVal(4), "target written");
        assertEquals( 1, dp.readCount, "reads");

        p.readCV("4", l);
        waitReply();
        assertEquals( 12, readValue, "read back");
    }

    @Test
    public void testCvLimit() {
        ProgDebugger dp = new ProgDebugger();
        dp.setTestReadLimit(1024);
        dp.setTestWriteLimit(1024);

        Programmer p = new VerifyWriteProgrammerFacade(dp);

        assertTrue( p.getCanRead("1024"), "CV limit read OK");
        assertTrue( p.getCanWrite("1024"), "CV limit write OK");
        assertFalse( p.getCanRead("1025"), "CV limit read fail");
        assertFalse( p.getCanWrite("1025"), "CV limit write fail");
    }

    // from here down is testing infrastructure
    synchronized void waitReply() throws InterruptedException {
        while (!replied) {
            wait(200);
        }
        replied = false;
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown(){
        jmri.util.JUnitUtil.tearDown();
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(VerifyWriteProgrammerFacadeTest.class);

}
