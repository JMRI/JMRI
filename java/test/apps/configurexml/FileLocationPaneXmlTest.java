package apps.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

