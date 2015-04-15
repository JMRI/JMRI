//ScheduleTest.java
package jmri.jmrit.operations.locations;

import java.io.File;
import java.util.List;
import java.util.Locale;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManagerXml;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManagerXml;
import jmri.jmrit.operations.routes.RouteManagerXml;
import jmri.jmrit.operations.setup.OperationsSetupXml;
import jmri.jmrit.operations.trains.TrainManagerXml;
import jmri.util.FileUtil;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the Operations Locations class Last manually cross-checked on
 * 20090131
 *
 * Still to do: ScheduleItem: XML read/write Schedule: Register, List, XML
 * read/write Track: AcceptsDropTrain, AcceptsDropRoute Track:
 * AcceptsPickupTrain, AcceptsPickupRoute Track: CheckScheduleValid Track: XML
 * read/write Location: Track support <-- I am here Location: XML read/write
 *
 * @author Bob Coleman Copyright (C) 2008, 2009
 * @version $Revision$
 */
public class ScheduleTest extends TestCase {

    // test Schedule class
    // test schedule public constants
    public void testScheduleConstants() {
        Assert.assertEquals("Location Schedule Constant LISTCHANGE_CHANGED_PROPERTY", "scheduleListChange",
                Schedule.LISTCHANGE_CHANGED_PROPERTY);
        Assert.assertEquals("Location Schedule Constant DISPOSE", "scheduleDispose", Schedule.DISPOSE);
    }

    // test schedule attributes
    public void testScheduleAttributes() {
        Schedule lts = new Schedule("Test id", "Test Name");
        Assert.assertEquals("Location Schedule id", "Test id", lts.getId());
        Assert.assertEquals("Location Schedule Name", "Test Name", lts.getName());

        lts.setName("New Test Name");
        Assert.assertEquals("Location Schedule set Name", "New Test Name", lts.getName());
        Assert.assertEquals("Location Schedule toString", "New Test Name", lts.toString());

        lts.setComment("New Test Comment");
        Assert.assertEquals("Location Schedule set Comment", "New Test Comment", lts.getComment());
    }

    // test schedule scheduleitem
    public void testScheduleScheduleItems() {
        Schedule lts = new Schedule("Test id", "Test Name");
        Assert.assertEquals("Location Schedule ScheduleItem id", "Test id", lts.getId());
        Assert.assertEquals("Location Schedule ScheduleItem Name", "Test Name", lts.getName());

        ScheduleItem ltsi1, ltsi2, ltsi3, ltsi4;

        ltsi1 = lts.addItem("New Test Type");
        Assert.assertEquals("Location Schedule ScheduleItem Check Type", "New Test Type", ltsi1.getTypeName());

        String testid = ltsi1.getId();
        ltsi2 = lts.getItemByType("New Test Type");
        Assert.assertEquals("Location Schedule ScheduleItem Check Ids", testid, ltsi2.getId());
        Assert.assertEquals("Location Schedule ScheduleItem Check Seq 0", 1, ltsi2.getSequenceId());

        ltsi3 = lts.addItem("New Second Test Type");
        Assert.assertEquals("Location Schedule ScheduleItem Check Seq 1", 1, ltsi1.getSequenceId());
        Assert.assertEquals("Location Schedule ScheduleItem Check Seq 2", 2, ltsi3.getSequenceId());

        lts.moveItemUp(ltsi3);
        Assert.assertEquals("Location Schedule ScheduleItem Check Seq 3", 2, ltsi1.getSequenceId());
        Assert.assertEquals("Location Schedule ScheduleItem Check Seq 4", 1, ltsi3.getSequenceId());

        ltsi4 = lts.addItem("New Third Test Item", 1);
        Assert.assertEquals("Location Schedule ScheduleItem Check Seq 5", 3, ltsi1.getSequenceId());
        Assert.assertEquals("Location Schedule ScheduleItem Check Seq 6", 1, ltsi3.getSequenceId());
        Assert.assertEquals("Location Schedule ScheduleItem Check Seq 7", 2, ltsi4.getSequenceId());

        lts.moveItemDown(ltsi3);
        Assert.assertEquals("Location Schedule ScheduleItem Check Seq 8", 3, ltsi1.getSequenceId());
        Assert.assertEquals("Location Schedule ScheduleItem Check Seq 9", 2, ltsi3.getSequenceId());
        Assert.assertEquals("Location Schedule ScheduleItem Check Seq 10", 1, ltsi4.getSequenceId());

        lts.deleteItem(ltsi3);
        Assert.assertEquals("Location Schedule ScheduleItem Check Seq 8", 2, ltsi1.getSequenceId());
        Assert.assertEquals("Location Schedule ScheduleItem Check Seq 9", 1, ltsi4.getSequenceId());
    }

    // from here down is testing infrastructure
    // Ensure minimal setup for log4J
    /**
     * Test-by test initialization. Does log4j for standalone use, and then
     * creates a set of turnouts, sensors and signals as common background for
     * testing
     */
    @Override
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();

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

        LocationManager.instance().dispose();
        ScheduleManager.instance().dispose();
        CarTypes.instance().dispose();
    }

    public ScheduleTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", ScheduleTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(ScheduleTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }
}
