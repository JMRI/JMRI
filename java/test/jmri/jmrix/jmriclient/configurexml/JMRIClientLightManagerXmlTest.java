package jmri.jmrix.jmriclient.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * JMRIClientLightManagerXmlTest.java
 *
 * Description: tests for the JMRIClientLightManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class JMRIClientLightManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("JMRIClientLightManagerXml constructor",new JMRIClientLightManagerXml());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

