package jmri.jmrit.picker;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class PickPanelTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        PickListModel[] models = {
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
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
