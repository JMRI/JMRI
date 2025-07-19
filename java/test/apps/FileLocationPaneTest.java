package apps;

import jmri.InstanceManager;
import jmri.implementation.FileLocationsPreferences;
import jmri.swing.PreferencesPanelTestBase;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.mockito.Mockito;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class FileLocationPaneTest extends PreferencesPanelTestBase<FileLocationPane> {

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        InstanceManager.setDefault(FileLocationsPreferences.class, Mockito.mock(FileLocationsPreferences.class));
        prefsPanel = new FileLocationPane();
    }

    @Override
    @Test
    public void isPersistant() {
        Assertions.assertTrue( prefsPanel.isPersistant() );
    }

    // private final static Logger log = LoggerFactory.getLogger(FileLocationPaneTest.class);

}
