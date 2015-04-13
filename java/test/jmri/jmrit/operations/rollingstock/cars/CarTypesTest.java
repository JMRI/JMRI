// CarTypesTest.java
package jmri.jmrit.operations.rollingstock.cars;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import javax.swing.JComboBox;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManagerXml;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.engines.EngineManagerXml;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.routes.RouteManagerXml;
import jmri.jmrit.operations.setup.OperationsSetupXml;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManagerXml;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import jmri.util.JUnitUtil;
import org.jdom2.JDOMException;

/**
 * Tests for the Operationsations CarTypes class Last manually cross-checked
 * on 20090131
 *
 * Derived from prevous "OperationsCarTest" to include only the tests related to * CarTypes.
 *
 * @author	Bob Coleman Copyright (C) 2008, 2009
 * @version $Revision$
 */
public class CarTypesTest extends TestCase {

    public void testCarTypes() {
        CarTypes ct1 = CarTypes.instance();
        ct1.getNames();	//Load predefined car types

        Assert.assertTrue("Car Types Predefined Boxcar", ct1.containsName("Boxcar"));
        Assert.assertTrue("Car Types Predefined Caboose", ct1.containsName("Caboose"));

        ct1.addName("Type New1");
        Assert.assertTrue("Car Types Add New1", ct1.containsName("Type New1"));
        Assert.assertFalse("Car Types Never Added New2", ct1.containsName("Type New2"));
        ct1.addName("Type New3");
        Assert.assertTrue("Car Types Still Has New1", ct1.containsName("Type New1"));
        Assert.assertTrue("Car Types Add New3", ct1.containsName("Type New3"));
        ct1.replaceName("Type New3", "Type New4");
        Assert.assertFalse("Car Types replace New3", ct1.containsName("Type New3"));
        Assert.assertTrue("Car Types replace New3 with New4", ct1.containsName("Type New4"));
        String[] types = ct1.getNames();
        Assert.assertEquals("First type name", "Type New4", types[0]);
        Assert.assertEquals("2nd type name", "Type New1", types[1]);
        JComboBox<?> box = ct1.getComboBox();
        Assert.assertEquals("First comboBox type name", "Type New4", box.getItemAt(0));
        Assert.assertEquals("2nd comboBox type name", "Type New1", box.getItemAt(1));
        ct1.deleteName("Type New4");
        Assert.assertFalse("Car Types Delete New4", ct1.containsName("Type New4"));
        ct1.deleteName("Type New1");
        Assert.assertFalse("Car Types Delete New1", ct1.containsName("Type New1"));
    }
    // from here down is testing infrastructure
    // Ensure minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initDebugThrottleManager();
        JUnitUtil.initIdTagManager();
        jmri.InstanceManager.setShutDownManager( new
                 jmri.managers.DefaultShutDownManager() {
                    @Override
                    public void register(jmri.ShutDownTask s){
                       // do nothing with registered shutdown tasks for testing.
                    }
                 });
        // set the locale to US English
        Locale.setDefault(Locale.ENGLISH);

        // Repoint OperationsSetupXml to JUnitTest subdirectory
        OperationsSetupXml.setOperationsDirectoryName("operations" + File.separator + "JUnitTest");
        // Change file names to ...Test.xml
        OperationsSetupXml.instance().setOperationsFileName("OperationsJUnitTest.xml");
        RouteManagerXml.instance().setOperationsFileName("OperationsJUnitTestRouteRoster.xml");
        EngineManagerXml.instance().setOperationsFileName("OperationsJUnitTestEngineRoster.xml");
        CarManagerXml.instance().setOperationsFileName("OperationsJUnitTestCarRoster.xml");
        LocationManagerXml.instance().setOperationsFileName("OperationsJUnitTestLocationRoster.xml");
        TrainManagerXml.instance().setOperationsFileName("OperationsJUnitTestTrainRoster.xml");

        // Need to clear out CarManager global variables
        CarManager manager = CarManager.instance();
        CarColors.instance().dispose();
        CarLengths.instance().dispose();
        CarLoads.instance().dispose();
        CarRoads.instance().dispose();
        CarTypes.instance().dispose();
        manager.dispose();
    }

    public CarTypesTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", CarTypesTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(CarTypesTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void tearDown() throws Exception {
       JUnitUtil.resetInstanceManager();
       apps.tests.Log4JFixture.tearDown();
       super.tearDown();
    }
}
