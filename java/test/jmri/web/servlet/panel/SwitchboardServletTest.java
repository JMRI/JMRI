package jmri.web.servlet.panel;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.web.servlet.panel.SwitchboardServlet class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 * @author Egbert Broerse Copyright (C) 2017
 */
public class SwitchboardServletTest {

    @Test
    public void testCtor() {
        SwitchboardServlet a = new SwitchboardServlet();
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
