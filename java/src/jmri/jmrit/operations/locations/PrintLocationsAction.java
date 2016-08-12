// PrintLocationsAction.java
package jmri.jmrit.operations.locations;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.rollingstock.engines.EngineTypes;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteManager;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.davidflanagan.HardcopyWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to print a summary of the Location Roster contents
 * <P>
 * This uses the older style printing, for compatibility with Java 1.1.8 in
 * Macintosh MRJ
 *
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Dennis Miller Copyright (C) 2005
 * @author Daniel Boudreau Copyright (C) 2008, 2011, 2012, 2014
 * @version $Revision$
 */
public class PrintLocationsAction extends AbstractAction {

    static final String NEW_LINE = "\n"; // NOI18N
    static final String FORM_FEED = "\f"; // NOI18N
    static final String TAB = "\t"; // NOI18N
    static final int TAB_LENGTH = 10;
    static final String SPACE = "   ";

    static final int MAX_NAME_LENGTH = Control.max_len_string_location_name;

    LocationManager manager = LocationManager.instance();

    public PrintLocationsAction(String actionName, boolean isPreview) {
        super(actionName);
        _isPreview = isPreview;
    }

    public PrintLocationsAction(String actionName, boolean isPreview, Location location) {
        super(actionName);
        _isPreview = isPreview;
        _location = location;
    }

    /**
     * Variable to set whether this is to be printed or previewed
     */
    boolean _isPreview;
    Location _location = null;
    HardcopyWriter writer;
    LocationPrintOptionFrame lpof = null;

    @Override
    public void actionPerformed(ActionEvent e) {
        if (lpof == null) {
            lpof = new LocationPrintOptionFrame(this);
        } else {
            lpof.setVisible(true);
        }
        lpof.initComponents();
    }
    
    private int charactersPerLine = 70;

    public void printLocations() {
        // obtain a HardcopyWriter
        String title = Bundle.getMessage("TitleLocationsTable");
        if (_location != null) {
            title = _location.getName();
        }
        try {
            writer = new HardcopyWriter(new Frame(), title, Control.reportFontSize, .5, .5, .5, .5, _isPreview);
        } catch (HardcopyWriter.PrintCanceledException ex) {
            log.debug("Print cancelled");
            return;
        }
        
        charactersPerLine = writer.getCharactersPerLine();
        
        try {
            // print locations?
            if (printLocations.isSelected()) {
                printLocationsSelected();
            }
            // print schedules?
            if (printSchedules.isSelected()) {
                printSchedulesSelected();
            }
            if (printComments.isSelected()) {
                printCommentsSelected();
            }
            // print detailed report?
            if (printDetails.isSelected()) {
                printDetailsSelected();
            }
            // print analysis?
            if (printAnalysis.isSelected()) {
                printAnalysisSelected();
            }
            // prevent NPE on close
            if (printLocations.isSelected() || printSchedules.isSelected() || printComments.isSelected()
                    || printDetails.isSelected() || printAnalysis.isSelected()) // force completion of the printing
            {
                writer.close();
            }
        } catch (IOException we) {
            log.error("Error printing PrintLocationAction: " + we);
        }
    }

