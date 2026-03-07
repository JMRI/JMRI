package jmri.jmrit.display.palette;

import jmri.jmrit.display.DisplayFrame;
import jmri.jmrit.picker.PickListModel;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class MultiSensorItemPanelTest {

    @Test
    @DisabledIfHeadless
    public void testCTor() {
        PickListModel<jmri.Sensor> tableModel = PickListModel.sensorPickModelInstance();
        DisplayFrame df = new DisplayFrame("MultiSensor Item Panel Test");
        MultiSensorItemPanel t = new MultiSensorItemPanel(df,"IS01","",tableModel);
        Assertions.assertNotNull(t,"exists");
        JUnitUtil.dispose(df);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(MultiSensorItemPanelTest.class);

}
