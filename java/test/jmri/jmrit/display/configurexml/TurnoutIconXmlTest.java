package jmri.jmrit.display.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * TurnoutIconXmlTest.java
 *
 * Test for the TurnoutIconXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class TurnoutIconXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("TurnoutIconXml constructor",new TurnoutIconXml());
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

