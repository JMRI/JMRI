package jmri.jmris.simpleserver;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.Assume;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Tests for the jmri.jmris.simpleserver.SimpleServerAction class
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SimpleServerActionTest {

    @Test public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SimpleServerAction a = new SimpleServerAction();
        assertThat(a).isNotNull();
    }

    @Test public void testStringCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SimpleServerAction a = new SimpleServerAction("Hello World");
        assertThat(a).isNotNull();
    }

    @BeforeEach public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach public void tearDown() {
        JUnitUtil.tearDown();
    }

}
