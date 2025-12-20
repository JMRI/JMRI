package jmri.jmrit.logix;

import jmri.InstanceManager;
import jmri.Memory;
import jmri.Sensor;
import jmri.SignalHead;
import jmri.Turnout;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the OPath class
 *
 * @author Pete Cressman Copyright 2014
 */
public class LogixActionTest {

    @Test
    @DisabledIfSystemProperty(named ="jmri.skipTestsRequiringSeparateRunning", matches ="true")
    public void testLogixAction() throws Exception {
        jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager() {
        };

        // load and display sample file. Panel file does not display screen
        java.io.File f = new java.io.File("java/test/jmri/jmrit/logix/valid/LogixActionTest.xml");
        cm.load(f);
        InstanceManager.getDefault(jmri.LogixManager.class).activateAllLogixs();

        Memory im6 = InstanceManager.memoryManagerInstance().getMemory("IM6");
        assertThat(im6).withFailMessage("Memory IM6").isNotNull();
        assertThat(im6.getValue()).withFailMessage("Contents IM6").isEqualTo("EastToWestOnSiding");

        // Find Enable Logix button  <<< Use GUI, but need Container to find button in
        // JUnitUtil.pressButton(container, "Enable/Disable Tests");
        // OK, do it this way
        Sensor sensor = InstanceManager.sensorManagerInstance().getSensor("enableButton");
        assertThat(sensor).withFailMessage("Sensor IS5").isNotNull();
        sensor.setState(Sensor.ACTIVE);
        sensor.setState(Sensor.INACTIVE);
        sensor.setState(Sensor.ACTIVE);
        SignalHead sh1 = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead("IH1");
        assertThat(sh1).withFailMessage("shi null").isNotNull();
        assertThat(sh1.getAppearance()).withFailMessage("SignalHead IH1").isEqualTo(SignalHead.RED);

        // do some buttons -Sensors
        sensor = InstanceManager.sensorManagerInstance().getSensor("ISLITERAL");
        assertThat(sensor).withFailMessage("sensor null").isNotNull();
        sensor.setState(Sensor.ACTIVE); // activate direct logix action
        Sensor is1 = InstanceManager.sensorManagerInstance().getSensor("sensor1");
        assertThat(is1).withFailMessage("is1 null").isNotNull();
        assertThat(is1.getState()).withFailMessage("direct set Sensor IS1 active").isEqualTo(Sensor.ACTIVE); // action
        sensor = InstanceManager.sensorManagerInstance().getSensor("ISINDIRECT");
        assertThat(sensor).withFailMessage("sensor null").isNotNull();
        sensor.setState(Sensor.ACTIVE); // activate Indirect logix action
        assertThat(is1.getState()).withFailMessage("Indirect set Sensor IS1 inactive").isEqualTo(Sensor.INACTIVE); // action

        // SignalHead buttons
        Sensor is4 = InstanceManager.sensorManagerInstance().getSensor("IS4");
        assertThat(is4).withFailMessage("is4 null").isNotNull();
        is4.setState(Sensor.ACTIVE); // activate direct logix action
        assertThat(sh1.getAppearance()).withFailMessage("direct set SignalHead IH1 to Green").isEqualTo(SignalHead.GREEN);
        is4.setState(Sensor.INACTIVE); // activate direct logix action
        assertThat(sh1.getAppearance()).withFailMessage("direct set SignalHead IH1 to Red").isEqualTo(SignalHead.RED);

        Memory im3 = InstanceManager.memoryManagerInstance().getMemory("IM3");
        assertThat(im3).withFailMessage("Memory IM3").isNotNull();
        assertThat(im3.getValue()).withFailMessage("Contents IM3").isEqualTo("IH1");
        Sensor is3 = InstanceManager.sensorManagerInstance().getSensor("IS3");
        assertThat(is3).withFailMessage("is3 null").isNotNull();
        is3.setState(Sensor.ACTIVE); // activate indirect logix action
        assertThat(sh1.getAppearance()).withFailMessage("Indirect set SignalHead IH1 to Green").isEqualTo(SignalHead.GREEN);
        is3.setState(Sensor.INACTIVE); // activate indirect logix action
        assertThat(sh1.getAppearance()).withFailMessage("Indirect set SignalHead IH1 to Red").isEqualTo(SignalHead.RED);
        // change memory value
        im3.setValue("IH2");
        is3.setState(Sensor.ACTIVE); // activate logix action
        assertThat(im3.getValue()).withFailMessage("Contents IM3").isEqualTo("IH2");
        SignalHead sh2 = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead("IH2");
        assertThat(sh2).withFailMessage("sh2 null").isNotNull();
        assertThat(sh2.getAppearance()).withFailMessage("Indirect SignalHead IH2").isEqualTo(SignalHead.GREEN);

        // Turnout Buttons
        Sensor is6 = InstanceManager.sensorManagerInstance().getSensor("IS6");
        assertThat(is6).withFailMessage("is6 null").isNotNull();
        is6.setState(Sensor.ACTIVE); // activate direct logix action
        Turnout it2 = InstanceManager.turnoutManagerInstance().getTurnout("IT2");
        assertThat(it2).withFailMessage("it2 null").isNotNull();
        assertThat(it2.getState()).withFailMessage("direct set Turnout IT2 to Closed").isEqualTo(Turnout.CLOSED);
        Memory im4 = InstanceManager.memoryManagerInstance().getMemory("IM4");
        assertThat(im4).withFailMessage("im4 null").isNotNull();
        assertThat(im4.getValue()).withFailMessage("Contents IM4").isEqualTo("IT3");
        Sensor is7 = InstanceManager.sensorManagerInstance().getSensor("IS7");
        assertThat(is7).withFailMessage("is7 null").isNotNull();
        is7.setState(Sensor.INACTIVE); // activate indirect logix action
        Turnout it3 = InstanceManager.turnoutManagerInstance().getTurnout("IT3");
        assertThat(it3).withFailMessage("it3 null").isNotNull();
        assertThat(it3.getState()).withFailMessage("Indirect set Turnout IT2 to Thrown").isEqualTo(Turnout.THROWN);
        is7.setState(Sensor.ACTIVE); // activate indirect logix action
        assertThat(it3.getState()).withFailMessage("Indirect set Turnout IT2 to Closed").isEqualTo(Turnout.CLOSED);
        // change memory value
        im4.setValue("IT2");
        assertThat(im4.getValue()).withFailMessage("Contents IM4").isEqualTo("IT2");
        is7.setState(Sensor.INACTIVE); // activate indirect logix action
        assertThat(it2.getState()).withFailMessage("Indirect set Turnout IT2 to Thrown").isEqualTo(Turnout.THROWN);
        is7.setState(Sensor.ACTIVE); // activate indirect logix action
        assertThat(it2.getState()).withFailMessage("Indirect set Turnout IT2 to Closed").isEqualTo(Turnout.CLOSED);

        // OBlock Buttons
        OBlock ob1 = InstanceManager.getDefault(OBlockManager.class).getOBlock("Left");
        assertThat(ob1.getState()).withFailMessage("OBlock OB1").isEqualTo((OBlock.OUT_OF_SERVICE | Sensor.INACTIVE));
        OBlock ob2 = InstanceManager.getDefault(OBlockManager.class).getOBlock("Right");
        assertThat(ob2.getState()).withFailMessage("OBlock OB2").isEqualTo((OBlock.TRACK_ERROR | Sensor.INACTIVE));
        Sensor is8 = InstanceManager.sensorManagerInstance().getSensor("IS8");
        assertThat(is8).withFailMessage("is8 null").isNotNull();
        is8.setState(Sensor.ACTIVE); // direct action
        assertThat(ob1.getState()).withFailMessage("Direct set OBlock OB1 to normal").isEqualTo(Sensor.INACTIVE);
        is8.setState(Sensor.INACTIVE); // direct action
        assertThat(ob1.getState()).withFailMessage("Direct set OBlock OB1 to OOS").isEqualTo((OBlock.OUT_OF_SERVICE | Sensor.INACTIVE));
        Sensor is9 = InstanceManager.sensorManagerInstance().getSensor("IS9");
        assertThat(is9).withFailMessage("is9 null").isNotNull();
        is9.setState(Sensor.ACTIVE); // indirect action
        assertThat(ob2.getState()).withFailMessage("Indirect set OBlock OB2 to normal").isEqualTo(Sensor.INACTIVE);
        // change memory value
        Memory im5 = InstanceManager.memoryManagerInstance().getMemory("IM5");
        assertThat(im5).withFailMessage("im5 null").isNotNull();
        im5.setValue("OB1");
        is9.setState(Sensor.INACTIVE); // indirect action
        assertThat(ob1.getState()).withFailMessage("Indirect set OBlock OB1 to normal").isEqualTo((OBlock.TRACK_ERROR | OBlock.OUT_OF_SERVICE | Sensor.INACTIVE));
        is9.setState(Sensor.ACTIVE); // indirect action
        is8.setState(Sensor.ACTIVE); // indirect action
        assertThat(ob1.getState()).withFailMessage("Direct set OBlock OB1 to normal").isEqualTo(Sensor.INACTIVE);

        // Warrant buttons
        Sensor is14 = InstanceManager.sensorManagerInstance().getSensor("IS14");
        assertThat(is14).withFailMessage("is14 null").isNotNull();
        is14.setState(Sensor.ACTIVE); // indirect action
        Warrant w = InstanceManager.getDefault(WarrantManager.class).getWarrant("EastToWestOnSiding");
        assertThat(w.isAllocated()).withFailMessage("warrant EastToWestOnSiding allocated").isTrue();
        Sensor is15 = InstanceManager.sensorManagerInstance().getSensor("IS15");
        assertThat(is15).withFailMessage("is15 null").isNotNull();
        is15.setState(Sensor.ACTIVE); // indirect action
        Assert.assertFalse("warrant EastToWestOnSiding deallocated", w.isAllocated());
        // change memory value
        im6.setValue("WestToEastOnMain");
        is14.setState(Sensor.INACTIVE); // toggle
        is14.setState(Sensor.ACTIVE); // indirect action
        Warrant w2 = InstanceManager.getDefault(WarrantManager.class).getWarrant("WestToEastOnMain");
        assertThat(w2.isAllocated()).withFailMessage("warrant WestToEastOnMain allocated").isTrue();
        im6.setValue("LeftToRightOnPath");
        is14.setState(Sensor.INACTIVE); // toggle
        is14.setState(Sensor.ACTIVE); // indirect action
        w = InstanceManager.getDefault(WarrantManager.class).getWarrant("LeftToRightOnPath");
        assertThat(w.isAllocated()).withFailMessage("warrant LeftToRightOnPath allocated").isTrue();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();

        JUnitUtil.initLogixManager();
        JUnitUtil.initConditionalManager();
        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);
        JUnitUtil.initWarrantManager();
    }

    @AfterEach
    public void tearDown() {
        InstanceManager.getDefault(WarrantManager.class).dispose();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
