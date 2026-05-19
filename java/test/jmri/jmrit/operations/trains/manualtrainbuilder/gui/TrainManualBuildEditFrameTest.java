package jmri.jmrit.operations.trains.manualtrainbuilder.gui;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JTableOperator;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import jmri.util.swing.JemmyUtil;

/**
 * @author Daniel Boudreau Copyright (C) 2028
 */
public class TrainManualBuildEditFrameTest extends OperationsTestCase {

    @Test
    public void testCreate() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        setupTest();
        JUnitUtil.dispose(tmbef);
    }

    @Test
    public void testDeleteButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        setupTest();
        JmriJFrame f = JmriJFrame.getFrame(Bundle.getMessage("TitleManualBuild"));
        Assert.assertNotNull("manual build frame", f);

        JemmyUtil.enterClickAndLeaveThreadSafe(tmbef.deleteManualBuildButton);
        JemmyUtil.pressDialogButton(Bundle.getMessage("DeleteManualBuild?"), Bundle.getMessage("ButtonYes"));
        f = JmriJFrame.getFrame(Bundle.getMessage("TitleManualBuild"));
        Assert.assertNull("manual build frame", f);
    }

    @Test
    public void testSaveButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        setupTest();
        JemmyUtil.enterClickAndLeave(tmbef.saveManualBuildButton);
        JmriJFrame f = JmriJFrame.getFrame(Bundle.getMessage("TitleManualBuild"));
        Assert.assertNotNull("manual build frame", f);
        Setup.setCloseWindowOnSaveEnabled(true);
        JemmyUtil.enterClickAndLeave(tmbef.saveManualBuildButton);
        f = JmriJFrame.getFrame(Bundle.getMessage("TitleManualBuild"));
        Assert.assertNull("manual build frame", f);
    }

    @Test
    public void testAddCarButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        setupTest();
        JemmyUtil.enterClickAndLeave(tmbef.addLocAtTop);
        JemmyUtil.enterClickAndLeave(tmbef.addButton);
        Assert.assertEquals("Confirm id", "1m4", jto.getValueAt(0, jto.findColumn(Bundle.getMessage("Id"))));
        Assert.assertEquals("Confirm id", "1m1", jto.getValueAt(1, jto.findColumn(Bundle.getMessage("Id"))));
        Assert.assertEquals("Confirm id", "1m2", jto.getValueAt(2, jto.findColumn(Bundle.getMessage("Id"))));
        Assert.assertEquals("Confirm id", "1m3", jto.getValueAt(3, jto.findColumn(Bundle.getMessage("Id"))));

        JemmyUtil.enterClickAndLeave(tmbef.addLocAtMiddle);
        JemmyUtil.enterClickAndLeave(tmbef.addButton);
        Assert.assertEquals("Confirm id", "1m4", jto.getValueAt(0, jto.findColumn(Bundle.getMessage("Id"))));
        Assert.assertEquals("Confirm id", "1m1", jto.getValueAt(1, jto.findColumn(Bundle.getMessage("Id"))));
        Assert.assertEquals("Confirm id", "1m5", jto.getValueAt(2, jto.findColumn(Bundle.getMessage("Id"))));
        Assert.assertEquals("Confirm id", "1m2", jto.getValueAt(3, jto.findColumn(Bundle.getMessage("Id"))));
        Assert.assertEquals("Confirm id", "1m3", jto.getValueAt(4, jto.findColumn(Bundle.getMessage("Id"))));

        jto.clickOnCell(1, jto.findColumn(Bundle.getMessage("Id")));
        JemmyUtil.enterClickAndLeave(tmbef.addButton);
        Assert.assertEquals("Confirm id", "1m4", jto.getValueAt(0, jto.findColumn(Bundle.getMessage("Id"))));
        Assert.assertEquals("Confirm id", "1m6", jto.getValueAt(1, jto.findColumn(Bundle.getMessage("Id"))));
        Assert.assertEquals("Confirm id", "1m1", jto.getValueAt(2, jto.findColumn(Bundle.getMessage("Id"))));
        Assert.assertEquals("Confirm id", "1m5", jto.getValueAt(3, jto.findColumn(Bundle.getMessage("Id"))));
        Assert.assertEquals("Confirm id", "1m2", jto.getValueAt(4, jto.findColumn(Bundle.getMessage("Id"))));
        Assert.assertEquals("Confirm id", "1m3", jto.getValueAt(5, jto.findColumn(Bundle.getMessage("Id"))));

        JUnitUtil.dispose(tmbef);
    }

    @Test
    public void testDownButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        setupTest();

        // test down button
        jto.clickOnCell(0, jto.findColumn(Bundle.getMessage("Down")));
        Assert.assertEquals("Confirm id", "1m2", jto.getValueAt(0, jto.findColumn(Bundle.getMessage("Id"))));
        Assert.assertEquals("Confirm id", "1m1", jto.getValueAt(1, jto.findColumn(Bundle.getMessage("Id"))));
        Assert.assertEquals("Confirm id", "1m3", jto.getValueAt(2, jto.findColumn(Bundle.getMessage("Id"))));

        JUnitUtil.dispose(tmbef);
    }

    @Test
    public void testUpButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        setupTest();

        // test up button
        // findColumn returns "pick up" as the "up" column
        jto.clickOnCell(0, jto.findColumn(Bundle.getMessage("Down")) - 1);
        Assert.assertEquals("Confirm id", "1m2", jto.getValueAt(0, jto.findColumn(Bundle.getMessage("Id"))));
        Assert.assertEquals("Confirm id", "1m3", jto.getValueAt(1, jto.findColumn(Bundle.getMessage("Id"))));
        Assert.assertEquals("Confirm id", "1m1", jto.getValueAt(2, jto.findColumn(Bundle.getMessage("Id"))));

        JUnitUtil.dispose(tmbef);
    }

    @Test
    public void testDeleteItemButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        setupTest();

        // test delete button
        jto.clickOnCell(1, jto.findColumn(Bundle.getMessage("ButtonDelete")));
        Assert.assertEquals("Confirm number of rows", 2, tmbtm.getRowCount());
        Assert.assertEquals("Confirm id", "1m1", jto.getValueAt(0, jto.findColumn(Bundle.getMessage("Id"))));
        Assert.assertEquals("Confirm id", "1m3", jto.getValueAt(1, jto.findColumn(Bundle.getMessage("Id"))));

        JUnitUtil.dispose(tmbef);
    }

    TrainManualBuildEditFrame tmbef;
    TrainManualBuildTableModel tmbtm;
    JFrameOperator jfo;
    JTableOperator jto;

    private void setupTest() {
        TrainManager trainManager = InstanceManager.getDefault(TrainManager.class);
        Train train = trainManager.newTrain("Test");
        tmbef = new TrainManualBuildEditFrame(train.getId());
        Assert.assertNotNull("exists", tmbef);

        tmbtm = tmbef.manualBuildModel;

        jfo = new JFrameOperator(tmbef);
        jto = new JTableOperator(jfo);

        // add 3 lines
        JemmyUtil.enterClickAndLeave(tmbef.addButton);
        JemmyUtil.enterClickAndLeave(tmbef.addButton);
        JemmyUtil.enterClickAndLeave(tmbef.addButton);

        Assert.assertEquals("Confirm number of rows", 3, tmbtm.getRowCount());
    }
}
