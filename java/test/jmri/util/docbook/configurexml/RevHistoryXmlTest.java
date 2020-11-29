package jmri.util.docbook.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
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
      Assert.assertNotNull("RevHistoryXml constructor",new RevHistoryXml());
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

