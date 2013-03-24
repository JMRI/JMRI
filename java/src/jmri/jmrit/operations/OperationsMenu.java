/**
 * OperationsMenu.java
 */

package jmri.jmrit.operations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.*;

/**
 * Create a "Operations" menu
 * 
 * @author Bob Jacobsen Copyright 2003
 * @author Daniel Boudreau Copyright 2008
 * @version $Revision$
 */
public class OperationsMenu extends JMenu {
	
	public OperationsMenu(String name) {
		this();
		setText(name);
	}

	public OperationsMenu() {
		super();

		setText(Bundle.getMessage("MenuOperations"));

		add(new jmri.jmrit.operations.setup.OperationsSetupAction());
		add(new jmri.jmrit.operations.locations.LocationsTableAction());
		add(new jmri.jmrit.operations.rollingstock.cars.CarsTableAction());
		add(new jmri.jmrit.operations.rollingstock.engines.EnginesTableAction());
		add(new jmri.jmrit.operations.routes.RoutesTableAction());
		add(new jmri.jmrit.operations.trains.TrainsTableAction());

	}

	static Logger log = LoggerFactory.getLogger(OperationsMenu.class.getName());
}
