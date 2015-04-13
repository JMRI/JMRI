// CarRoadsTest.java
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
 * Tests for the Operations RollingStock Cars Roads class Last manually cross-checked
 * on 20090131
 *
 * Still to do: Everything
 *
 * @author	Bob Coleman Copyright (C) 2008, 2009
 * @version $Revision$
 */
public class CarRoadsTest extends TestCase {

    public void testCarRoads() {
        CarRoads cr1 = CarRoads.instance();
        cr1.getNames();	//load predefined roads

        Assert.assertTrue("Car Roads Predefined AA", cr1.containsName("AA"));
        Assert.assertTrue("Car Roads Predefined CP", cr1.containsName("CP"));
        Assert.assertTrue("Car Roads Predefined CN", cr1.containsName("CN"));
        Assert.assertTrue("Car Roads Predefined UP", cr1.containsName("UP"));

        cr1.addName("Road New1");
        Assert.assertTrue("Car Roads Add New1", cr1.containsName("Road New1"));
        Assert.assertFalse("Car Roads Never Added New2", cr1.containsName("Road New2"));
        cr1.addName("Road New3");
        Assert.assertTrue("Car Roads Still Has New1", cr1.containsName("Road New1"));
        Assert.assertTrue("Car Roads Add New3", cr1.containsName("Road New3"));
        cr1.replaceName("Road New3", "Road New4");
        Assert.assertFalse("Car Roads replace New3", cr1.containsName("Road New3"));
        Assert.assertTrue("Car Roads replace New3 with New4", cr1.containsName("Road New4"));
        String[] roads = cr1.getNames();
        Assert.assertEquals("First road name", "Road New4", roads[0]);
        Assert.assertEquals("2nd road name", "Road New1", roads[1]);
        JComboBox<?> box = cr1.getComboBox();
        Assert.assertEquals("First comboBox road name", "Road New4", box.getItemAt(0));
        Assert.assertEquals("2nd comboBox road name", "Road New1", box.getItemAt(1));
        cr1.deleteName("Road New4");
        Assert.assertFalse("Car Roads Delete New4", cr1.containsName("Road New4"));
        cr1.deleteName("Road New1");
        Assert.assertFalse("Car Roads Delete New1", cr1.containsName("Road New1"));
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

    public CarRoadsTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", CarRoadsTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(CarRoadsTest.class);
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
