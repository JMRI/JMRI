package jmri;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the Light class
 *
 * @author Bob Jacobsen Copyright (C) 2008, 2010
 */
public class LightTest {

    @Test
    @SuppressWarnings("all")
    public void testStateConstants() {
        assertTrue( (Light.ON & Light.OFF) == 0, "On and Off differ");
        assertTrue( (Light.ON & Light.UNKNOWN) == 0, "On and Unknown differ");
        assertTrue( (Light.OFF & Light.UNKNOWN) == 0, "Off and Unknown differ");
        assertTrue( (Light.ON & Light.INCONSISTENT) == 0, "On and Inconsistent differ");
        assertTrue( (Light.OFF & Light.INCONSISTENT) == 0, "Off and Inconsistent differ");
    }

    @Test
    @SuppressWarnings("all")
    public void testTransitionConstants() {
        assertTrue( (Light.ON & Light.INTERMEDIATE) == 0, "On and INTERMEDIATE are bits");

        assertTrue( (Light.TRANSITIONINGTOFULLON & Light.TRANSITIONING) != 0, "TRANSITIONINGTOFULLON overlap");
        assertTrue( (Light.TRANSITIONINGHIGHER & Light.TRANSITIONING) != 0, "TRANSITIONINGHIGHER overlap");
        assertTrue( (Light.TRANSITIONINGLOWER & Light.TRANSITIONING) != 0, "TRANSITIONINGLOWER overlap");
        assertTrue( (Light.TRANSITIONINGTOFULLOFF & Light.TRANSITIONING) != 0, "TRANSITIONINGTOFULLOFF overlap");
    }
    
    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

}
