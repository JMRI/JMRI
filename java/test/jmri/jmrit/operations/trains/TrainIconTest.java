package jmri.jmrit.operations.trains;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class TrainIconTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        jmri.jmrit.display.EditorScaffold es = new jmri.jmrit.display.EditorScaffold();
        TrainIcon t = new TrainIcon(es);
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(es);
    }

    // test TrainIcon attributes
    @Test
    public void testTrainIconAttributes() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Train train1 = new Train("TESTTRAINID", "TESTNAME");

        jmri.jmrit.display.panelEditor.PanelEditor editor = new jmri.jmrit.display.panelEditor.PanelEditor(
                "Test Panel");
        TrainIcon trainicon1 = editor.addTrainIcon("TestName");
        trainicon1.setTrain(train1);
        Assert.assertEquals("TrainIcon set train", "TESTNAME", trainicon1.getTrain().getName());
        editor.getTargetFrame().dispose();
    }

    @Test
    public void testTrainIconColorChangeAttributes() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Train train1 = new Train("TESTTRAINID", "TESTNAME");

        jmri.jmrit.display.panelEditor.PanelEditor editor = new jmri.jmrit.display.panelEditor.PanelEditor(
                "Test Panel");
        TrainIcon trainicon1 = editor.addTrainIcon("TestName");
        trainicon1.setTrain(train1);

        // test color change
        String[] colors = TrainIcon.getLocoColors();
        for (int i = 0; i < colors.length; i++) {
            trainicon1.setLocoColor(colors[i]);
        }
        editor.getTargetFrame().dispose();
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TrainIconTest.class);

}
