package apps.gui3.tabbedpreferences;

import apps.gui3.tabbedpreferences.EditConnectionPreferencesDialog;
import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

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

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.resetPreferencesProviders();
        
        jmri.InstanceManager.setDefault(TabbedPreferences.class, new TabbedPreferences());
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TabbedPreferencesFrameTest.class);

}
