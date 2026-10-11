package jmri.jmrit.roster;

import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.Icon;
import javax.swing.JComboBox;

import jmri.beans.BeanUtil;
import jmri.jmrit.roster.rostergroup.RosterGroupSelector;
import jmri.jmrit.roster.swing.RosterEntryComboBox;
import jmri.util.FileUtil;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.JmriJOptionPane;
import jmri.util.swing.WindowInterface;

/**
 * Remove a locomotive from the roster.
 *
 * <p>
 * In case of error, this moves the definition file to a backup. This action
 * posts a dialog box to select the loco to be deleted, and then posts an "are
 * you sure" dialog box before acting.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002, 2025
 * @see jmri.jmrit.XmlFile
 */
public class DeleteRosterItemAction extends JmriAbstractAction {

    public DeleteRosterItemAction(String s, WindowInterface wi) {
        super(s, wi);
    }

    public DeleteRosterItemAction(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
    }

    /**
     * @param s   Name of this action, e.g. in menus
     * @param who Component that action is associated with, used to ensure
     *            proper position in of dialog boxes
     */
    public DeleteRosterItemAction(String s, Component who) {
        super(s);
        _who = who;
    }
    Component _who;

    @Override
    public void actionPerformed(ActionEvent event) {

        Roster roster = Roster.getDefault();
        String rosterGroup = Roster.getDefault().getDefaultRosterGroup();
        RosterEntry[] entries;
        // rosterGroup may legitimately be null
        // but getProperty returns null if the property cannot be found, so
        // we test that the property exists before attempting to get its value
        if (BeanUtil.hasProperty(wi, RosterGroupSelector.SELECTED_ROSTER_GROUP)) {
            rosterGroup = (String) BeanUtil.getProperty(wi, RosterGroupSelector.SELECTED_ROSTER_GROUP);
            log.debug("selectedRosterGroup was {}", rosterGroup);
        }
        if (BeanUtil.hasProperty(wi, "selectedRosterEntries")) {
            entries = (RosterEntry[]) BeanUtil.getProperty(wi, "selectedRosterEntries");
            if (entries != null) {
                log.debug("selectedRosterEntries found {} entries", entries.length);
            } else {
                log.debug("selectedRosterEntries left entries null");
            }
        } else {
            entries = selectRosterEntry(rosterGroup);
            if (entries.length > 0 ) {
                log.debug("selectRosterEntry(rosterGroup) found {} entries", entries.length);
            } else {
                log.debug("selectRosterEntry(rosterGroup) found no entries");
            }
        }
        if (entries == null || entries.length == 0) {
            return;
        }
        // get parent object if there is one
        //Component parent = null;
        //if ( event.getSource() instanceof Component) parent = (Component)event.getSource();

        // find the file for the selected entry
        for (RosterEntry re : entries) {
            String filename = roster.fileFromTitle(re.titleString());
            String fullFilename = Roster.getDefault().getRosterFilesLocation() + filename;
            log.debug("resolves to [{}], [{}]", filename, fullFilename);

            // prompt for one last chance
            log.debug("rosterGroup now {}", rosterGroup);
            if (rosterGroup == null || rosterGroup.equals(Roster.NOGROUP)) {
                if (!userOK(re.titleString(), filename, fullFilename)) {
                    return;
                }
                // delete it from roster
                roster.removeEntry(re);
            } else {
                String group = Roster.getRosterGroupProperty(rosterGroup);
                log.debug("removing {} group from entry", group);
                re.deleteAttribute(group);
                re.updateFile();
            }
            Roster.getDefault().writeRoster();

            // backup the file & delete it
            if (rosterGroup == null) {
                try {
                    // ensure preferences will be found
                    FileUtil.createDirectory(Roster.getDefault().getRosterFilesLocation());

                    // move original file to backup
                    LocoFile df = new LocoFile();   // need a dummy object to do this operation in next line
                    df.makeBackupFile(Roster.getDefault().getRosterFilesLocation() + filename);

                } catch (Exception ex) {
                    log.error("error during locomotive file output", ex);
                }
            }
        }

    }

    protected RosterEntry[] selectRosterEntry(String rosterGroup) {
        RosterEntry[] entries = new RosterEntry[1];
        // create a dialog to select the roster entry
        JComboBox<?> selections = new RosterEntryComboBox(rosterGroup);
        int retval = JmriJOptionPane.showOptionDialog(_who,
                Bundle.getMessage("CopyEntrySelectDialog"),
                Bundle.getMessage("DeleteEntryTitle roster entry"),
                JmriJOptionPane.DEFAULT_OPTION,
                JmriJOptionPane.INFORMATION_MESSAGE,
                null,
                new Object[] {Bundle.getMessage("ButtonCancel"), Bundle.getMessage("ButtonOK"), selections}, null);
        log.debug("Dialog value {} selected {}:{}", retval, selections.getSelectedIndex(), selections.getSelectedItem()); // NOI18N
        if (retval != 1 ) { // array position 1 ButtonOK
            return entries; // empty
        }
        entries[0] = (RosterEntry) selections.getSelectedItem();
        return entries;
    }

    /**
     * Can provide some mechanism to prompt for user for one last chance to
     * change his/her mind.
     *
     * @param entry Roster entry being operated on
     * @param filename Just name of file
     * @param fullFileName including path
     * @return true if user says to continue
     */
    boolean userOK(String entry, String filename, String fullFileName) {
        return (JmriJOptionPane.YES_OPTION == JmriJOptionPane.showConfirmDialog(_who,
                Bundle.getMessage("DeletePrompt", entry, fullFileName),
                Bundle.getMessage("DeleteTitle", entry),
                JmriJOptionPane.YES_NO_OPTION));
    }

    // never invoked, because we overrode actionPerformed above
    @Override
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }

    // initialize logging
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DeleteRosterItemAction.class);

}
