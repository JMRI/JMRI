package jmri.jmrix.can.adapters.loopback;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for LoopbackTrafficController.
 * @author Paul Bender Copyright (C) 2016
 */
public class LoopbackTrafficControllerTest extends jmri.jmrix.can.TrafficControllerTest {
   
    @Override
    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp(); 
        JUnitUtil.resetInstanceManager();
        tc = new LoopbackTrafficController();
    }

    @Override
    @AfterEach
    public void tearDown(){
       tc = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
 
    }

}
