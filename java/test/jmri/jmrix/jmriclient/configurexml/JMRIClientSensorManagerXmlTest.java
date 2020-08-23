package jmri.jmrix.jmriclient.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * JMRIClientSensorManagerXmlTest.java
 *
 * Test for the JMRIClientSensorManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class JMRIClientSensorManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("JMRIClientSensorManagerXml constructor",new JMRIClientSensorManagerXml());
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

