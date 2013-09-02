// TrainSwitchLists.java

package jmri.jmrit.operations.trains;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.swing.JLabel;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarColors;
import jmri.jmrit.operations.rollingstock.cars.CarLengths;
import jmri.jmrit.operations.rollingstock.cars.CarLoad;
import jmri.jmrit.operations.rollingstock.cars.CarLoads;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.CarOwners;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;

/**
 * Common routines for trains
 * 
 * @author Daniel Boudreau (C) Copyright 2008, 2009, 2010, 2011, 2012, 2013
 * @version $Revision: 1 $
 */
public class TrainCommon {

	private static final String LENGTHABV = Setup.LENGTHABV;	// Length symbol
	protected static final String TAB = "    "; // NOI18N
	protected static final String NEW_LINE = "\n"; // NOI18N
	protected static final String SPACE = " ";
	protected static final String BLANK_LINE = " ";
	private static final boolean PICKUP = true;
	private static final boolean LOCAL = true;

	CarManager carManager = CarManager.instance();
	EngineManager engineManager = EngineManager.instance();
	
	// for manifests
	int cars = 0;
	int emptyCars = 0;
	boolean newWork = false;
	
	// for switch lists
	boolean pickupCars;	
	boolean dropCars;
	
	protected void blockLocosTwoColumn(PrintWriter fileOut, List<String> engineList, RouteLocation rl, boolean isManifest) {
		for (int k = 0; k < engineList.size(); k++) {
			Engine engine = engineManager.getById(engineList.get(k));
			if (engine.getRouteLocation() == rl && !engine.getTrackName().equals("")) { 
				newLine(fileOut, pickupEngine(engine).trim(), isManifest);
			}
			if (engine.getRouteDestination() == rl) {
				int lineLength;
				if (isManifest)
					lineLength = getLineLength(Setup.getManifestOrientation());
				else
					lineLength = getLineLength(Setup.getSwitchListOrientation());
				String s = padString("", lineLength / 2);
				s = s + " |" + dropEngine(engine);
				if (s.length() > lineLength)
					s = s.substring(0, lineLength);
				newLine(fileOut, s, isManifest);
			}
		}
	}

	/**
	 * Adds a list of locomotive pick ups for the route location to the output file
	 * @param fileOut
	 * @param engineList
	 * @param rl
	 * @param orientation
	 */
	protected void pickupEngines(PrintWriter fileOut, List<String> engineList, RouteLocation rl,
			String orientation) {
		for (int i = 0; i < engineList.size(); i++) {
			Engine engine = engineManager.getById(engineList.get(i));
			if (engine.getRouteLocation() == rl && !engine.getTrackName().equals(""))
				pickupEngine(fileOut, engine, orientation);
		}
	}
	
	private void pickupEngine(PrintWriter file, Engine engine, String orientation) {
		StringBuffer buf = new StringBuffer(Setup.getPickupEnginePrefix());
		String[] format = Setup.getPickupEngineMessageFormat();
		for (int i = 0; i < format.length; i++) {
			String s = getEngineAttribute(engine, format[i], PICKUP);
			if (!checkStringLength(buf.toString() + s, orientation, Setup.getFontName(), Setup.getManifestFontSize())) {
				addLine(file, buf.toString());
				buf = new StringBuffer(TAB);
			}
			buf.append(s);
		}
		addLine(file, buf.toString());
	}

	/**
	 * Adds a list of locomotive drops for the route location to the output file
	 * @param fileOut
	 * @param engineList
	 * @param rl
	 * @param orientation
	 */
	protected void dropEngines(PrintWriter fileOut, List<String> engineList, RouteLocation rl,
			String orientation) {
		for (int i = 0; i < engineList.size(); i++) {
			Engine engine = engineManager.getById(engineList.get(i));
			if (engine.getRouteDestination() == rl)
				dropEngine(fileOut, engine, orientation);
		}
	}
	
	private void dropEngine(PrintWriter file, Engine engine, String orientation) {
		StringBuffer buf = new StringBuffer(Setup.getDropEnginePrefix());
		String[] format = Setup.getDropEngineMessageFormat();
		for (int i = 0; i < format.length; i++) {
			String s = getEngineAttribute(engine, format[i], !PICKUP);
			if (!checkStringLength(buf.toString() + s, orientation, Setup.getFontName(), Setup.getManifestFontSize())) {
				addLine(file, buf.toString());
				buf = new StringBuffer(TAB);
			}
			buf.append(s);
		}
		addLine(file, buf.toString());
	}

	/**
	 * Returns the pick up string for a loco.  Useful for frames like the train conductor and yardmaster.
	 * @param engine
	 * @return engine pick up string
	 */
	public String pickupEngine(Engine engine) {
		StringBuffer buf = new StringBuffer();
		String[] format = Setup.getPickupEngineMessageFormat();
		for (int i = 0; i < format.length; i++) {
			String s = getEngineAttribute(engine, format[i], PICKUP);
			buf.append(s);
		}
		return buf.toString();
	}

	/**
	 * Returns the drop string for a loco.  Useful for frames like the train conductor and yardmaster.
	 * @param engine
	 * @return engine drop string
	 */
	public String dropEngine(Engine engine) {
		StringBuffer buf = new StringBuffer();
		String[] format = Setup.getDropEngineMessageFormat();
		for (int i = 0; i < format.length; i++) {
			String s = getEngineAttribute(engine, format[i], !PICKUP);
			buf.append(s);
		}
		return buf.toString();
	}
	
