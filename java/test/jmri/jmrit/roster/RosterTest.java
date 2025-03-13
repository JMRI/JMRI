package jmri.jmrit.roster;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Set;

import javax.swing.JComboBox;

import jmri.InstanceManager;
import jmri.jmrit.roster.swing.RosterEntryComboBox;
import jmri.util.FileUtil;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for the jmrit.roster.Roster class.
 * <p>
 * This separates tests of the DefaultRoster functionality from tests of Roster
 * objects individually. Roster itself doesn't (yet) do a good job of
 * separating those, so this is somewhat arbitrary.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002, 2012, 2025
 */
public class RosterTest {

    @Test
    public void testDirty() {
        Roster r = new Roster();
        Assertions.assertFalse(r.isDirty(), "new object ");
        r.addEntry(new RosterEntry());
        Assertions.assertTrue(r.isDirty(), "after add ");
    }

    @Test
    public void testAdd() {
        Roster r = new Roster();
        Assertions.assertEquals(0, r.numEntries(), "empty length ");
        r.addEntry(new RosterEntry("file name Bob"));
        Assertions.assertEquals(1, r.numEntries(), "one item ");
    }

    @Test
    public void testDontAddNullEntriesLater() {
        // test as documentation...
        Roster r = new Roster();
        r.addEntry(new RosterEntry());
        r.addEntry(new RosterEntry());

        Exception ex = Assertions.assertThrows(NullPointerException.class, () -> {
            r.addEntry(null); } );
        Assertions.assertNotNull(ex,"Adding null entry should have caused NPE");
    }

    @Test
    public void testDontAddNullEntriesFirst() {
        // test as documentation...
        Roster r = new Roster();

        Exception ex = Assertions.assertThrows(NullPointerException.class, () -> {
            r.addEntry(null); } );
        Assertions.assertNotNull(ex,"Adding null entry should have caused NPE");
    }

    @Test
    public void testAddrSearch() {
        Roster r = new Roster();
        RosterEntry e = new RosterEntry("file name Bob");
        e.setRoadNumber("123");
        r.addEntry(e);
        Assertions.assertFalse(r.checkEntry(0, null, "321", null, null, null, null, null, null), "search not OK ");
        Assertions.assertTrue(r.checkEntry(0, null, "123", null, null, null, null, null, null), "search OK ");
    }

    @Test
    public void testGetByDccAddress() {
        Roster r = new Roster();
        RosterEntry e = new RosterEntry("file name Bob");
        e.setDccAddress("456");
        r.addEntry(e);
        Assertions.assertFalse(r.checkEntry(0, null, null, "123", null, null, null, null, null), "search not OK ");
        Assertions.assertTrue(r.checkEntry(0, null, null, "456", null, null, null, null, null), "search OK ");

        List<RosterEntry> l;

        l = r.matchingList(null, null, "123", null, null, null, null);
        Assertions.assertEquals(0, l.size(), "match 123");

        l = r.matchingList(null, null, "456", null, null, null, null);
        Assertions.assertEquals(1, l.size(), "match 456");

        l = r.getEntriesByDccAddress("123");
        Assertions.assertEquals(0, l.size(), "address 123");

        l = r.getEntriesByDccAddress("456");
        Assertions.assertEquals(1, l.size(), "address 456");
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
        Assertions.assertEquals(0, l.size(), "search for 0 ");

        l = r.matchingList("UP", null, null, null, null, null, null);
        Assertions.assertEquals(1, l.size(), "search for 1 ");
        Assertions.assertEquals("UP", l.get(0).getRoadName(), "search for 1 ");
        Assertions.assertEquals("123", l.get(0).getRoadNumber(), "search for 1 ");

        l = r.matchingList(null, "123", null, null, null, null, null);
        Assertions.assertEquals(3, l.size(), "search for 3 ");
        Assertions.assertEquals("SP", l.get(2).getRoadName(), "search for 3 ");
        Assertions.assertEquals("123", l.get(2).getRoadNumber(), "search for 3 ");
        Assertions.assertEquals("UP", l.get(0).getRoadName(), "search for 3 ");
        Assertions.assertEquals("123", l.get(0).getRoadNumber(), "search for 3 ");
    }

