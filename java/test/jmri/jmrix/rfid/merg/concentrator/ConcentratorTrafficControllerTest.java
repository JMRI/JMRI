package jmri.jmrix.rfid.merg.concentrator;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * ConcentratorTrafficControllerTest.java
 *
 * Description:	tests for the ConcentratorTrafficController class
 *
 * @author	Paul Bender Copyright (C) 2012,2016
 */
public class ConcentratorTrafficControllerTest extends jmri.jmrix.rfid.RfidTrafficControllerTest {

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        tc = new ConcentratorTrafficController(new ConcentratorSystemConnectionMemo(),"A-H"){
           @Override
           public void sendInitString(){
           }
        };
    }

    @Override
    @After
    public void tearDown() {
        tc = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

}
