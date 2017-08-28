package jmri.jmris.simpleserver;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;


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

    @Before public void setUp() {
        JUnitUtil.setUp();
    }

    @After public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }

}
