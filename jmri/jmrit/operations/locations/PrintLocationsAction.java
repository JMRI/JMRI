// PrintLocationsAction.java

package jmri.jmrit.operations.locations;

import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.rollingstock.engines.EngineTypes;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.davidflanagan.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

import javax.swing.*;

import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;


/**
 * Action to print a summary of the Location Roster contents
 * <P>
 * This uses the older style printing, for compatibility with Java 1.1.8 in
 * Macintosh MRJ
 *
 * @author	Bob Jacobsen   Copyright (C) 2003
 * @author  Dennis Miller  Copyright (C) 2005
 * @author Daniel Boudreau Copyright (C) 2008
 * @version     $Revision: 1.15 $
 */
public class PrintLocationsAction  extends AbstractAction {
	
	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.locations.JmritOperationsLocationsBundle");
	private boolean detailed = true;
	String newLine = "\n";
	String formFeed = "\f";
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
    HardcopyWriter writer;

    public void actionPerformed(ActionEvent e) {
        // obtain a HardcopyWriter
        try {
            writer = new HardcopyWriter(mFrame, rb.getString("TitleLocationsTable"), 10, .5, .5, .5, .5, isPreview);
        } catch (HardcopyWriter.PrintCanceledException ex) {
            log.debug("Print cancelled");
            return;
        }
  
        // Loop through the Roster, printing as needed
       
        List<String> locations = manager.getLocationsByNameList();
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
        		Location location = manager.getLocationById(locations.get(i));
        		String name = location.getName();
        		StringBuffer buf = new StringBuffer(name);
        		// pad out the location name
        		for (int j=name.length(); j < LocationEditFrame.MAX_NAME_LENGTH; j++) {
        			buf.append(" ");
        		}
         		s = buf.toString() + " \t  " + Integer.toString(location.getLength()) + "\t"
         						+ Integer.toString(location.getUsedLength()) + "\t"
								+ Integer.toString(location.getNumberRS()) + "\t"
								+ "\t" + "\t"
								+ Integer.toString(location.getPickupRS()) + "\t"
								+ Integer.toString(location.getDropRS())+ newLine;
        		writer.write(s, 0, s.length());
        		
        		totalLength += location.getLength();
        		usedLength += location.getUsedLength();
        		numberRS += location.getNumberRS();
        		
        		List<String> yards = location.getTracksByNameList(Track.YARD);
        		if (yards.size()>0){
        			s = "    " + rb.getString("YardName")	+ newLine;
        			writer.write(s, 0, s.length());
         			for (int k=0; k<yards.size(); k++){
        				Track yard = location.getTrackById(yards.get(k));
        				name = yard.getName();
        	      		buf = new StringBuffer(name);
                		// pad out the track name
                		for (int j=name.length(); j < TrackEditFrame.MAX_NAME_LENGTH; j++) {
                			buf.append(" ");
                		}
                		s = getTrackString (yard, buf.toString());
                		writer.write(s, 0, s.length());
                		numberCars += yard.getNumberCars();
                		numberEngines += yard.getNumberEngines();
         			}
        		}
        		
        		List<String> sidings = location.getTracksByNameList(Track.SIDING);
        		if (sidings.size()>0){
        			s = "    " + rb.getString("SidingName")	+ newLine;
        			writer.write(s, 0, s.length());
         			for (int k=0; k<sidings.size(); k++){
        				Track siding = location.getTrackById(sidings.get(k));
        				name = siding.getName();
        				buf = new StringBuffer(name);
                		for (int j=name.length(); j < TrackEditFrame.MAX_NAME_LENGTH; j++) {
                			buf.append(" ");
                		}
                		s = getTrackString (siding, buf.toString());
                		writer.write(s, 0, s.length());
                   		numberCars += siding.getNumberCars();
                		numberEngines += siding.getNumberEngines();
         			}
        		}
        		
        		List<String> interchanges = location.getTracksByNameList(Track.INTERCHANGE);
        		if (interchanges.size()>0){
        			s = "    " + rb.getString("InterchangeName")	+ newLine;
        			writer.write(s, 0, s.length());
         			for (int k=0; k<interchanges.size(); k++){
        				Track interchange = location.getTrackById(interchanges.get(k));
        				name = interchange.getName();
        				buf = new StringBuffer(name);
                		for (int j=name.length(); j < TrackEditFrame.MAX_NAME_LENGTH; j++) {
                			buf.append(" ");
                		}
                		s = getTrackString (interchange, buf.toString());
                		writer.write(s, 0, s.length());
                		numberCars += interchange.getNumberCars();
                		numberEngines += interchange.getNumberEngines();
         			}
        		}
          		
        		List<String> stagings = location.getTracksByNameList(Track.STAGING);
        		if (stagings.size()>0){
        			s = "    " + rb.getString("StagingName")	+ newLine;
        			writer.write(s, 0, s.length());
         			for (int k=0; k<stagings.size(); k++){
        				Track staging = location.getTrackById(stagings.get(k));
        				name = staging.getName();
        				buf = new StringBuffer(name);
                		for (int j=name.length(); j < TrackEditFrame.MAX_NAME_LENGTH; j++) {
                			buf.append(" ");
                		}
                		s = getTrackString (staging, buf.toString());
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
        			Location location = manager.getLocationById(locations.get(j));
        			List<String> sidings = location.getTracksByNameList(Track.SIDING);
        			for (int k=0; k<sidings.size(); k++){
        				Track siding = location.getTrackById(sidings.get(k));
        				if (siding.getScheduleName().equals(schedule.getName())){
        					String name = schedule.getName();
        					// pad out schedule name
        					StringBuffer buf = new StringBuffer(name);
        					for (int n=name.length(); n<MAX_NAME_LENGTH; n++){
        						buf.append(" ");
        					}
        					s = buf.toString() +" "+ location.getName()+ " - " + siding.getName();
        					String status = siding.checkScheduleValid(location);
        					if (!status.equals("")){
        						buf = new StringBuffer(s);
        						for (int m=s.length(); m<63; m++){
            						buf.append(" ");
        						}
        						s = buf.toString();
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
        	// user requesting detailed report?
        	if (detailed){
        		s = formFeed + newLine + rb.getString("DetailedReport") + newLine;
        		writer.write(s, 0, s.length());
        		String tab = "   ";
            	for (int i=0; i<locations.size(); i++){
            		Location location = manager.getLocationById(locations.get(i));
            		String name = location.getName();
            		// services train direction
            		int dir = location.getTrainDirections();
            		s = newLine + name + getDirection(dir);     		
            		writer.write(s, 0, s.length());
            		// services car and engine types
            		s = getLocationTypes(location);
            		writer.write(s, 0, s.length());
            		
               		List<String> yards = location.getTracksByNameList(Track.YARD);
            		if (yards.size()>0){
            			s = tab + rb.getString("YardName") + newLine;
            			writer.write(s, 0, s.length());
            			printTrackInfo(location, yards);
            		}
            		
              		List<String> sidings = location.getTracksByNameList(Track.SIDING);
            		if (sidings.size()>0){
            			s = tab + rb.getString("SidingName") + newLine;
            			writer.write(s, 0, s.length());
              			printTrackInfo(location, sidings);
            		}
            		
               		List<String> interchanges = location.getTracksByNameList(Track.INTERCHANGE);
            		if (interchanges.size()>0){
            			s = tab + rb.getString("InterchangeName") + newLine;
            			writer.write(s, 0, s.length());
             			printTrackInfo(location, interchanges);
            		}
            		
               		List<String> stagings = location.getTracksByNameList(Track.STAGING);
            		if (stagings.size()>0){
            			s = tab + rb.getString("StagingName") + newLine;
            			writer.write(s, 0, s.length());
             			printTrackInfo(location, stagings);
            		}
            	}
        	}
        	
        	// and force completion of the printing
        	writer.close();
        } catch (IOException we) {
        	log.error("Error printing PrintLocationAction: " + we);
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
    
    private String getDirection(int dir){
   		if ((Setup.getTrainDirection() & dir) == 0){
   			return " " + rb.getString("LocalOnly") + newLine;
   		}
   		String 	direction = " " + rb.getString("ServicedByTrain")+ " ";
		if ((Setup.getTrainDirection() & dir & Location.NORTH)>0)
			direction = direction + rb.getString("North") + " ";
		if ((Setup.getTrainDirection() & dir & Location.SOUTH)>0)
			direction = direction + rb.getString("South") + " ";
		if ((Setup.getTrainDirection() & dir & Location.EAST)>0)
			direction = direction + rb.getString("East") + " ";
		if ((Setup.getTrainDirection() & dir & Location.WEST)>0)
			direction = direction + rb.getString("West") + " ";
		direction = direction + newLine;
		return direction;
    }
    
	private void printTrackInfo(Location location, List<String> tracks){
		for (int k=0; k<tracks.size(); k++){
			Track track = location.getTrackById(tracks.get(k));
			String name = track.getName();
			try {
				String s = "\t" +name + getDirection(track.getTrainDirections());
				writer.write(s, 0, s.length());
				s = getTrackTypes(location, track);
				writer.write(s, 0, s.length());
				s = getTrackRoads(track);
				writer.write(s, 0, s.length());
			} catch (IOException we) {
				log.error("Error printing PrintLocationAction: " + we);
			}
		}
	}
	
	private int characters = 60;
	private String getLocationTypes(Location location){
		StringBuffer buf = new StringBuffer("\t\t" + rb.getString("TypesServiced") + newLine + "\t\t");
		int charCount = 0;
		int typeCount = 0;
		String[] cTypes = CarTypes.instance().getNames();	
		for (int i =0; i<cTypes.length; i++){
			if(location.acceptsTypeName(cTypes[i])){
				buf.append(cTypes[i] + ", ");
				typeCount++;
				charCount += cTypes[i].length() +2;
				if(charCount > characters){
					buf.append(newLine + "\t\t");
					charCount = 0;
				}
			}
		}
		String[] eTypes = EngineTypes.instance().getNames();
		for (int i =0; i<eTypes.length; i++){
			if (location.acceptsTypeName(eTypes[i])){
				buf.append(eTypes[i] + ", ");
				typeCount++;
				charCount += eTypes[i].length() +2;
				if(charCount > characters){
					buf.append(newLine + "\t\t");
					charCount = 0;
				}
			}
		}
		// does this location accept all types?
		if (typeCount == cTypes.length + eTypes.length )
			buf = new StringBuffer("\t\t" + rb.getString("LocationAcceptsAllTypes"));
		buf.append(newLine);
		return buf.toString();
	}
	
	private String getTrackTypes(Location location, Track track){
		StringBuffer buf = new StringBuffer("\t\t" + rb.getString("TypesServicedTrack") + newLine + "\t\t");
		int charCount = 0;
		int typeCount = 0;
		String[] cTypes = CarTypes.instance().getNames();	
		for (int i =0; i<cTypes.length; i++){
			if(location.acceptsTypeName(cTypes[i]) && track.acceptsTypeName(cTypes[i])){
				buf.append(cTypes[i] + ", ");
				typeCount++;
				charCount += cTypes[i].length() +2;
				if(charCount > characters){
					buf.append(newLine + "\t\t");
					charCount = 0;
				}
			}
		}
		String[] eTypes = EngineTypes.instance().getNames();
		for (int i=0; i<eTypes.length; i++){
			if (track.acceptsTypeName(eTypes[i])){
				buf.append(eTypes[i] + ", ");
				typeCount++;
				charCount += eTypes[i].length() +2;
				if( charCount > characters){
					buf.append(newLine + "\t\t");
					charCount = 0;
				}
			}
		}
		// does this track accept all types?
		if (typeCount == cTypes.length + eTypes.length )
			buf = new StringBuffer("\t\t" + rb.getString("TrackAcceptsAllTypes"));
		buf.append(newLine);
		return buf.toString();
	}
	
	private String getTrackRoads(Track track){
		if (track.getRoadOption().equals(Track.ALLROADS)){
			return "\t\t" + rb.getString("AcceptsAllRoads") + newLine;
		}
		StringBuffer buf = new StringBuffer("\t\t" + rb.getString("RoadsServicedTrack") + newLine + "\t\t");
		int charCount = 0;
		String[] roads = CarRoads.instance().getNames();	
		for (int i=0; i<roads.length; i++){
			if (track.acceptsRoadName(roads[i])){
				buf.append(roads[i] +", ");
				charCount += roads[i].length() +2;
				if( charCount > characters){
					buf.append(newLine + "\t\t");
					charCount = 0;
				}
			}
		}
		buf.append(newLine);
		return buf.toString();
	}

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PrintLocationsAction.class.getName());
}
