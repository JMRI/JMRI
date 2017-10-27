package jmri.web.servlet.json;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.web.servlet.json.JsonServlet class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class JsonServletTest {

    @Test
    public void testCtor() {
        JsonServlet a = new JsonServlet();
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