	/**
	 * Block cars by track, then pick up and set out for each location in a train's route.
	 */
	protected void blockCarsByTrack(PrintWriter fileOut, Train train, List<String> carList,
			List<String> routeList, RouteLocation rl, int r, boolean isManifest) {
		List<String> trackIds = rl.getLocation().getTrackIdsByNameList(null);
		List<String> trackNames = new ArrayList<String>();
		clearUtilityCarTypes(); // list utility cars by quantity
		for (int i = 0; i < trackIds.size(); i++) {
			Track track = rl.getLocation().getTrackById(trackIds.get(i));
			if (trackNames.contains(splitString(track.getName())))
				continue;
			trackNames.add(splitString(track.getName())); // use a track name once
			// block cars by destination
			for (int j = r; j < routeList.size(); j++) {
				RouteLocation rld = train.getRoute().getLocationById(routeList.get(j));
				for (int k = 0; k < carList.size(); k++) {
					Car car = carManager.getById(carList.get(k));
					if (Setup.isSortByTrackEnabled()
							&& !splitString(track.getName()).equals(splitString(car.getTrackName())))
						continue;
					// note that a car in train doesn't have a track assignment
					if (car.getRouteLocation() == rl && car.getTrack() != null
							&& car.getRouteDestination() == rld) {
						if (car.isUtility())
							pickupUtilityCars(fileOut, carList, car, rl, rld, isManifest);
						// use truncated format if there's a switch list
						else if (isManifest && Setup.isTruncateManifestEnabled() && rl.getLocation().isSwitchListEnabled())
							pickUpCarTruncated(fileOut, car);
						else
							pickUpCar(fileOut, car, isManifest);
						pickupCars = true;
						cars++;
						newWork = true;
						if (CarLoads.instance().getLoadType(car.getTypeName(), car.getLoadName()).equals(
								CarLoad.LOAD_TYPE_EMPTY))
							emptyCars++;
					}
				}
			}
			for (int j = 0; j < carList.size(); j++) {
				Car car = carManager.getById(carList.get(j));
				if (Setup.isSortByTrackEnabled()
						&& !splitString(track.getName()).equals(
								splitString(car.getDestinationTrackName())))
					continue;
				if (car.getRouteDestination() == rl
						&& car.getDestinationTrack() != null) {
					if (car.isUtility())
						setoutUtilityCars(fileOut, carList, car, rl, isManifest);
					// use truncated format if there's a switch list
					else if (isManifest && Setup.isTruncateManifestEnabled() && rl.getLocation().isSwitchListEnabled())
						truncatedDropCar(fileOut, car);
					else
						dropCar(fileOut, car, isManifest);
					dropCars = true;
					cars--;
					newWork = true;
					if (CarLoads.instance().getLoadType(car.getTypeName(), car.getLoadName()).equals(
							CarLoad.LOAD_TYPE_EMPTY))
						emptyCars--;
				}
			}
			if (!Setup.isSortByTrackEnabled())
				break; // done
		}
	}
	
	/**
	 * Produces a two column format for car pick ups and set outs. Sorted by track and then by destination.
	 */
	protected void blockCarsByTrackTwoColumn(PrintWriter fileOut, Train train, List<String> carList,
			List<String> routeList, RouteLocation rl, int r, boolean isManifest) {
		index = 0;
		int lineLength;
		if (isManifest)
			lineLength = getLineLength(Setup.getManifestOrientation());
		else
			lineLength = getLineLength(Setup.getSwitchListOrientation());
		List<String> trackIds = rl.getLocation().getTrackIdsByNameList(null);
		List<String> trackNames = new ArrayList<String>();
		clearUtilityCarTypes(); // list utility cars by quantity
		for (int i = 0; i < trackIds.size(); i++) {
			Track track = rl.getLocation().getTrackById(trackIds.get(i));
			if (trackNames.contains(splitString(track.getName())))
				continue;
			trackNames.add(splitString(track.getName())); // use a track name once
			// block car pick ups by destination
			for (int j = r; j < routeList.size(); j++) {
				RouteLocation rld = train.getRoute().getLocationById(routeList.get(j));
				for (int k = 0; k < carList.size(); k++) {
					Car car = carManager.getById(carList.get(k));
					if (car.getRouteLocation() == rl && !car.getTrackName().equals("")
							&& car.getRouteDestination() == rld) {
						if (Setup.isSortByTrackEnabled()
								&& !splitString(track.getName()).equals(splitString(car.getTrackName())))
							continue;
						pickupCars = true;
						cars++;
						newWork = true;
						if (CarLoads.instance().getLoadType(car.getTypeName(), car.getLoadName()).equals(
								CarLoad.LOAD_TYPE_EMPTY))
							emptyCars++;
						String s;
						if (car.isUtility()) {
							s = pickupUtilityCars(carList, car, rl, rld, isManifest);
							if (s == null)
								continue;
							s = s.trim();
						} else {
							s = pickupCar(car, isManifest).trim();
						}
						s = padString(s, lineLength / 2);
						if (s.length() > lineLength / 2)
							s = s.substring(0, lineLength / 2);
						if (isLocalMove(car)) {
							String sl = appendSetoutString(s, carList, car.getRouteDestination(), car,
									isManifest);
							// check for utility car, and local route with two or more locations
							if (!sl.equals(s)) {
								s = sl;
								carList.remove(car.getId()); // done with this car, remove from list
								k--;
							}
						} else {
							s = appendSetoutString(s, carList, rl, true, isManifest);
						}
						newLine(fileOut, s, isManifest);
					}
				}
			}
			if (!Setup.isSortByTrackEnabled())
					break;	//done
		}
		while (index < carList.size()) {
			String s = padString("", lineLength / 2);
			s = appendSetoutString(s, carList, rl, false, isManifest);
			String test = s.trim();
			if (test.length() > 0)
				newLine(fileOut, s, isManifest);
		}
	}
	
	int index = 0;

