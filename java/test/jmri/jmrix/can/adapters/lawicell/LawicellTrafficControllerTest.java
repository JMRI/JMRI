package jmri.jmrix.can.adapters.lawicell;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * Tests for LawicellTrafficController.
 * @author Paul Bender Copyright (C) 2016
 */
public class LawicellTrafficControllerTest extends jmri.jmrix.can.TrafficControllerTest {
   
    @Override
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp(); 
        JUnitUtil.resetInstanceManager();
        tc = new LawicellTrafficController();
    }

    @Override
    @After
    public void tearDown(){
        tc = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
 
    }

}
