package jmri.jmrix.acela.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * AcelaTurnoutManagerXmlTest.java
 *
 * Test for the AcelaTurnoutManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class AcelaTurnoutManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("AcelaTurnoutManagerXml constructor",new AcelaTurnoutManagerXml());
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

