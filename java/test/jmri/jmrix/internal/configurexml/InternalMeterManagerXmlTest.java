package jmri.jmrix.internal.configurexml;

import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jmri.util.JUnitUtil;

/**
 * InternalMeterManagerXmlTest.java
 *
 * Test for the InternalLightManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class InternalMeterManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("InternalMeterManagerXml constructor",new InternalMeterManagerXml());
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

