package jmri.jmrix.rps.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * RpsReporterManagerXmlTest.java
 *
 * Test for the RpsReporterManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class RpsReporterManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("RpsReporterManagerXml constructor",new RpsReporterManagerXml());
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

