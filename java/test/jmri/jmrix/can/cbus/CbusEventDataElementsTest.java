package jmri.jmrix.can.cbus;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2020
 */
public class CbusEventDataElementsTest {

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testSetGet() {
        
        Assert.assertEquals("0 elements",0,t.getNumElements());
        
        t.setNumElements(1);
        Assert.assertEquals("1 element",1,t.getNumElements());

        t.setData(1, 79);
        Assert.assertEquals("1 data 79",79,t.getData(1));
        
    }
    
    @Test
    public void testMessageRequest() {
        
        Assert.assertEquals("Long Request","[581] 92 00 02 00 03",
            t.getCanMessage(1,2,3,CbusEventDataElements.EvState.REQUEST).getToString());
        
        t.setNumElements(3); // does nothing as request event
        
        Assert.assertEquals("Short Request","[581] 9A 00 00 00 05",
            t.getCanMessage(1,0,5,CbusEventDataElements.EvState.REQUEST).getToString());
    
    }
    
    @Test
    public void testMessageOn() {
    
        Assert.assertEquals("Long On","[581] 90 00 02 00 03",
            t.getCanMessage(1,2,3,CbusEventDataElements.EvState.ON).getToString());
        
        t.setNumElements(1);
        t.setData(1, 0xaa);
        Assert.assertEquals("Long On Data1","[581] B0 00 02 00 03 AA",
            t.getCanMessage(1,2,3,CbusEventDataElements.EvState.ON).getToString());
        
        t.setNumElements(2);
        t.setData(2, 0xbb);
        Assert.assertEquals("Long On Data2","[581] D0 00 02 00 03 AA BB",
            t.getCanMessage(1,2,3,CbusEventDataElements.EvState.ON).getToString());
        
        t.setNumElements(3);
        t.setData(3, 0xcc);
        Assert.assertEquals("Long On Data3","[581] F0 00 02 00 03 AA BB CC",
            t.getCanMessage(1,2,3,CbusEventDataElements.EvState.ON).getToString());
        
    }
    
    @Test
    public void testMessageOff() {
    
        Assert.assertEquals("Long Off","[581] 91 00 02 00 03",
            t.getCanMessage(1,2,3,CbusEventDataElements.EvState.OFF).getToString());
        
        t.setNumElements(1);
        t.setData(1, 0xaa);
        Assert.assertEquals("Long Off Data1","[581] B1 00 02 00 03 AA",
            t.getCanMessage(1,2,3,CbusEventDataElements.EvState.OFF).getToString());
        
        t.setNumElements(2);
        t.setData(2, 0xbb);
        Assert.assertEquals("Long Off Data2","[581] D1 00 02 00 03 AA BB",
            t.getCanMessage(1,2,3,CbusEventDataElements.EvState.OFF).getToString());
        
        t.setNumElements(3);
        t.setData(3, 0xcc);
        Assert.assertEquals("Long Off Data3","[581] F1 00 02 00 03 AA BB CC",
            t.getCanMessage(1,2,3,CbusEventDataElements.EvState.OFF).getToString());
        
    }
    
    private CbusEventDataElements t;
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        t = new CbusEventDataElements();
    }

    @After
    public void tearDown() {
        t = null;
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(CbusEventDataElementsTest.class);

}
