package apps.configurexml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * GuiLafConfigPaneXmlTest.java
 *
 * Test for the GuiLafConfigPaneXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class GuiLafConfigPaneXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("GuiLafConfigPaneXml constructor",new GuiLafConfigPaneXml());
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

