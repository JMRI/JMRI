package jmri.jmrit.roster;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import jmri.InstanceManager;
import jmri.util.*;

import org.jdom2.JDOMException;
import org.junit.jupiter.api.*;

/**
 * Tests for the jmrit.roster.RosterEntry class.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002, 2018, 2025
 */
public class RosterEntryTest {

    @Test
    public void testCreate() {
        RosterEntry r = new RosterEntry("file here");
        Assertions.assertEquals("file here", r.getFileName(), "file name ");
        Assertions.assertEquals("3", r.getDccAddress(), "DCC Address ");
        Assertions.assertEquals("", r.getRoadName(), "road name ");
        Assertions.assertEquals("", r.getRoadNumber(), "road number ");
        Assertions.assertEquals("", r.getMfg(), "manufacturer ");
        Assertions.assertEquals("", r.getDecoderModel(), "model ");
        Assertions.assertEquals("", r.getDecoderFamily(), "family ");
        Assertions.assertEquals("", r.getProgrammingModes(), "programmingModes ");
    }

    @Test
    public void testPartialLoad() {
        // create Element
        org.jdom2.Element e = new org.jdom2.Element("locomotive")
                .setAttribute("id", "our id 1")
                .setAttribute("fileName", "file here")
                .setAttribute("roadNumber", "431")
                .setAttribute("roadName", "SP")
                .setAttribute("mfg", "Athearn")
                .setAttribute("dccAddress", "1234")
                .addContent(
                        new org.jdom2.Element("locoaddress").addContent(
                                new org.jdom2.Element("dcclocoaddress")
                                .setAttribute("number", "1234")
                                .setAttribute("longaddress", "yes")
                        )
                ); // end create element

        RosterEntry r = new RosterEntry(e);
        // check
        Assertions.assertEquals("file here", r.getFileName(), "file name ");
        Assertions.assertEquals("1234", r.getDccAddress(), "DCC Address ");
        Assertions.assertEquals("SP", r.getRoadName(), "road name ");
        Assertions.assertEquals("431", r.getRoadNumber(), "road number ");
        Assertions.assertEquals("Athearn", r.getMfg(), "manufacturer ");
        Assertions.assertEquals("", r.getDecoderModel(), "model ");
        Assertions.assertEquals("", r.getDecoderFamily(), "family ");
        Assertions.assertEquals("", r.getProgrammingModes(), "programmingModes ");
    }

    @Test
    public void testEmptyLoad() {
        // create Element
        org.jdom2.Element e = new org.jdom2.Element("locomotive")
                .setAttribute("id", "our id 2")
                .setAttribute("fileName", "file here"); // end create element

        RosterEntry r = new RosterEntry(e) {
            @Override
            protected void warnShortLong(String s) {
            }
        };
        // check
        Assertions.assertEquals("file here", r.getFileName(), "file name ");
        Assertions.assertEquals("3", r.getDccAddress(), "DCC Address ");
        Assertions.assertEquals("", r.getRoadName(), "road name ");
        Assertions.assertEquals("", r.getRoadNumber(), "road number ");
        Assertions.assertEquals("", r.getMfg(), "manufacturer ");
        Assertions.assertEquals("", r.getDecoderModel(), "model ");
        Assertions.assertEquals("", r.getDecoderFamily(), "family ");
        Assertions.assertEquals("", r.getProgrammingModes(), "programmingModes ");
    }

    @Test
    public void testFullLoad() {
        // create Element
        org.jdom2.Element e = new org.jdom2.Element("locomotive")
                .setAttribute("id", "our id 3")
                .setAttribute("fileName", "file here")
                .setAttribute("roadNumber", "431")
                .setAttribute("roadName", "SP")
                .setAttribute("mfg", "Athearn")
                .setAttribute("dccAddress", "1234")
                .setAttribute("decoderModes", "AMODE")
                .addContent(new org.jdom2.Element("decoder")
                        .setAttribute("family", "91")
                        .setAttribute("model", "33")
                ); // end create element

        RosterEntry r = new RosterEntry(e) {
            @Override
            protected void warnShortLong(String s) {
            }
        };

        // check
        Assertions.assertEquals("file here", r.getFileName(), "file name ");
        Assertions.assertEquals("1234", r.getDccAddress(), "DCC Address ");
        Assertions.assertEquals("SP", r.getRoadName(), "road name ");
        Assertions.assertEquals("431", r.getRoadNumber(), "road number ");
        Assertions.assertEquals("Athearn", r.getMfg(), "manufacturer ");
        Assertions.assertEquals("33", r.getDecoderModel(), "model ");
        Assertions.assertEquals("91", r.getDecoderFamily(), "family ");
        Assertions.assertEquals("AMODE", r.getProgrammingModes(), "programmingModes ");
    }

