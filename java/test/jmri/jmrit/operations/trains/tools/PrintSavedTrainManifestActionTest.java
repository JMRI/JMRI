package jmri.jmrit.operations.trains.tools;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.JUnitOperationsUtil;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.netbeans.jemmy.operators.JFileChooserOperator;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class PrintSavedTrainManifestActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Train train1 = new Train("TESTTRAINID", "TESTTRAINNAME");
        PrintSavedTrainManifestAction t = new PrintSavedTrainManifestAction("Test Action", true, train1);
        Assert.assertNotNull("exists", t);
    }

    @Test
    public void testPrintAction() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JUnitOperationsUtil.initOperationsData();
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train1 = tmanager.getTrainById("1");
        Assert.assertNotNull(train1);

        // create a manifest
        Setup.setSaveTrainManifestsEnabled(true);
        Assert.assertTrue(train1.build());
        train1.terminate();

        PrintSavedTrainManifestAction pa = new PrintSavedTrainManifestAction("Test Action", true, train1);
        Assert.assertNotNull("exists", pa);

        // should cause file chooser to appear
        Thread printAction = new Thread(new Runnable() {
            @Override
            public void run() {
                pa.actionPerformed(new ActionEvent(this, 0, null));
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
    }

    // private final static Logger log = LoggerFactory.getLogger(PrintSavedTrainManifestActionTest.class);

}
