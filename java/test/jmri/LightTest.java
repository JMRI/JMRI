package jmri;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the Light class
 *
 * @author	Bob Jacobsen Copyright (C) 2008, 2010
 */
public class LightTest {

    @Test
    @SuppressWarnings("all")
    public void testStateConstants() {
        Assert.assertTrue("On and Off differ", (Light.ON & Light.OFF) == 0);
        Assert.assertTrue("On and Unknown differ", (Light.ON & Light.UNKNOWN) == 0);
        Assert.assertTrue("Off and Unknown differ", (Light.OFF & Light.UNKNOWN) == 0);
        Assert.assertTrue("On and Inconsistent differ", (Light.ON & Light.INCONSISTENT) == 0);
        Assert.assertTrue("Off and Inconsistent differ", (Light.OFF & Light.INCONSISTENT) == 0);
    }

    @Test
    @SuppressWarnings("all")
    public void testTransitionConstants() {
        Assert.assertTrue("On and INTERMEDIATE are bits", (Light.ON & Light.INTERMEDIATE) == 0);

        Assert.assertTrue("TRANSITIONINGTOFULLON overlap", (Light.TRANSITIONINGTOFULLON & Light.TRANSITIONING) != 0);
        Assert.assertTrue("TRANSITIONINGHIGHER overlap", (Light.TRANSITIONINGHIGHER & Light.TRANSITIONING) != 0);
        Assert.assertTrue("TRANSITIONINGLOWER overlap", (Light.TRANSITIONINGLOWER & Light.TRANSITIONING) != 0);
        Assert.assertTrue("TRANSITIONINGTOFULLOFF overlap", (Light.TRANSITIONINGTOFULLOFF & Light.TRANSITIONING) != 0);
    }
}
