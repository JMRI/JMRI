package jmri.jmrit.roster;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import jmri.InstanceManager;
import jmri.util.FileUtil;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmrit.roster.RosterEntry class.
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2002
 * @version	$Revision$
 */
public class RosterEntryTest extends TestCase {

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

    public void testEmptyLoad() {
        // create Element
        org.jdom2.Element e = new org.jdom2.Element("locomotive")
                .setAttribute("id", "our id 2")
                .setAttribute("fileName", "file here"); // end create element

        RosterEntry r = new RosterEntry(e) {
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

    public void testStoreFunctionLabel() {
        RosterEntry r = new RosterEntry("file here");

        r.setFunctionLabel(3, "tree");
        Assert.assertEquals("tree", r.getFunctionLabel(3));
        Assert.assertEquals(null, r.getFunctionLabel(4));

    }

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
            protected void warnShortLong(String s) {
            }
        };

        org.jdom2.Element o = r.store();
        // check
        Assert.assertEquals("XML Element ", e.toString(), o.toString());
        Assert.assertEquals("family ", "91", o.getChild("decoder").getAttribute("family").getValue());
        Assert.assertEquals("model ", "33", o.getChild("decoder").getAttribute("model").getValue());
    }

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

    public void testEnsureFilenameExistsNew() {
        RosterEntry r = new RosterEntry();
        Assert.assertEquals("initial filename ", null, r.getFileName());
        r.setId("test Roster Entry 123456789ABC");
        Assert.assertEquals("initial ID ", "test Roster Entry 123456789ABC", r.getId());
        File f = new File(LocoFile.getFileLocation() + "test_Roster_Entry_123456789ABC.xml");
        if (f.exists()) {
            f.delete();
        }
        r.ensureFilenameExists();
        Assert.assertEquals("final filename ", "test_Roster_Entry_123456789ABC.xml", r.getFileName());
        if (f.exists()) {
            f.delete();  // clean up afterwards
        }
    }

    public void testEnsureFilenameExistsOld() throws IOException {
        FileUtil.createDirectory(LocoFile.getFileLocation());
        RosterEntry r = new RosterEntry();
        Assert.assertEquals("initial filename ", null, r.getFileName());
        r.setId("test Roster Entry 123456789ABC");
        Assert.assertEquals("initial ID ", "test Roster Entry 123456789ABC", r.getId());
        File f1 = new File(LocoFile.getFileLocation() + "test_Roster_Entry_123456789ABC.xml");
        if (!f1.exists()) {
            // create a dummy
            FileOutputStream f = new FileOutputStream(f1);
            f.write(0);
            f.close();
        }
        File f2 = new File(LocoFile.getFileLocation() + "test_Roster_Entry_123456789ABC0.xml");
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

    public void testNoAttribute() {
        RosterEntry r = new RosterEntry();
        Assert.assertNull(r.getAttribute("foo"));
    }

    public void testOneAttribute() {
        RosterEntry r = new RosterEntry();
        r.putAttribute("foo", "bar");
        Assert.assertEquals("bar", r.getAttribute("foo"));
    }

    public void testReplaceAttribute() {
        RosterEntry r = new RosterEntry();
        r.putAttribute("foo", "bar");
        r.putAttribute("foo", "a nicer bar");
        Assert.assertEquals("a nicer bar", r.getAttribute("foo"));
    }

    public void testNullAttributeValue() {
        RosterEntry r = new RosterEntry();
        r.putAttribute("foo", "bar");
        r.putAttribute("foo", null);
        Assert.assertNull(r.getAttribute("foo"));
    }

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
            protected void warnShortLong(String s) {
            }
        };

        Assert.assertEquals("value 1", r.getAttribute("key 1"));
        Assert.assertEquals("value 2", r.getAttribute("key 2"));
        Assert.assertEquals(null, r.getAttribute("key 4"));
    }

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

    // from here down is testing infrastructure
    public RosterEntryTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", RosterEntryTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(RosterEntryTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
        InstanceManager.setDefault(RosterConfigManager.class, new RosterConfigManager());
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
