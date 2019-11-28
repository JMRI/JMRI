package jmri.jmrit.decoderdefn;

import java.util.List;
import javax.swing.JComboBox;
import jmri.util.JUnitUtil;
import org.jdom2.DocType;
import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for DecoderIndexFile class.
 *
 * @author	Bob Jacobsen, Copyright (c) 2001, 2002
 */
public class DecoderIndexFileTest {

    @Test
    public void testLoading() {
        // setup the test object with guts
        DecoderIndexFile di = new DecoderIndexFile();
        setupDoc();
        // invoke parsing
        di.readMfgSection(decoderIndexElement);
        di.readFamilySection(decoderIndexElement);
        // success here is getting to the end
    }

    @Test
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

    @Test
    public void testReadFamilySection() {
        // setup the test object with guts
        DecoderIndexFile di = new DecoderIndexFile();
        setupDoc();
        // invoke parsing
        di.readMfgSection(decoderIndexElement);
        di.readFamilySection(decoderIndexElement);
        // check first Digitrax decoder in test tree; actually the 5th decoder (counting 2 families)
        Assert.assertEquals("1st decoder model ", "DH142", (di.decoderList.get(4)).getModel());
        Assert.assertEquals("1st decoder mfg ", "Digitrax", (di.decoderList.get(4)).getMfg());
        Assert.assertEquals("1st decoder mfgID ", "129", (di.decoderList.get(4)).getMfgID());
        Assert.assertEquals("1st decoder family ", "FX2 family", (di.decoderList.get(4)).getFamily());
    }

