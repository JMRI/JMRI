// DebugProgrammerTest.java

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
 * @version     $Revision$
 */
public class SingleIndexProgrammerFacadeTest extends TestCase {


    int readValue = -2;
    boolean replied = false;

    public void testWriteReadDirect() throws jmri.ProgrammerException, InterruptedException {

        ProgDebugger dp = new ProgDebugger();
        Programmer p = new SingleIndexProgrammerFacade(dp, 256, 255, 1024);
        ProgListener l = new ProgListener(){
                public void programmingOpReply(int value, int status) {
                    log.debug("callback value="+value+" status="+status);
                    replied = true;
                    readValue = value;
                }
            };
        p.writeCV(4, 12, l);
        waitReply();
        Assert.assertEquals("target written", 12, dp.getCvVal(4));
        Assert.assertTrue("index not written", !dp.hasBeenWritten(255));

        p.readCV(4, l);
        waitReply();
        Assert.assertEquals("read back", 12, readValue);
    }
    
    public void testWriteReadIndexed() throws jmri.ProgrammerException, InterruptedException {
        
        ProgDebugger dp = new ProgDebugger();
        Programmer p = new SingleIndexProgrammerFacade(dp, 256, 255, 1024);
        ProgListener l = new ProgListener(){
                public void programmingOpReply(int value, int status) {
                    log.debug("callback value="+value+" status="+status);
                    replied = true;
                    readValue = value;
                }
            };
        p.writeCV(258, 12, l);
        waitReply();
        Assert.assertTrue("target not written", !dp.hasBeenWritten(258));
        Assert.assertEquals("index written", 1, dp.getCvVal(255));
        Assert.assertEquals("offset written", 12, dp.getCvVal(2));

        p.readCV(258, l);
        waitReply();
        Assert.assertEquals("read back", 12, readValue);
    }
    
    public void testCvLimit() {
        Programmer p = new SingleIndexProgrammerFacade(new jmri.progdebugger.ProgDebugger(), 256, 255, 1024);
        Assert.assertEquals("CV limit", 1024, p.getMaxCvAddr());
    }
    
    // from here down is testing infrastructure

    synchronized void waitReply() throws InterruptedException {
        while(!replied)
            wait(200);
        replied = false;
    }

    
    // from here down is testing infrastructure
    public SingleIndexProgrammerFacadeTest(String s) {
        super(s);
    }
    
    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SingleIndexProgrammerFacadeTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }
    
    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite(SingleIndexProgrammerFacadeTest.class);
        return suite;
    }
    
    static Logger log = Logger.getLogger(SingleIndexProgrammerFacadeTest.class.getName());

}
