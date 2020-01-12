package jmri;

import javax.swing.tree.TreeModel;
import jmri.jmrit.catalog.CatalogTreeNode;

/**
 * Represents a CatalogTree, a tree displaying a taxonomy - e.g. a file system
 * directory, or an index of references or a table of contents built according
 * to the user's taxonomy.
 * <p>
 * Specific implementations are in the jmri.jmrit.catalog package.
 * <p>
 * The states and names are Java Bean parameters, so that listeners can be
 * registered to be notified of any changes.
 * <p>
 * Each CatalogTree object has a two names. The "user" name is entirely free
 * form, and can be used for any purpose. The "system" name is provided by the
 * purpose-specific implementations.
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
 * @author Pete Cressman Copyright (C) 2009
 */
public interface CatalogTree extends NamedBean, TreeModel {

    public static final char IMAGE = 'I';    // letter to filter for images/icons
    public static final char SOUND = 'S';    // letter to filter for sounds 
    public static final char SCRIPT = 'T';    // letter to filter for scripts
    public static final char NOFILTER = 'N';    // letter for unfiltered
    public static final char FILESYS = 'F';    // typeLetter for tree based on file system
    public static final char XML = 'X';    // typeLetter for index tree stored in XML file

    /**
     * Recursively add a representation of the resources below a particular
     * resource
     *
     * @param pName   Name of the resource to be scanned; this is only used for
     *                the human-readable tree
     * @param pPath   Path to this resource, including the pName part
     * @param pParent Node for the parent of the resource to be scanned, e.g.
     *                where in the tree to insert it.
     */
    public void insertNodes(String pName, String pPath, CatalogTreeNode pParent);

    /**
     * Starting point to recursively add nodes to the tree by scanning a file
     * directory
     *
     * @param pathToRoot Path to Directory to be scanned
     */
    public void insertNodes(String pathToRoot);

    /**
     * Get the root element of the tree as a jmri.CatalogTreeNode object.
     * (Instead of Object, as parent swing.TreeModel provides)
     */
    @Override
    public CatalogTreeNode getRoot();
}
