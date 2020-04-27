package jmri.implementation.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * QuadOutputSignalHeadXmlTest.java
 *
 * Test for the QuadOutputSignalHeadXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class QuadOutputSignalHeadXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("QuadOutputSignalHeadXml constructor",new QuadOutputSignalHeadXml());
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

