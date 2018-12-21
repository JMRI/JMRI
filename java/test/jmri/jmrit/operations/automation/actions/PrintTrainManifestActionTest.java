package jmri.jmrit.operations.automation.actions;

import java.awt.GraphicsEnvironment;
import java.util.ResourceBundle;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.automation.AutomationItem;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class PrintTrainManifestActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        PrintTrainManifestAction t = new PrintTrainManifestAction();
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testActionNoAutomationItem() {
        PrintTrainManifestAction action = new PrintTrainManifestAction();
        Assert.assertNotNull("exists", action);
        // does nothing, no automationItem
        action.doAction();
    }

    @Test
    public void testGetActionName() {
        PrintTrainManifestAction action = new PrintTrainManifestAction();
        Assert.assertEquals("name", Bundle.getMessage("PrintTrainManifest"), action.getName());
        InstanceManager.getDefault(TrainManager.class).setPrintPreviewEnabled(true);
        Assert.assertEquals("name", Bundle.getMessage("PreviewTrainManifest"), action.getName());
    }

    @Test
    public void testIsMessageOkEnabled() {
        PrintTrainManifestAction action = new PrintTrainManifestAction();
        Assert.assertTrue(action.isMessageOkEnabled());
    }
    
    @Test
    public void testIsMessageFailedEnabled() {
        PrintTrainManifestAction action = new PrintTrainManifestAction();
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

        PrintTrainManifestAction action = new PrintTrainManifestAction();
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
        JmriJFrame printPreviewFrame =
                JmriJFrame.getFrame(rb.getString("PrintPreviewTitle") + " " + train.getDescription());
        Assert.assertNotNull("exists", printPreviewFrame);
        JUnitUtil.dispose(printPreviewFrame);
    }

    // private final static Logger log = LoggerFactory.getLogger(PrintTrainManifestActionTest.class);

}
