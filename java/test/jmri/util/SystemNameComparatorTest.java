package jmri.util;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SystemNameComparatorTest {

    @Test
    public void testCTor() {
        SystemNameComparator t = new SystemNameComparator();
        Assert.assertNotNull("exists", t);
    }

    @Test
    public void testNumericComparison() {
        SystemNameComparator t = new SystemNameComparator();

        Assert.assertEquals("same IS100", 0, t.compare("IS100", "IS100"));

        Assert.assertEquals("IS100 < IS101", -1, t.compare("IS100", "IS101"));
        Assert.assertEquals("IS101 > IS100", +1, t.compare("IS101", "IS100"));

        Assert.assertEquals("not same IS001 IS1", 0, t.compare("IS1", "IS1")); 
        Assert.assertEquals("not same IS0100 IS100", 0, t.compare("IS100", "IS100"));
        Assert.assertEquals("not same IS100 IS0100", 0, t.compare("IS100", "IS100"));
    }

    @Test
    public void testMixedComparison() {
        SystemNameComparator t = new SystemNameComparator();

        Assert.assertEquals("same IS100A", 0, t.compare("IS100A", "IS100A"));

        Assert.assertEquals("IS100 < IS100A", -1, t.compare("IS100", "IS100A"));
        Assert.assertEquals("IS100A > IS100", +1, t.compare("IS100A", "IS100"));

    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SystemNameComparatorTest.class);
}
