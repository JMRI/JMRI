package jmri.jmrit.operations.trains.tools;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import jmri.util.junit.rules.RetryRule;
import jmri.util.swing.JemmyUtil;

/**
 * @author Paul Bender Copyright (C) 2017
 */
@Timeout(20)
public class PrintTrainManifestActionTest extends OperationsTestCase {

    public RetryRule retryRule = new RetryRule(3); // allow 3 retries

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Train train1 = new Train("TESTTRAINID", "TESTTRAINNAME");
        PrintTrainManifestAction t = new PrintTrainManifestAction(true, train1);
        Assert.assertNotNull("exists", t);
    }

    @Test
    public void testPrintAction() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JUnitOperationsUtil.initOperationsData();
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train1 = tmanager.getTrainById("1");
        Assert.assertNotNull(train1);

        // create a build report
        Assert.assertTrue(train1.build());
        train1.terminate(); // this will cause dialog window to appear

        PrintTrainManifestAction pa = new PrintTrainManifestAction(true, train1);
        Assert.assertNotNull("exists", pa);

        // should cause dialog window to appear
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
        }, "wait for dialog to appear");

        // preview previous manifest?
        JemmyUtil.pressDialogButton(MessageFormat.format(
                Bundle.getMessage("PrintPreviousManifest"), new Object[]{"preview"}), Bundle.getMessage("ButtonYes"));

        try {
            printAction.join();
        } catch (InterruptedException e) {
            // do nothing
        }

        // confirm print preview window is showing
        ResourceBundle rb = ResourceBundle
                .getBundle("jmri.util.UtilBundle");
        JmriJFrame printPreviewFrame = JmriJFrame.getFrame(rb.getString("PrintPreviewTitle") +
                " " +
                train1.getDescription());
        Assert.assertNotNull("exists", printPreviewFrame);

        JUnitUtil.dispose(printPreviewFrame);

        JUnitOperationsUtil.checkOperationsShutDownTask();

    }

    // private final static Logger log =
    // LoggerFactory.getLogger(PrintTrainManifestActionTest.class);

}
