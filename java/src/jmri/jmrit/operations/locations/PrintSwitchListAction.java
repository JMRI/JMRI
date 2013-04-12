// PrintSwitchListAction.java

package jmri.jmrit.operations.locations;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import jmri.jmrit.operations.trains.TrainSwitchLists;

/**
 * Swing action to preview or print a switch list for a location.
 * 
 * @author Daniel Boudreau Copyright (C) 2013
 * @version $Revision: 22219 $
 */
public class PrintSwitchListAction extends AbstractAction {

	public PrintSwitchListAction(String actionName, Location location, boolean isPreview) {
		super(actionName);
		this.location = location;
		this.isPreview = isPreview;
	}

	Location location;
	boolean isPreview;


	public void actionPerformed(ActionEvent e) {
		TrainSwitchLists ts = new TrainSwitchLists();
		ts.buildSwitchList(location);
		ts.printSwitchList(location, isPreview);
	}
}

/* @(#)ModifyLocationsAction.java */
