// AwtHandlerTest.java

package jmri.util.exceptionhandler;

import org.apache.log4j.Logger;
import junit.framework.*;

import jmri.util.*;

/**
 * Tests for the jmri.util.AwtHandlerTest class.
 * @author	Bob Jacobsen  Copyright 2010
 * @version	$Revision$
 */
public class AwtHandlerTest extends SwingTestCase {

    public void testHandle() throws Exception {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                dref(null);
            }
            void dref(Object o) { o.toString(); }
        });
        flushAWT();
        JUnitAppender.assertErrorMessage("Unhandled Exception: java.lang.NullPointerException");
    }
        
	// from here down is testing infrastructure

	public AwtHandlerTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {AwtHandlerTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(AwtHandlerTest.class);
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

	static Logger log = Logger.getLogger(AwtHandlerTest.class.getName());

}
