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
 * @version			$Revision: 1.1 $
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

		// create a dialog to select the roster entry & capture the new name
        JComboBox selections = roster.matchingComboBox(null,null, null, null, null,null,null);
        JTextField newNameField = new JTextField(20);
        int retval = JOptionPane.showOptionDialog(null,
                        "Select one roster entry", "Select roster entry",
                        0, JOptionPane.INFORMATION_MESSAGE, null,
                        new Object[]{"Cancel", "OK", newNameField, selections}, null );
        log.debug("Dialog value "+retval+" selected "+selections.getSelectedIndex()+":\""
                    +selections.getSelectedItem()+"\", \""
                    +newNameField.getText()+"\"");
        if (retval!=1) return;  // user didn't select

        String entry = (String) selections.getSelectedItem();
        String newEntry = newNameField.getText();

        // find the file for the selected entry
        String filename = roster.fileFromTitle(entry);
        String fullFilename = XmlFile.prefsDir()+LocoFile.fileLocation+filename;
        log.debug("resolves to \""+filename+"\", \""+fullFilename+"\"");

        // read the file, change the ID, and write it out
		try {
            // ensure preferences will be found
            XmlFile.ensurePrefsPresent(XmlFile.prefsDir()+LocoFile.fileLocation);

            // locate the file and delete
			File f = new File(fullFilename);

            LocoFile lf = new LocoFile();  // used as a temporary
		    Element lroot = null;
		    try {
			    lroot = lf.rootFromName(fullFilename);
		    } catch (Exception e) { log.error("Exception while loading loco XML file: "+fullFilename+" exception: "+e); }

		} catch (Exception ex) {
			log.error("error during locomotive file output: "+ex);
		}

        // add new entry to roster
        //roster.removeEntry(roster.entryFromTitle(entry));
        //roster.writeRosterFile();

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
