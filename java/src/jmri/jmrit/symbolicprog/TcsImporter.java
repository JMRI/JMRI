package jmri.jmrit.symbolicprog;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

import jmri.jmrit.roster.RosterEntry;

/**
 * Import CV values from a TCS backup file (from a CDI backup)
 * directly into a RosterEntry.
 *<p>
 * Note that this does not update any GUI that's showing the
 * RosterEntry, e.g. a RosterPane or FunctionLabelPane. Those
 * must have their updates triggered elsewhere, e.g. TcsImportAction.
 *
 * @author Alex Shepherd Copyright (C) 2003 (original Pr1Importer)
 * *author Bob Jacobsen  Copyright (C) 2023
 */
public class TcsImporter {

    Properties tcsProperties;

    /**
     * The import process starts upon creation of a TcsImporter
     * @param file The File object to be read
     * @param cvModel Model used to look up CV values (not used)
     * @param model Variable model used to look up function bits (retained for later use)
     * @throws IOException from underlying file operations
     */
    public TcsImporter(File file, CvTableModel cvModel, VariableTableModel model) throws IOException {
        this.model = model;
        tcsProperties = new Properties();
        FileInputStream fileStream = new FileInputStream(file);
        try {
            tcsProperties.load(fileStream);
        } finally {
            fileStream.close();
        }
    }

    VariableTableModel model;

    public TcsImporter(InputStream stream) throws IOException {
        tcsProperties = new Properties();
        tcsProperties.load(stream);
    }

    public void setRosterEntry(RosterEntry rosterEntry) {
        // TODO: How to handle name?  Check for mismatch with ID? (Don't change ID?)
        log.debug("found name {}", tcsProperties.get("Train.Name"));
        // TODO: Confirm that address is correct?  How to handle that?
        log.debug("found address {}", tcsProperties.get("Train.Address"));
        log.debug("found step {}", tcsProperties.get("Train.Speed")); // note key truncated as space

        // TODO: move the following to a static method ot allow reuse from elsewhere

        // copy User Description to comment
        var userDescription = tcsProperties.get("Train.User").toString(); // note key truncated as space
        // remove the "Description=" at front (due to space in key=value pair in input)
        userDescription = userDescription.substring("Description=".length());
        rosterEntry.setComment(userDescription);

        for (int i=0; i < 27; i++) {
            var momentaryObj = tcsProperties.get("Train.Functions("+i+").Momentary");
            log.trace("Found Momentary {} as {}", i, momentaryObj);
            if (momentaryObj == null) continue; // no change if null
            var momentary = momentaryObj.toString();

            var displayObj = tcsProperties.get("Train.Functions("+i+").Display");
            log.trace("Found Display {} as {}", i, displayObj);
            if (displayObj == null) continue; // no change if null
            var display = displayObj.toString();

            var descriptionObj = tcsProperties.get("Train.Functions("+i+").Description");
            log.trace("Found Description {} as {}", i, descriptionObj);
            if (descriptionObj == null) continue; // no change if null
            var description = descriptionObj.toString();

            // Handle non-zero Display values by updating description value
            description = unpackDescription(description, display);

            // Here, we copy Description to the label
            rosterEntry.setFunctionLabel(i+1, description);
            // and momentary to the "locked" status
            rosterEntry.setFunctionLockable(i+1, momentary.equals("1"));

            // process consist bit
            // first, see if function variable exists
            var variable = model.findVar("Consist Address Active For F"+(i+1));
            if (variable != null) {
                // it exists, so we can't ignore the consist info.
                // retrieve it
                var consistObj = tcsProperties.get("Train.Functions("+i+").Consist");
                log.debug("Found {} as \'{}\'", "Train.Functions("+i+").Consist", consistObj);
                if (consistObj != null) {
                    if (consistObj.equals("Behavior=1")) { // "Current Cab Only"
                        variable.setIntValue(1);
                    } else {
                        variable.setIntValue(0);    // respond to the consist address
                    }
                    log.trace("result is value {}", variable.getIntValue());
                }
            } else {
                log.debug("Variable {} not found", "Consist Address Active For F"+(i+1) );
            }
        }
    }

    static String unpackDescription(String description, String display) {
        // if there is a description value, that wins
        if (!description.isEmpty()) return description;

        // there must be a value in display, unpack it.
        // We do string switch in case of non-numerically-parseable garbage
        switch (display) {

            case "0":   return "";

            case "1":   return "Headlight";
            case "13":  return "Bell";
            case "14":  return "Horn";
            case "15":  return "Whistle";
            case "11":  return "Pantograph";
            case "10":  return "Smoke";
            case "4":   return "Engine";
            case "74":  return "Light";
            case "28":  return "Coupler Clank";
            case "122": return "Couple";
            case "9":   return "Uncouple";

            case "7":   return "Shunting Mode";
            case "8":   return "Momentum";

            case "57":  return "Brake";
            case "200": return "Brake Release";
            case "41":  return "Dynamic Brake";
            case "31":  return "Manual Notch Down";
            case "30":  return "Manual Notch Up";
            case "69":  return "Reverser";
            case "100": return "Mute";

            case "12":  return "Far Light";
            case "3":   return "Cab Light";
            case "48":  return "Ditch Lights";
            case "98":  return "Step Lights";
            case "62":  return "Tail Lights";
            case "58":  return "Switching Lights";
            case "51":  return "Dimmer";
            case "2":   return "Interior Lights";

            case "42":  return "Air Compressor";
            case "45":  return "Air Pump";
            case "60":  return "Injector";
            case "108": return "Exhaust Fan";
            case "17":  return "Radiator Fan";
            case "66":  return "Steam Generator";
            case "105": return "Blower";
            case "56":  return "Blow Down";
            case "38":  return "Safety";
            case "55":  return "Sanding";
            case "88":  return "Ash Dump";
            case "18":  return "Shoveling";
            case "35":  return "Water Fill";

            case "103": return "Long Whistle";
            case "64":  return "Short Whistle";
            case "63":  return "Doppler Horn";

            case "36":  return "Curve Squeal";
            case "21":  return "Brake Squeal";
            case "6":   return "Announce";
            case "27":  return "Cab Chatter";

            case "255": return "Unavailable_";

            default:    return "<entry error \""+display+"\">";
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TcsImporter.class);
}
