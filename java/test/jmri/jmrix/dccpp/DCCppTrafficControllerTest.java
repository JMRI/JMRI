package jmri.jmrix.dccpp;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 *
 * @author Bob Jacobsen Copyright 2002
 * @author Mark Underwood Copyright 2015
 */
public class DCCppTrafficControllerTest extends jmri.jmrix.AbstractMRTrafficControllerTest {

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        tc = new DCCppTrafficController(new DCCppCommandStation()){
            @Override
            public void sendDCCppMessage(DCCppMessage m, DCCppListener reply){
            }
        };

    }

    @Override
    @After
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

}
