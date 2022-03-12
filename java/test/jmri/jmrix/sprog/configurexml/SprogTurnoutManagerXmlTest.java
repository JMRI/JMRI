package jmri.jmrix.sprog.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * SprogTurnoutManagerXmlTest.java
 *
 * Test for the SprogTurnoutManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class SprogTurnoutManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("SprogTurnoutManagerXml constructor",new SprogTurnoutManagerXml());
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

