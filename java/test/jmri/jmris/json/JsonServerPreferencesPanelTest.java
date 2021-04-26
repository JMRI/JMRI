package jmri.jmris.json;

import jmri.swing.PreferencesPanelTestBase;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class JsonServerPreferencesPanelTest extends PreferencesPanelTestBase<JsonServerPreferencesPanel> {

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        prefsPanel = new JsonServerPreferencesPanel();
    }

    // private final static Logger log = LoggerFactory.getLogger(JsonServerPreferencesPanelTest.class);

}
