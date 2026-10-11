package apps.configurexml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

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
        Assertions.assertNotNull(new FileLocationPaneXml(), "FileLocationPaneXml constructor");
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

