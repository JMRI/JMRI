package jmri.jmrit.audio;

import com.jogamp.openal.AL;
import com.jogamp.openal.ALC;
import com.jogamp.openal.ALCdevice;
import com.jogamp.openal.ALConstants;
import com.jogamp.openal.ALException;
import com.jogamp.openal.ALFactory;
import com.jogamp.openal.util.ALut;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.SortedSet;
import java.util.TreeSet;
import jmri.Audio;
import jmri.AudioManager;
import jmri.InstanceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the JOAL audio system specific AudioFactory.
 * <p>
 * The JOAL sound system supports, where available, full surround-sound with 3D
 * positioning capabilities.
 * <p>
 * When only stereo capable hardware is available, it will automatically create
 * an approximation of the desired sound-scape.
 * <p>
 * This factory initialises JOAL, provides new Joal-specific Audio objects and
 * deals with clean-up operations.
 * <br><br><hr><br><b>
 * This software is based on or using the JOAL Library available from
 * <a href="http://jogamp.org/joal/www/">http://jogamp.org/joal/www/</a>
 * </b><br><br>
 * JOAL is released under the BSD license. The full license terms follow:
 * <br><i>
 * Copyright (c) 2003-2006 Sun Microsystems, Inc. All Rights Reserved.
 * <br>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * <br>
 * - Redistribution of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * <br>
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * <br>
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * <br>
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES,
 * INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN
 * MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR
 * ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR
 * DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE
 * DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY,
 * ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF
 * SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * <br>
 * You acknowledge that this software is not designed or intended for use
 * in the design, construction, operation or maintenance of any nuclear
 * facility.
 * <br><br><br></i>
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
public class JoalAudioFactory extends AbstractAudioFactory {

    private static AL al;

    private static ALC alc;

    private static ALCdevice alcDevice;

    //private static ALCcontext alcContext;
    private static boolean initialised = false;

    private JoalAudioListener activeAudioListener;

    /**
     * Definition of 8-bit quad multi-channel audio format.
     * <p>
     * These formats are only supported by certain OpenAL implementations and
     * support is determined at runtime.
     * <p>
     * Initially set format to unknown.
     */
    static int AL_FORMAT_QUAD8 = AudioBuffer.FORMAT_UNKNOWN;

    /**
     * Definition of 16-bit quad multi-channel audio format.
     * <p>
     * These formats are only supported by certain OpenAL implementations and
     * support is determined at runtime.
     * <p>
     * Initially set format to unknown.
     */
    static int AL_FORMAT_QUAD16 = AudioBuffer.FORMAT_UNKNOWN;

    /**
     * Definition of 8-bit 5.1 multi-channel audio format.
     * <p>
     * These formats are only supported by certain OpenAL implementations and
     * support is determined at runtime.
     * <p>
     * Initially set format to unknown.
     */
    static int AL_FORMAT_51CHN8 = AudioBuffer.FORMAT_UNKNOWN;

    /**
     * Definition of 16-bit 5.1 multi-channel audio format.
     * <p>
     * These formats are only supported by certain OpenAL implementations and
     * support is determined at runtime.
     * <p>
     * Initially set format to unknown.
     */
    static int AL_FORMAT_51CHN16 = AudioBuffer.FORMAT_UNKNOWN;

    /**
     * Definition of 8-bit 6.1 multi-channel audio format.
     * <p>
     * These formats are only supported by certain OpenAL implementations and
     * support is determined at runtime.
     * <p>
     * Initially set format to unknown.
     */
    static int AL_FORMAT_61CHN8 = AudioBuffer.FORMAT_UNKNOWN;

    /**
     * Definition of 16-bit 6.1 multi-channel audio format.
     * <p>
     * These formats are only supported by certain OpenAL implementations and
     * support is determined at runtime.
     * <p>
     * Initially set format to unknown.
     */
    static int AL_FORMAT_61CHN16 = AudioBuffer.FORMAT_UNKNOWN;

    /**
     * Definition of 8-bit 7.1 multi-channel audio format.
     * <p>
     * These formats are only supported by certain OpenAL implementations and
     * support is determined at runtime.
     * <p>
     * Initially set format to unknown.
     */
    static int AL_FORMAT_71CHN8 = AudioBuffer.FORMAT_UNKNOWN;

    /**
     * Definition of 16-bit 7.1 multi-channel audio format.
     * <p>
     * These formats are only supported by certain OpenAL implementations and
     * support is determined at runtime.
     * <p>
     * Initially set format to unknown.
     */
    static int AL_FORMAT_71CHN16 = AudioBuffer.FORMAT_UNKNOWN;

