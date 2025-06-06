package jmri.jmrit.operations.locations.tools;

import java.awt.*;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;

import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.locations.*;
import jmri.jmrit.operations.locations.schedules.*;
import jmri.jmrit.operations.rollingstock.cars.*;
import jmri.jmrit.operations.rollingstock.engines.EngineTypes;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteManager;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.jmrit.operations.trains.trainbuilder.TrainCommon;
import jmri.util.davidflanagan.HardcopyWriter;

/**
 * Frame to print a summary of the Location Roster contents
 * <p>
 * This uses the older style printing, for compatibility with Java 1.1.8 in
 * Macintosh MRJ
 *
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Dennis Miller Copyright (C) 2005
 * @author Daniel Boudreau Copyright (C) 2008, 2011, 2012, 2014, 2022, 2023
 */
public class PrintLocationsFrame extends OperationsFrame {

    static final String FORM_FEED = "\f"; // NOI18N
    static final String TAB = "\t"; // NOI18N
    static final int TAB_LENGTH = 10;
    static final String SPACES_2 = "  ";
    static final String SPACES_3 = "   ";
    static final String SPACES_4 = "    ";

    static final int MAX_NAME_LENGTH = Control.max_len_string_location_name;

    JCheckBox printLocations = new JCheckBox(Bundle.getMessage("PrintLocations"));
    JCheckBox printSchedules = new JCheckBox(Bundle.getMessage("PrintSchedules"));
    JCheckBox printComments = new JCheckBox(Bundle.getMessage("PrintComments"));
    JCheckBox printDetails = new JCheckBox(Bundle.getMessage("PrintDetails"));
    JCheckBox printAnalysis = new JCheckBox(Bundle.getMessage("PrintAnalysis"));
    JCheckBox printErrorAnalysis = new JCheckBox(Bundle.getMessage("PrintErrorAnalysis"));

    JButton okayButton = new JButton(Bundle.getMessage("ButtonOK"));

    LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
    CarTypes cts = InstanceManager.getDefault(CarTypes.class);
    CarLoads cls = InstanceManager.getDefault(CarLoads.class);
    CarRoads crs = InstanceManager.getDefault(CarRoads.class);

    boolean _isPreview;
    Location _location;

    private int charactersPerLine = 70;

    HardcopyWriter writer;

