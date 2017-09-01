package apps.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * CreateButtonModelXmlTest.java
 *
 * Description: tests for the CreateButtonModelXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class CreateButtonModelXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("CreateButtonModelXml constructor",new CreateButtonModelXml());
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

