package jmri.jmrix.can.cbus.eventtable;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2019
 */
public class CbusTableEventTest {

    @Test
    public void testCTor() {
        // int,int,EvState,int,String,String,String,int,int,int,int,Date
        CbusTableEvent t = new CbusTableEvent(0,1,null,0,"","",0,0,0,0,null);
        Assert.assertNotNull("exists",t);
        t = null;
    }
    
    @Test
    public void testSetGet() {
        
        CbusTableEvent t = new CbusTableEvent(0,1,null,0,"","",0,0,0,0,null);
        
        t.setDate(new java.util.Date() );
        Assert.assertNotNull("date",t.getDate());
        
        t.setStlOn("ON STL String");
        Assert.assertEquals("ON STL String", "ON STL String",(t.getStlOn()) );
        
        t.setStlOff("OFF STL String");
        Assert.assertEquals("OFF STL String", "OFF STL String",(t.getStlOff()) );
        
        t.setCanId(123);
        Assert.assertEquals("Can ID set", 123,(t.getEventCanId()) );
        
        t.setComment("EvEnT CoMmEnT");
        Assert.assertEquals("EvEnT CoMmEnT", "EvEnT CoMmEnT",(t.getComment()) );
        
        Assert.assertEquals("getSessionOn starts 0", 0,(t.getSessionOn()) );
        Assert.assertEquals("getSessionOff starts 0", 0,(t.getSessionOff()) );
        Assert.assertEquals("getSessionIn starts 0", 0,(t.getSessionIn()) );
        Assert.assertEquals("getSessionOut starts 0", 0,(t.getSessionOut()) );
        
        Assert.assertEquals("getTotalOn starts 0", 0,(t.getTotalOn()) );
        Assert.assertEquals("getTotalOff starts 0", 0,(t.getTotalOff()) );
        Assert.assertEquals("getTotalIn starts 0", 0,(t.getTotalIn()) );
        Assert.assertEquals("getTotalOut starts 0", 0,(t.getTotalOut()) );
        
        
        t.bumpSessionOn();
        t.bumpSessionOff();
        t.bumpSessionIn();
        t.bumpSessionOut();

        Assert.assertEquals("getSessionOn 1", 1,(t.getSessionOn()) );
        Assert.assertEquals("getSessionOff 1", 1,(t.getSessionOff()) );
        Assert.assertEquals("getSessionIn 1", 1,(t.getSessionIn()) );
        Assert.assertEquals("getSessionOut 1", 1,(t.getSessionOut()) );
        
        Assert.assertEquals("getTotalOn 1", 1,(t.getTotalOn()) );
        Assert.assertEquals("getTotalOff 1", 1,(t.getTotalOff()) );
        Assert.assertEquals("getTotalIn 1", 1,(t.getTotalIn()) );
        Assert.assertEquals("getTotalOut 1", 1,(t.getTotalOut()) );
        
        
        t.resetSessionTotals();
        
        Assert.assertEquals("getSessionOn reset", 0,(t.getSessionOn()) );
        Assert.assertEquals("getSessionOff reset", 0,(t.getSessionOff()) );
        Assert.assertEquals("getSessionIn reset", 0,(t.getSessionIn()) );
        Assert.assertEquals("getSessionOut reset", 0,(t.getSessionOut()) );
        
        t.setTotalOn(123);
        t.setTotalOff(456);
        t.setTotalIn(789);
        t.setTotalOut(0);
        
        Assert.assertEquals("getTotalOn 123", 123,(t.getTotalOn()) );
        Assert.assertEquals("getTotalOff 456", 456,(t.getTotalOff()) );
        Assert.assertEquals("getTotalIn 789", 789,(t.getTotalIn()) );
        Assert.assertEquals("getTotalOut 0", 0,(t.getTotalOut()) );
        
    }    
    

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusTableEventTest.class);

}
