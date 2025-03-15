package jmri.jmrit.decoderdefn;

import java.util.List;

import javax.swing.JComboBox;

import jmri.util.JUnitUtil;

import org.jdom2.DocType;
import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.jupiter.api.*;

/**
 * Tests for DecoderIndexFile class.
 *
 * @author Bob Jacobsen, Copyright (c) 2001, 2002, 2025
 */
public class DecoderIndexFileTest {

    @Test
    public void testLoading() throws org.jdom2.JDOMException, java.io.IOException {
        // set up the test object with guts
        DecoderIndexFile di = new DecoderIndexFile();
        setupDoc();
        // invoke parsing
        di.readMfgSection();
        di.readFamilySection(decoderIndexElement);
        // success here is getting to the end
    }

    @Test
    public void testMfgSection() throws org.jdom2.JDOMException, java.io.IOException {
        // set up the test object with guts
        DecoderIndexFile di = new DecoderIndexFile();
        setupDoc();
        // invoke parsing
        di.readMfgSection();
        // check results
        Assertions.assertEquals("129", di.mfgIdFromName("Digitrax"), "Digitrax ID from name ");
        Assertions.assertEquals("999", di.mfgIdFromName("NMRA"), "NMRA ID from name ");
        Assertions.assertEquals("Digitrax", di.mfgNameFromID("129"), "Digitrax name from id ");
    }

    @Test
    public void testReadFamilySection() throws org.jdom2.JDOMException, java.io.IOException {
        // set up the test object with guts
        DecoderIndexFile di = new DecoderIndexFile();
        setupDoc();
        // invoke parsing
        di.readMfgSection();
        di.readFamilySection(decoderIndexElement);
        // check first Digitrax decoder in test tree; actually the 5th decoder (counting 2 families)
        Assertions.assertEquals("DH142", (di.decoderList.get(4)).getModel(), "1st decoder model ");
        Assertions.assertEquals("Digitrax", (di.decoderList.get(4)).getMfg(), "1st decoder mfg ");
        Assertions.assertEquals("129", (di.decoderList.get(4)).getMfgID(), "1st decoder mfgID ");
        Assertions.assertEquals("FX2 family", (di.decoderList.get(4)).getFamily(), "1st decoder family ");
    }

    @Test
    public void testReadFamily1() throws org.jdom2.JDOMException, java.io.IOException {
        // set up the test object with guts
        DecoderIndexFile di = new DecoderIndexFile();
        setupDoc();
        di.readMfgSection();
        // parse a single Family
        di.readFamily(family1);
        // expect to find two decoders in a single family
        Assertions.assertEquals(3, di.numDecoders(), "number of decoders ");
        // check second one
        Assertions.assertEquals("full set", (di.decoderList.get(1)).getModel(), "2nd decoder model ");
        Assertions.assertEquals("NMRA", (di.decoderList.get(1)).getMfg(), "2nd decoder mfg ");
        Assertions.assertEquals("999", (di.decoderList.get(1)).getMfgID(), "2nd decoder mfgID ");
        Assertions.assertEquals("NMRA S&RP definitions", (di.decoderList.get(1)).getFamily(), "2nd decoder family ");
    }

    @Test
    public void testReadFamily2() throws org.jdom2.JDOMException, java.io.IOException {
        // set up the test object with guts
        DecoderIndexFile di = new DecoderIndexFile();
        setupDoc();
        di.readMfgSection();
        // parse a single Family
        di.readFamily(family2);
        // expect to find two decoders in a single family
        Assertions.assertEquals(3, di.numDecoders(), "number of decoders ");
        // check family entry
        Assertions.assertEquals("FX2 family", (di.decoderList.get(0)).getModel(), "1st decoder model ");
        Assertions.assertEquals("Digitrax", (di.decoderList.get(0)).getMfg(), "1st decoder mfg ");
        Assertions.assertEquals("129", (di.decoderList.get(0)).getMfgID(), "1st decoder mfgID ");
        Assertions.assertEquals("FX2 family", (di.decoderList.get(0)).getFamily(), "1st decoder family ");
        Assertions.assertEquals(-1, (di.decoderList.get(0)).getNumFunctions(), "1st decoder numFns ");
        Assertions.assertEquals(-1, (di.decoderList.get(0)).getNumOutputs(), "1st decoder numOuts ");
        // check first read decoder
        Assertions.assertEquals("DH142", (di.decoderList.get(1)).getModel(), "1st decoder model ");
        Assertions.assertEquals("Digitrax", (di.decoderList.get(1)).getMfg(), "1st decoder mfg ");
        Assertions.assertEquals("129", (di.decoderList.get(1)).getMfgID(), "1st decoder mfgID ");
        Assertions.assertEquals("FX2 family", (di.decoderList.get(1)).getFamily(), "1st decoder family ");
        Assertions.assertEquals(4, (di.decoderList.get(1)).getNumFunctions(), "1st decoder numFns ");
        Assertions.assertEquals(2, (di.decoderList.get(1)).getNumOutputs(), "1st decoder numOuts ");
        // check second real decoder
        Assertions.assertEquals("DN142", (di.decoderList.get(2)).getModel(), "2nd decoder model ");
        Assertions.assertEquals("Digitrax", (di.decoderList.get(2)).getMfg(), "2nd decoder mfg ");
        Assertions.assertEquals("129", (di.decoderList.get(2)).getMfgID(), "2nd decoder mfgID ");
        Assertions.assertEquals("FX2 family", (di.decoderList.get(2)).getFamily(), "2nd decoder family ");
        Assertions.assertEquals(5, (di.decoderList.get(2)).getNumFunctions(), "2nd decoder numFns ");
        Assertions.assertEquals(1, (di.decoderList.get(2)).getNumOutputs(), "2nd decoder numOuts ");
    }

