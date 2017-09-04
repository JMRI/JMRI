package jmri.web.server;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.web.server.WebServer class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class WebServerTest {

    @Test
    public void testCtor() {
        WebServer a = new WebServer();
        Assert.assertNotNull(a);
    }

    @Test
    public void testGetDefault() {
        WebServer a = WebServer.getDefault();
        Assert.assertNotNull(a);
    }

    @Test
    public void testGetPort() {
        WebServer a = WebServer.getDefault();
        Assert.assertEquals("Default Port",12080,a.getPort());
    }

    @Test
    public void testURIForPreferences(){
        Assert.assertEquals("URI for Preferences directory","/prefs/",WebServer.URIforPortablePath("preference:"));
    }

    @Test
    public void testURIForProgram(){
        Assert.assertEquals("URI for Program directory","/dist/",WebServer.URIforPortablePath("program:"));
    }

    @Test
    public void testURIForOther(){
        Assert.assertNull("URI for Other directory",WebServer.URIforPortablePath("roster:"));
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
