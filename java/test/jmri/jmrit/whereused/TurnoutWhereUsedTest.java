package jmri.jmrit.whereused;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the TurnoutWhereUsed Class
 *
 * @author Dave Sand Copyright (C) 2020
 */
public class TurnoutWhereUsedTest {

    @Test
    public void testTurnoutWhereUsed() {
        TurnoutWhereUsed ctor = new TurnoutWhereUsed();
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
