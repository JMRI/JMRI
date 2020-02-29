package jmri.jmrit.whereused;

import jmri.util.JUnitUtil;
import org.junit.*;

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

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
