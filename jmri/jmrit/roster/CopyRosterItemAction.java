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
 * @version			$Revision: 1.2 $
 * @see             jmri.jmrit.XmlFile
 */
public class CopyRosterItemAction extends AbstractAction {

	public CopyRosterItemAction(String s, Component who) {
		super(s);
		mParent = who;
	}

	Component mParent;

    public void actionPerformed(ActionEvent event) {

        Roster roster = Roster.instance();

		// create a dialog to select the roster entry to copy
        JComboBox selections = roster.matchingComboBox(null, null, null, null, null, null, null);
        int retval = JOptionPane.showOptionDialog(mParent,
                        "Select one roster entry to copy", "Select roster entry",
                        0, JOptionPane.INFORMATION_MESSAGE, null,
                        new Object[]{"Cancel", "OK", selections}, null );
        log.debug("Dialog value "+retval+" selected "+selections.getSelectedIndex()+":\""
                    +selections.getSelectedItem()+"\"");
        if (retval!=1) return;  // user didn't select

        String fromID = (String) selections.getSelectedItem();

        // find the file for the selected entry to copy
        RosterEntry fromEntry = roster.entryFromTitle(fromID);
        String fromFilename = roster.fileFromTitle(fromID);
        String fullFromFilename = LocoFile.fileLocation+fromFilename;
        log.debug("resolves to \""+fromFilename+"\", \""+fullFromFilename+"\"");

        String newEntryID = null;
        do {
            // prompt for the new ID
            newEntryID = JOptionPane.showInputDialog(mParent, "Enter id for new roster entry:");
            if (newEntryID==null) return;

            // check for duplicate
            if (0 == roster.matchingList(null, null, null, null, null, null, newEntryID).size()) break;

            // here it is a duplicate, reprompt
            JOptionPane.showMessageDialog(mParent,
                                    "That entry already exists, please choose another");

        } while (true);

        // read the input file, change the ID, and write it out
		try {
            // ensure preferences will be found
            XmlFile.ensurePrefsPresent(XmlFile.prefsDir()+LocoFile.fileLocation);

            // locate the file
			File f = new File(fullFromFilename);

            // read it
            LocoFile lf = new LocoFile();  // used as a temporary
		    Element lroot = null;
		    try {
			    lroot = lf.rootFromName(fullFromFilename);
		    } catch (Exception e) {
                log.error("Exception while loading loco XML file: "+fullFromFilename+" exception: "+e);
                return;
            }

            // create a new entry
            RosterEntry newEntry = new RosterEntry(fromEntry, newEntryID);

            // set the filename from the ID
            newEntry.ensureFilenameExists();

            // transfer the contents to a new file
            LocoFile newLocoFile = new LocoFile();
            File fout = new File(XmlFile.prefsDir()+LocoFile.fileLocation+newEntry.getFileName());
            newLocoFile.writeFile(fout, lroot, newEntry);

            // add the new entry to the roster & write it out
            roster.addEntry(newEntry);
            roster.writeRosterFile();

		} catch (Exception ex) {
			log.error("unexpected error during copy operation: "+ex);
            return;
		}
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
        Action a = new CopyRosterItemAction("Delete Roster Item", null);
        a.actionPerformed(new ActionEvent(a, 0, "dummy"));
    }
}
