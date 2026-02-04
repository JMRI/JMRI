package jmri.jmrit.roster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;

import jmri.util.JUnitUtil;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.LocoAddress;
import jmri.SpeedStepMode;
import jmri.ThrottleListener;
import jmri.ThrottleManager;
import jmri.implementation.SignalSpeedMap;
import jmri.jmrit.roster.RosterSpeedProfile.SpeedSetting;
import jmri.util.JUnitAppender;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class RosterSpeedProfileTest {

    private static final float GLOBAL_TOTAL_DISTANCE_TOLERANCE = 2.0f;

    private static class ThrottleListen implements ThrottleListener {

        DccThrottle throttle;

        @Override
        public void notifyThrottleFound(DccThrottle t){
            throttle = t;
        }

        @Override
        public void notifyFailedThrottleRequest(LocoAddress address, String reason){
            throttle = null;
        }

        @Override
        public void notifyDecisionRequired(LocoAddress address, DecisionType question) {
            if ( question == DecisionType.STEAL ){
                throttle = null;
            }
        }
    }

    @Test
    public void testCTor() {
        RosterSpeedProfile t = new RosterSpeedProfile(new RosterEntry());
        assertNotNull( t, "exists");
    }

    private static org.jdom2.Element getLocoElement100() {
        return new org.jdom2.Element("locomotive")
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
    }

    private ReturnValues testScene(RosterEntry rF1, float currentSpeed,
            float newSpeed, float testDistance,
            float minSpeed, float maxSpeed, SpeedStepMode speedStepMode,
            DccThrottle inThrottle ) {
        inThrottle.setSpeedStepMode(speedStepMode);
        inThrottle.setIsForward(true);
        inThrottle.setSpeedSetting(currentSpeed);
        RosterSpeedProfile sp = rF1.getSpeedProfile();
        float mmFactor = sp.getForwardSpeed(1.0f);
        sp.setTestMode(true);
        sp.setExtraInitialDelay(0f);
        sp.setMinMaxLimits(minSpeed, maxSpeed);
        // assertNotNull(throttle);
        sp.changeLocoSpeed(inThrottle, testDistance, newSpeed);
        ReturnValues returnValues = new ReturnValues();
        returnValues.totalDistance = 0.0f;
        returnValues.numberOfElements = sp.getSpeedStepTrace().size();
        for (SpeedSetting ss : sp.getSpeedStepTrace()) {
            returnValues.totalDistance += (ss.getDuration() / 1000.0f) * (ss.getSpeedStep() * mmFactor);
            returnValues.finalSpeed = ss.getSpeedStep();
        }
        returnValues.throttleSpeedSetting = inThrottle.getSpeedSetting();
        sp.cancelSpeedChange();

        return returnValues;
    }

    ReturnValuesFromAct testSpeedStep(SpeedStepMode stm,
            long fromDistanceMm, long toDistanceMm, long byDistanceMm,
            float fromSpeedStep, float bySpeedStep,    // limit for speed step is max throttle step
            float fromSpeed, float toSpeed, float bySpeed,
            float fromMin, float toMin, float byMin,
            float fromMax_IsMinPlus, float toMax, float byMax,
            org.jdom2.Element speedCurve) {
        ReturnValuesFromAct resultSummary = new ReturnValuesFromAct();
        long stmLimit;
        // statics for test objects
        RosterEntry rF1 = new RosterEntry(speedCurve) {
            @Override
            protected void warnShortLong(String s) {
            }
        };

        ThrottleListen throtListen = new ThrottleListen();
        ThrottleManager tm = InstanceManager.getDefault(ThrottleManager.class);
        assertTrue( tm.requestThrottle(rF1, throtListen, false) );
        stmLimit = stm.numSteps;
        resultSummary.testTotalCount = 0;
        for (float testDistance = fromDistanceMm; testDistance <= toDistanceMm; testDistance += byDistanceMm) {
            //Runtime.getRuntime().gc();
            for (float currentSpeedStep = fromSpeedStep; currentSpeedStep >= 0 &&
                    currentSpeedStep <= stmLimit; currentSpeedStep += bySpeedStep) {
                for (float newPercentSpeed = fromSpeed; newPercentSpeed <= toSpeed; newPercentSpeed += bySpeed) {
                    for (float minPercentSpeed = 0.00f; minPercentSpeed <= 1.0f ; minPercentSpeed+= 0.1f)  {
                        for (float maxPercentSpeed = minPercentSpeed + fromMax_IsMinPlus;
                                maxPercentSpeed <= toMax; maxPercentSpeed += byMax) {
                            ReturnValues returnValues = testScene(rF1, //profile
                                    currentSpeedStep / stmLimit, //current speed
                                    newPercentSpeed, // new speed
                                    testDistance, // distance
                                    minPercentSpeed, // minSpeed
                                    maxPercentSpeed, // max speed
                                    stm, // stepmode
                                    throtListen.throttle
                            );
                            resultSummary.testTotalCount++;
                            long ix = Math.round(
                                    (Math.abs(testDistance - returnValues.totalDistance) / testDistance) *
                                            100.0f);
                            if (ix > 100) {
                                ix = 101;
                            }
                            float expectedSpeed = newPercentSpeed;
                            if (expectedSpeed > 0.0f &&
                                    minPercentSpeed > 0.0f &&
                                    minPercentSpeed > expectedSpeed) {
                                expectedSpeed = minPercentSpeed;
                            }
                            if (maxPercentSpeed < expectedSpeed) {
                                expectedSpeed = maxPercentSpeed;
                            }
                            // If number of elements is 0 then
                            // speed was not altered from the input and final speed is unset
                            if (returnValues.numberOfElements > 0 && Math.abs(expectedSpeed - returnValues.finalSpeed) > (1.0f / stmLimit)) {
                                resultSummary.failedTestEndSpeed += 1;
                            }
                            if (returnValues.numberOfElements == 0 && returnValues.totalDistance == 0.0f &&
                                    Math.abs(expectedSpeed - (currentSpeedStep / stmLimit)) < (1.0f /
                                            stmLimit)) {
                                // no speed change expected so no distance
                                resultSummary.zeroTests++;
                                resultSummary.lengthError[0] += 1;
                                // speed test will have always failed so dont add.
                            } else if ((Math.abs(testDistance - returnValues.totalDistance) /
                                    testDistance) < GLOBAL_TOTAL_DISTANCE_TOLERANCE) {
                                // within tolerance
                                resultSummary.passedTests++;
                                resultSummary.lengthError[(int) ix] += 1;
                            } else {
                                resultSummary.failedTests++;
                                // use this to debug individual entries
//                                    log.info("Failed {} CS {} NS {} D {} MIN {} MAX {} ACTD {} AS {} FTS {}",
//                                            stm.name(),
//                                            currentSpeedStep / stmLimit, //current speed
//                                            newPercentSpeed, // new speed
//                                            testDistance, // distance
//                                            minPercentSpeed, // minSpeed
//                                            maxPercentSpeed, returnValues.totalDistance, returnValues.finalSpeed,
//                                            returnValues.throttleSpeedSetting);
                                resultSummary.lengthError[(int) ix] += 1;
                            }
                            // unused returnValues = null;
                        }
                    }
                }
            }
        }
        return resultSummary;
    }

    @Test
    public void testSpeedProfile_28() {
        SpeedStepMode stm = SpeedStepMode.NMRA_DCC_28;
        ReturnValuesFromAct resultSummary = testSpeedStep(stm,
                100L, 2000L, 100L, //distance steps
                1.0f, 1.0f, //from speed
                0.0f, 1.0f, 0.1f, //   to speeds
                0.0f, 0.3f, 0.6f, // min speeds
                0.10f, 1.0f, 0.10f,
                getLocoElement100());
        assertEquals(0, resultSummary.failedTests, () -> getResultString( stm, resultSummary));
        assertEquals(0, resultSummary.failedTestEndSpeed,() -> getResultString( stm, resultSummary));
    }

    @Test
    public void testSpeedProfile_128() {
        SpeedStepMode stm = SpeedStepMode.NMRA_DCC_128;
        ReturnValuesFromAct resultSummary = testSpeedStep(stm,
                100L, 2000L, 100L,  //distance steps
                1.0f,1.0f,         //from speed
                0.0f, 1.0f, 0.1f, //   to speeds
                0.0f, 0.3f, 0.6f, // min speeds
                0.10f, 1.0f, 0.10f,
                getLocoElement100());
        assertEquals(0, resultSummary.failedTests, () -> getResultString( stm, resultSummary));
        assertEquals(0, resultSummary.failedTestEndSpeed,() -> getResultString( stm, resultSummary));
    }

    @Test
    public void testSpeedProfile_14() {
        SpeedStepMode stm = SpeedStepMode.NMRA_DCC_14;
        ReturnValuesFromAct resultSummary = testSpeedStep(stm,
                100L, 2000L, 100L,  //distance steps
                1.0f,1.0f,         //from speed
                0.0f, 1.0f, 0.1f, //   to speeds
                0.0f, 0.3f, 0.6f, // min speeds
                0.10f, 1.0f, 0.10f,
                getLocoElement100());
        assertEquals(0, resultSummary.failedTests, () -> getResultString( stm, resultSummary));
        assertEquals(0, resultSummary.failedTestEndSpeed,() -> getResultString( stm, resultSummary));
    }

    @Test
    public void testSpeedProfile_100() {
        SpeedStepMode stm = SpeedStepMode.TMCC1_100;
        ReturnValuesFromAct resultSummary = testSpeedStep(stm,
                100L, 2000L, 100L, //distance steps
                1.0f, 1.0f, //from speed
                0.0f, 1.0f, 0.1f, //   to speeds
                0.0f, 0.3f, 0.6f, // min speeds
                0.10f, 1.0f, 0.10f,
                getLocoElement100());
        
        assertEquals(0, resultSummary.failedTests, () -> getResultString( stm, resultSummary));
        assertEquals(0, resultSummary.failedTestEndSpeed, () -> getResultString( stm, resultSummary));
    }

    private String getResultString(SpeedStepMode stm, ReturnValuesFromAct resultSummary) {
        return stm.name + " Tests [" + resultSummary.testTotalCount
            + "] run. Length Failed [" + resultSummary.failedTests
            + "] Final Speed errors [" + + resultSummary.failedTestEndSpeed + "]"
            + " Zero:" + resultSummary.zeroTests
            + " Passed:" + resultSummary.passedTests;
    }

    @Test
    public void testSpeedProfileFromFiftyPercentToTwentyShortBlock() {
        // statics for test objects
        org.jdom2.Element f1 = getLocoElement400();
        RosterEntry rF1 = new RosterEntry(f1) {
            @Override
            protected void warnShortLong(String s) {
            }
        };
        ThrottleListen throtListen = new ThrottleListen();
        ThrottleManager tm = InstanceManager.getDefault(ThrottleManager.class);
        assertTrue( tm.requestThrottle(rF1, throtListen, false), "Throttle request denied");
        JUnitUtil.waitFor(()-> ( throtListen.throttle != null), "Got No throttle");
        DccThrottle throttle = throtListen.throttle;
        assertNotNull(throttle);
        throttle.setIsForward(true);
        throttle.setSpeedSetting(0.6f);
        RosterSpeedProfile sp = rF1.getSpeedProfile();
        sp.setTestMode(true);
        sp.setExtraInitialDelay(1500f);
        sp.changeLocoSpeed(throttle, 152.0f, 0.20f);
        // Allow speed step table to be constructed
        //JUnitUtil.waitFor(3000);
        // Note it must be a perfect 0.20
        JUnitUtil.waitFor(()-> Float.compare(throttle.getSpeedSetting(), 0.20f)==0,
            "Failed to reach requested speed");

        JUnitAppender.assertWarnMessageStartsWith("There is insufficient distance");
        // as the calc goes wrong we immediately set speed to final speed. The entries are rubbish so dont bother checking
        assertEquals( 0, sp.getSpeedStepTrace().size(), "SpeedStep Table has incorrect number of entries.");

        sp.cancelSpeedChange();
    }


    private static org.jdom2.Element getLocoElement400 () {
        return new org.jdom2.Element("locomotive")
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
    }

    @Test
    public void testconvertThrottleSettingToScaleSpeedWithUnits(){

        SignalSpeedMap ssm = InstanceManager.getDefault(SignalSpeedMap.class);
        setSpeedInterpretation(ssm, SignalSpeedMap.PERCENT_NORMAL);
        assertEquals("0.50 millimeters/sec",RosterSpeedProfile.convertMMSToScaleSpeedWithUnits(0.5f));

        setSpeedInterpretation(ssm, SignalSpeedMap.PERCENT_THROTTLE);
        assertEquals("0.50 millimeters/sec",RosterSpeedProfile.convertMMSToScaleSpeedWithUnits(0.5f));

        setSpeedInterpretation(ssm, SignalSpeedMap.SPEED_KMPH);
        assertEquals("0.16 Kilometers/Hour",RosterSpeedProfile.convertMMSToScaleSpeedWithUnits(0.5f));

        setSpeedInterpretation(ssm, SignalSpeedMap.SPEED_MPH);
        assertEquals("0.10 Miles/Hour",RosterSpeedProfile.convertMMSToScaleSpeedWithUnits(0.5f));

    }

    @Test
    public void testMmsToScaleSpeed(){

        org.jdom2.Element f1 = getLocoElement100();
        RosterEntry rF1 = new RosterEntry(f1) {
            @Override
            protected void warnShortLong(String s) {
            }
        };
        RosterSpeedProfile rsp = rF1.getSpeedProfile();

        SignalSpeedMap ssm = InstanceManager.getDefault(SignalSpeedMap.class);
        var timeBase = InstanceManager.getDefault(jmri.Timebase.class);
        timeBase.setRun(false);
        Assertions.assertDoesNotThrow(() -> { timeBase.userSetRate(1.0d); } );

        setSpeedInterpretation(ssm, SignalSpeedMap.PERCENT_THROTTLE);
        Assertions.assertEquals(10.0f, rsp.mmsToScaleSpeed(10, false), 0.0001);
        Assertions.assertEquals(10.0f, rsp.mmsToScaleSpeed(10, true), 0.0001);

        setSpeedInterpretation(ssm, SignalSpeedMap.SPEED_KMPH);
        Assertions.assertEquals(3.13559f, rsp.mmsToScaleSpeed(10, false), 0.001);
        Assertions.assertEquals(3.13559f, rsp.mmsToScaleSpeed(10, true), 0.001);

        setSpeedInterpretation(ssm, SignalSpeedMap.SPEED_MPH);
        Assertions.assertEquals(1.94837f, rsp.mmsToScaleSpeed(10, false), 0.0001);
        Assertions.assertEquals(1.94837f, rsp.mmsToScaleSpeed(10, true), 0.0001);

        Assertions.assertDoesNotThrow(() -> { timeBase.userSetRate(2.0d); } );
        Assertions.assertEquals(1.94837f, rsp.mmsToScaleSpeed(10, false), 0.0001);
        Assertions.assertEquals(3.89675f, rsp.mmsToScaleSpeed(10, true), 0.0001);

        setSpeedInterpretation(ssm, SignalSpeedMap.SPEED_KMPH);
        Assertions.assertEquals(3.13559f, rsp.mmsToScaleSpeed(10, false), 0.001);
        Assertions.assertEquals(6.27119f, rsp.mmsToScaleSpeed(10, true), 0.001);
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

    private static class ReturnValues {
        // return values from an individual scene
        float totalDistance;
        float finalSpeed;
        int numberOfElements;
        float throttleSpeedSetting;
        ReturnValues() {
            totalDistance = 0.0f;
            finalSpeed = 0.0f;
            numberOfElements = 0;
            throttleSpeedSetting = 0.0f;
        }
        // Unused Ctor
        //ReturnValues(float totalDistance, float finalSpeed, int numberOfElements) {
        //    this.totalDistance = totalDistance;
        //    this.finalSpeed = finalSpeed;
        //    this.numberOfElements = numberOfElements;
        //}
        @Override
        public String toString() {
            return Float.toString(totalDistance)
               +  "," + Float.toString(finalSpeed)
               +  "," + Integer.toString(numberOfElements)
               +  "," + Float.toString(throttleSpeedSetting);
        }
    }

    private static class ReturnValuesFromAct {
        // return values from a collection of scenes
        long passedTests;
        long failedTests;
        long zeroTests;
        //float finalSpeed;
        long[] lengthError = new long[102];
        long failedTestEndSpeed;
        long testTotalCount;
    }

}
