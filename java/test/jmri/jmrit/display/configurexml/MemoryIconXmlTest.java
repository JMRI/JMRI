package jmri.jmrit.display.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * MemoryIconXmlTest.java
 *
 * Test for the MemoryIconXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class MemoryIconXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("MemoryIconXml constructor",new MemoryIconXml());
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

