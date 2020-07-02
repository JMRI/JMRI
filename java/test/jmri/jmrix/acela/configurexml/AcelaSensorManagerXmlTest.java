package jmri.jmrix.acela.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * AcelaSensorManagerXmlTest.java
 *
 * Test for the AcelaSensorManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class AcelaSensorManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("AcelaSensorManagerXml constructor",new AcelaSensorManagerXml());
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

