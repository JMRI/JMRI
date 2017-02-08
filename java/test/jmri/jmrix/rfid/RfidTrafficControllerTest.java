package jmri.jmrix.rfid;

import org.junit.After;
import org.junit.Before;

/**
 * RfidTrafficControllerTest.java
 *
 * Description:	tests for the jmri.jmrix.rfid.RfidTrafficController class
 *
 * @author	Paul Bender Copyright (C) 2012,2016
 */
public class RfidTrafficControllerTest extends jmri.jmrix.AbstractMRTrafficControllerTest {

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        tc = new RfidTrafficController(){
           @Override
           public void sendInitString(){
           }
        };
    }

    @Override
    @After
    public void tearDown() {
        tc = null;
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

}
