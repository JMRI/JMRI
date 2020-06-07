package jmri.jmrit.display.layoutEditor.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author   Paul Bender  Copyright (C) 2016
 * @author   Bob Jacobsen Copyright (C) 2020
 */
public class PositionablePointViewXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("PositionablePointXml constructor",new PositionablePointViewXml());
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

