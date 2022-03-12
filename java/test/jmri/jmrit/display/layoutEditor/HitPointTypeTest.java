package jmri.jmrit.display.layoutEditor;

import jmri.util.*;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Test simple functioning of HitPointType.
 *
 * @author Paul Bender Copyright (C) 2016
 * @author George Warner Copyright (C) 2019
 * @author Bob Jacobsen Copyright (C) 2020
 */
public class HitPointTypeTest {

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

       // *****************************************************************
       //    Turntable Ray support
       // *****************************************************************
       
    @Test
    public void testHPTturntableTrackIndexOK() {
        Assert.assertEquals(0, HitPointType.TURNTABLE_RAY_0.turntableTrackIndex());
        Assert.assertEquals(1, HitPointType.TURNTABLE_RAY_1.turntableTrackIndex());
        Assert.assertEquals(2, HitPointType.TURNTABLE_RAY_2.turntableTrackIndex());
        // ..
        Assert.assertEquals(62, HitPointType.TURNTABLE_RAY_62.turntableTrackIndex());
        Assert.assertEquals(63, HitPointType.TURNTABLE_RAY_63.turntableTrackIndex());        
    }
    
    @Test
    public void testHPTturntableTrackIndexBad1() {
        Assert.assertThrows(IllegalArgumentException.class, () -> HitPointType.POS_POINT.turntableTrackIndex());
    }

    @Test
    public void testHPTturntableTrackIndexBad2() {
        Assert.assertThrows(IllegalArgumentException.class, () -> HitPointType.SHAPE_POINT_9.turntableTrackIndex());
    }

    @Test
    public void testHPTturntableTrackIndexedValueOK() {
        Assert.assertEquals(HitPointType.TURNTABLE_RAY_0, HitPointType.turntableTrackIndexedValue(0));
        Assert.assertEquals(HitPointType.TURNTABLE_RAY_1, HitPointType.turntableTrackIndexedValue(1));
        Assert.assertEquals(HitPointType.TURNTABLE_RAY_2, HitPointType.turntableTrackIndexedValue(2));
        // ..
        Assert.assertEquals(HitPointType.TURNTABLE_RAY_61, HitPointType.turntableTrackIndexedValue(61));
        Assert.assertEquals(HitPointType.TURNTABLE_RAY_62, HitPointType.turntableTrackIndexedValue(62));
        Assert.assertEquals(HitPointType.TURNTABLE_RAY_63, HitPointType.turntableTrackIndexedValue(63));
    }
    
    @Test
    public void testHPTturntableTrackIndexedValueBad1() throws IllegalArgumentException {
        Assert.assertThrows(IllegalArgumentException.class, () -> HitPointType.turntableTrackIndexedValue(-1));
    }

    @Test
    public void testHPTturntableTrackIndexedValueBad2() {
        Assert.assertThrows(IllegalArgumentException.class, () -> HitPointType.turntableTrackIndexedValue(64));
    }

    @Test
    public void testHPTturntableValues() {
        HitPointType[] array = HitPointType.turntableValues();
        Assert.assertEquals(64, array.length);
        Assert.assertEquals(HitPointType.TURNTABLE_RAY_0, array[0]);
        Assert.assertEquals(HitPointType.TURNTABLE_RAY_1, array[1]);
        // ..
        Assert.assertEquals(HitPointType.TURNTABLE_RAY_62, array[62]);
        Assert.assertEquals(HitPointType.TURNTABLE_RAY_63, array[63]);
        
        // while available, use the indexing operations to test content
        for (int i = 0; i<array.length; i++) {
            Assert.assertEquals(HitPointType.turntableTrackIndexedValue(i),array[i]);
        }
    }

       // *****************************************************************
       //    Shape Point support
       // *****************************************************************
       
    @Test
    public void testHPTshapePointIndexOK() {
        Assert.assertEquals(0, HitPointType.SHAPE_POINT_0.shapePointIndex());
        Assert.assertEquals(1, HitPointType.SHAPE_POINT_1.shapePointIndex());
        Assert.assertEquals(2, HitPointType.SHAPE_POINT_2.shapePointIndex());
        // ..
        Assert.assertEquals(8, HitPointType.SHAPE_POINT_8.shapePointIndex());
        Assert.assertEquals(9, HitPointType.SHAPE_POINT_9.shapePointIndex());        
    }
    
