package jmri.jmrix.loconet.loconetovertcp;

import jmri.swing.PreferencesPanelTestBase;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * @author Paul Bender Copyright (C) 2017
 */
public class LnTcpPreferencesPanelTest extends PreferencesPanelTestBase<LnTcpPreferencesPanel> {

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        jmri.util.JUnitUtil.initStartupActionsManager();
        prefsPanel = new LnTcpPreferencesPanel();
    }

    // private final static Logger log = LoggerFactory.getLogger(LnTcpPreferencesPanelTest.class);

}
