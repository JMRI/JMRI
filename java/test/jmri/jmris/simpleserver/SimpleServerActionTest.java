package jmri.jmris.simpleserver;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * Tests for the jmri.jmris.simpleserver.SimpleServerAction class
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SimpleServerActionTest {

    @Test public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SimpleServerAction a = new SimpleServerAction();
        Assert.assertNotNull(a);
    }

    @Test public void testStringCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SimpleServerAction a = new SimpleServerAction("Hello World");
        Assert.assertNotNull(a);
    }

    @BeforeEach public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }

}
