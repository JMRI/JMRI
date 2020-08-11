package jmri.jmrix.xpa.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * XpaTurnoutManagerXmlTest.java
 *
 * Test for the XpaTurnoutManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class XpaTurnoutManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("XpaTurnoutManagerXml constructor",new XpaTurnoutManagerXml());
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

