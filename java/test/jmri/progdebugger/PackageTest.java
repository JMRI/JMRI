// PackageTest.java

package jmri.progdebugger;

import org.apache.log4j.Logger;
import jmri.ProgListener;
import jmri.Programmer;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Invoke complete set of tests for the Jmri.progdebugger package.
 * <P>
 * Due to existing package and class names, this is both the test
 * suite for the package, but also contains some tests for the ProgDebugger class.
 *
 * @author		Bob Jacobsen, Copyright (C) 2001, 2002
 * @version         $Revision$
 */
public class PackageTest extends TestCase {

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
    
    // from here down is testing infrastructure

    synchronized void waitReply() throws InterruptedException {
        while(!replied)
            wait(200);
        replied = false;
    }
    
    public PackageTest(String s) {
        super(s);
    }
    
    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {PackageTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }
    
    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite(PackageTest.class);
        suite.addTest(jmri.progdebugger.DebugProgrammerManagerTest.suite());
        return suite;
    }
    
     // The minimal setup for log4J
      protected void setUp() throws Exception { 
          apps.tests.Log4JFixture.setUp(); 
          super.setUp();
          jmri.util.JUnitUtil.resetInstanceManager();
          jmri.util.JUnitUtil.initInternalSensorManager();
      }
      protected void tearDown() throws Exception { 
          jmri.util.JUnitUtil.resetInstanceManager();
          super.tearDown();
          apps.tests.Log4JFixture.tearDown(); 
      }
  
    static Logger log = Logger.getLogger(PackageTest.class.getName());    
}
