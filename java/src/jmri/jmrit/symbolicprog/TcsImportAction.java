package jmri.jmrit.symbolicprog;

import java.io.File;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JLabel;

import jmri.jmrit.roster.RosterEntry;

/**
 * Action to import the CV values from a TCS data file.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2023
 * @author Dave Heap Copyright (C) 2015
 */
public class TcsImportAction extends GenericImportAction {

    public TcsImportAction(String actionName, CvTableModel pModel, JFrame pParent, JLabel pStatus, RosterEntry re) {
        super(actionName, pModel, pParent, pStatus, "TCS files", "txt", null);
        this.rosterEntry = re;
    }

    RosterEntry rosterEntry;

    @Override
    boolean launchImporter(File file, CvTableModel tableModel) {
            try {
                // ctor launches operation
                var importer = new TcsImporter(file);
                importer.setRosterEntry(rosterEntry);
                return true;
            } catch (IOException ex) {
                return false;
        }
    }
}
