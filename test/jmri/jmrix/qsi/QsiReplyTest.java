/**
 * QsiReplyTest.java
 *
 * Description:	    JUnit tests for the QsiReplyclass
 * @author			Bob Jacobsen
 * @version         $Revision: 1.1 $
 */

package jmri.jmrix.qsi;

import apps.tests.Log4JFixture;
import junit.framework.Test;
import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.jmrix.qsi.QsiReply;

public class QsiReplyTest extends TestCase {

	public void testCreate() {
		QsiReply m = new QsiReply();
		m.setElement(0, 'A');
		Assert.assertEquals("expected length ", 1, m.getNumDataElements());
		m.setElement(1, 'B');
		m.setElement(2, 'C');
		Assert.assertEquals("expected length ", 3, m.getNumDataElements());
		m.setElement(0, '1');
		Assert.assertEquals("expected length ", 3, m.getNumDataElements());
		m.setElement(3, 'D');
		Assert.assertEquals("expected length ", 4, m.getNumDataElements());
		m.setElement(5, 'A');
		Assert.assertEquals("expected length ", 6, m.getNumDataElements());

	}

	public void testAsciiToString() {
		QsiReply m = new QsiReply();
		m.setOpCode('C');
		m.setElement(1, 'o');
		m.setElement(2, 'm');
		m.setElement(3, ':');
		Assert.assertEquals("string compare ", "Com:", m.toString());
	}

	public void testSkipWhiteSpace() {
		QsiReply m = new QsiReply();
		m.setElement(0, '0');
		m.setElement(1, ' ');
		m.setElement(2, ' ');
		m.setElement(3, 'A');
		m.setElement(4, 0x0A);
		m.setElement(5, 'A');
		Assert.assertEquals(" skip two blanks", 3, m.skipWhiteSpace(1));
		Assert.assertEquals(" skip no blanks", 3, m.skipWhiteSpace(3));
		Assert.assertEquals(" handle start", 0, m.skipWhiteSpace(0));
		Assert.assertEquals(" skip LF", 5, m.skipWhiteSpace(4));
	}

	public void testValue1() {
		// value when just the string comes back
		QsiReply m = new QsiReply();
                int i= 0;
		m.setElement(i++, 'C');
		m.setElement(i++, 'V');
		m.setElement(i++, '0');
		m.setElement(i++, '0');
		m.setElement(i++, '1');
		m.setElement(i++, '2');
		m.setElement(i++, '7');
		Assert.assertEquals("value ", -1, m.value());
	}

	public void testValue2() {
		// value when a hex string comes back
		QsiReply m = new QsiReply();
        int i= 0;
		m.setElement(i++, 'C');
		m.setElement(i++, 'V');
		m.setElement(i++, '0');
		m.setElement(i++, '0');
		m.setElement(i++, '1');
		m.setElement(i++, 'A');
		m.setElement(i++, 'B');
		Assert.assertEquals("value ", -1, m.value());
	}

	public void testMatch() {
		QsiReply m = new QsiReply("**** PROGRAMMING MODE - MAIN TRACK NOW DISCONNECTED ****");
		Assert.assertEquals("find ", 5, m.match("PROGRAMMING"));
		Assert.assertEquals("not find ", -1, m.match("foo"));
	}

	// from here down is testing infrastructure

	public QsiReplyTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {QsiReplyTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(QsiReplyTest.class);
		return suite;
	}

	Log4JFixture log4jfixtureInst = new Log4JFixture(this);

    protected void setUp() {
    	log4jfixtureInst.setUp();
    }

    protected void tearDown() {
    	log4jfixtureInst.tearDown();
    }

}
