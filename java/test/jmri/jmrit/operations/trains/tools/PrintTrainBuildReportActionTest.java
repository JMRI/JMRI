package jmri.jmrit.operations.trains.tools;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import jmri.util.swing.JemmyUtil;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class PrintTrainBuildReportActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Train train1 = new Train("TESTTRAINID", "TESTTRAINNAME");

        PrintTrainBuildReportAction t = new PrintTrainBuildReportAction("Test Action", true, train1);
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

        PrintTrainBuildReportAction pa = new PrintTrainBuildReportAction("Test Action", true, train1);
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
        }, "wait for dialog to appear");

        JemmyUtil.pressDialogButton(MessageFormat.format(
                Bundle.getMessage("PrintPreviousBuildReport"), new Object[]{"preview"}), Bundle.getMessage("ButtonYes"));

        jmri.util.JUnitUtil.waitFor(() -> {
            return printAction.getState().equals(Thread.State.TERMINATED);
        }, "wait to complete");

        // confirm print preview window is showing
        ResourceBundle rb = ResourceBundle
                .getBundle("jmri.util.UtilBundle");
        JmriJFrame printPreviewFrame = JmriJFrame.getFrame(rb.getString("PrintPreviewTitle") +
                " " +
                MessageFormat.format(Bundle.getMessage("buildReport"),
                        new Object[]{train1.getDescription()}));
        Assert.assertNotNull("exists", printPreviewFrame);

        JUnitUtil.dispose(printPreviewFrame);
    }

    // private final static Logger log = LoggerFactory.getLogger(PrintTrainBuildReportActionTest.class);

}
