/** 
 * PaneProgPaneTest.java
 *
 * Description:	
 * @author			Bob Jacobsen
 * @version			
 */

package jmri.jmrit.symbolicprog.tabbedframe;

import java.awt.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import junit.framework.Test;
import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jdom.*;
import org.jdom.output.*;
import org.jdom.input.*;

import jmri.*;
import jmri.progdebugger.*;
import jmri.jmrit.symbolicprog.*;
import jmri.jmrit.decoderdefn.*;
import jmri.jmrit.roster.*;

public class PaneProgPaneTest extends TestCase {

	// test creating columns in a pane
	public void testColumn() {
		setupDoc();
		CvTableModel cvModel = new CvTableModel(new JLabel());
		String[] args = {"CV", "Name"};
		VariableTableModel varModel = new VariableTableModel(null, args, cvModel);
		
		// create test object with speciai implementation of the newColumn(String) operation
		colCount = 0;
		PaneProgPane p = new PaneProgPane("name", pane1, ns, cvModel, varModel) {
				public JPanel newColumn(Element e, Namespace ns) { colCount++; return new JPanel();}
			};
	
		assertEquals("column count", 2, colCount);
	}

	// test specifying variables in columns
	public void testVariables() {
		setupDoc();  // make sure XML document is ready
		CvTableModel cvModel = new CvTableModel(new JLabel());
		String[] args = {"CV", "Name"};
		VariableTableModel varModel = new VariableTableModel(null, args, cvModel);
		
		// create test object with special implementation of the newVariable(String) operation
		varCount = 0;
		PaneProgPane p = new PaneProgPane("name", pane1, ns, cvModel, varModel) {
				public void newVariable(Element e, Namespace ns, JComponent p) { varCount++; }
			};

		assertEquals("variable defn count", 7, varCount);
	}

	// test storage of programming info in list
	public void testVarListFill() {
		setupDoc();  // make sure XML document is ready
		CvTableModel cvModel = new CvTableModel(new JLabel());
		String[] args = {"CV", "Name"};
		VariableTableModel varModel = new VariableTableModel(null, args, cvModel);
		// have to add a couple of defined variables
		Element el0 = new Element("variable", ns)
												.addAttribute("CV","17")
												.addAttribute("readOnly","no")
												.addAttribute("mask","VVVVVVVV")
												.addAttribute("name","Start voltage")
												.addContent( new Element("longAddressVal", ns));
		varModel.setRow(0, el0, ns);
		Element el1 = new Element("variable", ns)
												.addAttribute("CV","17")
												.addAttribute("readOnly","no")
												.addAttribute("mask","VVVVVVVV")
												.addAttribute("name","Primary Address")
												.addContent( new Element("decVal", ns));
		varModel.setRow(1, el1, ns);
		
		// test by invoking
		PaneProgPane p = new PaneProgPane("name", pane1, ns, cvModel, varModel);
		assertEquals("variable list length", 2, p.varList.size());
		assertEquals("1st variable index ", new Integer(1), p.varList.get(0));
		assertEquals("2nd variable index ", new Integer(0), p.varList.get(1));
	}

	// test storage of programming info in list
	public void testPaneRead() {
		if (log.isDebugEnabled()) log.debug("testPaneRead starts");
		// initialize the system
		ProgDebugger pr = new ProgDebugger();
		InstanceManager.setProgrammer(pr);
		setupDoc();  // make sure XML document is ready

		CvTableModel cvModel = new CvTableModel(new JLabel());
		String[] args = {"CV", "Name"};
		VariableTableModel varModel = new VariableTableModel(null, args, cvModel);
		// have to add a couple of defined variables
		Element el0 = new Element("variable", ns)
												.addAttribute("CV","2")
												.addAttribute("readOnly","no")
												.addAttribute("mask","VVVVVVVV")
												.addAttribute("name","Start voltage")
												.addContent( new Element("decVal", ns));
		varModel.setRow(0, el0, ns);
		Element el1 = new Element("variable", ns)
												.addAttribute("CV","1")
												.addAttribute("readOnly","no")
												.addAttribute("mask","VVVVVVVV")
												.addAttribute("name","Primary Address")
												.addContent( new Element("decVal", ns));
		varModel.setRow(1, el1, ns);
		
		PaneProgPane p = new PaneProgPane("name", pane1, ns, cvModel, varModel);

		// test by invoking
		p.readPane();
		
		// wait for reply (normally, done by callback; will check that later)
		int i = 0;
		while ( p.isBusy() && i++ < 100 )  {
			try {
				Thread.sleep(50);
			} catch (Exception e) {
			}
		}
		if (log.isDebugEnabled()) log.debug("past loop, i="+i);
		if (i==0) log.warn("testPaneRead saw an immediate return from isBusy");
		assertTrue("busy period ends before timeout ", i<=100);

		Assert.assertEquals("last cv read ", 2, pr.lastReadCv());

		if (log.isDebugEnabled()) log.debug("testPaneRead ends ok");
	}

