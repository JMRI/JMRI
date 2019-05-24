package jmri;

/**
 * Represent an Audio, a place to store or control sound information.
 * <p>
 * The AbstractAudio class contains a basic implementation of the state and
 * messaging code, and forms a useful start for a system-specific
 * implementation. Specific implementations in jmrix sub-packages will convert
 * to and from the layout commands.
 * <p>
 * The states and names are Java Bean parameters, so that listeners can be
 * registered to be notified of any changes.
 * <p>
 * Each Audio object has a two names. The "user" name is entirely free form, and
 * can be used for any purpose. The "system" name is provided by the
 * system-specific implementations, and provides a unique mapping to the layout
 * control system and address within that system.
 * <br>
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
public interface Audio extends NamedBean {

    // Define Object sub-type constants
    /**
     * Definition of AudioSource NamedBean sub-type code
     */
    public static final char SOURCE = 'S';

    /**
     * Definition of AudioBuffer NamedBean sub-type code
     */
    public static final char BUFFER = 'B';

    /**
     * Definition of AudioListener NamedBean sub-type code
     */
    public static final char LISTENER = 'L';

    // Define orientation constants
    /**
     * Definition of Audio object orientation at vector code
     */
    public static final int AT = 0x01;

    /**
     * Definition of Audio object orientation up vector code
     */
    public static final int UP = 0x02;

    // Define state variables for Audio objects
    /**
     * Default state for any newly created Audio object
     */
    public static final int STATE_INITIAL = 0x00;

    // Define applicable states for Source sub-types
    /**
     * State code for an AudioSource when stopped
     */
    public static final int STATE_STOPPED = 0x10;

    /**
     * State code for an AudioSource when playing
     */
    public static final int STATE_PLAYING = 0x11;

    // Define applicable states for Buffer sub-types
    /**
     * State code for an AudioBuffer when empty
     */
    public static final int STATE_EMPTY = 0x20;

    /**
     * State code for an AudioBuffer when loaded
     */
    public static final int STATE_LOADED = 0x21;

    // Define applicable states for Listener sub-types
    /**
     * State code for an AudioListener when positioned
     */
    public static final int STATE_POSITIONED = 0x30;

    /**
     * State code for an AudioListener when moving
     */
    public static final int STATE_MOVING = 0x31;

    // Define Audio command constants
    // Constants defining Factory related commands
    /**
     * Command to initialise AudioFactory
     */
    public static final int CMD_INIT_FACTORY = 0x01;

    // Constants defining Buffer related commands
    /**
     * Command to load the sound
     */
    public static final int CMD_LOAD_SOUND = 0x20;

    // Constants defining Source related commands
    /**
     * Command to bind Buffer to Source
     */
    public static final int CMD_BIND_BUFFER = 0x30;

    /**
     * Command to queue Buffer to Source
     */
    public static final int CMD_QUEUE_BUFFERS = 0x31;

    /**
     * Command to unqueue used Buffers from Source
     */
    public static final int CMD_UNQUEUE_BUFFERS = 0x32;

    /**
     * Command to play this Source from the beginning
     */
    public static final int CMD_PLAY = 0x40;

    /**
     * Command to stop playing this Source and rewind to the start
     */
    public static final int CMD_STOP = 0x41;

    /**
     * Command to start or stop this Source from the beginning
     */
    public static final int CMD_PLAY_TOGGLE = 0x42;

    /**
     * Command to pause playback of this Source and retain the position
     */
    public static final int CMD_PAUSE = 0x43;

    /**
     * Command to resume playback of this Source from the current position
     */
    public static final int CMD_RESUME = 0x44;

    /**
     * Command to pause or resume this Source from the current position
     */
    public static final int CMD_PAUSE_TOGGLE = 0x45;

    /**
     * Command to rewind this Source to the beginning
     */
    public static final int CMD_REWIND = 0x46;

    /**
     * Command to fade in and start playback of this Source
     */
    public static final int CMD_FADE_IN = 0x47;

    /**
     * Command to fade out and stop playback of this Source
     */
    public static final int CMD_FADE_OUT = 0x48;

    /**
     * Command to reset the position of this Source
     */
    public static final int CMD_RESET_POSITION = 0x49;

    // Define state variables for fade states
    /**
     * Fade state of Source when not fading
     */
    public static final int FADE_NONE = 0x00;

    /**
     * Fade state of Source when fading out
     */
    public static final int FADE_OUT = 0x01;

    /**
     * Fade state of Source when fading in
     */
    public static final int FADE_IN = 0x02;

    /**
     * Maximum distance for Audio objects
     */
    public static final float MAX_DISTANCE = 9999.99f;

    /**
     * Number of decimal places for float values to be stored in
     */
    public static final double DECIMAL_PLACES = 2;

    /**
     * An Audio object can represent one of a number of subtypes of object.
     * <p>
     * This method enables us to determine which of those subtypes this
     * particular instance is and be able to process accordingly.
     * <p>
     * Current supported subtypes are:
     * <ul>
     * <li>B = Buffer
     * <li>L = Listener
     * <li>S = Source
     * </ul>
     *
     * @return subType char
     */
    public char getSubType();

    /**
     * Method used to update the current state of the Audio object
     *
     * @param oldState the former state
     */
    public void stateChanged(int oldState);

}
