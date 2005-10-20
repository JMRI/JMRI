// DecoderIndexFileTest.java

package jmri.jmrit.decoderdefn;

import javax.swing.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Assert;
import org.jdom.*;
import com.sun.java.util.collections.List;

/**
 * Tests for DecoderIndexFile class
 *
 * @author			Bob Jacobsen, Copyright (c) 2001, 2002
 * @version			$Revision: 1.6 $
 */
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
        // check first Digitrax decoder in test tree; actually the 5th decoder (counting 2 families)
        Assert.assertEquals("1st decoder model ", "DH142", ((DecoderFile)di.decoderList.get(4)).getModel());
        Assert.assertEquals("1st decoder mfg ", "Digitrax", ((DecoderFile)di.decoderList.get(4)).getMfg());
        Assert.assertEquals("1st decoder mfgID ", "129", ((DecoderFile)di.decoderList.get(4)).getMfgID());
        Assert.assertEquals("1st decoder family ", "FX2 family", ((DecoderFile)di.decoderList.get(4)).getFamily());
    }

    public void testReadFamily1() {
        // setup the test object with guts
        DecoderIndexFile di = new DecoderIndexFile();
        setupDoc();
        di.readMfgSection(decoderIndexElement);
        // parse a single Family
        di.readFamily(family1);
        // expect to find two decoders in a single family
        Assert.assertEquals("number of decoders ", 3, di.numDecoders());
        // check second one
        Assert.assertEquals("2nd decoder model ", "full set", ((DecoderFile)di.decoderList.get(1)).getModel());
        Assert.assertEquals("2nd decoder mfg ", "NMRA", ((DecoderFile)di.decoderList.get(1)).getMfg());
        Assert.assertEquals("2nd decoder mfgID ", null, ((DecoderFile)di.decoderList.get(1)).getMfgID());
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
        Assert.assertEquals("number of decoders ", 3, di.numDecoders());
        // check family entry
        Assert.assertEquals("1st decoder model ", "FX2 family", ((DecoderFile)di.decoderList.get(0)).getModel());
        Assert.assertEquals("1st decoder mfg ", "Digitrax", ((DecoderFile)di.decoderList.get(0)).getMfg());
        Assert.assertEquals("1st decoder mfgID ", "129", ((DecoderFile)di.decoderList.get(0)).getMfgID());
        Assert.assertEquals("1st decoder family ", "FX2 family", ((DecoderFile)di.decoderList.get(0)).getFamily());
        Assert.assertEquals("1st decoder numFns ", -1, ((DecoderFile)di.decoderList.get(0)).getNumFunctions());
        Assert.assertEquals("1st decoder numOuts ", -1, ((DecoderFile)di.decoderList.get(0)).getNumOutputs());
        // check first read decoder
        Assert.assertEquals("1st decoder model ", "DH142", ((DecoderFile)di.decoderList.get(1)).getModel());
        Assert.assertEquals("1st decoder mfg ", "Digitrax", ((DecoderFile)di.decoderList.get(1)).getMfg());
        Assert.assertEquals("1st decoder mfgID ", "129", ((DecoderFile)di.decoderList.get(1)).getMfgID());
        Assert.assertEquals("1st decoder family ", "FX2 family", ((DecoderFile)di.decoderList.get(1)).getFamily());
        Assert.assertEquals("1st decoder numFns ", 4, ((DecoderFile)di.decoderList.get(1)).getNumFunctions());
        Assert.assertEquals("1st decoder numOuts ", 2, ((DecoderFile)di.decoderList.get(1)).getNumOutputs());
        // check second real decoder
        Assert.assertEquals("2nd decoder model ", "DN142", ((DecoderFile)di.decoderList.get(2)).getModel());
        Assert.assertEquals("2nd decoder mfg ", "Digitrax", ((DecoderFile)di.decoderList.get(2)).getMfg());
        Assert.assertEquals("2nd decoder mfgID ", "129", ((DecoderFile)di.decoderList.get(2)).getMfgID());
        Assert.assertEquals("2nd decoder family ", "FX2 family", ((DecoderFile)di.decoderList.get(2)).getFamily());
        Assert.assertEquals("2nd decoder numFns ", 5, ((DecoderFile)di.decoderList.get(2)).getNumFunctions());
        Assert.assertEquals("2nd decoder numOuts ", 1, ((DecoderFile)di.decoderList.get(2)).getNumOutputs());
    }

    public void testMatchingDecoderList() {
        // setup the test object with guts
        DecoderIndexFile di = new DecoderIndexFile();
        setupDoc();
        // invoke parsing
        di.readMfgSection(decoderIndexElement);
        di.readFamilySection(decoderIndexElement);
        // search for the two Digitrax decoders
        List l1 = di.matchingDecoderList("Digitrax", null, null, null, null, null);
        Assert.assertEquals("Found with name Digitrax ", 3, l1.size());
        Assert.assertEquals("Found with name Digitrax ", "DH142", ((DecoderFile)l1.get(1)).getModel());
        Assert.assertEquals("Found with name Digitrax ", "DN142", ((DecoderFile)l1.get(2)).getModel());
        // search for the two decoders from mfgID 129
        List l2 = di.matchingDecoderList(null, null, "129", null, null, null);
        Assert.assertEquals("Found with id 129 ", 3, l2.size());
        // search for the two from the NMRA family
        List l4 = di.matchingDecoderList(null, "NMRA S&RP definitions", null, null, null, null);
        Assert.assertEquals("Found from NMRA family ", 3, l4.size());
        // search for the one with version ID 21
        List l3 = di.matchingDecoderList(null, null, null, "21", null, null);
        Assert.assertEquals("Found with version 21 ", 1, l3.size());
    }

    public void testMatchingComboBox() {
        // setup the test object with guts
        DecoderIndexFile di = new DecoderIndexFile();
        setupDoc();
        // invoke parsing
        di.readMfgSection(decoderIndexElement);
        di.readFamilySection(decoderIndexElement);
        // search for the two Digitrax decoders
        JComboBox l1 = di.matchingComboBox("Digitrax", null, null, null, null, null);
        Assert.assertEquals("Found with name Digitrax ", 3, l1.getItemCount());
        Assert.assertEquals("Found with name Digitrax ", "DH142 (FX2 family)", (String)l1.getItemAt(1));
        Assert.assertEquals("Found with name Digitrax ", "DN142 (FX2 family)", (String)l1.getItemAt(2));
        // search for the two decoders from mfgID 129
        JComboBox l2 = di.matchingComboBox(null, null, "129", null, null, null);
        Assert.assertEquals("Found with id 129 ", 3, l2.getItemCount());
        // search for the two from the NMRA family
        JComboBox l4 = di.matchingComboBox(null, "NMRA S&RP definitions", null, null, null, null);
        Assert.assertEquals("Found from NMRA family ", 3, l4.getItemCount());
        // search for the one with version ID 21
        JComboBox l3 = di.matchingComboBox(null, null, null, "21", null, null);
        Assert.assertEquals("Found with version 21 ", 1, l3.getItemCount());
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
        l3 = di.matchingDecoderList(null, null, null, "20", null, null);
        Assert.assertEquals("Found with version 20 ", 0, l3.size());
        l3 = di.matchingDecoderList(null, null, null, "21", null, null);
        Assert.assertEquals("Found with version 21 ", 1, l3.size());
        l3 = di.matchingDecoderList(null, null, null, "22", null, null);
        Assert.assertEquals("Found with version 22 ", 1, l3.size());
        l3 = di.matchingDecoderList(null, null, null, "23", null, null);
        Assert.assertEquals("Found with version 23 ", 1, l3.size());
        l3 = di.matchingDecoderList(null, null, null, "24", null, null);
        Assert.assertEquals("Found with version 24 ", 1, l3.size());
        l3 = di.matchingDecoderList(null, null, null, "25", null, null);
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
                                                .addContent(new Element("model")
            .addAttribute("model", "full set")
            .addAttribute("comment", "all CVs in RP 9.2.1")
                                                )
                                                .addContent(new Element("model")
            .addAttribute("model", "required set")
            .addAttribute("comment", "required CVs in RP 9.2.1")
                                                )
                                    )
                                    .addContent(family2 = new Element("family")
                                                .addAttribute("mfg", "Digitrax")
                                                .addAttribute("name", "FX2 family")
                                                .addAttribute("file", "DH142.xml")
                                                .addContent(new Element("model")
            .addAttribute("model", "DH142")
            .addAttribute("numFns", "4")
            .addAttribute("numOuts", "2")
            .addAttribute("lowVersionID", "21")
                                                )
                                                .addContent(new Element("model")
            .addAttribute("model", "DN142")
            .addAttribute("numFns", "5")
            .addAttribute("numOuts", "1")
            .addContent(new Element("versionCV")
                        .addAttribute("lowVersionID", "22")
                        .addAttribute("highVersionID", "24")
            )
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

    // The minimal setup for log4J
    apps.tests.Log4JFixture log4jfixtureInst = new apps.tests.Log4JFixture(this);
    protected void setUp() { log4jfixtureInst.setUp(); }
    protected void tearDown() { log4jfixtureInst.tearDown(); }
    // static private org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DecoderIndexFileTest.class.getName());

}
