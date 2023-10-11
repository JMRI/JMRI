package jmri.web.server;

import jmri.swing.PreferencesPanelTestBase;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.web.server.FrameServletPreferencesPanel class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class FrameServletPreferencesPanelTest extends PreferencesPanelTestBase<FrameServletPreferencesPanel> {

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        jmri.InstanceManager.setDefault(WebServerPreferences.class, new WebServerPreferences());
        prefsPanel = new FrameServletPreferencesPanel();
    }

}
