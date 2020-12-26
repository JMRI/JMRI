package jmri.jmrit.symbolicprog.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * ProgrammerConfigPaneXmlTest.java
 *
 * Test for the ProgrammerConfigPaneXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class ProgrammerConfigPaneXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("ProgrammerConfigPaneXml constructor",new ProgrammerConfigPaneXml());
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

