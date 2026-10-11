package jmri.web.server;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the jmri.web.server.WebServer class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class WebServerTest {

    @Test
    public void testGetPort() {
        WebServer a = WebServer.getDefault();
        assertEquals( 12080, a.getPort(), "Default Port");
    }

    @Test
    public void testPreferencesToURI() {
        assertEquals( "/prefs/", WebServer.portablePathToURI("preference:"), "URI for Preferences directory");
    }

    @Test
    public void testProgramToURI() {
        assertEquals( "/dist/", WebServer.portablePathToURI("program:"), "URI for Program directory");
    }

    @Test
    public void testProfileToURI() {
        assertEquals( "/project/", WebServer.portablePathToURI("profile:"), "URI for Profile directory");
    }

    @Test
    public void testSettingsToURI() {
        assertEquals( "/settings/", WebServer.portablePathToURI("settings:"), "URI for Settings directory");
    }

    @Test
    public void testOtherToURI() {
        assertNull( WebServer.portablePathToURI("roster:"), "URI for Other directory");
    }

    @Test
    public void testStartAndStop() throws Exception {
        WebServer a = new WebServer();
        a.start();
        JUnitUtil.waitFor(() -> a.isStarted(), "server failed to start in time");
        assertTrue(a.isStarted());
        a.stop();
        JUnitUtil.waitFor(() -> a.isStopped(), "server failed to stop in time");
        assertTrue(a.isStopped());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initZeroConfServiceManager();
    }

    @AfterEach
    public void tearDown() {
        assertTrue(JUnitUtil.resetZeroConfServiceManager());
        JUnitUtil.tearDown();
    }
}
