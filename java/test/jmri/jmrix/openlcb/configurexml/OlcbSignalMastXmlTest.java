package jmri.jmrix.openlcb.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * OlcbSignalMastXmlTest
 *
 * Description: tests for the OlcbSignalMastXml class
 *
 * @author   Bob Jacobsen Copyright (C) 2018
 */
public class OlcbSignalMastXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("OlcbSignalMastXml constructor",new OlcbSignalMastXml());
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

