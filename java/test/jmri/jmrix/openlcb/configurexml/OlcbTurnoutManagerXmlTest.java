package jmri.jmrix.openlcb.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * OlcbTurnoutManagerXmlTest.java
 *
 * Test for the OlcbTurnoutManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class OlcbTurnoutManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("OlcbTurnoutManagerXml constructor",new OlcbTurnoutManagerXml());
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

