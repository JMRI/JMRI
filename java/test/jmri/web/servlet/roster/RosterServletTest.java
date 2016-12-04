package jmri.web.servlet.roster;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.web.servlet.roster.RosterServlet class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class RosterServletTest {

    @Test
    public void testCtor() {
        RosterServlet a = new RosterServlet();
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
