
// LRouteTableActionTest.java

package jmri.jmrit.beantable;

import junit.framework.*;

import jmri.util.*;

import java.util.ResourceBundle;

import jmri.InstanceManager;
import jmri.managers.InternalSensorManager;
import jmri.managers.InternalTurnoutManager;
import jmri.Conditional;
import jmri.Light;
import jmri.Route;
import jmri.Sensor;
import jmri.SignalHead;
import jmri.Turnout;


/**
 * Tests for the jmri.jmrit.beantable.LRouteTableAction class
 * @author	Pete Cressman  Copyright 2009
 */
public class LRouteTableActionTest extends SwingTestCase {

	static final ResourceBundle rbx = ResourceBundle
			.getBundle("jmri.jmrit.beantable.LRouteTableBundle");

    private LRouteTableAction _lRouteTable;

    public void testCreate() {
        assertNotNull("LRouteTableAction is null!", _lRouteTable);        // test has begun
        _lRouteTable.addPressed(null);
        Thread.currentThread().yield();     // allow event thread to draw window 
        _lRouteTable._userName.setText("TestLRoute");    
        _lRouteTable._systemName.setText("T");
        for (int i=0; i<20; i++)
        {
            _lRouteTable._inputList.get(3*i).setIncluded(true);
            _lRouteTable._outputList.get(3*i).setIncluded(true);
        }
        _lRouteTable._alignList.get(5).setIncluded(true);
        _lRouteTable.createPressed(null);
        Thread.currentThread().yield();     // allow event thread to draw window 

        _lRouteTable.m.setValueAt((Object)rbx.getString("ButtonEdit"), 0, 
                                  LRouteTableAction.LBeanTableDataModel.EDITCOL);
    }

    // from here down is testing infrastructure

    public LRouteTableActionTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {LRouteTableActionTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(LRouteTableActionTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() { 
        apps.tests.Log4JFixture.setUp(); 
        _lRouteTable = new LRouteTableAction("LRoute");
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        
        for (int i=1; i<20; i++)
        {
            Sensor s = InstanceManager.sensorManagerInstance().newSensor("IS"+i, "Sensor"+i);
            assertNotNull("Sensor is null!", s);
            Turnout t = InstanceManager.turnoutManagerInstance().newTurnout("IT"+i, "Turnout"+i);
            assertNotNull("Turnout is null!", t);
            Light l = InstanceManager.lightManagerInstance().newLight("IL"+(i), "Light"+i);
            assertNotNull(i+"th Light is null!", l);
            Conditional c = InstanceManager.conditionalManagerInstance().createNewConditional(
                                                        "Conditional"+i, "Conditional"+i);
            assertNotNull(i+"th Conditional is null!", c);
            SignalHead sh = new jmri.implementation.VirtualSignalHead("Signal"+i);
            assertNotNull(i+"th SignalHead is null!", sh);
            InstanceManager.signalHeadManagerInstance().register(sh);
            Route r = new jmri.DefaultRoute("Route"+i);
            assertNotNull(i+"th Route is null!", r);
            InstanceManager.routeManagerInstance().register(r);
        }
    }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LRouteTableActionTest.class.getName());
}

