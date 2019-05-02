package jmri.jmrit.operations.trains.tools;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainEditFrame;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class PrintTrainActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Train train1 = new Train("TESTTRAINID", "TESTTRAINNAME");
        TrainEditFrame tef = new TrainEditFrame(train1);
        PrintTrainAction t = new PrintTrainAction("Test Action", true, tef);
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(tef);
    }

    @Test
    public void testPrintAction() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JUnitOperationsUtil.initOperationsData();
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train1 = tmanager.getTrainById("1");
        Assert.assertNotNull(train1);

        TrainEditFrame tef = new TrainEditFrame(train1);
        PrintTrainAction pta = new PrintTrainAction("Test Action", true, tef);
        Assert.assertNotNull("exists", pta);

        pta.actionPerformed(new ActionEvent(this, 0, null));

        // confirm print preview window is showing
        ResourceBundle rb = ResourceBundle
                .getBundle("jmri.util.UtilBundle");
        JmriJFrame printPreviewFrame = JmriJFrame.getFrame(rb.getString("PrintPreviewTitle") +
                " " +
                MessageFormat.format(Bundle.getMessage("TitleTrain"),
                        new Object[]{train1.getName()}));
        Assert.assertNotNull("exists", printPreviewFrame);

        JUnitUtil.dispose(printPreviewFrame);
        JUnitUtil.dispose(tef);
    }

    // private final static Logger log = LoggerFactory.getLogger(PrintTrainActionTest.class);

}
