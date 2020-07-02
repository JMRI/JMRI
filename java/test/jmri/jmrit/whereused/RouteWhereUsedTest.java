package jmri.jmrit.whereused;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the RouteWhereUsed Class
 *
 * @author Dave Sand Copyright (C) 2020
 */
public class RouteWhereUsedTest {

    @Test
    public void testRouteWhereUsed() {
        RouteWhereUsed ctor = new RouteWhereUsed();
        Assert.assertNotNull("exists", ctor);
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
