/** 
 * DecVariableValueTest.java
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
import junit.framework.Assert;

import jmri.*;
import jmri.progdebugger.*;

public class DecVariableValueTest extends VariableValueTest {

	// abstract members invoked by tests in parent VariableValueTest class
	VariableValue makeVar(String name, String comment, boolean readOnly,
							int cvNum, String mask, int minVal, int maxVal,
							Vector v, JLabel status) {
		return new DecVariableValue(name, comment, readOnly, cvNum, mask, minVal, maxVal, v, status);
	}


	void setValue(VariableValue var, String val) {
		((JTextField)var.getValue()).setText(val);
		((JTextField)var.getValue()).postActionEvent();	
	}
	
	void setReadOnlyValue(VariableValue var, String val) {
		((DecVariableValue)var).setValue(Integer.valueOf(val).intValue());
	}
	
	void checkValue(VariableValue var, String comment, String val) {
			Assert.assertEquals(comment, val, ((JTextField)var.getValue()).getText() );
	}
		
	void checkReadOnlyValue(VariableValue var, String comment, String val) {
			Assert.assertEquals(comment, val, ((JLabel)var.getValue()).getText() );
	}
		
	// end of abstract members


	// from here down is testing infrastructure
	
	public  DecVariableValueTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		jmri.tests.AllTest.initLogging();
		String[] testCaseName = { DecVariableValueTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}
	
	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite( DecVariableValueTest.class);
		return suite;
	}
	
	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance( DecVariableValueTest.class.getName());

}
