package jmri.jmrit.whereused;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
