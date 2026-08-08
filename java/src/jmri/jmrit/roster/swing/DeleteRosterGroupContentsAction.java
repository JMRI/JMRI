package jmri.jmrit.roster.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.Icon;

import jmri.beans.BeanUtil;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.rostergroup.RosterGroupSelector;
import jmri.util.swing.CountingBusyDialog;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.JmriJOptionPane;
import jmri.util.swing.WindowInterface;
import jmri.util.ThreadingUtil;

/**
 * Remove roster group contents, leaving the group
 *
 * @author Kevin Dickerson Copyright (C) 2009, 2026
 * @author Bob Jacobsen    Copyright (C) 2026
 */
public class DeleteRosterGroupContentsAction extends JmriAbstractAction {

    public DeleteRosterGroupContentsAction(String s, WindowInterface wi) {
        super(s, wi);
    }

    public DeleteRosterGroupContentsAction(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
    }

    /**
     * @param s   Name of this action, e.g. in menus
     * @param who Component that action is associated with, used to ensure
     *            proper position in of dialog boxes
     */
    public DeleteRosterGroupContentsAction(String s, Component who) {
        super(s);
        _who = who;
    }

    Component _who;
    CountingBusyDialog dialog;
    
    /**
     * Call setParameter("group", oldName) prior to calling
     * actionPerformed(event) to bypass the roster group selection dialog if the
     * name of the group to be copied is already known and is not the
     * selectedRosterGroup property of the WindowInterface.
     *
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        var roster = Roster.getDefault();
        String group = null;
        if (BeanUtil.hasProperty(wi, RosterGroupSelector.SELECTED_ROSTER_GROUP)) {
            group = (String) BeanUtil.getProperty(wi, RosterGroupSelector.SELECTED_ROSTER_GROUP);
        }
        // null might be valid output from getting the selectedRosterGroup,
        // so we have to check for null again.
        if (group == null) {
            group = (String) JmriJOptionPane.showInputDialog(_who,
                    Bundle.getMessage("DeleteRosterGroupContentsDialog"),
                    Bundle.getMessage("DeleteRosterGroupContentsTitle", ""),
                    JmriJOptionPane.INFORMATION_MESSAGE,
                    null,
                    roster.getRosterGroupListWithNoGroup().toArray(),
                    null);
        }
        // can't delete the roster itself (ALLENTRIES and null represent the full roster)
        if (group == null || group.equals(Roster.ALLENTRIES)) {
            return;
        }
        // prompt for one last chance
        if (!userOK(group)) {
            return;
        }

        final String deleteGroup = group;
        
        ThreadingUtil.newThread(() -> {
            // delete the roster group contents
            List<RosterEntry> entries = roster.getEntriesInGroup(deleteGroup);
            
            dialog = new CountingBusyDialog(null, "Deleting Roster Entries", true, entries.size());
            ThreadingUtil.runOnGUIEventually(() -> {dialog.start();});
    
            int count = 0;
            for (RosterEntry entry : entries) {
                log.info("Deleting entry: {}", entry.getId());
                roster.removeEntry(entry);
                final int thisCount = ++count;
                ThreadingUtil.runOnGUI(() -> {dialog.count(thisCount);});
            }
            
            roster.writeRoster();
            ThreadingUtil.runOnGUIEventually(() -> {dialog.finish();});
        }, "Delete Roster Group Contents").start();
        
    }

    /**
     * Can provide some mechanism to prompt for user for one last chance to
     * change his/her mind
     * @param entry roster group to confirm deletion of
     *
     * @return true if user says to continue
     */
    boolean userOK(String entry) {
        String[] titles = {Bundle.getMessage("ButtonDelete"), Bundle.getMessage("ButtonCancel")};
        // TODO: replace "Are you sure..." string with JPanel containing string
        //       and checkbox silencing this message in the future
        return ( 0 == // array position 0, ButtonDelete 
                JmriJOptionPane.showOptionDialog(_who,
                Bundle.getMessage("DeleteRosterGroupContentsSure", entry),
                Bundle.getMessage("DeleteRosterGroupContentsTitle", entry),
                JmriJOptionPane.DEFAULT_OPTION,
                JmriJOptionPane.QUESTION_MESSAGE,
                null,
                titles,
                null));
    }

    // never invoked, because we overrode actionPerformed above
    @Override
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }

    // initialize logging
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DeleteRosterGroupContentsAction.class);

}
