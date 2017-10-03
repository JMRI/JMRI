package jmri.jmrit.operations.routes;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import jmri.InstanceManager;
import jmri.jmrit.operations.setup.Control;
import jmri.util.davidflanagan.HardcopyWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to print all of the routes used in operations.
 *
 * @author Daniel Boudreau Copyright (C) 2012
 */
public class PrintRoutesAction extends PrintRouteAction {

    private static final char FORM_FEED = '\f';
    private final static Logger log = LoggerFactory.getLogger(PrintRoutesAction.class);

    public PrintRoutesAction(String actionName, boolean preview) {
        super(actionName, preview, null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        log.debug("Print all routes");
        // obtain a HardcopyWriter to do this
        HardcopyWriter writer = null;
        try {
            writer = new HardcopyWriter(mFrame, MessageFormat.format(
                    Bundle.getMessage("TitleRoutesTable"), new Object[]{}), Control.reportFontSize, .5, .5, .5, .5,
                    isPreview);
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
