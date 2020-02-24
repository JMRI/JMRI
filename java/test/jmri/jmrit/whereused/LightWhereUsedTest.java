package jmri.jmrit.whereused;

import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Tests for the LightWhereUsed Class
 *
 * @author Dave Sand Copyright (C) 2020
 */
public class LightWhereUsedTest {

    @Test
    public void testTurnoutWhereUsed() {
        LightWhereUsed ctor = new LightWhereUsed();
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
