/** 
 * DecoderFileTest.java
 *
 * Description:	
 * @author			Bob Jacobsen
 * @version			
 */

package jmri.jmrit.decoderdefn;

import java.io.*;
import java.util.*;
import javax.swing.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Assert;
import org.jdom.*;
import org.jdom.output.*;
import jmri.jmrit.symbolicprog.*;

public class DecoderFileTest extends TestCase {
	
	public void testMfgName() {
		setupDecoder();
		Assert.assertEquals("mfg name ", "Digitrax", DecoderFile.getMfgName(decoder));
	}
	
	public void testFamilyName() {
		setupDecoder();
		Assert.assertEquals("Family name ", "DH142 etc", DecoderFile.getFamilyName(decoder));
	}
	
	public void testLoadTable() {
		setupDecoder();

		// this test should probably be done in terms of a test class instead of the real one...
		JLabel progStatus       	= new JLabel(" OK ");
		CvTableModel	cvModel		= new CvTableModel(progStatus);
		VariableTableModel		variableModel	= new VariableTableModel(progStatus,
					new String[]  {"Name", "Value"},
					cvModel);
		DecoderFile d = new DecoderFile("mfg", "mfgID", "model", "23", "24",
										"family", "filename", 16, 16);
		
		d.loadVariableModel(decoder, variableModel);
		Assert.assertEquals("read rows ", 3, variableModel.getRowCount());
		Assert.assertEquals("first row name ", "Address", variableModel.getLabel(0));
		Assert.assertEquals("third row name ", "Normal direction of motion", variableModel.getLabel(2));
	}
	
	public void testMinOut() {
		setupDecoder();

		// this test should probably be done in terms of a test class instead of the real one...
		JLabel progStatus       	= new JLabel(" OK ");
		CvTableModel	cvModel		= new CvTableModel(progStatus);
		VariableTableModel		variableModel	= new VariableTableModel(progStatus,
					new String[]  {"Name", "Value"},
					cvModel);
		DecoderFile d = new DecoderFile("mfg", "mfgID", "model", "23", "24",
										"family", "filename", 16, 3);
		
		d.loadVariableModel(decoder, variableModel);
		Assert.assertEquals("read rows ", 2, variableModel.getRowCount());
	}

	public void testMinFn() {
		setupDecoder();

		// this test should probably be done in terms of a test class instead of the real one...
		JLabel progStatus       	= new JLabel(" OK ");
		CvTableModel	cvModel		= new CvTableModel(progStatus);
		VariableTableModel		variableModel	= new VariableTableModel(progStatus,
					new String[]  {"Name", "Value"},
					cvModel);
		DecoderFile d = new DecoderFile("mfg", "mfgID", "model", "23", "24",
										"family", "filename", 3, 16);
		
		d.loadVariableModel(decoder, variableModel);
		Assert.assertEquals("read rows ", 2, variableModel.getRowCount());
	}

	// static variables for the test XML structures
	Element root = null;
	public Element decoder = null;
	Document doc = null;
	
	// provide a test document in the above static variables
	public void setupDecoder() {
		// create a JDOM tree with just some elements
		root = new Element("decoder-config");
		doc = new Document(root);
		doc.setDocType(new DocType("decoder-config","decoder-config.dtd"));
		
		// add some elements
		root.addContent(decoder = new Element("decoder")
					.addContent(new Element("id")
									.addAttribute("family","DH142 etc")
									.addAttribute("mfg","Digitrax")
									.addAttribute("defnVersion","242")
									.addAttribute("mfgID","129")
									.addAttribute("comment","DH142 decoder: FX, transponding")
								)
					.addContent(new Element("programming")
									.addAttribute("direct","byteOnly")
									.addAttribute("paged","yes")
									.addAttribute("register","yes")
									.addAttribute("ops","yes")
								)
					.addContent(new Element("variables")
									.addContent(new Element("variable")
										.addAttribute("label", "Address")
										.addAttribute("CV", "1")
										.addAttribute("minFn", "4")
										.addAttribute("mask", "VVVVVVVV")
										.addAttribute("readOnly", "no")
										.addContent(new Element("decVal")
											.addAttribute("max", "127")
													)
												)
									.addContent(new Element("variable")
										.addAttribute("label", "Acceleration rate")
										.addAttribute("CV", "3")
										.addAttribute("minOut", "2")
										.addAttribute("mask", "VVVVVVVV")
										.addAttribute("readOnly", "no")
										.addContent(new Element("decVal")
											.addAttribute("max", "127")
													)
												)
									.addContent(new Element("variable")
										.addAttribute("label", "Normal direction of motion")
										.addAttribute("CV", "29")
										.addAttribute("minFn", "2")
										.addAttribute("minOut", "5")
										.addAttribute("mask", "XXXXXXXV")
										.addAttribute("readOnly", "no")
										.addContent(new Element("enumVal")
											.addContent(new Element("enumChoice")
													.addAttribute("choice", "forward")
														)
											.addContent(new Element("enumChoice")
													.addAttribute("choice", "reverse")
														)
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
	
	// static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DecoderFileTest.class.getName());

}
