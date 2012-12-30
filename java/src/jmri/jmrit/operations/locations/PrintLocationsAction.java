// PrintLocationsAction.java

package jmri.jmrit.operations.locations;

import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarLoads;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.rollingstock.engines.EngineTypes;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteManager;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.davidflanagan.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

import javax.swing.*;

import java.text.MessageFormat;
import java.util.ArrayList;
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
 * @author Daniel Boudreau Copyright (C) 2008, 2011, 2012
 * @version     $Revision$
 */
public class PrintLocationsAction  extends AbstractAction {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.locations.JmritOperationsLocationsBundle");
	static final String newLine = "\n";		// NOI18N
	static final String formFeed = "\f";	// NOI18N
	static final String tab = "\t";			// NOI18N
	static final String space = "   ";
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
	LocationPrintOptionFrame lpof = null;

	public void actionPerformed(ActionEvent e) {
		if (lpof == null)
			lpof = new LocationPrintOptionFrame(this);
		else
			lpof.setVisible(true);
		lpof.initComponents();
	}

	public void printLocations() {
		// obtain a HardcopyWriter
		try {
			writer = new HardcopyWriter(mFrame, rb.getString("TitleLocationsTable"), 10, .5, .5, .5, .5, isPreview);
		} catch (HardcopyWriter.PrintCanceledException ex) {
			log.debug("Print cancelled");
			return;
		}
		try {
			// print locations?
			if (printLocations.isSelected())
				printLocationsSelected();
			// print schedules?
			if (printSchedules.isSelected())
				printSchedulesSelected();
			// print detailed report?
			if (printDetails.isSelected())
				printDetailsSelected();
			// print analysis?
			if (printAnalysis.isSelected())
				printAnalysisSelected();
			// force completion of the printing
			writer.close();
		} catch (IOException we) {
			log.error("Error printing PrintLocationAction: " + we);
		}
	}
	
