package jmri.jmrit.operations.automation.actions;

import java.io.File;
import java.io.IOException;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.automation.AutomationItem;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.jmrit.operations.trains.excel.TrainCustomManifest;
import jmri.util.JUnitOperationsUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class RunTrainActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        RunTrainAction t = new RunTrainAction();
        Assert.assertNotNull("exists", t);
    }

    @Test
    public void testActionNoAutomationItem() {
        RunTrainAction action = new RunTrainAction();
        Assert.assertNotNull("exists", action);
        // does nothing, no automationItem
        action.doAction();
    }

    @Test
    public void testGetActionName() {
        RunTrainAction action = new RunTrainAction();
        Assert.assertEquals("name", Bundle.getMessage("RunTrain"), action.getName());
    }

    @Test
    public void testIsMessageOkEnabled() {
        RunTrainAction action = new RunTrainAction();
        Assert.assertTrue(action.isMessageOkEnabled());
    }

    @Test
    public void testIsMessageFailEnabled() {
        RunTrainAction action = new RunTrainAction();
        Assert.assertTrue(action.isMessageFailEnabled());
    }

    @Test
    public void testIsConcurrentAction() {
        RunTrainAction action = new RunTrainAction();
        Assert.assertFalse(action.isConcurrentAction());
    }

    @Test
    public void testRunTrainActionFailures() {

        JUnitOperationsUtil.initOperationsData();

        RunTrainAction action = new RunTrainAction();
        Assert.assertNotNull("exists", action);
        AutomationItem automationItem = new AutomationItem("TestId");
        automationItem.setAction(action);
        Assert.assertEquals("confirm registered", automationItem, action.getAutomationItem());

        // confirm that default is not enabled
        Assert.assertFalse(Setup.isGenerateCsvManifestEnabled());

        // does nothing, not enabled
        action.doAction();
        Assert.assertFalse(automationItem.isActionRunning());
        Assert.assertFalse(automationItem.isActionSuccessful());

        jmri.util.JUnitAppender.assertWarnMessage("Generate CSV Manifest isn't enabled!");

        Setup.setGenerateCsvManifestEnabled(true);

        // does nothing, excel program not configured
        action.doAction();
        Assert.assertFalse(automationItem.isActionRunning());
        Assert.assertFalse(automationItem.isActionSuccessful());

        jmri.util.JUnitAppender.assertWarnMessage(
                "Manifest creator file not found!, directory name: csvManifests, file name: MC4JMRI.xls");

        // load dummy file
        File file = new File(
                InstanceManager.getDefault(OperationsManager.class)
                        .getFile(InstanceManager.getDefault(TrainCustomManifest.class).getDirectoryName()),
                "Test_Excel_File.xls");
        InstanceManager.getDefault(TrainCustomManifest.class).setFileName(file.getName());

        try {
            file.createNewFile();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // confirm file exists
        Assert.assertTrue(InstanceManager.getDefault(TrainCustomManifest.class).excelFileExists());

        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train1 = tmanager.getTrainById("1");
        Assert.assertNotNull(train1);

        // does nothing, no train assignment
        action.doAction();
        Assert.assertFalse(automationItem.isActionRunning());
        Assert.assertFalse(automationItem.isActionSuccessful());

        jmri.util.JUnitAppender.assertWarnMessage("No train selected for custom manifest");

        automationItem.setTrain(train1);
        // does nothing, train isn't built
        action.doAction();
        Assert.assertFalse(automationItem.isActionRunning());
        Assert.assertFalse(automationItem.isActionSuccessful());

        jmri.util.JUnitAppender.assertWarnMessage("Train (STF) needs to be built before creating a custom manifest");
    }

    // private final static Logger log = LoggerFactory.getLogger(RunTrainActionTest.class);

}