    // Loop through the Roster, printing as needed
    private void printLocationsSelected() throws IOException {
        List<Location> locations = manager.getLocationsByNameList();
        int totalLength = 0;
        int usedLength = 0;
        int numberRS = 0;
        int numberCars = 0;
        int numberEngines = 0;
        // header
        String s = Bundle.getMessage("Location") + TAB + TAB + TAB + Bundle.getMessage("Length") + " "
                + Bundle.getMessage("Used") + TAB + Bundle.getMessage("RS") + TAB + Bundle.getMessage("Cars") + TAB
                + Bundle.getMessage("Engines") + TAB + Bundle.getMessage("Pickups") + " " + Bundle.getMessage("Drop")
                + NEW_LINE;
        writer.write(s);
        for (Location location : locations) {
            if (_location != null && location != _location) {
                continue;
            }
            // location name, track length, used, number of RS, scheduled pick ups and drops
            s = padOutString(location.getName(), Control.max_len_string_location_name) + TAB + "  "
                    + Integer.toString(location.getLength()) + TAB + Integer.toString(location.getUsedLength()) + TAB
                    + Integer.toString(location.getNumberRS()) + TAB + Integer.toString(location.getNumberCars()) + TAB
                    + Integer.toString(location.getNumberEngines()) + TAB + Integer.toString(location.getPickupRS())
                    + TAB + Integer.toString(location.getDropRS()) + NEW_LINE;
            writer.write(s);

            totalLength += location.getLength();
            usedLength += location.getUsedLength();
            numberRS += location.getNumberRS();

            List<Track> yards = location.getTrackByNameList(Track.YARD);
            if (yards.size() > 0) {
                // header
                writer.write(SPACE + Bundle.getMessage("YardName") + NEW_LINE);
                for (Track yard : yards) {
                    writer.write(getTrackString(yard));
                    numberCars += yard.getNumberCars();
                    numberEngines += yard.getNumberEngines();
                }
            }

            List<Track> spurs = location.getTrackByNameList(Track.SPUR);
            if (spurs.size() > 0) {
                // header
                writer.write(SPACE + Bundle.getMessage("SpurName") + NEW_LINE);
                for (Track spur : spurs) {
                    writer.write(getTrackString(spur));
                    numberCars += spur.getNumberCars();
                    numberEngines += spur.getNumberEngines();
                }
            }

            List<Track> interchanges = location.getTrackByNameList(Track.INTERCHANGE);
            if (interchanges.size() > 0) {
                // header
                writer.write(SPACE + Bundle.getMessage("InterchangeName") + NEW_LINE);
                for (Track interchange : interchanges) {
                    writer.write(getTrackString(interchange));
                    numberCars += interchange.getNumberCars();
                    numberEngines += interchange.getNumberEngines();
                }
            }

            List<Track> stagingTracks = location.getTrackByNameList(Track.STAGING);
            if (stagingTracks.size() > 0) {
                // header
                writer.write(SPACE + Bundle.getMessage("StagingName") + NEW_LINE);
                for (Track staging : stagingTracks) {
                    writer.write(getTrackString(staging));
                    numberCars += staging.getNumberCars();
                    numberEngines += staging.getNumberEngines();
                }
            }
            writer.write(NEW_LINE);
        }

        // summary
        s = MessageFormat.format(Bundle.getMessage("TotalLengthMsg"), new Object[]{Integer.toString(totalLength),
            Integer.toString(usedLength), Integer.toString(usedLength * 100 / totalLength)})
                + NEW_LINE;
        writer.write(s);
        s = MessageFormat.format(Bundle.getMessage("TotalRollingMsg"), new Object[]{Integer.toString(numberRS),
            Integer.toString(numberCars), Integer.toString(numberEngines)})
                + NEW_LINE;
        writer.write(s);
        // are there trains en route, then some cars and engines not counted!
        if (numberRS != numberCars + numberEngines) {
            s = MessageFormat.format(Bundle.getMessage("NoteRSMsg"), new Object[]{Integer.toString(numberRS
                - (numberCars + numberEngines))})
                    + NEW_LINE;
            writer.write(s);
        }
        if (printSchedules.isSelected() || printComments.isSelected() || printDetails.isSelected()
                || printAnalysis.isSelected()) {
            writer.write(FORM_FEED);
        }
    }

    private void printSchedulesSelected() throws IOException {
        List<Location> locations = manager.getLocationsByNameList();
        String s = padOutString(Bundle.getMessage("Schedules"), MAX_NAME_LENGTH) + " " + Bundle.getMessage("Location")
                + " - " + Bundle.getMessage("SpurName") + NEW_LINE;
        writer.write(s);
        List<Schedule> schedules = ScheduleManager.instance().getSchedulesByNameList();
        for (Schedule schedule : schedules) {
            for (Location location : locations) {
                if (_location != null && location != _location) {
                    continue;
                }
                List<Track> spurs = location.getTrackByNameList(Track.SPUR);
                for (Track spur : spurs) {
                    if (spur.getScheduleId().equals(schedule.getId())) {
                        // pad out schedule name
                        s = padOutString(schedule.getName(), MAX_NAME_LENGTH) + " " + location.getName() + " - "
                                + spur.getName();
                        String status = spur.checkScheduleValid();
                        if (!status.equals(Track.SCHEDULE_OKAY)) {
                            StringBuffer buf = new StringBuffer(s);
                            for (int m = s.length(); m < 63; m++) {
                                buf.append(" ");
                            }
                            s = buf.toString();
                            if (s.length() > 63) {
                                s = s.substring(0, 63);
                            }
                            s = s + TAB + status;
                        }
                        s = s + NEW_LINE;
                        writer.write(s);
                        // show the schedule's mode
                        String mode = Bundle.getMessage("Sequential");
                        if (spur.getScheduleMode() == Track.MATCH) {
                            mode = Bundle.getMessage("Match");
                        }
                        s = padOutString("", MAX_NAME_LENGTH) + SPACE + Bundle.getMessage("ScheduleMode") + ": " + mode
                                + NEW_LINE;
                        writer.write(s);
                        // show alternate track if there's one
                        if (spur.getAlternateTrack() != null) {
                            s = padOutString("", MAX_NAME_LENGTH)
                                    + SPACE
                                    + MessageFormat.format(Bundle.getMessage("AlternateTrackName"), new Object[]{spur
                                        .getAlternateTrack().getName()}) + NEW_LINE;
                            writer.write(s);
                        }
                        // show custom loads from staging if not 100%
                        if (spur.getReservationFactor() != 100) {
                            s = padOutString("", MAX_NAME_LENGTH)
                                    + SPACE
                                    + MessageFormat.format(Bundle.getMessage("PercentageStaging"), new Object[]{spur
                                        .getReservationFactor()}) + NEW_LINE;
                            writer.write(s);
                        }
                    }
                }
            }
        }
        if (printComments.isSelected() || printDetails.isSelected() || printAnalysis.isSelected()) {
            writer.write(FORM_FEED);
        }
    }

