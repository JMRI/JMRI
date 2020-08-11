package jmri.web.servlet.panel;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();

    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
