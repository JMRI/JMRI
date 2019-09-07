package jmri.jmrit.audio;

import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.vecmath.Vector3f;
import jmri.InstanceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JavaSound implementation of the Audio Source sub-class.
 * <p>
 * For now, no system-specific implementations are foreseen - this will remain
 * internal-only
 * <p>
 * For more information about the JavaSound API, visit
 * <a href="http://java.sun.com/products/java-media/sound/">http://java.sun.com/products/java-media/sound/</a>
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
public class JavaSoundAudioSource extends AbstractAudioSource {

    /**
     * Reference to JavaSound mixer object
     */
    private static Mixer mixer = JavaSoundAudioFactory.getMixer();

    /**
     * Reference to current active AudioListener
     */
    private AudioListener activeAudioListener = loadAudioListener();

    private AudioListener loadAudioListener() {
        AudioFactory audioFact = InstanceManager.getDefault(jmri.AudioManager.class).getActiveAudioFactory();
        if (audioFact != null) {
            return audioFact.getActiveAudioListener();
        }
        log.error("no AudioListener found");
        return null;
    }

    /**
     * True if we've been initialised
     */
    private boolean initialised = false;

    /**
     * Used for playing back sound source
     */
    private transient Clip clip = null;

    /**
     * Holds reference to the JavaSoundAudioChannel object
     */
    private transient JavaSoundAudioChannel audioChannel = null;

    private boolean jsState;

    /**
     * Constructor for new JavaSoundAudioSource with system name
     *
     * @param systemName AudioSource object system name (e.g. IAS1)
     */
    public JavaSoundAudioSource(String systemName) {
        super(systemName);
        log.debug("New JavaSoundAudioSource: {}", systemName);
        initialised = init();
    }

