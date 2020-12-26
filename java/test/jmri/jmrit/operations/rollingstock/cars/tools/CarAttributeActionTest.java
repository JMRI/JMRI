package jmri.jmrit.operations.rollingstock.cars.tools;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;

/**
 * @author Paul Bender Copyright (C) 2017
 */
public class CarAttributeActionTest extends OperationsTestCase {

    @Test
    public void testAction() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CarAttributeEditFrame cef = new CarAttributeEditFrame();
        cef.initComponents(CarAttributeEditFrame.ROAD);
        CarAttributeAction a = new CarAttributeAction(cef);
        Assert.assertNotNull("exists", a);
        
        Assert.assertFalse("toggle state", cef.showQuanity);
        a.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
        Assert.assertTrue("toggle state", cef.showQuanity);
        
        JmriJFrame f = JmriJFrame.getFrame("Edit Car Road");
        Assert.assertNotNull("frame exists", f);
        JUnitUtil.dispose(f);
    }
}
