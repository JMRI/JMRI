package jmri.implementation.configurexml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * SE8cSignalHeadXmlTest.java
 *
 * Test for the SE8cSignalHeadXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class SE8cSignalHeadXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("SE8cSignalHeadXml constructor",new SE8cSignalHeadXml());
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

