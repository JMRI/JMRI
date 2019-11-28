package jmri.jmrix.can.cbus;

import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.LocoAddress;
import jmri.SpeedStepMode;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.simulator.CbusDummyCS;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.jmrix.can.TrafficControllerScaffoldLoopback;
import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeTableDataModel;
import jmri.Throttle;
import jmri.ThrottleListener;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import org.junit.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Paul Bender Copyright (C) 2017   
 */
public class CbusThrottleManagerTest extends jmri.managers.AbstractThrottleManagerTestBase {

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",tm);
    }

    @Test
    public void testIncomingFunctions() {
        Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));
    
        CbusThrottleManager cbtm = (CbusThrottleManager) tm;
        Assert.assertNotNull("exists",cbtm);
        DccLocoAddress addr = new DccLocoAddress(1234,true);
        
        CbusThrottleListen throtListen = new CbusThrottleListen();
        cbtm.requestThrottle(addr,throtListen,true);
        
        JUnitUtil.waitFor(()->{ return(cbtm.getThrottleUsageCount(addr)>0); }, "reply didn't arrive");
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
        
    }
    
    @Test
    public void testIncomingFunctionsDecimal() {
        CbusThrottleManager cbtmb = ( CbusThrottleManager) tm;
        Assert.assertNotNull("exists",cbtmb);
        DccLocoAddress addr = new DccLocoAddress(221,true);
        
        CbusThrottleListen throtListen = new CbusThrottleListen();
        cbtmb.requestThrottle(addr,throtListen,true);
        
        JUnitUtil.waitFor(()->{ return(cbtmb.getThrottleInfo(addr,"F0")!=null); }, "throttle not created");
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
        Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));

        CbusThrottleManager cbtmb = (CbusThrottleManager) tm;
        Assert.assertNotNull("exists",cbtmb);
        DccLocoAddress addr = new DccLocoAddress(422,true);
        
        CbusThrottleListen throtListen = new CbusThrottleListen();
        cbtmb.requestThrottle(addr,throtListen,true);
        
        JUnitUtil.waitFor(()->{ return(cbtmb.getThrottleInfo(addr,"F0")!=null); }, "reply didn't arrive");
        
        Assert.assertEquals("speed setting",0.0f,cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING));
        Assert.assertEquals("speed increment",(1.0f/126.0f),cbtmb.getThrottleInfo(addr,Throttle.SPEEDINCREMENT));
        Assert.assertEquals("speed step mode",SpeedStepMode.NMRA_DCC_128,cbtmb.getThrottleInfo(addr,Throttle.SPEEDSTEPMODE));

        CanReply r = new CanReply( new int[]{CbusConstants.CBUS_DSPD, 1, 0 },0x12 );
        cbtmb.reply(r);
        Assert.assertEquals("speed setting 0",0.0f,cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING));
        Assert.assertEquals("is forward 0",false,cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD));
        
        r = new CanReply( new int[]{CbusConstants.CBUS_DSPD, 77, 77 },0x12 );
        cbtmb.reply(r);
        Assert.assertEquals("different session speed unchanged",0.0f,cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING));
        
        r = new CanReply( new int[]{CbusConstants.CBUS_DSPD, 1, 1 },0x12 );
        cbtmb.reply(r);  
        Assert.assertEquals("speed setting 1",-1.0f,cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING));
        Assert.assertEquals("is forward 1",false,cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD));
        
        r.setElement(2, 2);
        cbtmb.reply(r);  
        Assert.assertEquals("speed setting 2",(1.0f/126.0f),cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING));
        Assert.assertEquals("is forward 2",false,cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD));

        r.setElement(2, 77);
        cbtmb.reply(r);  
        Assert.assertEquals("speed setting 77",(76.0f/126.0f),cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING));
        Assert.assertEquals("is forward 77",false,cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD));

        r.setElement(2, 126);
        cbtmb.reply(r);  
        Assert.assertEquals("speed setting 126",(125.0f/126.0f),cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING));
        Assert.assertEquals("is forward 126",false,cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD));
        
        r.setElement(2, 127);
        cbtmb.reply(r);  
        Assert.assertEquals("speed setting 127",(1.0f),cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING));
        Assert.assertEquals("is forward 127",false,cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD));

        r.setElement(2, 128);
        cbtmb.reply(r);  
        Assert.assertEquals("speed setting 128",(0.0f),cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING));
        Assert.assertEquals("is forward 128",true,cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD));

        r.setElement(2, 129);
        cbtmb.reply(r);  
        Assert.assertEquals("speed setting 129",(-1.0f),cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING));
        Assert.assertEquals("is forward 129",true,cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD));

        r.setElement(2, 130);
        cbtmb.reply(r);  
        Assert.assertEquals("speed setting 130",(1.0f/126.0f),cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING));
        Assert.assertEquals("is forward 130",true,cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD));

        r.setElement(2, 211);
        cbtmb.reply(r);  
        Assert.assertEquals("speed setting 211",(82.0f/126.0f),cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING));
        Assert.assertEquals("is forward 211",true,cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD));

        r.setElement(2, 254);
        cbtmb.reply(r);  
        Assert.assertEquals("speed setting 254",(125.0f/126.0f),cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING));
        Assert.assertEquals("is forward 254",true,cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD));

        r.setElement(2, 255);
        cbtmb.reply(r);
        Assert.assertEquals("speed setting 255",(1.0f),cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING));
        Assert.assertEquals("is forward 255",true,cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD));

        r = new CanReply( new int[]{CbusConstants.CBUS_ESTOP },0x12 );
        cbtmb.reply(r);
        Assert.assertEquals("speed setting estop",(-1.0f),cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING));
        Assert.assertEquals("estop forward",true,cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD));

        r = new CanReply( new int[]{CbusConstants.CBUS_DSPD, 1, 77 },0x12 );
        cbtmb.reply(r);
        Assert.assertEquals("speed setting before reverse estop 77",(76.0f/126.0f),cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING));
        Assert.assertEquals("is forward b4 estop 77",false,cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD));
        r = new CanReply( new int[]{CbusConstants.CBUS_ESTOP },0x12 );
        cbtmb.reply(r);
        Assert.assertEquals("speed setting estop",(-1.0f),cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING));
        Assert.assertEquals("estop reverse",false,cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD));
        
        r = new CanReply( new int[]{CbusConstants.CBUS_DSPD, 1, 255 },0x12 );
        cbtmb.reply(r);
        Assert.assertEquals("speed setting 255",(1.0f),cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING));
        Assert.assertEquals("is forward 255",true,cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD));

        r = new CanReply( new int[]{CbusConstants.CBUS_RESTP },0x12 );
        cbtmb.reply(r);
        Assert.assertEquals("speed setting r estop",(-1.0f),cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING));
        Assert.assertEquals("restop forward",true,cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD));

        
        r = new CanReply( new int[]{CbusConstants.CBUS_DSPD, 1, 77 },0x12 );
        cbtmb.reply(r);
        Assert.assertEquals("speed setting before reverse restop 77",(76.0f/126.0f),cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING));
        Assert.assertEquals("is forward b4 restop 77",false,cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD));
        r = new CanReply( new int[]{CbusConstants.CBUS_RESTP },0x12 );
        cbtmb.reply(r);
        Assert.assertEquals("speed setting restop",(-1.0f),cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING));
        Assert.assertEquals("restop reverse",false,cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD));

        r = null;

    }

    @Test
    public void testMessage() {
        Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));
        
        CbusThrottleManager cbtmb = (CbusThrottleManager) tm;
        Assert.assertNotNull("exists",cbtmb);
        DccLocoAddress addr = new DccLocoAddress(1234,true);
        
        CbusThrottleListen throtListen = new CbusThrottleListen();
        cbtmb.requestThrottle(addr,throtListen,true);
        
        JUnitUtil.waitFor(()->{ return(cbtmb.getThrottleInfo(addr,"F0")!=null); }, "Throttle didn't create");        
        
        CanReply r = new CanReply( new int[]{CbusConstants.CBUS_DSPD, 1, 88 },0x12 );
        r.setExtended(true);
        cbtmb.reply(r);
        Assert.assertTrue((float)cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING)==0f);
        
        r.setExtended(false);
        r.setRtr(true);
        cbtmb.reply(r);
        Assert.assertTrue((float)cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING)==0f);
        
        r = new CanReply( new int[]{CbusConstants.CBUS_DSPD, 1, 77 },0x12 );
        // rtr and extended both false by default
        
        cbtmb.reply(r);
        
        JUnitUtil.waitFor(()->{ return(((float)cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING))!=0f); }, "Speed command not received");
        Assert.assertEquals("msg speed setting before reverse estop 77",(76.0f/126.0f),cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING));
        Assert.assertEquals("msg is forward b4 estop 77",false,cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD));
        CanMessage m = new CanMessage( new int[]{CbusConstants.CBUS_RESTP },0x12 );
        cbtmb.message(m);
        Assert.assertEquals("msg speed setting estop",(-1.0f),cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING));
        Assert.assertEquals("msg estop reverse",false,cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD));
        
        r = new CanReply( new int[]{CbusConstants.CBUS_DSPD, 44, 77 },0x12 );
        cbtmb.reply(r);
        Assert.assertEquals("msg speed setting unchanged wrong address",(-1.0f),cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING));
        
        r = new CanReply( new int[]{CbusConstants.CBUS_DSPD, 1, 77 },0x12 );
        cbtmb.reply(r);
        Assert.assertEquals("msg speed setting before reverse estop 77",(76.0f/126.0f),cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING));
        Assert.assertEquals("msg is forward b4 estop 77",false,cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD));
        m = new CanMessage( new int[]{CbusConstants.CBUS_ESTOP },0x12 );
        
        m.setExtended(true);
        cbtmb.message(m);
        Assert.assertEquals("msg speed setting Extended CAN ignored",(76.0f/126.0f),cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING));
        
        m.setExtended(false);
        m.setRtr(true);
        cbtmb.message(m);
        Assert.assertEquals("msg speed setting Rtr CAN ignored",(76.0f/126.0f),cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING));
        
        m.setRtr(false);
        cbtmb.message(m);
        
        Assert.assertEquals("msg speed setting estop",(-1.0f),cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING));
        Assert.assertEquals("msg estop reverse",false,cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD));

        r = new CanReply( new int[]{CbusConstants.CBUS_DSPD, 1, 77 },0x12 );
        cbtmb.reply(r);
        Assert.assertEquals("msg speed setting before reverse estop 77",(76.0f/126.0f),cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING));
        Assert.assertEquals("msg is forward b4 estop 77",false,cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD));
        m = new CanMessage( new int[]{CbusConstants.CBUS_DSPD, 1, 99 },0x12 );
        cbtmb.message(m);
        Assert.assertEquals("msg speed does not change",(76.0f/126.0f),cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING));
        Assert.assertEquals("msg estop reverse",false,cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD));

        m.setElement(2, 1);
        cbtmb.message(m);
        Assert.assertEquals("msg speed change estop",(-1.0f),cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING));
        Assert.assertEquals("msg estop reverse",false,cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD));

        m.setElement(2, 129);
        cbtmb.message(m);
        Assert.assertEquals("msg speed change estop",(-1.0f),cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING));
        Assert.assertEquals("msg estop reverse",true,cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD));
        
        m = new CanMessage( new int[]{CbusConstants.CBUS_KLOC, 22 },0x12 );
        cbtmb.message(m);
        Assert.assertEquals("addr unchanged as no session number match",true,cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD));
        
        m = new CanMessage( new int[]{CbusConstants.CBUS_KLOC, 1 },0x12 );
        cbtmb.message(m);
        
        m = new CanMessage( new int[]{CbusConstants.CBUS_DSPD, 1, 11 },0x12 );
        cbtmb.message(m);
        Assert.assertEquals("msg speed change ignored as session cancelled",(-1.0f),cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING));
        
    }

    @Test
    public void testCbdispose() {
        CbusThrottleManager cbtmb =  (CbusThrottleManager)tm;
        Assert.assertNotNull("exists",cbtmb);
        DccLocoAddress addr = new DccLocoAddress(555,true);
        Assert.assertEquals("throttle use 0", 0, cbtmb.getThrottleUsageCount(addr));
        CbusThrottleListen throtListen = new CbusThrottleListen();
        cbtmb.requestThrottle(addr,throtListen,true);
        CbusThrottle cbt = new CbusThrottle(memo,addr,1);
        JUnitUtil.waitFor(()->{ return(cbtmb.getThrottleUsageCount(addr)>0); }, "Throttle count did not increase");
        Assert.assertEquals("throttle use 1", 1, cbtmb.getThrottleUsageCount(addr));
        
        Assert.assertTrue(cbtmb.disposeThrottle(cbt,throtListen));
        JUnitUtil.waitFor(()->{ return(cbtmb.getThrottleUsageCount(addr)==0); }, 
            "Throttle Count did not go 0 on dispose, add retry rule for this if regular?");
        Assert.assertEquals("disposed throttle use 0", 0, cbtmb.getThrottleUsageCount(addr));
        Assert.assertNull("NULL",cbtmb.getThrottleInfo(addr,Throttle.F28));
        
        Assert.assertFalse(cbtmb.disposeThrottle(null,throtListen));
        
        throtListen = null;
        
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
       
        /**
         * {@inheritDoc}
         * @deprecated since 4.15.7; use #notifyDecisionRequired
         */
        @Override
        @Deprecated
        public void notifyStealThrottleRequired(jmri.LocoAddress address) {
            notifyDecisionRequired(address,DecisionType.STEAL);
        }

        @Override
        public void notifyDecisionRequired(LocoAddress address, DecisionType question) {
            // this is a WIP impelementation.
            if ( question == DecisionType.STEAL ){
            }
        }
    }
    
    // Failed Throttle request when unable to steal / share the session
    // as no command station reporting as a node
    @Test
    public void testCreateCbusThrottleStealScenario1() {
        
        ThrottleListener throtListen = new ThrottleListener() {
            @Override
            public void notifyThrottleFound(DccThrottle t) {
                throttle = t;
                log.error("created a throttle");
            }

            @Override
            public void notifyFailedThrottleRequest(jmri.LocoAddress address, String reason) {
                log.error("Throttle request failed for " + address + " because " + reason);
                failedThrottleRequest = true;
            }
            
            /**
             * @deprecated since 4.15.7; use #notifyDecisionRequired
             */
            @Deprecated
            @Override
            public void notifyStealThrottleRequired(LocoAddress address){
            }

            @Override
            public void notifyDecisionRequired(LocoAddress address, DecisionType question) {
                // this is a never-stealing impelementation.
                if ( question == DecisionType.STEAL ){
                    InstanceManager.throttleManagerInstance().cancelThrottleRequest(address, this);
                    log.error("1: Got a steal request {}",address);
                }
                if ( question == DecisionType.SHARE ){
                    InstanceManager.throttleManagerInstance().cancelThrottleRequest(address, this);
                    log.error("1: Got a share request {}",address);
                }
                if ( question == DecisionType.STEAL_OR_SHARE ){
                    InstanceManager.throttleManagerInstance().cancelThrottleRequest(address, this);
                    log.error("1: Got a steal OR share question {}",address);
                }
            }
        };
        
        TrafficControllerScaffold tc = new TrafficControllerScaffold();
        memo = new CanSystemConnectionMemo();
        memo.setTrafficController(tc);
        CbusThrottleManager cbtm = new CbusThrottleManager(memo);
        cbtm.requestThrottle(129, throtListen, true);
        Assert.assertEquals("address request message", "[78] 40 C0 81",
            tc.outbound.elementAt(tc.outbound.size() - 1).toString()); // OPC 0x40 RLOC Request Loco
        Assert.assertEquals("count is correct", 1, tc.outbound.size());
        CanReply r = new CanReply( new int[]{CbusConstants.CBUS_ERR, 0xc0, 0x81, 0x02 },0x12 ); // Loco address taken
        cbtm.reply(r);
        jmri.util.JUnitAppender.assertErrorMessage("Throttle request failed for 129(L) because Loco address 129 taken");
        Assert.assertTrue("No steal or share request", failedThrottleRequest);
        throtListen = null;
    }

    @Test
    public void testCreateCbusThrottleStealScenario2() {
        
        ThrottleListener throtListen = new ThrottleListener() {
            @Override
            public void notifyThrottleFound(DccThrottle t) {
                throttle = t;
                log.error("created a throttle");
            }

            @Override
            public void notifyFailedThrottleRequest(LocoAddress address, String reason) {
                log.error("Throttle request failed for " + address + " because " + reason);
                failedThrottleRequest = true;
            }
            
            /**
             * @deprecated since 4.15.7; use #notifyDecisionRequired
             */
            @Deprecated
            @Override
            public void notifyStealThrottleRequired(LocoAddress address){
            }

            @Override
            public void notifyDecisionRequired(LocoAddress address, DecisionType question) {
                
                // this is a never-steal or sharing impelementation.
                if ( question == DecisionType.STEAL ){
                    InstanceManager.throttleManagerInstance().cancelThrottleRequest(address, this);
                    log.error("1: Got a Steal question {}",address);
                }
                if ( question == DecisionType.SHARE ){
                    InstanceManager.throttleManagerInstance().cancelThrottleRequest(address, this);
                    log.error("1: Got a Share question {}",address);
                }
                if ( question == DecisionType.STEAL_OR_SHARE ){
                    InstanceManager.throttleManagerInstance().cancelThrottleRequest(address, this);
                    log.error("1: Got a steal OR share question {}",address);
                }
            }
        };
        
        TrafficControllerScaffold tc = new TrafficControllerScaffold();
        memo = new CanSystemConnectionMemo();
        memo.setTrafficController(tc);
        CbusThrottleManager cbtm = new CbusThrottleManager(memo);
        InstanceManager.setThrottleManager( cbtm );

        CbusNodeTableDataModel nodemodel = new CbusNodeTableDataModel(memo, 3,CbusNodeTableDataModel.MAX_COLUMN);        
        InstanceManager.store(nodemodel, CbusNodeTableDataModel.class);
        InstanceManager.store(new CbusCommandStation(memo), jmri.CommandStation.class);
        // register a command station in the node table so can be found by the Command Station
        CbusNode cs = nodemodel.provideNodeByNodeNum(65534);
        cs.setCsNum(0); // Command Station 0 is master command station
        cs.setParameters(new int[]{7,165,4,10,0,0,255,4}); // in this test we emaulate a CANCMD v4
        cs.setNV(2, 0b00000110); // steal + share enabled
        
        cbtm.requestThrottle(141, throtListen, true);
        Assert.assertEquals("address request message", "[78] 40 C0 8D",
            tc.outbound.elementAt(tc.outbound.size() - 1).toString()); // OPC 0x40 RLOC Request Loco
        Assert.assertEquals("count is correct", 1, tc.outbound.size());
        
        CanReply r = new CanReply( new int[]{CbusConstants.CBUS_ERR, 0xc0, 0x8d, 0x02 },0x12 ); // Loco address taken
        cbtm.reply(r);
        
        jmri.util.JUnitAppender.assertErrorMessage("1: Got a steal OR share question 141(L)");
        // which was cancelled by the throttlelistener
        
        cs.setNV(2, 0b00000010); // steal only enabled
        
        
        cbtm.requestThrottle(141, throtListen, true);
        Assert.assertEquals("address request message", "[78] 40 C0 8D",
            tc.outbound.elementAt(tc.outbound.size() - 1).toString()); // OPC 0x40 RLOC Request Loco
        Assert.assertEquals("count is correct", 2, tc.outbound.size());
        
        r = new CanReply( new int[]{CbusConstants.CBUS_ERR, 0xc0, 0x8d, 0x02 },0x12 ); // Loco address taken
        cbtm.reply(r);
        
        jmri.util.JUnitAppender.assertErrorMessage("1: Got a Steal question 141(L)");
        // which was cancelled by the throttlelistener

        cs.setNV(2, 0b00000100); // share only enabled
        
        cbtm.requestThrottle(141, throtListen, true);
        Assert.assertEquals("address request message", "[78] 40 C0 8D",
            tc.outbound.elementAt(tc.outbound.size() - 1).toString()); // OPC 0x61 RLOC Request Loco
        Assert.assertEquals("count is correct", 3, tc.outbound.size());
        
        r = new CanReply( new int[]{CbusConstants.CBUS_ERR, 0xc0, 0x8d, 0x02 },0x12 ); // Loco address taken
        cbtm.reply(r);
        
        jmri.util.JUnitAppender.assertErrorMessage("1: Got a Share question 141(L)");
        // which was cancelled by the throttlelistener
        
        throtListen = null;
        nodemodel.dispose();
    }

    // test default share attempt when no options checked
    @Test
    public void testCreateCbusThrottleDefaultShareScenario() {
        
        ThrottleListener throtListen = new ThrottleListener() {
            @Override
            public void notifyThrottleFound(DccThrottle t) {
                throttle = t;
                log.error("created a throttle");
            }

            @Override
            public void notifyFailedThrottleRequest(LocoAddress address, String reason) {
                log.error("Throttle request failed for " + address + " because " + reason);
                failedThrottleRequest = true;
            }
            
            /**
             * @deprecated since 4.15.7; use #notifyDecisionRequired
             */
            @Deprecated
            @Override
            public void notifyStealThrottleRequired(LocoAddress address){
            }

            @Override
            public void notifyDecisionRequired(LocoAddress address, DecisionType question) {
                
                if ( question == DecisionType.STEAL ){
                    InstanceManager.throttleManagerInstance().cancelThrottleRequest(address, this);
                    log.error("1: Got a Steal question {}",address);
                }
                if ( question == DecisionType.SHARE ){
                    InstanceManager.throttleManagerInstance().cancelThrottleRequest(address, this);
                    log.error("1: Got a Share question {}",address);
                }
                if ( question == DecisionType.STEAL_OR_SHARE ){
                    InstanceManager.throttleManagerInstance().cancelThrottleRequest(address, this);
                    log.error("1: Got a steal OR share question {}",address);
                }
            }
        };
        
        TrafficControllerScaffold tc = new TrafficControllerScaffold();
        memo = new CanSystemConnectionMemo();
        memo.setTrafficController(tc);
        CbusThrottleManager cbtm =  new CbusThrottleManager(memo);
        InstanceManager.setThrottleManager( cbtm );

        CbusNodeTableDataModel nodemodel = new CbusNodeTableDataModel(memo, 3,CbusNodeTableDataModel.MAX_COLUMN);        
        InstanceManager.store(nodemodel, CbusNodeTableDataModel.class);
        InstanceManager.store(new CbusCommandStation(memo), jmri.CommandStation.class);
        // register a command station in the node table so can be found by the Command Station
        CbusNode cs = nodemodel.provideNodeByNodeNum(65534);
        cs.setCsNum(0); // Command Station 0 is master command station
        cs.setParameters(new int[]{7,165,4,10,0,0,255,4}); // in this test we emaulate a CANCMD v4
        cs.setNV(2, 0b00000110); // steal + share enabled
        
        cbtm.requestThrottle(141, throtListen, false);
        Assert.assertEquals("address request message", "[78] 40 C0 8D",
            tc.outbound.elementAt(tc.outbound.size() - 1).toString()); // OPC 0x40 RLOC Request Loco
        Assert.assertEquals("count is correct", 1, tc.outbound.size());
        
        CanReply r = new CanReply( new int[]{CbusConstants.CBUS_ERR, 0xc0, 0x8d, 0x02 },0x12 ); // Loco address taken
        cbtm.reply(r);
        
        Assert.assertEquals("address request message", "[78] 61 C0 8D 02",
            tc.outbound.elementAt(tc.outbound.size() - 1).toString()); // OPC 0x40 GLOC Request Share Loco
        Assert.assertEquals("count is correct", 2, tc.outbound.size());
        Assert.assertFalse("not yet failed", failedThrottleRequest);
        
        r = new CanReply( new int[]{CbusConstants.CBUS_ERR, 0xc0, 0x8d, 0x02 },0x12 ); // Loco address taken
        cbtm.reply(r);
        
        Assert.assertTrue("Failed on 2nd attempt", failedThrottleRequest);
        jmri.util.JUnitAppender.assertErrorMessage("Throttle request failed for 141(L) because Loco address 141 taken");
        
        throtListen = null;
        nodemodel.dispose();
    }

    // test steal attempt when silent steal checked
    @Test
    public void testCreateCbusThrottleSilentStealCheckedScenario() {
        
        ThrottleListener throtListen = new ThrottleListener() {
            @Override
            public void notifyThrottleFound(DccThrottle t) {
                throttle = t;
                log.error("created a throttle");
            }

            @Override
            public void notifyFailedThrottleRequest(LocoAddress address, String reason) {
                log.error("Throttle request failed for " + address + " because " + reason);
                failedThrottleRequest = true;
            }
            
            /**
             * @deprecated since 4.15.7; use #notifyDecisionRequired
             */
            @Deprecated
            @Override
            public void notifyStealThrottleRequired(LocoAddress address){
            }

            @Override
            public void notifyDecisionRequired(LocoAddress address, DecisionType question) {
                
                if ( question == DecisionType.STEAL ){
                    InstanceManager.throttleManagerInstance().cancelThrottleRequest(address, this);
                    log.error("1: Got a Steal question {}",address);
                }
                if ( question == DecisionType.SHARE ){
                    InstanceManager.throttleManagerInstance().cancelThrottleRequest(address, this);
                    log.error("1: Got a Share question {}",address);
                }
                if ( question == DecisionType.STEAL_OR_SHARE ){
                    InstanceManager.throttleManagerInstance().cancelThrottleRequest(address, this);
                    log.error("1: Got a steal OR share question {}",address);
                }
            }
        };
        
        TrafficControllerScaffold tc = new TrafficControllerScaffold();
        memo = new CanSystemConnectionMemo();
        memo.setTrafficController(tc);
        CbusThrottleManager cbtm = new CbusThrottleManager(memo);
        InstanceManager.setThrottleManager( cbtm );

        CbusNodeTableDataModel nodemodel = new CbusNodeTableDataModel(memo, 3,CbusNodeTableDataModel.MAX_COLUMN);        
        InstanceManager.store(nodemodel, CbusNodeTableDataModel.class);
        InstanceManager.store(new CbusCommandStation(memo), jmri.CommandStation.class);
        // register a command station in the node table so can be found by the Command Station
        CbusNode cs = nodemodel.provideNodeByNodeNum(65534);
        cs.setCsNum(0); // Command Station 0 is master command station
        cs.setParameters(new int[]{7,165,4,10,0,0,255,4}); // in this test we emaulate a CANCMD v4
        cs.setNV(2, 0b00000110); // steal + share enabled
        
        // set ThrottlesPreferences to steal enabled
        
        jmri.jmrit.throttle.ThrottlesPreferences pref = new jmri.jmrit.throttle.ThrottlesPreferences();
        pref.setSilentSteal(true);
        jmri.InstanceManager.store(pref, jmri.jmrit.throttle.ThrottlesPreferences.class);
        
        
        cbtm.requestThrottle(141, throtListen, false);
        Assert.assertEquals("address request message", "[78] 40 C0 8D",
            tc.outbound.elementAt(tc.outbound.size() - 1).toString()); // OPC 0x40 RLOC Request Loco
        Assert.assertEquals("count is correct", 1, tc.outbound.size());
        
        CanReply r = new CanReply( new int[]{CbusConstants.CBUS_ERR, 0xc0, 0x8d, 0x02 },0x12 ); // Loco address taken
        cbtm.reply(r);
        
        Assert.assertEquals("address request message", "[78] 61 C0 8D 01",
            tc.outbound.elementAt(tc.outbound.size() - 1).toString()); // OPC 0x40 GLOC Request Steal Loco
        Assert.assertEquals("count is correct", 2, tc.outbound.size());
        Assert.assertFalse("not yet failed", failedThrottleRequest);
        
        r = new CanReply( new int[]{CbusConstants.CBUS_ERR, 0xc0, 0x8d, 0x02 },0x12 ); // Loco address taken
        cbtm.reply(r);
        
        Assert.assertTrue("Failed on 2nd attempt", failedThrottleRequest);
        jmri.util.JUnitAppender.assertErrorMessage("Throttle request failed for 141(L) because Loco address 141 taken");
        
        throtListen = null;
        nodemodel.dispose();
    }

    // test default steal attempt when no options checked
    @Test
    public void testCreateCbusThrottleDefaultNoShareavailableScenario() {
        
        ThrottleListener throtListen = new ThrottleListener() {
            @Override
            public void notifyThrottleFound(DccThrottle t) {
                throttle = t;
                log.error("created a throttle");
            }

            @Override
            public void notifyFailedThrottleRequest(LocoAddress address, String reason) {
                log.error("Throttle request failed for " + address + " because " + reason);
                failedThrottleRequest = true;
            }
            
            /**
             * @deprecated since 4.15.7; use #notifyDecisionRequired
             */
            @Deprecated
            @Override
            public void notifyStealThrottleRequired(LocoAddress address){
            }

            @Override
            public void notifyDecisionRequired(LocoAddress address, DecisionType question) {
                
                if ( question == DecisionType.STEAL ){
                    InstanceManager.throttleManagerInstance().cancelThrottleRequest(address, this);
                    log.error("1: Got a Steal question {}",address);
                }
                if ( question == DecisionType.SHARE ){
                    InstanceManager.throttleManagerInstance().cancelThrottleRequest(address, this);
                    log.error("1: Got a Share question {}",address);
                }
                if ( question == DecisionType.STEAL_OR_SHARE ){
                    InstanceManager.throttleManagerInstance().cancelThrottleRequest(address, this);
                    log.error("1: Got a steal OR share question {}",address);
                }
            }
        };
        
        TrafficControllerScaffold tc = new TrafficControllerScaffold();
        memo = new CanSystemConnectionMemo();
        memo.setTrafficController(tc);
        CbusThrottleManager cbtm = new CbusThrottleManager(memo);
        // InstanceManager.setThrottleManager( cbtm );

        CbusNodeTableDataModel nodemodel = new CbusNodeTableDataModel(memo, 3,CbusNodeTableDataModel.MAX_COLUMN);        
        InstanceManager.store(nodemodel, CbusNodeTableDataModel.class);
        InstanceManager.store(new CbusCommandStation(memo), jmri.CommandStation.class);
        // register a command station in the node table so can be found by the Command Station
        CbusNode cs = nodemodel.provideNodeByNodeNum(65534);
        cs.setCsNum(0); // Command Station 0 is master command station
        cs.setParameters(new int[]{7,165,4,10,0,0,255,4}); // in this test we emaulate a CANCMD v4
        cs.setNV(2, 0b00000100); // steal enabled
        
        cbtm.requestThrottle(141, throtListen, false);
        Assert.assertEquals("address request message", "[78] 40 C0 8D",
            tc.outbound.elementAt(tc.outbound.size() - 1).toString()); // OPC 0x40 RLOC Request Loco
        Assert.assertEquals("count is correct", 1, tc.outbound.size());
        
        CanReply r = new CanReply( new int[]{CbusConstants.CBUS_ERR, 0xc0, 0x8d, 0x02 },0x12 ); // Loco address taken
        cbtm.reply(r);
        
        Assert.assertEquals("address request message", "[78] 61 C0 8D 02",
            tc.outbound.elementAt(tc.outbound.size() - 1).toString()); // OPC 0x61 GLOC Request Share Loco
        Assert.assertEquals("count is correct", 2, tc.outbound.size());
        Assert.assertFalse("not yet failed", failedThrottleRequest);
        
        r = new CanReply( new int[]{CbusConstants.CBUS_ERR, 0xc0, 0x8d, 0x02 },0x12 ); // Loco address taken
        cbtm.reply(r);
        
        Assert.assertTrue("Failed on 2nd attempt", failedThrottleRequest);
        jmri.util.JUnitAppender.assertErrorMessage("Throttle request failed for 141(L) because Loco address 141 taken");
        
        throtListen = null;
        nodemodel.dispose();
    }

    // throttle acquired after 2nd level response
    @Test
    public void testCreateCbusThrottleSteal() {
        
        ThrottleListener throtListen = new ThrottleListener() {
            @Override
            public void notifyThrottleFound(DccThrottle t) {
                throttle = t;
                log.error("created a throttle");
            }

            @Override
            public void notifyFailedThrottleRequest(LocoAddress address, String reason) {
                log.error("Throttle request failed for " + address + " because " + reason);
                failedThrottleRequest = true;
            }
            
            /**
             * @deprecated since 4.15.7; use #notifyDecisionRequired
             */
            @Deprecated
            @Override
            public void notifyStealThrottleRequired(LocoAddress address){
            }

            @Override
            public void notifyDecisionRequired(LocoAddress address, DecisionType question) {
                
                
                // this is a never-steal or sharing impelementation.
                if ( question == DecisionType.STEAL ){
                    InstanceManager.throttleManagerInstance().responseThrottleDecision(address, null, DecisionType.STEAL );
                    log.error("1: Got a Steal question {}",address);
                }
                if ( question == DecisionType.SHARE ){
                    InstanceManager.throttleManagerInstance().cancelThrottleRequest(address, this);
                    log.error("1: Got a Share question {}",address);
                }
                if ( question == DecisionType.STEAL_OR_SHARE ){
                    InstanceManager.throttleManagerInstance().responseThrottleDecision(address, null, DecisionType.STEAL );
                    log.error("1: Got a steal OR share question {}",address);
                }
            }
        };
        
        TrafficControllerScaffold tc = new TrafficControllerScaffold();
        memo = new CanSystemConnectionMemo();
        memo.setTrafficController(tc);
        CbusThrottleManager cbtm = new CbusThrottleManager(memo);
        InstanceManager.setThrottleManager( cbtm );

        CbusNodeTableDataModel nodemodel = new CbusNodeTableDataModel(memo, 3,CbusNodeTableDataModel.MAX_COLUMN);        
        InstanceManager.store(nodemodel, CbusNodeTableDataModel.class);
        InstanceManager.store(new CbusCommandStation(memo), jmri.CommandStation.class);
        // register a command station in the node table so can be found by the Command Station
        CbusNode cs = nodemodel.provideNodeByNodeNum(65534);
        cs.setCsNum(0); // Command Station 0 is master command station
        cs.setParameters(new int[]{7,165,4,10,0,0,255,4}); // in this test we emaulate a CANCMD v4
        cs.setNV(2, 0b00000010); // steal enabled
        
        cbtm.requestThrottle(141, throtListen, true);
        Assert.assertEquals("address request message", "[78] 40 C0 8D",
            tc.outbound.elementAt(tc.outbound.size() - 1).toString()); // OPC 0x40 RLOC Request Loco
        Assert.assertEquals("count is correct", 1, tc.outbound.size());
        
        CanReply r = new CanReply( new int[]{CbusConstants.CBUS_ERR, 0xc0, 0x8d, 0x02 },0x12 ); // Loco address taken
        cbtm.reply(r);
        
        jmri.util.JUnitAppender.assertErrorMessage("1: Got a Steal question 141(L)");
        // which was confirmed please steal by the throttlelistener
        
        
        
        Assert.assertEquals("steal request message", "[78] 61 C0 8D 01",
            tc.outbound.elementAt(tc.outbound.size() - 1).toString()); // OPC 0x61 GLOC Request Steal Loco
        Assert.assertEquals("count is correct", 2, tc.outbound.size());
        Assert.assertFalse("not yet failed", failedThrottleRequest);
        
        r = new CanReply();
        r.setHeader(tc.getCanid());
        r.setNumDataElements(8);
        r.setElement(0, CbusConstants.CBUS_PLOC);
        r.setElement(1, 0x01); // session 1
        r.setElement(2, 0xc0); // dcc ms byte
        r.setElement(3, 0x8d); // dcc ls byte
        r.setElement(4, 0xa7); // speed direction
        r.setElement(5, 0xa2); // function f0 to f4
        r.setElement(6, 0x7b); // function f5 to f8
        r.setElement(7, 0x00); // function f9 to f12
        cbtm.reply(r);
        
        jmri.util.JUnitAppender.assertErrorMessage("created a throttle");
        
        throttle.dispose(throtListen);
        
        throtListen = null;
        nodemodel.dispose();
    }
    
    @Test
    public void testStealShareOptionsEnabled() {
        Assert.assertTrue("Can Silent Steal", tm.enablePrefSilentStealOption());
        Assert.assertTrue("Can Silent Share", tm.enablePrefSilentShareOption());
    }
    
    @Test
    public void testIsLongAddress() {
        
        Assert.assertFalse("local isLong 1", CbusThrottleManager.isLongAddress(1));
        Assert.assertFalse("local isLong 127", CbusThrottleManager.isLongAddress(127));
        Assert.assertTrue("local isLong 128", CbusThrottleManager.isLongAddress(128));
        Assert.assertTrue("local isLong 129", CbusThrottleManager.isLongAddress(129));
        
        Assert.assertFalse("can be long 0", tm.canBeLongAddress(0));
        Assert.assertTrue("can be long 1", tm.canBeLongAddress(1));
        
    }
    
    @Test
    public void testNotADccLocoAddress() {
        CbusThrottleManager cbtmb = (CbusThrottleManager)tm;
        
        cbtmb.responseThrottleDecision(null,null,null);
        JUnitAppender.assertErrorMessageStartsWith("null is not a DccLocoAddress");
    }
    
    @Test
    public void testCanErrorsReceived() {
        Assume.assumeFalse(java.awt.GraphicsEnvironment.isHeadless());
        CbusThrottleManager  ncbtm = (CbusThrottleManager)tm;
        CanReply r = new CanReply( 
            new int[]{CbusConstants.CBUS_ERR, 0x00, 0x00, CbusConstants.ERR_CAN_BUS_ERROR },0x11 );
        Thread checkCanErrorDialog = jmri.util.swing.JemmyUtil.createModalDialogOperatorThread(
            Bundle.getMessage("CBUS_ERROR"), Bundle.getMessage("ButtonOK"));
      
        // pass the message
        ncbtm.reply(r);
        
        JUnitAppender.assertErrorMessageStartsWith(Bundle.getMessage("ERR_CAN_BUS_ERROR"));
        JUnitUtil.waitFor(()->{return !(checkCanErrorDialog.isAlive());}, "checkCanErrorDialog finished");
    }

    @Test
    public void testInvalidRequestErrorsReceived() {
        Assume.assumeFalse(java.awt.GraphicsEnvironment.isHeadless());
        CbusThrottleManager  ncbtm = (CbusThrottleManager)tm;
        CanReply r = new CanReply( 
            new int[]{CbusConstants.CBUS_ERR, 0x00, 0x00, CbusConstants.ERR_INVALID_REQUEST },0x11 );
        Thread checkCbusInvalidRequestDialog = jmri.util.swing.JemmyUtil.createModalDialogOperatorThread(
            Bundle.getMessage("CBUS_ERROR"), Bundle.getMessage("ButtonOK"));
      
        // pass the message
        ncbtm.reply(r);
        
        JUnitAppender.assertErrorMessageStartsWith(Bundle.getMessage("ERR_INVALID_REQUEST"));
        JUnitUtil.waitFor(()->{return !(checkCbusInvalidRequestDialog.isAlive());}, "checkCbusInvalidRequestDialog finished");
    }
    
    private CanSystemConnectionMemo memo;
    private CbusDummyCS _cs;
    private DccThrottle throttle;
    private boolean failedThrottleRequest = false;
    
    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        TrafficControllerScaffoldLoopback tc = new TrafficControllerScaffoldLoopback(); // do not use this tc normally
        memo = new CanSystemConnectionMemo();
        memo.setTrafficController(tc);
        tm = new CbusThrottleManager(memo);
        InstanceManager.setThrottleManager( tm );
        _cs = new CbusDummyCS(memo); // we are testing the tm, not the command station
        _cs.setDelay(0); // no need to simulate network delay
    }

    @After
    public void tearDown() {
        CbusThrottleManager dtm = (CbusThrottleManager)tm;
        dtm.dispose();
        tm=null;
        dtm=null;
        _cs.dispose();
        _cs = null;
        memo.dispose();
        memo = null;
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

    private final static Logger log = LoggerFactory.getLogger(CbusThrottleManagerTest.class);

}