	private String appendSetoutString(String s, List<String> carList, RouteLocation rl, boolean local, boolean isManfest) {
		while (index < carList.size()) {
			Car car = carManager.getById(carList.get(index++));
			if (local && isLocalMove(car))
				continue;	// skip local moves
			// car list is already sorted by destination track
			if (car.getRouteDestination() == rl) {
				String so = appendSetoutString(s, carList, rl, car, isManfest);
				// check for utility car
				if (!so.equals(s))
					return so;
			}
		}
		return s;
	}

	private String appendSetoutString(String s, List<String> carList, RouteLocation rl, Car car,
			boolean isManifest) {
		dropCars = true;
		cars--;
		newWork = true;
		if (CarLoads.instance().getLoadType(car.getTypeName(), car.getLoadName()).equals(
				CarLoad.LOAD_TYPE_EMPTY))
			emptyCars--;
		String newS;
		// use truncated format if there's a switch list
		// else if (Setup.isTruncateManifestEnabled() && rl.getLocation().isSwitchListEnabled())
		// truncatedDropCar(fileOut, car);
		if (isLocalMove(car))
			newS = s + "->"; // NOI18N
		else
			newS = s + " |";
		if (car.isUtility()) {
			String so = setoutUtilityCars(carList, car, rl, false, isManifest);
			if (so == null)
				return s; // no changes to the input string
			newS = newS + so;
		} else {
			newS = newS + dropCar(car, isManifest);
		}
		int lineLength;
		if (isManifest)
			lineLength = getLineLength(Setup.getManifestOrientation());
		else
			lineLength = getLineLength(Setup.getSwitchListOrientation());
		if (newS.length() > lineLength)
			newS = newS.substring(0, lineLength);
		return newS;
	}

	/**
	 * Adds the car's pick up string to the output file using the manifest format
	 * @param file
	 * @param car
	 */
	protected void pickUpCar(PrintWriter file, Car car) {
		pickUpCar(file, car, new StringBuffer(Setup.getPickupCarPrefix()),
				Setup.getPickupCarMessageFormat(), Setup.getManifestOrientation());
	}

	/**
	 * Adds the car's pick up string to the output file using the truncated manifest format
	 * @param file
	 * @param car
	 */
	protected void pickUpCarTruncated(PrintWriter file, Car car) {
		pickUpCar(file, car, new StringBuffer(Setup.getPickupCarPrefix()),
				Setup.getTruncatedPickupManifestMessageFormat(), Setup.getManifestOrientation());
	}

	/**
	 * Adds the car's pick up string to the output file using the switch list format
	 * @param file
	 * @param car
	 */
	protected void pickUpCar(PrintWriter file, Car car, boolean isManifest) {
		if (isManifest)
			pickUpCar(file, car, new StringBuffer(Setup.getPickupCarPrefix()), Setup
					.getPickupCarMessageFormat(), Setup.getManifestOrientation());
		else
			pickUpCar(file, car, new StringBuffer(Setup.getSwitchListPickupCarPrefix()), Setup
					.getSwitchListPickupCarMessageFormat(), Setup.getSwitchListOrientation());
	}

	private void pickUpCar(PrintWriter file, Car car, StringBuffer buf, String[] format,
			String orientation) {
		if (isLocalMove(car))
			return; // print nothing local move, see dropCar
		for (int i = 0; i < format.length; i++) {
			String s = getCarAttribute(car, format[i], PICKUP, !LOCAL);
			if (!checkStringLength(buf.toString() + s, orientation, Setup.getFontName(), Setup.getManifestFontSize())) {
				addLine(file, buf.toString());
				buf = new StringBuffer(TAB);
			}
			buf.append(s);
		}
		String s = buf.toString();
		if (!s.equals(TAB))
			addLine(file, s);
	}

	/**
	 * Returns the pick up car string. Useful for frames like train conductor and yardmaster.
	 * @param isManifest when true use manifest format, when false use switch list format
	 * @param car
	 * @return pick up car string
	 */
	public String pickupCar(Car car, boolean isManifest) {
		StringBuffer buf = new StringBuffer();
		String[] format = Setup.getPickupCarMessageFormat();
		if (!isManifest)
			format = Setup.getSwitchListPickupCarMessageFormat();
		for (int i = 0; i < format.length; i++) {
			String s = getCarAttribute(car, format[i], PICKUP, !LOCAL);
			buf.append(s);
		}
		return buf.toString();
	}

	/**
	 * Adds the car's set out string to the output file using the manifest format
	 * @param file
	 * @param car
	 */
	protected void dropCar(PrintWriter file, Car car) {
		StringBuffer buf = new StringBuffer(Setup.getDropCarPrefix());
		String[] format = Setup.getDropCarMessageFormat();
		boolean isLocal = isLocalMove(car);
		if (isLocal) {
			buf = new StringBuffer(Setup.getLocalPrefix());
			format = Setup.getLocalMessageFormat();
		}
		dropCar(file, car, buf, format, isLocal, Setup.getManifestOrientation());
	}

	/**
	 * Adds the car's set out string to the output file using the truncated manifest format. Does not print out local
	 * moves. Local moves are only shown on the switch list for that location.
	 * @param file
	 * @param car
	 */
	protected void truncatedDropCar(PrintWriter file, Car car) {
		// local move?
		if (isLocalMove(car))
			return; // yes, don't print local moves on train manifest
		dropCar(file, car, new StringBuffer(Setup.getDropCarPrefix()),
				Setup.getTruncatedSetoutManifestMessageFormat(), false,
				Setup.getManifestOrientation());
	}

