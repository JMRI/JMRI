package jmri.implementation.configurexml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

