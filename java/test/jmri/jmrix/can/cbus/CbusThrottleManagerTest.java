package jmri.jmrix.can.cbus;

import jmri.*;
import jmri.jmrit.throttle.ThrottlesPreferences;
import jmri.jmrix.can.*;
import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeTableDataModel;

import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class CbusThrottleManagerTest extends jmri.managers.AbstractThrottleManagerTestBase {

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists", tm);
    }

    @Test
    public void testIncomingFunctions() {
        Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));

        // CbusThrottleManager cbtm = (CbusThrottleManager) tm;
        Assert.assertNotNull("exists",tm);
        DccLocoAddress addr = new DccLocoAddress(1234,true);

        CbusThrottleListen throtListen = new CbusThrottleListen();
        tm.requestThrottle(addr,throtListen,true);

        Assert.assertEquals("address request message", "[78] 40 C4 D2",
            tc.outbound.elementAt(tc.outbound.size() - 1).toString()); // OPC 0x40 RLOC Request Loco
        Assert.assertEquals("count is correct", 1, tc.outbound.size());

        // Allocate engine session 1 with all functions set to 0
        CanReply r = new CanReply( new int[]{CbusConstants.CBUS_PLOC, 1, 0xc4, 0xd2, 0,0,0 },0x12 );
        ((CbusThrottleManager)tm).reply(r);

        JUnitUtil.waitFor(()-> (tm.getThrottleUsageCount(addr)>0), "throttle didn't create");
        Assert.assertFalse("F0 init",(boolean) tm.getThrottleInfo(addr,Throttle.F0));

        r = new CanReply( new int[]{CbusConstants.CBUS_DFUN, 1, 0x00, 0x00 },0x12 );
        ((CbusThrottleManager)tm).reply(r);

        r.setElement(2, 1);
        r.setElement(3, 0x1f);
        ((CbusThrottleManager)tm).reply(r);
        Assert.assertTrue("F0 on",(boolean) tm.getThrottleInfo(addr,Throttle.F0));
        Assert.assertTrue("F1 on",(boolean) tm.getThrottleInfo(addr,Throttle.F1));
        Assert.assertTrue("F2 on",(boolean) tm.getThrottleInfo(addr,Throttle.F2));
        Assert.assertTrue("F3 on",(boolean) tm.getThrottleInfo(addr,Throttle.F3));
        Assert.assertTrue("F4 on",(boolean) tm.getThrottleInfo(addr,Throttle.F4));
        r.setElement(3, 0x00);
        ((CbusThrottleManager)tm).reply(r);
        Assert.assertFalse("F0 off",(boolean) tm.getThrottleInfo(addr,Throttle.F0));
        Assert.assertFalse("F1 off",(boolean) tm.getThrottleInfo(addr,Throttle.F1));
        Assert.assertFalse("F2 off",(boolean) tm.getThrottleInfo(addr,Throttle.F2));
        Assert.assertFalse("F3 off",(boolean) tm.getThrottleInfo(addr,Throttle.F3));
        Assert.assertFalse("F4 off",(boolean) tm.getThrottleInfo(addr,Throttle.F4));
        r.setElement(2, 2);
        r.setElement(3, 0x0f);
        ((CbusThrottleManager)tm).reply(r);
        Assert.assertTrue("F5 on",(boolean) tm.getThrottleInfo(addr,Throttle.F5));
        Assert.assertTrue("F6 on",(boolean) tm.getThrottleInfo(addr,Throttle.F6));
        Assert.assertTrue("F7 on",(boolean) tm.getThrottleInfo(addr,Throttle.F7));
        Assert.assertTrue("F8 on",(boolean) tm.getThrottleInfo(addr,Throttle.F8));
        r.setElement(3, 0x00);
        ((CbusThrottleManager)tm).reply(r);
        Assert.assertFalse("F5 off",(boolean) tm.getThrottleInfo(addr,Throttle.F5));
        Assert.assertFalse("F6 off",(boolean) tm.getThrottleInfo(addr,Throttle.F6));
        Assert.assertFalse("F7 off",(boolean) tm.getThrottleInfo(addr,Throttle.F7));
        Assert.assertFalse("F8 off",(boolean) tm.getThrottleInfo(addr,Throttle.F8));

        r.setElement(2, 3);
        r.setElement(3, 0x0f);
        ((CbusThrottleManager)tm).reply(r);
        Assert.assertTrue("F9  on",(boolean) tm.getThrottleInfo(addr,Throttle.F9));
        Assert.assertTrue("F10 on",(boolean) tm.getThrottleInfo(addr,Throttle.F10));
        Assert.assertTrue("F11 on",(boolean) tm.getThrottleInfo(addr,Throttle.F11));
        Assert.assertTrue("F12 on",(boolean) tm.getThrottleInfo(addr,Throttle.F12));
        r.setElement(3, 0x00);
        ((CbusThrottleManager)tm).reply(r);
        Assert.assertFalse("F9  off",(boolean) tm.getThrottleInfo(addr,Throttle.F9));
        Assert.assertFalse("F10 off",(boolean) tm.getThrottleInfo(addr,Throttle.F10));
        Assert.assertFalse("F11 off",(boolean) tm.getThrottleInfo(addr,Throttle.F11));
        Assert.assertFalse("F12 off",(boolean) tm.getThrottleInfo(addr,Throttle.F12));

        r.setElement(2, 4);
        r.setElement(3, 0xff);
        ((CbusThrottleManager)tm).reply(r);
        Assert.assertTrue("F13 on",(boolean) tm.getThrottleInfo(addr,Throttle.F13));
        Assert.assertTrue("F14 on",(boolean) tm.getThrottleInfo(addr,Throttle.F14));
        Assert.assertTrue("F15 on",(boolean) tm.getThrottleInfo(addr,Throttle.F15));
        Assert.assertTrue("F16 on",(boolean) tm.getThrottleInfo(addr,Throttle.F16));
        Assert.assertTrue("F17 on",(boolean) tm.getThrottleInfo(addr,Throttle.F17));
        Assert.assertTrue("F18 on",(boolean) tm.getThrottleInfo(addr,Throttle.F18));
        Assert.assertTrue("F19 on",(boolean) tm.getThrottleInfo(addr,Throttle.F19));
        Assert.assertTrue("F20 on",(boolean) tm.getThrottleInfo(addr,Throttle.F20));

        r.setElement(3, 0x00);
        ((CbusThrottleManager)tm).reply(r);
        Assert.assertFalse("F13 off",(boolean) tm.getThrottleInfo(addr,Throttle.F13));
        Assert.assertFalse("F14 off",(boolean) tm.getThrottleInfo(addr,Throttle.F14));
        Assert.assertFalse("F15 off",(boolean) tm.getThrottleInfo(addr,Throttle.F15));
        Assert.assertFalse("F16 off",(boolean) tm.getThrottleInfo(addr,Throttle.F16));
        Assert.assertFalse("F17 off",(boolean) tm.getThrottleInfo(addr,Throttle.F17));
        Assert.assertFalse("F18 off",(boolean) tm.getThrottleInfo(addr,Throttle.F18));
        Assert.assertFalse("F19 off",(boolean) tm.getThrottleInfo(addr,Throttle.F19));
        Assert.assertFalse("F20 off",(boolean) tm.getThrottleInfo(addr,Throttle.F20));

        r.setElement(2, 5);
        r.setElement(3, 0xff);
        ((CbusThrottleManager)tm).reply(r);
        Assert.assertTrue("F21 on",(boolean) tm.getThrottleInfo(addr,Throttle.F21));
        Assert.assertTrue("F22 on",(boolean) tm.getThrottleInfo(addr,Throttle.F22));
        Assert.assertTrue("F23 on",(boolean) tm.getThrottleInfo(addr,Throttle.F23));
        Assert.assertTrue("F24 on",(boolean) tm.getThrottleInfo(addr,Throttle.F24));
        Assert.assertTrue("F25 on",(boolean) tm.getThrottleInfo(addr,Throttle.F25));
        Assert.assertTrue("F26 on",(boolean) tm.getThrottleInfo(addr,Throttle.F26));
        Assert.assertTrue("F27 on",(boolean) tm.getThrottleInfo(addr,Throttle.F27));
        Assert.assertTrue("F28 on",(boolean) tm.getThrottleInfo(addr,Throttle.F28));
        r.setElement(3, 0x00);
        ((CbusThrottleManager)tm).reply(r);
        Assert.assertFalse("F21 off",(boolean) tm.getThrottleInfo(addr,Throttle.F21));
        Assert.assertFalse("F22 off",(boolean) tm.getThrottleInfo(addr,Throttle.F22));
        Assert.assertFalse("F23 off",(boolean) tm.getThrottleInfo(addr,Throttle.F23));
        Assert.assertFalse("F24 off",(boolean) tm.getThrottleInfo(addr,Throttle.F24));
        Assert.assertFalse("F25 off",(boolean) tm.getThrottleInfo(addr,Throttle.F25));
        Assert.assertFalse("F26 off",(boolean) tm.getThrottleInfo(addr,Throttle.F26));
        Assert.assertFalse("F27 off",(boolean) tm.getThrottleInfo(addr,Throttle.F27));
        Assert.assertFalse("F28 off",(boolean) tm.getThrottleInfo(addr,Throttle.F28));

    }

    @Test
    public void testIncomingFunctionsDecimal() {

        Assert.assertNotNull("exists",tm);
        DccLocoAddress addr = new DccLocoAddress(221,true);

        CbusThrottleListen throtListen = new CbusThrottleListen();
        tm.requestThrottle(addr,throtListen,true);

        Assert.assertEquals("address request message", "[78] 40 C0 DD",
            tc.outbound.elementAt(tc.outbound.size() - 1).toString()); // OPC 0x40 RLOC Request Loco
        Assert.assertEquals("count is correct", 1, tc.outbound.size());

        // Allocate engine session 1 with all functions set to 0
        CanReply r = new CanReply( new int[]{CbusConstants.CBUS_PLOC, 1, 0xc0, 0xdd, 0,0,0 },0x12 );
        ((CbusThrottleManager)tm).reply(r);

        JUnitUtil.waitFor(()->{ return(tm.getThrottleInfo(addr,"F0")!=null); }, "throttle not created");
        r = new CanReply( new int[]{CbusConstants.CBUS_DFNON, 1, 0 },0x12 );
        r.setElement(2, 0xff);
        ((CbusThrottleManager)tm).reply(r);
        JUnitAppender.assertWarnMessageStartingWith("Unhandled update function number: 255");

        for ( int i=0 ; (i < 29 ) ; i++){
            String _f = "F" + i;
            r.setElement(0, CbusConstants.CBUS_DFNON);
            r.setElement(2, i);
            ((CbusThrottleManager)tm).reply(r);
            JUnitUtil.waitFor(()->{ return(tm.getThrottleInfo(addr,_f).equals(true)); }, "Function loop on " + i);
            // Assert.assertEquals("Function loop on " + i,true,cbtmb.getThrottleInfo(addr,_f));
            r.setElement(0, CbusConstants.CBUS_DFNOF);
            ((CbusThrottleManager)tm).reply(r);
            JUnitUtil.waitFor(()->{ return(tm.getThrottleInfo(addr,_f).equals(false)); }, "Function loop off " + i);
        }

    }

    @Test
    public void testIncomingSpeedDirection() {
        Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));

        CbusThrottleManager cbtmb = (CbusThrottleManager) tm;
        Assert.assertNotNull("exists",cbtmb);
        DccLocoAddress addr = new DccLocoAddress(422,true);

        CbusThrottleListen throtListen = new CbusThrottleListen();
        cbtmb.requestThrottle(addr,throtListen,true);

        Assert.assertEquals("address request message", "[78] 40 C1 A6",
            tc.outbound.elementAt(tc.outbound.size() - 1).toString()); // OPC 0x40 RLOC Request Loco
        Assert.assertEquals("count is correct", 1, tc.outbound.size());

        // Allocate engine session 1 with all functions set to 0
        CanReply r = new CanReply( new int[]{CbusConstants.CBUS_PLOC, 1, 0xc1, 0xa6, 0,0,0 },0x12 );
        ((CbusThrottleManager)tm).reply(r);

        JUnitUtil.waitFor(()->{ return(cbtmb.getThrottleInfo(addr,"F0")!=null); }, "reply didn't arrive");

        Assert.assertEquals("speed setting",0f,(float) cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.01f);
        Assert.assertEquals("speed increment",(1.0f/126.0f),(float) cbtmb.getThrottleInfo(addr,Throttle.SPEEDINCREMENT),0.1f);
        Assert.assertEquals("speed step mode",SpeedStepMode.NMRA_DCC_128,cbtmb.getThrottleInfo(addr,Throttle.SPEEDSTEPMODE));

        r = new CanReply( new int[]{CbusConstants.CBUS_DSPD, 1, 0 },0x12 );
        cbtmb.reply(r);
        Assert.assertEquals("speed setting 0",0.0f,(float) cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.01f);

        JUnitUtil.waitFor(()->{ return(cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD).equals(false)); }, "Throttle didn't update");
        Assert.assertFalse("is forward 0",(boolean) cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD));

        r = new CanReply( new int[]{CbusConstants.CBUS_DSPD, 77, 77 },0x12 );
        cbtmb.reply(r);
        Assert.assertEquals("different session speed unchanged",0.0f,(float) cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.01f);

        r = new CanReply( new int[]{CbusConstants.CBUS_DSPD, 1, 1 },0x12 );
        cbtmb.reply(r);
        Assert.assertEquals("speed setting 1",-1.0f,(float) cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.01f);
        Assert.assertFalse("is forward 1",(boolean) cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD));

        r.setElement(2, 2);
        cbtmb.reply(r);
        Assert.assertEquals("speed setting 2",(1.0f/126.0f),(float) cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.1f);
        Assert.assertFalse("is forward 2",(boolean) cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD));

        r.setElement(2, 77);
        cbtmb.reply(r);
        Assert.assertEquals("speed setting 77",(76.0f/126.0f),(float) cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.1f);
        Assert.assertFalse("is forward 77",(boolean) cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD));

        r.setElement(2, 126);
        cbtmb.reply(r);
        Assert.assertEquals("speed setting 126",(125.0f/126.0f),(float) cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.1f);
        Assert.assertFalse("is forward 126",(boolean) cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD));

        r.setElement(2, 127);
        cbtmb.reply(r);
        Assert.assertEquals("speed setting 127",(1.0f),(float) cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.01f);
        Assert.assertFalse("is forward 127",(boolean) cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD));

        r.setElement(2, 128);
        cbtmb.reply(r);
        Assert.assertEquals("speed setting 128",(0.0f),(float) cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.01f);
        Assert.assertTrue("is forward 128",(boolean) cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD));

        r.setElement(2, 129);
        cbtmb.reply(r);
        Assert.assertEquals("speed setting 129",(-1.0f),(float) cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.01f);
        Assert.assertTrue("is forward 129",(boolean) cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD));

        r.setElement(2, 130);
        cbtmb.reply(r);
        Assert.assertEquals("speed setting 130",(1.0f/126.0f),(float) cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.1f);
        Assert.assertTrue("is forward 130",(boolean) cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD));

        r.setElement(2, 211);
        cbtmb.reply(r);
        Assert.assertEquals("speed setting 211",(82.0f/126.0f),(float) cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.1f);
        Assert.assertTrue("is forward 211",(boolean) cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD));

        r.setElement(2, 254);
        cbtmb.reply(r);
        Assert.assertEquals("speed setting 254",(125.0f/126.0f),(float) cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.1f);
        Assert.assertTrue("is forward 254",(boolean) cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD));

        r.setElement(2, 255);
        cbtmb.reply(r);
        Assert.assertEquals("speed setting 255",(1.0f),(float) cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.01f);
        Assert.assertTrue("is forward 255",(boolean) cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD));

        r = new CanReply( new int[]{CbusConstants.CBUS_ESTOP },0x12 );
        cbtmb.reply(r);
        Assert.assertEquals("speed setting estop",(-1.0f),(float) cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.01f);
        Assert.assertTrue("estop forward",(boolean) cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD));

        r = new CanReply( new int[]{CbusConstants.CBUS_DSPD, 1, 77 },0x12 );
        cbtmb.reply(r);
        Assert.assertEquals("speed setting before reverse estop 77",(76.0f/126.0f),(float) cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.01f);
        Assert.assertFalse("is forward b4 estop 77",(boolean) cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD));
        r = new CanReply( new int[]{CbusConstants.CBUS_ESTOP },0x12 );
        cbtmb.reply(r);
        Assert.assertEquals("speed setting estop",(-1.0f),(float) cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.1f);
        Assert.assertFalse("estop reverse",(boolean) cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD));

        r = new CanReply( new int[]{CbusConstants.CBUS_DSPD, 1, 255 },0x12 );
        cbtmb.reply(r);
        Assert.assertEquals("speed setting 255",(1.0f),(float) cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.01f);
        Assert.assertTrue("is forward 255",(boolean) cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD));

        r = new CanReply( new int[]{CbusConstants.CBUS_RESTP },0x12 );
        cbtmb.reply(r);
        Assert.assertEquals("speed setting r estop",(-1.0f),(float) cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.01f);
        Assert.assertTrue("restop forward",(boolean) cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD));


        r = new CanReply( new int[]{CbusConstants.CBUS_DSPD, 1, 77 },0x12 );
        cbtmb.reply(r);
        Assert.assertEquals("speed setting before reverse restop 77",(76.0f/126.0f),(float) cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.01f);
        Assert.assertFalse("is forward b4 restop 77",(boolean) cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD));
        r = new CanReply( new int[]{CbusConstants.CBUS_RESTP },0x12 );
        cbtmb.reply(r);
        Assert.assertEquals("speed setting restop",(-1.0f),(float) cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.01f);
        Assert.assertFalse("restop reverse",(boolean) cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD));

    }

    @Test
    public void testMessage() {
        Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));

        Assert.assertNotNull("exists",tm);
        DccLocoAddress addr = new DccLocoAddress(1234,true);

        CbusThrottleListen throtListen = new CbusThrottleListen();
        tm.requestThrottle(addr,throtListen,true);

        Assert.assertEquals("address request message", "[78] 40 C4 D2",
            tc.outbound.elementAt(tc.outbound.size() - 1).toString()); // OPC 0x40 RLOC Request Loco
        Assert.assertEquals("count is correct", 1, tc.outbound.size());

        // Allocate engine session 1 with all functions set to 0
        CanReply r = new CanReply( new int[]{CbusConstants.CBUS_PLOC, 1, 0xc4, 0xd2, 0,0,0 },0x12 );
        ((CbusThrottleManager)tm).reply(r);

        JUnitUtil.waitFor(()->{ return(tm.getThrottleInfo(addr,"F0")!=null); }, "Throttle didn't create");

        r = new CanReply( new int[]{CbusConstants.CBUS_DSPD, 1, 88 },0x12 );
        r.setExtended(true);
        ((CbusThrottleManager)tm).reply(r);
        Assert.assertTrue((float)tm.getThrottleInfo(addr,Throttle.SPEEDSETTING)==0f);

        r.setExtended(false);
        r.setRtr(true);
        ((CbusThrottleManager)tm).reply(r);
        Assert.assertTrue((float)tm.getThrottleInfo(addr,Throttle.SPEEDSETTING)==0f);

        r = new CanReply( new int[]{CbusConstants.CBUS_DSPD, 1, 77 },0x12 );
        // rtr and extended both false by default

        ((CbusThrottleManager)tm).reply(r);

        JUnitUtil.waitFor(()->{ return(((float)tm.getThrottleInfo(addr,Throttle.SPEEDSETTING))!=0f); }, "Speed command not received");
        Assert.assertEquals("msg speed setting before reverse estop 77",(76.0f/126.0f),(float) tm.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.1f);
        Assert.assertEquals("msg is forward b4 estop 77",false, tm.getThrottleInfo(addr,Throttle.ISFORWARD));
        CanMessage m = new CanMessage( new int[]{CbusConstants.CBUS_RESTP },0x12 );
        ((CbusThrottleManager)tm).message(m);
        Assert.assertEquals("msg speed setting estop",(-1.0f),(float) tm.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.01f);
        Assert.assertFalse("msg estop reverse",(boolean) tm.getThrottleInfo(addr,Throttle.ISFORWARD));

        r = new CanReply( new int[]{CbusConstants.CBUS_DSPD, 44, 77 },0x12 );
        ((CbusThrottleManager)tm).reply(r);
        Assert.assertEquals("msg speed setting unchanged wrong address",(-1.0f),(float) tm.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.01f);

        r = new CanReply( new int[]{CbusConstants.CBUS_DSPD, 1, 77 },0x12 );
        ((CbusThrottleManager)tm).reply(r);
        Assert.assertEquals("msg speed setting before reverse estop 77",(76.0f/126.0f),(float) tm.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.1f);
        Assert.assertFalse("msg is forward b4 estop 77",(boolean) tm.getThrottleInfo(addr,Throttle.ISFORWARD));
        m = new CanMessage( new int[]{CbusConstants.CBUS_ESTOP },0x12 );

        m.setExtended(true);
        ((CbusThrottleManager)tm).message(m);
        Assert.assertEquals("msg speed setting Extended CAN ignored",(76.0f/126.0f),(float) tm.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.1f);

        m.setExtended(false);
        m.setRtr(true);
        ((CbusThrottleManager)tm).message(m);
        Assert.assertEquals("msg speed setting Rtr CAN ignored",(76.0f/126.0f),(float) tm.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.1f);

        m.setRtr(false);
        ((CbusThrottleManager)tm).message(m);

        Assert.assertEquals("msg speed setting estop",(-1.0f),(float) tm.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.01f);
        Assert.assertFalse("msg estop reverse",(boolean) tm.getThrottleInfo(addr,Throttle.ISFORWARD));

        r = new CanReply( new int[]{CbusConstants.CBUS_DSPD, 1, 77 },0x12 );
        ((CbusThrottleManager)tm).reply(r);
        Assert.assertEquals("msg speed setting before reverse estop 77",(76.0f/126.0f),(float) tm.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.1f);
        Assert.assertFalse("msg is forward b4 estop 77",(boolean) tm.getThrottleInfo(addr,Throttle.ISFORWARD));
        m = new CanMessage( new int[]{CbusConstants.CBUS_DSPD, 1, 99 },0x12 );
        ((CbusThrottleManager)tm).message(m);
        Assert.assertEquals("msg speed does not change",(76.0f/126.0f),(float) tm.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.1f);
        Assert.assertEquals("msg estop reverse",false, tm.getThrottleInfo(addr,Throttle.ISFORWARD));

        m.setElement(2, 1);
        ((CbusThrottleManager)tm).message(m);
        Assert.assertEquals("msg speed change estop",(-1.0f),(float) tm.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.1f);
        Assert.assertFalse("msg estop reverse",(boolean) tm.getThrottleInfo(addr,Throttle.ISFORWARD));

        m.setElement(2, 129);
        ((CbusThrottleManager)tm).message(m);
        Assert.assertEquals("msg speed change estop",(-1.0f),(float) tm.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.1f);
        Assert.assertTrue("msg estop reverse",(boolean) tm.getThrottleInfo(addr,Throttle.ISFORWARD));

        m = new CanMessage( new int[]{CbusConstants.CBUS_KLOC, 22 },0x12 );
        ((CbusThrottleManager)tm).message(m);
        Assert.assertTrue("addr unchanged as no session number match",(boolean) tm.getThrottleInfo(addr,Throttle.ISFORWARD));

        m = new CanMessage( new int[]{CbusConstants.CBUS_KLOC, 1 },0x12 );
        ((CbusThrottleManager)tm).message(m);

        m = new CanMessage( new int[]{CbusConstants.CBUS_DSPD, 1, 11 },0x12 );
        ((CbusThrottleManager)tm).message(m);
        Assert.assertEquals("msg speed change ignored as session cancelled",(-1.0f),(float) tm.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.1f);

    }

    @Test
    public void testCbdispose() {

        DccLocoAddress addr = new DccLocoAddress(555,true);
        Assert.assertEquals("throttle use 0", 0, tm.getThrottleUsageCount(addr));
        CbusThrottleListen throtListen = new CbusThrottleListen();
        tm.requestThrottle(addr,throtListen,true);

        Assert.assertEquals("address request message", "[78] 40 C2 2B",
            tc.outbound.elementAt(tc.outbound.size() - 1).toString()); // OPC 0x40 RLOC Request Loco
        Assert.assertEquals("count is correct", 1, tc.outbound.size());

        // Allocate engine session 1 with all functions set to 0
        CanReply r = new CanReply( new int[]{CbusConstants.CBUS_PLOC, 1, 0xc2, 0x2b, 0,0,0 },0x12 );
        ((CbusThrottleManager)tm).reply(r);

        CbusThrottle cbt = new CbusThrottle(memo,addr,1);
        JUnitUtil.waitFor(()->{ return(tm.getThrottleUsageCount(addr)>0); }, "Throttle count did not increase");
        Assert.assertEquals("throttle use 1", 1, tm.getThrottleUsageCount(addr));

        Assert.assertTrue(tm.disposeThrottle(cbt,throtListen));
        JUnitUtil.waitFor(()->{ return(tm.getThrottleUsageCount(addr)==0); },
            "Throttle Count did not go 0 on dispose, add retry rule for this if regular?");
        Assert.assertEquals("disposed throttle use 0", 0, tm.getThrottleUsageCount(addr));
        Assert.assertNull("NULL",tm.getThrottleInfo(addr,Throttle.F28));

        Assert.assertFalse(tm.disposeThrottle(null,throtListen));

    }

    private static class CbusThrottleListen implements ThrottleListener {

        @Override
        public void notifyThrottleFound(DccThrottle t){
            // throttleFoundResult = true;
        }

        @Override
        public void notifyFailedThrottleRequest(LocoAddress address, String reason){
            // throttleNotFoundResult = true;
        }

        @Override
        public void notifyDecisionRequired(LocoAddress address, DecisionType question) {
            // this is a WIP impelementation.
            // if ( question == DecisionType.STEAL ){
            // }
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
                log.error("Throttle request failed for {} because {}", address, reason);
                failedThrottleRequest = true;
            }

            @Override
            public void notifyDecisionRequired(LocoAddress address, DecisionType question) {
                // this is a never-stealing impelementation.
                if ( question == DecisionType.STEAL ){
                    InstanceManager.throttleManagerInstance().cancelThrottleRequest(address, this);
                    log.error("1: Got a steal request {}", address);
                }
                if ( question == DecisionType.SHARE ){
                    InstanceManager.throttleManagerInstance().cancelThrottleRequest(address, this);
                    log.error("1: Got a share request {}", address);
                }
                if ( question == DecisionType.STEAL_OR_SHARE ){
                    InstanceManager.throttleManagerInstance().cancelThrottleRequest(address, this);
                    log.error("1: Got a steal OR share question {}", address);
                }
            }
        };

        memo.setDisabled(true); // disable access to memo.get(CommandStation.class);

        tm.requestThrottle(129, throtListen, true);
        Assert.assertEquals("address request message", "[78] 40 C0 81",
            tc.outbound.elementAt(tc.outbound.size() - 1).toString()); // OPC 0x40 RLOC Request Loco
        Assert.assertEquals("count is correct", 1, tc.outbound.size());
        CanReply r = new CanReply( new int[]{CbusConstants.CBUS_ERR, 0xc0, 0x81, 0x02 },0x12 ); // Loco address taken
        ((CbusThrottleManager)tm).reply(r);
        JUnitAppender.assertErrorMessage("Throttle request failed for 129(L) because Loco address 129 taken");
        Assert.assertTrue("No steal or share request", failedThrottleRequest);
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
                log.error("Throttle request failed for {} because {}", address, reason);
                failedThrottleRequest = true;
            }

            @Override
            public void notifyDecisionRequired(LocoAddress address, DecisionType question) {

                // this is a never-steal or sharing impelementation.
                if ( question == DecisionType.STEAL ){
                    InstanceManager.throttleManagerInstance().cancelThrottleRequest(address, this);
                    log.error("1: Got a Steal question {}", address);
                }
                if ( question == DecisionType.SHARE ){
                    InstanceManager.throttleManagerInstance().cancelThrottleRequest(address, this);
                    log.error("1: Got a Share question {}", address);
                }
                if ( question == DecisionType.STEAL_OR_SHARE ){
                    InstanceManager.throttleManagerInstance().cancelThrottleRequest(address, this);
                    log.error("1: Got a steal OR share question {}", address);
                }
            }
        };

        CbusNodeTableDataModel nodemodel = memo.get(CbusConfigurationManager.class)
            .provide(CbusNodeTableDataModel.class);

        // register a command station in the node table so can be found by the Command Station
        CbusNode cs = nodemodel.provideNodeByNodeNum(65534);
        cs.setCsNum(0); // Command Station 0 is master command station
        cs.getNodeParamManager().setParameters(new int[]{7,165,4,10,0,0,255,4}); // in this test we emaulate a CANCMD v4
        cs.getNodeNvManager().setNV(2, 0b00000110); // steal + share enabled

        tm.requestThrottle(141, throtListen, true);
        Assert.assertEquals("address request message", "[78] 40 C0 8D",
            tc.outbound.elementAt(tc.outbound.size() - 1).toString()); // OPC 0x40 RLOC Request Loco
        Assert.assertEquals("count is correct", 1, tc.outbound.size());

        CanReply r = new CanReply( new int[]{CbusConstants.CBUS_ERR, 0xc0, 0x8d, 0x02 },0x12 ); // Loco address taken
        ((CbusThrottleManager)tm).reply(r);

        JUnitAppender.assertErrorMessage("1: Got a steal OR share question 141(L)");
        // which was cancelled by the throttlelistener

        cs.getNodeNvManager().setNV(2, 0b00000010); // steal only enabled


        tm.requestThrottle(141, throtListen, true);
        Assert.assertEquals("address request message", "[78] 40 C0 8D",
            tc.outbound.elementAt(tc.outbound.size() - 1).toString()); // OPC 0x40 RLOC Request Loco
        Assert.assertEquals("count is correct", 2, tc.outbound.size());

        r = new CanReply( new int[]{CbusConstants.CBUS_ERR, 0xc0, 0x8d, 0x02 },0x12 ); // Loco address taken
        ((CbusThrottleManager)tm).reply(r);

        JUnitAppender.assertErrorMessage("1: Got a Steal question 141(L)");
        // which was cancelled by the throttlelistener

        cs.getNodeNvManager().setNV(2, 0b00000100); // share only enabled

        tm.requestThrottle(141, throtListen, true);
        Assert.assertEquals("address request message", "[78] 40 C0 8D",
            tc.outbound.elementAt(tc.outbound.size() - 1).toString()); // OPC 0x61 RLOC Request Loco
        Assert.assertEquals("count is correct", 3, tc.outbound.size());

        r = new CanReply( new int[]{CbusConstants.CBUS_ERR, 0xc0, 0x8d, 0x02 },0x12 ); // Loco address taken
        ((CbusThrottleManager)tm).reply(r);

        JUnitAppender.assertErrorMessage("1: Got a Share question 141(L)");
        // which was cancelled by the throttlelistener

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
                log.error("Throttle request failed for {} because {}", address, reason);
                failedThrottleRequest = true;
            }

            @Override
            public void notifyDecisionRequired(LocoAddress address, DecisionType question) {

                if ( question == DecisionType.STEAL ){
                    InstanceManager.throttleManagerInstance().cancelThrottleRequest(address, this);
                    log.error("1: Got a Steal question {}", address);
                }
                if ( question == DecisionType.SHARE ){
                    InstanceManager.throttleManagerInstance().cancelThrottleRequest(address, this);
                    log.error("1: Got a Share question {}", address);
                }
                if ( question == DecisionType.STEAL_OR_SHARE ){
                    InstanceManager.throttleManagerInstance().cancelThrottleRequest(address, this);
                    log.error("1: Got a steal OR share question {}", address);
                }
            }
        };

        CbusNodeTableDataModel nodemodel = memo.get(CbusConfigurationManager.class)
            .provide(CbusNodeTableDataModel.class);

        // register a command station in the node table so can be found by the Command Station
        CbusNode cs = nodemodel.provideNodeByNodeNum(65534);
        cs.setCsNum(0); // Command Station 0 is master command station
        cs.getNodeParamManager().setParameters(new int[]{7,165,4,10,0,0,255,4}); // in this test we emaulate a CANCMD v4
        cs.getNodeNvManager().setNV(2, 0b00000110); // steal + share enabled

        ((CbusThrottleManager)tm).requestThrottle(141, throtListen, false);
        Assert.assertEquals("address request message", "[78] 40 C0 8D",
            tc.outbound.elementAt(tc.outbound.size() - 1).toString()); // OPC 0x40 RLOC Request Loco
        Assert.assertEquals("count is correct", 1, tc.outbound.size());

        CanReply r = new CanReply( new int[]{CbusConstants.CBUS_ERR, 0xc0, 0x8d, 0x02 },0x12 ); // Loco address taken
        ((CbusThrottleManager)tm).reply(r);

        Assert.assertEquals("address request message", "[78] 61 C0 8D 02",
            tc.outbound.elementAt(tc.outbound.size() - 1).toString()); // OPC 0x40 GLOC Request Share Loco
        Assert.assertEquals("count is correct", 2, tc.outbound.size());
        Assert.assertFalse("not yet failed", failedThrottleRequest);

        r = new CanReply( new int[]{CbusConstants.CBUS_ERR, 0xc0, 0x8d, 0x02 },0x12 ); // Loco address taken
        ((CbusThrottleManager)tm).reply(r);

        Assert.assertTrue("Failed on 2nd attempt", failedThrottleRequest);
        JUnitAppender.assertErrorMessage("Throttle request failed for 141(L) because Loco address 141 taken");

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
                log.error("Throttle request failed for {} because {}", address, reason);
                failedThrottleRequest = true;
            }

            @Override
            public void notifyDecisionRequired(LocoAddress address, DecisionType question) {

                if ( question == DecisionType.STEAL ){
                    InstanceManager.throttleManagerInstance().cancelThrottleRequest(address, this);
                    log.error("1: Got a Steal question {}", address);
                }
                if ( question == DecisionType.SHARE ){
                    InstanceManager.throttleManagerInstance().cancelThrottleRequest(address, this);
                    log.error("1: Got a Share question {}", address);
                }
                if ( question == DecisionType.STEAL_OR_SHARE ){
                    InstanceManager.throttleManagerInstance().cancelThrottleRequest(address, this);
                    log.error("1: Got a steal OR share question {}", address);
                }
            }
        };

        CbusNodeTableDataModel nodemodel = memo.get(CbusConfigurationManager.class)
            .provide(CbusNodeTableDataModel.class);

        // register a command station in the node table so can be found by the Command Station
        CbusNode cs = nodemodel.provideNodeByNodeNum(65534);
        cs.setCsNum(0); // Command Station 0 is master command station
        cs.getNodeParamManager().setParameters(new int[]{7,165,4,10,0,0,255,4}); // in this test we emulate a CANCMD v4
        cs.getNodeNvManager().setNV(2, 0b00000110); // steal + share enabled

        // set ThrottlesPreferences to steal enabled
        InstanceManager.getDefault(ThrottlesPreferences.class).setSilentSteal(true);

        tm.requestThrottle(141, throtListen, false);
        Assert.assertEquals("address request message", "[78] 40 C0 8D",
            tc.outbound.elementAt(tc.outbound.size() - 1).toString()); // OPC 0x40 RLOC Request Loco
        Assert.assertEquals("count is correct", 1, tc.outbound.size());

        CanReply r = new CanReply( new int[]{CbusConstants.CBUS_ERR, 0xc0, 0x8d, 0x02 },0x12 ); // Loco address taken
        ((CbusThrottleManager)tm).reply(r);

        JUnitUtil.waitFor(()->{ return tc.outbound.size()>1; }, "gloc to request ");
        Assert.assertEquals("address request message", "[78] 61 C0 8D 01",
            tc.outbound.elementAt(tc.outbound.size() - 1).toString()); // OPC 0x40 GLOC Request Steal Loco
        Assert.assertEquals("count is correct", 2, tc.outbound.size());
        Assert.assertFalse("not yet failed", failedThrottleRequest);

        r = new CanReply( new int[]{CbusConstants.CBUS_ERR, 0xc0, 0x8d, 0x02 },0x12 ); // Loco address taken
        ((CbusThrottleManager)tm).reply(r);

        Assert.assertTrue("Failed on 2nd attempt", failedThrottleRequest);
        JUnitAppender.assertErrorMessage("Throttle request failed for 141(L) because Loco address 141 taken");

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
                log.error("Throttle request failed for {} because {}", address, reason);
                failedThrottleRequest = true;
            }

            @Override
            public void notifyDecisionRequired(LocoAddress address, DecisionType question) {

                if ( question == DecisionType.STEAL ){
                    InstanceManager.throttleManagerInstance().cancelThrottleRequest(address, this);
                    log.error("1: Got a Steal question {}", address);
                }
                if ( question == DecisionType.SHARE ){
                    InstanceManager.throttleManagerInstance().cancelThrottleRequest(address, this);
                    log.error("1: Got a Share question {}", address);
                }
                if ( question == DecisionType.STEAL_OR_SHARE ){
                    InstanceManager.throttleManagerInstance().cancelThrottleRequest(address, this);
                    log.error("1: Got a steal OR share question {}", address);
                }
            }
        };

        CbusNodeTableDataModel nodemodel = memo.get(CbusConfigurationManager.class)
            .provide(CbusNodeTableDataModel.class);

        // register a command station in the node table so can be found by the Command Station
        CbusNode cs = nodemodel.provideNodeByNodeNum(65534);
        cs.setCsNum(0); // Command Station 0 is master command station
        cs.getNodeParamManager().setParameters(new int[]{7,165,4,10,0,0,255,4}); // in this test we emaulate a CANCMD v4
        cs.getNodeNvManager().setNV(2, 0b00000100); // steal enabled

        tm.requestThrottle(141, throtListen, false);
        Assert.assertEquals("address request message", "[78] 40 C0 8D",
            tc.outbound.elementAt(tc.outbound.size() - 1).toString()); // OPC 0x40 RLOC Request Loco
        Assert.assertEquals("count is correct", 1, tc.outbound.size());

        CanReply r = new CanReply( new int[]{CbusConstants.CBUS_ERR, 0xc0, 0x8d, 0x02 },0x12 ); // Loco address taken
        ((CbusThrottleManager)tm).reply(r);

        Assert.assertEquals("address request message", "[78] 61 C0 8D 02",
            tc.outbound.elementAt(tc.outbound.size() - 1).toString()); // OPC 0x61 GLOC Request Share Loco
        Assert.assertEquals("count is correct", 2, tc.outbound.size());
        Assert.assertFalse("not yet failed", failedThrottleRequest);

        r = new CanReply( new int[]{CbusConstants.CBUS_ERR, 0xc0, 0x8d, 0x02 },0x12 ); // Loco address taken
        ((CbusThrottleManager)tm).reply(r);

        Assert.assertTrue("Failed on 2nd attempt", failedThrottleRequest);
        JUnitAppender.assertErrorMessage("Throttle request failed for 141(L) because Loco address 141 taken");

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
                log.error("Throttle request failed for {} because {}", address, reason);
                failedThrottleRequest = true;
            }

            @Override
            public void notifyDecisionRequired(LocoAddress address, DecisionType question) {


                // this is a never-steal or sharing impelementation.
                if ( question == DecisionType.STEAL ){
                    InstanceManager.throttleManagerInstance().responseThrottleDecision(address, null, DecisionType.STEAL );
                    log.error("1: Got a Steal question {}", address);
                }
                if ( question == DecisionType.SHARE ){
                    InstanceManager.throttleManagerInstance().cancelThrottleRequest(address, this);
                    log.error("1: Got a Share question {}", address);
                }
                if ( question == DecisionType.STEAL_OR_SHARE ){
                    InstanceManager.throttleManagerInstance().responseThrottleDecision(address, null, DecisionType.STEAL );
                    log.error("1: Got a steal OR share question {}", address);
                }
            }
        };

        CbusNodeTableDataModel nodemodel = memo.get(CbusConfigurationManager.class)
            .provide(CbusNodeTableDataModel.class);

        // register a command station in the node table so can be found by the Command Station
        CbusNode cs = nodemodel.provideNodeByNodeNum(65534);
        cs.setCsNum(0); // Command Station 0 is master command station
        cs.getNodeParamManager().setParameters(new int[]{7,165,4,10,0,0,255,4}); // in this test we emaulate a CANCMD v4
        cs.getNodeNvManager().setNV(2, 0b00000010); // steal enabled

        tm.requestThrottle(141, throtListen, true);
        Assert.assertEquals("address request message", "[78] 40 C0 8D",
            tc.outbound.elementAt(tc.outbound.size() - 1).toString()); // OPC 0x40 RLOC Request Loco
        Assert.assertEquals("count is correct", 1, tc.outbound.size());

        CanReply r = new CanReply( new int[]{CbusConstants.CBUS_ERR, 0xc0, 0x8d, 0x02 },0x12 ); // Loco address taken
        ((CbusThrottleManager)tm).reply(r);

        JUnitAppender.assertErrorMessage("1: Got a Steal question 141(L)");
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
        ((CbusThrottleManager)tm).reply(r);

        JUnitAppender.assertErrorMessage("created a throttle");

        throttle.dispose(throtListen);

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
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    public void testCanErrorsReceived() {
        CbusThrottleManager  ncbtm = (CbusThrottleManager)tm;
        CanReply r = new CanReply(
            new int[]{CbusConstants.CBUS_ERR, 0x00, 0x00, CbusConstants.ERR_CAN_BUS_ERROR },0x11 );

        Thread t1 = new Thread(() -> {
            JemmyUtil.pressDialogButton(Bundle.getMessage("CBUS_ERROR"), Bundle.getMessage("ButtonOK"));
        });

        // pass the message
        ncbtm.reply(r);
        JUnitUtil.waitFor(()->{return !(t1.isAlive());}, "checkCanErrorDialog finished");
        JUnitAppender.assertErrorMessageStartsWith(Bundle.getMessage("ERR_CAN_BUS_ERROR"));
        JUnitUtil.resetWindows(false, false); // nameless invisible frame created by creating a dialog with a null parent
    }

    @Test
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    public void testInvalidRequestErrorsReceived() {
        CbusThrottleManager  ncbtm = (CbusThrottleManager)tm;
        CanReply r = new CanReply(
            new int[]{CbusConstants.CBUS_ERR, 0x00, 0x00, CbusConstants.ERR_INVALID_REQUEST },0x11 );

        Thread t1 = new Thread(() -> {
            JemmyUtil.pressDialogButton(Bundle.getMessage("CBUS_ERROR"), Bundle.getMessage("ButtonOK"));
        });

        // pass the message
        ncbtm.reply(r);
        JUnitUtil.waitFor(()->{return !(t1.isAlive());}, "checkCbusInvalidRequestDialog finished");
        JUnitAppender.assertErrorMessageStartsWith(Bundle.getMessage("ERR_INVALID_REQUEST"));
        JUnitUtil.resetWindows(false, false); // nameless invisible frame created by creating a dialog with a null parent
    }

    @Test
    @Override
    public void testGetThrottleInfo() {
        DccLocoAddress addr = new DccLocoAddress(42, false);
        Assert.assertEquals("throttle use 0", 0, tm.getThrottleUsageCount(addr));
        Assert.assertEquals("throttle use 0", 0, tm.getThrottleUsageCount(42, false));
        Assert.assertNull("NULL", tm.getThrottleInfo(addr, Throttle.F28));
        CbusThrottleListen throtListen = new CbusThrottleListen();
        tm.requestThrottle(addr, throtListen, true);

        Assert.assertEquals("address request message", "[78] 40 00 2A",
            tc.outbound.elementAt(tc.outbound.size() - 1).toString()); // OPC 0x40 RLOC Request Loco
        Assert.assertEquals("count is correct", 1, tc.outbound.size());

        // Allocate engine session 1 with all functions set to 0
        CanReply r = new CanReply( new int[]{CbusConstants.CBUS_PLOC, 1, 0x00, 0x2a, 0,0,0 },0x12 );
        ((CbusThrottleManager)tm).reply(r);


        JUnitUtil.waitFor(()->{ return(tm.getThrottleInfo(addr,Throttle.ISFORWARD)!=null); }, "reply didn't arrive");

        Assert.assertNotNull("is forward",tm.getThrottleInfo(addr,Throttle.ISFORWARD));
        Assert.assertNotNull("speed setting",tm.getThrottleInfo(addr,Throttle.SPEEDSETTING));
        Assert.assertNotNull("speed increment",tm.getThrottleInfo(addr,Throttle.SPEEDINCREMENT));
        Assert.assertNotNull("speed step mode",tm.getThrottleInfo(addr,Throttle.SPEEDSTEPMODE));
        Assert.assertNotNull("F0",tm.getThrottleInfo(addr,Throttle.F0));
        Assert.assertNotNull("F1",tm.getThrottleInfo(addr,Throttle.F1));
        Assert.assertNotNull("F2",tm.getThrottleInfo(addr,Throttle.F2));
        Assert.assertNotNull("F3",tm.getThrottleInfo(addr,Throttle.F3));
        Assert.assertNotNull("F4",tm.getThrottleInfo(addr,Throttle.F4));
        Assert.assertNotNull("F5",tm.getThrottleInfo(addr,Throttle.F5));
        Assert.assertNotNull("F6",tm.getThrottleInfo(addr,Throttle.F6));
        Assert.assertNotNull("F7",tm.getThrottleInfo(addr,Throttle.F7));
        Assert.assertNotNull("F8",tm.getThrottleInfo(addr,Throttle.F8));
        Assert.assertNotNull("F9",tm.getThrottleInfo(addr,Throttle.F9));
        Assert.assertNotNull("F10",tm.getThrottleInfo(addr,Throttle.F10));
        Assert.assertNotNull("F11",tm.getThrottleInfo(addr,Throttle.F11));
        Assert.assertNotNull("F12",tm.getThrottleInfo(addr,Throttle.F12));
        Assert.assertNotNull("F13",tm.getThrottleInfo(addr,Throttle.F13));
        Assert.assertNotNull("F14",tm.getThrottleInfo(addr,Throttle.F14));
        Assert.assertNotNull("F15",tm.getThrottleInfo(addr,Throttle.F15));
        Assert.assertNotNull("F16",tm.getThrottleInfo(addr,Throttle.F16));
        Assert.assertNotNull("F17",tm.getThrottleInfo(addr,Throttle.F17));
        Assert.assertNotNull("F18",tm.getThrottleInfo(addr,Throttle.F18));
        Assert.assertNotNull("F19",tm.getThrottleInfo(addr,Throttle.F19));
        Assert.assertNotNull("F20",tm.getThrottleInfo(addr,Throttle.F20));
        Assert.assertNotNull("F21",tm.getThrottleInfo(addr,Throttle.F21));
        Assert.assertNotNull("F22",tm.getThrottleInfo(addr,Throttle.F22));
        Assert.assertNotNull("F23",tm.getThrottleInfo(addr,Throttle.F23));
        Assert.assertNotNull("F24",tm.getThrottleInfo(addr,Throttle.F24));
        Assert.assertNotNull("F25",tm.getThrottleInfo(addr,Throttle.F25));
        Assert.assertNotNull("F26",tm.getThrottleInfo(addr,Throttle.F26));
        Assert.assertNotNull("F27",tm.getThrottleInfo(addr,Throttle.F27));
        Assert.assertNotNull("F28",tm.getThrottleInfo(addr,Throttle.F28));
        Assert.assertNull("NULL",tm.getThrottleInfo(addr,"NOT A VARIABLE"));
        Assert.assertEquals("throttle use 1 addr", 1, tm.getThrottleUsageCount(addr));
        Assert.assertEquals("throttle use 1 int b", 1, tm.getThrottleUsageCount(42,false));
        Assert.assertEquals("throttle use 0", 0, tm.getThrottleUsageCount(77,true));
    }

    @Test
    public void testThrottleAddress10239() {
        CbusThrottleManager cbtmb = (CbusThrottleManager) tm;
        Assertions.assertNotNull(cbtmb);
        DccLocoAddress addr = new DccLocoAddress(10239,true);

        CbusThrottleListen throtListen = new CbusThrottleListen();
        cbtmb.requestThrottle(addr,throtListen,true);

        Assert.assertEquals("address request message", "[78] 40 E7 FF",
            tc.outbound.elementAt(tc.outbound.size() - 1).toString()); // OPC 0x40 RLOC Request Loco
        Assert.assertEquals("count is correct", 1, tc.outbound.size());
    }

    private CanSystemConnectionMemo memo;
    private DccThrottle throttle;
    private boolean failedThrottleRequest = false;
    private TrafficControllerScaffold tc;

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();

        tc = new TrafficControllerScaffold();
        memo = new CanSystemConnectionMemo();
        memo.setProtocol(jmri.jmrix.can.CanConfigurationManager.SPROGCBUS);
        memo.setTrafficController(tc);
        memo.configureManagers();
        tm = memo.get(ThrottleManager.class);

    }

    @AfterEach
    public void tearDown() {
        tm.dispose();
        tm = null;
        memo.dispose();
        memo = null;
        tc.terminateThreads();
        tc = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(CbusThrottleManagerTest.class);

}
