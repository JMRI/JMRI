package jmri.jmrit.beantable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.GraphicsEnvironment;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JDialog;

import org.junit.*;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.util.NameComponentChooser;

import jmri.InstanceManager;
import jmri.Light;
import jmri.Route;
import jmri.Sensor;
import jmri.SignalHead;
import jmri.Turnout;
import jmri.util.JUnitUtil;

/**
 * Tests for the jmri.jmrit.beantable.LRouteTableAction class
 *
 * @author	Pete Cressman Copyright 2009
 */
public class LRouteTableActionTest {

    static final ResourceBundle rbx = ResourceBundle
            .getBundle("jmri.jmrit.beantable.LRouteTableBundle");

    private LRouteTableAction _lRouteTable;
    private LogixTableAction _logixTable;

    @Test
    public void testRouteElementComparator() {
        LRouteTableAction.RouteElement e1 = new LRouteTableAction.RouteElement("ISname1", "B", LRouteTableAction.SENSOR_TYPE);
        LRouteTableAction.RouteElement e2 = new LRouteTableAction.RouteElement("ISname2", "B", LRouteTableAction.SENSOR_TYPE);
        
        LRouteTableAction.RouteElementComparator rc = new LRouteTableAction.RouteElementComparator();
        
        assertTrue("e1 = e1", rc.compare(e1, e1) == 0);
        assertTrue("e2 > e1", rc.compare(e2, e1) > 0);
        assertTrue("e1 < e2", rc.compare(e1, e2) < 0);
        
    }
    
    @Test
    public void testCreate() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        _lRouteTable.actionPerformed(null);
        _lRouteTable.addPressed(null);
        _lRouteTable._userName.setText("TestLRoute");
        _lRouteTable._systemName.setText("T");
        _lRouteTable._alignList.get(5).setIncluded(true);
        for (int i = 0; i < 25; i++) {
            _lRouteTable._inputList.get(3 * i).setIncluded(true);
            _lRouteTable._outputList.get(3 * i + 1).setIncluded(true);
        }
        _lRouteTable.createPressed(null);
        java.util.List<String> l = InstanceManager.getDefault(jmri.LogixManager.class).getSystemNameList();
        assertEquals("Logix Count", 1, l.size());

        _lRouteTable.m.setValueAt(Bundle.getMessage("ButtonEdit"), 0,
                LRouteTableAction.LBeanTableDataModel.EDITCOL);
        // now close window
        // now close action window
        java.awt.event.WindowListener[] listeners = _lRouteTable._addFrame.getWindowListeners();
        for (int i = 0; i < listeners.length; i++) {
            _lRouteTable._addFrame.removeWindowListener(listeners[i]);
        }
        new JFrameOperator(_lRouteTable._addFrame).dispose();
    }

    @SuppressWarnings("unchecked")
    @Test
    @Ignore("Commented out in JUnit 3")
    public void testPrompt() {
        assertNotNull("LRouteTableAction is null!", _lRouteTable); // test has begun
        _lRouteTable.addPressed(null);
        _lRouteTable._userName.setText("TestLRoute2");
        _lRouteTable._systemName.setText("T2");
        for (int i = 0; i < 25; i++) {
            _lRouteTable._inputList.get(3 * i).setIncluded(true);
            _lRouteTable._outputList.get(3 * i).setIncluded(true);
        }
        _lRouteTable._alignList.get(5).setIncluded(true);

        // find the "Update" button in add/edit window and press it,
        // so the window is marked as dirty
        JButton updateButton = JButtonOperator.findJButton(_lRouteTable._addFrame, new NameComponentChooser("CreateButton"));
        Assert.assertNotNull("Could not find the updateButton", updateButton);
        new JButtonOperator(updateButton).doClick();

        // now close window
        new JFrameOperator(_lRouteTable._addFrame).dispose();

        // cancel the Reminder dialog
        JDialog dialog = JDialogOperator.findJDialog("Reminder", true, true);
        Assert.assertNotNull("Expected Reminder dialog is missing", dialog);
        new JDialogOperator(dialog).dispose();

    }

    @Before
    public void setUp() throws Exception {
        jmri.util.JUnitUtil.setUp();

        jmri.util.JUnitUtil.resetProfileManager();

        JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalSignalHeadManager();

        _lRouteTable = new LRouteTableAction("LRoute");
        assertNotNull("LRouteTableAction is null!", _lRouteTable);        // test has begun
        _logixTable = new LogixTableAction() {
            // skip dialog box if in edit mode, just assume OK pressed
//             @Override
//             boolean checkEditConditional() {
//                 if (inEditConditionalMode) {
//                     return true;
//                 }
//                 return false;
//             }
        };
        assertNotNull("LogixTableAction is null!", _logixTable);

//        Logix x1 = new jmri.implementation.DefaultLogix("RTXABC");
//        assertNotNull("Logix x1 is null!", x1);
//        InstanceManager.getDefault(jmri.LogixManager.class).register(x1);

        for (int i = 1; i < 20; i++) {
            Sensor s = InstanceManager.sensorManagerInstance().newSensor("IS" + i, "Sensor" + i);
            assertNotNull("Sensor is null!", s);
            Turnout t = InstanceManager.turnoutManagerInstance().newTurnout("IT" + i, "Turnout" + i);
            assertNotNull("Turnout is null!", t);
            Light l = InstanceManager.lightManagerInstance().newLight("IL" + (i), "Light" + i);
            assertNotNull(i + "th Light is null!", l);
//            Conditional c = InstanceManager.getDefault(jmri.ConditionalManager.class).createNewConditional(
//                    "Conditional" + i, "Conditional" + i);
//            assertNotNull(i + "th Conditional is null!", c);
            SignalHead sh = new jmri.implementation.VirtualSignalHead("SignalHead" + i);
            assertNotNull(i + "th SignalHead is null!", sh);
            InstanceManager.getDefault(jmri.SignalHeadManager.class).register(sh);
            Route r = new jmri.implementation.DefaultRoute("Route" + i);
            assertNotNull(i + "th Route is null!", r);
            InstanceManager.getDefault(jmri.RouteManager.class).register(r);
        }
    }

    @After
    public void tearDown() throws Exception {
        // now close action window
        if (_lRouteTable.f != null) {
            _lRouteTable.f.dispose();
        }
        JUnitUtil.tearDown();
    }
}
