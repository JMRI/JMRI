package jmri.web.servlet.about;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.web.servlet.about.AboutServlet class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class AboutServletTest {

    @Test
    public void testCtor() {
        AboutServlet a = new AboutServlet();
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
