package jmri.implementation;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class DefaultMemoryTest {

    @Test
    public void testCTor() {
        DefaultMemory t = new DefaultMemory("Test Memory");
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testSystemNames() {
        DefaultMemory myMemory_1 = new DefaultMemory("IM1");
        DefaultMemory myMemory_2 = new DefaultMemory("IM01");
        Assert.assertEquals("Memory system name is correct", "IM1", myMemory_1.getSystemName());
        Assert.assertEquals("Memory system name is correct", "IM01", myMemory_2.getSystemName());
    }
    
    @Test
    public void testCompareTo() {
        DefaultMemory myMemory_1 = new DefaultMemory("IM1");
        DefaultMemory myMemory_2 = new DefaultMemory("IM01");
        Assert.assertNotEquals("Memory's are different", myMemory_1, myMemory_2);
        Assert.assertNotEquals("Memory compareTo returns not zero", 0, myMemory_1.compareTo(myMemory_2));
    }
    
    @Test
    public void testCompareSystemNameSuffix() {
        DefaultMemory myMemory_1 = new DefaultMemory("IM1");
        DefaultMemory myMemory_2 = new DefaultMemory("IM01");
        Assert.assertEquals("compareSystemNameSuffix returns correct value",
                -1, myMemory_1.compareSystemNameSuffix("01", "1", myMemory_2));
        Assert.assertEquals("compareSystemNameSuffix returns correct value",
                0, myMemory_1.compareSystemNameSuffix("1", "1", myMemory_2));
        Assert.assertEquals("compareSystemNameSuffix returns correct value",
                0, myMemory_1.compareSystemNameSuffix("01", "01", myMemory_2));
        Assert.assertEquals("compareSystemNameSuffix returns correct value",
                +1, myMemory_1.compareSystemNameSuffix("1", "01", myMemory_2));
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

    // private final static Logger log = LoggerFactory.getLogger(DefaultMemoryTest.class);

}
