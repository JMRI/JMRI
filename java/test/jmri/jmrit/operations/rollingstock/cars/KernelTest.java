// KernelTest.java
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
 * Tests for the Operations RollingStock Cars Kernel class Last manually cross-checked
 * on 20090131
 *
 * Still to do: Everything
 *
 * @author	Bob Coleman Copyright (C) 2008, 2009
 * @version $Revision$
 */
public class KernelTest extends TestCase {

    public void testKernel() {
        Kernel k1 = new Kernel("TESTKERNEL");
        Assert.assertEquals("Kernel Name", "TESTKERNEL", k1.getName());

        Car c1 = new Car("TESTCARROAD", "TESTCARNUMBER1");
        c1.setLength("40");
        c1.setWeight("1000");
        c1.setWeightTons("10");
        c1.setLoadName("L");
        Car c2 = new Car("TESTCARROAD", "TESTCARNUMBER2");
        c2.setLength("60");
        c2.setWeight("2000");
        c2.setWeightTons("20");
        c2.setLoadName("L");
        Car c3 = new Car("TESTCARROAD", "TESTCARNUMBER3");
        c3.setLength("50");
        c3.setWeight("1500");
        c3.setWeightTons("15");
        c3.setLoadName("E");

        Assert.assertEquals("Kernel Initial Length", 0, k1.getTotalLength());
        Assert.assertEquals("Kernel Initial Weight Tons", 0, k1.getAdjustedWeightTons());

        k1.add(c1);
        Assert.assertEquals("Kernel Car 1 Length", 40 + Car.COUPLER, k1.getTotalLength());
        Assert.assertEquals("Kernel Car 1 Weight Tons", 10, k1.getAdjustedWeightTons());

        k1.add(c2);
        Assert.assertEquals("Kernel Car 2 Length", 40 + Car.COUPLER + 60 + Car.COUPLER, k1.getTotalLength());
        Assert.assertEquals("Kernel Car 2 Weight Tons", 30, k1.getAdjustedWeightTons());

        k1.add(c3);
        Assert.assertEquals("Kernel Car 3 Length", 40 + Car.COUPLER + 60 + Car.COUPLER + 50 + Car.COUPLER, k1.getTotalLength());
        // car 3 is empty, so only 5 tons, 15/3
        Assert.assertEquals("Kernel Car 3 Weight Tons", 35, k1.getAdjustedWeightTons());

        k1.setLead(c2);
        Assert.assertTrue("Kernel Lead Car 1", k1.isLead(c2));
        Assert.assertFalse("Kernel Lead Car 2", k1.isLead(c1));
        Assert.assertFalse("Kernel Lead Car 3", k1.isLead(c3));

        k1.delete(c2);
        Assert.assertEquals("Kernel Car Delete 2 Length", 40 + Car.COUPLER + 50 + Car.COUPLER, k1.getTotalLength());
        Assert.assertEquals("Kernel Car Delete 2 Weight Tons", 15, k1.getAdjustedWeightTons());

        k1.delete(c1);
        Assert.assertEquals("Kernel Car Delete 1 Length", 50 + Car.COUPLER, k1.getTotalLength());
        Assert.assertEquals("Kernel Car Delete 1 Weight Tons", 5, k1.getAdjustedWeightTons());

        k1.delete(c3);
        Assert.assertEquals("Kernel Car Delete 3 Length", 0, k1.getTotalLength());
        Assert.assertEquals("Kernel Car Delete 3 Weight Tons", 0, k1.getAdjustedWeightTons());

    }

