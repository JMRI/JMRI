/** 
 * SymbolicProgTest.java
 *
 * Description:	
 * @author			Bob Jacobsen
 * @version			
 */

package jmri.tests.symbolicprog;

import java.io.*;
import java.util.*;
import javax.swing.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jdom.*;
import org.jdom.output.*;

import jmri.*;
import jmri.symbolicprog.*;
import jmri.progdebugger.*;

public class SymbolicProgTest extends TestCase {

// CvVal tests
	// can we create one and manipulate info?
	public void testCvValCreate() {
		CvValue cv = new CvValue(19);
		assert(cv.number() == 19);
		cv.setValue(23);
		assert(cv.getValue() == 23);
	}

	// check configuring the programmer
	public void testConfigProgrammer() {
		// initialize the system
		Programmer p = new ProgDebugger();
		InstanceManager.setProgrammer(p);
		assert (InstanceManager.programmerInstance() == p);
	}
	
	// check a read operation
	public void testCvValRead() {
		// initialize the system
		Programmer p = new ProgDebugger();
		InstanceManager.setProgrammer(p);
		
		// create the CV value
		CvValue cv = new CvValue(91);
		cv.read();
		// wait for reply (normally, done by callback; will check that later)
		int i = 0;
		while ( cv.isBusy() && i++ < 100 )  {
			try {
				Thread.sleep(10);
			} catch (Exception e) {
			}
		}
		if (log.isDebugEnabled()) log.debug("past loop, i="+i+" value="+cv.getValue()+" state="+cv.getState());
		
		assert(i>0);
		assert(i<100);
		assert(cv.getValue() == 123);
		assert(cv.getState() == CvValue.READ);
	}

	// check a write operation
	public void testCvValWrite() {
		// initialize the system
		ProgDebugger p = new ProgDebugger();
		InstanceManager.setProgrammer(p);
		
		// create the CV value
		CvValue cv = new CvValue(91);
		cv.setValue(12);
		cv.write(); 
		// wait for reply (normally, done by callback; will check that later)
		int i = 0;
		while ( cv.isBusy() && i++ < 100 )  {
			try {
				Thread.sleep(10);
			} catch (Exception e) {
			}
		}
		if (log.isDebugEnabled()) log.debug("past loop, i="+i+" value="+cv.getValue()+" state="+cv.getState());
		
		assert(i>0);
		assert(i<100);
		assert(cv.getValue() == 12);
		assert(cv.getState() == CvValue.STORED);
		assert(p.lastWrite() == 12);
	}
	
	// check the state diagram
	public void testCvValStates() {
		CvValue cv = new CvValue(21);
		assert(cv.getState() == CvValue.UNKNOWN);
		cv.setValue(23);
		assert(cv.getState() == CvValue.EDITTED);
	}
		
	
	protected Vector createCvVector() {
		Vector v = new Vector(512);
		for (int i=0; i < 512; i++) v.addElement(null);
		return v;
	}
	
// VariableValue tests via decimal subclass
	// can we create one, then manipulate the variable to change the CV?
	public void testVariableValueCreate() {
		Vector v = createCvVector();
		CvValue cv = new CvValue(81);
		cv.setValue(3);
		v.setElementAt(cv, 81);
		// create a variable pointed at CV 81, check name
		DecVariableValue var = new DecVariableValue("name", "comment", false, 81, "XXVVVVXX", v);
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
		DecVariableValue var = new DecVariableValue("name", "comment", false, 81, "XXVVVVXX", v);
		((JTextField)var.getValue()).setText("5");

		// change the CV, expect to see a change in the variable value
		cv.setValue(7*4+1);
		assert( ((JTextField)var.getValue()).getText().equals("7") );
		assert(cv.getValue() == 7*4+1);
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
		DecVariableValue var = new DecVariableValue("name", "comment", false, 81, "XXVVVVXX", v);
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

		assert(i>0);
		assert(i<100);
		assert( ((JTextField)var.getValue()).getText().equals("14") );
		assert(var.getState() == CvValue.READ);
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
		DecVariableValue var = new DecVariableValue("name", "comment", false, 81, "XXVVVVXX", v);
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

		assert(i>0);
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
		DecVariableValue var = new DecVariableValue("name", "comment", false, 81, "XXVVVVXX", v);
		assert(var.getState() == VariableValue.UNKNOWN);
		((JTextField)var.getValue()).setText("5");
		var.actionPerformed(new java.awt.event.ActionEvent(var, 0, ""));
		assert(var.getState() == VariableValue.EDITTED);
	}
		
	
// VariableTableModel tests
	// Can we create a table?
	public void testVarTableCreate() {
		String[] args = {"CV", "Name"};
		VariableTableModel t = new VariableTableModel(args, null);  // CvTableModel ref is null for this test
	}

	// Check column count member fn, column names
	public void testVarTableColumnCount() {
		String[] args = {"CV", "Name"};
		VariableTableModel t = new VariableTableModel(args, null);
		assert(t.getColumnCount() == 2);
		assert(t.getColumnName(1) == "Name");
	}

	// Check loading two columns, three rows
	public void testVarTableLoad_2_3() {
		String[] args = {"CV", "Name"};
		VariableTableModel t = new VariableTableModel(args, new CvTableModel());
		
		// create a JDOM tree with just some elements
		Namespace ns = Namespace.getNamespace("decoder", "http://www.slac.stanford.edu/BFROOT/java/streamcalc");
		Element root = new Element("decoder-config", ns);
		Document doc = new Document(root);
		doc.setDocType(new DocType("decoder:decoder-config","DTD/decoder-config.dtd"));
		
		// add some elements
		Element el0, el1;
		root.addContent(new Element("decoder",ns)		// the sites information here lists all relevant
					.addContent(new Element("variables", ns)
									.addContent(el0 = new Element("variable", ns)
												.addAttribute("CV","1")
												.addAttribute("name","one")
												)
									.addContent(el1 = new Element("variable", ns)
												.addAttribute("CV","4")
												.addAttribute("mask","XXXVVVVX")
												.addAttribute("name","two")
												)
										)	// variables element									
						) // decoder element
			; // end of adding contents

		// print JDOM tree, to check
		// OutputStream o = System.out;
		// XMLOutputter fmt = new XMLOutputter();
		// fmt.setNewlines(true);   // pretty printing
		// fmt.setIndent(true);
		// try {
			// fmt.output(doc, o);
		// } catch (Exception e) { System.out.println("error writing XML: "+e);}	

		// and test reading this
		t.setRow(0, el0, ns);
		assert(t.getValueAt(0,0).equals("1"));
		assert(t.getValueAt(0,1).equals("one"));

		t.setRow(1, el1, ns);
		assert(t.getValueAt(1,0).equals("4"));
		assert(t.getValueAt(1,1).equals("two"));

		assert(t.getRowCount() == 2);
		
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
	
	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(SymbolicProgTest.class);
		return suite;
	}
	
	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SymbolicProgTest.class.getName());

}
