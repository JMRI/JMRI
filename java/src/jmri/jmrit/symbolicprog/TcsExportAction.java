package jmri.jmrit.symbolicprog;

import java.awt.event.ActionEvent;
import java.io.*;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

import jmri.jmrit.roster.RosterEntry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to export the CV values to a TCS-format data file.
 *
 * @author Bob Jacobsen Copyright (C) 2003
 */
public class TcsExportAction extends AbstractAction {

    public TcsExportAction(String actionName, CvTableModel pModel, RosterEntry rosterEntry, JFrame pParent) {
        super(actionName);
        mModel = pModel;
        mParent = pParent;
        this.rosterEntry = rosterEntry;
    }

    JFileChooser fileChooser;
    JFrame mParent;
    RosterEntry rosterEntry;

    /**
     * CvTableModel to load
     */
    CvTableModel mModel;

    @Override
    public void actionPerformed(ActionEvent e) {

        if (fileChooser == null) {
            fileChooser = new JFileChooser();
        }

        int retVal = fileChooser.showSaveDialog(mParent);

        if (retVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (log.isDebugEnabled()) {
                log.debug("start to export to TCS file {}", file);
            }

            try ( PrintStream str = new PrintStream(new FileOutputStream(file)); ) {
                formatTcsVirtualNodeDefinition(str, rosterEntry, mModel);
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
     * @throws IOException
     */
    public static void formatTcsVirtualNodeDefinition(PrintStream str, RosterEntry rosterEntry, CvTableModel model) {
        str.println("Train.Name="+rosterEntry.getId());
        str.println("Train.User Description="+rosterEntry.getComment());
        str.println("Train.Address="+rosterEntry.getDccAddress());

        // set "forced long address 128 steps" or "forced short address 128 steps"
        str.println("Train.Speed Step Mode="+(rosterEntry.isLongAddress() ? "15" : "14"));

        // TODO: See if these can be obtained from standard CV locations
        str.println("Train.F0.Consist Behavior=1");
        str.println("Train.F0.Directional=0");
        str.println("Train.F0.MU switch=0");

        // TODO: Should check for the known strings and encode them in the Display attribute?
        for (int i = 0; i < 28; i++) { // TCS sample file went to 27?
            int displayValue = 0;
            if (rosterEntry.getFunctionLabel(i) != null) {
                switch (rosterEntry.getFunctionLabel(i)) {

                    case "Light": displayValue = 1; break;
                    case "Beamer": displayValue = 2; break;
                    case "Bell": displayValue = 3; break;
                    case "Horn": displayValue = 4; break;
                    case "Shunting mode": displayValue = 5; break;
                    case "Pantograph": displayValue = 6; break;
                    case "Smoke": displayValue = 7; break;
                    case "Momentum off": displayValue = 8; break;
                    case "Whistle": displayValue = 9; break;
                    case "Sound": displayValue = 10; break;
                    case "F": displayValue = 11; break;
                    case "Announce": displayValue = 12; break;
                    case "Engine": displayValue = 13; break;
                    case "Light1": displayValue = 14; break;
                    case "Light2": displayValue = 15; break;
                    case "Uncouple": displayValue = 17; break;

                    default: displayValue = 0; break;
                }
            }

            str.println("Train.Functions("+i+").Display="+displayValue);
            str.println("Train.Functions("+i+").Momentary="+(rosterEntry.getFunctionLockable(i) ? "0" : "1")); // Momentary == not locking
            str.println("Train.Functions("+i+").Consist Behavior=1");
            str.println("Train.Functions("+i+").Description="+(rosterEntry.getFunctionLabel(i)==null ? "" : rosterEntry.getFunctionLabel(i)) );
        }

        str.println("Train.Delete From Roster?=0");

        str.flush();
        str.close();
    }

    private final static Logger log = LoggerFactory.getLogger(TcsExportAction.class);
}
