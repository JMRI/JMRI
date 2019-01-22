package jmri.jmrit.operations.rollingstock.cars.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.NumberFormat;
import javax.swing.JOptionPane;
import jmri.InstanceManager;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.ImportRollingStock;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarColors;
import jmri.jmrit.operations.rollingstock.cars.CarLengths;
import jmri.jmrit.operations.rollingstock.cars.CarLoads;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.CarOwners;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.rollingstock.cars.Kernel;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This routine will import cars into the operation database.
 *
 * Each field is space or comma delimited. Field order: Number Road Type Length
 * Weight Color Owner Built Location - Track. If a CSV file, the import will
 * accept these additional fields: Load Kernel Moves Value Comment Miscellaneous
 * Extensions
 *
 * @author Dan Boudreau Copyright (C) 2008 2010 2011, 2013, 2016
 */
public class ImportCars extends ImportRollingStock {

    CarManager manager = InstanceManager.getDefault(CarManager.class);

    private int weightResults = JOptionPane.NO_OPTION; // Automatically calculate weight for car if weight entry is not
    // found
    private boolean autoCalculate = true;
    private boolean askAutoCreateTypes = true;
    private boolean askAutoCreateLocations = true;
    private boolean askAutoCreateTracks = true;
    private boolean askAutoLocationType = true;
    private boolean askAutoIncreaseTrackLength = true;
    private boolean askAutoForceCar = true;

    private boolean autoCreateTypes = false;
    private boolean autoCreateLocations = false;
    private boolean autoCreateTracks = false;
    private boolean autoAdjustLocationType = false;
    private boolean autoAdjustTrackLength = false;
    private boolean autoForceCar = false;

    private final boolean autoCreateRoads = true;
    private final boolean autoCreateLengths = true;
    private final boolean autoCreateColors = true;
    private final boolean autoCreateOwners = true;

    private static final int CAR_NUMBER = 0;
    private static final int CAR_ROAD = 1;
    private static final int CAR_TYPE = 2;
    private static final int CAR_LENGTH = 3;
    private static final int CAR_WEIGHT = 4;
    private static final int CAR_COLOR = 5;
    private static final int CAR_OWNER = 6;
    private static final int CAR_BUILT = 7;
    private static final int CAR_LOCATION = 8;
    private static final int CAR_LOCATION_TRACK_SEPARATOR = 9;
    //    private static final int CAR_TRACK = 10;

    // only for CSV files
    private static final int CAR_LOAD = 11;
    private static final int CAR_KERNEL = 12;
    private static final int CAR_MOVES = 13;
    private static final int CAR_VALUE = 14;
    private static final int CAR_COMMENT = 15;
    //    private static final int CAR_MISCELLANEOUS = 16;
    private static final int CAR_EXTENSIONS = 17;

    private static final int MAXIMUM_NUMBER_FIELDS = CAR_EXTENSIONS + 1;

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

        createStatusFrame(Bundle.getMessage("ImportCars"));

        // Now read the input file
        boolean importOkay = false;
        boolean comma = false;
        boolean importKernel = false;
        int lineNum = 0;
        int carsAdded = 0;
        String line = " ";
        String carNumber;
        String carRoad;
        String carType;
        String carLength;
        String carWeight;
        String carColor = "";
        String carOwner = "";
        String carBuilt = "";
        String carLocationName = "";
        String carTrackName = "";
        String carLoadName = "";
        String carKernelName = "";
        int carMoves = 0;
        String carValue = "";
        String carComment = "";
        String[] inputLine;

