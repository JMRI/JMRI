// ExportRosterItemAction.java

package jmri.jmrit.roster;

import jmri.jmrit.XmlFile;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.Action;

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
 * @author	Bob Jacobsen   Copyright (C) 2001, 2002
 * @version	$Revision: 1.4 $
 * @see         jmri.jmrit.roster.ImportRosterItemAction
 * @see         jmri.jmrit.XmlFile
 */
public class ExportRosterItemAction extends AbstractRosterItemAction  {

    public ExportRosterItemAction(String pName, Component pWho) {
        super(pName, pWho);
    }

    boolean selectFrom() {
        return selectExistingFromEntry();
    }

    boolean selectTo() {
        return selectNewToFile();
    }

    boolean doTransfer() {

        // read the file for the "from" entry and write it out

        // ensure preferences will be found for read
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
        mToEntry = new RosterEntry(mFromEntry, mFromID);

        // transfer the contents to the new file
        LocoFile newLocoFile = new LocoFile();
        // File fout = new File(mFullToFilename);
        mToEntry.setFileName(mToFilename);
        newLocoFile.writeFile(mToFile, lroot, mToEntry);

        return true;
    }

    void updateRoster() {
        // exported entry is NOT added to Roster
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ExportRosterItemAction.class.getName());

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
        Action a = new ExportRosterItemAction("Export Roster Item", null);
        a.actionPerformed(new ActionEvent(a, 0, "dummy"));
    }
}
