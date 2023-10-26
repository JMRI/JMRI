package jmri.jmrit.symbolicprog;

import java.awt.event.ActionEvent;
import java.io.*;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.symbolicprog.tabbedframe.PaneProgFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to export the RosterEntry values to a TCS-format data file.
 * <p>
 * TODO: Note: This first does an update of the RosterEntry from the GUI,
 * then writes out from the Roster entry.  This means that they (RE and GUI)
 * now agree, which has the side effect of erasing the dirty state.  Better
 * would be to do the export directly from the GUI.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2023
 */
public class TcsExportAction extends AbstractAction {

    public TcsExportAction(String actionName, CvTableModel mModel, VariableTableModel vModel, RosterEntry rosterEntry, PaneProgFrame pParent) {
        super(actionName);
        this.mModel = mModel;
        this.vModel = vModel;
        frame = pParent;
        this.rosterEntry = rosterEntry;
    }

    JFileChooser fileChooser;
    PaneProgFrame frame;
    RosterEntry rosterEntry;

    /**
     * CvTableModel to load
     */
    CvTableModel mModel;

    /**
     * VariableTableModel to load
     */
    VariableTableModel vModel;

    @Override
    public void actionPerformed(ActionEvent e) {

        if (fileChooser == null) {
            fileChooser = new jmri.util.swing.JmriJFileChooser();
        }

        int retVal = fileChooser.showSaveDialog(frame);

        if (retVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (log.isDebugEnabled()) {
                log.debug("start to export to TCS file {}", file);
            }

            // update the RosterEntry from the GUI
            frame.getRosterPane().update(rosterEntry);
            frame.getFnLabelPane().update(rosterEntry);

            try ( PrintStream str = new PrintStream(new FileOutputStream(file)); ) {
                formatTcsVirtualNodeDefinition(str, rosterEntry, mModel, vModel);
            } catch (IOException ex) {
                log.error("Error writing file", ex);
            }
        }
    }

    /**
     * Format the contents of the locomotive definition.
     * This method is public static so it can be used elsewhere for e.g.
     * direct writing to a node.
     * @param str receives the formatted definition String
     * @param rosterEntry defines the information to store
     * @param model provides CV contents as available
     * @param vModel provides variable contents as available
     */
    public static void formatTcsVirtualNodeDefinition(PrintStream str, RosterEntry rosterEntry, CvTableModel model, VariableTableModel vModel) {
        str.println("Train.Name="+rosterEntry.getId());
        str.println("Train.User Description="+rosterEntry.getComment());
        str.println("Train.Address="+rosterEntry.getDccAddress());

        // set "forced long address 128 steps" or "forced short address 128 steps"
        str.println("Train.Speed Step Mode="+(rosterEntry.isLongAddress() ? "15" : "14"));

        // Skip Consist, Directional and MU switch to allow round-trip

        for (int i = 0; i <= 27; i++) { // TCS sample file went to 27
            String label = rosterEntry.getFunctionLabel(i+1);
            if (label == null) label = "";
            int displayValue = intFromFunctionString(label);

            str.println("Train.Functions("+i+").Display="+displayValue);
            str.println("Train.Functions("+i+").Momentary="+(rosterEntry.getFunctionLockable(i+1) ? "0" : "1")); // Momentary == not locking

            // check for CV21/CV22 variable value, otherwise skip (leave unchanged)
            var variable = vModel.findVar("Consist Address Active For F"+(i+1));
            if (variable != null) {
                var value = variable.getIntValue();
                log.trace("For index {} found consist value {}", i, value);
                str.println("Train.Functions("+i+").Consist Behavior="+value);
            }

            str.println("Train.Functions("+i+").Description="+(displayValue != 0 ? "" : label) );
        }

        str.println("Train.Delete From Roster?=0");

        str.flush();
        str.close();
    }

    static int intFromFunctionString(String fn) {
        if (fn == null) return 0;

        switch (fn.toLowerCase().strip()) {

            case "unassigned": return 0;

            case "headlight": return 1;
            case "bell": return 13;
            case "horn": return 14;
            case "whistle": return 15;
            case "pantograph": return 11;
            case "smoke": return 10;
            case "engine": return 4;
            case "light": return 74;
            case "coupler clank": return 28;
            case "couple": return 122;
            case "uncouple": return 9;

            case "shunting mode": return 7;
            case "momentum": return 8;

            case "brake": return 57;
            case "brake release": return 200;
            case "dynamic brake": return 41;
            case "manual notch down": return 31;
            case "manual notch up": return 30;
            case "reverser": return 69;
            case "mute": return 100;

            case "far light": return 12;
            case "cab light": return 3;
            case "ditch lights": return 48;
            case "step lights": return 98;
            case "tail lights": return 62;
            case "switching lights": return 58;
            case "dimmer": return 51;
            case "interior lights": return 2;

            case "air compressor": return 42;
            case "air pump": return 45;
            case "injector": return 60;
            case "exhaust fan": return 108;
            case "radiator fan": return 17;
            case "steam generator": return 66;
            case "blower": return 105;
            case "blow down": return 56;
            case "safety": return 38;
            case "sanding": return 55;
            case "ash dump": return 88;
            case "shoveling": return 18;
            case "water fill": return 35;

            case "long whistle": return 103;
            case "short whistle": return 64;
            case "doppler horn": return 63;

            case "curve squeal": return 36;
            case "brake squeal": return 21;
            case "announce": return 6;
            case "cab chatter": return 27;

            default: return 0;
        }
}

    private final static Logger log = LoggerFactory.getLogger(TcsExportAction.class);
}
