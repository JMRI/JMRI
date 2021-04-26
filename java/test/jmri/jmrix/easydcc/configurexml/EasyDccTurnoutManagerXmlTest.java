package jmri.jmrix.easydcc.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the EasyDccTurnoutManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class EasyDccTurnoutManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("EasyDccTurnoutManagerXml constructor", new EasyDccTurnoutManagerXml());
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

