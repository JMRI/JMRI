// AbstractRosterItemAction.java

package jmri.jmrit.roster;

import org.apache.log4j.Logger;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import jmri.util.swing.WindowInterface;
import javax.swing.Icon;

import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import jmri.beans.Beans;
import jmri.jmrit.roster.swing.RosterEntryComboBox;

/**
 * Base class for Actions to copy, export and import RosterEntrys.
 * <P>
 * Note that {@link DeleteRosterItemAction} is sufficiently
 * different that it doesn't use this base class.
 *
 * @author	Bob Jacobsen   Copyright (C) 2001, 2002, 2007, 2008
 * @version	$Revision$
 * @see         jmri.jmrit.XmlFile
 */
abstract public class AbstractRosterItemAction extends jmri.util.swing.JmriAbstractAction {

    public AbstractRosterItemAction(String pName, Component pWho) {
        super(pName);
        mParent = pWho;
    }
    
    public AbstractRosterItemAction(String s, WindowInterface wi) {
    	super(s, wi);
    }
     
 	public AbstractRosterItemAction(String s, Icon i, WindowInterface wi) {
    	super(s, i, wi);
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

    protected abstract boolean selectFrom();
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
        String group = null;
        if (Beans.hasProperty(wi, "selectedRosterGroup")) {
            group = (String) Beans.getProperty(wi, "selectedRosterGroup");
        }
        JComboBox selections = new RosterEntryComboBox(group);
        int retval = JOptionPane.showOptionDialog(mParent,
                                                  "Select one roster entry", "Select roster entry",
                                                  0, JOptionPane.INFORMATION_MESSAGE, null,
                                                  new Object[]{"Cancel", "OK", selections}, null );
        log.debug("Dialog value "+retval+" selected "+selections.getSelectedIndex()+":\""
                  +selections.getSelectedItem()+"\"");
        if (retval!=1) return false;  // user didn't select

        // find the file for the selected entry to copy
        setExistingEntry((RosterEntry) selections.getSelectedItem());
        
        return true;
    }
    
    /**
     * method added for DP3 where the existing roster entry is selected from a table
     */
    
    public void setExistingEntry(RosterEntry mFromEntry){
        this.mFromEntry = mFromEntry;
        mFromFilename = mFromEntry.getFileName();
        mFullFromFilename = LocoFile.getFileLocation()+mFromFilename;
        log.debug(" from resolves to \""+mFromFilename+"\", \""+mFullFromFilename+"\"");
    }

    boolean selectNewToEntryID() {
        do {
            // prompt for the new ID
            mToID = JOptionPane.showInputDialog(mParent, "Enter id for new roster entry:");
            if (mToID==null) return false;

            // check for empty
            if (mToID.equals("")) {
                JOptionPane.showMessageDialog(mParent,
                                          "The ID cannot be blank");
                // ask again
                continue;
            }
                        
            // check for duplicate
            if (0 == Roster.instance().matchingList(null, null, null, null, null, null, mToID).size()) break;

            // here it is a duplicate, reprompt
            JOptionPane.showMessageDialog(mParent,
                                          "That entry already exists, please choose another");

        } while (true);
        return true;
    }

    javax.swing.JFileChooser fileChooser;

    boolean selectNewFromFile() {
        if (fileChooser==null) {
            fileChooser = jmri.jmrit.XmlFile.userFileChooser();
        }
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
        if (fileChooser==null) {
            fileChooser = jmri.jmrit.XmlFile.userFileChooser();
        }
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
        Roster.writeRosterFile();
    }

    // never invoked, because we overrode actionPerformed above
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }
    
    // initialize logging
    static Logger log
        = Logger.getLogger(AbstractRosterItemAction.class.getName());

}