	/**
	 * Adds the car's set out string to the output file using the switch list format
	 * @param file
	 * @param car
	 */
	protected void dropCar(PrintWriter file, Car car, boolean isManifest) {
		if (isManifest)
			dropCar(file, car);
		else {
			StringBuffer buf = new StringBuffer(Setup.getSwitchListDropCarPrefix());
			String[] format = Setup.getSwitchListDropCarMessageFormat();
			boolean isLocal = isLocalMove(car);
			if (isLocal) {
				buf = new StringBuffer(Setup.getSwitchListLocalPrefix());
				format = Setup.getSwitchListLocalMessageFormat();
			}
			dropCar(file, car, buf, format, isLocal, Setup.getSwitchListOrientation());
		}
	}

	private void dropCar(PrintWriter file, Car car, StringBuffer buf, String[] format,
			boolean isLocal, String orientation) {
		for (int i = 0; i < format.length; i++) {
			String s = getCarAttribute(car, format[i], !PICKUP, isLocal);
			if (!checkStringLength(buf.toString() + s, orientation, Setup.getFontName(), Setup.getManifestFontSize())) {
				addLine(file, buf.toString());
				buf = new StringBuffer(TAB);
			}
			buf.append(s);
		}
		String s = buf.toString();
		if (!s.equals(TAB))
			addLine(file, s);
	}

	/**
	 * Returns the drop car string. Useful for frames like train conductor and yardmaster.
	 * @param car
	 * @param isManifest when true use manifest format, when false use switch list format
	 * @return drop car string
	 */
	public String dropCar(Car car, boolean isManifest) {
		StringBuffer buf = new StringBuffer();
		String[] format = Setup.getDropCarMessageFormat();
		if (!isManifest)
			format = Setup.getSwitchListDropCarMessageFormat();
		for (int i = 0; i < format.length; i++) {
			// TODO the Setup.Location doesn't work correctly for the conductor
			// window
			// therefore we use the local true to disable it.
			String s = getCarAttribute(car, format[i], !PICKUP, LOCAL);
			buf.append(s);
		}
		return buf.toString();
	}

	/**
	 * Returns the move car string. Useful for frames like train conductor and yardmaster.
	 * @param isManifest when true use manifest format, when false use switch list format
	 * @param car
	 * @return move car string
	 */
	public String moveCar(Car car, boolean isManifest) {
		StringBuffer buf = new StringBuffer();
		String[] format = Setup.getLocalMessageFormat();
		if (!isManifest)
			format = Setup.getSwitchListLocalMessageFormat();
		for (int i = 0; i < format.length; i++) {
			String s = getCarAttribute(car, format[i], !PICKUP, LOCAL);
			buf.append(s);
		}
		return buf.toString();
	}
	
	List<String> utilityCarTypes = new ArrayList<String>();

	/**
	 * Add a list of utility cars scheduled for pick up from the route location to the output file.
	 * The cars are blocked by destination.  
	 * @param fileOut
	 * @param carList
	 * @param car
	 * @param rl
	 * @param rld
	 * @param isManifest
	 */
	protected void pickupUtilityCars(PrintWriter fileOut, List<String> carList, Car car, RouteLocation rl,
			RouteLocation rld, boolean isManifest) {
		// list utility cars by type, track, length, and load
		String[] messageFormat = Setup.getPickupUtilityCarMessageFormat();
		if (!isManifest)
			messageFormat = Setup.getSwitchListPickupUtilityCarMessageFormat();
		int count = countUtiltiyCars(messageFormat, carList, car, rl, rld, PICKUP);
		if (count == 0)
			return; // already printed out this car type
		pickUpCar(fileOut, car, new StringBuffer(Setup.getPickupCarPrefix() + " "
				+ padString(Integer.toString(count), 3)), messageFormat, Setup.getManifestOrientation());
	}
	
	/**
	 * Add a list of utility cars scheduled for drop at the route location to the output file.
	 * @param fileOut
	 * @param carList
	 * @param car
	 * @param rl
	 * @param isManifest
	 */
	protected void setoutUtilityCars(PrintWriter fileOut, List<String> carList, Car car, RouteLocation rl,
			boolean isManifest) {
		boolean isLocal = isLocalMove(car);
		StringBuffer buf = new StringBuffer(Setup.getDropCarPrefix());
		String[] messageFormat = Setup.getSetoutUtilityCarMessageFormat();
		if (isLocal && isManifest) {
			buf = new StringBuffer(Setup.getLocalPrefix());
			messageFormat = Setup.getLocalUtilityCarMessageFormat();
		} else if (isLocal && !isManifest) {
			buf = new StringBuffer(Setup.getSwitchListLocalPrefix());
			messageFormat = Setup.getSwitchListLocalUtilityCarMessageFormat();
		} else if (!isLocal && !isManifest) {
			buf = new StringBuffer(Setup.getSwitchListDropCarPrefix());
			messageFormat = Setup.getSwitchListSetoutUtilityCarMessageFormat();
		}
		int count = countUtiltiyCars(messageFormat, carList, car, rl, null, !PICKUP);
		if (count == 0)
			return; // already printed out this car type
		buf.append(" " + padString(Integer.toString(count), 3));
		dropCar(fileOut, car, buf, messageFormat, isLocal, Setup.getManifestOrientation());
	}
	
	
	public String pickupUtilityCars(List<String> carList, Car car, RouteLocation rl,
			RouteLocation rld, boolean isManifest) {
		// list utility cars by type, track, length, and load
		String[] messageFormat = Setup.getPickupUtilityCarMessageFormat();
		if (!isManifest)
			messageFormat = Setup.getSwitchListPickupUtilityCarMessageFormat();		
		int count = countUtiltiyCars(messageFormat, carList, car, rl, rld, PICKUP);
		if (count == 0)
			return null;
		StringBuffer buf = new StringBuffer(" " + padString(Integer.toString(count), 3));
		for (int i = 0; i < messageFormat.length; i++) {
			String s = getCarAttribute(car, messageFormat[i], PICKUP, !LOCAL);
			buf.append(s);
		}
		return buf.toString();
	}
	
