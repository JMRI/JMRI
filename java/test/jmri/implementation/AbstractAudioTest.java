package jmri.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.*;

/**
 * Tests for AbstractAudio
 */
public class AbstractAudioTest {

    private final AtomicBoolean stateHasChanged = new AtomicBoolean(false);

    @Test
    public void testCtor() {
        MyAbstractAudio audio = new MyAbstractAudio();
        assertNotNull(audio, "AbstractAudio constructor return");
    }

    @Test
    public void testState() {
        MyAbstractAudio audio = new MyAbstractAudio();
        
        assertEquals(jmri.Audio.STATE_INITIAL, audio.getState(), "state is initial");
        audio.setState(jmri.Audio.STATE_PLAYING);
        assertEquals(jmri.Audio.STATE_PLAYING, audio.getState(), "state is playing");
        // Check that audio.setState() triggers stateChanged()
        assertTrue(stateHasChanged.get(), "state has changed");
        assertEquals("MySystemName", audio.toString(), "toString() matches");

        assertEquals("Audio", audio.getBeanType(), "getBeanType() matches");
    }

    @Test
    public void testRoundDecimal() {
        // Test AbstractAudio.roundDecimal()
        assertTrue(AbstractAudio.roundDecimal((float) 10.5555555, 1) > 10.59, "test roundDecimal()");
        assertTrue(AbstractAudio.roundDecimal((float) 10.5555555, 1) < 10.61, "test roundDecimal()");
        assertTrue(AbstractAudio.roundDecimal((float) 10.5555555, 3) > 10.5559, "test roundDecimal()");
        assertTrue(AbstractAudio.roundDecimal((float) 10.5555555, 3) < 10.5561, "test roundDecimal()");
        assertTrue(AbstractAudio.roundDecimal((float) 10.5555555, 5) > 10.555559, "test roundDecimal()");
        assertTrue(AbstractAudio.roundDecimal((float) 10.5555555, 5) < 10.555561, "test roundDecimal()");

        // Test AbstractAudio.roundDecimal()
        // The test of jmri.Audio.DECIMAL_PLACES is only a "heads up" for the tests below.
        // If jmri.Audio.DECIMAL_PLACES is changed, the tests below must be changed too.
        assertEquals(2, Math.round(jmri.Audio.DECIMAL_PLACES));
        assertTrue(AbstractAudio.roundDecimal((float) 10.5555555) > 10.559, "test roundDecimal()");
        assertTrue(AbstractAudio.roundDecimal((float) 10.5555555) < 10.561, "test roundDecimal()");
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }


    private class MyAbstractAudio extends AbstractAudio {
        
        MyAbstractAudio() {
            super("MySystemName");
        }
        
        @Override
        public char getSubType() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void stateChanged(int oldState) {
            stateHasChanged.set(true);
        }

        @Override
        protected void cleanup() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }

}
