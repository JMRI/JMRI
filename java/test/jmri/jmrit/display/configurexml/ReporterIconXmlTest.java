package jmri.jmrit.display.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