    private void printCommentsSelected() throws IOException {
        String s = Bundle.getMessage("PrintComments") + NEW_LINE + NEW_LINE;
        writer.write(s);
        List<Location> locations = manager.getLocationsByNameList();
        for (Location location : locations) {
            if (_location != null && location != _location) {
                continue;
            }
            s = location.getName() + NEW_LINE;
            writer.write(s);
            s = SPACE + location.getComment() + NEW_LINE;
            writer.write(s);
            for (Track track : location.getTrackByNameList(null)) {
                if (!track.getComment().equals(Track.NONE) || !track.getCommentBoth().equals(Track.NONE) || !track.getCommentPickup().equals(Track.NONE)
                        || !track.getCommentSetout().equals(Track.NONE)) {
                    s = SPACE + track.getName() + NEW_LINE;
                    writer.write(s);
                    if (!track.getComment().equals(Track.NONE)) {
                        s = SPACE + SPACE + track.getComment() + NEW_LINE;
                        writer.write(s);
                    }
                    if (!track.getCommentBoth().equals(Track.NONE)) {
                        s = SPACE + SPACE + Bundle.getMessage("CommentBoth") + ":" + NEW_LINE;
                        writer.write(s);
                        s = SPACE + SPACE + track.getCommentBoth() + NEW_LINE;
                        writer.write(s);
                    }
                    if (!track.getCommentPickup().equals(Track.NONE)) {
                        s = SPACE + SPACE + Bundle.getMessage("CommentPickup") + ":" + NEW_LINE;
                        writer.write(s);
                        s = SPACE + SPACE + track.getCommentPickup() + NEW_LINE;
                        writer.write(s);
                    }
                    if (!track.getCommentSetout().equals(Track.NONE)) {
                        s = SPACE + SPACE + Bundle.getMessage("CommentSetout") + ":" + NEW_LINE;
                        writer.write(s);
                        s = SPACE + SPACE + track.getCommentSetout() + NEW_LINE;
                        writer.write(s);
                    }
                }
            }
        }
        if (printDetails.isSelected() || printAnalysis.isSelected()) {
            writer.write(FORM_FEED);
        }
    }

    private void printDetailsSelected() throws IOException {
        List<Location> locations = manager.getLocationsByNameList();
        String s = Bundle.getMessage("DetailedReport") + NEW_LINE;
        writer.write(s);
        for (Location location : locations) {
            if (_location != null && location != _location) {
                continue;
            }
            String name = location.getName();
            // services train direction
            int dir = location.getTrainDirections();
            s = NEW_LINE + name + getDirection(dir);
            writer.write(s);
            // services car and engine types
            s = getLocationTypes(location);
            writer.write(s);

            List<Track> yards = location.getTrackByNameList(Track.YARD);
            if (yards.size() > 0) {
                s = SPACE + Bundle.getMessage("YardName") + NEW_LINE;
                writer.write(s);
                printTrackInfo(location, yards);
            }

            List<Track> spurs = location.getTrackByNameList(Track.SPUR);
            if (spurs.size() > 0) {
                s = SPACE + Bundle.getMessage("SpurName") + NEW_LINE;
                writer.write(s);
                printTrackInfo(location, spurs);
            }

            List<Track> interchanges = location.getTrackByNameList(Track.INTERCHANGE);
            if (interchanges.size() > 0) {
                s = SPACE + Bundle.getMessage("InterchangeName") + NEW_LINE;
                writer.write(s);
                printTrackInfo(location, interchanges);
            }

            List<Track> stagings = location.getTrackByNameList(Track.STAGING);
            if (stagings.size() > 0) {
                s = SPACE + Bundle.getMessage("StagingName") + NEW_LINE;
                writer.write(s);
                printTrackInfo(location, stagings);
            }
        }
        if (printAnalysis.isSelected()) {
            writer.write(FORM_FEED);
        }
    }

