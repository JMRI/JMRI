package jmri.jmrix.bidib.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the BiDiBReporterManagerXml class
 * 
 * @author  Eckart Meyer  Copyright (C) 2020
 */
public class BiDiBReporterManagerXmlTest {
    
    @Test
    public void testCtor(){
      Assert.assertNotNull("BiDiBReporterManagerXml constructor",new BiDiBReporterManagerXml());
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