        // does the file name end with .csv?
        if (file.getAbsolutePath().endsWith(".csv")) { // NOI18N
            log.info("Using comma as delimiter for import cars");
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
            if (line.equalsIgnoreCase("kernel")) { // NOI18N
                log.info("Importing kernel names");
                importKernel = true;
                continue;
            }
            if (line.equalsIgnoreCase("comma")) { // NOI18N
                log.info("Using comma as delimiter for import cars");
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

            // The minimum import is car number, road, type and length
            if (inputLine.length > base + 3) {

                carNumber = inputLine[base + CAR_NUMBER];
                carRoad = inputLine[base + CAR_ROAD];
                carType = inputLine[base + CAR_TYPE];
                carLength = inputLine[base + CAR_LENGTH];
                carWeight = "0";
                carColor = "";
                carOwner = "";
                carBuilt = "";
                carLocationName = "";
                carTrackName = "";
                carLoadName = InstanceManager.getDefault(CarLoads.class).getDefaultEmptyName();
                carKernelName = "";
                carMoves = 0;
                carValue = "";
                carComment = "";

                if (inputLine.length > base + CAR_WEIGHT) {
                    carWeight = inputLine[base + CAR_WEIGHT];
                }
                if (inputLine.length > base + CAR_COLOR) {
                    carColor = inputLine[base + CAR_COLOR];
                }

                log.debug("Checking car number ({}) road ({}) type ({}) length ({}) weight ({}) color ({})", carNumber,
                        carRoad, carType, carLength, carWeight, carColor); // NOI18N
                
                if (carNumber.trim().equals("")) {
                    log.info("Import line {} missing car number", lineNum);
                    break;
                }
                if (carRoad.trim().equals("")) {
                    log.info("Import line {} missing car road", lineNum);
                    break;
                }
                if (carType.trim().equals("")) {
                    log.info("Import line {} missing car type", lineNum);
                    break;
                }
                if (carLength.trim().equals("")) {
                    log.info("Import line {} missing car length", lineNum);
                    break;
                }
                if (carNumber.length() > Control.max_len_string_road_number) {
                    JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle.getMessage("CarRoadNumberTooLong"),
                            new Object[]{(carRoad + " " + carNumber), carNumber}),
                            MessageFormat.format(Bundle
                                    .getMessage("RoadNumMustBeLess"), new Object[]{Control.max_len_string_road_number + 1}),
                            JOptionPane.ERROR_MESSAGE);
                    break;
                }
                if (carRoad.length() > Control.max_len_string_attibute) {
                    JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle.getMessage("CarRoadNameTooLong"),
                            new Object[]{(carRoad + " " + carNumber), carRoad}),
                            MessageFormat.format(Bundle
                                    .getMessage("carAttribute"), new Object[]{Control.max_len_string_attibute}),
                            JOptionPane.ERROR_MESSAGE);
                    break;
                }
                if (carType.length() > Control.max_len_string_attibute) {
                    JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle.getMessage("CarTypeNameTooLong"),
                            new Object[]{(carRoad + " " + carNumber), carType}),
                            MessageFormat.format(Bundle
                                    .getMessage("carAttribute"), new Object[]{Control.max_len_string_attibute}),
                            JOptionPane.ERROR_MESSAGE);
                    break;
                }
                if (!InstanceManager.getDefault(CarTypes.class).containsName(carType)) {
                    if (autoCreateTypes) {
                        log.debug("Adding car type ({})", carType);
                        InstanceManager.getDefault(CarTypes.class).addName(carType);
                    } else {
                        int results = JOptionPane.showConfirmDialog(null, Bundle.getMessage("Car") +
                                " (" +
                                carRoad +
                                " " +
                                carNumber +
                                ")" +
                                NEW_LINE +
                                MessageFormat.format(Bundle.getMessage("typeNameNotExist"),
                                        new Object[]{carType}),
                                Bundle.getMessage("carAddType"),
                                JOptionPane.YES_NO_CANCEL_OPTION);
                        if (results == JOptionPane.YES_OPTION) {
                            InstanceManager.getDefault(CarTypes.class).addName(carType);
                            if (askAutoCreateTypes) {
                                results = JOptionPane.showConfirmDialog(null, Bundle
                                        .getMessage("DoYouWantToAutoAddCarTypes"), Bundle.getMessage("OnlyAskedOnce"),
                                        JOptionPane.YES_NO_OPTION);
                                if (results == JOptionPane.YES_OPTION) {
                                    autoCreateTypes = true;
                                }
                            }
                            askAutoCreateTypes = false;
                        } else if (results == JOptionPane.CANCEL_OPTION) {
                            break;
                        }
                    }
                }
                if (carLength.length() > Control.max_len_string_length_name) {
                    JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle.getMessage("CarLengthNameTooLong"),
                            new Object[]{(carRoad + " " + carNumber), carLength}),
                            MessageFormat.format(Bundle
                                    .getMessage("carAttribute"), new Object[]{Control.max_len_string_length_name}),
                            JOptionPane.ERROR_MESSAGE);
                    break;
                }
                if (carLength.equals("")) {
                    log.debug("Car ({} {}) length not specified", carRoad, carNumber);
                    JOptionPane.showMessageDialog(null, MessageFormat.format(
                            Bundle.getMessage("CarLengthNotSpecified"), new Object[]{(carRoad + " " + carNumber)}),
                            Bundle.getMessage("CarLengthMissing"), JOptionPane.ERROR_MESSAGE);
                    break;
                }
                try {
                    Integer.parseInt(carLength);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle
                            .getMessage("CarLengthNameNotNumber"),
                            new Object[]{(carRoad + " " + carNumber),
                                    carLength}),
                            Bundle.getMessage("CarLengthMissing"), JOptionPane.ERROR_MESSAGE);
                    break;
                }
                if (carWeight.length() > Control.max_len_string_weight_name) {
                    JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle.getMessage("CarWeightNameTooLong"),
                            new Object[]{(carRoad + " " + carNumber), carWeight}),
                            MessageFormat.format(Bundle
                                    .getMessage("carAttribute"), new Object[]{Control.max_len_string_weight_name}),
                            JOptionPane.ERROR_MESSAGE);
                    break;
                }
                if (carColor.length() > Control.max_len_string_attibute) {
                    JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle.getMessage("CarColorNameTooLong"),
                            new Object[]{(carRoad + " " + carNumber), carColor}),
                            MessageFormat.format(Bundle
                                    .getMessage("carAttribute"), new Object[]{Control.max_len_string_attibute}),
                            JOptionPane.ERROR_MESSAGE);
                    break;
                }
                // calculate car weight if "0"
                if (carWeight.equals("0")) {
                    try {
                        double doubleCarLength = Double.parseDouble(carLength) * 12 / Setup.getScaleRatio();
                        double doubleCarWeight =
                                (Setup.getInitalWeight() + doubleCarLength * Setup.getAddWeight()) / 1000;
                        NumberFormat nf = NumberFormat.getNumberInstance();
                        nf.setMaximumFractionDigits(1);
                        carWeight = nf.format(doubleCarWeight); // car weight in ounces.
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(null, Bundle.getMessage("carLengthMustBe"), Bundle
                                .getMessage("carWeigthCanNot"), JOptionPane.ERROR_MESSAGE);
                    }
                }
                Car existingCar = manager.getByRoadAndNumber(carRoad, carNumber);
                if (existingCar != null) {
                    log.info("Can not add, car number (" + carNumber + ") road (" + carRoad + ") already exists!"); // NOI18N
                } else {
                    if (inputLine.length > base + CAR_OWNER) {
                        carOwner = inputLine[base + CAR_OWNER];
                        if (carOwner.length() > Control.max_len_string_attibute) {
                            JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle
                                    .getMessage("CarOwnerNameTooLong"),
                                    new Object[]{(carRoad + " " + carNumber),
                                            carOwner}),
                                    MessageFormat.format(Bundle.getMessage("carAttribute"),
                                            new Object[]{Control.max_len_string_attibute}),
                                    JOptionPane.ERROR_MESSAGE);
                            break;
                        }
                    }
                    if (inputLine.length > base + CAR_BUILT) {
                        carBuilt = inputLine[base + CAR_BUILT];
                        if (carBuilt.length() > Control.max_len_string_built_name) {
                            JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle
                                    .getMessage("CarBuiltNameTooLong"),
                                    new Object[]{(carRoad + " " + carNumber),
                                            carBuilt}),
                                    MessageFormat.format(Bundle.getMessage("carAttribute"),
                                            new Object[]{Control.max_len_string_built_name}),
                                    JOptionPane.ERROR_MESSAGE);
                            break;
                        }
                    }
                    if (inputLine.length > base + CAR_LOCATION) {
                        carLocationName = inputLine[base + CAR_LOCATION];

                    }
                    // Location name can be one to three words
                    if (inputLine.length > base + CAR_LOCATION_TRACK_SEPARATOR) {
                        if (!inputLine[base + CAR_LOCATION_TRACK_SEPARATOR].equals(LOCATION_TRACK_SEPARATOR)) {
                            carLocationName = carLocationName + " " + inputLine[base + CAR_LOCATION + 1];
                            if (inputLine.length > base + CAR_LOCATION + 2 &&
                                    !inputLine[base + CAR_LOCATION + 2].equals(LOCATION_TRACK_SEPARATOR)) {
                                carLocationName = carLocationName + " " + inputLine[base + CAR_LOCATION + 2];
                            }
                        }
                        log.debug("Car ({} {}) has location ({})", carRoad, carNumber, carLocationName);
                        // now get the track name, no more than 3 words to describe track name when importing a space delimited file
                        boolean foundLocationTrackSeparator = false;
                        for (int i =
                                base + CAR_LOCATION_TRACK_SEPARATOR; i < base + CAR_LOCATION_TRACK_SEPARATOR + 6; i++) {
                            if (inputLine[i].equals(LOCATION_TRACK_SEPARATOR)) {
                                foundLocationTrackSeparator = true;
                                if (inputLine.length > i + 1) {
                                    carTrackName = inputLine[++i];
                                    if (comma) {
                                        break;
                                    }
                                }
                            } else if (foundLocationTrackSeparator && inputLine.length > i) {
                                carTrackName = carTrackName + " " + inputLine[i];
                            }
                        }
                        if (carTrackName == null) {
                            carTrackName = "";
                        }
                        log.debug("Car ({} {}) has track ({})", carRoad, carNumber, carTrackName);
                    }

                    // is there a load name?
                    if (comma && inputLine.length > base + CAR_LOAD) {
                        carLoadName = inputLine[CAR_LOAD];
                        log.debug("Car ({} {}) has load ({})", carRoad, carNumber, carLoadName);
                    }
                    // is there a kernel name?
                    if (comma && inputLine.length > base + CAR_KERNEL) {
                        carKernelName = inputLine[CAR_KERNEL];
                        log.debug("Car ({} {}) has kernel name ({})", carRoad, carNumber, carKernelName);
                    }
                    // is the a move count?
                    if (comma && inputLine.length > base + CAR_MOVES) {
                        try {
                            carMoves = Integer.parseInt(inputLine[CAR_MOVES]);
                            log.debug("Car ({} {}) has move count ({})", carRoad, carNumber, carMoves);
                        } catch (NumberFormatException e) {
                            log.error("Car ({} {}) has move count ({}) not a number", carRoad, carNumber, carMoves);
                        }
                    }
                    // is there a car value?
                    if (comma && inputLine.length > base + CAR_VALUE) {
                        carValue = inputLine[CAR_VALUE];
                    }
                    // is there a car comment?
                    if (comma && inputLine.length > base + CAR_COMMENT) {
                        carComment = inputLine[CAR_COMMENT];
                    }

                    if (carLocationName.length() > Control.max_len_string_location_name) {
                        JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle
                                .getMessage("CarLocationNameTooLong"),
                                new Object[]{(carRoad + " " + carNumber),
                                        carLocationName}),
                                MessageFormat.format(Bundle.getMessage("carAttribute"),
                                        new Object[]{Control.max_len_string_location_name}),
                                JOptionPane.ERROR_MESSAGE);
                        break;
                    }
                    if (carTrackName.length() > Control.max_len_string_track_name) {
                        JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle
                                .getMessage("CarTrackNameTooLong"),
                                new Object[]{(carRoad + " " + carNumber),
                                        carTrackName}),
                                MessageFormat.format(Bundle.getMessage("carAttribute"),
                                        new Object[]{Control.max_len_string_track_name}),
                                JOptionPane.ERROR_MESSAGE);
                        break;
                    }
                    Location location =
                            InstanceManager.getDefault(LocationManager.class).getLocationByName(carLocationName);
                    Track track = null;
                    if (location == null && !carLocationName.equals("")) {
                        if (autoCreateLocations) {
                            log.debug("Create location ({})", carLocationName);
                            location = InstanceManager.getDefault(LocationManager.class).newLocation(carLocationName);
                        } else {
                            JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle
                                    .getMessage("CarLocationDoesNotExist"),
                                    new Object[]{(carRoad + " " + carNumber),
                                            carLocationName}),
                                    Bundle.getMessage("carLocation"), JOptionPane.ERROR_MESSAGE);
                            int results = JOptionPane.showConfirmDialog(null, MessageFormat.format(Bundle
                                    .getMessage("DoYouWantToCreateLoc"), new Object[]{carLocationName}), Bundle
                                            .getMessage("carLocation"),
                                    JOptionPane.YES_NO_OPTION);
                            if (results == JOptionPane.YES_OPTION) {
                                log.debug("Create location ({})", carLocationName);
                                location =
                                        InstanceManager.getDefault(LocationManager.class).newLocation(carLocationName);
                                if (askAutoCreateLocations) {
                                    results = JOptionPane.showConfirmDialog(null, Bundle
                                            .getMessage("DoYouWantToAutoCreateLoc"),
                                            Bundle.getMessage("OnlyAskedOnce"), JOptionPane.YES_NO_OPTION);
                                    if (results == JOptionPane.YES_OPTION) {
                                        autoCreateLocations = true;
                                    }
                                }
                                askAutoCreateLocations = false;
                            } else {
                                break;
                            }
                        }
                    }
                    if (location != null && !carTrackName.equals("")) {
                        track = location.getTrackByName(carTrackName, null);
                        if (track == null) {
                            if (autoCreateTracks) {
                                if (location.getLocationOps() == Location.NORMAL) {
                                    log.debug("Create 1000 foot yard track ({})", carTrackName);
                                    track = location.addTrack(carTrackName, Track.YARD);
                                } else {
                                    log.debug("Create 1000 foot staging track ({})", carTrackName);
                                    track = location.addTrack(carTrackName, Track.STAGING);
                                }
                                track.setLength(1000);
                            } else {
                                JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle
                                        .getMessage("CarTrackDoesNotExist"),
                                        new Object[]{
                                                (carRoad + " " + carNumber), carTrackName, carLocationName}),
                                        Bundle.getMessage("carTrack"),
                                        JOptionPane.ERROR_MESSAGE);
                                int results = JOptionPane.showConfirmDialog(null, MessageFormat.format(Bundle
                                        .getMessage("DoYouWantToCreateTrack"),
                                        new Object[]{carTrackName, carLocationName}),
                                        Bundle.getMessage("carTrack"), JOptionPane.YES_NO_OPTION);
                                if (results == JOptionPane.YES_OPTION) {
                                    if (location.getLocationOps() == Location.NORMAL) {
                                        log.debug("Create 1000 foot yard track ({})", carTrackName);
                                        track = location.addTrack(carTrackName, Track.YARD);
                                    } else {
                                        log.debug("Create 1000 foot staging track ({})", carTrackName);
                                        track = location.addTrack(carTrackName, Track.STAGING);
                                    }
                                    track.setLength(1000);
                                    if (askAutoCreateTracks) {
                                        results = JOptionPane.showConfirmDialog(null, Bundle
                                                .getMessage("DoYouWantToAutoCreateTrack"),
                                                Bundle
                                                        .getMessage("OnlyAskedOnce"),
                                                JOptionPane.YES_NO_OPTION);
                                        if (results == JOptionPane.YES_OPTION) {
                                            autoCreateTracks = true;
                                        }
                                        askAutoCreateTracks = false;
                                    }
                                } else {
                                    break;
                                }
                            }
                        }
                    }

                    log.debug("Add car ({} {}) owner ({}) built ({}) location ({}, {})", carRoad, carNumber, carOwner,
                            carBuilt, carLocationName, carTrackName);
                    Car car = manager.newRS(carRoad, carNumber);
                    car.setTypeName(carType);
                    car.setLength(carLength);
                    car.setWeight(carWeight);
                    car.setColor(carColor);
                    car.setOwner(carOwner);
                    car.setBuilt(carBuilt);
                    car.setLoadName(carLoadName);
                    car.setKernel(manager.newKernel(carKernelName));
                    car.setMoves(carMoves);
                    car.setValue(carValue);
                    car.setComment(carComment);
                    carsAdded++;

                    // if the car's type name is "Caboose" then make it a caboose
                    car.setCaboose(carType.equals("Caboose"));
                    // determine if there are any car extensions
                    if (comma && inputLine.length > base + CAR_EXTENSIONS) {
                        String extensions = inputLine[CAR_EXTENSIONS];
                        log.debug("Car ({} {}) has extension ({})", carRoad, carNumber, extensions);
                        String[] ext = extensions.split(Car.EXTENSION_REGEX);
                        for (int i = 0; i < ext.length; i++) {
                            if (ext[i].equals(Car.CABOOSE_EXTENSION)) {
                                car.setCaboose(true);
                            }
                            if (ext[i].equals(Car.FRED_EXTENSION)) {
                                car.setFred(true);
                            }
                            if (ext[i].equals(Car.PASSENGER_EXTENSION)) {
                                car.setPassenger(true);
                                car.setBlocking(Integer.parseInt(ext[i + 1]));
                            }
                            if (ext[i].equals(Car.UTILITY_EXTENSION)) {
                                car.setUtility(true);
                            }
                            if (ext[i].equals(Car.HAZARDOUS_EXTENSION)) {
                                car.setHazardous(true);
                            }
                        }
                    }

                    // add new roads
                    if (!InstanceManager.getDefault(CarRoads.class).containsName(carRoad)) {
                        if (autoCreateRoads) {
                            log.debug("add car road {}", carRoad);
                            InstanceManager.getDefault(CarRoads.class).addName(carRoad);
                        }
                    }

                    // add new lengths
                    if (!InstanceManager.getDefault(CarLengths.class).containsName(carLength)) {
                        if (autoCreateLengths) {
                            log.debug("add car length {}", carLength);
                            InstanceManager.getDefault(CarLengths.class).addName(carLength);
                        }
                    }

                    // add new colors
                    if (!InstanceManager.getDefault(CarColors.class).containsName(carColor)) {
                        if (autoCreateColors) {
                            log.debug("add car color {}", carColor);
                            InstanceManager.getDefault(CarColors.class).addName(carColor);
                        }
                    }

                    // add new owners
                    if (!InstanceManager.getDefault(CarOwners.class).containsName(carOwner)) {
                        if (autoCreateOwners) {
                            log.debug("add car owner {}", carOwner);
                            InstanceManager.getDefault(CarOwners.class).addName(carOwner);
                        }
                    }

                    if (car.getWeight().equals("")) {
                        log.debug("Car ({} {}) weight not specified", carRoad, carNumber);
                        if (weightResults != JOptionPane.CANCEL_OPTION) {
                            weightResults = JOptionPane.showOptionDialog(null, MessageFormat.format(Bundle
                                    .getMessage("CarWeightNotFound"), new Object[]{(carRoad + " " + carNumber)}),
                                    Bundle.getMessage("CarWeightMissing"), JOptionPane.YES_NO_CANCEL_OPTION,
                                    JOptionPane.INFORMATION_MESSAGE, null, new Object[]{
                                            Bundle.getMessage("ButtonYes"), Bundle.getMessage("ButtonNo"),
                                            Bundle.getMessage("ButtonDontShow")},
                                    autoCalculate ? Bundle
                                            .getMessage("ButtonYes") : Bundle.getMessage("ButtonNo"));
                        }
                        if (weightResults == JOptionPane.NO_OPTION) {
                            autoCalculate = false;
                        }
                        if (weightResults == JOptionPane.YES_OPTION ||
                                autoCalculate == true && weightResults == JOptionPane.CANCEL_OPTION) {
                            autoCalculate = true;
                            try {
                                double carLen = Double.parseDouble(car.getLength()) * 12 / Setup.getScaleRatio();
                                double carWght = (Setup.getInitalWeight() + carLen * Setup.getAddWeight()) / 1000;
                                NumberFormat nf = NumberFormat.getNumberInstance();
                                nf.setMaximumFractionDigits(1);
                                car.setWeight(nf.format(carWght)); // car weight in ounces.
                                int tons = (int) (carWght * Setup.getScaleTonRatio());
                                // adjust weight for caboose
                                if (car.isCaboose()) {
                                    tons = (int) (Double.parseDouble(car.getLength()) * .9); // .9 tons/foot
                                }
                                car.setWeightTons(Integer.toString(tons));
                            } catch (NumberFormatException e) {
                                JOptionPane.showMessageDialog(null, Bundle.getMessage("carLengthMustBe"), Bundle
                                        .getMessage("carWeigthCanNot"), JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                    if (location != null && track != null) {
                        String status = car.setLocation(location, track);
                        if (!status.equals(Track.OKAY)) {
                            log.debug("Can't set car's location because of {}", status);
                            if (status.startsWith(Track.TYPE)) {
                                if (autoAdjustLocationType) {
                                    location.addTypeName(carType);
                                    track.addTypeName(carType);
                                    status = car.setLocation(location, track);
                                } else {
                                    JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle
                                            .getMessage("CanNotSetCarAtLocation"),
                                            new Object[]{
                                                    (carRoad + " " + carNumber), carType, carLocationName, carTrackName,
                                                    status}),
                                            Bundle.getMessage("rsCanNotLoc"), JOptionPane.ERROR_MESSAGE);
                                    int results = JOptionPane.showConfirmDialog(null, MessageFormat.format(Bundle
                                            .getMessage("DoYouWantToAllowService"),
                                            new Object[]{carLocationName,
                                                    carTrackName, (carRoad + " " + carNumber), carType}),
                                            Bundle
                                                    .getMessage("ServiceCarType"),
                                            JOptionPane.YES_NO_OPTION);
                                    if (results == JOptionPane.YES_OPTION) {
                                        location.addTypeName(carType);
                                        track.addTypeName(carType);
                                        status = car.setLocation(location, track);
                                        log.debug("Set car's location status: {}", status);
                                        if (askAutoLocationType) {
                                            results = JOptionPane.showConfirmDialog(null, Bundle
                                                    .getMessage("DoYouWantToAutoAdjustLocations"),
                                                    Bundle
                                                            .getMessage("OnlyAskedOnce"),
                                                    JOptionPane.YES_NO_OPTION);
                                            if (results == JOptionPane.YES_OPTION) {
                                                autoAdjustLocationType = true;
                                            }
                                            askAutoLocationType = false;
                                        }
                                    } else {
                                        break;
                                    }
                                }
                            }
                            if (status.startsWith(Track.LENGTH)) {
                                if (autoAdjustTrackLength) {
                                    track.setLength(track.getLength() + 1000);
                                    status = car.setLocation(location, track);
                                    log.debug("Set track length status: {}", status);
                                } else {
                                    JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle
                                            .getMessage("CanNotSetCarAtLocation"),
                                            new Object[]{
                                                    (carRoad + " " + carNumber), carType, carLocationName, carTrackName,
                                                    status}),
                                            Bundle.getMessage("rsCanNotLoc"), JOptionPane.ERROR_MESSAGE);
                                    int results = JOptionPane.showConfirmDialog(null, MessageFormat.format(Bundle
                                            .getMessage("DoYouWantIncreaseLength"), new Object[]{carTrackName}), Bundle
                                                    .getMessage("TrackLength"),
                                            JOptionPane.YES_NO_OPTION);
                                    if (results == JOptionPane.YES_OPTION) {
                                        track.setLength(track.getLength() + 1000);
                                        status = car.setLocation(location, track);
                                        log.debug("Set track length status: {}", status);
                                        if (askAutoIncreaseTrackLength) {
                                            results = JOptionPane.showConfirmDialog(null, Bundle
                                                    .getMessage("DoYouWantToAutoAdjustTrackLength"),
                                                    Bundle
                                                            .getMessage("OnlyAskedOnce"),
                                                    JOptionPane.YES_NO_OPTION);
                                            if (results == JOptionPane.YES_OPTION) {
                                                autoAdjustTrackLength = true;
                                            }
                                            askAutoIncreaseTrackLength = false;
                                        }
                                    } else {
                                        break;
                                    }
                                }
                            }
                            if (!status.equals(Track.OKAY)) {
                                if (autoForceCar) {
                                    car.setLocation(location, track, RollingStock.FORCE); // force car
                                } else {
                                    JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle
                                            .getMessage("CanNotSetCarAtLocation"),
                                            new Object[]{
                                                    (carRoad + " " + carNumber), carType, carLocationName, carTrackName,
                                                    status}),
                                            Bundle.getMessage("rsCanNotLoc"), JOptionPane.ERROR_MESSAGE);
                                    int results = JOptionPane.showConfirmDialog(null, MessageFormat.format(Bundle
                                            .getMessage("DoYouWantToForceCar"),
                                            new Object[]{
                                                    (carRoad + " " + carNumber), carLocationName, carTrackName}),
                                            Bundle
                                                    .getMessage("OverRide"),
                                            JOptionPane.YES_NO_OPTION);
                                    if (results == JOptionPane.YES_OPTION) {
                                        car.setLocation(location, track, true); // force car
                                        if (askAutoForceCar) {
                                            results = JOptionPane.showConfirmDialog(null, Bundle
                                                    .getMessage("DoYouWantToAutoForceCar"),
                                                    Bundle
                                                            .getMessage("OnlyAskedOnce"),
                                                    JOptionPane.YES_NO_OPTION);
                                            if (results == JOptionPane.YES_OPTION) {
                                                autoForceCar = true;
                                            }
                                            askAutoForceCar = false;
                                        }
                                    } else {
                                        break;
                                    }
                                }
                            }
                        }
                    } else {
                        // log.debug("No location for car ("+carRoad+" "+carNumber+")");
                    }
                }
            } else if (importKernel && inputLine.length == base + 3) {
                carNumber = inputLine[base + 0];
                carRoad = inputLine[base + 1];
                String kernelName = inputLine[base + 2];
                Car car = manager.getByRoadAndNumber(carRoad, carNumber);
                if (car != null) {
                    Kernel kernel = manager.newKernel(kernelName);
                    car.setKernel(kernel);
                    carsAdded++;
                } else {
                    log.info("Car number (" + carNumber + ") road (" + carRoad + ") does not exist!"); // NOI18N
                    break;
                }
            } else if (!line.equals("")) {
                log.info("Car import line " + lineNum + " missing attributes: " + line);
                JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle.getMessage("ImportMissingAttributes"),
                        new Object[]{lineNum}) +
                        NEW_LINE +
                        line +
                        NEW_LINE +
                        Bundle.getMessage("ImportMissingAttributes2"),
                        Bundle
                                .getMessage("CarAttributeMissing"),
                        JOptionPane.ERROR_MESSAGE);
                break;
            }
        } try {
            in.close();
        } catch (IOException e) {
        }

        if (importOkay) {
            JOptionPane
                    .showMessageDialog(null, MessageFormat.format(Bundle.getMessage("ImportCarsAdded"),
                            new Object[]{carsAdded}), Bundle.getMessage("SuccessfulImport"),
                            JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle.getMessage("ImportCarsAdded"),
                    new Object[]{carsAdded}), Bundle.getMessage("ImportFailed"), JOptionPane.ERROR_MESSAGE);
        }
        
        // kill status panel
        fstatus.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(ImportCars.class);
}
