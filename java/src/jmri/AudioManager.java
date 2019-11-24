package jmri;

import java.util.List;
import java.util.SortedSet;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import jmri.jmrit.audio.AudioFactory;

/**
 * Locate an Audio object representing some specific audio information.
 * <p>
 * Audio objects are obtained from an AudioManager, which in turn is generally
 * located from the InstanceManager. A typical call sequence might be:
 * <pre>
 * Audio audio = InstanceManager.getDefault(jmri.AudioManager.class).provideAudio("myAudio");
 * </pre>
 * <p>
 * Each Audio has two names. The "user" name is entirely free form, and can be
 * used for any purpose. The "system" name is provided by the system-specific
 * implementations, if any, and provides a unique mapping to the layout control
 * system (for example LocoNet or NCE) and address within that system. Note that
 * most (all?) layout systems don't have anything corresponding to this, in
 * which case the "Internal" Audio objects are still available with names like
 * IAS23.
 * <p>
 * Much of the book-keeping is implemented in the AbstractAudioManager class,
 * which can form the basis for a system-specific implementation.
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
 * @author Matthew Harris Copyright (c) 2009
 */
public interface AudioManager extends Manager<Audio> {

    /**
     * Define the maximum number of AudioListener objects that can be created
     */
    public static final int MAX_LISTENERS = 1;

    /**
     * Define the maximum number of AudioSource objects that can be created
     */
    public static final int MAX_SOURCES = 255;

    /**
     * Define the maximum number of AudioBuffer objects that can be created
     */
    public static final int MAX_BUFFERS = 255;

    /**
     * Get the Audio with the user name, then system name if needed; if that fails, create a
     * new Audio. 
     * If the name is a valid system name, it will be used for the
     * new Audio. Otherwise, the makeSystemName method will attempt to turn it
     * into a valid system name.
     *
     * @param name User name or system name to match, or which can be promoted
     *             to system name
     * @return Never null under normal circumstances
     * @throws AudioException if error occurs during creation
     */
    @Nonnull
    public Audio provideAudio(@Nonnull String name) throws AudioException;

    /**
     * Get an existing Audio or return null if it doesn't exists. 
     * 
     * Locates via user name, then system name if needed.
     *
     * @param name User name or system name to match
     * @return null if no match found
     */
    @CheckForNull
    public Audio getAudio(@Nonnull String name);

    /**
     * Get the Audio with the given system name or return null if no instance
     * already exists.
     *
     * @param systemName Audio object system name (such as IAS1 or IAB4)
     * @return requested Audio object or null if none exists
     */
    @CheckForNull
    public Audio getBySystemName(@Nonnull String systemName);

    /**
     * Get the Audio with the given user name or return null if no instance
     * already exists.
     *
     * @param userName Audio object user name
     * @return requested Audio object or null if none exists
     */
    @CheckForNull
    public Audio getByUserName(@Nonnull String userName);

    /**
     * Return an Audio with the specified system and user names. 
     * Note that
     * two calls with the same arguments will get the same instance; there is
     * only one Audio object representing a given physical Audio and therefore
     * only one with a specific system or user name.
     * <p>
     * This will always return a valid object reference; a new object will be
     * created if necessary. In that case:
     * <ul>
     * <li>If a null reference is given for user name, no user name will be
     * associated with the Audio object created; a valid system name must be
     * provided
     * <li>If both names are provided, the system name defines the hardware
     * access of the desired Audio, and the user address is associated with it.
     * The system name must be valid.
     * </ul>
     * Note that it is possible to make an inconsistent request if both
     * addresses are provided, but the given values are associated with
     * different objects. This is a problem, and we don't have a good solution
     * except to issue warnings. This will mostly happen if you're creating
     * Audio objects when you should be looking them up.
     *
     * @param systemName Audio object system name (such as IAS1 or IAB4)
     * @param userName   Audio object user name
     * @return requested Audio object (never null)
     * @throws AudioException if error occurs during creation
     */
    @Nonnull
    public Audio newAudio(@Nonnull String systemName, String userName) throws AudioException;

    /**
     * Returns the currently active AudioFactory object.
     * <p>
     * An Audio factory is responsible for the creation of implementation
     * specific audio objects.
     *
     * @return current active AudioFactory object
     */
    @CheckForNull
    public AudioFactory getActiveAudioFactory();

    /**
     * Get a list of specified Audio sub-type objects' system names.
     *
     * @param subType sub-type to retrieve
     * @return List of specified Audio sub-type objects' system names.
     * @deprecated 4.17.6 use direct access via {@link #getNamedBeanSet(char)}
     */
    @Nonnull
    @Deprecated
    public List<String> getSystemNameList(char subType);

    /**
     * Get the specified Audio sub-type NamedBeans.
     *
     * @param subType sub-type to retrieve
     * @return Unmodifiable access to a SortedSet of NamedBeans for the specified Audio sub-type .
     * 
     * @since 4.17.6
     */
    @Nonnull
    public SortedSet<Audio> getNamedBeanSet(char subType);

    /**
     * Perform any initialisation operations
     */
    public void init();

    /**
     * Perform any clean-up operations
     */
    public void cleanup();

    /**
     * Determine if this AudioManager is initialised
     * @return true if initialised
     */
    public boolean isInitialised();

}
