/**
 * OperationsMenu.java
 */

package jmri.jmrit.operations;

import org.apache.log4j.Logger;
import javax.swing.*;

/**
 * Create a "Operations" menu 
 *
 * @author	Bob Jacobsen   Copyright 2003
 * @author Daniel Boudreau Copyright 2008
 * @version     $Revision$
 */
public class OperationsMenu extends JMenu {
    public OperationsMenu(String name) {
        this();
        setText(name);
    }

    public OperationsMenu() {
        super();

        setText(Bundle.getMessage("MenuOperations"));
        
        add(new jmri.jmrit.operations.setup.OperationsSetupAction(Bundle.getMessage("MenuSetup")));
        add(new jmri.jmrit.operations.locations.LocationsTableAction(Bundle.getMessage("MenuLocations")));
        add(new jmri.jmrit.operations.rollingstock.cars.CarsTableAction(Bundle.getMessage("MenuCars")));
        add(new jmri.jmrit.operations.rollingstock.engines.EnginesTableAction(Bundle.getMessage("MenuEngines")));
        add(new jmri.jmrit.operations.routes.RoutesTableAction(Bundle.getMessage("MenuRoutes")));
        add(new jmri.jmrit.operations.trains.TrainsTableAction(Bundle.getMessage("MenuTrains")));
            
    }

    static Logger log = Logger.getLogger(OperationsMenu.class.getName());
}


