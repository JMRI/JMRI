package jmri.implementation;

import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.*;

/**
 * Tests for AbstractAudio
 */
public class AbstractAudioTest {
    
    AtomicBoolean stateHasChanged = new AtomicBoolean(false);
    
    @Test
    public void testCtor() {
        
        MyAbstractAudio audio = new MyAbstractAudio();
        Assert.assertNotNull("AbstractAudio constructor return", audio);
    }
    
    @Test
    public void testState() {
        MyAbstractAudio audio = new MyAbstractAudio();
        
        Assert.assertTrue("state is initial", audio.getState() == jmri.Audio.STATE_INITIAL);
        audio.setState(jmri.Audio.STATE_PLAYING);
        Assert.assertTrue("state is playing", audio.getState() == jmri.Audio.STATE_PLAYING);
        // Check that audio.setState() triggers stateChanged()
        Assert.assertTrue("state has changed", stateHasChanged.get());
        Assert.assertTrue("toString() matches",
                "jmri.implementation.AbstractAudioTest$MyAbstractAudio (MySystemName)".equals(audio.toString()));
        
        Assert.assertTrue("getBeanType() matches", "Audio".equals(audio.getBeanType()));
    }
    
    @Test
    public void testRoundDecimal() {
        // Test AbstractAudio.roundDecimal()
        Assert.assertTrue("test roundDecimal()", AbstractAudio.roundDecimal((float) 10.5555555, 1) > 10.59);
        Assert.assertTrue("test roundDecimal()", AbstractAudio.roundDecimal((float) 10.5555555, 1) < 10.61);
        Assert.assertTrue("test roundDecimal()", AbstractAudio.roundDecimal((float) 10.5555555, 3) > 10.5559);
        Assert.assertTrue("test roundDecimal()", AbstractAudio.roundDecimal((float) 10.5555555, 3) < 10.5561);
        Assert.assertTrue("test roundDecimal()", AbstractAudio.roundDecimal((float) 10.5555555, 5) > 10.555559);
        Assert.assertTrue("test roundDecimal()", AbstractAudio.roundDecimal((float) 10.5555555, 5) < 10.555561);
        
        // Test AbstractAudio.roundDecimal()
        // The test of jmri.Audio.DECIMAL_PLACES is only a "heads up" for the tests below.
        // If jmri.Audio.DECIMAL_PLACES is changed, the tests below must be changed too.
        Assert.assertEquals(2, Math.round(jmri.Audio.DECIMAL_PLACES));
        Assert.assertTrue("test roundDecimal()", AbstractAudio.roundDecimal((float) 10.5555555) > 10.559);
        Assert.assertTrue("test roundDecimal()", AbstractAudio.roundDecimal((float) 10.5555555) < 10.561);
    }

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @After
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
