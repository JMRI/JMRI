package jmri.jmrit.display.layoutEditor.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * @author   Paul Bender  Copyright (C) 2016
 * @author   Bob Jacobsen Copyright (C) 2020
 */
public class LayoutTurntableViewXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("LayoutTurntableXml constructor", new LayoutTurntableViewXml());
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

