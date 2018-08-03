package jmri.jmrix.can.cbus;

import jmri.jmrix.can.TestTrafficController;
import jmri.jmrix.can.CanMessage;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2018
 */
public class CbusOpCodesTest {

    private TestTrafficController tc;
    private CbusOpCodes t;
    

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",t);
    }
    
    
    @Test
    public void tASOFTest() {
        CanMessage myMessage = new CanMessage(
            new int[]{0x99, 0x00, 0x00, 0x00, 0x01}, tc.getCanid()
        );
        Assert.assertNotNull("exists",myMessage);
        String messageOutput=t.decode(myMessage);
        // log.debug(" Translated message {} ", messageOutput); 
        Assert.assertNotNull("exists", messageOutput);
    }
    
    
    @Test
    public void OpNotexistTest() {
        CanMessage myMessage = new CanMessage(
            new int[]{0x18, 0x00, 0x00, 0x00, 0x00}, tc.getCanid()
        );
        Assert.assertTrue("Reserved opcode" == t.decode(myMessage));
    }    
    
    
    @Test
    public void NotAllReservedTest() {
        CanMessage myMessage = new CanMessage(
            new int[]{0x98, 0x05, 0x00, 0x01, 0x05}, tc.getCanid()
        );
        Assert.assertFalse("Reserved opcode" == t.decode(myMessage));
        Assert.assertNotNull("exists", t.decode(myMessage));
    }
    
    
    @Test
    public void IsShortTest() {
        CanMessage myMessage = new CanMessage(
            new int[]{0x98, 0x05, 0x00, 0x01, 0x05}, tc.getCanid()
        );
        int opc = CbusMessage.getOpcode(myMessage);
        Assert.assertTrue(t.isShortEvent(opc));
    }
    
    
    @Test
    public void IsNotShortTest() {
        CanMessage myMessage = new CanMessage(
            new int[]{0x91, 0x05, 0x00, 0x01, 0x05}, tc.getCanid()
        );
        Assert.assertFalse(t.isShortEvent(CbusMessage.getOpcode(myMessage)));
    }
    
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    //    apps.tests.Log4JFixture.setUp();
        tc = new TestTrafficController();
        t = new CbusOpCodes();
    }

    @After
    public void tearDown() {
        tc=null;
        t=null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusOpCodesTest.class);

}