    private final boolean showStaging = false;

    private void printAnalysisSelected() throws IOException {
        CarManager carManager = CarManager.instance();
        List<Location> locations = manager.getLocationsByNameList();
        List<RollingStock> cars = carManager.getByLocationList();
        String[] carTypes = CarTypes.instance().getNames();

        String s = Bundle.getMessage("TrackAnalysis") + NEW_LINE;
        writer.write(s);

        // print the car type being analyzed
        for (String type : carTypes) {
            // get the total length for a given car type
            int numberOfCars = 0;
            int totalTrackLength = 0;
            for (RollingStock car : cars) {
                if (car.getTypeName().equals(type) && car.getLocation() != null) {
                    numberOfCars++;
                    totalTrackLength += car.getTotalLength();
                }
            }
            writer.write(MessageFormat.format(Bundle.getMessage("NumberTypeLength"), new Object[]{numberOfCars, type,
                totalTrackLength, Setup.getLengthUnit().toLowerCase()})
                    + NEW_LINE);
            // don't bother reporting when the number of cars for a given type is zero
            if (numberOfCars > 0) {
                // spurs
                writer.write(SPACE
                        + MessageFormat.format(Bundle.getMessage("SpurTrackThatAccept"), new Object[]{type})
                        + NEW_LINE);
                int trackLength = getTrackLengthAcceptType(locations, type, Track.SPUR);
                if (trackLength > 0) {
                    writer.write(SPACE
                            + MessageFormat.format(Bundle.getMessage("TotalLengthSpur"), new Object[]{type,
                                trackLength, Setup.getLengthUnit().toLowerCase(), 100 * totalTrackLength / trackLength}) + NEW_LINE);
                } else {
                    writer.write(SPACE + Bundle.getMessage("None") + NEW_LINE);
                }
                // yards
                writer.write(SPACE
                        + MessageFormat.format(Bundle.getMessage("YardTrackThatAccept"), new Object[]{type})
                        + NEW_LINE);
                trackLength = getTrackLengthAcceptType(locations, type, Track.YARD);
                if (trackLength > 0) {
                    writer.write(SPACE
                            + MessageFormat.format(Bundle.getMessage("TotalLengthYard"), new Object[]{type,
                                trackLength, Setup.getLengthUnit().toLowerCase(), 100 * totalTrackLength / trackLength}) + NEW_LINE);
                } else {
                    writer.write(SPACE + Bundle.getMessage("None") + NEW_LINE);
                }
                // interchanges
                writer.write(SPACE
                        + MessageFormat.format(Bundle.getMessage("InterchangesThatAccept"), new Object[]{type})
                        + NEW_LINE);
                trackLength = getTrackLengthAcceptType(locations, type, Track.INTERCHANGE);
                if (trackLength > 0) {
                    writer.write(SPACE
                            + MessageFormat.format(Bundle.getMessage("TotalLengthInterchange"), new Object[]{type,
                                trackLength, Setup.getLengthUnit().toLowerCase(), 100 * totalTrackLength / trackLength}) + NEW_LINE);
                } else {
                    writer.write(SPACE + Bundle.getMessage("None") + NEW_LINE);
                }
                // staging
                if (showStaging) {
                    writer.write(SPACE
                            + MessageFormat.format(Bundle.getMessage("StageTrackThatAccept"), new Object[]{type})
                            + NEW_LINE);
                    trackLength = getTrackLengthAcceptType(locations, type, Track.STAGING);
                    if (trackLength > 0) {
                        writer.write(SPACE
                                + MessageFormat.format(Bundle.getMessage("TotalLengthStage"), new Object[]{type,
                                    trackLength, Setup.getLengthUnit().toLowerCase(), 100 * totalTrackLength / trackLength}) + NEW_LINE);
                    } else {
                        writer.write(SPACE + Bundle.getMessage("None") + NEW_LINE);
                    }
                }
            }
        }
    }

    private int getTrackLengthAcceptType(List<Location> locations, String carType, String trackType) throws IOException {
        int trackLength = 0;
        for (Location location : locations) {
            if (_location != null && location != _location) {
                continue;
            }
            List<Track> tracks = location.getTrackByNameList(trackType);
            for (Track track : tracks) {
                if (track.acceptsTypeName(carType)) {
                    trackLength = trackLength + track.getLength();
                    writer.write(SPACE
                            + SPACE
                            + MessageFormat.format(Bundle.getMessage("LocationTrackLength"), new Object[]{
                                location.getName(), track.getName(), track.getLength(), Setup.getLengthUnit().toLowerCase()}) + NEW_LINE);
                }
            }
        }
        return trackLength;
    }

