package jmri.jmrit.display.layoutEditor;

import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.io.File;
import jmri.*;
import jmri.jmrit.display.*;
import jmri.util.*;
import jmri.util.junit.rules.*;
import jmri.util.swing.JemmyUtil;
import org.junit.*;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.netbeans.jemmy.*;
import org.netbeans.jemmy.operators.JMenuOperator;

/**
 * Test simple functioning of LayoutEditor.
 *
 * @author Paul Bender Copyright (C) 2016
 * @author George Warner Copyright (C) 2019
 */
public class LayoutEditorTest extends AbstractEditorTestBase<LayoutEditor> {

    private EditorFrameOperator jfo;

    @Rule
    public Timeout globalTimeout = Timeout.seconds(10); // 10 second timeout for methods in this test class.

    @Rule
    public RetryRule retryRule = new RetryRule(3); // allow 3 retries

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initLayoutBlockManager();
        if (!GraphicsEnvironment.isHeadless()) {
            e = new LayoutEditor("Layout Editor Test Layout");
            e.setVisible(true);
            jfo = new EditorFrameOperator(e);
        }
    }

    @After
    @Override
    public void tearDown() {
        if (e != null) {
            jfo.closeFrameWithConfirmations();
            e = null;
        }
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    @Test
    public void testStringCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor("Layout Editor Test Layout");
        Assert.assertNotNull("exists", e);
        JUnitUtil.dispose(e);
    }

    @Test
    public void testDefaultCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor(); // create layout editor
        Assert.assertNotNull("exists", e);
        JUnitUtil.dispose(e);
    }

       // *****************************************************************
       //    Turntable Ray support
       // *****************************************************************
       
    @Test
    public void testHPTturntableTrackIndexOK() {
        Assert.assertEquals(0, LayoutEditor.HitPointType.TURNTABLE_RAY_0.turntableTrackIndex());
        Assert.assertEquals(1, LayoutEditor.HitPointType.TURNTABLE_RAY_1.turntableTrackIndex());
        Assert.assertEquals(2, LayoutEditor.HitPointType.TURNTABLE_RAY_2.turntableTrackIndex());
        // ..
        Assert.assertEquals(62, LayoutEditor.HitPointType.TURNTABLE_RAY_62.turntableTrackIndex());
        Assert.assertEquals(63, LayoutEditor.HitPointType.TURNTABLE_RAY_63.turntableTrackIndex());        
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testHPTturntableTrackIndexBad1() {
        LayoutEditor.HitPointType.POS_POINT.turntableTrackIndex();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHPTturntableTrackIndexBad2() {
        LayoutEditor.HitPointType.SHAPE_POINT_9.turntableTrackIndex();
    }

    @Test
    public void testHPTturntableTrackIndexedValueOK() {
        Assert.assertEquals(LayoutEditor.HitPointType.TURNTABLE_RAY_0, LayoutEditor.HitPointType.turntableTrackIndexedValue(0));
        Assert.assertEquals(LayoutEditor.HitPointType.TURNTABLE_RAY_1, LayoutEditor.HitPointType.turntableTrackIndexedValue(1));
        Assert.assertEquals(LayoutEditor.HitPointType.TURNTABLE_RAY_2, LayoutEditor.HitPointType.turntableTrackIndexedValue(2));
        // ..
        Assert.assertEquals(LayoutEditor.HitPointType.TURNTABLE_RAY_61, LayoutEditor.HitPointType.turntableTrackIndexedValue(61));
        Assert.assertEquals(LayoutEditor.HitPointType.TURNTABLE_RAY_62, LayoutEditor.HitPointType.turntableTrackIndexedValue(62));
        Assert.assertEquals(LayoutEditor.HitPointType.TURNTABLE_RAY_63, LayoutEditor.HitPointType.turntableTrackIndexedValue(63));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testHPTturntableTrackIndexedValueBad1() throws IllegalArgumentException {
        LayoutEditor.HitPointType.turntableTrackIndexedValue(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHPTturntableTrackIndexedValueBad2() {
        LayoutEditor.HitPointType.turntableTrackIndexedValue(64);
    }

    @Test
    public void testHPTturntableValues() {
        LayoutEditor.HitPointType[] array = LayoutEditor.HitPointType.turntableValues();
        Assert.assertEquals(64, array.length);
        Assert.assertEquals(LayoutEditor.HitPointType.TURNTABLE_RAY_0, array[0]);
        Assert.assertEquals(LayoutEditor.HitPointType.TURNTABLE_RAY_1, array[1]);
        // ..
        Assert.assertEquals(LayoutEditor.HitPointType.TURNTABLE_RAY_62, array[62]);
        Assert.assertEquals(LayoutEditor.HitPointType.TURNTABLE_RAY_63, array[63]);
        
        // while available, use the indexing operations to test content
        for (int i = 0; i<array.length; i++) {
            Assert.assertEquals(LayoutEditor.HitPointType.turntableTrackIndexedValue(i),array[i]);
        }
    }

       // *****************************************************************
       //    Shape Point support
       // *****************************************************************
       
    @Test
    public void testHPTshapePointIndexOK() {
        Assert.assertEquals(0, LayoutEditor.HitPointType.SHAPE_POINT_0.shapePointIndex());
        Assert.assertEquals(1, LayoutEditor.HitPointType.SHAPE_POINT_1.shapePointIndex());
        Assert.assertEquals(2, LayoutEditor.HitPointType.SHAPE_POINT_2.shapePointIndex());
        // ..
        Assert.assertEquals(8, LayoutEditor.HitPointType.SHAPE_POINT_8.shapePointIndex());
        Assert.assertEquals(9, LayoutEditor.HitPointType.SHAPE_POINT_9.shapePointIndex());        
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testHPTshapePointIndexBad1() {
        LayoutEditor.HitPointType.POS_POINT.shapePointIndex();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHPTshapePointIndexBad2() {
        LayoutEditor.HitPointType.TURNTABLE_RAY_0.shapePointIndex();
    }

    @Test
    public void testHPTshapePointIndexedValueOK() {
        Assert.assertEquals(LayoutEditor.HitPointType.SHAPE_POINT_0, LayoutEditor.HitPointType.shapePointIndexedValue(0));
        Assert.assertEquals(LayoutEditor.HitPointType.SHAPE_POINT_1, LayoutEditor.HitPointType.shapePointIndexedValue(1));
        Assert.assertEquals(LayoutEditor.HitPointType.SHAPE_POINT_2, LayoutEditor.HitPointType.shapePointIndexedValue(2));
        // ..
        Assert.assertEquals(LayoutEditor.HitPointType.SHAPE_POINT_7, LayoutEditor.HitPointType.shapePointIndexedValue(7));
        Assert.assertEquals(LayoutEditor.HitPointType.SHAPE_POINT_8, LayoutEditor.HitPointType.shapePointIndexedValue(8));
        Assert.assertEquals(LayoutEditor.HitPointType.SHAPE_POINT_9, LayoutEditor.HitPointType.shapePointIndexedValue(9));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testHPTshapePointIndexedValueBad1() throws IllegalArgumentException {
        LayoutEditor.HitPointType.shapePointIndexedValue(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHPTshapePointIndexedValueBad2() {
        LayoutEditor.HitPointType.shapePointIndexedValue(10);
    }

    @Test
    public void testHPTshapePointValues() {
        LayoutEditor.HitPointType[] array = LayoutEditor.HitPointType.shapePointValues();
        Assert.assertEquals(10, array.length);
        Assert.assertEquals(LayoutEditor.HitPointType.SHAPE_POINT_0, array[0]);
        Assert.assertEquals(LayoutEditor.HitPointType.SHAPE_POINT_1, array[1]);
        // ..
        Assert.assertEquals(LayoutEditor.HitPointType.SHAPE_POINT_8, array[8]);
        Assert.assertEquals(LayoutEditor.HitPointType.SHAPE_POINT_9, array[9]);
        
        // while available, use the indexing operations to test content
        for (int i = 0; i<array.length; i++) {
            Assert.assertEquals(LayoutEditor.HitPointType.shapePointIndexedValue(i),array[i]);
        }
    }

       // *****************************************************************
       //    Bezier Point support
       // *****************************************************************
       
    @Test
    public void testHPTbezierPointIndexOK() {
        Assert.assertEquals(0, LayoutEditor.HitPointType.BEZIER_CONTROL_POINT_0.bezierPointIndex());
        Assert.assertEquals(1, LayoutEditor.HitPointType.BEZIER_CONTROL_POINT_1.bezierPointIndex());
        Assert.assertEquals(2, LayoutEditor.HitPointType.BEZIER_CONTROL_POINT_2.bezierPointIndex());
        // ..
        Assert.assertEquals(7, LayoutEditor.HitPointType.BEZIER_CONTROL_POINT_7.bezierPointIndex());
        Assert.assertEquals(8, LayoutEditor.HitPointType.BEZIER_CONTROL_POINT_8.bezierPointIndex());        
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testHPTbezierPointIndexBad1() {
        LayoutEditor.HitPointType.POS_POINT.bezierPointIndex();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHPTbezierPointIndexBad2() {
        LayoutEditor.HitPointType.TURNTABLE_RAY_0.bezierPointIndex();
    }

    @Test
    public void testHPTbezierPointIndexedValueOK() {
        Assert.assertEquals(LayoutEditor.HitPointType.BEZIER_CONTROL_POINT_0, LayoutEditor.HitPointType.bezierPointIndexedValue(0));
        Assert.assertEquals(LayoutEditor.HitPointType.BEZIER_CONTROL_POINT_1, LayoutEditor.HitPointType.bezierPointIndexedValue(1));
        Assert.assertEquals(LayoutEditor.HitPointType.BEZIER_CONTROL_POINT_2, LayoutEditor.HitPointType.bezierPointIndexedValue(2));
        // ..
        Assert.assertEquals(LayoutEditor.HitPointType.BEZIER_CONTROL_POINT_6, LayoutEditor.HitPointType.bezierPointIndexedValue(6));
        Assert.assertEquals(LayoutEditor.HitPointType.BEZIER_CONTROL_POINT_7, LayoutEditor.HitPointType.bezierPointIndexedValue(7));
        Assert.assertEquals(LayoutEditor.HitPointType.BEZIER_CONTROL_POINT_8, LayoutEditor.HitPointType.bezierPointIndexedValue(8));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testHPTbezierPointIndexedValueBad1() throws IllegalArgumentException {
        LayoutEditor.HitPointType.bezierPointIndexedValue(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHPTbezierPointIndexedValueBad2() {
        LayoutEditor.HitPointType.bezierPointIndexedValue(9);
    }
    
        // *****************************************************************
        // General Enum tests
        // *****************************************************************
    
    /**
     * Ensure that assigned integer values (xmlValue) and ordinal values are actually the same
     */
    @Test
    public void testHPTesureOrdinalValuesSameAsAssigned() {
        for (LayoutEditor.HitPointType instance : LayoutEditor.HitPointType.values()) {
            Assert.assertEquals(Integer.valueOf(instance.ordinal()), instance.getXmlValue());
        }
    }

    /** 
     * The specific values are tested for historical i/O compatibility
     * and because some internal operations do arithmetic with them.
     * For now, it's sufficient for this test to ensure they don't change,
     * hence this test.
     */
    @Test
    public void testEnumIntegerValues() {
        Assert.assertEquals("HitPointTypes.NONE.getValue() == 0", LayoutEditor.HitPointType.NONE.getXmlValue(), Integer.valueOf(0));
        Assert.assertEquals("HitPointTypes.POS_POINT.getValue() == 1", LayoutEditor.HitPointType.POS_POINT.getXmlValue(), Integer.valueOf(1));
        Assert.assertEquals("HitPointTypes.TURNOUT_A.getValue() == 2", LayoutEditor.HitPointType.TURNOUT_A.getXmlValue(), Integer.valueOf(2));
        Assert.assertEquals("HitPointTypes.TURNOUT_B.getValue() == 3", LayoutEditor.HitPointType.TURNOUT_B.getXmlValue(), Integer.valueOf(3));
        Assert.assertEquals("HitPointTypes.TURNOUT_C.getValue() == 4", LayoutEditor.HitPointType.TURNOUT_C.getXmlValue(), Integer.valueOf(4));
        Assert.assertEquals("HitPointTypes.TURNOUT_D.getValue() == 5", LayoutEditor.HitPointType.TURNOUT_D.getXmlValue(), Integer.valueOf(5));
        Assert.assertEquals("HitPointTypes.LEVEL_XING_A.getValue() == 6", LayoutEditor.HitPointType.LEVEL_XING_A.getXmlValue(), Integer.valueOf(6));
        Assert.assertEquals("HitPointTypes.LEVEL_XING_B.getValue() == 7", LayoutEditor.HitPointType.LEVEL_XING_B.getXmlValue(), Integer.valueOf(7));
        Assert.assertEquals("HitPointTypes.LEVEL_XING_C.getValue() == 8", LayoutEditor.HitPointType.LEVEL_XING_C.getXmlValue(), Integer.valueOf(8));
        Assert.assertEquals("HitPointTypes.LEVEL_XING_D.getValue() == 9", LayoutEditor.HitPointType.LEVEL_XING_D.getXmlValue(), Integer.valueOf(9));
        Assert.assertEquals("HitPointTypes.TRACK.getValue() == 10", LayoutEditor.HitPointType.TRACK.getXmlValue(), Integer.valueOf(10));
        Assert.assertEquals("HitPointTypes.TURNOUT_CENTER.getValue() == 11", LayoutEditor.HitPointType.TURNOUT_CENTER.getXmlValue(), Integer.valueOf(11));
        Assert.assertEquals("HitPointTypes.LEVEL_XING_CENTER.getValue() == 12", LayoutEditor.HitPointType.LEVEL_XING_CENTER.getXmlValue(), Integer.valueOf(12));
        Assert.assertEquals("HitPointTypes.TURNTABLE_CENTER.getValue() == 13", LayoutEditor.HitPointType.TURNTABLE_CENTER.getXmlValue(), Integer.valueOf(13));
        Assert.assertEquals("HitPointTypes.LAYOUT_POS_LABEL.getValue() == 14", LayoutEditor.HitPointType.LAYOUT_POS_LABEL.getXmlValue(), Integer.valueOf(14));
        Assert.assertEquals("HitPointTypes.LAYOUT_POS_JCOMP.getValue() == 15", LayoutEditor.HitPointType.LAYOUT_POS_JCOMP.getXmlValue(), Integer.valueOf(15));
        Assert.assertEquals("HitPointTypes.MULTI_SENSOR.getValue() == 16", LayoutEditor.HitPointType.MULTI_SENSOR.getXmlValue(), Integer.valueOf(16));
        Assert.assertEquals("HitPointTypes.MARKER.getValue() == 17", LayoutEditor.HitPointType.MARKER.getXmlValue(), Integer.valueOf(17));
        Assert.assertEquals("HitPointTypes.TRACK_CIRCLE_CENTRE.getValue() == 18", LayoutEditor.HitPointType.TRACK_CIRCLE_CENTRE.getXmlValue(), Integer.valueOf(18));
        Assert.assertEquals("HitPointTypes.UNUSED_19.getValue() == 19", LayoutEditor.HitPointType.UNUSED_19.getXmlValue(), Integer.valueOf(19));
        Assert.assertEquals("HitPointTypes.SLIP_CENTER.getValue() == 20", LayoutEditor.HitPointType.SLIP_CENTER.getXmlValue(), Integer.valueOf(20));
        Assert.assertEquals("HitPointTypes.SLIP_A.getValue() == 21", LayoutEditor.HitPointType.SLIP_A.getXmlValue(), Integer.valueOf(21));
        Assert.assertEquals("HitPointTypes.SLIP_B.getValue() == 22", LayoutEditor.HitPointType.SLIP_B.getXmlValue(), Integer.valueOf(22));
        Assert.assertEquals("HitPointTypes.SLIP_C.getValue() == 23", LayoutEditor.HitPointType.SLIP_C.getXmlValue(), Integer.valueOf(23));
        Assert.assertEquals("HitPointTypes.SLIP_D.getValue() == 24", LayoutEditor.HitPointType.SLIP_D.getXmlValue(), Integer.valueOf(24));
        Assert.assertEquals("HitPointTypes.SLIP_LEFT.getValue() == 25", LayoutEditor.HitPointType.SLIP_LEFT.getXmlValue(), Integer.valueOf(25));
        Assert.assertEquals("HitPointTypes.SLIP_RIGHT.getValue() == 26", LayoutEditor.HitPointType.SLIP_RIGHT.getXmlValue(), Integer.valueOf(26));
        Assert.assertEquals("HitPointTypes.UNUSED_27.getValue() == 27", LayoutEditor.HitPointType.UNUSED_27.getXmlValue(), Integer.valueOf(27));
        Assert.assertEquals("HitPointTypes.UNUSED_28.getValue() == 28", LayoutEditor.HitPointType.UNUSED_28.getXmlValue(), Integer.valueOf(28));
        Assert.assertEquals("HitPointTypes.UNUSED_29.getValue() == 29", LayoutEditor.HitPointType.UNUSED_29.getXmlValue(), Integer.valueOf(29));
        Assert.assertEquals("HitPointTypes.BEZIER_CONTROL_POINT_0.getValue() == 30", LayoutEditor.HitPointType.BEZIER_CONTROL_POINT_0.getXmlValue(), Integer.valueOf(30));
        Assert.assertEquals("HitPointTypes.BEZIER_CONTROL_POINT_1.getValue() == 31", LayoutEditor.HitPointType.BEZIER_CONTROL_POINT_1.getXmlValue(), Integer.valueOf(31));
        Assert.assertEquals("HitPointTypes.BEZIER_CONTROL_POINT_2.getValue() == 32", LayoutEditor.HitPointType.BEZIER_CONTROL_POINT_2.getXmlValue(), Integer.valueOf(32));
        Assert.assertEquals("HitPointTypes.BEZIER_CONTROL_POINT_3.getValue() == 33", LayoutEditor.HitPointType.BEZIER_CONTROL_POINT_3.getXmlValue(), Integer.valueOf(33));
        Assert.assertEquals("HitPointTypes.BEZIER_CONTROL_POINT_4.getValue() == 34", LayoutEditor.HitPointType.BEZIER_CONTROL_POINT_4.getXmlValue(), Integer.valueOf(34));
        Assert.assertEquals("HitPointTypes.BEZIER_CONTROL_POINT_5.getValue() == 35", LayoutEditor.HitPointType.BEZIER_CONTROL_POINT_5.getXmlValue(), Integer.valueOf(35));
        Assert.assertEquals("HitPointTypes.BEZIER_CONTROL_POINT_6.getValue() == 36", LayoutEditor.HitPointType.BEZIER_CONTROL_POINT_6.getXmlValue(), Integer.valueOf(36));
        Assert.assertEquals("HitPointTypes.BEZIER_CONTROL_POINT_7.getValue() == 37", LayoutEditor.HitPointType.BEZIER_CONTROL_POINT_7.getXmlValue(), Integer.valueOf(37));
        Assert.assertEquals("HitPointTypes.BEZIER_CONTROL_POINT_8.getValue() == 38", LayoutEditor.HitPointType.BEZIER_CONTROL_POINT_8.getXmlValue(), Integer.valueOf(38));
        Assert.assertEquals("HitPointTypes.SHAPE_CENTER.getValue() == 39", LayoutEditor.HitPointType.SHAPE_CENTER.getXmlValue(), Integer.valueOf(39));
        Assert.assertEquals("HitPointTypes.SHAPE_POINT_0.getValue() == 40", LayoutEditor.HitPointType.SHAPE_POINT_0.getXmlValue(), Integer.valueOf(40));
        Assert.assertEquals("HitPointTypes.SHAPE_POINT_1.getValue() == 41", LayoutEditor.HitPointType.SHAPE_POINT_1.getXmlValue(), Integer.valueOf(41));
        Assert.assertEquals("HitPointTypes.SHAPE_POINT_2.getValue() == 42", LayoutEditor.HitPointType.SHAPE_POINT_2.getXmlValue(), Integer.valueOf(42));
        Assert.assertEquals("HitPointTypes.SHAPE_POINT_3.getValue() == 43", LayoutEditor.HitPointType.SHAPE_POINT_3.getXmlValue(), Integer.valueOf(43));
        Assert.assertEquals("HitPointTypes.SHAPE_POINT_4.getValue() == 44", LayoutEditor.HitPointType.SHAPE_POINT_4.getXmlValue(), Integer.valueOf(44));
        Assert.assertEquals("HitPointTypes.SHAPE_POINT_5.getValue() == 45", LayoutEditor.HitPointType.SHAPE_POINT_5.getXmlValue(), Integer.valueOf(45));
        Assert.assertEquals("HitPointTypes.SHAPE_POINT_6.getValue() == 46", LayoutEditor.HitPointType.SHAPE_POINT_6.getXmlValue(), Integer.valueOf(46));
        Assert.assertEquals("HitPointTypes.SHAPE_POINT_7.getValue() == 47", LayoutEditor.HitPointType.SHAPE_POINT_7.getXmlValue(), Integer.valueOf(47));
        Assert.assertEquals("HitPointTypes.SHAPE_POINT_8.getValue() == 48", LayoutEditor.HitPointType.SHAPE_POINT_8.getXmlValue(), Integer.valueOf(48));
        Assert.assertEquals("HitPointTypes.SHAPE_POINT_9.getValue() == 49", LayoutEditor.HitPointType.SHAPE_POINT_9.getXmlValue(), Integer.valueOf(49));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_0.getValue() == 50", LayoutEditor.HitPointType.TURNTABLE_RAY_0.getXmlValue(), Integer.valueOf(50));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_1.getValue() == 51", LayoutEditor.HitPointType.TURNTABLE_RAY_1.getXmlValue(), Integer.valueOf(51));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_2.getValue() == 52", LayoutEditor.HitPointType.TURNTABLE_RAY_2.getXmlValue(), Integer.valueOf(52));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_3.getValue() == 53", LayoutEditor.HitPointType.TURNTABLE_RAY_3.getXmlValue(), Integer.valueOf(53));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_4.getValue() == 54", LayoutEditor.HitPointType.TURNTABLE_RAY_4.getXmlValue(), Integer.valueOf(54));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_5.getValue() == 55", LayoutEditor.HitPointType.TURNTABLE_RAY_5.getXmlValue(), Integer.valueOf(55));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_6.getValue() == 56", LayoutEditor.HitPointType.TURNTABLE_RAY_6.getXmlValue(), Integer.valueOf(56));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_7.getValue() == 57", LayoutEditor.HitPointType.TURNTABLE_RAY_7.getXmlValue(), Integer.valueOf(57));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_8.getValue() == 58", LayoutEditor.HitPointType.TURNTABLE_RAY_8.getXmlValue(), Integer.valueOf(58));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_9.getValue() == 59", LayoutEditor.HitPointType.TURNTABLE_RAY_9.getXmlValue(), Integer.valueOf(59));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_10.getValue() == 60", LayoutEditor.HitPointType.TURNTABLE_RAY_10.getXmlValue(), Integer.valueOf(60));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_11.getValue() == 61", LayoutEditor.HitPointType.TURNTABLE_RAY_11.getXmlValue(), Integer.valueOf(61));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_12.getValue() == 62", LayoutEditor.HitPointType.TURNTABLE_RAY_12.getXmlValue(), Integer.valueOf(62));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_13.getValue() == 63", LayoutEditor.HitPointType.TURNTABLE_RAY_13.getXmlValue(), Integer.valueOf(63));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_14.getValue() == 64", LayoutEditor.HitPointType.TURNTABLE_RAY_14.getXmlValue(), Integer.valueOf(64));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_15.getValue() == 65", LayoutEditor.HitPointType.TURNTABLE_RAY_15.getXmlValue(), Integer.valueOf(65));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_16.getValue() == 66", LayoutEditor.HitPointType.TURNTABLE_RAY_16.getXmlValue(), Integer.valueOf(66));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_17.getValue() == 67", LayoutEditor.HitPointType.TURNTABLE_RAY_17.getXmlValue(), Integer.valueOf(67));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_18.getValue() == 68", LayoutEditor.HitPointType.TURNTABLE_RAY_18.getXmlValue(), Integer.valueOf(68));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_19.getValue() == 69", LayoutEditor.HitPointType.TURNTABLE_RAY_19.getXmlValue(), Integer.valueOf(69));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_20.getValue() == 70", LayoutEditor.HitPointType.TURNTABLE_RAY_20.getXmlValue(), Integer.valueOf(70));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_21.getValue() == 71", LayoutEditor.HitPointType.TURNTABLE_RAY_21.getXmlValue(), Integer.valueOf(71));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_22.getValue() == 72", LayoutEditor.HitPointType.TURNTABLE_RAY_22.getXmlValue(), Integer.valueOf(72));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_23.getValue() == 73", LayoutEditor.HitPointType.TURNTABLE_RAY_23.getXmlValue(), Integer.valueOf(73));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_24.getValue() == 74", LayoutEditor.HitPointType.TURNTABLE_RAY_24.getXmlValue(), Integer.valueOf(74));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_25.getValue() == 75", LayoutEditor.HitPointType.TURNTABLE_RAY_25.getXmlValue(), Integer.valueOf(75));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_26.getValue() == 76", LayoutEditor.HitPointType.TURNTABLE_RAY_26.getXmlValue(), Integer.valueOf(76));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_27.getValue() == 77", LayoutEditor.HitPointType.TURNTABLE_RAY_27.getXmlValue(), Integer.valueOf(77));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_28.getValue() == 78", LayoutEditor.HitPointType.TURNTABLE_RAY_28.getXmlValue(), Integer.valueOf(78));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_29.getValue() == 79", LayoutEditor.HitPointType.TURNTABLE_RAY_29.getXmlValue(), Integer.valueOf(79));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_30.getValue() == 80", LayoutEditor.HitPointType.TURNTABLE_RAY_30.getXmlValue(), Integer.valueOf(80));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_31.getValue() == 81", LayoutEditor.HitPointType.TURNTABLE_RAY_31.getXmlValue(), Integer.valueOf(81));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_32.getValue() == 82", LayoutEditor.HitPointType.TURNTABLE_RAY_32.getXmlValue(), Integer.valueOf(82));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_33.getValue() == 83", LayoutEditor.HitPointType.TURNTABLE_RAY_33.getXmlValue(), Integer.valueOf(83));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_34.getValue() == 84", LayoutEditor.HitPointType.TURNTABLE_RAY_34.getXmlValue(), Integer.valueOf(84));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_35.getValue() == 85", LayoutEditor.HitPointType.TURNTABLE_RAY_35.getXmlValue(), Integer.valueOf(85));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_36.getValue() == 86", LayoutEditor.HitPointType.TURNTABLE_RAY_36.getXmlValue(), Integer.valueOf(86));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_37.getValue() == 87", LayoutEditor.HitPointType.TURNTABLE_RAY_37.getXmlValue(), Integer.valueOf(87));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_38.getValue() == 88", LayoutEditor.HitPointType.TURNTABLE_RAY_38.getXmlValue(), Integer.valueOf(88));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_39.getValue() == 89", LayoutEditor.HitPointType.TURNTABLE_RAY_39.getXmlValue(), Integer.valueOf(89));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_40.getValue() == 90", LayoutEditor.HitPointType.TURNTABLE_RAY_40.getXmlValue(), Integer.valueOf(90));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_41.getValue() == 91", LayoutEditor.HitPointType.TURNTABLE_RAY_41.getXmlValue(), Integer.valueOf(91));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_42.getValue() == 92", LayoutEditor.HitPointType.TURNTABLE_RAY_42.getXmlValue(), Integer.valueOf(92));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_43.getValue() == 93", LayoutEditor.HitPointType.TURNTABLE_RAY_43.getXmlValue(), Integer.valueOf(93));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_44.getValue() == 94", LayoutEditor.HitPointType.TURNTABLE_RAY_44.getXmlValue(), Integer.valueOf(94));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_45.getValue() == 95", LayoutEditor.HitPointType.TURNTABLE_RAY_45.getXmlValue(), Integer.valueOf(95));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_46.getValue() == 96", LayoutEditor.HitPointType.TURNTABLE_RAY_46.getXmlValue(), Integer.valueOf(96));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_47.getValue() == 97", LayoutEditor.HitPointType.TURNTABLE_RAY_47.getXmlValue(), Integer.valueOf(97));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_48.getValue() == 98", LayoutEditor.HitPointType.TURNTABLE_RAY_48.getXmlValue(), Integer.valueOf(98));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_49.getValue() == 99", LayoutEditor.HitPointType.TURNTABLE_RAY_49.getXmlValue(), Integer.valueOf(99));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_50.getValue() == 100", LayoutEditor.HitPointType.TURNTABLE_RAY_50.getXmlValue(), Integer.valueOf(100));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_51.getValue() == 101", LayoutEditor.HitPointType.TURNTABLE_RAY_51.getXmlValue(), Integer.valueOf(101));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_52.getValue() == 102", LayoutEditor.HitPointType.TURNTABLE_RAY_52.getXmlValue(), Integer.valueOf(102));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_53.getValue() == 103", LayoutEditor.HitPointType.TURNTABLE_RAY_53.getXmlValue(), Integer.valueOf(103));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_54.getValue() == 104", LayoutEditor.HitPointType.TURNTABLE_RAY_54.getXmlValue(), Integer.valueOf(104));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_55.getValue() == 105", LayoutEditor.HitPointType.TURNTABLE_RAY_55.getXmlValue(), Integer.valueOf(105));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_56.getValue() == 106", LayoutEditor.HitPointType.TURNTABLE_RAY_56.getXmlValue(), Integer.valueOf(106));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_57.getValue() == 107", LayoutEditor.HitPointType.TURNTABLE_RAY_57.getXmlValue(), Integer.valueOf(107));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_58.getValue() == 108", LayoutEditor.HitPointType.TURNTABLE_RAY_58.getXmlValue(), Integer.valueOf(108));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_59.getValue() == 109", LayoutEditor.HitPointType.TURNTABLE_RAY_59.getXmlValue(), Integer.valueOf(109));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_60.getValue() == 110", LayoutEditor.HitPointType.TURNTABLE_RAY_60.getXmlValue(), Integer.valueOf(110));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_61.getValue() == 111", LayoutEditor.HitPointType.TURNTABLE_RAY_61.getXmlValue(), Integer.valueOf(111));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_62.getValue() == 112", LayoutEditor.HitPointType.TURNTABLE_RAY_62.getXmlValue(), Integer.valueOf(112));
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_63.getValue() == 113", LayoutEditor.HitPointType.TURNTABLE_RAY_63.getXmlValue(), Integer.valueOf(113));
    }

    @Test
    @Ignore("Test fails to find and close dialog on Jenkins")
    public void testSavePanel() {

        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JMenuOperator jmo = new JMenuOperator(jfo, Bundle.getMessage("MenuFile"));

        //delete this file so we won't get the "<xxx> exists... do you want to replace?" dialog.
        new File("temp/Layout Editor Test Layout.xml").delete();

        // test the file -> delete panel menu item
        Thread misc1 = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("StorePanelTitle"),
                Bundle.getMessage("ButtonCancel"));  // NOI18N
        jmo.pushMenu(Bundle.getMessage("MenuFile") + "/"
                + Bundle.getMessage("MenuItemStore"), "/");
        JUnitUtil.waitFor(() -> {
            return !(misc1.isAlive());
        }, "misc1 finished");

        //clean up after ourselves...
        new File("temp/Layout Editor Test Layout.xml").delete();
    }

    @Test
    public void testDeletePanel() {

        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JMenuOperator jmo = new JMenuOperator(jfo, Bundle.getMessage("MenuFile"));

        // test the file -> delete panel menu item
        Thread misc1 = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("DeleteVerifyTitle"),
                Bundle.getMessage("ButtonYesDelete"));  // NOI18N
        jmo.pushMenu(Bundle.getMessage("MenuFile") + "/"
                + Bundle.getMessage("DeletePanel"), "/");
        JUnitUtil.waitFor(() -> {
            return !(misc1.isAlive());
        }, "misc1 finished");
        JUnitUtil.dispose(e);
        e = null; // prevent closing the window using the operator in shutDown.
    }

    @Test
    public void testGetFinder() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditorFindItems f = e.getFinder();
        Assert.assertNotNull("exists", f);
    }

    @Test
    @Override
    @Ignore("failing to set size on appveyor")
    public void testSetSize() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        e.setSize(100, 100);
        java.awt.Dimension d = e.getSize();

        // the java.awt.Dimension stores the values as floating point
        // numbers, but setSize expects integer parameters.
        Assert.assertEquals("Width Set", 100.0, d.getWidth(), 0.0);
        Assert.assertEquals("Height Set", 100.0, d.getHeight(), 0.0);
    }

    @Test
    @Ignore("Failing to set second zoom")
    public void testGetSetZoom() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((m) -> {
            m.setSaveAllowed(false); // prevent attempts to save while zooming in rest of test
        });
        Assert.assertEquals("Get initial Zoom", 1.0, e.getZoom(), 0.0);

        // note: Layout Editor won't allow zooms above 8.0.
        e.setZoom(10.0);
        Assert.assertEquals("Get Zoom after set above max", 8.0, e.getZoom(), 0.0);
        e.setZoom(3.33);
        Assert.assertEquals("Get Zoom After Set to 3.33", 3.33, e.getZoom(), 0.0);
    }

    @Test
    public void testGetOpenDispatcherOnLoad() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // defaults to false.
        Assert.assertFalse("getOpenDispatcherOnLoad", e.getOpenDispatcherOnLoad());
    }

    @Test
    public void testSetOpenDispatcherOnLoad() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // defaults to false, so set to true.
        e.setOpenDispatcherOnLoad(true);
        Assert.assertTrue("setOpenDispatcherOnLoad after set", e.getOpenDispatcherOnLoad());
    }

    @Test
    public void testIsDirty() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // defaults to false.
        Assert.assertFalse("isDirty", e.isDirty());
    }

    @Test
    public void testSetDirty() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // defaults to false, setDirty() sets it to true.
        e.setDirty();
        Assert.assertTrue("isDirty after set", e.isDirty());
    }

    @Test
    public void testSetDirtyWithParameter() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // defaults to false, so set it to true.
        e.setDirty(true);
        Assert.assertTrue("isDirty after set", e.isDirty());
    }

    @Test
    public void testResetDirty() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // defaults to false, so set it to true.
        e.setDirty(true);
        // then call resetDirty, which sets it back to false.
        e.resetDirty();
        Assert.assertFalse("isDirty after reset", e.isDirty());
    }

    @Test
    public void testIsAnimating() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // default to true
        Assert.assertTrue("isAnimating", e.isAnimating());
    }

    @Test
    public void testSetTurnoutAnimating() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // default to true, so set to false.
        e.setTurnoutAnimation(false);
        Assert.assertFalse("isAnimating after set", e.isAnimating());
    }

    @Test
    public void testGetLayoutWidth() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // defaults to 0
        Assert.assertEquals("layout width", 0, e.getLayoutWidth());
    }

    @Test
    public void testGetLayoutHeight() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // defaults to 0
        Assert.assertEquals("layout height", 0, e.getLayoutHeight());
    }

    @Test
    public void testGetWindowWidth() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // defaults to screen width - 20
        int w = (int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() - 20);
        Assert.assertEquals("window width", w, e.getWindowWidth());
    }

    @Test
    public void testGetWindowHeight() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // defaults to screen height - 120
        int h = (int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight() - 120);
        Assert.assertEquals("window height", h, e.getWindowHeight());
    }

    @Test
    public void testGetUpperLeftX() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // defaults to 0
        Assert.assertEquals("upper left X", 0, e.getUpperLeftX());
    }

    @Test
    public void testGetUpperLeftY() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // defaults to 0
        Assert.assertEquals("upper left Y", 0, e.getUpperLeftY());
    }

    @Test
    public void testSetLayoutDimensions() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ThreadingUtil.runOnGUI(() -> {
            // set the panel dimensions to known values
            e.setLayoutDimensions(100, 100, 100, 100, 100, 100);
            Assert.assertEquals("layout width after set", 100, e.getLayoutWidth());
            Assert.assertEquals("layout height after set", 100, e.getLayoutHeight());
            Assert.assertEquals("window width after set", 100, e.getWindowWidth());
            Assert.assertEquals("window height after set", 100, e.getWindowHeight());
            Assert.assertEquals("upper left X after set", 100, e.getUpperLeftX());
            Assert.assertEquals("upper left Y after set", 100, e.getUpperLeftX());
        });
    }

    @Test
    public void testSetGrideSize() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertEquals("grid size after set", 100, e.setGridSize(100));
    }

    @Test
    public void testGetGrideSize() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // defaults to 10.
        Assert.assertEquals("grid size", 10, e.getGridSize());
    }

    @Test
    public void testGetMainlineTrackWidth() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // defaults to 4.
        Assert.assertEquals("mainline track width", 4, e.getMainlineTrackWidth());
    }

    @Test
    public void testSetMainlineTrackWidth() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // set to known value
        e.setMainlineTrackWidth(10);
        Assert.assertEquals("mainline track width after set", 10, e.getMainlineTrackWidth());
    }

    @Test
    public void testGetSidelineTrackWidth() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // defaults to 2.
        Assert.assertEquals("side track width", 2, e.getSidelineTrackWidth());
    }

    @Test
    public void testSetSideTrackWidth() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // set to known value
        e.setSidelineTrackWidth(10);
        Assert.assertEquals("Side track width after set", 10, e.getSidelineTrackWidth());
    }

    @Test
    public void testGetXScale() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // defaults to 1.
        Assert.assertEquals("XScale", 1.0, e.getXScale(), 0.0);
    }

    @Test
    public void testSetXScale() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // set to known value
        e.setXScale(2.0);
        Assert.assertEquals("XScale after set ", 2.0, e.getXScale(), 0.0);
    }

    @Test
    public void testGetYScale() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // defaults to 1.
        Assert.assertEquals("YScale", 1.0, e.getYScale(), 0.0);
    }

    @Test
    public void testSetYScale() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // set to known value
        e.setYScale(2.0);
        Assert.assertEquals("YScale after set ", 2.0, e.getYScale(), 0.0);
    }

    @Test
    public void testGetDefaultTrackColor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertEquals("Default Track Color", ColorUtil.ColorDarkGray, e.getDefaultTrackColor());
    }

    @Test
    public void testSetDefaultTrackColor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        e.setDefaultTrackColor(ColorUtil.stringToColor(ColorUtil.ColorPink));
        Assert.assertEquals("Default Track Color after Set", ColorUtil.ColorPink, e.getDefaultTrackColor());
    }

    @Test
    public void testGetDefaultOccupiedTrackColor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertEquals("Default Occupied Track Color", "red", e.getDefaultOccupiedTrackColor());
    }

    @Test
    public void testSetDefaultOccupiedTrackColor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        e.setDefaultOccupiedTrackColor(ColorUtil.stringToColor(ColorUtil.ColorPink));
        Assert.assertEquals("Default Occupied Track Color after Set", ColorUtil.ColorPink, e.getDefaultOccupiedTrackColor());
    }

    @Test
    public void testGetDefaultAlternativeTrackColor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertEquals("Default Alternative Track Color", ColorUtil.ColorWhite, e.getDefaultAlternativeTrackColor());
    }

    @Test
    public void testSetDefaultAlternativeTrackColor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        e.setDefaultAlternativeTrackColor(ColorUtil.stringToColor(ColorUtil.ColorPink));
        Assert.assertEquals("Default Alternative Track Color after Set", ColorUtil.ColorPink, e.getDefaultAlternativeTrackColor());
    }

    @Test
    public void testSetAllTracksToDefaultColors() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        LayoutBlock layoutBlock = InstanceManager.getDefault(LayoutBlockManager.class).createNewLayoutBlock("ILB999", "Test Block");
        Assert.assertNotNull("layoutBlock created", layoutBlock);

        Assert.assertEquals("BlockTrackColor default", e.getDefaultTrackColorColor(), layoutBlock.getBlockTrackColor());
        layoutBlock.setBlockTrackColor(Color.pink);
        Assert.assertEquals("BlockTrackColor set to pink", Color.pink, layoutBlock.getBlockTrackColor());

        Assert.assertEquals("BlockOccupiedColor default", e.getDefaultOccupiedTrackColorColor(), layoutBlock.getBlockOccupiedColor());
        layoutBlock.setBlockOccupiedColor(Color.pink);
        Assert.assertEquals("BlockOccupiedColor set to pink", Color.pink, layoutBlock.getBlockOccupiedColor());

        Assert.assertEquals("BlockExtraColor default", e.getDefaultAlternativeTrackColorColor(), layoutBlock.getBlockExtraColor());
        layoutBlock.setBlockExtraColor(Color.pink);
        Assert.assertEquals("BlockExtraColor set to pink", Color.pink, layoutBlock.getBlockExtraColor());

        int changed = e.setAllTracksToDefaultColors();
        Assert.assertEquals("setAllTracksToDefaultColors changed one block", 1, changed);

        Assert.assertEquals("BlockTrackColor back to default", e.getDefaultTrackColorColor(), layoutBlock.getBlockTrackColor());
        Assert.assertEquals("BlockOccupiedColor back to default", e.getDefaultOccupiedTrackColorColor(), layoutBlock.getBlockOccupiedColor());
        Assert.assertEquals("BlockExtraColor back to default", e.getDefaultAlternativeTrackColorColor(), layoutBlock.getBlockExtraColor());
    }

    @Test
    public void testGetDefaultTextColor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertEquals("Default Text Color", ColorUtil.ColorBlack, e.getDefaultTextColor());
    }

    @Test
    public void testSetDefaultTextColor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        e.setDefaultTextColor(ColorUtil.stringToColor(ColorUtil.ColorPink));
        Assert.assertEquals("Default Text Color after Set", ColorUtil.ColorPink, e.getDefaultTextColor());
    }

    @Test
    public void testGetTurnoutCircleColor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertEquals("Turnout Circle Color", ColorUtil.ColorBlack, e.getTurnoutCircleColor());
    }

    @Test
    public void testSetTurnoutCircleColor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        e.setTurnoutCircleColor(ColorUtil.stringToColor(ColorUtil.ColorPink));
        Assert.assertEquals("Turnout Circle after Set", ColorUtil.ColorPink, e.getTurnoutCircleColor());
    }

    @Test
    public void testGetTurnoutCircleThrownColor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertEquals("Turnout Circle Thrown Color", ColorUtil.ColorBlack, e.getTurnoutCircleThrownColor());
    }

    @Test
    public void testSetTurnoutCircleThrownColor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        e.setTurnoutCircleThrownColor(ColorUtil.stringToColor(ColorUtil.ColorPink));
        Assert.assertEquals("Turnout Circle after Set", ColorUtil.ColorPink, e.getTurnoutCircleThrownColor());
    }

    @Test
    public void testIsTurnoutFillControlCircles() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // default to false
        Assert.assertFalse("isTurnoutFillControlCircles", e.isTurnoutFillControlCircles());
    }

    @Test
    public void testSetTurnoutFillControlCircles() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // default to false, so set to true.
        e.setTurnoutFillControlCircles(true);
        Assert.assertTrue("isTurnoutFillControlCircles after set true", e.isTurnoutFillControlCircles());
        // set back to default (false) and confirm new value
        e.setTurnoutFillControlCircles(false);
        Assert.assertFalse("isTurnoutFillControlCircles after set false", e.isTurnoutFillControlCircles());
    }

    @Test
    public void testGetTurnoutCircleSize() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // defaults to 4.
        Assert.assertEquals("turnout circle size", 4, e.getTurnoutCircleSize());
    }

    @Test
    public void testSetTurnoutCircleSize() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        e.setTurnoutCircleSize(11);
        Assert.assertEquals("turnout circle size after set", 11, e.getTurnoutCircleSize());
    }

    @Test
    public void testGetTurnoutDrawUnselectedLeg() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // default to true
        Assert.assertTrue("getTurnoutDrawUnselectedLeg", e.isTurnoutDrawUnselectedLeg());
    }

    @Test
    public void testSetTurnoutDrawUnselectedLeg() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // default to true, so set to false.
        e.setTurnoutDrawUnselectedLeg(false);
        Assert.assertFalse("getTurnoutDrawUnselectedLeg after set", e.isTurnoutDrawUnselectedLeg());
    }

    @Test
    public void testGetLayoutName() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        e.dispose(); // remove existing instance
        e = new LayoutEditor(); // create new instance to test the default name
        // default is "My Layout"
        Assert.assertEquals("getLayoutName", "My Layout", e.getLayoutName());
    }

    @Test
    public void testSetLayoutName() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // the test layout editor setUp created is named this
        Assert.assertEquals("getLayoutName", "Layout Editor Test Layout", e.getLayoutName());
        // set to a known (different) value
        e.setLayoutName("foo");
        Assert.assertEquals("getLayoutName after set", "foo", e.getLayoutName());
    }

    @Test
    public void testGetShowHelpBar() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        ThreadingUtil.runOnGUI(() -> {
            e.setShowHelpBar(true);
        });
        ThreadingUtil.runOnGUI(() -> {
            Assert.assertTrue("getShowHelpBar", e.getShowHelpBar());
        });
        ThreadingUtil.runOnGUI(() -> {
            e.setShowHelpBar(false);
        });
        ThreadingUtil.runOnGUI(() -> {
            Assert.assertFalse("getShowHelpBar", e.getShowHelpBar());
        });
    }

    @Test
    public void testSetShowHelpBar() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        ThreadingUtil.runOnGUI(() -> {
            e.setShowHelpBar(false);
        });
        ThreadingUtil.runOnGUI(() -> {
            Assert.assertFalse("getShowHelpBar after set", e.getShowHelpBar());
        });
        ThreadingUtil.runOnGUI(() -> {
            e.setShowHelpBar(true);
        });
        ThreadingUtil.runOnGUI(() -> {
            Assert.assertTrue("getShowHelpBar", e.getShowHelpBar());
        });
        ThreadingUtil.runOnGUI(() -> {
            e.setShowHelpBar(false);
        });
        ThreadingUtil.runOnGUI(() -> {
            Assert.assertFalse("getShowHelpBar", e.getShowHelpBar());
        });
        ThreadingUtil.runOnGUI(() -> {
            e.setShowHelpBar(true);
        });
        ThreadingUtil.runOnGUI(() -> {
            Assert.assertTrue("getShowHelpBar", e.getShowHelpBar());
        });
    }

    @Test
    public void testGetDrawGrid() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // default to true
        Assert.assertTrue("getDrawGrid", e.getDrawGrid());
    }

    @Test
    public void testSetDrawGrid() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // default to false, so set to true.
        e.setDrawGrid(true);
        Assert.assertTrue("getDrawGrid after set", e.getDrawGrid());
    }

    @Test
    public void testGetSnapOnAdd() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // default to false
        Assert.assertFalse("getSnapOnAdd", e.getSnapOnAdd());
    }

    @Test
    public void testSetSnapOnAdd() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // default to false, so set to true.
        e.setSnapOnAdd(true);
        Assert.assertTrue("getSnapOnAdd after set", e.getSnapOnAdd());
    }

    @Test
    public void testGetSnapOnMove() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // default to false
        Assert.assertFalse("getSnapOnMove", e.getSnapOnMove());
    }

    @Test
    public void testSetSnapOnMove() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // default to false, so set to true.
        e.setSnapOnMove(true);
        Assert.assertTrue("getSnapOnMove after set", e.getSnapOnMove());
    }

    @Test
    public void testGetAntialiasingOn() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // default to false
        Assert.assertFalse("getAntialiasingOn", e.getAntialiasingOn());
    }

    @Test
    public void testSetAntialiasingOn() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // default to false, so set to true.
        e.setAntialiasingOn(true);
        Assert.assertTrue("getAntialiasingOn after set", e.getAntialiasingOn());
    }

    @Test
    public void testGetTurnoutCircles() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // default to false
        Assert.assertFalse("getTurnoutCircles", e.getTurnoutCircles());
    }

    @Test
    public void testSetTurnoutCircles() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // default to false, so set to true.
        e.setTurnoutCircles(true);
        Assert.assertTrue("getSetTurnoutCircles after set", e.getTurnoutCircles());
    }

    @Test
    public void testGetTooltipsNotEdit() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // default to false
        Assert.assertFalse("getTooltipsNotEdit", e.getTooltipsNotEdit());
    }

    @Test
    public void testSetTooltipsNotEdit() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // default to false, so set to true.
        e.setTooltipsNotEdit(true);
        Assert.assertTrue("getTooltipsNotEdit after set", e.getTooltipsNotEdit());
    }

    @Test
    public void testGetTooltipsInEdit() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // default to true
        Assert.assertTrue("getTooltipsInEdit", e.getTooltipsInEdit());
    }

    @Test
    public void testSetTooltipsInEdit() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // default to true, so set to false.
        e.setTooltipsInEdit(false);
        Assert.assertFalse("getTooltipsInEdit after set", e.getTooltipsInEdit());
    }

    @Test
    public void testGetAutoBlockAssignment() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // default to false
        Assert.assertFalse("getAutoBlockAssignment", e.getAutoBlockAssignment());
    }

    @Test
    public void testSetAutoBlockAssignment() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // default to false, so set to true.
        e.setAutoBlockAssignment(true);
        Assert.assertTrue("getAutoBlockAssignment after set", e.getAutoBlockAssignment());
    }

    @Test
    public void testGetTurnoutBX() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // defaults to 20.
        Assert.assertEquals("getTurnoutBX", 20.0, e.getTurnoutBX(), 0.0);
    }

    @Test
    public void testSetTurnoutBX() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // set to known value
        e.setTurnoutBX(2.0);
        Assert.assertEquals("getTurnoutBX after set ", 2.0, e.getTurnoutBX(), 0.0);
    }

    @Test
    public void testGetTurnoutCX() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // defaults to 20.
        Assert.assertEquals("getTurnoutCX", 20.0, e.getTurnoutCX(), 0.0);
    }

    @Test
    public void testSetTurnoutCX() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // set to known value
        e.setTurnoutCX(2.0);
        Assert.assertEquals("getTurnoutCX after set ", 2.0, e.getTurnoutCX(), 0.0);
    }

    @Test
    public void testGetTurnoutWid() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // defaults to 10.
        Assert.assertEquals("getTurnoutWid", 10.0, e.getTurnoutWid(), 0.0);
    }

    @Test
    public void testSetTurnoutWid() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // set to known value
        e.setTurnoutWid(2.0);
        Assert.assertEquals("getTurnoutWid after set ", 2.0, e.getTurnoutWid(), 0.0);
    }

    @Test
    public void testGetXOverLong() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // defaults to 30.
        Assert.assertEquals("getXOverLong", 30.0, e.getXOverLong(), 0.0);
    }

    @Test
    public void testSetXOverLong() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // set to known value
        e.setXOverLong(2.0);
        Assert.assertEquals("getXOverLong after set ", 2.0, e.getXOverLong(), 0.0);
    }

    @Test
    public void testGetXOverHWid() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // defaults to 10.
        Assert.assertEquals("getXOverHWid", 10.0, e.getXOverHWid(), 0.0);
    }

    @Test
    public void testSetXOverHWid() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // set to known value
        e.setXOverHWid(2.0);
        Assert.assertEquals("getXOverWid after set ", 2.0, e.getXOverHWid(), 0.0);
    }

    @Test
    public void testGetXOverShort() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // defaults to 10.
        Assert.assertEquals("getXOverShort", 10.0, e.getXOverShort(), 0.0);
    }

    @Test
    public void testSetXOverShort() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // set to known value
        e.setXOverShort(2.0);
        Assert.assertEquals("getXOverShort after set ", 2.0, e.getXOverShort(), 0.0);
    }

    @Test
    public void testResetTurnoutSizes() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // set all dimensions to known value
        e.setTurnoutBX(2.0);
        e.setTurnoutCX(2.0);
        e.setTurnoutWid(2.0);
        e.setXOverLong(2.0);
        e.setXOverHWid(2.0);
        e.setXOverShort(2.0);

        // reset - uses reflection to get a private method.
        java.lang.reflect.Method resetTurnoutSize = null;
        try {
            resetTurnoutSize = e.getClass().getDeclaredMethod("resetTurnoutSize");
        } catch (java.lang.NoSuchMethodException nsm) {
            Assert.fail("Could not find method resetTurnoutSize in LayoutEditor class.");
        }
        // override the default permissions.
        Assert.assertNotNull(resetTurnoutSize);
        resetTurnoutSize.setAccessible(true);
        try {
            resetTurnoutSize.invoke(e);
        } catch (java.lang.IllegalAccessException iae) {
            Assert.fail("Could not access method resetTurnoutSize in LayoutEditor class.");
        } catch (java.lang.reflect.InvocationTargetException ite) {
            Throwable cause = ite.getCause();
            Assert.fail("resetTurnoutSize execution failed reason: " + cause.getMessage());
        }

        // then check for the default values.
        Assert.assertEquals("getTurnoutBX", 20.0, e.getTurnoutBX(), 0.0);
        Assert.assertEquals("getTurnoutCX", 20.0, e.getTurnoutBX(), 0.0);
        Assert.assertEquals("getTurnoutWid", 20.0, e.getTurnoutBX(), 0.0);
        Assert.assertEquals("getXOverLong", 30.0, e.getXOverLong(), 0.0);
        Assert.assertEquals("getXOverHWid", 30.0, e.getXOverLong(), 0.0);
        Assert.assertEquals("getXOverShort", 30.0, e.getXOverLong(), 0.0);
        // and reset also sets the dirty bit.
        Assert.assertTrue("isDirty after resetTurnoutSize", e.isDirty());
    }

    @Test
    public void testGetDirectTurnoutControl() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // default to false
        Assert.assertFalse("getDirectTurnoutControl", e.getDirectTurnoutControl());
    }

    @Test
    public void testSetDirectTurnoutControl() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // default to false, so set to true.
        e.setDirectTurnoutControl(true);
        Assert.assertTrue("getDirectTurnoutControl after set", e.getDirectTurnoutControl());
    }

    @Test
    public void testSetDirectTurnoutControlOff() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        e.setDirectTurnoutControl(false);
        Assert.assertFalse("getDirectTurnoutControl after set", e.getDirectTurnoutControl());
    }

    @Test
    public void testIsEditableDefault() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // default to true
        Assert.assertTrue("isEditable default true", e.isEditable());
    }

    @Test
    public void testSetAllEditableFalse() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        e.setAllEditable(false);
        Assert.assertFalse("isEditable after setAllEditable(false)", e.isEditable());
    }

    @Test
    public void testSetAllEditableTrue() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        e.setAllEditable(true);
        Assert.assertTrue("isEditable after setAllEditable(true)", e.isEditable());
    }

    @Test
    public void testGetHighlightSelectedBlockDefault() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // default to false
        Assert.assertFalse("le.getHighlightSelectedBlock default false", e.getHighlightSelectedBlock());
    }

    @Test
    public void testSetHighlightSelectedBlockTrue() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        e.setHighlightSelectedBlock(true);
        // setHighlightSelectedBlock performs some GUI actions, so give
        // the AWT queue some time to clear.
        new QueueTool().waitEmpty(100);
        Assert.assertTrue("le.getHighlightSelectedBlock after setHighlightSelectedBlock(true)", e.getHighlightSelectedBlock());
    }

    @Test
    @Ignore("unreliable on CI servers")
    public void testSetHighlightSelectedBlockFalse() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        e.setHighlightSelectedBlock(false);
        // setHighlightSelectedBlock performs some GUI actions, so give
        // the AWT queue some time to clear.
        new QueueTool().waitEmpty(100);
        Assert.assertFalse("le.getHighlightSelectedBlock after setHighlightSelectedBlock(false)", e.getHighlightSelectedBlock());
    }

    @Test
    public void checkOptionsMenuExists() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JMenuOperator jmo = new JMenuOperator(jfo, Bundle.getMessage("MenuOptions"));
        Assert.assertNotNull("Options Menu Exists", jmo);
        Assert.assertEquals("Menu Item Count", 17, jmo.getItemCount());
    }

    @Test
    public void checkToolsMenuExists() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JMenuOperator jmo = new JMenuOperator(jfo, Bundle.getMessage("MenuTools"));
        Assert.assertNotNull("Tools Menu Exists", jmo);
        Assert.assertEquals("Tools Menu Item Count", 20, jmo.getItemCount());
    }

    @Test
    public void checkZoomMenuExists() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JMenuOperator jmo = new JMenuOperator(jfo, Bundle.getMessage("MenuZoom"));
        Assert.assertNotNull("Zoom Menu Exists", jmo);
        Assert.assertEquals("Menu Item Count", 16, jmo.getItemCount());
    }

    @Test
    public void checkMarkerMenuExists() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JMenuOperator jmo = new JMenuOperator(jfo, Bundle.getMessage("MenuMarker"));
        Assert.assertNotNull("Marker Menu Exists", jmo);
        Assert.assertEquals("Menu Item Count", 3, jmo.getItemCount());
    }

    @Test
    public void checkDispatcherMenuExists() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JMenuOperator jmo = new JMenuOperator(jfo, Bundle.getMessage("MenuDispatcher"));
        Assert.assertNotNull("Dispatcher Menu Exists", jmo);
        Assert.assertEquals("Menu Item Count", 2, jmo.getItemCount());
    }

    @Test
    @Ignore("Fails on AppVeyor, macOS and Windows 12/20/2019")
    public void testToolBarPositionLeft() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JMenuOperator jmo = new JMenuOperator(jfo, Bundle.getMessage("MenuOptions"));

        //switch to Left
        jmo.pushMenuNoBlock(Bundle.getMessage("MenuOptions") + "/"
                + Bundle.getMessage("ToolBar") + "/"
                + Bundle.getMessage("ToolBarSide") + "/"
                + Bundle.getMessage("ToolBarSideLeft"), "/");

        new EventTool().waitNoEvent(200);

        //back to Top
        jmo.pushMenu(Bundle.getMessage("MenuOptions") + "/"
                + Bundle.getMessage("ToolBar") + "/"
                + Bundle.getMessage("ToolBarSide") + "/"
                + Bundle.getMessage("ToolBarSideTop"), "/");
    }

    @Test
    @Ignore("Fails on AppVeyor, macOS and Windows 12/20/2019")
    public void testToolBarPositionBottom() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JMenuOperator jmo = new JMenuOperator(jfo, Bundle.getMessage("MenuOptions"));

        //switch to Bottom
        jmo.pushMenuNoBlock(Bundle.getMessage("MenuOptions") + "/"
                + Bundle.getMessage("ToolBar") + "/"
                + Bundle.getMessage("ToolBarSide") + "/"
                + Bundle.getMessage("ToolBarSideBottom"), "/");

        new EventTool().waitNoEvent(200);

        //back to Top
        jmo.pushMenu(Bundle.getMessage("MenuOptions") + "/"
                + Bundle.getMessage("ToolBar") + "/"
                + Bundle.getMessage("ToolBarSide") + "/"
                + Bundle.getMessage("ToolBarSideTop"), "/");
    }

    @Test
    @Ignore("Fails on AppVeyor, macOS and Windows 12/20/2019")
    public void testToolBarPositionRight() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JMenuOperator jmo = new JMenuOperator(jfo, Bundle.getMessage("MenuOptions"));

        //switch to Right
        jmo.pushMenuNoBlock(Bundle.getMessage("MenuOptions") + "/"
                + Bundle.getMessage("ToolBar") + "/"
                + Bundle.getMessage("ToolBarSide") + "/"
                + Bundle.getMessage("ToolBarSideRight"), "/");

        new EventTool().waitNoEvent(200);

        //back to Top
        jmo.pushMenu(Bundle.getMessage("MenuOptions") + "/"
                + Bundle.getMessage("ToolBar") + "/"
                + Bundle.getMessage("ToolBarSide") + "/"
                + Bundle.getMessage("ToolBarSideTop"), "/");
    }

    @Test
    @Ignore("Fails on AppVeyor, macOS and Windows 12/20/2019")
    public void testToolBarPositionFloat() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JMenuOperator jmo = new JMenuOperator(jfo, Bundle.getMessage("MenuOptions"));

        //switch to Float
        jmo.pushMenuNoBlock(Bundle.getMessage("MenuOptions") + "/"
                + Bundle.getMessage("ToolBar") + "/"
                + Bundle.getMessage("ToolBarSide") + "/"
                + Bundle.getMessage("ToolBarSideFloat"), "/");

        // bring this window back to the front...
        jfo.activate();

        //back to Top
        jmo.pushMenu(Bundle.getMessage("MenuOptions") + "/"
                + Bundle.getMessage("ToolBar") + "/"
                + Bundle.getMessage("ToolBarSide") + "/"
                + Bundle.getMessage("ToolBarSideTop"), "/");
    }

    @Test
    public void testGetLEAuxTools() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        LayoutEditorAuxTools t = e.getLEAuxTools();
        Assert.assertNotNull("tools exist", t);
        JUnitUtil.dispose(e);
    }

    // private final static Logger log = LoggerFactory.getLogger(LayoutEditorTest.class.getName());
}
