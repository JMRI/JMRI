package jmri.jmrit.operations.locations.tools;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

/**
 * Exports the location roster into a comma delimited file (CSV).
 *
 * @author Daniel Boudreau Copyright (C) 2018
 *
 */
public class ExportLocations extends XmlFile {

    TrainManager trainManager = InstanceManager.getDefault(TrainManager.class);
    RouteManager routeManager = InstanceManager.getDefault(RouteManager.class);
    LocationManager locationManager = InstanceManager.getDefault(LocationManager.class);

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
        } catch (IOException e) {
            log.error("Exception while writing the new CSV operations file, may not be complete", e);
        }
    }

    public void writeFile(String name) {
        log.debug("writeFile {}", name);
        File file = findFile(name);
        if (file == null) {
            file = new File(name);
        }

        try (CSVPrinter fileOut = new CSVPrinter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)),
                CSVFormat.DEFAULT)) {
            // create header
            fileOut.printRecord(Bundle.getMessage("Location"),
                    Bundle.getMessage("Track"),
                    Bundle.getMessage("Type"),
                    Bundle.getMessage("Length"),
                    Bundle.getMessage("Division"),
                    Bundle.getMessage("ServicedByTrains"),
                    Bundle.getMessage("RollingStock"),
                    Bundle.getMessage("ServiceOrder"),
                    Bundle.getMessage("RoadOption"),
                    Bundle.getMessage("Roads"),
                    Bundle.getMessage("LoadOption"),
                    Bundle.getMessage("Loads"),
                    Bundle.getMessage("ShipLoadOption"),
                    Bundle.getMessage("Ships"),
                    Bundle.getMessage("SetOutRestrictions"),
                    Bundle.getMessage("Restrictions"),
                    Bundle.getMessage("PickUpRestrictions"),
                    Bundle.getMessage("Restrictions"),
                    Bundle.getMessage("ScheduleName"),
                    Bundle.getMessage("ScheduleMode"),
                    Bundle.getMessage("AlternateTrack"),
                    Bundle.getMessage("PoolName"),
                    Bundle.getMessage("Minimum"),
                    Bundle.getMessage("TitleTrackBlockingOrder"),
                    Bundle.getMessage("MenuItemPlannedPickups"),
                    Bundle.getMessage("MenuItemDestinations"),
                    Bundle.getMessage("Destinations"),
                    Bundle.getMessage("SwapCarLoads"),
                    Bundle.getMessage("EmptyDefaultCarLoads"),
                    Bundle.getMessage("EmptyCarLoads"),
                    Bundle.getMessage("LoadCarLoads"),
                    Bundle.getMessage("LoadAnyCarLoads"),
                    Bundle.getMessage("LoadsStaging"),
                    Bundle.getMessage("BlockCars"),
                    Bundle.getMessage("Comment"),
                    Bundle.getMessage("CommentBoth"),
                    Bundle.getMessage("CommentPickup"),
                    Bundle.getMessage("CommentSetout"));

            List<Location> locations = locationManager.getLocationsByNameList();
            for (Location location : locations) {
                for (Track track : location.getTracksByNameList(null)) {

                    StringBuilder trainDirections = new StringBuilder();
                    String[] directions = Setup.getDirectionStrings(
                            Setup.getTrainDirection() & location.getTrainDirections() & track.getTrainDirections());
                    for (String dir : directions) {
                        if (dir != null) {
                            trainDirections.append(dir).append("; ");
                        }
                    }

                    StringBuilder rollingStockNames = new StringBuilder();
                    for (String rollingStockName : track.getTypeNames()) {
                        rollingStockNames.append(rollingStockName).append("; ");
                    }

                    StringBuilder roadNames = new StringBuilder();
                    if (!track.getRoadOption().equals(Track.ALL_ROADS)) {
                        for (String roadName : track.getRoadNames()) {
                            roadNames.append(roadName).append("; ");
                        }
                    }

                    StringBuilder loadNames = new StringBuilder();
                    if (!track.getLoadOption().equals(Track.ALL_LOADS)) {
                        for (String loadName : track.getLoadNames()) {
                            loadNames.append(loadName).append("; ");
                        }
                    }

                    StringBuilder shipNames = new StringBuilder();
                    if (!track.getShipLoadOption().equals(Track.ALL_LOADS)) {
                        for (String shipName : track.getShipLoadNames()) {
                            shipNames.append(shipName).append("; ");
                        }
                    }

                    String setOutRestriction = Bundle.getMessage("None");
                    switch (track.getDropOption()) {
                        case Track.TRAINS:
                            setOutRestriction = Bundle.getMessage("Trains");
                            break;
                        case Track.ROUTES:
                            setOutRestriction = Bundle.getMessage("Routes");
                            break;
                        case Track.EXCLUDE_TRAINS:
                            setOutRestriction = Bundle.getMessage("ExcludeTrains");
                            break;
                        case Track.EXCLUDE_ROUTES:
                            setOutRestriction = Bundle.getMessage("ExcludeRoutes");
                            break;
                        default:
                            break;
                    }

                    StringBuilder setOutRestrictions = new StringBuilder();
                    if (track.getDropOption().equals(Track.TRAINS) || track.getDropOption().equals(Track.EXCLUDE_TRAINS)) {
                        for (String id : track.getDropIds()) {
                            Train train = trainManager.getTrainById(id);
                            if (train != null) {
                                setOutRestrictions.append(train.getName()).append("; ");
                            }
                        }
                    }
                    if (track.getDropOption().equals(Track.ROUTES) || track.getDropOption().equals(Track.EXCLUDE_ROUTES)) {
                        for (String id : track.getDropIds()) {
                            Route route = routeManager.getRouteById(id);
                            if (route != null) {
                                setOutRestrictions.append(route.getName()).append("; ");
                            }
                        }
                    }

                    String pickUpRestriction = Bundle.getMessage("None");
                    switch (track.getPickupOption()) {
                        case Track.TRAINS:
                            pickUpRestriction = Bundle.getMessage("Trains");
                            break;
                        case Track.ROUTES:
                            pickUpRestriction = Bundle.getMessage("Routes");
                            break;
                        case Track.EXCLUDE_TRAINS:
                            pickUpRestriction = Bundle.getMessage("ExcludeTrains");
                            break;
                        case Track.EXCLUDE_ROUTES:
                            pickUpRestriction = Bundle.getMessage("ExcludeRoutes");
                            break;
                        default:
                            break;
                    }

                    StringBuilder pickUpRestrictions = new StringBuilder();
                    if (track.getPickupOption().equals(Track.TRAINS)
                            || track.getPickupOption().equals(Track.EXCLUDE_TRAINS)) {
                        for (String id : track.getPickupIds()) {
                            Train train = trainManager.getTrainById(id);
                            if (train != null) {
                                pickUpRestrictions.append(train.getName()).append("; ");
                            }
                        }
                    }
                    if (track.getPickupOption().equals(Track.ROUTES)
                            || track.getPickupOption().equals(Track.EXCLUDE_ROUTES)) {
                        for (String id : track.getPickupIds()) {
                            Route route = routeManager.getRouteById(id);
                            if (route != null) {
                                pickUpRestrictions.append(route.getName()).append("; ");
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

                    StringBuilder destinationNames = new StringBuilder();
                    for (String id : track.getDestinationIds()) {
                        Location destination = locationManager.getLocationById(id);
                        if (destination != null) {
                            destinationNames.append(destination.getName()).append("; ");
                        }
                    }

                    fileOut.printRecord(location.getName(),
                            track.getName(),
                            track.getTrackTypeName(),
                            track.getLength(),
                            track.getDivision(),
                            trainDirections.toString(),
                            rollingStockNames.toString(),
                            track.getServiceOrder(),
                            track.getRoadOptionString(),
                            roadNames.toString(),
                            track.getLoadOptionString(),
                            loadNames.toString(),
                            track.getShipLoadOptionString(),
                            shipNames.toString(),
                            setOutRestriction,
                            setOutRestrictions.toString(),
                            pickUpRestriction,
                            pickUpRestrictions.toString(),
                            track.getScheduleName(),
                            track.getScheduleModeName(),
                            alternateTrackName,
                            track.getPoolName(),
                            track.getMinimumLength(),
                            track.getBlockingOrder(),
                            track.getIgnoreUsedLengthPercentage(),
                            Bundle.getMessage(track.getDestinationOption().equals(Track.ALL_DESTINATIONS) ? "All" : "Include"),
                            destinationNames.toString(),
                            (track.isLoadSwapEnabled() ? Bundle.getMessage("ButtonYes") : ""),
                            (track.isLoadEmptyEnabled() ? Bundle.getMessage("ButtonYes") : ""),
                            (track.isRemoveCustomLoadsEnabled() ? Bundle.getMessage("ButtonYes") : ""),
                            (track.isAddCustomLoadsEnabled() ? Bundle.getMessage("ButtonYes") : ""),
                            (track.isAddCustomLoadsAnySpurEnabled() ? Bundle.getMessage("ButtonYes") : ""),
                            (track.isAddCustomLoadsAnyStagingTrackEnabled() ? Bundle.getMessage("ButtonYes") : ""),
                            (track.isBlockCarsEnabled() ? Bundle.getMessage("ButtonYes") : ""),
                            track.getComment(),
                            track.getCommentBothWithColor(),
                            track.getCommentPickupWithColor(),
                            track.getCommentSetoutWithColor());
                }
            }
            fileOut.flush();
            fileOut.close();
            log.info("Exported {} locations to file {}", locations.size(), defaultOperationsFilename());
            JOptionPane.showMessageDialog(null,
                    MessageFormat.format(Bundle.getMessage("ExportedLocationsToFile"), new Object[]{
                locations.size(), defaultOperationsFilename()}),
                    Bundle.getMessage("ExportComplete"),
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            log.error("Can not open export locations CSV file: {}", file.getName());
            JOptionPane.showMessageDialog(null,
                    MessageFormat.format(Bundle.getMessage("ExportedLocationsToFile"), new Object[]{
                0, defaultOperationsFilename()}),
                    Bundle.getMessage("ExportFailed"),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Operation files always use the same directory
    public static String defaultOperationsFilename() {
        return OperationsSetupXml.getFileLocation()
                + OperationsSetupXml.getOperationsDirectoryName()
                + File.separator
                + getOperationsFileName();
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
