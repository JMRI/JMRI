package jmri.jmrit.symbolicprog.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * ProgrammerConfigPaneXmlTest.java
 *
 * Description: tests for the ProgrammerConfigPaneXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class ProgrammerConfigPaneXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("ProgrammerConfigPaneXml constructor",new ProgrammerConfigPaneXml());
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

