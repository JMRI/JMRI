/** 
 * VariableValueTest.java
 *
 * Description:	 base for tests of classes inheriting from VariableValue abstract class
 * @author			Bob Jacobsen
 * @version			
 */

package jmri.jmrit.symbolicprog;

import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.Component;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Assert;

import jmri.*;
import jmri.progdebugger.*;

public abstract class VariableValueTest extends TestCase {

	abstract VariableValue makeVar(String name, String comment, boolean readOnly,
							int cvNum, String mask, int minVal, int maxVal,
							Vector v, JLabel status, String stdName);
	
	abstract void setValue(VariableValue var, String value);
	abstract void checkValue(VariableValue var, String comment, String value);
	
	// we have separate fns for ReadOnly, as they may have different "value" object types
	abstract void setReadOnlyValue(VariableValue var, String value);
	abstract void checkReadOnlyValue(VariableValue var, String comment, String value);
	
	// start of base tests
	
	// check name, stdName from ctor
	public void testVariableNaming() {
		Vector v = createCvVector();
		CvValue cv = new CvValue(81);
		cv.setValue(3);
		v.setElementAt(cv, 81);
		// create a variable pointed at CV 81, check name
		VariableValue variable = makeVar("name check", "comment", false, 81, "XXVVVVXX", 0, 255, v, null, "stdName check");
		Assert.assertEquals("name", "name check", variable.name() );
		Assert.assertEquals("stdName", "stdName check", variable.stdName() );
	}

	// can we create one, then manipulate the variable to change the CV?
	public void testVariableValueCreate() {
		Vector v = createCvVector();
		CvValue cv = new CvValue(81);
		cv.setValue(3);
		v.setElementAt(cv, 81);
		// create a variable pointed at CV 81, check name
		VariableValue variable = makeVar("name", "comment", false, 81, "XXVVVVXX", 0, 255, v, null, null);
		Assert.assertEquals("name", "name", variable.name() );
		checkValue(variable, "value object initially contains ", "0");

		// pretend you've editted the value & manually notify
		setValue(variable, "5");
		
		// check value
		checkValue(variable, "value object contains ", "5");

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
		VariableValue variable = makeVar("name", "comment", false, 81, "XXVVVVXX", 0, 255, v, null, null);
		Assert.assertTrue("getValue not null ", variable.getValue() != null);
		setValue(variable, "5");
		checkValue(variable, "variable value", "5");
		
		// change the CV, expect to see a change in the variable value
		cv.setValue(7*4+1);
		checkValue(variable, "value after CV set", "7");
		Assert.assertEquals("cv after CV set ", 7*4+1, cv.getValue());
	}
	
	// Do we get the right return from a readOnly == true DecVariable?
	public void testVariableReadOnly() {
		Vector v = createCvVector();
		CvValue cv = new CvValue(81);
		cv.setValue(3);
		v.setElementAt(cv, 81);
		// create a variable pointed at CV 81, loaded as 5
		VariableValue variable = makeVar("name", "comment", true, 81, "XXVVVVXX", 0, 255, v, null, null);
		assert( variable.getValue() != null);
		setReadOnlyValue(variable, "5");
		checkReadOnlyValue(variable, "value", "5");
	}
	
	// check a read operation
	public void testVariableValueRead() {
		log.debug("testVariableValueRead base starts");
		// initialize the system
		Programmer p = new ProgDebugger();
		InstanceManager.setProgrammer(p);
		
		Vector v = createCvVector();
		CvValue cv = new CvValue(81);
		v.setElementAt(cv, 81);
		// create a variable pointed at CV 81, loaded as 5, manually notified
		VariableValue variable = makeVar("name", "comment", false, 81, "XXVVVVXX", 0, 255, v, null, null);
		setValue(variable, "5");

		variable.read();
		// wait for reply (normally, done by callback; will check that later)
		int i = 0;
		while ( variable.isBusy() && i++ < 100 )  {
			try {
				Thread.sleep(50);
			} catch (Exception e) {
			}
		}
		if (log.isDebugEnabled()) log.debug("past loop, i="+i+" value="+variable.getValue()+" state="+variable.getState());
		if (i==0) log.warn("textVariableValueRead saw an immediate return from isBusy");

		Assert.assertTrue("wait time for message",i<100);
		checkValue(variable, "text var value ", "14");
		Assert.assertEquals("var state ", AbstractValue.READ, variable.getState());
		Assert.assertEquals("cv value", 123, cv.getValue());
		Assert.assertEquals("CV state ", AbstractValue.READ, cv.getState());
	}

