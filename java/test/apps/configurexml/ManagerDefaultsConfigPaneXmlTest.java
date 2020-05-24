package apps.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * ManagerDefaultsConfigPaneXmlTest.java
 *
 * Test for the ManagerDefaultsConfigPaneXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class ManagerDefaultsConfigPaneXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("ManagerDefaultsConfigPaneXml constructor",new ManagerDefaultsConfigPaneXml());
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

