// SprogMessageTest.java

package jmri.jmrix.sprog;

import jmri.Programmer;

import junit.framework.Test;
import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit tests for the SprogMessage class
 * @author			Bob Jacobsen Copyright 2012
 * @version			$Revision$
 */

public class SprogMessageTest extends TestCase {

	public void testCreate() {
		SprogMessage m = new SprogMessage(1);
		Assert.assertNotNull("exists", m );
	}

	public void testReadCv() {
		SprogMessage m = SprogMessage.getReadCV(12, Programmer.PAGEMODE);
		Assert.assertEquals("string compare ", "V 0012", m.toString());
	}

	public void testWriteCV() {
		SprogMessage m = SprogMessage.getWriteCV(12, 251, Programmer.PAGEMODE);
		Assert.assertEquals("string compare ", "V 0012 251", m.toString());
	}

	public void testReadCvLarge() {
		SprogMessage m = SprogMessage.getReadCV(1021, Programmer.PAGEMODE);
		Assert.assertEquals("string compare ", "V 1021", m.toString());
	}

	public void testWriteCVLarge() {
		SprogMessage m = SprogMessage.getWriteCV(1021, 251, Programmer.PAGEMODE);
		Assert.assertEquals("string compare ", "V 1021 251", m.toString());
	}

	// from here down is testing infrastructure

	public SprogMessageTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {SprogMessageTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(SprogMessageTest.class);
		return suite;
	}

}
