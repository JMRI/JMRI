/** 
 * LocoIOFrameTest.java
 *
 * Description:	    tests for the jmri.jmrix.loconet.locoio.LocoIOFrame class
 * @author			Bob Jacobsen
 * @version			
 */

package jmri.jmrix.loconet.locoio;

import java.io.*;
import junit.framework.Test;
import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.jmrix.loconet.*;

public class LocoIOFrameTest extends TestCase {

	public void testFrameCreate() {
		new LocoIOFrame();		
	}
		
	// from here down is testing infrastructure
	
	public LocoIOFrameTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {LocoIOFrameTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}
	
	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(LocoIOFrameTest.class);
		return suite;
	}
	 
	 static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LocoIOFrameTest.class.getName());

}
