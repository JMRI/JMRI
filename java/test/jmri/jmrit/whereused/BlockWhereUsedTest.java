package jmri.jmrit.whereused;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
