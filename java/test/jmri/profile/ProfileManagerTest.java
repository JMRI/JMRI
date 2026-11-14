package jmri.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import jmri.util.FileUtil;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Randall Wood Copyright (C) 2017
 */
public class ProfileManagerTest {

    @Test
    public void testCTor() {
        ProfileManager pm = ProfileManager.getDefault();
        assertNotNull( pm, "exists");
    }

    @Test
    public void testSetActiveProfile_Profile(@TempDir File folder) throws IOException {
        ProfileManager pm = ProfileManager.getDefault();
        // null profile
        pm.setActiveProfile((Profile) null);
        assertNull(pm.getActiveProfile());
        // non-null profile
        File profileFolder = new File(folder, "test");
        NullProfile p = new NullProfile("test", "test", profileFolder);
        assertNotNull(p);
        pm.setActiveProfile(p);
        assertEquals(p, pm.getActiveProfile());
    }

    @Test
    public void testSetActiveProfile_String(@TempDir File folder) throws IOException {
        ProfileManager pm = ProfileManager.getDefault();
        // null profile
        pm.setActiveProfile((String) null);
        assertNull(pm.getActiveProfile());
        // non-existant profile
        pm.setActiveProfile("NonExistantId");
        assertNull(pm.getActiveProfile());
        JUnitAppender.assertWarnMessage("Unable to set active profile.  No profile with id NonExistantId could be found.");
        // existant non-profile directory (real directory, but no profile)
        File newFolder = new File(folder, "non-profile");
        assertTrue(newFolder.mkdir());
        String folderName = newFolder.getAbsolutePath();
        pm.setActiveProfile(folderName);
        assertNull(pm.getActiveProfile());
        JUnitAppender.assertErrorMessage(folderName + " is not a profile folder.");
        JUnitAppender.assertWarnMessage("Unable to set active profile.  No profile with id " + folderName + " could be found.");
        // existant profile directory
        newFolder = new File(folder, "profile");
        assertTrue(newFolder.mkdir());
        folderName = newFolder.getAbsolutePath();
        File profileFolder = new File(folderName);
        FileUtil.copy(new File("java/test/jmri/profile/samples/ln-simulator"), profileFolder); // where is existing profile?
        pm.setActiveProfile(folderName);
        Profile p = new Profile(profileFolder);
        assertNotNull(pm.getActiveProfile());
        assertEquals(p, pm.getActiveProfile());
    }

    @Test
    public void testCreateUniqueIdNeverRepeats() {
        // A full test run creates thousands of profiles with these ids. Two
        // profiles with the same id are equal, so a repeated id lets a new
        // profile pick up paths cached for an earlier one. The old ids
        // repeated about 8 times in 20,000, so this fails on them nearly
        // every run; 2,000 would catch them less than one run in ten.
        int count = 20000;
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < count; i++) {
            String id = ProfileManager.createUniqueId();
            assertTrue(id.matches("[0-9a-f]{8}"), "eight hex digits: " + id);
            ids.add(id);
        }
        assertEquals(count, ids.size(), "every id is different");
    }

    @Test
    public void testNewProfilesHaveTheirOwnPaths(@TempDir File folder) throws IOException {
        // a check of what a repeated id breaks; with the old ids this usually passed too
        File first = new File(folder, "first");
        File second = new File(folder, "second");
        NullProfile p1 = new NullProfile(first);
        NullProfile p2 = new NullProfile(second);
        assertNotEquals(p1.getId(), p2.getId());
        assertNotEquals(p1, p2);
        assertEquals(first.getAbsolutePath() + File.separator, FileUtil.getProfilePath(p1));
        assertEquals(second.getAbsolutePath() + File.separator, FileUtil.getProfilePath(p2));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private static final Logger log = LoggerFactory.getLogger(ProfileManagerTest.class);
}
