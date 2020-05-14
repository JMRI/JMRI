package jmri.jmrit.display.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * SlipTurnoutIconXmlTest.java
 *
 * Test for the SlipTurnoutIconXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class SlipTurnoutIconXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("SlipTurnoutIconXml constructor",new SlipTurnoutIconXml());
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

