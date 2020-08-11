package apps.startup.configurexml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

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
      Assert.assertNotNull("StartupActionsPreferencesPanelXml constructor",new StartupActionsPreferencesPanelXml());
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

