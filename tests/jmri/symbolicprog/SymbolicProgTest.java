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
		ProgDebugger p = new ProgDebugger();
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
		if (i==0) log.warn("textCvValRead saw an immediate return from isBusy");

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
		if (i==0) log.warn("textCvValWrite saw an immediate return from isBusy");

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
		DecVariableValue var = new DecVariableValue("name", "comment", false, 81, "XXVVVVXX", 0, 255, v);
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
		DecVariableValue var = new DecVariableValue("name", "comment", false, 81, "XXVVVVXX", 0, 255, v);
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
		DecVariableValue var = new DecVariableValue("name", "comment", true, 81, "XXVVVVXX", 0, 255, v);
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
		DecVariableValue var = new DecVariableValue("name", "comment", false, 81, "XXVVVVXX", 0, 255, v);
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
		DecVariableValue var = new DecVariableValue("name", "comment", false, 81, "XXVVVVXX", 0, 255, v);
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
		DecVariableValue var = new DecVariableValue("name", "comment", false, 81, "XXVVVVXX", 0, 255, v);
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
		LongAddrVariableValue var = new LongAddrVariableValue("name", "comment", false, 17, "VVVVVVVV", 0, 255, v);
		assert(var.name() == "name");
		// pretend you've editted the value, check its in same object
		((JTextField)var.getValue()).setText("1029");
		assert( ((JTextField)var.getValue()).getText().equals("1029") );
		// manually notify
		var.actionPerformed(new java.awt.event.ActionEvent(var, 0, ""));
		// see if the CV was updated
		assert(cv17.getValue() == 5);
		assert(cv18.getValue() == 8);
	}
	
	// can we change the CV and see the result in the Variable?
	public void testLongAddressFromCV() {
		Vector v = createCvVector();
		CvValue cv17 = new CvValue(17);
		CvValue cv18 = new CvValue(18);
		cv17.setValue(2);
		cv18.setValue(3);
		v.setElementAt(cv17, 17);
		v.setElementAt(cv18, 18);
		// create a variable pointed at CV 81, loaded as 5
		LongAddrVariableValue var = new LongAddrVariableValue("name", "comment", false, 17, "VVVVVVVV", 0, 255, v);
		((JTextField)var.getValue()).setText("1029");
		var.actionPerformed(new java.awt.event.ActionEvent(var, 0, ""));

		// change the CV, expect to see a change in the variable value
		cv17.setValue(7);
		assert( ((JTextField)var.getValue()).getText().equals("1031") );
		assert(cv17.getValue() == 7);
		cv18.setValue(9);
		assert( ((JTextField)var.getValue()).getText().equals("1159") );
		assert(cv18.getValue() == 9);
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

		LongAddrVariableValue var = new LongAddrVariableValue("name", "comment", false, 17, "XXVVVVXX", 0, 255, v);
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
		assert( ((JTextField)var.getValue()).getText().equals("15867") );
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

		LongAddrVariableValue var = new LongAddrVariableValue("name", "comment", false, 17, "XXVVVVXX", 0, 255, v);
		((JTextField)var.getValue()).setText("1029");
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

		assert(cv17.getValue() == 5);
		assert(cv18.getValue() == 8);
		assert(i<100);
		assert( ((JTextField)var.getValue()).getText().equals("1029") );
		assert(var.getState() == CvValue.STORED);
		assert(p.lastWrite() == 8);
		// how do you check separation of the two writes?  State model?
	}
	
	// need a check of the MIXED state model for long address
	
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
												.addContent( new Element("decVal", ns)
														.addAttribute("max","31")
														.addAttribute("min","1")
													)
												)
									.addContent(el1 = new Element("variable", ns)
												.addAttribute("CV","4")
												.addAttribute("mask","XXXVVVVX")
												.addAttribute("name","two")
												.addContent( new Element("decVal", ns)
														.addAttribute("max","31")
														.addAttribute("min","1")
													)
												)
										)	// variables element									
						) // decoder element
			; // end of adding contents

		// print JDOM tree, to check
		//OutputStream o = System.out;
		//XMLOutputter fmt = new XMLOutputter();
		//fmt.setNewlines(true);   // pretty printing
		//fmt.setIndent(true);
		//try {
		//	 fmt.output(doc, o);
		//} catch (Exception e) { System.out.println("error writing XML: "+e);}	

		log.warn("expect next message: WARN jmri.symbolicprog.VariableTableModel  - Element missing mask attribute: one");
		// and test reading this
		t.setRow(0, el0, ns);
		assert(t.getValueAt(0,0).equals("1"));
		assert(t.getValueAt(0,1).equals("one"));

		t.setRow(1, el1, ns);
		assert(t.getValueAt(1,0).equals("4"));
		assert(t.getValueAt(1,1).equals("two"));

		assert(t.getRowCount() == 2);
		
	}

	// Check creating a longaddr type, walk through its programming
	public void testVarTableLoadLongAddr() {
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
												.addAttribute("CV","17")
												.addAttribute("mask","VVVVVVVV")
												.addAttribute("name","long")
												.addContent( new Element("longAddressVal", ns)
													)
												)
										)	// variables element									
						) // decoder element
			; // end of adding contents

		// print JDOM tree, to check
		//OutputStream o = System.out;
		//XMLOutputter fmt = new XMLOutputter();
		//fmt.setNewlines(true);   // pretty printing
		//fmt.setIndent(true);
		//try {
		//	 fmt.output(doc, o);
		//} catch (Exception e) { System.out.println("error writing XML: "+e);}	

		// and test reading this
		t.setRow(0, el0, ns);
		assert(t.getValueAt(0,0).equals("17"));
		assert(t.getValueAt(0,1).equals("long"));

		assert(t.getRowCount() == 1);
		
	}
	
		// Check creating bogus XML (unknown variable type)
	public void testVarTableLoadBogus() {
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
												.addAttribute("CV","17")
												.addAttribute("mask","VVVVVVVV")
												.addAttribute("name","long")
												.addContent( new Element("bogusVal", ns)
													)
												)
										)	// variables element									
						) // decoder element
			; // end of adding contents

		// print JDOM tree, to check
		//OutputStream o = System.out;
		//XMLOutputter fmt = new XMLOutputter();
		//fmt.setNewlines(true);   // pretty printing
		//fmt.setIndent(true);
		//try {
		//	 fmt.output(doc, o);
		//} catch (Exception e) { System.out.println("error writing XML: "+e);}	

		// and test reading this
		log.warn("expect next message: ERROR jmri.symbolicprog.VariableTableModel  - Did not find a valid variable type");
		t.setRow(0, el0, ns);
		assert(t.getRowCount() == 0);
		
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
