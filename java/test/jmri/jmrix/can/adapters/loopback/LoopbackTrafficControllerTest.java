package jmri.jmrix.can.adapters.loopback;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * Tests for LoopbackTrafficController.
 * @author Paul Bender Copyright (C) 2016
 */
public class LoopbackTrafficControllerTest extends jmri.jmrix.can.TrafficControllerTest {
   
    @Override
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp(); 
        JUnitUtil.resetInstanceManager();
        tc = new LoopbackTrafficController();
    }

    @Override
    @After
    public void tearDown(){
       tc = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
 
    }

}
