package apps.gui3.tabbedpreferences;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class TabbedPreferencesTest extends jmri.util.swing.JmriPanelTest {

    // The minimal setup for log4J
    @Before
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

    @After
    @Override
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TabbedPreferencesTest.class);

}