    public PrintLocationsFrame(boolean isPreview, Location location) {
        super();
        _isPreview = isPreview;
        _location = location;

        // create panel
        JPanel pPanel = new JPanel();
        pPanel.setLayout(new GridBagLayout());
        pPanel.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("PrintOptions")));
        addItemLeft(pPanel, printLocations, 0, 0);
        addItemLeft(pPanel, printSchedules, 0, 3);
        addItemLeft(pPanel, printComments, 0, 5);
        addItemLeft(pPanel, printDetails, 0, 7);
        addItemLeft(pPanel, printAnalysis, 0, 9);
        addItemLeft(pPanel, printErrorAnalysis, 0, 11);

        // set defaults
        printLocations.setSelected(true);
        printSchedules.setSelected(false);
        printComments.setSelected(false);
        printDetails.setSelected(false);
        printAnalysis.setSelected(false);
        printErrorAnalysis.setSelected(false);

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
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        setVisible(false);
        printLocations();
    }

    private void printLocations() {
        // prevent NPE on close
        if (!printLocations.isSelected() &&
                !printSchedules.isSelected() &&
                !printComments.isSelected() &&
                !printDetails.isSelected() &&
                !printAnalysis.isSelected() &&
                !printErrorAnalysis.isSelected()) {
            return;
        }
        // obtain a HardcopyWriter
        String title = Bundle.getMessage("TitleLocationsTable");
        if (_location != null) {
            title = _location.getName();
        }
        try (HardcopyWriter writer =
                new HardcopyWriter(new Frame(), title, Control.reportFontSize, .5, .5, .5, .5, _isPreview)) {

            this.writer = writer;

            charactersPerLine = writer.getCharactersPerLine();

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
            if (printErrorAnalysis.isSelected()) {
                printErrorAnalysisSelected();
            }
        } catch (HardcopyWriter.PrintCanceledException ex) {
            log.debug("Print canceled");
        } catch (IOException we) {
            log.error("Error printing PrintLocationAction: {}", we.getLocalizedMessage());
        }
    }

    // Loop through the Roster, printing as needed
    private void printLocationsSelected() throws IOException {
        List<Location> locations = lmanager.getLocationsByNameList();
        int totalLength = 0;
        int usedLength = 0;
        int numberRS = 0;
        int numberCars = 0;
        int numberEngines = 0;
        // header
        String s = Bundle.getMessage("Location") +
                TAB +
                TAB +
                TAB +
                Bundle.getMessage("Length") +
                " " +
                Bundle.getMessage("Used") +
                TAB +
                Bundle.getMessage("RS") +
                TAB +
                Bundle.getMessage("Cars") +
                TAB +
                Bundle.getMessage("Engines") +
                TAB +
                Bundle.getMessage("Pickups") +
                " " +
                Bundle.getMessage("Drop") +
                NEW_LINE;
        writer.write(s);
        for (Location location : locations) {
            if (_location != null && location != _location) {
                continue;
            }
            // location name, track length, used, number of RS, scheduled pick
            // ups and drops
            s = padOutString(location.getName(), MAX_NAME_LENGTH) +
                    TAB +
                    "  " +
                    Integer.toString(location.getLength()) +
                    TAB +
                    Integer.toString(location.getUsedLength()) +
                    TAB +
                    Integer.toString(location.getNumberRS()) +
                    TAB +
                    Integer.toString(location.getNumberCars()) +
                    TAB +
                    Integer.toString(location.getNumberEngines()) +
                    TAB +
                    Integer.toString(location.getPickupRS()) +
                    TAB +
                    Integer.toString(location.getDropRS()) +
                    NEW_LINE;
            writer.write(s);

            if (location.getDivision() != null) {
                writer.write(SPACES_3 + Bundle.getMessage("Division") + ": " + location.getDivisionName() + NEW_LINE);
            }

            totalLength += location.getLength();
            usedLength += location.getUsedLength();
            numberRS += location.getNumberRS();

            List<Track> yards = location.getTracksByNameList(Track.YARD);
            if (yards.size() > 0) {
                // header
                writer.write(SPACES_3 + Bundle.getMessage("YardName") + NEW_LINE);
                for (Track yard : yards) {
                    writer.write(getTrackString(yard));
                    numberCars += yard.getNumberCars();
                    numberEngines += yard.getNumberEngines();
                }
            }

            List<Track> spurs = location.getTracksByNameList(Track.SPUR);
            if (spurs.size() > 0) {
                // header
                writer.write(SPACES_3 + Bundle.getMessage("SpurName") + NEW_LINE);
                for (Track spur : spurs) {
                    writer.write(getTrackString(spur));
                    numberCars += spur.getNumberCars();
                    numberEngines += spur.getNumberEngines();
                }
            }

            List<Track> interchanges = location.getTracksByNameList(Track.INTERCHANGE);
            if (interchanges.size() > 0) {
                // header
                writer.write(SPACES_3 + Bundle.getMessage("InterchangeName") + NEW_LINE);
                for (Track interchange : interchanges) {
                    writer.write(getTrackString(interchange));
                    numberCars += interchange.getNumberCars();
                    numberEngines += interchange.getNumberEngines();
                }
            }

            List<Track> stagingTracks = location.getTracksByNameList(Track.STAGING);
            if (stagingTracks.size() > 0) {
                // header
                writer.write(SPACES_3 + Bundle.getMessage("StagingName") + NEW_LINE);
                for (Track staging : stagingTracks) {
                    writer.write(getTrackString(staging));
                    numberCars += staging.getNumberCars();
                    numberEngines += staging.getNumberEngines();
                }
            }
            writer.write(NEW_LINE);
        }

        // summary
        s = MessageFormat
                .format(Bundle.getMessage("TotalLengthMsg"),
                        new Object[]{Integer.toString(totalLength), Integer.toString(usedLength),
                                totalLength > 0 ? Integer.toString(usedLength * 100 / totalLength) : 0}) +
                NEW_LINE;
        writer.write(s);
        s = MessageFormat
                .format(Bundle.getMessage("TotalRollingMsg"),
                        new Object[]{Integer.toString(numberRS), Integer.toString(numberCars),
                                Integer.toString(numberEngines)}) +
                NEW_LINE;
        writer.write(s);
        // are there trains en route, then some cars and engines not counted!
        if (numberRS != numberCars + numberEngines) {
            s = Bundle.getMessage("NoteRSMsg", Integer.toString(numberRS - (numberCars + numberEngines))) + NEW_LINE;
            writer.write(s);
        }
        if (printSchedules.isSelected() ||
                printComments.isSelected() ||
                printDetails.isSelected() ||
                printAnalysis.isSelected() ||
                printErrorAnalysis.isSelected()) {
            writer.write(FORM_FEED);
        }
    }

    private void printSchedulesSelected() throws IOException {
        List<Location> locations = lmanager.getLocationsByNameList();
        String s = padOutString(Bundle.getMessage("Schedules"), MAX_NAME_LENGTH) +
                " " +
                Bundle.getMessage("Location") +
                " - " +
                Bundle.getMessage("SpurName") +
                NEW_LINE;
        writer.write(s);
        List<Schedule> schedules = InstanceManager.getDefault(ScheduleManager.class).getSchedulesByNameList();
        for (Schedule schedule : schedules) {
            for (Location location : locations) {
                if (_location != null && location != _location) {
                    continue;
                }
                List<Track> spurs = location.getTracksByNameList(Track.SPUR);
                for (Track spur : spurs) {
                    if (spur.getScheduleId().equals(schedule.getId())) {
                        // pad out schedule name
                        s = padOutString(schedule.getName(),
                                MAX_NAME_LENGTH) + " " + location.getName() + " - " + spur.getName();
                        String status = spur.checkScheduleValid();
                        if (!status.equals(Schedule.SCHEDULE_OKAY)) {
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
                        s = padOutString("", MAX_NAME_LENGTH) +
                                SPACES_3 +
                                Bundle.getMessage("ScheduleMode") +
                                ": " +
                                spur.getScheduleModeName() +
                                NEW_LINE;
                        writer.write(s);
                        // show alternate track if there's one
                        if (spur.getAlternateTrack() != null) {
                            s = padOutString("", MAX_NAME_LENGTH) +
                                    SPACES_3 +
                                    Bundle.getMessage("AlternateTrackName", spur.getAlternateTrack().getName()) +
                                    NEW_LINE;
                            writer.write(s);
                        }
                        // show custom loads from staging if not 100%
                        if (spur.getReservationFactor() != 100) {
                            s = padOutString("", MAX_NAME_LENGTH) +
                                    SPACES_3 +
                                    Bundle.getMessage("PercentageStaging",
                                            spur.getReservationFactor()) +
                                    NEW_LINE;
                            writer.write(s);
                        }
                    }
                }
            }
        }
        // now show the contents of each schedule
        for (Schedule schedule : schedules) {
            writer.write(FORM_FEED);
            s = schedule.getName() + NEW_LINE;
            writer.write(s);

            for (ScheduleItem si : schedule.getItemsBySequenceList()) {
                s = padOutString(Bundle.getMessage("Type"), cts.getMaxNameLength() + 1) +
                        padOutString(Bundle.getMessage("Receive"), cls.getMaxNameLength() + 1) +
                        padOutString(Bundle.getMessage("Ship"), cls.getMaxNameLength() + 1) +
                        padOutString(Bundle.getMessage("Destination"), lmanager.getMaxLocationNameLength() + 1) +
                        Bundle.getMessage("Track") +
                        NEW_LINE;
                writer.write(s);
                s = padOutString(si.getTypeName(), cts.getMaxNameLength() + 1) +
                        padOutString(si.getReceiveLoadName(), cls.getMaxNameLength() + 1) +
                        padOutString(si.getShipLoadName(), cls.getMaxNameLength() + 1) +
                        padOutString(si.getDestinationName(), lmanager.getMaxLocationNameLength() + 1) +
                        si.getDestinationTrackName() +
                        NEW_LINE;
                writer.write(s);

                s = padOutString("", cts.getMaxNameLength() + 1) +
                        padOutString(Bundle.getMessage("Random"), Bundle.getMessage("Random").length() + 1) +
                        padOutString(Bundle.getMessage("Delivery"), Bundle.getMessage("Delivery").length() + 1) +
                        padOutString(Bundle.getMessage("Road"), crs.getMaxNameLength() + 1) +
                        padOutString(Bundle.getMessage("Pickup"), Bundle.getMessage("Delivery").length() + 1) +
                        Bundle.getMessage("Wait") +
                        NEW_LINE;
                writer.write(s);

                s = padOutString("", cts.getMaxNameLength() + 1) +
                        padOutString(si.getRandom(), Bundle.getMessage("Random").length() + 1) +
                        padOutString(si.getSetoutTrainScheduleName(), Bundle.getMessage("Delivery").length() + 1) +
                        padOutString(si.getRoadName(), crs.getMaxNameLength() + 1) +
                        padOutString(si.getPickupTrainScheduleName(), Bundle.getMessage("Delivery").length() + 1) +
                        si.getWait() +
                        NEW_LINE;
                writer.write(s);
            }
        }
        if (printComments.isSelected() ||
                printDetails.isSelected() ||
                printAnalysis.isSelected() ||
                printErrorAnalysis.isSelected()) {
            writer.write(FORM_FEED);
        }
    }

    private void printCommentsSelected() throws IOException {
        String s = Bundle.getMessage("PrintComments") + NEW_LINE + NEW_LINE;
        writer.write(s);
        List<Location> locations = lmanager.getLocationsByNameList();
        for (Location location : locations) {
            if (_location != null && location != _location) {
                continue;
            }
            s = location.getName() + NEW_LINE;
            writer.write(s);
            s = SPACES_3 + location.getComment() + NEW_LINE;
            writer.write(s);
            for (Track track : location.getTracksByNameList(null)) {
                if (!track.getComment().equals(Track.NONE) ||
                        !track.getCommentBoth().equals(Track.NONE) ||
                        !track.getCommentPickup().equals(Track.NONE) ||
                        !track.getCommentSetout().equals(Track.NONE)) {
                    s = SPACES_2 + track.getName() + NEW_LINE;
                    writer.write(s);
                    if (!track.getComment().equals(Track.NONE)) {
                        s = SPACES_4 + track.getComment() + NEW_LINE;
                        writer.write(s);
                    }
                    if (!track.getCommentBoth().equals(Track.NONE)) {
                        s = SPACES_3 + Bundle.getMessage("CommentBoth") + ":" + NEW_LINE;
                        writer.write(s);
                        s = SPACES_4 + track.getCommentBoth() + NEW_LINE;
                        writer.write(s);
                    }
                    if (!track.getCommentPickup().equals(Track.NONE)) {
                        s = SPACES_3 + Bundle.getMessage("CommentPickup") + ":" + NEW_LINE;
                        writer.write(s);
                        s = SPACES_4 + track.getCommentPickup() + NEW_LINE;
                        writer.write(s);
                    }
                    if (!track.getCommentSetout().equals(Track.NONE)) {
                        s = SPACES_3 + Bundle.getMessage("CommentSetout") + ":" + NEW_LINE;
                        writer.write(s);
                        s = SPACES_4 + track.getCommentSetout() + NEW_LINE;
                        writer.write(s);
                    }
                }
            }
        }
        if (printDetails.isSelected() || printAnalysis.isSelected() || printErrorAnalysis.isSelected()) {
            writer.write(FORM_FEED);
        }
    }

    private void printDetailsSelected() throws IOException {
        List<Location> locations = lmanager.getLocationsByNameList();
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

            // division
            if (location.getDivision() != null) {
                s = SPACES_3 + Bundle.getMessage("Division") + ": " + location.getDivisionName() + NEW_LINE;
                writer.write(s);
            }

            // services car and engine types
            s = getLocationTypes(location);
            writer.write(s);

            List<Track> spurs = location.getTracksByNameList(Track.SPUR);
            if (spurs.size() > 0) {
                s = SPACES_3 + Bundle.getMessage("SpurName") + NEW_LINE;
                writer.write(s);
                printTrackInfo(location, spurs);
            }

            List<Track> yards = location.getTracksByNameList(Track.YARD);
            if (yards.size() > 0) {
                s = SPACES_3 + Bundle.getMessage("YardName") + NEW_LINE;
                writer.write(s);
                printTrackInfo(location, yards);
            }

            List<Track> interchanges = location.getTracksByNameList(Track.INTERCHANGE);
            if (interchanges.size() > 0) {
                s = SPACES_3 + Bundle.getMessage("InterchangeName") + NEW_LINE;
                writer.write(s);
                printTrackInfo(location, interchanges);
            }

            List<Track> staging = location.getTracksByNameList(Track.STAGING);
            if (staging.size() > 0) {
                s = SPACES_3 + Bundle.getMessage("StagingName") + NEW_LINE;
                writer.write(s);
                printTrackInfo(location, staging);
            }
        }
        if (printAnalysis.isSelected() || printErrorAnalysis.isSelected()) {
            writer.write(FORM_FEED);
        }
    }

    private final boolean showStaging = true;

    private void printAnalysisSelected() throws IOException {
        CarManager carManager = InstanceManager.getDefault(CarManager.class);
        List<Location> locations = lmanager.getLocationsByNameList();
        List<Car> cars = carManager.getByLocationList();
        String[] carTypes = cts.getNames();

        String s = Bundle.getMessage("TrackAnalysis") + NEW_LINE;
        writer.write(s);

        // print the car type being analyzed
        for (String type : carTypes) {
            // get the total length for a given car type
            int numberOfCars = 0;
            int totalTrackLength = 0;
            for (Car car : cars) {
                if (car.getTypeName().equals(type) && car.getLocation() != null) {
                    numberOfCars++;
                    totalTrackLength += car.getTotalLength();
                }
            }
            writer.write(Bundle.getMessage("NumberTypeLength",
                    numberOfCars, type, totalTrackLength, Setup.getLengthUnit().toLowerCase()) +
                    NEW_LINE);
            // don't bother reporting when the number of cars for a given type
            // is zero. Round up percentage used by a car type.
            if (numberOfCars > 0) {
                // spurs
                writer.write(SPACES_3 +
                        Bundle.getMessage("SpurTrackThatAccept", type) +
                        NEW_LINE);
                int trackLength = getTrackLengthAcceptType(locations, type, Track.SPUR);
                if (trackLength > 0) {
                    writer.write(SPACES_3 +
                            Bundle.getMessage("TotalLengthSpur", type, trackLength, Setup.getLengthUnit().toLowerCase(),
                                    Math.ceil((double) 100 * totalTrackLength / trackLength)) +
                            NEW_LINE);
                } else {
                    writer.write(SPACES_3 + Bundle.getMessage("None") + NEW_LINE);
                }
                // yards
                writer.write(SPACES_3 +
                        Bundle.getMessage("YardTrackThatAccept", type) +
                        NEW_LINE);
                trackLength = getTrackLengthAcceptType(locations, type, Track.YARD);
                if (trackLength > 0) {
                    writer.write(SPACES_3 +
                            Bundle.getMessage("TotalLengthYard", type, trackLength, Setup.getLengthUnit().toLowerCase(),
                                    Math.ceil((double) 100 * totalTrackLength / trackLength)) +
                            NEW_LINE);
                } else {
                    writer.write(SPACES_3 + Bundle.getMessage("None") + NEW_LINE);
                }
                // interchanges
                writer.write(SPACES_3 +
                        Bundle.getMessage("InterchangesThatAccept", type) +
                        NEW_LINE);
                trackLength = getTrackLengthAcceptType(locations, type, Track.INTERCHANGE);
                if (trackLength > 0) {
                    writer.write(SPACES_3 +
                            Bundle.getMessage("TotalLengthInterchange",
                                    type, trackLength, Setup.getLengthUnit().toLowerCase(),
                                    Math.ceil((double) 100 * totalTrackLength / trackLength)) +
                            NEW_LINE);
                } else {
                    writer.write(SPACES_3 + Bundle.getMessage("None") + NEW_LINE);
                }
                // staging
                if (showStaging) {
                    writer.write(SPACES_3 +
                            Bundle.getMessage("StageTrackThatAccept", type) +
                            NEW_LINE);
                    trackLength = getTrackLengthAcceptType(locations, type, Track.STAGING);
                    if (trackLength > 0) {
                        writer.write(SPACES_3 +
                                Bundle.getMessage("TotalLengthStage",
                                        type, trackLength, Setup.getLengthUnit().toLowerCase(),
                                        Math.ceil((double) 100 * totalTrackLength / trackLength)) +
                                NEW_LINE);
                    } else {
                        writer.write(SPACES_3 + Bundle.getMessage("None") + NEW_LINE);
                    }
                }
            }
        }
        if (printErrorAnalysis.isSelected()) {
            writer.write(FORM_FEED);
        }
    }

    private void printErrorAnalysisSelected() throws IOException {
        writer.write(Bundle.getMessage("TrackErrorAnalysis") + NEW_LINE);
        boolean foundError = false;
        for (Location location : lmanager.getLocationsByNameList()) {
            if (_location != null && location != _location) {
                continue;
            }
            writer.write(location.getName() + NEW_LINE);
            for (Track track : location.getTracksByNameList(null)) {
                if (!track.checkPickups().equals(Track.PICKUP_OKAY)) {
                    writer.write(TAB + track.checkPickups() + NEW_LINE);
                    foundError = true;
                }
            }
        }
        if (!foundError) {
            writer.write(Bundle.getMessage("NoErrors"));
        }
    }

    private int getTrackLengthAcceptType(List<Location> locations, String carType,
            String trackType)
            throws IOException {
        int trackLength = 0;
        for (Location location : locations) {
            if (_location != null && location != _location) {
                continue;
            }
            List<Track> tracks = location.getTracksByNameList(trackType);
            for (Track track : tracks) {
                if (track.isTypeNameAccepted(carType)) {
                    trackLength = trackLength + track.getLength();
                    writer.write(SPACES_3 +
                            SPACES_3 +
                            Bundle.getMessage("LocationTrackLength",
                                    location.getName(), track.getName(), track.getLength(),
                                    Setup.getLengthUnit().toLowerCase()) +
                            NEW_LINE);
                }
            }
        }
        return trackLength;
    }

    private String getTrackString(Track track) {
        String s = TAB +
                padOutString(track.getName(), Control.max_len_string_track_name) +
                " " +
                Integer.toString(track.getLength()) +
                TAB +
                Integer.toString(track.getUsedLength()) +
                TAB +
                Integer.toString(track.getNumberRS()) +
                TAB +
                Integer.toString(track.getNumberCars()) +
                TAB +
                Integer.toString(track.getNumberEngines()) +
                TAB +
                Integer.toString(track.getPickupRS()) +
                TAB +
                Integer.toString(track.getDropRS()) +
                NEW_LINE;
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
                String s = TAB +
                        track.getName() +
                        getDirection(location.getTrainDirections() & track.getTrainDirections());
                writer.write(s);
                isAlternate(track);
                writer.write(getTrackCarTypes(track));
                writer.write(getTrackEngineTypes(track));
                writer.write(getTrackRoads(track));
                writer.write(getTrackLoads(track));
                writer.write(getTrackShipLoads(track));
                writer.write(getCarOrder(track));
                writer.write(getSetOutTrains(track));
                writer.write(getPickUpTrains(track));
                writer.write(getDestinations(track));
                writer.write(getTrackInfo(track));
                writer.write(getSpurInfo(track));
                writer.write(getSchedule(track));
                writer.write(getStagingInfo(track));
                writer.write(NEW_LINE);
            } catch (IOException we) {
                log.error("Error printing PrintLocationAction: {}", we.getLocalizedMessage());
            }
        }
    }

    private String getLocationTypes(Location location) {
        StringBuffer buf = new StringBuffer(TAB + TAB + Bundle.getMessage("TypesServiced") + NEW_LINE + TAB + TAB);
        int charCount = 0;
        int typeCount = 0;

        for (String type : cts.getNames()) {
            if (location.acceptsTypeName(type)) {
                typeCount++;
                charCount += type.length() + 2;
                if (charCount > charactersPerLine - 2 * TAB_LENGTH) {
                    buf.append(NEW_LINE + TAB + TAB);
                    charCount = type.length() + 2;
                }
                buf.append(type + ", ");
            }
        }

        for (String type : InstanceManager.getDefault(EngineTypes.class).getNames()) {
            if (location.acceptsTypeName(type)) {
                typeCount++;
                charCount += type.length() + 2;
                if (charCount > charactersPerLine - 2 * TAB_LENGTH) {
                    buf.append(NEW_LINE + TAB + TAB);
                    charCount = type.length() + 2;
                }
                buf.append(type + ", ");
            }
        }
        if (buf.length() > 2) {
            buf.setLength(buf.length() - 2); // remove trailing separators
        }
        // does this location accept all types?
        if (typeCount == cts.getNames().length + InstanceManager.getDefault(EngineTypes.class).getNames().length) {
            buf = new StringBuffer(TAB + TAB + Bundle.getMessage("LocationAcceptsAllTypes"));
        }
        buf.append(NEW_LINE);
        return buf.toString();
    }

    private String getTrackCarTypes(Track track) {
        StringBuffer buf =
                new StringBuffer(TAB + TAB + Bundle.getMessage("CarTypesServicedTrack") + NEW_LINE + TAB + TAB);
        int charCount = 0;
        int typeCount = 0;

        for (String type : cts.getNames()) {
            if (track.isTypeNameAccepted(type)) {
                typeCount++;
                charCount += type.length() + 2;
                if (charCount > charactersPerLine - 2 * TAB_LENGTH) {
                    buf.append(NEW_LINE + TAB + TAB);
                    charCount = type.length() + 2;
                }
                buf.append(type + ", ");
            }
        }
        if (buf.length() > 2) {
            buf.setLength(buf.length() - 2); // remove trailing separators
        }
        // does this track accept all types?
        if (typeCount == cts.getNames().length) {
            buf = new StringBuffer(TAB + TAB + Bundle.getMessage("TrackAcceptsAllCarTypes"));
        }
        buf.append(NEW_LINE);
        return buf.toString();
    }

    private String getTrackEngineTypes(Track track) {
        StringBuffer buf =
                new StringBuffer(TAB + TAB + Bundle.getMessage("EngineTypesServicedTrack") + NEW_LINE + TAB + TAB);
        int charCount = 0;
        int typeCount = 0;

        for (String type : InstanceManager.getDefault(EngineTypes.class).getNames()) {
            if (track.isTypeNameAccepted(type)) {
                typeCount++;
                charCount += type.length() + 2;
                if (charCount > charactersPerLine - 2 * TAB_LENGTH) {
                    buf.append(NEW_LINE + TAB + TAB);
                    charCount = type.length() + 2;
                }
                buf.append(type + ", ");
            }
        }
        if (buf.length() > 2) {
            buf.setLength(buf.length() - 2); // remove trailing separators
        }
        // does this track accept all types?
        if (typeCount == InstanceManager.getDefault(EngineTypes.class).getNames().length) {
            buf = new StringBuffer(TAB + TAB + Bundle.getMessage("TrackAcceptsAllEngTypes"));
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
            if (charCount > charactersPerLine - 2 * TAB_LENGTH) {
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
            if (charCount > charactersPerLine - 2 * TAB_LENGTH) {
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
        if (!track.isStaging()) {
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
            if (charCount > charactersPerLine - 2 * TAB_LENGTH) {
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
        if (track.isSpur() || track.isStaging() || track.getServiceOrder().equals(Track.NORMAL)) {
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
                Train train = InstanceManager.getDefault(TrainManager.class).getTrainById(id);
                if (train == null) {
                    log.info("Could not find a train for id: {} track ({})", id, track.getName());
                    continue;
                }
                charCount += train.getName().length() + 2;
                if (charCount > charactersPerLine - 2 * TAB_LENGTH) {
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
                Route route = InstanceManager.getDefault(RouteManager.class).getRouteById(id);
                if (route == null) {
                    log.info("Could not find a route for id: {} location ({}) track ({})", id,
                            track.getLocation().getName(), track.getName()); // NOI18N
                    continue;
                }
                charCount += route.getName().length() + 2;
                if (charCount > charactersPerLine - 2 * TAB_LENGTH) {
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
                Train train = InstanceManager.getDefault(TrainManager.class).getTrainById(id);
                if (train == null) {
                    log.info("Could not find a train for id: {} track ({})", id, track.getName());
                    continue;
                }
                charCount += train.getName().length() + 2;
                if (charCount > charactersPerLine - 2 * TAB_LENGTH) {
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
                Route route = InstanceManager.getDefault(RouteManager.class).getRouteById(id);
                if (route == null) {
                    log.info("Could not find a route for id: {} location ({}) track ({})", id,
                            track.getLocation().getName(), track.getName()); // NOI18N
                    continue;
                }
                charCount += route.getName().length() + 2;
                if (charCount > charactersPerLine - 2 * TAB_LENGTH) {
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
        StringBuffer buf = new StringBuffer();
        if (track.isOnlyCarsWithFinalDestinationEnabled()) {
            buf.append(TAB + TAB + Bundle.getMessage("OnlyCarsWithFD"));
            buf.append(NEW_LINE);
        }
        if (track.getDestinationOption().equals(Track.ALL_DESTINATIONS)) {
            return buf.toString();
        }
        String op = Bundle.getMessage(
                "AcceptOnly") + " " + track.getDestinationListSize() + " " + Bundle.getMessage("Destinations") + ":";
        if (track.getDestinationOption().equals(Track.EXCLUDE_DESTINATIONS)) {
            op = Bundle.getMessage("Exclude") +
                    " " +
                    (lmanager.getNumberOfLocations() - track.getDestinationListSize()) +
                    " " +
                    Bundle.getMessage("Destinations") +
                    ":";
        }
        buf.append(TAB + TAB + op + NEW_LINE + TAB + TAB);
        String[] destIds = track.getDestinationIds();
        int charCount = 0;
        for (String id : destIds) {
            Location location = lmanager.getLocationById(id);
            if (location == null) {
                continue;
            }
            charCount += location.getName().length() + 2;
            if (charCount > charactersPerLine - 2 * TAB_LENGTH) {
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

    private String getTrackInfo(Track track) {
        if (track.getPool() != null) {
            StringBuffer buf =
                    new StringBuffer(TAB + TAB + Bundle.getMessage("Pool") + ": " + track.getPoolName() + NEW_LINE);
            return buf.toString();
        }
        return "";
    }

    private String getSchedule(Track track) {
        // only spurs have schedules
        if (!track.isSpur() || track.getSchedule() == null) {
            return "";
        }
        StringBuffer buf = new StringBuffer(TAB +
                TAB +
                Bundle.getMessage("TrackScheduleName", track.getScheduleName()) +
                NEW_LINE);
        if (track.getAlternateTrack() != null) {
            buf.append(TAB +
                    TAB +
                    Bundle.getMessage("AlternateTrackName",
                            track.getAlternateTrack().getName()) +
                    NEW_LINE);
        }
        if (track.getReservationFactor() != 100) {
            buf.append(TAB +
                    TAB +
                    Bundle.getMessage("PercentageStaging",
                            track.getReservationFactor()) +
                    NEW_LINE);
        }
        return buf.toString();
    }

    private void isAlternate(Track track) throws IOException {
        if (track.isAlternate()) {
            writer.write(TAB + TAB + Bundle.getMessage("AlternateTrack") + NEW_LINE);
        }
    }

    private String getSpurInfo(Track track) {
        if (!track.isSpur()) {
            return "";
        }

        StringBuffer buf = new StringBuffer();

        if (track.isHoldCarsWithCustomLoadsEnabled()) {
            buf.append(TAB + TAB + Bundle.getMessage("HoldCarsWithCustomLoads") + NEW_LINE);
        }
        if (track.isDisableLoadChangeEnabled()) {
            buf.append(TAB + TAB + Bundle.getMessage("DisableLoadChange") + NEW_LINE);
        }
        return buf.toString();
    }

    private String getStagingInfo(Track track) {
        if (!track.isStaging()) {
            return "";
        }

        StringBuffer buf = new StringBuffer();

        if (track.isLoadSwapEnabled() || track.isLoadEmptyEnabled()) {
            buf.append(TAB + SPACES_3 + Bundle.getMessage("OptionalLoads") + NEW_LINE);
            if (track.isLoadSwapEnabled()) {
                buf.append(TAB + TAB + Bundle.getMessage("SwapCarLoads") + NEW_LINE);
            }
            if (track.isLoadEmptyEnabled()) {
                buf.append(TAB + TAB + Bundle.getMessage("EmptyDefaultCarLoads") + NEW_LINE);
            }
        }

        if (track.isRemoveCustomLoadsEnabled() ||
                track.isAddCustomLoadsEnabled() ||
                track.isAddCustomLoadsAnySpurEnabled() ||
                track.isAddCustomLoadsAnyStagingTrackEnabled()) {
            buf.append(TAB + SPACES_3 + Bundle.getMessage("OptionalCustomLoads") + NEW_LINE);
            if (track.isRemoveCustomLoadsEnabled()) {
                buf.append(TAB + TAB + Bundle.getMessage("EmptyCarLoads") + NEW_LINE);
            }
            if (track.isAddCustomLoadsEnabled()) {
                buf.append(TAB + TAB + Bundle.getMessage("LoadCarLoads") + NEW_LINE);
            }
            if (track.isAddCustomLoadsAnySpurEnabled()) {
                buf.append(TAB + TAB + Bundle.getMessage("LoadAnyCarLoads") + NEW_LINE);
            }
            if (track.isAddCustomLoadsAnyStagingTrackEnabled()) {
                buf.append(TAB + TAB + Bundle.getMessage("LoadsStaging") + NEW_LINE);
            }
        }

        if (track.isBlockCarsEnabled()) {
            buf.append(TAB + SPACES_3 + Bundle.getMessage("OptionalBlocking") + NEW_LINE);
            buf.append(TAB + TAB + Bundle.getMessage("BlockCars") + NEW_LINE);
        }
        return buf.toString();
    }

    private String padOutString(String s, int length) {
        return TrainCommon.padAndTruncate(s, length);
    }

    private final static Logger log = LoggerFactory.getLogger(PrintLocationsFrame.class);
}
