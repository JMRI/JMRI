package jmri.jmrit.display.controlPanelEditor.shape.configurexml;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * PositionableRectangleXmlTest.java
 *
 * Description: tests for the PositionableRectangleXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class PositionableRectangleXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("PositionableRectangleXml constructor",new PositionableRectangleXml());
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

