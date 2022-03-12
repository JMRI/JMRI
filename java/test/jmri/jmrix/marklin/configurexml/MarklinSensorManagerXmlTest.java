package jmri.jmrix.marklin.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * MarklinSensorManagerXmlTest.java
 *
 * Test for the MarklinSensorManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class MarklinSensorManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("MarklinSensorManagerXml constructor",new MarklinSensorManagerXml());
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

