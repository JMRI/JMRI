package jmri.jmrit.throttle;

import java.io.File;
import java.io.IOException;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Steve Young (c) Copyright 2022
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class ThrottlesPreferencesUISettingsPaneTest {

    @Test
    public void testConstructorNull() {
        assertDoesNotThrow( () -> { 
            ThrottlesPreferencesUISettingsPane t = new ThrottlesPreferencesUISettingsPane(null);
            assertNotNull(t);
        });
    }

    @Test
    public void testConstructorWithPreferences() {
        assertDoesNotThrow( () -> {
            ThrottlesPreferences tp = new ThrottlesPreferences();
            ThrottlesPreferencesUISettingsPane t = new ThrottlesPreferencesUISettingsPane(tp);
            assertNotNull(t);
        });
    }

    @BeforeEach
    public void setUp(@TempDir File folder) throws IOException {
        JUnitUtil.setUp();
        // The profile is setup with a temp directory to ensure no contamination
        // from previous tests when setting user preferences.
        JUnitUtil.resetProfileManager(new jmri.profile.NullProfile(folder));
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
