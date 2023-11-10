package jmri.jmrix.bidib;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import jmri.util.JUnitUtil;
import org.junit.Assert;

/**
 * Tests for the BiDiBConnectionTypeList class
 *
 * @author  Eckart Meyer  Copyright (C) 2020
 */
public class BiDiBConnectionTypeListTest {
    
    @Test
    public void testCtor() {

        BiDiBConnectionTypeList c = new BiDiBConnectionTypeList();
        Assert.assertNotNull(c);
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
