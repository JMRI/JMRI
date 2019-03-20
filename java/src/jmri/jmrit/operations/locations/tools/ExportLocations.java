package jmri.jmrit.operations.locations.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.List;
import javax.swing.JOptionPane;
import jmri.InstanceManager;
import jmri.jmrit.XmlFile;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteManager;
import jmri.jmrit.operations.setup.OperationsSetupXml;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exports the location roster into a comma delimitated file (CSV).
 *
 * @author Daniel Boudreau Copyright (C) 2018
 *
 */
public class ExportLocations extends XmlFile {

    static final String ESC = "\""; // escape character NOI18N
    private String del = ","; // delimiter

    TrainManager trainManager = InstanceManager.getDefault(TrainManager.class);
    RouteManager routeManager = InstanceManager.getDefault(RouteManager.class);
    LocationManager locationManager = InstanceManager.getDefault(LocationManager.class);

    public void setDeliminter(String delimiter) {
        del = delimiter;
    }

    public void writeOperationsLocationFile() {
        makeBackupFile(defaultOperationsFilename());
        try {
            if (!checkFile(defaultOperationsFilename())) {
                // The file does not exist, create it before writing
                java.io.File file = new java.io.File(defaultOperationsFilename());
                java.io.File parentDir = file.getParentFile();
                if (!parentDir.exists()) {
                    if (!parentDir.mkdir()) {
                        log.error("Directory wasn't created");
                    }
                }
                if (file.createNewFile()) {
                    log.debug("File created");
                }
            }
            writeFile(defaultOperationsFilename());
        } catch (Exception e) {
            log.error("Exception while writing the new CSV operations file, may not be complete: " + e);
        }
    }

