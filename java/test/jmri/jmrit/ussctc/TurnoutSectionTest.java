package jmri.jmrit.ussctc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.*;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for TurnoutSection class in the jmri.jmrit.ussctc package
 *
 * @author Bob Jacobsen Copyright 2007
 */
@SuppressWarnings("unchecked")
public class TurnoutSectionTest {

    @Test
    public void testConstruction() {
        TurnoutSection t = new TurnoutSection("Sec 1 Layout TO", "Sec1 TO 1 N",
            "Sec1 TO 1 R", "Sec1 TO 1 N", "Sec1 TO 1 R", station);
        assertNotNull(t);
    }

    @Test
    public void testInitialSet() throws JmriException {
        layoutTurnout.setCommandedState(Turnout.THROWN);
        normSensor.setState(Sensor.INACTIVE);
        revSensor.setState(Sensor.INACTIVE);

        TurnoutSection t = new TurnoutSection("Sec 1 Layout TO", "Sec1 TO 1 N",
            "Sec1 TO 1 R", "Sec1 TO 1 N", "Sec1 TO 1 R", station);
        assertNotNull(t);

        // initialization sets indicators to follow actual turnout state
        assertEquals(Turnout.THROWN, layoutTurnout.getKnownState());
        assertEquals(Turnout.CLOSED, normIndicator.getCommandedState());
        assertEquals(Turnout.THROWN,  revIndicator.getCommandedState());

    }

    @Test
    public void testLayoutMonitoring() throws JmriException {
        layoutTurnout.setCommandedState(Turnout.THROWN);

        TurnoutSection t = new TurnoutSection("Sec 1 Layout TO", "Sec1 TO 1 N",
             "Sec1 TO 1 R", "Sec1 TO 1 N", "Sec1 TO 1 R", station);
        assertNotNull(t);

        layoutTurnout.setCommandedState(Turnout.CLOSED);

        // initialization sets indicators to follow actual turnout state
        assertTrue(requestIndicationStart);
    }

    @Test
    public void testCodeSendStartReturns() throws JmriException {
        // return value depends only on set inputs
        TurnoutSection t = new TurnoutSection("Sec 1 Layout TO", "Sec1 TO 1 N", "Sec1 TO 1 R", "Sec1 TO 1 N", "Sec1 TO 1 R", station);

        normSensor.setState(Sensor.ACTIVE);
        revSensor.setState(Sensor.INACTIVE);
        assertEquals(CodeGroupTwoBits.Double10, t.codeSendStart());

        normSensor.setState(Sensor.INACTIVE);
        revSensor.setState(Sensor.ACTIVE);
        assertEquals(CodeGroupTwoBits.Double01, t.codeSendStart());
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
        assertEquals(CodeGroupTwoBits.Double10, t.codeSendStart());
        assertEquals(Turnout.THROWN, normIndicator.getKnownState());
        assertEquals(Turnout.CLOSED, revIndicator.getKnownState());

        normSensor.setState(Sensor.INACTIVE);
        revSensor.setState(Sensor.ACTIVE);
        t.central.state = TurnoutSection.TurnoutCentralSection.State.SHOWING_REVERSED;
        normIndicator.setCommandedState(Turnout.CLOSED);
        revIndicator.setCommandedState(Turnout.THROWN);
        assertEquals(CodeGroupTwoBits.Double01, t.codeSendStart());
        assertEquals(Turnout.CLOSED, normIndicator.getKnownState());
        assertEquals(Turnout.THROWN, revIndicator.getKnownState());

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

        assertEquals(CodeGroupTwoBits.Double10, t.codeSendStart());
        assertEquals(Turnout.CLOSED, normIndicator.getKnownState());
        assertEquals(Turnout.CLOSED, revIndicator.getKnownState());

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

        assertEquals(CodeGroupTwoBits.Double01, t.codeSendStart());
        assertEquals(Turnout.CLOSED, normIndicator.getKnownState());
        assertEquals(Turnout.CLOSED, revIndicator.getKnownState());

    }

    @Test
    public void testIndicationStart() throws JmriException {

        // test getting indication from layout
        TurnoutSection t = new TurnoutSection("Sec 1 Layout TO", "Sec1 TO 1 N", "Sec1 TO 1 R", "Sec1 TO 1 N", "Sec1 TO 1 R", station);

        // check multiple patterns for state -> return value
        t.field.lastCodeValue = CodeGroupTwoBits.Double10;  // access for testing
        layoutTurnout.setCommandedState(Turnout.CLOSED);
        assertEquals(CodeGroupTwoBits.Double10, t.indicationStart(), "CLOSED OK");

        t.field.lastCodeValue = CodeGroupTwoBits.Double10;
        layoutTurnout.setCommandedState(Turnout.THROWN);
        assertEquals(CodeGroupTwoBits.Double00, t.indicationStart(), "CLOSED out of correspondence");

        t.field.lastCodeValue = CodeGroupTwoBits.Double01;
        layoutTurnout.setCommandedState(Turnout.THROWN);
        assertEquals(CodeGroupTwoBits.Double01, t.indicationStart(), "THROWN OK");

        t.field.lastCodeValue = CodeGroupTwoBits.Double01;
        layoutTurnout.setCommandedState(Turnout.CLOSED);
        assertEquals(CodeGroupTwoBits.Double00, t.indicationStart(), "THROWN out of correspondence");

    }

    @Test
    public void testIndicationComplete00()  throws JmriException  {

        TurnoutSection t = new TurnoutSection("Sec 1 Layout TO", "Sec1 TO 1 N", "Sec1 TO 1 R", "Sec1 TO 1 N", "Sec1 TO 1 R", station);
        normIndicator.setCommandedState(Turnout.INCONSISTENT);
        revIndicator.setCommandedState(Turnout.INCONSISTENT);

        t.indicationComplete(CodeGroupTwoBits.Double00);

        assertEquals(Turnout.CLOSED, normIndicator.getKnownState());
        assertEquals(Turnout.CLOSED, revIndicator.getKnownState());
    }

    @Test
    public void testIndicationComplete10()  throws JmriException  {

        TurnoutSection t = new TurnoutSection("Sec 1 Layout TO", "Sec1 TO 1 N", "Sec1 TO 1 R", "Sec1 TO 1 N", "Sec1 TO 1 R", station);
        normIndicator.setCommandedState(Turnout.INCONSISTENT);
        revIndicator.setCommandedState(Turnout.INCONSISTENT);

        t.indicationComplete(CodeGroupTwoBits.Double10);

        assertEquals(Turnout.THROWN, normIndicator.getKnownState());
        assertEquals(Turnout.CLOSED, revIndicator.getKnownState());
    }

    @Test
    public void testIndicationComplete01()  throws JmriException  {

        TurnoutSection t = new TurnoutSection("Sec 1 Layout TO", "Sec1 TO 1 N", "Sec1 TO 1 R", "Sec1 TO 1 N", "Sec1 TO 1 R", station);
        normIndicator.setCommandedState(Turnout.INCONSISTENT);
        revIndicator.setCommandedState(Turnout.INCONSISTENT);

        t.indicationComplete(CodeGroupTwoBits.Double01);

        assertEquals(Turnout.CLOSED, normIndicator.getKnownState());
        assertEquals(Turnout.THROWN, revIndicator.getKnownState());
    }

    private CodeLine codeline;
    private Station station;
    private boolean requestIndicationStart;

    private Turnout layoutTurnout;
    private Turnout normIndicator;
    private Turnout revIndicator;
    private Sensor normSensor;
    private Sensor revSensor;

    @BeforeEach
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

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
