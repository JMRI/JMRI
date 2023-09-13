package jmri.jmrit.operations.trains.schedules;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;
import org.netbeans.jemmy.operators.*;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.*;
import jmri.util.swing.JemmyUtil;

/**
 * @author Paul Bender Copyright (C) 2017
 */
public class TrainsScheduleTableFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrainsScheduleTableFrame t = new TrainsScheduleTableFrame();
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(t);
    }

    @Test
    public void testButtons() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();
        TrainManager tm = InstanceManager.getDefault(TrainManager.class);
        Train sff = tm.getTrainByName("SFF");
        Train stf = tm.getTrainByName("STF");

        TrainsScheduleTableFrame f = new TrainsScheduleTableFrame();
        Assert.assertNotNull("exists", f);

        JFrameOperator jfo = new JFrameOperator(f);
        JTableOperator tbl = new JTableOperator(jfo);

        Assert.assertFalse("button enabled", f.activateButton.isEnabled());
        JemmyUtil.enterClickAndLeave(f.anyButton);
        Assert.assertTrue("button enabled", f.activateButton.isEnabled());

        JemmyUtil.enterClickAndLeave(f.noneButton);
        Assert.assertFalse("button enabled", f.activateButton.isEnabled());

        JRadioButtonOperator jbo = new JRadioButtonOperator(jfo, "Monday");
        jbo.doClick();
        Assert.assertTrue("button enabled", f.activateButton.isEnabled());

        Assert.assertEquals("Monday", false, tbl.getValueAt(0, tbl.findColumn("Monday")));
        Assert.assertEquals("Monday", false, tbl.getValueAt(1, tbl.findColumn("Monday")));
        JemmyUtil.enterClickAndLeave(f.applyButton);
        Assert.assertFalse("Build", sff.isBuildEnabled());
        Assert.assertFalse("Build", stf.isBuildEnabled());
        Assert.assertFalse("Built", sff.isBuilt());
        Assert.assertFalse("Built", stf.isBuilt());

        JemmyUtil.enterClickAndLeave(f.selectButton);
        Assert.assertEquals("Monday", true, tbl.getValueAt(0, tbl.findColumn("Monday")));
        Assert.assertEquals("Monday", true, tbl.getValueAt(1, tbl.findColumn("Monday")));
        JemmyUtil.enterClickAndLeave(f.applyButton);
        Assert.assertTrue("Build", sff.isBuildEnabled());
        Assert.assertTrue("Build", stf.isBuildEnabled());
        JemmyUtil.enterClickAndLeave(f.buildButton);
        jmri.util.JUnitUtil.waitFor(() -> {
            return sff.isBuilt();
        }, "wait for train to build");
        Assert.assertTrue("Built", sff.isBuilt());
        Assert.assertTrue("Built", stf.isBuilt());

        // test activate button
        TrainSchedule sch = InstanceManager.getDefault(TrainScheduleManager.class).getActiveSchedule();
        Assert.assertNull(sch);
        JemmyUtil.enterClickAndLeave(f.activateButton);
        sch = InstanceManager.getDefault(TrainScheduleManager.class).getActiveSchedule();
        Assert.assertNotNull(sch);
        Assert.assertEquals("schedule name", "Monday", sch.getName());
        Assert.assertEquals("schedule comment", "", sch.getComment());
        
        sch.setName("MONDAY");
        
        // test Save
        f.commentTextArea.setText("Test Comment");
        JemmyUtil.enterClickAndLeave(f.saveButton);
        Assert.assertEquals("schedule comment", "Test Comment", sch.getComment());       

        JemmyUtil.enterClickAndLeave(f.clearButton);
        Assert.assertEquals("Monday", false, tbl.getValueAt(0, tbl.findColumn("Monday")));
        Assert.assertEquals("Monday", false, tbl.getValueAt(1, tbl.findColumn("Monday")));
        JemmyUtil.enterClickAndLeave(f.applyButton);
        Assert.assertFalse("Build", sff.isBuildEnabled());
        Assert.assertFalse("Build", stf.isBuildEnabled());
        Assert.assertTrue("Built", sff.isBuilt());
        Assert.assertTrue("Built", stf.isBuilt());

        tm.setPrintPreviewEnabled(true);
        JemmyUtil.enterClickAndLeave(f.selectButton);
        JemmyUtil.enterClickAndLeave(f.applyButton);
        JemmyUtil.enterClickAndLeave(f.sortByName);
        JemmyUtil.enterClickAndLeave(f.printButton); // preview
        JemmyUtil.enterClickAndLeave(f.switchListsButton); // preview
        
        // TODO test terminate button
        // JemmyUtil.enterClickAndLeave(f.terminateButton);
        //
        // jmri.util.JUnitUtil.waitFor(() -> {
        // return !sff.isBuilt();
        // }, "wait for train to terminate");
        // Assert.assertFalse("Built", sff.isBuilt());
        // Assert.assertFalse("Built", stf.isBuilt());
        
        Assert.assertEquals("Train name", "SFF", tbl.getValueAt(0, tbl.findColumn(Bundle.getMessage("Name"))));

        JemmyUtil.enterClickAndLeave(f.sortByTime);
        Assert.assertEquals("Train name", "STF", tbl.getValueAt(0, tbl.findColumn(Bundle.getMessage("Name"))));

        JUnitUtil.dispose(f);
        JmriJFrame jFrame = JmriJFrame.getFrame("Print Preview: Train STF");
        Assert.assertNotNull(jFrame);
        JUnitUtil.dispose(jFrame);
        
        jFrame = JmriJFrame.getFrame("Print Preview: Train SFF");
        Assert.assertNotNull(jFrame);
        JUnitUtil.dispose(jFrame);
        
        jFrame = JmriJFrame.getFrame("Print Preview: North End Staging");
        Assert.assertNotNull(jFrame);
        JUnitUtil.dispose(jFrame);
        
        jFrame = JmriJFrame.getFrame("Print Preview: North Industries");
        Assert.assertNotNull(jFrame);
        JUnitUtil.dispose(jFrame);
        
        jFrame = JmriJFrame.getFrame("Print Preview: South End Staging");
        Assert.assertNotNull(jFrame);
        JUnitUtil.dispose(jFrame);
        
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }
    
    @Test
    public void testCloseWindowOnSave() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrainsScheduleTableFrame f = new TrainsScheduleTableFrame();
        f.initComponents();
        JUnitOperationsUtil.testCloseWindowOnSave(f.getTitle());
    }
}
