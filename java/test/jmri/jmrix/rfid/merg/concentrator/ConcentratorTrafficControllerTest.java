package jmri.jmrix.rfid.merg.concentrator;

import jmri.jmrix.rfid.RfidSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * ConcentratorTrafficControllerTest.java
 *
 * Test for the ConcentratorTrafficController class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class ConcentratorTrafficControllerTest extends jmri.jmrix.rfid.RfidTrafficControllerTest {

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        tc = new ConcentratorTrafficController(new RfidSystemConnectionMemo(),"A-H"){
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
