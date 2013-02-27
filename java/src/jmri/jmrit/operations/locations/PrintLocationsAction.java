// PrintLocationsAction.java

package jmri.jmrit.operations.locations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.rollingstock.engines.EngineTypes;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteManager;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.davidflanagan.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

import javax.swing.*;

import java.text.MessageFormat;
import java.util.List;

/**
 * Action to print a summary of the Location Roster contents
 * <P>
 * This uses the older style printing, for compatibility with Java 1.1.8 in Macintosh MRJ
 * 
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Dennis Miller Copyright (C) 2005
 * @author Daniel Boudreau Copyright (C) 2008, 2011, 2012
 * @version $Revision$
 */
public class PrintLocationsAction extends AbstractAction {

	static final String NEW_LINE = "\n"; // NOI18N
	static final String FORM_FEED = "\f"; // NOI18N
	static final String TAB = "\t"; // NOI18N
	static final String SPACE = "   ";

	static final int MAX_NAME_LENGTH = Control.max_len_string_location_name;

	LocationManager manager = LocationManager.instance();

	public PrintLocationsAction(String actionName, boolean preview) {
		super(actionName);
		isPreview = preview;
	}

	/**
	 * Variable to set whether this is to be printed or previewed
	 */
	boolean isPreview;
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
			writer = new HardcopyWriter(new Frame(), Bundle.getMessage("TitleLocationsTable"), 10, .5, .5, .5, .5,
					isPreview);
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
	private void printLocationsSelected() throws IOException {
		List<String> locations = manager.getLocationsByNameList();
		int totalLength = 0;
		int usedLength = 0;
		int numberRS = 0;
		int numberCars = 0;
		int numberEngines = 0;
		// header
		String s = Bundle.getMessage("Location") + TAB + TAB + TAB + Bundle.getMessage("Length") + " "
				+ Bundle.getMessage("Used") + TAB + Bundle.getMessage("RS") + TAB + Bundle.getMessage("Cars")
				+ TAB + Bundle.getMessage("Engines") + TAB + Bundle.getMessage("Pickup") + " "
				+ Bundle.getMessage("Drop") + NEW_LINE;
		writer.write(s);
		for (int i = 0; i < locations.size(); i++) {
			Location location = manager.getLocationById(locations.get(i));
			// location name, track length, used, number of RS, scheduled pick ups and drops
			s = padOutString(location.getName(), Control.max_len_string_location_name) + TAB + "  "
					+ Integer.toString(location.getLength()) + TAB
					+ Integer.toString(location.getUsedLength()) + TAB
					+ Integer.toString(location.getNumberRS()) + TAB + TAB + TAB
					+ Integer.toString(location.getPickupRS()) + TAB + Integer.toString(location.getDropRS())
					+ NEW_LINE;
			writer.write(s);

			totalLength += location.getLength();
			usedLength += location.getUsedLength();
			numberRS += location.getNumberRS();

			List<String> yards = location.getTrackIdsByNameList(Track.YARD);
			if (yards.size() > 0) {
				// header
				writer.write(SPACE + Bundle.getMessage("YardName") + NEW_LINE);
				for (int k = 0; k < yards.size(); k++) {
					Track yard = location.getTrackById(yards.get(k));
					writer.write(getTrackString(yard));
					numberCars += yard.getNumberCars();
					numberEngines += yard.getNumberEngines();
				}
			}

			List<String> spurs = location.getTrackIdsByNameList(Track.SPUR);
			if (spurs.size() > 0) {
				// header
				writer.write(SPACE + Bundle.getMessage("SpurName") + NEW_LINE);
				for (int k = 0; k < spurs.size(); k++) {
					Track spur = location.getTrackById(spurs.get(k));
					writer.write(getTrackString(spur));
					numberCars += spur.getNumberCars();
					numberEngines += spur.getNumberEngines();
				}
			}

			List<String> interchanges = location.getTrackIdsByNameList(Track.INTERCHANGE);
			if (interchanges.size() > 0) {
				// header
				writer.write(SPACE + Bundle.getMessage("InterchangeName") + NEW_LINE);
				for (int k = 0; k < interchanges.size(); k++) {
					Track interchange = location.getTrackById(interchanges.get(k));
					writer.write(getTrackString(interchange));
					numberCars += interchange.getNumberCars();
					numberEngines += interchange.getNumberEngines();
				}
			}

			List<String> stagings = location.getTrackIdsByNameList(Track.STAGING);
			if (stagings.size() > 0) {
				// header
				writer.write(SPACE + Bundle.getMessage("StagingName") + NEW_LINE);
				for (int k = 0; k < stagings.size(); k++) {
					Track staging = location.getTrackById(stagings.get(k));
					writer.write(getTrackString(staging));
					numberCars += staging.getNumberCars();
					numberEngines += staging.getNumberEngines();
				}
			}
			writer.write(NEW_LINE);
		}

		// summary
		s = MessageFormat.format(Bundle.getMessage("TotalLengthMsg"), new Object[] {
				Integer.toString(totalLength), Integer.toString(usedLength),
				Integer.toString(usedLength * 100 / totalLength) })
				+ NEW_LINE;
		writer.write(s);
		s = MessageFormat.format(Bundle.getMessage("TotalRollingMsg"), new Object[] {
				Integer.toString(numberRS), Integer.toString(numberCars), Integer.toString(numberEngines) })
				+ NEW_LINE;
		writer.write(s);
		// are there trains in route, then some cars and engines not counted!
		if (numberRS != numberCars + numberEngines) {
			s = MessageFormat.format(Bundle.getMessage("NoteRSMsg"), new Object[] { Integer.toString(numberRS
					- (numberCars + numberEngines)) })
					+ NEW_LINE;
			writer.write(s);
		}
		if (printSchedules.isSelected() || printDetails.isSelected() || printAnalysis.isSelected())
			writer.write(FORM_FEED);
	}

