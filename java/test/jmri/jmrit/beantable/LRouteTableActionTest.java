package jmri.jmrit.beantable;

import java.awt.event.WindowListener;

import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JDialog;

import jmri.InstanceManager;
import jmri.Light;
import jmri.Route;
import jmri.Sensor;
import jmri.SignalHead;
import jmri.Turnout;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;

import org.netbeans.jemmy.operators.*;
import org.netbeans.jemmy.util.NameComponentChooser;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the jmri.jmrit.beantable.LRouteTableAction class
 *
 * @author Pete Cressman Copyright 2009
 */
public class LRouteTableActionTest extends AbstractTableActionBase<jmri.Logix> {

    static final ResourceBundle rbx = ResourceBundle
            .getBundle("jmri.jmrit.beantable.LRouteTableBundle");

    private LRouteTableAction _lRouteTable;
    private LogixTableAction _logixTable;

    @Test
    public void testRouteElementComparator() {
        LRouteTableAction.RouteElement e1 = new LRouteTableAction.RouteElement("ISname1", "B", LRouteTableAction.SENSOR_TYPE);
        LRouteTableAction.RouteElement e2 = new LRouteTableAction.RouteElement("ISname2", "B", LRouteTableAction.SENSOR_TYPE);

        LRouteTableAction.RouteElementComparator rc = new LRouteTableAction.RouteElementComparator();

        assertTrue( rc.compare(e1, e1) == 0, "e1 = e1");
        assertTrue( rc.compare(e2, e1) > 0, "e2 > e1");
        assertTrue( rc.compare(e1, e2) < 0, "e1 < e2");

    }

    @Test
    @jmri.util.junit.annotations.DisabledIfHeadless
    public void testCreate() {

        ThreadingUtil.runOnGUI(() -> _lRouteTable.actionPerformed(null)); // show table
        // JFrameOperator jfo = new JFrameOperator(Bundle.getMessage("TitleLRouteTable"));
        // Assertions.assertNotNull(jfo);

        ThreadingUtil.runOnGUI(() -> _lRouteTable.addPressed(null)); // show add window
        JFrameOperator jfoAdd = new JFrameOperator(rbx.getString("LRouteAddTitle"));
        Assertions.assertNotNull(jfoAdd);

        JTextFieldOperator jto = new JTextFieldOperator(jfoAdd, new NameComponentChooser("hwAddressTextField"));
        jto.typeText("T");

        jto = new JTextFieldOperator(jfoAdd, new NameComponentChooser("userNameTextField"));
        jto.typeText("TestLRoute");

        _lRouteTable._alignList.get(5).setIncluded(true);
        for (int i = 0; i < 25; i++) {
            _lRouteTable._inputList.get(3 * i).setIncluded(true);
            _lRouteTable._outputList.get(3 * i + 1).setIncluded(true);
        }
        _lRouteTable.createPressed(null);
        assertEquals( 1, InstanceManager.getDefault(jmri.LogixManager.class).getNamedBeanSet().size(), "Logix Count");

        _lRouteTable.m.setValueAt(Bundle.getMessage("ButtonEdit"), 0,
                LRouteTableAction.LBeanTableDataModel.EDITCOL);

        // now close window
        JFrameOperator jfoEdit = new JFrameOperator(rbx.getString("LRouteEditTitle"));
        assertNotNull(jfoEdit);

        JButtonOperator jbo = new JButtonOperator(jfoEdit, Bundle.getMessage("ButtonCancel"));
        jbo.doClick();
        jfoEdit.waitClosed();

        // now close action window
        java.awt.event.WindowListener[] listeners = _lRouteTable._addFrame.getWindowListeners();
        for (WindowListener listener : listeners) {
            _lRouteTable._addFrame.removeWindowListener(listener);
        }
        JUnitUtil.dispose(jfoAdd.getWindow());
        jfoAdd.waitClosed();
    }

