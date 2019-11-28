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
public class ScheduleTest extends OperationsTestCase {

    // test Schedule class
    // test schedule public constants
    @Test
    public void testScheduleConstants() {
        Assert.assertEquals("Location Schedule Constant LISTCHANGE_CHANGED_PROPERTY", "scheduleListChange",
                Schedule.LISTCHANGE_CHANGED_PROPERTY);
        Assert.assertEquals("Location Schedule Constant DISPOSE", "scheduleDispose", Schedule.DISPOSE);
    }

    // test schedule attributes
    @Test
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

    // test schedule schedule items
    @Test
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
}
