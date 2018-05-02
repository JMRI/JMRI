package jmri.jmrit.roster;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Set;
import javax.swing.JComboBox;
import jmri.jmrit.roster.swing.RosterEntryComboBox;
import jmri.util.FileUtil;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Tests for the jmrit.roster.Roster class.
 *
 * This separates tests of the DefaultRoster functionality from tests of Roster
 * objects individually. Roster itself doesn't (yet) do go a good job of
 * separating, those, so this is somewhat arbitrary.
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2002, 2012
 */
public class RosterTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testDirty() {
        Roster r = new Roster();
        Assert.assertEquals("new object ", false, r.isDirty());
        r.addEntry(new RosterEntry());
        Assert.assertEquals("after add ", true, r.isDirty());
    }

    @Test
    public void testAdd() {
        Roster r = new Roster();
        Assert.assertEquals("empty length ", 0, r.numEntries());
        r.addEntry(new RosterEntry("file name Bob"));
        Assert.assertEquals("one item ", 1, r.numEntries());
    }

    @Test
    public void testDontAddNullEntriesLater() {
        // test as documentation...
        Roster r = new Roster();
        r.addEntry(new RosterEntry());
        r.addEntry(new RosterEntry());

        boolean pass = false;
        try {
            r.addEntry(null);
        } catch (NullPointerException e) {
            pass = true;
        }
        Assert.assertTrue("Adding null entry should have caused NPE", pass);
    }

    @Test
    public void testDontAddNullEntriesFirst() {
        // test as documentation...
        Roster r = new Roster();

        boolean pass = false;
        try {
            r.addEntry(null);
        } catch (NullPointerException e) {
            pass = true;
        }
        Assert.assertTrue("Adding null entry should have caused NPE", pass);
    }

    @Test
    public void testAddrSearch() {
        Roster r = new Roster();
        RosterEntry e = new RosterEntry("file name Bob");
        e.setRoadNumber("123");
        r.addEntry(e);
        Assert.assertEquals("search not OK ", false, r.checkEntry(0, null, "321", null, null, null, null, null, null));
        Assert.assertEquals("search OK ", true, r.checkEntry(0, null, "123", null, null, null, null, null, null));
    }

    @Test
    public void testGetByDccAddress() {
        Roster r = new Roster();
        RosterEntry e = new RosterEntry("file name Bob");
        e.setDccAddress("456");
        r.addEntry(e);
        Assert.assertEquals("search not OK ", false, r.checkEntry(0, null, null, "123", null, null, null, null, null));
        Assert.assertEquals("search OK ", true, r.checkEntry(0, null, null, "456", null, null, null, null, null));

        List<RosterEntry> l;

        l = r.matchingList(null, null, "123", null, null, null, null);
        Assert.assertEquals("match 123", 0, l.size());

        l = r.matchingList(null, null, "456", null, null, null, null);
        Assert.assertEquals("match 456", 1, l.size());

        l = r.getEntriesByDccAddress("123");
        Assert.assertEquals("address 123", 0, l.size());

        l = r.getEntriesByDccAddress("456");
        Assert.assertEquals("address 456", 1, l.size());
    }

    @Test
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

        List<RosterEntry> l;
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

    @Test
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

        JComboBox<Object> box;

        // "Select Loco" is the first entry in the RosterEntryComboBox, so an
        // empty comboBox has 1 item, and the first item is not a RosterEntry
        box = new RosterEntryComboBox(r, null, "321", null, null, null, null, null);
        Assert.assertEquals("search for zero matches", 1, box.getItemCount());

        box = new RosterEntryComboBox(r, "UP", null, null, null, null, null, null);
        Assert.assertEquals("search for one match", 2, box.getItemCount());
        Assert.assertEquals("search for one match", e3, box.getItemAt(1));

        box = new RosterEntryComboBox(r, null, "123", null, null, null, null, null);
        Assert.assertEquals("search for three matches", 4, box.getItemCount());
        Assert.assertEquals("search for three matches", e1, box.getItemAt(1));
        Assert.assertEquals("search for three matches", e2, box.getItemAt(2));
        Assert.assertEquals("search for three matches", e3, box.getItemAt(3));

    }

    @Test
    public void testBackupFile() throws Exception {
        // this test uses explicit filenames intentionally, to ensure that
        // the resulting files go into the test tree area.

        // create a file in "temp"
        File rosterDir = folder.newFolder();
        folder.newFolder();
        FileUtil.createDirectory(rosterDir);
        File f = new File(rosterDir, "roster.xml");

        // failure of test infrastructure if it exists already
        Assert.assertTrue("test roster.xml should not exist in new folder", !f.exists());

        // load a new one to ensure it exists
        String contents = "stuff" + "           ";
        PrintStream p = new PrintStream(new FileOutputStream(f));
        p.println(contents);
        p.close();

        File bf = new File(rosterDir, "rosterBackupTest");
        // failure of test infrastructure if backup exists already
        Assert.assertTrue("test backup file should not exist in new folder", !bf.exists());

        // now do the backup
        Roster r = new Roster() {
            @Override
            public String backupFileName(String name) {
                return new File(rosterDir, "rosterBackupTest").getAbsolutePath();
            }
        };
        r.makeBackupFile(new File(rosterDir, "roster.xml").getAbsolutePath());

        // and check
        InputStream in = new FileInputStream(new File(rosterDir, "rosterBackupTest"));
        Assert.assertEquals("read 0 ", contents.charAt(0), in.read());
        Assert.assertEquals("read 1 ", contents.charAt(1), in.read());
        Assert.assertEquals("read 2 ", contents.charAt(2), in.read());
        Assert.assertEquals("read 3 ", contents.charAt(3), in.read());
        in.close();

        // now see if backup works when a backup file already exists
        contents = "NEWER JUNK" + "           ";
        p = new PrintStream(new FileOutputStream(f));
        p.println(contents);
        p.close();

        // now do the backup
        r.makeBackupFile(f.getAbsolutePath());

        // and check
        in = new FileInputStream(new File(rosterDir, "rosterBackupTest"));
        Assert.assertEquals("read 4 ", contents.charAt(0), in.read());
        Assert.assertEquals("read 5 ", contents.charAt(1), in.read());
        Assert.assertEquals("read 6 ", contents.charAt(2), in.read());
        Assert.assertEquals("read 7 ", contents.charAt(3), in.read());
        in.close();
    }

    @Test
    public void testReadWrite() throws Exception {
        // create a test roster & store in file
        Roster r = createTestRoster();
        Assert.assertNotNull("exists", r);
        // write it
        r.writeFile(r.getRosterIndexPath());
        // create new roster & read
        Roster t = new Roster();
        t.readFile(r.getRosterIndexPath());

        // check contents
        Assert.assertEquals("search for 0 ", 0, t.matchingList(null, "321", null, null, null, null, null).size());
        Assert.assertEquals("search for 1 ", 1, t.matchingList("UP", null, null, null, null, null, null).size());
        Assert.assertEquals("search for 3 ", 3, t.matchingList(null, "123", null, null, null, null, null).size());
    }

    @Test
    public void testAttributeAccess() throws Exception {
        // create a test roster & store in file
        Roster r = createTestRoster();
        Assert.assertNotNull("exists", r);

        List<RosterEntry> l;

        l = r.getEntriesWithAttributeKey("key a");
        Assert.assertEquals("match key a", 2, l.size());
        l = r.getEntriesWithAttributeKey("no match");
        Assert.assertEquals("no match", 0, l.size());

    }

    @Test
    public void testAttributeValueAccess() throws Exception {
        // create a test roster & store in file
        Roster r = createTestRoster();
        Assert.assertNotNull("exists", r);

        List<RosterEntry> l;

        l = r.getEntriesWithAttributeKeyValue("key a", "value a");
        Assert.assertEquals("match key a", 2, l.size());
        l = r.getEntriesWithAttributeKeyValue("key a", "none");
        Assert.assertEquals("no match key a", 0, l.size());
        l = r.getEntriesWithAttributeKeyValue("no match", "none");
        Assert.assertEquals("no match", 0, l.size());

    }

    @Test
    public void testAttributeList() throws Exception {
        // create a test roster & store in file
        Roster r = createTestRoster();
        Assert.assertNotNull("exists", r);

        Set<String> s;

        s = r.getAllAttributeKeys();

        Assert.assertTrue("contains right key", s.contains("key b"));
        Assert.assertTrue("not contains wrong key", !s.contains("no key"));
        Assert.assertEquals("length", 2, s.size());

    }

    @Test
    public void testDefaultLocation() {
        Assert.assertTrue("creates a default", Roster.getDefault() != null);
        Assert.assertEquals("always same", Roster.getDefault(), Roster.getDefault());

        // since we created it when we referenced it, should be in InstanceManager
        Assert.assertTrue("registered a default", jmri.InstanceManager.getNullableDefault(Roster.class) != null);
    }

    @Test
    public void testProfileOnePointForward() {
        RosterEntry r = new RosterEntry();
        RosterSpeedProfile rp = new RosterSpeedProfile(r);
        rp.setSpeed(1000, 500, 5000);
        Assert.assertEquals(500.0,rp.getForwardSpeed(1.0f),0.0);
        Assert.assertEquals(375.0,rp.getForwardSpeed(0.75f),0.0);
        Assert.assertEquals(250.0,rp.getForwardSpeed(0.5f), 0.0);
        Assert.assertEquals(125.0,rp.getForwardSpeed(0.25f),0.0);
        Assert.assertEquals(4.0,rp.getForwardSpeed(0.0078125f),0.0); //routine will use 8 (round( value * 1000))
    }

    @Test
    public void testProfileTwoPointForward() {
        RosterEntry r = new RosterEntry();
        RosterSpeedProfile rp = new RosterSpeedProfile(r);
        rp.setSpeed(1000, 500, 5000);
        rp.setSpeed(500, 250, 2500);
        Assert.assertEquals(500.0,rp.getForwardSpeed(1.0f),0.0);
        Assert.assertEquals(375.0,rp.getForwardSpeed(0.75f),0.0);
        Assert.assertEquals(250.0,rp.getForwardSpeed(0.5f), 0.0);
        Assert.assertEquals(125.0,rp.getForwardSpeed(0.25f),0.0);
        Assert.assertEquals(4.0,rp.getForwardSpeed(0.0078125f),0.0); //routine will use 8 (round( value * 1000))
    }
    @Test
    public void testProfileOnePointReverse() {
        RosterEntry r = new RosterEntry();
        RosterSpeedProfile rp = new RosterSpeedProfile(r);
        rp.setSpeed(1000, 500, 5000);
        Assert.assertEquals(5000.0,rp.getReverseSpeed(1.0f),0.0);
        Assert.assertEquals(3750.0,rp.getReverseSpeed(0.75f),0.0);
        Assert.assertEquals(2500.0,rp.getReverseSpeed(0.5f), 0.0);
        Assert.assertEquals(1250.0,rp.getReverseSpeed(0.25f),0.0);
        Assert.assertEquals(40.0,rp.getReverseSpeed(0.0078125f),0.0);   //routine will use 8 (round( value * 1000))
    }

    @Test
    public void testProfileTwoPointReverse() {
        RosterEntry r = new RosterEntry();
        RosterSpeedProfile rp = new RosterSpeedProfile(r);
        rp.setSpeed(1000, 500, 5000);
        rp.setSpeed(500, 250, 2500);
        Assert.assertEquals(5000.0,rp.getReverseSpeed(1.0f),0.0);
        Assert.assertEquals(3750.0,rp.getReverseSpeed(0.75f),0.0);
        Assert.assertEquals(2500.0,rp.getReverseSpeed(0.5f), 0.0);
        Assert.assertEquals(1250.0,rp.getReverseSpeed(0.25f),0.0);
        Assert.assertEquals(40.0,rp.getReverseSpeed(0.0078125f),0.0); //routine will use 8 (round( value * 1000))
    }

    @Test
    public void testProfileTwoPointForwardGetThrottleSetting() {
        RosterEntry r = new RosterEntry();
        RosterSpeedProfile rp = new RosterSpeedProfile(r);
        rp.setSpeed(1000, 500, 5000);
        rp.setSpeed(500, 250, 2500);
        Assert.assertEquals(1.0,rp.getThrottleSetting(500,true),0.0);
        Assert.assertEquals(0.5,rp.getThrottleSetting(250,true),0.0);
        Assert.assertEquals(0.25,rp.getThrottleSetting(125,true),0.0);
    }

   @Test
    public void testProfileTwoPointReverseGetThrottleSetting() {
        RosterEntry r = new RosterEntry();
        RosterSpeedProfile rp = new RosterSpeedProfile(r);
        rp.setSpeed(1000, 500, 5000);
        rp.setSpeed(500, 250, 2500);
        Assert.assertEquals(1.0,rp.getThrottleSetting(5000,false),0.0);
        Assert.assertEquals(0.5,rp.getThrottleSetting(2500,false),0.0);
        Assert.assertEquals(0.25,rp.getThrottleSetting(1250,false),0.0);
    }

    public Roster createTestRoster() throws IOException, FileNotFoundException {
        // this uses explicit filenames intentionally, to ensure that
        // the resulting files go into the test tree area.

        // store files in random temp directory
        File rosterDir = folder.newFolder();
        FileUtil.createDirectory(rosterDir);

        File f = new File(rosterDir, "rosterTest.xml");
        // File should never be there is TemporaryFolder working
        if (f.exists()) Assert.fail("rosterTest.xml in "+rosterDir+" already present: "+f);

        // create a roster with known contents
        Roster r = new Roster();
        r.setRosterLocation(rosterDir.getAbsolutePath());
        r.setRosterIndexFileName("rosterTest.xml");

        RosterEntry e1 = new RosterEntry("file name Bob");
        e1.setId("Bob");
        e1.setDccAddress("123");
        e1.setRoadNumber("123");
        e1.setRoadName("SP");
        e1.ensureFilenameExists();
        e1.putAttribute("key a", "value a");
        e1.putAttribute("key b", "value b");
        r.addEntry(e1);
        RosterEntry e2 = new RosterEntry("file name Bill");
        e2.setId("Bill");
        e2.setDccAddress("456");
        e2.setRoadNumber("123");
        e2.setRoadName("ATSF");
        e2.setDecoderModel("81");
        e2.setDecoderFamily("33");
        e2.ensureFilenameExists();
        e2.putAttribute("key a", "value a");
        r.addEntry(e2);
        RosterEntry e3 = new RosterEntry("file name Ben");
        e3.setId("Ben");
        e3.setRoadNumber("123");
        e3.setRoadName("UP");
        e3.ensureFilenameExists();
        e3.putAttribute("key b", "value b");
        r.addEntry(e3);

        // write it
        //r.writeFile(r.getRosterIndexPath());
        return r;
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
