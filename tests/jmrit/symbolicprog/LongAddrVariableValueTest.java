/** 
 * LongAddrVariableValueTest.java
 *
 * Description:	
 * @author			Bob Jacobsen
 * @version			
 */

// need a check of the MIXED state model for long address

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

public class LongAddrVariableValueTest extends VariableValueTest {

	// abstract members invoked by tests in parent VariableValueTest class
	VariableValue makeVar(String name, String comment, boolean readOnly,
							int cvNum, String mask, int minVal, int maxVal,
							Vector v, JLabel status) {
		// make sure next CV exists
		CvValue cvNext = new CvValue(cvNum+1);
		cvNext.setValue(0);
		v.setElementAt(cvNext, cvNum+1);
		return new LongAddrVariableValue(name, comment, readOnly, cvNum, mask, minVal, maxVal, v, status);
	}


	void setValue(VariableValue var, String val) {
		((JTextField)var.getValue()).setText(val);
		((JTextField)var.getValue()).postActionEvent();	
	}
	
	void setReadOnlyValue(VariableValue var, String val) {
		((LongAddrVariableValue)var).setValue(Integer.valueOf(val).intValue());
	}
	
	void checkValue(VariableValue var, String comment, String val) {
		Assert.assertEquals(comment, val, ((JTextField)var.getValue()).getText() );
	}
		
	void checkReadOnlyValue(VariableValue var, String comment, String val) {
		Assert.assertEquals(comment, val, ((JLabel)var.getValue()).getText() );
	}
		
	// end of abstract members
	
	// some of the premade tests don't quite make sense; override them here.
	
	public void testVariableValueCreate() {}// mask is ignored by LongAddr
	public void testVariableFromCV() {}     // low CV is upper part of address
	public void testVariableValueRead() {}	// due to multi-cv nature of LongAddr
	public void testVariableValueWrite() {} // due to multi-cv nature of LongAddr
	public void testVariableCvWrite() {}    // due to multi-cv nature of LongAddr
	public void testWriteSynch2() {}        // programmer synch is different
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
	
	// can we change both CVs and see the result in the Variable?
	public void testLongAddressFromCV() {
		Vector v = createCvVector();
		CvValue cv17 = new CvValue(17);
		CvValue cv18 = new CvValue(18);
		cv17.setValue(2);
		cv18.setValue(3);
		v.setElementAt(cv17, 17);
		v.setElementAt(cv18, 18);
		// create a variable pointed at CV 17 & 18
		LongAddrVariableValue var = new LongAddrVariableValue("name", "comment", false, 17, "VVVVVVVV", 0, 255, v, null);
		((JTextField)var.getValue()).setText("1029");
		var.actionPerformed(new java.awt.event.ActionEvent(var, 0, ""));

		// change the CV, expect to see a change in the variable value
		cv17.setValue(210);
		assert(cv17.getValue() == 210);
		cv18.setValue(189);
		assert( ((JTextField)var.getValue()).getText().equals("4797") );
		assert(cv18.getValue() == 189);
	}
		
	// check a long address read operation
	public void testLongAddressRead() {
		// initialize the system
		Programmer p = new ProgDebugger();
		InstanceManager.setProgrammer(p);
		
		Vector v = createCvVector();
		CvValue cv17 = new CvValue(17);
		CvValue cv18 = new CvValue(18);
		v.setElementAt(cv17, 17);
		v.setElementAt(cv18, 18);

		LongAddrVariableValue var = new LongAddrVariableValue("name", "comment", false, 17, "XXVVVVXX", 0, 255, v, null);
		// set to specific value
		((JTextField)var.getValue()).setText("5");
		var.actionPerformed(new java.awt.event.ActionEvent(var, 0, ""));

		var.read();
		// wait for reply (normally, done by callback; will check that later)
		int i = 0;
		while ( var.isBusy() && i++ < 100 )  {
			try {
				Thread.sleep(100);
			} catch (Exception e) {
			}
		}
		if (log.isDebugEnabled()) log.debug("past loop, i="+i+" value="+((JTextField)var.getValue()).getText()+" state="+var.getState());
		if (i==0) log.warn("testLongAddressRead saw an immediate return from isBusy");

		assert(i<100);
		assert( ((JTextField)var.getValue()).getText().equals("15227") );  // 15227 = (1230x3f)*256+123
		assert(var.getState() == CvValue.READ);
		assert(cv17.getValue() == 123);
		assert(cv18.getValue() == 123);
	}

	// check a long address write operation
	public void testLongAddressWrite() {
		// initialize the system
		ProgDebugger p = new ProgDebugger();
		InstanceManager.setProgrammer(p);
		
		Vector v = createCvVector();
		CvValue cv17 = new CvValue(17);
		CvValue cv18 = new CvValue(18);
		v.setElementAt(cv17, 17);
		v.setElementAt(cv18, 18);

		LongAddrVariableValue var = new LongAddrVariableValue("name", "comment", false, 17, "XXVVVVXX", 0, 255, v, null);
		((JTextField)var.getValue()).setText("4797");
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
		if (log.isDebugEnabled()) log.debug("past loop, i="+i+" value="+((JTextField)var.getValue()).getText()
															+" state="+var.getState()
															+" last write: "+p.lastWrite());
		if (i==0) log.warn("testLongAddressWrite saw an immediate return from isBusy");

		assert(cv17.getValue() == 210);
		assert(cv18.getValue() == 189);
		assert(i<100);
		assert( ((JTextField)var.getValue()).getText().equals("4797") );
		assert(var.getState() == CvValue.STORED);
		assert(p.lastWrite() == 189);
		// how do you check separation of the two writes?  State model?
	}

	protected Vector createCvVector() {
		Vector v = new Vector(512);
		for (int i=0; i < 512; i++) v.addElement(null);
		return v;
	}

	// from here down is testing infrastructure
	
	public  LongAddrVariableValueTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = { LongAddrVariableValueTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}
	
	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite( LongAddrVariableValueTest.class);
		return suite;
	}
	
	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance( LongAddrVariableValue.class.getName());

}