    public void writeFile(String name) {
        log.debug("writeFile {}", name);
        // This is taken in large part from "Java and XML" page 368
        File file = findFile(name);
        if (file == null) {
            file = new File(name);
        }

        PrintWriter fileOut = null;

        try {
            fileOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8")), // NOI18N
                    true); // NOI18N
        } catch (IOException e) {
            log.error("Can not open export locations CSV file: {}", file.getName());
            JOptionPane.showMessageDialog(null,
                    MessageFormat.format(Bundle.getMessage("ExportedLocationsToFile"), new Object[]{
                            0, defaultOperationsFilename()}),
                    Bundle.getMessage("ExportFailed"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // create header
        String header = Bundle.getMessage("Location") +
                del +
                Bundle.getMessage("Track") +
                del +
                Bundle.getMessage("Type") +
                del +
                Bundle.getMessage("Length") +
                del +
                Bundle.getMessage("ServicedByTrains") +
                del +
                Bundle.getMessage("RollingStock") +
                del +
                Bundle.getMessage("ServiceOrder") +
                del +
                Bundle.getMessage("RoadOption") +
                del +
                Bundle.getMessage("Roads") +
                del +
                Bundle.getMessage("LoadOption") +
                del +
                Bundle.getMessage("Loads") +
                del +
                Bundle.getMessage("ShipLoadOption") +
                del +
                Bundle.getMessage("Ships") +
                del +
                Bundle.getMessage("SetOutRestrictions") +
                del +
                Bundle.getMessage("Restrictions") +
                del +
                Bundle.getMessage("PickUpRestrictions") +
                del +
                Bundle.getMessage("Restrictions") +
                del +
                Bundle.getMessage("ScheduleName") +
                del +
                Bundle.getMessage("ScheduleMode") +
                del +
                Bundle.getMessage("AlternateTrack") +
                del +
                Bundle.getMessage("PoolName") +
                del +
                Bundle.getMessage("Minimum") +
                del +
                Bundle.getMessage("TitleTrackBlockingOrder") +
                del +
                Bundle.getMessage("MenuItemPlannedPickups") +
                del +
                Bundle.getMessage("MenuItemDestinations") +
                del +
                Bundle.getMessage("Destinations") +
                del +
                Bundle.getMessage("SwapCarLoads") +
                del +
                Bundle.getMessage("EmptyDefaultCarLoads") +
                del +
                Bundle.getMessage("EmptyCarLoads") +
                del +
                Bundle.getMessage("LoadCarLoads") +
                del +
                Bundle.getMessage("LoadAnyCarLoads") +
                del +
                Bundle.getMessage("LoadsStaging") +
                del +
                Bundle.getMessage("BlockCars") +
                del +
                Bundle.getMessage("Comment") +
                del +
                Bundle.getMessage("CommentBoth") +
                del +
                Bundle.getMessage("CommentPickup") +
                del +
                Bundle.getMessage("CommentSetout");

        fileOut.println(header);

        List<Location> locations = locationManager.getLocationsByNameList();
        for (Location location : locations) {
            for (Track track : location.getTrackByNameList(null)) {

                StringBuffer trainDirections = new StringBuffer();
                String[] directions = Setup.getDirectionStrings(
                        Setup.getTrainDirection() & location.getTrainDirections() & track.getTrainDirections());
                for (String dir : directions) {
                    if (dir != null) {
                        trainDirections.append(dir + "; ");
                    }
                }

                StringBuffer rollingStockNames = new StringBuffer();
                for (String rollingStockName : track.getTypeNames()) {
                    rollingStockNames.append(rollingStockName + "; ");
                }

                StringBuffer roadNames = new StringBuffer();
                if (!track.getRoadOption().equals(Track.ALL_ROADS)) {
                    for (String roadName : track.getRoadNames()) {
                        roadNames.append(roadName + "; ");
                    }
                }

                StringBuffer loadNames = new StringBuffer();
                if (!track.getLoadOption().equals(Track.ALL_LOADS)) {
                    for (String loadName : track.getLoadNames()) {
                        loadNames.append(loadName + "; ");
                    }
                }

                StringBuffer shipNames = new StringBuffer();
                if (!track.getShipLoadOption().equals(Track.ALL_LOADS)) {
                    for (String shipName : track.getShipLoadNames()) {
                        shipNames.append(shipName + "; ");
                    }
                }

                String setOutRestriction = Bundle.getMessage("None");
                if (track.getDropOption().equals(Track.TRAINS)) {
                    setOutRestriction = Bundle.getMessage("Trains");
                } else if (track.getDropOption().equals(Track.ROUTES)) {
                    setOutRestriction = Bundle.getMessage("Routes");
                } else if (track.getDropOption().equals(Track.EXCLUDE_TRAINS)) {
                    setOutRestriction = Bundle.getMessage("ExcludeTrains");
                } else if (track.getDropOption().equals(Track.EXCLUDE_ROUTES)) {
                    setOutRestriction = Bundle.getMessage("ExcludeRoutes");
                }

                StringBuffer setOutRestrictions = new StringBuffer();
                if (track.getDropOption().equals(Track.TRAINS) || track.getDropOption().equals(Track.EXCLUDE_TRAINS)) {
                    for (String id : track.getDropIds()) {
                        Train train = trainManager.getTrainById(id);
                        if (train != null) {
                            setOutRestrictions.append(train.getName() + "; ");
                        }
                    }
                }
                if (track.getDropOption().equals(Track.ROUTES) || track.getDropOption().equals(Track.EXCLUDE_ROUTES)) {
                    for (String id : track.getDropIds()) {
                        Route route = routeManager.getRouteById(id);
                        if (route != null) {
                            setOutRestrictions.append(route.getName() + "; ");
                        }
                    }
                }

                String pickUpRestriction = Bundle.getMessage("None");
                if (track.getPickupOption().equals(Track.TRAINS)) {
                    pickUpRestriction = Bundle.getMessage("Trains");
                } else if (track.getPickupOption().equals(Track.ROUTES)) {
                    pickUpRestriction = Bundle.getMessage("Routes");
                } else if (track.getPickupOption().equals(Track.EXCLUDE_TRAINS)) {
                    pickUpRestriction = Bundle.getMessage("ExcludeTrains");
                } else if (track.getPickupOption().equals(Track.EXCLUDE_ROUTES)) {
                    pickUpRestriction = Bundle.getMessage("ExcludeRoutes");
                }

                StringBuffer pickUpRestrictions = new StringBuffer();
                if (track.getPickupOption().equals(Track.TRAINS) ||
                        track.getPickupOption().equals(Track.EXCLUDE_TRAINS)) {
                    for (String id : track.getPickupIds()) {
                        Train train = trainManager.getTrainById(id);
                        if (train != null) {
                            pickUpRestrictions.append(train.getName() + "; ");
                        }
                    }
                }
                if (track.getPickupOption().equals(Track.ROUTES) ||
                        track.getPickupOption().equals(Track.EXCLUDE_ROUTES)) {
                    for (String id : track.getPickupIds()) {
                        Route route = routeManager.getRouteById(id);
                        if (route != null) {
                            pickUpRestrictions.append(route.getName() + "; ");
                        }
                    }
                }

                String alternateTrackName = "";
                if (track.getAlternateTrack() != null) {
                    alternateTrackName = track.getAlternateTrack().getName();
                }
                if (track.isAlternate()) {
                    alternateTrackName = Bundle.getMessage("ButtonYes");
                }

                StringBuffer destinationNames = new StringBuffer();
                for (String id : track.getDestinationIds()) {
                    Location destination = locationManager.getLocationById(id);
                    if (destination != null) {
                        destinationNames.append(destination.getName() + "; ");
                    }
                }

                String line = ESC +
                        location.getName() +
                        ESC +
                        del +
                        ESC +
                        track.getName() +
                        ESC +
                        del +
                        track.getTrackTypeName() +
                        del +
                        track.getLength() +
                        del +
                        trainDirections.toString() +
                        del +
                        ESC +
                        rollingStockNames.toString() +
                        ESC +
                        del +
                        track.getServiceOrder() +
                        del +
                        track.getRoadOptionString() +
                        del +
                        ESC +
                        roadNames.toString() +
                        ESC +
                        del +
                        track.getLoadOptionString() +
                        del +
                        ESC +
                        loadNames.toString() +
                        ESC +
                        del +
                        track.getShipLoadOptionString() +
                        del +
                        ESC +
                        shipNames.toString() +
                        ESC +
                        del +
                        setOutRestriction +
                        del +
                        ESC +
                        setOutRestrictions.toString() +
                        ESC +
                        del +
                        pickUpRestriction +
                        del +
                        ESC +
                        pickUpRestrictions.toString() +
                        ESC +
                        del +
                        ESC +
                        track.getScheduleName() +
                        ESC +
                        del +
                        (track.getScheduleMode() == Track.MATCH ? Bundle.getMessage("Match")
                                : Bundle.getMessage("Sequential")) +
                        del +
                        ESC +
                        alternateTrackName +
                        ESC +
                        del +
                        ESC +
                        track.getPoolName() +
                        ESC +
                        del +
                        track.getMinimumLength() +
                        del +
                        track.getBlockingOrder() +
                        del +
                        track.getIgnoreUsedLengthPercentage() +
                        del +
                        (track.getDestinationOption().equals(Track.ALL_DESTINATIONS) ? Bundle.getMessage("All")
                                : Bundle.getMessage("Include")) +
                        del +
                        ESC +
                        destinationNames.toString() +
                        ESC +
                        del +
                        (track.isLoadSwapEnabled() ? Bundle.getMessage("ButtonYes") : "") +
                        del +
                        (track.isLoadEmptyEnabled() ? Bundle.getMessage("ButtonYes") : "") +
                        del +
                        (track.isRemoveCustomLoadsEnabled() ? Bundle.getMessage("ButtonYes") : "") +
                        del +
                        (track.isAddCustomLoadsEnabled() ? Bundle.getMessage("ButtonYes") : "") +
                        del +
                        (track.isAddCustomLoadsAnySpurEnabled() ? Bundle.getMessage("ButtonYes") : "") +
                        del +
                        (track.isAddCustomLoadsAnyStagingTrackEnabled() ? Bundle.getMessage("ButtonYes") : "") +
                        del +
                        (track.isBlockCarsEnabled() ? Bundle.getMessage("ButtonYes") : "") +
                        del +
                        ESC +
                        track.getComment() +
                        ESC +
                        del +
                        ESC +
                        track.getCommentBoth() +
                        ESC +
                        del +
                        ESC +
                        track.getCommentPickup() +
                        ESC +
                        del +
                        ESC +
                        track.getCommentSetout() +
                        ESC;

                fileOut.println(line);
            }
        }
        fileOut.flush();
        fileOut.close();
        log.info("Exported " + locations.size() + " locations to file " + defaultOperationsFilename());
        JOptionPane.showMessageDialog(null,
                MessageFormat.format(Bundle.getMessage("ExportedLocationsToFile"), new Object[]{
                        locations.size(), defaultOperationsFilename()}),
                Bundle.getMessage("ExportComplete"),
                JOptionPane.INFORMATION_MESSAGE);
    }

    // Operation files always use the same directory
    public static String defaultOperationsFilename() {
        return OperationsSetupXml.getFileLocation() +
                OperationsSetupXml.getOperationsDirectoryName() +
                File.separator +
                getOperationsFileName();
    }

    public static void setOperationsFileName(String name) {
        operationsFileName = name;
    }

    public static String getOperationsFileName() {
        return operationsFileName;
    }

    private static String operationsFileName = "ExportOperationsLocationRoster.csv"; // NOI18N

    private final static Logger log = LoggerFactory.getLogger(ExportLocations.class);

}
