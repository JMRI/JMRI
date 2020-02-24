package jmri.jmrit.whereused;

import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Tests for the SignalMastWhereUsed Class
 *
 * @author Dave Sand Copyright (C) 2020
 */
public class SignalMastWhereUsedTest {

    @Test
    public void testSignalMastWhereUsed() {
        SignalMastWhereUsed ctor = new SignalMastWhereUsed();
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
