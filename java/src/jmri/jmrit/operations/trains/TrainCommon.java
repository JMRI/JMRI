package jmri.jmrit.operations.trains;

import java.awt.*;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import javax.swing.JLabel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.util.StdDateFormat;

import jmri.InstanceManager;
import jmri.jmrit.operations.locations.*;
import jmri.jmrit.operations.locations.divisions.DivisionManager;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.cars.*;
import jmri.jmrit.operations.rollingstock.engines.*;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.ColorUtil;

/**
 * Common routines for trains
 *
 * @author Daniel Boudreau (C) Copyright 2008, 2009, 2010, 2011, 2012, 2013,
 *         2021
 */
public class TrainCommon {

    protected static final String TAB = "    "; // NOI18N
    protected static final String NEW_LINE = "\n"; // NOI18N
    public static final String SPACE = " ";
    protected static final String BLANK_LINE = " ";
    protected static final String HORIZONTAL_LINE_CHAR = "-";
    protected static final String BUILD_REPORT_CHAR = "-";
    public static final String HYPHEN = "-";
    protected static final String VERTICAL_LINE_CHAR = "|";
    protected static final String TEXT_COLOR_START = "<FONT color=\"";
    protected static final String TEXT_COLOR_DONE = "\">";
    protected static final String TEXT_COLOR_END = "</FONT>";

    // when true a pick up, when false a set out
    protected static final boolean PICKUP = true;
    // when true Manifest, when false switch list
    protected static final boolean IS_MANIFEST = true;
    // when true local car move
    public static final boolean LOCAL = true;
    // when true engine attribute, when false car
    protected static final boolean ENGINE = true;
    // when true, two column table is sorted by track names
    public static final boolean IS_TWO_COLUMN_TRACK = true;

    CarManager carManager = InstanceManager.getDefault(CarManager.class);
    EngineManager engineManager = InstanceManager.getDefault(EngineManager.class);
    LocationManager locationManager = InstanceManager.getDefault(LocationManager.class);

    // for switch lists
    protected boolean _pickupCars; // true when there are pickups
    protected boolean _dropCars; // true when there are set outs

    /**
     * Used to generate "Two Column" format for engines.
     *
     * @param file       Manifest or Switch List File
     * @param engineList List of engines for this train.
     * @param rl         The RouteLocation being printed.
     * @param isManifest True if manifest, false if switch list.
     */
    protected void blockLocosTwoColumn(PrintWriter file, List<Engine> engineList, RouteLocation rl,
            boolean isManifest) {
        if (isThereWorkAtLocation(null, engineList, rl)) {
            printEngineHeader(file, isManifest);
        }
        int lineLength = getLineLength(isManifest);
        for (Engine engine : engineList) {
            if (engine.getRouteLocation() == rl && !engine.getTrackName().equals(Engine.NONE)) {
                String pullText = padAndTruncate(pickupEngine(engine).trim(), lineLength / 2);
                pullText = formatColorString(pullText, Setup.getPickupColor());
                String s = pullText + VERTICAL_LINE_CHAR + tabString("", lineLength / 2 - 1);
                addLine(file, s);
            }
            if (engine.getRouteDestination() == rl) {
                String dropText = padAndTruncate(dropEngine(engine).trim(), lineLength / 2 - 1);
                dropText = formatColorString(dropText, Setup.getDropColor());
                String s = tabString("", lineLength / 2) + VERTICAL_LINE_CHAR + dropText;
                addLine(file, s);
            }
        }
    }

    /**
     * Adds a list of locomotive pick ups for the route location to the output
     * file. Used to generate "Standard" format.
     *
     * @param file       Manifest or Switch List File
     * @param engineList List of engines for this train.
     * @param rl         The RouteLocation being printed.
     * @param isManifest True if manifest, false if switch list
     */
    protected void pickupEngines(PrintWriter file, List<Engine> engineList, RouteLocation rl, boolean isManifest) {
        boolean printHeader = Setup.isPrintHeadersEnabled();
        for (Engine engine : engineList) {
            if (engine.getRouteLocation() == rl && !engine.getTrackName().equals(Engine.NONE)) {
                if (printHeader) {
                    printPickupEngineHeader(file, isManifest);
                    printHeader = false;
                }
                pickupEngine(file, engine, isManifest);
            }
        }
    }

    private void pickupEngine(PrintWriter file, Engine engine, boolean isManifest) {
        StringBuffer buf = new StringBuffer(padAndTruncateIfNeeded(Setup.getPickupEnginePrefix(),
                isManifest ? Setup.getManifestPrefixLength() : Setup.getSwitchListPrefixLength()));
        String[] format = Setup.getPickupEngineMessageFormat();
        for (String attribute : format) {
            String s = getEngineAttribute(engine, attribute, PICKUP);
            if (!checkStringLength(buf.toString() + s, isManifest)) {
                addLine(file, buf.toString());
                buf = new StringBuffer(TAB); // new line
            }
            buf.append(s);
        }
        addLine(file, buf.toString());
    }

    /**
     * Adds a list of locomotive drops for the route location to the output
     * file. Used to generate "Standard" format.
     *
     * @param file       Manifest or Switch List File
     * @param engineList List of engines for this train.
     * @param rl         The RouteLocation being printed.
     * @param isManifest True if manifest, false if switch list
     */
    protected void dropEngines(PrintWriter file, List<Engine> engineList, RouteLocation rl, boolean isManifest) {
        boolean printHeader = Setup.isPrintHeadersEnabled();
        for (Engine engine : engineList) {
            if (engine.getRouteDestination() == rl) {
                if (printHeader) {
                    printDropEngineHeader(file, isManifest);
                    printHeader = false;
                }
                dropEngine(file, engine, isManifest);
            }
        }
    }

    private void dropEngine(PrintWriter file, Engine engine, boolean isManifest) {
        StringBuffer buf = new StringBuffer(padAndTruncateIfNeeded(Setup.getDropEnginePrefix(),
                isManifest ? Setup.getManifestPrefixLength() : Setup.getSwitchListPrefixLength()));
        String[] format = Setup.getDropEngineMessageFormat();
        for (String attribute : format) {
            String s = getEngineAttribute(engine, attribute, !PICKUP);
            if (!checkStringLength(buf.toString() + s, isManifest)) {
                addLine(file, buf.toString());
                buf = new StringBuffer(TAB); // new line
            }
            buf.append(s);
        }
        addLine(file, buf.toString());
    }

    /**
     * Returns the pick up string for a loco. Useful for frames like the train
     * conductor and yardmaster.
     *
     * @param engine The Engine.
     * @return engine pick up string
     */
    public String pickupEngine(Engine engine) {
        StringBuilder builder = new StringBuilder();
        for (String attribute : Setup.getPickupEngineMessageFormat()) {
            builder.append(getEngineAttribute(engine, attribute, PICKUP));
        }
        return builder.toString();
    }

    /**
     * Returns the drop string for a loco. Useful for frames like the train
     * conductor and yardmaster.
     *
     * @param engine The Engine.
     * @return engine drop string
     */
    public String dropEngine(Engine engine) {
        StringBuilder builder = new StringBuilder();
        for (String attribute : Setup.getDropEngineMessageFormat()) {
            builder.append(getEngineAttribute(engine, attribute, !PICKUP));
        }
        return builder.toString();
    }

    // the next three booleans are used to limit the header to once per location
    boolean _printPickupHeader = true;
    boolean _printSetoutHeader = true;
    boolean _printLocalMoveHeader = true;

    /**
     * Block cars by track, then pick up and set out for each location in a
     * train's route. This routine is used for the "Standard" format.
     *
     * @param file        Manifest or switch list File
     * @param train       The train being printed.
     * @param carList     List of cars for this train
     * @param rl          The RouteLocation being printed
     * @param printHeader True if new location.
     * @param isManifest  True if manifest, false if switch list.
     */
    protected void blockCarsByTrack(PrintWriter file, Train train, List<Car> carList, RouteLocation rl,
            boolean printHeader, boolean isManifest) {
        if (printHeader) {
            _printPickupHeader = true;
            _printSetoutHeader = true;
            _printLocalMoveHeader = true;
        }
        List<Track> tracks = rl.getLocation().getTracksByNameList(null);
        List<String> trackNames = new ArrayList<>();
        clearUtilityCarTypes(); // list utility cars by quantity
        for (Track track : tracks) {
            if (trackNames.contains(track.getSplitName())) {
                continue;
            }
            trackNames.add(track.getSplitName()); // use a track name once

            // car pick ups
            blockCarsPickups(file, train, carList, rl, track, isManifest);

            // now do car set outs and local moves
            // group local moves first?
            blockCarsSetoutsAndMoves(file, train, carList, rl, track, isManifest, false,
                    Setup.isGroupCarMovesEnabled());
            // set outs or both
            blockCarsSetoutsAndMoves(file, train, carList, rl, track, isManifest, true,
                    !Setup.isGroupCarMovesEnabled());

            if (!Setup.isSortByTrackNameEnabled()) {
                break; // done
            }
        }
    }

    private void blockCarsPickups(PrintWriter file, Train train, List<Car> carList, RouteLocation rl,
            Track track, boolean isManifest) {
        // block pick up cars, except for passenger cars
        for (RouteLocation rld : train.getTrainBlockingOrder()) {
            for (Car car : carList) {
                if (Setup.isSortByTrackNameEnabled() &&
                        !track.getSplitName().equals(car.getSplitTrackName())) {
                    continue;
                }
                // Block cars
                // caboose or FRED is placed at end of the train
                // passenger cars are already blocked in the car list
                // passenger cars with negative block numbers are placed at
                // the front of the train, positive numbers at the end of
                // the train.
                if (isNextCar(car, rl, rld)) {
                    // determine if pick up header is needed
                    printPickupCarHeader(file, car, isManifest, !IS_TWO_COLUMN_TRACK);

                    // use truncated format if there's a switch list
                    boolean isTruncate = Setup.isPrintTruncateManifestEnabled() &&
                            rl.getLocation().isSwitchListEnabled();

                    if (car.isUtility()) {
                        pickupUtilityCars(file, carList, car, isTruncate, isManifest);
                    } else if (isManifest && isTruncate) {
                        pickUpCarTruncated(file, car, isManifest);
                    } else {
                        pickUpCar(file, car, isManifest);
                    }
                    _pickupCars = true;
                }
            }
        }
    }

    private void blockCarsSetoutsAndMoves(PrintWriter file, Train train, List<Car> carList, RouteLocation rl,
            Track track, boolean isManifest, boolean isSetout, boolean isLocalMove) {
        for (Car car : carList) {
            if (!car.isLocalMove() && isSetout || car.isLocalMove() && isLocalMove) {
                if (Setup.isSortByTrackNameEnabled() &&
                        car.getRouteLocation() != null &&
                        car.getRouteDestination() == rl) {
                    // must sort local moves by car's destination track name and not car's track name
                    // sorting by car's track name fails if there are "similar" location names.
                    if (!track.getSplitName().equals(car.getSplitDestinationTrackName())) {
                        continue;
                    }
                }
                if (car.getRouteDestination() == rl && car.getDestinationTrack() != null) {
                    // determine if drop or move header is needed
                    printDropOrMoveCarHeader(file, car, isManifest, !IS_TWO_COLUMN_TRACK);

                    // use truncated format if there's a switch list
                    boolean isTruncate = Setup.isPrintTruncateManifestEnabled() &&
                            rl.getLocation().isSwitchListEnabled() &&
                            !train.isLocalSwitcher();

                    if (car.isUtility()) {
                        setoutUtilityCars(file, carList, car, isTruncate, isManifest);
                    } else if (isManifest && isTruncate) {
                        truncatedDropCar(file, car, isManifest);
                    } else {
                        dropCar(file, car, isManifest);
                    }
                    _dropCars = true;
                }
            }
        }
    }

