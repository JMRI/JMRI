package jmri.jmrit.operations.locations.tools;

import jmri.InstanceManager;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.locations.divisions.Division;
import jmri.jmrit.operations.locations.divisions.DivisionManager;
import jmri.jmrit.operations.rollingstock.ImportRollingStock;
import jmri.jmrit.operations.setup.Setup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Locale;


/**
 * This routine will import Locations from a CSV file into the operations database. The field order is: Location, Track,
 * Type, Length, Division, Serviced by Trains Traveling, Rolling Stock, Order, Road Option, Roads, Load Option, Loads,
 * Ship Load Option, Ships, Set Out Restrictions, Restrictions, Pick up Restrictions, Restrictions, Schedule Name, Mode,
 * Alternate Track, Pool name, Minimum, Track Blocking Order, Planned Pick Ups, Track Destinations, Destinations, Swap
 * default loads and empties, Empty cars with default loads, Generate custom loads for spurs serviced by this train,
 * Generate custom loads for any spur (multiple trains), Generate custom loads for any staging track, Block cars by pick
 * up location, Comment, Comment when there is only pick ups, Comment when there is only set outs
 */
public class ImportLocations extends ImportRollingStock {

    LocationManager locationManager = InstanceManager.getDefault(LocationManager.class);
    DivisionManager divisionManager = InstanceManager.getDefault(DivisionManager.class);

    protected static final int FIELD_LOCATION = 0;
    protected static final int FIELD_TRACK = 1;
    protected static final int FIELD_TYPE = 2;
    protected static final int FIELD_LENGTH = 3;
    protected static final int FIELD_DIVISION = 4;
    protected static final int FIELD_SERVICED_BY = 5;
    protected static final int FIELD_ROLLING_STOCK = 6;
    protected static final int FIELD_ORDER = 7;
    protected static final int FIELD_ROAD_OPTION = 8;
    protected static final int FIELD_ROADS = 9;
    protected static final int FIELD_LOAD_OPTION = 10;
    protected static final int FIELD_LOADS = 11;
    protected static final int FIELD_SHIP_LOAD_OPTION = 12;
    protected static final int FIELD_SHIPS = 13;
    protected static final int FIELD_SET_OUT_RESTRICTIONS = 14;       // not used
    protected static final int FIELD_RESTRICTIONS_1 = 15;
    protected static final int FIELD_PICK_UP_RESTRICTIONS = 16;       // not used
    protected static final int FIELD_RESTRICTIONS_2 = 17;
    protected static final int FIELD_SCHEDULE_NAME = 18;
    protected static final int FIELD_SCHEDULE_MODE = 19;
    protected static final int FIELD_ALTERNATE_TRACK = 20;
    protected static final int FIELD_POOL_NAME = 21;
    protected static final int FIELD_IGNORE_MINIMUM = 22;
    protected static final int FIELD_TRACK_BLOCKING_ORDER = 23;
    protected static final int FIELD_PLANNED_PICK_UPS = 24;
    protected static final int FIELD_TRACK_DESTINATIONS = 25;
    protected static final int FIELD_DESTINATIONS = 26;
    protected static final int FIELD_SWAP_DEFAULT = 27;
    protected static final int FIELD_EMPTY_DEFAULT_LOADS = 28;
    protected static final int FIELD_EMPTY_CUSTOM_LOADS = 29;
    protected static final int FIELD_GENERATE_SPUR = 30;
    protected static final int FIELD_GENERATE_ANY_SPUR = 31;
    protected static final int FIELD_GENERATE_STAGING = 32;
    protected static final int FIELD_BLOCK_CARS_BY_PICKUP = 33;
    protected static final int FIELD_COMMENT = 34;
    protected static final int FIELD_COMMENT_BOTH = 35;
    protected static final int FIELD_COMMENT_PICKUPS = 36;            // not used
    protected static final int FIELD_COMMENT_SETOUTS = 37;            // not used

