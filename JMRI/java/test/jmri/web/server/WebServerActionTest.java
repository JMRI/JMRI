package jmri.web.server;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.web.server.WebServerAction class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class WebServerActionTest {

    @Test
    public void testCtor() {
        WebServerAction a = new WebServerAction();
        Assert.assertNotNull(a);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();


    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
