package jmri.jmrit.operations.rollingstock.cars.tools;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;

/**
 * @author Paul Bender Copyright (C) 2017
 */
public class CarLoadAttributeActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CarLoadEditFrame f = new CarLoadEditFrame();
        CarLoadAttributeAction t = new CarLoadAttributeAction("Test Action", f);
        Assert.assertNotNull("exists", t);
    }

    @Test
    public void testAction() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CarLoadEditFrame clef = new CarLoadEditFrame();
        clef.initComponents("Boxcar", "");
        CarLoadAttributeAction a = new CarLoadAttributeAction("Test Action", clef);
        Assert.assertNotNull("exists", a);
        
        Assert.assertFalse("toggle state", clef.showQuanity);
        a.actionPerformed(new ActionEvent(this, 0, null));
        Assert.assertTrue("toggle state", clef.showQuanity);

        JmriJFrame f = JmriJFrame.getFrame("Edit Boxcar Loads");
        Assert.assertNotNull("frame exists", f);
        JUnitUtil.dispose(f);
    }

    // private final static Logger log =
    // LoggerFactory.getLogger(CarLoadAttributeActionTest.class);

}
