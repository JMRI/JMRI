package jmri.jmrit.display.switchboardEditor;

import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.Objects;

import org.junit.Ignore;
import org.junit.jupiter.api.*;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JMenuOperator;

import jmri.*;
import jmri.jmrit.display.AbstractEditorTestBase;
import jmri.jmrit.display.EditorFrameOperator;
import jmri.util.ColorUtil;
import jmri.util.JUnitUtil;
import org.netbeans.jemmy.operators.JPopupMenuOperator;

/**
 * Test simple functioning of SwitchboardEditor
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Egbert Broerse Copyright (C) 2017, 2020, 2021
 */
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SwitchboardEditorTest extends AbstractEditorTestBase<SwitchboardEditor> {

    // SwitchboardEditor e is already present in super

    @Override
    @Ignore("ChangeView is not applicable to SwitchBoards")
    @Test
    public void testChangeView() {
    }

    @Test
    public void testDefaultCtor() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        SwitchboardEditor p = new SwitchboardEditor();
        Assertions.assertNotNull(p, "exists");
        p.dispose();
    }

    @Test
    public void testStringCtor() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        Assertions.assertNotNull(e, "exists");
    }

    @Test
    public void checkOptionsMenuExists() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        e.setVisible(true);
        EditorFrameOperator jfo = new EditorFrameOperator(e);
        JMenuOperator jmo = new JMenuOperator(jfo, Bundle.getMessage("MenuOptions"));
        Assertions.assertNotNull(jmo, "Options Menu Exists");
        Assertions.assertEquals(10, jmo.getItemCount(), "Menu Item Count");
    }

    @Test
    public void checkSetEditable() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        e.setVisible(true);
        e.setAllEditable(true);
        EditorFrameOperator jfo = new EditorFrameOperator(e);
        JMenuBarOperator jmbo = new JMenuBarOperator(jfo);
        Assertions.assertEquals(4, jmbo.getMenuCount(), "Editable Menu Count: 4");
        e.setAllEditable(false);
        Assertions.assertEquals(3, jmbo.getMenuCount(), "Non-editable Menu Count: 3");
    }

    @Test
    public void testTurnoutSwitchPopup() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        Assertions.assertEquals("I", e.getSwitchManu(), "Internal connection default at startup");
        Assertions.assertEquals(1, e.getPanelMenuRangeMin(), "MinSpinner=1 default at startup");
        Assertions.assertEquals(24, e.getPanelMenuRangeMax(), "MaxSpinner=24 default at startup");
        Assertions.assertEquals("Turnouts", e.getSwitchTypeName(), "Type=Turnout default at startup");
        BeanSwitch sw = e.getSwitch("IT1");
        Assertions.assertNotNull(sw, "Found BeanSwitch IT1");
        sw.showPopUp(new MouseEvent(sw, 1, 0, 0, 0, 0, 1, false));
        JPopupMenuOperator jpmo = new JPopupMenuOperator();
        jpmo.pushMenuNoBlock(sw.getNameString()); // close it
    }

    @Test
    public void testIsDirty() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        // defaults to false.
        Assertions.assertFalse(e.isDirty(), "isDirty");
    }

    @Test
    public void testSetDirty() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        // defaults to false, setDirty() sets it to true.
        e.setDirty();
        Assertions.assertTrue(e.isDirty(), "isDirty after set");
    }

    @Test
    public void testSetDirtyWithParameter() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        // defaults to false, so set it to true.
        e.setDirty(true);
        Assertions.assertTrue(e.isDirty(), "isDirty after set");
    }

    @Test
    public void testResetDirty() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        // defaults to false, so set it to true.
        e.setDirty(true);
        // then call resetDirty, which sets it back to false.
        e.resetDirty();
        Assertions.assertFalse(e.isDirty(), "isDirty after reset");
    }

    @Test
    public void testGetSetDefaultTextColor() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        Assertions.assertEquals(ColorUtil.ColorBlack, e.getDefaultTextColor(), "Default Text Color");
        e.setDefaultTextColor(Color.PINK);
        Assertions.assertEquals(ColorUtil.ColorPink, e.getDefaultTextColor(), "Default Text Color after Set");
    }

    @Test
    public void testSwitchRangeTurnouts() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        Assertions.assertEquals("T", e.getSwitchType(), "Default Switch Type Turnouts");
        Assertions.assertEquals("button", e.getSwitchShape(), "Default Switch Shape Button");
        Assertions.assertEquals(0, e.getRows(), "Default Rows 0"); // autoRows on
        e.setRows(10);
        Assertions.assertEquals(10, e.getRows(), "Set Rows 10");
        e.setShowUserName(false);
        Assertions.assertEquals("no", e.showUserName(), "Show User Name is No");
        e.setSwitchShape("symbol");
        Assertions.assertEquals("symbol", e.getSwitchShape(), "Switch shape set to 'symbol'");
        ((TurnoutManager) e.getManager('T')).provideTurnout("IT9"); // connect to item 1
        e.getSwitch("IT8").okAddPressed(new ActionEvent(e, ActionEvent.ACTION_PERFORMED, "test")); // to item 2
        e.setHideUnconnected(true); // speed up redraw
        e.updatePressed(); // rebuild for new Turnouts + symbol shape
        Assertions.assertEquals(2, e.getTotal(), "2 connected switches shown");
    }

    @Test
    public void testSwitchInvertSensors() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        e.setSwitchType("S");
        e.updatePressed(); // rebuild for Sensors
        Sensor sensor20 = ((SensorManager) e.getManager('S')).provideSensor("IS20");
        Assertions.assertNotNull(jmri.InstanceManager.sensorManagerInstance().getSensor("IS20"));
        Objects.requireNonNull(InstanceManager.sensorManagerInstance().getSensor("IS20")).setUserName("twenty"); // make it harder to fetch label
        e.setHideUnconnected(true); // speed up redraw
        e.updatePressed(); // connect switch IS20 to Sensor IS20
        Assertions.assertEquals(Sensor.UNKNOWN, sensor20.getState(), "IS20 is Unknown");
        Assertions.assertNotNull(e.getSwitch("IS20"));
        Assertions.assertEquals("IS20: ?", e.getSwitch("IS20").getIconLabel(), "IS20 is Inactive");
        sensor20.setCommandedState(Sensor.ACTIVE);
        e.updatePressed(); // connect switch IS20 to Sensor IS20
        Assertions.assertEquals("IS20: +", e.getSwitch("IS20").getIconLabel(), "IS20 displays Active");
        e.getSwitch("IS20").doMouseClicked(new MouseEvent(e, 1, 0, 0, 0, 0, 1, false));
        Assertions.assertEquals(Sensor.INACTIVE, sensor20.getState(), "IS20 is Inactive");
        Assertions.assertEquals("IS20: -", e.getSwitch("IS20").getIconLabel(), "IS20 displays Inactive");
        e.getSwitch("IS20").setBeanInverted(true);
        Assertions.assertEquals(Sensor.ACTIVE, sensor20.getState(), "IS20 is Active");
        Assertions.assertEquals("IS20: +", e.getSwitch("IS20").getIconLabel(), "IS20 displays Active");
    }

    @Test
    public void testSwitchAllLights() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        e.setSwitchType("L");
        e.updatePressed(); // rebuild for Lights
        e.getSwitch("IL1").doMouseClicked(null); // no result, so nothing to test
        Assertions.assertNull(e.getSwitch("IL1").getLight(), "Click on unconnected switch");
        Light light1 = ((LightManager) e.getManager('L')).provideLight("IL1");
        Assertions.assertNotNull(jmri.InstanceManager.lightManagerInstance().getLight("IL1"));
        e.updatePressed(); // connect switch IL1 to Light IL1
        Assertions.assertEquals(Light.OFF, light1.getState(), "IL1 is Off");

        Assertions.assertNotNull(e.getSwitch("IL1"));
        e.getSwitch("IL1").doMouseClicked(new MouseEvent(e, 1, 0, 0, 0, 0, 1, false));
        Assertions.assertEquals(Light.ON, light1.getState(), "IL1 is On");

        e.switchAllLights(Light.OFF);
        Assertions.assertEquals(Light.OFF, light1.getState(), "IL1 is Off via All");

        Assertions.assertEquals(24, e.getTotal(), "Default 4x6 switches shown");
        e.setHideUnconnected(true);
        e.updatePressed(); // rebuild to hide unconnected
        Assertions.assertEquals(1, e.getTotal(), "1 connected switch shown");
    }

    @Test
    public void testGetSwitches() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        e.setMinSpinner(8);
        e.setMinSpinner(17);
        Assertions.assertEquals(8, e.getSwitches().size(), "Get array containing all items");
    }
    // from here down is testing infrastructure

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalLightManager();
        if (!GraphicsEnvironment.isHeadless()) {
            e = new SwitchboardEditor("Switchboard Editor Test");
        }
    }

    @AfterEach
    @Override
    public void tearDown() {
        if (e != null) {
           // dispose on Swing thread
           new EditorFrameOperator(e.getTargetFrame()).closeFrameWithConfirmations();
           e = null;
        }
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
