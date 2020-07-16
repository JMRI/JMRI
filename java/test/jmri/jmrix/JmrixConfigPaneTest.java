package jmri.jmrix;

import jmri.swing.PreferencesPanelTestBase;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class JmrixConfigPaneTest extends PreferencesPanelTestBase {

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initConnectionConfigManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        prefsPanel = JmrixConfigPane.createNewPanel();
    }

    @Override
    public void getTabbedPreferencesTitle() {
        // should this return null?
        assertThat(prefsPanel.getTabbedPreferencesTitle()).isNull();
    }

    @Override
    @Test
    public void getLabelKey() {
        // should this return null?
        assertThat(prefsPanel.getLabelKey()).isNull();
    }

    @Override
    @Test
    public void isPersistant() {
        assertThat(prefsPanel.isPersistant()).isFalse();
    }

    @Override
    @Test
    public void getPreferencesTooltip() {
        // should this return null?
        assertThat(prefsPanel.getPreferencesTooltip()).isNull();
    }

    @Override
    @Test
    public void isDirty() {
        assertThat(prefsPanel.isDirty()).isFalse();
    }

    @Override
    @Test
    public void isRestartRequired() {
        assertThat(prefsPanel.isRestartRequired()).isFalse();
    }
    // private final static Logger log = LoggerFactory.getLogger(JmrixConfigPaneTest.class);
}
