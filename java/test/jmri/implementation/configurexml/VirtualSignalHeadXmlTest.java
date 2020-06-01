package jmri.implementation.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * VirtualSignalHeadXmlTest.java
 *
 * Test for the VirtualSignalHeadXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class VirtualSignalHeadXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("VirtualSignalHeadXml constructor",new VirtualSignalHeadXml());
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

