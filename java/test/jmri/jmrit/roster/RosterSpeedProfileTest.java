package jmri.jmrit.roster;

import java.util.HashMap;

import jmri.util.JUnitUtil;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.LocoAddress;
import jmri.ThrottleListener;
import jmri.ThrottleManager;
import jmri.implementation.SignalSpeedMap;
import jmri.jmrit.roster.RosterSpeedProfile.SpeedSetting;
import jmri.util.JUnitAppender;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class RosterSpeedProfileTest {

    private boolean throttleResult;
    private DccThrottle throttle;

    private class ThrottleListen implements ThrottleListener {

        @Override
        public void notifyThrottleFound(DccThrottle t){
            throttleResult = true;
            throttle = t;
        }

        @Override
        public void notifyFailedThrottleRequest(LocoAddress address, String reason){
            throttleResult = false;
        }

        @Override
        public void notifyDecisionRequired(LocoAddress address, DecisionType question) {
            if ( question == DecisionType.STEAL ){
                throttleResult = false;
            }
        }
    }

    @Test
    public void testCTor() {
        RosterSpeedProfile t = new RosterSpeedProfile(new RosterEntry());
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testSpeedProfileStopFromFiftyPercent() {
        // statics for test objects
        org.jdom2.Element f1;
        RosterEntry rF1;

        // create Element
        f1 = new org.jdom2.Element("locomotive")
                .setAttribute("id", "id info")
                .setAttribute("fileName", "file here")
                .setAttribute("roadNumber", "431")
                .setAttribute("roadName", "SP")
                .setAttribute("mfg", "Athearn")
                .setAttribute("dccAddress", "1234")
                .addContent(new org.jdom2.Element("decoder")
                        .setAttribute("family", "91")
                        .setAttribute("model", "33")
                )
                .addContent(new org.jdom2.Element("locoaddress")
                        .addContent(new org.jdom2.Element("number").addContent("1234"))
                        //As there is no throttle manager available all protocols default to dcc short
                        .addContent(new org.jdom2.Element("protocol").addContent("dcc_short"))
                )
                .addContent(new org.jdom2.Element("speedprofile")
                        .addContent(new org.jdom2.Element("overRunTimeForward").addContent("0.0"))
                        .addContent(new org.jdom2.Element("overRunTimeReverse").addContent("0.0"))
                        .addContent(new org.jdom2.Element("speeds")
                                .addContent(new org.jdom2.Element("speed")
                                        .addContent(new org.jdom2.Element("step").addContent("1000"))
                                        .addContent(new org.jdom2.Element("forward").addContent("100.00"))
                                        .addContent(new org.jdom2.Element("reverse").addContent("100.00"))
                                )
                        )
                );
        rF1 = new RosterEntry(f1) {
            @Override
            protected void warnShortLong(String s) {
            }
        };
        ThrottleListener throtListen = new ThrottleListen();
        ThrottleManager tm = InstanceManager.getDefault(ThrottleManager.class);
        boolean OK = tm.requestThrottle(rF1, throtListen, throttleResult);
        Assert.assertTrue("Throttle request denied",OK);
        JUnitUtil.waitFor(()-> (throttleResult), "Got No throttle");
        throttle.setIsForward(true);
        throttle.setSpeedSetting(0.5f);
        RosterSpeedProfile sp = rF1.getSpeedProfile();
        sp.setTestMode(true);
        sp.changeLocoSpeed(throttle, 50.0f, 0.0f);
        // Allow speed step table to be constructed
        //JUnitUtil.waitFor(5000);
        // Note it must be a perfect 0.0
        //Assert.assertEquals("Speed didnt get to a perfect zero", 0.0f, throttle.getSpeedSetting(), 0.0f);
        JUnitUtil.waitFor(()->(throttle.getSpeedSetting() == 0.00f),"Failed to reach requested speed");
        float maxDelta = 1.0f/126.0f/2.0f;  //half step
        Assert.assertEquals("SpeedStep Table has lincorrect number of entries.", 7, sp.getSpeedStepTrace().size() ) ;
        int[] correctDuration = {750, 750, 750, 750, 750, 519, 0} ;
        int[] durations = new int[sp.getSpeedStepTrace().size()];
        int ix = 0;
        for (SpeedSetting ss: sp.getSpeedStepTrace()) {
            durations[ix]= ss.getDuration();
            ix++;
        }
        Assert.assertArrayEquals("Durations are wrong",correctDuration, durations);

        float[] correctSpeed = {0.30798f, 0.17571f, 0.09067f, 0.04558f, 0.02260f, 0.01466f, 0.0f} ;
        float[] speed = new float[sp.getSpeedStepTrace().size()];
        ix=0;
        for (SpeedSetting ss: sp.getSpeedStepTrace()) {
            speed[ix]= ss.getSpeedStep();
            ix++;
        }
        Assert.assertArrayEquals("Speeds are wrong", correctSpeed, speed, maxDelta);
        sp.cancelSpeedChange();
    }

    @Test
    public void testSpeedProfileFromFiftyPercentToTwenty() {
        // statics for test objects
        org.jdom2.Element f1;
        RosterEntry rF1;

        // create Element
        f1 = new org.jdom2.Element("locomotive")
                .setAttribute("id", "id info")
                .setAttribute("fileName", "file here")
                .setAttribute("roadNumber", "431")
                .setAttribute("roadName", "SP")
                .setAttribute("mfg", "Athearn")
                .setAttribute("dccAddress", "1234")
                .addContent(new org.jdom2.Element("decoder")
                        .setAttribute("family", "91")
                        .setAttribute("model", "33")
                )
                .addContent(new org.jdom2.Element("locoaddress")
                        .addContent(new org.jdom2.Element("number").addContent("1234"))
                        //As there is no throttle manager available all protocols default to dcc short
                        .addContent(new org.jdom2.Element("protocol").addContent("dcc_short"))
                )
                .addContent(new org.jdom2.Element("speedprofile")
                        .addContent(new org.jdom2.Element("overRunTimeForward").addContent("0.0"))
                        .addContent(new org.jdom2.Element("overRunTimeReverse").addContent("0.0"))
                        .addContent(new org.jdom2.Element("speeds")
                                .addContent(new org.jdom2.Element("speed")
                                        .addContent(new org.jdom2.Element("step").addContent("1000"))
                                        .addContent(new org.jdom2.Element("forward").addContent("200.00"))
                                        .addContent(new org.jdom2.Element("reverse").addContent("200.00"))
                                )
                        )
                );
        rF1 = new RosterEntry(f1) {
            @Override
            protected void warnShortLong(String s) {
            }
        };
        ThrottleListener throtListen = new ThrottleListen();
        ThrottleManager tm = InstanceManager.getDefault(ThrottleManager.class);
        boolean OK = tm.requestThrottle(rF1, throtListen, throttleResult);
        Assert.assertTrue("Throttle request denied",OK);

        JUnitUtil.waitFor(()-> (throttleResult), "Got No throttle");
        throttle.setIsForward(true);
        throttle.setSpeedSetting(0.6f);
        RosterSpeedProfile sp = rF1.getSpeedProfile();
        sp.setTestMode(true);
        sp.changeLocoSpeed(throttle, 150.0f, 0.20f);
        // Allow speed step table to be constructed
        //JUnitUtil.waitFor(5000);
        // Note it must be a perfect 0.20
        JUnitUtil.waitFor(()->(throttle.getSpeedSetting() == 0.20f),"Failed to reach requested speed");
        //Assert.assertEquals("Speed didnt get to a perfect 20", 0.20f, throttle.getSpeedSetting(), 0.00f);
        float maxDelta = 1.0f/126.0f/2.0f;  //half step
        Assert.assertEquals("SpeedStep Table has lincorrect number of entries.", 4, sp.getSpeedStepTrace().size() ) ;
        int[] correctDuration = {750, 750, 750, 540} ;
        int[] durations = new int[sp.getSpeedStepTrace().size()];
        int ix = 0;
        for (SpeedSetting ss: sp.getSpeedStepTrace()) {
            durations[ix]= ss.getDuration();
            ix++;
        }
        Assert.assertArrayEquals("Durations are wrong",correctDuration, durations);

        float[] correctSpeed = {0.43912f, 0.30069f, 0.20311f, 0.2f} ;
        float[] speed = new float[sp.getSpeedStepTrace().size()];
        ix=0;
        for (SpeedSetting ss: sp.getSpeedStepTrace()) {
            speed[ix]= ss.getSpeedStep();
            ix++;
        }
        Assert.assertArrayEquals("Speeds are wrong", correctSpeed, speed, maxDelta);
        sp.cancelSpeedChange();
    }

    @Test
    public void testSpeedProfileFromFiftyPercentToTwentyShortBlock() {
        // statics for test objects
        org.jdom2.Element f1;
        RosterEntry rF1;

        // create Element
        f1 = new org.jdom2.Element("locomotive")
                .setAttribute("id", "id info")
                .setAttribute("fileName", "file here")
                .setAttribute("roadNumber", "431")
                .setAttribute("roadName", "SP")
                .setAttribute("mfg", "Athearn")
                .setAttribute("dccAddress", "1234")
                .addContent(new org.jdom2.Element("decoder")
                        .setAttribute("family", "91")
                        .setAttribute("model", "33")
                )
                .addContent(new org.jdom2.Element("locoaddress")
                        .addContent(new org.jdom2.Element("number").addContent("1234"))
                        //As there is no throttle manager available all protocols default to dcc short
                        .addContent(new org.jdom2.Element("protocol").addContent("dcc_short"))
                )
                .addContent(new org.jdom2.Element("speedprofile")
                        .addContent(new org.jdom2.Element("overRunTimeForward").addContent("0.0"))
                        .addContent(new org.jdom2.Element("overRunTimeReverse").addContent("0.0"))
                        .addContent(new org.jdom2.Element("speeds")
                                .addContent(new org.jdom2.Element("speed")
                                        .addContent(new org.jdom2.Element("step").addContent("200"))
                                        .addContent(new org.jdom2.Element("forward").addContent("40.00"))
                                        .addContent(new org.jdom2.Element("reverse").addContent("40.00"))
                                )
                                .addContent(new org.jdom2.Element("speed")
                                        .addContent(new org.jdom2.Element("step").addContent("1000"))
                                        .addContent(new org.jdom2.Element("forward").addContent("400.00"))
                                        .addContent(new org.jdom2.Element("reverse").addContent("400.00"))
                                )
                        )
                );
        rF1 = new RosterEntry(f1) {
            @Override
            protected void warnShortLong(String s) {
            }
        };
        ThrottleListener throtListen = new ThrottleListen();
        ThrottleManager tm = InstanceManager.getDefault(ThrottleManager.class);
        boolean OK = tm.requestThrottle(rF1, throtListen, throttleResult);
        Assert.assertTrue("Throttle request denied",OK);
        JUnitUtil.waitFor(()-> (throttleResult), "Got No throttle");
        throttle.setIsForward(true);
        throttle.setSpeedSetting(0.6f);
        RosterSpeedProfile sp = rF1.getSpeedProfile();
        sp.setTestMode(true);
        sp.setExtraInitialDelay(1500f);
        sp.changeLocoSpeed(throttle, 152.0f, 0.20f);
        // Allow speed step table to be constructed
        //JUnitUtil.waitFor(3000);
        // Note it must be a perfect 0.20
        JUnitUtil.waitFor(()->(throttle.getSpeedSetting() == 0.20f),"Failed to reach requested speed");

        JUnitAppender.assertWarnMessageStartsWith("distance remaining is now 0, but we have not reached desired speed setting 0.2 v 0.3");

        // as the calc goes wrong we immediatly set speed to final speed. The entries are rubbish so dont bother checking
        Assert.assertEquals("SpeedStep Table has lincorrect number of entries.", 1, sp.getSpeedStepTrace().size() ) ;
        sp.cancelSpeedChange();
    }

    @Test
    public void testconvertThrottleSettingToScaleSpeedWithUnits(){

        SignalSpeedMap ssm = InstanceManager.getDefault(SignalSpeedMap.class);
        setSpeedInterpretation(ssm, SignalSpeedMap.PERCENT_NORMAL);
        Assertions.assertEquals("0.50 millimeters/sec",RosterSpeedProfile.convertMMSToScaleSpeedWithUnits(0.5f));

        setSpeedInterpretation(ssm, SignalSpeedMap.PERCENT_THROTTLE);
        Assertions.assertEquals("0.50 millimeters/sec",RosterSpeedProfile.convertMMSToScaleSpeedWithUnits(0.5f));

        setSpeedInterpretation(ssm, SignalSpeedMap.SPEED_KMPH);
        Assertions.assertEquals("0.16 Kilometers/Hour",RosterSpeedProfile.convertMMSToScaleSpeedWithUnits(0.5f));

        setSpeedInterpretation(ssm, SignalSpeedMap.SPEED_MPH);
        Assertions.assertEquals("0.10 Miles/Hour",RosterSpeedProfile.convertMMSToScaleSpeedWithUnits(0.5f));

    }

    private void setSpeedInterpretation(SignalSpeedMap map, int interpretation) {
        var speedNames = map.getValidSpeedNames();
        HashMap<String, Float> newMap = new HashMap<>(speedNames.size());
        for ( var speedName : speedNames ) {
            newMap.put(speedName, map.getSpeed(speedName));
            // System.out.println("key " + speedName + " value: " + map.getSpeed(speedName));
        }
        map.setAspects(newMap, interpretation);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDebugThrottleManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
