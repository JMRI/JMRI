package jmri.jmrit.whereused;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the SectionWhereUsed Class
 *
 * @author Dave Sand Copyright (C) 2020
 */
public class SectionWhereUsedTest {

    @Test
    public void testSectionWhereUsed() {
        SectionWhereUsed ctor = new SectionWhereUsed();
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
