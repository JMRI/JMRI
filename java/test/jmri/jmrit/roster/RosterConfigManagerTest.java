package jmri.jmrit.roster;

import java.util.prefs.Preferences;

import jmri.profile.ProfileManager;
import jmri.profile.ProfileUtils;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.prefs.InitializationException;
import jmri.util.swing.JemmyUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

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
     * Headless-safe: no dialog is involved at this level.
     */
    @Test
    public void testSetDirectoryNonExistentThrows() {
        RosterConfigManager manager = new RosterConfigManager();
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                manager.setDirectory(null, "/nonexistent/roster/path/that/does/not/exist/"));
    }

    /**
     * When the configured roster directory is unavailable at startup,
     * initialize() must log a warning, show a Continue/Quit dialog, and throw
     * InitializationException if the user chooses Continue.
     * Uses Jemmy to dismiss the modal dialog automatically.
     */
    @Test
    @jmri.util.junit.annotations.DisabledIfHeadless
    public void testInitializeWithUnavailableDirectoryShowsDialog() throws Exception {
        String badPath = "/nonexistent/roster/path/that/does/not/exist/";
        Preferences prefs = ProfileUtils.getPreferences(
                ProfileManager.getDefault().getActiveProfile(),
                RosterConfigManager.class,
                true);
        prefs.put(RosterConfigManager.DIRECTORY, badPath);
        prefs.sync();

        String title = Bundle.getMessage("RosterLocationUnavailableTitle");
        String continueBtn = Bundle.getMessage("RosterLocationUnavailableContinue");
        Thread dialogThread = JemmyUtil.createModalDialogOperatorThread(title, continueBtn);

        RosterConfigManager manager = new RosterConfigManager();
        Assertions.assertThrows(InitializationException.class, () ->
                manager.initialize(ProfileManager.getDefault().getActiveProfile()));

        JUnitUtil.waitFor(() -> !dialogThread.isAlive(), "dialog dismissed");
        JUnitAppender.assertWarnMessage("Roster location unavailable: " + badPath);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
