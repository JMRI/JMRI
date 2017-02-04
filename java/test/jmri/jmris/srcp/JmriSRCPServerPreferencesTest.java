package jmri.jmris.srcp;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * Tests for the jmri.jmris.srcp.JmriSRCPServerPreferences class
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class JmriSRCPServerPreferencesTest {

    @Test public void testCtor() {
        JmriSRCPServerPreferences a = new JmriSRCPServerPreferences();
        Assert.assertNotNull(a);
    }

    @Test public void testStringCtor() {
        JmriSRCPServerPreferences a = new JmriSRCPServerPreferences("Hello World");
        Assert.assertNotNull(a);
    }

    @Test public void defaultPort() {
        JmriSRCPServerPreferences a = new JmriSRCPServerPreferences();
        Assert.assertEquals("Default Port",4303,a.getDefaultPort());
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