    /**
     * Used to determine if car is the next to be processed when producing
     * Manifests or Switch Lists. Caboose or FRED is placed at end of the train.
     * Passenger cars are already blocked in the car list. Passenger cars with
     * negative block numbers are placed at the front of the train, positive
     * numbers at the end of the train. Note that a car in train doesn't have a
     * track assignment.
     * 
     * @param car the car being tested
     * @param rl  when in train's route the car is being pulled
     * @param rld the destination being tested
     * @return true if this car is the next one to be processed
     */
    public static boolean isNextCar(Car car, RouteLocation rl, RouteLocation rld) {
        return isNextCar(car, rl, rld, false);
    }
        
    public static boolean isNextCar(Car car, RouteLocation rl, RouteLocation rld, boolean isIgnoreTrack) {
        Train train = car.getTrain();
        if (train != null &&
                (car.getTrack() != null || isIgnoreTrack) &&
                car.getRouteLocation() == rl &&
                (rld == car.getRouteDestination() &&
                        !car.isCaboose() &&
                        !car.hasFred() &&
                        !car.isPassenger() ||
                        rld == train.getTrainDepartsRouteLocation() &&
                                car.isPassenger() &&
                                car.getBlocking() < 0 ||
                        rld == train.getTrainTerminatesRouteLocation() &&
                                (car.isCaboose() ||
                                        car.hasFred() ||
                                        car.isPassenger() && car.getBlocking() >= 0))) {
            return true;
        }
        return false;
    }

    private void printPickupCarHeader(PrintWriter file, Car car, boolean isManifest, boolean isTwoColumnTrack) {
        if (_printPickupHeader && !car.isLocalMove()) {
            printPickupCarHeader(file, isManifest, !IS_TWO_COLUMN_TRACK);
            _printPickupHeader = false;
            // check to see if the other headers are needed. If
            // they are identical, not needed
            if (getPickupCarHeader(isManifest, !IS_TWO_COLUMN_TRACK)
                    .equals(getDropCarHeader(isManifest, !IS_TWO_COLUMN_TRACK))) {
                _printSetoutHeader = false;
            }
            if (getPickupCarHeader(isManifest, !IS_TWO_COLUMN_TRACK)
                    .equals(getLocalMoveHeader(isManifest))) {
                _printLocalMoveHeader = false;
            }
        }
    }

    private void printDropOrMoveCarHeader(PrintWriter file, Car car, boolean isManifest, boolean isTwoColumnTrack) {
        if (_printSetoutHeader && !car.isLocalMove()) {
            printDropCarHeader(file, isManifest, !IS_TWO_COLUMN_TRACK);
            _printSetoutHeader = false;
            // check to see if the other headers are needed. If they
            // are identical, not needed
            if (getPickupCarHeader(isManifest, !IS_TWO_COLUMN_TRACK)
                    .equals(getDropCarHeader(isManifest, !IS_TWO_COLUMN_TRACK))) {
                _printPickupHeader = false;
            }
            if (getDropCarHeader(isManifest, !IS_TWO_COLUMN_TRACK).equals(getLocalMoveHeader(isManifest))) {
                _printLocalMoveHeader = false;
            }
        }
        if (_printLocalMoveHeader && car.isLocalMove()) {
            printLocalCarMoveHeader(file, isManifest);
            _printLocalMoveHeader = false;
            // check to see if the other headers are needed. If they
            // are identical, not needed
            if (getPickupCarHeader(isManifest, !IS_TWO_COLUMN_TRACK)
                    .equals(getLocalMoveHeader(isManifest))) {
                _printPickupHeader = false;
            }
            if (getDropCarHeader(isManifest, !IS_TWO_COLUMN_TRACK).equals(getLocalMoveHeader(isManifest))) {
                _printSetoutHeader = false;
            }
        }
    }

    /**
     * Produces a two column format for car pick ups and set outs. Sorted by
     * track and then by blocking order. This routine is used for the "Two
     * Column" format.
     *
     * @param file        Manifest or switch list File
     * @param train       The train
     * @param carList     List of cars for this train
     * @param rl          The RouteLocation being printed
     * @param printHeader True if new location.
     * @param isManifest  True if manifest, false if switch list.
     */
    protected void blockCarsTwoColumn(PrintWriter file, Train train, List<Car> carList, RouteLocation rl,
            boolean printHeader, boolean isManifest) {
        index = 0;
        int lineLength = getLineLength(isManifest);
        List<Track> tracks = rl.getLocation().getTracksByNameList(null);
        List<String> trackNames = new ArrayList<>();
        clearUtilityCarTypes(); // list utility cars by quantity
        if (printHeader) {
            printCarHeader(file, isManifest, !IS_TWO_COLUMN_TRACK);
        }
        for (Track track : tracks) {
            if (trackNames.contains(track.getSplitName())) {
                continue;
            }
            trackNames.add(track.getSplitName()); // use a track name once
            // block car pick ups
            for (RouteLocation rld : train.getTrainBlockingOrder()) {
                for (int k = 0; k < carList.size(); k++) {
                    Car car = carList.get(k);
                    // block cars
                    // caboose or FRED is placed at end of the train
                    // passenger cars are already blocked in the car list
                    // passenger cars with negative block numbers are placed at
                    // the front of the train, positive numbers at the end of
                    // the train.
                    if (isNextCar(car, rl, rld)) {
                        if (Setup.isSortByTrackNameEnabled() &&
                                !track.getSplitName().equals(car.getSplitTrackName())) {
                            continue;
                        }
                        _pickupCars = true;
                        String s;
                        if (car.isUtility()) {
                            s = pickupUtilityCars(carList, car, isManifest, !IS_TWO_COLUMN_TRACK);
                            if (s == null) {
                                continue;
                            }
                            s = s.trim();
                        } else {
                            s = pickupCar(car, isManifest, !IS_TWO_COLUMN_TRACK).trim();
                        }
                        s = padAndTruncate(s, lineLength / 2);
                        if (car.isLocalMove()) {
                            s = formatColorString(s, Setup.getLocalColor());
                            String sl = appendSetoutString(s, carList, car.getRouteDestination(), car, isManifest,
                                    !IS_TWO_COLUMN_TRACK);
                            // check for utility car, and local route with two
                            // or more locations
                            if (!sl.equals(s)) {
                                s = sl;
                                carList.remove(car); // done with this car, remove from list
                                k--;
                            } else {
                                s = padAndTruncate(s + VERTICAL_LINE_CHAR, getLineLength(isManifest));
                            }
                        } else {
                            s = formatColorString(s, Setup.getPickupColor());
                            s = appendSetoutString(s, carList, rl, true, isManifest, !IS_TWO_COLUMN_TRACK);
                        }
                        addLine(file, s);
                    }
                }
            }
            if (!Setup.isSortByTrackNameEnabled()) {
                break; // done
            }
        }
        while (index < carList.size()) {
            String s = padString("", lineLength / 2);
            s = appendSetoutString(s, carList, rl, false, isManifest, !IS_TWO_COLUMN_TRACK);
            String test = s.trim();
            // null line contains |
            if (test.length() > 1) {
                addLine(file, s);
            }
        }
    }

    List<Car> doneCars = new ArrayList<>();

    /**
     * Produces a two column format for car pick ups and set outs. Sorted by
     * track and then by destination. Track name in header format, track name
     * removed from format. This routine is used to generate the "Two Column by
     * Track" format.
     *
     * @param file        Manifest or switch list File
     * @param train       The train
     * @param carList     List of cars for this train
     * @param rl          The RouteLocation being printed
     * @param printHeader True if new location.
     * @param isManifest  True if manifest, false if switch list.
     */
    protected void blockCarsByTrackNameTwoColumn(PrintWriter file, Train train, List<Car> carList, RouteLocation rl,
            boolean printHeader, boolean isManifest) {
        index = 0;
        List<Track> tracks = rl.getLocation().getTracksByNameList(null);
        List<String> trackNames = new ArrayList<>();
        doneCars.clear();
        clearUtilityCarTypes(); // list utility cars by quantity
        if (printHeader) {
            printCarHeader(file, isManifest, IS_TWO_COLUMN_TRACK);
        }
        for (Track track : tracks) {
            String trackName = track.getSplitName();
            if (trackNames.contains(trackName)) {
                continue;
            }
            // block car pick ups
            for (RouteLocation rld : train.getTrainBlockingOrder()) {
                for (Car car : carList) {
                    if (car.getTrack() != null &&
                            car.getRouteLocation() == rl &&
                            trackName.equals(car.getSplitTrackName()) &&
                            ((car.getRouteDestination() == rld && !car.isCaboose() && !car.hasFred()) ||
                                    (rld == train.getTrainTerminatesRouteLocation() &&
                                            (car.isCaboose() || car.hasFred())))) {
                        if (!trackNames.contains(trackName)) {
                            printTrackNameHeader(file, trackName, isManifest);
                        }
                        trackNames.add(trackName); // use a track name once
                        _pickupCars = true;
                        String s;
                        if (car.isUtility()) {
                            s = pickupUtilityCars(carList, car, isManifest, IS_TWO_COLUMN_TRACK);
                            if (s == null) {
                                continue;
                            }
                            s = s.trim();
                        } else {
                            s = pickupCar(car, isManifest, IS_TWO_COLUMN_TRACK).trim();
                        }
                        s = padAndTruncate(s, getLineLength(isManifest) / 2);
                        s = formatColorString(s, car.isLocalMove() ? Setup.getLocalColor() : Setup.getPickupColor());
                        s = appendSetoutString(s, trackName, carList, rl, isManifest, IS_TWO_COLUMN_TRACK);
                        addLine(file, s);
                    }
                }
            }
            for (Car car : carList) {
                if (!doneCars.contains(car) &&
                        car.getRouteDestination() == rl &&
                        trackName.equals(car.getSplitDestinationTrackName())) {
                    if (!trackNames.contains(trackName)) {
                        printTrackNameHeader(file, trackName, isManifest);
                    }
                    trackNames.add(trackName); // use a track name once
                    String s = padString("", getLineLength(isManifest) / 2);
                    String so = appendSetoutString(s, carList, rl, car, isManifest, IS_TWO_COLUMN_TRACK);
                    // check for utility car
                    if (so.equals(s)) {
                        continue;
                    }
                    String test = so.trim();
                    if (test.length() > 1) // null line contains |
                    {
                        addLine(file, so);
                    }
                }
            }
        }
    }

    protected void printTrackComments(PrintWriter file, RouteLocation rl, List<Car> carList, boolean isManifest) {
        Location location = rl.getLocation();
        if (location != null) {
            List<Track> tracks = location.getTracksByNameList(null);
            for (Track track : tracks) {
                if (isManifest && !track.isPrintManifestCommentEnabled() ||
                        !isManifest && !track.isPrintSwitchListCommentEnabled()) {
                    continue;
                }
                // any pick ups or set outs to this track?
                boolean pickup = false;
                boolean setout = false;
                for (Car car : carList) {
                    if (car.getRouteLocation() == rl && car.getTrack() != null && car.getTrack() == track) {
                        pickup = true;
                    }
                    if (car.getRouteDestination() == rl &&
                            car.getDestinationTrack() != null &&
                            car.getDestinationTrack() == track) {
                        setout = true;
                    }
                }
                // print the appropriate comment if there's one
                if (pickup && setout && !track.getCommentBothWithColor().equals(Track.NONE)) {
                    newLine(file, track.getCommentBothWithColor(), isManifest);
                } else if (pickup && !setout && !track.getCommentPickupWithColor().equals(Track.NONE)) {
                    newLine(file, track.getCommentPickupWithColor(), isManifest);
                } else if (!pickup && setout && !track.getCommentSetoutWithColor().equals(Track.NONE)) {
                    newLine(file, track.getCommentSetoutWithColor(), isManifest);
                }
            }
        }
    }

