package jmri.jmris.srcp;

import jmri.swing.PreferencesPanelTestBase;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the jmri.jmris.srcp.JmriSRCPServerPreferencesPanel class
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class JmriSRCPServerPreferencesPanelTest extends PreferencesPanelTestBase<JmriSRCPServerPreferencesPanel> {

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        prefsPanel = new JmriSRCPServerPreferencesPanel();
    }

}
