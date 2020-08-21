package jmri.web.servlet.panel;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.web.servlet.panel.LayoutPanelServlet class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class LayoutPanelServletTest {

    @Test
    public void testCtor() {
        LayoutPanelServlet a = new LayoutPanelServlet();
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
