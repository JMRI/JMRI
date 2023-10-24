package jmri.jmrit.operations.trains.tools;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.trains.TrainsTableFrame;
import jmri.util.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class PrintTrainsActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrainsTableFrame ttf = new TrainsTableFrame();
        PrintTrainsAction t = new PrintTrainsAction(true, ttf);
        Assert.assertNotNull("exists", t);
        
        JUnitUtil.dispose(ttf);
        
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }
    
    @Test
    public void testPrintAction() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JUnitOperationsUtil.initOperationsData();

        TrainsTableFrame ttf = new TrainsTableFrame();
        PrintTrainsAction pta = new PrintTrainsAction(true, ttf);
        Assert.assertNotNull("exists", pta);

        pta.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));

        // confirm print preview window is showing
        JmriJFrame printPreviewFrame = JmriJFrame.getFrame(Bundle.getMessage("MenuItemPreview"));

        Assert.assertNotNull("exists", printPreviewFrame);

        JUnitUtil.dispose(printPreviewFrame);
        JUnitUtil.dispose(ttf);
        
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }
}
