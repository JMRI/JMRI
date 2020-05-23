package jmri.jmrix.dccpp.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * DCCppTurnoutManagerXmlTest.java
 *
 * Test for the DCCppTurnoutManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class DCCppTurnoutManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("DCCppTurnoutManagerXml constructor",new DCCppTurnoutManagerXml());
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

