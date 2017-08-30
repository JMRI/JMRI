package jmri.jmrix.jmriclient.json.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * JsonNetworkConnectionConfigXmlTest.java
 *
 * Description: tests for the JsonNetworkConnectionConfigXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class JsonNetworkConnectionConfigXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("JsonNetworkConnectionConfigXml constructor",new JsonNetworkConnectionConfigXml());
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

