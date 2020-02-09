package jmri.jmrit.operations.automation.actions;

import java.awt.GraphicsEnvironment;
import java.util.ResourceBundle;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.automation.AutomationItem;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class PrintSwitchListChangesActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        PrintSwitchListChangesAction t = new PrintSwitchListChangesAction();
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testActionNoAutomationItem() {
        PrintSwitchListChangesAction action = new PrintSwitchListChangesAction();
        Assert.assertNotNull("exists", action);
        // does nothing, no automationItem
        action.doAction();
    }

    @Test
    public void testGetActionName() {
        PrintSwitchListChangesAction action = new PrintSwitchListChangesAction();
        Assert.assertEquals("name", Bundle.getMessage("PrintSwitchListChanges"), action.getName());
        InstanceManager.getDefault(TrainManager.class).setPrintPreviewEnabled(true);
        Assert.assertEquals("name", Bundle.getMessage("PreviewSwitchListChanges"), action.getName());
    }

    @Test
    public void testIsMessageOkEnabled() {
        PrintSwitchListChangesAction action = new PrintSwitchListChangesAction();
        Assert.assertTrue(action.isMessageOkEnabled());
    }

    @Test
    public void testAction() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        tmanager.setPrintPreviewEnabled(true);

        Train train = tmanager.getTrainByName("STF");
        Assert.assertTrue(train.build());

        Assert.assertEquals("status", Train.NONE, train.getSwitchListStatus());

        PrintSwitchListChangesAction action = new PrintSwitchListChangesAction();
        Assert.assertNotNull("exists", action);
        AutomationItem automationItem = new AutomationItem("TestId");
        automationItem.setAction(action);
        Assert.assertEquals("confirm registered", automationItem, action.getAutomationItem());

        action.doAction();

        Assert.assertEquals("status", Train.PRINTED, train.getSwitchListStatus());
        
        // 3 print preview windows should have appeared
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Location northEndStaging = lmanager.getLocationByName("North End Staging");
        Location southEndStaging = lmanager.getLocationByName("South End Staging");
        Location northIndustries = lmanager.getLocationByName("North Industries");
        
        // confirm print preview window is showing
        ResourceBundle rb = ResourceBundle
                .getBundle("jmri.util.UtilBundle");
        JmriJFrame printPreviewFrame =
                JmriJFrame.getFrame(rb.getString("PrintPreviewTitle") + " " + northEndStaging.getName());
        Assert.assertNotNull("exists", printPreviewFrame);
        JUnitUtil.dispose(printPreviewFrame);
        
        printPreviewFrame =
                JmriJFrame.getFrame(rb.getString("PrintPreviewTitle") + " " + southEndStaging.getName());
        Assert.assertNotNull("exists", printPreviewFrame);
        JUnitUtil.dispose(printPreviewFrame);
        
        printPreviewFrame =
                JmriJFrame.getFrame(rb.getString("PrintPreviewTitle") + " " + northIndustries.getName());
        Assert.assertNotNull("exists", printPreviewFrame);
        JUnitUtil.dispose(printPreviewFrame);
        
        JUnitOperationsUtil.checkOperationsShutDownTask();

    }

    // private final static Logger log = LoggerFactory.getLogger(PrintSwitchListChangesActionTest.class);

}
