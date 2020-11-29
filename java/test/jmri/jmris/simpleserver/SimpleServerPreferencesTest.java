package jmri.jmris.simpleserver;

import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the jmri.jmris.simpleserver.SimpleServerPreferences class
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SimpleServerPreferencesTest {

    @Test public void testCtor() {
        SimpleServerPreferences a = new SimpleServerPreferences();
        assertThat(a).isNotNull();
    }

    @Test public void testStringCtor() {
        SimpleServerPreferences a = new SimpleServerPreferences("Hello World");
        assertThat(a).isNotNull();
    }

    @Test public void defaultPort() {
        SimpleServerPreferences a = new SimpleServerPreferences();
        assertThat(a.getDefaultPort()).withFailMessage("Default Port").isEqualTo(2048);
    }

    @BeforeEach public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach public void tearDown() {
        JUnitUtil.tearDown();
    }

}
