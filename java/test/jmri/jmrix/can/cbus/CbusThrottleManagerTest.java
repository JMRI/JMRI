package jmri.jmrix.can.cbus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.junit.jupiter.api.Assumptions.assumeFalse;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.throttle.ThrottlesPreferences;
import jmri.jmrix.can.*;
import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeTableDataModel;

import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;
import jmri.util.swing.JemmyUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class CbusThrottleManagerTest extends jmri.managers.AbstractThrottleManagerTestBase {

    @Test
    public void testCbusThrottleManagerCTor() {
        assertNotNull( tm, "exists");
    }

    @Test
    public void testIncomingFunctions() {
        assumeFalse( Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"), "Ignoring intermittent test");

        // CbusThrottleManager cbtm = (CbusThrottleManager) tm;
        assertNotNull( tm, "exists");
        DccLocoAddress addr = new DccLocoAddress(1234,true);

        CancelRequestThrottleListen throtListen = new CancelRequestThrottleListen();
        tm.requestThrottle(addr,throtListen,true);

        assertEquals( "[78] 40 C4 D2",
            tc.outbound.elementAt(tc.outbound.size() - 1).toString(),
            "address request message 0x40 RLOC Request Loco");
        assertEquals( 1, tc.outbound.size(), "count is correct");

        // Allocate engine session 1 with all functions set to 0
        CanReply r = new CanReply( new int[]{CbusConstants.CBUS_PLOC, 1, 0xc4, 0xd2, 0,0,0 },0x12 );
        ((CbusThrottleManager)tm).reply(r);

        JUnitUtil.waitFor(()-> (tm.getThrottleUsageCount(addr)>0), "throttle didn't create");
        assertFalse( (boolean) tm.getThrottleInfo(addr,Throttle.getFunctionString(0)), "F0 init");

        r = new CanReply( new int[]{CbusConstants.CBUS_DFUN, 1, 0x00, 0x00 },0x12 );
        ((CbusThrottleManager)tm).reply(r);

        r.setElement(2, 1);
        r.setElement(3, 0x1f);
        ((CbusThrottleManager)tm).reply(r);
        assertTrue( (boolean) tm.getThrottleInfo(addr,Throttle.getFunctionString(0)), "F0 on");
        assertTrue( (boolean) tm.getThrottleInfo(addr,Throttle.getFunctionString(1)), "F1 on");
        assertTrue( (boolean) tm.getThrottleInfo(addr,Throttle.getFunctionString(2)), "F2 on");
        assertTrue( (boolean) tm.getThrottleInfo(addr,Throttle.getFunctionString(3)), "F3 on");
        assertTrue( (boolean) tm.getThrottleInfo(addr,Throttle.getFunctionString(4)), "F4 on");
        r.setElement(3, 0x00);
        ((CbusThrottleManager)tm).reply(r);
        assertFalse( (boolean) tm.getThrottleInfo(addr,Throttle.getFunctionString(0)), "F0 off");
        assertFalse( (boolean) tm.getThrottleInfo(addr,Throttle.getFunctionString(1)), "F1 off");
        assertFalse( (boolean) tm.getThrottleInfo(addr,Throttle.getFunctionString(2)), "F2 off");
        assertFalse( (boolean) tm.getThrottleInfo(addr,Throttle.getFunctionString(3)), "F3 off");
        assertFalse( (boolean) tm.getThrottleInfo(addr,Throttle.getFunctionString(4)), "F4 off");
        r.setElement(2, 2);
        r.setElement(3, 0x0f);
        ((CbusThrottleManager)tm).reply(r);
        assertTrue( (boolean) tm.getThrottleInfo(addr,Throttle.getFunctionString(5)), "F5 on");
        assertTrue( (boolean) tm.getThrottleInfo(addr,Throttle.getFunctionString(6)), "F6 on");
        assertTrue( (boolean) tm.getThrottleInfo(addr,Throttle.getFunctionString(7)), "F7 on");
        assertTrue( (boolean) tm.getThrottleInfo(addr,Throttle.getFunctionString(8)), "F8 on");
        r.setElement(3, 0x00);
        ((CbusThrottleManager)tm).reply(r);
        assertFalse( (boolean) tm.getThrottleInfo(addr,Throttle.getFunctionString(5)), "F5 off");
        assertFalse( (boolean) tm.getThrottleInfo(addr,Throttle.getFunctionString(6)), "F6 off");
        assertFalse( (boolean) tm.getThrottleInfo(addr,Throttle.getFunctionString(7)), "F7 off");
        assertFalse( (boolean) tm.getThrottleInfo(addr,Throttle.getFunctionString(8)), "F8 off");

        r.setElement(2, 3);
        r.setElement(3, 0x0f);
        ((CbusThrottleManager)tm).reply(r);
        assertTrue( (boolean) tm.getThrottleInfo(addr,Throttle.getFunctionString(9)), "F9 on");
        assertTrue( (boolean) tm.getThrottleInfo(addr,Throttle.getFunctionString(10)), "F10 on");
        assertTrue( (boolean) tm.getThrottleInfo(addr,Throttle.getFunctionString(11)), "F11 on");
        assertTrue( (boolean) tm.getThrottleInfo(addr,Throttle.getFunctionString(12)), "F12 on");
        r.setElement(3, 0x00);
        ((CbusThrottleManager)tm).reply(r);
        assertFalse( (boolean) tm.getThrottleInfo(addr,Throttle.getFunctionString(9)), "F9 off");
        assertFalse( (boolean) tm.getThrottleInfo(addr,Throttle.getFunctionString(10)), "F10 off");
        assertFalse( (boolean) tm.getThrottleInfo(addr,Throttle.getFunctionString(11)), "F11  off");
        assertFalse( (boolean) tm.getThrottleInfo(addr,Throttle.getFunctionString(12)), "F12  off");

        r.setElement(2, 4);
        r.setElement(3, 0xff);
        ((CbusThrottleManager)tm).reply(r);
        assertTrue( (boolean) tm.getThrottleInfo(addr,Throttle.getFunctionString(13)), "F13 on");
        assertTrue( (boolean) tm.getThrottleInfo(addr,Throttle.getFunctionString(14)), "F14 on");
        assertTrue( (boolean) tm.getThrottleInfo(addr,Throttle.getFunctionString(15)), "F15 on");
        assertTrue( (boolean) tm.getThrottleInfo(addr,Throttle.getFunctionString(16)), "F16 on");
        assertTrue( (boolean) tm.getThrottleInfo(addr,Throttle.getFunctionString(17)), "F17 on");
        assertTrue( (boolean) tm.getThrottleInfo(addr,Throttle.getFunctionString(18)), "F18 on");
        assertTrue( (boolean) tm.getThrottleInfo(addr,Throttle.getFunctionString(19)), "F19 on");
        assertTrue( (boolean) tm.getThrottleInfo(addr,Throttle.getFunctionString(20)), "F20 on");

        r.setElement(3, 0x00);
        ((CbusThrottleManager)tm).reply(r);
        assertFalse( (boolean) tm.getThrottleInfo(addr,Throttle.getFunctionString(13)), "F13 off");
        assertFalse( (boolean) tm.getThrottleInfo(addr,Throttle.getFunctionString(14)), "F14 off");
        assertFalse( (boolean) tm.getThrottleInfo(addr,Throttle.getFunctionString(15)), "F15 off");
        assertFalse( (boolean) tm.getThrottleInfo(addr,Throttle.getFunctionString(16)), "F16 off");
        assertFalse( (boolean) tm.getThrottleInfo(addr,Throttle.getFunctionString(17)), "F17 off");
        assertFalse( (boolean) tm.getThrottleInfo(addr,Throttle.getFunctionString(18)), "F18 off");
        assertFalse( (boolean) tm.getThrottleInfo(addr,Throttle.getFunctionString(19)), "F19 off");
        assertFalse( (boolean) tm.getThrottleInfo(addr,Throttle.getFunctionString(20)), "F20 off");

        r.setElement(2, 5);
        r.setElement(3, 0xff);
        ((CbusThrottleManager)tm).reply(r);
        assertTrue( (boolean) tm.getThrottleInfo(addr,Throttle.getFunctionString(21)), "F21 on");
        assertTrue( (boolean) tm.getThrottleInfo(addr,Throttle.getFunctionString(22)), "F22 on");
        assertTrue( (boolean) tm.getThrottleInfo(addr,Throttle.getFunctionString(23)), "F23 on");
        assertTrue( (boolean) tm.getThrottleInfo(addr,Throttle.getFunctionString(24)), "F24 on");
        assertTrue( (boolean) tm.getThrottleInfo(addr,Throttle.getFunctionString(25)), "F25 on");
        assertTrue( (boolean) tm.getThrottleInfo(addr,Throttle.getFunctionString(26)), "F26 on");
        assertTrue( (boolean) tm.getThrottleInfo(addr,Throttle.getFunctionString(27)), "F27 on");
        assertTrue( (boolean) tm.getThrottleInfo(addr,Throttle.getFunctionString(28)), "F28 on");
        r.setElement(3, 0x00);
        ((CbusThrottleManager)tm).reply(r);
        assertFalse( (boolean) tm.getThrottleInfo(addr,Throttle.getFunctionString(21)), "F21 off");
        assertFalse( (boolean) tm.getThrottleInfo(addr,Throttle.getFunctionString(22)), "F22 off");
        assertFalse( (boolean) tm.getThrottleInfo(addr,Throttle.getFunctionString(23)), "F23 off");
        assertFalse( (boolean) tm.getThrottleInfo(addr,Throttle.getFunctionString(24)), "F24 off");
        assertFalse( (boolean) tm.getThrottleInfo(addr,Throttle.getFunctionString(25)), "F25 off");
        assertFalse( (boolean) tm.getThrottleInfo(addr,Throttle.getFunctionString(26)), "F26 off");
        assertFalse( (boolean) tm.getThrottleInfo(addr,Throttle.getFunctionString(27)), "F27 off");
        assertFalse( (boolean) tm.getThrottleInfo(addr,Throttle.getFunctionString(28)), "F28 off");

    }

    @Test
    public void testIncomingFunctionsDecimal() {

        assertNotNull( tm, "exists");
        DccLocoAddress addr = new DccLocoAddress(221,true);

        CancelRequestThrottleListen throtListen = new CancelRequestThrottleListen();
        tm.requestThrottle(addr,throtListen,true);

        assertEquals( "[78] 40 C0 DD",
            tc.outbound.elementAt(tc.outbound.size() - 1).toString(),
            "address request message OPC 0x40 RLOC Request Loco");
        assertEquals( 1, tc.outbound.size(), "count is correct");

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
        assumeFalse( Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"), "Ignoring intermittent test");

        CbusThrottleManager cbtmb = (CbusThrottleManager) tm;
        assertNotNull( cbtmb, "exists");
        DccLocoAddress addr = new DccLocoAddress(422,true);

        CancelRequestThrottleListen throtListen = new CancelRequestThrottleListen();
        cbtmb.requestThrottle(addr,throtListen,true);

        assertEquals( "[78] 40 C1 A6",
            tc.outbound.elementAt(tc.outbound.size() - 1).toString(),
            "address request message OPC 0x40 RLOC Request Loco");
        assertEquals( 1, tc.outbound.size(), "count is correct");

        // Allocate engine session 1 with all functions set to 0
        CanReply r = new CanReply( new int[]{CbusConstants.CBUS_PLOC, 1, 0xc1, 0xa6, 0,0,0 },0x12 );
        ((CbusThrottleManager)tm).reply(r);

        JUnitUtil.waitFor(()->{ return(cbtmb.getThrottleInfo(addr,"F0")!=null); }, "reply didn't arrive");

        assertEquals( 0f, (float) cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.01f, "speed setting");
        assertEquals( (1.0f/126.0f),(float) cbtmb.getThrottleInfo(addr,Throttle.SPEEDINCREMENT),0.1f, "speed increment");
        assertEquals( SpeedStepMode.NMRA_DCC_128,cbtmb.getThrottleInfo(addr,Throttle.SPEEDSTEPMODE),
            "speed step mode");

        r = new CanReply( new int[]{CbusConstants.CBUS_DSPD, 1, 0 },0x12 );
        cbtmb.reply(r);
        assertEquals( 0.0f, (float) cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.01f,
            "speed setting 0");

        JUnitUtil.waitFor(()->{ return(cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD).equals(false)); }, "Throttle didn't update");
        assertFalse( (boolean) cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD), "is forward 0");

        r = new CanReply( new int[]{CbusConstants.CBUS_DSPD, 77, 77 },0x12 );
        cbtmb.reply(r);
        assertEquals( 0.0f, (float) cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.01f,
            "different session speed unchanged");

        r = new CanReply( new int[]{CbusConstants.CBUS_DSPD, 1, 1 },0x12 );
        cbtmb.reply(r);
        assertEquals( -1.0f, (float) cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.01f, "speed setting 1");
        assertFalse( (boolean) cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD), "is forward 1");

        r.setElement(2, 2);
        cbtmb.reply(r);
        assertEquals( (1.0f/126.0f), (float) cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.1f, "speed setting 2");
        assertFalse( (boolean) cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD), "is forward 2");

        r.setElement(2, 77);
        cbtmb.reply(r);
        assertEquals( (76.0f/126.0f),(float) cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.1f, "speed setting 77");
        assertFalse( (boolean) cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD), "is forward 77");

        r.setElement(2, 126);
        cbtmb.reply(r);
        assertEquals( (125.0f/126.0f),(float) cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.1f, "speed setting 126");
        assertFalse( (boolean) cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD), "is forward 126");

        r.setElement(2, 127);
        cbtmb.reply(r);
        assertEquals( (1.0f),(float) cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.01f, "speed setting 127");
        assertFalse( (boolean) cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD), "is forward 127");

        r.setElement(2, 128);
        cbtmb.reply(r);
        assertEquals( (0.0f),(float) cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.01f, "speed setting 128");
        assertTrue( (boolean) cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD), "is forward 128");

        r.setElement(2, 129);
        cbtmb.reply(r);
        assertEquals( -1.0f, (float) cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.01f, "speed setting 129");
        assertTrue( (boolean) cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD), "is forward 129");

        r.setElement(2, 130);
        cbtmb.reply(r);
        assertEquals( (1.0f/126.0f),(float) cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.1f, "speed setting 130");
        assertTrue( (boolean) cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD), "is forward 130");

        r.setElement(2, 211);
        cbtmb.reply(r);
        assertEquals( (82.0f/126.0f),(float) cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.1f, "speed setting 211");
        assertTrue( (boolean) cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD), "is forward 211");

        r.setElement(2, 254);
        cbtmb.reply(r);
        assertEquals( (125.0f/126.0f),(float) cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.1f, "speed setting 254");
        assertTrue( (boolean) cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD), "is forward 254");

        r.setElement(2, 255);
        cbtmb.reply(r);
        assertEquals( (1.0f),(float) cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.01f, "speed setting 255");
        assertTrue( (boolean) cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD), "is forward 255");

        r = new CanReply( new int[]{CbusConstants.CBUS_ESTOP },0x12 );
        cbtmb.reply(r);
        assertEquals( (-1.0f),(float) cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.01f, "speed setting estop");
        assertTrue( (boolean) cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD), "estop forward");

        r = new CanReply( new int[]{CbusConstants.CBUS_DSPD, 1, 77 },0x12 );
        cbtmb.reply(r);
        assertEquals( (76.0f/126.0f),(float) cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.01f, "speed setting before reverse estop 77");
        assertFalse( (boolean) cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD), "is forward b4 estop 77");
        r = new CanReply( new int[]{CbusConstants.CBUS_ESTOP },0x12 );
        cbtmb.reply(r);
        assertEquals( (-1.0f),(float) cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.1f, "speed setting estop");
        assertFalse( (boolean) cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD), "estop reverse");

        r = new CanReply( new int[]{CbusConstants.CBUS_DSPD, 1, 255 },0x12 );
        cbtmb.reply(r);
        assertEquals( (1.0f),(float) cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.01f, "speed setting 255");
        assertTrue( (boolean) cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD), "is forward 255");

        r = new CanReply( new int[]{CbusConstants.CBUS_RESTP },0x12 );
        cbtmb.reply(r);
        assertEquals( (-1.0f),(float) cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.01f, "speed setting r estop");
        assertTrue( (boolean) cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD), "restop forward");


        r = new CanReply( new int[]{CbusConstants.CBUS_DSPD, 1, 77 },0x12 );
        cbtmb.reply(r);
        assertEquals( (76.0f/126.0f),(float) cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.01f, "speed setting before reverse restop 77");
        assertFalse( (boolean) cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD), "is forward b4 restop 77");
        r = new CanReply( new int[]{CbusConstants.CBUS_RESTP },0x12 );
        cbtmb.reply(r);
        assertEquals( (-1.0f),(float) cbtmb.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.01f, "speed setting restop");
        assertFalse( (boolean) cbtmb.getThrottleInfo(addr,Throttle.ISFORWARD), "restop reverse");

    }

    @Test
    public void testMessage() {
        assumeFalse( Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"), "Ignoring intermittent test");

        assertNotNull( tm, "exists");
        DccLocoAddress addr = new DccLocoAddress(1234,true);

        CancelRequestThrottleListen throtListen = new CancelRequestThrottleListen();
        tm.requestThrottle(addr,throtListen,true);

        assertEquals( "[78] 40 C4 D2",
            tc.outbound.elementAt(tc.outbound.size() - 1).toString(),
            "address request message OPC 0x40 RLOC Request Loco");
        assertEquals( 1, tc.outbound.size(), "count is correct");

        // Allocate engine session 1 with all functions set to 0
        CanReply r = new CanReply( new int[]{CbusConstants.CBUS_PLOC, 1, 0xc4, 0xd2, 0,0,0 },0x12 );
        ((CbusThrottleManager)tm).reply(r);

        JUnitUtil.waitFor(()->{ return(tm.getThrottleInfo(addr,"F0")!=null); }, "Throttle didn't create");

        r = new CanReply( new int[]{CbusConstants.CBUS_DSPD, 1, 88 },0x12 );
        r.setExtended(true);
        ((CbusThrottleManager)tm).reply(r);
        assertEquals( 0f, (float)tm.getThrottleInfo(addr,Throttle.SPEEDSETTING));

        r.setExtended(false);
        r.setRtr(true);
        ((CbusThrottleManager)tm).reply(r);
        assertEquals( 0f, (float)tm.getThrottleInfo(addr,Throttle.SPEEDSETTING));

        r = new CanReply( new int[]{CbusConstants.CBUS_DSPD, 1, 77 },0x12 );
        // rtr and extended both false by default

        ((CbusThrottleManager)tm).reply(r);

        JUnitUtil.waitFor(()->{ return(((float)tm.getThrottleInfo(addr,Throttle.SPEEDSETTING))!=0f); }, "Speed command not received");
        assertEquals( (76.0f/126.0f), (float) tm.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.1f, "msg speed setting before reverse estop 77");
        assertFalse( (boolean) tm.getThrottleInfo(addr,Throttle.ISFORWARD), "msg is forward b4 estop 77");
        CanMessage m = new CanMessage( new int[]{CbusConstants.CBUS_RESTP },0x12 );
        ((CbusThrottleManager)tm).message(m);
        assertEquals( (-1.0f),(float) tm.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.01f, "msg speed setting estop");
        assertFalse( (boolean) tm.getThrottleInfo(addr,Throttle.ISFORWARD), "msg estop reverse");

        r = new CanReply( new int[]{CbusConstants.CBUS_DSPD, 44, 77 },0x12 );
        ((CbusThrottleManager)tm).reply(r);
        assertEquals( -1.0f, (float) tm.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.01f, "msg speed setting unchanged wrong address");

        r = new CanReply( new int[]{CbusConstants.CBUS_DSPD, 1, 77 },0x12 );
        ((CbusThrottleManager)tm).reply(r);
        assertEquals( (76.0f/126.0f),(float) tm.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.1f, "msg speed setting before reverse estop 77");
        assertFalse( (boolean) tm.getThrottleInfo(addr,Throttle.ISFORWARD), "msg is forward b4 estop 77");
        m = new CanMessage( new int[]{CbusConstants.CBUS_ESTOP },0x12 );

        m.setExtended(true);
        ((CbusThrottleManager)tm).message(m);
        assertEquals( (76.0f/126.0f),(float) tm.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.1f, "msg speed setting Extended CAN ignored");

        m.setExtended(false);
        m.setRtr(true);
        ((CbusThrottleManager)tm).message(m);
        assertEquals( (76.0f/126.0f),(float) tm.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.1f, "msg speed setting Rtr CAN ignored");

        m.setRtr(false);
        ((CbusThrottleManager)tm).message(m);

        assertEquals( (-1.0f),(float) tm.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.01f, "msg speed setting estop");
        assertFalse( (boolean) tm.getThrottleInfo(addr,Throttle.ISFORWARD), "msg estop reverse");

        r = new CanReply( new int[]{CbusConstants.CBUS_DSPD, 1, 77 },0x12 );
        ((CbusThrottleManager)tm).reply(r);
        assertEquals( (76.0f/126.0f),(float) tm.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.1f, "msg speed setting before reverse estop 77");
        assertFalse( (boolean) tm.getThrottleInfo(addr,Throttle.ISFORWARD), "msg is forward b4 estop 77");
        m = new CanMessage( new int[]{CbusConstants.CBUS_DSPD, 1, 99 },0x12 );
        ((CbusThrottleManager)tm).message(m);
        assertEquals( (76.0f/126.0f),(float) tm.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.1f, "msg speed does not change");
        assertFalse( (boolean)tm.getThrottleInfo(addr,Throttle.ISFORWARD), "msg estop reverse");

        m.setElement(2, 1);
        ((CbusThrottleManager)tm).message(m);
        assertEquals( -1.0f, (float) tm.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.1f, "msg speed change estop");
        assertFalse( (boolean) tm.getThrottleInfo(addr,Throttle.ISFORWARD), "msg estop reverse");

        m.setElement(2, 129);
        ((CbusThrottleManager)tm).message(m);
        assertEquals( -1.0f, (float) tm.getThrottleInfo(addr,Throttle.SPEEDSETTING),0.1f, "msg speed change estop");
        assertTrue( (boolean) tm.getThrottleInfo(addr,Throttle.ISFORWARD), "msg estop reverse");

        m = new CanMessage( new int[]{CbusConstants.CBUS_KLOC, 22 },0x12 );
        ((CbusThrottleManager)tm).message(m);
        assertTrue( (boolean) tm.getThrottleInfo(addr,Throttle.ISFORWARD), "addr unchanged as no session number match");

        m = new CanMessage( new int[]{CbusConstants.CBUS_KLOC, 1 },0x12 );
        ((CbusThrottleManager)tm).message(m);

        assertNull( tm.getThrottleInfo(addr,Throttle.SPEEDSETTING), "session cancelled");

    }

    @Test
    public void testCbdispose() {

        DccLocoAddress addr = new DccLocoAddress(555,true);
        assertEquals( 0, tm.getThrottleUsageCount(addr), "throttle use 0");
        CancelRequestThrottleListen throtListen = new CancelRequestThrottleListen();
        tm.requestThrottle(addr,throtListen,true);

        assertEquals( "[78] 40 C2 2B",
            tc.outbound.elementAt(tc.outbound.size() - 1).toString(),
            "address request message OPC 0x40 RLOC Request Loco");
        assertEquals( 1, tc.outbound.size(), "count is correct");

        // Allocate engine session 1 with all functions set to 0
        CanReply r = new CanReply( new int[]{CbusConstants.CBUS_PLOC, 1, 0xc2, 0x2b, 0,0,0 },0x12 );
        ((CbusThrottleManager)tm).reply(r);

        CbusThrottle cbt = new CbusThrottle(memo,addr,1);
        JUnitUtil.waitFor(()-> tm.getThrottleUsageCount(addr)>0, "Throttle count did not increase");
        assertEquals( 1, tm.getThrottleUsageCount(addr), "throttle use 1");

        assertTrue( tm.disposeThrottle(cbt,throtListen));
        JUnitUtil.waitFor(()->{ return(tm.getThrottleUsageCount(addr)==0); },
            "Throttle Count did not go 0 on dispose, add retry rule for this if regular?");
        assertEquals( 0, tm.getThrottleUsageCount(addr), "disposed throttle use 0");
        assertNull( tm.getThrottleInfo(addr,Throttle.getFunctionString(28)), "NULL");

        assertFalse(tm.disposeThrottle(null,throtListen));

    }

    private class CancelRequestThrottleListen implements ThrottleListener {

        private boolean failedRequest;
        String lastNotification = "";

        @Override
        public void notifyThrottleFound(DccThrottle t){
            throttle = t;
            lastNotification = "created a throttle";
        }

        @Override
        public void notifyFailedThrottleRequest(LocoAddress address, String reason){
            failedRequest = true;
            lastNotification = "Throttle request failed for " + address + " because " + reason;
        }

        @Override
        public void notifyDecisionRequired(LocoAddress address, DecisionType question) {
            // this is a never-stealing impelementation.
            if ( question == DecisionType.STEAL ){
                InstanceManager.throttleManagerInstance().cancelThrottleRequest(address, this);
                lastNotification = "1: Got a Steal question "+ address;
            }
            if ( question == DecisionType.SHARE ){
                InstanceManager.throttleManagerInstance().cancelThrottleRequest(address, this);
                lastNotification = "1: Got a Share question "+ address;
            }
            if ( question == DecisionType.STEAL_OR_SHARE ){
                InstanceManager.throttleManagerInstance().cancelThrottleRequest(address, this);
                lastNotification = "1: Got a Steal OR Share question "+ address;
            }
        }

        boolean getFailedRequest() {
            return failedRequest;
        }

        @Nonnull
        String getLastNotification() {
            return lastNotification;
        }

    }

    private class StealRequestThrottleListen extends CancelRequestThrottleListen{
        @Override
        public void notifyDecisionRequired(LocoAddress address, DecisionType question) {

            if ( question == DecisionType.STEAL ){
                InstanceManager.throttleManagerInstance().responseThrottleDecision(address, null, DecisionType.STEAL );
                lastNotification = "2: Got a Steal question " + address;
            }
            if ( question == DecisionType.SHARE ){
                InstanceManager.throttleManagerInstance().cancelThrottleRequest(address, this);
                lastNotification = "2: Got a Share question " + address;
            }
            if ( question == DecisionType.STEAL_OR_SHARE ){
                InstanceManager.throttleManagerInstance().responseThrottleDecision(address, null, DecisionType.STEAL );
                lastNotification = "2: Got a steal OR share question " + address;
            }
        }
    }

    // Failed Throttle request when unable to steal / share the session
    // as no command station reporting as a node
    @Test
    public void testCreateCbusThrottleStealScenario1() {

        CancelRequestThrottleListen throtListen = new CancelRequestThrottleListen();

        memo.setDisabled(true); // disable access to memo.get(CommandStation.class);

        tm.requestThrottle(129, throtListen, true);
        assertEquals( "[78] 40 C0 81",
            tc.outbound.elementAt(tc.outbound.size() - 1).toString(),
            "address request message OPC 0x40 RLOC Request Loco");
        assertEquals( 1, tc.outbound.size(), "count is correct");
        CanReply r = new CanReply( new int[]{CbusConstants.CBUS_ERR, 0xc0, 0x81, 0x02 },0x12 ); // Loco address taken
        ((CbusThrottleManager)tm).reply(r);
        assertEquals( "Throttle request failed for 129(L) because Loco address 129 taken",
            throtListen.getLastNotification());
        assertTrue( throtListen.getFailedRequest(), "No steal or share request");
    }

    @Test
    public void testCreateCbusThrottleStealScenario2() {

        CancelRequestThrottleListen throtListen = new CancelRequestThrottleListen();

        CbusNodeTableDataModel nodemodel = memo.get(CbusConfigurationManager.class)
            .provide(CbusNodeTableDataModel.class);

        // register a command station in the node table so can be found by the Command Station
        CbusNode cs = nodemodel.provideNodeByNodeNum(65534);
        cs.setCsNum(0); // Command Station 0 is master command station
        cs.getNodeParamManager().setParameters(new int[]{7,165,4,10,0,0,255,4}); // in this test we emaulate a CANCMD v4
        cs.getNodeNvManager().setNV(2, 0b00000110); // steal + share enabled

        tm.requestThrottle(141, throtListen, true);
        assertEquals( "[78] 40 C0 8D",
            tc.outbound.elementAt(tc.outbound.size() - 1).toString(),
            "address request message OPC 0x40 RLOC Request Loco");
        assertEquals( 1, tc.outbound.size(), "count is correct");

        CanReply r = new CanReply( new int[]{CbusConstants.CBUS_ERR, 0xc0, 0x8d, 0x02 },0x12 ); // Loco address taken
        ((CbusThrottleManager)tm).reply(r);

        assertEquals("1: Got a Steal OR Share question 141(L)",
            throtListen.getLastNotification());
        // which was cancelled by the throttlelistener

        cs.getNodeNvManager().setNV(2, 0b00000010); // steal only enabled


        tm.requestThrottle(141, throtListen, true);
        assertEquals( "[78] 40 C0 8D",
            tc.outbound.elementAt(tc.outbound.size() - 1).toString(),
            "address request message OPC 0x40 RLOC Request Loco");
        assertEquals( 2, tc.outbound.size(), "count is correct");

        r = new CanReply( new int[]{CbusConstants.CBUS_ERR, 0xc0, 0x8d, 0x02 },0x12 ); // Loco address taken
        ((CbusThrottleManager)tm).reply(r);

        assertEquals("1: Got a Steal question 141(L)",
            throtListen.getLastNotification());
        // which was cancelled by the throttlelistener

        cs.getNodeNvManager().setNV(2, 0b00000100); // share only enabled

        tm.requestThrottle(141, throtListen, true);
        assertEquals( "[78] 40 C0 8D",
            tc.outbound.elementAt(tc.outbound.size() - 1).toString(),
            "address request message OPC 0x61 RLOC Request Loco");
        assertEquals( 3, tc.outbound.size(), "count is correct");

        r = new CanReply( new int[]{CbusConstants.CBUS_ERR, 0xc0, 0x8d, 0x02 },0x12 ); // Loco address taken
        ((CbusThrottleManager)tm).reply(r);

        assertEquals("1: Got a Share question 141(L)",
            throtListen.getLastNotification());
        // which was cancelled by the throttlelistener

        nodemodel.dispose();
    }

    // test default share attempt when no options checked
    @Test
    public void testCreateCbusThrottleDefaultShareScenario() {

        CancelRequestThrottleListen throtListen = new CancelRequestThrottleListen();

        CbusNodeTableDataModel nodemodel = memo.get(CbusConfigurationManager.class)
            .provide(CbusNodeTableDataModel.class);

        // register a command station in the node table so can be found by the Command Station
        CbusNode cs = nodemodel.provideNodeByNodeNum(65534);
        cs.setCsNum(0); // Command Station 0 is master command station
        cs.getNodeParamManager().setParameters(new int[]{7,165,4,10,0,0,255,4}); // in this test we emaulate a CANCMD v4
        cs.getNodeNvManager().setNV(2, 0b00000110); // steal + share enabled

        ((CbusThrottleManager)tm).requestThrottle(141, throtListen, false);
        assertEquals( "[78] 40 C0 8D",
            tc.outbound.elementAt(tc.outbound.size() - 1).toString(),
            "address request message OPC 0x40 RLOC Request Loco"); //
        assertEquals( 1, tc.outbound.size(), "count is correct");

        CanReply r = new CanReply( new int[]{CbusConstants.CBUS_ERR, 0xc0, 0x8d, 0x02 },0x12 ); // Loco address taken
        ((CbusThrottleManager)tm).reply(r);

        assertEquals( "[78] 61 C0 8D 02",
            tc.outbound.elementAt(tc.outbound.size() - 1).toString(),
            "address request message OPC 0x40 GLOC Request Share Loco");
        assertEquals( 2, tc.outbound.size(), "count is correct");
        assertFalse( throtListen.getFailedRequest(), "not yet failed");

        r = new CanReply( new int[]{CbusConstants.CBUS_ERR, 0xc0, 0x8d, 0x02 },0x12 ); // Loco address taken
        ((CbusThrottleManager)tm).reply(r);

        assertTrue( throtListen.getFailedRequest(), "Failed on 2nd attempt");
        assertEquals("Throttle request failed for 141(L) because Loco address 141 taken",
            throtListen.getLastNotification());

        nodemodel.dispose();
    }

    // test steal attempt when silent steal checked
    @Test
    public void testCreateCbusThrottleSilentStealCheckedScenario() {

        CancelRequestThrottleListen throtListen = new CancelRequestThrottleListen();

        CbusNodeTableDataModel nodemodel = memo.get(CbusConfigurationManager.class)
            .provide(CbusNodeTableDataModel.class);

        // register a command station in the node table so can be found by the Command Station
        CbusNode cs = new CbusNode(memo, 65534);
        cs.setCsNum(0); // Command Station 0 is master command station
        // in this test we emulate a CANCMD v4 but with just 10 NVs
        cs.getNodeParamManager().setParameters(new int[]{7,165,4,10,0,0,10,4});

        // NV2 = steal + share enabled
        // set all NVs so Node Manager does not request them from the Node.
        int[] nVs = {10,1,0b00000110,3,4,5,6,7,8,9,10};
        cs.getNodeNvManager().setNVs(nVs);
        nodemodel.addNode(cs);

        // set ThrottlesPreferences to steal enabled
        InstanceManager.getDefault(ThrottlesPreferences.class).setSilentSteal(true);

        tm.requestThrottle(141, throtListen, false);
        assertEquals( "[78] 40 C0 8D",
            tc.outbound.elementAt(tc.outbound.size() - 1).toString(),
            "address request message OPC 0x40 RLOC Request Loco");
        assertEquals( 1, tc.outbound.size(), () -> "count is correct "+tc.outbound);

        CanReply r = new CanReply( new int[]{CbusConstants.CBUS_ERR, 0xc0, 0x8d, 0x02 },0x12 ); // Loco address taken
        ((CbusThrottleManager)tm).reply(r);

        JUnitUtil.waitFor(()-> tc.outbound.size()>1 , "gloc to request ");
        assertEquals( "[78] 61 C0 8D 01",
            tc.outbound.elementAt(tc.outbound.size() - 1).toString(),
            "address request message OPC 0x40 GLOC Request Steal Loco");
        assertEquals( 2, tc.outbound.size(), "count is correct");
        assertFalse( throtListen.getFailedRequest(), "not yet failed");

        r = new CanReply( new int[]{CbusConstants.CBUS_ERR, 0xc0, 0x8d, 0x02 },0x12 ); // Loco address taken
        ((CbusThrottleManager)tm).reply(r);

        assertTrue( throtListen.getFailedRequest(), "Failed on 2nd attempt");
        assertEquals("Throttle request failed for 141(L) because Loco address 141 taken",
            throtListen.getLastNotification());

        nodemodel.dispose();
    }

    // test default steal attempt when no options checked
    @Test
    public void testCreateCbusThrottleDefaultNoShareavailableScenario() {

        CancelRequestThrottleListen throtListen = new CancelRequestThrottleListen();

        CbusNodeTableDataModel nodemodel = memo.get(CbusConfigurationManager.class)
            .provide(CbusNodeTableDataModel.class);

        // register a command station in the node table so can be found by the Command Station
        CbusNode cs = nodemodel.provideNodeByNodeNum(65534);
        cs.setCsNum(0); // Command Station 0 is master command station
        cs.getNodeParamManager().setParameters(new int[]{7,165,4,10,0,0,255,4}); // in this test we emaulate a CANCMD v4
        cs.getNodeNvManager().setNV(2, 0b00000100); // steal enabled

        tm.requestThrottle(141, throtListen, false);
        assertEquals( "[78] 40 C0 8D",
            tc.outbound.elementAt(tc.outbound.size() - 1).toString(),
            "address request message OPC 0x40 RLOC Request Loco");
        assertEquals( 1, tc.outbound.size(), "count is correct");

        CanReply r = new CanReply( new int[]{CbusConstants.CBUS_ERR, 0xc0, 0x8d, 0x02 },0x12 ); // Loco address taken
        ((CbusThrottleManager)tm).reply(r);

        assertEquals( "[78] 61 C0 8D 02",
            tc.outbound.elementAt(tc.outbound.size() - 1).toString(),
            "address request message OPC 0x61 GLOC Request Share Loco");
        assertEquals( 2, tc.outbound.size(), () -> "count is correct " + tc.outbound.toString());
        assertFalse( throtListen.getFailedRequest(), "not yet failed");

        r = new CanReply( new int[]{CbusConstants.CBUS_ERR, 0xc0, 0x8d, 0x02 },0x12 ); // Loco address taken
        ((CbusThrottleManager)tm).reply(r);

        assertTrue( throtListen.getFailedRequest(), "Failed on 2nd attempt");
        assertEquals("Throttle request failed for 141(L) because Loco address 141 taken",
            throtListen.getLastNotification());

        nodemodel.dispose();
    }

    // throttle acquired after 2nd level response
    @Test
    public void testCreateCbusThrottleSteal() {

        StealRequestThrottleListen throtListen = new StealRequestThrottleListen();

        CbusNodeTableDataModel nodemodel = memo.get(CbusConfigurationManager.class)
            .provide(CbusNodeTableDataModel.class);

        // register a command station in the node table so can be found by the Command Station
        CbusNode cs = nodemodel.provideNodeByNodeNum(65534);
        cs.setCsNum(0); // Command Station 0 is master command station
        cs.getNodeParamManager().setParameters(new int[]{7,165,4,10,0,0,255,4}); // in this test we emaulate a CANCMD v4
        cs.getNodeNvManager().setNV(2, 0b00000010); // steal enabled

        tm.requestThrottle(141, throtListen, true);
        assertEquals( "[78] 40 C0 8D",
            tc.outbound.elementAt(tc.outbound.size() - 1).toString(),
            "address request message OPC 0x40 RLOC Request Loco");
        assertEquals( 1, tc.outbound.size(), "count is correct");

        CanReply r = new CanReply( new int[]{CbusConstants.CBUS_ERR, 0xc0, 0x8d, 0x02 },0x12 ); // Loco address taken
        ((CbusThrottleManager)tm).reply(r);

        assertEquals("2: Got a Steal question 141(L)",
            throtListen.getLastNotification());
        // which was confirmed please steal by the throttlelistener

        assertEquals( "[78] 61 C0 8D 01",
            tc.outbound.elementAt(tc.outbound.size() - 1).toString(),
            "steal request message OPC 0x61 GLOC Request Steal Loco");
        assertEquals( 2, tc.outbound.size(), "count is correct");
        assertFalse( throtListen.getFailedRequest(), "not yet failed");

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

        assertEquals("created a throttle",
            throtListen.getLastNotification());
        assertNotNull(throttle);

        throttle.dispose(throtListen);

        nodemodel.dispose();
    }

    @Test
    public void testStealShareOptionsEnabled() {
        assertTrue( tm.enablePrefSilentStealOption(), "Can Silent Steal");
        assertTrue( tm.enablePrefSilentShareOption(), "Can Silent Share");
    }

    @Test
    public void testIsLongAddress() {

        assertFalse( CbusThrottleManager.isLongAddress(1), "local isLong 1");
        assertFalse( CbusThrottleManager.isLongAddress(127), "local isLong 127");
        assertTrue( CbusThrottleManager.isLongAddress(128), "local isLong 128");
        assertTrue( CbusThrottleManager.isLongAddress(129), "local isLong 129");

        assertFalse( tm.canBeLongAddress(0), "can be long 0");
        assertTrue( tm.canBeLongAddress(1), "can be long 1");

    }

    @Test
    public void testNotADccLocoAddress() {
        CbusThrottleManager cbtmb = (CbusThrottleManager)tm;

        cbtmb.responseThrottleDecision(null,null,null);
        JUnitAppender.assertErrorMessageStartsWith("null is not a DccLocoAddress");
    }

    @Test
    @DisabledIfHeadless
    public void testCanErrorsReceived() {
        CbusThrottleManager  ncbtm = (CbusThrottleManager)tm;
        CanReply r = new CanReply(
            new int[]{CbusConstants.CBUS_ERR, 0x00, 0x00, CbusConstants.ERR_CAN_BUS_ERROR },0x11 );

        Thread t1 = new Thread(() -> {
            JemmyUtil.pressDialogButton(Bundle.getMessage("CBUS_ERROR"), Bundle.getMessage("ButtonOK"));
        });

        // pass the message
        ncbtm.reply(r);
        JUnitUtil.waitFor(()-> !t1.isAlive(), "checkCanErrorDialog finished");
        JUnitAppender.assertErrorMessageStartsWith(Bundle.getMessage("ERR_CAN_BUS_ERROR"));
        JUnitUtil.resetWindows(false, false); // nameless invisible frame created by creating a dialog with a null parent
    }

    @Test
    @DisabledIfHeadless
    public void testInvalidRequestErrorsReceived() {
        CbusThrottleManager  ncbtm = (CbusThrottleManager)tm;
        CanReply r = new CanReply(
            new int[]{CbusConstants.CBUS_ERR, 0x00, 0x00, CbusConstants.ERR_INVALID_REQUEST },0x11 );

        Thread t1 = new Thread(() -> {
            JemmyUtil.pressDialogButton(Bundle.getMessage("CBUS_ERROR"), Bundle.getMessage("ButtonOK"));
        });

        // pass the message
        ncbtm.reply(r);
        JUnitUtil.waitFor(()-> !t1.isAlive(), "checkCbusInvalidRequestDialog finished");
        JUnitAppender.assertErrorMessageStartsWith(Bundle.getMessage("ERR_INVALID_REQUEST"));
        JUnitUtil.resetWindows(false, false); // nameless invisible frame created by creating a dialog with a null parent
    }

    @Test
    @Override
    public void testGetThrottleInfo() {
        DccLocoAddress addr = new DccLocoAddress(42, false);
        assertEquals( 0, tm.getThrottleUsageCount(addr), "throttle use 0");
        assertEquals( 0, tm.getThrottleUsageCount(42, false), "throttle use 0");
        assertNull( tm.getThrottleInfo(addr, Throttle.getFunctionString(28)), "NULL");
        CancelRequestThrottleListen throtListen = new CancelRequestThrottleListen();
        tm.requestThrottle(addr, throtListen, true);

        assertEquals( "[78] 40 00 2A",
            tc.outbound.elementAt(tc.outbound.size() - 1).toString(),
            "address request message OPC 0x40 RLOC Request Loco");
        assertEquals( 1, tc.outbound.size(), "count is correct");

        // Allocate engine session 1 with all functions set to 0
        CanReply r = new CanReply( new int[]{CbusConstants.CBUS_PLOC, 1, 0x00, 0x2a, 0,0,0 },0x12 );
        ((CbusThrottleManager)tm).reply(r);


        JUnitUtil.waitFor(()->{ return(tm.getThrottleInfo(addr,Throttle.ISFORWARD)!=null); }, "reply didn't arrive");

        assertNotNull( tm.getThrottleInfo(addr,Throttle.ISFORWARD), "is forward");
        assertNotNull( tm.getThrottleInfo(addr,Throttle.SPEEDSETTING), "speed setting");
        assertNotNull( tm.getThrottleInfo(addr,Throttle.SPEEDINCREMENT), "speed increment");
        assertNotNull( tm.getThrottleInfo(addr,Throttle.SPEEDSTEPMODE), "speed step mode");
        for ( int i = 0; i<29; i++) {
            assertNotNull( tm.getThrottleInfo(addr,Throttle.getFunctionString(i)),
                Throttle.getFunctionString(i));
        }
        assertNull( tm.getThrottleInfo(addr,"NOT A VARIABLE"), "NULL");
        assertEquals( 1, tm.getThrottleUsageCount(addr), "throttle use 1 addr");
        assertEquals( 1, tm.getThrottleUsageCount(42,false), "throttle use 1 int b");
        assertEquals( 0, tm.getThrottleUsageCount(77,true), "throttle use 0");
    }

    @Test
    public void testThrottleAddress10239() {
        CbusThrottleManager cbtmb = (CbusThrottleManager) tm;
        assertNotNull(cbtmb);
        DccLocoAddress addr = new DccLocoAddress(10239,true);

        CancelRequestThrottleListen throtListen = new CancelRequestThrottleListen();
        cbtmb.requestThrottle(addr,throtListen,true);

        assertEquals( "[78] 40 E7 FF",
            tc.outbound.elementAt(tc.outbound.size() - 1).toString(),
            "address request message OPC 0x40 RLOC Request Loco");
        assertEquals( 1, tc.outbound.size(), "count is correct");
    }

    private CanSystemConnectionMemo memo;
    private DccThrottle throttle;
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

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CbusThrottleManagerTest.class);

}
