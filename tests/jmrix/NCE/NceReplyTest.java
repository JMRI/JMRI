/** 
 * NceReplyTest.java
 *
 * Description:	    JUnit tests for the NceReplyclass
 * @author			Bob Jacobsen
 * @version			
 */

package jmri.tests.jmrix.nce;

import jmri.*;

import junit.framework.Test;
import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.jmrix.nce.NceReply;

public class NceReplyTest extends TestCase {

	public void testCreate() {
		NceReply m = new NceReply();
	}

	public void testBinaryToString() {
		NceReply m = new NceReply();
		m.setBinary(true);
		m.setOpCode(0x81);
		m.setElement(1, 0x02);
		m.setElement(2, 0xA2);
		m.setElement(3, 0x00);
		Assert.assertEquals("string compare ", "81 02 a2 00", m.toString());
	}
	
	public void testAsciiToString() {
		NceReply m = new NceReply();
		m.setBinary(false);
		m.setOpCode('C');
		m.setElement(1, 'o');
		m.setElement(2, 'm');
		m.setElement(3, ':');
		Assert.assertEquals("string compare ", "Com:", m.toString());
	}

	public void testValue() {
		NceReply m = new NceReply();
		m.setBinary(false);
		m.setElement(0, '0');
		m.setElement(1, '2');
		m.setElement(2, '7');
		m.setElement(3, ' ');
		Assert.assertEquals("value ", 27, m.value());
	}
	
	// from here down is testing infrastructure
	
	public NceReplyTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {NceReplyTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}
	
	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(NceReplyTest.class);
		return suite;
	}
	
}