	// check a write operation to the variable
	public void testVariableValueWrite() {
		log.debug("testVariableValueWrite base starts");
		// initialize the system
		ProgDebugger p = new ProgDebugger();
		InstanceManager.setProgrammer(p);
		
		Vector v = createCvVector();
		CvValue cv = new CvValue(81);
		v.setElementAt(cv, 81);
		// create a variable pointed at CV 81, loaded as 5, manually notified
		VariableValue variable = makeVar("name", "comment", false, 81, "XXVVVVXX", 0, 255, v, null, null);
		setValue(variable, "5");

		variable.write(); 
		// wait for reply (normally, done by callback; will check that later)
		int i = 0;
		while ( variable.isBusy() && i++ < 100  )  {
			try {
				Thread.sleep(50);
			} catch (Exception e) {
			}
		}
		if (log.isDebugEnabled()) log.debug("past loop, i="+i+" value="+variable.getValue()+" state="+variable.getState());
		if (i==0) log.warn("testVariableValueWrite saw an immediate return from isBusy");

		Assert.assertTrue("iterations ",i<100);
		checkValue(variable, "value ","5");
		Assert.assertEquals("var state ", AbstractValue.STORED, variable.getState());
		Assert.assertEquals("cv state ", AbstractValue.STORED, cv.getState());
		Assert.assertEquals("last program write ", 5*4, p.lastWrite());
	}
	
	// check synch during a write operation to the CV
	public void testVariableCvWrite() {
		if (log.isDebugEnabled()) log.debug("start testVariableCvWrite test");
		// initialize the system
		ProgDebugger p = new ProgDebugger();
		InstanceManager.setProgrammer(p);
		
		Vector v = createCvVector();
		CvValue cv = new CvValue(81);
		v.setElementAt(cv, 81);
		// create a variable pointed at CV 81, loaded as 5, manually notified
		VariableValue variable = makeVar("name", "comment", false, 81, "XXVVVVXX", 0, 255, v, null, null);
		setValue(variable, "5");

		JLabel statusLabel = new JLabel("nothing");
		cv.write(statusLabel);  // JLabel is for reporting status, ignored here 
		// wait for reply (normally, done by callback; will check that later)
		int i = 0;
		while ( cv.isBusy() && i++ < 100  )  {
			try {
				Thread.sleep(50);
			} catch (Exception e) {
			}
		}
		if (log.isDebugEnabled()) log.debug("past loop, i="+i+" value="+cv.getValue()+" state="+cv.getState());
		if (i==0) log.warn("testVariableCvWrite saw an immediate return from isBusy");

		Assert.assertTrue("iterations needed ", i<100);
		checkValue(variable, "value ","5");
		Assert.assertEquals("variable state ", AbstractValue.STORED, variable.getState() );
		Assert.assertEquals("cv state ", AbstractValue.STORED, cv.getState());
		Assert.assertEquals("value written ", 5*4, p.lastWrite());
		Assert.assertEquals("status label ", "OK", statusLabel.getText());
		if (log.isDebugEnabled()) log.debug("end testVariableCvWrite test");
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
		VariableValue variable = makeVar("name", "comment", false, 81, "XXVVVVXX", 0, 255, v, null, null);
		Assert.assertEquals("initial state", VariableValue.FROMFILE, variable.getState());
		cv.setState(CvValue.UNKNOWN);
		Assert.assertEquals("after CV set unknown", VariableValue.UNKNOWN, variable.getState());
		setValue(variable, "5");
		Assert.assertEquals("state after setValue", VariableValue.EDITTED, variable.getState());
	}

	// check the state <-> color connection for value
	public void testVariableValueStateColor() {
		// initialize the system
		Programmer p = new ProgDebugger();
		InstanceManager.setProgrammer(p);
		
		Vector v = createCvVector();
		CvValue cv = new CvValue(81);
		v.setElementAt(cv, 81);
		// create a variable pointed at CV 81, loaded as 5, manually notified
		VariableValue variable = makeVar("name", "comment", false, 81, "XXVVVVXX", 0, 255, v, null, null);
		Assert.assertEquals("EDITTED color", VariableValue.COLOR_EDITTED, variable.getValue().getBackground() );

		cv.setState(CvValue.UNKNOWN);
		Assert.assertEquals("UNKNOWN color", VariableValue.COLOR_UNKNOWN, variable.getValue().getBackground() );
	}
	
	// check the state <-> color connection for rep when var changes
	public void testVariableRepStateColor() {
		// initialize the system
		Programmer p = new ProgDebugger();
		InstanceManager.setProgrammer(p);
		
		Vector v = createCvVector();
		CvValue cv = new CvValue(81);
		v.setElementAt(cv, 81);
		// create a variable pointed at CV 81, loaded as 5, manually notified
		VariableValue variable = makeVar("name", "comment", false, 81, "XXVVVVXX", 0, 255, v, null, null);
		// get a representation
		JTextField rep = (JTextField)variable.getRep("");
		
		Assert.assertEquals("EDITTED color", VariableValue.COLOR_EDITTED, variable.getValue().getBackground() );
		Assert.assertEquals("EDITTED color", VariableValue.COLOR_EDITTED, rep.getBackground() );

		cv.setState(CvValue.UNKNOWN);

		Assert.assertEquals("UNKNOWN color", VariableValue.COLOR_UNKNOWN, variable.getValue().getBackground() );
		Assert.assertEquals("UNKNOWN color", VariableValue.COLOR_UNKNOWN, rep.getBackground() );

		setValue(variable, "5");

		Assert.assertEquals("EDITTED color", VariableValue.COLOR_EDITTED, variable.getValue().getBackground() );
		Assert.assertEquals("EDITTED color", VariableValue.COLOR_EDITTED, rep.getBackground() );
	}
	
