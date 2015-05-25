// CarLoadsTest.java
package jmri.jmrit.operations.rollingstock.cars;

import java.io.File;
import java.util.List;
import java.util.Locale;
import jmri.jmrit.operations.locations.LocationManagerXml;
import jmri.jmrit.operations.rollingstock.engines.EngineManagerXml;
import jmri.jmrit.operations.routes.RouteManagerXml;
import jmri.jmrit.operations.setup.OperationsSetupXml;
import jmri.jmrit.operations.trains.TrainManagerXml;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import jmri.util.JUnitUtil;

/**
 * Tests for the Operations RollingStock Cars Loads class Last manually cross-checked
 * on 20090131
 *
 * Still to do: Everything
 *
 * @author	Bob Coleman Copyright (C) 2008, 2009
 * @version $Revision$
 */
public class CarLoadsTest extends TestCase {

    public void testCarLoads() {
        CarLoads cl = CarLoads.instance();
        List<String> names = cl.getNames("BoXcaR");

        Assert.assertEquals("Two default names", 2, names.size());
        Assert.assertTrue("Default load", cl.containsName("BoXcaR", "L"));
        Assert.assertTrue("Default empty", cl.containsName("BoXcaR", "E"));

        names = cl.getNames("bOxCaR");

        Assert.assertEquals("Two default names", 2, names.size());
        Assert.assertTrue("Default load", cl.containsName("bOxCaR", "L"));
        Assert.assertTrue("Default empty", cl.containsName("bOxCaR", "E"));

        cl.addName("BoXcaR", "New Boxcar Load");
        cl.addName("bOxCaR", "A boxcar load");
        cl.addName("bOxCaR", "B boxcar load");
        names = cl.getNames("BoXcaR");

        Assert.assertEquals("number of names", 3, names.size());
        Assert.assertTrue("Default load", cl.containsName("BoXcaR", "L"));
        Assert.assertTrue("Default empty", cl.containsName("BoXcaR", "E"));
        Assert.assertTrue("new load", cl.containsName("BoXcaR", "New Boxcar Load"));

        names = cl.getNames("bOxCaR");

        Assert.assertEquals("number of names", 4, names.size());
        Assert.assertTrue("Default load", cl.containsName("bOxCaR", "L"));
        Assert.assertTrue("Default empty", cl.containsName("bOxCaR", "E"));
        Assert.assertTrue("new load", cl.containsName("bOxCaR", "A boxcar load"));
        Assert.assertTrue("new load", cl.containsName("bOxCaR", "B boxcar load"));

        cl.replaceName("bOxCaR", "A boxcar load", "C boxcar load");

        names = cl.getNames("bOxCaR");

        Assert.assertEquals("number of names", 4, names.size());
        Assert.assertTrue("Default load", cl.containsName("bOxCaR", "L"));
        Assert.assertTrue("Default empty", cl.containsName("bOxCaR", "E"));
        Assert.assertFalse("new load", cl.containsName("bOxCaR", "A boxcar load"));
        Assert.assertTrue("new load", cl.containsName("bOxCaR", "B boxcar load"));
        Assert.assertTrue("new load", cl.containsName("bOxCaR", "C boxcar load"));

        cl.deleteName("bOxCaR", "B boxcar load");

        names = cl.getNames("bOxCaR");

        Assert.assertEquals("number of names", 3, names.size());
        Assert.assertTrue("Default load", cl.containsName("bOxCaR", "L"));
        Assert.assertTrue("Default empty", cl.containsName("bOxCaR", "E"));
        Assert.assertFalse("new load", cl.containsName("bOxCaR", "A boxcar load"));
        Assert.assertFalse("new load", cl.containsName("bOxCaR", "B boxcar load"));
        Assert.assertTrue("new load", cl.containsName("bOxCaR", "C boxcar load"));

        Assert.assertEquals("default empty", "E", cl.getDefaultEmptyName());
        Assert.assertEquals("default load", "L", cl.getDefaultLoadName());

        cl.setDefaultEmptyName("E<mpty>");
        cl.setDefaultLoadName("L<oad>");

        Assert.assertEquals("default empty", "E<mpty>", cl.getDefaultEmptyName());
        Assert.assertEquals("default load", "L<oad>", cl.getDefaultLoadName());

        names = cl.getNames("BOXCAR");

        Assert.assertEquals("number of names", 2, names.size());
        Assert.assertFalse("Default load", cl.containsName("BOXCAR", "L"));
        Assert.assertFalse("Default empty", cl.containsName("BOXCAR", "E"));
        Assert.assertTrue("Default load", cl.containsName("BOXCAR", "L<oad>"));
        Assert.assertTrue("Default empty", cl.containsName("BOXCAR", "E<mpty>"));

        // bOxCaR was created using old defaults
        Assert.assertTrue("Default load", cl.containsName("bOxCaR", "L"));
        Assert.assertTrue("Default empty", cl.containsName("bOxCaR", "E"));
        Assert.assertTrue("new load", cl.containsName("bOxCaR", "C boxcar load"));

        cl.setDefaultEmptyName("E");
        cl.setDefaultLoadName("L");

        Assert.assertEquals("default empty", "E", cl.getDefaultEmptyName());
        Assert.assertEquals("default load", "L", cl.getDefaultLoadName());
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

    public CarLoadsTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", CarLoadsTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(CarLoadsTest.class);
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
