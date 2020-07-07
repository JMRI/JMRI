package jmri.implementation.configurexml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * TripleTurnoutSignalHeadXmlTest.java
 *
 * Test for the TripleTurnoutSignalHeadXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class TripleTurnoutSignalHeadXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("TripleTurnoutSignalHeadXml constructor",new TripleTurnoutSignalHeadXml());
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

