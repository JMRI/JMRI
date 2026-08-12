package apps.startup.configurexml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * StartupActionsPreferencesPanelXmlTest.java
 *
 * Test for the StartupActionsPreferencesPanelXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class StartupActionsPreferencesPanelXmlTest {

    @Test
    public void testCtor(){
      Assertions.assertNotNull(new StartupActionsPreferencesPanelXml(), "StartupActionsPreferencesPanelXml constructor");
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

