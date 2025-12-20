package jmri.swing;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 *  Base Tests for implementations of the PreferencesPanel interface.
 *
 * @author Paul Bender Colyright (C) 2020
 */
abstract public class PreferencesPanelTestBase<P extends PreferencesPanel> {

    protected P prefsPanel;

    /**
     * Implementing classes should set the value of prefsPanel in setUp.
     */
    abstract public void setUp();

    @AfterEach
    public void tearDown(){
        prefsPanel = null;
        JUnitUtil.tearDown();
    }

    @Test
    public void testCtor(){
        assertNotNull( prefsPanel );
    }

    @Test
    public void getPreferencesItem() {
        assertNotNull( prefsPanel.getPreferencesItem() );
    }

    @Test
    public void getPreferencesItemText() {
        assertNotNull( prefsPanel.getPreferencesItemText() );
    }

    @Test
    public void getTabbedPreferencesTitle() {
        assertDoesNotThrow( prefsPanel::getTabbedPreferencesTitle );
    }

    @Test
    public void getLabelKey() {
        assertDoesNotThrow( prefsPanel::getLabelKey );
    }

    @Test
    public void getPreferencesComponent() {
        assertNotNull( prefsPanel.getPreferencesComponent() );
    }

    @Test
    public void isPersistant() {
        assertFalse( prefsPanel.isPersistant() );
    }

    @Test
    public void getPreferencesTooltip() {
        assertDoesNotThrow( prefsPanel::getPreferencesTooltip );
    }

    @Test
    public void savePreferences() {
        assertDoesNotThrow( prefsPanel::savePreferences );
    }

    @Test
    public void isDirty() {
        assertFalse( prefsPanel.isDirty() );
    }

    @Test
    public void isRestartRequired() {
        assertFalse( prefsPanel.isRestartRequired() );
    }

    @Test
    public void isPreferencesValid() {
        assertTrue( prefsPanel.isPreferencesValid() );
    }

    @Test
    public void getSortOrder() {
        assertTrue( prefsPanel.getSortOrder() > 0 );
    }

}

