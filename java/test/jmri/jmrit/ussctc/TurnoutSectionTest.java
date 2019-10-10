package jmri.jmrit.ussctc;

import jmri.util.JUnitUtil;
import jmri.*;
import org.junit.*;

/**
 * Tests for TurnoutSection class in the jmri.jmrit.ussctc package
 *
 * @author	Bob Jacobsen Copyright 2007
 */
public class TurnoutSectionTest {

    @Test
    public void testConstruction() {
        new TurnoutSection("Sec 1 Layout TO", "Sec1 TO 1 N", "Sec1 TO 1 R", "Sec1 TO 1 N", "Sec1 TO 1 R", station);
    }
 
    @Test
    public void testInitialSet() throws JmriException {
        layoutTurnout.setCommandedState(Turnout.THROWN);
        normSensor.setState(Sensor.INACTIVE);
        revSensor.setState(Sensor.INACTIVE);
        
        new TurnoutSection("Sec 1 Layout TO", "Sec1 TO 1 N", "Sec1 TO 1 R", "Sec1 TO 1 N", "Sec1 TO 1 R", station);
        
        // initialization sets indicators to follow actual turnout state
        Assert.assertEquals(Turnout.THROWN, layoutTurnout.getKnownState());
        Assert.assertEquals(Turnout.CLOSED, normIndicator.getCommandedState());
        Assert.assertEquals(Turnout.THROWN,  revIndicator.getCommandedState());
        
    }

    @Test
    public void testLayoutMonitoring() throws JmriException {
        layoutTurnout.setCommandedState(Turnout.THROWN);        
        
        new TurnoutSection("Sec 1 Layout TO", "Sec1 TO 1 N", "Sec1 TO 1 R", "Sec1 TO 1 N", "Sec1 TO 1 R", station);
        
        layoutTurnout.setCommandedState(Turnout.CLOSED);
        
        // initialization sets indicators to follow actual turnout state
        Assert.assertTrue(requestIndicationStart);        
    }

    @Test 
    public void testCodeSendStartReturns() throws JmriException {
        // return value depends only on set inputs
        TurnoutSection t = new TurnoutSection("Sec 1 Layout TO", "Sec1 TO 1 N", "Sec1 TO 1 R", "Sec1 TO 1 N", "Sec1 TO 1 R", station);

        normSensor.setState(Sensor.ACTIVE);
        revSensor.setState(Sensor.INACTIVE);
        Assert.assertEquals(CodeGroupTwoBits.Double10, t.codeSendStart());
                
        normSensor.setState(Sensor.INACTIVE);
        revSensor.setState(Sensor.ACTIVE);
        Assert.assertEquals(CodeGroupTwoBits.Double01, t.codeSendStart());
    }
    
    @Test 
    public void testCodeSendStartIndicatorsUnchanged() throws JmriException {
        // unchanged setting, not changing indicators
        TurnoutSection t = new TurnoutSection("Sec 1 Layout TO", "Sec1 TO 1 N", "Sec1 TO 1 R", "Sec1 TO 1 N", "Sec1 TO 1 R", station);

        normSensor.setState(Sensor.ACTIVE);
        revSensor.setState(Sensor.INACTIVE);
        t.central.state = TurnoutSection.TurnoutCentralSection.State.SHOWING_NORMAL;
        normIndicator.setCommandedState(Turnout.THROWN);
        revIndicator.setCommandedState(Turnout.CLOSED);
        Assert.assertEquals(CodeGroupTwoBits.Double10, t.codeSendStart());
        Assert.assertEquals(Turnout.THROWN, normIndicator.getKnownState());
        Assert.assertEquals(Turnout.CLOSED, revIndicator.getKnownState());
        
        normSensor.setState(Sensor.INACTIVE);
        revSensor.setState(Sensor.ACTIVE);
        t.central.state = TurnoutSection.TurnoutCentralSection.State.SHOWING_REVERSED;
        normIndicator.setCommandedState(Turnout.CLOSED);
        revIndicator.setCommandedState(Turnout.THROWN);
        Assert.assertEquals(CodeGroupTwoBits.Double01, t.codeSendStart());
        Assert.assertEquals(Turnout.CLOSED, normIndicator.getKnownState());
        Assert.assertEquals(Turnout.THROWN, revIndicator.getKnownState());
        
    }
    
    @Test 
    public void testCodeSendStartIndicatorsChangedToN() throws JmriException {
        // unchanged setting, not changing indicators

        // test getting indication from layout
        TurnoutSection t = new TurnoutSection("Sec 1 Layout TO", "Sec1 TO 1 N", "Sec1 TO 1 R", "Sec1 TO 1 N", "Sec1 TO 1 R", station);
        normSensor.setState(Sensor.ACTIVE);
        revSensor.setState(Sensor.INACTIVE);
        t.central.state = TurnoutSection.TurnoutCentralSection.State.SHOWING_REVERSED;
        normIndicator.setCommandedState(Turnout.CLOSED);
        revIndicator.setCommandedState(Turnout.THROWN);
        
        Assert.assertEquals(CodeGroupTwoBits.Double10, t.codeSendStart());
        Assert.assertEquals(Turnout.CLOSED, normIndicator.getKnownState());
        Assert.assertEquals(Turnout.CLOSED, revIndicator.getKnownState());
        
    }
    
