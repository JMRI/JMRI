/** 
 * SymbolicProgTest.java
 *
 * Description:	
 * @author			Bob Jacobsen
 * @version			
 */

package jmri.tests.symbolicprog;

import java.io.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jdom.*;
import org.jdom.output.*;

import jmri.symbolicprog.*;

public class SymbolicProgTest extends TestCase {

	// Can we create a table?
	public void testCreate() {
		String[] args = {"CV", "Name"};
		VariableTableModel t = new VariableTableModel(args);
	}

	// Check column count member fn, column names
	public void testColumnCount() {
		String[] args = {"CV", "Name"};
		VariableTableModel t = new VariableTableModel(args);
		assert(t.getColumnCount() == 2);
		assert(t.getColumnName(1) == "Name");
	}

	// Check loading two columns, three rows
	public void testLoad_2_3() {
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
	
}