	public void testPaneWrite() {
		if (log.isDebugEnabled()) log.debug("testPaneWrite starts");
		// initialize the system
		ProgDebugger pr = new ProgDebugger();
		InstanceManager.setProgrammer(pr);
		setupDoc();  // make sure XML document is ready

		CvTableModel cvModel = new CvTableModel(new JLabel());
		String[] args = {"CV", "Name"};
		VariableTableModel varModel = new VariableTableModel(null, args, cvModel);
		// have to add a couple of defined variables
		Element el0 = new Element("variable", ns)
												.addAttribute("CV","2")
												.addAttribute("readOnly","no")
												.addAttribute("mask","VVVVVVVV")
												.addAttribute("name","Start voltage")
												.addContent( new Element("decVal", ns));
		varModel.setRow(0, el0, ns);
		Element el1 = new Element("variable", ns)
												.addAttribute("CV","1")
												.addAttribute("readOnly","no")
												.addAttribute("mask","VVVVVVVV")
												.addAttribute("name","Primary Address")
												.addContent( new Element("decVal", ns));
		varModel.setRow(1, el1, ns);
		
		PaneProgPane p = new PaneProgPane("name", pane1, ns, cvModel, varModel);

		// test by invoking
		p.writePane();
		
		// wait for reply (normally, done by callback; will check that later)
		int i = 0;
		while ( p.isBusy() && i++ < 100 )  {
			try {
				Thread.sleep(50);
			} catch (Exception e) {
			}
		}
		if (log.isDebugEnabled()) log.debug("past loop, i="+i);
		if (i==0) log.warn("testPaneWrite saw an immediate return from isBusy");
		assertTrue("busy period ends before timeout ", i<=100);

		Assert.assertEquals("last cv written ", 2, pr.lastWriteCv());

		if (log.isDebugEnabled()) log.debug("testPaneWrite ends ok");
	}

	// static variables for internal classes to report their interpretations
	static String result = null;
	static int colCount = -1;
	static int varCount = -1;
	
	// static variables for the test XML structures
	Namespace ns = null;
	Element root = null;
	Element pane1 = null;
	Element pane2 = null;
	Element pane3 = null;
	Document doc = null;
	
	// provide a test document in the above static variables
	void setupDoc() {
		// create a JDOM tree with just some elements
		ns = Namespace.getNamespace("programmer", "");
		root = new Element("programmer-config", ns);
		doc = new Document(root);
		doc.setDocType(new DocType("programmer:programmer-config","DTD/programmer-config.dtd"));
		
		// add some elements
		root.addContent(new Element("programmer",ns)
					.addContent(pane1 = new Element("pane", ns)
									.addAttribute("name","Basic")
									.addContent(new Element("column", ns)
										.addContent(new Element("variable", ns)
													.addAttribute("name", "Primary Address")
													)
										.addContent(new Element("variable", ns)
													.addAttribute("name", "Start voltage")
													)
										.addContent(new Element("variable", ns)
													.addAttribute("name", "Normal direction of motion")
													)
												)
									.addContent(new Element("column", ns)
										.addContent(new Element("variable", ns)
													.addAttribute("name", "Address")
													)
										.addContent(new Element("variable", ns)
													.addAttribute("name", "Normal direction of motion")
													)
										.addContent(new Element("variable", ns)
													.addAttribute("name", "Normal direction of motion")
													.addAttribute("format","checkbox")
													)
										.addContent(new Element("variable", ns)
													.addAttribute("name", "Normal direction of motion")
													.addAttribute("format","radiobuttons")
													)
												)
								)
					.addContent(pane2 = new Element("pane",ns)
									.addAttribute("name", "CV")
									.addContent(new Element("column", ns)
										.addContent(new Element("cvtable", ns))
												)
								)
					.addContent(pane3 = new Element("pane",ns)
									.addAttribute("name", "Other")
									.addContent(new Element("column", ns)
										.addContent(new Element("variable", ns)
													.addAttribute("name", "Address")
													)
										.addContent(new Element("variable", ns)
													.addAttribute("name", "Normal direction of motion")
													)
												)
								)
				)
			; // end of adding contents
		
		return;
	}

	// from here down is testing infrastructure
	
	public PaneProgPaneTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {PaneProgPaneTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}
	
	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(PaneProgPaneTest.class);
		return suite;
	}
	
	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PaneProgPaneTest.class.getName());

}
