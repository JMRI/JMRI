package jmri.jmrit.display.palette;

import java.awt.GraphicsEnvironment;

import jmri.jmrit.display.DisplayFrame;
import jmri.jmrit.picker.PickListModel;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class MultiSensorItemPanelTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        PickListModel<jmri.Sensor> tableModel = PickListModel.sensorPickModelInstance();
        DisplayFrame df = new DisplayFrame("MultiSensor Item Panel Test");
        MultiSensorItemPanel t = new MultiSensorItemPanel(df,"IS01","",tableModel);
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(df);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(MultiSensorItemPanelTest.class);

}
