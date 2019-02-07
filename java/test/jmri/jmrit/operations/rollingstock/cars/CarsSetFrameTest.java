package jmri.jmrit.operations.rollingstock.cars;

import java.awt.GraphicsEnvironment;
import javax.swing.JTable;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class CarsSetFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CarsSetFrame t = new CarsSetFrame();
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(t);
    }

    @Test
    public void testCarsSetFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();

        // create cars table
        CarsSetFrame f = new CarsSetFrame();
        CarsTableFrame ctf = new CarsTableFrame(true, null, null);
        JTable ctm = ctf.carsTable;
        f.initComponents(ctm);
        f.setTitle("Test Cars Set Frame");

        CarManager cManager = InstanceManager.getDefault(CarManager.class);
        Car c888 = cManager.getByRoadAndNumber("CP", "888");
        Assert.assertNotNull("car exists", c888);
        f.loadCar(c888);

        JUnitUtil.dispose(ctf);
        JUnitUtil.dispose(f);
    }

    @Test
    public void testCarsSetFrameApplyButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();

        // create cars table
        CarsSetFrame f = new CarsSetFrame();
        CarsTableFrame ctf = new CarsTableFrame(true, null, null);
        JTable ctm = ctf.carsTable;
        f.initComponents(ctm);
        f.setTitle("Test Cars Set Frame");

        // Save button is labeled "Apply"
        JemmyUtil.enterClickAndLeave(f.saveButton);

        // no cars selected dialog should appear
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("carNoneSelected"), Bundle.getMessage("ButtonOK"));

        JUnitUtil.dispose(ctf);
        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testCarsSetFrameApplyButtonWithSelections() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();

        // get a cars table with data
        CarsSetFrame f = new CarsSetFrame();
        CarsTableFrame ctf = new CarsTableFrame(true, null, null);
        JTable ctm = ctf.carsTable;
        
        // select the first three cars CP 99, CP 777, and CP 888
        ctm.setRowSelectionInterval(0, 2);
        f.initComponents(ctm);
        f.setTitle("Test Cars Set Frame");
        
        CarManager cManager = InstanceManager.getDefault(CarManager.class);
        Car c99 = cManager.getByRoadAndNumber("CP", "99");
        Assert.assertNotNull("car exists", c99);
        Car c777 = cManager.getByRoadAndNumber("CP", "777");
        Assert.assertNotNull("car exists", c777);
        Car c888 = cManager.getByRoadAndNumber("CP", "888");
        Assert.assertNotNull("car exists", c888);
        
        // check defaults
        Assert.assertFalse("Out of service", c99.isOutOfService());
        Assert.assertFalse("Out of service", c777.isOutOfService());
        Assert.assertFalse("Out of service", c888.isOutOfService());

        // change the 3 car's status
        JemmyUtil.enterClickAndLeave(f.outOfServiceCheckBox);
        // Save button is labeled "Apply"
        JemmyUtil.enterClickAndLeave(f.saveButton);
        
        // confirm
        Assert.assertTrue("Out of service", c99.isOutOfService());
        Assert.assertTrue("Out of service", c777.isOutOfService());
        Assert.assertTrue("Out of service", c888.isOutOfService());

        JUnitUtil.dispose(ctf);
        JUnitUtil.dispose(f);
    }

    // private final static Logger log = LoggerFactory.getLogger(CarsSetFrameTest.class);

}
