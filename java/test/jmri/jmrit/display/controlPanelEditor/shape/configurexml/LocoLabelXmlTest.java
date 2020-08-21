package jmri.jmrit.display.controlPanelEditor.shape.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

