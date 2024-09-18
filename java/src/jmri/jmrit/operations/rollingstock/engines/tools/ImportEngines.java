package jmri.jmrit.operations.rollingstock.engines.tools;

import java.io.*;

import jmri.InstanceManager;
import jmri.jmrit.operations.locations.*;
import jmri.jmrit.operations.rollingstock.ImportRollingStock;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.engines.*;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.trains.TrainCommon;
import jmri.util.swing.JmriJOptionPane;

/**
 * This routine will import engines into the operation database. Each field is
 * space or comma delimited. Field order: Number Road Model Length Owner Built
 * Location - Track Consist HP WeightTons Type
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
    // private static final int ENG_MOVES = 10;
    // private static final int ENG_VALUE = 12;
    private static final int ENG_HP = 13;
    private static final int ENG_WEIGHT = 14;
    private static final int ENG_TYPE = 15;
    // private static final int ENG_COMMENT = 16;
    // private static final int ENG_MISCELLANEOUS = 17;

    // as of 7/23/2018 there were 18 attributes exported by operations

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
        String engineLocationName = "";
        String engineTrackName = "";
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
                inputLine = parseCommaLine(line);
            } else {
                inputLine = line.split("\\s+"); // NOI18N
            }
            if (inputLine.length < 1 || line.isEmpty()) {
                log.debug("Skipping blank line");
                continue;
            }
            int base = 1;
            if (comma || !inputLine[0].isEmpty()) {
                base--; // skip over any spaces at start of line
            }

            if (inputLine.length > base + 3) {
                engineNumber = inputLine[base + ENG_NUMBER].trim();
                engineRoad = inputLine[base + ENG_ROAD].trim();
                engineModel = inputLine[base + ENG_MODEL].trim();
                engineLength = inputLine[base + ENG_LENGTH].trim();
                engineOwner = "";
                engineBuilt = "";
                engineLocationName = "";
                engineTrackName = "";
                engineConsistName = "";
                engineHp = "";
                engineWeightTons = "";
                engineType = "";

                log.debug("Checking engine number ({}) road ({}) model ({}) length ({})", engineNumber, engineRoad,
                        engineModel, engineLength); // NOI18N
                if (engineNumber.isEmpty()) {
                    log.info("Import line {} missing engine number", lineNum);
                    JmriJOptionPane.showMessageDialog(null, 
                            Bundle.getMessage("RoadNumberNotSpecified", lineNum),
                            Bundle.getMessage("RoadNumberMissing"), JmriJOptionPane.ERROR_MESSAGE);
                    break;
                }
                if (engineRoad.isEmpty()) {
                    log.info("Import line {} missing engine road", lineNum);
                    JmriJOptionPane.showMessageDialog(null, 
                            Bundle.getMessage("RoadNameNotSpecified", lineNum),
                            Bundle.getMessage("RoadNameMissing"), JmriJOptionPane.ERROR_MESSAGE);
                    break;
                }
                if (engineModel.isEmpty()) {
                    log.info("Import line {} missing engine model", lineNum);
                    JmriJOptionPane.showMessageDialog(null, 
                            Bundle.getMessage("EngineModelNotSpecified", lineNum),
                            Bundle.getMessage("EngineModelMissing"), JmriJOptionPane.ERROR_MESSAGE);
                    break;
                }
                if (engineLength.isEmpty()) {
                    log.info("Import line {} missing engine length", lineNum);
                    JmriJOptionPane.showMessageDialog(null, 
                            Bundle.getMessage("EngineLengthNotSpecified", lineNum),
                            Bundle.getMessage("EngineLengthMissing"), JmriJOptionPane.ERROR_MESSAGE);
                    break;
                }
                if (TrainCommon.splitString(engineNumber).length() > Control.max_len_string_road_number) {
                    JmriJOptionPane.showMessageDialog(
                            null, Bundle.getMessage("EngineRoadNumberTooLong",
                                    engineRoad, engineNumber, engineNumber),
                            Bundle.getMessage("RoadNumMustBeLess"), JmriJOptionPane.ERROR_MESSAGE);
                    break;
                }
                if (engineRoad.length() > Control.max_len_string_attibute) {
                    JmriJOptionPane.showMessageDialog(null, 
                            Bundle.getMessage("EngineRoadNameTooLong",
                                    engineRoad, engineNumber, engineRoad),
                            Bundle.getMessage("engineAttribute",
                                    Control.max_len_string_attibute),
                            JmriJOptionPane.ERROR_MESSAGE);
                    break;
                }
                if (engineModel.length() > Control.max_len_string_attibute) {
                    JmriJOptionPane.showMessageDialog(
                            null, Bundle.getMessage("EngineModelNameTooLong",
                                    engineRoad, engineNumber, engineModel),
                            Bundle.getMessage("engineAttribute",
                                    Control.max_len_string_attibute),
                            JmriJOptionPane.ERROR_MESSAGE);
                    break;
                }
                if (!InstanceManager.getDefault(EngineModels.class).containsName(engineModel)) {
                    int results = JmriJOptionPane.showConfirmDialog(null, Bundle.getMessage("Engine") +
                            " (" +
                            engineRoad +
                            " " +
                            engineNumber +
                            ")" +
                            NEW_LINE +
                            Bundle.getMessage("modelNameNotExist", engineModel),
                            Bundle.getMessage("engineAddModel"), JmriJOptionPane.YES_NO_CANCEL_OPTION);
                    if (results == JmriJOptionPane.YES_OPTION) {
                        InstanceManager.getDefault(EngineModels.class).addName(engineModel);
                    } else if (results == JmriJOptionPane.CANCEL_OPTION ) {
                        break;
                    }
                }
                if (engineLength.length() > Control.max_len_string_length_name) {
                    JmriJOptionPane.showMessageDialog(
                            null, Bundle.getMessage("EngineLengthNameTooLong",
                                    engineRoad, engineNumber, engineLength),
                            Bundle.getMessage("engineAttribute",
                                    Control.max_len_string_length_name),
                            JmriJOptionPane.ERROR_MESSAGE);
                    break;
                }
                try {
                    Integer.parseInt(engineLength);
                } catch (NumberFormatException e) {
                    JmriJOptionPane.showMessageDialog(
                            null, Bundle.getMessage("EngineLengthNameNotNumber",
                                    engineRoad, engineNumber, engineLength),
                            Bundle.getMessage("EngineLengthMissing"), JmriJOptionPane.ERROR_MESSAGE);
                    break;
                }
                Engine e = engineManager.getByRoadAndNumber(engineRoad, engineNumber);
                if (e != null) {
                    log.info("Can not add engine number ({}) road ({}) it already exists!", engineNumber, engineRoad); // NOI18N
                    continue;
                }

                if (inputLine.length > base + ENG_OWNER) {
                    engineOwner = inputLine[base + ENG_OWNER].trim();
                    if (engineOwner.length() > Control.max_len_string_attibute) {
                        JmriJOptionPane.showMessageDialog(null,
                                Bundle.getMessage("EngineOwnerNameTooLong",
                                        engineRoad, engineNumber, engineOwner),
                                Bundle.getMessage("engineAttribute",
                                        Control.max_len_string_attibute),
                                JmriJOptionPane.ERROR_MESSAGE);
                        break;
                    }
                }
                if (inputLine.length > base + ENG_BUILT) {
                    engineBuilt = inputLine[base + ENG_BUILT].trim();
                    if (engineBuilt.length() > Control.max_len_string_built_name) {
                        JmriJOptionPane.showMessageDialog(null,
                                Bundle.getMessage("EngineBuiltDateTooLong",
                                        engineRoad, engineNumber, engineBuilt),
                                Bundle.getMessage("engineAttribute",
                                        Control.max_len_string_built_name),
                                JmriJOptionPane.ERROR_MESSAGE);
                        break;
                    }
                }
                if (inputLine.length > base + ENG_LOCATION) {
                    engineLocationName = inputLine[base + ENG_LOCATION].trim();
                }
                if (comma && inputLine.length > base + ENG_TRACK) {
                    engineTrackName = inputLine[base + ENG_TRACK].trim();
                }
                // Location and track name can be one or more words in a
                // space delimited file
                if (!comma) {
                    int j = 0;
                    StringBuffer name = new StringBuffer(engineLocationName);
                    for (int i = base + ENG_LOCATION_TRACK_SEPARATOR; i < inputLine.length; i++) {
                        if (inputLine[i].equals(LOCATION_TRACK_SEPARATOR)) {
                            j = i + 1; // skip over separator
                            break;
                        } else {
                            name.append(" " + inputLine[i]);
                        }
                    }
                    engineLocationName = name.toString();
                    log.debug("Engine ({} {}) has location ({})", engineRoad, engineNumber, engineLocationName);
                    // now get the track name
                    name = new StringBuffer();
                    if (j != 0 && j < inputLine.length) {
                        name.append(inputLine[j]);
                        for (int i = j + 1; i < inputLine.length; i++) {
                            name.append(" " + inputLine[i]);
                        }
                        log.debug("Engine ({} {}) has track ({})", engineRoad, engineNumber, engineTrackName);
                    }
                    engineTrackName = name.toString();
                }

                if (engineLocationName.length() > Control.max_len_string_location_name) {
                    JmriJOptionPane.showMessageDialog(null,
                            Bundle.getMessage("EngineLocationNameTooLong",
                                    engineRoad, engineNumber, engineLocationName),
                            Bundle.getMessage("engineAttribute",
                                    Control.max_len_string_location_name),
                            JmriJOptionPane.ERROR_MESSAGE);
                    break;
                }
                if (engineTrackName.length() > Control.max_len_string_track_name) {
                    JmriJOptionPane.showMessageDialog(null,
                            Bundle.getMessage("EngineTrackNameTooLong",
                                    engineRoad, engineNumber, engineTrackName),
                            Bundle.getMessage("engineAttribute",
                                    Control.max_len_string_track_name),
                            JmriJOptionPane.ERROR_MESSAGE);
                    break;
                }
                Location location =
                        InstanceManager.getDefault(LocationManager.class).getLocationByName(engineLocationName);
                Track track = null;
                if (location == null && !engineLocationName.isEmpty()) {
                    JmriJOptionPane.showMessageDialog(null,
                            Bundle.getMessage("EngineLocationDoesNotExist",
                                    engineRoad, engineNumber, engineLocationName),
                            Bundle.getMessage("engineLocation"), JmriJOptionPane.ERROR_MESSAGE);
                    int results = JmriJOptionPane.showConfirmDialog(null,
                            Bundle.getMessage("DoYouWantToCreateLoc",
                                    engineLocationName),
                            Bundle.getMessage("engineLocation"), JmriJOptionPane.YES_NO_OPTION);
                    if (results == JmriJOptionPane.YES_OPTION) {
                        log.debug("Create location ({})", engineLocationName);
                        location = InstanceManager.getDefault(LocationManager.class).newLocation(engineLocationName);
                    } else {
                        break;
                    }
                }
                if (location != null && !engineTrackName.isEmpty()) {
                    track = location.getTrackByName(engineTrackName, null);
                    if (track == null) {
                        JmriJOptionPane.showMessageDialog(
                                null, Bundle.getMessage("EngineTrackDoesNotExist",
                                        engineRoad, engineNumber, engineTrackName,
                                                engineLocationName),
                                Bundle.getMessage("engineTrack"), JmriJOptionPane.ERROR_MESSAGE);
                        int results = JmriJOptionPane.showConfirmDialog(null,
                                Bundle.getMessage("DoYouWantToCreateTrack",
                                        engineTrackName, engineLocationName),
                                Bundle.getMessage("engineTrack"), JmriJOptionPane.YES_NO_OPTION);
                        if (results == JmriJOptionPane.YES_OPTION) {
                            if (!location.isStaging()) {
                                log.debug("Create 1000 foot yard track ({})", engineTrackName);
                                track = location.addTrack(engineTrackName, Track.YARD);
                            } else {
                                log.debug("Create 1000 foot staging track ({})", engineTrackName);
                                track = location.addTrack(engineTrackName, Track.STAGING);
                            }
                            track.setLength(1000);
                        } else {
                            break;
                        }
                    }
                }
                // check for consist name
                if (comma && inputLine.length > base + ENG_CONSIST) {
                    engineConsistName = inputLine[ENG_CONSIST].trim();
                    log.debug("Consist name ({})", engineConsistName);
                }
                // check for HP
                if (comma && inputLine.length > base + ENG_HP) {
                    engineHp = inputLine[ENG_HP].trim();
                    log.debug("Engine HP ({})", engineHp);
                }
                // check for engine weight tons
                if (comma && inputLine.length > base + ENG_WEIGHT) {
                    engineWeightTons = inputLine[ENG_WEIGHT].trim();
                    log.debug("Engine weight tons ({})", engineWeightTons);
                }
                // check for engine type
                if (comma && inputLine.length > base + ENG_TYPE) {
                    engineType = inputLine[ENG_TYPE].trim();
                    log.debug("Engine type ({})", engineType);
                }
                log.debug("Add engine ({} {}) owner ({}) built ({}) location ({}, {})", engineRoad, engineNumber,
                        engineOwner, engineBuilt, engineLocationName, engineTrackName);
                Engine engine = engineManager.newRS(engineRoad, engineNumber);
                engine.setModel(engineModel);
                engine.setLength(engineLength);
                // does this model already have a type?
                if (engine.getTypeName().equals(Engine.NONE)) {
                    if (!engineType.isEmpty()) {
                        engine.setTypeName(engineType);
                    } else {
                        engine.setTypeName(DEFAULT_ENGINE_TYPE);
                    }
                }
                // does this model already have a HP?
                if (engine.getHp().equals(Engine.NONE)) {
                    if (!engineHp.isEmpty()) {
                        engine.setHp(engineHp);
                    } else {
                        engine.setHp(DEFAULT_ENGINE_HP);
                    }
                }
                // does this model already have a weight in tons?
                if (engine.getWeightTons().equals(Engine.NONE)) {
                    engine.setWeightTons(engineWeightTons);
                }
                engine.setOwnerName(engineOwner);
                engine.setBuilt(engineBuilt);
                // consist?
                if (!engineConsistName.isEmpty()) {
                    Consist consist = InstanceManager.getDefault(ConsistManager.class).newConsist(engineConsistName);
                    engine.setConsist(consist);
                }

                enginesAdded++;

                if (location != null && track != null) {
                    String status = engine.setLocation(location, track);
                    if (!status.equals(Track.OKAY)) {
                        log.debug("Can't set engine's location because of {}", status);
                        JmriJOptionPane.showMessageDialog(
                                null, Bundle.getMessage("CanNotSetEngineAtLocation",
                                        engineRoad, engineNumber, engineModel, engineLocationName,
                                                engineTrackName, status),
                                Bundle.getMessage("rsCanNotLoc"), JmriJOptionPane.ERROR_MESSAGE);
                        if (status.startsWith(Track.TYPE)) {
                            int results = JmriJOptionPane.showConfirmDialog(
                                    null, Bundle.getMessage("DoYouWantToAllowService",
                                            engineLocationName, engineTrackName,
                                                    engineRoad, engineNumber, engine.getTypeName()),
                                    Bundle.getMessage("ServiceEngineType"),
                                    JmriJOptionPane.YES_NO_OPTION);
                            if (results == JmriJOptionPane.YES_OPTION) {
                                location.addTypeName(engine.getTypeName());
                                track.addTypeName(engine.getTypeName());
                                status = engine.setLocation(location, track);
                            } else {
                                break;
                            }
                        }
                        if (status.startsWith(Track.LENGTH) || status.startsWith(Track.CAPACITY)) {
                            int results = JmriJOptionPane.showConfirmDialog(null,
                                    Bundle.getMessage("DoYouWantIncreaseLength",
                                            engineTrackName),
                                    Bundle.getMessage("TrackLength"), JmriJOptionPane.YES_NO_OPTION);
                            if (results == JmriJOptionPane.YES_OPTION) {
                                track.setLength(track.getLength() + 1000);
                                status = engine.setLocation(location, track);
                            } else {
                                break;
                            }
                        }
                        if (!status.equals(Track.OKAY)) {
                            int results = JmriJOptionPane.showConfirmDialog(
                                    null, Bundle.getMessage("DoYouWantToForceEngine",
                                            engineRoad, engineNumber, engineLocationName,
                                                    engineTrackName),
                                    Bundle.getMessage("OverRide"), JmriJOptionPane.YES_NO_OPTION);
                            if (results == JmriJOptionPane.YES_OPTION) {
                                engine.setLocation(location, track, RollingStock.FORCE); // force
                            } else {
                                break;
                            }
                        }
                    }
                } else {
                    // log.debug("No location for engine ("+engineRoad+"
                    // "+engineNumber+")");
                }
            } else if (!line.isEmpty()) {
                log.info("Engine import line {} missing attributes: {}", lineNum, line);
                JmriJOptionPane.showMessageDialog(null, Bundle.getMessage("ImportMissingAttributes",
                        lineNum), Bundle.getMessage("EngineAttributeMissing"),
                        JmriJOptionPane.ERROR_MESSAGE);
                break;
            }
        }
        try {
            in.close();
        } catch (IOException e) {
            log.error("Import Engines failed: {}", e.getLocalizedMessage());
        }

        if (importOkay) {
            JmriJOptionPane.showMessageDialog(null, Bundle.getMessage("ImportEnginesAdded",
                    enginesAdded), Bundle.getMessage("SuccessfulImport"),
                    JmriJOptionPane.INFORMATION_MESSAGE);
        } else {
            JmriJOptionPane.showMessageDialog(null, Bundle.getMessage("ImportEnginesAdded",
                    enginesAdded), Bundle.getMessage("ImportFailed"), JmriJOptionPane.ERROR_MESSAGE);
        }

        // kill status panel
        fstatus.dispose();
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ImportEngines.class);
}
