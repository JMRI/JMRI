package jmri.jmrix.jmriclient;

import org.apache.log4j.Logger;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JMRIClientTrafficControllerTest.java
 *
 * Description:	    tests for the jmri.jmrix.jmriclient.JMRIClientTrafficController class
 * @author			Bob Jacobsen
 * @version         $Revision: 17977 $
 */
public class JMRIClientTrafficControllerTest extends TestCase {

    public void testCtor() {
        JMRIClientTrafficController m = new JMRIClientTrafficController();
        Assert.assertNotNull(m);
    }

	// from here down is testing infrastructure

	public JMRIClientTrafficControllerTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", JMRIClientTrafficControllerTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(JMRIClientTrafficControllerTest.class);
		return suite;
	}

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

    static Logger log = Logger.getLogger(JMRIClientTrafficControllerTest.class.getName());

}