    @Test
    public void testFromSchemaFile() throws JDOMException, IOException {

        // Create a RosterEntry from a test xml file
        // This one references the Schema version
        RosterEntry r = RosterEntry.fromFile(new File("java/test/jmri/jmrit/roster/ACL1012-Schema.xml"));

        // check for various values
        Assertions.assertEquals("ACL1012-Schema.xml", r.getFileName(), "file name ");
        Assertions.assertEquals("1012", r.getDccAddress(), "DCC Address ");
        Assertions.assertEquals("Atlantic Coast Line", r.getRoadName(), "road name ");
        Assertions.assertEquals("1012", r.getRoadNumber(), "road number ");
        Assertions.assertEquals("Synch Diesel Sound 1812 - N Scale Atlas Short Board Dropin", r.getDecoderModel(), "model ");
        Assertions.assertEquals("Brilliance Sound Decoders", r.getDecoderFamily(), "family ");
    }

    @Test
    public void testFromDtdFile() throws JDOMException, IOException {

        // Create a RosterEntry from a test xml file
        // This one references the DTD to make sure that still works
        // post migration
        RosterEntry r = RosterEntry.fromFile(new File("java/test/jmri/jmrit/roster/ACL1012-DTD.xml"));

        // check for various values
        Assertions.assertEquals("ACL1012-DTD.xml", r.getFileName(), "file name ");
        Assertions.assertEquals("1012", r.getDccAddress(), "DCC Address ");
        Assertions.assertEquals("Atlantic Coast Line", r.getRoadName(), "road name ");
        Assertions.assertEquals("1012", r.getRoadNumber(), "road number ");
        Assertions.assertEquals("Synch Diesel Sound 1812 - N Scale Atlas Short Board Dropin", r.getDecoderModel(), "model ");
        Assertions.assertEquals("Brilliance Sound Decoders", r.getDecoderFamily(), "family ");
        Assertions.assertEquals("", r.getProgrammingModes(), "programmingModes ");
    }

    @Test
    public void testFromExistingEntry() throws JDOMException, IOException {

        RosterEntry re = new RosterEntry(RosterEntry.fromFile(
            new File("java/test/jmri/jmrit/roster/ACL1012-Schema.xml")),"New pID");
        Assertions.assertNotNull(re);
        Assertions.assertEquals( "New pID", re.getId());
        Assertions.assertNull( re.getFileName(), "no file name");
        Assertions.assertEquals( "1012", re.getDccAddress(), "DCC Address");
        Assertions.assertEquals( "Atlantic Coast Line", re.getRoadName(), "road name");
        Assertions.assertEquals( "1012", re.getRoadNumber(), "road number");
        Assertions.assertEquals( "Synch Diesel Sound 1812 - N Scale Atlas Short Board Dropin", re.getDecoderModel());
        Assertions.assertEquals( "Brilliance Sound Decoders", re.getDecoderFamily(), "family");
    }

    @Test
    public void testStoreFunctionLabel() {
        RosterEntry r = new RosterEntry("file here");

        r.setFunctionLabel(3, "tree");
        Assertions.assertEquals("tree", r.getFunctionLabel(3));
        Assertions.assertNull(r.getFunctionLabel(4));
    }

    @Test
    public void testModifyDateUnparseable() {
        RosterEntry r = new RosterEntry("file here");

        r.setId("test Id");
        r.setDateUpdated("unparseable date");

        jmri.util.JUnitAppender.assertWarnMessage("Unable to parse \"unparseable date\" as a date in roster entry \"test Id\".");
    }

