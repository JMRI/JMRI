// ExportRosterItemAction.java

package jmri.jmrit.roster;

import org.apache.log4j.Logger;
import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.Action;
import jmri.util.FileUtil;

import org.jdom.Element;

/**
 * Export a roster element as a new definition file.
 * <P>
 * This creates the new file containing the entry,
 * but does <b>not</b> add it to the local
 * {@link Roster} of locomotives.  This is intended for
 * making a transportable copy of entry, which can be
 * imported via {@link ImportRosterItemAction} on another system.
 *
 *
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Gene  ral Public License 
 * for more details.
 * <P>
 * @author	Bob Jacobsen   Copyright (C) 2001, 2002
 * @version	$Revision$
 * @see         jmri.jmrit.roster.ImportRosterItemAction
 * @see         jmri.jmrit.XmlFile
 */
public class ExportRosterItemAction extends AbstractRosterItemAction  {

    public ExportRosterItemAction(String pName, Component pWho) {
        super(pName, pWho);
    }
    
    protected boolean selectFrom() {
        return selectExistingFromEntry();
    }

    boolean selectTo() {
        return selectNewToFile();
    }

    boolean doTransfer() {

        // read the file for the "from" entry and write it out

        // ensure preferences will be found for read
        FileUtil.createDirectory(LocoFile.getFileLocation());

        // locate the file
        //File f = new File(mFullFromFilename);

        // read it
        LocoFile lf = new LocoFile();  // used as a temporary
        Element lroot = null;
        try {
            lroot = (Element)lf.rootFromName(mFullFromFilename).clone();
        } catch (Exception e) {
            log.error("Exception while loading loco XML file: "+mFullFromFilename+" exception: "+e);
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

    void updateRoster() {
        // exported entry is NOT added to Roster
    }

    // initialize logging
    static Logger log = Logger.getLogger(ExportRosterItemAction.class.getName());

    /**
     * Main entry point to run as standalone tool. This doesn't work
     * so well yet:  It should take an optional command line argument,
     * and should terminate when done, or at least let you delete
     * another file.
     */
    public static void main(String s[]) {

    	// initialize log4j - from logging control file (lcf) only
    	// if can find it!
    	String logFile = "default.lcf";
    	try {
            if (new java.io.File(logFile).canRead()) {
                org.apache.log4j.PropertyConfigurator.configure("default.lcf");
            } else {
                org.apache.log4j.BasicConfigurator.configure();
            }
        }
        catch (java.lang.NoSuchMethodError e) { System.out.println("Exception starting logging: "+e); }

        // log.info("CopyRosterItemAction starts");

        // fire the action
        Action a = new ExportRosterItemAction("Export Roster Item", new javax.swing.JFrame());
        a.actionPerformed(new ActionEvent(a, 0, "dummy"));
    }
}
