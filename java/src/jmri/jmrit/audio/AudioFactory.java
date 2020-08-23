package jmri.jmrit.audio;

/**
 * An AudioFactory is responsible for the initialisation of specific audio
 * system implementations, creation of audio system specific Audio objects and
 * any necessary clean-up operations required by a specific audio system
 * implementation.
 * <p>
 * Each factory varies in its capabilities with regard the faithfulness of the
 * audio rendering model (such as spatial positioning approximation), number of
 * concurrent sounds (polyphony), hardware required, etc.
 * <p>
 * Current implemented audio systems include:
 * <ul>
 * <li>JOAL
 * <li>JavaSound
 * <li>Null (a catch-all which doesn't actually play any sounds)
 * </ul>
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
public interface AudioFactory {

    /**
     * Perform any implementation specific initialisation routines.
     *
     * @return true, if initialisation successful
     */
    public boolean init();

    /**
     * Perform any implementation specific clean-up operations.
     */
    public void cleanup();

    /**
     * Determine if this AudioFactory is initialised
     * @return true if initialised
     */
    public boolean isInitialised();

    /**
     * Provide a specific new AudioBuffer object.
     *
     * @param systemName for this object instance
     * @param userName   for this object instance
     * @return a new specific AudioBuffer
     */
    public AudioBuffer createNewBuffer(String systemName, String userName);

    /**
     * Provide a specific new AudioListener object.
     *
     * @param systemName for this object instance
     * @param userName   for this object instance
     * @return a new specific AudioListener
     */
    public AudioListener createNewListener(String systemName, String userName);

    /**
     * Get the currently active Listener object.
     *
     * @return active AudioListener
     */
    public AudioListener getActiveAudioListener();

    /**
     * Provide a specific new AudioSource object.
     *
     * @param systemName for this object instance
     * @param userName   for this object instance
     * @return a new specific AudioSource
     */
    public AudioSource createNewSource(String systemName, String userName);

    /**
     * Queues a new AudioCommand for subsequent execution.
     * <p>
     * If newAudioCommand is null the current queue is executed and cleaned.
     *
     * @param newAudioCommand AudioCommand to queue or null to execute queue
     * @return true, if further commands exist; false, if empty
     */
    public boolean audioCommandQueue(AudioCommand newAudioCommand);

    /**
     * Get the currently active Command thread.
     *
     * @return active CommandThread
     */
    public Thread getCommandThread();

    /**
     * Set if this AudioFactory should attenuate sources based on their
     * distance from the listener.
     * <p>
     * Default = true
     *
     * @param attenuated true if distance attenuation to be used
     */
    public void setDistanceAttenuated(boolean attenuated);

    /**
     * Determine if this AudioFactory attenuates sources based on their
     * distance from the Listener.
     *
     * @return true if distance attenuation used
     */
    public boolean isDistanceAttenuated();

}
