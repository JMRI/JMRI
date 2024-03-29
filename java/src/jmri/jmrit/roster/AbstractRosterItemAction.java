package jmri.jmrit.roster;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Objects;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;

import jmri.beans.BeanUtil;
import jmri.jmrit.roster.rostergroup.RosterGroupSelector;
import jmri.jmrit.roster.swing.RosterEntryComboBox;
import jmri.util.swing.JmriJOptionPane;
import jmri.util.swing.WindowInterface;

/**
 * Base class for Actions to copy, export and import RosterEntry objects.
 * <p>
 * Note that {@link DeleteRosterItemAction} is sufficiently different that it
 * doesn't use this base class.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002, 2007, 2008
 * @see jmri.jmrit.XmlFile
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

    @Override
    public void actionPerformed(ActionEvent event) {

        // select the "from" entry/file
        if (!selectFrom()) {
            return;
        }
        // select the "to" entry/file
        if (!selectTo()) {
            return;
        }
        // transfer "from" to "to" as needed
        if (!doTransfer()) {
            return;
        }
        // update roster
        updateRoster();
    }

    protected abstract boolean selectFrom();

    abstract boolean selectTo();

    abstract boolean doTransfer();

    /**
     * Common, but not unique implementation to add the "To" entry to the Roster
     * and rewrite the roster file.
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
    String mFullToFilename = null; // includes path to preferences

    boolean selectExistingFromEntry() {
        // create a dialog to select the roster entry to copy
        String group = null;
        if (BeanUtil.hasProperty(wi, RosterGroupSelector.SELECTED_ROSTER_GROUP)) {
            group = (String) BeanUtil.getProperty(wi, RosterGroupSelector.SELECTED_ROSTER_GROUP);
        }
        JComboBox<?> selections = new RosterEntryComboBox(group);
        int retval = JmriJOptionPane.showOptionDialog(mParent,
                Bundle.getMessage("CopyEntrySelectDialog"), Bundle.getMessage("CopyEntrySelectDialogTitle"),
                JmriJOptionPane.DEFAULT_OPTION, JmriJOptionPane.INFORMATION_MESSAGE, null,
                new Object[]{Bundle.getMessage("ButtonCancel"), Bundle.getMessage("ButtonOK"), selections}, null);
        log.debug("Dialog value {} selected {}:\"{}\"", retval, selections.getSelectedIndex(), selections.getSelectedItem());
        if (retval != 1) { // if not array position 1, ButtonOK
            return false;  // user didn't select
        }
        // find the file for the selected entry to copy
        setExistingEntry((RosterEntry) Objects.requireNonNull(selections.getSelectedItem()));

        return true;
    }

    /**
     * Set the roster entry this action acts upon.
     *
     * @param mFromEntry the roster entry to act upon
     */
    public void setExistingEntry(RosterEntry mFromEntry) {
        this.mFromEntry = mFromEntry;
        mFromFilename = mFromEntry.getFileName();
        mFullFromFilename = Roster.getDefault().getRosterFilesLocation() + mFromFilename;
        log.debug(" from resolves to \"{}\", \"{}\"", mFromFilename, mFullFromFilename);
    }

    boolean selectNewToEntryID() {
        do {
            // prompt for the new ID
            mToID = JmriJOptionPane.showInputDialog(mParent, Bundle.getMessage("NewEntryDialog"),"");
            if (mToID == null) {
                return false;
            }

            // check for empty
            if (mToID.isEmpty()) {
                JmriJOptionPane.showMessageDialog(mParent, Bundle.getMessage("NewEntryEmptyWarn"));
                // ask again
                continue;
            }

            // check for duplicate
            if (0 == Roster.getDefault().matchingList(null, null, null, null,
                    null, null, mToID).size()) {
                break;
            }

            // here it is a duplicate, reprompt
            JmriJOptionPane.showMessageDialog(mParent, Bundle.getMessage("NewEntryDuplicateWarn"));

        } while (true);
        return true;
    }

    javax.swing.JFileChooser fileChooser;

    boolean selectNewFromFile() {
        if (fileChooser == null) {
            fileChooser = jmri.jmrit.XmlFile.userFileChooser();
        }
        // refresh fileChooser view of directory, in case it changed
        fileChooser.rescanCurrentDirectory();
        int retVal = fileChooser.showOpenDialog(mParent);

        // handle selection or cancel
        if (retVal != JFileChooser.APPROVE_OPTION) {
            return false;  // give up if no file selected
        }
        // call load to process the file
        mFromFile = fileChooser.getSelectedFile();
        mFromFilename = mFromFile.getName();
        mFullFromFilename = mFromFile.getAbsolutePath();
        log.debug("New from file: {} at {}", mFromFilename, mFullFromFilename); // NOI18N
        return true;
    }

    boolean selectNewToFile() {
        if (fileChooser == null) {
            fileChooser = jmri.jmrit.XmlFile.userFileChooser();
        }
        fileChooser.setSelectedFile(new File(mFromFilename));
        int retVal = fileChooser.showSaveDialog(mParent);

        // handle selection or cancel
        if (retVal != JFileChooser.APPROVE_OPTION) {
            return false;  // give up if no file selected
        }
        // call load to process the file
        mToFile = fileChooser.getSelectedFile();
        mToFilename = mToFile.getName();
        mFullToFilename = mToFile.getAbsolutePath();
        log.debug("New to file: {} at {}", mToFilename, mFullToFilename); // NOI18N
        return true;
    }

    void addToEntryToRoster() {
        // add the new entry to the roster & write it out
        Roster.getDefault().addEntry(mToEntry);
        Roster.getDefault().writeRoster();
    }

    // never invoked, because we overrode actionPerformed above
    @Override
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }

    // initialize logging
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractRosterItemAction.class);

}
