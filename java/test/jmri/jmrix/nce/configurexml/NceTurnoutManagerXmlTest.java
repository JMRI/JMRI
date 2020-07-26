package jmri.jmrix.nce.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * NceTurnoutManagerXmlTest.java
 *
 * Test for the NceTurnoutManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class NceTurnoutManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("NceTurnoutManagerXml constructor",new NceTurnoutManagerXml());
    }

    @Test
    public void testInvalidLoad(){
       NceTurnoutManagerXml ntmx = new NceTurnoutManagerXml();
       ntmx.load(new org.jdom2.Element("test"),ntmx);
       jmri.util.JUnitAppender.assertErrorMessage("Invalid method called");
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

