// EngineLengthsTest.java
package jmri.jmrit.operations.rollingstock.engines;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManagerXml;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.cars.CarManagerXml;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteManagerXml;
import jmri.jmrit.operations.setup.OperationsSetupXml;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManagerXml;
import jmri.util.JUnitUtil;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jdom2.JDOMException;

/**
 * Tests for the Operations RollingStock Engine class Last manually
 * cross-checked on 20090131
 *
 * Still to do: Engine: Destination Engine: Verify everything else EngineTypes:
 * get/set Names lists EngineModels: get/set Names lists EngineLengths:
 * Everything Consist: Everything Import: Everything EngineManager: Engine
 * register/deregister EngineManager: Consists
 *
 * @author	Bob Coleman Copyright (C) 2008, 2009
 * @version $Revision$
 */
public class EngineLengthsTest extends TestCase {

    // test EngineLengths Class
    // test EngineLengths creation
    public void testEngineLengthsCreate() {
        EngineLengths el1 = new EngineLengths();
        Assert.assertNotNull("exists", el1);
    }

    // test EngineLengths public constants
    public void testEngineLengthsConstants() {
        EngineLengths el1 = new EngineLengths();

        Assert.assertNotNull("exists", el1);
        Assert.assertEquals("EngineTypes ENGINELENGTHS_CHANGED_PROPERTY", "EngineLengths", EngineLengths.ENGINELENGTHS_CHANGED_PROPERTY);
    }

    // test EngineLengths Names
    public void testEngineLengthsNames() {
        EngineLengths el1 = new EngineLengths();

        Assert.assertEquals("EngineLengths Null Names", false, el1.containsName("TESTENGINELENGTHNAME1"));

        el1.addName("TESTENGINELENGTHNAME1");
        Assert.assertEquals("EngineLengths add Name1", true, el1.containsName("TESTENGINELENGTHNAME1"));

        el1.addName("TESTENGINELENGTHNAME2");
        Assert.assertEquals("EngineLengths add Name2", true, el1.containsName("TESTENGINELENGTHNAME2"));

        el1.deleteName("TESTENGINELENGTHNAME2");
        Assert.assertEquals("EngineLengths delete Name2", false, el1.containsName("TESTENGINELENGTHNAME2"));

        el1.deleteName("TESTENGINELENGTHNAME1");
        Assert.assertEquals("EngineLengths delete Name1", false, el1.containsName("TESTENGINELENGTHNAME1"));
    }

    // TODO: Add test for import
    // from here down is testing infrastructure
    // Ensure minimal setup for log4J
    @Override
    protected void setUp() throws Exception{
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
        CarManagerXml.instance().setOperationsFileName("OperationsJUnitTestCarRoster.xml");
        EngineManagerXml.instance().setOperationsFileName("OperationsJUnitTestEngineRoster.xml");
        LocationManagerXml.instance().setOperationsFileName("OperationsJUnitTestLocationRoster.xml");
        TrainManagerXml.instance().setOperationsFileName("OperationsJUnitTestTrainRoster.xml");

        // Need to clear out EngineManager global variables
        EngineManager manager = EngineManager.instance();
        List<String> tempconsistList = manager.getConsistNameList();
        for (int i = 0; i < tempconsistList.size(); i++) {
            String consistId = tempconsistList.get(i);
            manager.deleteConsist(consistId);
        }
        EngineModels.instance().dispose();
        EngineLengths.instance().dispose();
        manager.dispose();
    }

    public EngineLengthsTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", EngineLengthsTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(EngineLengthsTest.class);
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
