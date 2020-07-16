package jmri.jmrix.loconet.loconetovertcp;

import jmri.swing.PreferencesPanelTestBase;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Paul Bender Copyright (C) 2017
 */
public class LnTcpPreferencesPanelTest extends PreferencesPanelTestBase {

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        jmri.util.JUnitUtil.initStartupActionsManager();
        prefsPanel = new LnTcpPreferencesPanel();
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

    // private final static Logger log = LoggerFactory.getLogger(LnTcpPreferencesPanelTest.class);

}
