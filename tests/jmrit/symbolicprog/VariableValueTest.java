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
		Assert.assertEquals("name", "name", var.name() );

		// pretend you've editted the value & manually notify
		((JTextField)var.getValue()).setText("5");
		((JTextField)var.getValue()).postActionEvent();
		
		// check value
		Assert.assertEquals("value object contains ", "5", ((JTextField)var.getValue()).getText() );

		// see if the CV was updated
		Assert.assertEquals("cv value", 5*4+3, cv.getValue());
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
		((JTextField)var.getValue()).postActionEvent();

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

		Assert.assertTrue("wait time for message",i<100);
		Assert.assertEquals("text var value ", "14", ((JTextField)var.getValue()).getText());
		Assert.assertEquals("state ", CvValue.READ, var.getState());
		Assert.assertEquals("cv value", 123, cv.getValue());
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
		((JTextField)var.getValue()).postActionEvent();

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
		((JTextField)var.getValue()).postActionEvent();
		assert(var.getState() == VariableValue.EDITTED);
	}

	// check synchonization of value, representations
	public void testVariableSynch() {
		// initialize the system
		ProgDebugger p = new ProgDebugger();
		InstanceManager.setProgrammer(p);
		
		Vector v = createCvVector();
		CvValue cv = new CvValue(81);
		v.setElementAt(cv, 81);
		// create a variable pointed at CV 81, loaded as 5, manually notified
		DecVariableValue var = new DecVariableValue("name", "comment", false, 81, "XXVVVVXX", 0, 255, v, null);
		((JTextField)var.getValue()).setText("5");
		((JTextField)var.getValue()).postActionEvent();
		// now get value, check
		JTextField val1 = (JTextField) var.getValue();
		Assert.assertEquals("initial value ", "5", val1.getText());
		// now get rep, check
		JTextField rep1 = (JTextField) var.getRep("");
		Assert.assertEquals("initial rep ", "5", rep1.getText());
		
		// update via value
		((JTextField)var.getValue()).setText("12");
		((JTextField)var.getValue()).postActionEvent();
		
		// check again with existing references
		Assert.assertEquals("1 saved value ", "12", val1.getText());
		Assert.assertEquals("1 saved rep ", "12", rep1.getText());
		// pick up new references and check
		Assert.assertEquals("1 new value ", "12", ((JTextField) var.getValue()).getText());
		Assert.assertEquals("1 new rep ", "12", ((JTextField) var.getRep("")).getText());
		
		// update via rep
		rep1.setText("201");
		rep1.postActionEvent();
		
		// check again with existing references
		Assert.assertEquals("2 saved value ", "201", val1.getText());
		Assert.assertEquals("2 saved rep ", "201", rep1.getText());
		// pick up new references and check
		Assert.assertEquals("2 new value ", "201", ((JTextField) var.getValue()).getText());
		Assert.assertEquals("2 new rep ", "201", ((JTextField) var.getRep("")).getText());
	}
	
	// test that you're not using too much space when you call for a value
	public void XtestSpaceUsage() {  // leading X prevents test from being called
		Vector v = createCvVector();
		CvValue cv = new CvValue(81);
		cv.setValue(3);
		v.setElementAt(cv, 81);
		// create a variable pointed at CV 81, loaded as 5
		DecVariableValue var = new DecVariableValue("name", "comment", false, 81, "XXVVVVXX", 0, 255, v, null);
		System.out.println("free, total memory at start = "+Runtime.getRuntime().freeMemory()
							+" "+Runtime.getRuntime().totalMemory());
		Runtime.getRuntime().gc();
		long usedStart = Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
		System.out.println("free, total memory after gc = "+Runtime.getRuntime().freeMemory()
							+" "+Runtime.getRuntime().totalMemory());
		JTextField master = new JTextField(3);
		javax.swing.text.Document doc = master.getDocument();
		// loop to repeat getting value
		for (int i = 0; i<10; i++) {
			JTextField j = new JTextField(doc,"",3);
			//JTextField temp = ((JTextField)var.getValue());
			//Assert.assertTrue(temp != null);
		}
		long freeAfter = Runtime.getRuntime().freeMemory();
		System.out.println("free, total memory after loop = "+Runtime.getRuntime().freeMemory()
							+" "+Runtime.getRuntime().totalMemory());
		long usedAfter = Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
		
		Runtime.getRuntime().gc();
		long usedAfterGC = Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
		System.out.println("free, total memory after gc = "+Runtime.getRuntime().freeMemory()
							+" "+Runtime.getRuntime().totalMemory());
		System.out.println("used & kept = "+(usedAfterGC-usedStart)+" used before reclaim = "+(usedAfter-usedStart));
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
