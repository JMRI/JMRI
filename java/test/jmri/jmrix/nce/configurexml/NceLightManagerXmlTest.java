package jmri.jmrix.nce.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * NceLightManagerXmlTest.java
 *
 * Test for the NceLightManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class NceLightManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("NceLightManagerXml constructor",new NceLightManagerXml());
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

