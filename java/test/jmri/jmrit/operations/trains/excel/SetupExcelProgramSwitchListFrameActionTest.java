package jmri.jmrit.operations.trains.excel;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SetupExcelProgramSwitchListFrameActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        SetupExcelProgramSwitchListFrameAction t = new SetupExcelProgramSwitchListFrameAction();
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testAction() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SetupExcelProgramSwitchListFrameAction sepfa = new SetupExcelProgramSwitchListFrameAction();
        Assert.assertNotNull("exists", sepfa);
        
        sepfa.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
        
        JmriJFrame excelFrame = JmriJFrame.getFrame(Bundle.getMessage("MenuItemSetupExcelProgramSwitchList"));
        Assert.assertNotNull("frame exists", excelFrame);
        JUnitUtil.dispose(excelFrame);
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }
    
    // private final static Logger log = LoggerFactory.getLogger(SetupExcelProgramSwitchListFrameActionTest.class);

}
