package jmri.jmrix.bidib.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the BiDiBSignalMastXml class
 * 
 * @author  Eckart Meyer  Copyright (C) 2020
 */
public class BiDiBSignalMastXmlTest {
    
    @Test
    public void testCtor(){
      Assert.assertNotNull("BiDiBSignalMastXml constructor",new BiDiBSignalMastXml());
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
