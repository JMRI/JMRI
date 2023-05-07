package jmri.jmris.simpleserver;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Tests for the jmri.jmris.simpleserver.SimpleServerMenu class
 *
 * @author Paul Bender Copyright (C) 2016
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public class SimpleServerMenuTest {

    @Test
    public void testCtor() {
        SimpleServerMenu a = new SimpleServerMenu();
        Assertions.assertNotNull(a);
    }

    @Test
    public void testSimpleServerMenuStringCtor() {
        SimpleServerMenu a = new SimpleServerMenu("Hello World");
        Assertions.assertNotNull(a);
    }

    @BeforeEach public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach public void tearDown() {
        JUnitUtil.tearDown();
    }

}
