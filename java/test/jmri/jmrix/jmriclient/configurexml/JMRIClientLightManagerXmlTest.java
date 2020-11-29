package jmri.jmrix.jmriclient.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * JMRIClientLightManagerXmlTest.java
 *
 * Test for the JMRIClientLightManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class JMRIClientLightManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("JMRIClientLightManagerXml constructor",new JMRIClientLightManagerXml());
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

