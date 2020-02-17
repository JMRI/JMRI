package jmri.jmris.simpleserver;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * Tests for the jmri.jmris.simpleserver.SimpleServerMenu class
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SimpleServerMenuTest {

    @Test public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SimpleServerMenu a = new SimpleServerMenu();
        Assert.assertNotNull(a);
    }

    @Test public void testStringCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SimpleServerMenu a = new SimpleServerMenu("Hello World");
        Assert.assertNotNull(a);
    }

    @BeforeEach public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }

}
