package jmri.jmrix.swing;

import jmri.swing.PreferencesPanelTestBase;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ConnectionsPreferencesPanelTest extends PreferencesPanelTestBase {

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initConnectionConfigManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        prefsPanel = new ConnectionsPreferencesPanel();
    }

    @Override
    @Test
    public void getLabelKey(){
        // This class returns null for label key, but should it?
        assertThat(prefsPanel.getLabelKey()).isNull();
    }

    @Override
    @Test
    public void getPreferencesTooltip(){
        // This class returns null for preferences tool tip, but should it?
        assertThat(prefsPanel.getPreferencesTooltip()).isNull();
    }

    @Override
    @Test
    public void getTabbedPreferencesTitle(){
        // This class returns null for preferences tool tip, but should it?
        assertThat(prefsPanel.getTabbedPreferencesTitle()).isNull();
    }

    @Override
    @Test
    public void isRestartRequired(){
        assertThat(prefsPanel.isRestartRequired()).isTrue();
    }

    @Override
    @Test
    public void isDirty(){
        assertThat(prefsPanel.isDirty()).isTrue();
    }

    // private final static Logger log = LoggerFactory.getLogger(ConnectionsPreferencesPanelTest.class);

}