	// check the state <-> color connection for var when rep changes
	public void testVariableVarChangeColorRep() {
		// initialize the system
		Programmer p = new ProgDebugger();
		InstanceManager.setProgrammer(p);
		
		Vector v = createCvVector();
		CvValue cv = new CvValue(81);
		v.setElementAt(cv, 81);
		// create a variable pointed at CV 81, loaded as 5, manually notified
		VariableValue variable = makeVar("name", "comment", false, 81, "XXVVVVXX", 0, 255, v, null, null);
		// get a representation
		JTextField rep = (JTextField)variable.getRep("");
		
		Assert.assertEquals("EDITTED color", VariableValue.COLOR_EDITTED, variable.getValue().getBackground() );
		Assert.assertEquals("EDITTED color", VariableValue.COLOR_EDITTED, rep.getBackground() );

		cv.setState(CvValue.UNKNOWN);
		Assert.assertEquals("UNKNOWN color", VariableValue.COLOR_UNKNOWN, variable.getValue().getBackground() );
		Assert.assertEquals("UNKNOWN color", VariableValue.COLOR_UNKNOWN, rep.getBackground() );
		
		rep.setText("9");
		rep.postActionEvent();
		Assert.assertEquals("EDITTED color", VariableValue.COLOR_EDITTED, variable.getValue().getBackground() );
		Assert.assertEquals("EDITTED color", VariableValue.COLOR_EDITTED, rep.getBackground() );
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
		VariableValue variable = makeVar("name", "comment", false, 81, "XXVVVVXX", 0, 255, v, null, null);
		setValue(variable, "5");

		// now get value, check
		checkValue(variable, "first value check ", "5");
		Component val1 = variable.getValue();
		// now get rep, check
		JTextField rep1 = (JTextField) variable.getRep("");
		Assert.assertEquals("initial rep ", "5", rep1.getText());
		
		// update via value
		setValue(variable, "2");
		
		// check again with existing reference
		Assert.assertEquals("same value object ", val1, variable.getValue());
		Assert.assertEquals("1 saved rep ", "2", rep1.getText());
		// pick up new references and check
		checkValue(variable, "1 new value ", "2");
		Assert.assertEquals("1 new rep ", "2", ((JTextField) variable.getRep("")).getText());
		
		// update via rep
		rep1.setText("9");
		rep1.postActionEvent();
		
		// check again with existing references
		Assert.assertEquals("2 saved value ", "9", ((JTextField)val1).getText());
		Assert.assertEquals("2 saved rep ", "9", rep1.getText());
		// pick up new references and check
		checkValue(variable, "2 new value ", "9");
		Assert.assertEquals("2 new rep ", "9", ((JTextField) variable.getRep("")).getText());
	}

	// check synchronization of two vars during a write
	public void testWriteSynch2() {
		if (log.isDebugEnabled()) log.debug("start testWriteSynch2 test");
		// initialize the system
		ProgDebugger p = new ProgDebugger();
		InstanceManager.setProgrammer(p);
		
		Vector v = createCvVector();
		CvValue cv = new CvValue(81);
		v.setElementAt(cv, 81);
		// create a variable pointed at CV 81, loaded as 5, manually notified
		VariableValue var1 = makeVar("name", "comment", false, 81, "XXVVVVXX", 0, 255, v, null, null);
		VariableValue var2 = makeVar("alternate", "comment", false, 81, "XXVVVVXX", 0, 255, v, null, null);
		setValue(var1, "5");

		var1.write(); 
		// wait for reply (normally, done by callback; will check that later)
		int i = 0;
		while ( var1.isBusy() && i++ < 100  )  {
			try {
				Thread.sleep(50);
			} catch (Exception e) {
			}
		}
		if (log.isDebugEnabled()) log.debug("past loop, i="+i+" value="+var1.getValue()+" state="+var1.getState());
		if (i==0) log.warn("testWriteSynch2 saw an immediate return from isBusy");

		Assert.assertTrue("Number of iterations ",i<100);
		checkValue(var1, "var 1 value","5");
		checkValue(var2, "var 2 value","5");
		Assert.assertEquals("1st variable state ", AbstractValue.STORED, var1.getState());
		Assert.assertEquals("2nd variable state ", AbstractValue.STORED, var2.getState());
		Assert.assertEquals("value written to programmer ",5*4, p.lastWrite());
		if (log.isDebugEnabled()) log.debug("end testWriteSynch2 test");
	}
	
	// end of common tests
	
	// this next is just preserved here; note not being invoked.
	// test that you're not using too much space when you call for a value
	public void XtestSpaceUsage() {  // leading X prevents test from being called
		Vector v = createCvVector();
		CvValue cv = new CvValue(81);
		cv.setValue(3);
		v.setElementAt(cv, 81);
		// create a variable pointed at CV 81, loaded as 5
		DecVariableValue var = new DecVariableValue("name", "comment", false, 81, "XXVVVVXX", 0, 255, v, null, null);
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

	// abstract class has no main entry point, test suite
	
	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(VariableValueTest.class.getName());

}
