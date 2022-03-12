package jmri.jmrix.ecos.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * EcosTurnoutManagerXmlTest.java
 *
 * Test for the EcosTurnoutManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class EcosTurnoutManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("EcosTurnoutManagerXml constructor",new EcosTurnoutManagerXml());
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

