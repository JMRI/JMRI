package jmri.jmrix.rfid;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * RfidTrafficControllerTest.java
 *
 * Test for the jmri.jmrix.rfid.RfidTrafficController class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class RfidTrafficControllerTest extends jmri.jmrix.AbstractMRTrafficControllerTest {

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        tc = new RfidTrafficController(){
           @Override
           public void sendInitString(){
           }
        };
    }

    @Override
    @AfterEach
    public void tearDown() {
        tc = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

}
