// ImportRosterItemAction.java

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
 * Import a locomotive XML file as a new RosterEntry.
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
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 * <P>
 * @author	Bob Jacobsen   Copyright (C) 2001, 2002
 * @version	$Revision$
 * @see         jmri.jmrit.roster.AbstractRosterItemAction
 * @see         jmri.jmrit.XmlFile
 */
public class ImportRosterItemAction extends AbstractRosterItemAction  {

    public ImportRosterItemAction(String s, WindowInterface wi) {
    	super(s, wi);
    }
     
 	public ImportRosterItemAction(String s, Icon i, WindowInterface wi) {
    	super(s, i, wi);
    }
    
    public ImportRosterItemAction(String pName, Component pWho) {
        super(pName, pWho);
    }

    protected boolean selectFrom() {
        return selectNewFromFile();
    }

    boolean selectTo() {
        return selectNewToEntryID();
    }

    boolean doTransfer() {

        // read the file for the "from" entry, create a new entry, write it out

        // ensure preferences will be found for read
        FileUtil.createDirectory(LocoFile.getFileLocation());

        // locate the file
        //File f = new File(mFullFromFilename);

        // read it
        LocoFile lf = new LocoFile();  // used as a temporary
        Element lroot = null;
        try {
            lroot = (Element)lf.rootFromFile(mFromFile).clone();
        } catch (Exception e) {
            log.error("Exception while loading loco XML file: "+mFullFromFilename+" exception: "+e);
            return false;
        }

        // create a new entry from XML info - find the element
        Element loco = lroot.getChild("locomotive");
        mToEntry = new RosterEntry(loco);

        // set the filename from the ID
        mToEntry.setId(mToID);
        mToEntry.setFileName(""); // to force recreation
        mToEntry.ensureFilenameExists();

        // transfer the contents to a new file
        LocoFile newLocoFile = new LocoFile();
        File fout = new File(LocoFile.getFileLocation()+mToEntry.getFileName());
        newLocoFile.writeFile(fout, lroot, mToEntry);
        
        String[] attributes = mToEntry.getAttributeList();
        if (attributes!=null){
            Roster roster = Roster.instance();
            for(int x=0; x<attributes.length; x++){
                if(attributes[x].startsWith(roster.getRosterGroupPrefix())){
                    //We don't bother checking to see if the group already exists as this is done by the addRosterGroupList.
                    roster.addRosterGroupList(attributes[x].substring(roster.getRosterGroupPrefix().length()));
                }
            }
        }

        return true;
    }
    
    // never invoked, because we overrode actionPerformed above
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }

    // initialize logging
    static Logger log = Logger.getLogger(ImportRosterItemAction.class.getName());

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
        Action a = new ImportRosterItemAction("Import Roster Item", new javax.swing.JFrame());
        a.actionPerformed(new ActionEvent(a, 0, "dummy"));
    }
}
