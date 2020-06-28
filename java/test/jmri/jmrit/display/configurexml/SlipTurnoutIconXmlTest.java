package jmri.jmrit.display.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