    int index = 0;

    /*
     * Used by two column format. Local moves (pulls and spots) are lined up
     * when using this format,
     */
    private String appendSetoutString(String s, List<Car> carList, RouteLocation rl, boolean local, boolean isManifest,
            boolean isTwoColumnTrack) {
        while (index < carList.size()) {
            Car car = carList.get(index++);
            if (local && car.isLocalMove()) {
                continue; // skip local moves
            }
            // car list is already sorted by destination track
            if (car.getRouteDestination() == rl) {
                String so = appendSetoutString(s, carList, rl, car, isManifest, isTwoColumnTrack);
                // check for utility car
                if (!so.equals(s)) {
                    return so;
                }
            }
        }
        // no set out for this line
        return s + VERTICAL_LINE_CHAR + padAndTruncate("", getLineLength(isManifest) / 2 - 1);
    }

    /*
     * Used by two column, track names shown in the columns.
     */
    private String appendSetoutString(String s, String trackName, List<Car> carList, RouteLocation rl,
            boolean isManifest, boolean isTwoColumnTrack) {
        for (Car car : carList) {
            if (!doneCars.contains(car) &&
                    car.getRouteDestination() == rl &&
                    trackName.equals(car.getSplitDestinationTrackName())) {
                doneCars.add(car);
                String so = appendSetoutString(s, carList, rl, car, isManifest, isTwoColumnTrack);
                // check for utility car
                if (!so.equals(s)) {
                    return so;
                }
            }
        }
        // no set out for this track
        return s + VERTICAL_LINE_CHAR + padAndTruncate("", getLineLength(isManifest) / 2 - 1);
    }

    /*
     * Appends to string the vertical line character, and the car set out
     * string. Used in two column format.
     */
    private String appendSetoutString(String s, List<Car> carList, RouteLocation rl, Car car, boolean isManifest,
            boolean isTwoColumnTrack) {
        _dropCars = true;
        String dropText;

        if (car.isUtility()) {
            dropText = setoutUtilityCars(carList, car, !LOCAL, isManifest, isTwoColumnTrack);
            if (dropText == null) {
                return s; // no changes to the input string
            }
        } else {
            dropText = dropCar(car, isManifest, isTwoColumnTrack).trim();
        }

        dropText = padAndTruncate(dropText.trim(), getLineLength(isManifest) / 2 - 1);
        dropText = formatColorString(dropText, car.isLocalMove() ? Setup.getLocalColor() : Setup.getDropColor());
        return s + VERTICAL_LINE_CHAR + dropText;
    }

    /**
     * Adds the car's pick up string to the output file using the truncated
     * manifest format
     *
     * @param file       Manifest or switch list File
     * @param car        The car being printed.
     * @param isManifest True if manifest, false if switch list.
     */
    protected void pickUpCarTruncated(PrintWriter file, Car car, boolean isManifest) {
        pickUpCar(file, car,
                new StringBuffer(padAndTruncateIfNeeded(Setup.getPickupCarPrefix(), Setup.getManifestPrefixLength())),
                Setup.getPickupTruncatedManifestMessageFormat(), isManifest);
    }

    /**
     * Adds the car's pick up string to the output file using the manifest or
     * switch list format
     *
     * @param file       Manifest or switch list File
     * @param car        The car being printed.
     * @param isManifest True if manifest, false if switch list.
     */
    protected void pickUpCar(PrintWriter file, Car car, boolean isManifest) {
        if (isManifest) {
            pickUpCar(file, car,
                    new StringBuffer(
                            padAndTruncateIfNeeded(Setup.getPickupCarPrefix(), Setup.getManifestPrefixLength())),
                    Setup.getPickupManifestMessageFormat(), isManifest);
        } else {
            pickUpCar(file, car, new StringBuffer(
                    padAndTruncateIfNeeded(Setup.getSwitchListPickupCarPrefix(), Setup.getSwitchListPrefixLength())),
                    Setup.getPickupSwitchListMessageFormat(), isManifest);
        }
    }

    private void pickUpCar(PrintWriter file, Car car, StringBuffer buf, String[] format, boolean isManifest) {
        if (car.isLocalMove()) {
            return; // print nothing local move, see dropCar
        }
        for (String attribute : format) {
            String s = getCarAttribute(car, attribute, PICKUP, !LOCAL);
            if (!checkStringLength(buf.toString() + s, isManifest)) {
                addLine(file, buf.toString());
                buf = new StringBuffer(TAB); // new line
            }
            buf.append(s);
        }
        String s = buf.toString();
        if (s.trim().length() != 0) {
            addLine(file, s);
        }
    }

    /**
     * Returns the pick up car string. Useful for frames like train conductor
     * and yardmaster.
     *
     * @param car              The car being printed.
     * @param isManifest       when true use manifest format, when false use
     *                         switch list format
     * @param isTwoColumnTrack True if printing using two column format sorted
     *                         by track name.
     * @return pick up car string
     */
    public String pickupCar(Car car, boolean isManifest, boolean isTwoColumnTrack) {
        StringBuffer buf = new StringBuffer();
        String[] format;
        if (isManifest && !isTwoColumnTrack) {
            format = Setup.getPickupManifestMessageFormat();
        } else if (!isManifest && !isTwoColumnTrack) {
            format = Setup.getPickupSwitchListMessageFormat();
        } else if (isManifest && isTwoColumnTrack) {
            format = Setup.getPickupTwoColumnByTrackManifestMessageFormat();
        } else {
            format = Setup.getPickupTwoColumnByTrackSwitchListMessageFormat();
        }
        for (String attribute : format) {
            buf.append(getCarAttribute(car, attribute, PICKUP, !LOCAL));
        }
        return buf.toString();
    }

    /**
     * Adds the car's set out string to the output file using the truncated
     * manifest format. Does not print out local moves. Local moves are only
     * shown on the switch list for that location.
     *
     * @param file       Manifest or switch list File
     * @param car        The car being printed.
     * @param isManifest True if manifest, false if switch list.
     */
    protected void truncatedDropCar(PrintWriter file, Car car, boolean isManifest) {
        // local move?
        if (car.isLocalMove()) {
            return; // yes, don't print local moves on train manifest
        }
        dropCar(file, car, new StringBuffer(Setup.getDropCarPrefix()), Setup.getDropTruncatedManifestMessageFormat(),
                false, isManifest);
    }

    /**
     * Adds the car's set out string to the output file using the manifest or
     * switch list format
     *
     * @param file       Manifest or switch list File
     * @param car        The car being printed.
     * @param isManifest True if manifest, false if switch list.
     */
    protected void dropCar(PrintWriter file, Car car, boolean isManifest) {
        boolean isLocal = car.isLocalMove();
        if (isManifest) {
            StringBuffer buf = new StringBuffer(
                    padAndTruncateIfNeeded(Setup.getDropCarPrefix(), Setup.getManifestPrefixLength()));
            String[] format = Setup.getDropManifestMessageFormat();
            if (isLocal) {
                buf = new StringBuffer(padAndTruncateIfNeeded(Setup.getLocalPrefix(), Setup.getManifestPrefixLength()));
                format = Setup.getLocalManifestMessageFormat();
            }
            dropCar(file, car, buf, format, isLocal, isManifest);
        } else {
            StringBuffer buf = new StringBuffer(
                    padAndTruncateIfNeeded(Setup.getSwitchListDropCarPrefix(), Setup.getSwitchListPrefixLength()));
            String[] format = Setup.getDropSwitchListMessageFormat();
            if (isLocal) {
                buf = new StringBuffer(
                        padAndTruncateIfNeeded(Setup.getSwitchListLocalPrefix(), Setup.getSwitchListPrefixLength()));
                format = Setup.getLocalSwitchListMessageFormat();
            }
            dropCar(file, car, buf, format, isLocal, isManifest);
        }
    }

    private void dropCar(PrintWriter file, Car car, StringBuffer buf, String[] format, boolean isLocal,
            boolean isManifest) {
        for (String attribute : format) {
            String s = getCarAttribute(car, attribute, !PICKUP, isLocal);
            if (!checkStringLength(buf.toString() + s, isManifest)) {
                addLine(file, buf.toString());
                buf = new StringBuffer(TAB); // new line
            }
            buf.append(s);
        }
        String s = buf.toString();
        if (!s.trim().isEmpty()) {
            addLine(file, s);
        }
    }

    /**
     * Returns the drop car string. Useful for frames like train conductor and
     * yardmaster.
     *
     * @param car              The car being printed.
     * @param isManifest       when true use manifest format, when false use
     *                         switch list format
     * @param isTwoColumnTrack True if printing using two column format.
     * @return drop car string
     */
    public String dropCar(Car car, boolean isManifest, boolean isTwoColumnTrack) {
        StringBuffer buf = new StringBuffer();
        String[] format;
        if (isManifest && !isTwoColumnTrack) {
            format = Setup.getDropManifestMessageFormat();
        } else if (!isManifest && !isTwoColumnTrack) {
            format = Setup.getDropSwitchListMessageFormat();
        } else if (isManifest && isTwoColumnTrack) {
            format = Setup.getDropTwoColumnByTrackManifestMessageFormat();
        } else {
            format = Setup.getDropTwoColumnByTrackSwitchListMessageFormat();
        }
        // TODO the Setup.Location doesn't work correctly for the conductor
        // window due to the fact that the car can be in the train and not
        // at its starting location.
        // Therefore we use the local true to disable it.
        boolean local = false;
        if (car.getTrack() == null) {
            local = true;
        }
        for (String attribute : format) {
            buf.append(getCarAttribute(car, attribute, !PICKUP, local));
        }
        return buf.toString();
    }

    /**
     * Returns the move car string. Useful for frames like train conductor and
     * yardmaster.
     *
     * @param car        The car being printed.
     * @param isManifest when true use manifest format, when false use switch
     *                   list format
     * @return move car string
     */
    public String localMoveCar(Car car, boolean isManifest) {
        StringBuffer buf = new StringBuffer();
        String[] format;
        if (isManifest) {
            format = Setup.getLocalManifestMessageFormat();
        } else {
            format = Setup.getLocalSwitchListMessageFormat();
        }
        for (String attribute : format) {
            buf.append(getCarAttribute(car, attribute, !PICKUP, LOCAL));
        }
        return buf.toString();
    }

    List<String> utilityCarTypes = new ArrayList<>();
    private static final int UTILITY_CAR_COUNT_FIELD_SIZE = 3;

    /**
     * Add a list of utility cars scheduled for pick up from the route location
     * to the output file. The cars are blocked by destination.
     *
     * @param file       Manifest or Switch List File.
     * @param carList    List of cars for this train.
     * @param car        The utility car.
     * @param isTruncate True if manifest is to be truncated
     * @param isManifest True if manifest, false if switch list.
     */
    protected void pickupUtilityCars(PrintWriter file, List<Car> carList, Car car, boolean isTruncate,
            boolean isManifest) {
        // list utility cars by type, track, length, and load
        String[] format;
        if (isManifest) {
            format = Setup.getPickupUtilityManifestMessageFormat();
        } else {
            format = Setup.getPickupUtilitySwitchListMessageFormat();
        }
        if (isTruncate && isManifest) {
            format = Setup.createTruncatedManifestMessageFormat(format);
        }
        int count = countUtilityCars(format, carList, car, PICKUP);
        if (count == 0) {
            return; // already printed out this car type
        }
        pickUpCar(file, car,
                new StringBuffer(padAndTruncateIfNeeded(Setup.getPickupCarPrefix(),
                        isManifest ? Setup.getManifestPrefixLength() : Setup.getSwitchListPrefixLength()) +
                        SPACE +
                        padString(Integer.toString(count), UTILITY_CAR_COUNT_FIELD_SIZE)),
                format, isManifest);
    }

