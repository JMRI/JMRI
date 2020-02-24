package jmri.jmrit.whereused;

import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Tests for the MemoryWhereUsed Class
 *
 * @author Dave Sand Copyright (C) 2020
 */
public class MemoryWhereUsedTest {

    @Test
    public void testMemoryWhereUsed() {
        MemoryWhereUsed ctor = new MemoryWhereUsed();
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
