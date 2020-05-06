package jmri.jmrix.internal.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * InternalLightManagerXmlTest.java
 *
 * Test for the InternalLightManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class InternalLightManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("InternalLightManagerXml constructor",new InternalLightManagerXml());
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

