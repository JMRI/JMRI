package jmri.jmrit.display.palette;

import java.awt.GraphicsEnvironment;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.ShutDownManager;
import jmri.jmrit.display.DisplayFrame;
import jmri.jmrit.display.EditorScaffold;
import jmri.jmrit.picker.PickListModel;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class MultiSensorIconDialogTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        PickListModel<Sensor> tableModel = PickListModel.sensorPickModelInstance(); // NOI18N
        EditorScaffold editor = new EditorScaffold("ED");
        DisplayFrame df = new DisplayFrame("Indicator TO Icon Dialog Test", editor); // NOI18N
        MultiSensorItemPanel mip = new MultiSensorItemPanel(df,"IS01","",tableModel);
        MultiSensorIconDialog t = new MultiSensorIconDialog("MultiSensor","MultiSensor",mip,null); // NOI18N
        Assert.assertNotNull("exists",t); // NOI18N
        JUnitUtil.dispose(t);
        JUnitUtil.dispose(df);
        JUnitUtil.dispose(df);
        JUnitUtil.dispose(editor);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(MultiSensorIconDialogTest.class);

}
