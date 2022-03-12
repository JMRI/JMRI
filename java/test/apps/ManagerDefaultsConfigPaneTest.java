package apps;

import jmri.swing.PreferencesPanelTestBase;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ManagerDefaultsConfigPaneTest extends PreferencesPanelTestBase<ManagerDefaultsConfigPane> {

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
        assertThat(prefsPanel.isPersistant()).isTrue();
    }

    // private final static Logger log = LoggerFactory.getLogger(ManagerDefaultsConfigPaneTest.class);

}
