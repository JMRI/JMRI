package apps;

import jmri.swing.PreferencesPanelTestBase;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ManagerDefaultsConfigPaneTest extends PreferencesPanelTestBase<ManagerDefaultsConfigPane> {

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetPreferencesProviders();
        JUnitUtil.initConfigureManager();
        prefsPanel = new ManagerDefaultsConfigPane();
    }

    @Override
    @Test
    public void isPersistant() {
        Assertions.assertTrue(prefsPanel.isPersistant());
    }

    // private static final Logger log = LoggerFactory.getLogger(ManagerDefaultsConfigPaneTest.class);

}
