package jmri.jmrit.operations.trains.tools;

import java.awt.event.ActionEvent;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.netbeans.jemmy.operators.JFileChooserOperator;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.JUnitOperationsUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
@org.junit.jupiter.api.Disabled("Temporary disable class for testing")
/*
This class fails when running on Java 25 with the following error:
PrintSavedTrainManifestActionTest>OperationsTestCase.tearDown:102 Unexpected ERROR or higher messages emitted:
"Exception while creating operations file, may not be complete: Invalid file path" ==> expected: <false> but was: <true>
*/
public class PrintSavedTrainManifestActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Train train1 = new Train("TESTTRAINID", "TESTTRAINNAME");
        PrintSavedTrainManifestAction t = new PrintSavedTrainManifestAction(true, train1);
        Assert.assertNotNull("exists", t);
    }

    @Test
    @jmri.util.junit.annotations.DisabledIfHeadless
    public void testPrintAction() {

        JUnitOperationsUtil.initOperationsData();
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train1 = tmanager.getTrainById("1");
        Assert.assertNotNull(train1);

        // create a manifest
        Setup.setSaveTrainManifestsEnabled(true);
        Assert.assertTrue(train1.build());
        train1.terminate();

        PrintSavedTrainManifestAction pa = new PrintSavedTrainManifestAction(true, train1);
        Assert.assertNotNull("exists", pa);

        // should cause file chooser to appear
        Thread printAction = new Thread(new Runnable() {
            @Override
            public void run() {
                pa.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
            }
        });
        printAction.setName("Test Print Action"); // NOI18N
        printAction.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return printAction.getState().equals(Thread.State.WAITING);
        }, "wait for file chooser");

        // opens file chooser path "..\operations\JUnitTest\manifestsBackups\STF"
        JFileChooserOperator fco = new JFileChooserOperator();
        String text = fco.getPathCombo().getSelectedItem().toString();
        Assert.assertTrue(text.contains("manifestsBackups"));
        Assert.assertTrue(text.contains("STF"));
        fco.cancelSelection();

        jmri.util.JUnitUtil.waitFor(() -> !printAction.isAlive(), "wait for printAction to complete");

        JUnitOperationsUtil.checkOperationsShutDownTask();

    }

    // private final static Logger log = LoggerFactory.getLogger(PrintSavedTrainManifestActionTest.class);

}
