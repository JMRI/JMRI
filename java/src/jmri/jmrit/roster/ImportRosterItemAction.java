// ImportRosterItemAction.java

package jmri.jmrit.roster;

import java.awt.Component;
import java.io.File;
import javax.swing.Icon;
import jmri.util.FileUtil;
import jmri.util.swing.WindowInterface;
import org.jdom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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

    @Override
    protected boolean selectFrom() {
        return selectNewFromFile();
    }

    @Override
    boolean selectTo() {
        return selectNewToEntryID();
    }

    @Override
    boolean doTransfer() {

        // read the file for the "from" entry, create a new entry, write it out

        // ensure preferences will be found for read
        FileUtil.createDirectory(LocoFile.getFileLocation());

        // locate the file
        //File f = new File(mFullFromFilename);

        // read it
        LocoFile lf = new LocoFile();  // used as a temporary
        Element lroot;
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
    @Override
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }

    // initialize logging
    static Logger log = LoggerFactory.getLogger(ImportRosterItemAction.class.getName());
}
