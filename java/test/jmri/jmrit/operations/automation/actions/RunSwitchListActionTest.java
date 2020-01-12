package jmri.jmrit.operations.automation.actions;

import java.io.File;
import java.io.IOException;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.automation.AutomationItem;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.excel.TrainCustomSwitchList;
import jmri.util.JUnitOperationsUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class RunSwitchListActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        RunSwitchListAction t = new RunSwitchListAction();
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testActionNoAutomationItem() {
        RunSwitchListAction action = new RunSwitchListAction();
        Assert.assertNotNull("exists", action);
        // does nothing, no automationItem
        action.doAction();
    }

    @Test
    public void testGetActionName() {
        RunSwitchListAction action = new RunSwitchListAction();
        Assert.assertEquals("name", Bundle.getMessage("RunSwitchList"), action.getName());
    }

    @Test
    public void testIsMessageOkEnabled() {
        RunSwitchListAction action = new RunSwitchListAction();
        Assert.assertTrue(action.isMessageOkEnabled());
    }

    @Test
    public void testIsMessageFailEnabled() {
        RunSwitchListAction action = new RunSwitchListAction();
        Assert.assertTrue(action.isMessageFailEnabled());
    }

    @Test
    public void testIsConcurrentAction() {
        RunSwitchListAction action = new RunSwitchListAction();
        Assert.assertFalse(action.isConcurrentAction());
    }

    @Test
    public void testRunSwitchListActionFailures() {

        JUnitOperationsUtil.initOperationsData();

        RunSwitchListAction action = new RunSwitchListAction();
        Assert.assertNotNull("exists", action);
        AutomationItem automationItem = new AutomationItem("TestId");
        automationItem.setAction(action);
        Assert.assertEquals("confirm registered", automationItem, action.getAutomationItem());

        // confirm that default is not enabled
        Assert.assertFalse(Setup.isGenerateCsvSwitchListEnabled());

        // does nothing, not enabled
        action.doAction();
        Assert.assertFalse(automationItem.isActionRunning());
        Assert.assertFalse(automationItem.isActionSuccessful());

        jmri.util.JUnitAppender.assertWarnMessage("Generate CSV Switch List isn't enabled!");

        Setup.setGenerateCsvSwitchListEnabled(true);

        // does nothing, excel program not configured
        action.doAction();
        Assert.assertFalse(automationItem.isActionRunning());
        Assert.assertFalse(automationItem.isActionSuccessful());

        jmri.util.JUnitAppender.assertWarnMessage(
                "Manifest creator file not found!, directory name: csvSwitchLists, file name: MC4JMRI.xls");

        // load dummy file
        File file = new File(
                InstanceManager.getDefault(OperationsManager.class)
                        .getFile(InstanceManager.getDefault(TrainCustomSwitchList.class).getDirectoryName()),
                "Test_Excel_File.xls");
        InstanceManager.getDefault(TrainCustomSwitchList.class).setFileName(file.getName());

        try {
            file.createNewFile();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // confirm file exists
        Assert.assertTrue(InstanceManager.getDefault(TrainCustomSwitchList.class).excelFileExists());
    }

    // private final static Logger log = LoggerFactory.getLogger(RunSwitchListActionTest.class);

}
