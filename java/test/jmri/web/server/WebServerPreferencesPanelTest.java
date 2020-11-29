package jmri.web.server;

import jmri.swing.PreferencesPanelTestBase;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the jmri.web.server.WebServerPreferencesPanel class
 *
 * @author Paul Bender Copyright (C) 2012, 2016
 */
public class WebServerPreferencesPanelTest extends PreferencesPanelTestBase<WebServerPreferencesPanel> {

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        jmri.InstanceManager.setDefault(WebServerPreferences.class, new WebServerPreferences());
        JUnitUtil.initStartupActionsManager();
        prefsPanel = new WebServerPreferencesPanel();
    }

}
