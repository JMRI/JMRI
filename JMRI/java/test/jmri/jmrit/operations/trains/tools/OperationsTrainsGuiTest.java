package jmri.jmrit.operations.trains.tools;

import java.awt.Color;
import java.awt.GraphicsEnvironment;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsSwingTestCase;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainIcon;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.jmrit.operations.trains.timetable.TrainsScheduleTableFrame;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the Operations Trains GUI class
 *
 * @author Dan Boudreau Copyright (C) 2009
 */
public class OperationsTrainsGuiTest extends OperationsSwingTestCase {

    @Test
    public void testTrainModifyFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // confirm that train default accepts Boxcars
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train t = tmanager.newTrain("Test Train Name 2");
        Assert.assertTrue("accepts Boxcar 1", t.acceptsTypeName("Boxcar"));

        TrainsByCarTypeFrame f = new TrainsByCarTypeFrame();
        f.initComponents("Boxcar");

        // remove Boxcar from trains
        enterClickAndLeave(f.clearButton);
        enterClickAndLeave(f.saveButton);

        Assert.assertFalse("accepts Boxcar 2", t.acceptsTypeName("Boxcar"));

        // now add Boxcar to trains
        enterClickAndLeave(f.setButton);
        enterClickAndLeave(f.saveButton);

        Assert.assertTrue("accepts Boxcar 3", t.acceptsTypeName("Boxcar"));

        JUnitUtil.dispose(f);
    }

    @Test
    public void testTrainByCarTypeFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train = tmanager.getTrainByName("Test Train Name");
        TrainByCarTypeFrame f = new TrainByCarTypeFrame();
        f.initComponents(train);

        Assert.assertNotNull("frame exists", f);
        JUnitUtil.dispose(f);
    }

    @Test
    public void testTrainsScheduleTableFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrainsScheduleTableFrame f = new TrainsScheduleTableFrame();
        f.setVisible(true);

        Assert.assertNotNull("frame exists", f);
        JUnitUtil.dispose(f);
    }

    // test TrainIcon attributes
    @Test
    public void testTrainIconAttributes() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Train train1 = new Train("TESTTRAINID", "TESTNAME");

        Assert.assertEquals("Train Id", "TESTTRAINID", train1.getId());
        Assert.assertEquals("Train Name", "TESTNAME", train1.getName());
        Assert.assertEquals("Train toString", "TESTNAME", train1.toString());

        jmri.jmrit.display.panelEditor.PanelEditor editor = new jmri.jmrit.display.panelEditor.PanelEditor(
                "Test Panel");
        Assert.assertNotNull("New editor", editor);
        TrainIcon trainicon1 = editor.addTrainIcon("TestName");
        trainicon1.setTrain(train1);
        Assert.assertEquals("TrainIcon set train", "TESTNAME", trainicon1.getTrain().getName());
        
        // confirm that there are 6 icon colors
        Assert.assertEquals("Six colors", 6, TrainIcon.getLocoColors().length);

        // test color change
        for (String color : TrainIcon.getLocoColors()) {
            trainicon1.setLocoColor(color);
            if (color.equals(TrainIcon.WHITE)) {
                Assert.assertEquals("White train icon", Color.WHITE, trainicon1.getLocoColor());
            }
            if (color.equals(TrainIcon.GREEN)) {
                Assert.assertEquals("Green train icon", Color.GREEN, trainicon1.getLocoColor());
            }
            if (color.equals(TrainIcon.GRAY)) {
                Assert.assertEquals("Gray train icon", Color.GRAY, trainicon1.getLocoColor());
            }
            if (color.equals(TrainIcon.RED)) {
                Assert.assertEquals("Red train icon", Color.RED, trainicon1.getLocoColor());
            }
            if (color.equals(TrainIcon.YELLOW)) {
                Assert.assertEquals("Yellow train icon", Color.YELLOW, trainicon1.getLocoColor());
            }
            if (color.equals(TrainIcon.BLUE)) {
                Assert.assertEquals("Blue train icon", TrainIcon.COLOR_BLUE, trainicon1.getLocoColor());
            }
        }
        editor.getTargetFrame().dispose();
    }

    // Ensure minimal setup for log4J
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }
}
