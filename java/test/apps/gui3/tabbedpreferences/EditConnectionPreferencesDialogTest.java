package apps.gui3.tabbedpreferences;

import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for EditConnectionPreferencesDialog
 */
public class EditConnectionPreferencesDialogTest {

    @Test
    @jmri.util.junit.annotations.DisabledIfHeadless
    public void testCTor() {

        Thread t = JemmyUtil.createModalDialogOperatorThread("Preferences", "Quit");
        boolean tst = ThreadingUtil.runOnGUIwithReturn(() -> {
            return EditConnectionPreferencesDialog.showDialog();
        });
        Assertions.assertFalse(tst);
        JUnitUtil.waitFor(() -> !t.isAlive()," dialog thread did not close");

    }

    @BeforeEach
    public void setUp(@TempDir java.io.File folder) {
        JUnitUtil.setUp();
        try {
            JUnitUtil.resetProfileManager(new jmri.profile.NullProfile(folder));
        } catch (java.io.IOException ioe) {
            Assertions.fail("Failed to reset the profile", ioe);
        }
        JUnitUtil.initConfigureManager();
        JUnitUtil.resetPreferencesProviders();

        jmri.InstanceManager.setDefault(TabbedPreferences.class, new TabbedPreferences());
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.resetWindows(false, false); // tidy up after Dialog with null parent
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TabbedPreferencesFrameTest.class);

}
