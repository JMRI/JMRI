// TrainSwitchLists.java

package jmri.jmrit.operations.trains;

import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.swing.JLabel;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
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
import jmri.jmrit.operations.rollingstock.engines.EngineModels;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common routines for trains
 * 
 * @author Daniel Boudreau (C) Copyright 2008, 2009, 2010, 2011, 2012, 2013
 * @version $Revision: 1 $
 */
public class TrainCommon {

	private static final String LENGTHABV = Setup.LENGTHABV; // Length symbol
	protected static final String TAB = "    "; // NOI18N
	protected static final String NEW_LINE = "\n"; // NOI18N
	protected static final String SPACE = " ";
	protected static final String BLANK_LINE = " ";
	protected static final String HORIZONTAL_LINE_CHAR = "-";
	protected static final String VERTICAL_LINE_CHAR = "|";
	protected static final String ARROW = ">";

	protected static final boolean PICKUP = true;
	protected static final boolean LOCAL = true;

	CarManager carManager = CarManager.instance();
	EngineManager engineManager = EngineManager.instance();
	LocationManager locationManager = LocationManager.instance();

	// for manifests
	protected int cars = 0;
	protected int emptyCars = 0;
	protected boolean newWork = false;

	// for switch lists
	protected boolean pickupCars;
	protected boolean dropCars;

	protected void blockLocosTwoColumn(PrintWriter file, List<Engine> engineList, RouteLocation rl, boolean isManifest) {
		if (isThereWorkAtLocation(null, engineList, rl))
			printEngineHeader(file, isManifest);
		int lineLength = getLineLength(isManifest);
		for (Engine engine : engineList) {
			if (engine.getRouteLocation() == rl && !engine.getTrackName().equals("")) {
				String s = padAndTruncateString(pickupEngine(engine).trim(), lineLength / 2, true) + VERTICAL_LINE_CHAR;
				newLine(file, s, isManifest);
			}
			if (engine.getRouteDestination() == rl) {
				String s = padAndTruncateString(tabString("", lineLength / 2, true) + VERTICAL_LINE_CHAR
						+ dropEngine(engine).trim(), lineLength, true);
				newLine(file, s, isManifest);
			}
		}
	}

	/**
	 * Adds a list of locomotive pick ups for the route location to the output file
	 * 
	 * @param file
	 * @param engineList
	 * @param rl
         * @param isManifest
	 */
	protected void pickupEngines(PrintWriter file, List<Engine> engineList, RouteLocation rl, boolean isManifest) {
		boolean printHeader = Setup.isPrintHeadersEnabled();
		for (Engine engine : engineList) {
			if (engine.getRouteLocation() == rl && !engine.getTrackName().equals("")) {
				if (printHeader) {
					printPickupEngineHeader(file, isManifest);
					printHeader = false;
				}
				pickupEngine(file, engine, isManifest);
			}
		}
	}

	private void pickupEngine(PrintWriter file, Engine engine, boolean isManifest) {
		StringBuffer buf = new StringBuffer(padAndTruncateString(Setup.getPickupEnginePrefix(), Setup
				.getManifestPrefixLength()));
		String[] format = Setup.getPickupEngineMessageFormat();
		for (String attribute : format) {
			String s = getEngineAttribute(engine, attribute, PICKUP);
			if (!checkStringLength(buf.toString() + s, isManifest)) {
				addLine(file, buf.toString());
				buf = new StringBuffer(TAB);
			}
			buf.append(s);
		}
		addLine(file, buf.toString());
	}

	/**
	 * Adds a list of locomotive drops for the route location to the output file
	 * 
	 * @param file
	 * @param engineList
	 * @param rl
         * @param isManifest
	 */
	protected void dropEngines(PrintWriter file, List<Engine> engineList, RouteLocation rl, boolean isManifest) {
		boolean printHeader = Setup.isPrintHeadersEnabled();
		for (Engine engine : engineList) {
			if (engine.getRouteDestination() == rl) {
				if (printHeader) {
					printDropEngineHeader(file, isManifest);
					printHeader = false;
				}
				dropEngine(file, engine, isManifest);
			}
		}
	}

	private void dropEngine(PrintWriter file, Engine engine, boolean isManifest) {
		StringBuffer buf = new StringBuffer(padAndTruncateString(Setup.getDropEnginePrefix(), Setup
				.getManifestPrefixLength()));
		String[] format = Setup.getDropEngineMessageFormat();
		for (String attribute : format) {
			String s = getEngineAttribute(engine, attribute, !PICKUP);
			if (!checkStringLength(buf.toString() + s, isManifest)) {
				addLine(file, buf.toString());
				buf = new StringBuffer(TAB);
			}
			buf.append(s);
		}
		addLine(file, buf.toString());
	}

	/**
	 * Returns the pick up string for a loco. Useful for frames like the train conductor and yardmaster.
	 * 
	 * @param engine
	 * @return engine pick up string
	 */
	public String pickupEngine(Engine engine) {
		StringBuilder builder = new StringBuilder();
		for (String attribute : Setup.getPickupEngineMessageFormat()) {
			builder.append(getEngineAttribute(engine, attribute, PICKUP));
		}
		return builder.toString();
	}

	/**
	 * Returns the drop string for a loco. Useful for frames like the train conductor and yardmaster.
	 * 
	 * @param engine
	 * @return engine drop string
	 */
	public String dropEngine(Engine engine) {
		StringBuilder builder = new StringBuilder();
		for (String attribute : Setup.getDropEngineMessageFormat()) {
			builder.append(getEngineAttribute(engine, attribute, !PICKUP));
		}
		return builder.toString();
	}

