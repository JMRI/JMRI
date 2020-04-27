package apps.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * PerformActionModelXmlTest.java
 *
 * Test for the PerformActionModelXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class PerformActionModelXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("PerformActionModelXml constructor",new PerformActionModelXml());
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

