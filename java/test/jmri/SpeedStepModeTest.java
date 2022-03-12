package jmri;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Test simple functioning of SpeedStepMode
 *
 * @author Austin Hendrix Copyright (C) 2019
 */
public class SpeedStepModeTest {

    @Test
    public void testValidValues() {
        for( SpeedStepMode mode : SpeedStepMode.values()) {
            Assert.assertNotNull(mode.name);
            Assert.assertTrue(mode.name.length() > 0);

            Assert.assertNotNull(mode.description);
            Assert.assertTrue(mode.description.length() > 0);

            Assert.assertTrue(mode.numSteps > 0);

            Assert.assertTrue(mode.increment >= 0);
        }
    }

}