    @Test 
    public void testCodeSendStartIndicatorsChangedToR() throws JmriException {
        // unchanged setting, not changing indicators

        // test getting indication from layout
        TurnoutSection t = new TurnoutSection("Sec 1 Layout TO", "Sec1 TO 1 N", "Sec1 TO 1 R", "Sec1 TO 1 N", "Sec1 TO 1 R", station);
        normSensor.setState(Sensor.INACTIVE);
        revSensor.setState(Sensor.ACTIVE);
        t.central.state = TurnoutSection.TurnoutCentralSection.State.SHOWING_NORMAL;
        normIndicator.setCommandedState(Turnout.THROWN);
        revIndicator.setCommandedState(Turnout.CLOSED);
        
        Assert.assertEquals(CodeGroupTwoBits.Double01, t.codeSendStart());
        Assert.assertEquals(Turnout.CLOSED, normIndicator.getKnownState());
        Assert.assertEquals(Turnout.CLOSED, revIndicator.getKnownState());
        
    }
    
    @Test
    public void testIndicationStart() throws JmriException {
        
        // test getting indication from layout
        TurnoutSection t = new TurnoutSection("Sec 1 Layout TO", "Sec1 TO 1 N", "Sec1 TO 1 R", "Sec1 TO 1 N", "Sec1 TO 1 R", station);

        // check multiple patterns for state -> return value
        t.field.lastCodeValue = CodeGroupTwoBits.Double10;  // access for testing
        layoutTurnout.setCommandedState(Turnout.CLOSED);
        Assert.assertEquals("CLOSED OK", CodeGroupTwoBits.Double10, t.indicationStart());  
              
        t.field.lastCodeValue = CodeGroupTwoBits.Double10;
        layoutTurnout.setCommandedState(Turnout.THROWN);
        Assert.assertEquals("CLOSED out of correspondence", CodeGroupTwoBits.Double00, t.indicationStart());  
              
        t.field.lastCodeValue = CodeGroupTwoBits.Double01;
        layoutTurnout.setCommandedState(Turnout.THROWN);
        Assert.assertEquals("THROWN OK", CodeGroupTwoBits.Double01, t.indicationStart());  
              
        t.field.lastCodeValue = CodeGroupTwoBits.Double01;
        layoutTurnout.setCommandedState(Turnout.CLOSED);
        Assert.assertEquals("THROWN out of correspondence", CodeGroupTwoBits.Double00, t.indicationStart());  
              
    }

    @Test
    public void testIndicationComplete00()  throws JmriException  {

        TurnoutSection t = new TurnoutSection("Sec 1 Layout TO", "Sec1 TO 1 N", "Sec1 TO 1 R", "Sec1 TO 1 N", "Sec1 TO 1 R", station);
        normIndicator.setCommandedState(Turnout.INCONSISTENT);
        revIndicator.setCommandedState(Turnout.INCONSISTENT);
        
        t.indicationComplete(CodeGroupTwoBits.Double00);

        Assert.assertEquals(Turnout.CLOSED, normIndicator.getKnownState());
        Assert.assertEquals(Turnout.CLOSED, revIndicator.getKnownState());
    }

    @Test
    public void testIndicationComplete10()  throws JmriException  {

        TurnoutSection t = new TurnoutSection("Sec 1 Layout TO", "Sec1 TO 1 N", "Sec1 TO 1 R", "Sec1 TO 1 N", "Sec1 TO 1 R", station);
        normIndicator.setCommandedState(Turnout.INCONSISTENT);
        revIndicator.setCommandedState(Turnout.INCONSISTENT);
        
        t.indicationComplete(CodeGroupTwoBits.Double10);

        Assert.assertEquals(Turnout.THROWN, normIndicator.getKnownState());
        Assert.assertEquals(Turnout.CLOSED, revIndicator.getKnownState());
    }

    @Test
    public void testIndicationComplete01()  throws JmriException  {

        TurnoutSection t = new TurnoutSection("Sec 1 Layout TO", "Sec1 TO 1 N", "Sec1 TO 1 R", "Sec1 TO 1 N", "Sec1 TO 1 R", station);
        normIndicator.setCommandedState(Turnout.INCONSISTENT);
        revIndicator.setCommandedState(Turnout.INCONSISTENT);
        
        t.indicationComplete(CodeGroupTwoBits.Double01);

        Assert.assertEquals(Turnout.CLOSED, normIndicator.getKnownState());
        Assert.assertEquals(Turnout.THROWN, revIndicator.getKnownState());
    }

    CodeLine codeline;
    Station station;
    boolean requestIndicationStart;
    
    Turnout layoutTurnout;
    Turnout normIndicator;
    Turnout revIndicator;
    Sensor normSensor;
    Sensor revSensor;
    
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
        
        layoutTurnout = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT1"); layoutTurnout.setUserName("Sec 1 Layout TO");
        normIndicator = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT2"); normIndicator.setUserName("Sec1 TO 1 N");
        revIndicator  = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT3");  revIndicator.setUserName("Sec1 TO 1 R");

        normSensor = InstanceManager.getDefault(SensorManager.class).provideSensor("IT2"); normSensor.setUserName("Sec1 TO 1 N");
        revSensor  = InstanceManager.getDefault(SensorManager.class).provideSensor("IT3");  revSensor.setUserName("Sec1 TO 1 R");

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
