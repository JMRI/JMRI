package jmri.web.servlet.operations;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();

    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
