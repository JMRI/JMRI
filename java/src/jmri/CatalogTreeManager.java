package jmri;

import java.util.List;

/**
 * Locate a CatalogTree object representing some specific information.
 * <P>
 * CatalogTree objects are obtained from a CatalogTreeManager, which in turn is
 * generally located from the InstanceManager.
 * <P>
 * Much of the book-keeping is implemented in the AbstractCatalogTreeManager
 * class, which can form the basis for a system-specific implementation.
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
 *
 * @author	Pete Cressman Copyright (C) 2009
 *
 */
public interface CatalogTreeManager extends Manager {

    /**
     * Locate via user name, then system name if needed. If that fails, return
     * null
     *
     * @param name CatalogTree object to locate
     * @return null if no match found
     */
    public CatalogTree getCatalogTree(String name);

    /**
     * Locate an instance based on a system name. Returns null if no instance
     * already exists.
     *
     * @param systemName CatalogTree object to locate
     * @return requested CatalogTree object or null if none exists
     */
    public CatalogTree getBySystemName(String systemName);

    /**
     * Locate an instance based on a user name. Returns null if no instance
     * already exists.
     *
     * @param userName CatalogTree object to locate
     * @return requested CatalogTree object or null if none exists
     */
    public CatalogTree getByUserName(String userName);

    /**
     * Return an instance with the specified system and user names. Note that
     * two calls with the same arguments will get the same instance; there is
     * only one CatalogTree object representing a given physical CatalogTree and
     * therefore only one with a specific system or user name.
     * <P>
     * This will always return a valid object reference; a new object will be
     * created if necessary. In that case:
     * <UL>
     * <LI>If a null reference is given for user name, no user name will be
     * associated with the CatalogTree object created; a valid system name must
     * be provided
     * <LI>If both names are provided, the system name defines the hardware
     * access of the desired CatalogTree, and the user address is associated
     * with it. The system name must be valid.
     * </UL>
     * Note that it is possible to make an inconsistent request if both
     * addresses are provided, but the given values are associated with
     * different objects. This is a problem, and we don't have a good solution
     * except to issue warnings. This will mostly happen if you're creating
     * CatalogTree objects when you should be looking them up.
     *
     * @param systemName system name for new CatalogTree
     * @param userName   user name for new CatalogTree
     * @return requested CatalogTree object (never null)
     */
    public CatalogTree newCatalogTree(String systemName, String userName);

    /**
     * Get a list of all CatalogTree objects' system names.
     *
     * @return list of all CatalogTree system names
     */
    @Override
    public List<String> getSystemNameList();

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "MS_MUTABLE_ARRAY",
            justification = "with existing code structure, just have to accept these exposed arrays. Someday...")
    static final String[] IMAGE_FILTER = {"gif", "jpg", "jpeg", "png"};

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "MS_OOI_PKGPROTECT",
            justification = "with existing code structure, just have to accept these exposed arrays. Someday...")
    static final String[] SOUND_FILTER = {"wav"};

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "MS_OOI_PKGPROTECT",
            justification = "with existing code structure, just have to accept these exposed arrays. Someday...")
    static final String[] SCRIPT_FILTER = {"py", "scpt"};

}
