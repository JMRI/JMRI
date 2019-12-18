package jmri.jmrit.operations.trains;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.swing.JLabel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.util.StdDateFormat;

import jmri.InstanceManager;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.cars.*;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.rollingstock.engines.EngineModels;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.ColorUtil;

/**
 * Common routines for trains
 *
 * @author Daniel Boudreau (C) Copyright 2008, 2009, 2010, 2011, 2012, 2013
 */
public class TrainCommon {

    public static final String LENGTHABV = Setup.LENGTHABV; // Length symbol
    protected static final String TAB = "    "; // NOI18N
    protected static final String NEW_LINE = "\n"; // NOI18N
    protected static final String SPACE = " ";
    protected static final String BLANK_LINE = " ";
    protected static final String HORIZONTAL_LINE_CHAR = "-";
    protected static final String VERTICAL_LINE_CHAR = "|";
    protected static final String TEXT_COLOR_START = "<FONT color=\"";
    protected static final String TEXT_COLOR_END = "</FONT>";
    // protected static final String ARROW = ">";

    protected static final boolean PICKUP = true;
    protected static final boolean IS_MANIFEST = true;
    public static final boolean LOCAL = true;
    protected static final boolean ENGINE = true;
    public static final boolean IS_TWO_COLUMN_TRACK = true; // when true, two
                                                            // column table is
                                                            // sorted by track
                                                            // names

    CarManager carManager = InstanceManager.getDefault(CarManager.class);
    EngineManager engineManager = InstanceManager.getDefault(EngineManager.class);
    LocationManager locationManager = InstanceManager.getDefault(LocationManager.class);

    // for manifests
    protected int cars = 0; // number of cars in train at a RouteLocation
    protected int emptyCars = 0; // number of empty cars in train at a
                                 // RouteLocation

