package jmri.jmrit.operations.trains.tools;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainEditFrame;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;

/**
 * @author Paul Bender Copyright (C) 2017
 */
public class TrainManifestOptionActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Train train1 = new Train("TESTTRAINID", "TESTTRAINNAME");
        TrainEditFrame tef = new TrainEditFrame(train1);
        TrainManifestOptionAction t = new TrainManifestOptionAction("Test Action", tef);
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(tef);
    }

    @Test
    public void testAction() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Train train1 = new Train("TESTTRAINID", "TESTTRAINNAME");
        TrainEditFrame tef = new TrainEditFrame(train1);
        TrainManifestOptionAction a = new TrainManifestOptionAction("Test Action", tef);
        Assert.assertNotNull("exists", a);

        a.actionPerformed(new ActionEvent(this, 0, null));

        JmriJFrame f = JmriJFrame.getFrame(Bundle.getMessage("TitleOptions"));
        Assert.assertNotNull("exists", f);
        JUnitUtil.dispose(f);
    }

    // private final static Logger log =
    // LoggerFactory.getLogger(TrainManifestOptionActionTest.class);

}
