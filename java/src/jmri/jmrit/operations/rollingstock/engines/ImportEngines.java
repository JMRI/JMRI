// ImportEngines.java
package jmri.jmrit.operations.rollingstock.engines;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import javax.swing.JOptionPane;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.ImportRollingStock;
import jmri.jmrit.operations.setup.Control;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This routine will import engines into the operation database.
 *
 * Each field is space or comma delimited. Field order: Number Road Type Length
 * Owner Year Location
 *
 * @author Dan Boudreau Copyright (C) 2008, 2013
 * @version $Revision$
 */
public class ImportEngines extends ImportRollingStock {

    private static String defaultEngineType = Bundle.getMessage("engineDefaultType");
    private static String defaultEngineHp = Bundle.getMessage("engineDefaultHp");

    EngineManager manager = EngineManager.instance();

    // we use a thread so the status frame will work!
    public void run() {
        File file = getFile();
        if (file == null) {
            return;
        }
        BufferedReader in = getBufferedReader(file);
        if (in == null) {
            return;
        }

        createStatusFrame(Bundle.getMessage("ImportEngines"));

        // Now read the input file
        boolean importOkay = false;
        boolean comma = false;
        int lineNum = 0;
        int enginesAdded = 0;
        String line = " ";
        String engineNumber;
        String engineRoad;
        String engineModel;
        String engineLength;
        String engineOwner = "";
        String engineBuilt = "";
        String engineLocation = "";
        String engineTrack = "";
        String[] inputLine;

        // does the file name end with .csv?
        if (file.getAbsolutePath().endsWith(".csv")) { // NOI18N
            log.info("Using comma as delimiter for import engines");
            comma = true;
        }

        while (true) {
            lineNumber.setText(Bundle.getMessage("LineNumber") + " " + Integer.toString(++lineNum));
            try {
                line = in.readLine();
            } catch (IOException e) {
                break;
            }

            if (line == null) {
                importOkay = true;
                break;
            }

            // has user canceled import?
            if (!fstatus.isShowing()) {
                break;
            }

            line = line.trim();
            if (log.isDebugEnabled()) {
                log.debug("Import: {}", line);
            }
            importLine.setText(line);

            if (line.startsWith(Bundle.getMessage("Number"))) {
                continue; // skip header
            }
            if (line.equalsIgnoreCase("comma")) { // NOI18N
                log.info("Using comma as delimiter for import engines");
                comma = true;
                continue;
            }
            // use comma as delimiter if found otherwise use spaces
            if (comma) {
                inputLine = parseCommaLine(line, 9);
            } else {
                inputLine = line.split("\\s+"); // NOI18N
            }
            if (inputLine.length < 1 || line.equals("")) {
                log.debug("Skipping blank line");
                continue;
            }
            int base = 0;
            if (comma || !inputLine[0].equals("")) {
                base--; // skip over any spaces at start of line
            }

            if (inputLine.length > base + 4) {
                engineNumber = inputLine[base + 1];
                engineRoad = inputLine[base + 2];
                engineModel = inputLine[base + 3];
                engineLength = inputLine[base + 4];
                engineOwner = "";
                engineBuilt = "";
                engineLocation = "";
                engineTrack = "";

                log.debug("Checking engine number ({}) road ({}) model ({}) length ({})", engineNumber, engineRoad,
                        engineModel, engineLength); // NOI18N
                if (engineNumber.length() > Control.max_len_string_road_number) {
                    JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle
                            .getMessage("EngineRoadNumberTooLong"), new Object[]{(engineRoad + " " + engineNumber),
                                engineNumber}), Bundle.getMessage("engineRoadNum"), JOptionPane.ERROR_MESSAGE);
                    break;
                }
                if (engineRoad.length() > Control.max_len_string_attibute) {
                    JOptionPane.showMessageDialog(null, MessageFormat.format(
                            Bundle.getMessage("EngineRoadNameTooLong"), new Object[]{
                                (engineRoad + " " + engineNumber), engineRoad}), MessageFormat.format(Bundle
                                    .getMessage("engineAttribute"), new Object[]{Control.max_len_string_attibute}),
                            JOptionPane.ERROR_MESSAGE);
                    break;
                }
                if (engineModel.length() > Control.max_len_string_attibute) {
                    JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle
                            .getMessage("EngineModelNameTooLong"), new Object[]{(engineRoad + " " + engineNumber),
                                engineModel}), MessageFormat.format(Bundle.getMessage("engineAttribute"),
                                    new Object[]{Control.max_len_string_attibute}), JOptionPane.ERROR_MESSAGE);
                    break;
                }
                if (!EngineModels.instance().containsName(engineModel)) {
                    int results = JOptionPane.showConfirmDialog(null, Bundle.getMessage("Engine")
                            + " ("
                            + engineRoad
                            + " "
                            + engineNumber
                            + ")"
                            + NEW_LINE
                            + MessageFormat
                            .format(Bundle.getMessage("modelNameNotExist"), new Object[]{engineModel}),
                            Bundle.getMessage("engineAddModel"), JOptionPane.YES_NO_CANCEL_OPTION);
                    if (results == JOptionPane.YES_OPTION) {
                        EngineModels.instance().addName(engineModel);
                    } else if (results == JOptionPane.CANCEL_OPTION) {
                        break;
                    }
                }
                if (engineLength.length() > Control.max_len_string_length_name) {
                    JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle
                            .getMessage("EngineLengthNameTooLong"), new Object[]{(engineRoad + " " + engineNumber),
                                engineLength}), MessageFormat.format(Bundle.getMessage("engineAttribute"),
                                    new Object[]{Control.max_len_string_length_name}), JOptionPane.ERROR_MESSAGE);
                    break;
                }
                try {
                    Integer.parseInt(engineLength);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle
                            .getMessage("EngineLengthNameNotNumber"), new Object[]{(engineRoad + " " + engineNumber),
                                engineLength}), Bundle.getMessage("EngineLengthMissing"), JOptionPane.ERROR_MESSAGE);
                    break;
                }
                Engine e = manager.getByRoadAndNumber(engineRoad, engineNumber);
                if (e != null) {
                    log.info("Can not add, engine number (" + engineNumber + ") road (" + engineRoad
                            + ") already exists"); // NOI18N
                } else {

                    if (inputLine.length > base + 5) {
                        engineOwner = inputLine[base + 5];
                        if (engineOwner.length() > Control.max_len_string_attibute) {
                            JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle
                                    .getMessage("EngineOwnerNameTooLong"), new Object[]{
                                        (engineRoad + " " + engineNumber), engineOwner}), MessageFormat.format(Bundle
                                            .getMessage("engineAttribute"), new Object[]{Control.max_len_string_attibute}),
                                    JOptionPane.ERROR_MESSAGE);
                            break;
                        }
                    }
                    if (inputLine.length > base + 6) {
                        engineBuilt = inputLine[base + 6];
                        if (engineBuilt.length() > Control.max_len_string_built_name) {
                            JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle
                                    .getMessage("EngineBuiltDateTooLong"), new Object[]{
                                        (engineRoad + " " + engineNumber), engineBuilt}),
                                    MessageFormat.format(Bundle.getMessage("engineAttribute"),
                                            new Object[]{Control.max_len_string_built_name}),
                                    JOptionPane.ERROR_MESSAGE);
                            break;
                        }
                    }
                    if (inputLine.length > base + 7) {
                        engineLocation = inputLine[base + 7];
                    }
                    // Location name can be one to three words
                    if (inputLine.length > base + 8) {
                        if (!inputLine[base + 8].equals("-")) {
                            engineLocation = engineLocation + " " + inputLine[base + 8];
                            if (inputLine.length > base + 9) {
                                if (!inputLine[base + 9].equals("-")) {
                                    engineLocation = engineLocation + " " + inputLine[base + 9];
                                }
                            }
                            // get track name if there's one
                        }
                        boolean foundDash = false;
                        for (int i = base + 8; i < inputLine.length; i++) {
                            if (inputLine[i].equals("-")) {
                                foundDash = true;
                                if (inputLine.length > i + 1) {
                                    engineTrack = inputLine[++i];
                                }
                            } else if (foundDash && !comma) {
                                engineTrack = engineTrack + " " + inputLine[i];
                            }
                        }
                        if (engineTrack == null) {
                            engineTrack = "";
                        }
                        log.debug("Engine ({} {}) has track ({})", engineRoad, engineNumber, engineTrack);
                    }

                    if (engineLocation.length() > Control.max_len_string_location_name) {
                        JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle
                                .getMessage("EngineLocationNameTooLong"), new Object[]{
                                    (engineRoad + " " + engineNumber), engineLocation}), MessageFormat.format(Bundle
                                        .getMessage("engineAttribute"), new Object[]{Control.max_len_string_location_name}),
                                JOptionPane.ERROR_MESSAGE);
                        break;
                    }
                    if (engineTrack.length() > Control.max_len_string_track_name) {
                        JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle
                                .getMessage("EngineTrackNameTooLong"), new Object[]{
                                    (engineRoad + " " + engineNumber), engineTrack}), MessageFormat.format(Bundle
                                        .getMessage("engineAttribute"), new Object[]{Control.max_len_string_track_name}),
                                JOptionPane.ERROR_MESSAGE);
                        break;
                    }
                    Location location = LocationManager.instance().getLocationByName(engineLocation);
                    Track track = null;
                    if (location == null && !engineLocation.equals("")) {
                        JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle
                                .getMessage("EngineLocationDoesNotExist"), new Object[]{
                                    (engineRoad + " " + engineNumber), engineLocation}), Bundle
                                .getMessage("engineLocation"), JOptionPane.ERROR_MESSAGE);
                        int results = JOptionPane.showConfirmDialog(null, MessageFormat.format(Bundle
                                .getMessage("DoYouWantToCreateLoc"), new Object[]{engineLocation}), Bundle
                                .getMessage("engineLocation"), JOptionPane.YES_NO_OPTION);
                        if (results == JOptionPane.YES_OPTION) {
                            log.debug("Create location ({})", engineLocation);
                            location = LocationManager.instance().newLocation(engineLocation);
                        } else {
                            break;
                        }
                    }
                    if (location != null && !engineTrack.equals("")) {
                        track = location.getTrackByName(engineTrack, null);
                        if (track == null) {
                            JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle
                                    .getMessage("EngineTrackDoesNotExist"), new Object[]{
                                        (engineRoad + " " + engineNumber), engineTrack, engineLocation}), Bundle
                                    .getMessage("engineTrack"), JOptionPane.ERROR_MESSAGE);
                            int results = JOptionPane.showConfirmDialog(null, MessageFormat
                                    .format(Bundle.getMessage("DoYouWantToCreateTrack"), new Object[]{engineTrack,
                                        engineLocation}), Bundle.getMessage("engineTrack"),
                                    JOptionPane.YES_NO_OPTION);
                            if (results == JOptionPane.YES_OPTION) {
                                if (location.getLocationOps() == Location.NORMAL) {
                                    log.debug("Create 1000 foot yard track ({})", engineTrack);
                                    track = location.addTrack(engineTrack, Track.YARD);
                                } else {
                                    log.debug("Create 1000 foot staging track ({})", engineTrack);
                                    track = location.addTrack(engineTrack, Track.STAGING);
                                }
                                track.setLength(1000);
                            } else {
                                break;
                            }
                        }
                    }
                    log.debug("Add engine ({} {}) owner ({}) built ({}) location ({}, {})", engineRoad, engineNumber,
                            engineOwner, engineBuilt, engineLocation, engineTrack);
                    Engine engine = manager.newEngine(engineRoad, engineNumber);
                    engine.setModel(engineModel);
                    engine.setLength(engineLength);
                    // does this model already have a type?
                    if (engine.getTypeName().equals(Engine.NONE)) {
                        engine.setTypeName(defaultEngineType);
                    }
                    // does this model already have a hp?
                    if (engine.getHp().equals(Engine.NONE)) {
                        engine.setHp(defaultEngineHp);
                    }
                    engine.setOwner(engineOwner);
                    engine.setBuilt(engineBuilt);
                    enginesAdded++;

                    if (location != null && track != null) {
                        String status = engine.setLocation(location, track);
                        if (!status.equals(Track.OKAY)) {
                            log.debug("Can't set engine's location because of {}", status);
                            JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle
                                    .getMessage("CanNotSetEngineAtLocation"),
                                    new Object[]{(engineRoad + " " + engineNumber), engineModel, engineLocation,
                                        engineTrack, status}), Bundle.getMessage("rsCanNotLoc"),
                                    JOptionPane.ERROR_MESSAGE);
                            if (status.startsWith(Track.TYPE)) {
                                int results = JOptionPane.showConfirmDialog(null, MessageFormat.format(Bundle
                                        .getMessage("DoYouWantToAllowService"), new Object[]{engineLocation,
                                            engineTrack, (engineRoad + " " + engineNumber), engine.getTypeName()}), Bundle
                                        .getMessage("ServiceEngineType"), JOptionPane.YES_NO_OPTION);
                                if (results == JOptionPane.YES_OPTION) {
                                    location.addTypeName(engine.getTypeName());
                                    track.addTypeName(engine.getTypeName());
                                    status = engine.setLocation(location, track);
                                } else {
                                    break;
                                }
                            }
                            if (status.startsWith(Track.LENGTH)) {
                                int results = JOptionPane.showConfirmDialog(null, MessageFormat.format(Bundle
                                        .getMessage("DoYouWantIncreaseLength"), new Object[]{engineTrack}), Bundle
                                        .getMessage("TrackLength"), JOptionPane.YES_NO_OPTION);
                                if (results == JOptionPane.YES_OPTION) {
                                    track.setLength(track.getLength() + 1000);
                                    status = engine.setLocation(location, track);
                                } else {
                                    break;
                                }
                            }
                            if (!status.equals(Track.OKAY)) {
                                int results = JOptionPane.showConfirmDialog(null, MessageFormat.format(Bundle
                                        .getMessage("DoYouWantToForceEngine"), new Object[]{
                                            (engineRoad + " " + engineNumber), engineLocation, engineTrack}), Bundle
                                        .getMessage("OverRide"), JOptionPane.YES_NO_OPTION);
                                if (results == JOptionPane.YES_OPTION) {
                                    engine.setLocation(location, track, true); // force engine
                                } else {
                                    break;
                                }
                            }
                        }
                    } else {
                        // log.debug("No location for engine ("+engineRoad+" "+engineNumber+")");
                    }
                }
            } else if (!line.equals("")) {
                log.info("Engine import line " + lineNum + " missing attributes: " + line);
                JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle.getMessage("ImportMissingAttributes"),
                        new Object[]{lineNum}), Bundle.getMessage("EngineAttributeMissing"),
                        JOptionPane.ERROR_MESSAGE);
                break;
            }
        }
        try {
            in.close();
        } catch (IOException e) {
        }

        // kill status panel
        fstatus.dispose();

        if (importOkay) {
            JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle.getMessage("ImportEnginesAdded"),
                    new Object[]{enginesAdded}), Bundle.getMessage("SuccessfulImport"),
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle.getMessage("ImportEnginesAdded"),
                    new Object[]{enginesAdded}), Bundle.getMessage("ImportFailed"), JOptionPane.ERROR_MESSAGE);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(ImportEngines.class.getName());
}
