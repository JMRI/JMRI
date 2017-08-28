package jmri.jmrix.loconet.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * LnReporterManagerXmlTest.java
 *
 * Description: tests for the LnReporterManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class LnReporterManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("LnReporterManagerXml constructor",new LnReporterManagerXml());
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