    @Test
    public void testGetEntriesMatchingList() throws Exception {
        // create a test roster with 3 entries
        Roster r = jmri.util.RosterTestUtil.createTestRoster(new File(Roster.getDefault().getRosterLocation()), "rosterTest.xml");
        Assertions.assertNotNull(r, "exists");

        List<RosterEntry> l;

        // 5 param (LNCV)
        l = r.getEntriesMatchingCriteria( "3", null,null, "123", null);
        Assertions.assertEquals(1, l.size(), "match 6 param");

        // 8 param
        l = r.getEntriesMatchingCriteria("UP", null, null, "100",
                null, null,
                "UP123", null);
        Assertions.assertEquals(0, l.size(), "match 8 param");
        l = r.getEntriesMatchingCriteria("UP", null, null, null,
                null, null,
                "UP123", null);
        Assertions.assertEquals(1, l.size(), "match 8 param");

        // 11 param
        l = r.getEntriesMatchingCriteria(null, "123", null, null,
                null, null, "UP123",
                null, "23", "100", "123");
        Assertions.assertEquals(1, l.size(), "match 11 param - multi");
        l = r.getEntriesMatchingCriteria(null, "123", null, null,
                null, null, null,
                null, null, null, null);
        Assertions.assertEquals(3, l.size(), "match 11 param - only roadNum");
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
        Assertions.assertEquals(1, box.getItemCount(), "search for zero matches");

        box = new RosterEntryComboBox(r, "UP", null, null, null, null, null, null);
        Assertions.assertEquals(2, box.getItemCount(), "search for one match");
        Assertions.assertEquals(e3, box.getItemAt(1), "search for one match");

        box = new RosterEntryComboBox(r, null, "123", null, null, null, null, null);
        Assertions.assertEquals(4, box.getItemCount(), "search for three matches");
        Assertions.assertEquals(e1, box.getItemAt(1), "search for three matches");
        Assertions.assertEquals(e2, box.getItemAt(2), "search for three matches");
        Assertions.assertEquals(e3, box.getItemAt(3), "search for three matches");

    }

    @Test
    public void testBackupFile(@TempDir File folder) throws Exception {
        // this test uses explicit filenames intentionally, to ensure that
        // the resulting files go into the test tree area.

        // create a file in "temp"
        File rosterDir = new File(folder, "roster");
        FileUtil.createDirectory(rosterDir);
        File f = new File(rosterDir, "roster.xml");

        // failure of test infrastructure if it exists already
        Assertions.assertFalse(f.exists(), "test roster.xml should not exist in new folder");

        // load a new one to ensure it exists
        String contents = "stuff" + "           ";
        PrintStream p = new PrintStream(new FileOutputStream(f));
        p.println(contents);
        p.close();

        File bf = new File(rosterDir, "rosterBackupTest");
        // failure of test infrastructure if backup exists already
        Assertions.assertFalse(bf.exists(), "test backup file should not exist in new folder");

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
        Assertions.assertEquals(contents.charAt(0), in.read(), "read 0 ");
        Assertions.assertEquals(contents.charAt(1), in.read(), "read 1 ");
        Assertions.assertEquals(contents.charAt(2), in.read(), "read 2 ");
        Assertions.assertEquals(contents.charAt(3), in.read(), "read 3 ");
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
        Assertions.assertEquals(contents.charAt(0), in.read(), "read 4 ");
        Assertions.assertEquals(contents.charAt(1), in.read(), "read 5 ");
        Assertions.assertEquals(contents.charAt(2), in.read(), "read 6 ");
        Assertions.assertEquals(contents.charAt(3), in.read(), "read 7 ");
        in.close();
    }

