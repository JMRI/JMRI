package jmri.jmrix.ecos.utilities;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * RemoveObjectFromEcosTest.java
 *
 * Description: tests for the RemoveObjectFromEcos class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class RemoveObjectFromEcosTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("RemoveObjectFromEcos constructor",new RemoveObjectFromEcos());
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

