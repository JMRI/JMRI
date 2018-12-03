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
    public void testSystemPrefixTests() {
        SystemNameComparator t = new SystemNameComparator();

        Assert.assertEquals("IS1 < I2S1", -1, t.compare("IS1", "I2S1"));
        Assert.assertEquals("I2S1 > IS1", +1, t.compare("I2S1", "IS1"));

        Assert.assertEquals("I2S1 < I10S1", -1, t.compare("I2S1", "I10S1"));
        Assert.assertEquals("I10S1 > I2S1", +1, t.compare("I10S1", "I2S1"));
    }

    @Test
    public void testTypeLetterTests() {
        SystemNameComparator t = new SystemNameComparator();

        Assert.assertEquals("IS1 < IT1", -1, t.compare("IS1", "IT1"));
        Assert.assertEquals("IT1 > IS1", +1, t.compare("IT1", "IS1"));

        Assert.assertEquals("I2S1 > IT1", +1, t.compare("I2S1", "IT1"));
        Assert.assertEquals("IT1 < I2S1", -1, t.compare("IT1", "I2S1"));
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

    @Test
    public void testLegacyCases() {
        SystemNameComparator t = new SystemNameComparator();

        Assert.assertEquals("same DCCPP100", 0, t.compare("DCCPP100", "DCCPP100"));
        Assert.assertEquals("same DX12",    0, t.compare("DX12", "DX12"));

        // Should sort first by system, then letter, then rest:
        //   Compare DCCPP T 100 to D S 100 - DCCPP>D
        Assert.assertEquals("DCCPPT100 > DS100", +1, t.compare("DCCPPT100", "DS100"));
        Assert.assertEquals("DS100 < DCCPPT100", -1, t.compare("DS100", "DCCPPT100"));

    }


    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SystemNameComparatorTest.class);
}
