package jmri.jmrit.operations.automation.actions;

import java.awt.GraphicsEnvironment;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.automation.AutomationItem;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;

/**
 * @author Paul Bender Copyright (C) 2017
 */
public class PrintTrainBuildReportActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        PrintTrainBuildReportAction t = new PrintTrainBuildReportAction();
        Assert.assertNotNull("exists", t);
    }

    @Test
    public void testActionNoAutomationItem() {
        PrintTrainBuildReportAction action = new PrintTrainBuildReportAction();
        Assert.assertNotNull("exists", action);
        // does nothing, no automationItem
        action.doAction();
    }

    @Test
    public void testGetActionName() {
        PrintTrainBuildReportAction action = new PrintTrainBuildReportAction();
        Assert.assertEquals("name", Bundle.getMessage("PrintTrainBuildReport"), action.getName());
        InstanceManager.getDefault(TrainManager.class).setPrintPreviewEnabled(true);
        Assert.assertEquals("name", Bundle.getMessage("PreviewTrainBuildReport"), action.getName());
    }

    @Test
    public void testIsMessageOkEnabled() {
        PrintTrainBuildReportAction action = new PrintTrainBuildReportAction();
        Assert.assertTrue(action.isMessageOkEnabled());
    }

    @Test
    public void testIsMessageFailedEnabled() {
        PrintTrainBuildReportAction action = new PrintTrainBuildReportAction();
        Assert.assertTrue(action.isMessageFailEnabled());
    }

    @Test
    public void testAction() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        tmanager.setPrintPreviewEnabled(true);

        Train train = tmanager.getTrainByName("STF");
        Assert.assertTrue(train.build());

        PrintTrainBuildReportAction action = new PrintTrainBuildReportAction();
        Assert.assertNotNull("exists", action);
        AutomationItem automationItem = new AutomationItem("TestId");
        automationItem.setAction(action);
        Assert.assertEquals("confirm registered", automationItem, action.getAutomationItem());

        // does nothing, no train assignment
        action.doAction();
        Assert.assertFalse(automationItem.isActionRunning());
        Assert.assertFalse(automationItem.isActionSuccessful());
        Assert.assertEquals("action item status", Bundle.getMessage("FAILED"), automationItem.getStatus());

        automationItem.setTrain(train);
        action.doAction();
        Assert.assertFalse(automationItem.isActionRunning());
        Assert.assertTrue(automationItem.isActionSuccessful());

        // confirm print preview window is showing
        ResourceBundle rb = ResourceBundle
                .getBundle("jmri.util.UtilBundle");
        ResourceBundle rb2 = ResourceBundle
                .getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle");
        String frameName = rb.getString("PrintPreviewTitle") + " " +
                MessageFormat.format(rb2.getString("buildReport"), new Object[]{train.getDescription()});
        JmriJFrame printPreviewFrame = JmriJFrame.getFrame(frameName);
        Assert.assertNotNull("exists", printPreviewFrame);
        JUnitUtil.dispose(printPreviewFrame);

        JUnitOperationsUtil.checkOperationsShutDownTask();

    }

    // private final static Logger log =
    // LoggerFactory.getLogger(PrintTrainManifestActionTest.class);

}
