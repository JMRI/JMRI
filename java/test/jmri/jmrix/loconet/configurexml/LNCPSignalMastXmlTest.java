package jmri.jmrix.loconet.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * LNCPSignalMastXmlTest.java
 *
 * Test for the LNCPSignalMastXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class LNCPSignalMastXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("LNCPSignalMastXml constructor",new LNCPSignalMastXml());
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

