package jmri.jmrit.revhistory.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * FileHistoryXmlTest.java
 *
 * Test for the FileHistoryXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class FileHistoryXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("FileHistoryXml constructor",new FileHistoryXml());
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

