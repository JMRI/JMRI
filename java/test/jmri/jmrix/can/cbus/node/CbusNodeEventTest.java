package jmri.jmrix.can.cbus.node;

import jmri.util.JUnitAppender;
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
        Assert.assertTrue("next outstanding 0",t.getNextOutstanding()== 0 );
        Assert.assertTrue("EvVars 2 -1",t.getEvVar(2)== 2 );
        t.setEvVar(2,255);
        Assert.assertTrue("EvVars 2 255",t.getEvVar(2)== 255 );
        
        Assert.assertTrue("defalt no temp name set",t.getTempFcuNodeName().isEmpty() );
        t.setTempFcuNodeName("Alonso");
        Assert.assertTrue("temp name set",t.getTempFcuNodeName()=="Alonso" );
        
        t.setEvVar(1,256);
        JUnitAppender.assertErrorMessageStartsWith("Event Variable value needs to be less than 255 (oxff)");
        
    }
    
    @Test
    public void testEventHexString(){
        
        CbusNodeEvent t = new CbusNodeEvent(0,7,256,-1,4);
        
        Assert.assertEquals("4 Ev Vars Unset","FFFFFFFF",t.getHexEvVarString());
        t.setEvArr(new int[]{0,0,0,0});
        Assert.assertEquals("4 Ev Vars 0000","00000000",t.getHexEvVarString());
        t.setEvArr(new int[]{1,2,3,4});
        Assert.assertEquals("4 Ev Vars 1234","01020304",t.getHexEvVarString());
        t.setEvArr(null);
        Assert.assertEquals("4 Ev Vars null","",t.getHexEvVarString());
        
    }
    
    @Test
    @SuppressWarnings("unlikely-arg-type") // Makes sures different objects do not pass
    public void testNodeEventEquals() {
        
        CbusNodeEvent t = new CbusNodeEvent(0,7,256,-1,4);
        // t.setEvArr(new int[]{1,2,3,4});
        CbusNodeEvent tt = new CbusNodeEvent(0,7,256,-1,4);
        // tt.setEvArr(new int[]{1,2,3,4});
        
        Assert.assertFalse("Null Equals",t.equals(null));
        Assert.assertTrue("Equals t",t.equals(t));
        Assert.assertTrue("Equals tt",t.equals(tt));
        
        Assert.assertTrue("Same hashcode tt",t.hashCode()==tt.hashCode());
        
        
        Assert.assertFalse("Equals Node different",t.equals(new CbusNodeEvent(1,7,256,-1,4)));
        Assert.assertFalse("Equals Event Different",t.equals(new CbusNodeEvent(0,8,256,-1,4)));
        Assert.assertFalse("Equals Host different",t.equals(new CbusNodeEvent(0,7,257,-1,4)));
        Assert.assertFalse("Equals Event Length different",t.equals(new CbusNodeEvent(0,7,256,-1,5)));
        
        t.setEvArr(new int[]{1,2,3,4});
        tt.setEvArr(new int[]{1,2,3,4});
        Assert.assertTrue("Equals ev var 1234",t.equals(tt));
        Assert.assertTrue("Same hashcode tt",t.hashCode()==tt.hashCode());
        tt.setEvArr(new int[]{1,2,3,5});
        Assert.assertFalse("Equals ev var 1235",t.equals(tt));
        Assert.assertTrue("Same hashcode tt",t.hashCode()!=tt.hashCode());
        Assert.assertFalse("Equals different object",t.equals("Random String"));
        
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
