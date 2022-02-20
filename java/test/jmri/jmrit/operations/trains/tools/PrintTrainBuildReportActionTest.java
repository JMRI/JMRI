package jmri.jmrit.operations.trains.tools;

import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

import org.junit.Assert;
import org.junit.jupiter.api.* ;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import org.netbeans.jemmy.operators.JFrameOperator;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class PrintTrainBuildReportActionTest extends OperationsTestCase {

    @Test
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    public void testCTor() {
        Train train1 = new Train("TESTTRAINID", "TESTTRAINNAME");

        PrintTrainBuildReportAction t = new PrintTrainBuildReportAction(true, train1);
        Assert.assertNotNull("exists", t);
    }

    @RepeatedTest(50)
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    public void testPrintAction() {

        JUnitOperationsUtil.initOperationsData();
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train1 = tmanager.getTrainById("1");
        Assert.assertNotNull(train1);

        // create a build report
        Assert.assertTrue(train1.build());
        train1.terminate(); // this will cause dialog window to appear

        PrintTrainBuildReportAction pa = new PrintTrainBuildReportAction(true, train1);
        Assert.assertNotNull("exists", pa);

        Thread t1 = new Thread(() -> {
            JemmyUtil.pressDialogButton(MessageFormat.format(
                Bundle.getMessage("PrintPreviousBuildReport"), new Object[]{"preview"}), Bundle.getMessage("ButtonYes"));
        });
        t1.setName("click PrintPreviousBuildReport preview Yes Thread 1");
        t1.start();

        // should cause file chooser to appear
        jmri.util.ThreadingUtil.runOnGUI(() -> {
            pa.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
        });

        JUnitUtil.waitFor(() -> {
            return !t1.isAlive();
        }, "wait for dialog to complete");

        // confirm print preview window is showing
        ResourceBundle rb = ResourceBundle.getBundle("jmri.util.UtilBundle");
        String frameTitle = rb.getString("PrintPreviewTitle") +
                " " +
                MessageFormat.format(Bundle.getMessage("buildReport"),
                        new Object[]{train1.getDescription()});
        
        JFrameOperator jfo = new JFrameOperator( frameTitle ); // waits for frame to appear
        
        jfo.requestClose();
        jfo.waitClosed();
        
        JUnitOperationsUtil.checkOperationsShutDownTask();

    }

    // private final static Logger log = LoggerFactory.getLogger(PrintTrainBuildReportActionTest.class);

}
