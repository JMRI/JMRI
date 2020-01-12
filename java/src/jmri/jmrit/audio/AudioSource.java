package jmri.jmrit.audio;

import java.util.Queue;
import javax.vecmath.Vector3f;
import jmri.Audio;

/**
 * Represent an AudioSource, a place to store or control sound information.
 * <p>
 * The AbstractAudio class contains a basic implementation of the state and
 * messaging code, and forms a useful start for a system-specific
 * implementation. Specific implementations in the jmrix package, e.g. for
 * LocoNet and NCE, will convert to and from the layout commands.
 * <p>
 * The states and names are Java Bean parameters, so that listeners can be
 * registered to be notified of any changes.
 * <p>
 * Each AudioSource object has a two names. The "user" name is entirely free
 * form, and can be used for any purpose. The "system" name is provided by the
 * system-specific implementations, and provides a unique mapping to the layout
 * control system (for example LocoNet or NCE) and address within that system.
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
public interface AudioSource extends Audio {

    /**
     * Constant to define that this source should loop continously when played
     */
    public static final int LOOP_CONTINUOUS = -1;

    /**
     * Constant to define that this source should not loop when played
     */
    public static final int LOOP_NONE = 0;

    /**
     * Sets the position of this AudioSource object
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Listener
     * <li>Source
     * </ul>
     *
     * @param pos 3d position vector
     */
    public void setPosition(Vector3f pos);

    /**
     * Sets the position of this AudioSource object in x, y and z planes
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Listener
     * <li>Source
     * </ul>
     *
     * @param x x-coordinate
     * @param y y-coordinate
     * @param z z-coordinate
     */
    public void setPosition(float x, float y, float z);

    /**
     * Sets the position of this AudioSource object in x and y planes with z
     * plane position fixed at zero
     * <p>
     * Equivalent to setPosition(x, y, 0.0f)
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Listener
     * <li>Source
     * </ul>
     *
     * @param x x-coordinate
     * @param y y-coordinate
     */
    public void setPosition(float x, float y);

    /**
     * Returns the position of this AudioSource object as a 3-dimensional
     * vector.
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Listener
     * <li>Source
     * </ul>
     *
     * @return 3d position vector
     */
    public Vector3f getPosition();

    /**
     * Returns the current position of this AudioSource object as a
     * 3-dimensional vector.
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Listener
     * <li>Source
     * </ul>
     *
     * @return 3d position vector
     */
    public Vector3f getCurrentPosition();

    /**
     * Method to reset the current position of this AudioSource object to the
     * initial position as defined by setPosition.
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Listener
     * <li>Source
     * </ul>
     */
    public void resetCurrentPosition();

    /**
     * Sets the position of this AudioSource object to be relative to the
     * position of the AudioListener object or absolute.
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Source
     * </ul>
     *
     * @param relative position relative or absolute
     */
    public void setPositionRelative(boolean relative);

    /**
     * Returns a boolean value that determines if the position of this
     * AudioSource object is relative to the position of the AudioListener
     * object or absolute.
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Source
     * </ul>
     *
     * @return boolean position relative
     */
    public boolean isPositionRelative();

    /**
     * Sets the velocity of this AudioSource object
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Listener
     * <li>Source
     * </ul>
     *
     * @param vel 3d velocity vector
     */
    public void setVelocity(Vector3f vel);

    /**
     * Returns the velocity of this AudioSource object
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Listener
     * <li>Source
     * </ul>
     *
     * @return 3d velocity vector
     */
    public Vector3f getVelocity();

    /**
     * Returns linked AudioBuffer object
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Source
     * </ul>
     *
     * @return AudioBuffer the AudioBuffer object bound to this AudioSource
     */
    public AudioBuffer getAssignedBuffer();

    /**
     * Return system name of linked AudioBuffer object
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Source
     * </ul>
     *
     * @return sysName the SystemName of the AudioBuffer bound to this
     *         AudioSource
     */
    public String getAssignedBufferName();

    /**
     * Sets the linked AudioBuffer object
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Source
     * </ul>
     *
     * @param audioBuffer the AudioBuffer object to bind to this AudioSource
     */
    public void setAssignedBuffer(AudioBuffer audioBuffer);

    /**
     * Sets the system name of the linked AudioBuffer object
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Source
     * </ul>
     *
     * @param sysName the SystemName of the AudioBuffer (i.e. IAB1) to bind to
     *                this AudioSource
     */
    public void setAssignedBuffer(String sysName);

    /**
     * Queues the linked AudioBuffer object to this Source's buffer queue
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Source
     * </ul>
     *
     * @param audioBuffers the AudioBuffer object to enqueue to this AudioSource
     * @return true if successfully queued audioBuffers; false otherwise
     */
    public boolean queueBuffers(Queue<AudioBuffer> audioBuffers);

    public boolean queueBuffer(AudioBuffer audioBuffer);

    public boolean unqueueBuffers();

    public int numQueuedBuffers();

    public int numProcessedBuffers();

    /**
     * Method to return if this AudioSource has been bound to an AudioBuffer
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Source
     * </ul>
     *
     * @return True if bound to an AudioBuffer
     */
    public boolean isBound();

    /**
     * Method to return if this AudioSource has AudioBuffers queued to it
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Source
     * </ul>
     *
     * @return True if AudioBuffers are queued.
     */
    public boolean isQueued();

    /**
     * Return the currently stored gain setting
     * <p>
     * Default value = 1.0f
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Listener
     * <li>Source
     * </ul>
     *
     * @return gain setting of this AudioSource
     */
    public float getGain();

    /**
     * Set the gain of this AudioSource object
     * <p>
     * Default value = 1.0f
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Listener
     * <li>Source
     * </ul>
     *
     * @param gain the gain of this AudioSource
     */
    public void setGain(float gain);

    /**
     * Return the current pitch setting
     * <p>
     * Values are restricted from 0.5f to 2.0f, i.e. half to double
     * <p>
     * Default value = 1.0f
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Source
     * </ul>
     *
     * @return pitch of this AudioSource
     */
    public float getPitch();

    /**
     * Set the pitch of this AudioSource object
     * <p>
     * Values are restricted from 0.5f to 2.0f, i.e. half to double
     * <p>
     * Default value = 1.0f
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Source
     * </ul>
     *
     * @param pitch the pitch of this AudioSource
     */
    public void setPitch(float pitch);

    /**
     * Return the current reference distance setting
     * <p>
     * Default value = 1.0f
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Source
     * </ul>
     *
     * @return Reference Distance of this AudioSource
     */
    public float getReferenceDistance();

    /**
     * Set the reference distance of this AudioSource object.
     * <p>
     * Default value = 1.0f
     * <p>
     * The Reference Distance is one of the main parameters you have for
     * controlling the way that sounds attenuate with distance. A Source with
     * Reference Distance set to 5 (meters) will be at maximum volume while it
     * is within 5 metere of the listener, and start to fade out as it moves
     * further away. At 10 meters it will be at half volume, and at 20 meters at
     * a quarter volume, etc ...
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Source
     * </ul>
     *
     * @param referenceDistance the Reference Distance for this AudioSource
     */
    public void setReferenceDistance(float referenceDistance);

    /**
     * Set the offset in which to start playback of this AudioSource.
     * <p>
     * Default value = 0
     * <p>
     * Value is clamped between 0 and length of attached AudioBuffer
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Source
     * </ul>
     *
     * @param offset the offset in samples marking the point to commence
     *               playback
     */
    public void setOffset(long offset);

    /**
     * Return the offset in which to start playback of this AudioSource.
     * <p>
     * Default value = 0
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Source
     * </ul>
     *
     * @return the offset in samples marking the point to commence playback
     */
    public long getOffset();

    /**
     * Return the current maximum distance setting.
     * <p>
     * Default value = Audio.MAX_DISTANCE
     * <p>
     * The maximum distance is that where the volume of the sound would normally
     * be zero.
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Source
     * </ul>
     * @return the maximum distance
     */
    public float getMaximumDistance();

    /**
     * Set the current maximum distance setting.
     * <p>
     * Default value = Audio.MAX_DISTANCE
     * <p>
     * The maximum distance is that where the volume of the sound would normally
     * be zero.
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Source
     * </ul>
     *
     * @param maximumDistance maximum distance of this source
     */
    public void setMaximumDistance(float maximumDistance);

    /**
     * Set the roll-off factor of this AudioSource object.
     * <p>
     * Default value = 1.0f
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Source
     * </ul>
     *
     * @param rollOffFactor roll-off factor
     */
    public void setRollOffFactor(float rollOffFactor);

    /**
     * Get the roll-off factor of this AudioSource object.
     * <p>
     * Default value = 1.0f
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Source
     * </ul>
     * @return the roll-off factor
     */
    public float getRollOffFactor();

    /**
     * Check if this AudioSource object will loop or not.
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Source
     * </ul>
     *
     * @return boolean loop
     */
    public boolean isLooped();

    /**
     * Sets this AudioSource object to loop infinitely or not.
     * <p>
     * When loop == false, sets the min and max number of loops to zero.
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Source
     * </ul>
     *
     * @param loop infinite loop setting
     */
    public void setLooped(boolean loop);

    /**
     * Returns the minimum number of times that this AudioSource will loop, or
     * LOOP_CONTINUOUS for infinite looping.
     * <p>
     * Default value = 0
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Source
     * </ul>
     *
     * @return number of loops
     */
    public int getMinLoops();

    /**
     * The minimum number of times that this AudioSource should loop.
     * <p>
     * When set to 1, the sound will loop once (i.e. play through twice).
     * <p>
     * When set to LOOP_CONTINUOUS, determines that this AudioSource object
     * should loop indefinitely until explicitly stopped.
     * <p>
     * Default value = 0
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Source
     * </ul>
     *
     * @param loops minimum number of loops
     */
    public void setMinLoops(int loops);

    /**
     * Returns the maximum number of times that this AudioSource will loop, or
     * LOOP_CONTINUOUS for infinite looping.
     * <p>
     * Default value = 0
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Source
     * </ul>
     *
     * @return maximum number of loops
     */
    public int getMaxLoops();

    /**
     * The maximum number of times that this AudioSource should loop.
     * <p>
     * When set to 1, the sound will loop once (i.e. play through twice).
     * <p>
     * When set to LOOP_CONTINUOUS, determines that this AudioSource object
     * should loop indefinitely until explicitly stopped.
     * <p>
     * Default value = 0
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Source
     * </ul>
     *
     * @param loops maximum number of loops
     */
    public void setMaxLoops(int loops);

    /**
     * The number of times that this AudioSource should loop, or LOOP_CONTINUOUS
     * for infinite looping.
     * <p>
     * When the minimum and maximum number of loops are different, each call to
     * this method will return a different random number that lies between the
     * two settings:
     * <pre>
     * minimum {@literal <=} number of loops {@literal <=} maximum
     * </pre> Default value = 0
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Source
     * </ul>
     *
     * @return number of loops
     */
    public int getNumLoops();

