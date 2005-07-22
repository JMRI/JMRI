/**
 * AbstractTurnoutMgrTest.java
 *
 * Description:	    AbsBaseClass for TurnoutManager tests in specific jmrix. packages
 * @author			Bob Jacobsen
 * @version
 */

/**
 * This is not itself a test class, e.g. should not be added to a suite.  Instead,
 * this forms the base for test classes, including providing some common tests
 */

package jmri;

import jmri.*;

import java.io.*;
import java.beans.PropertyChangeListener;
import junit.framework.Test;
import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public abstract class AbstractTurnoutMgrTest extends TestCase {

	// implementing classes must provide these abstract members:
	//
	abstract public void setUp();    	// load t with actual object; create scaffolds as needed
	abstract public String getSystemName(int i);

	public AbstractTurnoutMgrTest(String s) {
		super(s);
	}

	protected TurnoutManager l = null;	// holds objects under test

	static protected boolean listenerResult = false;
	protected class Listen implements PropertyChangeListener {
		public void propertyChange(java.beans.PropertyChangeEvent e) {
			listenerResult = true;
		}
	}

	// start of common tests

	// test creation - real work is in the setup() routine
	public void testCreate() {
	}

	public void testDispose() throws JmriException {
		l.dispose();  // all we're really doing here is making sure the method exists
	}

	public void testTurnoutPutGet() {
		// create
		Turnout t = l.newTurnout(getSystemName(21), "mine");
		// check
		Assert.assertTrue("real object ", t != null);
		Assert.assertTrue("user name ", t == l.getByUserName("mine"));
		Assert.assertTrue("system name ", t == l.getBySystemName(getSystemName(21)));
	}

	public void testDefaultSystemName() {
		// create
		Turnout t = l.provideTurnout("21");
		// check
		Assert.assertTrue("real object ", t != null);
		Assert.assertTrue("system name ", t == l.getBySystemName(getSystemName(21)));
	}

	public void testSingleObject() {
		// test that you always get the same representation
		Turnout t1 = l.newTurnout(getSystemName(21), "mine");
		Assert.assertTrue("t1 real object ", t1 != null);
		Assert.assertTrue("same by user ", t1 == l.getByUserName("mine"));
		Assert.assertTrue("same by system ", t1 == l.getBySystemName(getSystemName(21)));

		Turnout t2 = l.newTurnout(getSystemName(21), "mine");
		Assert.assertTrue("t2 real object ", t2 != null);
		// check
		Assert.assertTrue("same new ", t1 == t2);
	}

	public void testMisses() {
		// sample address object
		TurnoutAddress a = new TurnoutAddress(getSystemName(31), "user");

		// try to get nonexistant turnouts
		Assert.assertTrue(null == l.getByUserName("foo"));
		Assert.assertTrue(null == l.getBySystemName("bar"));
	}
	
	public void testUpperLower() {
		// sample address object
		TurnoutAddress a = new TurnoutAddress(getSystemName(31), "user");

		Turnout t = l.provideTurnout("31");
		String name = t.getSystemName();
		Assert.assertTrue(t.equals(l.getTurnout(name.toLowerCase())));
	}

	public void testRename() {
		// get turnout
		Turnout t1 = l.newTurnout(getSystemName(21), "before");
		Assert.assertTrue("t1 real object ", t1 != null);
		t1.setUserName("after");
		Turnout t2 = l.getByUserName("after");
		Assert.assertEquals("same object", t1, t2);
		Assert.assertEquals("no old object", null, l.getByUserName("before"));
	}
}