    @Test
    public void testHPTshapePointIndexBad1() {
        Assert.assertThrows(IllegalArgumentException.class, () -> HitPointType.POS_POINT.shapePointIndex());
    }

    @Test
    public void testHPTshapePointIndexBad2() {
        Assert.assertThrows(IllegalArgumentException.class, () -> HitPointType.TURNTABLE_RAY_0.shapePointIndex());
    }

    @Test
    public void testHPTshapePointIndexedValueOK() {
        Assert.assertEquals(HitPointType.SHAPE_POINT_0, HitPointType.shapePointIndexedValue(0));
        Assert.assertEquals(HitPointType.SHAPE_POINT_1, HitPointType.shapePointIndexedValue(1));
        Assert.assertEquals(HitPointType.SHAPE_POINT_2, HitPointType.shapePointIndexedValue(2));
        // ..
        Assert.assertEquals(HitPointType.SHAPE_POINT_7, HitPointType.shapePointIndexedValue(7));
        Assert.assertEquals(HitPointType.SHAPE_POINT_8, HitPointType.shapePointIndexedValue(8));
        Assert.assertEquals(HitPointType.SHAPE_POINT_9, HitPointType.shapePointIndexedValue(9));
    }
    
    @Test
    public void testHPTshapePointIndexedValueBad1() {
        Assert.assertThrows(IllegalArgumentException.class, () -> HitPointType.shapePointIndexedValue(-1));
    }

    @Test
    public void testHPTshapePointIndexedValueBad2() {
        Assert.assertThrows(IllegalArgumentException.class, () -> HitPointType.shapePointIndexedValue(10));
    }

    @Test
    public void testHPTshapePointValues() {
        HitPointType[] array = HitPointType.shapePointValues();
        Assert.assertEquals(10, array.length);
        Assert.assertEquals(HitPointType.SHAPE_POINT_0, array[0]);
        Assert.assertEquals(HitPointType.SHAPE_POINT_1, array[1]);
        // ..
        Assert.assertEquals(HitPointType.SHAPE_POINT_8, array[8]);
        Assert.assertEquals(HitPointType.SHAPE_POINT_9, array[9]);
        
        // while available, use the indexing operations to test content
        for (int i = 0; i<array.length; i++) {
            Assert.assertEquals(HitPointType.shapePointIndexedValue(i),array[i]);
        }
    }

       // *****************************************************************
       //    Bezier Point support
       // *****************************************************************
       
    @Test
    public void testHPTbezierPointIndexOK() {
        Assert.assertEquals(0, HitPointType.BEZIER_CONTROL_POINT_0.bezierPointIndex());
        Assert.assertEquals(1, HitPointType.BEZIER_CONTROL_POINT_1.bezierPointIndex());
        Assert.assertEquals(2, HitPointType.BEZIER_CONTROL_POINT_2.bezierPointIndex());
        // ..
        Assert.assertEquals(7, HitPointType.BEZIER_CONTROL_POINT_7.bezierPointIndex());
        Assert.assertEquals(8, HitPointType.BEZIER_CONTROL_POINT_8.bezierPointIndex());        
    }
    
    @Test
    public void testHPTbezierPointIndexBad1() {
        Assert.assertThrows(IllegalArgumentException.class, () -> HitPointType.POS_POINT.bezierPointIndex());
    }

    @Test
    public void testHPTbezierPointIndexBad2() {
        Assert.assertThrows(IllegalArgumentException.class, () -> HitPointType.TURNTABLE_RAY_0.bezierPointIndex());
    }

