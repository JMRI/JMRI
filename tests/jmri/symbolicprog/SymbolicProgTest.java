/** 
 * SymbolicProgTest.java
 *
 * Description:	
 * @author			Bob Jacobsen
 * @version			
 */

package jmri.tests.symbolicprog;

import java.io.*;
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
		while ( i++ < 100 && cv.isBusy() )  {
			try {
				Thread.sleep(10);
			} catch (Exception e) {
			}
		}
		if (log.isDebugEnabled()) log.debug("past loop, i="+i+" value="+cv.getValue()+" state="+cv.getState());
		
		assert(cv.getValue() == 123);
		assert(cv.getState() == CvValue.READ);
	}

	// check a write operation
	public void testCvValWrite() {
		// initialize the system
		Programmer p = new ProgDebugger();
		InstanceManager.setProgrammer(p);
		
		// create the CV value
		CvValue cv = new CvValue(91);
		cv.setValue(12);
		cv.write(); 
		// wait for reply (normally, done by callback; will check that later)
		int i = 0;
		while ( i++ < 100 && cv.isBusy() )  {
			try {
				Thread.sleep(10);
			} catch (Exception e) {
			}
		}
		if (log.isDebugEnabled()) log.debug("past loop, i="+i+" value="+cv.getValue()+" state="+cv.getState());
		
		assert(cv.getValue() == 12);
		assert(cv.getState() == CvValue.STORED);
	}
	
	// check the state diagram
	public void testCvValStates() {
		CvValue cv = new CvValue(21);
		assert(cv.getState() == CvValue.UNKNOWN);
		cv.setValue(23);
		assert(cv.getState() == CvValue.EDITTED);
	}
		
		
// VariableValue tests via decimal subclass
	// can we create one and manipulate info?
	public void testVariableValueCreate() {
		DecVariableValue var = new DecVariableValue("name", "comment", false, 81, "VVVVVVVV", null);
		assert(var.name() == "name");
		System.out.println(" name "+ var.name());
		((JTextField)var.getValue()).setText("123");
		System.out.println("val "+ ((JTextField)var.getValue()).getText() );
		assert( ((JTextField)var.getValue()).getText().equals("123") );
	}
	
	// check a read operation
	public void testVariableValueRead() {
		// initialize the system
		Programmer p = new ProgDebugger();
		InstanceManager.setProgrammer(p);
		
		// create the CV value
		DecVariableValue var = new DecVariableValue("name", "comment", false, 81, "VVVVVVVV", null);
		var.read();
		// wait for reply (normally, done by callback; will check that later)
		int i = 0;
		while ( i++ < 100 && var.isBusy() )  {
			try {
				Thread.sleep(10);
			} catch (Exception e) {
			}
		}
		if (log.isDebugEnabled()) log.debug("past loop, i="+i+" value="+var.getValue()+" state="+var.getState());
		
		// assert(cv.getValue() == 123);
		// assert(cv.getState() == CvValue.READ);
	}

	// check a write operation
	public void testVariableValueWrite() {
		// initialize the system
		Programmer p = new ProgDebugger();
		InstanceManager.setProgrammer(p);
		
		// create the CV value
		DecVariableValue var = new DecVariableValue("name", "comment", false, 81, "VVVVVVVV", null);
		var.setValue(12);
		var.write(); 
		// wait for reply (normally, done by callback; will check that later)
		int i = 0;
		while ( i++ < 100 && var.isBusy() )  {
			try {
				Thread.sleep(10);
			} catch (Exception e) {
			}
		}
		if (log.isDebugEnabled()) log.debug("past loop, i="+i+" value="+var.getValue()+" state="+var.getState());
		
		//assert(cv.getValue() == 12);
		//assert(cv.getState() == CvValue.STORED);
	}
	
	// check the state diagram
	public void testVariableValueStates() {
		DecVariableValue var = new DecVariableValue("name", "comment", false, 81, "VVVVVVVV", null);
		assert(var.getState() == VariableValue.UNKNOWN);
		var.setValue(23);
		assert(var.getState() == VariableValue.EDITTED);
	}
		
	
// VariableTableModel tests
	// Can we create a table?
	public void testVarTableCreate() {
		String[] args = {"CV", "Name"};
		VariableTableModel t = new VariableTableModel(args);
	}

	// Check column count member fn, column names
	public void testVarTableColumnCount() {
		String[] args = {"CV", "Name"};
		VariableTableModel t = new VariableTableModel(args);
		assert(t.getColumnCount() == 2);
		assert(t.getColumnName(1) == "Name");
	}

	// Check loading two columns, three rows
	public void testVarTableLoad_2_3() {
		String[] args = {"CV", "Name"};
		VariableTableModel t = new VariableTableModel(args);
		t.setNumRows(3);
		assert(t.getRowCount() == 3);
		
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
												.addAttribute("CV","2")
												.addAttribute("name","two")
												)
										)	// variables element									
						) // decoder element
			; // end of adding contents
		// print, to check
		// OutputStream o = System.out;
		// XMLOutputter fmt = new XMLOutputter();
		// fmt.setNewlines(true);   // pretty printing
		// fmt.setIndent(true);
		// try {
			// fmt.output(doc, o);
		// } catch (Exception e) { System.out.println("error writing XML: "+e);}	

		// and test reading this
		t.setRow(0, el0, ns);
		assert(t.getValueAt(0,0) == "1");
		assert(t.getValueAt(0,1) == "one");
		t.setRow(1, el1, ns);
		assert(t.getValueAt(1,0) == "2");
		assert(t.getValueAt(1,1) == "two");
		
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
