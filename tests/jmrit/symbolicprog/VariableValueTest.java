/** 
 * VariableValueTest.java
 *
 * Description:	
 * @author			Bob Jacobsen
 * @version			
 */

// VariableValue tests via decimal subclass

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

public class VariableValueTest extends TestCase {

	// can we create one, then manipulate the variable to change the CV?
	public void testVariableValueCreate() {
		Vector v = createCvVector();
		CvValue cv = new CvValue(81);
		cv.setValue(3);
		v.setElementAt(cv, 81);
		// create a variable pointed at CV 81, check name
		DecVariableValue var = new DecVariableValue("name", "comment", false, 81, "XXVVVVXX", 0, 255, v, null);
		assert(var.name() == "name");
		// pretend you've editted the value, check its in same object
		((JTextField)var.getValue()).setText("5");
		assert( ((JTextField)var.getValue()).getText().equals("5") );
		// manually notify
		var.actionPerformed(new java.awt.event.ActionEvent(var, 0, ""));
		// see if the CV was updated
		assert(cv.getValue() == 5*4+3);
	}

	// can we change the CV and see the result in the Variable?
	public void testVariableFromCV() {
		Vector v = createCvVector();
		CvValue cv = new CvValue(81);
		cv.setValue(3);
		v.setElementAt(cv, 81);
		// create a variable pointed at CV 81, loaded as 5
		DecVariableValue var = new DecVariableValue("name", "comment", false, 81, "XXVVVVXX", 0, 255, v, null);
		assert( ((JTextField)var.getValue()) != null);
		var.setValue(5);
		assert( ((JTextField)var.getValue()).getText().equals("5"));
		
		// change the CV, expect to see a change in the variable value
		cv.setValue(7*4+1);
		assert( ((JTextField)var.getValue()).getText().equals("7") );
		assert(cv.getValue() == 7*4+1);
	}
	
	// Do we get the right return from a readOnly == true DecVariable?
	public void testVariableDecReadOnly() {
		Vector v = createCvVector();
		CvValue cv = new CvValue(81);
		cv.setValue(3);
		v.setElementAt(cv, 81);
		// create a variable pointed at CV 81, loaded as 5
		DecVariableValue var = new DecVariableValue("name", "comment", true, 81, "XXVVVVXX", 0, 255, v, null);
		// notice type cast in next line
		assert( ((JLabel)var.getValue()) != null);
		var.setValue(5);
		assert( ((JLabel)var.getValue()).getText().equals("5"));
	}
	
	// check a read operation
	public void testVariableValueRead() {
		// initialize the system
		Programmer p = new ProgDebugger();
		InstanceManager.setProgrammer(p);
		
		Vector v = createCvVector();
		CvValue cv = new CvValue(81);
		v.setElementAt(cv, 81);
		// create a variable pointed at CV 81, loaded as 5, manually notified
		DecVariableValue var = new DecVariableValue("name", "comment", false, 81, "XXVVVVXX", 0, 255, v, null);
		((JTextField)var.getValue()).setText("5");
		var.actionPerformed(new java.awt.event.ActionEvent(var, 0, ""));

		var.read();
		// wait for reply (normally, done by callback; will check that later)
		int i = 0;
		while ( var.isBusy() && i++ < 100 )  {
			try {
				Thread.sleep(10);
			} catch (Exception e) {
			}
		}
		if (log.isDebugEnabled()) log.debug("past loop, i="+i+" value="+var.getValue()+" state="+var.getState());
		if (i==0) log.warn("textVariableValueRead saw an immediate return from isBusy");

		assert(i<100);
		assert( ((JTextField)var.getValue()).getText().equals("14") );
		assert(var.getState() == CvValue.READ);
		assert(cv.getValue() == 123);
	}

	// check a write operation
	public void testVariableValueWrite() {
		// initialize the system
		ProgDebugger p = new ProgDebugger();
		InstanceManager.setProgrammer(p);
		
		Vector v = createCvVector();
		CvValue cv = new CvValue(81);
		v.setElementAt(cv, 81);
		// create a variable pointed at CV 81, loaded as 5, manually notified
		DecVariableValue var = new DecVariableValue("name", "comment", false, 81, "XXVVVVXX", 0, 255, v, null);
		((JTextField)var.getValue()).setText("5");
		var.actionPerformed(new java.awt.event.ActionEvent(var, 0, ""));

		var.write(); 
		// wait for reply (normally, done by callback; will check that later)
		int i = 0;
		while ( var.isBusy() && i++ < 100  )  {
			try {
				Thread.sleep(10);
			} catch (Exception e) {
			}
		}
		if (log.isDebugEnabled()) log.debug("past loop, i="+i+" value="+var.getValue()+" state="+var.getState());
		if (i==0) log.warn("testVariableValueWrite saw an immediate return from isBusy");

		assert(i<100);
		assert( ((JTextField)var.getValue()).getText().equals("5") );
		assert(var.getState() == CvValue.STORED);
		assert(p.lastWrite() == 5*4);
	}
	
	// check the state diagram
	public void testVariableValueStates() {
		// initialize the system
		Programmer p = new ProgDebugger();
		InstanceManager.setProgrammer(p);
		
		Vector v = createCvVector();
		CvValue cv = new CvValue(81);
		v.setElementAt(cv, 81);
		// create a variable pointed at CV 81, loaded as 5, manually notified
		DecVariableValue var = new DecVariableValue("name", "comment", false, 81, "XXVVVVXX", 0, 255, v, null);
		assert(var.getState() == VariableValue.UNKNOWN);
		((JTextField)var.getValue()).setText("5");
		var.actionPerformed(new java.awt.event.ActionEvent(var, 0, ""));
		assert(var.getState() == VariableValue.EDITTED);
	}

	// can we create long address , then manipulate the variable to change the CV?
	public void testLongAddressCreate() {
		Vector v = createCvVector();
		CvValue cv17 = new CvValue(17);
		CvValue cv18 = new CvValue(18);
		cv17.setValue(2);
		cv18.setValue(3);
		v.setElementAt(cv17, 17);
		v.setElementAt(cv18, 18);
		// create a variable pointed at CV 17&18, check name
		LongAddrVariableValue var = new LongAddrVariableValue("name", "comment", false, 17, "VVVVVVVV", 0, 255, v, null);
		assert(var.name() == "name");
		// pretend you've editted the value, check its in same object
		((JTextField)var.getValue()).setText("4797");
		assert( ((JTextField)var.getValue()).getText().equals("4797") );
		// manually notify
		var.actionPerformed(new java.awt.event.ActionEvent(var, 0, ""));
		// see if the CV was updated
		assert(cv17.getValue() == 210);
		assert(cv18.getValue() == 189);
	}
	
	protected Vector createCvVector() {
		Vector v = new Vector(512);
		for (int i=0; i < 512; i++) v.addElement(null);
		return v;
	}

	// from here down is testing infrastructure
	
	public VariableValueTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {VariableValueTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}
	
	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(VariableValueTest.class);
		return suite;
	}
	
	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(VariableValue.class.getName());

}
