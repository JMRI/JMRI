package jmri.jmrix.jmriclient.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