    /**
     * Initialise this JoalAudioFactory and check for multi-channel support.
     * <p>
     * Initial values for multi-channel formats are set to unknown as OpenAL
     * implementations are only guaranteed to support MONO and STEREO Buffers.
     * <p>
     * On initialisation, we need to check if this implementation supports
     * multi-channel formats.
     * <p>
     * This is done by making alGetEnumValue calls to request the value of the
     * Buffer Format Tag Enum (that will be passed to an alBufferData call).
     * Enum Values are retrieved by string names. The following names are
     * defined for multi-channel wave formats ...
     * <ul>
     * <li>"AL_FORMAT_QUAD8"   : 4 Channel, 8 bit data
     * <li>"AL_FORMAT_QUAD16"  : 4 Channel, 16 bit data
     * <li>"AL_FORMAT_51CHN8"  : 5.1 Channel, 8 bit data
     * <li>"AL_FORMAT_51CHN16" : 5.1 Channel, 16 bit data
     * <li>"AL_FORMAT_61CHN8"  : 6.1 Channel, 8 bit data
     * <li>"AL_FORMAT_61CHN16" : 6.1 Channel, 16 bit data
     * <li>"AL_FORMAT_71CHN8"  : 7.1 Channel, 8 bit data
     * <li>"AL_FORMAT_71CHN16" : 7.1 Channel, 16 bit data
     * </ul>
     *
     * @return true, if initialisation successful
     */
    @Override
    @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
            justification = "OK to write to static variables as we only do so if not initialised")
    public boolean init() {
        if (initialised) {
            return true;
        }

        // Initialise OpenAL and clear the error bit
        try {
//            // Open default 'preferred' device
//            alcDevice = alc.alcOpenDevice(null);
//            alcContext = alc.alcCreateContext(alcDevice, null);
//            alc.alcMakeContextCurrent(alcContext);
//
            ALut.alutInit();
            al = ALFactory.getAL();
            al.alGetError();
            if (log.isInfoEnabled()) {
                log.info("Initialised JOAL using OpenAL:"
                        + " vendor - " + al.alGetString(AL.AL_VENDOR)
                        + " version - " + al.alGetString(AL.AL_VERSION));
            }
        } catch (ALException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error initialising JOAL: " + e);
            }
            return false;
        } catch (RuntimeException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error initialising OpenAL: " + e);
            }
            return false;
        }

        // Check for multi-channel support
        int checkMultiChannel;

        checkMultiChannel = al.alGetEnumValue("AL_FORMAT_QUAD8");
        checkALError();
        if (checkMultiChannel != ALConstants.AL_FALSE) {
            AL_FORMAT_QUAD8 = checkMultiChannel;
        }
        checkMultiChannel = al.alGetEnumValue("AL_FORMAT_QUAD16");
        checkALError();
        if (checkMultiChannel != ALConstants.AL_FALSE) {
            AL_FORMAT_QUAD16 = checkMultiChannel;
        }
        checkMultiChannel = al.alGetEnumValue("AL_FORMAT_51CHN8");
        checkALError();
        if (checkMultiChannel != ALConstants.AL_FALSE) {
            AL_FORMAT_51CHN8 = checkMultiChannel;
        }
        checkMultiChannel = al.alGetEnumValue("AL_FORMAT_51CHN16");
        checkALError();
        if (checkMultiChannel != ALConstants.AL_FALSE) {
            AL_FORMAT_51CHN16 = checkMultiChannel;
        }
        checkMultiChannel = al.alGetEnumValue("AL_FORMAT_61CHN8");
        checkALError();
        if (checkMultiChannel != ALConstants.AL_FALSE) {
            AL_FORMAT_61CHN8 = checkMultiChannel;
        }
        checkMultiChannel = al.alGetEnumValue("AL_FORMAT_61CHN16");
        checkALError();
        if (checkMultiChannel != ALConstants.AL_FALSE) {
            AL_FORMAT_61CHN16 = checkMultiChannel;
        }
        checkMultiChannel = al.alGetEnumValue("AL_FORMAT_71CHN8");
        checkALError();
        if (checkMultiChannel != ALConstants.AL_FALSE) {
            AL_FORMAT_71CHN8 = checkMultiChannel;
        }
        checkMultiChannel = al.alGetEnumValue("AL_FORMAT_71CHN16");
        checkALError();
        if (checkMultiChannel != ALConstants.AL_FALSE) {
            AL_FORMAT_71CHN16 = checkMultiChannel;
        }
        log.debug("8-bit quadrophonic supported? "
                + (AL_FORMAT_QUAD8 == AudioBuffer.FORMAT_UNKNOWN ? "No" : "Yes"));
        log.debug("16-bit quadrophonic supported? "
                + (AL_FORMAT_QUAD16 == AudioBuffer.FORMAT_UNKNOWN ? "No" : "Yes"));
        log.debug("8-bit 5.1 surround supported? "
                + (AL_FORMAT_51CHN8 == AudioBuffer.FORMAT_UNKNOWN ? "No" : "Yes"));
        log.debug("16-bit 5.1 surround supported? "
                + (AL_FORMAT_51CHN16 == AudioBuffer.FORMAT_UNKNOWN ? "No" : "Yes"));
        log.debug("8-bit 6.1 surround supported? "
                + (AL_FORMAT_61CHN8 == AudioBuffer.FORMAT_UNKNOWN ? "No" : "Yes"));
        log.debug("16-bit 6.1 surround supported? "
                + (AL_FORMAT_61CHN16 == AudioBuffer.FORMAT_UNKNOWN ? "No" : "Yes"));
        log.debug("8 bit 7.1 surround supported? "
                + (AL_FORMAT_71CHN8 == AudioBuffer.FORMAT_UNKNOWN ? "No" : "Yes"));
        log.debug("16 bit 7.1 surround supported? "
                + (AL_FORMAT_71CHN16 == AudioBuffer.FORMAT_UNKNOWN ? "No" : "Yes"));

        // Check context
        alc = ALFactory.getALC();
        alcDevice = alc.alcGetContextsDevice(alc.alcGetCurrentContext());
        if (!checkALCError(alcDevice)) {
            int[] size = new int[1];
            alc.alcGetIntegerv(alcDevice, ALC.ALC_ATTRIBUTES_SIZE, size.length, size, 0);
            log.debug("Size of ALC_ATTRIBUTES: " + size[0]);
            if (!checkALCError(alcDevice) && size[0] > 0) {
                int[] attributes = new int[size[0]];
                alc.alcGetIntegerv(alcDevice, ALC.ALC_ALL_ATTRIBUTES, attributes.length, attributes, 0);
                for (int i = 0; i < attributes.length; i++) {
                    if (i % 2 != 0) {
                        continue;
                    }
                    switch (attributes[i]) {
                        case ALC.ALC_INVALID:
                            log.debug("Invalid");
                            break;
                        case ALC.ALC_MONO_SOURCES:
                            log.debug("Number of mono sources: " + attributes[i + 1]);
                            break;
                        case ALC.ALC_STEREO_SOURCES:
                            log.debug("Number of stereo sources: " + attributes[i + 1]);
                            break;
                        case ALC.ALC_FREQUENCY:
                            log.debug("Frequency: " + attributes[i + 1]);
                            break;
                        case ALC.ALC_REFRESH:
                            log.debug("Refresh: " + attributes[i + 1]);
                            break;
                        default:
                            log.debug("Attribute " + i + ": " + attributes[i]);
                    }
                }
            }
        }

        super.init();
        initialised = true;
        return true;
    }

    @Override
    public String toString() {
        try {
            return "JoalAudioFactory, using OpenAL:"
                    + " vendor - " + al.alGetString(AL.AL_VENDOR)
                    + " version - " + al.alGetString(AL.AL_VERSION);
        } catch (NullPointerException e) {
            log.error("NPE from JoalAudioFactory: {}",e);
            return "JoalAudioFactory, using Null";
        }
    }

    @Override
    @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
            justification = "OK to write to static variables to record static library status")
    public void cleanup() {
        // Stop the command thread
        super.cleanup();

        // Get the active AudioManager
        AudioManager am = InstanceManager.getDefault(jmri.AudioManager.class);

        // Retrieve list of AudioSource objects and remove the sources
        SortedSet<Audio> sources = new TreeSet<>(am.getNamedBeanSet(Audio.SOURCE));
        for (Audio source: sources) {
            if (log.isDebugEnabled()) {
                log.debug("Removing JoalAudioSource: {}", source.getSystemName());
            }
            // Cast to JoalAudioSource and cleanup
            ((JoalAudioSource) source).cleanup();
        }

        // Now, retrieve list of AudioBuffer objects and remove the buffers
        SortedSet<Audio> buffers = new TreeSet<>(am.getNamedBeanSet(Audio.BUFFER));
        for (Audio buffer : buffers) {
            if (log.isDebugEnabled()) {
                log.debug("Removing JoalAudioBuffer: {}", buffer.getSystemName());
            }
            // Cast to JoalAudioBuffer and cleanup
            ((JoalAudioBuffer) buffer).cleanup();
        }

        // Lastly, retrieve list of AudioListener objects and remove listener.
        SortedSet<Audio> listeners = new TreeSet<>(am.getNamedBeanSet(Audio.LISTENER));
        for (Audio listener : listeners) {
            if (log.isDebugEnabled()) {
                log.debug("Removing JoalAudioListener: {}", listener.getSystemName());
            }
            // Cast to JoalAudioListener and cleanup
            ((JoalAudioListener) listener).cleanup();
        }

        // Finally, shutdown OpenAL and close the output device
        log.debug("Shutting down OpenAL, initialised: {}", initialised);
        if (initialised) ALut.alutExit();
        initialised = false;
    }

    @Override
    public boolean isInitialised() {
        return initialised;
    }

    @Override
    public AudioBuffer createNewBuffer(String systemName, String userName) {
        return new JoalAudioBuffer(systemName, userName);
    }

    @Override
    public AudioListener createNewListener(String systemName, String userName) {
        activeAudioListener = new JoalAudioListener(systemName, userName);
        return activeAudioListener;
    }

    @Override
    public AudioListener getActiveAudioListener() {
        return activeAudioListener;
    }

    @Override
    public AudioSource createNewSource(String systemName, String userName) {
        return new JoalAudioSource(systemName, userName);
    }

    @Override
    public void setDistanceAttenuated(boolean attenuated) {
        super.setDistanceAttenuated(attenuated);
        if (isDistanceAttenuated()) {
            al.alDistanceModel(ALConstants.AL_INVERSE_DISTANCE_CLAMPED);
        } else {
            al.alDistanceModel(ALConstants.AL_NONE);
        }
    }

    /**
     * Return a reference to the active AL object for use by other Joal objects
     *
     * @return active AL object
     */
    public static synchronized AL getAL() {
        return al;
    }

    /**
     * Method to check if any error has occurred in the OpenAL sub-system.
     * <p>
     * If an error has occurred, log the error as a warning message and return
     * True.
     * <p>
     * If no error has occurred, return False.
     *
     * @return True if an error has occurred
     */
    public static boolean checkALError() {
        return checkALError("");
    }

    /**
     * Method to check if any error has occurred in the OpenAL sub-system.
     * <p>
     * If an error has occurred, log the error as a warning message with the
     * defined message pre-pended and return True.
     * <p>
     * If no error has occurred, return False.
     *
     * @param msg additional message prepended to the log
     * @return True if an error has occurred
     */
    public static boolean checkALError(String msg) {
        // Trim any whitespace then append a space if required
        msg = msg.trim();
        if (msg.length() > 0) {
            msg = msg + " ";
        }

        // Check for error
        switch (al.alGetError()) {
            case AL.AL_NO_ERROR:
                return false;
            case AL.AL_INVALID_NAME:
                log.warn(msg + "Invalid name parameter");
                return true;
            case AL.AL_INVALID_ENUM:
                log.warn(msg + "Invalid enumerated parameter value");
                return true;
            case AL.AL_INVALID_VALUE:
                log.warn(msg + "Invalid parameter value");
                return true;
            case AL.AL_INVALID_OPERATION:
                log.warn(msg + "Requested operation is not valid");
                return true;
            case AL.AL_OUT_OF_MEMORY:
                log.warn(msg + "Out of memory");
                return true;
            default:
                log.warn(msg + "Unrecognised error occurred");
                return true;
        }
    }

    /**
     * Method to check if any error has occurred in the OpenAL sub-system.
     * <p>
     * If an error has occurred, log the error as a warning message and return
     * True.
     * <p>
     * If no error has occurred, return False.
     *
     * @param alcDevice OpenAL context device to check
     * @return True if an error has occurred
     */
    public static boolean checkALCError(ALCdevice alcDevice) {
        return checkALCError(alcDevice, "");
    }

    /**
     * Method to check if any error has occurred in the OpenAL context
     * sub-system.
     * <p>
     * If an error has occurred, log the error as a warning message with the
     * defined message pre-pended and return True.
     * <p>
     * If no error has occurred, return False.
     *
     * @param alcDevice OpenAL context device to check
     * @param msg       additional message prepended to the log
     * @return True if an error has occurred
     */
    public static boolean checkALCError(ALCdevice alcDevice, String msg) {
        // Trim any whitespace then append a space if required
        msg = msg.trim();
        if (msg.length() > 0) {
            msg = msg + " ";
        }

        // Check for error
        switch (alc.alcGetError(alcDevice)) {
            case ALC.ALC_NO_ERROR:
                return false;
            case ALC.ALC_INVALID_DEVICE:
                log.warn(msg + "Invalid device");
                return true;
            case ALC.ALC_INVALID_CONTEXT:
                log.warn(msg + "Invalid context");
                return true;
            case ALC.ALC_INVALID_ENUM:
                log.warn(msg + "Invalid enumerated parameter value");
                return true;
            case ALC.ALC_INVALID_VALUE:
                log.warn(msg + "Invalid parameter value");
                return true;
            case ALC.ALC_OUT_OF_MEMORY:
                log.warn(msg + "Out of memory");
                return true;
            default:
                log.warn(msg + "Unrecognised error occurred");
                return true;
        }
    }

    private static final Logger log = LoggerFactory.getLogger(JoalAudioFactory.class);

}
