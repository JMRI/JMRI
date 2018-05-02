//SimpleServerTest.java
package jmri.jmris.simpleserver;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * Tests for the jmri.jmris.simpleserver.SimpleServer class 
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class SimpleServerTest {

    @Test
    public void testCtor() {
        SimpleServer a = new SimpleServer();
        Assert.assertNotNull(a);
        jmri.util.JUnitAppender.suppressErrorMessage("Failed to connect to port 2048");
    }

    @Test
    public void testCtorwithParameter() {
        SimpleServer a = new SimpleServer(2048);
        Assert.assertNotNull(a);
        jmri.util.JUnitAppender.suppressErrorMessage("Failed to connect to port 2048");
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
