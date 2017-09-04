package jmri.jmrix.xpa.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * XpaTurnoutManagerXmlTest.java
 *
 * Description: tests for the XpaTurnoutManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class XpaTurnoutManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("XpaTurnoutManagerXml constructor",new XpaTurnoutManagerXml());
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

