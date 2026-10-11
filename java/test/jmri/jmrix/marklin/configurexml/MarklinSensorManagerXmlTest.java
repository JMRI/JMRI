package jmri.jmrix.marklin.configurexml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * MarklinSensorManagerXmlTest.java
 *
 * Test for the MarklinSensorManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class MarklinSensorManagerXmlTest {

    @Test
    public void testCtor(){
        Assertions.assertNotNull(new MarklinSensorManagerXml(), "MarklinSensorManagerXml constructor");
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

