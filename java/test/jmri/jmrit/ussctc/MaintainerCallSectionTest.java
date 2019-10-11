package jmri.jmrit.ussctc;

import jmri.util.JUnitUtil;
import jmri.*;
import org.junit.*;

/**
 * Tests for MaintainerCallSection class in the jmri.jmrit.ussctc package.
 *
 * @author	Bob Jacobsen Copyright 2007
 */
public class MaintainerCallSectionTest {

    @Test
    public void testConstruction() {
        new MaintainerCallSection("Sec1 MC input", "Sec 1 MC output", station);
    }
 
    @Test 
    public void testCodeSendStartReturns() throws JmriException {
        mcLayoutTurnout.setCommandedState(Turnout.THROWN);
        panelSensor.setKnownState(Sensor.INACTIVE);
        MaintainerCallSection t = new MaintainerCallSection("Sec1 MC input", "Sec 1 MC output", station);

        // return value depends only on set inputs

        panelSensor.setKnownState(Sensor.ACTIVE);
        Assert.assertEquals(CodeGroupOneBit.Single1, t.codeSendStart());
                
        panelSensor.setKnownState(Sensor.INACTIVE);
        Assert.assertEquals(CodeGroupOneBit.Single0, t.codeSendStart());
    }
    
    @Test 
    public void testCodeValueDelivered1() throws JmriException {
        mcLayoutTurnout.setCommandedState(Turnout.CLOSED);
        panelSensor.setKnownState(Sensor.INACTIVE);
        MaintainerCallSection t = new MaintainerCallSection("Sec1 MC input", "Sec 1 MC output", station);
        
        t.codeValueDelivered(CodeGroupOneBit.Single1);
        Assert.assertEquals(Turnout.THROWN, mcLayoutTurnout.getCommandedState());
    }

    @Test 
    public void testCodeValueDelivered0() throws JmriException {
        mcLayoutTurnout.setCommandedState(Turnout.THROWN);
        panelSensor.setKnownState(Sensor.INACTIVE);
        MaintainerCallSection t = new MaintainerCallSection("Sec1 MC input", "Sec 1 MC output", station);
        
        t.codeValueDelivered(CodeGroupOneBit.Single0);
        Assert.assertEquals(Turnout.CLOSED, mcLayoutTurnout.getCommandedState());
    }

    CodeLine codeline;
    Station station;
    boolean requestIndicationStart;
    
    Turnout mcLayoutTurnout;
    Sensor panelSensor;
    
    // The minimal setup for log4J
    @org.junit.Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initMemoryManager();  
        
        mcLayoutTurnout = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT1"); mcLayoutTurnout.setUserName("Sec 1 MC output");

        panelSensor = InstanceManager.getDefault(SensorManager.class).provideSensor("IS2"); panelSensor.setUserName("Sec1 MC input");

        codeline = new CodeLine("Code Indication Start", "Code Send Start", "IT101", "IT102", "IT103", "IT104");
        
        requestIndicationStart = false;
        station = new Station("test", codeline, new CodeButton("IS221", "IS222")) {
            @Override
            public void requestIndicationStart() {
                requestIndicationStart = true;
            }
        };
    }

    @org.junit.After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
