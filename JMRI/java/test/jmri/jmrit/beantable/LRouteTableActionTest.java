package jmri.jmrit.beantable;

import java.awt.GraphicsEnvironment;
import java.util.ResourceBundle;
import jmri.InstanceManager;
import jmri.Light;
import jmri.Route;
import jmri.Sensor;
import jmri.SignalHead;
import jmri.Turnout;
import jmri.util.JUnitUtil;
import junit.extensions.jfcunit.TestHelper;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrit.beantable.LRouteTableAction class
 *
 * @author	Pete Cressman Copyright 2009
 */
public class LRouteTableActionTest extends jmri.util.SwingTestCase //TestCase // jmri.util.SwingTestCase 
{

    static final ResourceBundle rbx = ResourceBundle
            .getBundle("jmri.jmrit.beantable.LRouteTableBundle");

    private LRouteTableAction _lRouteTable;
    private LogixTableAction _logixTable;

    public void testRouteElementComparator() {
        LRouteTableAction.RouteElement e1 = new LRouteTableAction.RouteElement("ISname1", "B", 0);
        LRouteTableAction.RouteElement e2 = new LRouteTableAction.RouteElement("ISname2", "B", 0);
        
        LRouteTableAction.RouteElementComparator rc = new LRouteTableAction.RouteElementComparator();
        
        assertTrue("e1 = e1", rc.compare(e1, e1) == 0);
        assertTrue("e2 > e1", rc.compare(e2, e1) > 0);
        assertTrue("e1 < e2", rc.compare(e1, e2) < 0);
        
    }
    
    public void testCreate() {
        if (GraphicsEnvironment.isHeadless()) {
            return; // can't Assume in TestCase
        }
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
        TestHelper.disposeWindow(_lRouteTable._addFrame, this);
    }
    /*
     @SuppressWarnings("unchecked")
     public void testPrompt() {
     assertNotNull("LRouteTableAction is null!", _lRouteTable);        // test has begun
     _lRouteTable.addPressed(null);
     _lRouteTable._userName.setText("TestLRoute2");    
     _lRouteTable._systemName.setText("T2");
     for (int i=0; i<25; i++)
     {
     _lRouteTable._inputList.get(3*i).setIncluded(true);
     _lRouteTable._outputList.get(3*i).setIncluded(true);
     }
     _lRouteTable._alignList.get(5).setIncluded(true);

     // find the "Update" button in add/edit window and press it,
     // so the window is marked as dirty
     NamedComponentFinder finder = new NamedComponentFinder(JComponent.class, "CreateButton" );
     JButton updateButton = ( JButton ) finder.find( _lRouteTable._addFrame, 0);
     Assert.assertNotNull( "Could not find the updateButton", updateButton );
     //getHelper().enterClickAndLeave( new MouseEventData( this, updateButton ) );
        
     // now close window
     TestHelper.disposeWindow(_lRouteTable._addFrame,this);
        
     // cancel the Reminder dialog
     DialogFinder dFinder = new DialogFinder( "Reminder" );
     java.util.List<JDialog> showingDialogs = dFinder.findAll();
     //Assert.assertEquals( "Number of dialogs showing is wrong", 1, showingDialogs.size( ) );
     JDialog dialog = showingDialogs.get( 0 );
     Assert.assertEquals( "Wrong dialog showing up", "Reminder", dialog.getTitle( ) );
     getHelper().disposeWindow( dialog, this );
        
     }
     */
    // from here down is testing infrastructure

    public LRouteTableActionTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", LRouteTableActionTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        Test suite = new TestSuite(LRouteTableActionTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        super.setUp();

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

    @Override
    protected void tearDown() throws Exception {
        // now close action window
        TestHelper.disposeWindow(_lRouteTable.f, this);
        super.tearDown();
        JUnitUtil.resetInstanceManager();
    }
}
