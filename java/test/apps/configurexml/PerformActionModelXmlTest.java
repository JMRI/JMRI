package apps.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * PerformActionModelXmlTest.java
 *
 * Description: tests for the PerformActionModelXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class PerformActionModelXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("PerformActionModelXml constructor",new PerformActionModelXml());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

