package jmri.jmrit.operations.locations.tools;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.util.*;

/**
 * @author Paul Bender Copyright (C) 2017
 */
public class PrintLocationsByCarTypesActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        PrintLocationsByCarTypesAction t = new PrintLocationsByCarTypesAction(true);
        Assert.assertNotNull("exists", t);
    }

    @Test
    public void testPrintPreview() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();
        PrintLocationsByCarTypesAction pla = new PrintLocationsByCarTypesAction(true);
        Assert.assertNotNull("exists", pla);
        pla.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));

        ResourceBundle rb = ResourceBundle
                .getBundle("jmri.util.UtilBundle");
        JmriJFrame f = JmriJFrame
                .getFrame(rb.getString("PrintPreviewTitle") + " " + Bundle.getMessage("TitleLocationsByType"));
        Assert.assertNotNull("exists", f);
        JUnitUtil.dispose(f);
    }

}
