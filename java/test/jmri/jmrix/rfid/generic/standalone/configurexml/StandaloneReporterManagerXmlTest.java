package jmri.jmrix.rfid.generic.standalone.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * StandaloneReporterManagerXmlTest.java
 *
 * Description: tests for the StandaloneReporterManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class StandaloneReporterManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("StandaloneReporterManagerXml constructor",new StandaloneReporterManagerXml());
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

