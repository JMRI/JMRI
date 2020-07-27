package jmri.implementation.configurexml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

