// AbstractRosterItemAction.java

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
 * Base class for Actions to copy, export and import RosterEntrys
 *
 * @author			Bob Jacobsen   Copyright (C) 2001, 2002
 * @version			$Revision: 1.2 $
 * @see             jmri.jmrit.XmlFile
 */
abstract public class AbstractRosterItemAction extends AbstractAction {

	public AbstractRosterItemAction(String pName, Component pWho) {
		super(pName);
		mParent = pWho;
	}

	Component mParent;
    Roster roster = Roster.instance();

    public void actionPerformed(ActionEvent event) {

        // select the "from" entry/file
        if (!selectFrom()) return;
        // select the "to" entry/file
        if (!selectTo()) return;
        // transfer "from" to "to" as needed
        if (!doTransfer()) return;
        // update roster
        updateRoster();

        return;
    }

    abstract boolean selectFrom();
    abstract boolean selectTo();
    abstract boolean doTransfer();
    abstract void updateRoster();

    // variables to communicate the "from" entry, file, etc
        String mFromID = null;
        RosterEntry mFromEntry = null;
        File mFromFile = null;
        String mFromFilename = null;
        String mFullFromFilename = null;  // includes path to preferences

    // variables to communicate the "to" entry, file, etc.
        String mToID = null;
        RosterEntry mToEntry = null;
        File mToFile = null;
        String mToFilename = null;
        String mFullToFilename = null;  // includes path to preferences

    boolean selectExistingFromEntry() {
		// create a dialog to select the roster entry to copy
        JComboBox selections = roster.matchingComboBox(null, null, null, null, null, null, null);
        int retval = JOptionPane.showOptionDialog(mParent,
                        "Select one roster entry to copy", "Select roster entry",
                        0, JOptionPane.INFORMATION_MESSAGE, null,
                        new Object[]{"Cancel", "OK", selections}, null );
        log.debug("Dialog value "+retval+" selected "+selections.getSelectedIndex()+":\""
                    +selections.getSelectedItem()+"\"");
        if (retval!=1) return false;  // user didn't select

        mFromID = (String) selections.getSelectedItem();

        // find the file for the selected entry to copy
        mFromEntry = roster.entryFromTitle(mFromID);
        mFromFilename = roster.fileFromTitle(mFromID);
        mFullFromFilename = LocoFile.fileLocation+mFromFilename;
        log.debug(" from resolves to \""+mFromFilename+"\", \""+mFullFromFilename+"\"");
        return true;
    }

    boolean selectNewToEntryID() {
        do {
            // prompt for the new ID
            mToID = JOptionPane.showInputDialog(mParent, "Enter id for new roster entry:");
            if (mToID==null) return false;

            // check for duplicate
            if (0 == roster.matchingList(null, null, null, null, null, null, mToID).size()) break;

            // here it is a duplicate, reprompt
            JOptionPane.showMessageDialog(mParent,
                                    "That entry already exists, please choose another");

        } while (true);
        return true;
    }

    boolean selectNewFromFile() {
    	javax.swing.JFileChooser fileChooser = new JFileChooser(" ");
		int retVal = fileChooser.showOpenDialog(mParent);

		// handle selection or cancel
		if (retVal != JFileChooser.APPROVE_OPTION) return false;  // give up if no file selected

		// call load to process the file
		mFromFile = fileChooser.getSelectedFile();
		mFromFilename = mFromFile.getName();
		mFullFromFilename = mFromFile.getAbsolutePath();
        log.debug("New from file: "+mFromFilename+" at "+mFullFromFilename);
        return true;
    }

    boolean selectNewToFile() {
    	javax.swing.JFileChooser fileChooser = new JFileChooser(" ");
        fileChooser.setSelectedFile(new File(mFromFilename));
		int retVal = fileChooser.showSaveDialog(mParent);

		// handle selection or cancel
		if (retVal != JFileChooser.APPROVE_OPTION) return false;  // give up if no file selected

		// call load to process the file
		mToFile = fileChooser.getSelectedFile();
		mToFilename = mToFile.getName();
		mFullToFilename = mToFile.getAbsolutePath();
        log.debug("New to file: "+mToFilename+" at "+mFullToFilename);
        return true;
    }

    void addToEntryToRoster() {
        // add the new entry to the roster & write it out
        roster.addEntry(mToEntry);
        roster.writeRosterFile();
    }

	// initialize logging
    static org.apache.log4j.Category log
            = org.apache.log4j.Category.getInstance(AbstractRosterItemAction.class.getName());

}
