package jmri.jmrit.operations.locations.schedules;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.util.List;

import javax.swing.JComboBox;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JTableOperator;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.trains.schedules.TrainSchedule;
import jmri.jmrit.operations.trains.schedules.TrainScheduleManager;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ScheduleEditFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Location l = new Location("Location Test Attridutes id", "Location Test Name");
        Track trk = new Track("Test id", "Test Name", "Test Type", l);
        ScheduleEditFrame t = new ScheduleEditFrame(new Schedule("Test id", "Test Name"), trk);
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(t);
    }

    @Test
    public void testScheduleEditFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LocationManager lManager = InstanceManager.getDefault(LocationManager.class);
        Location l = lManager.newLocation("Test Loc C");
        Track t = l.addTrack("3rd spur track", Track.SPUR);
        Assert.assertNotNull("Track exists", t);
        ScheduleEditFrame f = new ScheduleEditFrame(null, t);
        f.setSize(new Dimension(1300, Control.panelHeight500));
        f.setTitle("Test Schedule Frame");
        f.scheduleNameTextField.setText("Test Schedule A");
        f.commentTextField.setText("Test Comment");
        JemmyUtil.enterClickAndLeave(f.addScheduleButton);

        // was the schedule created?
        ScheduleManager sm = InstanceManager.getDefault(ScheduleManager.class);
        Schedule s = sm.getScheduleByName("Test Schedule A");
        Assert.assertNotNull("Test Schedule A exists", s);
        Assert.assertEquals("Confirm comment", "Test Comment", s.getComment());

        // now add some car types to the schedule
        String carTypes[] = Bundle.getMessage("carTypeNames").split(",");
        f.typeBox.setSelectedItem(carTypes[1]);
        JemmyUtil.enterClickAndLeave(f.addTypeButton);
        f.typeBox.setSelectedItem(carTypes[2]);
        JemmyUtil.enterClickAndLeave(f.addTypeButton);
        f.typeBox.setSelectedItem(carTypes[3]);
        JemmyUtil.enterClickAndLeave(f.addTypeButton);
        // put Tank Food at start of list
        f.typeBox.setSelectedItem(carTypes[4]);
        JemmyUtil.enterClickAndLeave(f.addLocAtTop);
        JemmyUtil.enterClickAndLeave(f.addTypeButton);
        JemmyUtil.enterClickAndLeave(f.saveScheduleButton);

        List<ScheduleItem> list = s.getItemsBySequenceList();
        Assert.assertEquals("number of items", 4, list.size());

        // since this test is internationalized, and the non-english
        // lists are internationalized, we can just check if each of
        // the types is in the list.
        for (ScheduleItem si : list) {
            boolean flag = false;
            for (int i = 1; i < 5; i++) {
                if (si.getTypeName().equals(carTypes[i])) {
                    flag = true;
                }
            }
            Assert.assertTrue("type " + si.getTypeName() + " in list", flag);
        }

        JemmyUtil.enterClickAndLeaveThreadSafe(f.deleteScheduleButton);
        // Yes to pop up
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("DeleteSchedule?"), Bundle.getMessage("ButtonYes"));
        JemmyUtil.waitFor(f);
        s = sm.getScheduleByName("Test Schedule A");
        Assert.assertNull("Test Schedule A exists", s);

        JUnitUtil.dispose(f);
    }

    @Test
    public void testScheduleDownButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        setupTest();
        
        // test down button
        tbl.clickOnCell(0, tbl.findColumn(Bundle.getMessage("Down")));
        Assert.assertEquals("1st line item car type from table", carTypes[2], tbl.getValueAt(0, tbl.findColumn(Bundle.getMessage("Type"))));
        Assert.assertEquals("2nd line item car type from table", carTypes[1], tbl.getValueAt(1, tbl.findColumn(Bundle.getMessage("Type"))));
        Assert.assertEquals("3rd line item car type from table", carTypes[3], tbl.getValueAt(2, tbl.findColumn(Bundle.getMessage("Type"))));
        
        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testScheduleUpButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        setupTest();
        
        // test up button
        // findColumn return the first column with the characters "up"
