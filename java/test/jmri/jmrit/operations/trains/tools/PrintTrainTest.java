package jmri.jmrit.operations.trains.tools;

import java.awt.GraphicsEnvironment;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class PrintTrainTest extends OperationsTestCase {

    @Test
    public void testPrint() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JUnitOperationsUtil.initOperationsData();
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train1 = tmanager.getTrainById("1");
        Assert.assertNotNull(train1);

        PrintTrainsFrame pta = new PrintTrainsFrame(true, train1);
        Assert.assertNotNull("exists", pta);

        // confirm print preview window is showing
        ResourceBundle rb = ResourceBundle
                .getBundle("jmri.util.UtilBundle");
        JmriJFrame printPreviewFrame = JmriJFrame.getFrame(rb.getString("PrintPreviewTitle") +
                " " +
                MessageFormat.format(Bundle.getMessage("TitleTrain"),
                        new Object[]{train1.getName()}));
        Assert.assertNotNull("exists", printPreviewFrame);

        JUnitUtil.dispose(printPreviewFrame);
    }

    // private final static Logger log = LoggerFactory.getLogger(PrintTrainActionTest.class);

}
