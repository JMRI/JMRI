package jmri.jmrix.lenz;

import jmri.*;
import jmri.jmrix.lenz.LenzCommandStation;
import jmri.jmrix.lenz.XNetInterfaceScaffold;
import jmri.jmrix.lenz.XNetListenerScaffold;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * XNetInitilizationManagerTest.java
 *
 * Description:	    tests for the jmri.jmrix.lenz.XNetInitilizationManager class
 * @author			Paul Bender
 * @version         $Revision: 2.1 $
 */
public class XNetInitilizationManagerTest extends TestCase {

    public void testCtor() {

// infrastructure objects
                XNetInterfaceScaffold t = new XNetInterfaceScaffold(new LenzCommandStation());
                XNetListenerScaffold l = new XNetListenerScaffold();

        XNetInitilizationManager m = new XNetInitilizationManager(){
            protected int getInitTimeout() {
                return 50;   // shorten, because this will fail & delay test
            }  
        };
        Assert.assertTrue(m != null);
        jmri.util.JUnitAppender.assertWarnMessage("Command Station disconnected, or powered down assuming LZ100/LZV100 V3.x");
    }

	// from here down is testing infrastructure
        public XNetInitilizationManagerTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", XNetInitilizationManagerTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(XNetInitilizationManagerTest.class);
		return suite;
	}

    // The minimal setup for log4J
    protected void setUp() throws Exception { 
        apps.tests.Log4JFixture.setUp(); 
        super.setUp();
    }
    protected void tearDown() throws Exception { 
        super.tearDown();
        apps.tests.Log4JFixture.tearDown(); 
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(XNetInitilizationManagerTest.class.getName());

}
