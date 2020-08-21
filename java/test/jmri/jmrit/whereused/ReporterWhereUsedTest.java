package jmri.jmrit.whereused;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