    private String getTrackString(Track track) {
        String s = TAB + padOutString(track.getName(), Control.max_len_string_track_name) + " "
                + Integer.toString(track.getLength()) + TAB + Integer.toString(track.getUsedLength()) + TAB
                + Integer.toString(track.getNumberRS()) + TAB + Integer.toString(track.getNumberCars()) + TAB
                + Integer.toString(track.getNumberEngines()) + TAB + Integer.toString(track.getPickupRS()) + TAB
                + Integer.toString(track.getDropRS()) + NEW_LINE;
        return s;
    }

    private String getDirection(int dir) {
        if ((Setup.getTrainDirection() & dir) == 0) {
            return " " + Bundle.getMessage("LocalOnly") + NEW_LINE;
        }
        StringBuffer direction = new StringBuffer(" " + Bundle.getMessage("ServicedByTrain") + " ");
        if ((Setup.getTrainDirection() & dir & Location.NORTH) == Location.NORTH) {
            direction.append(Bundle.getMessage("North") + " ");
        }
        if ((Setup.getTrainDirection() & dir & Location.SOUTH) == Location.SOUTH) {
            direction.append(Bundle.getMessage("South") + " ");
        }
        if ((Setup.getTrainDirection() & dir & Location.EAST) == Location.EAST) {
            direction.append(Bundle.getMessage("East") + " ");
        }
        if ((Setup.getTrainDirection() & dir & Location.WEST) == Location.WEST) {
            direction.append(Bundle.getMessage("West") + " ");
        }
        direction.append(NEW_LINE);
        return direction.toString();
    }

    private void printTrackInfo(Location location, List<Track> tracks) {
        for (Track track : tracks) {
            try {
                String s = TAB + track.getName() + getDirection(location.getTrainDirections() & track.getTrainDirections());
                writer.write(s);
                writer.write(getTrackTypes(location, track));
                writer.write(getTrackRoads(track));
                writer.write(getTrackLoads(track));
                writer.write(getTrackShipLoads(track));
                writer.write(getCarOrder(track));
                writer.write(getSetOutTrains(track));
                writer.write(getPickUpTrains(track));
                writer.write(getDestinations(track));
                writer.write(getSchedule(track));
            } catch (IOException we) {
                log.error("Error printing PrintLocationAction: " + we);
            }
        }
    }

    

    private String getLocationTypes(Location location) {
        StringBuffer buf = new StringBuffer(TAB + TAB + Bundle.getMessage("TypesServiced") + NEW_LINE + TAB + TAB);
        int charCount = 0;
        int typeCount = 0;

        for (String type : CarTypes.instance().getNames()) {
            if (location.acceptsTypeName(type)) {
                typeCount++;
                charCount += type.length() + 2;
                if (charCount > charactersPerLine - 2*TAB_LENGTH) {
                    buf.append(NEW_LINE + TAB + TAB);
                    charCount = type.length() + 2;
                }
                buf.append(type + ", ");
            }
        }

        for (String type : EngineTypes.instance().getNames()) {
            if (location.acceptsTypeName(type)) {
                typeCount++;
                charCount += type.length() + 2;
                if (charCount > charactersPerLine - 2*TAB_LENGTH) {
                    buf.append(NEW_LINE + TAB + TAB);
                    charCount = type.length() + 2;
                }
                buf.append(type + ", ");
            }
        }
        if (buf.length() > 2) {
            buf.setLength(buf.length() - 2); // remove trailing separators
        }		// does this location accept all types?
        if (typeCount == CarTypes.instance().getNames().length + EngineTypes.instance().getNames().length) {
            buf = new StringBuffer(TAB + TAB + Bundle.getMessage("LocationAcceptsAllTypes"));
        }
        buf.append(NEW_LINE);
        return buf.toString();
    }

    private String getTrackTypes(Location location, Track track) {
        StringBuffer buf = new StringBuffer(TAB + TAB + Bundle.getMessage("TypesServicedTrack") + NEW_LINE + TAB + TAB);
        int charCount = 0;
        int typeCount = 0;

        for (String type : CarTypes.instance().getNames()) {
            if (track.acceptsTypeName(type)) {
                typeCount++;
                charCount += type.length() + 2;
                if (charCount > charactersPerLine - 2*TAB_LENGTH) {
                    buf.append(NEW_LINE + TAB + TAB);
                    charCount = type.length() + 2;
                }
                buf.append(type + ", ");
            }
        }

        for (String type : EngineTypes.instance().getNames()) {
            if (track.acceptsTypeName(type)) {
                typeCount++;
                charCount += type.length() + 2;
                if (charCount > charactersPerLine - 2*TAB_LENGTH) {
                    buf.append(NEW_LINE + TAB + TAB);
                    charCount = type.length() + 2;
                }
                buf.append(type + ", ");
            }
        }
        if (buf.length() > 2) {
            buf.setLength(buf.length() - 2); // remove trailing separators
        }		// does this track accept all types?
        if (typeCount == CarTypes.instance().getNames().length + EngineTypes.instance().getNames().length) {
            buf = new StringBuffer(TAB + TAB + Bundle.getMessage("TrackAcceptsAllTypes"));
        }
        buf.append(NEW_LINE);
        return buf.toString();
    }

