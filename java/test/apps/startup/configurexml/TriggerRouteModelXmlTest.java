package apps.startup.configurexml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * TriggerRouteModelXmlTest.java
 *
 * Test for the TriggerRouteModelXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class TriggerRouteModelXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("TriggerRouteModelXml constructor",new TriggerRouteModelXml());
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

