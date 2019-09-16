package jmri.jmrit.roster;

import java.awt.Component;
import jmri.util.FileUtil;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Export a roster element as a new definition file.
 * <p>
 * This creates the new file containing the entry, but does <b>not</b> add it to
 * the local {@link Roster} of locomotives. This is intended for making a
 * transportable copy of entry, which can be imported via
 * {@link ImportRosterItemAction} on another system.
 *
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
 * A PARTICULAR PURPOSE. See the GNU Gene ral Public License for more details.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002
 * @see jmri.jmrit.roster.ImportRosterItemAction
 * @see jmri.jmrit.XmlFile
 */
public class ExportRosterItemAction extends AbstractRosterItemAction {

    public ExportRosterItemAction(String pName, Component pWho) {
        super(pName, pWho);
    }

    @Override
    protected boolean selectFrom() {
        return selectExistingFromEntry();
    }

    @Override
    boolean selectTo() {
        return selectNewToFile();
    }

    @Override
    boolean doTransfer() {

        // read the file for the "from" entry and write it out
        // ensure preferences will be found for read
        FileUtil.createDirectory(Roster.getDefault().getRosterFilesLocation());

        // locate the file
        //File f = new File(mFullFromFilename);
        // read it
        LocoFile lf = new LocoFile();  // used as a temporary
        Element lroot;
        try {
            lroot = lf.rootFromName(mFullFromFilename).clone();
        } catch (Exception e) {
            log.error("Exception while loading loco XML file: " + mFullFromFilename + " exception: " + e);
            return false;
        }

        // create a new entry
        mToEntry = new RosterEntry(mFromEntry, mFromID);

        // transfer the contents to the new file
        LocoFile newLocoFile = new LocoFile();
        // File fout = new File(mFullToFilename);
        mToEntry.setFileName(mToFilename);
        mToEntry.setId(mFromEntry.getId());
        newLocoFile.writeFile(mToFile, lroot, mToEntry);

        return true;
    }

    @Override
    void updateRoster() {
        // exported entry is NOT added to Roster
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(ExportRosterItemAction.class);

}
