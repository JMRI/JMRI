package jmri.jmrit.operations.trains.gui;

import java.awt.GraphicsEnvironment;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.trains.Train;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Daniel Boudreau Copyright (C) 2026
 */
public class ShowCarsByLocationActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Train train = new Train("TESTTRAINID", "TESTTRAINNAME");
        TrainConductorFrame tcf = new TrainConductorFrame(train);
        ShowCarsByLocationAction t = new ShowCarsByLocationAction((TrainConductorPanel) tcf.getContentPane());
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(tcf);
    }
}
