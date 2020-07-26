package jmri.jmrix.marklin;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * MarklinTrafficControllerTest.java
 *
 * Test for the jmri.jmrix.marklin.MarklinTrafficController class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class MarklinTrafficControllerTest extends jmri.jmrix.AbstractMRTrafficControllerTest {

    @Override
    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        tc = new MarklinTrafficController();
    }
    
    @Override
    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

}
