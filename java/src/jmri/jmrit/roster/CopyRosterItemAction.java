// CopyRosterItemAction.java
package jmri.jmrit.roster;

import java.awt.Component;
import java.io.File;
import javax.swing.Icon;
import jmri.util.FileUtil;
import jmri.util.swing.WindowInterface;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copy a roster element, including the definition file.
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
 * @author	Bob Jacobsen Copyright (C) 2001, 2002
 * @version	$Revision$
 * @see jmri.jmrit.XmlFile
 */
public class CopyRosterItemAction extends AbstractRosterItemAction {

    /**
     *
     */
    private static final long serialVersionUID = 1887063385344105392L;

    public CopyRosterItemAction(String s, WindowInterface wi) {
        super(s, wi);
    }

    public CopyRosterItemAction(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
    }

    public CopyRosterItemAction(String pName, Component pWho) {
        super(pName, pWho);
    }

    @Override
    protected boolean selectFrom() {
        return selectExistingFromEntry();
    }

    @Override
    boolean selectTo() {
        return selectNewToEntryID();
    }

    @Override
    boolean doTransfer() {

        // read the from file, change the ID, and write it out
        log.debug("doTransfer starts");

        // ensure preferences will be found
        FileUtil.createDirectory(LocoFile.getFileLocation());

        // locate the file
        //File f = new File(mFullFromFilename);
        // read it
        LocoFile lf = new LocoFile();  // used as a temporary
        Element lroot;
        try {
            lroot = lf.rootFromName(mFullFromFilename);
        } catch (Exception e) {
            log.error("Exception while loading loco XML file: " + mFullFromFilename + " exception: " + e);
            return false;
        }

        // create a new entry
        mToEntry = new RosterEntry(mFromEntry, mToID);

        // set the filename from the ID
        mToEntry.ensureFilenameExists();

        // detach the content element from it's existing file so 
        // it can be reused
        lroot.detach();

        // transfer the contents to a new file
        LocoFile newLocoFile = new LocoFile();
        File fout = new File(LocoFile.getFileLocation() + mToEntry.getFileName());
        newLocoFile.writeFile(fout, lroot, mToEntry);

        return true;
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(CopyRosterItemAction.class.getName());

    // never invoked, because we overrode actionPerformed above
    @Override
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }
}
