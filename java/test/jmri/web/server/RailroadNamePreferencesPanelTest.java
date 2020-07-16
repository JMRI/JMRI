package jmri.web.server;

import jmri.swing.PreferencesPanelTestBase;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the jmri.web.server.RailroadNamePreferencesPanel class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class RailroadNamePreferencesPanelTest extends PreferencesPanelTestBase {

    @Override
    @BeforeEach
    public void setUp(){
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        prefsPanel = new RailroadNamePreferencesPanel();
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
}
