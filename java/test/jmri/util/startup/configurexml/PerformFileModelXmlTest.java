package jmri.util.startup.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * PerformFileModelXmlTest.java
 *
 * Test for the PerformFileModelXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class PerformFileModelXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("PerformFileModelXml constructor",new PerformFileModelXml());
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

