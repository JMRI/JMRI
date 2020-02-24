package jmri.jmrit.whereused;

import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Tests for the EntryExitWhereUsed Class
 *
 * @author Dave Sand Copyright (C) 2020
 */
public class EntryExitWhereUsedTest {

    @Test
    public void testEntryExitWhereUsed() {
        EntryExitWhereUsed ctor = new EntryExitWhereUsed();
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
