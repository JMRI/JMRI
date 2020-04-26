package jmri.jmrix.sprog.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

