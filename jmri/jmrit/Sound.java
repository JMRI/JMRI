package jmri.jmrit;

import java.applet.AudioClip;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * Provide simple way to load and play sounds in JMRI.
 * <P>
 * This is placed in the jmri.jmrit package by process of
 * elimination.  It doesn't belong in the base jmri package, as
 * it's not a basic interface.  Nor is it a specific implementation
 * of a basic interface, which would put it in jmri.jmrix.  It seems
 * most like a "tool using JMRI", or perhaps a tool for use with JMRI,
 * so it was placed in jmri.jmrit.
 *
 * @author	Bob Jacobsen  Copyright (C) 2004
 * @version	$Revision: 1.1 $
 */
public class Sound  {

    /*
     * Constructor takes the filename or URL, and
     * causes the sound to be loaded
     */
     public Sound(String filename) {
        loadingSound(filename);
     }

    /**
     * Play the sound once
     */
    public void play() {
        audioClip.play();
    }

    /**
     * Play the sound as a loop
     */
    public void loop() {
        audioClip.loop();
    }

    /**
     * Stop playing as a loop
     */
    public void stop() {
        audioClip.stop();
    }


    /**
     * Load the requested sound resource
     */
    void loadingSound(String filename) {
        try {
            // create a base URL for the sound file location
            URL url = new URL(jmri.util.FileUtil.getUrl(new java.io.File(filename)));

            // create a loader and start asynchronous sound loading
            audioClip = new sun.applet.AppletAudioClip(url);

        } catch (MalformedURLException e) {
            log.error("Error creating sound address: "+e.getMessage());
        }
    }


    /**
     * The actual sound, stored as an AudioClip
     */
    public AudioClip audioClip = null;

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(Sound.class.getName());
}
