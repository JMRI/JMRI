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

    @Disabled("needs additional setup")
    @ToDo("fix setup so the parent class test can run")
    @Test
    @Override
    public void getPreferencesItem() {
    }

    @Disabled("needs additional setup")
    @ToDo("fix setup so the parent class test can run")
    @Test
    @Override
    public void getPreferencesItemText() {
    }

    @Disabled("needs additional setup")
    @ToDo("fix setup so the parent class test can run")
    @Test
    @Override
    public void getPreferencesComponent() {
    }

    @Disabled("needs additional setup")
    @ToDo("fix setup so the parent class test can run")
    @Test
    @Override
    public void getPreferencesTooltip() {
    }

    @Disabled("needs additional setup")
    @ToDo("fix setup so the parent class test can run")
    @Test
    @Override
    public void savePreferences() {
    }

    @Disabled("needs additional setup")
    @ToDo("fix setup so the parent class test can run")
    @Test
    @Override
    public void isDirty() {
    }

    @Disabled("needs additional setup")
    @ToDo("fix setup so the parent class test can run")
    @Test
    @Override
    public void isRestartRequired() {
    }

    @Disabled("needs additional setup")
    @ToDo("fix setup so the parent class test can run")
    @Test
    @Override
    public void isPreferencesValid() {
    }
    // private final static Logger log = LoggerFactory.getLogger(GuiLocalePreferencesPanelTest.class);

}
