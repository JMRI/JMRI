package jmri.jmrit.whereused;

import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Tests for the ReporterWhereUsed Class
 *
 * @author Dave Sand Copyright (C) 2020
 */
public class ReporterWhereUsedTest {

    @Test
    public void testReporterWhereUsed() {
        ReporterWhereUsed ctor = new ReporterWhereUsed();
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