    /**
     * Add a list of utility cars scheduled for drop at the route location to
     * the output file.
     *
     * @param file       Manifest or Switch List File.
     * @param carList    List of cars for this train.
     * @param car        The utility car.
     * @param isTruncate True if manifest is to be truncated
     * @param isManifest True if manifest, false if switch list.
     */
    protected void setoutUtilityCars(PrintWriter file, List<Car> carList, Car car, boolean isTruncate,
            boolean isManifest) {
        boolean isLocal = car.isLocalMove();
        StringBuffer buf;
        String[] format;
        if (isLocal && isManifest) {
            buf = new StringBuffer(padAndTruncateIfNeeded(Setup.getLocalPrefix(), Setup.getManifestPrefixLength()));
            format = Setup.getLocalUtilityManifestMessageFormat();
        } else if (!isLocal && isManifest) {
            buf = new StringBuffer(padAndTruncateIfNeeded(Setup.getDropCarPrefix(), Setup.getManifestPrefixLength()));
            format = Setup.getDropUtilityManifestMessageFormat();
        } else if (isLocal && !isManifest) {
            buf = new StringBuffer(
                    padAndTruncateIfNeeded(Setup.getSwitchListLocalPrefix(), Setup.getSwitchListPrefixLength()));
            format = Setup.getLocalUtilitySwitchListMessageFormat();
        } else {
            buf = new StringBuffer(
                    padAndTruncateIfNeeded(Setup.getSwitchListDropCarPrefix(), Setup.getSwitchListPrefixLength()));
            format = Setup.getDropUtilitySwitchListMessageFormat();
        }
        if (isTruncate && isManifest) {
            format = Setup.createTruncatedManifestMessageFormat(format);
        }

        int count = countUtilityCars(format, carList, car, !PICKUP);
        if (count == 0) {
            return; // already printed out this car type
        }
        buf.append(SPACE + padString(Integer.toString(count), UTILITY_CAR_COUNT_FIELD_SIZE));
        dropCar(file, car, buf, format, isLocal, isManifest);
    }

    public String pickupUtilityCars(List<Car> carList, Car car, boolean isManifest, boolean isTwoColumnTrack) {
        int count = countPickupUtilityCars(carList, car, isManifest);
        if (count == 0) {
            return null;
        }
        String[] format;
        if (isManifest && !isTwoColumnTrack) {
            format = Setup.getPickupUtilityManifestMessageFormat();
        } else if (!isManifest && !isTwoColumnTrack) {
            format = Setup.getPickupUtilitySwitchListMessageFormat();
        } else if (isManifest && isTwoColumnTrack) {
            format = Setup.getPickupTwoColumnByTrackUtilityManifestMessageFormat();
        } else {
            format = Setup.getPickupTwoColumnByTrackUtilitySwitchListMessageFormat();
        }
        StringBuffer buf = new StringBuffer(SPACE + padString(Integer.toString(count), UTILITY_CAR_COUNT_FIELD_SIZE));
        for (String attribute : format) {
            buf.append(getCarAttribute(car, attribute, PICKUP, !LOCAL));
        }
        return buf.toString();
    }

    public int countPickupUtilityCars(List<Car> carList, Car car, boolean isManifest) {
        // list utility cars by type, track, length, and load
        String[] format;
        if (isManifest) {
            format = Setup.getPickupUtilityManifestMessageFormat();
        } else {
            format = Setup.getPickupUtilitySwitchListMessageFormat();
        }
        return countUtilityCars(format, carList, car, PICKUP);
    }

    /**
     * For the Conductor and Yardmaster windows.
     *
     * @param carList    List of cars for this train.
     * @param car        The utility car.
     * @param isLocal    True if local move.
     * @param isManifest True if manifest, false if switch list.
     * @return A string representing the work of identical utility cars.
     */
    public String setoutUtilityCars(List<Car> carList, Car car, boolean isLocal, boolean isManifest) {
        return setoutUtilityCars(carList, car, isLocal, isManifest, !IS_TWO_COLUMN_TRACK);
    }

    protected String setoutUtilityCars(List<Car> carList, Car car, boolean isLocal, boolean isManifest,
            boolean isTwoColumnTrack) {
        int count = countSetoutUtilityCars(carList, car, isLocal, isManifest);
        if (count == 0) {
            return null;
        }
        // list utility cars by type, track, length, and load
        String[] format;
        if (isLocal && isManifest && !isTwoColumnTrack) {
            format = Setup.getLocalUtilityManifestMessageFormat();
        } else if (isLocal && !isManifest && !isTwoColumnTrack) {
            format = Setup.getLocalUtilitySwitchListMessageFormat();
        } else if (!isLocal && !isManifest && !isTwoColumnTrack) {
            format = Setup.getDropUtilitySwitchListMessageFormat();
        } else if (!isLocal && isManifest && !isTwoColumnTrack) {
            format = Setup.getDropUtilityManifestMessageFormat();
        } else if (isManifest && isTwoColumnTrack) {
            format = Setup.getDropTwoColumnByTrackUtilityManifestMessageFormat();
        } else {
            format = Setup.getDropTwoColumnByTrackUtilitySwitchListMessageFormat();
        }
        StringBuffer buf = new StringBuffer(SPACE + padString(Integer.toString(count), UTILITY_CAR_COUNT_FIELD_SIZE));
        // TODO the Setup.Location doesn't work correctly for the conductor
        // window due to the fact that the car can be in the train and not
        // at its starting location.
        // Therefore we use the local true to disable it.
        if (car.getTrack() == null) {
            isLocal = true;
        }
        for (String attribute : format) {
            buf.append(getCarAttribute(car, attribute, !PICKUP, isLocal));
        }
        return buf.toString();
    }

    public int countSetoutUtilityCars(List<Car> carList, Car car, boolean isLocal, boolean isManifest) {
        // list utility cars by type, track, length, and load
        String[] format;
        if (isLocal && isManifest) {
            format = Setup.getLocalUtilityManifestMessageFormat();
        } else if (isLocal && !isManifest) {
            format = Setup.getLocalUtilitySwitchListMessageFormat();
        } else if (!isLocal && !isManifest) {
            format = Setup.getDropUtilitySwitchListMessageFormat();
        } else {
            format = Setup.getDropUtilityManifestMessageFormat();
        }
        return countUtilityCars(format, carList, car, !PICKUP);
    }

    /**
     * Scans the car list for utility cars that have the same attributes as the
     * car provided. Returns 0 if this car type has already been processed,
     * otherwise the number of cars with the same attribute.
     *
     * @param format   Message format.
     * @param carList  List of cars for this train
     * @param car      The utility car.
     * @param isPickup True if pick up, false if set out.
     * @return 0 if the car type has already been processed
     */
    protected int countUtilityCars(String[] format, List<Car> carList, Car car, boolean isPickup) {
        int count = 0;
        // figure out if the user wants to show the car's length
        boolean showLength = showUtilityCarLength(format);
        // figure out if the user want to show the car's loads
        boolean showLoad = showUtilityCarLoad(format);
        boolean showLocation = false;
        boolean showDestination = false;
        String carType = car.getTypeName().split(HYPHEN)[0];
        String carAttributes;
        // Note for car pick up: type, id, track name. For set out type, track
        // name, id (reversed).
        if (isPickup) {
            carAttributes = carType + car.getRouteLocationId() + car.getSplitTrackName();
            showDestination = showUtilityCarDestination(format);
            if (showDestination) {
                carAttributes = carAttributes + car.getRouteDestinationId();
            }
        } else {
            // set outs and local moves
            carAttributes = carType + car.getSplitDestinationTrackName() + car.getRouteDestinationId();
            showLocation = showUtilityCarLocation(format);
            if (showLocation && car.getTrack() != null) {
                carAttributes = carAttributes + car.getRouteLocationId();
            }
            if (car.isLocalMove()) {
                carAttributes = carAttributes + car.getSplitTrackName();
            }
        }
        if (showLength) {
            carAttributes = carAttributes + car.getLength();
        }
        if (showLoad) {
            carAttributes = carAttributes + car.getLoadName();
        }
        // have we already done this car type?
        if (!utilityCarTypes.contains(carAttributes)) {
            utilityCarTypes.add(carAttributes); // don't do this type again
            // determine how many cars of this type
            for (Car c : carList) {
                if (!c.isUtility()) {
                    continue;
                }
                String cType = c.getTypeName().split(HYPHEN)[0];
                if (!cType.equals(carType)) {
                    continue;
                }
                if (showLength && !c.getLength().equals(car.getLength())) {
                    continue;
                }
                if (showLoad && !c.getLoadName().equals(car.getLoadName())) {
                    continue;
                }
                if (showLocation && !c.getRouteLocationId().equals(car.getRouteLocationId())) {
                    continue;
                }
                if (showDestination && !c.getRouteDestinationId().equals(car.getRouteDestinationId())) {
                    continue;
                }
                if (car.isLocalMove() ^ c.isLocalMove()) {
                    continue;
                }
                if (isPickup &&
                        c.getRouteLocation() == car.getRouteLocation() &&
                        c.getSplitTrackName().equals(car.getSplitTrackName())) {
                    count++;
                }
                if (!isPickup &&
                        c.getRouteDestination() == car.getRouteDestination() &&
                        c.getSplitDestinationTrackName().equals(car.getSplitDestinationTrackName()) &&
                        (c.getSplitTrackName().equals(car.getSplitTrackName()) || !c.isLocalMove())) {
                    count++;
                }
            }
        }
        return count;
    }

    public void clearUtilityCarTypes() {
        utilityCarTypes.clear();
    }

    private boolean showUtilityCarLength(String[] mFormat) {
        return showUtilityCarAttribute(Setup.LENGTH, mFormat);
    }

    private boolean showUtilityCarLoad(String[] mFormat) {
        return showUtilityCarAttribute(Setup.LOAD, mFormat);
    }

    private boolean showUtilityCarLocation(String[] mFormat) {
        return showUtilityCarAttribute(Setup.LOCATION, mFormat);
    }

    private boolean showUtilityCarDestination(String[] mFormat) {
        return showUtilityCarAttribute(Setup.DESTINATION, mFormat) ||
                showUtilityCarAttribute(Setup.DEST_TRACK, mFormat);
    }

