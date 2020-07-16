package apps;

import jmri.swing.PreferencesPanel;
import jmri.util.JUnitUtil;
import jmri.swing.PreferencesPanelTestBase;

import jmri.util.junit.annotations.ToDo;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class GuiLocalePreferencesPanelTest extends PreferencesPanelTestBase<GuiLocalePreferencesPanel> {

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        prefsPanel = new GuiLocalePreferencesPanel();
        PreferencesPanel parent = Mockito.mock(PreferencesPanel.class);
        prefsPanel.setParent(parent);
    }

    @Disabled
    @ToDo("fix setup so the parent class test can run")
    @Test
    @Override
    public void getPreferencesItem() {
    }

    @Disabled
    @ToDo("fix setup so the parent class test can run")
    @Test
    @Override
    public void getPreferencesItemText() {
    }

    @Disabled
    @ToDo("fix setup so the parent class test can run")
    @Test
    @Override
    public void getPreferencesComponent() {
    }

    @Disabled
    @ToDo("fix setup so the parent class test can run")
    @Test
    @Override
    public void getPreferencesTooltip() {
    }

    @Disabled
    @ToDo("fix setup so the parent class test can run")
    @Test
    @Override
    public void savePreferences() {
    }

    @Disabled
    @ToDo("fix setup so the parent class test can run")
    @Test
    @Override
    public void isDirty() {
    }

    @Disabled
    @ToDo("fix setup so the parent class test can run")
    @Test
    @Override
    public void isRestartRequired() {
    }

    @Disabled
    @ToDo("fix setup so the parent class test can run")
    @Test
    @Override
    public void isPreferencesValid() {
    }
    // private final static Logger log = LoggerFactory.getLogger(GuiLocalePreferencesPanelTest.class);

}
