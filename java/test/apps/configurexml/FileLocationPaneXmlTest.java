package apps.configurexml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * FileLocationPaneXmlTest.java
 *
 * Test for the FileLocationPaneXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class FileLocationPaneXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("FileLocationPaneXml constructor",new FileLocationPaneXml());
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

