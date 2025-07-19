package jmri.jmrit.operations.routes.tools;

import java.io.BufferedReader;
import java.io.File;

import jmri.InstanceManager;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.rollingstock.ImportCommon;
import jmri.jmrit.operations.routes.*;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.ThreadingUtil;
import jmri.util.swing.JmriJOptionPane;

/**
 * This routine will import Routes from a CSV file into the operations database.
 * The field order is as defined below.
 * 
 * @author Daniel Boudreau Copyright (C) 2025
 */
public class ImportRoutes extends ImportCommon {

    RouteManager routeManager = InstanceManager.getDefault(RouteManager.class);
    LocationManager locationManager = InstanceManager.getDefault(LocationManager.class);

    int routesAdded = 0;

    // each route starts with name and comment
    protected static final int FIELD_ROUTE_NAME = 0;
    protected static final int FIELD_EMPTY = 1;
    protected static final int FIELD_ROUTE_COMMENT = 2;

    // each route can have one or more route locations
    protected static final int FIELD_ROUTE_LOCATION_EMPTY = 0;
    protected static final int FIELD_ROUTE_LOCATION_NAME = 1;
    protected static final int FIELD_ROUTE_LOCATION_DIRECTION = 2;
    protected static final int FIELD_ROUTE_LOCATION_MOVES = 3;
    protected static final int FIELD_ROUTE_LOCATION_RANDOM = 4;
    protected static final int FIELD_ROUTE_LOCATION_PICKUP_ALLOWED = 5;
    protected static final int FIELD_ROUTE_LOCATION_DROP_ALLOWED = 6;
    protected static final int FIELD_ROUTE_LOCATION_LOCAL_MOVES_ALLOWED = 7;
    protected static final int FIELD_ROUTE_LOCATION_TRAVEL = 8;
    protected static final int FIELD_ROUTE_LOCATION_DEPARTURE_TIME = 9;
    protected static final int FIELD_ROUTE_LOCATION_TRAIN_LENGTH = 10;
    protected static final int FIELD_ROUTE_LOCATION_GRADE = 11;
    protected static final int FIELD_ROUTE_LOCATION_ICON_X = 12;
    protected static final int FIELD_ROUTE_LOCATION_ICON_Y = 13;
    protected static final int FIELD_ROUTE_LOCATION_COMMENT = 14;
    protected static final int FIELD_ROUTE_LOCATION_COMMENT_COLOR = 15;

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
        createStatusFrame(Bundle.getMessage("TitleImportRoutes"));

        String[] inputLine;
        boolean headerFound = false;
        Route route = null;

        while (true) {
            inputLine = readNextLine(rdr);
            if (inputLine == BREAK) {
                log.debug("Done");
                break;
            }
            if (inputLine.length < 1) {
                log.debug("Skipping blank line");
                continue;
            }
            // header?
            if (!headerFound && inputLine[FIELD_ROUTE_NAME].equals(Bundle.getMessage("Route"))) {
                headerFound = true;
                int elementNum = 0;
                for (String lineElement : inputLine) {
                    log.debug("Header {} is: {}", elementNum++, lineElement);
                }
                continue; // skip header
            }
            // add route?
            if (inputLine.length == 3) {
                log.debug("Found start of route: {}", inputLine[FIELD_ROUTE_NAME]);
                route = routeManager.getRouteByName(inputLine[FIELD_ROUTE_NAME]);
                if (route != null) {
                    log.info("Route: {} already exists, will not import!", inputLine[FIELD_ROUTE_NAME]);
                    route = null;
                } else {
                    routesAdded++;
                    route = routeManager.newRoute(inputLine[FIELD_ROUTE_NAME]);
                    log.info("Creating route: {}", inputLine[FIELD_ROUTE_NAME]);
                    route.setComment(inputLine[FIELD_ROUTE_COMMENT]);
                }
            }
            // add route location?
            if (route != null && inputLine.length == 16) {
                log.debug("Adding route location: {}", inputLine[FIELD_ROUTE_LOCATION_NAME]);
                Location location = locationManager.getLocationByName(inputLine[FIELD_ROUTE_LOCATION_NAME]);
                if (location == null) {
                    log.error("Location ({}) in route ({}) does not exist", inputLine[FIELD_ROUTE_LOCATION_NAME],
                            route.getName());
                    break;
                }
                RouteLocation rl = route.addLocation(location);
                rl.setTrainDirection(Setup.getDirectionInt(inputLine[FIELD_ROUTE_LOCATION_DIRECTION]));
                rl.setMaxCarMoves(Integer.parseInt(inputLine[FIELD_ROUTE_LOCATION_MOVES]));
                rl.setRandomControl(inputLine[FIELD_ROUTE_LOCATION_RANDOM]);
                rl.setPickUpAllowed(inputLine[FIELD_ROUTE_LOCATION_PICKUP_ALLOWED].equals(Bundle.getMessage("yes")));
                rl.setDropAllowed(inputLine[FIELD_ROUTE_LOCATION_DROP_ALLOWED].equals(Bundle.getMessage("yes")));
                rl.setLocalMovesAllowed(
                        inputLine[FIELD_ROUTE_LOCATION_LOCAL_MOVES_ALLOWED].equals(Bundle.getMessage("yes")));
                rl.setWait(Integer.parseInt(inputLine[FIELD_ROUTE_LOCATION_TRAVEL]) - Setup.getTravelTime());
                rl.setDepartureTime(inputLine[FIELD_ROUTE_LOCATION_DEPARTURE_TIME]);
                rl.setMaxTrainLength(Integer.parseInt(inputLine[FIELD_ROUTE_LOCATION_TRAIN_LENGTH]));
                rl.setGrade(Double.parseDouble(inputLine[FIELD_ROUTE_LOCATION_GRADE]));
                rl.setTrainIconX(Integer.parseInt(inputLine[FIELD_ROUTE_LOCATION_ICON_X]));
                rl.setTrainIconY(Integer.parseInt(inputLine[FIELD_ROUTE_LOCATION_ICON_Y]));
                rl.setComment(inputLine[FIELD_ROUTE_LOCATION_COMMENT]);
                rl.setCommentTextColor(inputLine[FIELD_ROUTE_LOCATION_COMMENT_COLOR]);
            }
        }

        ThreadingUtil.runOnGUI(()->{
            if (importOkay) {
                JmriJOptionPane.showMessageDialog(null, Bundle.getMessage("ImportRoutesAdded", routesAdded),
                        Bundle.getMessage("SuccessfulImport"), JmriJOptionPane.INFORMATION_MESSAGE);
            } else {
                JmriJOptionPane.showMessageDialog(null, Bundle.getMessage("ImportRoutesAdded", routesAdded),
                        Bundle.getMessage("ImportFailed"), JmriJOptionPane.ERROR_MESSAGE);
            }
        });
        fstatus.dispose();
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ImportRoutes.class);
}
