package jmri.jmrit.operations.locations.tools;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.TrainSwitchLists;

/**
 * Swing action to preview or print a switch list for a location.
 *
 * @author Daniel Boudreau Copyright (C) 2013
 * 
 */
public class PrintSwitchListAction extends AbstractAction {

    public PrintSwitchListAction(String actionName, Location location, boolean isPreview) {
        super(actionName);
        this.location = location;
        this.isPreview = isPreview;
        // The switch list must be accessed from the Trains window if running in consolidated mode
        setEnabled(Setup.isSwitchListRealTime());
    }

    Location location;
    boolean isPreview;

    @Override
    public void actionPerformed(ActionEvent e) {
        TrainSwitchLists ts = new TrainSwitchLists();
        ts.buildSwitchList(location);
        ts.printSwitchList(location, isPreview);
    }
}


