package jmri.implementation.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * SingleTurnoutSignalHeadXmlTest.java
 *
 * Test for the SingleTurnoutSignalHeadXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class SingleTurnoutSignalHeadXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("SingleTurnoutSignalHeadXml constructor",new SingleTurnoutSignalHeadXml());
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