	private void printSchedulesSelected() throws IOException {
		List<String> locations = manager.getLocationsByNameList();
		String s = padOutString(Bundle.getMessage("Schedules"), MAX_NAME_LENGTH) + " " + Bundle.getMessage("Location") + " - "
				+ Bundle.getMessage("SpurName") + NEW_LINE;
		writer.write(s);
		ScheduleManager sm = ScheduleManager.instance();
		List<String> schedules = sm.getSchedulesByNameList();
		for (int i = 0; i < schedules.size(); i++) {
			Schedule schedule = sm.getScheduleById(schedules.get(i));
			for (int j = 0; j < locations.size(); j++) {
				Location location = manager.getLocationById(locations.get(j));
				List<String> spurs = location.getTrackIdsByNameList(Track.SPUR);
				for (int k = 0; k < spurs.size(); k++) {
					Track spur = location.getTrackById(spurs.get(k));
					if (spur.getScheduleId().equals(schedule.getId())) {
						// pad out schedule name
						s = padOutString(schedule.getName(), MAX_NAME_LENGTH) + " " + location.getName() + " - " + spur.getName();
						String status = spur.checkScheduleValid();
						if (!status.equals("")) {
							StringBuffer buf = new StringBuffer(s);
							for (int m = s.length(); m < 63; m++) {
								buf.append(" ");
							}
							s = buf.toString();
							if (s.length() > 63)
								s = s.substring(0, 63);
							s = s + TAB + status;
						}
						s = s + NEW_LINE;
						writer.write(s);
						// show the schedule's mode
						String mode = Bundle.getMessage("Sequential");
						if (spur.getScheduleMode() == Track.MATCH)
							mode = Bundle.getMessage("Match");
						s = padOutString("", MAX_NAME_LENGTH)+ SPACE
								+ Bundle.getMessage("ScheduleMode") + ": " + mode  + NEW_LINE;
						writer.write(s);
						// show alternate track if there's one
						if (spur.getAlternativeTrack() != null) {
							s = padOutString("", MAX_NAME_LENGTH)+ SPACE
									+ MessageFormat.format(Bundle.getMessage("AlternateTrackName"), new Object[] { spur
											.getAlternativeTrack().getName() }) + NEW_LINE;
							writer.write(s);
						}
						// show custom loads from staging if not 100%
						if (spur.getReservationFactor() != 100) {
							s = padOutString("", MAX_NAME_LENGTH)+ SPACE
									+ MessageFormat.format(Bundle.getMessage("PercentageStaging"), new Object[] { spur
											.getReservationFactor() }) + NEW_LINE;
							writer.write(s);
						}
					}
				}
			}
		}
		if (printDetails.isSelected() || printAnalysis.isSelected())
			writer.write(FORM_FEED);
	}

