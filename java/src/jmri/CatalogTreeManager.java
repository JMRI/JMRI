package jmri;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Locate a CatalogTree object representing some specific information.
 * <p>
 * CatalogTree objects are obtained from a CatalogTreeManager, which in turn is
 * generally located from the InstanceManager.
 * <p>
 * Much of the book-keeping is implemented in the AbstractCatalogTreeManager
 * class, which can form the basis for a system-specific implementation.
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
 * @author Pete Cressman Copyright (C) 2009
 *
 */
public interface CatalogTreeManager extends Manager<CatalogTree> {

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
     * <p>
     * This will always return a valid object reference; a new object will be
     * created if necessary. In that case:
     * <ul>
     * <li>If a null reference is given for user name, no user name will be
     * associated with the CatalogTree object created; a valid system name must
     * be provided
     * <li>If both names are provided, the system name defines the hardware
     * access of the desired CatalogTree, and the user address is associated
     * with it. The system name must be valid.
     * </ul>
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

    public void storeImageIndex();
        
    public boolean isIndexChanged();
    
    public void indexChanged(boolean changed);

    @SuppressFBWarnings(value = "MS_MUTABLE_ARRAY",
            justification = "with existing code structure, just have to accept these exposed arrays. Someday...")
    static final String[] IMAGE_FILTER = {"gif", "jpg", "jpeg", "png"};

    @SuppressFBWarnings(value = "MS_OOI_PKGPROTECT",
            justification = "with existing code structure, just have to accept these exposed arrays. Someday...")
    static final String[] SOUND_FILTER = {"wav"};

    @SuppressFBWarnings(value = "MS_OOI_PKGPROTECT",
            justification = "with existing code structure, just have to accept these exposed arrays. Someday...")
    static final String[] SCRIPT_FILTER = {"py", "scpt"};

}