    @Test
    public void testReadWrite() throws Exception {
        // create a test roster & store in file
        Roster r = jmri.util.RosterTestUtil.createTestRoster(new File(Roster.getDefault().getRosterLocation()), "rosterTest.xml");
        Assertions.assertNotNull(r, "exists");
        // write it
        r.writeFile(r.getRosterIndexPath());
        // create new roster & read
        Roster t = new Roster();
        t.readFile(r.getRosterIndexPath());

        // check contents
        Assertions.assertEquals(0, t.matchingList(null, "321", null, null, null, null, null).size(), "search for 0 ");
        Assertions.assertEquals(1, t.matchingList("UP", null, null, null, null, null, null).size(), "search for 1 ");
        Assertions.assertEquals(3, t.matchingList(null, "123", null, null, null, null, null).size(), "search for 3 ");
    }

    @Test
    public void testAttributeAccess() throws Exception {
        // create a test roster & store in file
        Roster r = jmri.util.RosterTestUtil.createTestRoster(new File(Roster.getDefault().getRosterLocation()), "rosterTest.xml");
        Assertions.assertNotNull(r, "exists");

        List<RosterEntry> l;

        l = r.getEntriesWithAttributeKey("key a");
        Assertions.assertEquals(2, l.size(), "match key a");
        l = r.getEntriesWithAttributeKey("no match");
        Assertions.assertEquals(0, l.size(), "no match");

    }

    @Test
    public void testAttributeValueAccess() throws Exception {
        // create a test roster & store in file
        Roster r = jmri.util.RosterTestUtil.createTestRoster(new File(Roster.getDefault().getRosterLocation()), "rosterTest.xml");
        Assertions.assertNotNull(r, "exists");

        List<RosterEntry> l;

        l = r.getEntriesWithAttributeKeyValue("key a", "value a");
        Assertions.assertEquals(2, l.size(), "match key a");
        l = r.getEntriesWithAttributeKeyValue("key a", "none");
        Assertions.assertEquals(0, l.size(), "no match key a");
        l = r.getEntriesWithAttributeKeyValue("no match", "none");
        Assertions.assertEquals(0, l.size(), "no match");

    }

    @Test
    public void testAttributeList() throws Exception {
        // create a test roster & store in file
        Roster r = jmri.util.RosterTestUtil.createTestRoster(new File(Roster.getDefault().getRosterLocation()), "rosterTest.xml");
        Assertions.assertNotNull(r, "exists");

        Set<String> s;

        s = r.getAllAttributeKeys();

        Assertions.assertTrue(s.contains("key b"), "contains right key");
        Assertions.assertFalse(s.contains("no key"), "not contains wrong key");
        Assertions.assertEquals(2, s.size(), "length");

    }

    @Test
    public void testDefaultLocation() {
        Assertions.assertNotNull(Roster.getDefault(), "creates a default");
        Assertions.assertEquals(Roster.getDefault(), Roster.getDefault(), "always same");
        // Default roster not stored in InstanceManager
        Assertions.assertNull(InstanceManager.getNullableDefault(Roster.class), "registered a default");
    }

    @Test
    public void testProfileOnePointForward() {
        RosterEntry r = new RosterEntry();
        RosterSpeedProfile rp = new RosterSpeedProfile(r);
        rp.setSpeed(1000, 500, 5000);
        Assertions.assertEquals(500.0, rp.getForwardSpeed(1.0f), 0.0);
        Assertions.assertEquals(375.0, rp.getForwardSpeed(0.75f), 0.0);
        Assertions.assertEquals(250.0, rp.getForwardSpeed(0.5f), 0.0);
        Assertions.assertEquals(125.0, rp.getForwardSpeed(0.25f), 0.0);
        Assertions.assertEquals(4.0, rp.getForwardSpeed(0.0078125f), 0.0); //routine will use 8 (round( value * 1000))
    }

