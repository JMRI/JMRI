package jmri.jmrit.timetable;

import org.junit.jupiter.api.*;

import jmri.util.JUnitUtil;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Layout Class
 * @author Dave Sand Copyright (C) 2018
 */
public class LayoutTest {

    @Test
    public void testCreate() {
        Assertions.assertNotNull( new Layout() );
    }

    @Test
    public void testSettersAndGetters() {
        Layout layout = new Layout();
        assertNotNull(layout);
        layout.setLayoutName("Test Name");  // NOI18N
        assertEquals("Test Name", layout.getLayoutName());  // NOI18N
        assertEquals("HO", layout.getScale().getScaleName());  // NOI18N

        Exception ex = assertThrows(IllegalArgumentException.class, () -> { layout.setFastClock(0); });
        assertNotNull(ex);
        assertEquals("FastClockLt1", ex.getMessage());

        // supplying ridiculous fast clock values is self correcting since the user gets ridiculous timetable results
        assertDoesNotThrow( () -> layout.setFastClock(9999));

        assertDoesNotThrow( () -> layout.setFastClock(6));
        assertEquals(6, layout.getFastClock());
        assertTrue(layout.getRatio() > 0.0f);

        ex = assertThrows(IllegalArgumentException.class, () -> { layout.setThrottles( -2 ); });
        assertNotNull(ex);
        assertEquals("ThrottlesLt0", ex.getMessage());

        layout.setThrottles(3);
        assertEquals(3, layout.getThrottles());
        layout.setMetric(true);
        assertTrue(layout.getMetric());
        layout.setScaleMK();
        assertEquals(1.914, layout.getScaleMK(), .1);
        assertEquals("Test Name", layout.toString());  // NOI18N
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
//     private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutTest.class);
}
