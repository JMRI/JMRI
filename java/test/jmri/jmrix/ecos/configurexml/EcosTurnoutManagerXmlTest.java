package jmri.jmrix.ecos.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * EcosTurnoutManagerXmlTest.java
 *
 * Test for the EcosTurnoutManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class EcosTurnoutManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("EcosTurnoutManagerXml constructor",new EcosTurnoutManagerXml());
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