    public void testCarKernel() {
        Kernel kold = new Kernel("TESTKERNELOLD");
        Assert.assertEquals("Kernel Name old", "TESTKERNELOLD", kold.getName());

        Kernel knew = new Kernel("TESTKERNELNEW");
        Assert.assertEquals("Kernel Name new", "TESTKERNELNEW", knew.getName());

        Car c1 = new Car("TESTCARROAD", "TESTCARNUMBER1");
        c1.setLength("40");
        c1.setWeight("1000");
        Car c2 = new Car("TESTCARROAD", "TESTCARNUMBER2");
        c2.setLength("60");
        c2.setWeight("2000");
        Car c3 = new Car("TESTCARROAD", "TESTCARNUMBER3");
        c3.setLength("50");
        c3.setWeight("1500");

        //  All three cars start out in the old kernel with car 1 as the lead car.
        c1.setKernel(kold);
        c2.setKernel(kold);
        c3.setKernel(kold);
        Assert.assertEquals("Kernel Name for car 1 before", "TESTKERNELOLD", c1.getKernelName());
        Assert.assertEquals("Kernel Name for car 2 before", "TESTKERNELOLD", c2.getKernelName());
        Assert.assertEquals("Kernel Name for car 3 before", "TESTKERNELOLD", c3.getKernelName());
        Assert.assertEquals("Kernel old length before", 40 + 4 + 60 + 4 + 50 + 4, kold.getTotalLength());
        Assert.assertEquals("Kernel new length before", 0, knew.getTotalLength());
        Assert.assertTrue("Kernel old Lead is Car 1 before", kold.isLead(c1));
        Assert.assertFalse("Kernel old Lead is not Car 2 before", kold.isLead(c2));
        Assert.assertFalse("Kernel old Lead is not Car 3 before", kold.isLead(c3));
        Assert.assertFalse("Kernel new Lead is not Car 1 before", knew.isLead(c1));
        Assert.assertFalse("Kernel new Lead is not Car 2 before", knew.isLead(c2));
        Assert.assertFalse("Kernel new Lead is not Car 3 before", knew.isLead(c3));

        //  Move car 1 to the new kernel where it will be the lead car.
        //  Car 2 should now be the lead car of the old kernel.
        c1.setKernel(knew);
        Assert.assertEquals("Kernel Name for car 1 after", "TESTKERNELNEW", c1.getKernelName());
        Assert.assertEquals("Kernel Name for car 2 after", "TESTKERNELOLD", c2.getKernelName());
        Assert.assertEquals("Kernel Name for car 3 after", "TESTKERNELOLD", c3.getKernelName());
        Assert.assertEquals("Kernel old length after", 60 + 4 + 50 + 4, kold.getTotalLength());
        Assert.assertEquals("Kernel new length after", 40 + 4, knew.getTotalLength());
        Assert.assertFalse("Kernel old Lead is not Car 1 after", kold.isLead(c1));
        Assert.assertTrue("Kernel old Lead is Car 2 after", kold.isLead(c2));
        Assert.assertFalse("Kernel old Lead is not Car 3 after", kold.isLead(c3));
        Assert.assertTrue("Kernel new Lead is Car 1 after", knew.isLead(c1));
        Assert.assertFalse("Kernel new Lead is not Car 2 after", knew.isLead(c2));
        Assert.assertFalse("Kernel new Lead is not Car 3 after", knew.isLead(c3));

        //  Move car 3 to the new kernel.
        c3.setKernel(knew);
        Assert.assertEquals("Kernel Name for car 1 after3", "TESTKERNELNEW", c1.getKernelName());
        Assert.assertEquals("Kernel Name for car 2 after3", "TESTKERNELOLD", c2.getKernelName());
        Assert.assertEquals("Kernel Name for car 3 after3", "TESTKERNELNEW", c3.getKernelName());
        Assert.assertEquals("Kernel old length after3", 60 + 4, kold.getTotalLength());
        Assert.assertEquals("Kernel new length after3", 40 + 4 + 50 + 4, knew.getTotalLength());
        Assert.assertFalse("Kernel old Lead is not Car 1 after3", kold.isLead(c1));
        Assert.assertTrue("Kernel old Lead is Car 2 after3", kold.isLead(c2));
        Assert.assertFalse("Kernel old Lead is not Car 3 after3", kold.isLead(c3));
        Assert.assertTrue("Kernel new Lead is Car 1 after3", knew.isLead(c1));
        Assert.assertFalse("Kernel new Lead is not Car 2 after3", knew.isLead(c2));
        Assert.assertFalse("Kernel new Lead is not Car 3 after3", knew.isLead(c3));
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

    public KernelTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", KernelTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(KernelTest.class);
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
