// LogixTableActionTest.java

package jmri.jmrit.beantable;

import junit.framework.*;

import java.util.ResourceBundle;

import jmri.InstanceManager;
import jmri.managers.InternalSensorManager;
import jmri.managers.InternalTurnoutManager;
import jmri.Conditional;
import jmri.Light;
import jmri.Memory;
import jmri.Route;
import jmri.Sensor;
import jmri.SignalHead;
import jmri.Turnout;
import jmri.util.JUnitUtil;

/**
 * Tests for the jmri.jmrit.beantable.LogixTableAction class
 *
 * Note that the thread executing this test must yield to allow the event thread access
 * to create and dispose of window frames.  i.e. those calls that simulate an
 * actionPerformed event.
 * 
 * @author	Pete Cressman  Copyright 2009
 */
public class LogixTableActionTest extends jmri.util.SwingTestCase {

    String[] _varNames = new String[] {"is1", "Sensor2", "it1", "Turnout1", "C1", "C2",
                                       "il1", "Light2", "IMemory1", "FastClock", "Signal1", "Signal2",
                                       "Signal3", "Signal4", "Signal5", "Signal6", "Signal7", "Signal8", 
                                       "Signal9"};

    String[] _actNames = new String[] {"Turnout10", "Signal10",  "Signal11",  "Signal12", "Signal13", "Signal14", 
                                        "Route1", "Sensor10", "Sensor11", "Light10", "IMemory10", "IXTX", "IXTX", 
                                        "SoundFile", "ScriptFile", "Turnout4", "Turnout5", "Sensor5", "Sensor6",
                                        "Turnout11", "Turnout12", "FastClock", "FastClock", "FastClock",
                                        "IMemory11", "Light15", "Light16"};

    String[] _strData = new String[] {"", "",  "",  "", "", "", "", "", "10", "9:30", "IMemory5", "", "", 
                                       "SoundFile.wav", "jython/Sensor-sound.py", "13", "", "15", "",
                                        "22", "", "3:30", "", "", "IMemory19", "50", "100"};

	static final ResourceBundle rbx = ResourceBundle
			.getBundle("jmri.jmrit.beantable.LogixTableBundle");

    private static LogixTableAction _logixTable;

    public void testCreateLogix() throws Exception {
        _logixTable.actionPerformed(null);
        _logixTable.addPressed(null);
        _logixTable._addUserName.setText("TestLogix");    
        _logixTable._systemName.setText("TX");
        _logixTable.createPressed(null);
        _logixTable.donePressed(null);
        // note: _logixTable.m.EDITCOL = BeanTableDataModel.DELETECOL
        _logixTable.m.setValueAt((Object)rbx.getString("ButtonEdit"), 0, BeanTableDataModel.DELETECOL);
        _logixTable.newConditionalPressed(null);
        //_logixTable.helpPressed(null);
        _logixTable.conditionalUserName.setText("TestConditional");
        _logixTable.updateConditionalPressed(null);
        _logixTable.conditionalTableModel.setValueAt(null, 0, LogixTableAction.ConditionalTableModel.BUTTON_COLUMN);
        _logixTable.addVariablePressed(null);
        _logixTable.cancelEditVariablePressed();
        for (int i=0; i<Conditional.NUM_STATE_VARIABLE_TYPES; i++)
        {
            _logixTable.addVariablePressed(null);
            _logixTable._variableTypeBox.setSelectedIndex(i);
            _logixTable._variableNameField.setText(_varNames[i]);
            _logixTable._variableData1Field.setText(_strData[i]);
            _logixTable._variableData2Field.setText("11");
            _logixTable.updateVariablePressed();
        }
        _logixTable.addActionPressed(null);
        _logixTable.cancelEditActionPressed();
        for (int i=0; i<Conditional.NUM_ACTION_TYPES-3; i++)
        {
            _logixTable.addActionPressed(null);
            _logixTable._actionTypeBox.setSelectedIndex(i+1);
            _logixTable._actionOptionBox.setSelectedIndex(i%3);
            _logixTable._actionNameField.setText(_actNames[i]);
            _logixTable._actionTurnoutSetBox.setSelectedIndex(i%3);
            _logixTable._actionSensorSetBox.setSelectedIndex(i%3);
            _logixTable._actionLightSetBox.setSelectedIndex(i%3);
            _logixTable._actionLockSetBox.setSelectedIndex(i%3);
            _logixTable._actionSignalSetBox.setSelectedIndex(i%7);
            _logixTable._actionStringField.setText(_strData[i]);
            _logixTable.updateActionPressed();
        }
        _logixTable.updateConditionalPressed(null);
        Assert.assertEquals(_logixTable._curLogix.getNumConditionals(),1);

        _logixTable.donePressed(null);

        // note: _logixTable.m.EDITCOL = BeanTableDataModel.DELETECOL
        _logixTable.m.setValueAt((Object)rbx.getString("ButtonEdit"), 0, BeanTableDataModel.DELETECOL);
        _logixTable.conditionalTableModel.setValueAt(null, 0, LogixTableAction.ConditionalTableModel.BUTTON_COLUMN);
        _logixTable.conditionalUserName.setText("FirstConditional");
        assertEquals( 
                _logixTable._curConditional.getCopyOfStateVariables().size(), Conditional.NUM_STATE_VARIABLE_TYPES);
        assertEquals(_logixTable._curConditional.getCopyOfActions().size(), Conditional.NUM_ACTION_TYPES-3);
        //_logixTable.updateConditionalPressed(null);

        _logixTable.calculatePressed(null);
    }

    // from here down is testing infrastructure

    public LogixTableActionTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {LogixTableActionTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        Test suite = new TestSuite(LogixTableActionTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() throws Exception { 
        apps.tests.Log4JFixture.setUp(); 

        super.setUp();

        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();

        _logixTable = new LogixTableAction();
        assertNotNull("LogixTableAction is null!", _logixTable);        // test has begun
        _logixTable._suppressReminder = true;

        for (int i=1; i<20; i++)
        {
            Sensor s = InstanceManager.sensorManagerInstance().newSensor("IS"+i, "Sensor"+i);
            assertNotNull(i+"th Sensor is null!", s);
            Turnout t = InstanceManager.turnoutManagerInstance().newTurnout("IT"+i, "Turnout"+i);
            assertNotNull(i+"th Turnout is null!", t);
            Light l = InstanceManager.lightManagerInstance().newLight("IL"+(i), "Light"+i);
            assertNotNull(i+"th Light is null!", l);
            Conditional c = InstanceManager.conditionalManagerInstance().createNewConditional("C"+i, "Conditional"+i);
            assertNotNull(i+"th Conditional is null!", c);
            Memory m = InstanceManager.memoryManagerInstance().provideMemory("IMemory"+i);
            assertNotNull(i+"th Memory is null!", m);
            SignalHead sh = new jmri.implementation.VirtualSignalHead("Signal"+i);
            assertNotNull(i+"th SignalHead is null!", sh);
            InstanceManager.signalHeadManagerInstance().register(sh);
            Route r = new jmri.DefaultRoute("Route"+i);
            assertNotNull(i+"th Route is null!", r);
            InstanceManager.routeManagerInstance().register(r);
        }
    }
    protected void tearDown() throws Exception { 
        JUnitUtil.resetInstanceManager();
        super.tearDown();
        apps.tests.Log4JFixture.tearDown();
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LogixTableActionTest.class.getName());
}

