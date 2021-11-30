package jmri.jmrit.logix;

import jmri.swing.PreferencesPanelTestBase;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Paul Bender Copyright (C) 2017
 */
public class WarrantPreferencesPanelTest extends PreferencesPanelTestBase<WarrantPreferencesPanel> {

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        prefsPanel = new WarrantPreferencesPanel();
    }

    @Override
    @AfterEach
    public void tearDown() {
        prefsPanel = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(WarrantPreferencesPanelTest.class);

}
