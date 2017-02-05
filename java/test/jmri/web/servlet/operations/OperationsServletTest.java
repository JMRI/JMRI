package jmri.web.servlet.operations;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.web.servlet.operations.OperationsServlet class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class OperationsServletTest {

    @Test
    public void testCtor() {
        OperationsServlet a = new OperationsServlet();
        Assert.assertNotNull(a);
    }

    @Before
    public void setUp(){
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown(){
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }
}