    @Test
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
        Assert.assertEquals("2nd decoder model ", "full set", (di.decoderList.get(1)).getModel());
        Assert.assertEquals("2nd decoder mfg ", "NMRA", (di.decoderList.get(1)).getMfg());
        Assert.assertEquals("2nd decoder mfgID ", null, (di.decoderList.get(1)).getMfgID());
        Assert.assertEquals("2nd decoder family ", "NMRA S&RP definitions", (di.decoderList.get(1)).getFamily());
    }

    @Test
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
        Assert.assertEquals("1st decoder model ", "FX2 family", (di.decoderList.get(0)).getModel());
        Assert.assertEquals("1st decoder mfg ", "Digitrax", (di.decoderList.get(0)).getMfg());
        Assert.assertEquals("1st decoder mfgID ", "129", (di.decoderList.get(0)).getMfgID());
        Assert.assertEquals("1st decoder family ", "FX2 family", (di.decoderList.get(0)).getFamily());
        Assert.assertEquals("1st decoder numFns ", -1, (di.decoderList.get(0)).getNumFunctions());
        Assert.assertEquals("1st decoder numOuts ", -1, (di.decoderList.get(0)).getNumOutputs());
        // check first read decoder
        Assert.assertEquals("1st decoder model ", "DH142", (di.decoderList.get(1)).getModel());
        Assert.assertEquals("1st decoder mfg ", "Digitrax", (di.decoderList.get(1)).getMfg());
        Assert.assertEquals("1st decoder mfgID ", "129", (di.decoderList.get(1)).getMfgID());
        Assert.assertEquals("1st decoder family ", "FX2 family", (di.decoderList.get(1)).getFamily());
        Assert.assertEquals("1st decoder numFns ", 4, (di.decoderList.get(1)).getNumFunctions());
        Assert.assertEquals("1st decoder numOuts ", 2, (di.decoderList.get(1)).getNumOutputs());
        // check second real decoder
        Assert.assertEquals("2nd decoder model ", "DN142", (di.decoderList.get(2)).getModel());
        Assert.assertEquals("2nd decoder mfg ", "Digitrax", (di.decoderList.get(2)).getMfg());
        Assert.assertEquals("2nd decoder mfgID ", "129", (di.decoderList.get(2)).getMfgID());
        Assert.assertEquals("2nd decoder family ", "FX2 family", (di.decoderList.get(2)).getFamily());
        Assert.assertEquals("2nd decoder numFns ", 5, (di.decoderList.get(2)).getNumFunctions());
        Assert.assertEquals("2nd decoder numOuts ", 1, (di.decoderList.get(2)).getNumOutputs());
    }

    @Test
    public void testMatchingDecoderList() {
        // setup the test object with guts
        DecoderIndexFile di = new DecoderIndexFile();
        setupDoc();
        // invoke parsing
        di.readMfgSection(decoderIndexElement);
        di.readFamilySection(decoderIndexElement);
        // search for the two Digitrax decoders
        List<DecoderFile> l1 = di.matchingDecoderList("Digitrax", null, null, null, null, null);
        Assert.assertEquals("Found with name Digitrax ", 3, l1.size());
        Assert.assertEquals("Found with name Digitrax ", "DH142", (l1.get(1)).getModel());
        Assert.assertEquals("Found with name Digitrax ", "DN142", (l1.get(2)).getModel());
        // search for the two decoders from mfgID 129
        List<DecoderFile> l2 = di.matchingDecoderList(null, null, "129", null, null, null);
        Assert.assertEquals("Found with id 129 ", 3, l2.size());
        // search for the two from the NMRA family
        List<DecoderFile> l4 = di.matchingDecoderList(null, "NMRA S&RP definitions", null, null, null, null);
        Assert.assertEquals("Found from NMRA family ", 3, l4.size());
        // search for the one with version ID 21
        List<DecoderFile> l3 = di.matchingDecoderList(null, null, null, "21", null, null);
        Assert.assertEquals("Found with version 21 ", 1, l3.size());
    }

    @Test
    public void testMatchingComboBox() {
        // setup the test object with guts
        DecoderIndexFile di = new DecoderIndexFile();
        setupDoc();
        // invoke parsing
        di.readMfgSection(decoderIndexElement);
        di.readFamilySection(decoderIndexElement);
        // search for the two Digitrax decoders
        JComboBox<String> l1 = di.matchingComboBox("Digitrax", null, null, null, null, null);
        Assert.assertEquals("Found with name Digitrax ", 3, l1.getItemCount());
        Assert.assertEquals("Found with name Digitrax ", "DH142 (FX2 family)", l1.getItemAt(1));
        Assert.assertEquals("Found with name Digitrax ", "DN142 (FX2 family)", l1.getItemAt(2));
        // search for the two decoders from mfgID 129
        JComboBox<String> l2 = di.matchingComboBox(null, null, "129", null, null, null);
        Assert.assertEquals("Found with id 129 ", 3, l2.getItemCount());
        // search for the two from the NMRA family
        JComboBox<String> l4 = di.matchingComboBox(null, "NMRA S&RP definitions", null, null, null, null);
        Assert.assertEquals("Found from NMRA family ", 3, l4.getItemCount());
        // search for the one with version ID 21
        JComboBox<String> l3 = di.matchingComboBox(null, null, null, "21", null, null);
        Assert.assertEquals("Found with version 21 ", 1, l3.getItemCount());
    }

    @Test
    public void testMatchingVersionRange() {
        // setup the test object with guts
        DecoderIndexFile di = new DecoderIndexFile();
        setupDoc();
        // invoke parsing
        di.readMfgSection(decoderIndexElement);
        di.readFamilySection(decoderIndexElement);
        // search for the one with various version IDs
        List<DecoderFile> l3;
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
        doc.setDocType(new DocType("decoderIndex-config", "decoderIndex-config.dtd"));

        // add some elements
        root.addContent(decoderIndexElement = new Element("decoderIndex")
                .addContent(new Element("mfgList")
                        .addContent(new Element("manufacturer")
                                .setAttribute("mfg", "NMRA")
                        )
                        .addContent(new Element("manufacturer")
                                .setAttribute("mfg", "Digitrax")
                                .setAttribute("mfgID", "129")
                        )
                )
                .addContent(new Element("familyList")
                        .addContent(family1 = new Element("family")
                                .setAttribute("mfg", "NMRA")
                                .setAttribute("name", "NMRA S&RP definitions")
                                .setAttribute("file", "NMRA.xml")
                                .addContent(new Element("model")
                                        .setAttribute("model", "full set")
                                        .setAttribute("comment", "all CVs in RP 9.2.1")
                                )
                                .addContent(new Element("model")
                                        .setAttribute("model", "required set")
                                        .setAttribute("comment", "required CVs in RP 9.2.1")
                                )
                        )
                        .addContent(family2 = new Element("family")
                                .setAttribute("mfg", "Digitrax")
                                .setAttribute("name", "FX2 family")
                                .setAttribute("file", "DH142.xml")
                                .addContent(new Element("model")
                                        .setAttribute("model", "DH142")
                                        .setAttribute("numFns", "4")
                                        .setAttribute("numOuts", "2")
                                        .setAttribute("lowVersionID", "21")
                                )
                                .addContent(new Element("model")
                                        .setAttribute("model", "DN142")
                                        .setAttribute("numFns", "5")
                                        .setAttribute("numOuts", "1")
                                        .addContent(new Element("versionCV")
                                                .setAttribute("lowVersionID", "22")
                                                .setAttribute("highVersionID", "24")
                                        )
                                )
                        )
                )
        ); // end of adding contents

        return;
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(DecoderIndexFileTest.class);

}
