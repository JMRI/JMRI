// JavaSoundAudioSource.java

package jmri.jmrit.audio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import jmri.InstanceManager;
import javax.vecmath.Vector3f;

/**
 * JavaSound implementation of the Audio Source sub-class.
 * <p>
 * For now, no system-specific implementations are forseen - this will remain
 * internal-only
 * <p>
 * For more information about the JavaSound API, visit
 * <a href="http://java.sun.com/products/java-media/sound/">http://java.sun.com/products/java-media/sound/</a>
 *
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under
 * the terms of version 2 of the GNU General Public License as published
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * for more details.
 * <p>
 *
 * @author Matthew Harris  copyright (c) 2009
 * @version $Revision$
 */
public class JavaSoundAudioSource extends AbstractAudioSource {

    /**
     * Reference to JavaSound mixer object
     */
    private static Mixer _mixer = JavaSoundAudioFactory.getMixer();

    /**
     * Reference to current active AudioListener
     */
    private AudioListener _activeAudioListener = InstanceManager.audioManagerInstance().getActiveAudioFactory().getActiveAudioListener();

    /**
     * True if we've been initialised
     */
    private boolean _initialised = false;

    /**
     * Used for playing back sound source
     */
    private transient Clip _clip = null;

    /**
     * Holds reference to the JavaSoundAudioChannel object
     */
    private transient JavaSoundAudioChannel _audioChannel = null;


    private boolean _jsState;

    /**
     * Constructor for new JavaSoundAudioSource with system name
     *
     * @param systemName AudioSource object system name (e.g. IAS1)
     */
    public JavaSoundAudioSource(String systemName) {
        super(systemName);
        if (log.isDebugEnabled()) log.debug("New JavaSoundAudioSource: "+systemName);
        _initialised = init();
    }

    /**
     * Constructor for new JavaSoundAudioSource with system name and user name
     *
     * @param systemName AudioSource object system name (e.g. IAS1)
     * @param userName AudioSource object user name
     */
    public JavaSoundAudioSource(String systemName, String userName) {
        super(systemName, userName);
        if (log.isDebugEnabled()) log.debug("New JavaSoundAudioSource: "+userName+" ("+systemName+")");
        _initialised = init();
    }

    /**
     * Initialise this AudioSource
     *
     * @return True if initialised
     */
    private boolean init() {
        return true;
    }

