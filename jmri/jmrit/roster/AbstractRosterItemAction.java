// AbstractRosterItemAction.java

package jmri.jmrit.roster;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * Base class for Actions to copy, export and import RosterEntrys.
 * <P>
 * Note that {@link DeleteRosterItemAction} is sufficiently
 * different that it doesn't use this base class.
 *
 * @author	Bob Jacobsen   Copyright (C) 2001, 2002
 * @version	$Revision: 1.10 $
 * @see         jmri.jmrit.XmlFile
 */
abstract public class AbstractRosterItemAction extends AbstractAction {

    public AbstractRosterItemAction(String pName, Component pWho) {
        super(pName);
        mParent = pWho;
    }

    Component mParent;

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

    /**
     * Common, but not unique implementation to add the "To" entry
     * to the Roster and rewrite the roster file.
     */
    void updateRoster() {
        addToEntryToRoster();
    }


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
        JComboBox selections = Roster.instance().fullRosterComboBox();
        int retval = JOptionPane.showOptionDialog(mParent,
                                                  "Select one roster entry", "Select roster entry",
                                                  0, JOptionPane.INFORMATION_MESSAGE, null,
                                                  new Object[]{"Cancel", "OK", selections}, null );
        log.debug("Dialog value "+retval+" selected "+selections.getSelectedIndex()+":\""
                  +selections.getSelectedItem()+"\"");
        if (retval!=1) return false;  // user didn't select

        mFromID = (String) selections.getSelectedItem();

        // find the file for the selected entry to copy
        mFromEntry = Roster.instance().entryFromTitle(mFromID);
        mFromFilename = Roster.instance().fileFromTitle(mFromID);
        mFullFromFilename = LocoFile.getFileLocation()+mFromFilename;
        log.debug(" from resolves to \""+mFromFilename+"\", \""+mFullFromFilename+"\"");
        return true;
    }

    boolean selectNewToEntryID() {
        do {
            // prompt for the new ID
            mToID = JOptionPane.showInputDialog(mParent, "Enter id for new roster entry:");
            if (mToID==null) return false;

            // check for duplicate
            if (0 == Roster.instance().matchingList(null, null, null, null, null, null, mToID).size()) break;

            // here it is a duplicate, reprompt
            JOptionPane.showMessageDialog(mParent,
                                          "That entry already exists, please choose another");

        } while (true);
        return true;
    }

    javax.swing.JFileChooser fileChooser = new JFileChooser(jmri.jmrit.XmlFile.prefsDir());

    boolean selectNewFromFile() {
        // refresh fileChooser view of directory, in case it changed
        fileChooser.rescanCurrentDirectory();
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
        Roster.instance().addEntry(mToEntry);
        Roster.instance().writeRosterFile();
    }

    // initialize logging
    static org.apache.log4j.Category log
        = org.apache.log4j.Category.getInstance(AbstractRosterItemAction.class.getName());

}
