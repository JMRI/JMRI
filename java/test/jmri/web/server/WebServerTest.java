package jmri.web.server;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.web.server.WebServer class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class WebServerTest {

    @Test
    public void testGetPort() {
        WebServer a = WebServer.getDefault();
        Assert.assertEquals("Default Port", 12080, a.getPort());
    }

    @Test
    public void testPreferencesToURI() {
        Assert.assertEquals("URI for Preferences directory", "/prefs/", WebServer.portablePathToURI("preference:"));
    }

    @Test
    public void testProgramToURI() {
        Assert.assertEquals("URI for Program directory", "/dist/", WebServer.portablePathToURI("program:"));
    }

    @Test
    public void testProfileToURI() {
        Assert.assertEquals("URI for Program directory", "/project/", WebServer.portablePathToURI("profile:"));
    }

    @Test
    public void testSettingsToURI() {
        Assert.assertEquals("URI for Program directory", "/settings/", WebServer.portablePathToURI("settings:"));
    }

    @Test
    public void testOtherToURI() {
        Assert.assertNull("URI for Other directory", WebServer.portablePathToURI("roster:"));
    }

    @Test
    public void testStartAndStop() throws Exception {
        WebServer a = new WebServer();
        a.start();
        JUnitUtil.waitFor(() -> a.isStarted(), "server failed to start in time");
        Assert.assertTrue(a.isStarted());
        a.stop();
        JUnitUtil.waitFor(() -> a.isStopped(), "server failed to stop in time");
        Assert.assertTrue(a.isStopped());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initZeroConfServiceManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.resetZeroConfServiceManager();
        JUnitUtil.tearDown();
    }
}
