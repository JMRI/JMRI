package jmri.jmrix.loconet.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

