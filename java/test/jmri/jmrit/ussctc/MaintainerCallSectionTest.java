package jmri.jmrit.ussctc;

import jmri.util.JUnitUtil;
import jmri.*;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for MaintainerCallSection class in the jmri.jmrit.ussctc package.
 *
 * @author Bob Jacobsen Copyright 2007
 */
@SuppressWarnings("unchecked")
public class MaintainerCallSectionTest {

    @Test
    public void testConstruction() {
        Assert.assertNotNull(new MaintainerCallSection("Sec1 MC input", "Sec 1 MC output", station));
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

    @Test
    public void testImplListener(){
        Assertions.assertNotNull(station);
        Assertions.assertFalse(requestIndicationStart);
        station.requestIndicationStart();
        Assertions.assertTrue(requestIndicationStart);
    }

    CodeLine codeline;
    Station station = null;
    boolean requestIndicationStart;

    private Turnout mcLayoutTurnout = null;
    private Sensor panelSensor = null;

    private class StationImpl extends Station<CodeGroupOneBit,CodeGroupNoBits> {

        StationImpl(String name, CodeLine codeline, CodeButton button) {
            super(name, codeline, button);
            requestIndicationStart = false;
        }

        @Override
        public void requestIndicationStart() {
            requestIndicationStart = true;
        }
    }

    @BeforeEach
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

        station = new StationImpl("test", codeline, new CodeButton("IS221", "IS222"));
        
        Assertions.assertNotNull(mcLayoutTurnout);
        Assertions.assertNotNull(panelSensor);
        Assertions.assertNotNull(station);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
