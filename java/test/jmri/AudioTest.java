package jmri;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the Audio class
 *
 * @author Matthew Harris Copyright (C) 2011
 */
public class AudioTest {

    @Test
    public void testStateConstants() {

        Assert.assertTrue("Initial and Stopped differ", (Audio.STATE_INITIAL != Audio.STATE_STOPPED));
        Assert.assertTrue("Initial and Playing differ", (Audio.STATE_INITIAL != Audio.STATE_PLAYING));
        Assert.assertTrue("Initial and Empty differ", (Audio.STATE_INITIAL != Audio.STATE_EMPTY));
        Assert.assertTrue("Initial and Loaded differ", (Audio.STATE_INITIAL != Audio.STATE_LOADED));
        Assert.assertTrue("Initial and Positioned differ", (Audio.STATE_INITIAL != Audio.STATE_POSITIONED));
        Assert.assertTrue("Initial and Moving differ", (Audio.STATE_INITIAL != Audio.STATE_MOVING));

        Assert.assertTrue("Stopped and Playing differ", (Audio.STATE_STOPPED != Audio.STATE_PLAYING));
        Assert.assertTrue("Stopped and Empty differ", (Audio.STATE_STOPPED != Audio.STATE_EMPTY));
        Assert.assertTrue("Stopped and Loaded differ", (Audio.STATE_STOPPED != Audio.STATE_LOADED));
        Assert.assertTrue("Stopped and Positioned differ", (Audio.STATE_STOPPED != Audio.STATE_POSITIONED));
        Assert.assertTrue("Stopped and Moving differ", (Audio.STATE_STOPPED != Audio.STATE_MOVING));

        Assert.assertTrue("Playing and Empty differ", (Audio.STATE_PLAYING != Audio.STATE_EMPTY));
        Assert.assertTrue("Playing and Loaded differ", (Audio.STATE_PLAYING != Audio.STATE_LOADED));
        Assert.assertTrue("Playing and Positioned differ", (Audio.STATE_PLAYING != Audio.STATE_POSITIONED));
        Assert.assertTrue("Playing and Moving differ", (Audio.STATE_PLAYING != Audio.STATE_MOVING));

        Assert.assertTrue("Empty and Loaded differ", (Audio.STATE_EMPTY != Audio.STATE_LOADED));
        Assert.assertTrue("Empty and Positioned differ", (Audio.STATE_EMPTY != Audio.STATE_POSITIONED));
        Assert.assertTrue("Empty and Moving differ", (Audio.STATE_EMPTY != Audio.STATE_MOVING));

        Assert.assertTrue("Loaded and Positioned differ", (Audio.STATE_LOADED != Audio.STATE_POSITIONED));
        Assert.assertTrue("Loaded and Moving differ", (Audio.STATE_LOADED != Audio.STATE_MOVING));

        Assert.assertTrue("Positioned and Moving differ", (Audio.STATE_POSITIONED != Audio.STATE_MOVING));

    }

    @Test
    public void testSubTypeConstants() {

        Assert.assertEquals("AudioSource sub-type is 'S'", 'S', Audio.SOURCE);
        Assert.assertEquals("AudioBuffer sub-type is 'B'", 'B', Audio.BUFFER);
        Assert.assertEquals("AudioListener sub-type is 'L'", 'L', Audio.LISTENER);

    }

