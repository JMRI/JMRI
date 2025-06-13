package jmri.jmrit.operations.routes.tools;

import java.awt.Frame;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.operations.routes.*;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.trainbuilder.TrainCommon;
import jmri.util.davidflanagan.HardcopyWriter;

/**
 * Prints a summary of a route or all routes.
 * <p>
 * This uses the older style printing, for compatibility with Java 1.1.8 in
 * Macintosh MRJ
 *
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Dennis Miller Copyright (C) 2005
 * @author Daniel Boudreau Copyright (C) 2009, 2012, 2023
 */
public class PrintRoutes {

    static final String NEW_LINE = "\n"; // NOI18N
    static final String TAB = "\t"; // NOI18N
    static final String SPACE = " ";
    private static final char FORM_FEED = '\f';

    private static final int MAX_NAME_LENGTH = Control.max_len_string_location_name;

    boolean _isPreview;

    /**
     * Prints or previews a summary of all routes
     * 
     * @param isPreview true if print preview
     */
    public PrintRoutes(boolean isPreview) {
        _isPreview = isPreview;
        printRoutes();
    }

    /**
     * Prints or previews a summary of a route
     * 
     * @param isPreview true if print preview
     * @param route     The route to be printed
     */
    public PrintRoutes(boolean isPreview, Route route) {
        _isPreview = isPreview;
        printRoute(route);
    }

    private void printRoutes() {
        // obtain a HardcopyWriter to do this
        try (HardcopyWriter writer = new HardcopyWriter(new Frame(), Bundle.getMessage("TitleRoutesTable"),
                Control.reportFontSize, .5, .5, .5, .5, _isPreview)) {

            writer.write(SPACE); // prevents exception when using Preview and no routes
            List<Route> routes = InstanceManager.getDefault(RouteManager.class).getRoutesByNameList();
            for (int i = 0; i < routes.size(); i++) {
                Route route = routes.get(i);
                writer.write(route.getName() + NEW_LINE);
                printRoute(writer, route);
                if (i != routes.size() - 1) {
                    writer.write(FORM_FEED);
                }
            }
        } catch (HardcopyWriter.PrintCanceledException ex) {
            log.debug("Print canceled");
        } catch (IOException e1) {
            log.error("Exception in print routes: {}", e1.getLocalizedMessage());
        }
    }

    private void printRoute(Route route) {
        if (route == null) {
            return;
        }
        // obtain a HardcopyWriter to do this
        try (HardcopyWriter writer = new HardcopyWriter(new Frame(), Bundle.getMessage("TitleRoute", route.getName()),
                Control.reportFontSize, .5, .5, .5, .5, _isPreview)) {

            printRoute(writer, route);
        } catch (HardcopyWriter.PrintCanceledException ex) {
            log.debug("Print canceled");
        } catch (IOException e1) {
            log.error("Exception in print routes: {}", e1.getLocalizedMessage());
        }
    }

    private void printRoute(HardcopyWriter writer, Route route) throws IOException {
        writer.write(route.getComment() + NEW_LINE);
        if (!route.getComment().isBlank()) {
            writer.write(NEW_LINE);
        }

        writer.write(getPrintHeader1());

        List<RouteLocation> routeList = route.getLocationsBySequenceList();
        for (RouteLocation rl : routeList) {
            String s = padAndTruncate(rl.getName(), MAX_NAME_LENGTH) +
                    rl.getTrainDirectionString() +
                    TAB +
                    padAndTruncate(Integer.toString(rl.getMaxCarMoves()), 4) +
                    padAndTruncate(rl.isPickUpAllowed() ? Bundle.getMessage("yes") : Bundle.getMessage("no"), 6) +
                    padAndTruncate(rl.isDropAllowed() ? Bundle.getMessage("yes") : Bundle.getMessage("no"), 6) +
                    (rl.isLocalMovesAllowed() ? Bundle.getMessage("yes") : Bundle.getMessage("no")) +
                    TAB +
                    (rl.getWait() + Setup.getTravelTime()) +
                    TAB +
                    rl.getMaxTrainLength() +
                    TAB +
                    rl.getGrade() +
                    TAB +
                    padAndTruncate(Integer.toString(rl.getTrainIconX()), 5) +
                    rl.getTrainIconY() +
                    NEW_LINE;
            writer.write(s);
        }

        writer.write(getPrintHeader2());

        for (RouteLocation rl : routeList) {
            String s = padAndTruncate(rl.getName(),
                    MAX_NAME_LENGTH) + rl.getDepartureTime() + TAB + rl.getComment() + NEW_LINE;
            writer.write(s);
        }
    }

    private String getPrintHeader1() {
        String s = Bundle.getMessage("Location") +
                TAB +
                "    " +
                Bundle.getMessage("Direction") +
                SPACE +
                Bundle.getMessage("MaxMoves") +
                SPACE +
                Bundle.getMessage("Pull?") +
                SPACE +
                Bundle.getMessage("Drop?") +
                SPACE +
                Bundle.getMessage("Local?") +
                SPACE +
                Bundle.getMessage("Travel") +
                TAB +
                Bundle.getMessage("Length") +
                TAB +
                Bundle.getMessage("Grade") +
                TAB +
                Bundle.getMessage("X") +
                "    " +
                Bundle.getMessage("Y") +
                NEW_LINE;
        return s;
    }

    private String getPrintHeader2() {
        String s = NEW_LINE +
                Bundle.getMessage("Location") +
                TAB +
                Bundle.getMessage("DepartTime") +
                TAB +
                Bundle.getMessage("Comment") +
                NEW_LINE;
        return s;
    }

    private String padAndTruncate(String string, int fieldSize) {
        return TrainCommon.padAndTruncate(string, fieldSize);
    }

    private final static Logger log = LoggerFactory.getLogger(PrintRoutes.class);
}
