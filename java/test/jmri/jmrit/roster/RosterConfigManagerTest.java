package jmri.jmrit.roster;

import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

import jmri.profile.NullProfile;
import jmri.profile.Profile;
import jmri.profile.ProfileManager;
import jmri.profile.ProfileUtils;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class RosterConfigManagerTest {

    @Test
    public void testCTor() {
        RosterConfigManager t = new RosterConfigManager();
        Assert.assertNotNull("exists", t);
    }

    /**
     * setDirectory() must reject a path that does not exist as a directory.
     */
    @Test
    public void testSetDirectoryNonExistentThrows() {
        RosterConfigManager manager = new RosterConfigManager();
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                manager.setDirectory(null, "/nonexistent/roster/path/that/does/not/exist/"));
    }

    /**
     * When the configured roster directory is unavailable at startup,
     * initialize() must log a warning and throw RosterLocationUnavailableException
     * carrying the unavailable path. The Continue/Quit dialog is owned by
     * JmriConfigurationManager, not this class, so this test does not exercise UI.
     */
    @Test
    public void testInitializeWithUnavailableDirectoryThrows() throws Exception {
        String badPath = "/nonexistent/roster/path/that/does/not/exist/";
        Profile profile = ProfileManager.getDefault().getActiveProfile();
        Preferences prefs = ProfileUtils.getPreferences(profile, RosterConfigManager.class, true);
        prefs.put(RosterConfigManager.DIRECTORY, badPath);
        prefs.sync();

        RosterConfigManager manager = new RosterConfigManager();
        RosterLocationUnavailableException ex = Assertions.assertThrows(
                RosterLocationUnavailableException.class,
                () -> manager.initialize(profile));
        Assertions.assertEquals(badPath, ex.getUnavailablePath());
        JUnitAppender.assertWarnMessage("Roster location unavailable: " + badPath);
    }

    @BeforeEach
    public void setUp(@TempDir File folder) throws IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager(new NullProfile(folder));
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
