// PrintLocationsAction.java

package jmri.jmrit.operations.locations;

import jmri.util.davidflanagan.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

import javax.swing.*;

import java.util.List;
import java.util.ResourceBundle;


/**
 * Action to print a summary of the Roster contents
 * <P>
 * This uses the older style printing, for compatibility with Java 1.1.8 in
 * Macintosh MRJ
 *
 * @author	Bob Jacobsen   Copyright (C) 2003
 * @author  Dennis Miller  Copyright (C) 2005
 * @author Daniel Boudreau Copyright (C) 2008
 * @version     $Revision: 1.2 $
 */
public class PrintLocationsAction  extends AbstractAction {
	
	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.locations.JmritOperationsLocationsBundle");
	
	LocationManager manager = LocationManager.instance();

    public PrintLocationsAction(String actionName, Frame frame, boolean preview, Component pWho) {
        super(actionName);
        mFrame = frame;
        isPreview = preview;
        panel = (LocationsTableFrame)pWho;
    }

    /**
     * Frame hosting the printing
     */
    Frame mFrame;
    /**
     * Variable to set whether this is to be printed or previewed
     */
    boolean isPreview;
    LocationsTableFrame panel;
    

    public void actionPerformed(ActionEvent e) {

        // obtain a HardcopyWriter to do this
        HardcopyWriter writer = null;
        try {
            writer = new HardcopyWriter(mFrame, "Locations", 10, .5, .5, .5, .5, isPreview);
        } catch (HardcopyWriter.PrintCanceledException ex) {
            log.debug("Print cancelled");
            return;
        }
  
        // Loop through the Roster, printing as needed
        String newLine = "\n";
        List locations = manager.getLocationsByNameList();
        try {
        	String s = rb.getString("Location") + "\t\t\t "
					+ rb.getString("Length") + " " + rb.getString("Used")
					+ "\t" + rb.getString("RS") + "\t" + rb.getString("Pickup")
					+ "\t" + rb.getString("Drop") + newLine;
        	writer.write(s, 0, s.length());
        	for (int i=0; i<locations.size(); i++){
        		Location location = manager.getLocationById((String)locations.get(i));
        		String name = location.getName();
        		// pad out the location name
        		for (int j=name.length(); j < LocationsEditFrame.MAX_NAME_LENGTH; j++) {
        			name += " ";
        		}
         		s = name + " \t  " + Integer.toString(location.getLength()) + "\t"
         						+ Integer.toString(location.getUsedLength()) + "\t"
								+ Integer.toString(location.getNumberCars()) + "\t"
								+ Integer.toString(location.getPickupRS()) + "\t"
								+ Integer.toString(location.getDropCars())+ newLine;
        		writer.write(s, 0, s.length());
        		
        		List yards = location.getTracksByNameList(Track.YARD);
        		if (yards.size()>0){
        			s = "    " + rb.getString("YardName")	+ newLine;
        			writer.write(s, 0, s.length());
         			for (int k=0; k<yards.size(); k++){
        				Track yard = location.getTrackById((String)yards.get(k));
        				name = yard.getName();
                		for (int j=name.length(); j < YardEditFrame.MAX_NAME_LENGTH; j++) {
                			name += " ";
                		}
                		s = "\t" + name + " "
								+ Integer.toString(yard.getLength()) + "\t"
								+ Integer.toString(yard.getUsedLength()) + "\t"
								+ Integer.toString(yard.getNumberCars()) + "\t"
								+ Integer.toString(yard.getPickupRS()) + "\t"
								+ Integer.toString(yard.getDropCars())
								+ newLine;
                		writer.write(s, 0, s.length());
         			}
        		}
        		
        		List sidings = location.getTracksByNameList(Track.SIDING);
        		if (sidings.size()>0){
        			s = "    " + rb.getString("SidingName")	+ newLine;
        			writer.write(s, 0, s.length());
         			for (int k=0; k<sidings.size(); k++){
        				Track siding = location.getTrackById((String)sidings.get(k));
        				name = siding.getName();
                		for (int j=name.length(); j < SidingEditFrame.MAX_NAME_LENGTH; j++) {
                			name += " ";
                		}
						s = "\t" + name + " "
								+ Integer.toString(siding.getLength()) + "\t"
								+ Integer.toString(siding.getUsedLength()) + "\t"
								+ Integer.toString(siding.getNumberCars()) + "\t"
								+ Integer.toString(siding.getPickupRS()) + "\t"
								+ Integer.toString(siding.getDropCars())
								+ newLine;
                		writer.write(s, 0, s.length());
         			}
        		}
        		
        		List interchanges = location.getTracksByNameList(Track.INTERCHANGE);
        		if (interchanges.size()>0){
        			s = "    " + rb.getString("InterchangeName")	+ newLine;
        			writer.write(s, 0, s.length());
         			for (int k=0; k<interchanges.size(); k++){
        				Track interchange = location.getTrackById((String)interchanges.get(k));
        				name = interchange.getName();
                		for (int j=name.length(); j < InterchangeEditFrame.MAX_NAME_LENGTH; j++) {
                			name += " ";
                		}
                		s = "\t" + name + " "
								+ Integer.toString(interchange.getLength())	+ "\t"
								+ Integer.toString(interchange.getUsedLength()) + "\t"
								+ Integer.toString(interchange.getNumberCars())	+ "\t"
								+ Integer.toString(interchange.getPickupRS())	+ "\t"
								+ Integer.toString(interchange.getDropCars())
								+ newLine;
                		writer.write(s, 0, s.length());
         			}
        		}
          		
        		List stagings = location.getTracksByNameList(Track.STAGING);
        		if (stagings.size()>0){
        			s = "    " + rb.getString("StagingName")	+ newLine;
        			writer.write(s, 0, s.length());
         			for (int k=0; k<stagings.size(); k++){
        				Track staging = location.getTrackById((String)stagings.get(k));
        				name = staging.getName();
                		for (int j=name.length(); j < StagingEditFrame.MAX_NAME_LENGTH; j++) {
                			name += " ";
                		}
                		s = "\t" + name + " "
								+ Integer.toString(staging.getLength()) + "\t"
								+ Integer.toString(staging.getUsedLength()) + "\t"
								+ Integer.toString(staging.getNumberCars())	+ "\t"
								+ Integer.toString(staging.getPickupRS())	+ "\t"
								+ Integer.toString(staging.getDropCars())
								+ newLine;
                		writer.write(s, 0, s.length());
         			}
        		}
        		writer.write(newLine, 0, newLine.length());
        	}
        	// and force completion of the printing
        	writer.close();
        } catch (IOException we) {
        	log.error("Error printing ConsistRosterEntry: " + e);
        }
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PrintLocationsAction.class.getName());
}
