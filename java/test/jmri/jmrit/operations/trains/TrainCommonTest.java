//TrainCommonTest.java
package jmri.jmrit.operations.trains;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.LocationManagerXml;
import jmri.jmrit.operations.locations.Schedule;
import jmri.jmrit.operations.locations.ScheduleItem;
import jmri.jmrit.operations.locations.ScheduleManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarColors;
import jmri.jmrit.operations.rollingstock.cars.CarLengths;
import jmri.jmrit.operations.rollingstock.cars.CarLoad;
import jmri.jmrit.operations.rollingstock.cars.CarLoads;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.CarManagerXml;
import jmri.jmrit.operations.rollingstock.cars.CarOwners;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.rollingstock.cars.Kernel;
import jmri.jmrit.operations.rollingstock.engines.Consist;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.rollingstock.engines.EngineManagerXml;
import jmri.jmrit.operations.rollingstock.engines.EngineModels;
import jmri.jmrit.operations.rollingstock.engines.EngineTypes;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.routes.RouteManager;
import jmri.jmrit.operations.routes.RouteManagerXml;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.OperationsSetupXml;
import jmri.jmrit.operations.setup.Setup;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jdom2.JDOMException;

/**
 * Tests for the TrainCommon class 
 *
 * @author Paul Bender Copyright (C) 2015
 * @version $Revision$
 */
public class TrainCommonTest extends TestCase {

    public void testGetDate_DateArgument(){
       java.util.Calendar calendar = java.util.Calendar.getInstance();
       String date = TrainCommon.getDate(calendar.getTime());
       Assert.assertNotNull("Date String",date);
    }

    public void testGetDate_BooleanArgument(){
       String date = TrainCommon.getDate(false);
       Assert.assertNotNull("Date String",date);
    }

    public void testConvertStringDateToDouble(){
       java.util.Calendar calendar = java.util.Calendar.getInstance();
       calendar.set(java.util.Calendar.MONTH,4);
       calendar.set(java.util.Calendar.DAY_OF_MONTH,26);
       calendar.set(java.util.Calendar.YEAR,2015);
       calendar.set(java.util.Calendar.HOUR_OF_DAY,23);
       calendar.set(java.util.Calendar.MINUTE,30);
       java.util.Date d = calendar.getTime();
       String date = TrainCommon.getDate(d);
       TrainCommon tc = new TrainCommon();
       Assert.assertTrue("Double Time 0",0<tc.convertStringDateToDouble(date)); 
    }

    public void testComparableDateToDouble(){
       java.util.Calendar calendar = java.util.Calendar.getInstance();
       calendar.set(java.util.Calendar.MONTH,12);
       calendar.set(java.util.Calendar.DAY_OF_MONTH,31);
       calendar.set(java.util.Calendar.YEAR,2015);
       calendar.set(java.util.Calendar.HOUR_OF_DAY,23);
       calendar.set(java.util.Calendar.MINUTE,30);
       java.util.Date d = calendar.getTime();
       String date = TrainCommon.getDate(d);
       calendar.set(java.util.Calendar.MONTH,1);
       calendar.set(java.util.Calendar.DAY_OF_MONTH,1);
       calendar.set(java.util.Calendar.YEAR,2016);
       calendar.set(java.util.Calendar.HOUR_OF_DAY,00);
       calendar.set(java.util.Calendar.MINUTE,30);
       java.util.Date d1 = calendar.getTime();
       String date1 = TrainCommon.getDate(d1);
       TrainCommon tc = new TrainCommon();
       Assert.assertTrue("Comparable Dates",tc.convertStringDateToDouble(date)<tc.convertStringDateToDouble(date1)); 
    }

    // from here down is testing infrastructure
    // Ensure minimal setup for log4J
    @Override
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    public TrainCommonTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", TrainCommonTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(TrainCommonTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