	/**
	 * Block cars by track, then pick up and set out for each location in a train's route.
	 */
	protected void blockCarsByTrack(PrintWriter file, Train train, List<Car> carList, List<RouteLocation> routeList,
			RouteLocation rl, int r, boolean printHeader, boolean isManifest) {
		boolean printPickupHeader = printHeader;
		boolean printSetoutHeader = printHeader;
		boolean printLocalMoveHeader = printHeader;
		List<Track> tracks = rl.getLocation().getTrackByNameList(null);
		List<String> trackNames = new ArrayList<String>();
		clearUtilityCarTypes(); // list utility cars by quantity
		for (Track track : tracks) {
			if (trackNames.contains(splitString(track.getName())))
				continue;
			trackNames.add(splitString(track.getName())); // use a track name once
			// block pick up cars by destination
			for (int j = r; j < routeList.size(); j++) {
				RouteLocation rld = routeList.get(j);
				for (Car car : carList) {
					if (Setup.isSortByTrackEnabled()
							&& !splitString(track.getName()).equals(splitString(car.getTrackName())))
						continue;
					// note that a car in train doesn't have a track assignment
					if (car.getRouteLocation() == rl && car.getTrack() != null && car.getRouteDestination() == rld) {
						// determine if header is to be printed
						if (printPickupHeader && !isLocalMove(car)) {
							printPickupCarHeader(file, isManifest);
							printPickupHeader = false;
							// check to see if set out header is needed
							if (getPickupCarHeader(isManifest).equals(getDropCarHeader(isManifest)))
								printSetoutHeader = false;
							if (getPickupCarHeader(isManifest).equals(getLocalMoveHeader(isManifest)))
								printLocalMoveHeader = false;
						}
						if (car.isUtility())
							pickupUtilityCars(file, carList, car, rl, rld, isManifest);
						// use truncated format if there's a switch list
						else if (isManifest && Setup.isTruncateManifestEnabled()
								&& rl.getLocation().isSwitchListEnabled())
							pickUpCarTruncated(file, car, isManifest);
						else
							pickUpCar(file, car, isManifest);
						pickupCars = true;
						cars++;
						newWork = true;
						if (CarLoads.instance().getLoadType(car.getTypeName(), car.getLoadName()).equals(
								CarLoad.LOAD_TYPE_EMPTY))
							emptyCars++;
					}
				}
			}
			// now do set outs and local moves
			for (Car car : carList) {
				if (Setup.isSortByTrackEnabled()) {
					// sort local moves by the car's current track name
					if (isLocalMove(car)) {
						if (!splitString(track.getName()).equals(splitString(car.getTrackName())))
							continue;
					} else if (!splitString(track.getName()).equals(splitString(car.getDestinationTrackName()))) {
						continue;
					}
				}
				if (car.getRouteDestination() == rl && car.getDestinationTrack() != null) {
					if (printSetoutHeader && !isLocalMove(car)) {
						printDropCarHeader(file, isManifest);
						printSetoutHeader = false;
						if (getPickupCarHeader(isManifest).equals(getDropCarHeader(isManifest)))
							printPickupHeader = false;
						if (getDropCarHeader(isManifest).equals(getLocalMoveHeader(isManifest)))
							printLocalMoveHeader = false;
					}
					if (printLocalMoveHeader && isLocalMove(car)) {
						printLocalCarMoveHeader(file, isManifest);
						printLocalMoveHeader = false;
						if (getPickupCarHeader(isManifest).equals(getLocalMoveHeader(isManifest)))
							printPickupHeader = false;
						if (getDropCarHeader(isManifest).equals(getLocalMoveHeader(isManifest)))
							printSetoutHeader = false;
					}

					if (car.isUtility())
						setoutUtilityCars(file, carList, car, rl, isManifest);
					// use truncated format if there's a switch list
					else if (isManifest && Setup.isTruncateManifestEnabled() && rl.getLocation().isSwitchListEnabled())
						truncatedDropCar(file, car, isManifest);
					else
						dropCar(file, car, isManifest);
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
	protected void blockCarsByTrackTwoColumn(PrintWriter file, Train train, List<Car> carList,
			List<RouteLocation> routeList, RouteLocation rl, int r, boolean printHeader, boolean isManifest) {
		index = 0;
		int lineLength = getLineLength(isManifest);
		List<Track> tracks = rl.getLocation().getTrackByNameList(null);
		List<String> trackNames = new ArrayList<String>();
		clearUtilityCarTypes(); // list utility cars by quantity
		if (printHeader)
			printCarHeader(file, isManifest);
		for (Track track : tracks) {
			if (trackNames.contains(splitString(track.getName())))
				continue;
			trackNames.add(splitString(track.getName())); // use a track name once
			// block car pick ups by destination
			for (int j = r; j < routeList.size(); j++) {
				RouteLocation rld = routeList.get(j);
				for (int k = 0; k < carList.size(); k++) {
					Car car = carList.get(k);
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
						s = padAndTruncateString(s, lineLength / 2, true);
						if (isLocalMove(car)) {
							String sl = appendSetoutString(s, carList, car.getRouteDestination(), car, isManifest);
							// check for utility car, and local route with two or more locations
							if (!sl.equals(s)) {
								s = sl;
								carList.remove(car); // done with this car, remove from list
								k--;
							}
						} else {
							s = appendSetoutString(s, carList, rl, true, isManifest);
						}
						addLine(file, s);
					}
				}
			}
			if (!Setup.isSortByTrackEnabled())
				break; // done
		}
		while (index < carList.size()) {
			String s = padString("", lineLength / 2);
			s = appendSetoutString(s, carList, rl, false, isManifest);
			String test = s.trim();
			if (test.length() > 1) // null line contains |
				addLine(file, s);
		}
	}

	int index = 0;

	private String appendSetoutString(String s, List<Car> carList, RouteLocation rl, boolean local, boolean isManfest) {
		while (index < carList.size()) {
			Car car = carList.get(index++);
			if (local && isLocalMove(car))
				continue; // skip local moves
			// car list is already sorted by destination track
			if (car.getRouteDestination() == rl) {
				String so = appendSetoutString(s, carList, rl, car, isManfest);
				// check for utility car
				if (!so.equals(s))
					return so;
			}
		}
		return s + VERTICAL_LINE_CHAR;
	}

	private String appendSetoutString(String s, List<Car> carList, RouteLocation rl, Car car, boolean isManifest) {
		dropCars = true;
		cars--;
		newWork = true;
		if (CarLoads.instance().getLoadType(car.getTypeName(), car.getLoadName()).equals(CarLoad.LOAD_TYPE_EMPTY))
			emptyCars--;
		String newString;
		// use truncated format if there's a switch list
		// else if (Setup.isTruncateManifestEnabled() && rl.getLocation().isSwitchListEnabled())
		// truncatedDropCar(file, car);

		if (isLocalMove(car))
			newString = s + ARROW; // NOI18N
		else
			newString = s + VERTICAL_LINE_CHAR;

		if (car.isUtility()) {
			String so = setoutUtilityCars(carList, car, rl, false, isManifest);
			if (so == null)
				return s; // no changes to the input string
			newString = newString + so.trim();
		} else {
			newString = newString + dropCar(car, isManifest).trim();
		}
		return padAndTruncateString(newString, getLineLength(isManifest));
	}

	/**
	 * Adds the car's pick up string to the output file using the truncated manifest format
	 * 
	 * @param file
	 * @param car
	 */
	protected void pickUpCarTruncated(PrintWriter file, Car car, boolean isManifest) {
		pickUpCar(file, car, new StringBuffer(padAndTruncateString(Setup.getPickupCarPrefix(), Setup
				.getManifestPrefixLength())), Setup.getTruncatedPickupManifestMessageFormat(), isManifest);
	}

	/**
	 * Adds the car's pick up string to the output file using the manifest or switch list format
	 * 
	 * @param file
	 * @param car
	 */
	protected void pickUpCar(PrintWriter file, Car car, boolean isManifest) {
		if (isManifest)
			pickUpCar(file, car, new StringBuffer(padAndTruncateString(Setup.getPickupCarPrefix(), Setup
					.getManifestPrefixLength())), Setup.getPickupCarMessageFormat(), isManifest);
		else
			pickUpCar(file, car, new StringBuffer(padAndTruncateString(Setup.getSwitchListPickupCarPrefix(), Setup
					.getSwitchListPrefixLength())), Setup.getSwitchListPickupCarMessageFormat(), isManifest);
	}

	private void pickUpCar(PrintWriter file, Car car, StringBuffer buf, String[] format, boolean isManifest) {
		if (isLocalMove(car))
			return; // print nothing local move, see dropCar
		for (String attribute : format) {
			String s = getCarAttribute(car, attribute, PICKUP, !LOCAL);
			if (!checkStringLength(buf.toString() + s, isManifest)) {
				addLine(file, buf.toString());
				buf = new StringBuffer(TAB);
			}
			buf.append(s);
		}
		String s = buf.toString();
		if (s.trim().length() != 0)
			addLine(file, s);
	}

	/**
	 * Returns the pick up car string. Useful for frames like train conductor and yardmaster.
	 * 
	 * @param isManifest
	 *            when true use manifest format, when false use switch list format
	 * @param car
	 * @return pick up car string
	 */
	public String pickupCar(Car car, boolean isManifest) {
		StringBuffer buf = new StringBuffer();
		String[] format = Setup.getPickupCarMessageFormat();
		if (!isManifest)
			format = Setup.getSwitchListPickupCarMessageFormat();
		for (String attribute : format) {
			String s = getCarAttribute(car, attribute, PICKUP, !LOCAL);
			buf.append(s);
		}
		return buf.toString();
	}

	/**
	 * Adds the car's set out string to the output file using the truncated manifest format. Does not print out local
	 * moves. Local moves are only shown on the switch list for that location.
	 * 
	 * @param file
	 * @param car
	 */
	protected void truncatedDropCar(PrintWriter file, Car car, boolean isManifest) {
		// local move?
		if (isLocalMove(car))
			return; // yes, don't print local moves on train manifest
		dropCar(file, car, new StringBuffer(Setup.getDropCarPrefix()), Setup.getTruncatedSetoutManifestMessageFormat(),
				false, isManifest);
	}

	/**
	 * Adds the car's set out string to the output file using the manifest or switch list format
	 * 
	 * @param file
	 * @param car
	 * @param isManifest
	 */
	protected void dropCar(PrintWriter file, Car car, boolean isManifest) {
		if (isManifest) {
			StringBuffer buf = new StringBuffer(padAndTruncateString(Setup.getDropCarPrefix(), Setup
					.getManifestPrefixLength()));
			String[] format = Setup.getDropCarMessageFormat();
			boolean isLocal = isLocalMove(car);
			if (isLocal) {
				buf = new StringBuffer(padAndTruncateString(Setup.getLocalPrefix(), Setup.getManifestPrefixLength()));
				format = Setup.getLocalMessageFormat();
			}
			dropCar(file, car, buf, format, isLocal, isManifest);
		} else {
			StringBuffer buf = new StringBuffer(padAndTruncateString(Setup.getSwitchListDropCarPrefix(), Setup
					.getSwitchListPrefixLength()));
			String[] format = Setup.getSwitchListDropCarMessageFormat();
			boolean isLocal = isLocalMove(car);
			if (isLocal) {
				buf = new StringBuffer(padAndTruncateString(Setup.getSwitchListLocalPrefix(), Setup
						.getSwitchListPrefixLength()));
				format = Setup.getSwitchListLocalMessageFormat();
			}
			dropCar(file, car, buf, format, isLocal, isManifest);
		}
	}

	private void dropCar(PrintWriter file, Car car, StringBuffer buf, String[] format, boolean isLocal,
			boolean isManifest) {
		for (String attribute : format) {
			String s = getCarAttribute(car, attribute, !PICKUP, isLocal);
			if (!checkStringLength(buf.toString() + s, isManifest)) {
				addLine(file, buf.toString());
				buf = new StringBuffer(TAB);
			}
			buf.append(s);
		}
		String s = buf.toString();
		if (s.trim().length() != 0)
			addLine(file, s);
	}

	/**
	 * Returns the drop car string. Useful for frames like train conductor and yardmaster.
	 * 
	 * @param car
	 * @param isManifest
	 *            when true use manifest format, when false use switch list format
	 * @return drop car string
	 */
	public String dropCar(Car car, boolean isManifest) {
		StringBuffer buf = new StringBuffer();
		String[] format = Setup.getDropCarMessageFormat();
		if (!isManifest)
			format = Setup.getSwitchListDropCarMessageFormat();
		for (String attribute : format) {
			// TODO the Setup.Location doesn't work correctly for the conductor
			// window due to the fact that the car can be in the train and not
			// at its starting location.
			// Therefore we use the local true to disable it.
			String s = getCarAttribute(car, attribute, !PICKUP, LOCAL);
			buf.append(s);
		}
		return buf.toString();
	}

	/**
	 * Returns the move car string. Useful for frames like train conductor and yardmaster.
	 * 
	 * @param isManifest
	 *            when true use manifest format, when false use switch list format
	 * @param car
	 * @return move car string
	 */
	public String moveCar(Car car, boolean isManifest) {
		StringBuffer buf = new StringBuffer();
		String[] format = Setup.getLocalMessageFormat();
		if (!isManifest)
			format = Setup.getSwitchListLocalMessageFormat();
		for (String attribute : format) {
			String s = getCarAttribute(car, attribute, !PICKUP, LOCAL);
			buf.append(s);
		}
		return buf.toString();
	}

	List<String> utilityCarTypes = new ArrayList<String>();
	private static final int utilityCarCountFieldSize = 3;

	/**
	 * Add a list of utility cars scheduled for pick up from the route location to the output file. The cars are blocked
	 * by destination.
	 * 
	 * @param file
	 * @param carList
	 * @param car
	 * @param rl
	 * @param rld
	 * @param isManifest
	 */
	protected void pickupUtilityCars(PrintWriter file, List<Car> carList, Car car, RouteLocation rl, RouteLocation rld,
			boolean isManifest) {
		// list utility cars by type, track, length, and load
		String[] format = Setup.getPickupUtilityCarMessageFormat();
		if (!isManifest)
			format = Setup.getSwitchListPickupUtilityCarMessageFormat();
		int count = countUtilityCars(format, carList, car, rl, rld, PICKUP);
		if (count == 0)
			return; // already printed out this car type
		pickUpCar(file, car, new StringBuffer(padAndTruncateString(Setup.getPickupCarPrefix(), Setup
				.getManifestPrefixLength())
				+ " " + padString(Integer.toString(count), utilityCarCountFieldSize)), format, isManifest);
	}

	/**
	 * Add a list of utility cars scheduled for drop at the route location to the output file.
	 * 
	 * @param file
	 * @param carList
	 * @param car
	 * @param rl
	 * @param isManifest
	 */
	protected void setoutUtilityCars(PrintWriter file, List<Car> carList, Car car, RouteLocation rl, boolean isManifest) {
		boolean isLocal = isLocalMove(car);
		StringBuffer buf = new StringBuffer(padAndTruncateString(Setup.getDropCarPrefix(), Setup
				.getManifestPrefixLength()));
		String[] format = Setup.getSetoutUtilityCarMessageFormat();
		if (isLocal && isManifest) {
			buf = new StringBuffer(padAndTruncateString(Setup.getLocalPrefix(), Setup.getManifestPrefixLength()));
			format = Setup.getLocalUtilityCarMessageFormat();
		} else if (isLocal && !isManifest) {
			buf = new StringBuffer(padAndTruncateString(Setup.getSwitchListLocalPrefix(), Setup
					.getSwitchListPrefixLength()));
			format = Setup.getSwitchListLocalUtilityCarMessageFormat();
		} else if (!isLocal && !isManifest) {
			buf = new StringBuffer(padAndTruncateString(Setup.getSwitchListDropCarPrefix(), Setup
					.getSwitchListPrefixLength()));
			format = Setup.getSwitchListSetoutUtilityCarMessageFormat();
		}
		int count = countUtilityCars(format, carList, car, rl, null, !PICKUP);
		if (count == 0)
			return; // already printed out this car type
		buf.append(" " + padString(Integer.toString(count), utilityCarCountFieldSize));
		dropCar(file, car, buf, format, isLocal, isManifest);
	}

	public String pickupUtilityCars(List<Car> carList, Car car, RouteLocation rl, RouteLocation rld, boolean isManifest) {
		int count = countPickupUtilityCars(carList, car, rl, rld, isManifest);
		if (count == 0)
			return null;
		String[] format = Setup.getPickupUtilityCarMessageFormat();
		if (!isManifest)
			format = Setup.getSwitchListPickupUtilityCarMessageFormat();
		StringBuffer buf = new StringBuffer(" " + padString(Integer.toString(count), utilityCarCountFieldSize));
		for (String attribute : format) {
			String s = getCarAttribute(car, attribute, PICKUP, !LOCAL);
			buf.append(s);
		}
		return buf.toString();
	}

	public int countPickupUtilityCars(List<Car> carList, Car car, RouteLocation rl, RouteLocation rld,
			boolean isManifest) {
		// list utility cars by type, track, length, and load
		String[] format = Setup.getPickupUtilityCarMessageFormat();
		if (!isManifest)
			format = Setup.getSwitchListPickupUtilityCarMessageFormat();
		return countUtilityCars(format, carList, car, rl, rld, PICKUP);
	}

	public String setoutUtilityCars(List<Car> carList, Car car, RouteLocation rl, boolean isLocal, boolean isManifest) {
		// list utility cars by type, track, length, and load
		String[] format = Setup.getSetoutUtilityCarMessageFormat();
		if (isLocal && isManifest) {
			format = Setup.getLocalUtilityCarMessageFormat();
		} else if (isLocal && !isManifest) {
			format = Setup.getSwitchListLocalUtilityCarMessageFormat();
		} else if (!isLocal && !isManifest) {
			format = Setup.getSwitchListSetoutUtilityCarMessageFormat();
		}
		int count = countUtilityCars(format, carList, car, rl, null, !PICKUP);
		if (count == 0)
			return null;
		StringBuffer buf = new StringBuffer(" " + padString(Integer.toString(count), utilityCarCountFieldSize));
		for (String attribute : format) {
			// TODO the Setup.Location doesn't work correctly for the conductor
			// window, therefore we use the local true to disable it.
			String s = getCarAttribute(car, attribute, !PICKUP, LOCAL);
			buf.append(s);
		}
		return buf.toString();
	}

	public int countSetoutUtilityCars(List<Car> carList, Car car, RouteLocation rl, boolean isLocal, boolean isManifest) {
		// list utility cars by type, track, length, and load
		String[] format = Setup.getSetoutUtilityCarMessageFormat();
		if (isLocal && isManifest) {
			format = Setup.getLocalUtilityCarMessageFormat();
		} else if (isLocal && !isManifest) {
			format = Setup.getSwitchListLocalUtilityCarMessageFormat();
		} else if (!isLocal && !isManifest) {
			format = Setup.getSwitchListSetoutUtilityCarMessageFormat();
		}
		return countUtilityCars(format, carList, car, rl, null, !PICKUP);
	}

	/**
	 * Scans the car list for utility cars that have the same attributes as the car provided. Returns 0 if this car type
	 * has already been processed, otherwise the number of cars with the same attribute.
	 * 
	 * @param format
	 * @param carList
	 * @param car
	 * @param rl
	 * @param rld
	 * @param isPickup
	 * @return 0 if the car type has already been processed
	 */
	protected int countUtilityCars(String[] format, List<Car> carList, Car car, RouteLocation rl, RouteLocation rld,
			boolean isPickup) {
		int count = 0;
		// figure out if the user wants to show the car's length
		boolean showLength = showUtilityCarLength(format);
		// figure out if the user want to show the car's loads
		boolean showLoad = showUtilityCarLoad(format);
		boolean showLocation = false;
		boolean showDestination = false;
		String[] carType = car.getTypeName().split("-");
		String carAttributes;
		// Note for car pick up: type, id, track name. For set out type, track name, id.
		if (isPickup) {
			carAttributes = carType[0] + car.getRouteLocationId() + splitString(car.getTrackName());
			showDestination = showUtilityCarDestination(format);
			if (showDestination)
				carAttributes = carAttributes + car.getRouteDestinationId();
		} else {
			// set outs and local moves
			carAttributes = carType[0] + splitString(car.getDestinationTrackName()) + car.getRouteDestinationId();
			showLocation = showUtilityCarLocation(format);
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
			for (Car c : carList) {
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
				if (!isPickup && c.getRouteDestination() == rl
						&& splitString(c.getDestinationTrackName()).equals(splitString(car.getDestinationTrackName()))
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
	 * 
	 * @param file
	 *            build report file
	 * @param level
	 *            print level
	 * @param string
	 *            string to write
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
		int lineLengthMax = getLineLength(Setup.PORTRAIT, Setup.getBuildReportFontSize(), Setup.MONOSPACED);
		if (string.length() > lineLengthMax) {
			String[] words = string.split(SPACE);
			StringBuffer sb = new StringBuffer();
			for (String word : words) {
				if (sb.length() + word.length() < lineLengthMax) {
					sb.append(word + SPACE);
				} else {
					file.println(level + "- " + sb.toString());
					sb = new StringBuffer(word + SPACE);
				}
			}
			string = sb.toString();
		}
		file.println(level + "- " + string);
	}

	/**
	 * Used to determine if car is a local move
	 * 
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
		// look for sequential locations with the "same" name
		if (splitString(car.getRouteLocation().getName()).equals(splitString(car.getRouteDestination().getName()))
				&& car.getTrain() != null && car.getTrain().getRoute() != null) {
			boolean foundRl = false;
			for (RouteLocation rl : car.getTrain().getRoute().getLocationsBySequenceList()) {
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
	 * 
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
	 * 
	 * @param file
	 * @param string
	 * @param isManifest
	 *            set true for manifest page orientation, false for switch list orientation
	 */
	protected void newLine(PrintWriter file, String string, boolean isManifest) {
		String[] words = string.split(SPACE);
		StringBuffer sb = new StringBuffer();
		for (String word : words) {
			if (checkStringLength(sb.toString() + word, isManifest)) {
				sb.append(word + SPACE);
			} else {
				sb.setLength(sb.length() - 1); // remove last space added to string
				addLine(file, sb.toString());
				sb = new StringBuffer(word + SPACE);
			}
		}
		if (sb.length() > 0)
			sb.setLength(sb.length() - 1); // remove last space added to string
		addLine(file, sb.toString());
	}

	/**
	 * Adds a blank line to the file.
	 * 
	 * @param file
	 */
	protected void newLine(PrintWriter file) {
		file.println(BLANK_LINE);
	}

	/**
	 * Splits a string (example-number) as long as the second part of the string is an integer or if the first character
	 * after the hyphen is a left parenthesis "(".
	 * 
	 * @param name
	 * @return First half the string.
	 */
	public static String splitString(String name) {
		String[] fullname = name.split("-");
		String parsedName = fullname[0].trim();
		// is the hyphen followed by a number or left parenthesis?
		if (fullname.length > 1 && !fullname[1].startsWith("(")) {
			try {
				Integer.parseInt(fullname[1]);
			} catch (NumberFormatException e) {
				// no return full name
				parsedName = name.trim();
			}
		}
		return parsedName;
	}

	// returns true if there's work at location
	protected boolean isThereWorkAtLocation(List<Car> carList, List<Engine> engList, RouteLocation rl) {
		if (carList != null)
			for (Car car : carList) {
				if (car.getRouteLocation() == rl || car.getRouteDestination() == rl)
					return true;
			}
		if (engList != null)
			for (Engine eng : engList) {
				if (eng.getRouteLocation() == rl || eng.getRouteDestination() == rl)
					return true;
			}
		return false;
	}

	/**
	 * returns true if the train has work at the location
	 * 
	 * @param train
	 * @param location
	 * @return true if the train has work at the location
	 */
	public static boolean isThereWorkAtLocation(Train train, Location location) {
		if (isThereWorkAtLocation(train, location, CarManager.instance().getList(train)))
			return true;
		if (isThereWorkAtLocation(train, location, EngineManager.instance().getList(train)))
			return true;
		return false;
	}

	private static boolean isThereWorkAtLocation(Train train, Location location, List<RollingStock> list) {
		for (RollingStock rs : list) {
			if ((rs.getRouteLocation() != null && rs.getTrack() != null && TrainCommon.splitString(
					rs.getRouteLocation().getName()).equals(TrainCommon.splitString(location.getName())))
					|| (rs.getRouteDestination() != null && TrainCommon.splitString(rs.getRouteDestination().getName())
							.equals(TrainCommon.splitString(location.getName()))))
				return true;
		}
		return false;
	}

	protected void addCarsLocationUnknown(PrintWriter file, boolean isManifest) {
		CarManager carManager = CarManager.instance();
		List<Car> cars = carManager.getCarsLocationUnknown();
		if (cars.size() == 0)
			return; // no cars to search for!
		newLine(file);
		newLine(file, Setup.getMiaComment(), isManifest);
		for (Car car : cars)
			addSearchForCar(file, car);
	}

	private void addSearchForCar(PrintWriter file, Car car) {
		StringBuffer buf = new StringBuffer();
		String[] format = Setup.getMissingCarMessageFormat();
		for (String attribute : format) {
			buf.append(getCarAttribute(car, attribute, false, false));
		}
		addLine(file, buf.toString());
	}

	// @param isPickup true when rolling stock is being picked up
	private String getEngineAttribute(Engine engine, String attribute, boolean isPickup) {
		if (attribute.equals(Setup.MODEL))
			return " " + padAndTruncateString(engine.getModel(), EngineModels.instance().getCurMaxNameLength());
		if (attribute.equals(Setup.CONSIST))
			return " " + padAndTruncateString(engine.getConsistName(), engineManager.getConsistMaxNameLength());
		return getRollingStockAttribute(engine, attribute, isPickup, false);
	}

	private String getCarAttribute(Car car, String attribute, boolean isPickup, boolean isLocal) {
		if (attribute.equals(Setup.LOAD))
			return (car.isCaboose() || car.isPassenger()) ? padAndTruncateString("", CarLoads.instance()
					.getCurMaxNameLength() + 1) : " "
					+ padAndTruncateString(car.getLoadName(), CarLoads.instance().getCurMaxNameLength());
		else if (attribute.equals(Setup.HAZARDOUS))
			return (car.isHazardous() ? " " + Setup.getHazardousMsg() : padAndTruncateString("", Setup
					.getHazardousMsg().length() + 1));
		else if (attribute.equals(Setup.DROP_COMMENT))
			return " " + car.getDropComment();
		else if (attribute.equals(Setup.PICKUP_COMMENT))
			return " " + car.getPickupComment();
		else if (attribute.equals(Setup.KERNEL))
			return " " + padAndTruncateString(car.getKernelName(), carManager.getKernelMaxNameLength());
		else if (attribute.equals(Setup.RWE)) {
			if (!car.getReturnWhenEmptyDestName().equals(""))
				return " "
						+ padAndTruncateString(TrainManifestHeaderText.getStringHeader_RWE() + " "
								+ splitString(car.getReturnWhenEmptyDestinationName()) + " ,"
								+ splitString(car.getReturnWhenEmptyDestTrackName()), locationManager
								.getMaxLocationAndTrackNameLength()
								+ TrainManifestHeaderText.getStringHeader_RWE().length() + 3);
			return "";
		} else if (attribute.equals(Setup.FINAL_DEST)) {
			if (!car.getFinalDestinationName().equals(""))
				return Setup.isPrintHeadersEnabled() ? " "
						+ padAndTruncateString(splitString(car.getFinalDestinationName()), locationManager
								.getMaxLocationNameLength()) : " "
						+ padAndTruncateString(TrainManifestText.getStringFinalDestination() + " "
								+ splitString(car.getFinalDestinationName()), locationManager
								.getMaxLocationNameLength()
								+ TrainManifestText.getStringFinalDestination().length() + 1);
			return "";
		} else if (attribute.equals(Setup.FINAL_DEST_TRACK)) {
			if (!car.getFinalDestinationName().equals(""))
				return Setup.isPrintHeadersEnabled() ? " "
						+ padAndTruncateString(splitString(car.getFinalDestinationName()) + ", "
								+ splitString(car.getFinalDestinationTrackName()), locationManager
								.getMaxLocationAndTrackNameLength() + 2) : " "
						+ padAndTruncateString(TrainManifestText.getStringFinalDestination() + " "
								+ splitString(car.getFinalDestinationName()) + ", "
								+ splitString(car.getFinalDestinationTrackName()), locationManager
								.getMaxLocationAndTrackNameLength()
								+ TrainManifestText.getStringFinalDestination().length() + 3);
			return "";
		}
		return getRollingStockAttribute(car, attribute, isPickup, isLocal);
	}

	private static final int trimRoadNumber = 4; // trim the number of road numbers printed by 4

	private String getRollingStockAttribute(RollingStock rs, String attribute, boolean isPickup, boolean isLocal) {
		if (attribute.equals(Setup.NUMBER))
			return " "
					+ padAndTruncateString(splitString(rs.getNumber()), Control.max_len_string_road_number
							- trimRoadNumber);
		else if (attribute.equals(Setup.ROAD))
			return " " + padAndTruncateString(rs.getRoadName(), CarRoads.instance().getCurMaxNameLength());
		else if (attribute.equals(Setup.TYPE)) {
			String[] type = rs.getTypeName().split("-"); // second half of string can be anything
			return " " + padAndTruncateString(type[0], CarTypes.instance().getCurMaxNameLength());
		} else if (attribute.equals(Setup.LENGTH))
			return " " + padAndTruncateString(rs.getLength() + LENGTHABV, CarLengths.instance().getCurMaxNameLength());
		else if (attribute.equals(Setup.COLOR))
			return " " + padAndTruncateString(rs.getColor(), CarColors.instance().getCurMaxNameLength());
		else if (((attribute.equals(Setup.LOCATION)) && (isPickup || isLocal))
				|| (attribute.equals(Setup.TRACK) && isPickup)) {
			if (rs.getTrack() != null)
				return Setup.isPrintHeadersEnabled() ? " "
						+ padAndTruncateString(splitString(rs.getTrackName()), locationManager.getMaxTrackNameLength())
						: " "
								+ padAndTruncateString(TrainManifestText.getStringFrom() + " "
										+ splitString(rs.getTrackName()), TrainManifestText.getStringFrom().length()
										+ locationManager.getMaxTrackNameLength() + 1);
			return "";
		} else if (attribute.equals(Setup.LOCATION) && !isPickup && !isLocal)
			return Setup.isPrintHeadersEnabled() ? " "
					+ padAndTruncateString(splitString(rs.getLocationName()), locationManager
							.getMaxLocationNameLength())
					: " "
							+ padAndTruncateString(TrainManifestText.getStringFrom() + " "
									+ splitString(rs.getLocationName()), locationManager.getMaxLocationNameLength()
									+ TrainManifestText.getStringFrom().length() + 1);
		else if (attribute.equals(Setup.DESTINATION) && isPickup) {
			if (Setup.isTabEnabled())
				return Setup.isPrintHeadersEnabled() ? " "
						+ padAndTruncateString(splitString(rs.getDestinationName()), locationManager
								.getMaxLocationNameLength()) : " "
						+ padAndTruncateString(TrainManifestText.getStringDest() + " "
								+ splitString(rs.getDestinationName()), TrainManifestText.getStringDest().length()
								+ locationManager.getMaxLocationNameLength() + 1);
			else
				return " " + TrainManifestText.getStringDestination() + " " + splitString(rs.getDestinationName());
		} else if ((attribute.equals(Setup.DESTINATION) || attribute.equals(Setup.TRACK)) && !isPickup)
			return Setup.isPrintHeadersEnabled() ? " "
					+ padAndTruncateString(splitString(rs.getDestinationTrackName()), locationManager
							.getMaxTrackNameLength()) : " "
					+ padAndTruncateString(TrainManifestText.getStringTo() + " "
							+ splitString(rs.getDestinationTrackName()), locationManager.getMaxTrackNameLength()
							+ TrainManifestText.getStringTo().length() + 1);
		else if (attribute.equals(Setup.DEST_TRACK))
			return Setup.isPrintHeadersEnabled() ? " "
					+ padAndTruncateString(splitString(rs.getDestinationName()) + ", "
							+ splitString(rs.getDestinationTrackName()), locationManager
							.getMaxLocationAndTrackNameLength() + 2) : " "
					+ padAndTruncateString(TrainManifestText.getStringDest() + " "
							+ splitString(rs.getDestinationName()) + ", " + splitString(rs.getDestinationTrackName()),
							locationManager.getMaxLocationAndTrackNameLength()
									+ TrainManifestText.getStringDest().length() + 3);
		else if (attribute.equals(Setup.OWNER))
			return " " + padAndTruncateString(rs.getOwner(), CarOwners.instance().getCurMaxNameLength());
		else if (attribute.equals(Setup.COMMENT))
			return " " + rs.getComment();
		else if (attribute.equals(Setup.NONE))
			return "";
		// the three utility attributes that don't get printed but need to be tabbed out
		else if (attribute.equals(Setup.NO_NUMBER))
			return " "
					+ padAndTruncateString("", Control.max_len_string_road_number
							- (trimRoadNumber + utilityCarCountFieldSize + 1));
		else if (attribute.equals(Setup.NO_ROAD))
			return " " + padAndTruncateString("", CarRoads.instance().getCurMaxNameLength());
		else if (attribute.equals(Setup.NO_COLOR))
			return " " + padAndTruncateString("", CarColors.instance().getCurMaxNameLength());
		// the three truncated manifest attributes
		else if (attribute.equals(Setup.NO_DESTINATION) || attribute.equals(Setup.NO_DEST_TRACK)
				|| attribute.equals(Setup.NO_LOCATION))
			return "";
		else if (attribute.equals(Setup.TAB))
			return tabString("", Setup.getTab1Length());
		else if (attribute.equals(Setup.TAB2))
			return tabString("", Setup.getTab2Length());
		else if (attribute.equals(Setup.TAB3))
			return tabString("", Setup.getTab3Length());
		return MessageFormat.format(Bundle.getMessage("ErrorPrintOptions"), new Object[] { attribute }); // something
																											// isn't
																											// right!
	}

	public void printEngineHeader(PrintWriter file, boolean isManifest) {
		if (!Setup.isPrintHeadersEnabled())
			return;
		int lineLength = getLineLength(isManifest);
		printHorizontalLine(file, 0, lineLength);
		String s = padAndTruncateString(getPickupEngineHeader(), lineLength / 2, true);
		s = s + VERTICAL_LINE_CHAR + getDropEngineHeader();
		if (s.length() > lineLength)
			s = s.substring(0, lineLength);
		addLine(file, s);
		printHorizontalLine(file, 0, lineLength);
	}
	
	public void printPickupEngineHeader(PrintWriter file, boolean isManifest) {
		int lineLength = getLineLength(isManifest);
		printHorizontalLine(file, 0, lineLength);
		String s = padAndTruncateString(tabString("", Setup.getManifestPrefixLength() + 1, true)
				+ getPickupEngineHeader(), lineLength, true);
		addLine(file, s);
		printHorizontalLine(file, 0, lineLength);
	}
	
	public void printDropEngineHeader(PrintWriter file, boolean isManifest) {
		int lineLength = getLineLength(isManifest);
		printHorizontalLine(file, 0, lineLength);
		String s = padAndTruncateString(tabString("", Setup.getManifestPrefixLength() + 1, true)
				+ getDropEngineHeader(), lineLength, true);
		addLine(file, s);
		printHorizontalLine(file, 0, lineLength);
	}

	/**
	 * Prints the two column header for cars
	 * 
	 * @param file
	 * @param isManifest
	 */
	public void printCarHeader(PrintWriter file, boolean isManifest) {
		if (!Setup.isPrintHeadersEnabled() || !Setup.isTwoColumnFormatEnabled())
			return;
		int lineLength = getLineLength(isManifest);
		printHorizontalLine(file, 0, lineLength);
		// center pick up and set out text
		String s = padAndTruncateString(tabString(Setup.getPickupCarPrefix(), lineLength / 4
				- Setup.getPickupCarPrefix().length() / 2, true), lineLength / 2, true)
				+ VERTICAL_LINE_CHAR
				+ padAndTruncateString(tabString(Setup.getDropCarPrefix(), lineLength / 4
						- Setup.getDropCarPrefix().length() / 2, true), lineLength / 2 - 1, true);
		addLine(file, s);
		printHorizontalLine(file, 0, lineLength);

		s = padAndTruncateString(getPickupCarHeader(isManifest), lineLength / 2, true) + VERTICAL_LINE_CHAR
				+ getDropCarHeader(isManifest);
		if (s.length() > lineLength)
			s = s.substring(0, lineLength);
		addLine(file, s);
		printHorizontalLine(file, 0, lineLength);
	}

	public void printPickupCarHeader(PrintWriter file, boolean isManifest) {
		if (!Setup.isPrintHeadersEnabled())
			return;
		printHorizontalLine(file, isManifest);
		String s = padAndTruncateString(tabString("", Setup.getManifestPrefixLength() + 1, true)
				+ getPickupCarHeader(isManifest), getLineLength(isManifest), true);
		addLine(file, s);
		printHorizontalLine(file, isManifest);
	}

	public void printDropCarHeader(PrintWriter file, boolean isManifest) {
		if (!Setup.isPrintHeadersEnabled())
			return;
		printHorizontalLine(file, isManifest);
		String s = padAndTruncateString(tabString("", Setup.getManifestPrefixLength() + 1, true)
				+ getDropCarHeader(isManifest), getLineLength(isManifest), true);
		addLine(file, s);
		printHorizontalLine(file, isManifest);
	}

	public void printLocalCarMoveHeader(PrintWriter file, boolean isManifest) {
		if (!Setup.isPrintHeadersEnabled())
			return;
		printHorizontalLine(file, isManifest);
		String s = padAndTruncateString(tabString("", Setup.getManifestPrefixLength() + 1, true)
				+ getLocalMoveHeader(isManifest), getLineLength(isManifest), true);
		addLine(file, s);
		printHorizontalLine(file, isManifest);
	}

	public String getPickupEngineHeader() {
		return getHeader(Setup.getPickupEngineMessageFormat(), PICKUP, !LOCAL);
	}

	public String getDropEngineHeader() {
		return getHeader(Setup.getDropEngineMessageFormat(), !PICKUP, !LOCAL);
	}

	public String getPickupCarHeader(boolean isManifest) {
		if (isManifest)
			return getHeader(Setup.getPickupCarMessageFormat(), PICKUP, !LOCAL);
		else
			return getHeader(Setup.getSwitchListPickupCarMessageFormat(), PICKUP, !LOCAL);
	}

	public String getDropCarHeader(boolean isManifest) {
		if (isManifest)
			return getHeader(Setup.getDropCarMessageFormat(), !PICKUP, !LOCAL);
		else
			return getHeader(Setup.getSwitchListDropCarMessageFormat(), !PICKUP, !LOCAL);
	}

	public String getLocalMoveHeader(boolean isManifest) {
		if (isManifest)
			return getHeader(Setup.getLocalMessageFormat(), !PICKUP, LOCAL);
		else
			return getHeader(Setup.getSwitchListLocalMessageFormat(), !PICKUP, LOCAL);
	}

	private String getHeader(String[] format, boolean isPickup, boolean isLocal) {
		StringBuffer buf = new StringBuffer();
		for (String attribute : format) {
			if (attribute.equals(Setup.NONE))
				continue;
			if (attribute.equals(Setup.ROAD))
				buf.append(padAndTruncateString(TrainManifestHeaderText.getStringHeader_Road(), CarRoads.instance()
						.getCurMaxNameLength())
						+ " ");
			else if (attribute.equals(Setup.NUMBER))
				buf.append(padAndTruncateString(TrainManifestHeaderText.getStringHeader_Number(),
						Control.max_len_string_road_number - trimRoadNumber)
						+ " ");
			else if (attribute.equals(Setup.TYPE))
				buf.append(padAndTruncateString(TrainManifestHeaderText.getStringHeader_Type(), CarTypes.instance()
						.getCurMaxNameLength())
						+ " ");
			else if (attribute.equals(Setup.MODEL))
				buf.append(padAndTruncateString(TrainManifestHeaderText.getStringHeader_Model(), EngineModels
						.instance().getCurMaxNameLength())
						+ " ");
			else if (attribute.equals(Setup.CONSIST))
				buf.append(padAndTruncateString(TrainManifestHeaderText.getStringHeader_Consist(), engineManager
						.getConsistMaxNameLength())
						+ " ");
			else if (attribute.equals(Setup.KERNEL))
				buf.append(padAndTruncateString(TrainManifestHeaderText.getStringHeader_Kernel(), carManager
						.getKernelMaxNameLength())
						+ " ");
			else if (attribute.equals(Setup.LOAD))
				buf.append(padAndTruncateString(TrainManifestHeaderText.getStringHeader_Load(), CarLoads.instance()
						.getCurMaxNameLength())
						+ " ");
			else if (attribute.equals(Setup.COLOR))
				buf.append(padAndTruncateString(TrainManifestHeaderText.getStringHeader_Color(), CarColors.instance()
						.getCurMaxNameLength())
						+ " ");
			else if (attribute.equals(Setup.OWNER))
				buf.append(padAndTruncateString(TrainManifestHeaderText.getStringHeader_Owner(), CarOwners.instance()
						.getCurMaxNameLength())
						+ " ");
			else if (attribute.equals(Setup.LENGTH))
				buf.append(padAndTruncateString(TrainManifestHeaderText.getStringHeader_Length(),
						Control.max_len_string_length_name)
						+ " ");
			else if (attribute.equals(Setup.TRACK))
				buf.append(padAndTruncateString(TrainManifestHeaderText.getStringHeader_Track(), locationManager
						.getMaxTrackNameLength())
						+ " ");
			else if (attribute.equals(Setup.LOCATION) && (isPickup || isLocal))
				buf.append(padAndTruncateString(TrainManifestHeaderText.getStringHeader_Location(), locationManager
						.getMaxTrackNameLength())
						+ " ");
			else if (attribute.equals(Setup.LOCATION) && !isPickup)
				buf.append(padAndTruncateString(TrainManifestHeaderText.getStringHeader_Location(), locationManager
						.getMaxLocationNameLength())
						+ " ");
			else if (attribute.equals(Setup.DESTINATION) && !isPickup)
				buf.append(padAndTruncateString(TrainManifestHeaderText.getStringHeader_Destination(), locationManager
						.getMaxTrackNameLength())
						+ " ");
			else if (attribute.equals(Setup.DESTINATION) && isPickup)
				buf.append(padAndTruncateString(TrainManifestHeaderText.getStringHeader_Destination(), locationManager
						.getMaxLocationNameLength())
						+ " ");
			else if (attribute.equals(Setup.DEST_TRACK))
				buf.append(padAndTruncateString(TrainManifestHeaderText.getStringHeader_Dest_Track(), locationManager
						.getMaxLocationAndTrackNameLength() + 2)
						+ " ");
			else if (attribute.equals(Setup.FINAL_DEST))
				buf.append(padAndTruncateString(TrainManifestHeaderText.getStringHeader_Final_Dest(), locationManager
						.getMaxLocationNameLength())
						+ " ");
			else if (attribute.equals(Setup.FINAL_DEST_TRACK))
				buf.append(padAndTruncateString(TrainManifestHeaderText.getStringHeader_Final_Dest_Track(),
						locationManager.getMaxLocationAndTrackNameLength() + 2)
						+ " ");
			else if (attribute.equals(Setup.HAZARDOUS))
				buf.append(padAndTruncateString(TrainManifestHeaderText.getStringHeader_Hazardous(), Setup
						.getHazardousMsg().length())
						+ " ");
			else if (attribute.equals(Setup.RWE))
				buf.append(TrainManifestHeaderText.getStringHeader_RWE() + " ");
			else if (attribute.equals(Setup.COMMENT))
				buf.append(TrainManifestHeaderText.getStringHeader_Comment() + " ");
			else if (attribute.equals(Setup.TAB))
				buf.append(tabString("", Setup.getTab1Length()));
			else if (attribute.equals(Setup.TAB2))
				buf.append(tabString("", Setup.getTab2Length()));
			else if (attribute.equals(Setup.TAB3))
				buf.append(tabString("", Setup.getTab3Length()));
			else
				buf.append(attribute + " ");
		}
		return buf.toString();
	}

	/**
	 * Prints a line across the entire page.
	 * 
	 * @param file
	 */
	public void printHorizontalLine(PrintWriter file, boolean isManifest) {
		printHorizontalLine(file, 0, getLineLength(isManifest));
	}

	public void printHorizontalLine(PrintWriter file, int start, int end) {
		StringBuffer sb = new StringBuffer();
		while (start-- > 0)
			sb.append(SPACE);
		while (end-- > 0)
			sb.append(HORIZONTAL_LINE_CHAR);
		addLine(file, sb.toString());
	}

        public static String getISO8601Date(boolean isModelYear) {
            Calendar calendar = Calendar.getInstance();
            if (isModelYear && !Setup.getYearModeled().isEmpty()) {
                calendar.set(Calendar.YEAR, Integer.parseInt(Setup.getYearModeled().trim()));
            }
            return (new ISO8601DateFormat()).format(calendar.getTime());
        }

        public static String getDate(Date date) {
		Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);

		String year = Integer.toString(calendar.get(Calendar.YEAR));
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
		return calendar.get(Calendar.MONTH) + 1 + "/" + calendar.get(Calendar.DAY_OF_MONTH) + "/" + year + " "
				+ h + ":" + m + " " + AM_PM;
        }

	public static String getDate(boolean isModelYear) {
		Calendar calendar = Calendar.getInstance();
                if (isModelYear && !Setup.getYearModeled().equals("")) {
                    calendar.set(Calendar.YEAR, Integer.parseInt(Setup.getYearModeled().trim()));
                }
                return TrainCommon.getDate(calendar.getTime());
	}

	/**
	 * Returns a double in minutes representing the string date. Date string has to be in the order: Month / day / year
	 * hour:minute AM_PM
	 * 
	 * @param date
	 * @return double in minutes
	 */
	public double convertStringDateToDouble(String date) {
		double dateToDouble = 0;
		try {
			// log.debug("Convert date: " + date);
			String[] breakdownDate = date.split("/");
			// log.debug("Month: " + breakdownDate[0]);
			// convert month to minutes
			dateToDouble += 60 * 24 * 31 * Integer.parseInt(breakdownDate[0]);
			// log.debug("Day: " + breakdownDate[1]);
			dateToDouble += 60 * 24 * Integer.parseInt(breakdownDate[1]);
			String[] breakDownYear = breakdownDate[2].split(" ");
			// log.debug("Year: " + breakDownYear[0]);
			dateToDouble += 60 * 24 * 365 * Integer.parseInt(breakDownYear[0]);
			String[] breakDownTime = breakDownYear[1].split(":");
			// log.debug("Hour: " + breakDownTime[0]);
			dateToDouble += 60 * Integer.parseInt(breakDownTime[0]);
			// log.debug("Minute: " + breakDownTime[1]);
			dateToDouble += Integer.parseInt(breakDownTime[1]);
			if (breakDownYear.length > 2) {
				log.debug("AM_PM: " + breakDownYear[2]);
				if (breakDownYear[2].equals(Bundle.getMessage("PM")))
					dateToDouble += 60 * 12;
			}
		} catch (NumberFormatException e) {
			log.error("Not able to convert date: " + date + " to double");
		}
		// log.debug("Double: "+dateToDouble);
		return dateToDouble;
	}

	/**
	 * Will pad out a string by adding spaces to the end of the string, and will remove characters from the end of the
	 * string if the string exceeds the field size.
	 * 
	 * @param s
	 * @param fieldSize
	 * @return A String the specified length
	 */
	public static String padAndTruncateString(String s, int fieldSize) {
		return padAndTruncateString(s, fieldSize, Setup.isTabEnabled());
	}

	public static String padAndTruncateString(String s, int fieldSize, boolean enabled) {
		if (!enabled)
			return s;
		s = padString(s, fieldSize);
		if (s.length() > fieldSize)
			s = s.substring(0, fieldSize);
		return s;
	}

	/**
	 * Adjusts string to be a certain number of characters by adding spaces to the end of the string.
	 * 
	 * @param s
	 * @param fieldSize
	 * @return A String the specified length
	 */
	public static String padString(String s, int fieldSize) {
		StringBuffer buf = new StringBuffer(s);
		while (buf.length() < fieldSize) {
			buf.append(" ");
		}
		return buf.toString();
	}

	/**
	 * Adds the requested number of spaces to the start of the string.
	 * 
	 * @param s
	 * @param tabSize
	 * @return A String the specified length
	 */
	public static String tabString(String s, int tabSize) {
		return tabString(s, tabSize, Setup.isTabEnabled());
	}

	public static String tabString(String s, int tabSize, boolean enabled) {
		if (!enabled)
			return s;
		StringBuffer buf = new StringBuffer();
		while (buf.length() < tabSize) {
			buf.append(" ");
		}
		buf.append(s);
		return buf.toString();
	}

	protected int getLineLength(boolean isManifest) {
		return getLineLength(isManifest ? Setup.getManifestOrientation() : Setup.getSwitchListOrientation(), Setup
				.getManifestFontSize(), Setup.getFontName());
	}

	private static int getLineLength(String orientation, int fontSize, String fontName) {
		// page size has been adjusted to account for margins of .5
		Dimension pagesize = new Dimension(540, 792); // Portrait
		if (orientation.equals(Setup.LANDSCAPE))
			pagesize = new Dimension(720, 612);
		if (orientation.equals(Setup.HANDHELD))
			pagesize = new Dimension(206, 792);
		// Metrics don't always work for the various font names, so use
		// Monospaced
		Font font = new Font(fontName, Font.PLAIN, fontSize); // NOI18N
		JLabel label = new JLabel();
		FontMetrics metrics = label.getFontMetrics(font);
		int charwidth = metrics.charWidth('m');

		// compute lines and columns within margins
		return pagesize.width / charwidth;
	}

	private boolean checkStringLength(String string, boolean isManifest) {
		return checkStringLength(string,
				isManifest ? Setup.getManifestOrientation() : Setup.getSwitchListOrientation(), Setup.getFontName(),
				Setup.getManifestFontSize());
	}

	/**
	 * Checks to see if the the string fits on the page.
	 * 
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
		return stringWidth <= pagesize.width;
	}

	/**
	 * Produces a string using commas and spaces between the strings provided in the array. Does not check for embedded
	 * commas in the string array.
	 * 
	 * @param array
	 * @return formated string using commas and spaces
	 */
	public static String formatStringToCommaSeparated(String[] array) {
		StringBuffer sbuf = new StringBuffer("");
		for (String s : array) {
			if (s != null)
				sbuf = sbuf.append(s + ", ");
		}
		if (sbuf.length() > 2)
			sbuf.setLength(sbuf.length() - 2); // remove trailing separators
		return sbuf.toString();
	}

	private static final Logger log = LoggerFactory.getLogger(TrainCommon.class.getName());
}
