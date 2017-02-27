package jmri.jmris.simpleserver;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.jmris.simpleserver.SimpleServerPreferences class
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SimpleServerPreferencesTest {

    @Test public void testCtor() {
        SimpleServerPreferences a = new SimpleServerPreferences();
        Assert.assertNotNull(a);
    }

    @Test public void testStringCtor() {
        SimpleServerPreferences a = new SimpleServerPreferences("Hello World");
        Assert.assertNotNull(a);
    }

    @Test public void defaultPort() {
        SimpleServerPreferences a = new SimpleServerPreferences();
        Assert.assertEquals("Default Port",2048,a.getDefaultPort());
    }

    @Before public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After public void tearDown() throws Exception {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

}