    @SuppressWarnings("SleepWhileInLoop")
    @Override
    boolean bindAudioBuffer(AudioBuffer audioBuffer) {
        // First check we've been initialised
        if (!_initialised) {
            return false;
        }

        // Wait for AudioBuffer to be loaded, or 20 seconds
        long startTime = System.currentTimeMillis();
        while (audioBuffer.getState()!=AudioBuffer.STATE_LOADED &&
                System.currentTimeMillis()-startTime < 20000) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ex) {}            
        }

        if (audioBuffer instanceof JavaSoundAudioBuffer
                && audioBuffer.getState()==AudioBuffer.STATE_LOADED) {
            // Cast to JavaSoundAudioBuffer to enable easier access to specific methods
            JavaSoundAudioBuffer buffer = (JavaSoundAudioBuffer) audioBuffer;

            // Get a JavaSound DataLine and Clip
            DataLine.Info lineInfo;
            lineInfo = new DataLine.Info(Clip.class, buffer.getAudioFormat());
            Clip newClip = null;
            try {
                newClip = (Clip) _mixer.getLine(lineInfo);
            } catch (LineUnavailableException ex) {
                log.warn("Error binding JavaSoundSource (" + this.getSystemName() +
                         ") to AudioBuffer (" + this.getAssignedBufferName() + ") " + ex);
                return false;
            }

            this._clip = newClip;
            newClip = null;

            try {
                _clip.open(buffer.getAudioFormat(),
                           buffer.getDataStorageBuffer(),
                           0,
                           buffer.getDataStorageBuffer().length);
            } catch (LineUnavailableException ex) {
                log.warn("Error binding JavaSoundSource (" + this.getSystemName() +
                         ") to AudioBuffer (" + this.getAssignedBufferName() + ")" + ex );
            }
            if (log.isDebugEnabled()) log.debug("Bind JavaSoundAudioSource (" + this.getSystemName() +
                                            ") to JavaSoundAudioBuffer (" + audioBuffer.getSystemName() + ")");
            return true;
        } else {
            log.warn("AudioBuffer not loaded error when binding JavaSoundSource (" + this.getSystemName() +
                     ") to AudioBuffer (" + this.getAssignedBufferName() + ")");
            return false;
        }

    }

    @Override
    protected void changePosition(Vector3f pos) {
        if (_initialised && isBound() && _audioChannel != null) {
            calculateGain();
            calculatePan();
        }
    }
    
    @Override
    public void setGain(float gain) {
        super.setGain(gain);
        if (_initialised && isBound() && _audioChannel != null) {
            calculateGain();
        }
    }
    
    @Override
    public void setPitch(float pitch) {
        super.setPitch(pitch);
        if (_initialised && isBound() && _audioChannel != null) {
            calculatePitch();
        }
    }
    
    @Override
    public void setReferenceDistance(float referenceDistance) {
        super.setReferenceDistance(referenceDistance);
        if (_initialised && isBound() && _audioChannel != null) {
            calculateGain();
        }
    }

    @Override
    public int getState() {
        boolean old = _jsState;
        _jsState = (this._clip!=null?this._clip.isActive():false);
        if (_jsState != old) {
            if (_jsState == true) {
                this.setState(STATE_PLAYING);
            } else {
                this.setState(STATE_STOPPED);
            }
        }
        return super.getState();
    }

    @Override
    public void stateChanged(int oldState) {
        super.stateChanged(oldState);
        if (_initialised && isBound() && _audioChannel != null) {
            calculateGain();
            calculatePan();
            calculatePitch();
        } else {
            _initialised = init();
        }
        
    }

    @Override
    protected void doPlay() {
        if (log.isDebugEnabled()) log.debug("Play JavaSoundAudioSource (" + this.getSystemName() + ")");
        if (_initialised && isBound()) {
            doRewind();
            doResume();
        }
    }

    @Override
    protected void doStop() {
        if (log.isDebugEnabled()) log.debug("Stop JavaSoundAudioSource (" + this.getSystemName() + ")");
        if (_initialised && isBound()) {
            doPause();
            doRewind();
        }
    }

    @Override
    protected void doPause() {
        if (log.isDebugEnabled()) log.debug("Pause JavaSoundAudioSource (" + this.getSystemName() + ")");
        if (_initialised && isBound()) {
            this._clip.stop();
            if (_audioChannel != null) {
                if (log.isDebugEnabled())
                    log.debug("Remove JavaSoundAudioChannel for Source " + this.getSystemName());
                _audioChannel = null;
            }
        }
        this.setState(STATE_STOPPED);
    }

    @Override
    protected void doResume() {
        if (log.isDebugEnabled()) log.debug("Resume JavaSoundAudioSource (" + this.getSystemName() + ")");
        if (_initialised && isBound()) {
            if (_audioChannel == null) {
                if (log.isDebugEnabled())
                    log.debug("Create JavaSoundAudioChannel for Source " + this.getSystemName());
                _audioChannel = new JavaSoundAudioChannel(this);
            }
            this._clip.loop(this.getNumLoops());
            this.setState(STATE_PLAYING);
        }
    }

    @Override
    protected void doRewind() {
        if (log.isDebugEnabled()) log.debug("Rewind JavaSoundAudioSource (" + this.getSystemName() + ")");
        if (_initialised && isBound()) {
            this._clip.setFramePosition(0);
        }
    }

    @Override
    protected void doFadeIn() {
        if (log.isDebugEnabled()) log.debug("Fade-in JavaSoundAudioSource (" + this.getSystemName() + ")");
        if (_initialised && isBound()) {
            doPlay();
            AudioSourceFadeThread asft = new AudioSourceFadeThread(this);
            asft.start();
        }
    }

    @Override
    protected void doFadeOut() {
        if (log.isDebugEnabled()) log.debug("Fade-out JavaSoundAudioSource (" + this.getSystemName() + ")");
        if (_initialised && isBound()) {
            AudioSourceFadeThread asft = new AudioSourceFadeThread(this);
            asft.start();
        }
    }

    @Override
    protected void cleanUp() {
        if (_initialised && isBound()) {
            this._clip.stop();
            this._clip.close();
            this._clip = null;
        }
        if (log.isDebugEnabled()) log.debug("Cleanup JavaSoundAudioSource (" + this.getSystemName() + ")");
        this.dispose();
    }

    /**
     * Calculates the panning of this Source between fully left (-1.0f)
     * and fully right (1.0f)
     * <p>
     * Calculated internally from the relative positions of this source and
     * the listener.
     */
    protected void calculatePan() {
        Vector3f side = new Vector3f();
        side.cross(_activeAudioListener.getOrientation(UP), _activeAudioListener.getOrientation(AT));
        side.normalize();
        Vector3f vecX = new Vector3f(this.getCurrentPosition());
        Vector3f vecZ = new Vector3f(this.getCurrentPosition());
        float x = vecX.dot(side);
        float z = vecZ.dot(_activeAudioListener.getOrientation(AT));
        float angle = (float) Math.atan2( x, z );
        float pan = (float) - Math.sin( angle );

        // If playing, update the pan
        if (_audioChannel != null) {
            _audioChannel.setPan(pan);
        }
        if (log.isDebugEnabled())
            log.debug("Set pan of JavaSoundAudioSource " + this.getSystemName() + " to " + pan);
    }

    @Override
    protected void calculateGain() {

        // Calculate distance from listener
        Vector3f distance = new Vector3f(this.getCurrentPosition());
        if (!this.isPositionRelative()) {
            distance.sub(_activeAudioListener.getCurrentPosition());
        }

        float distanceFromListener =
                (float) Math.sqrt( distance.dot(distance));
        if (log.isDebugEnabled())
            log.debug("Distance of JavaSoundAudioSource " + this.getSystemName() + " from Listener = " + distanceFromListener);

        // Default value to start with (used for no distance attenuation)
        float currentGain = 1.0f;

        if (InstanceManager.audioManagerInstance().getActiveAudioFactory().isDistanceAttenuated())
        {
            // Calculate gain of this source using clamped inverse distance
            // attenuation model

            distanceFromListener = Math.max(distanceFromListener, this.getReferenceDistance());
            if (log.isDebugEnabled())
                log.debug("After initial clamping, distance of JavaSoundAudioSource " + this.getSystemName() + " from Listener = " + distanceFromListener);
            distanceFromListener = Math.min(distanceFromListener, this.getMaximumDistance());
            if (log.isDebugEnabled())
                log.debug("After final clamping, distance of JavaSoundAudioSource " + this.getSystemName() + " from Listener = " + distanceFromListener);

            currentGain =
                    _activeAudioListener.getMetersPerUnit()
                    * (this.getReferenceDistance()
                        / (this.getReferenceDistance() + this.getRollOffFactor()
                        * (distanceFromListener - this.getReferenceDistance())));
            if (log.isDebugEnabled())
                log.debug("Calculated for JavaSoundAudioSource " + this.getSystemName() + " gain = " + currentGain);

            // Ensure that gain is between 0 and 1
            if (currentGain > 1.0f) {
                currentGain = 1.0f;
            } else if (currentGain < 0.0f) {
                currentGain = 0.0f;
            }
        }

        // Finally, adjust based on master gain for this source, the gain
        // of listener and any calculated fade gains
        currentGain *= this.getGain() * _activeAudioListener.getGain() * this.getFadeGain();

        // If playing, update the gain
        if (_audioChannel != null) {
            _audioChannel.setGain(currentGain);
            if (log.isDebugEnabled())
                log.debug("Set current gain of JavaSoundAudioSource " + this.getSystemName() + " to " + currentGain);
        }
    }

    /**
     * Internal method used to calculate the pitch
     */
    protected void calculatePitch() {
        // If playing, update the pitch
        if (_audioChannel != null) {
            _audioChannel.setPitch(this.getPitch());
        }
    }

    private static final Logger log = LoggerFactory.getLogger(JavaSoundAudioSource.class.getName());

    private static class JavaSoundAudioChannel {

        /**
        * Control for changing the gain of this AudioSource
        */
        private FloatControl _gainControl = null;

        /**
        * Control for changing the pan of this AudioSource
        */
        private FloatControl _panControl = null;

        /**
        * Control for changing the sample rate of this AudioSource
        */
        private FloatControl _sampleRateControl = null;

        /**
         * Holds the initial sample rate setting
         */
        private float _initialSampleRate = 0.0f;

        /**
         * Holds the initial gain setting
         */
        private float _initialGain = 0.0f;

        /**
         * Holds reference to the parent AudioSource object
         */
        private JavaSoundAudioSource _audio;

        /**
         * Holds reference to the JavaSound clip
         */
        private Clip _clip;

        /**
         * Constructor for creating an AudioChannel for a specific
         * JavaSoundAudioSource
         * 
         * @param audio the specific JavaSoundAudioSource
         */
        public JavaSoundAudioChannel(JavaSoundAudioSource audio) {

            this._audio = audio;
            this._clip = this._audio._clip;

            // Check if changing gain is supported
            if (this._clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                // Yes, so create a new gain control
                this._gainControl = (FloatControl) this._clip.getControl(FloatControl.Type.MASTER_GAIN);
                this._initialGain = this._gainControl.getValue();
                if (log.isDebugEnabled()) {
                    log.debug("JavaSound gain control created");
                    log.debug("Initial Gain = " + this._initialGain);
                }
            } else {
                log.info("Gain control is not supported");
                this._gainControl = null;
            }

            // Check if changing pan is supported
            if (this._clip.isControlSupported(FloatControl.Type.PAN)) {
                // Yes, so create a new pan control
                this._panControl = (FloatControl) this._clip.getControl(FloatControl.Type.PAN);
                if (log.isDebugEnabled())
                    log.debug("JavaSound pan control created");
            } else {
                log.info("Pan control is not supported");
                this._panControl = null;
            }

            // Check if changing pitch is supported
            if (this._clip.isControlSupported(FloatControl.Type.SAMPLE_RATE)) {
                // Yes, so create a new pitch control
                this._sampleRateControl = (FloatControl) this._clip.getControl(FloatControl.Type.SAMPLE_RATE);
                this._initialSampleRate = this._sampleRateControl.getValue();
                if (log.isDebugEnabled()) {
                    log.debug("JavaSound pitch control created");
                    log.debug("Initial Sample Rate = " + this._initialSampleRate);
                }
            } else {
                log.info("Sample Rate control is not supported");
                this._sampleRateControl = null;
                this._initialSampleRate = 0;
            }
        }

        /**
         * Sets the gain of this AudioChannel
         * 
         * @param gain the gain (0.0f to 1.0f)
         */
        protected void setGain(float gain) {
            if (this._gainControl != null) {
                // Ensure gain is within limits
                if (gain <= 0.0f) {
                    gain = 0.0001f;
                } else if (gain > 1.0f) {
                    gain = 1.0f;
                }
                
                // Convert this linear gain to a decibel value
                float dB = (float) ( Math.log( gain ) / Math.log( 10.0 ) * 20.0 );

                this._gainControl.setValue(dB);
                if (log.isDebugEnabled())
                    log.debug("Actual gain value of JavaSoundAudioSource " + this._audio + " is " + this._gainControl.getValue());
            }
            if (log.isDebugEnabled())
                log.debug("Set gain of JavaSoundAudioSource " + this._audio + " to " + gain);
        }

        /**
         * Sets the pan of this AudioChannel
         * 
         * @param pan the pan (-1.0f to 1.0f)
         */
        protected void setPan(float pan) {
            if (this._panControl != null) {
                this._panControl.setValue(pan);
            }
            if (log.isDebugEnabled())
                log.debug("Set pan of JavaSoundAudioSource " + this._audio + " to " + pan);
        }

        /**
         * Sets the pitch of this AudioChannel
         * <p>
         * Calculated as a ratio of the initial sample rate
         * 
         * @param pitch the pitch
         */
        protected void setPitch(float pitch) {
            if (this._sampleRateControl != null) {
                this._sampleRateControl.setValue(pitch * this._initialSampleRate);
            }
            if (log.isDebugEnabled())
                log.debug("Set pitch of JavaSoundAudioSource " + this._audio + " to " + pitch);
        }
    }
    
    private static final long serialVersionUID = 1L;
}

/* $(#)JavaSoundAudioSource.java */