    // for switch lists
    protected boolean pickupCars; // true when there are pickups
    protected boolean dropCars; // true when there are set outs

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
                String pullText = padAndTruncateString(pickupEngine(engine).trim(), lineLength / 2, true);
                pullText = formatColorString(pullText, Setup.getPickupColor());
                String s = pullText + VERTICAL_LINE_CHAR + tabString("", lineLength / 2 -1, true);           
                addLine(file, s);
            }
            if (engine.getRouteDestination() == rl) {
                String dropText = padAndTruncateString(dropEngine(engine).trim(), lineLength / 2 - 1, true);
                dropText = formatColorString(dropText, Setup.getDropColor());
                String s = tabString("", lineLength / 2, true) + VERTICAL_LINE_CHAR + dropText;
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
        StringBuffer buf = new StringBuffer(padAndTruncateString(Setup.getPickupEnginePrefix(),
                isManifest ? Setup.getManifestPrefixLength() : Setup.getSwitchListPrefixLength()));
        String[] format = Setup.getPickupEngineMessageFormat();
        for (String attribute : format) {
            String s = getEngineAttribute(engine, attribute, PICKUP);
            if (!checkStringLength(buf.toString() + s, isManifest)) {
                addLine(file, buf.toString());
                buf = new StringBuffer(TAB);
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
        StringBuffer buf = new StringBuffer(padAndTruncateString(Setup.getDropEnginePrefix(),
                isManifest ? Setup.getManifestPrefixLength() : Setup.getSwitchListPrefixLength()));
        String[] format = Setup.getDropEngineMessageFormat();
        for (String attribute : format) {
            String s = getEngineAttribute(engine, attribute, !PICKUP);
            if (!checkStringLength(buf.toString() + s, isManifest)) {
                addLine(file, buf.toString());
                buf = new StringBuffer(TAB);
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
    boolean printPickupHeader = true;
    boolean printSetoutHeader = true;
    boolean printLocalMoveHeader = true;

    /**
     * Block cars by track, then pick up and set out for each location in a
     * train's route. This routine is used for the "Standard" format.
     *
     * @param file        Manifest or switch list File
     * @param train       The train being printed.
     * @param carList     List of cars for this train
     * @param routeList   The train's list of RouteLocations
     * @param rl          The RouteLocation being printed
     * @param printHeader True if new location.
     * @param isManifest  True if manifest, false if switch list.
     */
    protected void blockCarsByTrack(PrintWriter file, Train train, List<Car> carList, List<RouteLocation> routeList,
            RouteLocation rl, boolean printHeader, boolean isManifest) {
        if (printHeader) {
            printPickupHeader = true;
            printSetoutHeader = true;
            printLocalMoveHeader = true;
        }
        List<Track> tracks = rl.getLocation().getTrackByNameList(null);
        List<String> trackNames = new ArrayList<>();
        clearUtilityCarTypes(); // list utility cars by quantity
        boolean isOnlyPassenger = train.isOnlyPassengerCars();
        for (Track track : tracks) {
            if (trackNames.contains(splitString(track.getName()))) {
                continue;
            }
            trackNames.add(splitString(track.getName())); // use a track name
                                                          // once
            // block pick up cars by destination
            // except for passenger cars
            boolean found = false; // begin blocking at rl
            for (RouteLocation rld : routeList) {
                if (rld != rl && !found) {
                    continue;
                }
                found = true;
                for (Car car : carList) {
                    if (Setup.isSortByTrackNameEnabled() &&
                            !splitString(track.getName()).equals(splitString(car.getTrackName()))) {
                        continue;
                    }
                    // note that a car in train doesn't have a track assignment
                    // caboose or FRED is placed at end of the train
                    // passenger trains are already blocked in the car list
                    if (car.getRouteLocation() == rl &&
                            car.getTrack() != null &&
                            ((car.getRouteDestination() == rld && !car.isCaboose() && !car.hasFred()) ||
                                    (rld == routeList.get(routeList.size() - 1) &&
                                            (car.isCaboose() || car.hasFred())) ||
                                    (car.isPassenger() && isOnlyPassenger))) {
                        // determine if header is to be printed
                        if (printPickupHeader && !car.isLocalMove()) {
                            printPickupCarHeader(file, isManifest, !IS_TWO_COLUMN_TRACK);
                            printPickupHeader = false;
                            // check to see if the other headers are needed. If
                            // they are identical, not needed
                            if (getPickupCarHeader(isManifest, !IS_TWO_COLUMN_TRACK)
                                    .equals(getDropCarHeader(isManifest, !IS_TWO_COLUMN_TRACK))) {
                                printSetoutHeader = false;
                            }
                            if (getPickupCarHeader(isManifest, !IS_TWO_COLUMN_TRACK)
                                    .equals(getLocalMoveHeader(isManifest))) {
                                printLocalMoveHeader = false;
                            }
                        }
                        if (car.isUtility()) {
                            pickupUtilityCars(file, carList, car, isManifest);
                        } // use truncated format if there's a switch list
                        else if (isManifest &&
                                Setup.isTruncateManifestEnabled() &&
                                rl.getLocation().isSwitchListEnabled()) {
                            pickUpCarTruncated(file, car, isManifest);
                        } else {
                            pickUpCar(file, car, isManifest);
                        }
                        pickupCars = true;
                        cars++;
                        if (car.getLoadType().equals(CarLoad.LOAD_TYPE_EMPTY)) {
                            emptyCars++;
                        }
                    }
                }
                if (isOnlyPassenger) {
                    break;
                }
            }
            // now do set outs and local moves
            for (Car car : carList) {
                if (Setup.isSortByTrackNameEnabled() &&
                        car.getRouteLocation() != null &&
                        car.getRouteDestination() == rl) {
                    // must sort local moves by car's destination track name and
                    // not car's track name
                    // sorting by car's track name fails if there are "similar"
                    // location names.
                    if (!splitString(track.getName()).equals(splitString(car.getDestinationTrackName()))) {
                        continue;
                    }
                }
                if (car.getRouteDestination() == rl && car.getDestinationTrack() != null) {
                    if (printSetoutHeader && !car.isLocalMove()) {
                        printDropCarHeader(file, isManifest, !IS_TWO_COLUMN_TRACK);
                        printSetoutHeader = false;
                        // check to see if the other headers are needed. If they
                        // are identical, not needed
                        if (getPickupCarHeader(isManifest, !IS_TWO_COLUMN_TRACK)
                                .equals(getDropCarHeader(isManifest, !IS_TWO_COLUMN_TRACK))) {
                            printPickupHeader = false;
                        }
                        if (getDropCarHeader(isManifest, !IS_TWO_COLUMN_TRACK).equals(getLocalMoveHeader(isManifest))) {
                            printLocalMoveHeader = false;
                        }
                    }
                    if (printLocalMoveHeader && car.isLocalMove()) {
                        printLocalCarMoveHeader(file, isManifest);
                        printLocalMoveHeader = false;
                        // check to see if the other headers are needed. If they
                        // are identical, not needed
                        if (getPickupCarHeader(isManifest, !IS_TWO_COLUMN_TRACK)
                                .equals(getLocalMoveHeader(isManifest))) {
                            printPickupHeader = false;
                        }
                        if (getDropCarHeader(isManifest, !IS_TWO_COLUMN_TRACK).equals(getLocalMoveHeader(isManifest))) {
                            printSetoutHeader = false;
                        }
                    }

                    if (car.isUtility()) {
                        setoutUtilityCars(file, carList, car, isManifest);
                    } // use truncated format if there's a switch list
                    else if (isManifest &&
                            Setup.isTruncateManifestEnabled() &&
                            rl.getLocation().isSwitchListEnabled() &&
                            !train.isLocalSwitcher()) {
                        truncatedDropCar(file, car, isManifest);
                    } else {
                        dropCar(file, car, isManifest);
                    }
                    dropCars = true;
                    cars--;
                    if (InstanceManager.getDefault(CarLoads.class).getLoadType(car.getTypeName(), car.getLoadName())
                            .equals(CarLoad.LOAD_TYPE_EMPTY)) {
                        emptyCars--;
                    }
                }
            }
            if (!Setup.isSortByTrackNameEnabled()) {
                break; // done
            }
        }
    }

    /**
     * Produces a two column format for car pick ups and set outs. Sorted by
     * track and then by destination. This routine is used for the "Two Column"
     * format.
     *
     * @param file        Manifest or switch list File
     * @param carList     List of cars for this train
     * @param routeList   The train's list of RouteLocations
     * @param rl          The RouteLocation being printed
     * @param printHeader True if new location.
     * @param isManifest  True if manifest, false if switch list.
     */
    protected void blockCarsTwoColumn(PrintWriter file, List<Car> carList,
            List<RouteLocation> routeList, RouteLocation rl, boolean printHeader, boolean isManifest) {
        index = 0;
        int lineLength = getLineLength(isManifest);
        List<Track> tracks = rl.getLocation().getTrackByNameList(null);
        List<String> trackNames = new ArrayList<>();
        clearUtilityCarTypes(); // list utility cars by quantity
        if (printHeader) {
            printCarHeader(file, isManifest, !IS_TWO_COLUMN_TRACK);
        }
        for (Track track : tracks) {
            if (trackNames.contains(splitString(track.getName()))) {
                continue;
            }
            trackNames.add(splitString(track.getName())); // use a track name
                                                          // once
            // block car pick ups by destination
            boolean found = false; // begin blocking at rl
            for (RouteLocation rld : routeList) {
                if (rld != rl && !found) {
                    continue;
                }
                found = true;
                for (int k = 0; k < carList.size(); k++) {
                    Car car = carList.get(k);
                    if (car.getTrack() != null &&
                            car.getRouteLocation() == rl &&
                            ((car.getRouteDestination() == rld && !car.isCaboose() && !car.hasFred()) ||
                                    (rld == routeList.get(routeList.size() - 1) &&
                                            (car.isCaboose() || car.hasFred())))) {
                        if (Setup.isSortByTrackNameEnabled() &&
                                !splitString(track.getName()).equals(splitString(car.getTrackName()))) {
                            continue;
                        }
                        pickupCars = true;
                        cars++;
                        if (car.getLoadType().equals(CarLoad.LOAD_TYPE_EMPTY)) {
                            emptyCars++;
                        }
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
                        s = padAndTruncateString(s, lineLength / 2, true);
                        s = formatColorString(s, Setup.getPickupColor());
                        if (car.isLocalMove()) {
                            String sl = appendSetoutString(s, carList, car.getRouteDestination(), car, isManifest,
                                    !IS_TWO_COLUMN_TRACK);
                            // check for utility car, and local route with two
                            // or more locations
                            if (!sl.equals(s)) {
                                s = sl;
                                carList.remove(car); // done with this car,
                                                     // remove from list
                                k--;
                            } else {
                                s = padAndTruncateString(s + VERTICAL_LINE_CHAR, getLineLength(isManifest), true);
                            }
                        } else {
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
            if (test.length() > 1) // null line contains |
            {
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
     * @param carList     List of cars for this train
     * @param routeList   The train's list of RouteLocations
     * @param rl          The RouteLocation being printed
     * @param printHeader True if new location.
     * @param isManifest  True if manifest, false if switch list.
     */
    protected void blockCarsByTrackNameTwoColumn(PrintWriter file, List<Car> carList,
            List<RouteLocation> routeList, RouteLocation rl, boolean printHeader, boolean isManifest) {
        index = 0;
        List<Track> tracks = rl.getLocation().getTrackByNameList(null);
        List<String> trackNames = new ArrayList<>();
        doneCars.clear();
        clearUtilityCarTypes(); // list utility cars by quantity
        if (printHeader) {
            printCarHeader(file, isManifest, IS_TWO_COLUMN_TRACK);
        }
        for (Track track : tracks) {
            String trackName = splitString(track.getName());
            if (trackNames.contains(trackName)) {
                continue;
            }
            // block car pick ups by destination
            boolean found = false; // begin blocking at rl
            for (RouteLocation rld : routeList) {
                if (rld != rl && !found) {
                    continue;
                }
                found = true;
                for (Car car : carList) {
                    if (car.getTrack() != null &&
                            car.getRouteLocation() == rl &&
                            trackName.equals(splitString(car.getTrackName())) &&
                            ((car.getRouteDestination() == rld && !car.isCaboose() && !car.hasFred()) ||
                                    (rld == routeList.get(routeList.size() - 1) &&
                                            (car.isCaboose() || car.hasFred())))) {
                        if (!trackNames.contains(trackName)) {
                            printTrackNameHeader(file, trackName, isManifest);
                        }
                        trackNames.add(trackName); // use a track name once
                        pickupCars = true;
                        cars++;
                        if (car.getLoadType().equals(CarLoad.LOAD_TYPE_EMPTY)) {
                            emptyCars++;
                        }
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
                        s = padAndTruncateString(s, getLineLength(isManifest) / 2, true);
                        s = formatColorString(s, Setup.getPickupColor());
                        s = appendSetoutString(s, trackName, carList, rl, isManifest, IS_TWO_COLUMN_TRACK);
                        addLine(file, s);
                    }
                }
            }
            for (Car car : carList) {
                if (!doneCars.contains(car) &&
                        car.getRouteDestination() == rl &&
                        trackName.equals(splitString(car.getDestinationTrackName()))) {
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
            List<Track> tracks = location.getTrackByNameList(null);
            for (Track track : tracks) {
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
                if (pickup && setout && !track.getCommentBoth().equals(Track.NONE)) {
                    newLine(file, track.getCommentBoth(), isManifest);
                } else if (pickup && !setout && !track.getCommentPickup().equals(Track.NONE)) {
                    newLine(file, track.getCommentPickup(), isManifest);
                } else if (!pickup && setout && !track.getCommentSetout().equals(Track.NONE)) {
                    newLine(file, track.getCommentSetout(), isManifest);
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
        return s + VERTICAL_LINE_CHAR + padAndTruncateString("", getLineLength(isManifest) / 2 - 1, true);
    }

    /*
     * Used by two column, track names shown in the columns.
     */
    private String appendSetoutString(String s, String trackName, List<Car> carList, RouteLocation rl,
            boolean isManifest, boolean isTwoColumnTrack) {
        for (Car car : carList) {
            if (!doneCars.contains(car) &&
                    car.getRouteDestination() == rl &&
                    trackName.equals(splitString(car.getDestinationTrackName()))) {
                doneCars.add(car);
                String so = appendSetoutString(s, carList, rl, car, isManifest, isTwoColumnTrack);
                // check for utility car
                if (!so.equals(s)) {
                    return so;
                }
            }
        }
        // no set out for this track
        return s + VERTICAL_LINE_CHAR + padAndTruncateString("", getLineLength(isManifest) / 2 - 1, true);
    }

    /*
     * Appends to string the vertical line character, and the car set out
     * string. Used in two column format.
     */
    private String appendSetoutString(String s, List<Car> carList, RouteLocation rl, Car car, boolean isManifest,
            boolean isTwoColumnTrack) {
        dropCars = true;
        cars--;
        if (car.getLoadType().equals(CarLoad.LOAD_TYPE_EMPTY)) {
            emptyCars--;
        }

        String dropText;

        if (car.isUtility()) {
            dropText = setoutUtilityCars(carList, car, !LOCAL, isManifest, isTwoColumnTrack);
            if (dropText == null) {
                return s; // no changes to the input string
            }
        } else {
            dropText = dropCar(car, isManifest, isTwoColumnTrack).trim();
        }

        dropText = padAndTruncateString(dropText.trim(), getLineLength(isManifest) / 2 - 1, true);
        dropText = formatColorString(dropText, Setup.getDropColor());
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
                new StringBuffer(padAndTruncateString(Setup.getPickupCarPrefix(), Setup.getManifestPrefixLength())),
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
                    new StringBuffer(padAndTruncateString(Setup.getPickupCarPrefix(), Setup.getManifestPrefixLength())),
                    Setup.getPickupManifestMessageFormat(), isManifest);
        } else {
            pickUpCar(file, car, new StringBuffer(
                    padAndTruncateString(Setup.getSwitchListPickupCarPrefix(), Setup.getSwitchListPrefixLength())),
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
                buf = new StringBuffer(TAB);
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
            String s = getCarAttribute(car, attribute, PICKUP, !LOCAL);
            buf.append(s);
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
                    padAndTruncateString(Setup.getDropCarPrefix(), Setup.getManifestPrefixLength()));
            String[] format = Setup.getDropManifestMessageFormat();
            if (isLocal) {
                buf = new StringBuffer(padAndTruncateString(Setup.getLocalPrefix(), Setup.getManifestPrefixLength()));
                format = Setup.getLocalManifestMessageFormat();
            }
            dropCar(file, car, buf, format, isLocal, isManifest);
        } else {
            StringBuffer buf = new StringBuffer(
                    padAndTruncateString(Setup.getSwitchListDropCarPrefix(), Setup.getSwitchListPrefixLength()));
            String[] format = Setup.getDropSwitchListMessageFormat();
            if (isLocal) {
                buf = new StringBuffer(
                        padAndTruncateString(Setup.getSwitchListLocalPrefix(), Setup.getSwitchListPrefixLength()));
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
                buf = new StringBuffer(TAB);
            }
            buf.append(s);
        }
        String s = buf.toString();
        if (s.trim().length() != 0) {
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
            String s = getCarAttribute(car, attribute, !PICKUP, local);
            buf.append(s);
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
            String s = getCarAttribute(car, attribute, !PICKUP, LOCAL);
            buf.append(s);
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
     * @param isManifest True if manifest, false if switch list.
     */
    protected void pickupUtilityCars(PrintWriter file, List<Car> carList, Car car, boolean isManifest) {
        // list utility cars by type, track, length, and load
        String[] format;
        if (isManifest) {
            format = Setup.getPickupUtilityManifestMessageFormat();
        } else {
            format = Setup.getPickupUtilitySwitchListMessageFormat();
        }
        int count = countUtilityCars(format, carList, car, PICKUP);
        if (count == 0) {
            return; // already printed out this car type
        }
        pickUpCar(file, car, new StringBuffer(padAndTruncateString(Setup.getPickupCarPrefix(),
                isManifest ? Setup.getManifestPrefixLength() : Setup.getSwitchListPrefixLength()) +
                " " +
                padString(Integer.toString(count), UTILITY_CAR_COUNT_FIELD_SIZE)), format, isManifest);
    }

    /**
     * Add a list of utility cars scheduled for drop at the route location to
     * the output file.
     *
     * @param file       Manifest or Switch List File.
     * @param carList    List of cars for this train.
     * @param car        The utility car.
     * @param isManifest True if manifest, false if switch list.
     */
    protected void setoutUtilityCars(PrintWriter file, List<Car> carList, Car car, boolean isManifest) {
        boolean isLocal = car.isLocalMove();
        StringBuffer buf;
        String[] format;
        if (isLocal && isManifest) {
            buf = new StringBuffer(padAndTruncateString(Setup.getLocalPrefix(), Setup.getManifestPrefixLength()));
            format = Setup.getLocalUtilityManifestMessageFormat();
        } else if (!isLocal && isManifest) {
            buf = new StringBuffer(padAndTruncateString(Setup.getDropCarPrefix(), Setup.getManifestPrefixLength()));
            format = Setup.getDropUtilityManifestMessageFormat();
        } else if (isLocal && !isManifest) {
            buf = new StringBuffer(
                    padAndTruncateString(Setup.getSwitchListLocalPrefix(), Setup.getSwitchListPrefixLength()));
            format = Setup.getLocalUtilitySwitchListMessageFormat();
        } else {
            buf = new StringBuffer(
                    padAndTruncateString(Setup.getSwitchListDropCarPrefix(), Setup.getSwitchListPrefixLength()));
            format = Setup.getDropUtilitySwitchListMessageFormat();
        }
        int count = countUtilityCars(format, carList, car, !PICKUP);
        if (count == 0) {
            return; // already printed out this car type
        }
        buf.append(" " + padString(Integer.toString(count), UTILITY_CAR_COUNT_FIELD_SIZE));
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
        StringBuffer buf = new StringBuffer(" " + padString(Integer.toString(count), UTILITY_CAR_COUNT_FIELD_SIZE));
        for (String attribute : format) {
            String s = getCarAttribute(car, attribute, PICKUP, !LOCAL);
            buf.append(s);
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
        StringBuffer buf = new StringBuffer(" " + padString(Integer.toString(count), UTILITY_CAR_COUNT_FIELD_SIZE));
        // TODO the Setup.Location doesn't work correctly for the conductor
        // window due to the fact that the car can be in the train and not
        // at its starting location.
        // Therefore we use the local true to disable it.
        if (car.getTrack() == null) {
            isLocal = true;
        }
        for (String attribute : format) {
            String s = getCarAttribute(car, attribute, !PICKUP, isLocal);
            buf.append(s);
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
        String[] carType = car.getTypeName().split("-");
        String carAttributes;
        // Note for car pick up: type, id, track name. For set out type, track
        // name, id (reversed).
        if (isPickup) {
            carAttributes = carType[0] + car.getRouteLocationId() + splitString(car.getTrackName());
            showDestination = showUtilityCarDestination(format);
            if (showDestination) {
                carAttributes = carAttributes + car.getRouteDestinationId();
            }
        } else {
            // set outs and local moves
            carAttributes = carType[0] + splitString(car.getDestinationTrackName()) + car.getRouteDestinationId();
            showLocation = showUtilityCarLocation(format);
            if (showLocation && car.getTrack() != null) {
                carAttributes = carAttributes + car.getRouteLocationId();
            }
            if (car.isLocalMove()) {
                carAttributes = carAttributes + splitString(car.getTrackName());
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
                String[] cType = c.getTypeName().split("-");
                if (!cType[0].equals(carType[0])) {
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
                        splitString(c.getTrackName()).equals(splitString(car.getTrackName()))) {
                    count++;
                }
                if (!isPickup &&
                        c.getRouteDestination() == car.getRouteDestination() &&
                        splitString(c.getDestinationTrackName()).equals(splitString(car.getDestinationTrackName())) &&
                        (splitString(c.getTrackName()).equals(splitString(car.getTrackName())) || !c.isLocalMove())) {
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
        return showUtilityCarAttribute(Setup.DESTINATION, mFormat);
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
        log.debug(string);
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
                    file.println(level + "- " + sb.toString());
                    sb = new StringBuffer(word + SPACE);
                }
            }
            string = sb.toString();
        }
        file.println(level + "- " + string);
    }

    /**
     * Deprecated instead use car.isLocalMove() Used to determine if car is a
     * local move
     *
     * @param car The Car to test.
     * @return true if the move is at the same location
     */
    @Deprecated
    protected boolean isLocalMove(Car car) {
        return car.isLocalMove();
    }

    /**
     * Writes string to file. No line length wrap or protection.
     *
     * @param file   The File to write to.
     * @param string The string to write.
     */
    protected void addLine(PrintWriter file, String string) {
        log.debug(string);
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
                    sb.setLength(sb.length() - 1); // remove last space added to
                                                   // string
                    addLine(file, sb.toString());
                    sb = new StringBuffer(word + SPACE);
                }
            }
            if (sb.length() > 0) {
                sb.setLength(sb.length() - 1); // remove last space added to
                                               // string
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
        String[] splitname = name.split("-");
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
        String[] splitname = name.split("-");
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
                    TrainCommon.splitString(rs.getRouteLocation().getName())
                            .equals(TrainCommon.splitString(location.getName()))) ||
                    (rs.getRouteDestination() != null &&
                            TrainCommon.splitString(rs.getRouteDestination().getName())
                                    .equals(TrainCommon.splitString(location.getName())))) {
                return true;
            }
        }
        return false;
    }

    protected void addCarsLocationUnknown(PrintWriter file, boolean isManifest) {
        CarManager carManager = InstanceManager.getDefault(CarManager.class);
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

    // @param isPickup true when rolling stock is being picked up
    private String getEngineAttribute(Engine engine, String attribute, boolean isPickup) {
        if (attribute.equals(Setup.MODEL)) {
            return " " +
                    padAndTruncateString(splitStringLeftParenthesis(engine.getModel()),
                            InstanceManager.getDefault(EngineModels.class).getMaxNameLength());
        }
        if (attribute.equals(Setup.CONSIST)) {
            return " " + padAndTruncateString(engine.getConsistName(), engineManager.getConsistMaxNameLength());
        }
        return getRollingStockAttribute(engine, attribute, isPickup, false);
    }

    private String getCarAttribute(Car car, String attribute, boolean isPickup, boolean isLocal) {
        if (attribute.equals(Setup.LOAD)) {
            return ((car.isCaboose() && !Setup.isPrintCabooseLoadEnabled()) ||
                    (car.isPassenger() &&
                            !Setup.isPrintPassengerLoadEnabled())) ? padAndTruncateString("",
                                    InstanceManager.getDefault(CarLoads.class).getMaxNameLength() +
                                            1)
                                    : " " +
                                            padAndTruncateString(car.getLoadName().split("-")[0],
                                                    InstanceManager.getDefault(CarLoads.class).getMaxNameLength());
        } else if (attribute.equals(Setup.LOAD_TYPE)) {
            return " " + padAndTruncateString(car.getLoadType(), TrainManifestHeaderText.getStringHeader_Load_Type().length());
        } else if (attribute.equals(Setup.HAZARDOUS)) {
            return (car.isHazardous() ? " " +
                    Setup.getHazardousMsg() : padAndTruncateString("", Setup.getHazardousMsg().length() + 1));
        } else if (attribute.equals(Setup.DROP_COMMENT)) {
            return " " + car.getDropComment();
        } else if (attribute.equals(Setup.PICKUP_COMMENT)) {
            return " " + car.getPickupComment();
        } else if (attribute.equals(Setup.KERNEL)) {
            return " " + padAndTruncateString(car.getKernelName(), carManager.getKernelMaxNameLength());
        } else if (attribute.equals(Setup.KERNEL_SIZE)) {
            if (car.isLead()) {
                return " " + padAndTruncateString(Integer.toString(car.getKernel().getSize()), 2);
            } else {
                return "   "; // assumes that kernel size is 99 or less
            }
        } else if (attribute.equals(Setup.RWE)) {
            if (!car.getReturnWhenEmptyDestName().equals(Car.NONE)) {
                return " " +
                        padAndTruncateString(TrainManifestHeaderText.getStringHeader_RWE() +
                                " " +
                                splitString(car.getReturnWhenEmptyDestinationName()) +
                                " ," +
                                splitString(car.getReturnWhenEmptyDestTrackName()),
                                locationManager.getMaxLocationAndTrackNameLength() +
                                        TrainManifestHeaderText.getStringHeader_RWE().length() +
                                        3);
            }
            return "";
        } else if (attribute.equals(Setup.FINAL_DEST)) {
            if (!car.getFinalDestinationName().equals(Car.NONE)) {
                return Setup.isPrintHeadersEnabled() ? " " +
                        padAndTruncateString(splitString(car.getFinalDestinationName()),
                                locationManager.getMaxLocationNameLength())
                        : " " +
                                padAndTruncateString(TrainManifestText.getStringFinalDestination() +
                                        " " +
                                        splitString(car.getFinalDestinationName()),
                                        locationManager.getMaxLocationNameLength() +
                                                TrainManifestText.getStringFinalDestination().length() +
                                                1);
            }
            return "";
        } else if (attribute.equals(Setup.FINAL_DEST_TRACK)) {
            if (!car.getFinalDestinationName().equals(Car.NONE)) {
                return Setup.isPrintHeadersEnabled() ? " " +
                        padAndTruncateString(splitString(car.getFinalDestinationName()) +
                                ", " +
                                splitString(car.getFinalDestinationTrackName()),
                                locationManager.getMaxLocationAndTrackNameLength() +
                                        2)
                        : " " +
                                padAndTruncateString(TrainManifestText.getStringFinalDestination() +
                                        " " +
                                        splitString(car.getFinalDestinationName()) +
                                        ", " +
                                        splitString(car.getFinalDestinationTrackName()),
                                        locationManager.getMaxLocationAndTrackNameLength() +
                                                TrainManifestText.getStringFinalDestination().length() +
                                                3);
            }
            return "";
        }
        return getRollingStockAttribute(car, attribute, isPickup, isLocal);
    }

    private String getRollingStockAttribute(RollingStock rs, String attribute, boolean isPickup, boolean isLocal) {
        if (attribute.equals(Setup.NUMBER)) {
            return " " + padAndTruncateString(splitString(rs.getNumber()), Control.max_len_string_print_road_number);
        } else if (attribute.equals(Setup.ROAD)) {
            String[] road = rs.getRoadName().split("-"); // second half of string can be anything
            return " " + padAndTruncateString(road[0], InstanceManager.getDefault(CarRoads.class).getMaxNameLength());
        } else if (attribute.equals(Setup.TYPE)) {
            String[] type = rs.getTypeName().split("-"); // second half of string can be anything
            return " " + padAndTruncateString(type[0], InstanceManager.getDefault(CarTypes.class).getMaxNameLength());
        } else if (attribute.equals(Setup.LENGTH)) {
            return " " +
                    padAndTruncateString(rs.getLength() + LENGTHABV,
                            InstanceManager.getDefault(CarLengths.class).getMaxNameLength());
        } else if (attribute.equals(Setup.WEIGHT)) {
            return " " +
                    padAndTruncateString(Integer.toString(rs.getAdjustedWeightTons()), Control.max_len_string_weight_name);
        } else if (attribute.equals(Setup.COLOR)) {
            return " " +
                    padAndTruncateString(rs.getColor(), InstanceManager.getDefault(CarColors.class).getMaxNameLength());
        } else if (((attribute.equals(Setup.LOCATION)) && (isPickup || isLocal)) ||
                (attribute.equals(Setup.TRACK) && isPickup)) {
            if (rs.getTrack() != null) {
                return Setup.isPrintHeadersEnabled() ? " " +
                        padAndTruncateString(splitString(rs.getTrackName()),
                                locationManager.getMaxTrackNameLength())
                        : " " +
                                padAndTruncateString(TrainManifestText.getStringFrom() +
                                        " " +
                                        splitString(rs.getTrackName()),
                                        TrainManifestText.getStringFrom().length() +
                                                locationManager.getMaxTrackNameLength() +
                                                1);
            }
            return "";
        } else if (attribute.equals(Setup.LOCATION) && !isPickup && !isLocal) {
            return Setup.isPrintHeadersEnabled() ? " " +
                    padAndTruncateString(splitString(rs.getLocationName()),
                            locationManager.getMaxLocationNameLength())
                    : " " +
                            padAndTruncateString(TrainManifestText.getStringFrom() +
                                    " " +
                                    splitString(rs.getLocationName()),
                                    locationManager.getMaxLocationNameLength() +
                                            TrainManifestText.getStringFrom().length() +
                                            1);
        } else if (attribute.equals(Setup.DESTINATION) && isPickup) {
            if (rs.getDestination() == null)
                return "";
            if (Setup.isPrintHeadersEnabled()) {
                return " " +
                        padAndTruncateString(splitString(rs.getDestinationName()),
                                locationManager.getMaxLocationNameLength());
            }
            if (Setup.isTabEnabled()) {
                return " " +
                        padAndTruncateString(TrainManifestText.getStringDest() +
                                " " +
                                splitString(rs.getDestinationName()),
                                TrainManifestText.getStringDest().length() +
                                        locationManager.getMaxLocationNameLength() +
                                        1);
            } else {
                return " " + TrainManifestText.getStringDestination() + " " + splitString(rs.getDestinationName());
            }
        } else if ((attribute.equals(Setup.DESTINATION) || attribute.equals(Setup.TRACK)) && !isPickup) {
            return Setup.isPrintHeadersEnabled() ? " " +
                    padAndTruncateString(splitString(rs.getDestinationTrackName()),
                            locationManager.getMaxTrackNameLength())
                    : " " +
                            padAndTruncateString(TrainManifestText.getStringTo() +
                                    " " +
                                    splitString(rs.getDestinationTrackName()),
                                    locationManager.getMaxTrackNameLength() +
                                            TrainManifestText.getStringTo().length() +
                                            1);
        } else if (attribute.equals(Setup.DEST_TRACK)) {
            return Setup.isPrintHeadersEnabled() ? " " +
                    padAndTruncateString(splitString(rs.getDestinationName()) +
                            ", " +
                            splitString(rs.getDestinationTrackName()),
                            locationManager.getMaxLocationAndTrackNameLength() +
                                    2)
                    : " " +
                            padAndTruncateString(TrainManifestText.getStringDest() +
                                    " " +
                                    splitString(rs.getDestinationName()) +
                                    ", " +
                                    splitString(rs.getDestinationTrackName()),
                                    locationManager.getMaxLocationAndTrackNameLength() +
                                            TrainManifestText.getStringDest().length() +
                                            3);
        } else if (attribute.equals(Setup.OWNER)) {
            return " " +
                    padAndTruncateString(rs.getOwner(), InstanceManager.getDefault(CarOwners.class).getMaxNameLength());
        } else if (attribute.equals(Setup.COMMENT)) {
            return " " + rs.getComment();
        } else if (attribute.equals(Setup.BLANK)) {
            return "";
        } // the three utility attributes that don't get printed but need to be
          // tabbed out
        else if (attribute.equals(Setup.NO_NUMBER)) {
            return " " +
                    padAndTruncateString("", Control.max_len_string_print_road_number -
                            (UTILITY_CAR_COUNT_FIELD_SIZE + 1));
        } else if (attribute.equals(Setup.NO_ROAD)) {
            return " " + padAndTruncateString("", InstanceManager.getDefault(CarRoads.class).getMaxNameLength());
        } else if (attribute.equals(Setup.NO_COLOR)) {
            return " " + padAndTruncateString("", InstanceManager.getDefault(CarColors.class).getMaxNameLength());
        } // there are four truncated manifest attributes
        else if (attribute.equals(Setup.NO_DEST_TRACK)) {
            return Setup.isPrintHeadersEnabled() ? padAndTruncateString("",
                    locationManager.getMaxLocationAndTrackNameLength() +
                            2)
                    : "";
        } else if ((attribute.equals(Setup.NO_LOCATION) && !isPickup) ||
                (attribute.equals(Setup.NO_DESTINATION) && isPickup)) {
            return Setup.isPrintHeadersEnabled() ? padAndTruncateString("", locationManager.getMaxLocationNameLength() +
                    1) : "";
        } else if (attribute.equals(Setup.NO_TRACK) ||
                attribute.equals(Setup.NO_LOCATION) ||
                attribute.equals(Setup.NO_DESTINATION)) {
            return Setup.isPrintHeadersEnabled() ? padAndTruncateString("", locationManager.getMaxTrackNameLength() +
                    1) : "";
        } else if (attribute.equals(Setup.TAB)) {
            return tabString("", Setup.getTab1Length());
        } else if (attribute.equals(Setup.TAB2)) {
            return tabString("", Setup.getTab2Length());
        } else if (attribute.equals(Setup.TAB3)) {
            return tabString("", Setup.getTab3Length());
        }
        return MessageFormat.format(Bundle.getMessage("ErrorPrintOptions"), new Object[]{attribute}); // something
        // isn't
        // right!
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
            String s = padAndTruncateString(tabString(Setup.getPickupEnginePrefix().trim(), lineLength / 4 -
                    Setup.getPickupEnginePrefix().length() / 2, true), lineLength / 2, true) +
                    VERTICAL_LINE_CHAR +
                    tabString(Setup.getDropEnginePrefix(),
                            lineLength / 4 - Setup.getDropEnginePrefix().length() / 2,
                            true);
            s = padAndTruncateString(s, lineLength, true);
            addLine(file, s);
            printHorizontalLine(file, 0, lineLength);
        }

        String s = padAndTruncateString(getPickupEngineHeader(), lineLength / 2, true);
        s = padAndTruncateString(s + VERTICAL_LINE_CHAR + getDropEngineHeader(), lineLength, true);
        addLine(file, s);
        printHorizontalLine(file, 0, lineLength);
    }

    public void printPickupEngineHeader(PrintWriter file, boolean isManifest) {
        int lineLength = getLineLength(isManifest);
        printHorizontalLine(file, 0, lineLength);
        String s = padAndTruncateString(tabString("", Setup.getManifestPrefixLength() + 1, true) +
                getPickupEngineHeader(), lineLength, true);
        addLine(file, s);
        printHorizontalLine(file, 0, lineLength);
    }

    public void printDropEngineHeader(PrintWriter file, boolean isManifest) {
        int lineLength = getLineLength(isManifest);
        printHorizontalLine(file, 0, lineLength);
        String s = padAndTruncateString(tabString("", Setup.getManifestPrefixLength() + 1, true) +
                getDropEngineHeader(), lineLength, true);
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
        String s = padAndTruncateString(tabString(Setup.getPickupCarPrefix(), lineLength / 4 -
                Setup.getPickupCarPrefix().length() / 2, true), lineLength / 2, true) +
                VERTICAL_LINE_CHAR +
                tabString(Setup.getDropCarPrefix(), lineLength / 4 - Setup.getDropCarPrefix().length() / 2, true);
        s = padAndTruncateString(s, lineLength, true);
        addLine(file, s);
        printHorizontalLine(file, 0, lineLength);

        s = padAndTruncateString(getPickupCarHeader(isManifest, isTwoColumnTrack), lineLength / 2, true);
        s = padAndTruncateString(s +
                VERTICAL_LINE_CHAR +
                getDropCarHeader(isManifest, isTwoColumnTrack), lineLength, true);
        addLine(file, s);
        printHorizontalLine(file, 0, lineLength);
    }

    public void printPickupCarHeader(PrintWriter file, boolean isManifest, boolean isTwoColumnTrack) {
        if (!Setup.isPrintHeadersEnabled()) {
            return;
        }
        printHorizontalLine(file, isManifest);
        String s = padAndTruncateString(tabString("", Setup.getManifestPrefixLength() + 1, true) +
                getPickupCarHeader(isManifest, isTwoColumnTrack), getLineLength(isManifest), true);
        addLine(file, s);
        printHorizontalLine(file, isManifest);
    }

    public void printDropCarHeader(PrintWriter file, boolean isManifest, boolean isTwoColumnTrack) {
        if (!Setup.isPrintHeadersEnabled() || getDropCarHeader(isManifest, isTwoColumnTrack).trim().isEmpty()) {
            return;
        }
        printHorizontalLine(file, isManifest);
        String s = padAndTruncateString(tabString("", Setup.getManifestPrefixLength() + 1, true) +
                getDropCarHeader(isManifest, isTwoColumnTrack), getLineLength(isManifest), true);
        addLine(file, s);
        printHorizontalLine(file, isManifest);
    }

    public void printLocalCarMoveHeader(PrintWriter file, boolean isManifest) {
        if (!Setup.isPrintHeadersEnabled()) {
            return;
        }
        printHorizontalLine(file, isManifest);
        String s = padAndTruncateString(tabString("", Setup.getManifestPrefixLength() + 1, true) +
                getLocalMoveHeader(isManifest), getLineLength(isManifest), true);
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
                buf.append(padAndTruncateString(TrainManifestHeaderText.getStringHeader_Road(),
                        InstanceManager.getDefault(CarRoads.class).getMaxNameLength()) +
                        " ");
            } else if (attribute.equals(Setup.NUMBER) && !isEngine) {
                buf.append(padAndTruncateString(TrainManifestHeaderText.getStringHeader_Number(),
                        Control.max_len_string_print_road_number) +
                        " ");
            } else if (attribute.equals(Setup.NUMBER) && isEngine) {
                buf.append(padAndTruncateString(TrainManifestHeaderText.getStringHeader_EngineNumber(),
                        Control.max_len_string_print_road_number) +
                        " ");
            } else if (attribute.equals(Setup.TYPE)) {
                buf.append(padAndTruncateString(TrainManifestHeaderText.getStringHeader_Type(),
                        InstanceManager.getDefault(CarTypes.class).getMaxNameLength()) +
                        " ");
            } else if (attribute.equals(Setup.MODEL)) {
                buf.append(padAndTruncateString(TrainManifestHeaderText.getStringHeader_Model(),
                        InstanceManager.getDefault(EngineModels.class).getMaxNameLength()) +
                        " ");
            } else if (attribute.equals(Setup.CONSIST)) {
                buf.append(padAndTruncateString(TrainManifestHeaderText.getStringHeader_Consist(),
                        engineManager.getConsistMaxNameLength()) +
                        " ");
            } else if (attribute.equals(Setup.KERNEL)) {
                buf.append(padAndTruncateString(TrainManifestHeaderText.getStringHeader_Kernel(),
                        carManager.getKernelMaxNameLength()) +
                        " ");
            } else if (attribute.equals(Setup.KERNEL_SIZE)) {
                buf.append("   "); // assume kernel size is 99 or less
            } else if (attribute.equals(Setup.LOAD)) {
                buf.append(padAndTruncateString(TrainManifestHeaderText.getStringHeader_Load(),
                        InstanceManager.getDefault(CarLoads.class).getMaxNameLength()) +
                        " ");
            } else if (attribute.equals(Setup.LOAD_TYPE)) {
                buf.append(padAndTruncateString(TrainManifestHeaderText.getStringHeader_Load_Type(),
                        TrainManifestHeaderText.getStringHeader_Load_Type().length()) +
                        " ");
            } else if (attribute.equals(Setup.COLOR)) {
                buf.append(padAndTruncateString(TrainManifestHeaderText.getStringHeader_Color(),
                        InstanceManager.getDefault(CarColors.class).getMaxNameLength()) +
                        " ");
            } else if (attribute.equals(Setup.OWNER)) {
                buf.append(padAndTruncateString(TrainManifestHeaderText.getStringHeader_Owner(),
                        InstanceManager.getDefault(CarOwners.class).getMaxNameLength()) +
                        " ");
            } else if (attribute.equals(Setup.LENGTH)) {
                buf.append(padAndTruncateString(TrainManifestHeaderText.getStringHeader_Length(),
                        InstanceManager.getDefault(CarLengths.class).getMaxNameLength()) +
                        " ");
            } else if (attribute.equals(Setup.WEIGHT)) {
                buf.append(padAndTruncateString(TrainManifestHeaderText.getStringHeader_Weight(),
                        Control.max_len_string_weight_name) +
                        " ");
            } else if (attribute.equals(Setup.TRACK)) {
                buf.append(padAndTruncateString(TrainManifestHeaderText.getStringHeader_Track(),
                        locationManager.getMaxTrackNameLength()) +
                        " ");
            } else if (attribute.equals(Setup.LOCATION) && (isPickup || isLocal)) {
                buf.append(padAndTruncateString(TrainManifestHeaderText.getStringHeader_Location(),
                        locationManager.getMaxTrackNameLength()) +
                        " ");
            } else if (attribute.equals(Setup.LOCATION) && !isPickup) {
                buf.append(padAndTruncateString(TrainManifestHeaderText.getStringHeader_Location(),
                        locationManager.getMaxLocationNameLength()) +
                        " ");
            } else if (attribute.equals(Setup.DESTINATION) && !isPickup) {
                buf.append(padAndTruncateString(TrainManifestHeaderText.getStringHeader_Destination(),
                        locationManager.getMaxTrackNameLength()) +
                        " ");
            } else if (attribute.equals(Setup.DESTINATION) && isPickup) {
                buf.append(padAndTruncateString(TrainManifestHeaderText.getStringHeader_Destination(),
                        locationManager.getMaxLocationNameLength()) +
                        " ");
            } else if (attribute.equals(Setup.DEST_TRACK)) {
                buf.append(padAndTruncateString(TrainManifestHeaderText.getStringHeader_Dest_Track(),
                        locationManager.getMaxLocationAndTrackNameLength() +
                                2) +
                        " ");
            } else if (attribute.equals(Setup.FINAL_DEST)) {
                buf.append(padAndTruncateString(TrainManifestHeaderText.getStringHeader_Final_Dest(),
                        locationManager.getMaxLocationNameLength()) +
                        " ");
            } else if (attribute.equals(Setup.FINAL_DEST_TRACK)) {
                buf.append(padAndTruncateString(TrainManifestHeaderText.getStringHeader_Final_Dest_Track(),
                        locationManager.getMaxLocationAndTrackNameLength() +
                                2) +
                        " ");
            } else if (attribute.equals(Setup.HAZARDOUS)) {
                buf.append(padAndTruncateString(TrainManifestHeaderText.getStringHeader_Hazardous(),
                        Setup.getHazardousMsg().length()) +
                        " ");
            } else if (attribute.equals(Setup.RWE)) {
                buf.append(TrainManifestHeaderText.getStringHeader_RWE() + " ");
            } else if (attribute.equals(Setup.COMMENT)) {
                buf.append(TrainManifestHeaderText.getStringHeader_Comment() + " ");
            } else if (attribute.equals(Setup.TAB)) {
                buf.append(tabString("", Setup.getTab1Length()));
            } else if (attribute.equals(Setup.TAB2)) {
                buf.append(tabString("", Setup.getTab2Length()));
            } else if (attribute.equals(Setup.TAB3)) {
                buf.append(tabString("", Setup.getTab3Length()));
            } else {
                buf.append(attribute + " ");
            }
        }
        return buf.toString();
    }

    protected void printTrackNameHeader(PrintWriter file, String trackName, boolean isManifest) {
        printHorizontalLine(file, isManifest);
        int lineLength = getLineLength(isManifest);
        String s = padAndTruncateString(tabString(trackName.trim(), lineLength / 4 -
                trackName.trim().length() / 2, true), lineLength / 2, true) +
                VERTICAL_LINE_CHAR +
                tabString(trackName.trim(), lineLength / 4 - trackName.trim().length() / 2, true);
        s = padAndTruncateString(s, lineLength, true);
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

    /**
     * Pads out a string by adding spaces to the end of the string, and will
     * remove characters from the end of the string if the string exceeds the
     * field size.
     *
     * @param s         The string to pad.
     * @param fieldSize The maximum length of the string.
     * @return A String the specified length
     */
    public static String padAndTruncateString(String s, int fieldSize) {
        return padAndTruncateString(s, fieldSize, Setup.isTabEnabled());
    }

    public static String padAndTruncateString(String s, int fieldSize, boolean enabled) {
        if (!enabled) {
            return s;
        }
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
            buf.append(" ");
        }
        return buf.toString();
    }

    /**
     * Adds the requested number of spaces to the start of the string. Tabs must
     * be enabled. Setup.isTabEnabled()
     *
     * @param s       The string to pad out.
     * @param tabSize The fixed length of the string.
     * @return A String with the specified length
     */
    public static String tabString(String s, int tabSize) {
        return tabString(s, tabSize, Setup.isTabEnabled());
    }

    public static String tabString(String s, int tabSize, boolean enabled) {
        if (!enabled) {
            return s;
        }
        StringBuffer buf = new StringBuffer();
        // TODO this doesn't consider the length of s string.
        while (buf.length() < tabSize) {
            buf.append(" ");
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
        // Metrics don't always work for the various font names, so use
        // Monospaced
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
            charLength++; // make it even
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
                sbuf = sbuf.append(s + ", ");
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
            s = TEXT_COLOR_START + ColorUtil.colorToColorName(color) + "\">" + text + TEXT_COLOR_END;
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
            text = string.substring(0, string.indexOf(TEXT_COLOR_START)) + string.substring(string.indexOf(">") + 1);
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
            String c = string.substring(string.indexOf("\"") + 1);
            c = c.substring(0, c.indexOf("\""));
            color = ColorUtil.stringToColor(c);
        }
        return color;
    }

    private static final Logger log = LoggerFactory.getLogger(TrainCommon.class);
}