    @Test
    public void testDateFormatHistoric() {
        RosterEntry r = new RosterEntry("file here");

        r.setId("test Id");
        TimeZone tz = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("GMT-7"));
            r.setDateUpdated("03-Oct-2015 11:19:12"); // this is in local time
        } finally {
            TimeZone.setDefault(tz);
        }

        Assertions.assertTrue(JUnitAppender.verifyNoBacklog());
        Assertions.assertEquals("2015-10-03T18:19:12.000+00:00", r.getDateUpdated());
    }

    @Test
    public void testDateFormatISO() {
        RosterEntry r = new RosterEntry("file here");

        r.setId("test Id");
        r.setDateUpdated("2018-03-05T02:34:55Z");

        Assertions.assertTrue(JUnitAppender.verifyNoBacklog());
        Assertions.assertEquals("2018-03-05T02:34:55.000+00:00", r.getDateUpdated());
    }

    @Test
    public void testDateFormatPreviousJackson() {
        RosterEntry r = new RosterEntry("file here");

        r.setId("test Id");
        r.setDateUpdated("2018-03-05T02:34:55.000+0000");

        Assertions.assertTrue(JUnitAppender.verifyNoBacklog());
        Assertions.assertEquals("2018-03-05T02:34:55.000+00:00", r.getDateUpdated());

    }

    @Test
    public void testDateFormatTraditional() throws java.text.ParseException {
        RosterEntry r = new RosterEntry("file here");

        r.setId("test Id");

        TimeZone tz = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("GMT-7"));
            r.setDateUpdated("Mar 2, 2016 9:57:04 AM"); // this is in local time
        } finally {
            TimeZone.setDefault(tz);
        }

        Assertions.assertTrue(JUnitAppender.verifyNoBacklog());

        // convert that same local time in ISO format and compare
        Assertions.assertEquals("2016-03-02T16:57:04.000+00:00", r.getDateUpdated());
    }

    @Test
    public void testStoreFunctionLockable() {
        RosterEntry r = new RosterEntry("file here");

        r.setFunctionLabel(3, "tree");
        r.setFunctionLockable(3, true);

        r.setFunctionLabel(4, "fort");
        r.setFunctionLockable(4, false);

        Assertions.assertEquals("tree", r.getFunctionLabel(3));
        Assertions.assertTrue(r.getFunctionLockable(3));
        Assertions.assertEquals("fort", r.getFunctionLabel(4));
        Assertions.assertFalse(r.getFunctionLockable(4));
        Assertions.assertNull(r.getFunctionLabel(5));

    }

    @Test
    public void testSetAndGet() {
        RosterEntry r = new RosterEntry("file here");
        r.setId("New One");
        r.setDccAddress("6");
        r.setRoadName("SNCF");
        r.setRoadNumber("3601");
        r.setMfg("Lima");
        r.setDeveloperID("23");
        r.setManufacturerID("100");
        r.setProgrammingModes("A,B");
        r.setComment("notes");
        r.setDecoderFamily("fam1");
        r.setDecoderModel("model A");

        Assertions.assertEquals("file here", r.getFileName(), "file name ");
        Assertions.assertEquals("New One", r.getId(), "ID ");
        Assertions.assertEquals("6", r.getDccAddress(), "DCC Address ");
        Assertions.assertEquals("SNCF", r.getRoadName(), "road name ");
        Assertions.assertEquals("3601", r.getRoadNumber(), "road number ");
        Assertions.assertEquals("Lima", r.getMfg(), "manufacturer ");
        Assertions.assertEquals("23", r.getDeveloperID(), "developerID ");
        Assertions.assertEquals("100", r.getManufacturerID(), "manufacturer ID ");
        Assertions.assertEquals("A,B", r.getProgrammingModes(), "programming modes ");
        Assertions.assertEquals("notes", r.getComment(), "comment ");
        Assertions.assertEquals("fam1", r.getDecoderFamily(), "family ");
        Assertions.assertEquals("model A", r.getDecoderModel(), "model ");
    }

    @Test
    public void testXmlLoadStore() {
        // create Element
        org.jdom2.Element e = new org.jdom2.Element("locomotive")
                .setAttribute("id", "our id 4")
                .setAttribute("fileName", "file here")
                .setAttribute("roadNumber", "431")
                .setAttribute("roadName", "SP")
                .setAttribute("mfg", "Athearn")
                .setAttribute("dccAddress", "1234")
                .addContent(new org.jdom2.Element("decoder")
                        .setAttribute("family", "91")
                        .setAttribute("model", "33")
                        .setAttribute("comment", "decoder comment")
                ); // end create element

        RosterEntry r = new RosterEntry(e) {
            @Override
            protected void warnShortLong(String s) {
            }
        };

        org.jdom2.Element o = r.store();
        // check
        Assertions.assertEquals(e.toString(), o.toString(), "XML Element ");
        Assertions.assertEquals("91", o.getChild("decoder").getAttribute("family").getValue(), "family ");
        Assertions.assertEquals("33", o.getChild("decoder").getAttribute("model").getValue(), "model ");
        Assertions.assertEquals("decoder comment", o.getChild("decoder").getAttribute("comment").getValue(), "comment");
        Assertions.assertEquals("28", o.getChild("decoder").getAttribute("maxFnNum").getValue(), "default maxFnNum ");
    }

    @Test
    public void testXmlFunctionLabelsLoadStore() {
        // create Element
        org.jdom2.Element e = new org.jdom2.Element("locomotive")
                .setAttribute("id", "our id 4")
                .setAttribute("fileName", "file here")
                .setAttribute("roadNumber", "431")
                .setAttribute("roadName", "SP")
                .setAttribute("mfg", "Athearn")
                .setAttribute("dccAddress", "1234")
                .setAttribute("decoderModes", "BMODE")
                .addContent(new org.jdom2.Element("decoder")
                        .setAttribute("family", "91")
                        .setAttribute("model", "33")
                )
                .addContent(new org.jdom2.Element("functionlabels")
                        .addContent(new org.jdom2.Element("functionlabel")
                                .setAttribute("num", "2")
                                .setAttribute("lockable", "true")
                                .addContent("label 2")
                        )
                        .addContent(new org.jdom2.Element("functionlabel")
                                .setAttribute("num", "3")
                                .setAttribute("lockable", "false")
                                .addContent("label 3")
                        )
                ); // end create element

        RosterEntry r = new RosterEntry(e) {
            @Override
            protected void warnShortLong(String s) {
            }
        };

        // check loaded
        Assertions.assertNull(r.getFunctionLabel(1));
        Assertions.assertEquals("label 2", r.getFunctionLabel(2));
        Assertions.assertTrue(r.getFunctionLockable(2), "lockable 2");
        Assertions.assertEquals("label 3", r.getFunctionLabel(3));
        Assertions.assertFalse(r.getFunctionLockable(3), "lockable 2");
        Assertions.assertNull(r.getFunctionLabel(4));

        org.jdom2.Element o = r.store();

        // check stored element
        Assertions.assertEquals("2", o.getChild("functionlabels").getChild("functionlabel").getAttribute("num").getValue(), "num 2");
        Assertions.assertEquals("true", o.getChild("functionlabels").getChild("functionlabel").getAttribute("lockable").getValue(), "lockable 2");
        Assertions.assertEquals("label 2", o.getChild("functionlabels").getChild("functionlabel").getText(), "label 2");
    }

    @Test
    public void testEnsureFilenameExistsNew() {
        RosterEntry r = new RosterEntry();
        Assertions.assertNull(r.getFileName(), "initial filename ");
        r.setId("test Roster Entry 123456789ABC");
        Assertions.assertEquals("test Roster Entry 123456789ABC", r.getId(), "initial ID ");
        File f = new File(Roster.getDefault().getRosterFilesLocation() + "test_Roster_Entry_123456789ABC.xml");
        if (f.exists()) {
            Assertions.assertTrue(f.delete());
        }
        r.ensureFilenameExists();
        Assertions.assertEquals("test_Roster_Entry_123456789ABC.xml", r.getFileName(), "final filename ");
        if (f.exists()) {
            Assertions.assertTrue(f.delete());  // clean up afterwards
        }
    }

    @Test
    public void testEnsureFilenameExistsOld() throws IOException {
        FileUtil.createDirectory(Roster.getDefault().getRosterFilesLocation());
        RosterEntry r = new RosterEntry();
        Assertions.assertNull(r.getFileName(), "initial filename ");
        r.setId("test Roster Entry 123456789ABC");
        Assertions.assertEquals("test Roster Entry 123456789ABC", r.getId(), "initial ID ");
        File f1 = new File(Roster.getDefault().getRosterFilesLocation() + "test_Roster_Entry_123456789ABC.xml");
        if (!f1.exists()) {
            try ( FileOutputStream f = new FileOutputStream(f1)) { // create a dummy
                f.write(0);
            }
        }
        File f2 = new File(Roster.getDefault().getRosterFilesLocation() + "test_Roster_Entry_123456789ABC0.xml");
        if (!f2.exists()) {
            try ( FileOutputStream f = new FileOutputStream(f2)) { // create a dummy
                f.write(0);
            }
        }
        r.ensureFilenameExists();
        Assertions.assertEquals("test_Roster_Entry_123456789ABC1.xml", r.getFileName(), "final filename ");
        if (f1.exists()) {
            Assertions.assertTrue(f1.delete());  // clean up afterwards
        }
        if (f2.exists()) {
            Assertions.assertTrue(f2.delete());
        }
    }

    @Test
    public void testNoAttribute() {
        RosterEntry r = new RosterEntry();
        Assertions.assertNull(r.getAttribute("foo"));
    }

    @Test
    public void testOneAttribute() {
        RosterEntry r = new RosterEntry();
        r.putAttribute("foo", "bar");
        Assertions.assertEquals("bar", r.getAttribute("foo"));
    }

    @Test
    public void testReplaceAttribute() {
        RosterEntry r = new RosterEntry();
        r.putAttribute("foo", "bar");
        r.putAttribute("foo", "a nicer bar");
        Assertions.assertEquals("a nicer bar", r.getAttribute("foo"));
    }

    @Test
    public void testNullAttributeValue() {
        RosterEntry r = new RosterEntry();
        r.putAttribute("foo", "bar");
        r.putAttribute("foo", null);
        Assertions.assertNull(r.getAttribute("foo"));
    }

    @Test
    public void testAttributeList() {
        RosterEntry r = new RosterEntry();
        r.putAttribute("key 2", "value 2");
        r.putAttribute("key 3", "value 3");
        r.putAttribute("key 1", "value 1");
        java.util.Set<String> l = r.getAttributes();
        Assertions.assertEquals(3, l.size(), "number returned");
        java.util.Iterator<String> i = l.iterator();
        Assertions.assertEquals("key 1", i.next(), "1st item");
        Assertions.assertEquals("key 2", i.next(), "2nd item");
        Assertions.assertEquals("key 3", i.next(), "3rd item");
        Assertions.assertFalse(i.hasNext());
    }

    @Test
    public void testXmlAttributesLoadStore() {
        // create Element
        org.jdom2.Element e = new org.jdom2.Element("locomotive")
                .setAttribute("id", "our id 4")
                .setAttribute("fileName", "file here")
                .setAttribute("roadNumber", "431")
                .setAttribute("roadName", "SP")
                .setAttribute("mfg", "Athearn")
                .setAttribute("dccAddress", "1234")
                .addContent(new org.jdom2.Element("decoder")
                        .setAttribute("family", "91")
                        .setAttribute("model", "33")
                )
                .addContent(new org.jdom2.Element("attributepairs")
                        .addContent(new org.jdom2.Element("keyvaluepair")
                                .addContent(new org.jdom2.Element("key")
                                        .addContent("key 1")
                                )
                                .addContent(new org.jdom2.Element("value")
                                        .addContent("value 1")
                                )
                        )
                        .addContent(new org.jdom2.Element("keyvaluepair")
                                .addContent(new org.jdom2.Element("key")
                                        .addContent("key 2")
                                )
                                .addContent(new org.jdom2.Element("value")
                                        .addContent("value 2")
                                )
                        )
                ); // end create element

        RosterEntry r = new RosterEntry(e) {
            @Override
            protected void warnShortLong(String s) {
            }
        };

        Assertions.assertEquals("value 1", r.getAttribute("key 1"));
        Assertions.assertEquals("value 2", r.getAttribute("key 2"));
        Assertions.assertNull(r.getAttribute("key 4"));
    }

    @Test
    public void testStoreAttribute() {
        RosterEntry r = new RosterEntry("dummy filename");
        r.putAttribute("foo", "bar");

        org.jdom2.Element e = r.store();
        Assertions.assertNotNull(e);
        Assertions.assertNotNull(e.getChild("attributepairs"));
        Assertions.assertNotNull(e.getChild("attributepairs")
                .getChild("keyvaluepair"));
        Assertions.assertNotNull(e.getChild("attributepairs")
                .getChild("keyvaluepair")
                .getChild("key"));
        Assertions.assertNotNull(e.getChild("attributepairs")
                .getChild("keyvaluepair")
                .getChild("value"));
        Assertions.assertEquals("foo", e.getChild("attributepairs")
                .getChild("keyvaluepair")
                .getChild("key").getText());
        Assertions.assertNotNull(e.getChild("attributepairs")
                .getChild("keyvaluepair")
                .getChild("value").getText(), "bar");
    }

    @Test
    public void testToString() {
        Assertions.assertEquals("[RosterEntry: id 3 file here SP 431 Athearn   14  35 91     ]",
            RosterEntryImplementations.id3().toString());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        InstanceManager.setDefault(RosterConfigManager.class, new RosterConfigManager());
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
