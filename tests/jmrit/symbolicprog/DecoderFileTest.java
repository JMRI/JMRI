/** 
 * DecoderFileTest.java
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
import org.jdom.*;
import org.jdom.output.*;

public class DecoderFileTest extends TestCase {

	public void testStart() {
		new DecoderFile();
	}
	
	public void testMfgName() {
		setupDoc();
		Assert.assertEquals("mfg name ", "Digitrax", DecoderFile.getMfgName(decoder, ns));
	}
	
	public void testModelName() {
		setupDoc();
		Assert.assertEquals("model name ", "DH142", DecoderFile.getModelName(decoder, ns));
	}
	
	public void testLoadTable() {
		setupDoc();

		// this test should probably be done in terms of a test class instead of the real one...
		JLabel progStatus       	= new JLabel(" OK ");
		CvTableModel	cvModel		= new CvTableModel(progStatus);
		VariableTableModel		variableModel	= new VariableTableModel(progStatus,
					new String[]  {"Name", "Value"},
					cvModel);
		
		DecoderFile.loadVariableModel(decoder, ns, variableModel);
		Assert.assertEquals("read rows ", 1, variableModel.getRowCount());
		Assert.assertEquals("first row name ", "Address", variableModel.getName(0));
	}
	
	// static variables for the test XML structures
	Namespace ns = null;
	Element root = null;
	Element decoder = null;
	Document doc = null;
	
	// provide a test document in the above static variables
	void setupDoc() {
		// create a JDOM tree with just some elements
		ns = Namespace.getNamespace("decoder", "");
		root = new Element("decoder-config", ns);
		doc = new Document(root);
		doc.setDocType(new DocType("decoder:decoder-config","DTD/decoder-config.dtd"));
		
		// add some elements
		root.addContent(decoder = new Element("decoder",ns)
					.addContent(new Element("id", ns)
									.addAttribute("model","DH142")
									.addAttribute("mfg","Digitrax")
									.addAttribute("defnVersion","242")
									.addAttribute("mfgID","129")
									.addAttribute("comment","DH142 decoder: FX, transponding")
								)
					.addContent(new Element("programming", ns)
									.addAttribute("direct","byteOnly")
									.addAttribute("paged","yes")
									.addAttribute("register","yes")
									.addAttribute("ops","yes")
								)
					.addContent(new Element("variables", ns)
									.addContent(new Element("variable", ns)
										.addAttribute("name", "Address")
										.addAttribute("CV", "1")
										.addAttribute("mask", "VVVVVVVV")
										.addAttribute("readOnly", "no")
										.addContent(new Element("decVal", ns)
											.addAttribute("max", "127")
													)
												)
								)
						)
			; // end of adding contents
		
		return;
	}

	
	// from here down is testing infrastructure
	
	public DecoderFileTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {DecoderFileTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}
	
	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(DecoderFileTest.class);
		return suite;
	}
	
	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DecoderFileTest.class.getName());

}
