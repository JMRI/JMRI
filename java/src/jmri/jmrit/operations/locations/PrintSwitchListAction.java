// PrintSwitchListAction.java
package jmri.jmrit.operations.locations;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.TrainSwitchLists;

/**
 * Swing action to preview or print a switch list for a location.
 *
 * @author Daniel Boudreau Copyright (C) 2013
 * @version $Revision: 22219 $
 */
public class PrintSwitchListAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = -686196539645588273L;

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

/* @(#)ModifyLocationsAction.java */