	private void printDetailsSelected() throws IOException {
		List<String> locations = manager.getLocationsByNameList();
		String s = Bundle.getMessage("DetailedReport") + NEW_LINE;
		writer.write(s);
		for (int i = 0; i < locations.size(); i++) {
			Location location = manager.getLocationById(locations.get(i));
			String name = location.getName();
			// services train direction
			int dir = location.getTrainDirections();
			s = NEW_LINE + name + getDirection(dir);
			writer.write(s);
			// services car and engine types
			s = getLocationTypes(location);
			writer.write(s);

			List<String> yards = location.getTrackIdsByNameList(Track.YARD);
			if (yards.size() > 0) {
				s = SPACE + Bundle.getMessage("YardName") + NEW_LINE;
				writer.write(s);
				printTrackInfo(location, yards);
			}

			List<String> spurs = location.getTrackIdsByNameList(Track.SPUR);
			if (spurs.size() > 0) {
				s = SPACE + Bundle.getMessage("SpurName") + NEW_LINE;
				writer.write(s);
				printTrackInfo(location, spurs);
			}

			List<String> interchanges = location.getTrackIdsByNameList(Track.INTERCHANGE);
			if (interchanges.size() > 0) {
				s = SPACE + Bundle.getMessage("InterchangeName") + NEW_LINE;
				writer.write(s);
				printTrackInfo(location, interchanges);
			}

			List<String> stagings = location.getTrackIdsByNameList(Track.STAGING);
			if (stagings.size() > 0) {
				s = SPACE + Bundle.getMessage("StagingName") + NEW_LINE;
				writer.write(s);
				printTrackInfo(location, stagings);
			}
		}
		if (printAnalysis.isSelected())
			writer.write(FORM_FEED);
	}

	private final boolean showStaging = false;

