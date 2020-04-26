package apps.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * PerformScriptModelXmlTest.java
 *
 * Test for the PerformScriptModelXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class PerformScriptModelXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("PerformScriptModelXml constructor",new PerformScriptModelXml());
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

