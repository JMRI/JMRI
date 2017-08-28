package jmri.jmrit.operations.trains.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * TrainIconXmlTest.java
 *
 * Description: tests for the TrainIconXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class TrainIconXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("TrainIconXml constructor",new TrainIconXml());
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