	private void printAnalysisSelected() throws IOException {
		CarManager carManager = CarManager.instance();
		List<String> locations = manager.getLocationsByNameList();
		List<String> cars = carManager.getByLocationList();
		String[] carTypes = CarTypes.instance().getNames();

		String s = Bundle.getMessage("TrackAnalysis") + NEW_LINE;
		writer.write(s);

		// print the car type being analyzed
		for (int i = 0; i < carTypes.length; i++) {
			String type = carTypes[i];
			// get the total length for a given car type
			int numberOfCars = 0;
			int totalTrackLength = 0;
			for (int j = 0; j < cars.size(); j++) {
				Car car = carManager.getById(cars.get(j));
				if (car.getType().equals(type) && car.getLocation() != null) {
					numberOfCars++;
					totalTrackLength = totalTrackLength + Integer.parseInt(car.getLength()) + Car.COUPLER;
				}
			}
			writer.write(MessageFormat.format(Bundle.getMessage("NumberTypeLength"), new Object[] {
					numberOfCars, type, totalTrackLength })
					+ NEW_LINE);
			// don't bother reporting when the number of cars for a given type is zero
			if (numberOfCars > 0) {
				// spurs
				writer.write(SPACE
						+ MessageFormat.format(Bundle.getMessage("SpurTrackThatAccept"),
								new Object[] { type }) + NEW_LINE);
				int trackLength = getTrackLengthAcceptType(locations, type, Track.SPUR);
				if (trackLength > 0)
					writer.write(SPACE
							+ MessageFormat.format(Bundle.getMessage("TotalLengthSpur"), new Object[] { type,
									trackLength, 100 * totalTrackLength / trackLength }) + NEW_LINE);
				else
					writer.write(SPACE + Bundle.getMessage("None") + NEW_LINE);
				// yards
				writer.write(SPACE
						+ MessageFormat.format(Bundle.getMessage("YardTrackThatAccept"),
								new Object[] { type }) + NEW_LINE);
				trackLength = getTrackLengthAcceptType(locations, type, Track.YARD);
				if (trackLength > 0)
					writer.write(SPACE
							+ MessageFormat.format(Bundle.getMessage("TotalLengthYard"), new Object[] { type,
									trackLength, 100 * totalTrackLength / trackLength }) + NEW_LINE);
				else
					writer.write(SPACE + Bundle.getMessage("None") + NEW_LINE);
				// interchanges
				writer.write(SPACE
						+ MessageFormat.format(Bundle.getMessage("InterchangesThatAccept"),
								new Object[] { type }) + NEW_LINE);
				trackLength = getTrackLengthAcceptType(locations, type, Track.INTERCHANGE);
				if (trackLength > 0)
					writer.write(SPACE
							+ MessageFormat.format(Bundle.getMessage("TotalLengthInterchange"), new Object[] {
									type, trackLength, 100 * totalTrackLength / trackLength }) + NEW_LINE);
				else
					writer.write(SPACE + Bundle.getMessage("None") + NEW_LINE);
				// staging
				if (showStaging) {
					writer.write(SPACE
							+ MessageFormat.format(Bundle.getMessage("StageTrackThatAccept"),
									new Object[] { type }) + NEW_LINE);
					trackLength = getTrackLengthAcceptType(locations, type, Track.STAGING);
					if (trackLength > 0)
						writer.write(SPACE
								+ MessageFormat.format(Bundle.getMessage("TotalLengthStage"), new Object[] {
										type, trackLength, 100 * totalTrackLength / trackLength }) + NEW_LINE);
					else
						writer.write(SPACE + Bundle.getMessage("None") + NEW_LINE);
				}
			}
		}
	}

	private int getTrackLengthAcceptType(List<String> locations, String carType, String trackType)
			throws IOException {
		int trackLength = 0;
		for (int j = 0; j < locations.size(); j++) {
			Location location = manager.getLocationById(locations.get(j));
			// get a list of spur tracks at this location
			List<String> tracks = location.getTrackIdsByNameList(trackType);
			for (int k = 0; k < tracks.size(); k++) {
				Track track = location.getTrackById(tracks.get(k));
				if (track.acceptsTypeName(carType)) {
					trackLength = trackLength + track.getLength();
					writer.write(SPACE
							+ SPACE
							+ MessageFormat.format(Bundle.getMessage("LocationTrackLength"), new Object[] {
									location.getName(), track.getName(), track.getLength() }) + NEW_LINE);
				}
			}
		}
		return trackLength;
	}

	private String getTrackString(Track track) {
		String s = TAB + padOutString(track.getName(), Control.max_len_string_track_name) + " "
				+ Integer.toString(track.getLength()) + TAB + Integer.toString(track.getUsedLength()) + TAB
				+ Integer.toString(track.getNumberRS()) + TAB + Integer.toString(track.getNumberCars()) + TAB
				+ Integer.toString(track.getNumberEngines()) + TAB + Integer.toString(track.getPickupRS())
				+ TAB + Integer.toString(track.getDropRS()) + NEW_LINE;
		return s;
	}

