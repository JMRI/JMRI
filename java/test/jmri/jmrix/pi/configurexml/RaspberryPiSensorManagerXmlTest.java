package jmri.jmrix.pi.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the RaspberryPiSensorManagerXml class.
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class RaspberryPiSensorManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("RaspberryPiSensorManagerXml constructor", new RaspberryPiSensorManagerXml());
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

