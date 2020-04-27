package jmri.jmrix.nce.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * NceLightManagerXmlTest.java
 *
 * Test for the NceLightManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class NceLightManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("NceLightManagerXml constructor",new NceLightManagerXml());
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

