package jmri.jmris.simpleserver;

import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        assertThat(a.getDefaultPort()).isEqualTo(2048).withErrorFailMessage("Default Port");
    }

    @BeforeEach public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }

}
