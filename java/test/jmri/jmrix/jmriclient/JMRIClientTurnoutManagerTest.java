package jmri.jmrix.jmriclient;

import org.apache.log4j.Logger;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JMRIClientTurnoutManagerTest.java
 *
 * Description:	    tests for the jmri.jmrix.jmriclient.JMRIClientTurnoutManager class
 * @author			Bob Jacobsen
 * @version         $Revision: 17977 $
 */
public class JMRIClientTurnoutManagerTest extends TestCase {

    public void testCtor() {
        JMRIClientTurnoutManager m = new JMRIClientTurnoutManager(new JMRIClientSystemConnectionMemo());
        Assert.assertNotNull(m);
    }

	// from here down is testing infrastructure

	public JMRIClientTurnoutManagerTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", JMRIClientTurnoutManagerTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(JMRIClientTurnoutManagerTest.class);
		return suite;
	}

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

    static Logger log = Logger.getLogger(JMRIClientTurnoutManagerTest.class.getName());

}