//    /**
//     * Set the minimum length of time in milliseconds to wait before
//     * playing a subsequent loop of this source.
//     * <p>
//     * Not applicable when number of loops is LOOP_NONE or LOOP_CONTINUOUS
//     * <p>
//     * Default value = 0
//     * <p>
//     * Applies only to sub-types:
//     * <ul>
//     * <li>Source
//     * </ul>
//     * @param loopDelay minimum time in milliseconds to wait
//     */
//    public void setMinLoopDelay(int loopDelay);
//
//    /**
//     * Retrieve the minimum length of time in milliseconds to wait before
//     * playing a subsequent loop of this source.
//     * <p>
//     * Not applicable when number of loops is LOOP_NONE or LOOP_CONTINUOUS
//     * <p>
//     * Default value = 0
//     * <p>
//     * Applies only to sub-types:
//     * <ul>
//     * <li>Source
//     * </ul>
//     * @return minimum time in milliseconds to wait
//     */
//    public int getMinLoopDelay();
//
//    /**
//     * Set the maximum length of time in milliseconds to wait before
//     * playing a subsequent loop of this source.
//     * <p>
//     * Not applicable when number of loops is LOOP_NONE or LOOP_CONTINUOUS
//     * <p>
//     * Default value = 0
//     * <p>
//     * Applies only to sub-types:
//     * <ul>
//     * <li>Source
//     * </ul>
//     * @param loopDelay maximum time in milliseconds to wait
//     */
//    public void setMaxLoopDelay(int loopDelay);
//
//    /**
//     * Set the maximum length of time in milliseconds to wait before
//     * playing a subsequent loop of this source.
//     * <p>
//     * Not applicable when number of loops is LOOP_NONE or LOOP_CONTINUOUS
//     * <p>
//     * Default value = 0
//     * <p>
//     * Applies only to sub-types:
//     * <ul>
//     * <li>Source
//     * </ul>
//     * @return maximum time in milliseconds to wait
//     */
//    public int getMaxLoopDelay();
//
//    /**
//     * The length of time in milliseconds that this source should wait
//     * before playing a subsequent loop.
//     * <p>
//     * Not applicable when number of loops is LOOP_NONE or LOOP_CONTINUOUS
//     * <p>
//     * When the minimum and maximum delay times are different, each call
//     * to this method will return a different random number that lies between
//     * the two settings:
//     * <pre>
//     * minimum &lt= delay time &lt= maximum
//     * </pre>
//     * Default value = 0
//     * <p>
//     * Applies only to sub-types:
//     * <ul>
//     * <li>Source
//     * </ul>
//     * @return time in milliseconds to wait
//     */
//    public int getLoopDelay();
    /**
     * Set the length of time in milliseconds to fade this source in
     * <p>
     * Default value = 1000
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Source
     * </ul>
     *
     * @param fadeInTime fade-in time in milliseconds
     */
    public void setFadeIn(int fadeInTime);

    /**
     * Retrieve the length of time in milliseconds to fade this source in
     * <p>
     * Default value = 1000
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Source
     * </ul>
     *
     * @return fade-in time in milliseconds
     */
    public int getFadeIn();

    /**
     * Set the length of time in milliseconds to fade this source in
     * <p>
     * Default value = 1000
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Source
     * </ul>
     *
     * @param fadeOutTime fade-out time in milliseconds
     */
    public void setFadeOut(int fadeOutTime);

    /**
     * Retrieve the length of time in milliseconds to fade this source in
     * <p>
     * Default value = 1000
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Source
     * </ul>
     *
     * @return fade-in time in milliseconds
     */
    public int getFadeOut();

    /**
     * Set the doppler factor of this source
     * <p>
     * Default value = 1.0f
     * <p>
     * Only calculated for JoalAudioSources
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Source
     * </ul>
     *
     * @param dopplerFactor factor to apply in doppler calculations
     */
    @Deprecated
    default public void setDopplerFactor(float dopplerFactor) {}

    /**
     * Retrieve the doppler factor of this source
     * <p>
     * Default value = 1.0f
     * <p>
     * Only calculated for JoalAudioSources
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Source
     * </ul>
     *
     * @return factor to apply in doppler calculations
     */
    @Deprecated
    default public float getDopplerFactor() { return 1.0f; }

    /**
     * Method to start playing this AudioSource Object
     * <p>
     * If this AudioSource is already playing, this command is ignored.
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Source
     * </ul>
     */
    public void play();

    /**
     * Method to stop playing this AudioSource Object
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Source
     * </ul>
     */
    public void stop();

    /**
     * Method to toggle playback of this AudioSource Object reseting position
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Source
     * </ul>
     */
    public void togglePlay();

    /**
     * Method to pause playing this AudioSource Object
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Source
     * </ul>
     */
    public void pause();

    /**
     * Method to resume playing this AudioSource Object
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Source
     * </ul>
     */
    public void resume();

    /**
     * Method to toggle playback of this AudioSource Object retaining postition
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Source
     * </ul>
     */
    public void togglePause();

    /**
     * Method to rewind this AudioSource Object
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Source
     * </ul>
     */
    public void rewind();

    /**
     * Method to fade in and then play this AudioSource Object
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Source
     * </ul>
     */
    public void fadeIn();

    /**
     * Method to fade out and then stop this AudioSource Object only when it is
     * already playing.
     * <p>
     * If not playing, command is ignored.
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Source
     * </ul>
     */
    public void fadeOut();

}