    @Test
    public void testMatchingDecoderList() throws org.jdom2.JDOMException, java.io.IOException {
        // set up the test object with guts
        DecoderIndexFile di = new DecoderIndexFile();
        setupDoc();
        // invoke parsing
        di.readMfgSection();
        di.readFamilySection(decoderIndexElement);
        // search for the two Digitrax decoders
        List<DecoderFile> l1 = di.matchingDecoderList("Digitrax", null, null, null, null, null);
        Assertions.assertEquals(3, l1.size(), "Found with name Digitrax ");
        Assertions.assertEquals("DH142", (l1.get(1)).getModel(), "Found with name Digitrax ");
        Assertions.assertEquals("DN142", (l1.get(2)).getModel(), "Found with name Digitrax ");
        // search for the two decoders from mfgID 129
        List<DecoderFile> l2 = di.matchingDecoderList(null, null, "129", null, null, null);
        Assertions.assertEquals(3, l2.size(), "Found with id 129 ");
        // search for the two from the NMRA family
        List<DecoderFile> l3 = di.matchingDecoderList(null, "NMRA S&RP definitions", null, null, null, null);
        Assertions.assertEquals(3, l3.size(), "Found from NMRA family ");
        // search for the one with version ID 21
        List<DecoderFile> l4 = di.matchingDecoderList(null, null, null, "21", null, null);
        Assertions.assertEquals(1, l4.size(), "Found with version 21 ");
        List<DecoderFile> l5 = di.matchingDecoderList("BMODE");
        Assertions.assertEquals(3, l5.size(), "Found with mode BMODE ");
    }

    @Test
    public void testMatchingComboBox() throws org.jdom2.JDOMException, java.io.IOException {
        // set up the test object with guts
        DecoderIndexFile di = new DecoderIndexFile();
        setupDoc();
        // invoke parsing
        di.readMfgSection();
        di.readFamilySection(decoderIndexElement);
        // search for the two Digitrax decoders
        JComboBox<String> l1 = di.matchingComboBox("Digitrax", null, null, null, null, null);
        Assertions.assertEquals(3, l1.getItemCount(), "Found with name Digitrax ");
        Assertions.assertEquals("DH142 (FX2 family)", l1.getItemAt(1), "Found with name Digitrax ");
        Assertions.assertEquals("DN142 (FX2 family)", l1.getItemAt(2), "Found with name Digitrax ");
        // search for the two decoders from mfgID 129
        JComboBox<String> l2 = di.matchingComboBox(null, null, "129", null, null, null);
        Assertions.assertEquals(3, l2.getItemCount(), "Found with id 129 ");
        // search for the two from the NMRA family
        JComboBox<String> l4 = di.matchingComboBox(null, "NMRA S&RP definitions", null, null, null, null);
        Assertions.assertEquals(3, l4.getItemCount(), "Found from NMRA family ");
        // search for the one with version ID 21
        JComboBox<String> l3 = di.matchingComboBox(null, null, null, "21", null, null);
        Assertions.assertEquals(1, l3.getItemCount(), "Found with version 21 ");
    }

    @Test
    public void testMatchingVersionRange() throws org.jdom2.JDOMException, java.io.IOException {
        // set up the test object with guts
        DecoderIndexFile di = new DecoderIndexFile();
        setupDoc();
        // invoke parsing
        di.readMfgSection();
        di.readFamilySection(decoderIndexElement);
        // search for the one with various version IDs
        List<DecoderFile> l3;
        l3 = di.matchingDecoderList(null, null, null, "20", null, null);
        Assertions.assertEquals(0, l3.size(), "Found with version 20 ");
        l3 = di.matchingDecoderList(null, null, null, "21", null, null);
        Assertions.assertEquals(1, l3.size(), "Found with version 21 ");
        l3 = di.matchingDecoderList(null, null, null, "22", null, null);
        Assertions.assertEquals(1, l3.size(), "Found with version 22 ");
        l3 = di.matchingDecoderList(null, null, null, "23", null, null);
        Assertions.assertEquals(1, l3.size(), "Found with version 23 ");
        l3 = di.matchingDecoderList(null, null, null, "24", null, null);
        Assertions.assertEquals(1, l3.size(), "Found with version 24 ");
        l3 = di.matchingDecoderList(null, null, null, "25", null, null);
        Assertions.assertEquals(0, l3.size(), "Found with version 25 ");
    }

    // static variables for the test of XML structures
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
                                .setAttribute("modes", "AMODE,BMODE")
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
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(DecoderIndexFileTest.class);

}
