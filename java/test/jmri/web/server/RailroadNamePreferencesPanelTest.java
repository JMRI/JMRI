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
public class RailroadNamePreferencesPanelTest extends PreferencesPanelTestBase<RailroadNamePreferencesPanel> {

    @Override
    @BeforeEach
    public void setUp(){
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        prefsPanel = new RailroadNamePreferencesPanel();
    }

}
