package jmri.jmrit.display.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * MemoryInputIconXmlTest.java
 *
 * Test for the MemoryInputIconXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class MemoryInputIconXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("MemoryInputIconXml constructor",new MemoryInputIconXml());
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

