package jmri.jmrit.revhistory.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

