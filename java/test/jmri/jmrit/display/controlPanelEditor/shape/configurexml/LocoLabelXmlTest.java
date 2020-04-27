package jmri.jmrit.display.controlPanelEditor.shape.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * LocoLabelXmlTest.java
 *
 * Test for the LocoLabelXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class LocoLabelXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("LocoLabelXml constructor",new LocoLabelXml());
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

