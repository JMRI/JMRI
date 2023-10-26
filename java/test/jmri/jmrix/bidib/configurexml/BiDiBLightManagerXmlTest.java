package jmri.jmrix.bidib.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the BiDiBLightManagerXml class
 * 
 * @author  Eckart Meyer  Copyright (C) 2020
 */
public class BiDiBLightManagerXmlTest {
    
    @Test
    public void testCtor(){
      Assert.assertNotNull("BiDiBLightManagerXml constructor",new BiDiBLightManagerXml());
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
