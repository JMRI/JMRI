/**
 * OperationsMenu.java
 */

package jmri.jmrit.operations;

import javax.swing.*;
import java.util.*;

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

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.JmritOperationsBundle");

        setText(rb.getString("MenuOperations"));
        
        add(new jmri.jmrit.operations.setup.OperationsSetupAction(rb.getString("MenuSetup")));
        add(new jmri.jmrit.operations.locations.LocationsTableAction(rb.getString("MenuLocations")));
        add(new jmri.jmrit.operations.rollingstock.cars.CarsTableAction(rb.getString("MenuCars")));
        add(new jmri.jmrit.operations.rollingstock.engines.EnginesTableAction(rb.getString("MenuEngines")));
        add(new jmri.jmrit.operations.routes.RoutesTableAction(rb.getString("MenuRoutes")));
        add(new jmri.jmrit.operations.trains.TrainsTableAction(rb.getString("MenuTrains")));
        //add(new JSeparator());
        //add(new jmri.jmrit.powerpanel.PowerPanelAction(rb.getString("MenuCalendar")));
            
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(OperationsMenu.class.getName());
}


