package jmri.jmrit.operations.trains.tools;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class TrainsByCarTypeFrameTest extends OperationsTestCase{

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrainsByCarTypeFrame t = new TrainsByCarTypeFrame();
        t.initComponents("BoxCar");
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(t);
    }
    
    @Test
    public void testTrainsByCarTypeFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // confirm that train default accepts Boxcars
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train t = tmanager.newTrain("Test Train Name 2");
        Assert.assertTrue("accepts Boxcar 1", t.isTypeNameAccepted("Boxcar"));

        TrainsByCarTypeFrame f = new TrainsByCarTypeFrame();
        f.initComponents("Boxcar");

        // remove Boxcar from trains
        JemmyUtil.enterClickAndLeave(f.clearButton);
        JemmyUtil.enterClickAndLeave(f.saveButton);

        Assert.assertFalse("accepts Boxcar 2", t.isTypeNameAccepted("Boxcar"));

        // now add Boxcar to trains
        JemmyUtil.enterClickAndLeave(f.setButton);
        JemmyUtil.enterClickAndLeave(f.saveButton);

        Assert.assertTrue("accepts Boxcar 3", t.isTypeNameAccepted("Boxcar"));

        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testCopyCarType() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // confirm that train default accepts Boxcars
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train = tmanager.newTrain("Test Train Name 3");
        Assert.assertTrue("accepts Boxcar", train.isTypeNameAccepted("Boxcar"));
        Assert.assertTrue("accepts Flatcar", train.isTypeNameAccepted("Flatcar"));

        TrainsByCarTypeFrame f = new TrainsByCarTypeFrame();
        f.initComponents("Boxcar");

        // remove Boxcar from trains
        JemmyUtil.enterClickAndLeave(f.clearButton);
        JemmyUtil.enterClickAndLeave(f.saveButton);
        
        Assert.assertFalse("does not accept Boxcar", train.isTypeNameAccepted("Boxcar"));
        Assert.assertTrue("accepts Flatcar", train.isTypeNameAccepted("Flatcar"));
        
        f.copyComboBox.setSelectedItem("Flatcar");
        JemmyUtil.enterClickAndLeave(f.copyCheckBox);
        
        JemmyUtil.enterClickAndLeaveThreadSafe(f.saveButton); 
        // the save should have opened a dialog window
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("CopyCarTypeTitle"), Bundle.getMessage("ButtonYes"));
        JemmyUtil.waitFor(f);
        
        Assert.assertTrue("accepts Boxcar", train.isTypeNameAccepted("Boxcar"));
        Assert.assertTrue("accepts Flatcar", train.isTypeNameAccepted("Flatcar"));

        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testSelectTrain() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // confirm that train default accepts Boxcars
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        // create two trains
        Train train1 = tmanager.newTrain("Test Train 1");
        Assert.assertTrue("accepts Boxcar", train1.isTypeNameAccepted("Boxcar"));
        Assert.assertTrue("accepts Flatcar", train1.isTypeNameAccepted("Flatcar"));
        
        Train train2 = tmanager.newTrain("Test Train 2");
        Assert.assertTrue("accepts Boxcar", train2.isTypeNameAccepted("Boxcar"));
        Assert.assertTrue("accepts Flatcar", train2.isTypeNameAccepted("Flatcar"));

        TrainsByCarTypeFrame f = new TrainsByCarTypeFrame();
        f.initComponents("Boxcar");

        // remove Boxcar from train1
        JFrameOperator jfo = new JFrameOperator(f);
        JCheckBoxOperator jbo = new JCheckBoxOperator(jfo, "Test Train 1");
        jbo.doClick();           
        JemmyUtil.enterClickAndLeave(f.saveButton);
        
        Assert.assertFalse("does not accept Boxcar", train1.isTypeNameAccepted("Boxcar"));
        Assert.assertTrue("accepts Boxcar", train2.isTypeNameAccepted("Boxcar"));
        Assert.assertTrue("accepts Flatcar", train1.isTypeNameAccepted("Flatcar"));
        
        jbo = new JCheckBoxOperator(jfo, "Test Train 2");
        jbo.doClick();           
        JemmyUtil.enterClickAndLeave(f.saveButton);
        
        Assert.assertFalse("does not accept Boxcar", train1.isTypeNameAccepted("Boxcar"));
        Assert.assertFalse("does not accepts Boxcar", train2.isTypeNameAccepted("Boxcar"));
        Assert.assertTrue("accepts Flatcar", train1.isTypeNameAccepted("Flatcar"));
        
        jbo = new JCheckBoxOperator(jfo, "Test Train 1");
        jbo.doClick();           
        JemmyUtil.enterClickAndLeave(f.saveButton);
        
        Assert.assertTrue("accepts Boxcar", train1.isTypeNameAccepted("Boxcar"));
        Assert.assertFalse("does not accepts Boxcar", train2.isTypeNameAccepted("Boxcar"));
        Assert.assertTrue("accepts Flatcar", train1.isTypeNameAccepted("Flatcar"));

        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testCloseWindowOnSave() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrainsByCarTypeFrame f = new TrainsByCarTypeFrame();
        f.initComponents("Boxcar");
        JUnitOperationsUtil.testCloseWindowOnSave(f.getTitle());
    }

    // private final static Logger log = LoggerFactory.getLogger(TrainsByCarTypeFrameTest.class);

}