    @Test
    public void testCommandConstants() {

        Assert.assertTrue("Command Init Factory and Load Sound differ", (Audio.CMD_INIT_FACTORY != Audio.CMD_LOAD_SOUND));
        Assert.assertTrue("Command Init Factory and Bind Buffer differ", (Audio.CMD_INIT_FACTORY != Audio.CMD_BIND_BUFFER));
        Assert.assertTrue("Command Init Factory and Play differ", (Audio.CMD_INIT_FACTORY != Audio.CMD_PLAY));
        Assert.assertTrue("Command Init Factory and Stop differ", (Audio.CMD_INIT_FACTORY != Audio.CMD_STOP));
        Assert.assertTrue("Command Init Factory and Play Toggle differ", (Audio.CMD_INIT_FACTORY != Audio.CMD_PLAY_TOGGLE));
        Assert.assertTrue("Command Init Factory and Pause differ", (Audio.CMD_INIT_FACTORY != Audio.CMD_PAUSE));
        Assert.assertTrue("Command Init Factory and Resume differ", (Audio.CMD_INIT_FACTORY != Audio.CMD_RESUME));
        Assert.assertTrue("Command Init Factory and Pause Toggle differ", (Audio.CMD_INIT_FACTORY != Audio.CMD_PAUSE_TOGGLE));
        Assert.assertTrue("Command Init Factory and Rewind differ", (Audio.CMD_INIT_FACTORY != Audio.CMD_REWIND));
        Assert.assertTrue("Command Init Factory and Fade-In differ", (Audio.CMD_INIT_FACTORY != Audio.CMD_FADE_IN));
        Assert.assertTrue("Command Init Factory and Fade-Out differ", (Audio.CMD_INIT_FACTORY != Audio.CMD_FADE_OUT));
        Assert.assertTrue("Command Init Factory and Reset Position differ", (Audio.CMD_INIT_FACTORY != Audio.CMD_RESET_POSITION));
        Assert.assertTrue("Command Init Factory and Queue Buffers differ", (Audio.CMD_INIT_FACTORY != Audio.CMD_QUEUE_BUFFERS) );
        Assert.assertTrue("Command Init Factory and Unqueue Buffers differ", (Audio.CMD_INIT_FACTORY != Audio.CMD_UNQUEUE_BUFFERS) );

        Assert.assertTrue("Command Load Sound and Bind Buffer differ", (Audio.CMD_LOAD_SOUND != Audio.CMD_BIND_BUFFER));
        Assert.assertTrue("Command Load Sound and Play differ", (Audio.CMD_LOAD_SOUND != Audio.CMD_PLAY));
        Assert.assertTrue("Command Load Sound and Stop differ", (Audio.CMD_LOAD_SOUND != Audio.CMD_STOP));
        Assert.assertTrue("Command Load Sound and Play Toggle differ", (Audio.CMD_LOAD_SOUND != Audio.CMD_PLAY_TOGGLE));
        Assert.assertTrue("Command Load Sound and Pause differ", (Audio.CMD_LOAD_SOUND != Audio.CMD_PAUSE));
        Assert.assertTrue("Command Load Sound and Resume differ", (Audio.CMD_LOAD_SOUND != Audio.CMD_RESUME));
        Assert.assertTrue("Command Load Sound and Pause Toggle differ", (Audio.CMD_LOAD_SOUND != Audio.CMD_PAUSE_TOGGLE));
        Assert.assertTrue("Command Load Sound and Rewind differ", (Audio.CMD_LOAD_SOUND != Audio.CMD_REWIND));
        Assert.assertTrue("Command Load Sound and Fade-In differ", (Audio.CMD_LOAD_SOUND != Audio.CMD_FADE_IN));
        Assert.assertTrue("Command Load Sound and Fade-Out differ", (Audio.CMD_LOAD_SOUND != Audio.CMD_FADE_OUT));
        Assert.assertTrue("Command Load Sound and Reset Position differ", (Audio.CMD_LOAD_SOUND != Audio.CMD_RESET_POSITION));
        Assert.assertTrue("Command Load Sound and Queue Buffers differ", (Audio.CMD_LOAD_SOUND != Audio.CMD_QUEUE_BUFFERS) );
        Assert.assertTrue("Command Load Sound and Unqueue Buffers differ", (Audio.CMD_LOAD_SOUND != Audio.CMD_UNQUEUE_BUFFERS) );

        Assert.assertTrue("Command Bind Buffer and Play differ", (Audio.CMD_BIND_BUFFER != Audio.CMD_PLAY));
        Assert.assertTrue("Command Bind Buffer and Stop differ", (Audio.CMD_BIND_BUFFER != Audio.CMD_STOP));
        Assert.assertTrue("Command Bind Buffer and Play Toggle differ", (Audio.CMD_BIND_BUFFER != Audio.CMD_PLAY_TOGGLE));
        Assert.assertTrue("Command Bind Buffer and Pause differ", (Audio.CMD_BIND_BUFFER != Audio.CMD_PAUSE));
        Assert.assertTrue("Command Bind Buffer and Resume differ", (Audio.CMD_BIND_BUFFER != Audio.CMD_RESUME));
        Assert.assertTrue("Command Bind Buffer and Pause Toggle differ", (Audio.CMD_BIND_BUFFER != Audio.CMD_PAUSE_TOGGLE));
        Assert.assertTrue("Command Bind Buffer and Rewind differ", (Audio.CMD_BIND_BUFFER != Audio.CMD_REWIND));
        Assert.assertTrue("Command Bind Buffer and Fade-In differ", (Audio.CMD_BIND_BUFFER != Audio.CMD_FADE_IN));
        Assert.assertTrue("Command Bind Buffer and Fade-Out differ", (Audio.CMD_BIND_BUFFER != Audio.CMD_FADE_OUT));
        Assert.assertTrue("Command Bind Buffer and Reset Position differ", (Audio.CMD_BIND_BUFFER != Audio.CMD_RESET_POSITION));
        Assert.assertTrue("Command Bind Buffer and Queue Buffers differ", (Audio.CMD_BIND_BUFFER != Audio.CMD_QUEUE_BUFFERS) );
        Assert.assertTrue("Command Bind Buffer and Unqueue Buffers differ", (Audio.CMD_BIND_BUFFER != Audio.CMD_UNQUEUE_BUFFERS) );

        Assert.assertTrue("Command Play and Stop differ", (Audio.CMD_PLAY != Audio.CMD_STOP));
        Assert.assertTrue("Command Play and Play Toggle differ", (Audio.CMD_PLAY != Audio.CMD_PLAY_TOGGLE));
        Assert.assertTrue("Command Play and Pause differ", (Audio.CMD_PLAY != Audio.CMD_PAUSE));
        Assert.assertTrue("Command Play and Resume differ", (Audio.CMD_PLAY != Audio.CMD_RESUME));
        Assert.assertTrue("Command Play and Pause Toggle differ", (Audio.CMD_PLAY != Audio.CMD_PAUSE_TOGGLE));
        Assert.assertTrue("Command Play and Rewind differ", (Audio.CMD_PLAY != Audio.CMD_REWIND));
        Assert.assertTrue("Command Play and Fade-In differ", (Audio.CMD_PLAY != Audio.CMD_FADE_IN));
        Assert.assertTrue("Command Play and Fade-Out differ", (Audio.CMD_PLAY != Audio.CMD_FADE_OUT));
        Assert.assertTrue("Command Play and Reset Position differ", (Audio.CMD_PLAY != Audio.CMD_RESET_POSITION));
        Assert.assertTrue("Command Play and Queue Buffers differ", (Audio.CMD_PLAY != Audio.CMD_QUEUE_BUFFERS) );
        Assert.assertTrue("Command Play and Unqueue Buffers differ", (Audio.CMD_PLAY != Audio.CMD_UNQUEUE_BUFFERS) );

        Assert.assertTrue("Command Stop and Play Toggle differ", (Audio.CMD_STOP != Audio.CMD_PLAY_TOGGLE));
        Assert.assertTrue("Command Stop and Pause differ", (Audio.CMD_STOP != Audio.CMD_PAUSE));
        Assert.assertTrue("Command Stop and Resume differ", (Audio.CMD_STOP != Audio.CMD_RESUME));
        Assert.assertTrue("Command Stop and Pause Toggle differ", (Audio.CMD_STOP != Audio.CMD_PAUSE_TOGGLE));
        Assert.assertTrue("Command Stop and Rewind differ", (Audio.CMD_STOP != Audio.CMD_REWIND));
        Assert.assertTrue("Command Stop and Fade-In differ", (Audio.CMD_STOP != Audio.CMD_FADE_IN));
        Assert.assertTrue("Command Stop and Fade-Out differ", (Audio.CMD_STOP != Audio.CMD_FADE_OUT));
        Assert.assertTrue("Command Stop and Reset Position differ", (Audio.CMD_STOP != Audio.CMD_RESET_POSITION));
        Assert.assertTrue("Command Stop and Queue Buffers differ", (Audio.CMD_STOP != Audio.CMD_QUEUE_BUFFERS) );
        Assert.assertTrue("Command Stop and Unqueue Buffers differ", (Audio.CMD_STOP != Audio.CMD_UNQUEUE_BUFFERS) );

        Assert.assertTrue("Command Play Toggle and Pause differ", (Audio.CMD_PLAY_TOGGLE != Audio.CMD_PAUSE));
        Assert.assertTrue("Command Play Toggle and Resume differ", (Audio.CMD_PLAY_TOGGLE != Audio.CMD_RESUME));
        Assert.assertTrue("Command Play Toggle and Pause Toggle differ", (Audio.CMD_PLAY_TOGGLE != Audio.CMD_PAUSE_TOGGLE));
        Assert.assertTrue("Command Play Toggle and Rewind differ", (Audio.CMD_PLAY_TOGGLE != Audio.CMD_REWIND));
        Assert.assertTrue("Command Play Toggle and Fade-In differ", (Audio.CMD_PLAY_TOGGLE != Audio.CMD_FADE_IN));
        Assert.assertTrue("Command Play Toggle and Fade-Out differ", (Audio.CMD_PLAY_TOGGLE != Audio.CMD_FADE_OUT));
        Assert.assertTrue("Command Play Toggle and Reset Position differ", (Audio.CMD_PLAY_TOGGLE != Audio.CMD_RESET_POSITION));
        Assert.assertTrue("Command Play Toggle and Queue Buffers differ", (Audio.CMD_PLAY_TOGGLE != Audio.CMD_QUEUE_BUFFERS) );
        Assert.assertTrue("Command Play Toggle and Unqueue Buffers differ", (Audio.CMD_PLAY_TOGGLE != Audio.CMD_UNQUEUE_BUFFERS) );

        Assert.assertTrue("Command Pause and Resume differ", (Audio.CMD_PAUSE != Audio.CMD_RESUME));
        Assert.assertTrue("Command Pause and Pause Toggle differ", (Audio.CMD_PAUSE != Audio.CMD_PAUSE_TOGGLE));
        Assert.assertTrue("Command Pause and Rewind differ", (Audio.CMD_PAUSE != Audio.CMD_REWIND));
        Assert.assertTrue("Command Pause and Fade-In differ", (Audio.CMD_PAUSE != Audio.CMD_FADE_IN));
        Assert.assertTrue("Command Pause and Fade-Out differ", (Audio.CMD_PAUSE != Audio.CMD_FADE_OUT));
        Assert.assertTrue("Command Pause and Reset Position differ", (Audio.CMD_PAUSE != Audio.CMD_RESET_POSITION));
        Assert.assertTrue("Command Pause and Queue Buffers differ", (Audio.CMD_PAUSE != Audio.CMD_QUEUE_BUFFERS) );
        Assert.assertTrue("Command Pause and Unqueue Buffers differ", (Audio.CMD_PAUSE != Audio.CMD_UNQUEUE_BUFFERS) );

        Assert.assertTrue("Command Resume and Pause Toggle differ", (Audio.CMD_RESUME != Audio.CMD_PAUSE_TOGGLE));
        Assert.assertTrue("Command Resume and Rewind differ", (Audio.CMD_RESUME != Audio.CMD_REWIND));
        Assert.assertTrue("Command Resume and Fade-In differ", (Audio.CMD_RESUME != Audio.CMD_FADE_IN));
        Assert.assertTrue("Command Resume and Fade-Out differ", (Audio.CMD_RESUME != Audio.CMD_FADE_OUT));
        Assert.assertTrue("Command Resume and Reset Position differ", (Audio.CMD_RESUME != Audio.CMD_RESET_POSITION));
        Assert.assertTrue("Command Resume and Queue Buffers differ", (Audio.CMD_RESUME != Audio.CMD_QUEUE_BUFFERS) );
        Assert.assertTrue("Command Resume and Unqueue Buffers differ", (Audio.CMD_RESUME != Audio.CMD_UNQUEUE_BUFFERS) );

        Assert.assertTrue("Command Pause Toggle and Rewind differ", (Audio.CMD_PAUSE_TOGGLE != Audio.CMD_REWIND));
        Assert.assertTrue("Command Pause Toggle and Fade-In differ", (Audio.CMD_PAUSE_TOGGLE != Audio.CMD_FADE_IN));
        Assert.assertTrue("Command Pause Toggle and Fade-Out differ", (Audio.CMD_PAUSE_TOGGLE != Audio.CMD_FADE_OUT));
        Assert.assertTrue("Command Pause Toggle and Reset Position differ", (Audio.CMD_PAUSE_TOGGLE != Audio.CMD_RESET_POSITION));
        Assert.assertTrue("Command Pause Toggle and Queue Buffers differ", (Audio.CMD_PAUSE_TOGGLE != Audio.CMD_QUEUE_BUFFERS) );
        Assert.assertTrue("Command Pause Toggle and Unqueue Buffers differ", (Audio.CMD_PAUSE_TOGGLE != Audio.CMD_UNQUEUE_BUFFERS) );

        Assert.assertTrue("Command Rewind and Fade-In differ", (Audio.CMD_REWIND != Audio.CMD_FADE_IN));
        Assert.assertTrue("Command Rewind and Fade-Out differ", (Audio.CMD_REWIND != Audio.CMD_FADE_OUT));
        Assert.assertTrue("Command Rewind and Reset Position differ", (Audio.CMD_REWIND != Audio.CMD_RESET_POSITION));
        Assert.assertTrue("Command Rewind and Queue Buffers differ", (Audio.CMD_REWIND != Audio.CMD_QUEUE_BUFFERS) );
        Assert.assertTrue("Command Rewind and Unqueue Buffers differ", (Audio.CMD_REWIND != Audio.CMD_UNQUEUE_BUFFERS) );

        Assert.assertTrue("Command Fade-In and Fade-Out differ", (Audio.CMD_FADE_IN != Audio.CMD_FADE_OUT));
        Assert.assertTrue("Command Fade-In and Reset Position differ", (Audio.CMD_FADE_IN != Audio.CMD_RESET_POSITION));
        Assert.assertTrue("Command Fade-In and Queue Buffers differ", (Audio.CMD_FADE_IN != Audio.CMD_QUEUE_BUFFERS) );
        Assert.assertTrue("Command Fade-In and Unqueue Buffers differ", (Audio.CMD_FADE_IN != Audio.CMD_UNQUEUE_BUFFERS) );

        Assert.assertTrue("Command Fade-Out and Reset Position differ", (Audio.CMD_FADE_OUT != Audio.CMD_RESET_POSITION));
        Assert.assertTrue("Command Fade-Out and Queue Buffers differ", (Audio.CMD_FADE_OUT != Audio.CMD_QUEUE_BUFFERS) );
        Assert.assertTrue("Command Fade-Out and Unqueue Buffers differ", (Audio.CMD_FADE_OUT != Audio.CMD_UNQUEUE_BUFFERS) );

        Assert.assertTrue("Command Reset Position and Queue Buffers differ", (Audio.CMD_RESET_POSITION != Audio.CMD_QUEUE_BUFFERS) );
        Assert.assertTrue("Command Reset Position and Unqueue Buffers differ", (Audio.CMD_RESET_POSITION != Audio.CMD_UNQUEUE_BUFFERS) );

        Assert.assertTrue("Command Queue Buffers and Unqueue Buffers differ", (Audio.CMD_QUEUE_BUFFERS != Audio.CMD_UNQUEUE_BUFFERS) );

    }

    @Test
    public void testFadeStateConstants() {

        Assert.assertTrue("Fade State None and Out differ", (Audio.FADE_NONE != Audio.FADE_OUT));
        Assert.assertTrue("Fade State None and In differ", (Audio.FADE_NONE != Audio.FADE_IN));

        Assert.assertTrue("Fade State Out and In differ", (Audio.FADE_OUT != Audio.FADE_IN));

    }

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

}
