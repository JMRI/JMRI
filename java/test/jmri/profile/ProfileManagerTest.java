package jmri.profile;

import java.io.File;
import java.io.IOException;

import jmri.util.FileUtil;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.Assert;
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
        ProfileManager pm = new ProfileManager();
        Assert.assertNotNull("exists", pm);
    }

    @Test
    public void testSetActiveProfile_Profile(@TempDir File folder) throws IOException {
        ProfileManager pm = new ProfileManager();
        // null profile
        pm.setActiveProfile((Profile) null);
        Assert.assertNull(pm.getActiveProfile());
        // non-null profile
        File profileFolder = new File(folder, "test");
        NullProfile p = new NullProfile("test", "test", profileFolder);
        Assert.assertNotNull(p);
        pm.setActiveProfile(p);
        Assert.assertEquals(p, pm.getActiveProfile());
    }

    @Test
    public void testSetActiveProfile_String(@TempDir File folder) throws IOException {
        ProfileManager pm = new ProfileManager();
        // null profile
        pm.setActiveProfile((String) null);
        Assert.assertNull(pm.getActiveProfile());
        // non-existant profile
        pm.setActiveProfile("NonExistantId");
        Assert.assertNull(pm.getActiveProfile());
        JUnitAppender.assertWarnMessage("Unable to set active profile.  No profile with id NonExistantId could be found.");
        // existant non-profile directory (real directory, but no profile)
        File newFolder = new File(folder, "non-profile");
        newFolder.mkdir();
        String folderName = newFolder.getAbsolutePath();
        pm.setActiveProfile(folderName);
        Assert.assertNull(pm.getActiveProfile());
        JUnitAppender.assertErrorMessage(folderName + " is not a profile folder.");
        JUnitAppender.assertWarnMessage("Unable to set active profile.  No profile with id " + folderName + " could be found.");
        // existant profile directory
        newFolder = new File(folder, "profile");
        newFolder.mkdir();
        folderName = newFolder.getAbsolutePath();
        File profileFolder = new File(folderName);
        FileUtil.copy(new File("java/test/jmri/profile/samples/ln-simulator"), profileFolder); // where is existing profile?
        pm.setActiveProfile(folderName);
        Profile p = new Profile(profileFolder);
        Assert.assertNotNull(pm.getActiveProfile());
        Assert.assertEquals(p, pm.getActiveProfile());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ProfileManagerTest.class);
}
