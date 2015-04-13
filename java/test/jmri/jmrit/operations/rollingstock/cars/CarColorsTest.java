// CarColorsTest.java
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
 * Tests for the Operations RollingStock CarColors class Last manually cross-checked
 * on 20090131
 *
 * Still to do: Everything
 *
 * @author	Bob Coleman Copyright (C) 2008, 2009
 * @version $Revision$
 */
public class CarColorsTest extends TestCase {

    public void testCarColors() {
        CarColors cc1 = CarColors.instance();
        cc1.getNames();	// load predefined colors

        Assert.assertTrue("Car Color Predefined Red", cc1.containsName("Red"));
        Assert.assertTrue("Car Color Predefined Blue", cc1.containsName("Blue"));

        cc1.addName("BoxCar Red");
        Assert.assertTrue("Car Color Add", cc1.containsName("BoxCar Red"));
        Assert.assertFalse("Car Color Never Added Dirty Blue", cc1.containsName("Dirty Blue"));
        cc1.addName("Ugly Brown");
        Assert.assertTrue("Car Color Still Has BoxCar Red", cc1.containsName("BoxCar Red"));
        Assert.assertTrue("Car Color Add Ugly Brown", cc1.containsName("Ugly Brown"));
        String[] colors = cc1.getNames();
        Assert.assertEquals("First color name", "Ugly Brown", colors[0]);
        Assert.assertEquals("2nd color name", "BoxCar Red", colors[1]);
        JComboBox<?> box = cc1.getComboBox();
        Assert.assertEquals("First comboBox color name", "Ugly Brown", box.getItemAt(0));
        Assert.assertEquals("2nd comboBox color name", "BoxCar Red", box.getItemAt(1));
        cc1.deleteName("Ugly Brown");
        Assert.assertFalse("Car Color Delete Ugly Brown", cc1.containsName("Ugly Brown"));
        cc1.deleteName("BoxCar Red");
        Assert.assertFalse("Car Color Delete BoxCar Red", cc1.containsName("BoxCar Red"));
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

    public CarColorsTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", CarColorsTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(CarColorsTest.class);
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
