package jmri.swing;

import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.catchThrowable;

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
    @BeforeEach
    abstract public void setUp();

    @AfterEach
    public void tearDown(){
        prefsPanel = null;
        JUnitUtil.tearDown();
    }

    @Test
    public void testCtor(){
        assertThat(prefsPanel).isNotNull();
    }

    @Test
    public void getPreferencesItem() {
        assertThat(prefsPanel.getPreferencesItem()).isNotNull();
    }

    @Test
    public void getPreferencesItemText() {
        assertThat(prefsPanel.getPreferencesItemText()).isNotNull();
    }

    @Test
    public void getTabbedPreferencesTitle() {
        Throwable thrown = catchThrowable( () -> prefsPanel.getTabbedPreferencesTitle());
        assertThat(thrown).isNull();
    }

    @Test
    public void getLabelKey() {
        Throwable thrown = catchThrowable(() -> prefsPanel.getLabelKey());
        assertThat(thrown).isNull();
    }

    @Test
    public void getPreferencesComponent() {
        assertThat(prefsPanel.getPreferencesComponent()).isNotNull();
    }

    @Test
    public void isPersistant() {
        assertThat(prefsPanel.isPersistant()).isFalse();
    }

    @Test
    public void getPreferencesTooltip() {
        Throwable thrown = catchThrowable( () -> prefsPanel.getPreferencesTooltip());
        assertThat(thrown).isNull();
    }

    @Test
    public void savePreferences() {
        Throwable thrown = catchThrowable( () -> prefsPanel.savePreferences());
        assertThat(thrown).isNull();
    }

    @Test
    public void isDirty() {
        assertThat(prefsPanel.isDirty()).isFalse();
    }

    @Test
    public void isRestartRequired() {
        assertThat(prefsPanel.isRestartRequired()).isFalse();
    }

    @Test
    public void isPreferencesValid() {
        assertThat(prefsPanel.isPreferencesValid()).isTrue();
    }

    @Test
    public void getSortOrder() {
        assertThat(prefsPanel.getSortOrder()).isGreaterThan(0);
    }

}