    @Test
    public void testProfileTwoPointForward() {
        RosterEntry r = new RosterEntry();
        RosterSpeedProfile rp = new RosterSpeedProfile(r);
        rp.setSpeed(1000, 500, 5000);
        rp.setSpeed(500, 250, 2500);
        Assertions.assertEquals(500.0, rp.getForwardSpeed(1.0f), 0.0);
        Assertions.assertEquals(375.0, rp.getForwardSpeed(0.75f), 0.0);
        Assertions.assertEquals(250.0, rp.getForwardSpeed(0.5f), 0.0);
        Assertions.assertEquals(125.0, rp.getForwardSpeed(0.25f), 0.0);
        Assertions.assertEquals(4.0, rp.getForwardSpeed(0.0078125f), 0.0); //routine will use 8 (round( value * 1000))
    }

    @Test
    public void testProfileOnePointReverse() {
        RosterEntry r = new RosterEntry();
        RosterSpeedProfile rp = new RosterSpeedProfile(r);
        rp.setSpeed(1000, 500, 5000);
        Assertions.assertEquals(5000.0, rp.getReverseSpeed(1.0f), 0.0);
        Assertions.assertEquals(3750.0, rp.getReverseSpeed(0.75f), 0.0);
        Assertions.assertEquals(2500.0, rp.getReverseSpeed(0.5f), 0.0);
        Assertions.assertEquals(1250.0, rp.getReverseSpeed(0.25f), 0.0);
        Assertions.assertEquals(40.0, rp.getReverseSpeed(0.0078125f), 0.0);   //routine will use 8 (round( value * 1000))
    }

    @Test
    public void testProfileTwoPointReverse() {
        RosterEntry r = new RosterEntry();
        RosterSpeedProfile rp = new RosterSpeedProfile(r);
        rp.setSpeed(1000, 500, 5000);
        rp.setSpeed(500, 250, 2500);
        Assertions.assertEquals(5000.0, rp.getReverseSpeed(1.0f), 0.0);
        Assertions.assertEquals(3750.0, rp.getReverseSpeed(0.75f), 0.0);
        Assertions.assertEquals(2500.0, rp.getReverseSpeed(0.5f), 0.0);
        Assertions.assertEquals(1250.0, rp.getReverseSpeed(0.25f), 0.0);
        Assertions.assertEquals(40.0, rp.getReverseSpeed(0.0078125f), 0.0); //routine will use 8 (round( value * 1000))
    }

    @Test
    public void testProfileTwoPointForwardGetThrottleSetting() {
        RosterEntry r = new RosterEntry();
        RosterSpeedProfile rp = new RosterSpeedProfile(r);
        rp.setSpeed(1000, 500, 5000);
        rp.setSpeed(500, 250, 2500);
        Assertions.assertEquals(1.0, rp.getThrottleSetting(500, true), 0.0);
        Assertions.assertEquals(0.5, rp.getThrottleSetting(250, true), 0.0);
        Assertions.assertEquals(0.25, rp.getThrottleSetting(125, true), 0.0);
    }

    @Test
    public void testProfileTwoPointReverseGetThrottleSetting() {
        RosterEntry r = new RosterEntry();
        RosterSpeedProfile rp = new RosterSpeedProfile(r);
        rp.setSpeed(1000, 500, 5000);
        rp.setSpeed(500, 250, 2500);
        Assertions.assertEquals(1.0, rp.getThrottleSetting(5000, false), 0.0);
        Assertions.assertEquals(0.5, rp.getThrottleSetting(2500, false), 0.0);
        Assertions.assertEquals(0.25, rp.getThrottleSetting(1250, false), 0.0);
    }

    @BeforeEach
    public void setUp(@TempDir File folder) {
        JUnitUtil.setUp();
        try {
            JUnitUtil.resetProfileManager(new jmri.profile.NullProfile(folder));
        } catch (IOException ioe) {
            // failed to reset the profile relative to the temporary folder.
            // use the default reset.
            JUnitUtil.resetProfileManager();
        }
        JUnitUtil.initRosterConfigManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
