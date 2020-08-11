package jmri.jmrix.roco.z21.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Z21XNetTurnoutManagerXml.java
 *
 * Test for the Z21XNetTurnoutManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class Z21XNetTurnoutManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("Z21XNetTurnoutManagerXml constructor",new Z21XNetTurnoutManagerXml());
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

