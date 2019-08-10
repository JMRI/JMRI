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
import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class PrintTrainManifestActionTest extends OperationsTestCase {

    @Rule
    public jmri.util.junit.rules.RetryRule retryRule = new jmri.util.junit.rules.RetryRule(3);  // allow 3 retries

    @Rule // This test class was periodically stalling and causing the CI run to time out. Limit its duration.
	public org.junit.rules.Timeout globalTimeout = org.junit.rules.Timeout.seconds(20);

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Train train1 = new Train("TESTTRAINID", "TESTTRAINNAME");
        PrintTrainManifestAction t = new PrintTrainManifestAction("Test Action",true, train1);
        Assert.assertNotNull("exists",t);
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

        PrintTrainManifestAction pa = new PrintTrainManifestAction("Test Action", true, train1);
        Assert.assertNotNull("exists", pa);

        // should cause dialog window to appear
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
                " " + train1.getDescription());
        Assert.assertNotNull("exists", printPreviewFrame);

        JUnitUtil.dispose(printPreviewFrame);
    }

    // private final static Logger log = LoggerFactory.getLogger(PrintTrainManifestActionTest.class);

}