    private boolean showUtilityCarAttribute(String string, String[] mFormat) {
        for (String s : mFormat) {
            if (s.equals(string)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Writes a line to the build report file
     *
     * @param file   build report file
     * @param level  print level
     * @param string string to write
     */
    protected static void addLine(PrintWriter file, String level, String string) {
        log.debug("addLine: {}", string);
        if (file != null) {
            String[] lines = string.split(NEW_LINE);
            for (String line : lines) {
                printLine(file, level, line);
            }
        }
    }

    // only used by build report
    private static void printLine(PrintWriter file, String level, String string) {
        int lineLengthMax = getLineLength(Setup.PORTRAIT, Setup.MONOSPACED, Font.PLAIN, Setup.getBuildReportFontSize());
        if (string.length() > lineLengthMax) {
            String[] words = string.split(SPACE);
            StringBuffer sb = new StringBuffer();
            for (String word : words) {
                if (sb.length() + word.length() < lineLengthMax) {
                    sb.append(word + SPACE);
                } else {
                    file.println(level + BUILD_REPORT_CHAR + SPACE + sb.toString());
                    sb = new StringBuffer(word + SPACE);
                }
            }
            string = sb.toString();
        }
        file.println(level + BUILD_REPORT_CHAR + SPACE + string);
    }

    /**
     * Writes string to file. No line length wrap or protection.
     *
     * @param file   The File to write to.
     * @param string The string to write.
     */
    protected void addLine(PrintWriter file, String string) {
        log.debug("addLine: {}", string);
        if (file != null) {
            file.println(string);
        }
    }

    /**
     * Writes a string to a file. Checks for string length, and will
     * automatically wrap lines.
     *
     * @param file       The File to write to.
     * @param string     The string to write.
     * @param isManifest set true for manifest page orientation, false for
     *                   switch list orientation
     */
    protected void newLine(PrintWriter file, String string, boolean isManifest) {
        String[] lines = string.split(NEW_LINE);
        for (String line : lines) {
            String[] words = line.split(SPACE);
            StringBuffer sb = new StringBuffer();
            for (String word : words) {
                if (checkStringLength(sb.toString() + word, isManifest)) {
                    sb.append(word + SPACE);
                } else {
                    sb.setLength(sb.length() - 1); // remove last space added to string
                    addLine(file, sb.toString());
                    sb = new StringBuffer(word + SPACE);
                }
            }
            if (sb.length() > 0) {
                sb.setLength(sb.length() - 1); // remove last space added to string
            }
            addLine(file, sb.toString());
        }
    }

    /**
     * Adds a blank line to the file.
     *
     * @param file The File to write to.
     */
    protected void newLine(PrintWriter file) {
        file.println(BLANK_LINE);
    }

    /**
     * Splits a string (example-number) as long as the second part of the string
     * is an integer or if the first character after the hyphen is a left
     * parenthesis "(".
     *
     * @param name The string to split if necessary.
     * @return First half of the string.
     */
    public static String splitString(String name) {
        String[] splitname = name.split(HYPHEN);
        // is the hyphen followed by a number or left parenthesis?
        if (splitname.length > 1 && !splitname[1].startsWith("(")) {
            try {
                Integer.parseInt(splitname[1]);
            } catch (NumberFormatException e) {
                // no return full name
                return name.trim();
            }
        }
        return splitname[0].trim();
    }

    /**
     * Splits a string if there's a hyphen followed by a left parenthesis "-(".
     *
     * @return First half of the string.
     */
    private static String splitStringLeftParenthesis(String name) {
        String[] splitname = name.split(HYPHEN);
        if (splitname.length > 1 && splitname[1].startsWith("(")) {
            return splitname[0].trim();
        }
        return name.trim();
    }

    // returns true if there's work at location
    protected boolean isThereWorkAtLocation(List<Car> carList, List<Engine> engList, RouteLocation rl) {
        if (carList != null) {
            for (Car car : carList) {
                if (car.getRouteLocation() == rl || car.getRouteDestination() == rl) {
                    return true;
                }
            }
        }
        if (engList != null) {
            for (Engine eng : engList) {
                if (eng.getRouteLocation() == rl || eng.getRouteDestination() == rl) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * returns true if the train has work at the location
     *
     * @param train    The Train.
     * @param location The Location.
     * @return true if the train has work at the location
     */
    public static boolean isThereWorkAtLocation(Train train, Location location) {
        if (isThereWorkAtLocation(train, location, InstanceManager.getDefault(CarManager.class).getList(train))) {
            return true;
        }
        if (isThereWorkAtLocation(train, location, InstanceManager.getDefault(EngineManager.class).getList(train))) {
            return true;
        }
        return false;
    }

    private static boolean isThereWorkAtLocation(Train train, Location location, List<? extends RollingStock> list) {
        for (RollingStock rs : list) {
            if ((rs.getRouteLocation() != null &&
                    rs.getTrack() != null &&
                    rs.getRouteLocation().getSplitName()
                            .equals(location.getSplitName())) ||
                    (rs.getRouteDestination() != null &&
                            rs.getRouteDestination().getSplitName().equals(location.getSplitName()))) {
                return true;
            }
        }
        return false;
    }

    protected void addCarsLocationUnknown(PrintWriter file, boolean isManifest) {
        List<Car> cars = carManager.getCarsLocationUnknown();
        if (cars.size() == 0) {
            return; // no cars to search for!
        }
        newLine(file);
        newLine(file, Setup.getMiaComment(), isManifest);
        for (Car car : cars) {
            addSearchForCar(file, car);
        }
    }

    private void addSearchForCar(PrintWriter file, Car car) {
        StringBuffer buf = new StringBuffer();
        String[] format = Setup.getMissingCarMessageFormat();
        for (String attribute : format) {
            buf.append(getCarAttribute(car, attribute, false, false));
        }
        addLine(file, buf.toString());
    }

    /*
     * Gets an engine's attribute String. Returns empty if there isn't an
     * attribute and not using the tabular feature. isPickup true when engine is
     * being picked up.
     */
    private String getEngineAttribute(Engine engine, String attribute, boolean isPickup) {
        if (!attribute.equals(Setup.BLANK)) {
            String s = SPACE + getEngineAttrib(engine, attribute, isPickup);
            if (Setup.isTabEnabled() || !s.trim().isEmpty()) {
                return s;
            }
        }
        return "";
    }

    /*
     * Can not use String case statement since Setup.MODEL, etc, are not fixed
     * strings.
     */
    private String getEngineAttrib(Engine engine, String attribute, boolean isPickup) {
        if (attribute.equals(Setup.MODEL)) {
            return padAndTruncateIfNeeded(splitStringLeftParenthesis(engine.getModel()),
                    InstanceManager.getDefault(EngineModels.class).getMaxNameLength());
        } else if (attribute.equals(Setup.HP)) {
            return padAndTruncateIfNeeded(engine.getHp(), 5) +
                    (Setup.isPrintHeadersEnabled() ? "" : TrainManifestHeaderText.getStringHeader_Hp());
        } else if (attribute.equals(Setup.CONSIST)) {
            return padAndTruncateIfNeeded(engine.getConsistName(),
                    InstanceManager.getDefault(ConsistManager.class).getMaxNameLength());
        } else if (attribute.equals(Setup.DCC_ADDRESS)) {
            return padAndTruncateIfNeeded(engine.getDccAddress(),
                    TrainManifestHeaderText.getStringHeader_DCC_Address().length());
        } else if (attribute.equals(Setup.COMMENT)) {
            return padAndTruncateIfNeeded(engine.getComment(), engineManager.getMaxCommentLength());
        }
        return getRollingStockAttribute(engine, attribute, isPickup, false);
    }

    /*
     * Gets a car's attribute String. Returns empty if there isn't an attribute
     * and not using the tabular feature. isPickup true when car is being picked
     * up. isLocal true when car is performing a local move.
     */
    private String getCarAttribute(Car car, String attribute, boolean isPickup, boolean isLocal) {
        if (!attribute.equals(Setup.BLANK)) {
            String s = SPACE + getCarAttrib(car, attribute, isPickup, isLocal);
            if (Setup.isTabEnabled() || !s.trim().isEmpty()) {
                return s;
            }
        }
        return "";
    }

    private String getCarAttrib(Car car, String attribute, boolean isPickup, boolean isLocal) {
        if (attribute.equals(Setup.LOAD)) {
            return ((car.isCaboose() && !Setup.isPrintCabooseLoadEnabled()) ||
                    (car.isPassenger() && !Setup.isPrintPassengerLoadEnabled()))
                            ? padAndTruncateIfNeeded("",
                                    InstanceManager.getDefault(CarLoads.class).getMaxNameLength())
                            : padAndTruncateIfNeeded(car.getLoadName().split(HYPHEN)[0],
                                    InstanceManager.getDefault(CarLoads.class).getMaxNameLength());
        } else if (attribute.equals(Setup.LOAD_TYPE)) {
            return padAndTruncateIfNeeded(car.getLoadType(),
                    TrainManifestHeaderText.getStringHeader_Load_Type().length());
        } else if (attribute.equals(Setup.HAZARDOUS)) {
            return (car.isHazardous() ? Setup.getHazardousMsg()
                    : padAndTruncateIfNeeded("", Setup.getHazardousMsg().length()));
        } else if (attribute.equals(Setup.DROP_COMMENT)) {
            return padAndTruncateIfNeeded(car.getDropComment(),
                    InstanceManager.getDefault(CarLoads.class).getMaxLoadCommentLength());
        } else if (attribute.equals(Setup.PICKUP_COMMENT)) {
            return padAndTruncateIfNeeded(car.getPickupComment(),
                    InstanceManager.getDefault(CarLoads.class).getMaxLoadCommentLength());
        } else if (attribute.equals(Setup.KERNEL)) {
            return padAndTruncateIfNeeded(car.getKernelName(),
                    InstanceManager.getDefault(KernelManager.class).getMaxNameLength());
        } else if (attribute.equals(Setup.KERNEL_SIZE)) {
            if (car.isLead()) {
                return padAndTruncateIfNeeded(Integer.toString(car.getKernel().getSize()), 2);
            } else {
                return SPACE + SPACE; // assumes that kernel size is 99 or less
            }
        } else if (attribute.equals(Setup.RWE)) {
            if (!car.getReturnWhenEmptyDestinationName().equals(Car.NONE)) {
                // format RWE destination and track name
                String rweAndTrackName = car.getSplitReturnWhenEmptyDestinationName();
                if (!car.getReturnWhenEmptyDestTrackName().equals(Car.NONE)) {
                    rweAndTrackName = rweAndTrackName + "," + SPACE + car.getSplitReturnWhenEmptyDestinationTrackName();
                }
                return Setup.isPrintHeadersEnabled()
                        ? padAndTruncateIfNeeded(rweAndTrackName, locationManager.getMaxLocationAndTrackNameLength())
                        : padAndTruncateIfNeeded(
                                TrainManifestHeaderText.getStringHeader_RWE() + SPACE + rweAndTrackName,
                                locationManager.getMaxLocationAndTrackNameLength() +
                                        TrainManifestHeaderText.getStringHeader_RWE().length() +
                                        3);
            }
            return padAndTruncateIfNeeded("", locationManager.getMaxLocationAndTrackNameLength());
        } else if (attribute.equals(Setup.FINAL_DEST)) {
            return Setup.isPrintHeadersEnabled()
                    ? padAndTruncateIfNeeded(car.getSplitFinalDestinationName(),
                            locationManager.getMaxLocationNameLength())
                    : padAndTruncateIfNeeded(
                            TrainManifestText.getStringFinalDestination() +
                                    SPACE +
                                    car.getSplitFinalDestinationName(),
                            locationManager.getMaxLocationNameLength() +
                                    TrainManifestText.getStringFinalDestination().length() +
                                    1);
        } else if (attribute.equals(Setup.FINAL_DEST_TRACK)) {
            // format final destination and track name
            String FDAndTrackName = car.getSplitFinalDestinationName();
            if (!car.getFinalDestinationTrackName().equals(Car.NONE)) {
                FDAndTrackName = FDAndTrackName + "," + SPACE + car.getSplitFinalDestinationTrackName();
            }
            return Setup.isPrintHeadersEnabled()
                    ? padAndTruncateIfNeeded(FDAndTrackName, locationManager.getMaxLocationAndTrackNameLength() + 2)
                    : padAndTruncateIfNeeded(TrainManifestText.getStringFinalDestination() + SPACE + FDAndTrackName,
                            locationManager.getMaxLocationAndTrackNameLength() +
                                    TrainManifestText.getStringFinalDestination().length() +
                                    3);
        } else if (attribute.equals(Setup.DIVISION)) {
            return padAndTruncateIfNeeded(car.getDivisionName(),
                    InstanceManager.getDefault(DivisionManager.class).getMaxDivisionNameLength());
        } else if (attribute.equals(Setup.COMMENT)) {
            return padAndTruncateIfNeeded(car.getComment(), carManager.getMaxCommentLength());
        }
        return getRollingStockAttribute(car, attribute, isPickup, isLocal);
    }

    private String getRollingStockAttribute(RollingStock rs, String attribute, boolean isPickup, boolean isLocal) {
        try {
            if (attribute.equals(Setup.NUMBER)) {
                return padAndTruncateIfNeeded(splitString(rs.getNumber()), Control.max_len_string_print_road_number);
            } else if (attribute.equals(Setup.ROAD)) {
                String road = rs.getRoadName().split(HYPHEN)[0];
                return padAndTruncateIfNeeded(road, InstanceManager.getDefault(CarRoads.class).getMaxNameLength());
            } else if (attribute.equals(Setup.TYPE)) {
                String type = rs.getTypeName().split(HYPHEN)[0];
                return padAndTruncateIfNeeded(type, InstanceManager.getDefault(CarTypes.class).getMaxNameLength());
            } else if (attribute.equals(Setup.LENGTH)) {
                return padAndTruncateIfNeeded(rs.getLength() + Setup.getLengthUnitAbv(),
                        InstanceManager.getDefault(CarLengths.class).getMaxNameLength());
            } else if (attribute.equals(Setup.WEIGHT)) {
                return padAndTruncateIfNeeded(Integer.toString(rs.getAdjustedWeightTons()),
                        Control.max_len_string_weight_name) +
                        (Setup.isPrintHeadersEnabled() ? "" : TrainManifestHeaderText.getStringHeader_Weight());
            } else if (attribute.equals(Setup.COLOR)) {
                return padAndTruncateIfNeeded(rs.getColor(),
                        InstanceManager.getDefault(CarColors.class).getMaxNameLength());
            } else if (((attribute.equals(Setup.LOCATION)) && (isPickup || isLocal)) ||
                    (attribute.equals(Setup.TRACK) && isPickup)) {
                return Setup.isPrintHeadersEnabled()
                        ? padAndTruncateIfNeeded(rs.getSplitTrackName(),
                                locationManager.getMaxTrackNameLength())
                        : padAndTruncateIfNeeded(
                                TrainManifestText.getStringFrom() + SPACE + rs.getSplitTrackName(),
                                TrainManifestText.getStringFrom().length() +
                                        locationManager.getMaxTrackNameLength() +
                                        1);
            } else if (attribute.equals(Setup.LOCATION) && !isPickup && !isLocal) {
                return Setup.isPrintHeadersEnabled()
                        ? padAndTruncateIfNeeded(rs.getSplitLocationName(),
                                locationManager.getMaxLocationNameLength())
                        : padAndTruncateIfNeeded(
                                TrainManifestText.getStringFrom() + SPACE + rs.getSplitLocationName(),
                                locationManager.getMaxLocationNameLength() +
                                        TrainManifestText.getStringFrom().length() +
                                        1);
            } else if (attribute.equals(Setup.DESTINATION) && isPickup) {
                if (Setup.isPrintHeadersEnabled()) {
                    return padAndTruncateIfNeeded(rs.getSplitDestinationName(),
                            locationManager.getMaxLocationNameLength());
                }
                if (Setup.isTabEnabled()) {
                    return padAndTruncateIfNeeded(
                            TrainManifestText.getStringDest() + SPACE + rs.getSplitDestinationName(),
                            TrainManifestText.getStringDest().length() +
                                    locationManager.getMaxLocationNameLength() +
                                    1);
                } else {
                    return TrainManifestText.getStringDestination() +
                            SPACE +
                            rs.getSplitDestinationName();
                }
            } else if ((attribute.equals(Setup.DESTINATION) || attribute.equals(Setup.TRACK)) && !isPickup) {
                return Setup.isPrintHeadersEnabled()
                        ? padAndTruncateIfNeeded(rs.getSplitDestinationTrackName(),
                                locationManager.getMaxTrackNameLength())
                        : padAndTruncateIfNeeded(
                                TrainManifestText.getStringTo() +
                                        SPACE +
                                        rs.getSplitDestinationTrackName(),
                                locationManager.getMaxTrackNameLength() +
                                        TrainManifestText.getStringTo().length() +
                                        1);
            } else if (attribute.equals(Setup.DEST_TRACK)) {
                // format destination name and destination track name
                String destAndTrackName =
                        rs.getSplitDestinationName() + "," + SPACE + rs.getSplitDestinationTrackName();
                return Setup.isPrintHeadersEnabled()
                        ? padAndTruncateIfNeeded(destAndTrackName,
                                locationManager.getMaxLocationAndTrackNameLength() + 2)
                        : padAndTruncateIfNeeded(TrainManifestText.getStringDest() + SPACE + destAndTrackName,
                                locationManager.getMaxLocationAndTrackNameLength() +
                                        TrainManifestText.getStringDest().length() +
                                        3);
            } else if (attribute.equals(Setup.OWNER)) {
                return padAndTruncateIfNeeded(rs.getOwnerName(),
                        InstanceManager.getDefault(CarOwners.class).getMaxNameLength());
            } // the three utility attributes that don't get printed but need to
              // be tabbed out
            else if (attribute.equals(Setup.NO_NUMBER)) {
                return padAndTruncateIfNeeded("",
                        Control.max_len_string_print_road_number - (UTILITY_CAR_COUNT_FIELD_SIZE + 1));
            } else if (attribute.equals(Setup.NO_ROAD)) {
                return padAndTruncateIfNeeded("", InstanceManager.getDefault(CarRoads.class).getMaxNameLength());
            } else if (attribute.equals(Setup.NO_COLOR)) {
                return padAndTruncateIfNeeded("", InstanceManager.getDefault(CarColors.class).getMaxNameLength());
            } // there are four truncated manifest attributes
            else if (attribute.equals(Setup.NO_DEST_TRACK)) {
                return Setup.isPrintHeadersEnabled()
                        ? padAndTruncateIfNeeded("", locationManager.getMaxLocationAndTrackNameLength() + 1)
                        : "";
            } else if ((attribute.equals(Setup.NO_LOCATION) && !isPickup) ||
                    (attribute.equals(Setup.NO_DESTINATION) && isPickup)) {
                return Setup.isPrintHeadersEnabled()
                        ? padAndTruncateIfNeeded("", locationManager.getMaxLocationNameLength())
                        : "";
            } else if (attribute.equals(Setup.NO_TRACK) ||
                    attribute.equals(Setup.NO_LOCATION) ||
                    attribute.equals(Setup.NO_DESTINATION)) {
                return Setup.isPrintHeadersEnabled()
                        ? padAndTruncateIfNeeded("", locationManager.getMaxTrackNameLength())
                        : "";
            } else if (attribute.equals(Setup.TAB)) {
                return createTabIfNeeded(Setup.getTab1Length() - 1);
            } else if (attribute.equals(Setup.TAB2)) {
                return createTabIfNeeded(Setup.getTab2Length() - 1);
            } else if (attribute.equals(Setup.TAB3)) {
                return createTabIfNeeded(Setup.getTab3Length() - 1);
            }
            // something isn't right!
            return Bundle.getMessage("ErrorPrintOptions", attribute);

        } catch (ArrayIndexOutOfBoundsException e) {
            if (attribute.equals(Setup.ROAD)) {
                return padAndTruncateIfNeeded("", InstanceManager.getDefault(CarRoads.class).getMaxNameLength());
            } else if (attribute.equals(Setup.TYPE)) {
                return padAndTruncateIfNeeded("", InstanceManager.getDefault(CarTypes.class).getMaxNameLength());
            }
            // something isn't right!
            return Bundle.getMessage("ErrorPrintOptions", attribute);
        }
    }

    /**
     * Two column header format. Left side pick ups, right side set outs
     *
     * @param file       Manifest or switch list File.
     * @param isManifest True if manifest, false if switch list.
     */
    public void printEngineHeader(PrintWriter file, boolean isManifest) {
        int lineLength = getLineLength(isManifest);
        printHorizontalLine(file, 0, lineLength);
        if (!Setup.isPrintHeadersEnabled()) {
            return;
        }
        if (!Setup.getPickupEnginePrefix().trim().isEmpty() || !Setup.getDropEnginePrefix().trim().isEmpty()) {
            // center engine pick up and set out text
            String s = padAndTruncate(tabString(Setup.getPickupEnginePrefix().trim(),
                    lineLength / 4 - Setup.getPickupEnginePrefix().length() / 2), lineLength / 2) +
                    VERTICAL_LINE_CHAR +
                    tabString(Setup.getDropEnginePrefix(), lineLength / 4 - Setup.getDropEnginePrefix().length() / 2);
            s = padAndTruncate(s, lineLength);
            addLine(file, s);
            printHorizontalLine(file, 0, lineLength);
        }

        String s = padAndTruncate(getPickupEngineHeader(), lineLength / 2);
        s = padAndTruncate(s + VERTICAL_LINE_CHAR + getDropEngineHeader(), lineLength);
        addLine(file, s);
        printHorizontalLine(file, 0, lineLength);
    }

    public void printPickupEngineHeader(PrintWriter file, boolean isManifest) {
        int lineLength = getLineLength(isManifest);
        printHorizontalLine(file, 0, lineLength);
        String s = padAndTruncate(createTabIfNeeded(Setup.getManifestPrefixLength() + 1) + getPickupEngineHeader(),
                lineLength);
        addLine(file, s);
        printHorizontalLine(file, 0, lineLength);
    }

    public void printDropEngineHeader(PrintWriter file, boolean isManifest) {
        int lineLength = getLineLength(isManifest);
        printHorizontalLine(file, 0, lineLength);
        String s = padAndTruncate(createTabIfNeeded(Setup.getManifestPrefixLength() + 1) + getDropEngineHeader(),
                lineLength);
        addLine(file, s);
        printHorizontalLine(file, 0, lineLength);
    }

    /**
     * Prints the two column header for cars. Left side pick ups, right side set
     * outs.
     *
     * @param file             Manifest or Switch List File
     * @param isManifest       True if manifest, false if switch list.
     * @param isTwoColumnTrack True if two column format using track names.
     */
    public void printCarHeader(PrintWriter file, boolean isManifest, boolean isTwoColumnTrack) {
        int lineLength = getLineLength(isManifest);
        printHorizontalLine(file, 0, lineLength);
        if (!Setup.isPrintHeadersEnabled()) {
            return;
        }
        // center pick up and set out text
        String s = padAndTruncate(
                tabString(Setup.getPickupCarPrefix(), lineLength / 4 - Setup.getPickupCarPrefix().length() / 2),
                lineLength / 2) +
                VERTICAL_LINE_CHAR +
                tabString(Setup.getDropCarPrefix(), lineLength / 4 - Setup.getDropCarPrefix().length() / 2);
        s = padAndTruncate(s, lineLength);
        addLine(file, s);
        printHorizontalLine(file, 0, lineLength);

        s = padAndTruncate(getPickupCarHeader(isManifest, isTwoColumnTrack), lineLength / 2);
        s = padAndTruncate(s + VERTICAL_LINE_CHAR + getDropCarHeader(isManifest, isTwoColumnTrack), lineLength);
        addLine(file, s);
        printHorizontalLine(file, 0, lineLength);
    }

    public void printPickupCarHeader(PrintWriter file, boolean isManifest, boolean isTwoColumnTrack) {
        if (!Setup.isPrintHeadersEnabled()) {
            return;
        }
        printHorizontalLine(file, isManifest);
        String s = padAndTruncate(createTabIfNeeded(Setup.getManifestPrefixLength() + 1) +
                getPickupCarHeader(isManifest, isTwoColumnTrack), getLineLength(isManifest));
        addLine(file, s);
        printHorizontalLine(file, isManifest);
    }

    public void printDropCarHeader(PrintWriter file, boolean isManifest, boolean isTwoColumnTrack) {
        if (!Setup.isPrintHeadersEnabled() || getDropCarHeader(isManifest, isTwoColumnTrack).trim().isEmpty()) {
            return;
        }
        printHorizontalLine(file, isManifest);
        String s = padAndTruncate(
                createTabIfNeeded(Setup.getManifestPrefixLength() + 1) + getDropCarHeader(isManifest, isTwoColumnTrack),
                getLineLength(isManifest));
        addLine(file, s);
        printHorizontalLine(file, isManifest);
    }

    public void printLocalCarMoveHeader(PrintWriter file, boolean isManifest) {
        if (!Setup.isPrintHeadersEnabled()) {
            return;
        }
        printHorizontalLine(file, isManifest);
        String s = padAndTruncate(
                createTabIfNeeded(Setup.getManifestPrefixLength() + 1) + getLocalMoveHeader(isManifest),
                getLineLength(isManifest));
        addLine(file, s);
        printHorizontalLine(file, isManifest);
    }

    public String getPickupEngineHeader() {
        return getHeader(Setup.getPickupEngineMessageFormat(), PICKUP, !LOCAL, ENGINE);
    }

    public String getDropEngineHeader() {
        return getHeader(Setup.getDropEngineMessageFormat(), !PICKUP, !LOCAL, ENGINE);
    }

    public String getPickupCarHeader(boolean isManifest, boolean isTwoColumnTrack) {
        if (isManifest && !isTwoColumnTrack) {
            return getHeader(Setup.getPickupManifestMessageFormat(), PICKUP, !LOCAL, !ENGINE);
        } else if (!isManifest && !isTwoColumnTrack) {
            return getHeader(Setup.getPickupSwitchListMessageFormat(), PICKUP, !LOCAL, !ENGINE);
        } else if (isManifest && isTwoColumnTrack) {
            return getHeader(Setup.getPickupTwoColumnByTrackManifestMessageFormat(), PICKUP, !LOCAL, !ENGINE);
        } else {
            return getHeader(Setup.getPickupTwoColumnByTrackSwitchListMessageFormat(), PICKUP, !LOCAL, !ENGINE);
        }
    }

    public String getDropCarHeader(boolean isManifest, boolean isTwoColumnTrack) {
        if (isManifest && !isTwoColumnTrack) {
            return getHeader(Setup.getDropManifestMessageFormat(), !PICKUP, !LOCAL, !ENGINE);
        } else if (!isManifest && !isTwoColumnTrack) {
            return getHeader(Setup.getDropSwitchListMessageFormat(), !PICKUP, !LOCAL, !ENGINE);
        } else if (isManifest && isTwoColumnTrack) {
            return getHeader(Setup.getDropTwoColumnByTrackManifestMessageFormat(), !PICKUP, !LOCAL, !ENGINE);
        } else {
            return getHeader(Setup.getDropTwoColumnByTrackSwitchListMessageFormat(), !PICKUP, !LOCAL, !ENGINE);
        }
    }

    public String getLocalMoveHeader(boolean isManifest) {
        if (isManifest) {
            return getHeader(Setup.getLocalManifestMessageFormat(), !PICKUP, LOCAL, !ENGINE);
        } else {
            return getHeader(Setup.getLocalSwitchListMessageFormat(), !PICKUP, LOCAL, !ENGINE);
        }
    }

    private String getHeader(String[] format, boolean isPickup, boolean isLocal, boolean isEngine) {
        StringBuffer buf = new StringBuffer();
        for (String attribute : format) {
            if (attribute.equals(Setup.BLANK)) {
                continue;
            }
            if (attribute.equals(Setup.ROAD)) {
                buf.append(padAndTruncateIfNeeded(TrainManifestHeaderText.getStringHeader_Road(),
                        InstanceManager.getDefault(CarRoads.class).getMaxNameLength()) + SPACE);
            } else if (attribute.equals(Setup.NUMBER) && !isEngine) {
                buf.append(padAndTruncateIfNeeded(TrainManifestHeaderText.getStringHeader_Number(),
                        Control.max_len_string_print_road_number) + SPACE);
            } else if (attribute.equals(Setup.NUMBER) && isEngine) {
                buf.append(padAndTruncateIfNeeded(TrainManifestHeaderText.getStringHeader_EngineNumber(),
                        Control.max_len_string_print_road_number) + SPACE);
            } else if (attribute.equals(Setup.TYPE)) {
                buf.append(padAndTruncateIfNeeded(TrainManifestHeaderText.getStringHeader_Type(),
                        InstanceManager.getDefault(CarTypes.class).getMaxNameLength()) + SPACE);
            } else if (attribute.equals(Setup.MODEL)) {
                buf.append(padAndTruncateIfNeeded(TrainManifestHeaderText.getStringHeader_Model(),
                        InstanceManager.getDefault(EngineModels.class).getMaxNameLength()) + SPACE);
            } else if (attribute.equals(Setup.HP)) {
                buf.append(padAndTruncateIfNeeded(TrainManifestHeaderText.getStringHeader_Hp(),
                        5) + SPACE);
            } else if (attribute.equals(Setup.CONSIST)) {
                buf.append(padAndTruncateIfNeeded(TrainManifestHeaderText.getStringHeader_Consist(),
                        InstanceManager.getDefault(ConsistManager.class).getMaxNameLength()) + SPACE);
            } else if (attribute.equals(Setup.DCC_ADDRESS)) {
                buf.append(padAndTruncateIfNeeded(TrainManifestHeaderText.getStringHeader_DCC_Address(),
                        TrainManifestHeaderText.getStringHeader_DCC_Address().length()) + SPACE);
            } else if (attribute.equals(Setup.KERNEL)) {
                buf.append(padAndTruncateIfNeeded(TrainManifestHeaderText.getStringHeader_Kernel(),
                        InstanceManager.getDefault(KernelManager.class).getMaxNameLength()) + SPACE);
            } else if (attribute.equals(Setup.KERNEL_SIZE)) {
                buf.append("   "); // assume kernel size is 99 or less
            } else if (attribute.equals(Setup.LOAD)) {
                buf.append(padAndTruncateIfNeeded(TrainManifestHeaderText.getStringHeader_Load(),
                        InstanceManager.getDefault(CarLoads.class).getMaxNameLength()) + SPACE);
            } else if (attribute.equals(Setup.LOAD_TYPE)) {
                buf.append(padAndTruncateIfNeeded(TrainManifestHeaderText.getStringHeader_Load_Type(),
                        TrainManifestHeaderText.getStringHeader_Load_Type().length()) + SPACE);
            } else if (attribute.equals(Setup.COLOR)) {
                buf.append(padAndTruncateIfNeeded(TrainManifestHeaderText.getStringHeader_Color(),
                        InstanceManager.getDefault(CarColors.class).getMaxNameLength()) + SPACE);
            } else if (attribute.equals(Setup.OWNER)) {
                buf.append(padAndTruncateIfNeeded(TrainManifestHeaderText.getStringHeader_Owner(),
                        InstanceManager.getDefault(CarOwners.class).getMaxNameLength()) + SPACE);
            } else if (attribute.equals(Setup.LENGTH)) {
                buf.append(padAndTruncateIfNeeded(TrainManifestHeaderText.getStringHeader_Length(),
                        InstanceManager.getDefault(CarLengths.class).getMaxNameLength()) + SPACE);
            } else if (attribute.equals(Setup.WEIGHT)) {
                buf.append(padAndTruncateIfNeeded(TrainManifestHeaderText.getStringHeader_Weight(),
                        Control.max_len_string_weight_name) + SPACE);
            } else if (attribute.equals(Setup.TRACK)) {
                buf.append(padAndTruncateIfNeeded(TrainManifestHeaderText.getStringHeader_Track(),
                        locationManager.getMaxTrackNameLength()) + SPACE);
            } else if (attribute.equals(Setup.LOCATION) && (isPickup || isLocal)) {
                buf.append(padAndTruncateIfNeeded(TrainManifestHeaderText.getStringHeader_Location(),
                        locationManager.getMaxTrackNameLength()) + SPACE);
            } else if (attribute.equals(Setup.LOCATION) && !isPickup) {
                buf.append(padAndTruncateIfNeeded(TrainManifestHeaderText.getStringHeader_Location(),
                        locationManager.getMaxLocationNameLength()) + SPACE);
            } else if (attribute.equals(Setup.DESTINATION) && !isPickup) {
                buf.append(padAndTruncateIfNeeded(TrainManifestHeaderText.getStringHeader_Destination(),
                        locationManager.getMaxTrackNameLength()) + SPACE);
            } else if (attribute.equals(Setup.DESTINATION) && isPickup) {
                buf.append(padAndTruncateIfNeeded(TrainManifestHeaderText.getStringHeader_Destination(),
                        locationManager.getMaxLocationNameLength()) + SPACE);
            } else if (attribute.equals(Setup.DEST_TRACK)) {
                buf.append(padAndTruncateIfNeeded(TrainManifestHeaderText.getStringHeader_Dest_Track(),
                        locationManager.getMaxLocationAndTrackNameLength() + 2) + SPACE);
            } else if (attribute.equals(Setup.FINAL_DEST)) {
                buf.append(padAndTruncateIfNeeded(TrainManifestHeaderText.getStringHeader_Final_Dest(),
                        locationManager.getMaxLocationNameLength()) + SPACE);
            } else if (attribute.equals(Setup.FINAL_DEST_TRACK)) {
                buf.append(padAndTruncateIfNeeded(TrainManifestHeaderText.getStringHeader_Final_Dest_Track(),
                        locationManager.getMaxLocationAndTrackNameLength() + 2) + SPACE);
            } else if (attribute.equals(Setup.HAZARDOUS)) {
                buf.append(padAndTruncateIfNeeded(TrainManifestHeaderText.getStringHeader_Hazardous(),
                        Setup.getHazardousMsg().length()) + SPACE);
            } else if (attribute.equals(Setup.RWE)) {
                buf.append(padAndTruncateIfNeeded(TrainManifestHeaderText.getStringHeader_RWE(),
                        locationManager.getMaxLocationAndTrackNameLength()) + SPACE);
            } else if (attribute.equals(Setup.COMMENT)) {
                buf.append(padAndTruncateIfNeeded(TrainManifestHeaderText.getStringHeader_Comment(),
                        isEngine ? engineManager.getMaxCommentLength() : carManager.getMaxCommentLength()) + SPACE);
            } else if (attribute.equals(Setup.DROP_COMMENT)) {
                buf.append(padAndTruncateIfNeeded(TrainManifestHeaderText.getStringHeader_Drop_Comment(),
                        InstanceManager.getDefault(CarLoads.class).getMaxLoadCommentLength()) + SPACE);
            } else if (attribute.equals(Setup.PICKUP_COMMENT)) {
                buf.append(padAndTruncateIfNeeded(TrainManifestHeaderText.getStringHeader_Pickup_Comment(),
                        InstanceManager.getDefault(CarLoads.class).getMaxLoadCommentLength()) + SPACE);
            } else if (attribute.equals(Setup.DIVISION)) {
                buf.append(padAndTruncateIfNeeded(TrainManifestHeaderText.getStringHeader_Division(),
                        InstanceManager.getDefault(DivisionManager.class).getMaxDivisionNameLength()) + SPACE);
            } else if (attribute.equals(Setup.TAB)) {
                buf.append(createTabIfNeeded(Setup.getTab1Length()));
            } else if (attribute.equals(Setup.TAB2)) {
                buf.append(createTabIfNeeded(Setup.getTab2Length()));
            } else if (attribute.equals(Setup.TAB3)) {
                buf.append(createTabIfNeeded(Setup.getTab3Length()));
            } else {
                buf.append(attribute + SPACE);
            }
        }
        return buf.toString().trim();
    }

    protected void printTrackNameHeader(PrintWriter file, String trackName, boolean isManifest) {
        printHorizontalLine(file, isManifest);
        int lineLength = getLineLength(isManifest);
        String s = padAndTruncate(tabString(trackName.trim(), lineLength / 4 - trackName.trim().length() / 2),
                lineLength / 2) +
                VERTICAL_LINE_CHAR +
                tabString(trackName.trim(), lineLength / 4 - trackName.trim().length() / 2);
        s = padAndTruncate(s, lineLength);
        addLine(file, s);
        printHorizontalLine(file, isManifest);
    }

    /**
     * Prints a line across the entire page.
     *
     * @param file       The File to print to.
     * @param isManifest True if manifest, false if switch list.
     */
    public void printHorizontalLine(PrintWriter file, boolean isManifest) {
        printHorizontalLine(file, 0, getLineLength(isManifest));
    }

    public void printHorizontalLine(PrintWriter file, int start, int end) {
        StringBuffer sb = new StringBuffer();
        while (start-- > 0) {
            sb.append(SPACE);
        }
        while (end-- > 0) {
            sb.append(HORIZONTAL_LINE_CHAR);
        }
        addLine(file, sb.toString());
    }

    public static String getISO8601Date(boolean isModelYear) {
        Calendar calendar = Calendar.getInstance();
        // use the JMRI Timebase (which may be a fast clock).
        calendar.setTime(jmri.InstanceManager.getDefault(jmri.Timebase.class).getTime());
        if (isModelYear && !Setup.getYearModeled().isEmpty()) {
            try {
                calendar.set(Calendar.YEAR, Integer.parseInt(Setup.getYearModeled().trim()));
            } catch (NumberFormatException e) {
                return Setup.getYearModeled();
            }
        }
        return (new StdDateFormat()).format(calendar.getTime());
    }

    public static String getDate(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("M/dd/yyyy HH:mm"); // NOI18N
        if (Setup.is12hrFormatEnabled()) {
            format = new SimpleDateFormat("M/dd/yyyy hh:mm a"); // NOI18N
        }
        return format.format(date);
    }

    public static String getDate(boolean isModelYear) {
        Calendar calendar = Calendar.getInstance();
        // use the JMRI Timebase (which may be a fast clock).
        calendar.setTime(jmri.InstanceManager.getDefault(jmri.Timebase.class).getTime());
        if (isModelYear && !Setup.getYearModeled().equals(Setup.NONE)) {
            try {
                calendar.set(Calendar.YEAR, Integer.parseInt(Setup.getYearModeled().trim()));
            } catch (NumberFormatException e) {
                return Setup.getYearModeled();
            }
        }
        return TrainCommon.getDate(calendar.getTime());
    }

    public static Date convertStringToDate(String date) {
        if (!date.isBlank()) {
            // create a date object from the string.
            try {
                // try MM/dd/yyyy HH:mm:ss.
                SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss"); // NOI18N
                return formatter.parse(date);
            } catch (java.text.ParseException pe1) {
                // try the old 12 hour format (no seconds).
                try {
                    SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy hh:mmaa"); // NOI18N
                    return formatter.parse(date);
                } catch (java.text.ParseException pe2) {
                    try {
                        // try 24hour clock.
                        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm"); // NOI18N
                        return formatter.parse(date);
                    } catch (java.text.ParseException pe3) {
                        log.debug("Not able to parse date: {}", date);
                    }
                }
            }
        }
        return null; // there was no date specified.
    }

    /**
     * Pads out a string by adding spaces to the end of the string, and will
     * remove characters from the end of the string if the string exceeds the
     * field size.
     *
     * @param s         The string to pad.
     * @param fieldSize The maximum length of the string.
     * @return A String the specified length
     */
    public static String padAndTruncateIfNeeded(String s, int fieldSize) {
        if (Setup.isTabEnabled()) {
            return padAndTruncate(s, fieldSize);
        }
        return s;
    }

    public static String padAndTruncate(String s, int fieldSize) {
        s = padString(s, fieldSize);
        if (s.length() > fieldSize) {
            s = s.substring(0, fieldSize);
        }
        return s;
    }

    /**
     * Adjusts string to be a certain number of characters by adding spaces to
     * the end of the string.
     *
     * @param s         The string to pad
     * @param fieldSize The fixed length of the string.
     * @return A String the specified length
     */
    public static String padString(String s, int fieldSize) {
        StringBuffer buf = new StringBuffer(s);
        while (buf.length() < fieldSize) {
            buf.append(SPACE);
        }
        return buf.toString();
    }

    /**
     * Creates a String of spaces to create a tab for text. Tabs must be
     * enabled. Setup.isTabEnabled()
     * 
     * @param tabSize the length of tab
     * @return tab
     */
    public static String createTabIfNeeded(int tabSize) {
        if (Setup.isTabEnabled()) {
            return tabString("", tabSize);
        }
        return "";
    }

    protected static String tabString(String s, int tabSize) {
        StringBuffer buf = new StringBuffer();
        // TODO this doesn't consider the length of s string.
        while (buf.length() < tabSize) {
            buf.append(SPACE);
        }
        buf.append(s);
        return buf.toString();
    }

    /**
     * Returns the line length for manifest or switch list printout. Always an
     * even number.
     * 
     * @param isManifest True if manifest.
     * @return line length for manifest or switch list.
     */
    public static int getLineLength(boolean isManifest) {
        return getLineLength(isManifest ? Setup.getManifestOrientation() : Setup.getSwitchListOrientation(),
                Setup.getFontName(), Font.PLAIN, Setup.getManifestFontSize());
    }

    public static int getManifestHeaderLineLength() {
        return getLineLength(Setup.getManifestOrientation(), "SansSerif", Font.ITALIC, Setup.getManifestFontSize());
    }

    private static int getLineLength(String orientation, String fontName, int fontStyle, int fontSize) {
        Font font = new Font(fontName, fontStyle, fontSize); // NOI18N
        JLabel label = new JLabel();
        FontMetrics metrics = label.getFontMetrics(font);
        int charwidth = metrics.charWidth('m');
        if (charwidth == 0) {
            log.error("Line length charater width equal to zero. font size: {}, fontName: {}", fontSize, fontName);
            charwidth = fontSize / 2; // create a reasonable character width
        }
        // compute lines and columns within margins
        int charLength = getPageSize(orientation).width / charwidth;
        if (charLength % 2 != 0) {
            charLength--; // make it even
        }
        return charLength;
    }

    private boolean checkStringLength(String string, boolean isManifest) {
        return checkStringLength(string, isManifest ? Setup.getManifestOrientation() : Setup.getSwitchListOrientation(),
                Setup.getFontName(), Setup.getManifestFontSize());
    }

    /**
     * Checks to see if the string fits on the page.
     *
     * @return false if string length is longer than page width.
     */
    private boolean checkStringLength(String string, String orientation, String fontName, int fontSize) {
        // ignore text color controls when determining line length
        if (string.startsWith(TEXT_COLOR_START) && string.contains(TEXT_COLOR_DONE)) {
            string = string.substring(string.indexOf(TEXT_COLOR_DONE) + 2);
        }
        if (string.contains(TEXT_COLOR_END)) {
            string = string.substring(0, string.indexOf(TEXT_COLOR_END));
        }
        Font font = new Font(fontName, Font.PLAIN, fontSize); // NOI18N
        JLabel label = new JLabel();
        FontMetrics metrics = label.getFontMetrics(font);
        int stringWidth = metrics.stringWidth(string);
        return stringWidth <= getPageSize(orientation).width;
    }

    protected static final Dimension PAPER_MARGINS = new Dimension(84, 72);

    protected static Dimension getPageSize(String orientation) {
        // page size has been adjusted to account for margins of .5
        // Dimension(84, 72)
        Dimension pagesize = new Dimension(523, 720); // Portrait 8.5 x 11
        // landscape has .65 margins
        if (orientation.equals(Setup.LANDSCAPE)) {
            pagesize = new Dimension(702, 523); // 11 x 8.5
        }
        if (orientation.equals(Setup.HALFPAGE)) {
            pagesize = new Dimension(261, 720); // 4.25 x 11
        }
        if (orientation.equals(Setup.HANDHELD)) {
            pagesize = new Dimension(206, 720); // 3.25 x 11
        }
        return pagesize;
    }

    /**
     * Produces a string using commas and spaces between the strings provided in
     * the array. Does not check for embedded commas in the string array.
     *
     * @param array The string array to be formated.
     * @return formated string using commas and spaces
     */
    public static String formatStringToCommaSeparated(String[] array) {
        StringBuffer sbuf = new StringBuffer("");
        for (String s : array) {
            if (s != null) {
                sbuf = sbuf.append(s + "," + SPACE);
            }
        }
        if (sbuf.length() > 2) {
            sbuf.setLength(sbuf.length() - 2); // remove trailing separators
        }
        return sbuf.toString();
    }

    /**
     * Adds HTML like color text control characters around a string. Note that
     * black is the standard text color, and if black is requested no control
     * characters are added.
     * 
     * @param text  the text to be modified
     * @param color the color the text is to be printed
     * @return formated text with color modifiers
     */
    public static String formatColorString(String text, Color color) {
        String s = text;
        if (!color.equals(Color.black)) {
            s = TEXT_COLOR_START + ColorUtil.colorToColorName(color) + TEXT_COLOR_DONE + text + TEXT_COLOR_END;
        }
        return s;
    }

    /**
     * Removes the color text control characters around the desired string
     * 
     * @param string the string with control characters
     * @return pure text
     */
    public static String getTextColorString(String string) {
        String text = string;
        if (string.contains(TEXT_COLOR_START)) {
            text = string.substring(0, string.indexOf(TEXT_COLOR_START)) +
                    string.substring(string.indexOf(TEXT_COLOR_DONE) + 2);
        }
        if (text.contains(TEXT_COLOR_END)) {
            text = text.substring(0, text.indexOf(TEXT_COLOR_END)) +
                    string.substring(string.indexOf(TEXT_COLOR_END) + TEXT_COLOR_END.length());
        }
        return text;
    }

    public static Color getTextColor(String string) {
        Color color = Color.black;
        if (string.contains(TEXT_COLOR_START)) {
            String c = string.substring(string.indexOf(TEXT_COLOR_START) + TEXT_COLOR_START.length());
            c = c.substring(0, c.indexOf("\""));
            color = ColorUtil.stringToColor(c);
        }
        return color;
    }

    public static String getTextColorName(String string) {
        return ColorUtil.colorToColorName(getTextColor(string));
    }

    private static final Logger log = LoggerFactory.getLogger(TrainCommon.class);
}
