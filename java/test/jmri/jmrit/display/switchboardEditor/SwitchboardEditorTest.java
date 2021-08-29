package jmri.jmrit.display.switchboardEditor;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.Objects;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.netbeans.jemmy.operators.*;

import jmri.*;
import jmri.jmrit.display.AbstractEditorTestBase;
import jmri.jmrit.display.EditorFrameOperator;
import jmri.util.ColorUtil;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

/**
 * Test functioning of SwitchboardEditor.
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Egbert Broerse Copyright (C) 2017, 2020, 2021
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class SwitchboardEditorTest extends AbstractEditorTestBase<SwitchboardEditor> {

    // SwitchboardEditor e is already present in super
    private EditorFrameOperator jfo;

    @Override
    @Disabled("ChangeView is not applicable to SwitchBoards")
    @Test
    public void testChangeView() {
    }

    @Test
    public void testDefaultCtor() {
        e = new SwitchboardEditor();
        Assertions.assertNotNull(e, "exists");
    }

    @Test
    public void testStringCtor() {
        Assertions.assertNotNull(e, "exists");
    }

    @Test
    public void checkOptionsMenuExists() {
        JMenuOperator jmo = new JMenuOperator(jfo, Bundle.getMessage("MenuOptions"));
        Assertions.assertNotNull(jmo, "Options Menu Exists");
        Assertions.assertEquals(10, jmo.getItemCount(), "Menu Item Count");
    }

    @Test
    public void checkSetEditable() {
        e.setAllEditable(true);
        JMenuBarOperator jmbo = new JMenuBarOperator(jfo);
        Assertions.assertEquals(4, jmbo.getMenuCount(), "Editable Menu Count: 4");
        e.setAllEditable(false);
        Assertions.assertEquals(3, jmbo.getMenuCount(), "Non-editable Menu Count: 3");
        e.setAllEditable(true); // reset to be able to get to the menuBar to delete e
    }

    @Test
    @Disabled("Works locally (Linux) and on Appveyor (Windows).  Unable to find popup after click on Travis")
    public void testTurnoutSwitchPopup() {
        e.setSwitchManu("I");
        e.setSwitchType("T");
        // initially selected connection should be Internal
        Assertions.assertEquals("I", e.getSwitchManu(), "Internal connection default at startup");
        Assertions.assertEquals(1, e.getPanelMenuRangeMin(), "MinSpinner=1 default at startup");
        Assertions.assertEquals(24, e.getPanelMenuRangeMax(), "MaxSpinner=24 default at startup");
        Assertions.assertEquals("Turnouts", e.getSwitchTypeName(), "Type=Turnout default at startup");
        BeanSwitch sw = e.getSwitch("IT1");
        Assertions.assertNotNull(sw, "Found BeanSwitch IT1");

        Thread popup_thread1 = new Thread(() -> {
            JPopupMenuOperator jpmo = new JPopupMenuOperator();
            jpmo.pushMenuNoBlock(sw.getNameString()); // close it
        });
        popup_thread1.setName("Switch popup");
        popup_thread1.start();

        sw.showPopUp(new MouseEvent(sw, 1, 0, 0, 0, 0, 1, false));

        JUnitUtil.waitFor(() -> !(popup_thread1.isAlive()), "Switch popup");
    }

    @Test
    public void testStartsNotDirty() {
        // defaults to false.
        Assertions.assertFalse(e.isDirty(), "isDirty starts as false");
    }

    @Test
    public void testSetDirty() {
        // defaults to false, setDirty() sets it to true.
        e.setDirty();
        Assertions.assertTrue(e.isDirty(), "isDirty after set");
    }

    @Test
    public void testSetDirtyWithParameter() {
        // defaults to false, so set it to true.
        e.setDirty(true);
        Assertions.assertTrue(e.isDirty(), "isDirty after set");
    }

    @Test
    public void testResetDirty() {
        // defaults to false, so set it to true.
        e.setDirty(true);
        // then call resetDirty, which sets it back to false.
        e.resetDirty();
        Assertions.assertFalse(e.isDirty(), "isDirty after reset");
    }

    @Test
    public void testGetSetDefaultTextColor() {
        Assertions.assertEquals(ColorUtil.ColorBlack, e.getDefaultTextColor(), "Default Text Color");
        e.setDefaultTextColor(Color.PINK);
        Assertions.assertEquals(ColorUtil.ColorPink, e.getDefaultTextColor(), "Default Text Color after Set");
    }

    @Test
    public void testSwitchRangeTurnouts() {
        e.setSwitchType("T");
        e.setSwitchManu("I"); // set explicitly
        Assertions.assertEquals("T", e.getSwitchType(), "Default Switch Type is Turnouts");
        Assertions.assertEquals("button", e.getSwitchShape(), "Default Switch Shape Button");
        Assertions.assertEquals(0, e.getRows(), "Default Rows 0"); // autoRows on
        e.setRows(10); // will turn off autoRows checkboxmenu
        Assertions.assertEquals(10, e.getRows(), "Rows should now be be 10");
        e.setShowUserName(false);
        Assertions.assertEquals("no", e.showUserName(), "Show User Name is No");
        e.setSwitchShape("symbol");
        Assertions.assertEquals("symbol", e.getSwitchShape(), "Switch shape set to 'symbol'");
        ((TurnoutManager) e.getManager('T')).provideTurnout("IT9"); // will connect to item 1

        e.getSwitch("IT8").okAddPressed(new ActionEvent(e, ActionEvent.ACTION_PERFORMED, "test")); // and to item 2

        e.setHideUnconnected(true); // speed up redraw
        e.updatePressed(); // rebuild for new Turnouts + symbol shape
        Assertions.assertEquals(2, e.getTotal(), "1 connected switch shown");
    }

    @Test
    public void testSwitchInvertSensors() {
        e.setSwitchType("S");
        // initially selected connection should be Internal but is not 100% predictable (after type is changed?)
        e.setSwitchManu("I"); // so set explicitly
        e.updatePressed();    // rebuild for Sensors
        SensorManager sm = ((SensorManager) e.getManager('S'));
        Sensor sensor20 = sm.provideSensor("IS20");
        Assertions.assertNotNull(sm.getSensor("IS20"));
        Objects.requireNonNull(sm.getSensor("IS20")).setUserName("twenty"); // make it harder to fetch label
        e.updatePressed(); // recreate to connect switch "IS20" to Sensor sensor20
        Assertions.assertEquals(24, e.getSwitches().size(), "24 (connected) item displayed");
        Assertions.assertEquals(Sensor.UNKNOWN, sensor20.getState(), "sensor20 state is Unknown");
        Assertions.assertNotNull(e.getSwitch("IS20"));
        Assertions.assertEquals("IS20: ?", e.getSwitch("IS20").getIconLabel(), "IS20 displays Unknown");
        sensor20.setCommandedState(Sensor.ACTIVE);
        // should follow state
        Assertions.assertEquals("IS20: +", e.getSwitch("IS20").getIconLabel(), "IS20 displays Active");
        e.getSwitch("IS20").doMouseClicked(new MouseEvent(e, 1, 0, 0, 0, 0, 1, false));
        Assertions.assertEquals(Sensor.INACTIVE, sensor20.getState(), "sensor20 state is Inactive");
        Assertions.assertEquals("IS20: -", e.getSwitch("IS20").getIconLabel(), "IS20 displays Inactive");
        e.getSwitch("IS20").setBeanInverted(true);
        Assertions.assertEquals(Sensor.ACTIVE, sensor20.getState(), "sensor20 state is Active");
        Assertions.assertEquals("IS20: +", e.getSwitch("IS20").getIconLabel(), "IS20 displays Active");
    }

    @Test
    public void testHideUnconnected() {
        // initially selected connection should be Internal but is not 100% predictable (after type is changed?)
        e.setSwitchType("T");
        e.setSwitchManu("I"); // so set explicitly
        Assertions.assertTrue(e.getManager() instanceof TurnoutManager, "manager is a TurnoutManager");
        ((TurnoutManager) e.getManager()).provideTurnout("IT24"); // active manager should be a TurnoutManager, connect to item 1
        e.setHideUnconnected(true);
        e.updatePressed(); // setHideUnconnected will not invoke updatePressed
        Assertions.assertEquals(1, e.getTotal(), "1 connected switch shown");
    }

    @Test
    public void testSwitchAllLights() {
        e.setSwitchType("L");
        e.setSwitchManu("I"); // so set explicitly as LightManager
        e.updatePressed(); // rebuild for Lights
        e.getSwitch("IL1").doMouseClicked(null); // no result, so nothing to test
        Assertions.assertNull(e.getSwitch("IL1").getLight(), "Click on unconnected switch");
        Light light1 = ((LightManager) e.getManager('L')).provideLight("IL1");
        Assertions.assertNotNull(InstanceManager.lightManagerInstance().getLight("IL1"));
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
        e.setMinSpinner(8);
        e.setMaxSpinner(17);
        e.updatePressed();
        Assertions.assertEquals(10, e.getSwitches().size(), "Get array containing all items");
    }

    // from here down is testing infrastructure

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalLightManager();

        e = new SwitchboardEditor("Switchboard Editor Test");
        e.setVisible(true);
        JemmyUtil.waitFor(e);
        jfo = new EditorFrameOperator(e);
    }

    @AfterEach
    @Override
    public void tearDown() {
        if (e != null) {
            // dispose on Swing thread
            jfo.deleteViaFileMenuWithConfirmations();
            e = null;
        }
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
