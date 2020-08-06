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
public class JmrixConfigPaneTest extends PreferencesPanelTestBase<JmrixConfigPane> {

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initConnectionConfigManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        prefsPanel = JmrixConfigPane.createNewPanel();
    }

    @Override
    @Test
    public void isPersistant() {
        assertThat(prefsPanel.isPersistant()).isTrue();
    }

    @Override
    @Test
    public void isDirty() {
        assertThat(prefsPanel.isDirty()).isTrue();
    }

    @Override
    @Test
    public void isRestartRequired() {
        assertThat(prefsPanel.isRestartRequired()).isTrue();
    }
    // private final static Logger log = LoggerFactory.getLogger(JmrixConfigPaneTest.class);
}
