package apps.startup;

import jmri.swing.PreferencesPanelTestBase;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Paul Bender Copyright (C) 2017
 */
public class StartupActionsPreferencesPanelTest extends PreferencesPanelTestBase<StartupActionsPreferencesPanel> {

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initStartupActionsManager();
        prefsPanel = new StartupActionsPreferencesPanel();
    }

    @Override
    @Test
    public void isPersistant(){
        assertThat(prefsPanel.isPersistant()).isTrue();
    }
    // private final static Logger log = LoggerFactory.getLogger(StartupActionsPreferencesPanelTest.class);

}
