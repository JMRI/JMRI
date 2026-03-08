package jmri.jmrit.display.palette;

import jmri.Sensor;
import jmri.jmrit.display.DisplayFrame;
import jmri.jmrit.display.EditorScaffold;
import jmri.jmrit.picker.PickListModel;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class MultiSensorIconDialogTest {

    @Test
    @DisabledIfHeadless
    public void testCTor() {
        PickListModel<Sensor> tableModel = PickListModel.sensorPickModelInstance(); // NOI18N
        EditorScaffold editor = new EditorScaffold("ED");
        DisplayFrame df = new DisplayFrame("Indicator TO Icon Dialog Test", editor); // NOI18N
        MultiSensorItemPanel mip = new MultiSensorItemPanel(df,"IS01","",tableModel);
        MultiSensorIconDialog t = new MultiSensorIconDialog("MultiSensor","MultiSensor",mip); // NOI18N
        Assertions.assertNotNull(t,"exists");
        JUnitUtil.dispose(t);
        JUnitUtil.dispose(df);
        JUnitUtil.dispose(df);
        JUnitUtil.dispose(editor);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(MultiSensorIconDialogTest.class);

}
