/** 
 * DecoderIndexFileTest.java
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
import com.sun.java.util.collections.List;

public class DecoderIndexFileTest extends TestCase {

	public void testLoading() {
		// setup the test object with guts
		DecoderIndexFile di = new DecoderIndexFile();
		setupDoc();
		// invoke parsing
		di.readMfgSection(decoderIndexElement);
		di.readFamilySection(decoderIndexElement);
		// success here is getting to the end
	}
	
	public void testMfgSection() {
		// setup the test object with guts
		DecoderIndexFile di = new DecoderIndexFile();
		setupDoc();
		// invoke parsing
		di.readMfgSection(decoderIndexElement);
		// check results
		Assert.assertEquals("Digitrax ID from name ", "129", di.mfgIdFromName("Digitrax"));
		Assert.assertEquals("NMRA ID from name ", null, di.mfgIdFromName("NMRA"));
		Assert.assertEquals("Digitrax name from id ", "Digitrax", di.mfgNameFromId("129"));
	}

	public void testReadFamilySection() {
		// setup the test object with guts
		DecoderIndexFile di = new DecoderIndexFile();
		setupDoc();
		// invoke parsing
		di.readMfgSection(decoderIndexElement);
		di.readFamilySection(decoderIndexElement);
		// check first Digitrax decoder in test tree; actually the 3rd decoder
		Assert.assertEquals("1st decoder model ", "DH142", ((DecoderFile)di.decoderList.get(2)).getModel());
		Assert.assertEquals("1st decoder mfg ", "Digitrax", ((DecoderFile)di.decoderList.get(2)).getMfg());
		Assert.assertEquals("1st decoder mfgID ", "129", ((DecoderFile)di.decoderList.get(2)).getMfgID());
		Assert.assertEquals("1st decoder low versionID ", "21", ((DecoderFile)di.decoderList.get(2)).getLowVersionID());
		Assert.assertEquals("1st decoder family ", "**2 family", ((DecoderFile)di.decoderList.get(2)).getFamily());
	}
		
	public void testReadFamily1() {
		// setup the test object with guts
		DecoderIndexFile di = new DecoderIndexFile();
		setupDoc();
		di.readMfgSection(decoderIndexElement);
		// parse a single Family
		di.readFamily(family1);
		// expect to find two decoders in a single family
		Assert.assertEquals("number of decoders ", 2, di.numDecoders());
		// check second one
		Assert.assertEquals("2nd decoder model ", "required set", ((DecoderFile)di.decoderList.get(1)).getModel());
		Assert.assertEquals("2nd decoder mfg ", "NMRA", ((DecoderFile)di.decoderList.get(1)).getMfg());
		Assert.assertEquals("2nd decoder mfgID ", null, ((DecoderFile)di.decoderList.get(1)).getMfgID());
		Assert.assertEquals("2nd decoder low versionID ", null, ((DecoderFile)di.decoderList.get(1)).getLowVersionID());
		Assert.assertEquals("2nd decoder family ", "NMRA S&RP definitions", ((DecoderFile)di.decoderList.get(1)).getFamily());
	}

	public void testReadFamily2() {
		// setup the test object with guts
		DecoderIndexFile di = new DecoderIndexFile();
		setupDoc();
		di.readMfgSection(decoderIndexElement);
		// parse a single Family
		di.readFamily(family2);
		// expect to find two decoders in a single family
		Assert.assertEquals("number of decoders ", 2, di.numDecoders());
		// check first one
		Assert.assertEquals("1st decoder model ", "DH142", ((DecoderFile)di.decoderList.get(0)).getModel());
		Assert.assertEquals("1st decoder mfg ", "Digitrax", ((DecoderFile)di.decoderList.get(0)).getMfg());
		Assert.assertEquals("1st decoder mfgID ", "129", ((DecoderFile)di.decoderList.get(0)).getMfgID());
		Assert.assertEquals("1st decoder low versionID ", "21", ((DecoderFile)di.decoderList.get(0)).getLowVersionID());
		Assert.assertEquals("1st decoder family ", "**2 family", ((DecoderFile)di.decoderList.get(0)).getFamily());
		Assert.assertEquals("1st decoder numFns ", 4, ((DecoderFile)di.decoderList.get(0)).getNumFunctions());
		Assert.assertEquals("1st decoder numOuts ", 2, ((DecoderFile)di.decoderList.get(0)).getNumOutputs());
		// check second one
		Assert.assertEquals("2nd decoder model ", "DN142", ((DecoderFile)di.decoderList.get(1)).getModel());
		Assert.assertEquals("2nd decoder mfg ", "Digitrax", ((DecoderFile)di.decoderList.get(1)).getMfg());
		Assert.assertEquals("2nd decoder mfgID ", "129", ((DecoderFile)di.decoderList.get(1)).getMfgID());
		Assert.assertEquals("2nd decoder low versionID ", "22", ((DecoderFile)di.decoderList.get(1)).getLowVersionID());
		Assert.assertEquals("2nd decoder family ", "**2 family", ((DecoderFile)di.decoderList.get(1)).getFamily());
		Assert.assertEquals("2nd decoder numFns ", 5, ((DecoderFile)di.decoderList.get(1)).getNumFunctions());
		Assert.assertEquals("2nd decoder numOuts ", 1, ((DecoderFile)di.decoderList.get(1)).getNumOutputs());
	}
		
	public void testMatchingDecoderList() {
		// setup the test object with guts
		DecoderIndexFile di = new DecoderIndexFile();
		setupDoc();
		// invoke parsing
		di.readMfgSection(decoderIndexElement);
		di.readFamilySection(decoderIndexElement);
		// search for the two Digitrax decoders
		List l1 = di.matchingDecoderList("Digitrax", null, null, null, null);
		Assert.assertEquals("Found with name Digitrax ", 2, l1.size());
		// search for the two decoders from mfgID 129
		List l2 = di.matchingDecoderList(null, null, "129", null, null);
		Assert.assertEquals("Found with id 129 ", 2, l2.size());
		// search for the two from the NMRA family
		List l4 = di.matchingDecoderList(null, "NMRA S&RP definitions", null, null, null);
		Assert.assertEquals("Found from NMRA family ", 2, l4.size());
		// search for the one with version ID 21
		List l3 = di.matchingDecoderList(null, null, null, "21", null);
		Assert.assertEquals("Found with version 21 ", 1, l3.size());
	}

	public void testMatchingVersionRange() {
		// setup the test object with guts
		DecoderIndexFile di = new DecoderIndexFile();
		setupDoc();
		// invoke parsing
		di.readMfgSection(decoderIndexElement);
		di.readFamilySection(decoderIndexElement);
		// search for the one with various version IDs
		List l3;
		l3 = di.matchingDecoderList(null, null, null, "20", null);
		Assert.assertEquals("Found with version 20 ", 0, l3.size());
		l3 = di.matchingDecoderList(null, null, null, "21", null);
		Assert.assertEquals("Found with version 21 ", 1, l3.size());
		l3 = di.matchingDecoderList(null, null, null, "22", null);
		Assert.assertEquals("Found with version 22 ", 1, l3.size());
		l3 = di.matchingDecoderList(null, null, null, "23", null);
		Assert.assertEquals("Found with version 23 ", 1, l3.size());
		l3 = di.matchingDecoderList(null, null, null, "24", null);
		Assert.assertEquals("Found with version 24 ", 1, l3.size());
		l3 = di.matchingDecoderList(null, null, null, "25", null);
		Assert.assertEquals("Found with version 25 ", 0, l3.size());
	}

	// static variables for the test XML structures
	Element root = null;
	Document doc = null;
	Element decoderIndexElement = null;
	Element family1 = null;
	Element family2 = null;
	
	// provide a test document in the above static variables
	void setupDoc() {
		// create a JDOM tree with just some elements
		root = new Element("decoderIndex-config");
		doc = new Document(root);
		doc.setDocType(new DocType("decoderIndex-config","decoderIndex-config.dtd"));
		
		// add some elements
		root.addContent(decoderIndexElement = new Element("decoderIndex")
					.addContent(new Element("mfgList")
									.addContent(new Element("manufacturer")
										.addAttribute("mfg", "NMRA")
												)
									.addContent(new Element("manufacturer")
										.addAttribute("mfg", "Digitrax")
										.addAttribute("mfgID", "129")
												)
								)
					.addContent(new Element("familyList")
									.addContent(family1 = new Element("family")
										.addAttribute("mfg", "NMRA")
										.addAttribute("name", "NMRA S&RP definitions")
										.addAttribute("file", "NMRA.xml")
										.addContent(new Element("decoder")
												.addAttribute("model", "full set")
												.addAttribute("comment", "all CVs in RP 9.2.1")
												   )
										.addContent(new Element("decoder")
												.addAttribute("model", "required set")
												.addAttribute("comment", "required CVs in RP 9.2.1")
												   )
												)
									.addContent(family2 = new Element("family")
										.addAttribute("mfg", "Digitrax")
										.addAttribute("name", "**2 family")
										.addAttribute("file", "DH142.xml")
										.addAttribute("lowVersionID", "21")
										.addContent(new Element("decoder")
												.addAttribute("model", "DH142")
												.addAttribute("numFns", "4")
												.addAttribute("numOuts", "2")
												// no versionID here, so use 21 from parent
												   )
										.addContent(new Element("decoder")
												.addAttribute("model", "DN142")
												.addAttribute("numFns", "5")
												.addAttribute("numOuts", "1")
												.addAttribute("lowVersionID", "22")
												.addAttribute("highVersionID", "24")
												   )
												)
								)
				)
			; // end of adding contents
		
		return;
	}

	// from here down is testing infrastructure
	
	public DecoderIndexFileTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {DecoderIndexFileTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}
	
	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(DecoderIndexFileTest.class);
		return suite;
	}
	
	// static private org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DecoderIndexFileTest.class.getName());

}
