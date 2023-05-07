package apps.gui3.tabbedpreferences;

import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 *
 */
public class EditConnectionPreferencesDialogTest {

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testCTor() {

        Thread t = JemmyUtil.createModalDialogOperatorThread("Preferences", "Quit");
        ThreadingUtil.runOnGUI(() -> {
            Assertions.assertFalse(EditConnectionPreferencesDialog.showDialog());
        });
        JUnitUtil.waitFor(() -> { return !t.isAlive(); }," dialog thread did not close");

    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.resetPreferencesProviders();

        jmri.InstanceManager.setDefault(TabbedPreferences.class, new TabbedPreferences());
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TabbedPreferencesFrameTest.class);

}
