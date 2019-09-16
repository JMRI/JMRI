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
public class PrintTrainManifestIfSelectedActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        PrintTrainManifestIfSelectedAction t = new PrintTrainManifestIfSelectedAction();
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testActionNoAutomationItem() {
        PrintTrainManifestIfSelectedAction action = new PrintTrainManifestIfSelectedAction();
        Assert.assertNotNull("exists", action);
        // does nothing, no automationItem
        action.doAction();
    }

    @Test
    public void testGetActionName() {
        PrintTrainManifestIfSelectedAction action = new PrintTrainManifestIfSelectedAction();
        Assert.assertEquals("name", Bundle.getMessage("PrintTrainManifestIfSelected"), action.getName());
        InstanceManager.getDefault(TrainManager.class).setPrintPreviewEnabled(true);
        Assert.assertEquals("name", Bundle.getMessage("PreviewTrainManifestIfSelected"), action.getName());
    }

    @Test
    public void testIsMessageOkEnabled() {
        PrintTrainManifestIfSelectedAction action = new PrintTrainManifestIfSelectedAction();
        Assert.assertTrue(action.isMessageOkEnabled());
    }
    
    @Test
    public void testIsMessageFailedEnabled() {
        PrintTrainManifestIfSelectedAction action = new PrintTrainManifestIfSelectedAction();
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

        PrintTrainManifestIfSelectedAction action = new PrintTrainManifestIfSelectedAction();
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
        train.setBuildEnabled(false);
        
        // does nothing, build not enabled
        action.doAction();
        Assert.assertFalse(automationItem.isActionRunning());
        Assert.assertFalse(automationItem.isActionSuccessful());
        Assert.assertEquals("action item status", Bundle.getMessage("FAILED"), automationItem.getStatus());
        
        train.setBuildEnabled(true);
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

    // private final static Logger log = LoggerFactory.getLogger(PrintTrainManifestIfSelectedActionTest.class);

}
