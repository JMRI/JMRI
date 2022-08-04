package jmri.jmrit;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;

import jmri.util.JUnitUtil;

import org.junit.Assume;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Randall Wood (C) 2016
 */
public class SoundTest {

    private static final Logger log = LoggerFactory.getLogger(SoundTest.class);

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();

    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();

    }
    
    /**
     * Test of play method, of class Sound.
     */
    @Test
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    public void testPlay() {
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
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    public void testLoopInt() {
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
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    public void testStop() {
        try {
            AudioSystem.getClip();
        } catch (IllegalArgumentException | LineUnavailableException ex) {
            Assume.assumeNoException("Unable to initialize AudioSystem", ex);
        }
        Sound instance = new Sound("program:resources/sounds/RlyClick.wav");
        instance.loop();
        JUnitUtil.waitFor(500);
        instance.stop();
        log.info("Repeated relay clicks played.");
    }

}
