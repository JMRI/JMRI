package jmri.web.server;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.web.server.WebServerPreferencesPanel class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class WebServerPreferencesPanelTest {

    @Test
    public void testCtor() {
        WebServerPreferencesPanel a = new WebServerPreferencesPanel();
        Assert.assertNotNull(a);
    }

    @Before
    public void setUp(){
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.InstanceManager.setDefault(WebServerPreferences.class,new WebServerPreferences());
        jmri.util.JUnitUtil.initStartupActionsManager();
    }

    @After
    public void tearDown(){
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }
}
