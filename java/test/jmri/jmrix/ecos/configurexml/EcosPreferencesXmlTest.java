package jmri.jmrix.ecos.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * EcosPreferencesXmlTest.java
 *
 * Description: tests for the EcosPreferencesXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class EcosPreferencesXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("EcosPreferencesXml constructor",new EcosPreferencesXml());
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

