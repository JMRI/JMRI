// CopyRosterItemAction.java

package jmri.jmrit.roster;

import jmri.jmrit.XmlFile;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import java.io.*;
import javax.swing.*;
import java.awt.Component;
import org.jdom.*;
import org.jdom.input.*;
import com.sun.java.util.collections.List;

/**
 * Copy a roster element, including the definition file.
 *
 * @author			Bob Jacobsen   Copyright (C) 2001, 2002
 * @version			$Revision: 1.3 $
 * @see             jmri.jmrit.XmlFile
 */
public class CopyRosterItemAction extends AbstractRosterItemAction {

	public CopyRosterItemAction(String pName, Component pWho) {
		super(pName, pWho);
	}

    boolean selectFrom() {
        return selectExistingFromEntry();
    }

    boolean selectTo() {
        return selectNewToEntryID();
    }

    boolean doTransfer() {

        // read the from file, change the ID, and write it out

        // ensure preferences will be found
        XmlFile.ensurePrefsPresent(XmlFile.prefsDir());
        XmlFile.ensurePrefsPresent(XmlFile.prefsDir()+LocoFile.fileLocation);

        // locate the file
        File f = new File(mFullFromFilename);

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

        // transfer the contents to a new file
        LocoFile newLocoFile = new LocoFile();
        File fout = new File(XmlFile.prefsDir()+LocoFile.fileLocation+mToEntry.getFileName());
        newLocoFile.writeFile(fout, lroot, mToEntry);

        return true;
	}

    void updateRoster() {
        addToEntryToRoster();
    }

	// initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(CopyRosterItemAction.class.getName());

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
        Action a = new CopyRosterItemAction("Copy Roster Item", null);
        a.actionPerformed(new ActionEvent(a, 0, "dummy"));
    }
}