	private String getDirection(int dir) {
		if ((Setup.getTrainDirection() & dir) == 0) {
			return " " + Bundle.getMessage("LocalOnly") + NEW_LINE;
		}
		String direction = " " + Bundle.getMessage("ServicedByTrain") + " ";
		if ((Setup.getTrainDirection() & dir & Location.NORTH) > 0)
			direction = direction + Bundle.getMessage("North") + " ";
		if ((Setup.getTrainDirection() & dir & Location.SOUTH) > 0)
			direction = direction + Bundle.getMessage("South") + " ";
		if ((Setup.getTrainDirection() & dir & Location.EAST) > 0)
			direction = direction + Bundle.getMessage("East") + " ";
		if ((Setup.getTrainDirection() & dir & Location.WEST) > 0)
			direction = direction + Bundle.getMessage("West") + " ";
		direction = direction + NEW_LINE;
		return direction;
	}

	private void printTrackInfo(Location location, List<String> tracks) {
		for (int k = 0; k < tracks.size(); k++) {
			Track track = location.getTrackById(tracks.get(k));
			String name = track.getName();
			try {
				String s = TAB + name + getDirection(track.getTrainDirections());
				writer.write(s);
				writer.write(getTrackTypes(location, track));
				writer.write(getTrackRoads(track));
				writer.write(getTrackLoads(track));
				writer.write(getTrackShipLoads(track));
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

	private String getLocationTypes(Location location) {
		StringBuffer buf = new StringBuffer(TAB + TAB + Bundle.getMessage("TypesServiced") + NEW_LINE + TAB
				+ TAB);
		int charCount = 0;
		int typeCount = 0;
		String[] cTypes = CarTypes.instance().getNames();
		for (int i = 0; i < cTypes.length; i++) {
			if (location.acceptsTypeName(cTypes[i])) {
				typeCount++;
				charCount += cTypes[i].length() + 2;
				if (charCount > characters) {
					buf.append(NEW_LINE + TAB + TAB);
					charCount = cTypes[i].length() + 2;
				}
				buf.append(cTypes[i] + ", ");
			}
		}
		String[] eTypes = EngineTypes.instance().getNames();
		for (int i = 0; i < eTypes.length; i++) {
			if (location.acceptsTypeName(eTypes[i])) {
				typeCount++;
				charCount += eTypes[i].length() + 2;
				if (charCount > characters) {
					buf.append(NEW_LINE + TAB + TAB);
					charCount = eTypes[i].length() + 2;
				}
				buf.append(eTypes[i] + ", ");
			}
		}
		if (buf.length() > 2)
			buf.setLength(buf.length() - 2); // remove trailing separators
		// does this location accept all types?
		if (typeCount == cTypes.length + eTypes.length)
			buf = new StringBuffer(TAB + TAB + Bundle.getMessage("LocationAcceptsAllTypes"));
		buf.append(NEW_LINE);
		return buf.toString();
	}

	private String getTrackTypes(Location location, Track track) {
		StringBuffer buf = new StringBuffer(TAB + TAB + Bundle.getMessage("TypesServicedTrack") + NEW_LINE
				+ TAB + TAB);
		int charCount = 0;
		int typeCount = 0;
		String[] cTypes = CarTypes.instance().getNames();
		for (int i = 0; i < cTypes.length; i++) {
			if (track.acceptsTypeName(cTypes[i])) {
				typeCount++;
				charCount += cTypes[i].length() + 2;
				if (charCount > characters) {
					buf.append(NEW_LINE + TAB + TAB);
					charCount = cTypes[i].length() + 2;
				}
				buf.append(cTypes[i] + ", ");
			}
		}
		String[] eTypes = EngineTypes.instance().getNames();
		for (int i = 0; i < eTypes.length; i++) {
			if (track.acceptsTypeName(eTypes[i])) {
				typeCount++;
				charCount += eTypes[i].length() + 2;
				if (charCount > characters) {
					buf.append(NEW_LINE + TAB + TAB);
					charCount = eTypes[i].length() + 2;
				}
				buf.append(eTypes[i] + ", ");
			}
		}
		if (buf.length() > 2)
			buf.setLength(buf.length() - 2); // remove trailing separators
		// does this track accept all types?
		if (typeCount == cTypes.length + eTypes.length)
			buf = new StringBuffer(TAB + TAB + Bundle.getMessage("TrackAcceptsAllTypes"));
		buf.append(NEW_LINE);
		return buf.toString();
	}

	private String getTrackRoads(Track track) {
		if (track.getRoadOption().equals(Track.ALLROADS)) {
			return TAB + TAB + Bundle.getMessage("AcceptsAllRoads") + NEW_LINE;
		}
		
		String op = Bundle.getMessage("RoadsServicedTrack");
		if (track.getRoadOption().equals(Track.EXCLUDEROADS))
			op = Bundle.getMessage("ExcludeRoadsTrack");

		StringBuffer buf = new StringBuffer(TAB + TAB + op + NEW_LINE + TAB + TAB);
		int charCount = 0;
		String[] roads = track.getRoadNames();
		for (int i = 0; i < roads.length; i++) {
			charCount += roads[i].length() + 2;
			if (charCount > characters) {
				buf.append(NEW_LINE + TAB + TAB);
				charCount = roads[i].length() + 2;
			}
			buf.append(roads[i] + ", ");
		}
		if (buf.length() > 2)
			buf.setLength(buf.length() - 2); // remove trailing separators
		buf.append(NEW_LINE);
		return buf.toString();
	}

	private String getTrackLoads(Track track) {
		if (track.getLoadOption().equals(Track.ALLLOADS)) {
			return TAB + TAB + Bundle.getMessage("AcceptsAllLoads") + NEW_LINE;
		}
		
		String op = Bundle.getMessage("LoadsServicedTrack");
		if (track.getLoadOption().equals(Track.EXCLUDELOADS))
			op = Bundle.getMessage("ExcludeLoadsTrack");

		StringBuffer buf = new StringBuffer(TAB + TAB + op + NEW_LINE + TAB + TAB);
		int charCount = 0;
		String[] carLoads = track.getLoadNames();
		for (int i = 0; i < carLoads.length; i++) {
			charCount += carLoads[i].length() + 2;
			if (charCount > characters) {
				buf.append(NEW_LINE + TAB + TAB);
				charCount = carLoads[i].length() + 2;
			}
			buf.append(carLoads[i] + ", ");
		}
		if (buf.length() > 2)
			buf.setLength(buf.length() - 2); // remove trailing separators
		buf.append(NEW_LINE);
		return buf.toString();
	}
	
	private String getTrackShipLoads(Track track) {
		// only staging has the ship load control
		if (!track.getLocType().equals(Track.STAGING))
			return "";
		if (track.getShipLoadOption().equals(Track.ALLLOADS)) {
			return TAB + TAB + Bundle.getMessage("ShipsAllLoads") + NEW_LINE;
		}
		String op = Bundle.getMessage("LoadsShippedTrack");
		if (track.getShipLoadOption().equals(Track.EXCLUDELOADS))
			op = Bundle.getMessage("ExcludeLoadsShippedTrack");
		
		StringBuffer buf = new StringBuffer(TAB + TAB + op + NEW_LINE + TAB + TAB);
		int charCount = 0;
		String[] carLoads = track.getShipLoadNames();
		for (int i = 0; i < carLoads.length; i++) {
			charCount += carLoads[i].length() + 2;
			if (charCount > characters) {
				buf.append(NEW_LINE + TAB + TAB);
				charCount = carLoads[i].length() + 2;
			}
			buf.append(carLoads[i] + ", ");
		}
		if (buf.length() > 2)
			buf.setLength(buf.length() - 2); // remove trailing separators
		buf.append(NEW_LINE);
		return buf.toString();
	}

	private String getCarOrder(Track track) {
		// only yards and interchanges have the car order option
		if (track.getLocType().equals(Track.SPUR) || track.getLocType().equals(Track.STAGING)
				|| track.getServiceOrder().equals(Track.NORMAL))
			return "";
		if (track.getServiceOrder().equals(Track.FIFO))
			return TAB + TAB + Bundle.getMessage("TrackPickUpOrderFIFO") + NEW_LINE;
		return TAB + TAB + Bundle.getMessage("TrackPickUpOrderLIFO") + NEW_LINE;
	}

	private String getSetOutTrains(Track track) {
		if (track.getDropOption().equals(Track.ANY))
			return TAB + TAB + Bundle.getMessage("SetOutAllTrains") + NEW_LINE;
		StringBuffer buf;
		int charCount = 0;
		String[] ids = track.getDropIds();
		if (track.getDropOption().equals(Track.TRAINS) || track.getDropOption().equals(Track.EXCLUDE_TRAINS)) {
			String trainType = Bundle.getMessage("TrainsSetOutTrack");
			if (track.getDropOption().equals(Track.EXCLUDE_TRAINS))
				trainType = Bundle.getMessage("ExcludeTrainsSetOutTrack");
			buf = new StringBuffer(TAB + TAB + trainType + NEW_LINE + TAB + TAB);
			for (int i = 0; i < ids.length; i++) {
				Train train = TrainManager.instance().getTrainById(ids[i]);
				if (train == null) {
					log.info("Could not find a train for id: " + ids[i] + " track (" + track.getName() + ")");
					continue;
				}
				charCount += train.getName().length() + 2;
				if (charCount > characters) {
					buf.append(NEW_LINE + TAB + TAB);
					charCount = train.getName().length() + 2;
				}
				buf.append(train.getName() + ", ");
			}
		} else {
			String routeType = Bundle.getMessage("RoutesSetOutTrack");
			if (track.getDropOption().equals(Track.EXCLUDE_ROUTES))
				routeType = Bundle.getMessage("ExcludeRoutesSetOutTrack");
			buf = new StringBuffer(TAB + TAB + routeType + NEW_LINE + TAB + TAB);
			for (int i = 0; i < ids.length; i++) {
				Route route = RouteManager.instance().getRouteById(ids[i]);
				if (route == null) {
					log.info("Could not find a route for id: " + ids[i] + " location ("
							+ track.getLocation().getName() + ") track (" + track.getName() + ")"); // NOI18N
					continue;
				}
				charCount += route.getName().length() + 2;
				if (charCount > characters) {
					buf.append(NEW_LINE + TAB + TAB);
					charCount = route.getName().length() + 2;
				}
				buf.append(route.getName() + ", ");
			}
		}
		if (buf.length() > 2)
			buf.setLength(buf.length() - 2); // remove trailing separators
		buf.append(NEW_LINE);
		return buf.toString();
	}

	private String getPickUpTrains(Track track) {
		if (track.getPickupOption().equals(Track.ANY))
			return TAB + TAB + Bundle.getMessage("PickUpAllTrains") + NEW_LINE;
		StringBuffer buf;
		int charCount = 0;
		String[] ids = track.getPickupIds();
		if (track.getPickupOption().equals(Track.TRAINS)
				|| track.getPickupOption().equals(Track.EXCLUDE_TRAINS)) {
			String trainType = Bundle.getMessage("TrainsPickUpTrack");
			if (track.getPickupOption().equals(Track.EXCLUDE_TRAINS))
				trainType = Bundle.getMessage("ExcludeTrainsPickUpTrack");
			buf = new StringBuffer(TAB + TAB + trainType + NEW_LINE + TAB + TAB);
			for (int i = 0; i < ids.length; i++) {
				Train train = TrainManager.instance().getTrainById(ids[i]);
				if (train == null) {
					log.info("Could not find a train for id: " + ids[i] + " track (" + track.getName() + ")");
					continue;
				}
				charCount += train.getName().length() + 2;
				if (charCount > characters) {
					buf.append(NEW_LINE + TAB + TAB);
					charCount = train.getName().length() + 2;
				}
				buf.append(train.getName() + ", ");
			}
		} else {
			String routeType = Bundle.getMessage("RoutesPickUpTrack");
			if (track.getPickupOption().equals(Track.EXCLUDE_ROUTES))
				routeType = Bundle.getMessage("ExcludeRoutesPickUpTrack");
			buf = new StringBuffer(TAB + TAB + routeType + NEW_LINE + TAB + TAB);
			for (int i = 0; i < ids.length; i++) {
				Route route = RouteManager.instance().getRouteById(ids[i]);
				if (route == null) {
					log.info("Could not find a route for id: " + ids[i] + " location ("
							+ track.getLocation().getName() + ") track (" + track.getName() + ")"); // NOI18N
					continue;
				}
				charCount += route.getName().length() + 2;
				if (charCount > characters) {
					buf.append(NEW_LINE + TAB + TAB);
					charCount = route.getName().length() + 2;
				}
				buf.append(route.getName() + ", ");
			}
		}
		if (buf.length() > 2)
			buf.setLength(buf.length() - 2); // remove trailing separators
		buf.append(NEW_LINE);
		return buf.toString();
	}

	private String getSchedule(Track track) {
		// only spurs have schedules
		if (!track.getLocType().equals(Track.SPUR) || track.getSchedule() == null)
			return "";
		StringBuffer buf = new StringBuffer(TAB
				+ TAB
				+ MessageFormat.format(Bundle.getMessage("TrackScheduleName"), new Object[] { track
						.getScheduleName() }) + NEW_LINE);
		if (track.getAlternativeTrack() != null)
			buf.append(TAB
					+ TAB
					+ MessageFormat.format(Bundle.getMessage("AlternateTrackName"), new Object[] { track
							.getAlternativeTrack().getName() }) + NEW_LINE);
		if (track.getReservationFactor() != 100)
			buf.append(TAB
					+ TAB
					+ MessageFormat.format(Bundle.getMessage("PercentageStaging"), new Object[] { track
							.getReservationFactor() }) + NEW_LINE);
		return buf.toString();
	}
	
	private String padOutString(String s, int length) {
		StringBuffer buf = new StringBuffer(s);
		for (int n = s.length(); n < length; n++) {
			buf.append(" ");
		}
		return buf.toString();
	}

	JCheckBox printLocations = new JCheckBox(Bundle.getMessage("PrintLocations"));
	JCheckBox printSchedules = new JCheckBox(Bundle.getMessage("PrintSchedules"));
	JCheckBox printDetails = new JCheckBox(Bundle.getMessage("PrintDetails"));
	JCheckBox printAnalysis = new JCheckBox(Bundle.getMessage("PrintAnalysis"));

	JButton okayButton = new JButton(Bundle.getMessage("ButtonOkay"));

	public class LocationPrintOptionFrame extends OperationsFrame {
		PrintLocationsAction pla;

		public LocationPrintOptionFrame(PrintLocationsAction pla) {
			super();
			this.pla = pla;
			// create panel
			JPanel pPanel = new JPanel();
			pPanel.setLayout(new BoxLayout(pPanel, BoxLayout.Y_AXIS));
			pPanel.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("PrintOptions")));
			pPanel.add(printLocations);
			pPanel.add(printSchedules);
			pPanel.add(printDetails);
			pPanel.add(printAnalysis);
			// set defaults
			printLocations.setSelected(true);
			printSchedules.setSelected(true);
			printDetails.setSelected(true);
			printAnalysis.setSelected(true);

			// add tool tips

			JPanel pButtons = new JPanel();
			pButtons.setLayout(new GridBagLayout());
			pButtons.add(okayButton);
			addButtonAction(okayButton);

			getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
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

	static Logger log = LoggerFactory.getLogger(PrintLocationsAction.class
			.getName());
}
