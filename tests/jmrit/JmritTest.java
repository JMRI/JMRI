/** 
 * JmritTest.java
 *
 * Description:	    tests for the Jmrit package
 * @author			Bob Jacobsen
 * @version			
 */

package jmri.jmrit;

import java.io.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jdom.*;
import org.jdom.output.*;

public class JmritTest extends TestCase {

	// from here down is testing infrastructure	
	public JmritTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {JmritTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}
	
	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite("jmri.jmrit.JmritTest");   // no tests in this class itself
		suite.addTest(jmri.jmrit.AbstractIdentifyTest.suite());
		suite.addTest(jmri.jmrit.decoderdefn.DecoderDefnTest.suite());
		suite.addTest(jmri.jmrit.XmlFileTest.suite());
		suite.addTest(jmri.jmrit.symbolicprog.SymbolicProgTest.suite());
		suite.addTest(jmri.jmrit.powerpanel.PowerPanelTest.suite());
		suite.addTest(jmri.jmrit.roster.RosterTest.suite());
		return suite;
	}
	
}
