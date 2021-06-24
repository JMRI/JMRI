package jmri.web.servlet.help;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.web.servlet.help.HelpSSIServlet class
 *
 * @author Paul Bender       Copyright (C) 2012,2016
 * @author Daniel Bergqvist  Copyright (C) 2021
 */
public class HelpSSIServletTest {

    @Test
    public void testCtor() {
        HelpSSIServlet a = new HelpSSIServlet();
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