    @Test
    @Disabled("Commented out in JUnit 3")
    @jmri.util.junit.annotations.DisabledIfHeadless
    public void testPrompt() {
        assertNotNull( _lRouteTable, "LRouteTableAction is null!");
        ThreadingUtil.runOnGUI(() -> _lRouteTable.addPressed(null)); // show add window

        JFrameOperator jfoAdd = new JFrameOperator(rbx.getString("LRouteAddTitle"));
        Assertions.assertNotNull(jfoAdd);

        JTextFieldOperator jto = new JTextFieldOperator(jfoAdd, new NameComponentChooser("hwAddressTextField"));
        jto.typeText("T2");

        jto = new JTextFieldOperator(jfoAdd, new NameComponentChooser("userNameTextField"));
        jto.typeText("TestLRoute2");

        for (int i = 0; i < 25; i++) {
            _lRouteTable._inputList.get(3 * i).setIncluded(true);
            _lRouteTable._outputList.get(3 * i).setIncluded(true);
        }
        _lRouteTable._alignList.get(5).setIncluded(true);

        // find the "Update" button in add/edit window and press it,
        // so the window is marked as dirty
        JButton createButton = JButtonOperator.findJButton(_lRouteTable._addFrame, new NameComponentChooser("CreateButton"));
        assertNotNull( createButton, "Could not find the updateButton" );

        new JButtonOperator(createButton).doClick();

        JUnitUtil.dispose(jfoAdd.getWindow());
        jfoAdd.waitClosed();

        // cancel the Reminder dialog
        JDialog dialog = JDialogOperator.findJDialog("Reminder", true, true);
        assertNotNull( dialog, "Expected Reminder dialog is missing" );
        new JDialogOperator(dialog).dispose();

    }

    @Override
    public String getTableFrameName() {
        return Bundle.getMessage("TitleLRouteTable");
    }

    @Override
    public String getAddFrameName() {
        return rbx.getString("LRouteAddTitle");
    }

    @Override
    @Test
    public void testIncludeAddButton() {
        assertTrue( a.includeAddButton());
    }

    @Override
    @Test
    public void testGetClassDescription() {
        assertEquals( "LRoute Table", a.getClassDescription());
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();

        JUnitUtil.resetProfileManager();

        JUnitUtil.resetInstanceManager();
        JUnitUtil.initDefaultUserMessagePreferences();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalSignalHeadManager();

        _lRouteTable = new LRouteTableAction("LRoute");
        a = _lRouteTable;
        assertNotNull( _lRouteTable, "LRouteTableAction is null!");
        _logixTable = new LogixTableAction() {
        // skip dialog box if in edit mode, just assume OK pressed
        //     @Override
        //     boolean checkEditConditional() {
        //         if (inEditConditionalMode) {
        //             return true;
        //         }
        //         return false;
        //     }
        };
        assertNotNull( _logixTable, "LogixTableAction is null!");

        //  Logix x1 = new jmri.implementation.DefaultLogix("RTXABC");
        //  assertNotNull("Logix x1 is null!", x1);
        //  InstanceManager.getDefault(jmri.LogixManager.class).register(x1);

        for (int i = 1; i < 20; i++) {
            Sensor s = InstanceManager.sensorManagerInstance().newSensor("IS" + i, "Sensor" + i);
            assertNotNull( s, "Sensor is null!");
            Turnout t = InstanceManager.turnoutManagerInstance().newTurnout("IT" + i, "Turnout" + i);
            assertNotNull(t, "Turnout is null!");
            Light l = InstanceManager.lightManagerInstance().newLight("IL" + (i), "Light" + i);
            assertNotNull(l, i + "th Light is null!");
//            Conditional c = InstanceManager.getDefault(jmri.ConditionalManager.class).createNewConditional(
//                    "Conditional" + i, "Conditional" + i);
//            assertNotNull(i + "th Conditional is null!", c);
            SignalHead sh = new jmri.implementation.VirtualSignalHead("SignalHead" + i);
            assertNotNull( sh, i + "th SignalHead is null!");
            InstanceManager.getDefault(jmri.SignalHeadManager.class).register(sh);
            Route r = new jmri.implementation.DefaultRoute("Route" + i);
            assertNotNull( r, i + "th Route is null!" );
            InstanceManager.getDefault(jmri.RouteManager.class).register(r);
        }

        helpTarget = "package.jmri.jmrit.beantable.LRouteTable";
    }

    @AfterEach
    @Override
    public void tearDown() {
        // now close action window
        if (_lRouteTable.f != null) {
            JUnitUtil.dispose(_lRouteTable.f);
            _lRouteTable.dispose();
        }
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
