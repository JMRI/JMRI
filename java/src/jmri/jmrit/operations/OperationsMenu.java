/**
 * OperationsMenu.java
 */

package jmri.jmrit.operations;

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

        setText(Bundle.getString("MenuOperations"));
        
        add(new jmri.jmrit.operations.setup.OperationsSetupAction(Bundle.getString("MenuSetup")));
        add(new jmri.jmrit.operations.locations.LocationsTableAction(Bundle.getString("MenuLocations")));
        add(new jmri.jmrit.operations.rollingstock.cars.CarsTableAction(Bundle.getString("MenuCars")));
        add(new jmri.jmrit.operations.rollingstock.engines.EnginesTableAction(Bundle.getString("MenuEngines")));
        add(new jmri.jmrit.operations.routes.RoutesTableAction(Bundle.getString("MenuRoutes")));
        add(new jmri.jmrit.operations.trains.TrainsTableAction(Bundle.getString("MenuTrains")));
            
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(OperationsMenu.class.getName());
}


