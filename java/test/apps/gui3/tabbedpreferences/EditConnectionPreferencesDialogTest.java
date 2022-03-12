package apps.gui3.tabbedpreferences;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;
import org.junit.Assume;

/**
 *
 */
public class EditConnectionPreferencesDialogTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        EditConnectionPreferencesDialog d = new EditConnectionPreferencesDialog();
        Assert.assertNotNull("exists",d);
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
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TabbedPreferencesFrameTest.class);

}
