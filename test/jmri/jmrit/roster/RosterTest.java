// RosterTest.java

package jmri.jmrit.roster;

import jmri.jmrit.XmlFile;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmrit.roster package & jmrit.roster.Roster class.
 * @author	Bob Jacobsen     Copyright (C) 2001, 2002
 * @version     $Revision: 1.13 $
 */
public class RosterTest extends TestCase {

    public void testDirty() {
        Roster r = new Roster();
        Assert.assertEquals("new object ", false, r.isDirty());
        r.addEntry(null);
        Assert.assertEquals("after add ", true, r.isDirty());
    }

    public void testAdd() {
        Roster r = new Roster();
        Assert.assertEquals("empty length ", 0, r.numEntries());
        r.addEntry(new RosterEntry("file name Bob"));
        Assert.assertEquals("one item ", 1, r.numEntries());
    }

    public void testAddrSearch() {
        Roster r = new Roster();
        RosterEntry e = new RosterEntry("file name Bob");
        e.setRoadNumber("123");
        r.addEntry(e);
        Assert.assertEquals("search not OK ", false, r.checkEntry(0, null, "321", null, null, null, null, null));
        Assert.assertEquals("search OK ", true, r.checkEntry(0, null, "123", null, null, null, null, null));
    }

    public void testSearchList() {
        Roster r = new Roster();
        RosterEntry e;
        e = new RosterEntry("file name Bob");
        e.setRoadNumber("123");
        e.setRoadName("SP");
        r.addEntry(e);
        e = new RosterEntry("file name Bill");
        e.setRoadNumber("123");
        e.setRoadName("ATSF");
        e.setDecoderModel("81");
        e.setDecoderFamily("33");
        r.addEntry(e);
        e = new RosterEntry("file name Ben");
        e.setRoadNumber("123");
        e.setRoadName("UP");
        r.addEntry(e);
        Assert.assertEquals("search for 0 ", 0, r.matchingList(null, "321", null, null, null, null, null).size());
        Assert.assertEquals("search for 1 ", 1, r.matchingList("UP", null,  null, null, null, null, null).size());
        Assert.assertEquals("search for 3 ", 3, r.matchingList(null, "123", null, null, null, null, null).size());
    }

    public void testBackupFile() throws Exception {
        // this test uses explicit filenames intentionally, to ensure that
        // the resulting files go into the test tree area.

        // create a file in "temp"
        XmlFile.ensurePrefsPresent(XmlFile.prefsDir());
        XmlFile.ensurePrefsPresent(XmlFile.prefsDir()+"temp");
        Roster.setFileLocation("temp");
        File f = new File(XmlFile.prefsDir()+"temp"+File.separator+"roster.xml");
        // remove it if its there
        f.delete();
        // load a new one
        String contents = "stuff"+"           ";
        PrintStream p = new PrintStream (new FileOutputStream(f));
        p.println(contents);

        // now do the backup
        Roster r = new Roster() {
                public String backupFileName(String name)
                { return XmlFile.prefsDir()+File.separator+"temp"+File.separator+"rosterBackupTest"; }
            };
        r.makeBackupFile("temp"+File.separator+"roster.xml");

        // and check
        InputStream in = new FileInputStream(new File(XmlFile.prefsDir()+"temp"+File.separator+"rosterBackupTest"));
        Assert.assertEquals("read 0 ", contents.charAt(0), in.read());
        Assert.assertEquals("read 1 ", contents.charAt(1), in.read());
        Assert.assertEquals("read 2 ", contents.charAt(2), in.read());
        Assert.assertEquals("read 3 ", contents.charAt(3), in.read());
    }

    public void testReadWrite() throws Exception {
        // create a test roster & store in file
        Roster r = createTestRoster();

        // create new roster & read
        Roster t = new Roster();
        t.readFile(Roster.defaultRosterFilename());

        // check contents
        Assert.assertEquals("search for 0 ", 0, t.matchingList(null, "321", null, null, null, null, null).size());
        Assert.assertEquals("search for 1 ", 1, t.matchingList("UP", null,  null, null, null, null, null).size());
        Assert.assertEquals("search for 3 ", 3, t.matchingList(null, "123", null, null, null, null, null).size());
    }

    public static Roster createTestRoster() throws java.io.IOException {
        // this uses explicit filenames intentionally, to ensure that
        // the resulting files go into the test tree area.

        // store files in "temp"
        XmlFile.ensurePrefsPresent(XmlFile.prefsDir());
        XmlFile.ensurePrefsPresent(XmlFile.prefsDir()+"temp");
        Roster.setFileLocation("temp");
        Roster.setRosterFileName("rosterTest.xml");

        File f = new File(XmlFile.prefsDir()+"temp"+File.separator+"rosterTest.xml");
        // remove existing roster if its there
        f.delete();

        // create a roster with known contents
        Roster r = new Roster();
        RosterEntry e;
        e = new RosterEntry("file name Bob");
        e.setId("Bob");
        e.setDccAddress("123");
        e.setRoadNumber("123");
        e.setRoadName("SP");
        e.ensureFilenameExists();
        r.addEntry(e);
        e = new RosterEntry("file name Bill");
        e.setId("Bill");
        e.setDccAddress("456");
        e.setRoadNumber("123");
        e.setRoadName("ATSF");
        e.setDecoderModel("81");
        e.setDecoderFamily("33");
        e.ensureFilenameExists();
        r.addEntry(e);
        e = new RosterEntry("file name Ben");
        e.setId("Ben");
        e.setRoadNumber("123");
        e.setRoadName("UP");
        e.ensureFilenameExists();
        r.addEntry(e);

        // write it
        r.writeFile(Roster.defaultRosterFilename());

        return r;
    }

    // from here down is testing infrastructure

    public RosterTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {Roster.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(RosterTest.class);
        suite.addTest(jmri.jmrit.roster.CopyRosterItemActionTest.suite());
        suite.addTest(jmri.jmrit.roster.RosterEntryPaneTest.suite());
        suite.addTest(jmri.jmrit.roster.IdentifyLocoTest.suite());
        suite.addTest(jmri.jmrit.roster.RosterEntryTest.suite());
        return suite;
    }

    // The minimal setup for log4J
    apps.tests.Log4JFixture log4jfixtureInst = new apps.tests.Log4JFixture(this);
    protected void setUp() { log4jfixtureInst.setUp(); }
    protected void tearDown() { log4jfixtureInst.tearDown(); }

}