	public String setoutUtilityCars(List<String> carList, Car car, RouteLocation rl,
			boolean isLocal, boolean isManifest) {
		// list utility cars by type, track, length, and load
		String[] messageFormat = Setup.getSetoutUtilityCarMessageFormat();
		if (isLocal && isManifest) {
			messageFormat = Setup.getLocalUtilityCarMessageFormat();
		} else if (isLocal && !isManifest) {
			messageFormat = Setup.getSwitchListLocalUtilityCarMessageFormat();
		} else if (!isLocal && !isManifest) {
			messageFormat = Setup.getSwitchListSetoutUtilityCarMessageFormat();
		}
		int count = countUtiltiyCars(messageFormat, carList, car, rl, null, !PICKUP);
		if (count == 0)
			return null;
		StringBuffer buf = new StringBuffer(" " + padString(Integer.toString(count), 3));
		for (int i = 0; i < messageFormat.length; i++) {
			// TODO the Setup.Location doesn't work correctly for the conductor
			// window
			// therefore we use the local true to disable it.
			String s = getCarAttribute(car, messageFormat[i], !PICKUP, LOCAL);
			buf.append(s);
		}
		return buf.toString();
	}
	
	/**
	 * Scans the car list for utility cars that have the same attributes as the car provided.  Returns
	 * 0 if this car type has already been processed, otherwise the number of cars with the same attribute.
	 * @param messageFormat
	 * @param carList
	 * @param car
	 * @param rl
	 * @param rld
	 * @param isPickup
	 * @return 0 if the car type has already been processed
	 */
	private int countUtiltiyCars(String[] messageFormat, List<String> carList, Car car, RouteLocation rl,
			RouteLocation rld, boolean isPickup) {
		int count = 0;
		// figure out if the user wants to show the car's length
		boolean showLength = showUtilityCarLength(messageFormat);
		// figure out if the user want to show the car's loads
		boolean showLoad = showUtilityCarLoad(messageFormat);
		boolean showLocation = false;
		boolean showDestination = false;
		String[] carType = car.getTypeName().split("-");
		String carAttributes;
		// Note for car pick up: type, id, track name. For set out type, track name, id.
		if (isPickup) {
			carAttributes = carType[0] + car.getRouteLocationId() + splitString(car.getTrackName());
			showDestination = showUtilityCarDestination(messageFormat);
			if (showDestination)
				carAttributes = carAttributes + car.getRouteDestinationId();
		} else {
			// set outs and local moves
			carAttributes = carType[0] + splitString(car.getDestinationTrackName())
					+ car.getRouteDestinationId();
			showLocation = showUtilityCarLocation(messageFormat);
			if (showLocation && car.getTrack() != null)
				carAttributes = carAttributes + car.getRouteLocationId();
		}
		if (showLength)
			carAttributes = carAttributes + car.getLength();
		if (showLoad)
			carAttributes = carAttributes + car.getLoadName();
		// have we already done this car type?
		if (!utilityCarTypes.contains(carAttributes)) {
			utilityCarTypes.add(carAttributes); // don't do this type again
			// determine how many cars of this type
			for (int i = 0; i < carList.size(); i++) {
				Car c = carManager.getById(carList.get(i));
				if (!c.isUtility())
					continue;
				String[] cType = c.getTypeName().split("-");
				if (!cType[0].equals(carType[0]))
					continue;
				if (showLength && !c.getLength().equals(car.getLength()))
					continue;
				if (showLoad && !c.getLoadName().equals(car.getLoadName()))
					continue;
				if (showLocation && !c.getRouteLocationId().equals(car.getRouteLocationId()))
					continue;
				if (showDestination && !c.getRouteDestinationId().equals(car.getRouteDestinationId()))
					continue;
				if (isLocalMove(car) ^ isLocalMove(c))
					continue;
				if (isPickup && c.getRouteLocation() == rl
						&& splitString(c.getTrackName()).equals(splitString(car.getTrackName()))) {
					count++;
				}
				if (!isPickup
						&& c.getRouteDestination() == rl
						&& splitString(c.getDestinationTrackName()).equals(
								splitString(car.getDestinationTrackName()))
						&& c.getRouteDestination().equals(car.getRouteDestination())) {
					count++;
				}
			}
		}
		return count;
	}

	public void clearUtilityCarTypes() {
		utilityCarTypes.clear();
	}

	private boolean showUtilityCarLength(String[] mFormat) {
		return showUtilityCarAttribute(Setup.LENGTH, mFormat);
	}

	private boolean showUtilityCarLoad(String[] mFormat) {
		return showUtilityCarAttribute(Setup.LOAD, mFormat);
	}
	
	private boolean showUtilityCarLocation(String[] mFormat) {
		return showUtilityCarAttribute(Setup.LOCATION, mFormat);
	}
	
	private boolean showUtilityCarDestination(String[] mFormat) {
		return showUtilityCarAttribute(Setup.DESTINATION, mFormat);
	}
	
	private boolean showUtilityCarAttribute(String string, String[] mFormat) {
		for (int i = 0; i < mFormat.length; i++) {
			if (mFormat[i].equals(string))
				return true;
		}
		return false;
	}
	

	/**
	 * Writes a line to the build report file
	 * @param file build report file
	 * @param level print level
	 * @param string string to write
	 */
	protected static void addLine(PrintWriter file, String level, String string) {
		if (log.isDebugEnabled())
			log.debug(string);
		if (file != null) {
			String[] msg = string.split(NEW_LINE);
			for (int i = 0; i < msg.length; i++)
				printLine(file, level, msg[i]);
		}
	}
	
