package jmri.jmris.simpleserver;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Tests for the jmri.jmris.simpleserver.SimpleServerAction class
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SimpleServerActionTest {

    @Test
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    public void testCtor() {
        SimpleServerAction a = new SimpleServerAction();
        Assertions.assertNotNull(a);
    }

    @Test
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    public void testSimpleServerActionStringCtor() {
        SimpleServerAction a = new SimpleServerAction("Hello World");
        Assertions.assertNotNull(a);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
