package jmri.jmrit.whereused;

import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Tests for the OBlockWhereUsed Class
 *
 * @author Dave Sand Copyright (C) 2020
 */
public class OBlockWhereUsedTest {

    @Test
    public void testOBlockWhereUsed() {
        OBlockWhereUsed ctor = new OBlockWhereUsed();
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
