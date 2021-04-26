package jmri.jmrit.operations.routes.tools;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteManager;
import jmri.jmrit.operations.setup.Control;
import jmri.util.davidflanagan.HardcopyWriter;

/**
 * Action to print all of the routes used in operations.
 *
 * @author Daniel Boudreau Copyright (C) 2012
 */
public class PrintRoutesAction extends PrintRouteAction {

    private static final char FORM_FEED = '\f';
    private final static Logger log = LoggerFactory.getLogger(PrintRoutesAction.class);

    public PrintRoutesAction(boolean preview) {
        super(preview, null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        log.debug("Print all routes");
        // obtain a HardcopyWriter to do this
        HardcopyWriter writer = null;
        try {
            writer = new HardcopyWriter(new Frame(), Bundle.getMessage("TitleRoutesTable"), Control.reportFontSize, .5, .5, .5, .5,
                    _isPreview);
        } catch (HardcopyWriter.PrintCanceledException ex) {
            log.debug("Print cancelled");
            return;
        }
        try {
            writer.write(" "); // prevents exception when using Preview and no routes
            List<Route> routes = InstanceManager.getDefault(RouteManager.class).getRoutesByNameList();
            for (int i = 0; i < routes.size(); i++) {
                Route route = routes.get(i);
                writer.write(route.getName() + NEW_LINE);
                printRoute(writer, route);
                if (i != routes.size() - 1) {
                    writer.write(FORM_FEED);
                }
            }
        } catch (IOException e1) {
            log.error("Exception in print routes");
        }
        // and force completion of the printing
        writer.close();
    }
}
