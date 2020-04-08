package jmri.jmris.simpleserver;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.Assume;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Tests for the jmri.jmris.simpleserver.SimpleServerMenu class
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SimpleServerMenuTest {

    @Test public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SimpleServerMenu a = new SimpleServerMenu();
        assertThat(a).isNotNull();
    }

    @Test public void testStringCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SimpleServerMenu a = new SimpleServerMenu("Hello World");
        assertThat(a).isNotNull();
    }

    @BeforeEach public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach public void tearDown() {
        JUnitUtil.tearDown();
    }

}
