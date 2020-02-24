package jmri.jmrit.whereused;

import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Tests for the SignalHeadWhereUsed Class
 *
 * @author Dave Sand Copyright (C) 2020
 */
public class SignalHeadWhereUsedTest {

    @Test
    public void testSignalHeadWhereUsed() {
        SignalHeadWhereUsed ctor = new SignalHeadWhereUsed();
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
