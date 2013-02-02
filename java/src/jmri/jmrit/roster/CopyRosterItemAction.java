// CopyRosterItemAction.java

package jmri.jmrit.roster;

import org.apache.log4j.Logger;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import jmri.util.swing.WindowInterface;
import javax.swing.Icon;

import javax.swing.Action;
import jmri.util.FileUtil;

import org.jdom.Element;

/**
 * Copy a roster element, including the definition file.
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
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 * <P>
 * @author	Bob Jacobsen   Copyright (C) 2001, 2002
 * @version	$Revision$
 * @see         jmri.jmrit.XmlFile
 */
public class CopyRosterItemAction extends AbstractRosterItemAction {

    public CopyRosterItemAction(String s, WindowInterface wi) {
    	super(s, wi);
    }
     
 	public CopyRosterItemAction(String s, Icon i, WindowInterface wi) {
    	super(s, i, wi);
    }

    public CopyRosterItemAction(String pName, Component pWho) {
        super(pName, pWho);
    }

    protected boolean selectFrom() {
        return selectExistingFromEntry();
    }

    boolean selectTo() {
        return selectNewToEntryID();
    }

    boolean doTransfer() {

        // read the from file, change the ID, and write it out
        log.debug("doTransfer starts");

        // ensure preferences will be found
        FileUtil.createDirectory(LocoFile.getFileLocation());

        // locate the file
        //File f = new File(mFullFromFilename);

        // read it
        LocoFile lf = new LocoFile();  // used as a temporary
        Element lroot = null;
        try {
            lroot = lf.rootFromName(mFullFromFilename);
        } catch (Exception e) {
            log.error("Exception while loading loco XML file: "+mFullFromFilename+" exception: "+e);
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
        File fout = new File(LocoFile.getFileLocation()+mToEntry.getFileName());
        newLocoFile.writeFile(fout, lroot, mToEntry);

        return true;
    }

    // initialize logging
    static Logger log = Logger.getLogger(CopyRosterItemAction.class.getName());

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
        Action a = new CopyRosterItemAction("Copy Roster Item", new javax.swing.JFrame());
        a.actionPerformed(new ActionEvent(a, 0, "dummy"));
    }
    
    // never invoked, because we overrode actionPerformed above
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }
}
