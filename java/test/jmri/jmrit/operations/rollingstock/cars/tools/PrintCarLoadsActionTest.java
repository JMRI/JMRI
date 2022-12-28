package jmri.jmrit.operations.rollingstock.cars.tools;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.rollingstock.cars.CarLoads;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class PrintCarLoadsActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        PrintCarLoadsAction t = new PrintCarLoadsAction(true);
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testAction() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // create some car types and loads
        CarTypes ct = InstanceManager.getDefault(CarTypes.class);
        CarLoads cl = InstanceManager.getDefault(CarLoads.class);
        ct.addName("Flat");
        ct.addName("Boxcar");
        cl.addName("Boxcar", "TestLoad1");
        cl.addName("Boxcar", "TestLoad2");
        cl.setHazardous("Boxcar", "TestLoad2", true);
        
        PrintCarLoadsAction pcla = new PrintCarLoadsAction(true);
        Assert.assertNotNull("exists",pcla);
        pcla.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));

        ResourceBundle rb = ResourceBundle
                .getBundle("jmri.util.UtilBundle");
        JmriJFrame f = JmriJFrame
                .getFrame(rb.getString("PrintPreviewTitle") + " " + Bundle.getMessage("TitleCarLoads"));
        Assert.assertNotNull("exists", f);
        JUnitUtil.dispose(f);
    }

}