    @Override
    public void run() {
        File file = getFile();
        if (file == null) {
            return;
        }
        BufferedReader rdr = getBufferedReader(file);
        if (rdr == null) {
            return;
        }
        createStatusFrame(Bundle.getMessage("ImportLocations"));

        // read the import (CSV) file
        int lineNum = 0;
        int tracksAdded = 0;
        String line = " ";
        boolean importOkay = false;
        String[] inputLine;

        while (true) {
            lineNumber.setText(Bundle.getMessage("LineNumber") + " " + Integer.toString(++lineNum));
            try {
                line = rdr.readLine();
            } catch (IOException e) {
                break;
            }
            if (line == null) {
                importOkay = true;
                break;
            }
            if (!fstatus.isShowing()) {
                //user canceled input!
                break;
            }
            line = line.trim();
            importLine.setText(line);
            inputLine = parseCommaLine(line);
            log.debug("Import line number {} has {} elements", lineNum, inputLine.length);
            String fieldLocation = "";
            String fieldTrack = "";
            String fieldType = "";
            String fieldLength = "";
            if (line.startsWith(Bundle.getMessage("Location"))) {
                int elementNum = 0;
                for (String lineElement : inputLine) {
                    log.debug("Header {} is: {}", elementNum++, lineElement);
                }
                continue; // skip header
            }
            if (inputLine.length < 1) {
                log.debug("Skipping blank line");
                continue;
            }
            if (inputLine.length < 4) {
                log.info("Skipping row {} as we need at least 4 fields (Location, Track, Type and Length)", Integer.toString(lineNum));
                continue;
            }
            fieldLocation = inputLine[FIELD_LOCATION];
            Location location = locationManager.getLocationByName(fieldLocation);
            if (location == null) {
                log.debug("adding location - " + fieldLocation);
                location = locationManager.newLocation(fieldLocation);
            }
            fieldTrack = inputLine[FIELD_TRACK];
            fieldLength = inputLine[FIELD_LENGTH].trim();
            fieldType = inputLine[FIELD_TYPE].trim();
            String typeValue = null;
            if (fieldType.length() > 0) {
                if (fieldType.equals(Bundle.getMessage("Spur").toLowerCase(Locale.ROOT))) {
                    typeValue = Track.SPUR;
                } else if (fieldType.equals(Bundle.getMessage("Yard").toLowerCase(Locale.ROOT))) {
                    typeValue = Track.YARD;
                } else if (fieldType.equals(Bundle.getMessage("Class/Interchange"))) {
                    typeValue = Track.INTERCHANGE;
                } else if (fieldType.equals(Bundle.getMessage("Staging").toLowerCase(Locale.ROOT))) {
                    typeValue = Track.STAGING;
                } else {
                    typeValue = "unknown";
                }
            }
            Track thisTrack = location.getTrackByName(fieldTrack, null);
            Integer trackLength = null;
            try {
                trackLength = Integer.valueOf(fieldLength);
            } catch (NumberFormatException exception) {
                log.info("Import caught an exception converting the length field of the new track - value was {} at line number {}", fieldLength, Integer.toString(lineNum));
            }
            if (thisTrack != null) {
                if (!thisTrack.getTrackType().equals(typeValue)) {
                    log.debug("Import is changing type of track for Location {} track {} to {}", location.getName(), thisTrack.getName(), typeValue);
                    thisTrack.setTrackType(typeValue);
                }
            } else {
                log.debug("Import is adding location {} new track {} of type {}", location.getName(), fieldTrack, typeValue);
                thisTrack = location.addTrack(fieldTrack, typeValue);
                ++tracksAdded;
            }
            if (trackLength != null) {
                thisTrack.setLength(trackLength);
            }
            //if (inputLine.length >= FIELD_DIVISION) {
            // division was included in import
            String fieldDivision = inputLine[FIELD_DIVISION].trim();
            if (fieldDivision.length() > 0) {
                Division division = divisionManager.newDivision(fieldDivision);
                location.setDivision(division);
                log.debug("Setting this location to division {}", division);
            }
            if (inputLine.length >= FIELD_SERVICED_BY) {
                // process direction string (a list of directions each ending with a semicolon)
                String[] directions = inputLine[FIELD_SERVICED_BY].split("; ");
                log.debug("this track is serviced by {} directions", directions.length);
                int trackDir = 0; // no direction yet
                for (String dir : directions) {
                    trackDir += Setup.getDirectionInt(dir);
                }
                thisTrack.setTrainDirections(trackDir);
                log.debug("setting this location to directions {}", trackDir);
            }
            if (inputLine.length >= FIELD_ROLLING_STOCK) {
                // process rolling stock accepted
                if (inputLine[FIELD_ROLLING_STOCK].length() > 0) {
                    log.debug("Will be setting this location to accepting the following rolling stock: {}", inputLine[FIELD_ROLLING_STOCK]);
                    // first we need to remove all rolling stock, then add specific cars back
                    for (String car : thisTrack.getTypeNames()) {
                        thisTrack.deleteTypeName(car);
                    }
                    String[] rollingStock = inputLine[FIELD_ROLLING_STOCK].split("; ");
                    for (String typeName : rollingStock) {
                        thisTrack.addTypeName(typeName);
                    }
                }
            }
            if (inputLine.length >= FIELD_ORDER) {
                // process service order (Normal, FIFO or LIFO - Track handles the bundling
                String fieldServiceOrder = inputLine[FIELD_ORDER].trim();
                if (fieldServiceOrder.length() > 0) {
                    thisTrack.setServiceOrder(fieldServiceOrder);
                    log.debug("Setting the service order to {}", fieldServiceOrder);
                }
            }

            if (inputLine.length >= FIELD_ROADS) {
                log.debug("setting the road names to {}", inputLine[FIELD_ROADS]);
                // note -- don't trim so the final semi-colon space remains on the last field
                if (inputLine[FIELD_ROADS].length() > 0) {
                    String[] roads = inputLine[FIELD_ROADS].split("; ");
                    for (String road : roads) {
                        thisTrack.addRoadName(road);
                    }
                }
            }
            if (inputLine.length >= FIELD_ROAD_OPTION) {
                // process road option - again use the words imported
                String roadOptions = inputLine[FIELD_ROAD_OPTION].trim();
                String optionValue = "";
                if (roadOptions.length() > 0) {
                    if (roadOptions.startsWith(Bundle.getMessage("AcceptsAllRoads"))) {
                        optionValue = Track.ALL_ROADS;
                    } else if (roadOptions.startsWith(Bundle.getMessage("AcceptOnly"))) {
                        optionValue = Track.INCLUDE_ROADS;
                    } else if (roadOptions.startsWith(Bundle.getMessage("Exclude"))) {
                        optionValue = Track.EXCLUDE_ROADS;
                    }
                    thisTrack.setRoadOption(optionValue);
                    log.debug("setting the road options to {}", optionValue);
                }
            }
            if (inputLine.length >= FIELD_LOAD_OPTION) {
                String loadOptions = inputLine[FIELD_LOAD_OPTION].trim();
                String optionValue = "";
                if (loadOptions.length() > 0) {
                    if (loadOptions.startsWith(Bundle.getMessage("AcceptsAllLoads"))) {
                        optionValue = Track.ALL_LOADS;
                    } else if (loadOptions.startsWith(Bundle.getMessage("AcceptOnly"))) {
                        optionValue = Track.INCLUDE_ROADS;
                    } else if (loadOptions.startsWith(Bundle.getMessage("Exclude"))) {
                        optionValue = Track.EXCLUDE_LOADS;
                    } else {
                        log.debug("Locations Import Load option was not recognized: {} ", loadOptions);
                    }
                    thisTrack.setLoadOption(optionValue);
                }

            }
            if (inputLine.length >= FIELD_LOADS) {
                // process names of loads, again, don't trim first
                if (inputLine[FIELD_LOADS].length() > 0) {
                    String[] loads = inputLine[FIELD_LOADS].split("; ");
                    log.debug("This locations is surviced by {} loads", loads.length);
                    for (String load : loads) {
                        thisTrack.addLoadName(load);
                    }
                }

            }


            if (inputLine.length >= FIELD_IGNORE_MINIMUM) {
                String ignoreMin = inputLine[FIELD_IGNORE_MINIMUM].trim();
                if (ignoreMin.length() > 0) {
                    log.debug("setting the ignore minimum to {}", ignoreMin);
                    Integer ignoreValue = null;
                    try {
                        ignoreValue = Integer.valueOf(ignoreMin);
                        thisTrack.setBlockingOrder(ignoreValue);
                    } catch (NumberFormatException exception) {
                        log.debug("Exception converting the ignore minimum to a number - value was {}", ignoreMin);
                    }
                }
            }
            if (inputLine.length >= FIELD_TRACK_BLOCKING_ORDER) {
                String fieldTrackBlockingOrder = inputLine[FIELD_TRACK_BLOCKING_ORDER].trim();
                if (fieldTrackBlockingOrder.length() > 0) {
                    log.debug("setting the blocking order to {}", fieldTrackBlockingOrder);
                    Integer blockingOrder = null;
                    try {
                        blockingOrder = Integer.valueOf(fieldTrackBlockingOrder);
                        thisTrack.setBlockingOrder(blockingOrder);
                    } catch (NumberFormatException exception) {
                        log.debug("Exception converting the track blocking order to a number - value was {}", fieldTrackBlockingOrder);
                    }
                }
            }
            if (inputLine.length >= FIELD_PLANNED_PICK_UPS) {
                String ignoreUsedLength = inputLine[FIELD_PLANNED_PICK_UPS].trim();
                if (ignoreUsedLength.length() > 0) {
                    try {
                        Integer ignorePercentage = Integer.valueOf(ignoreUsedLength);
                        thisTrack.setIgnoreUsedLengthPercentage( ignorePercentage);
                    } catch (NumberFormatException exception) {
                        log.debug("Exception converting field Ignore Used track Percentage - value was {}", ignoreUsedLength);
                    }
                }
            }
            if (inputLine.length >= FIELD_COMMENT) {
                String fieldComment = inputLine[FIELD_COMMENT].trim();
                if (fieldComment.length() > 0) {
                    log.debug("setting the location comment to: {}", fieldComment);
                    thisTrack.setComment(fieldComment);
                }
            }
            if (inputLine.length >= FIELD_COMMENT_BOTH) {
                String commentBoth = inputLine[FIELD_COMMENT_BOTH].trim();
                thisTrack.setCommentBoth(commentBoth);
            }
            if (inputLine.length >= FIELD_COMMENT_PICKUPS) {
                String commentPickups = inputLine[FIELD_COMMENT_PICKUPS].trim();
                thisTrack.setCommentPickup(commentPickups);
            }
            if (inputLine.length >= FIELD_COMMENT_SETOUTS) {
                String commentSetouts = inputLine[FIELD_COMMENT_SETOUTS].trim();
                thisTrack.setCommentSetout(commentSetouts);
            }
        }
        if (importOkay) {
            JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle.getMessage("ImportTracksAdded"), new Object[]{tracksAdded}), Bundle.getMessage("SuccessfulImport"), JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle.getMessage("ImportTracksAdded"), new Object[]{tracksAdded}), Bundle.getMessage("ImportFailed"), JOptionPane.ERROR_MESSAGE);
        }
        fstatus.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(ImportLocations.class);

}
