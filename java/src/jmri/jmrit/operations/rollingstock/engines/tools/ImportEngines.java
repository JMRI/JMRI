package jmri.jmrit.operations.rollingstock.engines.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import javax.swing.JOptionPane;
import jmri.InstanceManager;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.ImportRollingStock;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.engines.Consist;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.rollingstock.engines.EngineModels;
import jmri.jmrit.operations.setup.Control;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This routine will import engines into the operation database.
 *
 * Each field is space or comma delimited. Field order: Number Road Model Length
 * Owner Built Location - Track
 * 
 * @author Dan Boudreau Copyright (C) 2008, 2013
 */
public class ImportEngines extends ImportRollingStock {

    private static final String DEFAULT_ENGINE_TYPE = Bundle.getMessage("engineDefaultType");
    private static final String DEFAULT_ENGINE_HP = Bundle.getMessage("engineDefaultHp");

    private static final int ENG_NUMBER = 0;
    private static final int ENG_ROAD = 1;
    private static final int ENG_MODEL = 2;
    private static final int ENG_LENGTH = 3;

    private static final int ENG_OWNER = 4;
    private static final int ENG_BUILT = 5;
    private static final int ENG_LOCATION = 6;
    private static final int ENG_LOCATION_TRACK_SEPARATOR = 7;
    private static final int ENG_TRACK = 8;

    // only for CSV files
    private static final int ENG_CONSIST = 9;
    //private static final int ENG_MOVES = 10;
    //private static final int ENG_VALUE = 12;
    private static final int ENG_HP = 13;
    private static final int ENG_WEIGHT = 14;
    private static final int ENG_TYPE = 15;
   // private static final int ENG_COMMENT = 16;
    private static final int ENG_MISCELLANEOUS = 17;

    // as of 7/23/2018 there were 18 attributes exported by operations
    private static final int MAXIMUM_NUMBER_FIELDS = ENG_MISCELLANEOUS + 1;

    EngineManager engineManager = InstanceManager.getDefault(EngineManager.class);

