package jmri.jmrit.whereused;

import jmri.util.JUnitUtil;
import org.junit.*;

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

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
