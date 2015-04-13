// CarsTest.java
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
 * Tests for the Operations RollingStock Cars class Last manually cross-checked
 * on 20090131
 *
 * Still to do: Everything
 *
 * @author	Bob Coleman Copyright (C) 2008, 2009
 * @version $Revision$
 */
public class CarsTest extends TestCase {

    // test creation
    public void testCreate() {
        Car c1 = new Car("TESTROAD", "TESTNUMBER1");
        c1.setTypeName("TESTTYPE");
        c1.setLength("TESTLENGTH");
        c1.setColor("TESTCOLOR");
        c1.setHazardous(true);
        c1.setFred(true);
        c1.setCaboose(true);
        c1.setWeight("TESTWEIGHT");
        c1.setBuilt("TESTBUILT");
        c1.setOwner("TESTOWNER");
        c1.setComment("TESTCOMMENT");
        c1.setMoves(5);

        Assert.assertEquals("Car Road", "TESTROAD", c1.getRoadName());
        Assert.assertEquals("Car Number", "TESTNUMBER1", c1.getNumber());
        Assert.assertEquals("Car ID", "TESTROAD" + "TESTNUMBER1", c1.getId());
        Assert.assertEquals("Car Type", "TESTTYPE", c1.getTypeName());
        Assert.assertEquals("Car Length", "TESTLENGTH", c1.getLength());
        Assert.assertEquals("Car Color", "TESTCOLOR", c1.getColor());
        Assert.assertTrue("Car Hazardous", c1.isHazardous());
        Assert.assertTrue("Car Fred", c1.hasFred());
        Assert.assertTrue("Car Caboose", c1.isCaboose());
        Assert.assertEquals("Car Weight", "TESTWEIGHT", c1.getWeight());
        Assert.assertEquals("Car Built", "TESTBUILT", c1.getBuilt());
        Assert.assertEquals("Car Owner", "TESTOWNER", c1.getOwner());
        Assert.assertEquals("Car Comment", "TESTCOMMENT", c1.getComment());
        Assert.assertEquals("Car Moves", 5, c1.getMoves());
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

    public CarsTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", CarsTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(CarsTest.class);
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
