// EngineTypesTest.java
package jmri.jmrit.operations.rollingstock.engines;

import java.io.File;
import java.util.List;
import java.util.Locale;
import jmri.jmrit.operations.locations.LocationManagerXml;
import jmri.jmrit.operations.rollingstock.cars.CarManagerXml;
import jmri.jmrit.operations.routes.RouteManagerXml;
import jmri.jmrit.operations.setup.OperationsSetupXml;
import jmri.jmrit.operations.trains.TrainManagerXml;
import jmri.util.JUnitUtil;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

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
public class EngineTypesTest extends TestCase {

    // test EngineTypes Class
    // test EngineTypes creation
    public void testEngineTypesCreate() {
        EngineTypes et1 = new EngineTypes();
        Assert.assertNotNull("exists", et1);
    }

    // test EngineTypes public constants
    public void testEngineTypesConstants() {
        EngineTypes et1 = new EngineTypes();

        Assert.assertNotNull("exists", et1);
        Assert.assertEquals("EngineTypes ENGINETYPES_LENGTH_CHANGED_PROPERTY", "EngineTypesLength", EngineTypes.ENGINETYPES_CHANGED_PROPERTY);
        Assert.assertEquals("EngineTypes ENGINETYPES_NAME_CHANGED_PROPERTY", "EngineTypesName", EngineTypes.ENGINETYPES_NAME_CHANGED_PROPERTY);
    }

    // test EngineTypes Names
    public void testEngineTypesNames() {
        EngineTypes et1 = new EngineTypes();

        Assert.assertEquals("EngineTypes Null Names", false, et1.containsName("TESTENGINETYPENAME1"));

        et1.addName("TESTENGINETYPENAME1");
        Assert.assertEquals("EngineTypes add Name1", true, et1.containsName("TESTENGINETYPENAME1"));

        et1.addName("TESTENGINETYPENAME2");
        Assert.assertEquals("EngineTypes add Name2", true, et1.containsName("TESTENGINETYPENAME2"));

        et1.replaceName("TESTENGINETYPENAME1", "TESTENGINETYPENAME3");
        Assert.assertEquals("EngineTypes replace Name1", false, et1.containsName("TESTENGINETYPENAME1"));
        Assert.assertEquals("EngineTypes replace Name3", true, et1.containsName("TESTENGINETYPENAME3"));

        et1.deleteName("TESTENGINETYPENAME2");
        Assert.assertEquals("EngineTypes delete Name2", false, et1.containsName("TESTENGINETYPENAME2"));

        et1.deleteName("TESTENGINETYPENAME3");
        Assert.assertEquals("EngineTypes delete Name3", false, et1.containsName("TESTENGINETYPENAME3"));
    }

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

    public EngineTypesTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", EngineTypesTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(EngineTypesTest.class);
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
