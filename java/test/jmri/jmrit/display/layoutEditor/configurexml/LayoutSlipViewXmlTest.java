package jmri.jmrit.display.layoutEditor.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author   George Warner  Copyright (C) 2017
 * @author   Bob Jacobsen Copyright (C) 2020
 */
public class LayoutSlipViewXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("LayoutSlipXml constructor", new LayoutSlipViewXml());
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
