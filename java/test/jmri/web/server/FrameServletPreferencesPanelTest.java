package jmri.web.server;

import jmri.swing.PreferencesPanelTestBase;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the jmri.web.server.FrameServletPreferencesPanel class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class FrameServletPreferencesPanelTest extends PreferencesPanelTestBase {

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        jmri.InstanceManager.setDefault(WebServerPreferences.class, new WebServerPreferences());
        prefsPanel = new FrameServletPreferencesPanel();
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

}
