/** 
 * SymbolicProgTest.java
 *
 * Description:	
 * @author			Bob Jacobsen
 * @version			
 */

package jmri.jmrit.symbolicprog;

import java.io.*;
import java.util.*;
import javax.swing.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jdom.*;
import org.jdom.output.*;

import jmri.*;
import jmri.progdebugger.*;

public class SymbolicProgTest extends TestCase {

	// check configuring the programmer
	public void testConfigProgrammer() {
		// initialize the system
		Programmer p = new ProgDebugger();
		InstanceManager.setProgrammer(p);
		assert (InstanceManager.programmerInstance() == p);
	}
		
	// from here down is testing infrastructure
	
	public SymbolicProgTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {SymbolicProgTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}
	
	// test suite from all defined tests, including others in the package
	public static Test suite() {
		TestSuite suite = new TestSuite(SymbolicProgTest.class);
		suite.addTest(jmri.jmrit.symbolicprog.ComboCheckBoxTest.suite());
		suite.addTest(jmri.jmrit.symbolicprog.ComboRadioButtonsTest.suite());
		suite.addTest(jmri.jmrit.symbolicprog.tabbedframe.PaneProgFrameTest.suite());
		suite.addTest(jmri.jmrit.symbolicprog.DecVariableValueTest.suite());
		suite.addTest(jmri.jmrit.symbolicprog.HexVariableValueTest.suite());
		suite.addTest(jmri.jmrit.symbolicprog.LongAddrVariableValueTest.suite());
		suite.addTest(jmri.jmrit.symbolicprog.CvValueTest.suite());
		suite.addTest(jmri.jmrit.symbolicprog.DecoderFileTest.suite());
		suite.addTest(jmri.jmrit.symbolicprog.CvTableModelTest.suite());
		suite.addTest(jmri.jmrit.symbolicprog.VariableTableModelTest.suite());
		return suite;
	}
	
	// static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SymbolicProgTest.class.getName());

}
