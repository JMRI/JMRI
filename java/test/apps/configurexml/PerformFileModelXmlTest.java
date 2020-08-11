package apps.configurexml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * PerformFileModelXmlTest.java
 *
 * Test for the PerformFileModelXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class PerformFileModelXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("PerformFileModelXml constructor",new PerformFileModelXml());
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

