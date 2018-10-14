package jmri.jmrix.internal.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * InternalReporterManagerXmlTest.java
 *
 * Description: tests for the InternalReporterManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class InternalReporterManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("InternalReporterManagerXml constructor",new InternalReporterManagerXml());
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

