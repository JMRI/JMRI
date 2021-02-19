package jmri.jmrit.throttle;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

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
        Assert.assertNotNull("exists", preferences);
        Assert.assertFalse("default preferences not dirty", preferences.isDirty());
    }

    @Test
    public void testIsUsingFunctionIcons() {
        Assume.assumeNotNull(preferences);
        Assume.assumeFalse(preferences.isDirty());

        Assert.assertTrue("default using extended throttle", preferences.isUsingExThrottle());
        Assert.assertFalse("default not using icons", preferences.isUsingFunctionIcon());

        preferences.setUsingFunctionIcon(true);
        Assert.assertTrue("preferences dirty after setting icons", preferences.isDirty());
        Assert.assertTrue("use icons after setting setUsingFunctionIcon", preferences.isUsingExThrottle());

    }

    @Test
    public void testWindowDimension() {
        Assume.assumeNotNull(preferences);
        Assume.assumeFalse(preferences.isDirty());

        Dimension d = new Dimension(800, 600);
        Assert.assertEquals("default window dimensions", preferences.getWindowDimension(), d);

        d.width = 640;
        d.height = 480;
        preferences.setWindowDimension(d);
        Assert.assertTrue("preferences dirty after setting window dimensions",
            preferences.isDirty());

        Dimension d2 = new Dimension(640, 480);
        Assert.assertEquals("test sanity", d, d2);
        Assert.assertEquals("new window dimensions", preferences.getWindowDimension(), d2);
    }

    @Test
    public void testUseExThrottle() {
        Assume.assumeNotNull(preferences);
        Assume.assumeFalse(preferences.isDirty());

        Assert.assertTrue("default extended throttle to true", preferences.isUsingExThrottle());

        preferences.setUseExThrottle(false);

        Assert.assertFalse("ex throttle setting was updated", preferences.isUsingExThrottle());
        Assert.assertTrue("preferences dirty after changing extened throttle setting", preferences.isDirty());
    }

    @Test
    public void testUsingToolbar() {
        Assume.assumeNotNull(preferences);
        Assume.assumeFalse(preferences.isDirty());

        Assert.assertTrue("default is using toolbar to true", preferences.isUsingToolBar());

        preferences.setUsingToolBar(false);

        Assert.assertFalse("using toolbar setting was updated", preferences.isUsingToolBar());
        Assert.assertTrue("preferences dirty after toolbar setting update", preferences.isDirty());
    }

    @BeforeEach
    public void setUp() throws Exception {
        jmri.util.JUnitUtil.setUp();
        preferences = new ThrottlesPreferences();
    }

    @AfterEach
    public void tearDown() throws Exception {
        jmri.util.JUnitUtil.tearDown();

    }
}
