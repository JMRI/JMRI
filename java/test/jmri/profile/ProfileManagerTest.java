package jmri.profile;

import java.io.File;
import java.io.IOException;
import jmri.util.FileUtil;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Randall Wood Copyright (C) 2017
 */
public class ProfileManagerTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testCTor() {
        ProfileManager pm = new ProfileManager();
        Assert.assertNotNull("exists", pm);
    }

    @Test
    @SuppressWarnings("null")
    public void testSetActiveProfile_Profile() throws IOException {
        ProfileManager pm = new ProfileManager();
        // expect this to throw exception because profile is null
        boolean threw = false;
        try {
            // null profile
            pm.setActiveProfile((Profile) null);
        } catch (IllegalArgumentException ex) {
            if (ex.getMessage().equals("profile is null")) {
                threw = true;
            } else {
                Assert.fail("failed to set profile due to wrong reason: " + ex);
            }
        }
        Assert.assertTrue("Expected exception IllegalArgumentException", threw);
        
        // non-null profile
        File profileFolder = new File(folder.newFolder(Profile.PROFILE), "test");
        NullProfile p = new NullProfile("test", "test", profileFolder);
        Assert.assertNotNull(p);
        pm.setActiveProfile(p);
        Assert.assertEquals(p, pm.getActiveProfile());
    }

    @Test
    @SuppressWarnings("null")
    public void testSetActiveProfile_String() throws IOException {
        ProfileManager pm = new ProfileManager();
        // expect this to throw exception because identifier is null
        boolean threw = false;
        try {
            // null profile
            pm.setActiveProfile((String) null);
        } catch (IllegalArgumentException ex) {
            if (ex.getMessage().equals("identifier is null")) {
                threw = true;
            } else {
                Assert.fail("failed to set profile due to wrong reason: " + ex);
            }
        }
        Assert.assertTrue("Expected exception IllegalArgumentException", threw);
        
        // non-existant profile
        pm.setActiveProfile("NonExistantId");
        Assert.assertFalse(pm.hasActiveProfile());
        JUnitAppender.assertWarnMessage("Unable to set active profile.  No profile with id NonExistantId could be found.");
        // existant non-profile directory (real directory, but no profile)
        String folderName = folder.newFolder("non-profile").getAbsolutePath();
        pm.setActiveProfile(folderName);
        Assert.assertFalse(pm.hasActiveProfile());
        JUnitAppender.assertErrorMessage(folderName + " is not a profile folder.");
        JUnitAppender.assertWarnMessage("Unable to set active profile.  No profile with id " + folderName + " could be found.");
        // existant profile directory
        folderName = folder.newFolder("profile").getAbsolutePath();
        File profileFolder = new File(folderName);
        FileUtil.copy(new File("java/test/jmri/profile/samples/ln-simulator"), profileFolder); // where is existing profile?
        pm.setActiveProfile(folderName);
        Profile p = new Profile(profileFolder);
        Assert.assertTrue(pm.hasActiveProfile());
        Assert.assertEquals(p, pm.getActiveProfile());
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

    // private final static Logger log = LoggerFactory.getLogger(ProfileManagerTest.class);
}
