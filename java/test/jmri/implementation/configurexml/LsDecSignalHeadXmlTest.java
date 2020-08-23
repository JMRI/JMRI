package jmri.implementation.configurexml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * LsDecSignalHeadXmlTest.java
 *
 * Test for the LsDecSignalHeadXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class LsDecSignalHeadXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("LsDecSignalHeadXml constructor",new LsDecSignalHeadXml());
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

