// DeleteRosterItemAction.java

package jmri.jmrit.roster;

import jmri.jmrit.XmlFile;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;

/**
 * Remove a locomotive from the roster.
 *
 * <P>In case of error, this
 * moves the definition file to a backup.  This action posts
 * a dialog box to select the loco to be deleted, and then posts
 * an "are you sure" dialog box before acting.
 *
 * @author	Bob Jacobsen   Copyright (C) 2001, 2002
 * @version	$Revision: 1.5 $
 * @see         jmri.jmrit.XmlFile
 */
public class DeleteRosterItemAction extends AbstractAction {

    /**
     * @param s Name of this action, e.g. in menus
     * @param who Component that action is associated with, used
     *              to ensure proper position in of dialog boxes
     */
    public DeleteRosterItemAction(String s, Component who) {
        super(s);
        _who = who;
    }

    Component _who;

    public void actionPerformed(ActionEvent event) {

        Roster roster = Roster.instance();

        // get parent object if there is one
        Component parent = null;
        if ( event.getSource() instanceof Component) parent = (Component)event.getSource();

        // create a dialog to select the roster entry
        JComboBox selections = roster.matchingComboBox(null,null, null, null, null,null,null);
        int retval = JOptionPane.showOptionDialog(_who,
                                                  "Select one roster entry", "Select roster entry",
                                                  0, JOptionPane.INFORMATION_MESSAGE, null,
                                                  new Object[]{"Cancel", "OK", selections}, null );
        log.debug("Dialog value "+retval+" selected "+selections.getSelectedIndex()+":"
                  +selections.getSelectedItem());
        if (retval != 1) return;
        String entry = (String) selections.getSelectedItem();

        // find the file for the selected entry
        String filename = roster.fileFromTitle(entry);
        String fullFilename = LocoFile.getFileLocation()+filename;
        log.debug("resolves to \""+filename+"\", \""+fullFilename+"\"");

        // prompt for one last chance
        if (!userOK(entry, filename, fullFilename)) return;

        // delete it from roster
        roster.removeEntry(roster.entryFromTitle(entry));
        roster.writeRosterFile();

        // backup the file & delete it
        try {
            // ensure preferences will be found
            XmlFile.ensurePrefsPresent(LocoFile.getFileLocation());

            // do backup
            LocoFile df = new LocoFile();   // need a dummy object to do this operation in next line
            df.makeBackupFile(LocoFile.getFileLocation()+filename);

            // locate the file and delete
            File f = new File(fullFilename);
            f.delete();

        } catch (Exception ex) {
            log.error("error during locomotive file output: "+ex);
        }

    }

    /**
     * Can provide some mechanism to prompt for user for one
     * last chance to change his/her mind
     * @return true if user says to continue
     */
    boolean userOK(String entry, String filename, String fullFileName) {
        return ( JOptionPane.YES_OPTION ==
                 JOptionPane.showConfirmDialog(_who,
                                               "Delete entry "+entry+" and file "+fullFileName+"?",
                                               "Delete entry "+entry+"?", JOptionPane.YES_NO_OPTION));
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DeleteRosterItemAction.class.getName());

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

        // log.info("DeleteRosterItemAction starts");

        // fire the action
        Action a = new DeleteRosterItemAction("Delete Roster Item", null);
        a.actionPerformed(new ActionEvent(a, 0, "dummy"));
    }
}
