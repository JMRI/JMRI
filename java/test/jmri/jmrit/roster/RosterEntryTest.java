package jmri.jmrit.roster;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import jmri.InstanceManager;
import jmri.util.*;

import org.jdom2.JDOMException;
import org.junit.*;

/**
 * Tests for the jmrit.roster.RosterEntry class.
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2002, 2018
 */
public class RosterEntryTest {

    @Test
    public void testCreate() {
        RosterEntry r = new RosterEntry("file here");
        Assert.assertEquals("file name ", "file here", r.getFileName());
        Assert.assertEquals("DCC Address ", "3", r.getDccAddress());
        Assert.assertEquals("road name ", "", r.getRoadName());
        Assert.assertEquals("road number ", "", r.getRoadNumber());
        Assert.assertEquals("manufacturer ", "", r.getMfg());
        Assert.assertEquals("model ", "", r.getDecoderModel());
        Assert.assertEquals("family ", "", r.getDecoderFamily());
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
        Assert.assertEquals("file name ", "file here", r.getFileName());
        Assert.assertEquals("DCC Address ", "1234", r.getDccAddress());
        Assert.assertEquals("road name ", "SP", r.getRoadName());
        Assert.assertEquals("road number ", "431", r.getRoadNumber());
        Assert.assertEquals("manufacturer ", "Athearn", r.getMfg());
        Assert.assertEquals("model ", "", r.getDecoderModel());
        Assert.assertEquals("family ", "", r.getDecoderFamily());
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
        Assert.assertEquals("file name ", "file here", r.getFileName());
        Assert.assertEquals("DCC Address ", "3", r.getDccAddress());
        Assert.assertEquals("road name ", "", r.getRoadName());
        Assert.assertEquals("road number ", "", r.getRoadNumber());
        Assert.assertEquals("manufacturer ", "", r.getMfg());
        Assert.assertEquals("model ", "", r.getDecoderModel());
        Assert.assertEquals("family ", "", r.getDecoderFamily());
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
        Assert.assertEquals("file name ", "file here", r.getFileName());
        Assert.assertEquals("DCC Address ", "1234", r.getDccAddress());
        Assert.assertEquals("road name ", "SP", r.getRoadName());
        Assert.assertEquals("road number ", "431", r.getRoadNumber());
        Assert.assertEquals("manufacturer ", "Athearn", r.getMfg());
        Assert.assertEquals("model ", "33", r.getDecoderModel());
        Assert.assertEquals("family ", "91", r.getDecoderFamily());
    }

    @Test
    public void testFromSchemaFile() throws JDOMException, IOException {
        
        // Create a RosterEntry from a test xml file
        // This one references the Schema version
        RosterEntry r = RosterEntry.fromFile(new File("java/test/jmri/jmrit/roster/ACL1012-Schema.xml"));

        // check for various values
        Assert.assertEquals("file name ", "ACL1012-Schema.xml", r.getFileName());
        Assert.assertEquals("DCC Address ", "1012", r.getDccAddress());
        Assert.assertEquals("road name ", "Atlantic Coast Line", r.getRoadName());
        Assert.assertEquals("road number ", "1012", r.getRoadNumber());
        Assert.assertEquals("model ", "Synch Diesel Sound 1812 - N Scale Atlas Short Board Dropin", r.getDecoderModel());
        Assert.assertEquals("family ", "Brilliance Sound Decoders", r.getDecoderFamily());
    }

    @Test
    public void testFromDtdFile() throws JDOMException, IOException {
        
        // Create a RosterEntry from a test xml file
        // This one references the DTD to make sure that still works
        // post migration
        RosterEntry r = RosterEntry.fromFile(new File("java/test/jmri/jmrit/roster/ACL1012-DTD.xml"));

        // check for various values
        Assert.assertEquals("file name ", "ACL1012-DTD.xml", r.getFileName());
        Assert.assertEquals("DCC Address ", "1012", r.getDccAddress());
        Assert.assertEquals("road name ", "Atlantic Coast Line", r.getRoadName());
        Assert.assertEquals("road number ", "1012", r.getRoadNumber());
        Assert.assertEquals("model ", "Synch Diesel Sound 1812 - N Scale Atlas Short Board Dropin", r.getDecoderModel());
        Assert.assertEquals("family ", "Brilliance Sound Decoders", r.getDecoderFamily());
    }

