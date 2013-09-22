// DebugProgrammerTest.java

package jmri.progdebugger;

import org.apache.log4j.Logger;
import jmri.InstanceManager;
import jmri.Programmer;
import jmri.ProgListener;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test the DebugProgrammer class.
 *
 * @author	Bob Jacobsen Copyright 2013
 * @version     $Revision$
 */
public class DebugProgrammerTest extends TestCase {


    int readValue = -2;
    boolean replied = false;

    public void testWriteRead() throws jmri.ProgrammerException, InterruptedException {
        Programmer p = new ProgDebugger();
        ProgListener l = new ProgListener(){
                public void programmingOpReply(int value, int status) {
                    log.debug("callback value="+value+" status="+status);
                    replied = true;
                    readValue = value;
                }
            };
        p.writeCV(4, 12, l);
        waitReply();
        log.debug("readValue is "+readValue);
        p.readCV(4, l);
        waitReply();
        log.debug("readValue is "+readValue);
        Assert.assertEquals("read back", 12, readValue);
    }
    
    public void testCvLimit() {
        Programmer p = new ProgDebugger();
        Assert.assertEquals("CV limit", 256, p.getMaxCvAddr());
    }
    
    // from here down is testing infrastructure

    synchronized void waitReply() throws InterruptedException {
        while(!replied)
            wait(200);
        replied = false;
    }

    
    // from here down is testing infrastructure
    public DebugProgrammerTest(String s) {
        super(s);
    }
    
    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {DebugProgrammerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }
    
    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite(DebugProgrammerTest.class);
        return suite;
    }
    
    static Logger log = Logger.getLogger(DebugProgrammerTest.class.getName());

}
