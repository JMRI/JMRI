package jmri.jmrix.ecos;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * EcosTrafficControllerTest.java
 *
 * Test for the EcosTrafficController class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class EcosTrafficControllerTest {

    EcosTrafficController tc = null;

    @Test
    public void testCtor(){
       Assert.assertNotNull(tc);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        tc = new EcosTrafficController();
    }

    @AfterEach
    public void tearDown() {
        tc = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }


}
