package jmri.jmris.simpleserver;

import jmri.swing.PreferencesPanelTestBase;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Tests for the jmri.jmris.simpleserver.SimpleServerPreferencesPanel class
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SimpleServerPreferencesPanelTest extends PreferencesPanelTestBase<SimpleServerPreferencesPanel> {

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        prefsPanel = new SimpleServerPreferencesPanel();
    }

}
