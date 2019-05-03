package jmri.util;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import jmri.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class LocoAddressComparatorTest {

    @Test
    public void testCTor() {
        LocoAddressComparator t = new LocoAddressComparator();
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testEquals() {
        LocoAddressComparator t = new LocoAddressComparator();
        
        LocoAddress l1 = new DccLocoAddress(200, true);
        LocoAddress l2 = new DccLocoAddress(200, true);
        LocoAddress l3 = new DccLocoAddress(300, true);
        LocoAddress l4 = new DccLocoAddress(30, true);
        LocoAddress l5 = new DccLocoAddress(30, false);
        
        Assert.assertEquals("200, true == 200, true", 0, t.compare(l1, l2));
        Assert.assertEquals("200, true < 300, true", -1, t.compare(l1, l3));
        Assert.assertEquals("300, true > 200, true", +1, t.compare(l3, l2));
        
        Assert.assertEquals("30, true < 30, false", -1, t.compare(l4, l5));
        Assert.assertEquals("30, false > 30, true", +1, t.compare(l5, l4));
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

    // private final static Logger log = LoggerFactory.getLogger(LocoAddressComparatorTest.class);

}
