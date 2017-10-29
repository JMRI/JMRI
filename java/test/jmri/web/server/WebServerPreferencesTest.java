package jmri.web.server;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.web.server.WebServerPreferences class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class WebServerPreferencesTest {

    @Test
    public void testCtor() {
        WebServerPreferences a = new WebServerPreferences();
        Assert.assertNotNull(a);
    }

    @Before
    public void setUp(){
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown(){
        JUnitUtil.tearDown();
    }
}
