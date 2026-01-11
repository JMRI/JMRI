package jmri.jmrit.throttle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.*;

import java.awt.Dimension;

/**
 * Test simple functioning of ThrottlesPreferences
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class ThrottlesPreferencesTest {
    private ThrottlesPreferences preferences;

    @Test
    public void testCtor() {
        assertNotNull( preferences, "exists");
        assertFalse( preferences.isDirty(), "default preferences not dirty");
    }

    @Test
    public void testIsUsingFunctionIcons() {
        assertNotNull( preferences, "exists");
        assertFalse( preferences.isDirty(), "default preferences not dirty");

        assertTrue( preferences.isUsingExThrottle(), "default using extended throttle");
        assertTrue( preferences.isUsingFunctionIcon(), "default using icons");

        preferences.setUsingFunctionIcon(true);
        assertTrue( preferences.isDirty(), "preferences dirty after setting icons");
        assertTrue( preferences.isUsingExThrottle(), "use icons after setting setUsingFunctionIcon");

    }

    @Test
    public void testWindowDimension() {
        assertNotNull( preferences, "exists");
        assertFalse( preferences.isDirty(), "default preferences not dirty");

        Dimension d = new Dimension(800, 600);
        assertEquals( preferences.getWindowDimension(), d, "default window dimensions");

        d.width = 640;
        d.height = 480;
        preferences.setWindowDimension(d);
        assertTrue( preferences.isDirty(),
                "preferences dirty after setting window dimensions");

        Dimension d2 = new Dimension(640, 480);
        assertEquals( d, d2, "test sanity");
        assertEquals( d2, preferences.getWindowDimension(), "new window dimensions");
    }

    @Test
    public void testUseExThrottle() {
        assertNotNull( preferences, "exists");
        assertFalse( preferences.isDirty(), "default preferences not dirty");

        assertTrue( preferences.isUsingExThrottle(), "default extended throttle to true");

        preferences.setUseExThrottle(false);

        assertFalse( preferences.isUsingExThrottle(), "ex throttle setting was updated");
        assertTrue( preferences.isDirty(), "preferences dirty after changing extened throttle setting");
    }

    @Test
    public void testUsingToolbar() {
        assertNotNull( preferences, "exists");
        assertFalse( preferences.isDirty(), "default preferences not dirty");

        assertTrue( preferences.isUsingToolBar(), "default is using toolbar to true");

        preferences.setUsingToolBar(false);

        assertFalse( preferences.isUsingToolBar(), "using toolbar setting was updated");
        assertTrue( preferences.isDirty(), "preferences dirty after toolbar setting update");
    }

    @Test
    public void testAutoInstance() {
        assertNotNull( jmri.InstanceManager.getDefault(ThrottlesPreferences.class)
            , "Instance should be created by call to getDefault");
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        preferences = new ThrottlesPreferences();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();

    }
}
