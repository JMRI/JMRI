package jmri.jmrit.whereused;

import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Tests for the BlockWhereUsed Class
 *
 * @author Dave Sand Copyright (C) 2020
 */
public class BlockWhereUsedTest {

    @Test
    public void testBlockWhereUsed() {
        BlockWhereUsed ctor = new BlockWhereUsed();
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
