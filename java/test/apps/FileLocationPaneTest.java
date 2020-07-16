package apps;

import jmri.InstanceManager;
import jmri.implementation.FileLocationsPreferences;
import jmri.swing.PreferencesPanelTestBase;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;
import org.junit.Assume;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class FileLocationPaneTest extends PreferencesPanelTestBase {

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        InstanceManager.setDefault(FileLocationsPreferences.class, Mockito.mock(FileLocationsPreferences.class));
        prefsPanel = new FileLocationPane();
    }

    @Override
    @Test
    public void isPersistant() {
        assertThat(prefsPanel.isPersistant()).isFalse();
    }

    @Override
    @Test
    public void getPreferencesTooltip() {
        // should this actually return null?
        assertThat(prefsPanel.getPreferencesTooltip()).isNull();
    }

    // private final static Logger log = LoggerFactory.getLogger(FileLocationPaneTest.class);

}