    @Test
    public void testHPTbezierPointIndexedValueOK() {
        Assert.assertEquals(HitPointType.BEZIER_CONTROL_POINT_0, HitPointType.bezierPointIndexedValue(0));
        Assert.assertEquals(HitPointType.BEZIER_CONTROL_POINT_1, HitPointType.bezierPointIndexedValue(1));
        Assert.assertEquals(HitPointType.BEZIER_CONTROL_POINT_2, HitPointType.bezierPointIndexedValue(2));
        // ..
        Assert.assertEquals(HitPointType.BEZIER_CONTROL_POINT_6, HitPointType.bezierPointIndexedValue(6));
        Assert.assertEquals(HitPointType.BEZIER_CONTROL_POINT_7, HitPointType.bezierPointIndexedValue(7));
        Assert.assertEquals(HitPointType.BEZIER_CONTROL_POINT_8, HitPointType.bezierPointIndexedValue(8));
    }
    
    @Test
    public void testHPTbezierPointIndexedValueBad1() {
        Assert.assertThrows(IllegalArgumentException.class, () -> HitPointType.bezierPointIndexedValue(-1));
    }

    @Test
    public void testHPTbezierPointIndexedValueBad2() {
        Assert.assertThrows(IllegalArgumentException.class, () -> HitPointType.bezierPointIndexedValue(9));
    }
    
        // *****************************************************************
        // General Enum tests
        // *****************************************************************
    
