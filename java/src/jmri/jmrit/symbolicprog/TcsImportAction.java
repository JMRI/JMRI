package jmri.jmrit.symbolicprog;

import java.io.File;
import java.io.IOException;
import javax.swing.JLabel;

import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.symbolicprog.tabbedframe.PaneProgFrame;

/**
 * Action to import the RosterEntry values from a TCS data file.
 * <p>
 * TODO: Note: This ends with an update of the GUI from the RosterEntry.
 * This means that they (RE and GUI)
 * now agree, which has the side effect of erasing the dirty state.  Better
 * would be to do the import directly into the GUI. See TcsImporter.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2023
 * @author Dave Heap Copyright (C) 2015
 */
public class TcsImportAction extends GenericImportAction {

    public TcsImportAction(String actionName, CvTableModel pModel, VariableTableModel vModel, PaneProgFrame pParent, JLabel pStatus, RosterEntry re) {
        super(actionName, pModel, pParent, pStatus, "TCS files", "txt", null);
        this.rosterEntry = re;
        this.frame = pParent;
        this.vModel = vModel;
    }

    RosterEntry rosterEntry;
    PaneProgFrame frame;
    VariableTableModel vModel;

    @Override
    boolean launchImporter(File file, CvTableModel tableModel) {
        try {
            // ctor launches operation
            var importer = new TcsImporter(file, tableModel, vModel);
            importer.setRosterEntry(rosterEntry);

            // now update the GUI from the roster entry
            frame.getRosterPane().updateGUI(rosterEntry);
            frame.getFnLabelPane().updateFromEntry(rosterEntry);

            return true;
        } catch (IOException ex) {
            return false;
        }
    }
}
