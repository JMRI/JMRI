package jmri.implementation.configurexml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * TripleOutputSignalHeadXmlTest.java
 *
 * Test for the TripleOutputSignalHeadXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class TripleOutputSignalHeadXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("TripleOutputSignalHeadXml constructor",new TripleOutputSignalHeadXml());
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

