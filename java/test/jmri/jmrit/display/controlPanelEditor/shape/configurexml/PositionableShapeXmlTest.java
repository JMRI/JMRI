package jmri.jmrit.display.controlPanelEditor.shape.configurexml;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * PositionableShapeXmlTest.java
 *
 * Description: tests for the PositionableShapeXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class PositionableShapeXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("PositionableShapeXml constructor",new PositionableShapeXml());
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

