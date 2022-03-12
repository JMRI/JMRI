package jmri.web.servlet.roster;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();

    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
