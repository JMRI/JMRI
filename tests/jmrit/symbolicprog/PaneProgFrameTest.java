/** 
 * PaneProgFrameTest.java
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
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jdom.*;
import org.jdom.output.*;
import org.jdom.input.*;

import jmri.*;
import jmri.progdebugger.*;
import jmri.jmrit.symbolicprog.*;
import jmri.jmrit.decoderdefn.*;

public class PaneProgFrameTest extends TestCase {

	// test creating a pane in config file
	public void testPane() {
		setupDoc();
		
		// create test object with speciai implementation of the newPane(String) operation
		result = null;
		PaneProgFrame p = new PaneProgFrame() {
				public void newPane(String s, Element e, Namespace ns) { result = s; }
			};
	
		// invoke
		result = null;
		p.readConfig(root, ns);
		assertEquals("pane name", "Other", result);
	}

	// test creating columns in a pane
	public void testColumn() {
		setupDoc();
				
		// create test object with speciai implementation of the newColumn(String) operation
		PaneProgFrame p = new PaneProgFrame() {
				public void newColumn(Element e, Namespace ns, JComponent p) { colCount++; }
			};
	
		// invoke
		colCount = 0;
		p.readConfig(root, ns);
		assertEquals("column count", 4, colCount);
	}

	// test specifying variables in columns
	public void testVariables() {
		setupDoc();  // make sure XML document is ready
		
		// create test object with special implementation of the newVariable(String) operation
		PaneProgFrame p = new PaneProgFrame() {
				public void newVariable(Element e, Namespace ns, JComponent p) { varCount++; }
			};
	
		// invoke
		varCount = 0;
		p.readConfig(root, ns);
		assertEquals("variable defn count", 9, varCount);
	}

	// show me the specially-created frame
	public void testFrame() {
		setupDoc();
		PaneProgFrame p = new PaneProgFrame();
		
		// ugly, temporary way to load the decoder info
		jmri.jmrit.decoderdefn.DecoderFileTest t = new jmri.jmrit.decoderdefn.DecoderFileTest("");
		t.setupDecoder();
		p.loadVariables(t.decoder, t.ns);
		
		p.readConfig(root, ns);
		p.pack();
		p.show();
	}
	
	// show me a frame made from files
	public void XtestFileFrame() {
		// Open and parse decoder file
		System.out.println("start decoder file");
		File dfile = new File("xml"+File.separator+"decoders"+File.separator+"NMRA_All.xml");
		Namespace dns = Namespace.getNamespace("decoder",
										"http://jmri.sourceforge.net/xml/decoder");
		SAXBuilder dbuilder = new SAXBuilder(true);  // argument controls validation, on for now
		Document ddoc = null;
		System.out.println("ctors done, do build");
		try {
			ddoc = dbuilder.build(new BufferedInputStream(new FileInputStream(dfile), 40000),"xml"+File.separator);
		}
		catch (Exception e) {
			log.error("Exception in SAXBuilder "+e);
		}
		// find root
		System.out.println("get root");
		Element droot = ddoc.getRootElement();

		// Open and parse programmer file
		System.out.println("start programmer file");
		File pfile = new File("xml"+File.separator+"programmers"+File.separator+"MultiPane.xml");
		Namespace pns = Namespace.getNamespace("programmer",
										"http://jmri.sourceforge.net/xml/programmer");
		SAXBuilder pbuilder = new SAXBuilder(true);  // argument controls validation, on for now
		Document pdoc = null;
		try {
			pdoc = pbuilder.build(new FileInputStream(pfile),"xml"+File.separator);
		}
		catch (Exception e) {
			log.error("Exception in programmer SAXBuilder "+e);
		}
		// find root
		Element proot = pdoc.getRootElement();

		// create the pane programmer
		PaneProgFrame p = new PaneProgFrame();
			
		// load its variables from decoder tree
		System.out.println("decoder object "+droot.getChild("decoder", dns));
		p.loadVariables(droot.getChild("decoder", dns), dns);
		
		// load its programmer config from programmer tree
		System.out.println("programmer object "+proot);
		p.readConfig(proot, pns);
		
		p.pack();
		p.show();
	}

	// static variables for internal classes to report their interpretations
	static String result = null;
	static int colCount = -1;
	static int varCount = -1;
	
	// static variables for the test XML structures
	Namespace ns = null;
	Element root = null;
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
					.addContent(new Element("pane", ns)
									.addAttribute("name","Basic")
									.addContent(new Element("column", ns)
										.addContent(new Element("variable", ns)
													.addAttribute("name", "Address")
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
					.addContent(new Element("pane",ns)
									.addAttribute("name", "CV")
									.addContent(new Element("column", ns)
										.addContent(new Element("cvtable", ns))
												)
								)
					.addContent(new Element("pane",ns)
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
	
	public PaneProgFrameTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {PaneProgFrameTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}
	
	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(PaneProgFrameTest.class);
		return suite;
	}
	
	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PaneProgFrameTest.class.getName());

}
