/**
 * OperationsMenu.java
 */
package jmri.jmrit.operations;

import javax.swing.JMenu;

/**
 * Create a "Operations" menu
 *
 * @author Bob Jacobsen Copyright 2003
 * @author Daniel Boudreau Copyright 2008
 */
public class OperationsMenu extends JMenu {

    public OperationsMenu(String name) {
        this();
        setText(name);
    }

    public OperationsMenu() {
        super();

        setText(Bundle.getMessage("MenuOperations"));

        add(new jmri.jmrit.operations.setup.gui.OperationsSettingsAction());
        add(new jmri.jmrit.operations.locations.gui.LocationsTableAction());
        add(new jmri.jmrit.operations.rollingstock.cars.gui.CarsTableAction());
        add(new jmri.jmrit.operations.rollingstock.engines.gui.EnginesTableAction());
        add(new jmri.jmrit.operations.routes.gui.RoutesTableAction());
        add(new jmri.jmrit.operations.trains.gui.TrainsTableAction());

    }

//    private final static Logger log = LoggerFactory.getLogger(OperationsMenu.class);
}
