package jmri.jmrit.roster;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import jmri.beans.Beans;
import jmri.jmrit.roster.rostergroup.RosterGroupSelector;
import jmri.jmrit.roster.swing.RosterEntryComboBox;
import jmri.util.FileUtil;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Remove a locomotive from the roster.
 *
 * <P>
 * In case of error, this moves the definition file to a backup. This action
 * posts a dialog box to select the loco to be deleted, and then posts an "are
 * you sure" dialog box before acting.
 *
 *
 * <hr> This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <P>
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2002
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

        Roster roster = Roster.instance();
        String rosterGroup = Roster.instance().getDefaultRosterGroup();
        RosterEntry[] entries;
        // rosterGroup may legitimately be null
        // but getProperty returns null if the property cannot be found, so
        // we test that the property exists before attempting to get its value
        if (Beans.hasProperty(wi, RosterGroupSelector.SELECTED_ROSTER_GROUP)) {
            rosterGroup = (String) Beans.getProperty(wi, RosterGroupSelector.SELECTED_ROSTER_GROUP);
            log.debug("selectedRosterGroup was {}", rosterGroup);
        }
        if (Beans.hasProperty(wi, "selectedRosterEntries")) {
            entries = (RosterEntry[]) Beans.getProperty(wi, "selectedRosterEntries");
            if (entries != null) {
                log.debug("selectedRosterEntries found {} entries", entries.length);
            } else {
                log.debug("selectedRosterEntries left entries null");
            }
        } else {
            entries = selectRosterEntry(rosterGroup);
            if (entries != null) {
                log.debug("selectRosterEntry(rosterGroup) found {} entries", entries.length);
            } else {
                log.debug("selectRosterEntry(rosterGroup) left entries null");
            }
        }
        if (entries == null) {
            return;
        }
        // get parent object if there is one
        //Component parent = null;
        //if ( event.getSource() instanceof Component) parent = (Component)event.getSource();

        // find the file for the selected entry
        for (RosterEntry re : entries) {
            String filename = roster.fileFromTitle(re.titleString());
            String fullFilename = LocoFile.getFileLocation() + filename;
            log.debug("resolves to [{}], [{}]", filename, fullFilename);

            // prompt for one last chance
            log.debug("rosterGroup now {}", rosterGroup);
            if (rosterGroup == null) {
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
            Roster.writeRosterFile();

            // backup the file & delete it
            if (rosterGroup == null) {
                try {
                    // ensure preferences will be found
                    FileUtil.createDirectory(LocoFile.getFileLocation());

                    // move original file to backup
                    LocoFile df = new LocoFile();   // need a dummy object to do this operation in next line
                    df.makeBackupFile(LocoFile.getFileLocation() + filename);

                } catch (Exception ex) {
                    log.error("error during locomotive file output: " + ex);
                }
            }
        }

    }

    protected RosterEntry[] selectRosterEntry(String rosterGroup) {
        // create a dialog to select the roster entry
        JComboBox<?> selections = new RosterEntryComboBox(rosterGroup);
        int retval = JOptionPane.showOptionDialog(_who,
                "Select one roster entry", "Delete roster entry",
                0, JOptionPane.INFORMATION_MESSAGE, null,
                new Object[]{"Cancel", "OK", selections}, null);
        log.debug("Dialog value " + retval + " selected " + selections.getSelectedIndex() + ":"
                + selections.getSelectedItem());
        if (retval != 1) {
            return null;
        }
        RosterEntry[] entries = new RosterEntry[1];
        entries[0] = (RosterEntry) selections.getSelectedItem();
        return entries;
    }

    /**
     * Can provide some mechanism to prompt for user for one last chance to
     * change his/her mind
     *
     * @return true if user says to continue
     */
    boolean userOK(String entry, String filename, String fullFileName) {
        return (JOptionPane.YES_OPTION
                == JOptionPane.showConfirmDialog(_who,
                        java.text.MessageFormat.format(ResourceBundle.getBundle("jmri.jmrit.roster.JmritRosterBundle").getString("DeletePrompt"),
                                entry, fullFileName),
                        java.text.MessageFormat.format(ResourceBundle.getBundle("jmri.jmrit.roster.JmritRosterBundle").getString("DeleteTitle"),
                                entry),
                        JOptionPane.YES_NO_OPTION));
    }
    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(DeleteRosterItemAction.class.getName());

    // never invoked, because we overrode actionPerformed above
    @Override
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }
}