//        tbl.clickOnCell(0, tbl.findColumn(Bundle.getMessage("Up")));
        tbl.clickOnCell(0, 13);
        Assert.assertEquals("1st line item car type from table", carTypes[2], tbl.getValueAt(0, tbl.findColumn(Bundle.getMessage("Type"))));
        Assert.assertEquals("2nd line item car type from table", carTypes[3], tbl.getValueAt(1, tbl.findColumn(Bundle.getMessage("Type"))));
        Assert.assertEquals("3rd line item car type from table", carTypes[1], tbl.getValueAt(2, tbl.findColumn(Bundle.getMessage("Type"))));
        
        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testScheduleDeleteButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());       
        setupTest();
        
        // test delete button
        tbl.clickOnCell(0, tbl.findColumn(Bundle.getMessage("ButtonDelete")));
        Assert.assertEquals("1st line item car type from table", carTypes[2], tbl.getValueAt(0, tbl.findColumn(Bundle.getMessage("Type"))));
        Assert.assertEquals("2nd line item car type from table", carTypes[3], tbl.getValueAt(1, tbl.findColumn(Bundle.getMessage("Type"))));
        Assert.assertEquals("Number of line items", 2, sch.getSize());
        
        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testScheduleCurrentPointer() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());       
        setupTest();
        // need to test in sequential mode
        JemmyUtil.enterClickAndLeave(f.sequentialRadioButton);
        JemmyUtil.enterClickAndLeave(f.saveScheduleButton);
        
        // test change current pointer
        tbl.clickForEdit(1, tbl.findColumn(Bundle.getMessage("Current")));
        JemmyUtil.enterClickAndLeave(f.saveScheduleButton);
        Assert.assertEquals("1st line item from table", "", tbl.getValueAt(0, tbl.findColumn(Bundle.getMessage("Current"))));
        Assert.assertEquals("2nd line item from table", ScheduleTableModel.POINTER, tbl.getValueAt(1, tbl.findColumn(Bundle.getMessage("Current"))));
        Assert.assertEquals("3rd line item from table", "", tbl.getValueAt(2, tbl.findColumn(Bundle.getMessage("Current"))));
        
        Assert.assertEquals("Current", si2, f._track.getCurrentScheduleItem());
                
        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testScheduleCount() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());       
        setupTest();
        // need to test in sequential mode
        JemmyUtil.enterClickAndLeave(f.sequentialRadioButton);
        JemmyUtil.enterClickAndLeave(f.saveScheduleButton);
        
        // test count
        tbl.setValueAt(7, 2, tbl.findColumn(Bundle.getMessage("Count")));
        JemmyUtil.enterClickAndLeave(f.saveScheduleButton);
        Assert.assertEquals("1st line item from table", 1, tbl.getValueAt(0, tbl.findColumn(Bundle.getMessage("Count"))));
        Assert.assertEquals("2nd line item from table", 1, tbl.getValueAt(1, tbl.findColumn(Bundle.getMessage("Count"))));
        Assert.assertEquals("3rd line item from table", 7, tbl.getValueAt(2, tbl.findColumn(Bundle.getMessage("Count"))));
        
        Assert.assertEquals("1st line item", 1, si1.getCount());
        Assert.assertEquals("2nd line item", 1, si2.getCount());
        Assert.assertEquals("3rd line item", 7, si3.getCount());
        
        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testScheduleWait() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());       
        setupTest();
        
        // test wait value
        tbl.setValueAt(3, 1, tbl.findColumn(Bundle.getMessage("Wait")));
        JemmyUtil.enterClickAndLeave(f.saveScheduleButton);
        Assert.assertEquals("1st line item from table", 0, tbl.getValueAt(0, tbl.findColumn(Bundle.getMessage("Wait"))));
        Assert.assertEquals("2nd line item from table", 3, tbl.getValueAt(1, tbl.findColumn(Bundle.getMessage("Wait"))));
        Assert.assertEquals("3rd line item from table", 0, tbl.getValueAt(2, tbl.findColumn(Bundle.getMessage("Wait"))));
        
        Assert.assertEquals("1st line item", 0, si1.getWait());
        Assert.assertEquals("2nd line item", 3, si2.getWait());
        Assert.assertEquals("3rd line item", 0, si3.getWait());
                
        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testScheduleRandom() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());       
        setupTest();
        
        // test random value
        JComboBox<String> box = f.scheduleModel.getRandomComboBox(si2);
        box.setSelectedItem("50");
        tbl.setValueAt(box, 1, tbl.findColumn(Bundle.getMessage("Random")));
        JemmyUtil.enterClickAndLeave(f.saveScheduleButton);
        
        Assert.assertEquals("1st line item", ScheduleItem.NONE, si1.getRandom());
        Assert.assertEquals("2nd line item", "50", si2.getRandom());
        Assert.assertEquals("3rd line item", ScheduleItem.NONE, si3.getRandom());
                
        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testSetOutDay() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());       
        setupTest();
        
        JComboBox<TrainSchedule> box = InstanceManager.getDefault(TrainScheduleManager.class).getSelectComboBox();
        box.setSelectedIndex(2); // Monday
        tbl.setValueAt(box, 1, tbl.findColumn(Bundle.getMessage("Delivery")));
        JemmyUtil.enterClickAndLeave(f.saveScheduleButton);
        
        Assert.assertEquals("1st line item", ScheduleItem.NONE, si1.getPickupTrainScheduleId());
        Assert.assertEquals("2nd line item", "Monday", si2.getSetoutTrainScheduleName());
        Assert.assertEquals("3rd line item", ScheduleItem.NONE, si3.getPickupTrainScheduleId());
                
        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testPickUpDay() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());       
        setupTest();
        
        JComboBox<TrainSchedule> box = InstanceManager.getDefault(TrainScheduleManager.class).getSelectComboBox();
        box.setSelectedIndex(1); // Sunday
        tbl.setValueAt(box, 1, tbl.findColumn(Bundle.getMessage("Pickup")));
        JemmyUtil.enterClickAndLeave(f.saveScheduleButton);
        
        Assert.assertEquals("1st line item", ScheduleItem.NONE, si1.getPickupTrainScheduleId());
        Assert.assertEquals("2nd line item", "Sunday", si2.getPickupTrainScheduleName());
        Assert.assertEquals("3rd line item", ScheduleItem.NONE, si3.getPickupTrainScheduleId());
                
        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testScheduleHit() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());       
        setupTest();
        
        // test hit value
        tbl.setValueAt(5, 1, tbl.findColumn(Bundle.getMessage("Hits")));
        JemmyUtil.enterClickAndLeave(f.saveScheduleButton);
        Assert.assertEquals("1st line item from table", 0, tbl.getValueAt(0, tbl.findColumn(Bundle.getMessage("Hits"))));
        Assert.assertEquals("2nd line item from table", 5, tbl.getValueAt(1, tbl.findColumn(Bundle.getMessage("Hits"))));
        Assert.assertEquals("3rd line item from table", 0, tbl.getValueAt(2, tbl.findColumn(Bundle.getMessage("Hits"))));
        
        JUnitUtil.dispose(f);
    }
    
    ScheduleEditFrame f;
    Schedule sch;
    ScheduleItem si1;
    ScheduleItem si2;
    ScheduleItem si3;
    JTableOperator tbl;
    String carTypes[] = Bundle.getMessage("carTypeNames").split(",");
    
    private void setupTest() {
        // create a schedule with 3 line items
        ScheduleManager sm = InstanceManager.getDefault(ScheduleManager.class);
        sch = sm.newSchedule("Test Schedule Name");
        sch.setComment("Test Comment");
        si1 = sch.addItem(carTypes[1]);
        si2 = sch.addItem(carTypes[2]);
        si3 = sch.addItem(carTypes[3]);
        
        LocationManager lManager = InstanceManager.getDefault(LocationManager.class);
        Location l = lManager.newLocation("Test Loc");
        Track t = l.addTrack("spur track", Track.SPUR);
        f = new ScheduleEditFrame(sch, t);
        f.setSize(new Dimension(1300, Control.panelHeight500));
        
        Assert.assertEquals("Confirm schedule name", "Test Schedule Name", f.scheduleNameTextField.getText());
        Assert.assertEquals("Confirm comment", "Test Comment", f.commentTextField.getText());
        
        // confirm 3 line items
        List<ScheduleItem> list = sch.getItemsBySequenceList();
        Assert.assertEquals("number of items", 3, list.size());
        Assert.assertEquals("1st line item car type", carTypes[1], list.get(0).getTypeName());
        
        JFrameOperator jfo = new JFrameOperator(f);
        tbl = new JTableOperator(jfo);
        
        Assert.assertEquals("1st line item car type from table", carTypes[1], tbl.getValueAt(0, tbl.findColumn(Bundle.getMessage("Type"))));
        Assert.assertEquals("2nd line item car type from table", carTypes[2], tbl.getValueAt(1, tbl.findColumn(Bundle.getMessage("Type"))));
        Assert.assertEquals("3rd line item car type from table", carTypes[3], tbl.getValueAt(2, tbl.findColumn(Bundle.getMessage("Type"))));
    }
}