    /** 
     * The specific values are tested for historical i/O compatibility
     * and because some internal operations do arithmetic with them.
     * For now, it's sufficient for this test to ensure they don't change,
     * hence this test.
     */
    @Test
    public void testEnumIntegerValues() {
        Assert.assertEquals("HitPointTypes.NONE.getValue() == 0", HitPointType.NONE.ordinal(), 0);
        Assert.assertEquals("HitPointTypes.POS_POINT.getValue() == 1", HitPointType.POS_POINT.ordinal(), 1);
        Assert.assertEquals("HitPointTypes.TURNOUT_A.getValue() == 2", HitPointType.TURNOUT_A.ordinal(), 2);
        Assert.assertEquals("HitPointTypes.TURNOUT_B.getValue() == 3", HitPointType.TURNOUT_B.ordinal(), 3);
        Assert.assertEquals("HitPointTypes.TURNOUT_C.getValue() == 4", HitPointType.TURNOUT_C.ordinal(), 4);
        Assert.assertEquals("HitPointTypes.TURNOUT_D.getValue() == 5", HitPointType.TURNOUT_D.ordinal(), 5);
        Assert.assertEquals("HitPointTypes.LEVEL_XING_A.getValue() == 6", HitPointType.LEVEL_XING_A.ordinal(), 6);
        Assert.assertEquals("HitPointTypes.LEVEL_XING_B.getValue() == 7", HitPointType.LEVEL_XING_B.ordinal(), 7);
        Assert.assertEquals("HitPointTypes.LEVEL_XING_C.getValue() == 8", HitPointType.LEVEL_XING_C.ordinal(), 8);
        Assert.assertEquals("HitPointTypes.LEVEL_XING_D.getValue() == 9", HitPointType.LEVEL_XING_D.ordinal(), 9);
        Assert.assertEquals("HitPointTypes.TRACK.getValue() == 10", HitPointType.TRACK.ordinal(), 10);
        Assert.assertEquals("HitPointTypes.TURNOUT_CENTER.getValue() == 11", HitPointType.TURNOUT_CENTER.ordinal(), 11);
        Assert.assertEquals("HitPointTypes.LEVEL_XING_CENTER.getValue() == 12", HitPointType.LEVEL_XING_CENTER.ordinal(), 12);
        Assert.assertEquals("HitPointTypes.TURNTABLE_CENTER.getValue() == 13", HitPointType.TURNTABLE_CENTER.ordinal(), 13);
        Assert.assertEquals("HitPointTypes.LAYOUT_POS_LABEL.getValue() == 14", HitPointType.LAYOUT_POS_LABEL.ordinal(), 14);
        Assert.assertEquals("HitPointTypes.LAYOUT_POS_JCOMP.getValue() == 15", HitPointType.LAYOUT_POS_JCOMP.ordinal(), 15);
        Assert.assertEquals("HitPointTypes.MULTI_SENSOR.getValue() == 16", HitPointType.MULTI_SENSOR.ordinal(), 16);
        Assert.assertEquals("HitPointTypes.MARKER.getValue() == 17", HitPointType.MARKER.ordinal(), 17);
        Assert.assertEquals("HitPointTypes.TRACK_CIRCLE_CENTRE.getValue() == 18", HitPointType.TRACK_CIRCLE_CENTRE.ordinal(), 18);
        Assert.assertEquals("HitPointTypes.UNUSED_19.getValue() == 19", HitPointType.UNUSED_19.ordinal(), 19);
        Assert.assertEquals("HitPointTypes.SLIP_CENTER.getValue() == 20", HitPointType.SLIP_CENTER.ordinal(), 20);
        Assert.assertEquals("HitPointTypes.SLIP_A.getValue() == 21", HitPointType.SLIP_A.ordinal(), 21);
        Assert.assertEquals("HitPointTypes.SLIP_B.getValue() == 22", HitPointType.SLIP_B.ordinal(), 22);
        Assert.assertEquals("HitPointTypes.SLIP_C.getValue() == 23", HitPointType.SLIP_C.ordinal(), 23);
        Assert.assertEquals("HitPointTypes.SLIP_D.getValue() == 24", HitPointType.SLIP_D.ordinal(), 24);
        Assert.assertEquals("HitPointTypes.SLIP_LEFT.getValue() == 25", HitPointType.SLIP_LEFT.ordinal(), 25);
        Assert.assertEquals("HitPointTypes.SLIP_RIGHT.getValue() == 26", HitPointType.SLIP_RIGHT.ordinal(), 26);
        Assert.assertEquals("HitPointTypes.UNUSED_27.getValue() == 27", HitPointType.UNUSED_27.ordinal(), 27);
        Assert.assertEquals("HitPointTypes.UNUSED_28.getValue() == 28", HitPointType.UNUSED_28.ordinal(), 28);
        Assert.assertEquals("HitPointTypes.UNUSED_29.getValue() == 29", HitPointType.UNUSED_29.ordinal(), 29);
        Assert.assertEquals("HitPointTypes.BEZIER_CONTROL_POINT_0.getValue() == 30", HitPointType.BEZIER_CONTROL_POINT_0.ordinal(), 30);
        Assert.assertEquals("HitPointTypes.BEZIER_CONTROL_POINT_1.getValue() == 31", HitPointType.BEZIER_CONTROL_POINT_1.ordinal(), 31);
        Assert.assertEquals("HitPointTypes.BEZIER_CONTROL_POINT_2.getValue() == 32", HitPointType.BEZIER_CONTROL_POINT_2.ordinal(), 32);
        Assert.assertEquals("HitPointTypes.BEZIER_CONTROL_POINT_3.getValue() == 33", HitPointType.BEZIER_CONTROL_POINT_3.ordinal(), 33);
        Assert.assertEquals("HitPointTypes.BEZIER_CONTROL_POINT_4.getValue() == 34", HitPointType.BEZIER_CONTROL_POINT_4.ordinal(), 34);
        Assert.assertEquals("HitPointTypes.BEZIER_CONTROL_POINT_5.getValue() == 35", HitPointType.BEZIER_CONTROL_POINT_5.ordinal(), 35);
        Assert.assertEquals("HitPointTypes.BEZIER_CONTROL_POINT_6.getValue() == 36", HitPointType.BEZIER_CONTROL_POINT_6.ordinal(), 36);
        Assert.assertEquals("HitPointTypes.BEZIER_CONTROL_POINT_7.getValue() == 37", HitPointType.BEZIER_CONTROL_POINT_7.ordinal(), 37);
        Assert.assertEquals("HitPointTypes.BEZIER_CONTROL_POINT_8.getValue() == 38", HitPointType.BEZIER_CONTROL_POINT_8.ordinal(), 38);
        Assert.assertEquals("HitPointTypes.SHAPE_CENTER.getValue() == 39", HitPointType.SHAPE_CENTER.ordinal(), 39);
        Assert.assertEquals("HitPointTypes.SHAPE_POINT_0.getValue() == 40", HitPointType.SHAPE_POINT_0.ordinal(), 40);
        Assert.assertEquals("HitPointTypes.SHAPE_POINT_1.getValue() == 41", HitPointType.SHAPE_POINT_1.ordinal(), 41);
        Assert.assertEquals("HitPointTypes.SHAPE_POINT_2.getValue() == 42", HitPointType.SHAPE_POINT_2.ordinal(), 42);
        Assert.assertEquals("HitPointTypes.SHAPE_POINT_3.getValue() == 43", HitPointType.SHAPE_POINT_3.ordinal(), 43);
        Assert.assertEquals("HitPointTypes.SHAPE_POINT_4.getValue() == 44", HitPointType.SHAPE_POINT_4.ordinal(), 44);
        Assert.assertEquals("HitPointTypes.SHAPE_POINT_5.getValue() == 45", HitPointType.SHAPE_POINT_5.ordinal(), 45);
        Assert.assertEquals("HitPointTypes.SHAPE_POINT_6.getValue() == 46", HitPointType.SHAPE_POINT_6.ordinal(), 46);
        Assert.assertEquals("HitPointTypes.SHAPE_POINT_7.getValue() == 47", HitPointType.SHAPE_POINT_7.ordinal(), 47);
        Assert.assertEquals("HitPointTypes.SHAPE_POINT_8.getValue() == 48", HitPointType.SHAPE_POINT_8.ordinal(), 48);
        Assert.assertEquals("HitPointTypes.SHAPE_POINT_9.getValue() == 49", HitPointType.SHAPE_POINT_9.ordinal(), 49);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_0.getValue() == 50", HitPointType.TURNTABLE_RAY_0.ordinal(), 50);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_1.getValue() == 51", HitPointType.TURNTABLE_RAY_1.ordinal(), 51);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_2.getValue() == 52", HitPointType.TURNTABLE_RAY_2.ordinal(), 52);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_3.getValue() == 53", HitPointType.TURNTABLE_RAY_3.ordinal(), 53);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_4.getValue() == 54", HitPointType.TURNTABLE_RAY_4.ordinal(), 54);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_5.getValue() == 55", HitPointType.TURNTABLE_RAY_5.ordinal(), 55);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_6.getValue() == 56", HitPointType.TURNTABLE_RAY_6.ordinal(), 56);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_7.getValue() == 57", HitPointType.TURNTABLE_RAY_7.ordinal(), 57);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_8.getValue() == 58", HitPointType.TURNTABLE_RAY_8.ordinal(), 58);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_9.getValue() == 59", HitPointType.TURNTABLE_RAY_9.ordinal(), 59);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_10.getValue() == 60", HitPointType.TURNTABLE_RAY_10.ordinal(), 60);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_11.getValue() == 61", HitPointType.TURNTABLE_RAY_11.ordinal(), 61);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_12.getValue() == 62", HitPointType.TURNTABLE_RAY_12.ordinal(), 62);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_13.getValue() == 63", HitPointType.TURNTABLE_RAY_13.ordinal(), 63);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_14.getValue() == 64", HitPointType.TURNTABLE_RAY_14.ordinal(), 64);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_15.getValue() == 65", HitPointType.TURNTABLE_RAY_15.ordinal(), 65);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_16.getValue() == 66", HitPointType.TURNTABLE_RAY_16.ordinal(), 66);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_17.getValue() == 67", HitPointType.TURNTABLE_RAY_17.ordinal(), 67);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_18.getValue() == 68", HitPointType.TURNTABLE_RAY_18.ordinal(), 68);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_19.getValue() == 69", HitPointType.TURNTABLE_RAY_19.ordinal(), 69);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_20.getValue() == 70", HitPointType.TURNTABLE_RAY_20.ordinal(), 70);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_21.getValue() == 71", HitPointType.TURNTABLE_RAY_21.ordinal(), 71);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_22.getValue() == 72", HitPointType.TURNTABLE_RAY_22.ordinal(), 72);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_23.getValue() == 73", HitPointType.TURNTABLE_RAY_23.ordinal(), 73);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_24.getValue() == 74", HitPointType.TURNTABLE_RAY_24.ordinal(), 74);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_25.getValue() == 75", HitPointType.TURNTABLE_RAY_25.ordinal(), 75);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_26.getValue() == 76", HitPointType.TURNTABLE_RAY_26.ordinal(), 76);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_27.getValue() == 77", HitPointType.TURNTABLE_RAY_27.ordinal(), 77);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_28.getValue() == 78", HitPointType.TURNTABLE_RAY_28.ordinal(), 78);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_29.getValue() == 79", HitPointType.TURNTABLE_RAY_29.ordinal(), 79);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_30.getValue() == 80", HitPointType.TURNTABLE_RAY_30.ordinal(), 80);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_31.getValue() == 81", HitPointType.TURNTABLE_RAY_31.ordinal(), 81);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_32.getValue() == 82", HitPointType.TURNTABLE_RAY_32.ordinal(), 82);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_33.getValue() == 83", HitPointType.TURNTABLE_RAY_33.ordinal(), 83);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_34.getValue() == 84", HitPointType.TURNTABLE_RAY_34.ordinal(), 84);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_35.getValue() == 85", HitPointType.TURNTABLE_RAY_35.ordinal(), 85);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_36.getValue() == 86", HitPointType.TURNTABLE_RAY_36.ordinal(), 86);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_37.getValue() == 87", HitPointType.TURNTABLE_RAY_37.ordinal(), 87);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_38.getValue() == 88", HitPointType.TURNTABLE_RAY_38.ordinal(), 88);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_39.getValue() == 89", HitPointType.TURNTABLE_RAY_39.ordinal(), 89);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_40.getValue() == 90", HitPointType.TURNTABLE_RAY_40.ordinal(), 90);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_41.getValue() == 91", HitPointType.TURNTABLE_RAY_41.ordinal(), 91);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_42.getValue() == 92", HitPointType.TURNTABLE_RAY_42.ordinal(), 92);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_43.getValue() == 93", HitPointType.TURNTABLE_RAY_43.ordinal(), 93);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_44.getValue() == 94", HitPointType.TURNTABLE_RAY_44.ordinal(), 94);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_45.getValue() == 95", HitPointType.TURNTABLE_RAY_45.ordinal(), 95);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_46.getValue() == 96", HitPointType.TURNTABLE_RAY_46.ordinal(), 96);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_47.getValue() == 97", HitPointType.TURNTABLE_RAY_47.ordinal(), 97);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_48.getValue() == 98", HitPointType.TURNTABLE_RAY_48.ordinal(), 98);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_49.getValue() == 99", HitPointType.TURNTABLE_RAY_49.ordinal(), 99);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_50.getValue() == 100", HitPointType.TURNTABLE_RAY_50.ordinal(), 100);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_51.getValue() == 101", HitPointType.TURNTABLE_RAY_51.ordinal(), 101);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_52.getValue() == 102", HitPointType.TURNTABLE_RAY_52.ordinal(), 102);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_53.getValue() == 103", HitPointType.TURNTABLE_RAY_53.ordinal(), 103);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_54.getValue() == 104", HitPointType.TURNTABLE_RAY_54.ordinal(), 104);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_55.getValue() == 105", HitPointType.TURNTABLE_RAY_55.ordinal(), 105);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_56.getValue() == 106", HitPointType.TURNTABLE_RAY_56.ordinal(), 106);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_57.getValue() == 107", HitPointType.TURNTABLE_RAY_57.ordinal(), 107);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_58.getValue() == 108", HitPointType.TURNTABLE_RAY_58.ordinal(), 108);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_59.getValue() == 109", HitPointType.TURNTABLE_RAY_59.ordinal(), 109);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_60.getValue() == 110", HitPointType.TURNTABLE_RAY_60.ordinal(), 110);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_61.getValue() == 111", HitPointType.TURNTABLE_RAY_61.ordinal(), 111);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_62.getValue() == 112", HitPointType.TURNTABLE_RAY_62.ordinal(), 112);
        Assert.assertEquals("HitPointTypes.TURNTABLE_RAY_63.getValue() == 113", HitPointType.TURNTABLE_RAY_63.ordinal(), 113);
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HitPointTypeTest.class.getName());
}
