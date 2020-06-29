package jmri.jmrit.whereused;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
