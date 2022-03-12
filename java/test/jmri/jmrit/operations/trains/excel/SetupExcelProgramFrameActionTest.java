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
 * @author Paul Bender Copyright (C) 2017
 */
public class SetupExcelProgramFrameActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        SetupExcelProgramFrameAction t = new SetupExcelProgramFrameAction();
        Assert.assertNotNull("exists", t);
    }

    @Test
    public void testAction() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SetupExcelProgramFrameAction sepfa = new SetupExcelProgramFrameAction();
        Assert.assertNotNull("exists", sepfa);
        
        sepfa.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
        
        JmriJFrame excelFrame = JmriJFrame.getFrame(Bundle.getMessage("MenuItemSetupExcelProgram"));
        Assert.assertNotNull("exists", excelFrame);
        JUnitUtil.dispose(excelFrame);
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }
}
