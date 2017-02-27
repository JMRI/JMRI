package jmri.jmrix.lenz;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * Tests for XNetTrafficController
 *
 * @author Bob Jacobsen Copyright (C) 2002
 * @author Paul Bender Copyright (C) 2016
 */
public class XNetTrafficControllerTest extends jmri.jmrix.AbstractMRTrafficControllerTest {

    @Before
    @Override
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
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
        JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }


}
