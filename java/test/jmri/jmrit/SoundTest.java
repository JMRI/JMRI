package jmri.jmrit;

import java.awt.GraphicsEnvironment;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Randall Wood (C) 2016
 */
public class SoundTest {

    private static final Logger log = LoggerFactory.getLogger(SoundTest.class);

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();

    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();

    }
    
    /**
     * Test of play method, of class Sound.
     */
    @Test
    public void testPlay() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        try {
            AudioSystem.getClip();
        } catch (IllegalArgumentException | LineUnavailableException ex) {
            Assume.assumeNoException("Unable to initialize AudioSystem", ex);
        }
        Sound instance = new Sound("program:resources/sounds/Button.wav");
        instance.play();
        log.info("Button pressed sound played once.");
    }

    /**
     * Test of loop method, of class Sound.
     */
    @Test
    public void testLoopInt() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        try {
            AudioSystem.getClip();
        } catch (IllegalArgumentException | LineUnavailableException ex) {
            Assume.assumeNoException("Unable to initialize AudioSystem", ex);
        }
        Sound instance = new Sound("program:resources/sounds/bell_stroke.wav");
        instance.loop(2);
        log.info("Bell stroke sounded twice.");
    }

    /**
     * Test of stop method, of class Sound.
     */
    @Test
    public void testStop() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        try {
            AudioSystem.getClip();
        } catch (IllegalArgumentException | LineUnavailableException ex) {
            Assume.assumeNoException("Unable to initialize AudioSystem", ex);
        }
        Sound instance = new Sound("program:resources/sounds/RlyClick.wav");
        instance.loop();
        Runnable waiter = new Runnable() {
            @Override
            public synchronized void run() {
                try {
                    this.wait(500);
                } catch (InterruptedException ex) {
                    log.error("Waiter interrupted.");
                }
            }
        };
        waiter.run();
        instance.stop();
        log.info("Repeated relay clicks played.");
    }

}
