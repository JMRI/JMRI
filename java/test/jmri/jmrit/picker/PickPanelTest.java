package jmri.jmrit.picker;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class PickPanelTest {

    @Test
    @DisabledIfHeadless
    public void testCTor() {
        PickListModel<?>[] models = {
            PickListModel.turnoutPickModelInstance(),
            PickListModel.sensorPickModelInstance(),
            PickListModel.multiSensorPickModelInstance(),
            PickListModel.signalHeadPickModelInstance(),
            PickListModel.signalMastPickModelInstance(),
            PickListModel.memoryPickModelInstance(),
            PickListModel.blockPickModelInstance(),
            PickListModel.reporterPickModelInstance(),
            PickListModel.lightPickModelInstance(),
            PickListModel.oBlockPickModelInstance(),
            PickListModel.warrantPickModelInstance(),
            PickListModel.entryExitPickModelInstance(),
            PickListModel.logixPickModelInstance()
        };
        PickPanel t = new PickPanel(models);
        Assertions.assertNotNull(t,"exists");
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
}
