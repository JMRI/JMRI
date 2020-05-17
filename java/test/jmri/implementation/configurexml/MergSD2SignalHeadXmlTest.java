package jmri.implementation.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * MergSD2SignalHeadXmlTest.java
 *
 * Test for the MergSD2SignalHeadXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class MergSD2SignalHeadXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("MergSD2SignalHeadXml constructor",new MergSD2SignalHeadXml());
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

