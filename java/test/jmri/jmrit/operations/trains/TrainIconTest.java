package jmri.jmrit.operations.trains;

import java.awt.GraphicsEnvironment;
import java.awt.Color;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class TrainIconTest {
        
    private jmri.jmrit.display.EditorScaffold editor = null;
    private TrainIcon trainicon = null;

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("exists",trainicon);
    }

    // test TrainIcon attributes
    @Test
    public void testTrainIconAttributes() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Train train1 = new Train("TESTTRAINID", "TESTNAME");
        trainicon.setTrain(train1);
        Assert.assertEquals("TrainIcon set train", "TESTNAME", trainicon.getTrain().getName());
    } 

    @Test
    public void testTrainIconColorChangeAttributes() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // test color change
        for (String color : TrainIcon.getLocoColors()) {
            trainicon.setLocoColor(color);
            if (color.equals(TrainIcon.WHITE)) {
                Assert.assertEquals("White train icon", Color.WHITE, trainicon.getLocoColor());
            }
            if (color.equals(TrainIcon.GREEN)) {
                Assert.assertEquals("Green train icon", Color.GREEN, trainicon.getLocoColor());
            }
            if (color.equals(TrainIcon.GRAY)) {
                Assert.assertEquals("Gray train icon", Color.GRAY, trainicon.getLocoColor());
            }
            if (color.equals(TrainIcon.RED)) {
                Assert.assertEquals("Red train icon", Color.RED, trainicon.getLocoColor());
            }
            if (color.equals(TrainIcon.YELLOW)) {
                Assert.assertEquals("Yellow train icon", Color.YELLOW, trainicon.getLocoColor());
            }
            if (color.equals(TrainIcon.BLUE)) {
                Assert.assertEquals("Blue train icon", TrainIcon.COLOR_BLUE, trainicon.getLocoColor());
            }
        }
    }

    @Test
    public void testNumberColors(){
        // confirm that there are 6 icon colors
        Assert.assertEquals("Six colors", 6, TrainIcon.getLocoColors().length);
    }



    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        if(!GraphicsEnvironment.isHeadless()){
           editor = new jmri.jmrit.display.EditorScaffold();
           trainicon = editor.addTrainIcon("TestName");
        }
    }

    @After
    public void tearDown() {
        if(editor!=null){
           JUnitUtil.dispose(editor);
        }
        editor = null;
        trainicon = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TrainIconTest.class);

}
