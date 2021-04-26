package jmri.jmrix.rfid.generic.standalone;

import jmri.jmrix.rfid.RfidSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * StandaloneTrafficControllerTest.java
 *
 * Test for the StandaloneTrafficController class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class StandaloneTrafficControllerTest extends jmri.jmrix.rfid.RfidTrafficControllerTest {

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        tc = new StandaloneTrafficController(new RfidSystemConnectionMemo());
    }

    @Override
    @AfterEach
    public void tearDown() {
        tc = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }
}
