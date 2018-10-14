package jmri.web.servlet.roster;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
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
    public void setUp() {
        JUnitUtil.setUp();

    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
