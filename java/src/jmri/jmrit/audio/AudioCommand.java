package jmri.jmrit.audio;

import jmri.Audio;

/**
 * Represents a queued command for later processing in the AudioController
 * thread.
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
public class AudioCommand {

    /**
     * Private variables containing command parameters
     */
    private final Audio audio;

    private final int command;

    /**
     * Constructor to process a command on an Audio object
     *
     * @param audio   Audio object to process
     * @param command Action to perform
     */
    public AudioCommand(Audio audio, int command) {
        this.audio = audio;
        this.command = command;
    }

    /**
     * Return the Audio object that this command refers to
     *
     * @return Audio object to process
     */
    public synchronized Audio getAudio() {
        return this.audio;
    }

    /**
     * Return the action to perform
     *
     * @return Action
     */
    public synchronized int getCommand() {
        return this.command;
    }

    @Override
    public String toString() {
        if (this.audio != null) {
            return "Command " + commandString() + " for Audio " + audio.getSystemName();
        } else {
            return "Command " + commandString() + " for null object";
        }
    }

    /**
     * Returns a string representation of the assigned command
     *
     * @return a string representation
     */
    private String commandString() {
        switch (this.command) {
            case Audio.CMD_INIT_FACTORY:
                return "Initialise Factory (0x" + Integer.toHexString(this.command) + ")";
            case Audio.CMD_LOAD_SOUND:
                return "Load Sound (0x" + Integer.toHexString(this.command) + ")";
            case Audio.CMD_BIND_BUFFER:
                return "Bind buffer (0x" + Integer.toHexString(this.command) + ")";
            case Audio.CMD_QUEUE_BUFFERS:
                return "Queue buffers (0x" + Integer.toHexString(this.command) + ")";
            case Audio.CMD_UNQUEUE_BUFFERS:
                return "Unqueue buffers (0x" + Integer.toHexString(this.command) + ")";
            case Audio.CMD_PLAY:
                return "Play (0x" + Integer.toHexString(this.command) + ")";
            case Audio.CMD_PAUSE:
                return "Pause (0x" + Integer.toHexString(this.command) + ")";
            case Audio.CMD_PLAY_TOGGLE:
                return "Play/Stop toggle (0x" + Integer.toHexString(this.command) + ")";
            case Audio.CMD_PAUSE_TOGGLE:
                return "Pause/Resume toggle (0x" + Integer.toHexString(this.command) + ")";
            case Audio.CMD_RESUME:
                return "Resume (0x" + Integer.toHexString(this.command) + ")";
            case Audio.CMD_STOP:
                return "Stop (0x" + Integer.toHexString(this.command) + ")";
            case Audio.CMD_REWIND:
                return "Rewind (0x" + Integer.toHexString(this.command) + ")";
            case Audio.CMD_FADE_IN:
                return "Fade-in (0x" + Integer.toHexString(this.command) + ")";
            case Audio.CMD_FADE_OUT:
                return "Fade-out (0x" + Integer.toHexString(this.command) + ")";
            default:
                return "Unknown (0x" + Integer.toHexString(this.command) + ")";
        }
    }

    //private static final Logger log = LoggerFactory.getLogger(AudioCommand.class);
}
