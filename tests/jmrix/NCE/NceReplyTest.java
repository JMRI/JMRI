/** 
 * NceReplyTest.java
 *
 * Description:	    JUnit tests for the NceReplyclass
 * @author			Bob Jacobsen
 * @version			
 */

package jmri.jmrix.nce;

import jmri.*;

import junit.framework.Test;
import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.jmrix.nce.NceReply;

public class NceReplyTest extends TestCase {

	public void testCreate() {
		NceReply m = new NceReply();
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

	public void testSkipWhiteSpace() {
		NceReply m = new NceReply();
		m.setBinary(false);
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

	public void testSkipCOMMAND() {
		NceReply m = new NceReply();
		m.setBinary(false);
		m.setElement(0, ' ');
		m.setElement(1, ' ');
		m.setElement(2, 'A');
		m.setElement(3, ' ');
		m.setElement(4, 'C');
		m.setElement(5, 'O');
		m.setElement(6, 'M');
		m.setElement(7, 'M');
		m.setElement(8, 'A');
		m.setElement(9, 'N');
		m.setElement(10, 'D');
		m.setElement(11, ':');
		m.setElement(12, ' ');
		m.setElement(13, 'X');
		Assert.assertEquals(" too short", 13, m.skipCOMMAND(13));
		Assert.assertEquals(" not found", 5, m.skipCOMMAND(5));
		Assert.assertEquals(" too short", 6, m.skipCOMMAND(6));
		Assert.assertEquals(" too short", 7, m.skipCOMMAND(7));
		Assert.assertEquals(" find & skip", 13, m.skipCOMMAND(4));
		Assert.assertEquals(" not found", 0, m.skipCOMMAND(0));
		m = new NceReply();
		m.setBinary(false);
		m.setElement(0, 'C');
		m.setElement(1, 'O');
		m.setElement(2, 'M');
		m.setElement(3, 'M');
		m.setElement(4, 'A');
		m.setElement(5, 'N');
		m.setElement(6, 'D');
		m.setElement(7, ':');
		m.setElement(8, ' ');
		m.setElement(9, '0');
		m.setElement(10, '2');
		m.setElement(11, '7');
		Assert.assertEquals(" start of reply ", 9, m.skipCOMMAND(0));
	}
			
	public void testValue1() {
		// value when just the string comes back
		NceReply m = new NceReply();
		m.setBinary(false);
		m.setElement(0, '0');
		m.setElement(1, '2');
		m.setElement(2, '7');
		m.setElement(3, ' ');
		Assert.assertEquals("value ", 27, m.value());
	}
	
	public void testValue2() {
		// value with a "Command:" prefix
		NceReply m = new NceReply();
		m.setBinary(false);
		m.setElement(0, 'C');
		m.setElement(1, 'O');
		m.setElement(2, 'M');
		m.setElement(3, 'M');
		m.setElement(4, 'A');
		m.setElement(5, 'N');
		m.setElement(6, 'D');
		m.setElement(7, ':');
		m.setElement(8, ' ');
		m.setElement(9, '0');
		m.setElement(10, '2');
		m.setElement(11, '7');
		Assert.assertEquals("value ", 27, m.value());
	}

	public void testMatch() {
		NceReply m = new NceReply("**** PROGRAMMING MODE - MAIN TRACK NOW DISCONNECTED ****");
		Assert.assertEquals("find ", 5, m.match("PROGRAMMING"));
		Assert.assertEquals("not find ", -1, m.match("foo"));
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
