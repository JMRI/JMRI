package jmri.jmrix.acela.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * AcelaTurnoutManagerXmlTest.java
 *
 * Test for the AcelaTurnoutManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class AcelaTurnoutManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("AcelaTurnoutManagerXml constructor",new AcelaTurnoutManagerXml());
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

