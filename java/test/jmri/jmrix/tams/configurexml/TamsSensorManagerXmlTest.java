package jmri.jmrix.tams.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * TamsSensorManagerXmlTest.java
 *
 * Test for the TamsSensorManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class TamsSensorManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("TamsSensorManagerXml constructor",new TamsSensorManagerXml());
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

