/** 
 * VariableTableModelTest.java
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

import org.jdom.Document;
import org.jdom.DocType;
import org.jdom.Element;
import org.jdom.Namespace;

public class VariableTableModelTest extends TestCase {

	public void testStart() {
		// create one with some dummy arguments
		new VariableTableModel(
								new JLabel(""),
								new String[] {"Name", "Value"},
								new CvTableModel(new JLabel(""))
							);
	}
	
	
	// Can we create a table?
	public void testVarTableCreate() {
		String[] args = {"CV", "Name"};
		VariableTableModel t = new VariableTableModel(null, args, null);  // CvTableModel ref is null for this test
	}

	// Check column count member fn, column names
	public void testVarTableColumnCount() {
		String[] args = {"CV", "Name"};
		VariableTableModel t = new VariableTableModel(null, args, null);
		assert(t.getColumnCount() == 2);
		assert(t.getColumnName(1) == "Name");
	}

	// Check loading two columns, three rows
	public void testVarTableLoad_2_3() {
		String[] args = {"CV", "Name"};
		VariableTableModel t = new VariableTableModel(null, args, new CvTableModel(null));
		
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
												.addAttribute("readOnly","no")
												.addContent( new Element("decVal", ns)
														.addAttribute("max","31")
														.addAttribute("min","1")
													)
												)
									.addContent(el1 = new Element("variable", ns)
												.addAttribute("CV","4")
												.addAttribute("readOnly","no")
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
		VariableTableModel t = new VariableTableModel(null, args, new CvTableModel(null));
		
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
												.addAttribute("readOnly","no")
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
		VariableTableModel t = new VariableTableModel(null, args, new CvTableModel(null));
		
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
												.addAttribute("readOnly","no")
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
	
	public VariableTableModelTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {VariableTableModelTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}
	
	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(VariableTableModelTest.class);
		return suite;
	}
	
	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(VariableTableModelTest.class.getName());

}
