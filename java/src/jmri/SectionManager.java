package jmri;

import javax.annotation.Nonnull;

/**
 * Basic Implementation of a SectionManager.
 * <p>
 * This doesn't have a "new" interface, since Sections are independently
 * implemented, instead of being system-specific.
 * <p>
 * Note that Section system names must begin with system prefix and type character,
 * usually IY, and be followed by a string, usually, but not always, a number. This
 * is enforced when a Section is created.
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
 * @author Dave Duchamp Copyright (C) 2008
 */
public interface SectionManager extends Manager<Section> {

    // void addListeners();

    /**
     * Create a new Section if the Section does not exist.
     *
     * @param systemName the desired system name
     * @param userName   the desired user name
     * @return a new Section or
     * @throws IllegalArgumentException if a Section with the same systemName or
     *         userName already exists, or if there is trouble creating a new
     *         Section.
     */
    @Nonnull
    Section createNewSection(@Nonnull String systemName, String userName) throws IllegalArgumentException;

    /**
     * Create a New Section with Auto System Name.
     * @param userName UserName for new Section
     * @return new Section with Auto System Name.
     * @throws IllegalArgumentException if existing Section, or
     *          unable to create a new Section.
     */
    @Nonnull
    Section createNewSection(String userName) throws IllegalArgumentException;

    /**
     * Remove an existing Section.
     *
     * @param y the section to remove
     */
    void deleteSection(Section y);

    /**
     * Get an existing Section. First look up assuming that name is a User
     * Name. If this fails look up assuming that name is a System Name.
     *
     * @param name the name to find; user names are searched for a match first,
     *             followed by system names
     * @return the found section of null if no matching Section found
     */
    Section getSection(String name);

    /**
     * Validate all Sections.
     *
     * @return number or validation errors; -2 is returned if there are no sections
     */
    int validateAllSections();

    /**
     * Check direction sensors in SSL for signals.
     *
     * @return the number or errors; 0 if no errors; -1 if the panel is null; -2 if there are no sections
     */
    int setupDirectionSensors();

    /**
     * Remove direction sensors from SSL for all signals.
     *
     * @return the number or errors; 0 if no errors; -1 if the panel is null; -2 if there are no sections
     */
    int removeDirectionSensorsFromSSL();

    /**
     * Initialize all blocking sensors that exist - set them to 'ACTIVE'.
     */
    void initializeBlockingSensors();

    /**
     * Generate Block Sections in stubs/sidings. Called after generating signal logic.
     */
    void generateBlockSections();

}