    private String getTrackRoads(Track track) {
        if (track.getRoadOption().equals(Track.ALL_ROADS)) {
            return TAB + TAB + Bundle.getMessage("AcceptsAllRoads") + NEW_LINE;
        }

        String op = Bundle.getMessage("RoadsServicedTrack");
        if (track.getRoadOption().equals(Track.EXCLUDE_ROADS)) {
            op = Bundle.getMessage("ExcludeRoadsTrack");
        }

        StringBuffer buf = new StringBuffer(TAB + TAB + op + NEW_LINE + TAB + TAB);
        int charCount = 0;

        for (String road : track.getRoadNames()) {
            charCount += road.length() + 2;
            if (charCount > charactersPerLine - 2*TAB_LENGTH) {
                buf.append(NEW_LINE + TAB + TAB);
                charCount = road.length() + 2;
            }
            buf.append(road + ", ");
        }
        if (buf.length() > 2) {
            buf.setLength(buf.length() - 2); // remove trailing separators
        }
        buf.append(NEW_LINE);
        return buf.toString();
    }

    private String getTrackLoads(Track track) {
        if (track.getLoadOption().equals(Track.ALL_LOADS)) {
            return TAB + TAB + Bundle.getMessage("AcceptsAllLoads") + NEW_LINE;
        }

        String op = Bundle.getMessage("LoadsServicedTrack");
        if (track.getLoadOption().equals(Track.EXCLUDE_LOADS)) {
            op = Bundle.getMessage("ExcludeLoadsTrack");
        }

        StringBuffer buf = new StringBuffer(TAB + TAB + op + NEW_LINE + TAB + TAB);
        int charCount = 0;

        for (String load : track.getLoadNames()) {
            charCount += load.length() + 2;
            if (charCount > charactersPerLine - 2*TAB_LENGTH) {
                buf.append(NEW_LINE + TAB + TAB);
                charCount = load.length() + 2;
            }
            buf.append(load + ", ");
        }
        if (buf.length() > 2) {
            buf.setLength(buf.length() - 2); // remove trailing separators
        }
        buf.append(NEW_LINE);
        return buf.toString();
    }

    private String getTrackShipLoads(Track track) {
        // only staging has the ship load control
        if (!track.getTrackType().equals(Track.STAGING)) {
            return "";
        }
        if (track.getShipLoadOption().equals(Track.ALL_LOADS)) {
            return TAB + TAB + Bundle.getMessage("ShipsAllLoads") + NEW_LINE;
        }
        String op = Bundle.getMessage("LoadsShippedTrack");
        if (track.getShipLoadOption().equals(Track.EXCLUDE_LOADS)) {
            op = Bundle.getMessage("ExcludeLoadsShippedTrack");
        }

        StringBuffer buf = new StringBuffer(TAB + TAB + op + NEW_LINE + TAB + TAB);
        int charCount = 0;

        for (String load : track.getShipLoadNames()) {
            charCount += load.length() + 2;
            if (charCount > charactersPerLine - 2*TAB_LENGTH) {
                buf.append(NEW_LINE + TAB + TAB);
                charCount = load.length() + 2;
            }
            buf.append(load + ", ");
        }
        if (buf.length() > 2) {
            buf.setLength(buf.length() - 2); // remove trailing separators
        }
        buf.append(NEW_LINE);
        return buf.toString();
    }

    private String getCarOrder(Track track) {
        // only yards and interchanges have the car order option
        if (track.getTrackType().equals(Track.SPUR) || track.getTrackType().equals(Track.STAGING)
                || track.getServiceOrder().equals(Track.NORMAL)) {
            return "";
        }
        if (track.getServiceOrder().equals(Track.FIFO)) {
            return TAB + TAB + Bundle.getMessage("TrackPickUpOrderFIFO") + NEW_LINE;
        }
        return TAB + TAB + Bundle.getMessage("TrackPickUpOrderLIFO") + NEW_LINE;
    }

