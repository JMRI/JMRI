package jmri.jmrit.display.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * ReporterIconXmlTest.java
 *
 * Test for the ReporterIconXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class ReporterIconXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("ReporterIconXml constructor",new ReporterIconXml());
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

