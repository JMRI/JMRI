package jmri.jmris.srcp;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Tests for the jmri.jmris.srcp.JmriSRCPServerAction class
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class JmriSRCPServerActionTest {

    @Test
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    public void testCtor() {
        JmriSRCPServerAction a = new JmriSRCPServerAction();
        Assertions.assertNotNull(a);
    }

    @Test
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    public void testJmriSRCPServerActionStringCtor() {
        JmriSRCPServerAction a = new JmriSRCPServerAction("Hello World");
        Assertions.assertNotNull(a);
    }

    @BeforeEach public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach public void tearDown() {
        JUnitUtil.tearDown();
    }

}