    @Test
    public void testStoreFunctionLabel() {
        RosterEntry r = new RosterEntry("file here");

        r.setFunctionLabel(3, "tree");
        Assert.assertEquals("tree", r.getFunctionLabel(3));
        Assert.assertEquals(null, r.getFunctionLabel(4));

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
        
        Assert.assertTrue(jmri.util.JUnitAppender.verifyNoBacklog()); 
        Assert.assertEquals("2015-10-03T18:19:12.000+0000", r.getDateUpdated());
    }

    @Test
    public void testDateFormatISO() {
        RosterEntry r = new RosterEntry("file here");

        r.setId("test Id");
        r.setDateUpdated("2018-03-05T02:34:55Z");
        
        Assert.assertTrue(jmri.util.JUnitAppender.verifyNoBacklog()); 
        Assert.assertEquals("2018-03-05T02:34:55.000+0000", r.getDateUpdated());
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
        
        Assert.assertTrue(jmri.util.JUnitAppender.verifyNoBacklog()); 
        
        // convert that same local time in ISO format and compare
        Assert.assertEquals("2016-03-02T16:57:04.000+0000", r.getDateUpdated());
    }

    @Test
    public void testStoreFunctionLockable() {
        RosterEntry r = new RosterEntry("file here");

        r.setFunctionLabel(3, "tree");
        r.setFunctionLockable(3, true);

        r.setFunctionLabel(4, "fort");
        r.setFunctionLockable(4, false);

        Assert.assertEquals("tree", r.getFunctionLabel(3));
        Assert.assertEquals(true, r.getFunctionLockable(3));
        Assert.assertEquals("fort", r.getFunctionLabel(4));
        Assert.assertEquals(false, r.getFunctionLockable(4));
        Assert.assertEquals(null, r.getFunctionLabel(5));

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
                ); // end create element

        RosterEntry r = new RosterEntry(e) {
            @Override
            protected void warnShortLong(String s) {
            }
        };

        org.jdom2.Element o = r.store();
        // check
        Assert.assertEquals("XML Element ", e.toString(), o.toString());
        Assert.assertEquals("family ", "91", o.getChild("decoder").getAttribute("family").getValue());
        Assert.assertEquals("model ", "33", o.getChild("decoder").getAttribute("model").getValue());
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
        Assert.assertEquals(null, r.getFunctionLabel(1));
        Assert.assertEquals("label 2", r.getFunctionLabel(2));
        Assert.assertEquals("lockable 2", true, r.getFunctionLockable(2));
        Assert.assertEquals("label 3", r.getFunctionLabel(3));
        Assert.assertEquals("lockable 2", false, r.getFunctionLockable(3));
        Assert.assertEquals(null, r.getFunctionLabel(4));

        org.jdom2.Element o = r.store();

