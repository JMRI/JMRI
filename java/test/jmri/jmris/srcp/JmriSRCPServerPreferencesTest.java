package jmri.jmris.srcp;

import jmri.util.JUnitUtil;
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
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
    }

    @After public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }

}
