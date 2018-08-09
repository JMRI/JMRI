package jmri.web.servlet.tables;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.web.servlet.tables.TablesServlet class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class TablesServletTest {

    @Test
    public void testCtor() {
        TablesServlet a = new TablesServlet();
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