        // check stored element
        Assert.assertEquals("num 2", "2", o.getChild("functionlabels").getChild("functionlabel").getAttribute("num").getValue());
        Assert.assertEquals("lockable 2", "true", o.getChild("functionlabels").getChild("functionlabel").getAttribute("lockable").getValue());
        Assert.assertEquals("label 2", "label 2", o.getChild("functionlabels").getChild("functionlabel").getText());
    }

    @Test
    public void testEnsureFilenameExistsNew() {
        RosterEntry r = new RosterEntry();
        Assert.assertEquals("initial filename ", null, r.getFileName());
        r.setId("test Roster Entry 123456789ABC");
        Assert.assertEquals("initial ID ", "test Roster Entry 123456789ABC", r.getId());
        File f = new File(Roster.getDefault().getRosterFilesLocation() + "test_Roster_Entry_123456789ABC.xml");
        if (f.exists()) {
            f.delete();
        }
        r.ensureFilenameExists();
        Assert.assertEquals("final filename ", "test_Roster_Entry_123456789ABC.xml", r.getFileName());
        if (f.exists()) {
            f.delete();  // clean up afterwards
        }
    }

    @Test
    public void testEnsureFilenameExistsOld() throws IOException {
        FileUtil.createDirectory(Roster.getDefault().getRosterFilesLocation());
        RosterEntry r = new RosterEntry();
        Assert.assertEquals("initial filename ", null, r.getFileName());
        r.setId("test Roster Entry 123456789ABC");
        Assert.assertEquals("initial ID ", "test Roster Entry 123456789ABC", r.getId());
        File f1 = new File(Roster.getDefault().getRosterFilesLocation() + "test_Roster_Entry_123456789ABC.xml");
        if (!f1.exists()) {
            // create a dummy
            FileOutputStream f = new FileOutputStream(f1);
            f.write(0);
            f.close();
        }
        File f2 = new File(Roster.getDefault().getRosterFilesLocation() + "test_Roster_Entry_123456789ABC0.xml");
        if (!f2.exists()) {
            // create a dummy
            FileOutputStream f = new FileOutputStream(f2);
            f.write(0);
            f.close();
        }
        r.ensureFilenameExists();
        Assert.assertEquals("final filename ", "test_Roster_Entry_123456789ABC1.xml", r.getFileName());
        if (f1.exists()) {
            f1.delete();  // clean up afterwards
        }
        if (f2.exists()) {
            f2.delete();
        }
    }

    @Test
    public void testNoAttribute() {
        RosterEntry r = new RosterEntry();
        Assert.assertNull(r.getAttribute("foo"));
    }

    @Test
    public void testOneAttribute() {
        RosterEntry r = new RosterEntry();
        r.putAttribute("foo", "bar");
        Assert.assertEquals("bar", r.getAttribute("foo"));
    }

    @Test
    public void testReplaceAttribute() {
        RosterEntry r = new RosterEntry();
        r.putAttribute("foo", "bar");
        r.putAttribute("foo", "a nicer bar");
        Assert.assertEquals("a nicer bar", r.getAttribute("foo"));
    }

    @Test
    public void testNullAttributeValue() {
        RosterEntry r = new RosterEntry();
        r.putAttribute("foo", "bar");
        r.putAttribute("foo", null);
        Assert.assertNull(r.getAttribute("foo"));
    }

    @Test
    public void testAttributeList() {
        RosterEntry r = new RosterEntry();
        r.putAttribute("key 2", "value 2");
        r.putAttribute("key 3", "value 3");
        r.putAttribute("key 1", "value 1");
        java.util.Set<String> l = r.getAttributes();
        Assert.assertEquals("number returned", 3, l.size());
        java.util.Iterator<String> i = l.iterator();
        Assert.assertEquals("1st item", "key 1", i.next());
        Assert.assertEquals("2nd item", "key 2", i.next());
        Assert.assertEquals("3rd item", "key 3", i.next());
        Assert.assertTrue(!i.hasNext());
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

        Assert.assertEquals("value 1", r.getAttribute("key 1"));
        Assert.assertEquals("value 2", r.getAttribute("key 2"));
        Assert.assertEquals(null, r.getAttribute("key 4"));
    }

    @Test
    public void testStoreAttribute() {
        RosterEntry r = new RosterEntry("dummy filename");
        r.putAttribute("foo", "bar");

        org.jdom2.Element e = r.store();
        Assert.assertNotNull(e);
        Assert.assertNotNull(e.getChild("attributepairs"));
        Assert.assertNotNull(e.getChild("attributepairs")
                .getChild("keyvaluepair"));
        Assert.assertNotNull(e.getChild("attributepairs")
                .getChild("keyvaluepair")
                .getChild("key"));
        Assert.assertNotNull(e.getChild("attributepairs")
                .getChild("keyvaluepair")
                .getChild("value"));
        Assert.assertEquals("foo", e.getChild("attributepairs")
                .getChild("keyvaluepair")
                .getChild("key").getText());
        Assert.assertNotNull("bar", e.getChild("attributepairs")
                .getChild("keyvaluepair")
                .getChild("value").getText());
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        InstanceManager.setDefault(RosterConfigManager.class, new RosterConfigManager());
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
