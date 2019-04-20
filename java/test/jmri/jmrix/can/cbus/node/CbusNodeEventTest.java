package jmri.jmrix.can.cbus.node;

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
public class CbusNodeEventTest {

    @Test
    public void testCTor() {
        CbusNodeEvent t = new CbusNodeEvent(0,1,0,0,0);
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testBasicSetGets() {
        // short event 7 on node 256, no index, 4 ev vars
        CbusNodeEvent t = new CbusNodeEvent(0,7,256,-1,4);
        Assert.assertNotNull("exists",t);
        
        Assert.assertTrue("outstanding event vars 4",t.getOutstandingVars()== 4 );
        Assert.assertTrue("next outstanding event var 1",t.getNextOutstanding()== 1 );
        Assert.assertTrue("next outstanding event var 1",t.getParentNn()== 256 );
        Assert.assertTrue("index -1",t.getIndex()== -1 );
        Assert.assertTrue("getNumEvVars 4",t.getNumEvVars()== 4 );
        Assert.assertTrue("EvVars 2 -1",t.getEvVar(2)== -1 );
        
        t.setIndex(2);
        Assert.assertTrue("index 2",t.getIndex()== 2 );
        
        int[] newArr = new int[]{1,2,3,4};
        t.setEvArr(newArr);
        
        Assert.assertEquals("new Arr string","1, 2, 3, 4",t.getEvVarString() );
        
        Assert.assertTrue("EvVars 2 -1",t.getEvVar(2)== 2 );
        t.setEvVar(2,255);
        Assert.assertTrue("EvVars 2 255",t.getEvVar(2)== 255 );
        
        Assert.assertTrue("defalt no temp name set",t.getTempFcuNodeName().isEmpty() );
        t.setTempFcuNodeName("Alonso");
        Assert.assertTrue("temp name set",t.getTempFcuNodeName()=="Alonso" );
        
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

    // private final static Logger log = LoggerFactory.getLogger(CbusNodeEventTest.class);

}
