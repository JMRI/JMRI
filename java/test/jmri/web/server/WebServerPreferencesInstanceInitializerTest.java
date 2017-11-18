package jmri.web.server;

import java.util.Set;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.web.server.WebServerPreferencesInstanceInitializer class
 *
 * @author Randall Wood Copyright 2017
 */
public class WebServerPreferencesInstanceInitializerTest {

    @Test
    public void testGetDefault() {
        WebServerPreferencesInstanceInitializer initializer = new WebServerPreferencesInstanceInitializer();
        WebServerPreferences preferences = (WebServerPreferences) initializer.getDefault(WebServerPreferences.class);
        Assert.assertNotNull(preferences);
    }

    @Test
    public void testGetInitalizes() {
        WebServerPreferencesInstanceInitializer initializer = new WebServerPreferencesInstanceInitializer();
        Set<Class<?>> set = initializer.getInitalizes();
        Assert.assertFalse(set.isEmpty());
        Assert.assertEquals(1, set.size());
        Assert.assertTrue(set.contains(WebServerPreferences.class));
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
