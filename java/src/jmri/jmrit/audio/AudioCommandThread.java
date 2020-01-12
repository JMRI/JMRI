package jmri.jmrit.audio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a thread for processing commands contained within AudioCommand
 * objects. All commands are processed in the order in which thet were queued
 *
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Matthew Harris copyright (c) 2009
 */
public class AudioCommandThread extends AbstractAudioThread {

    /**
     * Reference to active audio factory
     */
    private AudioFactory activeAudioFactory;

    /**
     * Constructor that takes handle to current active audio factory object
     *
     * @param activeAudioFactory handle to activeAudioFactory
     */
    public AudioCommandThread(AudioFactory activeAudioFactory) {
        super();
        this.setName("command-" + super.getName());
        this.activeAudioFactory = activeAudioFactory;
        if (log.isDebugEnabled()) {
            log.debug("Created AudioThread for AudioFactory " + activeAudioFactory.toString());
        }
    }

    /**
     * Main loop for processing commands. Starts out asleep, and also sleeps
     * once finished processing commands, so must be interrupted to process any
     * queued commands.
     */
    @Override
    public void run() {

        // Start out asleep (5 minutes)
        snooze(300000);

        while (!dying()) {
            // Process the command queue
            activeAudioFactory.audioCommandQueue(null);

            // Wait for more commands (5 minutes)
            if (!dying()) {
                snooze(300000);
            }
        }

        // Finish up
        if (log.isDebugEnabled()) {
            log.debug("Clean up thread " + this.getName());
        }
        cleanup();
    }

    /**
     * Shuts this thread down and clears references to created objects
     */
    @Override
    protected void cleanup() {

        // Thread is to shutdown
        die();

        // Clear references to objects
        this.activeAudioFactory = null;

        // Finalise cleanup in super-class
        super.cleanup();
    }

    private static final Logger log = LoggerFactory.getLogger(AudioCommandThread.class);

}
