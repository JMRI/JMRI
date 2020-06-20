package jmri.jmrit.whereused;

import jmri.util.JUnitUtil;

import java.util.List;

import org.junit.*;

import jmri.NamedBean;
import jmri.NamedBeanUsageReport;
import jmri.jmrit.logix.OBlock;

/**
 * Tests for the OBlockWhereUsed Class
 *
 * @author Dave Sand Copyright (C) 2020
 */
public class OBlockWhereUsedTest {

    @Test
    public void testOBlockWhereUsed() {
        OBlockWhereUsed ctor = new OBlockWhereUsed();
        Assert.assertNotNull("exists", ctor);
        
        // Pay the ransom to free PR#8715 to be merged
        OBlock b = new OBlock("OB1");
        List<NamedBeanUsageReport> list = b.getUsageReport(b);
        Assert.assertNotNull("exists", list);
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
