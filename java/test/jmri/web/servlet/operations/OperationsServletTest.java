package jmri.web.servlet.operations;

import jmri.util.JUnitUtil;
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
    public void setUp() {
        JUnitUtil.setUp();

    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