	// only used by build report
	private static void printLine(PrintWriter file, String level, String string) {
		int lineLengthMax = getLineLength(Setup.PORTRAIT, Setup.getBuildReportFontSize());
		if (string.length() > lineLengthMax) {
			String[] s = string.split(SPACE);
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < s.length; i++) {
				if (sb.length() + s[i].length() < lineLengthMax) {
					sb.append(s[i] + SPACE);
				} else {
					file.println(level + "- " + sb.toString());
					sb = new StringBuffer(s[i] + SPACE);
				}
			}
			string = sb.toString();
		}
		file.println(level + "- " + string);
	}

	/**
	 * Used to determine if car is a local move
	 * @param car
	 * @return true if the move is at the same location
	 */
	protected boolean isLocalMove(Car car) {
		if (car.getRouteLocation().equals(car.getRouteDestination()) && car.getTrack() != null)
			return true;
		if (car.getTrain() != null
				&& car.getTrain().isLocalSwitcher()
				&& splitString(car.getRouteLocation().getName()).equals(
						splitString(car.getRouteDestination().getName())) && car.getTrack() != null)
			return true;
		// look for sequential locations
		if (splitString(car.getRouteLocation().getName()).equals(splitString(car.getRouteDestination().getName()))
				&& car.getTrain() != null && car.getTrain().getRoute() != null) {
			Route route = car.getTrain().getRoute();
			List<String> locations = route.getLocationsBySequenceList();
			boolean foundRl = false;
			for (int i=0; i < locations.size(); i++) {
				RouteLocation rl = route.getLocationById(locations.get(i));
				if (foundRl) {
					if (splitString(car.getRouteDestination().getName()).equals(splitString(rl.getName()))) {
						// user can specify the "same" location two more more times in a row
						if (car.getRouteDestination() != rl)
							continue;
						else
							return true;
					} else {
						return false;
					}
				}
				if (car.getRouteLocation().equals(rl)) {
					foundRl = true;					
				}
			}
		}		
		return false;
	}
 
	/**
	 * Writes string to file. No line length wrap or protection.
	 * @param file
	 * @param string
	 */
	protected void addLine(PrintWriter file, String string) {
		if (log.isDebugEnabled()) {
			log.debug(string);
		}
		if (file != null)
			file.println(string);
	}
	
	/**
	 * Writes a string to a file. Checks for string length, and will automatically wrap lines.
	 * @param file
	 * @param string
	 * @param isManifest set true for manifest page orientation, false for switch list orientation
	 */
	protected void newLine(PrintWriter file, String string, boolean isManifest) {
		if (isManifest)
			newLine(file, string, Setup.getManifestOrientation());
		else
			newLine(file, string, Setup.getSwitchListOrientation());
	}

	/**
	 * Writes a string to file.  Checks for string length, and will automatically wrap lines.
	 * @param file
	 * @param string
	 * @param orientation
	 */
	protected void newLine(PrintWriter file, String string, String orientation) {
		String[] s = string.split(NEW_LINE);
		for (int i = 0; i < s.length; i++) {
			makeNewLine(file, s[i], orientation);
		}
	}
	
	private void makeNewLine(PrintWriter file, String string, String orientation) {
		String[] s = string.split(SPACE);
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < s.length; i++) {
			if (checkStringLength(sb.toString() + s[i], orientation, Setup.getFontName(), Setup.getManifestFontSize())) {
				sb.append(s[i] + SPACE);
			} else {
				addLine(file, sb.toString());
				sb = new StringBuffer(s[i] + SPACE);
			}
		}
		addLine(file, sb.toString());
	}

	/**
	 * Adds a blank line to the file.
	 * @param file
	 */
	protected void newLine(PrintWriter file) {
		file.println(BLANK_LINE);
	}

	/**
	 * Splits a string (example-number) as long as the second part of the string is an integer.
	 * 
	 * @param name
	 * @return First half the string.
	 */
	public static String splitString(String name) {
		String[] fullname = name.split("-");
		String parsedName = fullname[0].trim();
		// is the hyphen followed by a number?
		if (fullname.length > 1) {
			try {
				Integer.parseInt(fullname[1]);
			} catch (NumberFormatException e) {
				// no return full name
				parsedName = name;
			}
		}
		return parsedName;
	}
	
	// returns true if there's work at location
	protected boolean isThereWorkAtLocation(List<String> carList, List<String> engList, RouteLocation rl) {
		for (int i = 0; i < carList.size(); i++) {
			Car car = carManager.getById(carList.get(i));
			if (car.getRouteLocation() == rl || car.getRouteDestination() == rl)
				return true;
		}
		for (int i = 0; i < engList.size(); i++) {
			Engine eng = engineManager.getById(engList.get(i));
			if (eng.getRouteLocation() == rl || eng.getRouteDestination() == rl)
				return true;
		}
		return false;
	}
	
	/**
	 * returns true if the train has work at the location
	 * @param train
	 * @param location
	 * @return true if the train has work at the location
	 */
	public static boolean isThereWorkAtLocation(Train train, Location location) {
		CarManager carManager = CarManager.instance();
		List<String> carList = carManager.getByTrainDestinationList(train);
		for (int i = 0; i < carList.size(); i++) {
			Car car = carManager.getById(carList.get(i));
			if ((car.getRouteLocation() != null && car.getTrack() != null && TrainCommon.splitString(
					car.getRouteLocation().getName()).equals(TrainCommon.splitString(location.getName())))
					|| (car.getRouteDestination() != null && TrainCommon.splitString(
							car.getRouteDestination().getName()).equals(
							TrainCommon.splitString(location.getName()))))
				return true;
		}
		EngineManager engineManager = EngineManager.instance();
		List<String> engList = engineManager.getByTrainList(train);
		for (int i = 0; i < engList.size(); i++) {
			Engine eng = engineManager.getById(engList.get(i));
			if ((eng.getRouteLocation() != null && eng.getTrack() != null && TrainCommon.splitString(
					eng.getRouteLocation().getName()).equals(TrainCommon.splitString(location.getName())))
					|| (eng.getRouteDestination() != null && TrainCommon.splitString(
							eng.getRouteDestination().getName()).equals(
							TrainCommon.splitString(location.getName()))))
				return true;
		}
		return false;
	}

	protected void addCarsLocationUnknown(PrintWriter file, boolean isManifest) {
		CarManager cManager = CarManager.instance();
		List<String> cars = cManager.getCarsLocationUnknown();
		if (cars.size() == 0)
			return; // no cars to search for!
		newLine(file);
		newLine(file, Setup.getMiaComment(), isManifest);
		for (int i = 0; i < cars.size(); i++) {
			Car car = cManager.getById(cars.get(i));
			addSearchForCar(file, car);
		}
	}

	private void addSearchForCar(PrintWriter file, Car car) {
		StringBuffer buf = new StringBuffer();
		String[] format = Setup.getMissingCarMessageFormat();
		for (int i = 0; i < format.length; i++) {
			buf.append(getCarAttribute(car, format[i], false, false));
		}
		addLine(file, buf.toString());
	}

	// @param pickup true when rolling stock is being picked up
	private String getEngineAttribute(Engine engine, String attribute, boolean isPickup) {
		if (attribute.equals(Setup.MODEL))
			return " " + tabString(engine.getModel(), Control.max_len_string_attibute);
		if (attribute.equals(Setup.CONSIST))
			return " " + tabString(engine.getConsistName(), Control.max_len_string_attibute);
		return getRollingStockAttribute(engine, attribute, isPickup, false);
	}

	private String getCarAttribute(Car car, String attribute, boolean isPickup, boolean isLocal) {
		if (attribute.equals(Setup.LOAD))
			return (car.isCaboose() || car.isPassenger()) ? tabString("", CarLoads.instance()
					.getCurMaxNameLength() + 1) : " "
					+ tabString(car.getLoadName(), CarLoads.instance().getCurMaxNameLength());
		else if (attribute.equals(Setup.HAZARDOUS))
			return (car.isHazardous() ? " " + Setup.getHazardousMsg() : "");
		else if (attribute.equals(Setup.DROP_COMMENT))
			return " " + car.getDropComment();
		else if (attribute.equals(Setup.PICKUP_COMMENT))
			return " " + car.getPickupComment();
		else if (attribute.equals(Setup.KERNEL))
			return " " + tabString(car.getKernelName(), Control.max_len_string_attibute);
		else if (attribute.equals(Setup.RWE)) {
			if (!car.getReturnWhenEmptyDestName().equals(""))
				return " " + Bundle.getMessage("RWE") + " "
						+ splitString(car.getReturnWhenEmptyDestinationName()) + " ("
						+ splitString(car.getReturnWhenEmptyDestTrackName()) + ")";
			return "";
		} else if (attribute.equals(Setup.FINAL_DEST)) {
			if (!car.getFinalDestinationName().equals(""))
				return " " + TrainManifestText.getStringFinalDestination() + " " + splitString(car.getFinalDestinationName());
			return "";
		}
		return getRollingStockAttribute(car, attribute, isPickup, isLocal);
	}

	private String getRollingStockAttribute(RollingStock rs, String attribute, boolean isPickup,
			boolean isLocal) {
		if (attribute.equals(Setup.NUMBER))
			return " "
					+ tabString(splitString(rs.getNumber()), Control.max_len_string_road_number - 4);
		else if (attribute.equals(Setup.ROAD))
			return " " + tabString(rs.getRoadName(), CarRoads.instance().getCurMaxNameLength());
		else if (attribute.equals(Setup.TYPE)) {
			String[] type = rs.getTypeName().split("-"); // second half of string
														// can be anything
			return " " + tabString(type[0], CarTypes.instance().getCurMaxNameLength());
		} else if (attribute.equals(Setup.LENGTH))
			return " "
					+ tabString(rs.getLength() + LENGTHABV, CarLengths.instance()
							.getCurMaxNameLength());
		else if (attribute.equals(Setup.COLOR))
			return " " + tabString(rs.getColor(), CarColors.instance().getCurMaxNameLength());
		else if (attribute.equals(Setup.LOCATION) && (isPickup || isLocal)) {
			if (rs.getTrack() != null)
				return " " + TrainManifestText.getStringFrom() + " " + splitString(rs.getTrackName());
			return "";
		} else if (attribute.equals(Setup.LOCATION) && !isPickup && !isLocal)
			return " " + TrainManifestText.getStringFrom() + " " + splitString(rs.getLocationName());
		else if (attribute.equals(Setup.DESTINATION) && isPickup) {
			if (Setup.isTabEnabled())
				return " " + TrainManifestText.getStringDest() + " " + splitString(rs.getDestinationName());
			else
				return " " + TrainManifestText.getStringDestination() + " " + splitString(rs.getDestinationName());
		} else if (attribute.equals(Setup.DESTINATION) && !isPickup)
			return " " + TrainManifestText.getStringTo() + " " + splitString(rs.getDestinationTrackName());
		else if (attribute.equals(Setup.DEST_TRACK))
			return " " + TrainManifestText.getStringDest() + " " + splitString(rs.getDestinationName()) + ", "
					+ splitString(rs.getDestinationTrackName());
		else if (attribute.equals(Setup.OWNER))
			return " " + tabString(rs.getOwner(), CarOwners.instance().getCurMaxNameLength());
		else if (attribute.equals(Setup.COMMENT))
			return " " + rs.getComment();
		else if (attribute.equals(Setup.NONE))
			return "";
		// the three utility attributes that don't get printed but need to be tabbed out
		else if (attribute.equals(Setup.NO_NUMBER))
			return " " + tabString("", Control.max_len_string_road_number - 8); // (-4 -4) for utility quantity field
		else if (attribute.equals(Setup.NO_ROAD))
			return " " + tabString("", CarRoads.instance().getCurMaxNameLength());
		else if (attribute.equals(Setup.NO_COLOR))
			return " " + tabString("", CarColors.instance().getCurMaxNameLength());
		// the three truncated manifest attributes
		else if (attribute.equals(Setup.NO_DESTINATION) || attribute.equals(Setup.NO_DEST_TRACK)
				|| attribute.equals(Setup.NO_LOCATION))
			return "";
		// tab?
		else if (attribute.equals(Setup.TAB))
			return " " + tabString("", Setup.getTabLength());
		return " (" + Bundle.getMessage("ErrorPrintOptions") + ") "; // maybe user changed locale
	}

	public static String getDate(boolean isModelYear) {
		Calendar calendar = Calendar.getInstance();

		String year = Setup.getYearModeled();
		if (year.equals("") || !isModelYear)
			year = Integer.toString(calendar.get(Calendar.YEAR));
		year = year.trim();

		// Use 24 hour clock
		int hour = calendar.get(Calendar.HOUR_OF_DAY);

		if (Setup.is12hrFormatEnabled()) {
			hour = calendar.get(Calendar.HOUR);
			if (hour == 0)
				hour = 12;
		}

		String h = Integer.toString(hour);
		if (hour < 10)
			h = "0" + Integer.toString(hour);

		int minute = calendar.get(Calendar.MINUTE);
		String m = Integer.toString(minute);
		if (minute < 10)
			m = "0" + Integer.toString(minute);

		// AM_PM field
		String AM_PM = "";
		if (Setup.is12hrFormatEnabled()) {
			AM_PM = (calendar.get(Calendar.AM_PM) == Calendar.AM) ? Bundle.getMessage("AM") : Bundle.getMessage("PM");
		}

		// Java 1.6 methods calendar.getDisplayName(Calendar.MONTH,
		// Calendar.LONG, Locale.getDefault()
		// Java 1.6 methods calendar.getDisplayName(Calendar.AM_PM,
		// Calendar.LONG, Locale.getDefault())
		String date = calendar.get(Calendar.MONTH) + 1 + "/" + calendar.get(Calendar.DAY_OF_MONTH)
				+ "/" + year + " " + h + ":" + m + " " + AM_PM;
		return date;
	}

	protected static String tabString(String s, int fieldSize) {
		if (!Setup.isTabEnabled())
			return s;
		s = padString(s, fieldSize);
		if (s.length() > fieldSize)
			s = s.substring(0, fieldSize);
		return s;
	}
	
	protected static String padString(String s, int fieldSize) {
		StringBuffer buf = new StringBuffer(s);
		while (buf.length() < fieldSize) {
			buf.append(" ");
		}
		return buf.toString();
	}
	
	// used by manifests
	protected int getLineLength(String orientation) {
		return getLineLength(orientation, Setup.getManifestFontSize());
	}
	
	private static int getLineLength(String orientation, int fontSize) {
		// page size has been adjusted to account for margins of .5
		Dimension pagesize = new Dimension(540, 792); // Portrait
		if (orientation.equals(Setup.LANDSCAPE))
			pagesize = new Dimension(720, 612);
		if (orientation.equals(Setup.HANDHELD))
			pagesize = new Dimension(206, 792);
		// Metrics don't always work for the various font names, so use
		// Monospaced
		Font font = new Font("Monospaced", Font.PLAIN, fontSize); // NOI18N
		JLabel label = new JLabel();
		FontMetrics metrics = label.getFontMetrics(font);
		int charwidth = metrics.charWidth('m');

		// compute lines and columns within margins
		return pagesize.width / charwidth;
	}
	
	/**
	 * Checks to see if the the string fits on the page.
	 * @param string
	 * @param orientation
	 * @param fontName
	 * @param fontSize
	 * @return true if string length is longer than page width
	 */
	private boolean checkStringLength(String string, String orientation, String fontName, int fontSize) {
		// page size has been adjusted to account for margins of .5
		Dimension pagesize = new Dimension(540, 792); // Portrait
		if (orientation.equals(Setup.LANDSCAPE))
			pagesize = new Dimension(720, 612);
		if (orientation.equals(Setup.HANDHELD))
			pagesize = new Dimension(206, 792);
		Font font = new Font(fontName, Font.PLAIN, fontSize); // NOI18N
		JLabel label = new JLabel();
		FontMetrics metrics = label.getFontMetrics(font);
		int stringWidth = metrics.stringWidth(string);
		return stringWidth < pagesize.width;
	}
	
	/**
	 * Produces a string using commas and spaces between the strings provided in the array
	 * @param array
	 * @return formated string using commas and spaces
	 */
	public static String formatStringToCommaSeparated(String[] array) {
		StringBuffer sbuf = new StringBuffer("");
		for (int i = 0; i < array.length; i++) {
			sbuf = sbuf.append(array[i] + ", ");
		}
		if (sbuf.length() > 2)
			sbuf.setLength(sbuf.length() - 2); // remove trailing separators
		return sbuf.toString();
	}

	static Logger log = LoggerFactory.getLogger(TrainCommon.class.getName());
}
