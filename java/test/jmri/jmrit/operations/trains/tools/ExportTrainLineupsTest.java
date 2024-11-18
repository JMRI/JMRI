package jmri.jmrit.operations.trains.tools;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.JUnitOperationsUtil;
import jmri.util.swing.JemmyUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ExportTrainLineupsTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        ExportTrainLineups t = new ExportTrainLineups();
        Assert.assertNotNull("exists", t);
    }

    @Test
    @jmri.util.junit.annotations.DisabledIfHeadless
    public void testCreateFile() {
        ExportTrainLineups exportTrains = new ExportTrainLineups();
        Assert.assertNotNull("exists", exportTrains);

        JUnitOperationsUtil.initOperationsData();

        // built trains increase coverage
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train = tmanager.getTrainByName("STF");
        Assert.assertTrue(train.build());

        // next should cause export complete dialog to appear
        Thread export = new Thread(exportTrains::writeOperationsTrainsFile);
        export.setName("Export Trains"); // NOI18N
        export.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return export.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("ExportComplete"), Bundle.getMessage("ButtonOK"));

        jmri.util.JUnitUtil.waitFor(() -> !export.isAlive(), "wait for export to complete");

        java.io.File file = new java.io.File(ExportTrainLineups.defaultOperationsFilename());
        Assert.assertTrue("Confirm file creation", file.exists());

        JUnitOperationsUtil.checkOperationsShutDownTask();

    }

    // private final static Logger log = LoggerFactory.getLogger(ExportTrainsTest.class);

}
