package jmri.web.servlet.home;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.web.servlet.home.HomeServlet class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class HomeServletTest {

    @Test
    public void testCtor() {
        HomeServlet a = new HomeServlet();
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
