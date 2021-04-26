package jmri.jmris.srcp;

import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Tests for the jmri.jmris.srcp.JmriSRCPServerPreferences class
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class JmriSRCPServerPreferencesTest {

    @Test public void testCtor() {
        JmriSRCPServerPreferences a = new JmriSRCPServerPreferences();
        assertThat(a).isNotNull();
    }

    @Test public void testStringCtor() {
        JmriSRCPServerPreferences a = new JmriSRCPServerPreferences("Hello World");
        assertThat(a).isNotNull();
    }

    @Test public void defaultPort() {
        JmriSRCPServerPreferences a = new JmriSRCPServerPreferences();
        assertThat(a.getDefaultPort()).withFailMessage("Default Port").isEqualTo(4303);
    }

    @BeforeEach public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
    }

    @AfterEach public void tearDown() {
        JUnitUtil.tearDown();
    }

}
