package jmri.web.server;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.web.server.FrameServletPreferencesPanel class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class FrameServletPreferencesPanelTest {

    @Test
    public void testCtor() {
        FrameServletPreferencesPanel a = new FrameServletPreferencesPanel();
        Assert.assertNotNull(a);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        jmri.InstanceManager.setDefault(WebServerPreferences.class, new WebServerPreferences());
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
