package jmri.jmrit.roster.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * RosterConfigPaneXmlTest.java
 *
 * Test for the RosterConfigPaneXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class RosterConfigPaneXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("RosterConfigPaneXml constructor",new RosterConfigPaneXml());
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

