package jmri;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the NamedBeanUsageReport Class
 * @author Dave Sand Copyright (C) 2020
 */
public class NamedBeanUsageReportTest {

    @Test
    public void testReport() {
        NamedBeanUsageReport report = new NamedBeanUsageReport("Test_Bean_Report");
        Assert.assertNotNull("exists", report);
        Assert.assertEquals("Test_Bean_Report", report.usageKey);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
