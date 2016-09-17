package jmri;

import java.util.List;
import jmri.jmrit.audio.AudioFactory;

import javax.annotation.CheckReturnValue;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Locate an Audio object representing some specific audio information.
 * <P>
 * Audo objects are obtained from an AudioManager, which in turn is generally
 * located from the InstanceManager. A typical call sequence might be:
 * <PRE>
 * Audio audio = InstanceManager.getDefault(jmri.AudioManager.class).provideAudio("myAudio");
 * </PRE>
 * <P>
 * Each Audio has two names. The "user" name is entirely free form, and can be
 * used for any purpose. The "system" name is provided by the system-specific
 * implementations, if any, and provides a unique mapping to the layout control
 * system (e.g. LocoNet, NCE, etc) and address within that system. Note that
 * most (all?) layout systems don't have anything corresponding to this, in
 * which case the "Internal" Audio objects are still available with names like
 * IAS23.
 * <P>
 * Much of the book-keeping is implemented in the AbstractAudioManager class,
 * which can form the basis for a system-specific implementation.
 *
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <P>
 * @author Matthew Harris Copyright (c) 2009
 */
public interface AudioManager extends Manager {

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
     * Locate via user name, then system name if needed. If that fails, create a
     * new Audio. If the name is a valid system name, it will be used for the
     * new Audio. Otherwise, the makeSystemName method will attempt to turn it
     * into a valid system name.
     *
     * @param name User name or system name to match, or which can be promoted
     *             to system name
     * @return Never null under normal circumstances
     * @throws AudioException if error occurs during creation
     */
    public @Nonnull Audio provideAudio(@Nonnull String name) throws AudioException;

    /**
     * Locate via user name, then system name if needed. If that fails, return
     * null
     *
     * @param name User name or system name to match
     * @return null if no match found
     */
    public @CheckForNull Audio getAudio(@Nonnull String name);

    /**
     * Locate an instance based on a system name. Returns null if no instance
     * already exists.
     *
     * @param systemName Audio object system name (e.g. IAS1, IAB4)
     * @return requested Audio object or null if none exists
     */
    public @CheckForNull Audio getBySystemName(@Nonnull String systemName);

    /**
     * Locate an instance based on a user name. Returns null if no instance
     * already exists.
     *
     * @param userName Audio object user name
     * @return requested Audio object or null if none exists
     */
    public @CheckForNull Audio getByUserName(@Nonnull String userName);

    /**
     * Return an instance with the specified system and user names. Note that
     * two calls with the same arguments will get the same instance; there is
     * only one Audio object representing a given physical Audio and therefore
     * only one with a specific system or user name.
     * <P>
     * This will always return a valid object reference; a new object will be
     * created if necessary. In that case:
     * <UL>
     * <LI>If a null reference is given for user name, no user name will be
     * associated with the Audio object created; a valid system name must be
     * provided
     * <LI>If both names are provided, the system name defines the hardware
     * access of the desired Audio, and the user address is associated with it.
     * The system name must be valid.
     * </UL>
     * Note that it is possible to make an inconsistent request if both
     * addresses are provided, but the given values are associated with
     * different objects. This is a problem, and we don't have a good solution
     * except to issue warnings. This will mostly happen if you're creating
     * Audio objects when you should be looking them up.
     *
     * @param systemName Audio object system name (e.g. IAS1, IAB4)
     * @param userName   Audio object user name
     * @return requested Audio object (never null)
     * @throws AudioException if error occurs during creation
     */
    public @Nonnull Audio newAudio(@Nonnull String systemName, String userName) throws AudioException;

    /**
     * Returns the currently active AudioFactory object.
     * <p>
     * An Audio factory is responsible for the creation of implementation
     * specific audio objects.
     *
     * @return current active AudioFactory object
     */
    public @CheckForNull AudioFactory getActiveAudioFactory();

    /**
     * Get a list of all Audio objects' system names.
     *
     * @return List of all Audio objects' system names
     */
    @Override
    public @Nonnull List<String> getSystemNameList();

    /**
     * Get a list of specified Audio sub-type objects' system names.
     *
     * @param subType sub-type to retrieve
     * @return List of specified Audio sub-type objects' system names.
     */
    public @Nonnull List<String> getSystemNameList(char subType);

    /**
     * Perform any initialisation operations
     */
    public void init();

    /**
     * Perform any clean-up operations
     */
    public void cleanUp();

}
