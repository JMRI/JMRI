package jmri.jmrit.whereused;

import jmri.util.JUnitUtil;
import org.junit.*;

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

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