    // we use a thread so the status frame will work!
    @Override
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
        String engineConsistName = "";
        String engineHp = "";
        String engineWeightTons = "";
        String engineType = "";
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
            log.debug("Import: {}", line);
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
                inputLine = parseCommaLine(line, MAXIMUM_NUMBER_FIELDS);
            } else {
                inputLine = line.split("\\s+"); // NOI18N
            }
            if (inputLine.length < 1 || line.equals("")) {
                log.debug("Skipping blank line");
                continue;
            }
            int base = 1;
            if (comma || !inputLine[0].equals("")) {
                base--; // skip over any spaces at start of line
            }

            if (inputLine.length > base + 3) {
                engineNumber = inputLine[base + ENG_NUMBER];
                engineRoad = inputLine[base + ENG_ROAD];
                engineModel = inputLine[base + ENG_MODEL];
                engineLength = inputLine[base + ENG_LENGTH];
                engineOwner = "";
                engineBuilt = "";
                engineLocation = "";
                engineTrack = "";
                engineConsistName = "";
                engineHp = "";
                engineWeightTons = "";
                engineType = "";

                log.debug("Checking engine number ({}) road ({}) model ({}) length ({})", engineNumber, engineRoad,
                        engineModel, engineLength); // NOI18N
                if (engineNumber.trim().equals("")) {
                    log.info("Import line {} missing engine number", lineNum);
                    break;
                }
                if (engineRoad.trim().equals("")) {
                    log.info("Import line {} missing engine road", lineNum);
                    break;
                }
                if (engineModel.trim().equals("")) {
                    log.info("Import line {} missing engine model", lineNum);
                    break;
                }
                if (engineLength.trim().equals("")) {
                    log.info("Import line {} missing engine length", lineNum);
                    break;
                }
                if (engineNumber.length() > Control.max_len_string_road_number) {
                    JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle
                            .getMessage("EngineRoadNumberTooLong"),
                            new Object[]{(engineRoad + " " + engineNumber),
                                    engineNumber}),
                            Bundle.getMessage("RoadNumMustBeLess"), JOptionPane.ERROR_MESSAGE);
                    break;
                }
                if (engineRoad.length() > Control.max_len_string_attibute) {
                    JOptionPane.showMessageDialog(null, MessageFormat.format(
                            Bundle.getMessage("EngineRoadNameTooLong"), new Object[]{
                                    (engineRoad + " " + engineNumber), engineRoad}),
                            MessageFormat.format(Bundle
                                    .getMessage("engineAttribute"), new Object[]{Control.max_len_string_attibute}),
                            JOptionPane.ERROR_MESSAGE);
                    break;
                }
                if (engineModel.length() > Control.max_len_string_attibute) {
                    JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle
                            .getMessage("EngineModelNameTooLong"),
                            new Object[]{(engineRoad + " " + engineNumber),
                                    engineModel}),
                            MessageFormat.format(Bundle.getMessage("engineAttribute"),
                                    new Object[]{Control.max_len_string_attibute}),
                            JOptionPane.ERROR_MESSAGE);
                    break;
                }
                if (!InstanceManager.getDefault(EngineModels.class).containsName(engineModel)) {
                    int results = JOptionPane.showConfirmDialog(null, Bundle.getMessage("Engine") +
                            " (" +
                            engineRoad +
                            " " +
                            engineNumber +
                            ")" +
                            NEW_LINE +
                            MessageFormat
                                    .format(Bundle.getMessage("modelNameNotExist"), new Object[]{engineModel}),
                            Bundle.getMessage("engineAddModel"), JOptionPane.YES_NO_CANCEL_OPTION);
                    if (results == JOptionPane.YES_OPTION) {
                        InstanceManager.getDefault(EngineModels.class).addName(engineModel);
                    } else if (results == JOptionPane.CANCEL_OPTION) {
                        break;
                    }
                }
                if (engineLength.length() > Control.max_len_string_length_name) {
                    JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle
                            .getMessage("EngineLengthNameTooLong"),
                            new Object[]{(engineRoad + " " + engineNumber),
                                    engineLength}),
                            MessageFormat.format(Bundle.getMessage("engineAttribute"),
                                    new Object[]{Control.max_len_string_length_name}),
                            JOptionPane.ERROR_MESSAGE);
                    break;
                }
                try {
                    Integer.parseInt(engineLength);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle
                            .getMessage("EngineLengthNameNotNumber"),
                            new Object[]{(engineRoad + " " + engineNumber),
                                    engineLength}),
                            Bundle.getMessage("EngineLengthMissing"), JOptionPane.ERROR_MESSAGE);
                    break;
                }
                Engine e = engineManager.getByRoadAndNumber(engineRoad, engineNumber);
                if (e != null) {
                    log.info("Can not add engine number ({}) road ({}) it already exists!", engineNumber, engineRoad); // NOI18N
                    continue;
                }

                if (inputLine.length > base + ENG_OWNER) {
                    engineOwner = inputLine[base + ENG_OWNER];
                    if (engineOwner.length() > Control.max_len_string_attibute) {
                        JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle
                                .getMessage("EngineOwnerNameTooLong"),
                                new Object[]{
                                        (engineRoad + " " + engineNumber), engineOwner}),
                                MessageFormat.format(Bundle
                                        .getMessage("engineAttribute"),
                                        new Object[]{Control.max_len_string_attibute}),
                                JOptionPane.ERROR_MESSAGE);
                        break;
                    }
                }
                if (inputLine.length > base + ENG_BUILT) {
                    engineBuilt = inputLine[base + ENG_BUILT];
                    if (engineBuilt.length() > Control.max_len_string_built_name) {
                        JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle
                                .getMessage("EngineBuiltDateTooLong"),
                                new Object[]{
                                        (engineRoad + " " + engineNumber), engineBuilt}),
                                MessageFormat.format(Bundle.getMessage("engineAttribute"),
                                        new Object[]{Control.max_len_string_built_name}),
                                JOptionPane.ERROR_MESSAGE);
                        break;
                    }
                }
                if (inputLine.length > base + ENG_LOCATION) {
                    engineLocation = inputLine[base + ENG_LOCATION];
                }
                if (inputLine.length > base + ENG_TRACK) {
                    engineTrack = inputLine[base + ENG_TRACK];
                }
                // Location name can be one to three words
                if (!comma && inputLine.length > base + ENG_LOCATION_TRACK_SEPARATOR) {
                    if (!inputLine[base + ENG_LOCATION_TRACK_SEPARATOR].equals(LOCATION_TRACK_SEPARATOR)) {
                        engineLocation = engineLocation + " " + inputLine[base + ENG_LOCATION_TRACK_SEPARATOR];
                        if (inputLine.length > base + ENG_LOCATION_TRACK_SEPARATOR + 1) {
                            if (!inputLine[base + ENG_LOCATION_TRACK_SEPARATOR + 1].equals(LOCATION_TRACK_SEPARATOR)) {
                                engineLocation = engineLocation + " " + inputLine[base + ENG_LOCATION_TRACK_SEPARATOR + 1];
                            }
                        }
                    }
                    // get track name if there's one
                    boolean foundDash = false;
                    for (int i = base + ENG_LOCATION_TRACK_SEPARATOR; i < inputLine.length; i++) {
                        if (inputLine[i].equals(LOCATION_TRACK_SEPARATOR)) {
                            foundDash = true;
                            if (inputLine.length > i + 1) {
                                engineTrack = inputLine[++i];
                            }
                        } else if (foundDash) {
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
                            .getMessage("EngineLocationNameTooLong"),
                            new Object[]{
                                    (engineRoad + " " + engineNumber), engineLocation}),
                            MessageFormat.format(Bundle
                                    .getMessage("engineAttribute"),
                                    new Object[]{Control.max_len_string_location_name}),
                            JOptionPane.ERROR_MESSAGE);
                    break;
                }
                if (engineTrack.length() > Control.max_len_string_track_name) {
                    JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle
                            .getMessage("EngineTrackNameTooLong"),
                            new Object[]{
                                    (engineRoad + " " + engineNumber), engineTrack}),
                            MessageFormat.format(Bundle
                                    .getMessage("engineAttribute"),
                                    new Object[]{Control.max_len_string_track_name}),
                            JOptionPane.ERROR_MESSAGE);
                    break;
                }
                Location location = InstanceManager.getDefault(LocationManager.class).getLocationByName(engineLocation);
                Track track = null;
                if (location == null && !engineLocation.equals("")) {
                    JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle
                            .getMessage("EngineLocationDoesNotExist"),
                            new Object[]{
                                    (engineRoad + " " + engineNumber), engineLocation}),
                            Bundle
                                    .getMessage("engineLocation"),
                            JOptionPane.ERROR_MESSAGE);
                    int results = JOptionPane.showConfirmDialog(null, MessageFormat.format(Bundle
                            .getMessage("DoYouWantToCreateLoc"), new Object[]{engineLocation}), Bundle
                                    .getMessage("engineLocation"),
                            JOptionPane.YES_NO_OPTION);
                    if (results == JOptionPane.YES_OPTION) {
                        log.debug("Create location ({})", engineLocation);
                        location = InstanceManager.getDefault(LocationManager.class).newLocation(engineLocation);
                    } else {
                        break;
                    }
                }
                if (location != null && !engineTrack.equals("")) {
                    track = location.getTrackByName(engineTrack, null);
                    if (track == null) {
                        JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle
                                .getMessage("EngineTrackDoesNotExist"),
                                new Object[]{
                                        (engineRoad + " " + engineNumber), engineTrack, engineLocation}),
                                Bundle
                                        .getMessage("engineTrack"),
                                JOptionPane.ERROR_MESSAGE);
                        int results = JOptionPane.showConfirmDialog(null, MessageFormat
                                .format(Bundle.getMessage("DoYouWantToCreateTrack"), new Object[]{engineTrack,
                                        engineLocation}),
                                Bundle.getMessage("engineTrack"),
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
                // check for consist name
                if (comma && inputLine.length > base + ENG_CONSIST) {
                    engineConsistName = inputLine[ENG_CONSIST];
                    log.debug("Consist name ({})", engineConsistName);
                }
                // check for HP
                if (comma && inputLine.length > base + ENG_HP) {
                    engineHp = inputLine[ENG_HP];
                    log.debug("Engine HP ({})", engineHp);
                }
                // check for engine weight tons
                if (comma && inputLine.length > base + ENG_WEIGHT) {
                    engineWeightTons = inputLine[ENG_WEIGHT];
                    log.debug("Engine weight tons ({})", engineWeightTons);
                }
                // check for engine type
                if (comma && inputLine.length > base + ENG_TYPE) {
                    engineType = inputLine[ENG_TYPE];
                    log.debug("Engine type ({})", engineType);
                }
                log.debug("Add engine ({} {}) owner ({}) built ({}) location ({}, {})", engineRoad, engineNumber,
                        engineOwner, engineBuilt, engineLocation, engineTrack);
                Engine engine = engineManager.newRS(engineRoad, engineNumber);
                engine.setModel(engineModel);
                engine.setLength(engineLength);
                // does this model already have a type?
                if (engine.getTypeName().equals(Engine.NONE)) {
                    if (!engineType.equals("")) {
                        engine.setTypeName(engineType);
                    } else {
                        engine.setTypeName(DEFAULT_ENGINE_TYPE);
                    }
                }
                // does this model already have a HP?
                if (engine.getHp().equals(Engine.NONE)) {
                    if (!engineHp.equals("")) {
                        engine.setHp(engineHp);
                    } else {
                        engine.setHp(DEFAULT_ENGINE_HP);
                    }
                }
                // does this model already have a weight in tons?
                if (engine.getWeightTons().equals(Engine.NONE)) {
                    engine.setWeightTons(engineWeightTons);
                }
                engine.setOwner(engineOwner);
                engine.setBuilt(engineBuilt);
                // consist?
                if (!engineConsistName.equals("")) {
                    Consist consist = engineManager.newConsist(engineConsistName);
                    engine.setConsist(consist);
                }

                enginesAdded++;

                if (location != null && track != null) {
                    String status = engine.setLocation(location, track);
                    if (!status.equals(Track.OKAY)) {
                        log.debug("Can't set engine's location because of {}", status);
                        JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle
                                .getMessage("CanNotSetEngineAtLocation"),
                                new Object[]{(engineRoad + " " + engineNumber), engineModel, engineLocation,
                                        engineTrack, status}),
                                Bundle.getMessage("rsCanNotLoc"),
                                JOptionPane.ERROR_MESSAGE);
                        if (status.startsWith(Track.TYPE)) {
                            int results = JOptionPane.showConfirmDialog(null, MessageFormat.format(Bundle
                                    .getMessage("DoYouWantToAllowService"),
                                    new Object[]{engineLocation,
                                            engineTrack, (engineRoad + " " + engineNumber), engine.getTypeName()}),
                                    Bundle
                                            .getMessage("ServiceEngineType"),
                                    JOptionPane.YES_NO_OPTION);
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
                                            .getMessage("TrackLength"),
                                    JOptionPane.YES_NO_OPTION);
                            if (results == JOptionPane.YES_OPTION) {
                                track.setLength(track.getLength() + 1000);
                                status = engine.setLocation(location, track);
                            } else {
                                break;
                            }
                        }
                        if (!status.equals(Track.OKAY)) {
                            int results = JOptionPane.showConfirmDialog(null, MessageFormat.format(Bundle
                                    .getMessage("DoYouWantToForceEngine"),
                                    new Object[]{
                                            (engineRoad + " " + engineNumber), engineLocation, engineTrack}),
                                    Bundle
                                            .getMessage("OverRide"),
                                    JOptionPane.YES_NO_OPTION);
                            if (results == JOptionPane.YES_OPTION) {
                                engine.setLocation(location, track, RollingStock.FORCE); // force engine
                            } else {
                                break;
                            }
                        }
                    }
                } else {
                    // log.debug("No location for engine ("+engineRoad+" "+engineNumber+")");
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

        if (importOkay) {
            JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle.getMessage("ImportEnginesAdded"),
                    new Object[]{enginesAdded}), Bundle.getMessage("SuccessfulImport"),
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle.getMessage("ImportEnginesAdded"),
                    new Object[]{enginesAdded}), Bundle.getMessage("ImportFailed"), JOptionPane.ERROR_MESSAGE);
        }
        
        // kill status panel
        fstatus.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(ImportEngines.class);
}
