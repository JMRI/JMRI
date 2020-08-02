package jmri.jmrit.display.layoutEditor.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * TrackSegmentXmlTest.java
 *
 * Test for the TrackSegmentXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class TrackSegmentXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("TrackSegmentXml constructor",new TrackSegmentXml());
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

