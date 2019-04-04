package jmri.jmrix.can.cbus.swing.eventrequestmonitor;

// import jmri.jmrix.can.CanSystemConnectionMemo;
// import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of CbusEventRequestMonitorEvent
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Steve Young Copyright (C) 2019
 */
public class CbusEventRequestMonitorEventTest {

    @Test
    public void testCtor() {       
        
        CbusEventRequestMonitorEvent t = new CbusEventRequestMonitorEvent(0,1,null,null,0,0,null);
        // nn , en, Evstate, timestamp, feedback timeout, feedbacktotreqd, eventRequestModel
        Assert.assertNotNull("exists", t);
        
        t = null;
        
    }
    
    @Test
    public void testSetsGets() {
        
        // short event 1
        CbusEventRequestMonitorEvent t = new CbusEventRequestMonitorEvent(0,1,null,null,0,0,null);
        
        t.setFeedbackTimeout(17);
        Assert.assertTrue( t.getFeedbackTimeout() == 17 );
        
        t.setFeedbackOutstanding(71);
        Assert.assertTrue( t.getFeedbackOutstanding() == 71 );
        
        Assert.assertNull( t.getDate() );
        t.setDate ( new java.util.Date() );
        Assert.assertNotNull( t.getDate() );
        
        t = null;
        
    }
    
    @Test
    public void testMatchLongEvent() {
        
        // long event 1234 node 555
        CbusEventRequestMonitorEvent t = new CbusEventRequestMonitorEvent(555,1234,null,null,0,0,null);

        Assert.assertFalse( t.matchesFeedback(555,1234));
        Assert.assertFalse( t.matchesFeedback(55,123));
        Assert.assertFalse( t.matchesFeedback(555,123));
        Assert.assertFalse( t.matchesFeedback(55,1234));
        
        t.setExtraEvent(777);
        t.setExtraNode(6666);
        
        Assert.assertTrue( t.getExtraEvent() == 777 );
        Assert.assertTrue( t.getExtraNode() == 6666 );
        
        Assert.assertFalse( t.matchesFeedback(6666,7));
        Assert.assertFalse( t.matchesFeedback(6,777));
        Assert.assertTrue( t.matchesFeedback(6666,777));
        
        t = null;
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
