package apps.startup.configurexml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

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
        Assertions.assertNotNull(new TriggerRouteModelXml(), "TriggerRouteModelXml constructor");
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

