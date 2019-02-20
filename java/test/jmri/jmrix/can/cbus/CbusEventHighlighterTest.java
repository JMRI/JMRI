package jmri.jmrix.can.cbus;

import java.awt.Color;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;

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
 * @author Steve Young Copyright (C) 2019
 */
public class CbusEventHighlighterTest {
    
    
    // not checking event / nodes at present in case method changes - sy
    
    
    CbusEventHighlighter t;
    
    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testSetandGetColor() {
        t.setColor(new Color(110, 235, 131));
        Assert.assertEquals("color get",(new Color(110, 235, 131)),t.getColor());
        t.setColor(new Color(123, 0, 5));
        Assert.assertEquals("color get",(new Color(123, 0, 5)),t.getColor());
    }    
    
    // CbusConstants.EVENT_ON EVENT_OFF  EVENT_EITHER  EVENT_NEITHER
    // EVENT_IN, EVENT_OUT, EVENT_DIR_EITHER EVENT_DIR_UNSET
    
    @Test
    public void testNotAnEvent() {
        t.setNn(0);
        t.setNnEnable(false);
        t.setEv(0);
        t.setEvEnable(false);
        t.setType(CbusConstants.EVENT_EITHER);
        t.setDir(CbusConstants.EVENT_DIR_EITHER);
        
        CanMessage m = new CanMessage( new int[]{CbusConstants.CBUS_SNN, 0x00, 0x00, 0x00, 0x01},0x12 );
        CanReply r = new CanReply( new int[]{CbusConstants.CBUS_SNN, 0x00, 0x00, 0x00, 0x01},0x12 );
        Assert.assertFalse("does not highlight a snn", (t.highlight(m)));
        Assert.assertFalse("does not highlight a snn", (t.highlight(r)));
        m.setElement(0, CbusConstants.CBUS_ASOF);
        r.setElement(0, CbusConstants.CBUS_ASOF);        
        
        Assert.assertTrue("does highlight an event ASOF", (t.highlight(m)));
        Assert.assertTrue("does highlight an event ASOF", (t.highlight(r)));

        t.setDir(CbusConstants.EVENT_DIR_UNSET);
        Assert.assertFalse("does not highlight EVENT_DIR_UNSET ASOF", (t.highlight(m)));
        Assert.assertFalse("does not highlight EVENT_DIR_UNSET ASOF", (t.highlight(r)));
        
        t.setDir(CbusConstants.EVENT_DIR_IN);

        Assert.assertFalse("does not highlight EVENT_DIR_IN ASOF", (t.highlight(m)));
        Assert.assertTrue("does highlight EVENT_DIR_IN ASOF", (t.highlight(r)));
        
        t.setDir(CbusConstants.EVENT_DIR_OUT);
        Assert.assertTrue("does highlight EVENT_OUT ASOF", (t.highlight(m)));
        Assert.assertFalse("does not highlight EVENT_OUT ASOF", (t.highlight(r)));
        
        t.setDir(CbusConstants.EVENT_DIR_EITHER);
        t.setType(CbusConstants.EVENT_ON);
        Assert.assertFalse("does not highlight EVENT_ON ASOF", (t.highlight(m)));
        Assert.assertFalse("does not highlight EVENT_ON ASOF", (t.highlight(r)));        
        
        t.setType(CbusConstants.EVENT_OFF);
        Assert.assertTrue("does highlight EVENT_OFF ASOF", (t.highlight(m)));
        Assert.assertTrue("does highlight EVENT_OFF ASOF", (t.highlight(r)));          
        
        m.setElement(0, CbusConstants.CBUS_ACON);
        r.setElement(0, CbusConstants.CBUS_ACON); 
        Assert.assertFalse("does not highlight EVENT_OFF ACON", (t.highlight(m)));
        Assert.assertFalse("does not highlight EVENT_OFF ACON", (t.highlight(r))); 

        t.setType(CbusConstants.EVENT_ON);
        Assert.assertTrue("does highlight EVENT_ON ACON", (t.highlight(m)));
        Assert.assertTrue("does highlight EVENT_ON ACON", (t.highlight(r)));

    }

    @Test
    public void testEventNodeNums() {
        t.setNn(0);
        t.setNnEnable(false);
        t.setEv(0);
        t.setEvEnable(false);
        t.setType(CbusConstants.EVENT_EITHER);
        t.setDir(CbusConstants.EVENT_DIR_EITHER);
        
        CanMessage m = new CanMessage( new int[]{CbusConstants.CBUS_ACON, 0x00, 0x00, 0x00, 0x01},0x12 );
        CanReply r = new CanReply( new int[]{CbusConstants.CBUS_ACON, 0x00, 0x00, 0x00, 0x01},0x12 );
        Assert.assertTrue("does highlight m CBUS_ACON", (t.highlight(m)));
        Assert.assertTrue("does highlight r CBUS_ACON", (t.highlight(r)));
        
        t.setNnEnable(true);
        Assert.assertTrue("node does highlight m nn0", (t.highlight(m)));
        Assert.assertTrue("node does highlight r nn0", (t.highlight(r)));        
        
        m.setElement(2, 0xa4);
        r.setElement(2, 0xa4);
        
        Assert.assertFalse("does not highlight node a4 EVENT_ON ACON", (t.highlight(m)));
        Assert.assertFalse("does not highlight node a4 EVENT_ON ACON", (t.highlight(r)));        

        t.setNn(0xa4);
        Assert.assertTrue("does highlight node a4", (t.highlight(m)));
        Assert.assertTrue("does highlight node a4", (t.highlight(r)));        
        
        
        t.setNnEnable(false);
        t.setEvEnable(true);
        t.setEv(0xa4);
        Assert.assertFalse("does not highlight event 1", (t.highlight(m)));
        Assert.assertFalse("does not highlight event 1", (t.highlight(r)));          
        t.setEv(1);
        Assert.assertTrue("does highlight event 1", (t.highlight(m)));
        Assert.assertTrue("does highlight event 1", (t.highlight(r)));         
    }
    
    

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        t = new CbusEventHighlighter();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
        t = null;
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusEventHighlighterTest.class);

}
