package jmri.jmrix.loconet;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.loconet.LnSensor class.
 *
 * @author	Bob Jacobsen Copyright 2001, 2002
 */
public class LnSensorTest extends jmri.implementation.AbstractSensorTestBase {

    private LocoNetInterfaceScaffold lnis = null;

    @Override
    public int numListeners() {return lnis.numListeners();}

    @Override
    public void checkOnMsgSent() {}

    @Override
    public void checkOffMsgSent() {}

    @Override
    public void checkStatusRequestMsgSent() {
        // doesn't send a message right now, pending figuring out what
        // to send.
    }
    
    // LnSensor test for incoming status message
    @Test
    public void testLnSensorStatusMsg() {
        // create a new unregistered sensor.
        LnSensor s = new LnSensor("LS044", lnis, "L");
        LocoNetMessage m;

        // notify the Ln that somebody else changed it...
        m = new LocoNetMessage(4);
        m.setOpCode(0xb2);         // OPC_INPUT_REP
        m.setElement(1, 0x15);     // all but lowest bit of address
        m.setElement(2, 0x60);     // Aux (low addr bit high), sensor low
        m.setElement(3, 0x38);
        s.messageFromManager(m);
        Assert.assertEquals("Known state after inactivate ", jmri.Sensor.INACTIVE, s.getKnownState());

        m = new LocoNetMessage(4);
        m.setOpCode(0xb2);         // OPC_INPUT_REP
        m.setElement(1, 0x15);     // all but lowest bit of address
        m.setElement(2, 0x70);     // Aux (low addr bit high), sensor high
        m.setElement(3, 0x78);
        s.messageFromManager(m);
        Assert.assertEquals("Known state after activate ", jmri.Sensor.ACTIVE, s.getKnownState());
    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        // prepare an interface the sensor t, is unregistered so
        // we must feedback the message manually
        lnis = new LocoNetInterfaceScaffold() {
            @Override
            public void sendLocoNetMessage(LocoNetMessage m) {
                log.debug("sendLocoNetMessage [{}]", m);
                // save a copy
                outbound.addElement(m);
                ((LnSensor)t).messageFromManager(m);
            }
        };
        t = new LnSensor("LS042", lnis, "L");
    }

    @After
    @Override
    public void tearDown() {
        t.dispose();
	    lnis = null;
        JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(LnSensorTest.class);
    
}
