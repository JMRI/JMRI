/** 
 * NceMessageTest.java
 *
 * Description:	    JUnit tests for the NceMessage class
 * @author			Bob Jacobsen
 * @version			
 */

package jmri.tests.jmrix.nce;

import jmri.*;

import junit.framework.Test;
import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.jmrix.nce.NceMessage;

public class NceMessageTest extends TestCase {

	public void testCreate() {
		NceMessage m = new NceMessage(1);
	}

	public void testToString() {
		NceMessage m = new NceMessage(4);
		m.setOpCode('1');
		m.setElement(1, '2');
		m.setElement(2, 'A');
		m.setElement(3, 'D');
		Assert.assertEquals("string compare ", "12AD", m.toString());
	}
	
	// from here down is testing infrastructure
	
	public NceMessageTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {NceMessageTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}
	
	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(NceMessageTest.class);
		return suite;
	}
	
}
