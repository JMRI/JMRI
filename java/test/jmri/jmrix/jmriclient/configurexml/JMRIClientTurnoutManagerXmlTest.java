package jmri.jmrix.jmriclient.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * JMRIClientTurnoutManagerXmlTest.java
 *
 * Test for the JMRIClientTurnoutManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class JMRIClientTurnoutManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("JMRIClientTurnoutManagerXml constructor",new JMRIClientTurnoutManagerXml());
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

