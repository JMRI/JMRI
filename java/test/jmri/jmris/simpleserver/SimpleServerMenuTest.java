package jmri.jmris.simpleserver;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;


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

    @Before public void setUp() {
        JUnitUtil.setUp();
    }

    @After public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }

}
