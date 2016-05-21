// OffsetHighCvProgrammerFacadeTest.java

package jmri.implementation;

import org.apache.log4j.Logger;
import jmri.Programmer;
import jmri.ProgListener;
import jmri.progdebugger.ProgDebugger;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test the OffsetHighCvProgrammerFacade class.
 *
 * @author	Bob Jacobsen Copyright 2013
 * @version     $Revision: 24246 $
 */
public class OffsetHighCvProgrammerFacadeTest extends TestCase {


    int readValue = -2;
    boolean replied = false;

    public void testWriteReadDirect() throws jmri.ProgrammerException, InterruptedException {

        ProgDebugger dp = new ProgDebugger();
        dp.setTestReadLimit(256);
        dp.setTestWriteLimit(256);

        Programmer p = new OffsetHighCvProgrammerFacade(dp, "256", "7", "10", "100");
        ProgListener l = new ProgListener(){
                public void programmingOpReply(int value, int status) {
                    log.debug("callback value="+value+" status="+status);
                    replied = true;
                    readValue = value;
                }
            };
        p.writeCV("4", 12, l);
        waitReply();
        Assert.assertEquals("target written", 12, dp.getCvVal(4));
        Assert.assertTrue("index not written", !dp.hasBeenWritten(7));

        p.readCV(4, l);
        waitReply();
        Assert.assertEquals("read back", 12, readValue);
    }
    
    public void testWriteReadDirectHighCV() throws jmri.ProgrammerException, InterruptedException {

        ProgDebugger dp = new ProgDebugger();
        dp.setTestReadLimit(1024);
        dp.setTestWriteLimit(1024);

        Programmer p = new OffsetHighCvProgrammerFacade(dp, "256", "7", "10", "100");
        ProgListener l = new ProgListener(){
                public void programmingOpReply(int value, int status) {
                    log.debug("callback value="+value+" status="+status);
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
    
    public void testWriteReadIndexed() throws jmri.ProgrammerException, InterruptedException {
        
        ProgDebugger dp = new ProgDebugger();
        dp.setTestReadLimit(256);
        dp.setTestWriteLimit(256);
        Programmer p = new OffsetHighCvProgrammerFacade(dp, "256", "7", "10", "100");
        ProgListener l = new ProgListener(){
                public void programmingOpReply(int value, int status) {
                    log.debug("callback value="+value+" status="+status);
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
    
    public void testCvLimit() {
        ProgDebugger dp = new ProgDebugger();
        dp.setTestReadLimit(256);
        dp.setTestWriteLimit(256);
        Programmer p = new OffsetHighCvProgrammerFacade(dp, "256", "7", "10", "100");
        Assert.assertTrue("CV limit read OK", p.getCanRead("1024"));  
        Assert.assertTrue("CV limit write OK", p.getCanWrite("1024"));  
        Assert.assertTrue("CV limit read mode OK", p.getCanRead(0, "1024"));  
        Assert.assertTrue("CV limit write mode OK", p.getCanWrite(0, "1024"));  
        Assert.assertTrue("CV limit read fail", !p.getCanRead("1025"));  
        Assert.assertTrue("CV limit write fail", !p.getCanWrite("1025"));  
        Assert.assertTrue("CV limit read mode fail", !p.getCanRead(0, "1025"));  
        Assert.assertTrue("CV limit write mode fail", !p.getCanWrite(0, "1025"));  
    }
    
    // from here down is testing infrastructure

    synchronized void waitReply() throws InterruptedException {
        while(!replied)
            wait(200);
        replied = false;
    }

    
    // from here down is testing infrastructure
    public OffsetHighCvProgrammerFacadeTest(String s) {
        super(s);
    }
    
    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {OffsetHighCvProgrammerFacadeTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }
    
    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite(OffsetHighCvProgrammerFacadeTest.class);
        return suite;
    }
    
    static Logger log = Logger.getLogger(OffsetHighCvProgrammerFacadeTest.class.getName());

}
