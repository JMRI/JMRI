package jmri.jmrix.internal.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * InternalLightManagerXmlTest.java
 *
 * Test for the InternalLightManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class InternalLightManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("InternalLightManagerXml constructor",new InternalLightManagerXml());
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

