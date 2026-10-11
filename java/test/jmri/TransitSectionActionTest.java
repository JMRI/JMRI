package jmri;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for TransitSectionAction class.
 *
 * @author Bob Jacobsen Copyright (C) 2017
 * @author Paul Bender Copyright (C) 2016
 **/
public class TransitSectionActionTest {

    @Test
    public void testShortConstructor(){
        TransitSectionAction t = new TransitSectionAction(2,3);
        Assertions.assertNotNull(t, "Constructor" );
        assertEquals(2, t.getWhenCode() );
        assertEquals(3, t.getWhatCode() );

        // check defaults
        assertEquals(-1, t.getDataWhen());
        assertEquals(-1, t.getDataWhat1());
        assertEquals(-1, t.getDataWhat2());
        assertEquals("", t.getStringWhen());
        assertEquals("", t.getStringWhat());
    }

    @Test
    public void testLongConstructor(){
        TransitSectionAction t = new TransitSectionAction(4,5,6,7,8,"a","b");
        Assertions.assertNotNull(t, "Constructor");
        assertEquals(4, t.getWhenCode());
        assertEquals(5, t.getWhatCode());

        assertEquals(6, t.getDataWhen());
        assertEquals(7, t.getDataWhat1());
        assertEquals(8, t.getDataWhat2());
        assertEquals("a", t.getStringWhen());
        assertEquals("b", t.getStringWhat());
    }

    @Test
    public void testWhenCodeDataIndependent(){
        TransitSectionAction t = new TransitSectionAction(11,12,13,14,15,"A","B");

        assertEquals(11, t.getWhenCode());
        assertEquals(13, t.getDataWhen());

        t.setWhenCode(21);

        assertEquals(21, t.getWhenCode());
        assertEquals(13, t.getDataWhen());

        t.setDataWhen(32);

        assertEquals(21, t.getWhenCode());
        assertEquals(32, t.getDataWhen());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();

        JUnitUtil.initDefaultUserMessagePreferences();
   }

    @AfterEach
    public void tearDown(){
        JUnitUtil.tearDown();
    }

}
