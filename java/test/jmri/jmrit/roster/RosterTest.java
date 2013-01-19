// RosterTest.java

package jmri.jmrit.roster;

import jmri.jmrit.XmlFile;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;

import jmri.jmrit.roster.swing.RosterEntryComboBox;
import jmri.util.FileUtil;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmrit.roster.Roster class.
 * @author	Bob Jacobsen     Copyright (C) 2001, 2002, 2012
 * @version     $Revision$
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
        Assert.assertEquals("search not OK ", false, r.checkEntry(0, null, "321", null, null, null, null, null, null));
        Assert.assertEquals("search OK ", true, r.checkEntry(0, null, "123", null, null, null, null, null, null));
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
        
        java.util.List<RosterEntry> l;
        l = r.matchingList(null, "321", null, null, null, null, null);
        Assert.assertEquals("search for 0 ", 0, l.size());

        l = r.matchingList("UP", null, null, null, null, null, null);
        Assert.assertEquals("search for 1 ", 1, l.size());
        Assert.assertEquals("search for 1 ", "UP", l.get(0).getRoadName());
        Assert.assertEquals("search for 1 ", "123", l.get(0).getRoadNumber());
        
        l = r.matchingList(null, "123", null, null, null, null, null);
        Assert.assertEquals("search for 3 ", 3, l.size());
        Assert.assertEquals("search for 3 ", "SP", l.get(2).getRoadName());
        Assert.assertEquals("search for 3 ", "123", l.get(2).getRoadNumber());
        Assert.assertEquals("search for 3 ", "UP", l.get(0).getRoadName());
        Assert.assertEquals("search for 3 ", "123", l.get(0).getRoadNumber());
    }

    public void testComboBox() {
        Roster r = new Roster();
        RosterEntry e1;
        RosterEntry e2;
        RosterEntry e3;
        e1 = new RosterEntry("file name Bob");
        e1.setRoadNumber("123");
        e1.setRoadName("SP");
        e1.setId("entry 1");
        r.addEntry(e1);
        e2 = new RosterEntry("file name Bill");
        e2.setRoadNumber("123");
        e2.setRoadName("ATSF");
        e2.setDecoderModel("81");
        e2.setDecoderFamily("33");
        e2.setId("entry 2");
        r.addEntry(e2);
        e3 = new RosterEntry("file name Ben");
        e3.setRoadNumber("123");
        e3.setRoadName("UP");
        e3.setId("entry 3");
        r.addEntry(e3);
        
        javax.swing.JComboBox box;
        
        // "Select Loco" is the first entry in the RosterEntryComboBox, so an
        // empty comboBox has 1 item, and the first item is not a RosterEntry
        box = new RosterEntryComboBox(r, null, "321", null, null, null, null, null);
        Assert.assertEquals("search for zero matches", 1, box.getItemCount() );

        box = new RosterEntryComboBox(r, "UP", null, null, null, null, null, null);
        Assert.assertEquals("search for one match", 2, box.getItemCount() );
        Assert.assertEquals("search for one match", e3, box.getItemAt(1) );

        box = new RosterEntryComboBox(r, null, "123", null, null, null, null, null);
        Assert.assertEquals("search for three matches", 4, box.getItemCount() );
        Assert.assertEquals("search for three matches", e1, box.getItemAt(1) );
        Assert.assertEquals("search for three matches", e2, box.getItemAt(2) );
        Assert.assertEquals("search for three matches", e3, box.getItemAt(3) );

    }

    public void testBackupFile() throws Exception {
        // this test uses explicit filenames intentionally, to ensure that
        // the resulting files go into the test tree area.

        // create a file in "temp"
        XmlFile.ensurePrefsPresent(FileUtil.getUserFilesPath());
        XmlFile.ensurePrefsPresent(FileUtil.getUserFilesPath()+"temp");
        Roster.setFileLocation("temp");
        File f = new File(FileUtil.getUserFilesPath()+"temp"+File.separator+"roster.xml");
        // remove it if its there
        f.delete();
        // load a new one
        String contents = "stuff"+"           ";
        PrintStream p = new PrintStream (new FileOutputStream(f));
        p.println(contents);
        p.close();
        // delete previous backup file if there's one
        File bf = new File(FileUtil.getUserFilesPath()+"temp"+File.separator+"rosterBackupTest");
        bf.delete();

        // now do the backup
        Roster r = new Roster() {
                public String backupFileName(String name)
                { return FileUtil.getUserFilesPath()+File.separator+"temp"+File.separator+"rosterBackupTest"; }
            };
        r.makeBackupFile("temp"+File.separator+"roster.xml");

        // and check
        InputStream in = new FileInputStream(new File(FileUtil.getUserFilesPath()+"temp"+File.separator+"rosterBackupTest"));
        Assert.assertEquals("read 0 ", contents.charAt(0), in.read());
        Assert.assertEquals("read 1 ", contents.charAt(1), in.read());
        Assert.assertEquals("read 2 ", contents.charAt(2), in.read());
        Assert.assertEquals("read 3 ", contents.charAt(3), in.read());
        in.close();
        
        // now see if backup works when a backup file already exists
        contents = "NEWER JUNK"+"           ";
        p = new PrintStream (new FileOutputStream(f));
        p.println(contents);
        p.close();

        // now do the backup
        r.makeBackupFile("temp"+File.separator+"roster.xml");

        // and check
        in = new FileInputStream(new File(FileUtil.getUserFilesPath()+"temp"+File.separator+"rosterBackupTest"));
        Assert.assertEquals("read 4 ", contents.charAt(0), in.read());
        Assert.assertEquals("read 5 ", contents.charAt(1), in.read());
        Assert.assertEquals("read 6 ", contents.charAt(2), in.read());
        Assert.assertEquals("read 7 ", contents.charAt(3), in.read());
        in.close();
    }

    public void testReadWrite() throws Exception {
        // create a test roster & store in file
        Roster r = createTestRoster();
        Assert.assertNotNull("exists", r );

        // create new roster & read
        Roster t = new Roster();
        t.readFile(Roster.defaultRosterFilename());

        // check contents
        Assert.assertEquals("search for 0 ", 0, t.matchingList(null, "321", null, null, null, null, null).size());
        Assert.assertEquals("search for 1 ", 1, t.matchingList("UP", null,  null, null, null, null, null).size());
        Assert.assertEquals("search for 3 ", 3, t.matchingList(null, "123", null, null, null, null, null).size());
    }

    public void testAttributeAccess() throws Exception {
        // create a test roster & store in file
        Roster r = createTestRoster();
        Assert.assertNotNull("exists", r );

        //
        List<RosterEntry> l;
        
        l = r.getEntriesWithAttributeKey("key a");
        Assert.assertEquals("match key a", 2, l.size());
        l = r.getEntriesWithAttributeKey("no match");
        Assert.assertEquals("no match", 0, l.size());

    }

    public void testAttributeValueAccess() throws Exception {
        // create a test roster & store in file
        Roster r = createTestRoster();
        Assert.assertNotNull("exists", r );

        //
        List<RosterEntry> l;
        
        l = r.getEntriesWithAttributeKeyValue("key a", "value a");
        Assert.assertEquals("match key a", 2, l.size());
        l = r.getEntriesWithAttributeKeyValue("key a", "none");
        Assert.assertEquals("no match key a", 0, l.size());
        l = r.getEntriesWithAttributeKeyValue("no match", "none");
        Assert.assertEquals("no match", 0, l.size());

    }

    public void testAttributeList() throws Exception {
        // create a test roster & store in file
        Roster r = createTestRoster();
        Assert.assertNotNull("exists", r );

        //
        java.util.Set<String> s;
        
        s = r.getAllAttributeKeys();
        
        Assert.assertTrue("contains right key", s.contains("key b"));
        Assert.assertTrue("not contains wrong key", !s.contains("no key"));
        Assert.assertEquals("length", 2, s.size());

    }

    public static Roster createTestRoster() throws java.io.IOException, java.io.FileNotFoundException {
        // this uses explicit filenames intentionally, to ensure that
        // the resulting files go into the test tree area.

        // store files in "temp"
        XmlFile.ensurePrefsPresent(FileUtil.getUserFilesPath());
        XmlFile.ensurePrefsPresent(FileUtil.getUserFilesPath()+"temp");
        Roster.setFileLocation("temp"+File.separator);
        Roster.setRosterFileName("rosterTest.xml");

        File f = new File(FileUtil.getUserFilesPath()+"temp"+File.separator+"rosterTest.xml");
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
        e.putAttribute("key a", "value a");
        e.putAttribute("key b", "value b");
        r.addEntry(e);
        e = new RosterEntry("file name Bill");
        e.setId("Bill");
        e.setDccAddress("456");
        e.setRoadNumber("123");
        e.setRoadName("ATSF");
        e.setDecoderModel("81");
        e.setDecoderFamily("33");
        e.ensureFilenameExists();
        e.putAttribute("key a", "value a");
        r.addEntry(e);
        e = new RosterEntry("file name Ben");
        e.setId("Ben");
        e.setRoadNumber("123");
        e.setRoadName("UP");
        e.ensureFilenameExists();
        e.putAttribute("key b", "value b");
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
        String[] testCaseName = {"-noloading", RosterTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(RosterTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

}
