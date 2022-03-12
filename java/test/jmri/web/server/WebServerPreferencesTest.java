package jmri.web.server;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

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

    @BeforeEach
    public void setUp(){
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown(){
        JUnitUtil.tearDown();
    }
}
