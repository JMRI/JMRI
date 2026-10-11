package jmri.util.docbook.configurexml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * RevHistoryXmlTest.java
 *
 * Test for the RevHistoryXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class RevHistoryXmlTest {

    @Test
    public void testCtor(){
      Assertions.assertNotNull( new RevHistoryXml(), "RevHistoryXml constructor");
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

