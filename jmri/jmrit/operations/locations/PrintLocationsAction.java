// PrintLocationsAction.java

package jmri.jmrit.operations.locations;

import jmri.util.davidflanagan.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

import javax.swing.*;

import java.text.MessageFormat;
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
 * @version     $Revision: 1.8 $
 */
public class PrintLocationsAction  extends AbstractAction {
	
	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.locations.JmritOperationsLocationsBundle");
	String newLine = "\n";
	LocationManager manager = LocationManager.instance();
	public static final int MAX_NAME_LENGTH = 25;

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
            writer = new HardcopyWriter(mFrame, rb.getString("TitleLocationsTable"), 10, .5, .5, .5, .5, isPreview);
        } catch (HardcopyWriter.PrintCanceledException ex) {
            log.debug("Print cancelled");
            return;
        }
  
        // Loop through the Roster, printing as needed
       
        List locations = manager.getLocationsByNameList();
        int totalLength = 0;
        int usedLength = 0;
        int numberRS = 0;
        int numberCars = 0;
        int numberEngines = 0;
        
        try {
        	String s = rb.getString("Location") + "\t\t\t "
					+ rb.getString("Length") + " " + rb.getString("Used")
					+ "\t" + rb.getString("RS") 
					+ "\t" + rb.getString("Cars")
					+ "\t" + rb.getString("Engines").substring(0, 7)
					+ "\t" + rb.getString("Pickup")
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
								+ Integer.toString(location.getNumberRS()) + "\t"
								+ "\t" + "\t"
								+ Integer.toString(location.getPickupRS()) + "\t"
								+ Integer.toString(location.getDropRS())+ newLine;
        		writer.write(s, 0, s.length());
        		
        		totalLength += location.getLength();
        		usedLength += location.getUsedLength();
        		numberRS += location.getNumberRS();
        		
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
                		s = getTrackString (yard, name);
                		writer.write(s, 0, s.length());
                		numberCars += yard.getNumberCars();
                		numberEngines += yard.getNumberEngines();
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
                		s = getTrackString (siding, name);
                		writer.write(s, 0, s.length());
                   		numberCars += siding.getNumberCars();
                		numberEngines += siding.getNumberEngines();
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
                		s = getTrackString (interchange, name);
                		writer.write(s, 0, s.length());
                		numberCars += interchange.getNumberCars();
                		numberEngines += interchange.getNumberEngines();
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
                		s = getTrackString (staging, name);
                		writer.write(s, 0, s.length());
                		numberCars += staging.getNumberCars();
                		numberEngines += staging.getNumberEngines();
         			}
        		}
        		writer.write(newLine, 0, newLine.length());
        	}
        	
        	// summary
        	s = MessageFormat.format(rb.getString("TotalLengthMsg"),
					new Object[] { Integer.toString(totalLength),
							Integer.toString(usedLength),
							Integer.toString(usedLength * 100 / totalLength) })
					+ newLine;
        	writer.write(s, 0, s.length());
           	s = MessageFormat.format(rb.getString("TotalRollingMsg"),
					new Object[] { Integer.toString(numberRS),
							Integer.toString(numberCars),
							Integer.toString(numberEngines) })
					+ newLine;
           	writer.write(s, 0, s.length());
        	// are there trains in route, then some cars and engines not counted!
        	if (numberRS != numberCars+numberEngines){
        		s = MessageFormat.format(rb.getString("NoteRSMsg"),
    					new Object[] { Integer.toString(numberRS-(numberCars+numberEngines)) })
    					+ newLine;
        		writer.write(s, 0, s.length());
        	}
        	
         	// print schedules
        	writer.write(newLine, 0, newLine.length());
        	s = rb.getString("Schedules") + "\t\t  " +rb.getString("Location") +"\t"+ rb.getString("SidingName") + newLine;
        	writer.write(s, 0, s.length());
        	ScheduleManager sm = ScheduleManager.instance();
        	List<String> schedules = sm.getSchedulesByNameList();
        	for (int i=0; i<schedules.size(); i++){
        		Schedule schedule = sm.getScheduleById(schedules.get(i));
        		for (int j=0; j<locations.size(); j++){
        			Location location = manager.getLocationById((String)locations.get(j));
        			List sidings = location.getTracksByNameList(Track.SIDING);
        			for (int k=0; k<sidings.size(); k++){
        				Track siding = location.getTrackById((String)sidings.get(k));
        				if (siding.getScheduleName().equals(schedule.getName())){
        					String name = schedule.getName();
        					// pad out schedule name
        					for (int n=name.length(); n<MAX_NAME_LENGTH; n++){
        						name = name +" ";
        					}
        					s = name +" "+ location.getName()+ " - " + siding.getName();
        					String status = siding.checkScheduleValid(location);
        					if (!status.equals("")){
        						for (int m=s.length(); m<63; m++){
            						s = s + " ";
        						}
        						if (s.length()>63)
        							s = s.substring(0, 63);
        						s = s + "\t" + status;
        					}
        					s = s + newLine;
        					writer.write(s, 0, s.length());
        				}
        			}
        		}
        	}
        	
        	// and force completion of the printing
        	writer.close();
        } catch (IOException we) {
        	log.error("Error printing ConsistRosterEntry: " + e);
        }
    }
    
    private String getTrackString (Track track, String name){
   		String s = "\t" + name + " "
		+ Integer.toString(track.getLength()) + "\t"
		+ Integer.toString(track.getUsedLength()) + "\t"
		+ Integer.toString(track.getNumberRS())	+ "\t"
		+ Integer.toString(track.getNumberCars()) + "\t"
		+ Integer.toString(track.getNumberEngines()) + "\t"
		+ Integer.toString(track.getPickupRS())	+ "\t"
		+ Integer.toString(track.getDropRS())
		+ newLine;
   		return s;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PrintLocationsAction.class.getName());
}
