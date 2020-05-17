package apps.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * SystemConsoleConfigPanelXmlTest.java
 *
 * Test for the SystemConsoleConfigPanelXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class SystemConsoleConfigPanelXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("SystemConsoleConfigPanelXml constructor",new SystemConsoleConfigPanelXml());
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

