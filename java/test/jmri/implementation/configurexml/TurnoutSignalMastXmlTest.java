package jmri.implementation.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * TurnoutSignalMastXmlTest.java
 *
 * Description: tests for the TurnoutSignalMastXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class TurnoutSignalMastXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("TurnoutSignalMastXml constructor",new TurnoutSignalMastXml());
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