    private String getSetOutTrains(Track track) {
        if (track.getDropOption().equals(Track.ANY)) {
            return TAB + TAB + Bundle.getMessage("SetOutAllTrains") + NEW_LINE;
        }
        StringBuffer buf;
        int charCount = 0;
        String[] ids = track.getDropIds();
        if (track.getDropOption().equals(Track.TRAINS) || track.getDropOption().equals(Track.EXCLUDE_TRAINS)) {
            String trainType = Bundle.getMessage("TrainsSetOutTrack");
            if (track.getDropOption().equals(Track.EXCLUDE_TRAINS)) {
                trainType = Bundle.getMessage("ExcludeTrainsSetOutTrack");
            }
            buf = new StringBuffer(TAB + TAB + trainType + NEW_LINE + TAB + TAB);
            for (String id : ids) {
                Train train = TrainManager.instance().getTrainById(id);
                if (train == null) {
                    log.info("Could not find a train for id: " + id + " track (" + track.getName() + ")");
                    continue;
                }
                charCount += train.getName().length() + 2;
                if (charCount > charactersPerLine - 2*TAB_LENGTH) {
                    buf.append(NEW_LINE + TAB + TAB);
                    charCount = train.getName().length() + 2;
                }
                buf.append(train.getName() + ", ");
            }
        } else {
            String routeType = Bundle.getMessage("RoutesSetOutTrack");
            if (track.getDropOption().equals(Track.EXCLUDE_ROUTES)) {
                routeType = Bundle.getMessage("ExcludeRoutesSetOutTrack");
            }
            buf = new StringBuffer(TAB + TAB + routeType + NEW_LINE + TAB + TAB);
            for (String id : ids) {
                Route route = RouteManager.instance().getRouteById(id);
                if (route == null) {
                    log.info("Could not find a route for id: " + id + " location (" + track.getLocation().getName()
                            + ") track (" + track.getName() + ")"); // NOI18N
                    continue;
                }
                charCount += route.getName().length() + 2;
                if (charCount > charactersPerLine - 2*TAB_LENGTH) {
                    buf.append(NEW_LINE + TAB + TAB);
                    charCount = route.getName().length() + 2;
                }
                buf.append(route.getName() + ", ");
            }
        }
        if (buf.length() > 2) {
            buf.setLength(buf.length() - 2); // remove trailing separators
        }
        buf.append(NEW_LINE);
        return buf.toString();
    }

    private String getPickUpTrains(Track track) {
        if (track.getPickupOption().equals(Track.ANY)) {
            return TAB + TAB + Bundle.getMessage("PickUpAllTrains") + NEW_LINE;
        }
        StringBuffer buf;
        int charCount = 0;
        String[] ids = track.getPickupIds();
        if (track.getPickupOption().equals(Track.TRAINS) || track.getPickupOption().equals(Track.EXCLUDE_TRAINS)) {
            String trainType = Bundle.getMessage("TrainsPickUpTrack");
            if (track.getPickupOption().equals(Track.EXCLUDE_TRAINS)) {
                trainType = Bundle.getMessage("ExcludeTrainsPickUpTrack");
            }
            buf = new StringBuffer(TAB + TAB + trainType + NEW_LINE + TAB + TAB);
            for (String id : ids) {
                Train train = TrainManager.instance().getTrainById(id);
                if (train == null) {
                    log.info("Could not find a train for id: " + id + " track (" + track.getName() + ")");
                    continue;
                }
                charCount += train.getName().length() + 2;
                if (charCount > charactersPerLine - 2*TAB_LENGTH) {
                    buf.append(NEW_LINE + TAB + TAB);
                    charCount = train.getName().length() + 2;
                }
                buf.append(train.getName() + ", ");
            }
        } else {
            String routeType = Bundle.getMessage("RoutesPickUpTrack");
            if (track.getPickupOption().equals(Track.EXCLUDE_ROUTES)) {
                routeType = Bundle.getMessage("ExcludeRoutesPickUpTrack");
            }
            buf = new StringBuffer(TAB + TAB + routeType + NEW_LINE + TAB + TAB);
            for (String id : ids) {
                Route route = RouteManager.instance().getRouteById(id);
                if (route == null) {
                    log.info("Could not find a route for id: " + id + " location (" + track.getLocation().getName()
                            + ") track (" + track.getName() + ")"); // NOI18N
                    continue;
                }
                charCount += route.getName().length() + 2;
                if (charCount > charactersPerLine - 2*TAB_LENGTH) {
                    buf.append(NEW_LINE + TAB + TAB);
                    charCount = route.getName().length() + 2;
                }
                buf.append(route.getName() + ", ");
            }
        }
        if (buf.length() > 2) {
            buf.setLength(buf.length() - 2); // remove trailing separators
        }
        buf.append(NEW_LINE);
        return buf.toString();
    }

