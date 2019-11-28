package jmri.jmrix.lenz;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * Tests for XNetTrafficController.
 *
 * @author Bob Jacobsen Copyright (C) 2002
 * @author Paul Bender Copyright (C) 2016
 */
public class XNetTrafficControllerTest extends jmri.jmrix.AbstractMRTrafficControllerTest {

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        tc = new XNetTrafficController(new LenzCommandStation()){
            @Override
            public void sendXNetMessage(XNetMessage m, XNetListener reply){
            }
        };
    }

    @After
    @Override
    public void tearDown(){
        tc = null;
	    JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

}