    /**
     * Constructor for new JavaSoundAudioSource with system name and user name
     *
     * @param systemName AudioSource object system name (e.g. IAS1)
     * @param userName   AudioSource object user name
     */
    public JavaSoundAudioSource(String systemName, String userName) {
        super(systemName, userName);
        log.debug("New JavaSoundAudioSource: {} ({})", userName, systemName);
        initialised = init();
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
        if (!initialised) {
            return false;
        }

        // Wait for AudioBuffer to be loaded, or 20 seconds
        long startTime = System.currentTimeMillis();
        while (audioBuffer.getState() != AudioBuffer.STATE_LOADED
                && System.currentTimeMillis() - startTime < 20000) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ex) {
                log.debug("bindAudioBuffer was interruped");
            }
        }

        if (audioBuffer instanceof JavaSoundAudioBuffer
                && audioBuffer.getState() == AudioBuffer.STATE_LOADED) {
            // Cast to JavaSoundAudioBuffer to enable easier access to specific methods
            JavaSoundAudioBuffer buffer = (JavaSoundAudioBuffer) audioBuffer;

            // Get a JavaSound DataLine and Clip
            DataLine.Info lineInfo;
            lineInfo = new DataLine.Info(Clip.class, buffer.getAudioFormat());
            Clip newClip;
            try {
                newClip = (Clip) mixer.getLine(lineInfo);
            } catch (LineUnavailableException ex) {
                log.warn("Error binding JavaSoundSource ({}) to AudioBuffer ({}) ",
                        this.getSystemName(), this.getAssignedBufferName(), ex);
                return false;
            }

            this.clip = newClip;

            try {
                clip.open(buffer.getAudioFormat(),
                        buffer.getDataStorageBuffer(),
                        0,
                        buffer.getDataStorageBuffer().length);
            } catch (LineUnavailableException ex) {
                log.warn("Error binding JavaSoundSource ({}) to AudioBuffer ({}) ",
                        this.getSystemName(), this.getAssignedBufferName(), ex);
            }
            if (log.isDebugEnabled()) {
                log.debug("Bind JavaSoundAudioSource ({}) to JavaSoundAudioBuffer ({})",
                        this.getSystemName(), audioBuffer.getSystemName());
            }
            return true;
        } else {
            log.warn("AudioBuffer not loaded error when binding JavaSoundSource ({}) to AudioBuffer ({})",
                    this.getSystemName(), this.getAssignedBufferName());
            return false;
        }
    }

    @Override
    protected void changePosition(Vector3f pos) {
        if (initialised && isBound() && audioChannel != null) {
            calculateGain();
            calculatePan();
        }
    }

    @Override
    public void setGain(float gain) {
        super.setGain(gain);
        if (initialised && isBound() && audioChannel != null) {
            calculateGain();
        }
    }

    @Override
    public void setPitch(float pitch) {
        super.setPitch(pitch);
        if (initialised && isBound() && audioChannel != null) {
            calculatePitch();
        }
    }

    @Override
    public void setReferenceDistance(float referenceDistance) {
        super.setReferenceDistance(referenceDistance);
        if (initialised && isBound() && audioChannel != null) {
            calculateGain();
        }
    }

    @Override
    public void setOffset(long offset) {
        super.setOffset(offset);
        if (initialised && isBound() && audioChannel != null) {
            this.clip.setFramePosition((int) offset);
        }
    }

    @Override
    public int getState() {
        boolean old = jsState;
        jsState = (this.clip != null && this.clip.isActive());
        if (jsState != old) {
            if (jsState) {
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
        if (initialised && isBound() && audioChannel != null) {
            calculateGain();
            calculatePan();
            calculatePitch();
        } else {
            initialised = init();
        }
    }

    @Override
    protected void doPlay() {
        log.debug("Play JavaSoundAudioSource ({})", this.getSystemName());
        if (initialised && isBound()) {
            doRewind();
            doResume();
        }
    }

    @Override
    protected void doStop() {
        log.debug("Stop JavaSoundAudioSource ({})", this.getSystemName());
        if (initialised && isBound()) {
            doPause();
            doRewind();
        }
    }

    @Override
    protected void doPause() {
        if (log.isDebugEnabled()) {
            log.debug("Pause JavaSoundAudioSource ({})", this.getSystemName());
        }
        if (initialised && isBound()) {
            this.clip.stop();
            if (audioChannel != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Remove JavaSoundAudioChannel for Source {}", this.getSystemName());
                }
                audioChannel = null;
            }
        }
        this.setState(STATE_STOPPED);
    }

    @Override
    protected void doResume() {
        if (log.isDebugEnabled()) {
            log.debug("Resume JavaSoundAudioSource ({})", this.getSystemName());
        }
        if (initialised && isBound()) {
            if (audioChannel == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Create JavaSoundAudioChannel for Source {}", this.getSystemName());
                }
                audioChannel = new JavaSoundAudioChannel(this);
            }
            this.clip.loop(this.getNumLoops());
            this.setState(STATE_PLAYING);
        }
    }

    @Override
    protected void doRewind() {
        if (log.isDebugEnabled()) {
            log.debug("Rewind JavaSoundAudioSource ({})", this.getSystemName());
        }
        if (initialised && isBound()) {
            this.clip.setFramePosition(0);
        }
    }

    @Override
    protected void doFadeIn() {
        if (log.isDebugEnabled()) {
            log.debug("Fade-in JavaSoundAudioSource ({})", this.getSystemName());
        }
        if (initialised && isBound()) {
            doPlay();
            AudioSourceFadeThread asft = new AudioSourceFadeThread(this);
            asft.start();
        }
    }

    @Override
    protected void doFadeOut() {
        if (log.isDebugEnabled()) {
            log.debug("Fade-out JavaSoundAudioSource ({})", this.getSystemName());
        }
        if (initialised && isBound()) {
            AudioSourceFadeThread asft = new AudioSourceFadeThread(this);
            asft.start();
        }
    }

    @Override
    protected void cleanup() {
        if (initialised && isBound()) {
            this.clip.stop();
            this.clip.close();
            this.clip = null;
        }
        if (log.isDebugEnabled()) {
            log.debug("Cleanup JavaSoundAudioSource ({})", this.getSystemName());
        }
        this.dispose();
    }

    /**
     * Calculate the panning of this Source between fully left (-1.0f) and
     * fully right (1.0f)
     * <p>
     * Calculated internally from the relative positions of this source and the
     * listener.
     */
    protected void calculatePan() {
        Vector3f side = new Vector3f();
        side.cross(activeAudioListener.getOrientation(UP), activeAudioListener.getOrientation(AT));
        side.normalize();
        Vector3f vecX = new Vector3f(this.getCurrentPosition());
        Vector3f vecZ = new Vector3f(this.getCurrentPosition());
        float x = vecX.dot(side);
        float z = vecZ.dot(activeAudioListener.getOrientation(AT));
        float angle = (float) Math.atan2(x, z);
        float pan = (float) -Math.sin(angle);

        // If playing, update the pan
        if (audioChannel != null) {
            audioChannel.setPan(pan);
        }
        if (log.isDebugEnabled()) {
            log.debug("Set pan of JavaSoundAudioSource {} to {}", this.getSystemName(), pan);
        }
    }

    @Override
    protected void calculateGain() {

        // Calculate distance from listener
        Vector3f distance = new Vector3f(this.getCurrentPosition());
        if (!this.isPositionRelative()) {
            distance.sub(activeAudioListener.getCurrentPosition());
        }

        float distanceFromListener
                = (float) Math.sqrt(distance.dot(distance));
        if (log.isDebugEnabled()) {
            log.debug("Distance of JavaSoundAudioSource {} from Listener = {}", this.getSystemName(), distanceFromListener);
        }

        // Default value to start with (used for no distance attenuation)
        float currentGain = 1.0f;

        AudioFactory audioFact = InstanceManager.getDefault(jmri.AudioManager.class).getActiveAudioFactory();
        if (audioFact != null && audioFact.isDistanceAttenuated()) {
            // Calculate gain of this source using clamped inverse distance
            // attenuation model

            distanceFromListener = Math.max(distanceFromListener, this.getReferenceDistance());
            if (log.isDebugEnabled()) {
                log.debug("After initial clamping, distance of JavaSoundAudioSource {} from Listener = {}", this.getSystemName(), distanceFromListener);
            }
            distanceFromListener = Math.min(distanceFromListener, this.getMaximumDistance());
            if (log.isDebugEnabled()) {
                log.debug("After final clamping, distance of JavaSoundAudioSource {} from Listener = {}", this.getSystemName(), distanceFromListener);
            }

            currentGain
                    = activeAudioListener.getMetersPerUnit()
                    * (this.getReferenceDistance()
                    / (this.getReferenceDistance() + this.getRollOffFactor()
                    * (distanceFromListener - this.getReferenceDistance())));
            if (log.isDebugEnabled()) {
                log.debug("Calculated for JavaSoundAudioSource {} gain = {}", this.getSystemName(), currentGain);
            }

            // Ensure that gain is between 0 and 1
            if (currentGain > 1.0f) {
                currentGain = 1.0f;
            } else if (currentGain < 0.0f) {
                currentGain = 0.0f;
            }
        }

        // Finally, adjust based on master gain for this source, the gain
        // of listener and any calculated fade gains
        currentGain *= this.getGain() * activeAudioListener.getGain() * this.getFadeGain();

        // If playing, update the gain
        if (audioChannel != null) {
            audioChannel.setGain(currentGain);
            if (log.isDebugEnabled()) {
                log.debug("Set current gain of JavaSoundAudioSource {} to {}", this.getSystemName(), currentGain);
            }
        }
    }

    /**
     * Internal method used to calculate the pitch.
     */
    protected void calculatePitch() {
        // If playing, update the pitch
        if (audioChannel != null) {
            audioChannel.setPitch(this.getPitch());
        }
    }

    private static final Logger log = LoggerFactory.getLogger(JavaSoundAudioSource.class);

    private static class JavaSoundAudioChannel {

        /**
         * Control for changing the gain of this AudioSource
         */
        private FloatControl gainControl = null;

        /**
         * Control for changing the pan of this AudioSource
         */
        private FloatControl panControl = null;

        /**
         * Control for changing the sample rate of this AudioSource
         */
        private FloatControl sampleRateControl = null;

        /**
         * Holds the initial sample rate setting
         */
        private float initialSampleRate = 0.0f;

        /**
         * Holds the initial gain setting
         */
        private float initialGain = 0.0f;

        /**
         * Holds reference to the parent AudioSource object
         */
        private final JavaSoundAudioSource audio;

        /**
         * Holds reference to the JavaSound clip
         */
        private final Clip clip;

        /**
         * Constructor for creating an AudioChannel for a specific
         * JavaSoundAudioSource.
         *
         * @param audio the specific JavaSoundAudioSource
         */
        public JavaSoundAudioChannel(JavaSoundAudioSource audio) {

            this.audio = audio;
            this.clip = this.audio.clip;

            // Check if changing gain is supported
            if (this.clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                // Yes, so create a new gain control
                this.gainControl = (FloatControl) this.clip.getControl(FloatControl.Type.MASTER_GAIN);
                this.initialGain = this.gainControl.getValue();
                if (log.isDebugEnabled()) {
                    log.debug("JavaSound gain control created");
                    log.debug("Initial Gain = {}", this.initialGain);
                }
            } else {
                log.info("Gain control is not supported");
                this.gainControl = null;
            }

            // Check if changing pan is supported
            if (this.clip.isControlSupported(FloatControl.Type.PAN)) {
                // Yes, so create a new pan control
                this.panControl = (FloatControl) this.clip.getControl(FloatControl.Type.PAN);
                log.debug("JavaSound pan control created");
            } else {
                log.info("Pan control is not supported");
                this.panControl = null;
            }

            // Check if changing pitch is supported
            if (this.clip.isControlSupported(FloatControl.Type.SAMPLE_RATE)) {
                // Yes, so create a new pitch control
                this.sampleRateControl = (FloatControl) this.clip.getControl(FloatControl.Type.SAMPLE_RATE);
                this.initialSampleRate = this.sampleRateControl.getValue();
                if (log.isDebugEnabled()) {
                    log.debug("JavaSound pitch control created");
                    log.debug("Initial Sample Rate = {}", this.initialSampleRate);
                }
            } else {
                log.info("Sample Rate control is not supported");
                this.sampleRateControl = null;
                this.initialSampleRate = 0;
            }
        }

        /**
         * Set the gain of this AudioChannel.
         *
         * @param gain the gain (0.0f to 1.0f)
         */
        protected void setGain(float gain) {
            if (this.gainControl != null) {
                // Ensure gain is within limits
                if (gain <= 0.0f) {
                    gain = 0.0001f;
                } else if (gain > 1.0f) {
                    gain = 1.0f;
                }

                // Convert this linear gain to a decibel value
                float dB = (float) (Math.log(gain) / Math.log(10.0) * 20.0);

                this.gainControl.setValue(dB);
                if (log.isDebugEnabled()) {
                    log.debug("Actual gain value of JavaSoundAudioSource {} is {}", this.audio, this.gainControl.getValue());
                }
            }
            log.debug("Set gain of JavaSoundAudioSource {} to {}", this.audio, gain);
        }

        /**
         * Set the pan of this AudioChannel.
         *
         * @param pan the pan (-1.0f to 1.0f)
         */
        protected void setPan(float pan) {
            if (this.panControl != null) {
                this.panControl.setValue(pan);
            }
            log.debug("Set pan of JavaSoundAudioSource {} to {}", this.audio, pan);
        }

        /**
         * Set the pitch of this AudioChannel.
         * <p>
         * Calculated as a ratio of the initial sample rate
         *
         * @param pitch the pitch
         */
        protected void setPitch(float pitch) {
            if (this.sampleRateControl != null) {
                this.sampleRateControl.setValue(pitch * this.initialSampleRate);
            }
            log.debug("Set pitch of JavaSoundAudioSource {} to {}", this.audio, pitch);
        }

    }

}
