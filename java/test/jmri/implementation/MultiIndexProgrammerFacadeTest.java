// MultiIndexProgrammerFacadeTest.java

package jmri.implementation;

import org.apache.log4j.Logger;
import jmri.InstanceManager;
import jmri.Programmer;
import jmri.ProgListener;
import jmri.progdebugger.ProgDebugger;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test the SingleIndexProgrammerFacade class.
 *
 * @author	Bob Jacobsen Copyright 2013
 * @version     $Revision: 24246 $
 */
public class MultiIndexProgrammerFacadeTest extends TestCase {


    int readValue = -2;
    boolean replied = false;

    public void testParse() {
        ProgDebugger dp = new ProgDebugger();
        MultiIndexProgrammerFacade p = new MultiIndexProgrammerFacade(dp, 81);
        
        p.parseCV("12");
        Assert.assertEquals("for 12, cv", 12, p._cv);
        Assert.assertEquals("for 12, indexVal", -1, p._indexVal);
        
        p.parseCV("12.34");
        Assert.assertEquals("for 12.34, cv", 34, p._cv);
        Assert.assertEquals("for 12.34, indexVal", 12, p._indexVal);
        
    }
    
    public void testWriteReadDirect() throws jmri.ProgrammerException, InterruptedException {

        ProgDebugger dp = new ProgDebugger();
        Programmer p = new MultiIndexProgrammerFacade(dp, 81);
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
        Assert.assertTrue("index not written", !dp.hasBeenWritten(81));

        p.readCV("4", l);
        waitReply();
        Assert.assertEquals("read back", 12, readValue);
        Assert.assertTrue("index not written", !dp.hasBeenWritten(81));
    }
    
    public void testWriteReadIndexed() throws jmri.ProgrammerException, InterruptedException {
        
        ProgDebugger dp = new ProgDebugger();
        Programmer p = new MultiIndexProgrammerFacade(dp, 81);
        ProgListener l = new ProgListener(){
                public void programmingOpReply(int value, int status) {
                    log.debug("callback value="+value+" status="+status);
                    replied = true;
                    readValue = value;
                }
            };
        p.writeCV("123.45", 12, l);
        waitReply();
        Assert.assertEquals("index written", 123, dp.getCvVal(81));
        Assert.assertEquals("value written", 12, dp.getCvVal(45));

        dp.clearHasBeenWritten(81);
        
        p.readCV("123.45", l);
        waitReply();
        Assert.assertEquals("read back", 12, readValue);
        Assert.assertTrue("index written", dp.hasBeenWritten(81));
        Assert.assertEquals("index written", 123, dp.getCvVal(81));
    }
    
    public void testCvLimit() {
        Programmer p = new MultiIndexProgrammerFacade(new jmri.progdebugger.ProgDebugger(), 81);
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
    public MultiIndexProgrammerFacadeTest(String s) {
        super(s);
    }
    
    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {MultiIndexProgrammerFacadeTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }
    
    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite(MultiIndexProgrammerFacadeTest.class);
        return suite;
    }
    
    static Logger log = Logger.getLogger(MultiIndexProgrammerFacadeTest.class.getName());

}
