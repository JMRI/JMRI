package jmri.jmrit.operations.rollingstock.cars.tools;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class CarDeleteAttributeActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CarAttributeEditFrame f = new CarAttributeEditFrame();
        CarDeleteAttributeAction t = new CarDeleteAttributeAction(f);
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testAction() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CarAttributeEditFrame cef = new CarAttributeEditFrame();
        cef.initComponents(CarAttributeEditFrame.ROAD);
        CarDeleteAttributeAction a = new CarDeleteAttributeAction(cef);
        Assert.assertNotNull("exists", a);
        
        //TODO check that the delete worked
        
        JmriJFrame f = JmriJFrame.getFrame("Edit Car Road");
        Assert.assertNotNull("frame exists", f);
        JUnitUtil.dispose(f);
    }

    // private final static Logger log = LoggerFactory.getLogger(CarDeleteAttributeActionTest.class);

}
