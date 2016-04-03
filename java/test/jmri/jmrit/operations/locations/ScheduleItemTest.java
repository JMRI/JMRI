//ScheduleItemTest.java
package jmri.jmrit.operations.locations;

import jmri.jmrit.operations.OperationsTestCase;
import junit.framework.Assert;
import junit.framework.Test;
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
 */
public class ScheduleItemTest extends OperationsTestCase {

    // test ScheduleItem class
    // test ScheduleItem public constants
    public void testScheduleItemConstants() {
        Assert.assertEquals("Location ScheduleItem Constant COUNT_CHANGED_PROPERTY", "scheduleItemCount",
                ScheduleItem.COUNT_CHANGED_PROPERTY);
        Assert.assertEquals("Location ScheduleItem Constant TYPE_CHANGED_PROPERTY", "scheduleItemType",
                ScheduleItem.TYPE_CHANGED_PROPERTY);
        Assert.assertEquals("Location ScheduleItem Constant ROAD_CHANGED_PROPERTY", "scheduleItemRoad",
                ScheduleItem.ROAD_CHANGED_PROPERTY);
        Assert.assertEquals("Location ScheduleItem Constant LOAD_CHANGED_PROPERTY", "scheduleItemLoad",
                ScheduleItem.LOAD_CHANGED_PROPERTY);
        Assert.assertEquals("Location ScheduleItem Constant DISPOSE", "scheduleItemDispose", ScheduleItem.DISPOSE);
    }

    // test ScheduleItem attributes
    public void testScheduleItemAttributes() {
        ScheduleItem ltsi = new ScheduleItem("Test id", "Test Type");
        Assert.assertEquals("Location ScheduleItem id", "Test id", ltsi.getId());
        Assert.assertEquals("Location ScheduleItem Type", "Test Type", ltsi.getTypeName());

        ltsi.setTypeName("New Test Type");
        Assert.assertEquals("Location ScheduleItem set Type", "New Test Type", ltsi.getTypeName());

        ltsi.setComment("New Test Comment");
        Assert.assertEquals("Location ScheduleItem set Comment", "New Test Comment", ltsi.getComment());

        ltsi.setRoadName("New Test Road");
        Assert.assertEquals("Location ScheduleItem set Road", "New Test Road", ltsi.getRoadName());

        ltsi.setReceiveLoadName("New Test Load");
        Assert.assertEquals("Location ScheduleItem set Load", "New Test Load", ltsi.getReceiveLoadName());

        ltsi.setShipLoadName("New Test Ship");
        Assert.assertEquals("Location ScheduleItem set Ship", "New Test Ship", ltsi.getShipLoadName());

        ltsi.setSequenceId(22);
        Assert.assertEquals("Location ScheduleItem set SequenceId", 22, ltsi.getSequenceId());

        ltsi.setCount(222);
        Assert.assertEquals("Location ScheduleItem set Count", 222, ltsi.getCount());
    }

    // from here down is testing infrastructure
    // Ensure minimal setup for log4J
    /**
     * Test-by test initialization. Does log4j for standalone use, and then
     * creates a set of turnouts, sensors and signals as common background for
     * testing
     * @throws Exception 
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public ScheduleItemTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", ScheduleItemTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(ScheduleItemTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