    private String getDestinations(Track track) {
        if (track.getDestinationOption().equals(Track.ALL_DESTINATIONS)) {
            return "";
        }
        String op = Bundle.getMessage("AcceptOnly") + " " + track.getDestinationListSize() + " "
                + Bundle.getMessage("Destinations") + ":";
        if (track.getDestinationOption().equals(Track.EXCLUDE_DESTINATIONS)) {
            op = Bundle.getMessage("Exclude") + " "
                    + (LocationManager.instance().getNumberOfLocations() - track.getDestinationListSize()) + " "
                    + Bundle.getMessage("Destinations") + ":";
        }
        StringBuffer buf = new StringBuffer(TAB + TAB + op + NEW_LINE + TAB + TAB);
        String[] destIds = track.getDestinationIds();
        int charCount = 0;
        for (String id : destIds) {
            Location location = manager.getLocationById(id);
            if (location == null) {
                continue;
            }
            charCount += location.getName().length() + 2;
            if (charCount > charactersPerLine - 2*TAB_LENGTH) {
                buf.append(NEW_LINE + TAB + TAB);
                charCount = location.getName().length() + 2;
            }
            buf.append(location.getName() + ", ");
        }
        if (buf.length() > 2) {
            buf.setLength(buf.length() - 2); // remove trailing separators
        }
        buf.append(NEW_LINE);
        return buf.toString();
    }

    private String getSchedule(Track track) {
        // only spurs have schedules
        if (!track.getTrackType().equals(Track.SPUR) || track.getSchedule() == null) {
            return "";
        }
        StringBuffer buf = new StringBuffer(TAB
                + TAB
                + MessageFormat
                .format(Bundle.getMessage("TrackScheduleName"), new Object[]{track.getScheduleName()})
                + NEW_LINE);
        if (track.getAlternateTrack() != null) {
            buf.append(TAB
                    + TAB
                    + MessageFormat.format(Bundle.getMessage("AlternateTrackName"), new Object[]{track
                        .getAlternateTrack().getName()}) + NEW_LINE);
        }
        if (track.getReservationFactor() != 100) {
            buf.append(TAB
                    + TAB
                    + MessageFormat.format(Bundle.getMessage("PercentageStaging"), new Object[]{track
                        .getReservationFactor()}) + NEW_LINE);
        }
        return buf.toString();
    }

    private String padOutString(String s, int length) {
        StringBuffer buf = new StringBuffer(s);
        for (int n = s.length(); n < length; n++) {
            buf.append(" ");
        }
        return buf.toString();
    }

    JCheckBox printLocations = new JCheckBox(Bundle.getMessage("PrintLocations"));
    JCheckBox printSchedules = new JCheckBox(Bundle.getMessage("PrintSchedules"));
    JCheckBox printComments = new JCheckBox(Bundle.getMessage("PrintComments"));
    JCheckBox printDetails = new JCheckBox(Bundle.getMessage("PrintDetails"));
    JCheckBox printAnalysis = new JCheckBox(Bundle.getMessage("PrintAnalysis"));

    JButton okayButton = new JButton(Bundle.getMessage("ButtonOkay"));

    public class LocationPrintOptionFrame extends OperationsFrame {

        PrintLocationsAction pla;

        public LocationPrintOptionFrame(PrintLocationsAction pla) {
            super();
            this.pla = pla;
            // create panel
            JPanel pPanel = new JPanel();
            pPanel.setLayout(new GridBagLayout());
            pPanel.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("PrintOptions")));
            addItemLeft(pPanel, printLocations, 0, 0);
            addItemLeft(pPanel, printSchedules, 0, 3);
            addItemLeft(pPanel, printComments, 0, 5);
            addItemLeft(pPanel, printDetails, 0, 7);
            addItemLeft(pPanel, printAnalysis, 0, 9);
            // set defaults
            printLocations.setSelected(true);
            printSchedules.setSelected(true);
            printComments.setSelected(false);
            printDetails.setSelected(false);
            printAnalysis.setSelected(false);

            // add tool tips
            JPanel pButtons = new JPanel();
            pButtons.setLayout(new GridBagLayout());
            pButtons.add(okayButton);
            addButtonAction(okayButton);

            getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
            getContentPane().add(pPanel);
            getContentPane().add(pButtons);
            setPreferredSize(null);
            if (_isPreview) {
                setTitle(Bundle.getMessage("MenuItemPreview"));
            } else {
                setTitle(Bundle.getMessage("MenuItemPrint"));
            }
            initMinimumSize(new Dimension(Control.panelWidth300, Control.panelHeight250));
        }

        @Override
        public void initComponents() {

        }

        @Override
        public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
            setVisible(false);
            pla.printLocations();
        }
    }

    private final static Logger log = LoggerFactory.getLogger(PrintLocationsAction.class.getName());
}
