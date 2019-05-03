package jmri.jmrit.operations.locations.schedules;

import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.Test;

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
    @Test
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
    @Test
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
}
