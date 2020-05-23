package jmri.jmrix.internal.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * InternalTurnoutManagerXmlTest.java
 *
 * Test for the InternalTurnoutManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class InternalTurnoutManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("InternalTurnoutManagerXml constructor",new InternalTurnoutManagerXml());
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

