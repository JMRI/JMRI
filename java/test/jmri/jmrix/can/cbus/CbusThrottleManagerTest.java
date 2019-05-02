package jmri.jmrix.can.cbus;

import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.LocoAddress;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.simulator.CbusDummyCS;
import jmri.jmrix.can.TrafficControllerScaffoldLoopback;
import jmri.Throttle;
import jmri.ThrottleListener;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.*;
import org.junit.*;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 *
 * @author Paul Bender Copyright (C) 2017   
 */
public class CbusThrottleManagerTest extends jmri.managers.AbstractThrottleManagerTestBase {
    
    CanSystemConnectionMemo memo;
    CbusDummyCS _cs;
    CbusThrottleManager cbtm;

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        TrafficControllerScaffoldLoopback tc = new TrafficControllerScaffoldLoopback(); // do not use this tc normally
        memo = new CanSystemConnectionMemo();
        memo.setTrafficController(tc);
        tm = new CbusThrottleManager(memo);
        _cs = new CbusDummyCS(memo); // we are testing the tm, not the command station
        _cs.setDelay(0); // no need to simulate network delay
        
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
        tm=null;
        _cs.dispose();
        _cs = null;
        if (cbtm != null) { 
            cbtm.dispose();
        }
        cbtm = null;
    }

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",tm);
    }

    @Test
    public void testIncomingFuntions() {
        cbtm = new CbusThrottleManager(memo);
        Assert.assertNotNull("exists",cbtm);
        DccLocoAddress addr = new DccLocoAddress(1234,true);
        
        CbusThrottleListen throtListen = new CbusThrottleListen();
        cbtm.requestThrottle(addr,throtListen);
        
        JUnitUtil.waitFor(()->{ return(cbtm.getThrottleUsageCount(1234,true)>0); }, "reply didn't arrive");
        Assert.assertEquals("F0 init",false,cbtm.getThrottleInfo(addr,Throttle.F0));

        CanReply r = new CanReply( new int[]{CbusConstants.CBUS_DFUN, 1, 0x00, 0x00 },0x12 );
        cbtm.reply(r);
        JUnitAppender.assertErrorMessageStartsWith("Unrecognised function group");
        r.setElement(2, 1);
        r.setElement(3, 0x1f);
        cbtm.reply(r);
        Assert.assertEquals("F0 on",true,cbtm.getThrottleInfo(addr,Throttle.F0));
        Assert.assertEquals("F1 on",true,cbtm.getThrottleInfo(addr,Throttle.F1));
        Assert.assertEquals("F2 on",true,cbtm.getThrottleInfo(addr,Throttle.F2));
        Assert.assertEquals("F3 on",true,cbtm.getThrottleInfo(addr,Throttle.F3));
        Assert.assertEquals("F4 on",true,cbtm.getThrottleInfo(addr,Throttle.F4));
        r.setElement(3, 0x00);
        cbtm.reply(r);
        Assert.assertEquals("F0 off",false,cbtm.getThrottleInfo(addr,Throttle.F0));        
        Assert.assertEquals("F1 off",false,cbtm.getThrottleInfo(addr,Throttle.F1));
        Assert.assertEquals("F2 off",false,cbtm.getThrottleInfo(addr,Throttle.F2));
        Assert.assertEquals("F3 off",false,cbtm.getThrottleInfo(addr,Throttle.F3));
        Assert.assertEquals("F4 off",false,cbtm.getThrottleInfo(addr,Throttle.F4));
        r.setElement(2, 2);
        r.setElement(3, 0x0f);
        cbtm.reply(r);
        Assert.assertEquals("F5 on",true,cbtm.getThrottleInfo(addr,Throttle.F5));
        Assert.assertEquals("F6 on",true,cbtm.getThrottleInfo(addr,Throttle.F6));
        Assert.assertEquals("F7 on",true,cbtm.getThrottleInfo(addr,Throttle.F7));
        Assert.assertEquals("F8 on",true,cbtm.getThrottleInfo(addr,Throttle.F8));
        r.setElement(3, 0x00);
        cbtm.reply(r);
        Assert.assertEquals("F5 off",false,cbtm.getThrottleInfo(addr,Throttle.F5));
        Assert.assertEquals("F6 off",false,cbtm.getThrottleInfo(addr,Throttle.F6));
        Assert.assertEquals("F7 off",false,cbtm.getThrottleInfo(addr,Throttle.F7));
        Assert.assertEquals("F8 off",false,cbtm.getThrottleInfo(addr,Throttle.F8));        
        
        r.setElement(2, 3);
        r.setElement(3, 0x0f);
        cbtm.reply(r);
        Assert.assertEquals("F9 on",true,cbtm.getThrottleInfo(addr,Throttle.F9));
        Assert.assertEquals("F10 on",true,cbtm.getThrottleInfo(addr,Throttle.F10));
        Assert.assertEquals("F11 on",true,cbtm.getThrottleInfo(addr,Throttle.F11));
        Assert.assertEquals("F12 on",true,cbtm.getThrottleInfo(addr,Throttle.F12));
        r.setElement(3, 0x00);
        cbtm.reply(r);
        Assert.assertEquals("F9 off",false,cbtm.getThrottleInfo(addr,Throttle.F9));
        Assert.assertEquals("F10 off",false,cbtm.getThrottleInfo(addr,Throttle.F10));
        Assert.assertEquals("F11 off",false,cbtm.getThrottleInfo(addr,Throttle.F11));
        Assert.assertEquals("F12 off",false,cbtm.getThrottleInfo(addr,Throttle.F12));
        
        r.setElement(2, 4);
        r.setElement(3, 0xff);
        cbtm.reply(r);
        Assert.assertEquals("F13 on",true,cbtm.getThrottleInfo(addr,Throttle.F13));
        Assert.assertEquals("F14 on",true,cbtm.getThrottleInfo(addr,Throttle.F14));
        Assert.assertEquals("F15 on",true,cbtm.getThrottleInfo(addr,Throttle.F15));
        Assert.assertEquals("F16 on",true,cbtm.getThrottleInfo(addr,Throttle.F16));
        Assert.assertEquals("F17 on",true,cbtm.getThrottleInfo(addr,Throttle.F17));
        Assert.assertEquals("F18 on",true,cbtm.getThrottleInfo(addr,Throttle.F18));
        Assert.assertEquals("F19 on",true,cbtm.getThrottleInfo(addr,Throttle.F19));
        Assert.assertEquals("F20 on",true,cbtm.getThrottleInfo(addr,Throttle.F20));

        r.setElement(3, 0x00);
        cbtm.reply(r);
        Assert.assertEquals("F13 off",false,cbtm.getThrottleInfo(addr,Throttle.F13));
        Assert.assertEquals("F14 off",false,cbtm.getThrottleInfo(addr,Throttle.F14));
        Assert.assertEquals("F15 off",false,cbtm.getThrottleInfo(addr,Throttle.F15));
        Assert.assertEquals("F16 off",false,cbtm.getThrottleInfo(addr,Throttle.F16));
        Assert.assertEquals("F17 off",false,cbtm.getThrottleInfo(addr,Throttle.F17));
        Assert.assertEquals("F18 off",false,cbtm.getThrottleInfo(addr,Throttle.F18));
        Assert.assertEquals("F19 off",false,cbtm.getThrottleInfo(addr,Throttle.F19));
        Assert.assertEquals("F20 off",false,cbtm.getThrottleInfo(addr,Throttle.F20));

        r.setElement(2, 5);
        r.setElement(3, 0xff);
        cbtm.reply(r);
        Assert.assertEquals("F21 on",true,cbtm.getThrottleInfo(addr,Throttle.F21));
        Assert.assertEquals("F22 on",true,cbtm.getThrottleInfo(addr,Throttle.F22));
        Assert.assertEquals("F23 on",true,cbtm.getThrottleInfo(addr,Throttle.F23));
        Assert.assertEquals("F24 on",true,cbtm.getThrottleInfo(addr,Throttle.F24));
        Assert.assertEquals("F25 on",true,cbtm.getThrottleInfo(addr,Throttle.F25));
        Assert.assertEquals("F26 on",true,cbtm.getThrottleInfo(addr,Throttle.F26));
        Assert.assertEquals("F27 on",true,cbtm.getThrottleInfo(addr,Throttle.F27));
        Assert.assertEquals("F28 on",true,cbtm.getThrottleInfo(addr,Throttle.F28));
        r.setElement(3, 0x00);
        cbtm.reply(r);
        Assert.assertEquals("F21 off",false,cbtm.getThrottleInfo(addr,Throttle.F21));
        Assert.assertEquals("F22 off",false,cbtm.getThrottleInfo(addr,Throttle.F22));
        Assert.assertEquals("F23 off",false,cbtm.getThrottleInfo(addr,Throttle.F23));
        Assert.assertEquals("F24 off",false,cbtm.getThrottleInfo(addr,Throttle.F24));
        Assert.assertEquals("F25 off",false,cbtm.getThrottleInfo(addr,Throttle.F25));
        Assert.assertEquals("F26 off",false,cbtm.getThrottleInfo(addr,Throttle.F26));
        Assert.assertEquals("F27 off",false,cbtm.getThrottleInfo(addr,Throttle.F27));
        Assert.assertEquals("F28 off",false,cbtm.getThrottleInfo(addr,Throttle.F28));

        cbtm = null;
        
    }
    
    @Test
    public void testIncomingFuntionsDecimal() {
        CbusThrottleManager cbtmb = new CbusThrottleManager(memo);
        Assert.assertNotNull("exists",cbtmb);
        DccLocoAddress addr = new DccLocoAddress(221,true);
        
        CbusThrottleListen throtListen = new CbusThrottleListen();
        cbtmb.requestThrottle(addr,throtListen);
        
        JUnitUtil.waitFor(()->{ return(cbtmb.getThrottleInfo(addr,"F0")!=null); }, "reply didn't arrive");
        CanReply r = new CanReply( new int[]{CbusConstants.CBUS_DFNON, 1, 0 },0x12 );

        for ( int i=0 ; (i < 29 ) ; i++){
            String _f = "F" + i;            
            r.setElement(0, CbusConstants.CBUS_DFNON);
            r.setElement(2, i);
            cbtmb.reply(r);
            Assert.assertEquals("Function loop on " + i,true,cbtmb.getThrottleInfo(addr,_f));
            r.setElement(0, CbusConstants.CBUS_DFNOF);
            cbtmb.reply(r);            
            Assert.assertEquals("Function loop off " + i,false,cbtmb.getThrottleInfo(addr,_f));            
        }
        r.setElement(2, 0xff);
        cbtmb.reply(r);
        JUnitAppender.assertWarnMessage("Unhandled function number: 255");
    }
    
    @Test
    public void testIncomingSpeedDirection() {
        CbusThrottleManager cbtmb = new CbusThrottleManager(memo);
        Assert.assertNotNull("exists",cbtmb);
        DccLocoAddress addr = new DccLocoAddress(422,true);
        
        CbusThrottleListen throtListen = new CbusThrottleListen();
        cbtmb.requestThrottle(addr,throtListen);
        
        JUnitUtil.waitFor(()->{ return(cbtmb.getThrottleInfo(addr,"F0")!=null); }, "reply didn't arrive");
        
        Assert.assertEquals("speed setting",0.0f,cbtmb.getThrottleInfo(addr,"SpeedSetting"));
        Assert.assertEquals("speed increment",(1.0f/126.0f),cbtmb.getThrottleInfo(addr,"SpeedIncrement"));
        Assert.assertEquals("speed step mode",CbusConstants.CBUS_SS_128,cbtmb.getThrottleInfo(addr,"SpeedStepMode"));
        
        CanReply r = new CanReply( new int[]{CbusConstants.CBUS_DSPD, 1, 0 },0x12 );
        cbtmb.reply(r);
        Assert.assertEquals("speed setting 0",0.0f,cbtmb.getThrottleInfo(addr,"SpeedSetting"));
        Assert.assertEquals("is forward 0",false,cbtmb.getThrottleInfo(addr,"IsForward"));
        
        r.setElement(2, 1);
        cbtmb.reply(r);  
        Assert.assertEquals("speed setting 1",-1.0f,cbtmb.getThrottleInfo(addr,"SpeedSetting"));
        Assert.assertEquals("is forward 1",false,cbtmb.getThrottleInfo(addr,"IsForward"));
        
        r.setElement(2, 2);
        cbtmb.reply(r);  
        Assert.assertEquals("speed setting 2",(1.0f/126.0f),cbtmb.getThrottleInfo(addr,"SpeedSetting"));
        Assert.assertEquals("is forward 2",false,cbtmb.getThrottleInfo(addr,"IsForward"));        

        r.setElement(2, 77);
        cbtmb.reply(r);  
        Assert.assertEquals("speed setting 77",(76.0f/126.0f),cbtmb.getThrottleInfo(addr,"SpeedSetting"));
        Assert.assertEquals("is forward 77",false,cbtmb.getThrottleInfo(addr,"IsForward"));

        r.setElement(2, 126);
        cbtmb.reply(r);  
        Assert.assertEquals("speed setting 126",(125.0f/126.0f),cbtmb.getThrottleInfo(addr,"SpeedSetting"));
        Assert.assertEquals("is forward 126",false,cbtmb.getThrottleInfo(addr,"IsForward"));
        
        r.setElement(2, 127);
        cbtmb.reply(r);  
        Assert.assertEquals("speed setting 127",(1.0f),cbtmb.getThrottleInfo(addr,"SpeedSetting"));
        Assert.assertEquals("is forward 127",false,cbtmb.getThrottleInfo(addr,"IsForward"));

        r.setElement(2, 128);
        cbtmb.reply(r);  
        Assert.assertEquals("speed setting 128",(0.0f),cbtmb.getThrottleInfo(addr,"SpeedSetting"));
        Assert.assertEquals("is forward 128",true,cbtmb.getThrottleInfo(addr,"IsForward"));

        r.setElement(2, 129);
        cbtmb.reply(r);  
        Assert.assertEquals("speed setting 129",(-1.0f),cbtmb.getThrottleInfo(addr,"SpeedSetting"));
        Assert.assertEquals("is forward 129",true,cbtmb.getThrottleInfo(addr,"IsForward"));

        r.setElement(2, 130);
        cbtmb.reply(r);  
        Assert.assertEquals("speed setting 130",(1.0f/126.0f),cbtmb.getThrottleInfo(addr,"SpeedSetting"));
        Assert.assertEquals("is forward 130",true,cbtmb.getThrottleInfo(addr,"IsForward"));

        r.setElement(2, 211);
        cbtmb.reply(r);  
        Assert.assertEquals("speed setting 211",(82.0f/126.0f),cbtmb.getThrottleInfo(addr,"SpeedSetting"));
        Assert.assertEquals("is forward 211",true,cbtmb.getThrottleInfo(addr,"IsForward"));

        r.setElement(2, 254);
        cbtmb.reply(r);  
        Assert.assertEquals("speed setting 254",(125.0f/126.0f),cbtmb.getThrottleInfo(addr,"SpeedSetting"));
        Assert.assertEquals("is forward 254",true,cbtmb.getThrottleInfo(addr,"IsForward"));

        r.setElement(2, 255);
        cbtmb.reply(r);
        Assert.assertEquals("speed setting 255",(1.0f),cbtmb.getThrottleInfo(addr,"SpeedSetting"));
        Assert.assertEquals("is forward 255",true,cbtmb.getThrottleInfo(addr,"IsForward"));

        r = new CanReply( new int[]{CbusConstants.CBUS_ESTOP },0x12 );
        cbtmb.reply(r);
        Assert.assertEquals("speed setting estop",(-1.0f),cbtmb.getThrottleInfo(addr,"SpeedSetting"));
        Assert.assertEquals("estop forward",true,cbtmb.getThrottleInfo(addr,"IsForward"));

        r = new CanReply( new int[]{CbusConstants.CBUS_DSPD, 1, 77 },0x12 );
        cbtmb.reply(r);
        Assert.assertEquals("speed setting before reverse estop 77",(76.0f/126.0f),cbtmb.getThrottleInfo(addr,"SpeedSetting"));
        Assert.assertEquals("is forward b4 estop 77",false,cbtmb.getThrottleInfo(addr,"IsForward"));        
        r = new CanReply( new int[]{CbusConstants.CBUS_ESTOP },0x12 );
        cbtmb.reply(r);
        Assert.assertEquals("speed setting estop",(-1.0f),cbtmb.getThrottleInfo(addr,"SpeedSetting"));
        Assert.assertEquals("estop reverse",false,cbtmb.getThrottleInfo(addr,"IsForward"));
        
        r = new CanReply( new int[]{CbusConstants.CBUS_DSPD, 1, 255 },0x12 );
        cbtmb.reply(r);
        Assert.assertEquals("speed setting 255",(1.0f),cbtmb.getThrottleInfo(addr,"SpeedSetting"));
        Assert.assertEquals("is forward 255",true,cbtmb.getThrottleInfo(addr,"IsForward"));

        r = new CanReply( new int[]{CbusConstants.CBUS_RESTP },0x12 );
        cbtmb.reply(r);
        Assert.assertEquals("speed setting r estop",(-1.0f),cbtmb.getThrottleInfo(addr,"SpeedSetting"));
        Assert.assertEquals("restop forward",true,cbtmb.getThrottleInfo(addr,"IsForward"));

        
        r = new CanReply( new int[]{CbusConstants.CBUS_DSPD, 1, 77 },0x12 );
        cbtmb.reply(r);
        Assert.assertEquals("speed setting before reverse restop 77",(76.0f/126.0f),cbtmb.getThrottleInfo(addr,"SpeedSetting"));
        Assert.assertEquals("is forward b4 restop 77",false,cbtmb.getThrottleInfo(addr,"IsForward"));        
        r = new CanReply( new int[]{CbusConstants.CBUS_RESTP },0x12 );
        cbtmb.reply(r);
        Assert.assertEquals("speed setting restop",(-1.0f),cbtmb.getThrottleInfo(addr,"SpeedSetting"));
        Assert.assertEquals("restop reverse",false,cbtmb.getThrottleInfo(addr,"IsForward"));

        r = null;

    }

    @Test
    public void testMessage() {
        
        CbusThrottleManager cbtmb = new CbusThrottleManager(memo);
        Assert.assertNotNull("exists",cbtmb);
        DccLocoAddress addr = new DccLocoAddress(1234,true);
        
        CbusThrottleListen throtListen = new CbusThrottleListen();
        cbtmb.requestThrottle(addr,throtListen);
        
        JUnitUtil.waitFor(()->{ return(cbtmb.getThrottleInfo(addr,"F0")!=null); }, "reply didn't arrive");        
        
        CanReply r = new CanReply( new int[]{CbusConstants.CBUS_DSPD, 1, 77 },0x12 );
        cbtmb.reply(r);
        Assert.assertEquals("msg speed setting before reverse estop 77",(76.0f/126.0f),cbtmb.getThrottleInfo(addr,"SpeedSetting"));
        Assert.assertEquals("msg is forward b4 estop 77",false,cbtmb.getThrottleInfo(addr,"IsForward"));
        CanMessage m = new CanMessage( new int[]{CbusConstants.CBUS_RESTP },0x12 );
        cbtmb.message(m);
        Assert.assertEquals("msg speed setting estop",(-1.0f),cbtmb.getThrottleInfo(addr,"SpeedSetting"));
        Assert.assertEquals("msg estop reverse",false,cbtmb.getThrottleInfo(addr,"IsForward"));

        r = new CanReply( new int[]{CbusConstants.CBUS_DSPD, 1, 77 },0x12 );
        cbtmb.reply(r);
        Assert.assertEquals("msg speed setting before reverse estop 77",(76.0f/126.0f),cbtmb.getThrottleInfo(addr,"SpeedSetting"));
        Assert.assertEquals("msg is forward b4 estop 77",false,cbtmb.getThrottleInfo(addr,"IsForward"));
        m = new CanMessage( new int[]{CbusConstants.CBUS_ESTOP },0x12 );
        cbtmb.message(m);
        Assert.assertEquals("msg speed setting estop",(-1.0f),cbtmb.getThrottleInfo(addr,"SpeedSetting"));
        Assert.assertEquals("msg estop reverse",false,cbtmb.getThrottleInfo(addr,"IsForward"));

        r = new CanReply( new int[]{CbusConstants.CBUS_DSPD, 1, 77 },0x12 );
        cbtmb.reply(r);
        Assert.assertEquals("msg speed setting before reverse estop 77",(76.0f/126.0f),cbtmb.getThrottleInfo(addr,"SpeedSetting"));
        Assert.assertEquals("msg is forward b4 estop 77",false,cbtmb.getThrottleInfo(addr,"IsForward"));
        m = new CanMessage( new int[]{CbusConstants.CBUS_DSPD, 1, 99 },0x12 );
        cbtmb.message(m);
        Assert.assertEquals("msg speed does not change",(76.0f/126.0f),cbtmb.getThrottleInfo(addr,"SpeedSetting"));
        Assert.assertEquals("msg estop reverse",false,cbtmb.getThrottleInfo(addr,"IsForward"));

        m.setElement(2, 1);
        cbtmb.message(m);
        Assert.assertEquals("msg speed change estop",(-1.0f),cbtmb.getThrottleInfo(addr,"SpeedSetting"));
        Assert.assertEquals("msg estop reverse",false,cbtmb.getThrottleInfo(addr,"IsForward"));

        m.setElement(2, 129);
        cbtmb.message(m);
        Assert.assertEquals("msg speed change estop",(-1.0f),cbtmb.getThrottleInfo(addr,"SpeedSetting"));
        Assert.assertEquals("msg estop reverse",true,cbtmb.getThrottleInfo(addr,"IsForward"));
    }

    @Test
    public void testCbdispose() {
        CbusThrottleManager cbtmb = new CbusThrottleManager(memo);
        Assert.assertNotNull("exists",cbtmb);
        DccLocoAddress addr = new DccLocoAddress(555,true);
        Assert.assertEquals("throttle use 0", 0, cbtmb.getThrottleUsageCount(addr));
        CbusThrottleListen throtListen = new CbusThrottleListen();
        cbtmb.requestThrottle(addr,throtListen);
        CbusThrottle cbt = new CbusThrottle(memo,addr,1);
        JUnitUtil.waitFor(()->{ return(cbtmb.getThrottleUsageCount(addr)>0); }, "Throttle count did not increase");
        Assert.assertEquals("throttle use 1", 1, cbtmb.getThrottleUsageCount(addr));
        
        cbtmb.disposeThrottle(cbt,throtListen);
        JUnitUtil.waitFor(()->{ return(cbtmb.getThrottleUsageCount(addr)==0); }, 
            "Throttle Count did not go 0 on dispose, add retry rule for this if regular?");
        Assert.assertEquals("disposed throttle use 0", 0, cbtmb.getThrottleUsageCount(addr));
        Assert.assertNull("NULL",cbtmb.getThrottleInfo(addr,Throttle.F28));
    }

    private class CbusThrottleListen implements ThrottleListener {

       @Override
       public void notifyThrottleFound(DccThrottle t){
             // throttleFoundResult = true;
       }

       @Override
       public void notifyFailedThrottleRequest(LocoAddress address, String reason){
             // throttleNotFoundResult = true;
       }

       @Override
       public void notifyStealThrottleRequired(LocoAddress address){
            // throttleStealResult = true;
       }
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusThrottleManagerTest.class);

}
