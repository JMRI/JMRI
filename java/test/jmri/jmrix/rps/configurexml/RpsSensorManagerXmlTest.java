package jmri.jmrix.rps.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * RpsSensorManagerXmlTest.java
 *
 * Test for the RpsSensorManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class RpsSensorManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("RpsSensorManagerXml constructor",new RpsSensorManagerXml());
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

