package jmri.jmrix.can.cbus.eventtable;

import jmri.jmrix.can.cbus.CbusConstants;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2019
 */
public class CbusTableEventTest {

    @Test
    public void testCTor() {
        CbusTableEvent t = new CbusTableEvent(null,0,1);
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testSetGet() {
        
        CbusTableEvent t = new CbusTableEvent(null,0,1);
        
        t.setDate(new java.util.Date() );
        Assert.assertNotNull("date",t.getDate());
        
        t.setCanId(123);
        Assert.assertEquals("Can ID set", 123,(t.getEventCanId()) );
        
        t.setComment("EvEnT CoMmEnT");
        Assert.assertEquals("EvEnT CoMmEnT", "EvEnT CoMmEnT",(t.getComment()) );
        
        Assert.assertEquals("getSessionOn starts 0", 0,(t.getSessionOnOff(true)) );
        Assert.assertEquals("getSessionOff starts 0", 0,(t.getSessionOnOff(false)) );
        Assert.assertEquals("getSessionIn starts 0", 0,(t.getSessionInOut(true)) );
        Assert.assertEquals("getSessionOut starts 0", 0,(t.getSessionInOut(false)) );
        
        Assert.assertEquals("getTotalOn starts 0", 0,(t.getTotalOnOff(true)) );
        Assert.assertEquals("getTotalOff starts 0", 0,(t.getTotalOnOff(false)) );
        Assert.assertEquals("getTotalIn starts 0", 0,(t.getTotalInOut(true)) );
        Assert.assertEquals("getTotalOut starts 0", 0,(t.getTotalInOut(false)) );
        
        
        t.setState(CbusTableEvent.EvState.ON);
        Assert.assertEquals("getSessionOn 1", 1,(t.getSessionOnOff(true)) );
        
        t.setState(CbusTableEvent.EvState.OFF);
        Assert.assertEquals("getSessionOff 1", 1,(t.getSessionOnOff(false)) );
        
        t.bumpDirection(CbusConstants.EVENT_DIR_IN);
        Assert.assertEquals("getSessionIn 1", 1,(t.getSessionInOut(true)) );
        
        t.bumpDirection(CbusConstants.EVENT_DIR_OUT);
        Assert.assertEquals("getSessionOut 1", 1,(t.getSessionInOut(false)) );
        
        Assert.assertEquals("getTotalOn 1", 1,(t.getTotalOnOff(true)) );
        Assert.assertEquals("getTotalOff 1", 1,(t.getTotalOnOff(false)) );
        Assert.assertEquals("getTotalIn 1", 1,(t.getTotalInOut(true)) );
        Assert.assertEquals("getTotalOut 1", 1,(t.getTotalInOut(false)) );
        
        
        t.resetSessionTotals();
        
        Assert.assertEquals("getSessionOn reset", 0,(t.getSessionOnOff(true)) );
        Assert.assertEquals("getSessionOff reset", 0,(t.getSessionOnOff(false)) );
        Assert.assertEquals("getSessionIn reset", 0,(t.getSessionInOut(true)) );
        Assert.assertEquals("getSessionOut reset", 0,(t.getSessionInOut(false)) );
        
        t.setCounts(123,456,789,0);
        
        Assert.assertEquals("getTotalOn 123", 123,(t.getTotalOnOff(true)) );
        Assert.assertEquals("getTotalOff 456", 456,(t.getTotalOnOff(false)) );
        Assert.assertEquals("getTotalIn 789", 789,(t.getTotalInOut(true)) );
        Assert.assertEquals("getTotalOut 0", 0,(t.getTotalInOut(false)) );
        
    }
    
    @Test
    public void testEqualsToNormalEvent(){
        Assert.assertEquals(new jmri.jmrix.can.cbus.CbusEvent(null,123,456), new CbusTableEvent(null,123,456));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusTableEventTest.class);

}
