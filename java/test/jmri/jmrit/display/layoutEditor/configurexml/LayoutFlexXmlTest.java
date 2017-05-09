package jmri.jmrit.display.layoutEditor.configurexml;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * LayoutSlipXmlTest.java
 *
 * Description: tests for the LayoutSlipXml class
 *
 * @author   George Warner  Copyright (C) 2017
 */
public class LayoutFlexXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("LayoutFlexXml constructor",new LayoutFlexXml());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}

