package apps.gui3.tabbedpreferences;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class TabbedPreferencesTest extends jmri.util.swing.JmriPanelTest {

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.resetPreferencesProviders();

        panel  = new TabbedPreferences();
        helpTarget = "package.apps.TabbedPreferences";
        title = Bundle.getMessage("TitlePreferences");
    }

    @AfterEach
    @Override
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private static final Logger log = LoggerFactory.getLogger(TabbedPreferencesTest.class);

}
