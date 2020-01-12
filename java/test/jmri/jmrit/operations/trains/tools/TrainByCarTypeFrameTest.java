package jmri.jmrit.operations.trains.tools;

import java.awt.GraphicsEnvironment;

import org.junit.*;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class TrainByCarTypeFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrainByCarTypeFrame t = new TrainByCarTypeFrame((Train) null);
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(t);
    }

    @Test
    public void testSelection() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Train train = InstanceManager.getDefault(TrainManager.class).getTrainById("1");
        Assert.assertNotNull("exists", train);

        TrainByCarTypeFrame t = new TrainByCarTypeFrame(train);
        Assert.assertNotNull("exists", t);

        t.typeComboBox.setSelectedIndex(1);
        t.carsComboBox.setSelectedIndex(1);

        JUnitUtil.dispose(t);
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        super.setUp();

        JUnitOperationsUtil.initOperationsData();
    }

    @Override
    @After
    public void tearDown() {
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TrainByCarTypeFrameTest.class);

}
