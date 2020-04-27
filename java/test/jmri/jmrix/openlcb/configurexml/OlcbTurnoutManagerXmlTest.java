package jmri.jmrix.openlcb.configurexml;

import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(new OlcbTurnoutManagerXml()).withFailMessage("OlcbTurnoutManagerXml constructor").isNotNull();
    }

    // The minimal setup for log4J
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

