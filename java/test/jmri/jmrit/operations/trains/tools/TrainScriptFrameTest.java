package jmri.jmrit.operations.trains.tools;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.trains.*;
import jmri.jmrit.operations.trains.gui.TrainEditFrame;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class TrainScriptFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        JUnitOperationsUtil.initOperationsData();
        
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train = tmanager.getTrainByName("STF");
        
        TrainScriptFrame t = new TrainScriptFrame();
        TrainEditFrame tef = new TrainEditFrame(train);
        Assert.assertNotNull("exists",t);
        t.initComponents(tef);
        
        JUnitUtil.dispose(t);
        JUnitUtil.dispose(tef);
    }
    
    @Test
    public void testCloseWindowOnSave() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrainScriptFrame f = new TrainScriptFrame();
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train = tmanager.newTrain("Test");
        TrainEditFrame trainEditFrame = new TrainEditFrame(train);
        f.initComponents(trainEditFrame);
        JUnitOperationsUtil.testCloseWindowOnSave(f.getTitle());
    }

    // private final static Logger log = LoggerFactory.getLogger(TrainScriptFrameTest.class);

}
