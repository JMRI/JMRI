package jmri.jmrix.ecos;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * EcosTrafficControllerTest.java
 *
 * Description:	tests for the EcosTrafficController class
 *
 * @author	Paul Bender Copyright (C) 2012,2016
 */
public class EcosTrafficControllerTest {

    EcosTrafficController tc = null;

    @Test
    public void testCtor(){
       Assert.assertNotNull(tc);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        tc = new EcosTrafficController();
    }

    @After
    public void tearDown() {
        tc = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }


}
