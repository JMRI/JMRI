package apps.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * PerformFileModelXmlTest.java
 *
 * Description: tests for the PerformFileModelXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class PerformFileModelXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("PerformFileModelXml constructor",new PerformFileModelXml());
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