	// Loop through the Roster, printing as needed
	private void printLocationsSelected() throws IOException{
		List<String> locations = manager.getLocationsByNameList();
		int totalLength = 0;
		int usedLength = 0;
		int numberRS = 0;
		int numberCars = 0;
		int numberEngines = 0;
		String s = rb.getString("Location") + tab + tab +tab
		+ rb.getString("Length") + " " + rb.getString("Used")
		+ tab + rb.getString("RS") 
		+ tab + rb.getString("Cars")
		+ tab + rb.getString("Engines")
		+ tab + rb.getString("Pickup")
		+ " " + rb.getString("Drop") + newLine;
		writer.write(s);
		for (int i=0; i<locations.size(); i++){
			Location location = manager.getLocationById(locations.get(i));
			String name = location.getName();
			StringBuffer buf = new StringBuffer(name);
			// pad out the location name
			for (int j=name.length(); j < LocationEditFrame.MAX_NAME_LENGTH; j++) {
				buf.append(" ");
			}
			s = buf.toString() + tab + Integer.toString(location.getLength()) + tab
			+ Integer.toString(location.getUsedLength()) + tab
			+ Integer.toString(location.getNumberRS()) + tab
			+ tab + tab
			+ Integer.toString(location.getPickupRS()) + tab
			+ Integer.toString(location.getDropRS())+ newLine;
			writer.write(s);

			totalLength += location.getLength();
			usedLength += location.getUsedLength();
			numberRS += location.getNumberRS();

			List<String> yards = location.getTrackIdsByNameList(Track.YARD);
			if (yards.size()>0){
				s = "    " + rb.getString("YardName")	+ newLine;
				writer.write(s);
				for (int k=0; k<yards.size(); k++){
					Track yard = location.getTrackById(yards.get(k));
					name = yard.getName();
					buf = new StringBuffer(name);
					// pad out the track name
					for (int j=name.length(); j < TrackEditFrame.MAX_NAME_LENGTH; j++) {
						buf.append(" ");
					}
					s = getTrackString (yard, buf.toString());
					writer.write(s);
					numberCars += yard.getNumberCars();
					numberEngines += yard.getNumberEngines();
				}
			}

			List<String> sidings = location.getTrackIdsByNameList(Track.SIDING);
			if (sidings.size()>0){
				s = "    " + rb.getString("SidingName")	+ newLine;
				writer.write(s);
				for (int k=0; k<sidings.size(); k++){
					Track siding = location.getTrackById(sidings.get(k));
					name = siding.getName();
					buf = new StringBuffer(name);
					for (int j=name.length(); j < TrackEditFrame.MAX_NAME_LENGTH; j++) {
						buf.append(" ");
					}
					s = getTrackString (siding, buf.toString());
					writer.write(s);
					numberCars += siding.getNumberCars();
					numberEngines += siding.getNumberEngines();
				}
			}

			List<String> interchanges = location.getTrackIdsByNameList(Track.INTERCHANGE);
			if (interchanges.size()>0){
				s = "    " + rb.getString("InterchangeName")	+ newLine;
				writer.write(s);
				for (int k=0; k<interchanges.size(); k++){
					Track interchange = location.getTrackById(interchanges.get(k));
					name = interchange.getName();
					buf = new StringBuffer(name);
					for (int j=name.length(); j < TrackEditFrame.MAX_NAME_LENGTH; j++) {
						buf.append(" ");
					}
					s = getTrackString (interchange, buf.toString());
					writer.write(s);
					numberCars += interchange.getNumberCars();
					numberEngines += interchange.getNumberEngines();
				}
			}

			List<String> stagings = location.getTrackIdsByNameList(Track.STAGING);
			if (stagings.size()>0){
				s = "    " + rb.getString("StagingName")	+ newLine;
				writer.write(s);
				for (int k=0; k<stagings.size(); k++){
					Track staging = location.getTrackById(stagings.get(k));
					name = staging.getName();
					buf = new StringBuffer(name);
					for (int j=name.length(); j < TrackEditFrame.MAX_NAME_LENGTH; j++) {
						buf.append(" ");
					}
					s = getTrackString (staging, buf.toString());
					writer.write(s);
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
		writer.write(s);
		s = MessageFormat.format(rb.getString("TotalRollingMsg"),
				new Object[] { Integer.toString(numberRS),
			Integer.toString(numberCars),
			Integer.toString(numberEngines) })
			+ newLine;
		writer.write(s);
		// are there trains in route, then some cars and engines not counted!
		if (numberRS != numberCars+numberEngines){
			s = MessageFormat.format(rb.getString("NoteRSMsg"),
					new Object[] { Integer.toString(numberRS-(numberCars+numberEngines)) })
					+ newLine;
			writer.write(s);
		}
	}
	
	private void printSchedulesSelected() throws IOException{
		List<String> locations = manager.getLocationsByNameList();
		writer.write(newLine);
		String s = rb.getString("Schedules") + tab + tab +rb.getString("Location") + " - " + rb.getString("SidingName") + newLine;
		writer.write(s);
		ScheduleManager sm = ScheduleManager.instance();
		List<String> schedules = sm.getSchedulesByNameList();
		for (int i=0; i<schedules.size(); i++){
			Schedule schedule = sm.getScheduleById(schedules.get(i));
			for (int j=0; j<locations.size(); j++){
				Location location = manager.getLocationById(locations.get(j));
				List<String> sidings = location.getTrackIdsByNameList(Track.SIDING);
				for (int k=0; k<sidings.size(); k++){
					Track siding = location.getTrackById(sidings.get(k));
					if (siding.getScheduleId().equals(schedule.getId())){
						String name = schedule.getName();
						// pad out schedule name
						StringBuffer buf = new StringBuffer(name);
						for (int n=name.length(); n<MAX_NAME_LENGTH; n++){
							buf.append(" ");
						}
						s = buf.toString() +" "+ location.getName()+ " - " + siding.getName();
						String status = siding.checkScheduleValid();
						if (!status.equals("")){
							buf = new StringBuffer(s);
							for (int m=s.length(); m<63; m++){
								buf.append(" ");
							}
							s = buf.toString();
							if (s.length()>63)
								s = s.substring(0, 63);
							s = s + tab + status;
						}
						s = s + newLine;
						writer.write(s);
					}
				}
			}
		}
	}
	
	private void printDetailsSelected() throws IOException{
		List<String> locations = manager.getLocationsByNameList();
		String s = formFeed + newLine + rb.getString("DetailedReport") + newLine;
		writer.write(s);
		for (int i=0; i<locations.size(); i++){
			Location location = manager.getLocationById(locations.get(i));
			String name = location.getName();
			// services train direction
			int dir = location.getTrainDirections();
			s = newLine + name + getDirection(dir);     		
			writer.write(s);
			// services car and engine types
			s = getLocationTypes(location);
			writer.write(s);

			List<String> yards = location.getTrackIdsByNameList(Track.YARD);
			if (yards.size()>0){
				s = space + rb.getString("YardName") + newLine;
				writer.write(s);
				printTrackInfo(location, yards);
			}

			List<String> sidings = location.getTrackIdsByNameList(Track.SIDING);
			if (sidings.size()>0){
				s = space + rb.getString("SidingName") + newLine;
				writer.write(s);
				printTrackInfo(location, sidings);
			}

			List<String> interchanges = location.getTrackIdsByNameList(Track.INTERCHANGE);
			if (interchanges.size()>0){
				s = space + rb.getString("InterchangeName") + newLine;
				writer.write(s);
				printTrackInfo(location, interchanges);
			}

			List<String> stagings = location.getTrackIdsByNameList(Track.STAGING);
			if (stagings.size()>0){
				s = space + rb.getString("StagingName") + newLine;
				writer.write(s);
				printTrackInfo(location, stagings);
			}
		}
	}
	
	private final boolean showStaging = false;
	private void printAnalysisSelected() throws IOException{
		CarManager carManager = CarManager.instance();
		List<String> locations = manager.getLocationsByNameList();
		List<String> cars = carManager.getByLocationList();
		String[] carTypes = CarTypes.instance().getNames();
		
		String s = formFeed + newLine + rb.getString("TrackAnalysis") + newLine;
		writer.write(s);
		
		// print the car type being analyzed
		for (int i=0; i<carTypes.length; i++){
			String type = carTypes[i];
			// get the total length for a given car type
			int numberOfCars = 0;
			int totalTrackLength = 0;
			for (int j=0; j<cars.size(); j++){
				Car car = carManager.getById(cars.get(j));
				if (car.getType().equals(type) && car.getLocation() != null){
					numberOfCars++;
					totalTrackLength = totalTrackLength + Integer.parseInt(car.getLength()) + Car.COUPLER;
				}
			}
			writer.write(MessageFormat.format(rb.getString("NumberTypeLength"),new Object[]{numberOfCars, type, totalTrackLength}) + newLine);
			// don't bother reporting when the number of cars for a given type is zero
			if (numberOfCars > 0){
				// spurs
				writer.write(space+MessageFormat.format(rb.getString("SpurTrackThatAccept"),new Object[]{type})+ newLine);
				int trackLength = getTrackLengthAcceptType(locations, type, Track.SIDING);
				if (trackLength > 0)
					writer.write(space+MessageFormat.format(rb.getString("TotalLengthSpur"),new Object[]{type, trackLength, 100*totalTrackLength/trackLength}) + newLine);
				else
					writer.write(space+ rb.getString("None") + newLine);
				// yards
				writer.write(space+MessageFormat.format(rb.getString("YardTrackThatAccept"),new Object[]{type})+ newLine);
				trackLength = getTrackLengthAcceptType(locations, type, Track.YARD);
				if (trackLength > 0)
					writer.write(space+MessageFormat.format(rb.getString("TotalLengthYard"),new Object[]{type, trackLength, 100*totalTrackLength/trackLength}) + newLine);
				else
					writer.write(space+ rb.getString("None") + newLine);
				// interchanges
				writer.write(space+MessageFormat.format(rb.getString("InterchangesThatAccept"),new Object[]{type})+ newLine);
				trackLength = getTrackLengthAcceptType(locations, type, Track.INTERCHANGE);
				if (trackLength > 0)
					writer.write(space+MessageFormat.format(rb.getString("TotalLengthInterchange"),new Object[]{type, trackLength, 100*totalTrackLength/trackLength}) + newLine);
				else
					writer.write(space+ rb.getString("None") + newLine);
				// staging
				if (showStaging){
					writer.write(space+MessageFormat.format(rb.getString("StageTrackThatAccept"),new Object[]{type})+ newLine);
					trackLength = getTrackLengthAcceptType(locations, type, Track.STAGING);
					if (trackLength > 0)
						writer.write(space+MessageFormat.format(rb.getString("TotalLengthStage"),new Object[]{type, trackLength, 100*totalTrackLength/trackLength}) + newLine);
					else
						writer.write(space+ rb.getString("None") + newLine);
				}
			}
		}
	}

	private int getTrackLengthAcceptType(List<String> locations, String carType, String trackType) throws IOException{
		int trackLength = 0;
		for (int j=0; j<locations.size(); j++){
			Location location = manager.getLocationById(locations.get(j));
			// get a list of spur tracks at this location
			List<String> tracks = location.getTrackIdsByNameList(trackType);				
			for (int k=0; k<tracks.size(); k++){
				Track track = location.getTrackById(tracks.get(k));
				if (track.acceptsTypeName(carType)){
					trackLength = trackLength + track.getLength();
					writer.write(space+space+MessageFormat.format(rb.getString("LocationTrackLength"),new Object[]{location.getName(), track.getName(), track.getLength()})+ newLine);
				}
			}
		}
		return trackLength;
	}

	private String getTrackString (Track track, String name){
		String s = tab + name + " "
		+ Integer.toString(track.getLength()) + tab
		+ Integer.toString(track.getUsedLength()) + tab
		+ Integer.toString(track.getNumberRS())	+ tab
		+ Integer.toString(track.getNumberCars()) + tab
		+ Integer.toString(track.getNumberEngines()) + tab
		+ Integer.toString(track.getPickupRS())	+ tab
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
				String s = tab +name + getDirection(track.getTrainDirections());
				writer.write(s);
				writer.write(getTrackTypes(location, track));
				writer.write(getTrackRoads(track));
				writer.write(getTrackLoads(track));
				writer.write(getCarOrder(track));
				writer.write(getSetOutTrains(track));
				writer.write(getPickUpTrains(track));
				writer.write(getSchedule(track));
			} catch (IOException we) {
				log.error("Error printing PrintLocationAction: " + we);
			}
		}
	}

	private int characters = 70;
	private String getLocationTypes(Location location){
		StringBuffer buf = new StringBuffer(tab + tab + rb.getString("TypesServiced") + newLine + tab + tab);
		int charCount = 0;
		int typeCount = 0;
		String[] cTypes = CarTypes.instance().getNames();	
		for (int i =0; i<cTypes.length; i++){
			if(location.acceptsTypeName(cTypes[i])){
				typeCount++;
				charCount += cTypes[i].length() +2;
				if(charCount > characters){
					buf.append(newLine + tab + tab);
					charCount = cTypes[i].length() +2;
				}
				buf.append(cTypes[i] + ", ");
			}
		}
		String[] eTypes = EngineTypes.instance().getNames();
		for (int i =0; i<eTypes.length; i++){
			if (location.acceptsTypeName(eTypes[i])){
				typeCount++;
				charCount += eTypes[i].length() +2;
				if(charCount > characters){
					buf.append(newLine + tab + tab);
					charCount = eTypes[i].length() +2;
				}
				buf.append(eTypes[i] + ", ");
			}
		}
		if (buf.length() > 2) buf.setLength(buf.length()-2);	// remove trailing separators
		// does this location accept all types?
		if (typeCount == cTypes.length + eTypes.length )
			buf = new StringBuffer(tab + tab + rb.getString("LocationAcceptsAllTypes"));
		buf.append(newLine);
		return buf.toString();
	}

	private String getTrackTypes(Location location, Track track){
		StringBuffer buf = new StringBuffer(tab + tab + rb.getString("TypesServicedTrack") + newLine + tab + tab);
		int charCount = 0;
		int typeCount = 0;
		String[] cTypes = CarTypes.instance().getNames();	
		for (int i =0; i<cTypes.length; i++){
			if(track.acceptsTypeName(cTypes[i])){
				typeCount++;
				charCount += cTypes[i].length() +2;
				if(charCount > characters){
					buf.append(newLine + tab + tab);
					charCount = cTypes[i].length() +2;
				}
				buf.append(cTypes[i] + ", ");
			}
		}
		String[] eTypes = EngineTypes.instance().getNames();
		for (int i=0; i<eTypes.length; i++){
			if (track.acceptsTypeName(eTypes[i])){
				typeCount++;
				charCount += eTypes[i].length() +2;
				if( charCount > characters){
					buf.append(newLine + tab + tab);
					charCount = eTypes[i].length() +2;
				}
				buf.append(eTypes[i] + ", ");
			}
		}
		if (buf.length() > 2) buf.setLength(buf.length()-2);	// remove trailing separators
		// does this track accept all types?
		if (typeCount == cTypes.length + eTypes.length )
			buf = new StringBuffer(tab + tab + rb.getString("TrackAcceptsAllTypes"));
		buf.append(newLine);
		return buf.toString();
	}

	private String getTrackRoads(Track track){
		if (track.getRoadOption().equals(Track.ALLROADS)){
			return tab + tab + rb.getString("AcceptsAllRoads") + newLine;
		}
		StringBuffer buf = new StringBuffer(tab + tab + rb.getString("RoadsServicedTrack") + newLine + tab + tab);
		int charCount = 0;
		String[] roads = CarRoads.instance().getNames();	
		for (int i=0; i<roads.length; i++){
			if (track.acceptsRoadName(roads[i])){
				charCount += roads[i].length() +2;
				if( charCount > characters){
					buf.append(newLine + tab + tab);
					charCount = roads[i].length() +2;
				}
				buf.append(roads[i] +", ");
			}
		}
		if (buf.length() > 2) buf.setLength(buf.length()-2);	// remove trailing separators
		buf.append(newLine);
		return buf.toString();
	}

	private String getTrackLoads(Track track){
		if (track.getLoadOption().equals(Track.ALLLOADS)){
			return tab + tab + rb.getString("AcceptsAllLoads") + newLine;
		}
		StringBuffer buf = new StringBuffer(tab + tab + rb.getString("LoadsServicedTrack") + newLine + tab + tab);
		int charCount = 0;
		String[] cTypes = CarTypes.instance().getNames();
		List<String> serviceLoads = new ArrayList<String>();
		for (int i =0; i<cTypes.length; i++){
			if(track.acceptsTypeName(cTypes[i])){
				List<String> loads = CarLoads.instance().getNames(cTypes[i]);
				for (int j=0; j<loads.size(); j++){
					if (track.acceptsLoadName(loads.get(j))){
						if (!serviceLoads.contains(loads.get(j))){
							serviceLoads.add(loads.get(j));
							charCount += loads.get(j).length() +2;
							if( charCount > characters){
								buf.append(newLine + tab + tab);
								charCount = loads.get(j).length() +2;
							}
							buf.append(loads.get(j) +", ");
						}
					}
				}
			}
		}
		if (buf.length() > 2) buf.setLength(buf.length()-2);	// remove trailing separators
		buf.append(newLine);
		return buf.toString();
	}
	
	private String getCarOrder(Track track){
		// only yards and interchanges have the car order option
		if (track.getLocType().equals(Track.SIDING) 
				|| track.getLocType().equals(Track.STAGING)
				|| track.getServiceOrder().equals(Track.NORMAL))
			return "";
		if (track.getServiceOrder().equals(Track.FIFO))
			return tab + tab +rb.getString("TrackPickUpOrderFIFO")+ newLine;
		return tab + tab +rb.getString("TrackPickUpOrderLIFO")+ newLine;
	}
	
	private String getSetOutTrains(Track track){
		if (track.getDropOption().equals(Track.ANY))
			return tab + tab + rb.getString("SetOutAllTrains") + newLine;
		StringBuffer buf;
		int charCount = 0;
		String[] ids = track.getDropIds();
		if (track.getDropOption().equals(Track.TRAINS)){
			buf = new StringBuffer(tab + tab + rb.getString("TrainsSetOutTrack") + newLine + tab + tab);			
			for (int i=0; i<ids.length; i++){
				Train train = TrainManager.instance().getTrainById(ids[i]);
				if (train == null){
					log.info("Could not find a train for id: "+ids[i]+" track ("+track.getName()+")");
					continue;
				}
				charCount += train.getName().length() +2;
				if( charCount > characters){
					buf.append(newLine + tab + tab);
					charCount = train.getName().length() +2;
				}
				buf.append(train.getName() +", ");
			}
		} else {
			buf = new StringBuffer(tab + tab + rb.getString("RoutesSetOutTrack") + newLine + tab + tab);
			for (int i=0; i<ids.length; i++){
				Route route = RouteManager.instance().getRouteById(ids[i]);
				if (route == null){
					log.info("Could not find a route for id: "+ids[i]+" track ("+track.getName()+")");
					continue;
				}
				charCount += route.getName().length() +2;
				if( charCount > characters){
					buf.append(newLine + tab + tab);
					charCount = route.getName().length() +2;
				}
				buf.append(route.getName() +", ");
			}
		}
		if (buf.length() > 2) buf.setLength(buf.length()-2);	// remove trailing separators
		buf.append(newLine);
		return buf.toString();
	}
	
	private String getPickUpTrains(Track track){
		if (track.getPickupOption().equals(Track.ANY))
			return tab + tab + rb.getString("PickUpAllTrains") + newLine;
		StringBuffer buf;
		int charCount = 0;
		String[] ids = track.getPickupIds();
		if (track.getPickupOption().equals(Track.TRAINS)){
			buf = new StringBuffer(tab + tab + rb.getString("TrainsPickUpTrack") + newLine + tab + tab);			
			for (int i=0; i<ids.length; i++){
				Train train = TrainManager.instance().getTrainById(ids[i]);
				if (train == null){
					log.info("Could not find a train for id: "+ids[i]+" track ("+track.getName()+")");
					continue;
				}
				charCount += train.getName().length() +2;
				if( charCount > characters){
					buf.append(newLine + tab + tab);
					charCount = train.getName().length() +2;
				}
				buf.append(train.getName() +", ");
			}
		} else {
			buf = new StringBuffer(tab + tab + rb.getString("RoutesPickUpTrack") + newLine + tab + tab);
			for (int i=0; i<ids.length; i++){
				Route route = RouteManager.instance().getRouteById(ids[i]);
				if (route == null){
					log.info("Could not find a route for id: "+ids[i]+" track ("+track.getName()+")");
					continue;
				}
				charCount += route.getName().length() +2;
				if( charCount > characters){
					buf.append(newLine + tab + tab);
					charCount = route.getName().length() +2;
				}
				buf.append(route.getName() +", ");
			}
		}
		if (buf.length() > 2) buf.setLength(buf.length()-2);	// remove trailing separators
		buf.append(newLine);
		return buf.toString();
	}
	
	private String getSchedule(Track track){
		// only spurs have schedules
		if (!track.getLocType().equals(Track.SIDING) || track.getSchedule() == null)
			return "";
		StringBuffer buf = new StringBuffer(tab + tab + MessageFormat.format(rb.getString("TrackScheduleName"),new Object[]{track.getScheduleName()}) + newLine);
		if (track.getAlternativeTrack() != null)
			buf.append(tab + tab + MessageFormat.format(rb.getString("AlternateTrackName"),new Object[]{track.getAlternativeTrack().getName()}) + newLine);
		if (track.getReservationFactor() != 100)
			buf.append(tab + tab + MessageFormat.format(rb.getString("PercentageStaging"),new Object[]{track.getReservationFactor()}) + newLine);
		return buf.toString();
	}

	JCheckBox printLocations = new JCheckBox(rb.getString("PrintLocations"));
	JCheckBox printSchedules = new JCheckBox(rb.getString("PrintSchedules"));
	JCheckBox printDetails = new JCheckBox(rb.getString("PrintDetails"));
	JCheckBox printAnalysis = new JCheckBox(rb.getString("PrintAnalysis"));

	JButton okayButton = new JButton(rb.getString("ButtonOkay"));

	public class LocationPrintOptionFrame extends OperationsFrame{
		PrintLocationsAction pla;

		public LocationPrintOptionFrame(PrintLocationsAction pla){
			super();
			this.pla = pla;
			// create panel
			JPanel pPanel = new JPanel();
			pPanel.setLayout(new BoxLayout(pPanel,BoxLayout.Y_AXIS));
			pPanel.setBorder(BorderFactory.createTitledBorder(rb.getString("PrintOptions")));
			pPanel.add(printLocations);
			pPanel.add(printSchedules);
			pPanel.add(printDetails); 
			pPanel.add(printAnalysis);
			// set defaults
			printLocations.setSelected(true);
			printSchedules.setSelected(true);
			printDetails.setSelected(true);
			printAnalysis.setSelected(true);

			//add tool tips

			JPanel pButtons = new JPanel();  
			pButtons.setLayout(new GridBagLayout());
			pButtons.add(okayButton);
			addButtonAction(okayButton);

			getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
			getContentPane().add(pPanel);
			getContentPane().add(pButtons);
			setPreferredSize(null);
			pack();
			setVisible(true);
		}

		public void initComponents() {

		}

		public void buttonActionPerformed(java.awt.event.ActionEvent ae) { 		
			setVisible(false);
			pla.printLocations();  		
		}
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PrintLocationsAction.class.getName());
}
