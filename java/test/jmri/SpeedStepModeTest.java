package jmri;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test simple functioning of SpeedStepMode
 *
 * @author Austin Hendrix Copyright (C) 2019
 */
public class SpeedStepModeTest {

    @Test
    public void testValidValues() {
        for( SpeedStepMode mode : SpeedStepMode.values()) {
            assertNotNull(mode.name);
            assertTrue(mode.name.length() > 0);

            assertNotNull(mode.description);
            assertTrue(mode.description.length() > 0);

            assertTrue(mode.numSteps > 0);

            assertTrue(mode.increment >= 0);
        }
    }

}
