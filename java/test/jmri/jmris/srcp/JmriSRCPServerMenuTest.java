package jmri.jmris.srcp;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Tests for the jmri.jmris.srcp.JmriSRCPServerMenu class
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class JmriSRCPServerMenuTest {

    @Test
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    public void testCtor() {
        JmriSRCPServerMenu a = new JmriSRCPServerMenu();
        Assertions.assertNotNull(a);
    }

    @Test
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    public void testJmriSRCPServerStringCtor() {
        JmriSRCPServerMenu a = new JmriSRCPServerMenu("Hello World");
        Assertions.assertNotNull(a);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
