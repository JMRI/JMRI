package jmri.jmrit.operations.trains;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import javax.swing.JOptionPane;
import jmri.Version;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.ScheduleItem;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarLoad;
import jmri.jmrit.operations.rollingstock.cars.CarLoads;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.router.Router;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds a train and creates the train's manifest.
 * 
 * @author Daniel Boudreau Copyright (C) 2008, 2009, 2010, 2011, 2012, 2013
 * @version $Revision$
 */
public class TrainBuilder extends TrainCommon {

	// report levels
	protected static final String ONE = Setup.BUILD_REPORT_MINIMAL;
	protected static final String THREE = Setup.BUILD_REPORT_NORMAL;
	protected static final String FIVE = Setup.BUILD_REPORT_DETAILED;
	protected static final String SEVEN = Setup.BUILD_REPORT_VERY_DETAILED;

	protected static final String BLANK_LINE = " ";
	private static final int DISPLAY_CAR_LIMIT = 500;
	private static final int DISPLAY_NO_MOVE_CAR_LIMIT = 100;

	// build variables shared between local routines
	Train _train; // the train being built
	int _numberCars = 0; // how many cars are moved by this train
	int _reqNumEngines = 0; // the number of engines required for this train
	List<Engine> _engineList; // list of engines available for this train
	Engine _leadEngine; // last lead engine found from getEngine
	int _carIndex; // index for carList
	List<Car> _carList; // list of cars available for this train
	List<RouteLocation> _routeList; // list of locations from departure to termination served by this train
	Hashtable<String, Integer> _numOfBlocks; // Number of blocks of cars departing staging.
	int _completedMoves; // the number of pick up car moves for a location
	int _reqNumOfMoves; // the requested number of car moves for a location
	Location _departLocation; // train departs this location
	Track _departStageTrack; // departure staging track (null if not staging)
	Location _terminateLocation; // train terminate at this location
	Track _terminateStageTrack; // terminate staging track (null if not staging)
	boolean _success; // true when enough cars have been picked up from a location
	PrintWriter _buildReport; // build report for this train

	// managers
	CarManager carManager = CarManager.instance();
	LocationManager locationManager = LocationManager.instance();
	EngineManager engineManager = EngineManager.instance();

	/**
	 * Build rules:
	 * <ul>
	 * <li>1. Need at least one location in route to build train
	 * <li>2. Select only locos and cars the that train can service
	 * <li>3. Optional, train must depart with the required number of moves (cars)
	 * <li>4. If required, add caboose or car with FRED to train
	 * <li>5. When departing staging find a track matching train requirements
	 * <li>6. All cars and locos on one track must leave staging
	 * <li>7. Service locations based on train direction, location car types and roads
	 * <li>8. Ignore track direction when train is a local (serves one location)
	 * 
	 * @param train
	 *            the train that is to be built
	 * 
	 */
	public void build(Train train) {
		this._train = train;
		try {
			build();
		} catch (BuildFailedException e) {
			buildFailed(e);
		}
	}

	private void build() throws BuildFailedException {
		log.debug("Building train ({})", _train.getName());

		_train.setStatus(Train.CODE_BUILDING);
		_train.setBuilt(false);
		_train.setLeadEngine(null);

		// create build statusCode file
		File file = TrainManagerXml.instance().createTrainBuildReportFile(_train.getName());
		try {
			_buildReport = new PrintWriter(new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(file), "UTF-8")), true); // NOI18N
		} catch (IOException e) {
			log.error("Can not open build status file: "+file.getName());
			return;
		}
		Date startTime = new Date();
		addLine(_buildReport, ONE, MessageFormat.format(Bundle.getMessage("BuildReportMsg"), new Object[] {
				_train.getName(), startTime }));
		addLine(_buildReport, ONE, MessageFormat.format(Bundle.getMessage("BuildReportVersion"), new Object[] { Version
				.name() }));
		// show the various build detail levels
		addLine(_buildReport, THREE, Bundle.getMessage("buildReportLevelThree"));
		addLine(_buildReport, FIVE, Bundle.getMessage("buildReportLevelFive"));
		addLine(_buildReport, SEVEN, Bundle.getMessage("buildReportLevelSeven"));

		if (Setup.getRouterBuildReportLevel().equals(Setup.BUILD_REPORT_DETAILED))
			addLine(_buildReport, SEVEN, Bundle.getMessage("buildRouterReportLevelDetailed"));
		else if (Setup.getRouterBuildReportLevel().equals(Setup.BUILD_REPORT_VERY_DETAILED))
			addLine(_buildReport, SEVEN, Bundle.getMessage("buildRouterReportLevelVeryDetailed"));

		if (_train.getRoute() == null) {
			throw new BuildFailedException(MessageFormat.format(Bundle.getMessage("buildErrorRoute"),
					new Object[] { _train.getName() }));
		}
		// get the train's route
		_routeList = _train.getRoute().getLocationsBySequenceList();
		if (_routeList.size() < 1) {
			throw new BuildFailedException(MessageFormat.format(Bundle.getMessage("buildErrorNeedRoute"),
					new Object[] { _train.getName() }));
		}
		// train departs
		_departLocation = locationManager.getLocationByName(_train.getTrainDepartsName());
		if (_departLocation == null) {
			throw new BuildFailedException(MessageFormat.format(Bundle.getMessage("buildErrorNeedDepLoc"),
					new Object[] { _train.getName() }));
		}
		// train terminates
		_terminateLocation = locationManager.getLocationByName(_train.getTrainTerminatesName());
		if (_terminateLocation == null) {
			throw new BuildFailedException(MessageFormat.format(Bundle.getMessage("buildErrorNeedTermLoc"),
					new Object[] { _train.getName() }));
		}

		// show train build options in detailed mode
		addLine(_buildReport, FIVE, BLANK_LINE); // add line when in detailed report mode
		addLine(_buildReport, FIVE, Bundle.getMessage("MenuItemBuildOptions") + ":");
		if (Setup.isBuildAggressive())
			addLine(_buildReport, FIVE, Bundle.getMessage("BuildModeAggressive"));
		else
			addLine(_buildReport, FIVE, Bundle.getMessage("BuildModeNormal"));
		// warn if car routing is disabled
		if (!Setup.isCarRoutingEnabled()) {
			addLine(_buildReport, FIVE, Bundle.getMessage("RoutingDisabled"));
		} else {
			if (Setup.isCarRoutingViaYardsEnabled())
				addLine(_buildReport, FIVE, Bundle.getMessage("RoutingViaYardsEnabled"));
			if (Setup.isOnlyActiveTrainsEnabled())
				addLine(_buildReport, FIVE, Bundle.getMessage("OnlySelectedTrains"));
			if (Setup.isCheckCarDestinationEnabled())
				addLine(_buildReport, FIVE, Bundle.getMessage("CheckCarDestination"));
		}
		if (_train.isBuildTrainNormalEnabled())
			addLine(_buildReport, FIVE, Bundle.getMessage("NormalModeWhenBuilding"));
		if (_train.isSendCarsToTerminalEnabled())
			addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("SendToTerminal"),
					new Object[] { _terminateLocation.getName() }));
		if (_train.isAllowReturnToStagingEnabled() || Setup.isAllowReturnToStagingEnabled())
			addLine(_buildReport, FIVE, Bundle.getMessage("AllowCarsToReturn"));
		if (_train.isAllowLocalMovesEnabled())
			addLine(_buildReport, FIVE, Bundle.getMessage("AllowLocalMoves"));
		if (_train.isAllowThroughCarsEnabled())
			addLine(_buildReport, FIVE, Bundle.getMessage("AllowThroughCars"));
		if (_train.isServiceAllCarsWithFinalDestinationsEnabled())
			addLine(_buildReport, FIVE, Bundle.getMessage("ServiceAllCars"));
		if (_train.isSendCarsWithCustomLoadsToStagingEnabled())
			addLine(_buildReport, FIVE, Bundle.getMessage("SendCustomToStaging"));
		if (_train.isBuildConsistEnabled())
			addLine(_buildReport, FIVE, Bundle.getMessage("BuildConsist"));
		addLine(_buildReport, ONE, BLANK_LINE); // add line
		// TODO: DAB control minimal build by each train
		if (_train.getTrainDepartsRouteLocation().getMaxCarMoves() > _departLocation.getNumberRS()
				&& Control.fullTrainOnly) {
			throw new BuildFailedException(MessageFormat.format(Bundle.getMessage("buildErrorCars"), new Object[] {
					Integer.toString(_departLocation.getNumberRS()), _train.getTrainDepartsName(), _train.getName() }));
		}
		addLine(_buildReport, THREE, MessageFormat.format(Bundle.getMessage("buildTrainRoute"), new Object[] {
				_train.getName(), _train.getRoute().getName() }));
		// get the number of requested car moves for this train
		int requested = 0;
		for (RouteLocation rl :  _routeList) {
			// check to see if there's a location for each stop in the route
			Location l = locationManager.getLocationByName(rl.getName());
			if (l == null || rl.getLocation() == null) {
				throw new BuildFailedException(MessageFormat.format(Bundle.getMessage("buildErrorLocMissing"),
						new Object[] { _train.getRoute().getName() }));
			}
			// train doesn't drop or pick up cars from staging locations found in middle of a route
			if (l.getLocationOps() == Location.STAGING && rl != _train.getTrainDepartsRouteLocation()
					&& rl != _train.getTrainTerminatesRouteLocation()) {
				addLine(_buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildLocStaging"), new Object[] { rl
						.getName() }));
				rl.setCarMoves(rl.getMaxCarMoves()); // don't allow car moves for this location
			}
			// if a location is skipped, no car drops or pick ups
			else if (_train.skipsLocation(rl.getId())) {
				addLine(_buildReport, THREE, MessageFormat.format(Bundle.getMessage("buildLocSkippedMaxTrain"),
						new Object[] { rl.getName(), _train.getName(), rl.getMaxTrainLength(),
								Setup.getLengthUnit().toLowerCase() }));
				rl.setCarMoves(rl.getMaxCarMoves()); // don't allow car moves for this location
			} else {
				// we're going to use this location, so initialize the location
				rl.setCarMoves(0); // clear the number of moves
				// show the type of moves allowed at this location
				if (!rl.isDropAllowed() && !rl.isPickUpAllowed()) {
					addLine(_buildReport, THREE, MessageFormat.format(Bundle.getMessage("buildLocNoDropsOrPickups"),
							new Object[] { rl.getName(), rl.getMaxTrainLength(), Setup.getLengthUnit().toLowerCase() }));
				} else {
					requested = requested + rl.getMaxCarMoves(); // add up the total number of car moves requested
					if (rl.isDropAllowed() && rl.isPickUpAllowed())
						addLine(_buildReport, THREE, MessageFormat.format(Bundle.getMessage("buildLocRequestMoves"),
								new Object[] { rl.getName(), rl.getMaxCarMoves(), rl.getMaxTrainLength(),
										Setup.getLengthUnit().toLowerCase() }));
					else if (!rl.isDropAllowed())
						addLine(_buildReport, THREE, MessageFormat.format(Bundle.getMessage("buildLocRequestPickups"),
								new Object[] { rl.getName(), rl.getMaxCarMoves(), rl.getMaxTrainLength(),
										Setup.getLengthUnit().toLowerCase() }));
					else
						addLine(_buildReport, THREE, MessageFormat.format(Bundle.getMessage("buildLocRequestDrops"),
								new Object[] { rl.getName(), rl.getMaxCarMoves(), rl.getMaxTrainLength(),
										Setup.getLengthUnit().toLowerCase() }));
				}
			}
			rl.setTrainWeight(0); // clear the total train weight
			rl.setTrainLength(0); // and length
		}
		int numMoves = requested; // number of car moves
		if (_routeList.size() > 1)
			requested = requested / 2; // only need half as many cars to meet requests
		addLine(_buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildRouteRequest"), new Object[] {
				_train.getRoute().getName(), Integer.toString(requested), Integer.toString(numMoves) }));
		_train.setNumberCarsRequested(requested); // save number of car requested

		// get engine requirements for this train
		if (_train.getNumberEngines().equals(Train.AUTO)) {
			_reqNumEngines = getAutoEngines();
		} else {
			_reqNumEngines = Integer.parseInt(_train.getNumberEngines());
		}
		showTrainRequirements();

		// show road names that this train will service
		if (!_train.getRoadOption().equals(Train.ALL_ROADS)) {
			addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildTrainRoads"), new Object[] {
					_train.getName(), _train.getRoadOption(), formatStringToCommaSeparated(_train.getRoadNames()) }));
		}
		// show owner names that this train will service
		if (!_train.getOwnerOption().equals(Train.ALL_OWNERS)) {
			addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildTrainOwners"), new Object[] {
					_train.getName(), _train.getOwnerOption(), formatStringToCommaSeparated(_train.getOwnerNames()) }));
		}
		// show built date serviced
		if (!_train.getBuiltStartYear().equals(""))
			addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildTrainBuiltAfter"), new Object[] {
					_train.getName(), _train.getBuiltStartYear() }));
		if (!_train.getBuiltEndYear().equals(""))
			addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildTrainBuiltBefore"), new Object[] {
					_train.getName(), _train.getBuiltEndYear() }));

		// show engine types that this train will service
		if (_reqNumEngines > 0) {
			addLine(_buildReport, FIVE, BLANK_LINE); // add line when in detailed report mode
			addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildTrainServicesEngineTypes"),
					new Object[] { _train.getName() }));
			addLine(_buildReport, FIVE, formatStringToCommaSeparated(_train.getLocoTypeNames()));
		}

		// allow up to two engine and caboose swaps in the train's route
		RouteLocation engineTerminatesFirstLeg = _train.getTrainTerminatesRouteLocation();
		RouteLocation cabooseOrFredTerminatesFirstLeg = _train.getTrainTerminatesRouteLocation();
		RouteLocation engineTerminatesSecondLeg = _train.getTrainTerminatesRouteLocation();
		RouteLocation cabooseOrFredTerminatesSecondLeg = _train.getTrainTerminatesRouteLocation();

		// Adjust where the locos will terminate
		if ((_train.getSecondLegOptions() & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES
				&& _train.getSecondLegStartLocation() != null) {
			engineTerminatesFirstLeg = _train.getSecondLegStartLocation();
		}
		if ((_train.getThirdLegOptions() & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES
				&& _train.getThirdLegStartLocation() != null) {
			engineTerminatesSecondLeg = _train.getThirdLegStartLocation();
			// No engine or caboose change at first leg?
			if ((_train.getSecondLegOptions() & Train.CHANGE_ENGINES) != Train.CHANGE_ENGINES) {
				engineTerminatesFirstLeg = _train.getThirdLegStartLocation();
			}
		}

		// make any caboose changes
		if ((_train.getSecondLegOptions() & Train.REMOVE_CABOOSE) == Train.REMOVE_CABOOSE
				|| (_train.getSecondLegOptions() & Train.ADD_CABOOSE) == Train.ADD_CABOOSE)
			cabooseOrFredTerminatesFirstLeg = _train.getSecondLegStartLocation();
		else if ((_train.getThirdLegOptions() & Train.REMOVE_CABOOSE) == Train.REMOVE_CABOOSE
				|| (_train.getThirdLegOptions() & Train.ADD_CABOOSE) == Train.ADD_CABOOSE)
			cabooseOrFredTerminatesFirstLeg = _train.getThirdLegStartLocation();
		if ((_train.getThirdLegOptions() & Train.REMOVE_CABOOSE) == Train.REMOVE_CABOOSE
				|| (_train.getThirdLegOptions() & Train.ADD_CABOOSE) == Train.ADD_CABOOSE)
			cabooseOrFredTerminatesSecondLeg = _train.getThirdLegStartLocation();

		// does train terminate into staging?
		_terminateStageTrack = null;
		List<Track> stagingTracksTerminate = _terminateLocation.getTrackByMovesList(Track.STAGING);
		if (stagingTracksTerminate.size() > 0) {
			addLine(_buildReport, THREE, BLANK_LINE); // add line when in normal report mode
			addLine(_buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildTerminateStaging"), new Object[] {
					_terminateLocation.getName(), Integer.toString(stagingTracksTerminate.size()) }));
			if (stagingTracksTerminate.size() > 1 && Setup.isPromptToStagingEnabled()) {
				_terminateStageTrack = PromptToStagingDialog();
				startTime = new Date(); // reset build time
			} else {
				// is this train returning to the same staging in aggressive mode?
				if (_departLocation == _terminateLocation && Setup.isBuildAggressive()
						&& Setup.isStagingTrackImmediatelyAvail()) {
					addLine(_buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildStagingReturn"),
							new Object[] { _terminateLocation.getName() }));
				} else {
					for (Track track : stagingTracksTerminate) {
						_terminateStageTrack = track;
						if (checkTerminateStagingTrack(_terminateStageTrack)) {
							addLine(_buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildStagingAvail"),
									new Object[] { _terminateStageTrack.getName(), _terminateLocation.getName() }));
							break;
						}
						_terminateStageTrack = null;
					}
				}
			}
			if (_terminateStageTrack == null) {
				// is this train returning to the same staging in aggressive mode?
				if (_departLocation == _terminateLocation && Setup.isBuildAggressive()
						&& Setup.isStagingTrackImmediatelyAvail()) {
					log.debug("Train is returning to same track in staging");
				} else {
					addLine(_buildReport, ONE, Bundle.getMessage("buildErrorStagingFullNote"));
					throw new BuildFailedException(MessageFormat.format(Bundle.getMessage("buildErrorStagingFull"),
							new Object[] { _terminateLocation.getName() }));
				}
			}
		}

		// get list of engines for this route
		_engineList = engineManager.getAvailableTrainList(_train);

		// determine if train is departing staging
		_departStageTrack = null;
		List<Track> stagingTracks = _departLocation.getTrackByMovesList(Track.STAGING);
		if (stagingTracks.size() > 0) {
			addLine(_buildReport, THREE, BLANK_LINE); // add line when in normal report mode
			addLine(_buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildDepartStaging"), new Object[] {
					_departLocation.getName(), Integer.toString(stagingTracks.size()) }));
			if (stagingTracks.size() > 1 && Setup.isPromptFromStagingEnabled()) {
				_departStageTrack = PromptFromStagingDialog();
				startTime = new Date(); // restart build timer
				if (_departStageTrack == null) {
					showTrainRequirements();
					throw new BuildFailedException(MessageFormat.format(Bundle.getMessage("buildErrorStagingEmpty"),
							new Object[] { _departLocation.getName() }));
				}
				// load engines for this train
				if (!getEngines(_reqNumEngines, _train.getEngineModel(), _train.getEngineRoad(), _train
						.getTrainDepartsRouteLocation(), engineTerminatesFirstLeg)) {
					throw new BuildFailedException(MessageFormat.format(Bundle.getMessage("buildErrorEngines"),
							new Object[] { _reqNumEngines, _train.getTrainDepartsName(),
									engineTerminatesFirstLeg.getName() }));
				}
			} else
				for (Track track : stagingTracks) {
					_departStageTrack = track;
					addLine(_buildReport, THREE, MessageFormat.format(Bundle.getMessage("buildStagingHas"),
							new Object[] { _departStageTrack.getName(),
									Integer.toString(_departStageTrack.getNumberEngines()),
									Integer.toString(_departStageTrack.getNumberCars()) }));
					// is the departure track available?
					if (!checkDepartureStagingTrack(_departStageTrack)) {
						_departStageTrack = null;
						continue;
					}
					// try each departure track for the required engines
					if (getEngines(_reqNumEngines, _train.getEngineModel(), _train.getEngineRoad(), _train
							.getTrainDepartsRouteLocation(), engineTerminatesFirstLeg)) {
						addLine(_buildReport, SEVEN, Bundle.getMessage("buildDoneAssignEnginesStaging"));
						break; // done!
					}
					_departStageTrack = null;
				}
			if (_departStageTrack == null) {
				showTrainRequirements();
				throw new BuildFailedException(MessageFormat.format(Bundle.getMessage("buildErrorStagingEmpty"),
						new Object[] { _departLocation.getName() }));
				// departing staging and returning to same track?
			} else if (_terminateStageTrack == null && _departLocation == _terminateLocation && Setup.isBuildAggressive()
					&& Setup.isStagingTrackImmediatelyAvail()) {
				_terminateStageTrack = _departStageTrack; // use the same track
			}
		} else {
			// no staging tracks at this location, load engines for this train
			if (_reqNumEngines > 0)
				addLine(_buildReport, FIVE, BLANK_LINE); // add line when in detailed report mode
			if (!getEngines(_reqNumEngines, _train.getEngineModel(), _train.getEngineRoad(), _train
					.getTrainDepartsRouteLocation(), engineTerminatesFirstLeg)) {
				throw new BuildFailedException(MessageFormat
						.format(Bundle.getMessage("buildErrorEngines"), new Object[] { _reqNumEngines,
								_train.getTrainDepartsName(), engineTerminatesFirstLeg.getName() }));
			}
		}

		// Save termination and departure tracks
		_train.setTerminationTrack(_terminateStageTrack);
		_train.setDepartureTrack(_departStageTrack);

		// First engine change in route?
		Engine secondLeadEngine = null;
		if ((_train.getSecondLegOptions() & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES) {
			addLine(_buildReport, THREE, MessageFormat.format(Bundle.getMessage("buildTrainEngineChange"), new Object[] {
					_train.getSecondLegStartLocationName(), _train.getSecondLegNumberEngines(),
					_train.getSecondLegEngineModel(), _train.getSecondLegEngineRoad() }));
			if (getEngines(Integer.parseInt(_train.getSecondLegNumberEngines()), _train.getSecondLegEngineModel(), _train
					.getSecondLegEngineRoad(), _train.getSecondLegStartLocation(), engineTerminatesSecondLeg)) {
				secondLeadEngine = _leadEngine;
			} else {
				throw new BuildFailedException(MessageFormat.format(Bundle.getMessage("buildErrorEngines"),
						new Object[] { Integer.parseInt(_train.getSecondLegNumberEngines()),
								_train.getSecondLegStartLocation(), engineTerminatesSecondLeg }));
			}
		}
		// Second engine change in route?
		Engine thirdLeadEngine = null;
		if ((_train.getThirdLegOptions() & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES) {
			addLine(_buildReport, THREE, MessageFormat.format(Bundle.getMessage("buildTrainEngineChange"), new Object[] {
					_train.getThirdLegStartLocationName(), _train.getThirdLegNumberEngines(),
					_train.getThirdLegEngineModel(), _train.getThirdLegEngineRoad() }));
			if (getEngines(Integer.parseInt(_train.getThirdLegNumberEngines()), _train.getThirdLegEngineModel(), _train
					.getThirdLegEngineRoad(), _train.getThirdLegStartLocation(), _train.getTrainTerminatesRouteLocation())) {
				thirdLeadEngine = _leadEngine;
			} else {
				throw new BuildFailedException(MessageFormat.format(Bundle.getMessage("buildErrorEngines"),
						new Object[] { Integer.parseInt(_train.getThirdLegNumberEngines()),
								_train.getThirdLegStartLocation(), _train.getTrainTerminatesRouteLocation() }));
			}
		}
		if (_reqNumEngines > 0 && (!_train.isBuildConsistEnabled() || Setup.getHorsePowerPerTon() == 0))
			addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildDoneAssingEnginesTrain"),
					new Object[] { _train.getName() }));

		// show car types and loads that this train will service
		addLine(_buildReport, FIVE, BLANK_LINE); // add line when in detailed report mode
		addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildTrainServicesCarTypes"),
				new Object[] { _train.getName() }));
		addLine(_buildReport, FIVE, formatStringToCommaSeparated(_train.getCarTypeNames()));
		// show load names that this train will service
		if (!_train.getLoadOption().equals(Train.ALL_LOADS)) {
			addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildTrainLoads"), new Object[] {
					_train.getName(), _train.getLoadOption(), formatStringToCommaSeparated(_train.getLoadNames()) }));
		}

		// get list of cars for this route
		_carList = carManager.getAvailableTrainList(_train);
		// TODO: DAB this needs to be controlled by each train
		if (requested > _carList.size() && Control.fullTrainOnly) {
			throw new BuildFailedException(MessageFormat.format(Bundle.getMessage("buildErrorNumReq"), new Object[] {
					Integer.toString(requested), _train.getName(), Integer.toString(_carList.size()) }));
		}

		// remove unwanted cars
		removeCars();

		// Do caboose changes in reverse order in case there isn't enough track space

		// second caboose change?
		if ((_train.getThirdLegOptions() & Train.ADD_CABOOSE) > 0 && _train.getThirdLegStartLocation() != null
				&& _train.getTrainTerminatesRouteLocation() != null) {
			getCaboose(_train.getThirdLegCabooseRoad(), thirdLeadEngine, _train.getThirdLegStartLocation(), _train
					.getTrainTerminatesRouteLocation(), true);
		}

		// first caboose change?
		if ((_train.getSecondLegOptions() & Train.ADD_CABOOSE) > 0 && _train.getSecondLegStartLocation() != null
				&& cabooseOrFredTerminatesSecondLeg != null) {
			getCaboose(_train.getSecondLegCabooseRoad(), secondLeadEngine, _train.getSecondLegStartLocation(),
					cabooseOrFredTerminatesSecondLeg, true);
		}

		// get caboose or car with FRED if needed for train
		getCaboose(_train.getCabooseRoad(), _train.getLeadEngine(), _train.getTrainDepartsRouteLocation(),
				cabooseOrFredTerminatesFirstLeg, (_train.getRequirements() & Train.CABOOSE) > 0);
		getCarWithFred(_train.getCabooseRoad(), _train.getTrainDepartsRouteLocation(), cabooseOrFredTerminatesFirstLeg);

		// done assigning cabooses and cars with FRED, remove the rest, and save final destination
		removeCaboosesAndCarsWithFredAndSaveFinalDestination();

		blockCarsFromStaging(); // block cars from staging

		// now find destinations for cars
		addLine(_buildReport, THREE, BLANK_LINE); // add line when in normal report mode
		addLine(_buildReport, THREE, MessageFormat.format(Bundle.getMessage("buildTrain"), new Object[] { requested,
				_train.getName(), _carList.size() }));

		if (Setup.isBuildAggressive() && !_train.isBuildTrainNormalEnabled()) {
			// perform a two pass build for this train
			placeCars(50); // find destination for 50% of the available moves
		}
		placeCars(100); // done finding cars for this train!

		_train.setCurrentLocation(_train.getTrainDepartsRouteLocation());
		if (_numberCars < requested) {
			_train.setStatus(Train.CODE_PARTIAL_BUILT);
			addLine(_buildReport, ONE, Train.PARTIAL_BUILT + " " + _train.getNumberCarsWorked() + "/" + _train.getNumberCarsRequested() + " "
					+ Bundle.getMessage("cars"));
		} else {
			_train.setStatus(Train.CODE_BUILT);
			addLine(_buildReport, ONE, Train.BUILT + " " + _train.getNumberCarsWorked() + " " + Bundle.getMessage("cars"));
		}

		// check to see if additional engines are needed for this train
		checkNumnberOfEnginesNeeded();

		_train.setBuilt(true);
		addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildTime"), new Object[] { _train.getName(),
				new Date().getTime() - startTime.getTime() }));
		_buildReport.flush();
		_buildReport.close();

		// now make manifest
		new TrainManifest(_train);
		try {
			new JsonManifest(_train).build();
		} catch (IOException ex) {
			log.error("Unable to create JSON manifest: {}", ex.getLocalizedMessage());
			throw new BuildFailedException(ex);
		}
		if (Setup.isGenerateCsvManifestEnabled())
			new TrainCsvManifest(_train);
		// now create and place train icon
		_train.moveTrainIcon(_train.getTrainDepartsRouteLocation());
		log.debug("Done building train ({})", _train.getName());
	}

	private void showTrainRequirements() {
		addLine(_buildReport, ONE, BLANK_LINE); // add line
		addLine(_buildReport, ONE, Bundle.getMessage("TrainRequrements"));
		if (_reqNumEngines == 0)
			addLine(_buildReport, ONE, Bundle.getMessage("buildTrainReq0Engine"));
		else if (_reqNumEngines == 1)
			addLine(_buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildTrainReq1Engine"), new Object[] {
					_train.getEngineModel(), _train.getEngineRoad() }));
		else
			addLine(_buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildTrainReqEngine"), new Object[] {
					_train.getTrainDepartsName(), _train.getNumberEngines(), _train.getEngineModel(),
					_train.getEngineRoad() }));
		// show any required loco changes
		if ((_train.getSecondLegOptions() & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES) {
			addLine(_buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildTrainEngineChange"), new Object[] {
					_train.getSecondLegStartLocationName(), _train.getSecondLegNumberEngines(),
					_train.getSecondLegEngineModel(), _train.getSecondLegEngineRoad() }));
		}
		if ((_train.getSecondLegOptions() & Train.HELPER_ENGINES) == Train.HELPER_ENGINES) {
			addLine(_buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildTrainHelperEngines"), new Object[] {
					_train.getSecondLegNumberEngines(), _train.getSecondLegStartLocationName(),
					_train.getSecondLegEndLocationName(), _train.getSecondLegEngineModel(),
					_train.getSecondLegEngineRoad() }));
		}
		if ((_train.getThirdLegOptions() & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES) {
			addLine(_buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildTrainEngineChange"), new Object[] {
					_train.getThirdLegStartLocationName(), _train.getThirdLegNumberEngines(),
					_train.getThirdLegEngineModel(), _train.getThirdLegEngineRoad() }));
		}
		if ((_train.getThirdLegOptions() & Train.HELPER_ENGINES) == Train.HELPER_ENGINES) {
			addLine(_buildReport, ONE, MessageFormat
					.format(Bundle.getMessage("buildTrainHelperEngines"), new Object[] {
							_train.getThirdLegNumberEngines(), _train.getThirdLegStartLocationName(),
							_train.getThirdLegEndLocationName(), _train.getThirdLegEngineModel(),
							_train.getThirdLegEngineRoad() }));
		}
		// show caboose or FRED requirements
		if ((_train.getRequirements() & Train.CABOOSE) > 0) {
			addLine(_buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildTrainRequiresCaboose"),
					new Object[] { _train.getTrainDepartsName(), _train.getCabooseRoad() }));
		}
		// show any caboose changes in the train's route
		if ((_train.getSecondLegOptions() & Train.REMOVE_CABOOSE) == Train.REMOVE_CABOOSE
				|| (_train.getSecondLegOptions() & Train.ADD_CABOOSE) == Train.ADD_CABOOSE)
			addLine(_buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildCabooseChange"),
					new Object[] { _train.getSecondLegStartLocation() }));
		if ((_train.getThirdLegOptions() & Train.REMOVE_CABOOSE) == Train.REMOVE_CABOOSE
				|| (_train.getThirdLegOptions() & Train.ADD_CABOOSE) == Train.ADD_CABOOSE)
			addLine(_buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildCabooseChange"),
					new Object[] { _train.getThirdLegStartLocation() }));
		if ((_train.getRequirements() & Train.FRED) > 0) {
			addLine(_buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildTrainRequiresFRED"), new Object[] {
					_train.getTrainDepartsName(), _train.getCabooseRoad() }));
		}
		addLine(_buildReport, ONE, BLANK_LINE); // add line
	}

	/**
	 * Ask which staging track the train is to depart on.
	 * 
	 * @return The departure track the user selected.
	 */
	private Track PromptFromStagingDialog() {
		List<Track> tracksIn = _departLocation.getTrackList();
		List<Track> validTracks = new ArrayList<Track>();
		// only show valid tracks
		for (Track track : tracksIn) {
			if (checkDepartureStagingTrack(track))
				validTracks.add(track);
		}
		if (validTracks.size() > 1) {
			// need an object array for dialog window
			Object[] tracks = new Object[validTracks.size()];
			for (int i = 0; i < validTracks.size(); i++)
				tracks[i] = validTracks.get(i);

			Track selected = (Track) JOptionPane.showInputDialog(null, MessageFormat.format(Bundle
					.getMessage("TrainDepartingStaging"), new Object[] { _train.getName(), _departLocation.getName() }),
					Bundle.getMessage("SelectDepartureTrack"), JOptionPane.QUESTION_MESSAGE, null, tracks, null);
			if (selected != null)
				addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildUserSelectedDeparture"),
						new Object[] { selected.getName(), selected.getLocation().getName() }));
			return selected;
		} else if (validTracks.size() == 1)
			return validTracks.get(0);
		return null; // no tracks available
	}

	/**
	 * Ask which staging track the train is to terminate on.
	 * 
	 * @return The arrival track selected by the user.
	 */
	private Track PromptToStagingDialog() {
		List<Track> tracksIn = _terminateLocation.getTrackByNameList(null);
		List<Track> validTracks = new ArrayList<Track>();
		// only show valid tracks
		for (Track track : tracksIn) {
			if (checkTerminateStagingTrack(track))
				validTracks.add(track);
		}
		if (validTracks.size() > 1) {
			Object[] tracks = new Object[validTracks.size()];
			for (int i = 0; i < validTracks.size(); i++)
				tracks[i] = validTracks.get(i);
			// need an object array for dialog window
			Track selected = (Track) JOptionPane.showInputDialog(null, MessageFormat.format(Bundle
					.getMessage("TrainTerminatingStaging"),
					new Object[] { _train.getName(), _terminateLocation.getName() }), Bundle
					.getMessage("SelectArrivalTrack"), JOptionPane.QUESTION_MESSAGE, null, tracks, null);
			if (selected != null)
				addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildUserSelectedArrival"),
						new Object[] { selected.getName(), selected.getLocation().getName() }));
			return selected;
		} else if (validTracks.size() == 1)
			return validTracks.get(0);
		return null; // no tracks available
	}

	/**
	 * Get the engines for this train. If departing from staging (departStageTrack != null) engines must come from that
	 * track.
	 * 
	 * @return true if correct number of engines found.
	 * @throws BuildFailedException
	 */
	private boolean getEngines(int numberOfEngines, String model, String road, RouteLocation rl, RouteLocation rld)
			throws BuildFailedException {
		// load departure track if staging
		Track departTrack = null;
		if (rl == _train.getTrainDepartsRouteLocation())
			departTrack = _departStageTrack; // get departure track from staging, could be null
		// load termination track if staging
		Track terminateTrack = null;
		if (rld == _train.getTrainTerminatesRouteLocation())
			terminateTrack = _terminateStageTrack; // get termination track to staging, could be null
		// departing staging and returning to same track?
		if (_departStageTrack != null && terminateTrack == null && rld == _train.getTrainTerminatesRouteLocation()
				&& _departLocation == _terminateLocation && Setup.isBuildAggressive()
				&& Setup.isStagingTrackImmediatelyAvail())
			terminateTrack = _departStageTrack;

		// if not departing staging track and engines aren't required done!
		if (departTrack == null && numberOfEngines == 0)
			return true;

		// if departing staging and no engines required and none available, we're done
		if (departTrack != null && numberOfEngines == 0 && departTrack.getNumberEngines() == 0)
			return true;

		// if leaving staging, use any number of engines if required number is 0
		if (departTrack != null && numberOfEngines != 0 && departTrack.getNumberEngines() != numberOfEngines) {
			addLine(_buildReport, THREE, MessageFormat.format(Bundle.getMessage("buildStagingNotEngines"),
					new Object[] { departTrack.getName() }));
			return false; // done, wrong number of engines on staging track
		}

		// code check
		if (rl == null || rld == null)
			throw new BuildFailedException(MessageFormat.format(Bundle.getMessage("buildErrorEngLocUnknown"),
					new Object[] {}));

		addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildBegineSearchEngines"), new Object[] {
				numberOfEngines, model, road, rl.getName(), rld.getName() }));
		boolean foundLoco = false;
		List<Engine> singleLocos = new ArrayList<Engine>();
		for (int indexEng = 0; indexEng < _engineList.size(); indexEng++) {
			Engine engine = _engineList.get(indexEng);
			log.debug("Engine ({}) at location ({}, {})", engine.toString(), engine.getLocationName(), engine.getTrackName());

			// use engines that are departing from the selected staging track (departTrack != null if staging)
			if (departTrack != null && !departTrack.equals(engine.getTrack())) {
				continue;
			}

			// use engines that are departing from the correct location
			if (!engine.getLocationName().equals(rl.getName())) {
				log.debug("Skipping engine ({}) at location ({})", engine.toString(), engine.getLocationName());
				continue;
			}

			// remove engines types that train does not service
			if (!_train.acceptsTypeName(engine.getTypeName())) {
				addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildExcludeEngineType"),
						new Object[] { engine.toString(), engine.getTypeName() }));
				_engineList.remove(indexEng--);
				continue;
			}
			// skip engines models that train does not service
			if (!model.equals("") && !engine.getModel().equals(model)) {
				addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildExcludeEngineModel"),
						new Object[] { engine.toString(), engine.getModel(), engine.getLocationName() }));
				continue;
			}
			// Does the train have a very specific engine road name requirement?
			if (!road.equals("") && !engine.getRoadName().equals(road)) {
				addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildExcludeEngineRoad"),
						new Object[] { engine.toString(), engine.getRoadName() }));
				continue;
			}
			// remove rolling stock with roads that train does not service
			if (road.equals("") && !_train.acceptsRoadName(engine.getRoadName())) {
				addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildExcludeEngineRoad"),
						new Object[] { engine.toString(), engine.getRoadName() }));
				_engineList.remove(indexEng--);
				continue;
			}
			// remove engines with owners that train does not service
			if (!_train.acceptsOwnerName(engine.getOwner())) {
				addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildExcludeEngineOwner"),
						new Object[] { engine.toString(), engine.getOwner() }));
				_engineList.remove(indexEng--);
				continue;
			}
			// remove engines with built dates that train does not service
			if (!_train.acceptsBuiltDate(engine.getBuilt())) {
				addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildExcludeEngineBuilt"),
						new Object[] { engine.toString(), engine.getBuilt() }));
				_engineList.remove(indexEng--);
				continue;
			}
			// skip engines on tracks that don't service the train's departure direction
			if (!checkPickUpTrainDirection(engine, rl)) {
				continue;
			}
			// skip engines that have been assigned destinations that don't match the terminal
			if (engine.getDestination() != null && !engine.getDestinationName().equals(rld.getName())) {
				addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildExcludeEngineDestination"),
						new Object[] { engine.toString(), engine.getDestinationName() }));
				continue;
			}
			// remove engines that are out of service
			if (engine.isOutOfService()) {
				addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildExcludeEngineOutOfService"),
						new Object[] { engine.toString(), engine.getLocationName(), engine.getTrackName() }));
				_engineList.remove(indexEng--);
				continue;
			}
			// don't use non lead locos in a consist
			if (engine.getConsist() != null && !engine.getConsist().isLead(engine)) {
				addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildEnginePartConsist"),
						new Object[] { engine.toString(), engine.getConsist().getName(),
								engine.getConsist().getEngines().size() }));
				continue;
			}
			// departing staging, then all locos must go!
			if (departTrack != null) {
				if (!setLocoDestination(engine, rl, rld, terminateTrack))
					return false;
				_engineList.remove(indexEng--);
				foundLoco = true;
				continue;
			}
			// is this engine part of a consist?
			if (engine.getConsist() == null) {
				// single engine, but does the train require a consist?
				if (numberOfEngines > 1) {
					addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildExcludeEngineSingle"),
							new Object[] { engine.toString(), numberOfEngines }));
					singleLocos.add(engine);
					continue;
				}
				// engine is part of a consist
			} else {
				// Keep only lead engines in consist if required number is correct.
				if (engine.getConsist().isLead(engine)) {
					addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildEngineLeadConsist"),
							new Object[] { engine.toString(), engine.getConsist().getName(),
									engine.getConsist().getSize() }));
					if (engine.getConsist().getSize() == numberOfEngines) {
						log.debug("Consist ({}) has the required number of engines", engine.getConsist().getName()); // NOI18N
					} else if (numberOfEngines != 0) {
						// log.debug("Consist ("+engine.getConsist().getName()+") doesn't have the required number of engines");
						addLine(_buildReport, SEVEN, MessageFormat.format(Bundle
								.getMessage("buildExcludeEngConsistNumber"), new Object[] { engine.toString(),
								engine.getConsist().getName(), engine.getConsist().getSize() }));
						continue;
					}
				}
			}
			// found a loco!
			foundLoco = true;

			// now find terminal track for engine(s)
			addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildEngineRoadModelType"),
					new Object[] { engine.toString(), engine.getRoadName(), engine.getModel(), engine.getTypeName() }));
			addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildAtLocation"), new Object[] {
					(engine.getLocationName() + ", " + engine.getTrackName()), rld.getName() }));
			if (setLocoDestination(engine, rl, rld, terminateTrack)) {
				_engineList.remove(indexEng--);
				return true; // done
			}
		}
		// build a consist out of non-consisted locos
		if (!foundLoco && numberOfEngines > 1 && _train.isBuildConsistEnabled()) {
			addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildOptionBuildConsist"), new Object[] {
					numberOfEngines, rl.getName() }));
			addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildOptionSingleLocos"), new Object[] {
					singleLocos.size(), rl.getName() }));
			if (singleLocos.size() >= numberOfEngines) {
				int locos = 0;
				for (Engine engine : singleLocos) {
					if (setLocoDestination(engine, rl, rld, terminateTrack)) {
						_engineList.remove(engine);
						locos++;
					}
					if (locos == numberOfEngines)
						return true; // done
				}
			}
		}
		if (!foundLoco) {
			String locationName = rl.getName();
			if (departTrack != null)
				locationName = locationName + ", " + departTrack.getName();
			addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildNoLocosFoundAtLocation"),
					new Object[] { locationName }));
		} else if (departTrack != null)
			return true;
		// not able to assign engines to train
		return false;
	}

	private boolean setLocoDestination(Engine engine, RouteLocation rl, RouteLocation rld, Track terminateTrack) {
		// is there a staging track?
		if (terminateTrack != null) {
			String status = engine.testDestination(terminateTrack.getLocation(), terminateTrack);
			if (status.equals(Track.OKAY)) {
				addEngineToTrain(engine, rl, rld, terminateTrack);
				return true; // done
			} else {
				addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildCanNotDropEngineToTrack"),
						new Object[] { engine.toString(), terminateTrack.getName(), status, terminateTrack.getTrackTypeName() }));
			}
			// find a destination track for this engine
		} else {
			Location destination = rld.getLocation();
			List<Track> destTracks = destination.getTrackByMovesList(null);
			if (destTracks.size() == 0) {
				addLine(_buildReport, THREE, MessageFormat.format(Bundle.getMessage("buildNoTracksAtDestination"),
						new Object[] { rld.getName() }));
			}
			for (Track track : destTracks) {
				if (!checkDropTrainDirection(engine, rld, track))
					continue;
				String status = engine.testDestination(destination, track);
				if (status.equals(Track.OKAY)) {
					addEngineToTrain(engine, rl, rld, track);
					return true; // done
				} else {
					addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildCanNotDropEngineToTrack"),
							new Object[] { engine.toString(), track.getName(), status, track.getTrackTypeName() }));
				}
			}
			addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildCanNotDropEngToDest"),
					new Object[] { engine.toString(), rld.getName() }));
		}
		return false; // not able to set loco's destination
	}

	/**
	 * Returns the number of engines needed for this train, minimum 1, maximum user specified in setup. Based on maximum
	 * allowable train length and grade between locations, and the maximum cars that the train can have at the maximum
	 * train length. One engine per sixteen 40' cars for 1% grade. TODO Currently ignores the cars weight and engine
	 * horsepower
	 * 
	 * @return The number of engines needed
	 */
	private int getAutoEngines() {
		double numberEngines = 1;
		int moves = 0;

		for (RouteLocation rl :  _routeList) {
			moves += rl.getMaxCarMoves();
			double carDivisor = 16; // number of 40' cars per engine 1% grade
			// change engine requirements based on grade
			if (rl.getGrade() > 1) {
				double grade = rl.getGrade();
				carDivisor = carDivisor / grade;
			}
			log.debug("Maximum train length {} for location ({})", rl.getMaxTrainLength(), rl.getName());
			if (rl.getMaxTrainLength() / (carDivisor * 40) > numberEngines) {
				numberEngines = rl.getMaxTrainLength() / (carDivisor * (40 + Car.COUPLER));
				// round up to next whole integer
				numberEngines = Math.ceil(numberEngines);
				if (numberEngines > moves / carDivisor)
					numberEngines = Math.ceil(moves / carDivisor);
				if (numberEngines < 1)
					numberEngines = 1;
			}
		}
		int nE = (int) numberEngines;
		addLine(_buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildAutoBuildMsg"), new Object[] { Integer
				.toString(nE) }));
		if (nE > Setup.getMaxNumberEngines()) {
			addLine(_buildReport, THREE, MessageFormat.format(Bundle.getMessage("buildMaximumNumberEngines"),
					new Object[] { Setup.getMaxNumberEngines() }));
			nE = Setup.getMaxNumberEngines();
		}
		return nE;
	}

	/**
	 * Find a car with FRED if needed at the correct location and add it to the train. If departing staging, places car
	 * with FRED at the rear of the train.
	 * 
	 * @param road
	 *            Optional road name for this car.
	 * @param rl
	 *            Where in the route to pick up this car.
	 * @param rld
	 *            Where in the route to set out this car.
	 * @throws BuildFailedException
	 *             If car not found.
	 */
	private void getCarWithFred(String road, RouteLocation rl, RouteLocation rld) throws BuildFailedException {
		// load departure track if staging
		Track departTrack = null;
		if (rl == _train.getTrainDepartsRouteLocation())
			departTrack = _departStageTrack;
		boolean foundCar = false;
		boolean requiresCar = false;
		// Does this train require a car with FRED?
		if ((_train.getRequirements() & Train.FRED) == 0) {
			addLine(_buildReport, FIVE, Bundle.getMessage("buildTrainNoFred"));
			if (departTrack == null) // if not departing staging we're done
				return;
		} else {
			addLine(_buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildTrainReqFred"), new Object[] {
					_train.getName(), road, rl.getName(), rld.getName() }));
			requiresCar = true;
		}
		for (_carIndex = 0; _carIndex < _carList.size(); _carIndex++) {
			Car car = _carList.get(_carIndex);
			if (car.hasFred()) {
				addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildCarHasFRED"), new Object[] {
						car.toString(), car.getRoadName(), car.getLocationName() }));
				// car departing staging must leave with train
				if (car.getTrack() == departTrack) {
					foundCar = false;
					if (checkCarForDestinationAndTrack(car, rl, rld)) {
						if (car.getTrain() == _train)
							foundCar = true;
					} else if (findDestinationAndTrack(car, rl, rld)) {
						foundCar = true;
					}
					if (!foundCar) {
						throw new BuildFailedException(MessageFormat.format(
								Bundle.getMessage("buildErrorCarStageDest"), new Object[] { car.toString() }));
					}
				}
				// is there a specific road requirement for the car with FRED?
				else if (!road.equals("") && !road.equals(car.getRoadName())) {
					addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildExcludeCarWrongRoad"),
							new Object[] { car.toString(), car.getTypeName(), car.getRoadName() }));
					_carList.remove(car); // remove this car from the list
					_carIndex--;
					continue;
				} else if (!foundCar && car.getLocationName().equals(rl.getName())) {
					// remove cars that can't be picked up due to train and track directions
					if (!checkPickUpTrainDirection(car, rl)) {
						addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildExcludeCarTypeAtLoc"),
								new Object[] { car.toString(), car.getTypeName(),
										(car.getLocationName() + " " + car.getTrackName()) }));
						_carList.remove(car); // remove this car from the list
						_carIndex--;
						continue;
					}
					if (checkCarForDestinationAndTrack(car, rl, rld)) {
						if (car.getTrain() == _train)
							foundCar = true;
					} else if (findDestinationAndTrack(car, rl, rld)) {
						foundCar = true;
					}
					if (foundCar && departTrack == null)
						break;
				}
			}
		}
		if (requiresCar && !foundCar)
			throw new BuildFailedException(MessageFormat.format(Bundle.getMessage("buildErrorRequirements"),
					new Object[] { _train.getName(), Bundle.getMessage("FRED"), rl.getName(), rld.getName() }));
	}

	/**
	 * Find a caboose if needed at the correct location and add it to the train. If departing staging, places caboose at
	 * the rear of the train.
	 * 
	 * @param roadCaboose
	 *            Optional road name for this car.
	 * @param leadEngine
	 *            The lead engine for this train. Used to find a caboose with the same road name as the the engine.
	 * @param rl
	 *            Where in the route to pick up this car.
	 * @param rld
	 *            Where in the route to set out this car.
	 * @param requiresCaboose
	 *            When true, the train requires a caboose.
	 * @throws BuildFailedException
	 *             If car not found.
	 */
	private void getCaboose(String roadCaboose, Engine leadEngine, RouteLocation rl, RouteLocation rld,
			boolean requiresCaboose) throws BuildFailedException {
		if (rl == null) {
			throw new BuildFailedException(MessageFormat.format(Bundle.getMessage("buildErrorCabooseNoLocation"),
					new Object[] { _train.getName() }));
		}
		if (rld == null) {
			throw new BuildFailedException(MessageFormat.format(Bundle.getMessage("buildErrorCabooseNoDestination"),
					new Object[] { _train.getName(), rl.getName() }));
		}
		// load departure track if staging
		Track departTrack = null;
		if (rl == _train.getTrainDepartsRouteLocation())
			departTrack = _departStageTrack;
		if (!requiresCaboose) {
			addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildTrainNoCaboose"), new Object[] { rl
					.getName() }));
			if (departTrack == null)
				return;
		} else {
			addLine(_buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildTrainReqCaboose"), new Object[] {
					_train.getName(), roadCaboose, rl.getName(), rld.getName() }));
		}
		// Does the route have enough moves?
		if (requiresCaboose && rl.getMaxCarMoves() - rl.getCarMoves() <= 0) {
			throw new BuildFailedException(MessageFormat.format(Bundle.getMessage("buildErrorNoMoves"), new Object[] {
					rl.getName(), rl.getId(), Bundle.getMessage("Caboose") }));
		}
		// Now go through the car list looking for cabooses
		boolean cabooseTip = true; // add a user tip to the build report about cabooses if none found
		boolean cabooseAtDeparture = false; // set to true if caboose at departure location is found
		boolean foundCaboose = false;
		for (_carIndex = 0; _carIndex < _carList.size(); _carIndex++) {
			Car car = _carList.get(_carIndex);
			if (car.isCaboose()) {
				cabooseTip = false; // found at least one caboose, so they exist!
				addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildCarIsCaboose"), new Object[] {
						car.toString(), car.getRoadName(), car.getLocationName(), car.getTrackName() }));
				// car departing staging must leave with train
				if (car.getTrack() == departTrack) {
					foundCaboose = false;
					if (checkCarForDestinationAndTrack(car, rl, rld)) {
						if (car.getTrain() == _train)
							foundCaboose = true;
					} else if (findDestinationAndTrack(car, rl, rld)) {
						foundCaboose = true;
					}
					if (!foundCaboose) {
						throw new BuildFailedException(MessageFormat.format(
								Bundle.getMessage("buildErrorCarStageDest"), new Object[] { car.toString() }));
					}
				}
				// is there a specific road requirement for the caboose?
				else if (!roadCaboose.equals("") && !roadCaboose.equals(car.getRoadName())) {
					continue;
				} else if (!foundCaboose && car.getLocationName().equals(rl.getName())) {
					// remove cars that can't be picked up due to train and track directions
					if (!checkPickUpTrainDirection(car, rl)) {
						addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildExcludeCarTypeAtLoc"),
								new Object[] { car.toString(), car.getTypeName(),
										(car.getLocationName() + " " + car.getTrackName()) }));
						_carList.remove(car); // remove this car from the list
						_carIndex--;
						continue;
					}
					// first pass, take a caboose that matches the engine
					if (leadEngine != null && car.getRoadName().equals(leadEngine.getRoadName())) {
						addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildCabooseRoadMatches"),
								new Object[] { car.toString(), car.getRoadName(), leadEngine.toString() }));
						if (checkCarForDestinationAndTrack(car, rl, rld)) {
							if (car.getTrain() == _train)
								foundCaboose = true;
						} else if (findDestinationAndTrack(car, rl, rld)) {
							foundCaboose = true;
						}
						if (!foundCaboose) {
							_carList.remove(car); // remove this car from the list
							_carIndex--;
							continue;
						}
					}
					// done if we found a caboose and not departing staging
					if (foundCaboose && departTrack == null)
						break;
				}
			}
		}
		if (requiresCaboose && !foundCaboose) {
			log.debug("Second pass looking for caboose");
			// second pass, take any caboose available
			for (_carIndex = 0; _carIndex < _carList.size(); _carIndex++) {
				Car car = _carList.get(_carIndex);
				if (car.isCaboose() && car.getLocationName().equals(rl.getName())) {
					// is there a specific road requirement for the caboose?
					if (!roadCaboose.equals("") && !roadCaboose.equals(car.getRoadName())) {
						continue; // yes
					}
					// okay, we found a caboose at the departure location
					cabooseAtDeparture = true;
					if (checkCarForDestinationAndTrack(car, rl, rld)) {
						if (car.getTrain() == _train) {
							foundCaboose = true;
							break;
						}
					} else if (findDestinationAndTrack(car, rl, rld)) {
						foundCaboose = true;
						break;
					}
				}
			}
		}
		if (requiresCaboose && !foundCaboose) {
			if (cabooseTip) {
				addLine(_buildReport, ONE, Bundle.getMessage("buildNoteCaboose"));
				addLine(_buildReport, ONE, Bundle.getMessage("buildNoteCaboose2"));
			}
			if (!cabooseAtDeparture)
				throw new BuildFailedException(MessageFormat.format(Bundle.getMessage("buildErrorReqDepature"),
						new Object[] { _train.getName(), Bundle.getMessage("Caboose"), rl.getName() }));
			// we did find a caboose at departure that meet requirements, but couldn't place it at destination.
			throw new BuildFailedException(MessageFormat.format(Bundle.getMessage("buildErrorReqDest"), new Object[] {
					_train.getName(), Bundle.getMessage("Caboose"), rld.getName() }));
		}
	}

	/**
	 * Removes the remaining cabooses and cars with FRED from consideration. Also saves a car's final destination in
	 * case of train reset.
	 * 
	 * @throws BuildFailedException
	 */
	private void removeCaboosesAndCarsWithFredAndSaveFinalDestination() throws BuildFailedException {
		addLine(_buildReport, SEVEN, BLANK_LINE); // add line when in very detailed report mode
		addLine(_buildReport, SEVEN, Bundle.getMessage("buildRemoveCarsNotNeeded"));
		for (_carIndex = 0; _carIndex < _carList.size(); _carIndex++) {
			Car car = _carList.get(_carIndex);
			if (car.isCaboose() || car.hasFred()) {
				addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildExcludeCarTypeAtLoc"),
						new Object[] { car.toString(), car.getTypeName(),
								(car.getLocationName() + ", " + car.getTrackName()) }));
				if (car.getTrack() == _departStageTrack)
					throw new BuildFailedException("ERROR: Attempt to removed car with FRED or Caboose from staging");
				_carList.remove(car); // remove this car from the list
				_carIndex--;
			}
			// save final destination and track values in case of train reset
			car.setPreviousFinalDestination(car.getFinalDestination());
			car.setPreviousFinalDestinationTrack(car.getFinalDestinationTrack());
			car.setPreviousScheduleId(car.getScheduleId());
		}
	}

	/**
	 * Remove unwanted cars from the car list. Remove cars that don't have a track assignment, and check that the car
	 * can be serviced by this train
	 */
	private void removeCars() throws BuildFailedException {
		addLine(_buildReport, SEVEN, Bundle.getMessage("buildRemoveCars"));
		for (_carIndex = 0; _carIndex < _carList.size(); _carIndex++) {
			Car car = _carList.get(_carIndex);
			// remove cars that don't have a track assignment
			if (car.getTrack() == null) {
				addLine(_buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildErrorRsNoLoc"), new Object[] {
						car.toString(), car.getLocationName() }));
				_carList.remove(car);
				_carIndex--;
				continue;
			}
			// remove cars that have been reported as missing
			if (car.isLocationUnknown()) {
				addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildExcludeCarLocUnknown"),
						new Object[] { car.toString(), car.getLocationName(), car.getTrackName() }));
				if (car.getTrack().equals(_departStageTrack))
					throw new BuildFailedException(MessageFormat.format(Bundle.getMessage("buildErrorLocationUnknown"),
							new Object[] { car.getLocationName(), car.getTrackName(), car.toString() }));
				_carList.remove(car);
				_carIndex--;
				continue;
			}
			// remove cars that are out of service
			if (car.isOutOfService()) {
				addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildExcludeCarOutOfService"),
						new Object[] { car.toString(), car.getLocationName(), car.getTrackName() }));
				if (car.getTrack().equals(_departStageTrack))
					throw new BuildFailedException(MessageFormat.format(Bundle
							.getMessage("buildErrorLocationOutOfService"), new Object[] { car.getLocationName(),
							car.getTrackName(), car.toString() }));
				_carList.remove(car);
				_carIndex--;
				continue;
			}

			// remove cars with FRED that have a destination that isn't the terminal
			if (car.hasFred() && car.getDestination() != null && car.getDestination() != _terminateLocation) {
				addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildExcludeCarWrongDest"),
						new Object[] { car.toString(), car.getTypeName(), car.getDestinationName() }));
				_carList.remove(car);
				_carIndex--;
				continue;
			}

			// remove cabooses that have a destination that isn't the terminal, no caboose changes in the train's route
			if (car.isCaboose() && car.getDestination() != null && car.getDestination() != _terminateLocation
					&& _train.getSecondLegOptions() == Train.NONE && _train.getThirdLegOptions() == Train.NONE) {
				addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildExcludeCarWrongDest"),
						new Object[] { car.toString(), car.getTypeName(), car.getDestinationName() }));
				_carList.remove(car);
				_carIndex--;
				continue;
			}

			// is car at interchange?
			if (car.getTrack().getTrackType().equals(Track.INTERCHANGE)) {
				// don't service a car at interchange and has been dropped of by this train
				if (car.getTrack().getPickupOption().equals(Track.ANY)
						&& car.getSavedRouteId().equals(_train.getRoute().getId())) {
					addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildExcludeCarDropByTrain"),
							new Object[] { car.toString(), _train.getRoute().getName(), car.getLocationName(),
									car.getTrackName() }));
					_carList.remove(car);
					_carIndex--;
					continue;
				}
			}
			if (car.getTrack().getTrackType().equals(Track.INTERCHANGE)
					|| car.getTrack().getTrackType().equals(Track.SPUR)) {
				if (car.getTrack().getPickupOption().equals(Track.TRAINS)
						|| car.getTrack().getPickupOption().equals(Track.EXCLUDE_TRAINS)) {
					if (car.getTrack().acceptsPickupTrain(_train)) {
						log.debug("Car ({}) can be picked up by this train", car.toString());
					} else {
						addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildExcludeCarByTrain"),
								new Object[] { car.toString(), car.getLocationName(), car.getTrackName() }));
						_carList.remove(car);
						_carIndex--;
						continue;
					}
				} else if (car.getTrack().getPickupOption().equals(Track.ROUTES)
						|| car.getTrack().getPickupOption().equals(Track.EXCLUDE_ROUTES)) {
					if (car.getTrack().acceptsPickupRoute(_train.getRoute())) {
						log.debug("Car ({}) can be picked up by this route", car.toString());
					} else {
						addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildExcludeCarByRoute"),
								new Object[] { car.toString(), car.getLocationName(), car.getTrackName() }));
						_carList.remove(car);
						_carIndex--;
						continue;
					}
				}
			}

			// all cars in staging must be accepted, so don't exclude if in staging
			// note that for trains departing staging the engine and car roads and types were
			// checked in the routine checkDepartureStagingTrack().
			if (_departStageTrack == null || car.getTrack() != _departStageTrack) {
				if (!_train.acceptsRoadName(car.getRoadName())) {
					addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildExcludeCarWrongRoad"),
							new Object[] { car.toString(), car.getTypeName(), car.getRoadName() }));
					_carList.remove(car);
					_carIndex--;
					continue;
				}
				if (!_train.acceptsTypeName(car.getTypeName())) {
					addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildExcludeCarTypeAtLoc"),
							new Object[] { car.toString(), car.getTypeName(),
									(car.getLocationName() + ", " + car.getTrackName()) }));
					_carList.remove(car);
					_carIndex--;
					continue;
				}
				if (!car.isCaboose() && !car.isPassenger() && !_train.acceptsLoad(car.getLoadName(), car.getTypeName())) {
					addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildExcludeCarLoadAtLoc"),
							new Object[] { car.toString(), car.getTypeName(), car.getLoadName() }));
					_carList.remove(car);
					_carIndex--;
					continue;
				}
				if (!_train.acceptsOwnerName(car.getOwner())) {
					addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildExcludeCarOwnerAtLoc"),
							new Object[] { car.toString(), car.getOwner(),
									(car.getLocationName() + ", " + car.getTrackName()) }));
					_carList.remove(car);
					_carIndex--;
					continue;
				}
				if (!_train.acceptsBuiltDate(car.getBuilt())) {
					addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildExcludeCarBuiltAtLoc"),
							new Object[] { car.toString(), car.getBuilt(),
									(car.getLocationName() + ", " + car.getTrackName()) }));
					_carList.remove(car);
					_carIndex--;
					continue;
				}
				// remove cars with FRED if not needed by train
				if (car.hasFred() && (_train.getRequirements() & Train.FRED) == 0) {
					addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildExcludeCarWithFredAtLoc"),
							new Object[] { car.toString(), car.getTypeName(),
									(car.getLocationName() + ", " + car.getTrackName()) }));
					_carList.remove(car); // remove this car from the list
					_carIndex--;
					continue;
				}
				// does car have a wait count?
				if (car.getWait() > 0 && _train.services(car)) {
					addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildExcludeCarWait"),
							new Object[] { car.toString(), car.getTypeName(),
									(car.getLocationName() + ", " + car.getTrackName()), car.getWait() }));
					car.setWait(car.getWait() - 1); // decrement wait count
					_carList.remove(car);
					_carIndex--;
					continue;
				}
			}
		}
		// adjust car list to only have cars from one staging track
		if (_departStageTrack != null) {
			// Make sure that all cars in staging are moved
			_train.getTrainDepartsRouteLocation().setCarMoves(
					_train.getTrainDepartsRouteLocation().getMaxCarMoves() - _departStageTrack.getNumberCars());
			int numCarsFromStaging = 0;
			_numOfBlocks = new Hashtable<String, Integer>();
			for (_carIndex = 0; _carIndex < _carList.size(); _carIndex++) {
				Car c = _carList.get(_carIndex);
				if (c.getLocationName().equals(_departLocation.getName())) {
					if (c.getTrackName().equals(_departStageTrack.getName())) {
						numCarsFromStaging++;
						// populate car blocking hashtable
						// don't block cabooses, cars with FRED, or passenger. Only block lead cars in kernel
						if (!c.isCaboose() && !c.hasFred() && !c.isPassenger()
								&& (c.getKernel() == null || c.getKernel().isLead(c))) {
							log.debug("Car {} last location id: {}", c.toString(), c.getLastLocationId());
							Integer number = 1;
							if (_numOfBlocks.containsKey(c.getLastLocationId())) {
								number = _numOfBlocks.get(c.getLastLocationId()) + 1;
								_numOfBlocks.remove(c.getLastLocationId());
							}
							_numOfBlocks.put(c.getLastLocationId(), number);
						}
					} else {
						addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildExcludeCarAtLoc"),
								new Object[] { c.toString(), (c.getLocationName() + ", " + c.getTrackName()) }));
						_carList.remove(c);
						_carIndex--;
					}
				}
			}
			// show how many cars are departing from staging
			addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildDepartingStagingCars"),
					new Object[] { _departStageTrack.getLocation().getName(), _departStageTrack.getName(),
							numCarsFromStaging }));
			// and list them
			for (_carIndex = 0; _carIndex < _carList.size(); _carIndex++) {
				Car c = _carList.get(_carIndex);
				if (c.getTrack() == _departStageTrack) {
					addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildStagingCarAtLoc"),
							new Object[] { c.toString(), c.getTypeName(), c.getLoadName() }));
				}
			}
			// error if all of the cars and engines from staging aren't available
			if (numCarsFromStaging + _departStageTrack.getNumberEngines() != _departStageTrack.getNumberRS()) {
				throw new BuildFailedException(MessageFormat.format(Bundle.getMessage("buildErrorNotAll"),
						new Object[] { Integer.toString(_departStageTrack.getNumberRS()
								- (numCarsFromStaging + _departStageTrack.getNumberEngines())) }));
			}
			log.debug("Staging departure track ({}) has {} cars and {} blocks",
					_departStageTrack.getName(), numCarsFromStaging,	_numOfBlocks.size()); // NOI18N
		}

		// show how many cars were found
		addLine(_buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildFoundCars"), new Object[] {
				Integer.toString(_carList.size()), _train.getName() }));

		// now go through the car list and remove non-lead cars in kernels, destinations that aren't part of this route
		for (_carIndex = 0; _carIndex < _carList.size(); _carIndex++) {
			Car c = _carList.get(_carIndex);
			// only print out the first DISPLAY_CAR_LIMIT cars
			if (_carIndex < DISPLAY_CAR_LIMIT) {
				if (c.getLoadPriority().equals(CarLoad.PRIORITY_LOW))
					addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildCarAtLocWithMoves"),
							new Object[] { c.toString(), c.getTypeName(),
									(c.getLocationName() + ", " + c.getTrackName()), c.getMoves() }));
				else
					addLine(_buildReport, FIVE, MessageFormat.format(
							Bundle.getMessage("buildCarAtLocWithMovesPriority"), new Object[] { c.toString(),
									c.getTypeName(), (c.getLocationName() + ", " + c.getTrackName()), c.getMoves(),
									c.getLoadPriority() }));
			}
			if (_carIndex == DISPLAY_CAR_LIMIT)
				addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildOnlyFirstXXXCars"),
						new Object[] { _carIndex }));
			// use only the lead car in a kernel for building trains
			if (c.getKernel() != null) {
				addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildCarPartOfKernel"),
						new Object[] { c.toString(), c.getKernelName(), c.getKernel().getSize(),
								c.getKernel().getTotalLength(), Setup.getLengthUnit().toLowerCase() }));
				checkKernel(c);
				if (!c.getKernel().isLead(c)) {
					_carList.remove(c); // remove this car from the list
					_carIndex--;
					continue;
				}
			}
			if (_train.equals(c.getTrain())) {
				addLine(_buildReport, THREE, MessageFormat.format(Bundle.getMessage("buildCarAlreadyAssigned"),
						new Object[] { c.toString() }));
			}
			// does car have a destination that is part of this train's route?
			if (c.getDestination() != null) {
				addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildCarHasAssignedDest"),
						new Object[] { c.toString(), (c.getDestinationName() + ", " + c.getDestinationTrackName()) }));
				RouteLocation rld = _train.getRoute().getLastLocationByName(c.getDestinationName());
				if (rld == null) {
					addLine(_buildReport, FIVE, MessageFormat.format(Bundle
							.getMessage("buildExcludeCarDestNotPartRoute"), new Object[] { c.toString(),
							c.getDestinationName(), _train.getRoute().getName() }));
					// build failure if car departing staging
					if (c.getLocation().equals(_departLocation) && _departStageTrack != null) {
						// The following code should not be executed, departing staging tracks are checked before this
						// routine.
						throw new BuildFailedException(MessageFormat.format(Bundle
								.getMessage("buildErrorCarNotPartRoute"), new Object[] { c.toString() }));
					}
					_carList.remove(c); // remove this car from the list
					_carIndex--;
				}
			}
		}
		addLine(_buildReport, FIVE, BLANK_LINE); // add line when in detailed report mode
		return;
	}

	/**
	 * Verifies that all cars in the kernel have the same departure track. Also checks to see if the kernel has a lead
	 * car and the lead car is in service.
	 * 
	 * @param car
	 * @throws BuildFailedException
	 */
	private void checkKernel(Car car) throws BuildFailedException {
		boolean foundLeadCar = false;
		for (Car c : car.getKernel().getCars()) {
			// check that lead car exists
			if (c.getKernel().isLead(c) && !c.isOutOfService())
				foundLeadCar = true;
			// check to see that all cars have the same location and track
			if (car.getLocation() != c.getLocation() || car.getTrack() != c.getTrack())
				throw new BuildFailedException(MessageFormat.format(Bundle.getMessage("buildErrorCarKernelLocation"),
						new Object[] { c.toString(), car.getKernelName(), car.toString() }));
		}
		if (foundLeadCar == false)
			throw new BuildFailedException(MessageFormat.format(Bundle.getMessage("buildErrorCarKernelNoLead"),
					new Object[] { car.getKernelName() }));
	}

	/**
	 * Block cars departing staging. No guarantee that cars departing staging can be blocked by destination. By using
	 * the pick up location id, this routine tries to find destinations that are willing to accepts all of the cars that
	 * were "blocked" together when they were picked up. Rules: The route must allow set outs at the destination. The
	 * route must allow the correct number of set outs. The destination must accept all cars in the pick up block.
	 * 
	 * @throws BuildFailedException
	 */
	private void blockCarsFromStaging() throws BuildFailedException {
		if (_departStageTrack == null || !_departStageTrack.isBlockCarsEnabled()) {
			return;
		}
		
		addLine(_buildReport, THREE, BLANK_LINE); // add line when in very detailed report mode
		addLine(_buildReport, THREE, MessageFormat.format(Bundle.getMessage("blockDepartureHasBlocks"), new Object[] {
				_departStageTrack.getName(), _numOfBlocks.size() }));

		Enumeration<String> en = _numOfBlocks.keys();
		while (en.hasMoreElements()) {
			String locId = en.nextElement();
			int numCars = _numOfBlocks.get(locId);
			String locName = "";
			Location l = locationManager.getLocationById(locId);
			if (l != null)
				locName = l.getName();
			addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("blockFromHasCars"), new Object[] {
					locId, locName, numCars }));
			if (_numOfBlocks.size() < 2) {
				addLine(_buildReport, SEVEN, Bundle.getMessage("blockUnable"));
				return;
			}
		}
		blockByLocationMoves();
		addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("blockDone"),
				new Object[] { _departStageTrack.getName() }));
	}

	/**
	 * Blocks cars out of staging by assigning the largest blocks of cars to locations requesting the most moves.
	 */
	private void blockByLocationMoves() throws BuildFailedException {
		// start at the second location in the route to begin blocking
		List<RouteLocation> routeList = _train.getRoute().getLocationsBySequenceList();
		for (RouteLocation rl : routeList) {
			int possibleMoves = rl.getMaxCarMoves() - rl.getCarMoves();
			if (rl.isDropAllowed() && possibleMoves > 0) {
				addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("blockLocationHasMoves"),
						new Object[] { rl.getName(), possibleMoves }));
			}
		}
		// now block out cars, send the largest block of cars to the locations requesting the greatest number of moves
		RouteLocation rl = _train.getTrainDepartsRouteLocation();
		while (true) {
			String blockId = getLargestBlock(); // get the id of the largest block of cars
			if (blockId.equals("") || _numOfBlocks.get(blockId) == 1)
				break; // done
			RouteLocation rld = getLocationWithMaximumMoves(routeList, blockId); // get the location with the greatest
																					// number of moves
			if (rld == null)
				break; // done
			// check to see if there are enough moves for all of the cars departing staging
			if (rld.getMaxCarMoves() > _numOfBlocks.get(blockId)) {
				// remove the largest block and maximum moves RouteLocation from the lists
				_numOfBlocks.remove(blockId);
				// block 0 cars have never left staging.
				if (blockId.equals("0"))
					continue;
				routeList.remove(rld);
				Location loc = locationManager.getLocationById(blockId);
				Location setOutLoc = rld.getLocation();
				if (loc != null && setOutLoc != null && checkDropTrainDirection(rld)) {
					for (_carIndex = 0; _carIndex < _carList.size(); _carIndex++) {
						Car car = _carList.get(_carIndex);
						if (car.getTrack() == _departStageTrack && car.getLastLocationId().equals(blockId)) {
							if (car.getDestination() != null) {
								addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("blockNotAbleDest"),
										new Object[] { car.toString(), car.getDestinationName() }));
								continue;
							}
							if (car.getFinalDestination() != null) {
								addLine(_buildReport, SEVEN, MessageFormat.format(Bundle
										.getMessage("blockNotAbleFinalDest"), new Object[] { car.toString(),
										car.getFinalDestination().getName() }));
								continue;
							}
							if (!car.getLoadName().equals(CarLoads.instance().getDefaultEmptyName())
									&& !car.getLoadName().equals(CarLoads.instance().getDefaultLoadName())) {
								addLine(_buildReport, SEVEN, MessageFormat.format(Bundle
										.getMessage("blockNotAbleCustomLoad"), new Object[] { car.toString(),
										car.getLoadName() }));
								continue;
							}
							if (car.getLoadName().equals(CarLoads.instance().getDefaultEmptyName())
									&& (_departStageTrack.isAddCustomLoadsEnabled()
											|| _departStageTrack.isAddCustomLoadsAnySpurEnabled() || _departStageTrack
												.isAddCustomLoadsAnyStagingTrackEnabled())) {
								addLine(_buildReport, SEVEN, MessageFormat.format(Bundle
										.getMessage("blockNotAbleCarTypeGenerate"), new Object[] { car.toString(),
										car.getLoadName() }));
								continue;
							}
							addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("blockingCar"),
									new Object[] { car.toString(), loc.getName(), rld.getName() }));
							if (!findDestinationAndTrack(car, rl, rld)) {
								addLine(_buildReport, SEVEN, MessageFormat.format(Bundle
										.getMessage("blockNotAbleCarType"), new Object[] { car.toString(),
										rld.getName(), car.getTypeName() }));
							}
						}
					}
				}
			} else {
				addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("blockDestNotEnoughMoves"),
						new Object[] { rl.getName(), blockId }));
				_numOfBlocks.remove(blockId); // block is too large for any stop along this train's route
			}
		}
	}

	private String getLargestBlock() {
		Enumeration<String> en = _numOfBlocks.keys();
		String largestBlock = "";
		int maxCars = 0;
		while (en.hasMoreElements()) {
			String locId = en.nextElement();
			if (_numOfBlocks.get(locId) > maxCars) {
				largestBlock = locId;
				maxCars = _numOfBlocks.get(locId);
			}
		}
		return largestBlock;
	}

	/**
	 * Returns the routeLocation with the most available moves.
	 * 
	 * @param routeList
	 *            The route for this train.
	 * @param blockId
	 *            Where these cars were originally picked up from.
	 * @return The location in the route with the most available moves.
	 */
	private RouteLocation getLocationWithMaximumMoves(List<RouteLocation> routeList, String blockId) {
		RouteLocation rlMax = null;
		int maxMoves = 0;
		for (RouteLocation rl : routeList) {
			if (rl.getMaxCarMoves() - rl.getCarMoves() > maxMoves) {
				maxMoves = rl.getMaxCarMoves() - rl.getCarMoves();
				rlMax = rl;
			}
			// if two locations have the same number of moves, return the one that doesn't match the block id
			if (rl.getMaxCarMoves() - rl.getCarMoves() == maxMoves && !rl.getLocation().getId().equals(blockId)) {
				rlMax = rl;
			}
		}
		return rlMax;
	}

	boolean multipass = false;

	/**
	 * Main routine to place cars into the train. Can be called multiple times, percent controls how many cars are
	 * placed in any given pass.
	 */
	private void placeCars(int percent) throws BuildFailedException {
		if (percent < 100) {
			addLine(_buildReport, THREE, MessageFormat.format(Bundle.getMessage("buildMultiplePass"),
					new Object[] { percent }));
			multipass = true;
		}
		if (percent == 100 && multipass) {
			addLine(_buildReport, SEVEN, BLANK_LINE); // add line when in very detailed report mode
			addLine(_buildReport, THREE, Bundle.getMessage("buildFinalPass"));
		}
		// now go through each location starting at departure and place cars as requested
		for (int routeIndex = 0; routeIndex < _routeList.size(); routeIndex++) {
			RouteLocation rl = _routeList.get(routeIndex);
			if (_train.skipsLocation(rl.getId())) {
				addLine(_buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildLocSkipped"), new Object[] {
						rl.getName(), rl.getId(), _train.getName() }));
				continue;
			}
			if (!rl.isPickUpAllowed()) {
				addLine(_buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildLocNoPickups"), new Object[] {
						_train.getRoute().getName(), rl.getId(), rl.getName() }));
				continue;
			}
			// no pick ups from staging if at the end of the train's route
			if (routeIndex > 0 && rl.getLocation().getLocationOps() == Location.STAGING) {
				log.debug("No pick ups from terminus staging");
				continue;
			}
			// the next check provides a build report message if there's an issue with the train direction
			if (!checkPickUpTrainDirection(rl)) {
				continue;
			}
			_completedMoves = 0; // the number of moves completed for this location
			_success = true; // true when done with this location
			_reqNumOfMoves = rl.getMaxCarMoves() - rl.getCarMoves(); // the number of moves requested
			int saveReqMoves = _reqNumOfMoves; // save a copy for status message
			// multiple pass build?
			if (percent < 100) {
				_reqNumOfMoves = _reqNumOfMoves * percent / 100;
				// Departing staging?
				if (routeIndex == 0 && _departStageTrack != null) {
					_reqNumOfMoves = 0; // Move cars out of staging after working other locations
					// if leaving and returning to staging on the same track temporary pull cars off the track
					if (_departStageTrack == _terminateStageTrack && !_train.isAllowReturnToStagingEnabled()
							&& !Setup.isAllowReturnToStagingEnabled()) {
						for (Car car : _carList) {
							if (car.getTrack() == _departStageTrack)
								car.setLocation(car.getLocation(), null);
						}
					}
					addLine(_buildReport, THREE, MessageFormat.format(Bundle.getMessage("buildDepartStagingAggressive"),
							new Object[] { _departStageTrack.getLocation().getName() }));
				}
				// restore departure track for cars departing staging
			} else if (_departStageTrack != null && _departStageTrack == _terminateStageTrack
					&& !_train.isAllowReturnToStagingEnabled() && !Setup.isAllowReturnToStagingEnabled()) {
				for (Car car : _carList) {
					if (car.getTrack() == null)
						car.setLocation(car.getLocation(), _departStageTrack, true); // force
				}
			}
			addLine(_buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildLocReqMoves"), new Object[] {
					rl.getName(), rl.getId(), _reqNumOfMoves, saveReqMoves, rl.getMaxCarMoves() }));
			addLine(_buildReport, FIVE, BLANK_LINE); // add line when in detailed report mode
			findDestinationsForCarsFromLocation(rl, routeIndex, false);
			// perform a another pass if aggressive and there are requested moves
			// this will perform local moves at this location, services off spot tracks
			// only in aggressive mode, and at least one car has a new destination
			if (Setup.isBuildAggressive() && saveReqMoves != _reqNumOfMoves) {
				log.debug("Perform extra pass at location ({})", rl.getName());
				// use up to half of the available moves left for this location
				if (_reqNumOfMoves < (rl.getMaxCarMoves() - rl.getCarMoves()) / 2)
					_reqNumOfMoves = (rl.getMaxCarMoves() - rl.getCarMoves()) / 2;
				findDestinationsForCarsFromLocation(rl, routeIndex, true);
			}

			// we might have freed up space at a spur that has an alternate track
			if (redirectCarsFromAlternateTrack())
				addLine(_buildReport, SEVEN, BLANK_LINE); // add line when in very detailed report mode
			if (routeIndex == 0)
				checkDepartureForStaging(percent); // report ASAP that the build has failed

			addLine(_buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildStatusMsg"), new Object[] {
					(_success ? Bundle.getMessage("Success") : Bundle.getMessage("Partial")),
					Integer.toString(_completedMoves), Integer.toString(saveReqMoves), rl.getName(), _train.getName() }));

			reportCarsNotMoved(rl, percent);
		}
		checkDepartureForStaging(percent); // covers the cases: no pick ups, wrong train direction and train skips,
	}

	/**
	 * Attempts to find a destinations for cars departing a specific route location.
	 * 
	 * @param rl
	 *            The route location to search for cars.
	 * @param routeIndex
	 *            Where in the route to add cars to this train.
	 * @param isSecondPass
	 *            When true this is the second time we've looked at these cars.
	 * @throws BuildFailedException
	 */
	private void findDestinationsForCarsFromLocation(RouteLocation rl, int routeIndex, boolean isSecondPass)
			throws BuildFailedException {
		if (_reqNumOfMoves <= 0) {
			_carIndex = 0;	// see reportCarsNotMoved(rl, percent)
			return;
		}
		boolean messageFlag = true;
		_success = false;
		for (_carIndex = 0; _carIndex < _carList.size(); _carIndex++) {
			Car car = _carList.get(_carIndex);
			// second pass only cares about cars that have a final destination equal to this location
			if (isSecondPass && !car.getFinalDestinationName().equals(rl.getName()))
				continue;
			// find a car at this location
			if (!car.getLocationName().equals(rl.getName()))
				continue;
			// add message that we're on the second pass for this location
			if (isSecondPass && messageFlag) {
				messageFlag = false;
				addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildExtraPassForLocation"),
						new Object[] { rl.getName() }));
				addLine(_buildReport, SEVEN, BLANK_LINE); // add line when in very detailed report mode
			}
			// can this car be picked up?
			if (!checkPickUpTrainDirection(car, rl)) {
				addLine(_buildReport, SEVEN, BLANK_LINE); // add line when in very detailed report mode
				continue; // no
			}
			// check for car order?
			car = getCarOrder(car);
			// is car departing staging and generate custom load?
			if (!generateCarLoadFromStaging(car)) {
				if (!generateCarLoadStagingToStaging(car) && car.getTrack() == _departStageTrack
						&& !_departStageTrack.shipsLoad(car.getLoadName(), car.getTypeName())) {
					// build failure car departing staging with a restricted load
					addLine(_buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildErrorCarStageLoad"),
							new Object[] { car.toString(), car.getLoadName(), _departStageTrack.getName() }));
					addLine(_buildReport, SEVEN, BLANK_LINE); // add line when in very detailed report mode
					continue;
				}
			}
			// does car have a custom load without a destination?
			// If departing staging, a destination for this car is needed.
			if (findFinalDestinationForCarLoad(car) && car.getDestination() == null
					&& car.getTrack() != _departStageTrack) {
				// done with this car, it has a custom load, and there are spurs/schedules, but no destination found
				addLine(_buildReport, SEVEN, BLANK_LINE); // add line when in very detailed report mode
				continue;
			}
			// does car have a final destination, but no destination
			if (car.getFinalDestination() != null && car.getDestination() == null) {
				// no local moves for this train?
				if (!_train.isAllowLocalMovesEnabled()
						&& splitString(car.getLocationName()).equals(splitString(car.getFinalDestinationName()))
						&& car.getTrack() != _departStageTrack) {
					addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildCarHasFinalDestNoMove"),
							new Object[] { car.toString(), car.getFinalDestinationName() }));
					addLine(_buildReport, SEVEN, BLANK_LINE); // add line when in very detailed report mode
					log.debug("Removing car ({}) from list", car.toString());
					_carList.remove(car);
					_carIndex--;
					continue;
				}
				// no through traffic from origin to terminal?
				if (!_train.isAllowThroughCarsEnabled() && !_train.isLocalSwitcher() && !car.isCaboose()
						&& !car.hasFred() && !car.isPassenger()
						&& splitString(car.getLocationName()).equals(splitString(_departLocation.getName()))
						&& splitString(car.getFinalDestinationName()).equals(splitString(_terminateLocation.getName()))
						&& !splitString(car.getLocationName()).equals(splitString(car.getFinalDestinationName()))) {
					addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildCarHasFinalDestination"),
							new Object[] { car.toString(), car.getLocationName(), car.getFinalDestinationName() }));
					addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildThroughTrafficNotAllow"),
							new Object[] { _departLocation.getName(), _terminateLocation.getName() }));
					// don't remove car from list if departing staging
					if (car.getTrack() == _departStageTrack) {
						addLine(_buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildErrorCarStageDest"),
								new Object[] { car.toString() }));
					} else {
						log.debug("Removing car ({}) from list", car.toString());
						_carList.remove(car);
						_carIndex--;
					}
					addLine(_buildReport, SEVEN, BLANK_LINE); // add line when in very detailed report mode
					continue;
				}
				addLine(_buildReport, FIVE, MessageFormat
						.format(Bundle.getMessage("buildCarRoutingBegins"), new Object[] { car.toString(),
								car.getTypeName(), car.getLoadName(), car.getLocationName(), car.getTrackName(),
								car.getFinalDestinationName(), car.getFinalDestinationTrackName() }));
				// does the car have a final destination track that is willing to service the car?
				if (car.getFinalDestinationTrack() != null && car.getFinalDestinationTrack().getScheduleMode() == Track.MATCH) {
					String status = car.testDestination(car.getFinalDestination(), car.getFinalDestinationTrack());
					if (!status.equals(Track.OKAY) && !status.startsWith(Track.LENGTH)
							&& !(status.contains(Track.CUSTOM) && status.contains(Track.LOAD))) {
						addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildNoDestTrackNewLoad"),
								new Object[] { car.getFinalDestination().getName(),
										car.getFinalDestinationTrack().getName(), car.toString(), car.getLoadName(),
										status }));
						// is this car or kernel being sent to a track that is too short?
						if (status.startsWith(Track.CAPACITY)) {
							addLine(_buildReport, SEVEN, MessageFormat.format(Bundle
									.getMessage("buildRemovingFinalDestination"), new Object[] {
									car.getFinalDestinationTrack().getName(), car.toString(),
									car.getFinalDestination().getName(), car.getFinalDestinationTrack().getName() }));
							car.setFinalDestination(null);
						car.setFinalDestinationTrack(null);
						}
						else if (car.getTrack() == _departStageTrack) {
							addLine(_buildReport, ONE, MessageFormat.format(
									Bundle.getMessage("buildErrorCarStageDest"), new Object[] { car.toString() }));
						} else {
							log.debug("Removing car ({}) from list", car.toString());
							_carList.remove(car);
							_carIndex--;
						}
						addLine(_buildReport, SEVEN, BLANK_LINE); // add line when in very detailed report mode
						continue;
					}
				}
				if (!Router.instance().setDestination(car, _train, _buildReport)) {
					addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildNotAbleToSetDestination"),
							new Object[] { car.toString(), Router.instance().getStatus() }));
					// don't move car if routing issue was track space but not departing staging
					// if ((!Router.instance().getStatus().startsWith(Track.LENGTH) && !Router.instance()
					// .getStatus().startsWith(Track.CAPACITY))
					if ((!Router.instance().getStatus().startsWith(Track.LENGTH) && !_train
							.isServiceAllCarsWithFinalDestinationsEnabled())
							|| (car.getLocationName().equals(_departLocation.getName()) && _departStageTrack != null))
						// move this car, routing failed!
						findDestinationAndTrack(car, rl, routeIndex, _routeList.size());
					else
						addLine(_buildReport, SEVEN, BLANK_LINE); // add line when in very detailed report mode
				} else {
					// did the router assign a destination?
					if (!checkCarForDestinationAndTrack(car, rl, routeIndex) && car.getTrack() != _departStageTrack) {
						log.debug("Skipping car ({}) no car destination", car.toString()); // NOI18N
						addLine(_buildReport, SEVEN, BLANK_LINE); // add line when in very detailed report mode
						continue;
					} else {
						// if departing staging try and find a destination for this car
						if (car.getDestination() == null && car.getTrack() == _departStageTrack
								&& _terminateStageTrack != null) {
							log.debug(
									"Car ({}) departing staging with final destination ({}) but no destination, try staging", // NOI18N
									car.toString(), car.getFinalDestinationName());
							findDestinationAndTrack(car, rl, _train.getTrainTerminatesRouteLocation());
						}
					}
				}
			}
			// does car have a destination?
			else if (checkCarForDestinationAndTrack(car, rl, routeIndex)) {
				// car does not have a destination, search for the best one
			} else {
				findDestinationAndTrack(car, rl, routeIndex, _routeList.size());
			}
			if (_success) {
				// log.debug("done with location ("+destinationSave.getName()+")");
				break;
			}
			// build failure if car departing staging without a destination and a train
			// we'll just put out a warning message here so we can find out how many cars have issues
			if (car.getLocationName().equals(_departLocation.getName()) && _departStageTrack != null
					&& (car.getDestination() == null || car.getDestinationTrack() == null || car.getTrain() == null)) {
				addLine(_buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildErrorCarStageDest"),
						new Object[] { car.toString() }));
				// does the car has a final destination track going into staging? If so we need to reset this car
				if (car.getFinalDestinationTrack() != null && car.getFinalDestinationTrack() == _terminateStageTrack) {
					addLine(_buildReport, THREE, MessageFormat.format(Bundle.getMessage("buildStatingCarHasFinal"),
							new Object[] { car.toString(), car.getFinalDestinationName(),
									car.getFinalDestinationTrackName() }));
					car.reset();
				}
				addLine(_buildReport, SEVEN, BLANK_LINE); // add line when in very detailed report mode
			}
		}
	}

	/**
	 * Checks to see if all cars on a staging track have been given a destination. Throws exception if there's a car
	 * without a destination.
	 */
	private void checkDepartureForStaging(int percent) throws BuildFailedException {
		if (percent != 100)
			return; // only check departure track after last pass is complete
		// is train departing staging?
		if (_departStageTrack == null)
			return; // no, so we're done
		int carCount = 0;
		StringBuffer buf = new StringBuffer();
		// confirm that all cars in staging are departing
		for (_carIndex = 0; _carIndex < _carList.size(); _carIndex++) {
			Car car = _carList.get(_carIndex);
			// build failure if car departing staging without a destination and a train
			if (car.getLocationName().equals(_departLocation.getName())
					&& (car.getDestination() == null || car.getDestinationTrack() == null || car.getTrain() == null)) {
				carCount++;
				if (carCount < 21)
					buf.append(NEW_LINE + " " + car.toString());
			}
		}
		if (carCount > 0) {
			log.debug("{} cars stuck in staging", carCount);
			String msg = MessageFormat.format(Bundle.getMessage("buildStagingCouldNotFindDest"), new Object[] {
					carCount, _departStageTrack.getLocation().getName(), _departStageTrack.getName() });
			throw new BuildFailedException(msg + buf.toString(), BuildFailedException.STAGING);
		}
	}

	private void addEngineToTrain(Engine engine, RouteLocation rl, RouteLocation rld, Track track) {
		_leadEngine = engine;
		if (_train.getLeadEngine() == null)
			_train.setLeadEngine(engine); // load lead engine
		addLine(_buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildEngineAssigned"), new Object[] {
				engine.toString(), rld.getName(), track.getName() }));
		engine.setDestination(track.getLocation(), track);
		int length = Integer.parseInt(engine.getLength()) + Engine.COUPLER;
		int weightTons = engine.getAdjustedWeightTons();
		// engine in consist?
		if (engine.getConsist() != null) {
			length = engine.getConsist().getTotalLength();
			weightTons = engine.getConsist().getAdjustedWeightTons();
			for (Engine cEngine : engine.getConsist().getEngines()) {
				if (cEngine == engine)
					continue;
				addLine(_buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildEngineAssigned"), new Object[] {
						cEngine.toString(), rld.getName(), track.getName() }));
				cEngine.setTrain(_train);
				cEngine.setRouteLocation(rl);
				cEngine.setRouteDestination(rld);
				cEngine.setDestination(track.getLocation(), track, true); // force destination
			}
		}
		// now adjust train length and weight for each location that engines are in the train
		addRsToTrain(engine, rl, rld, track, length, weightTons);
	}

	/**
	 * Add car to train
	 * 
	 * @param car
	 *            The car!
	 * @param rl
	 *            the planned origin for this car
	 * @param rld
	 *            the planned destination for this car
	 * @param track
	 *            the final destination for car
	 * @return true if car was successfully added to train. Also makes the boolean "success" true if location doesn't
	 *         need any more pick ups.
	 */
	private void addCarToTrain(Car car, RouteLocation rl, RouteLocation rld, Track track) {
		addLine(_buildReport, THREE, MessageFormat.format(Bundle.getMessage("buildCarAssignedDest"), new Object[] {
				car.toString(), rld.getName(), track.getName() }));
		car.setDestination(track.getLocation(), track);
		int length = car.getTotalLength();
		int weightTons = car.getAdjustedWeightTons();
		// car could be part of a kernel
		if (car.getKernel() != null) {
			length = car.getKernel().getTotalLength(); // includes couplers
			weightTons = car.getKernel().getAdjustedWeightTons();
			List<Car> kCars = car.getKernel().getCars();
			addLine(_buildReport, THREE, MessageFormat.format(Bundle.getMessage("buildCarPartOfKernel"), new Object[] {
					car.toString(), car.getKernelName(), kCars.size(), car.getKernel().getTotalLength(),
					Setup.getLengthUnit().toLowerCase() }));
			for (Car kCar : kCars) {
				if (kCar == car)
					continue;
				addLine(_buildReport, THREE, MessageFormat.format(Bundle.getMessage("buildCarKernelAssignedDest"),
						new Object[] { kCar.toString(), kCar.getKernelName(), rld.getName(), track.getName() }));
				kCar.setTrain(_train);
				kCar.setRouteLocation(rl);
				kCar.setRouteDestination(rld);
				kCar.setDestination(track.getLocation(), track, true); // force destination
				// save final destination and track values in case of train reset
				kCar.setPreviousFinalDestination(car.getPreviousFinalDestination());
				kCar.setPreviousFinalDestinationTrack(car.getPreviousFinalDestinationTrack());
			}
			car.updateKernel();
		}
		// warn if car's load wasn't generated out of staging
		if (!_train.acceptsLoad(car.getLoadName(), car.getTypeName())) {
			addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildWarnCarDepartStaging"),
					new Object[] { car.toString(), car.getLoadName() }));
		}
		addLine(_buildReport, THREE, BLANK_LINE); // add line when in normal report mode
		_numberCars++; // bump number of cars moved by this train
		_completedMoves++; // bump number of car pick up moves for the location
		_reqNumOfMoves--; // decrement number of moves left for the location
		if (_reqNumOfMoves <= 0)
			_success = true; // done with this location!
		_carList.remove(car);
		_carIndex--; // removed car from list, so backup pointer

		rl.setCarMoves(rl.getCarMoves() + 1);
		if (rl != rld)
			rld.setCarMoves(rld.getCarMoves() + 1);
		// now adjust train length and weight for each location that car is in the train
		addRsToTrain(car, rl, rld, track, length, weightTons);
		return;
	}

	private void addRsToTrain(RollingStock rs, RouteLocation rl, RouteLocation rld, Track track, int length,
			int weightTons) {
		rl.getLocation().setStatusModified();
		rld.getLocation().setStatusModified();
		rs.setTrain(_train);
		rs.setRouteLocation(rl);
		rs.setRouteDestination(rld);
		// now adjust train length and weight for each location that the rolling stock is in the train
		boolean inTrain = false;
		for (RouteLocation r :  _routeList) {
			if (rl == r) {
				inTrain = true;
			}
			if (rld == r) {
				break;
			}
			if (inTrain) {
				r.setTrainLength(r.getTrainLength() + length); // couplers are included
				r.setTrainWeight(r.getTrainWeight() + weightTons);
			}
		}
	}

	private boolean checkPickUpTrainDirection(RollingStock rs, RouteLocation rl) {
		// check that car or engine is located on a track
		if (rs.getTrack() == null) {
			addLine(_buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildErrorRsNoLoc"), new Object[] {
					rs.toString(), rs.getLocationName() }));
			return false;
		}
		if (_train.isLocalSwitcher()) // ignore local switcher direction
			return true;
		if ((rl.getTrainDirection() & rs.getLocation().getTrainDirections() & rs.getTrack().getTrainDirections()) > 0)
			return true;

		// Only track direction can cause the following message. Location direction has already been checked
		addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildRsCanNotPickupUsingTrain"),
				new Object[] { rs.toString(), rl.getTrainDirectionString(), rs.getTrackName() }));
		addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildRsCanNotPickupUsingTrain2"),
				new Object[] { rs.getLocation().getName() }));
		return false;
	}

	private boolean checkPickUpTrainDirection(RouteLocation rl) {
		if (_train.isLocalSwitcher()) // ignore local switcher direction
			return true;
		if ((rl.getTrainDirection() & rl.getLocation().getTrainDirections()) > 0)
			return true;

		addLine(_buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildLocDirection"), new Object[] {
				rl.getName(), rl.getTrainDirectionString() }));
		return false;
	}

	/**
	 * Checks to see if train length would be exceeded if this car was added to the train.
	 * 
	 * @param car
	 *            the car
	 * @param rl
	 *            the planned origin for this car
	 * @param rld
	 *            the planned destination for this car
	 * @return true if car can be added to train
	 */
	private boolean checkTrainLength(Car car, RouteLocation rl, RouteLocation rld) {
		boolean carInTrain = false;
		for (RouteLocation rlt :  _routeList) {
			if (rl == rlt) {
				carInTrain = true;
			}
			if (rld == rlt) {
				carInTrain = false;
			}
			// car can be a kernel so get total length
			int length = car.getTotalLength();
			if (car.getKernel() != null)
				length = car.getKernel().getTotalLength();
			if (carInTrain && rlt.getTrainLength() + length > rlt.getMaxTrainLength()) {
				addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildCanNotPickupCarLength"),
						new Object[] { car.toString(), length, Setup.getLengthUnit().toLowerCase() }));
				addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildCanNotPickupCarLength2"),
						new Object[] { rlt.getMaxTrainLength(), Setup.getLengthUnit().toLowerCase(), rlt.getName(),
								rlt.getId() }));
				return false;
			}
		}
		return true;
	}

	private final boolean ignoreTrainDirectionIfLastLoc = false;

	private boolean checkDropTrainDirection(RollingStock rs, RouteLocation rld, Track track) {
		// local?
		if (_train.isLocalSwitcher())
			return true;
		// is the destination the last location on the route?
		if (ignoreTrainDirectionIfLastLoc && rld == _train.getTrainTerminatesRouteLocation())
			return true; // yes, ignore train direction

		// this location only services trains with these directions
		int serviceTrainDir = rld.getLocation().getTrainDirections();
		if (track != null)
			serviceTrainDir = serviceTrainDir & track.getTrainDirections();

		// is this a car going to alternate track?  Check to see if direct move from alternate to FD track is possible
		if ((rld.getTrainDirection() & serviceTrainDir) > 0 && rs != null && track != null
				&& (track.getTrainDirections() & serviceTrainDir) > 0 && Car.class.isInstance(rs)) {
			Car car = (Car) rs;
			if (car.getFinalDestinationTrack() != null && track == car.getFinalDestinationTrack().getAlternateTrack()
					&& (track.getTrainDirections() & car.getFinalDestinationTrack().getTrainDirections()) == 0) {
				addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildCanNotDropRsUsingTrain"),
						new Object[] { rs.toString(), rld.getTrainDirectionString() }));
				addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildCanNotDropRsUsingTrain4"),
						new Object[] {
								car.getFinalDestinationTrack().getName(),
								formatStringToCommaSeparated(Setup.getDirectionStrings(car.getFinalDestinationTrack()
										.getTrainDirections())),
								car.getFinalDestinationTrack().getAlternateTrack().getName(),
								formatStringToCommaSeparated(Setup.getDirectionStrings(car.getFinalDestinationTrack()
										.getAlternateTrack().getTrainDirections())) }));
				return false;
			}
		}

		if ((rld.getTrainDirection() & serviceTrainDir) > 0) {
			return true;
		}
		if (rs == null) {
			addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildDestinationDoesNotService"),
					new Object[] { rld.getLocation().getName(), rld.getTrainDirectionString() }));
			return false;
		}
		addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildCanNotDropRsUsingTrain"),
				new Object[] { rs.toString(), rld.getTrainDirectionString() }));
		if (track != null)
			addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildCanNotDropRsUsingTrain2"),
					new Object[] { track.getName() }));
		else
			addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildCanNotDropRsUsingTrain3"),
					new Object[] { rld.getLocation().getName() }));
		return false;
	}

	private boolean checkDropTrainDirection(RouteLocation rld) {
		return (checkDropTrainDirection(null, rld, null));
	}

	/**
	 * Determinate if car can be dropped by this train to the track specified.
	 * 
	 * @param car
	 *            the car.
	 * @param track
	 *            the destination track.
	 * @return true if able to drop.
	 */
	private boolean checkTrainCanDrop(Car car, Track track) {
		if (track.getTrackType().equals(Track.INTERCHANGE) || track.getTrackType().equals(Track.SPUR)) {
			if (track.getDropOption().equals(Track.TRAINS) || track.getDropOption().equals(Track.EXCLUDE_TRAINS)) {
				if (track.acceptsDropTrain(_train)) {
					log.debug("Car ({}) can be droped by train to track ({})", car.toString(), track.getName());
				} else {
					addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildCanNotDropCarTrain"),
							new Object[] { car.toString(), _train.getName(), track.getName() }));
					return false;
				}
			}
			if (track.getDropOption().equals(Track.ROUTES) || track.getDropOption().equals(Track.EXCLUDE_ROUTES)) {
				if (track.acceptsDropRoute(_train.getRoute())) {
					log.debug("Car ({}) can be droped by route to track ({})", car.toString(), track.getName());
				} else {
					addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildCanNotDropCarRoute"),
							new Object[] { car.toString(), _train.getRoute().getName(), track.getName() }));
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Check departure staging track to see if engines and cars are available to a new train. Also confirms that the
	 * engine and car type, load, road, etc. are accepted by the train.
	 * 
	 * @return true is there are engines and cars available.
	 */
	private boolean checkDepartureStagingTrack(Track departStageTrack) {
		if (departStageTrack.getNumberRS() == 0) {
			addLine(_buildReport, THREE, MessageFormat.format(Bundle.getMessage("buildStagingEmpty"),
					new Object[] { departStageTrack.getName() }));
			return false;
		}
		// does the staging track have the right number of locomotives?
		if (_reqNumEngines > 0 && _reqNumEngines != departStageTrack.getNumberEngines()) {
			addLine(_buildReport, THREE, MessageFormat.format(Bundle.getMessage("buildStagingNotEngines"),
					new Object[] { departStageTrack.getName() }));
			return false;
		}
		// is the staging track direction correct for this train?
		if ((departStageTrack.getTrainDirections() & _train.getTrainDepartsRouteLocation().getTrainDirection()) == 0) {
			addLine(_buildReport, THREE, MessageFormat.format(Bundle.getMessage("buildStagingNotDirection"),
					new Object[] { departStageTrack.getName() }));
			return false;
		}
		// does this staging track service this train?
		if (!departStageTrack.acceptsPickupTrain(_train)) {
			addLine(_buildReport, THREE, MessageFormat.format(Bundle.getMessage("buildStagingNotTrain"),
					new Object[] { departStageTrack.getName() }));
			return false;
		}
		if (departStageTrack.getNumberEngines() > 0) {
			List<RollingStock> engs = engineManager.getList();
			for (int i = 0; i < engs.size(); i++) {
				Engine eng = (Engine) engs.get(i);
				if (eng.getTrack() == departStageTrack) {
					// has engine been assigned to another train?
					if (eng.getRouteLocation() != null) {
						addLine(_buildReport, THREE, MessageFormat.format(Bundle.getMessage("buildStagingDepart"),
								new Object[] { departStageTrack.getName(), eng.getTrainName() }));
						return false;
					}
					// does the train accept the engine type from the staging track?
					if (!_train.acceptsTypeName(eng.getTypeName())) {
						addLine(_buildReport, THREE, MessageFormat.format(Bundle
								.getMessage("buildStagingDepartEngineType"), new Object[] { departStageTrack.getName(),
								eng.toString(), eng.getTypeName(), _train.getName() }));
						return false;
					}
					// does the train accept the engine model from the staging track?
					if (!_train.getEngineModel().equals("") && !_train.getEngineModel().equals(eng.getModel())) {
						addLine(_buildReport, THREE, MessageFormat.format(Bundle
								.getMessage("buildStagingDepartEngineModel"), new Object[] {
								departStageTrack.getName(), eng.toString(), eng.getModel(), _train.getName() }));
						return false;
					}
					// does the engine road match the train requirements?
					if (!_train.getRoadOption().equals(Train.ALL_LOADS) && !_train.getEngineRoad().equals("")
							&& !_train.getEngineRoad().equals(eng.getRoadName())) {
						addLine(_buildReport, THREE, MessageFormat.format(Bundle
								.getMessage("buildStagingDepartEngineRoad"), new Object[] { departStageTrack.getName(),
								eng.toString(), eng.getRoadName(), _train.getName() }));
						return false;
					}
					// does the train accept the engine road from the staging track?
					if (_train.getEngineRoad().equals("") && !_train.acceptsRoadName(eng.getRoadName())) {
						addLine(_buildReport, THREE, MessageFormat.format(Bundle
								.getMessage("buildStagingDepartEngineRoad"), new Object[] { departStageTrack.getName(),
								eng.toString(), eng.getRoadName(), _train.getName() }));
						return false;
					}
					// does the train accept the engine owner from the staging track?
					if (!_train.acceptsOwnerName(eng.getOwner())) {
						addLine(_buildReport, THREE, MessageFormat.format(Bundle
								.getMessage("buildStagingDepartEngineOwner"), new Object[] {
								departStageTrack.getName(), eng.toString(), eng.getOwner(), _train.getName() }));
						return false;
					}
					// does the train accept the engine built date from the staging track?
					if (!_train.acceptsBuiltDate(eng.getBuilt())) {
						addLine(_buildReport, THREE, MessageFormat.format(Bundle
								.getMessage("buildStagingDepartEngineBuilt"), new Object[] {
								departStageTrack.getName(), eng.toString(), eng.getBuilt(), _train.getName() }));
						return false;
					}
				}
			}
		}
		boolean foundCaboose = false;
		boolean foundFRED = false;
		if (departStageTrack.getNumberCars() > 0) {
			List<RollingStock> cars = carManager.getList();
			for (int i = 0; i < cars.size(); i++) {
				Car car = (Car) cars.get(i);
				if (car.getTrack() == departStageTrack) {
					// ignore non-lead cars in kernels
					if (car.getKernel() != null && !car.getKernel().isLead(car)) {
						continue; // ignore non-lead cars
					}
					// has car been assigned to another train?
					if (car.getRouteLocation() != null) {
						log.debug("Car ({}) has route location ({})", car.toString(), car.getRouteLocation().getName());
						addLine(_buildReport, THREE, MessageFormat.format(Bundle.getMessage("buildStagingDepart"),
								new Object[] { departStageTrack.getName(), car.getTrainName() }));
						return false;
					}
					// does the train accept the car type from the staging track?
					if (!_train.acceptsTypeName(car.getTypeName())) {
						addLine(_buildReport, THREE, MessageFormat.format(
								Bundle.getMessage("buildStagingDepartCarType"),
								new Object[] { departStageTrack.getName(), car.toString(), car.getTypeName(),
										_train.getName() }));
						return false;
					}
					// does the train accept the car road from the staging track?
					if (!_train.acceptsRoadName(car.getRoadName())) {
						addLine(_buildReport, THREE, MessageFormat.format(
								Bundle.getMessage("buildStagingDepartCarRoad"),
								new Object[] { departStageTrack.getName(), car.toString(), car.getRoadName(),
										_train.getName() }));
						return false;
					}
					// does the train accept the car load from the staging track?
					if (!car.isCaboose()
							&& !car.isPassenger()
							&& (!car.getLoadName().equals(CarLoads.instance().getDefaultEmptyName()) || !departStageTrack
									.isAddCustomLoadsEnabled()
									&& !departStageTrack.isAddCustomLoadsAnySpurEnabled()
									&& !departStageTrack.isAddCustomLoadsAnyStagingTrackEnabled())
							&& !_train.acceptsLoad(car.getLoadName(), car.getTypeName())) {
						addLine(_buildReport, THREE, MessageFormat.format(
								Bundle.getMessage("buildStagingDepartCarLoad"),
								new Object[] { departStageTrack.getName(), car.toString(), car.getLoadName(),
										_train.getName() }));
						return false;
					}
					// does the train accept the car owner from the staging track?
					if (!_train.acceptsOwnerName(car.getOwner())) {
						addLine(_buildReport, THREE, MessageFormat.format(Bundle
								.getMessage("buildStagingDepartCarOwner"), new Object[] { departStageTrack.getName(),
								car.toString(), car.getOwner(), _train.getName() }));
						return false;
					}
					// does the train accept the car built date from the staging track?
					if (!_train.acceptsBuiltDate(car.getBuilt())) {
						addLine(_buildReport, THREE, MessageFormat.format(Bundle
								.getMessage("buildStagingDepartCarBuilt"), new Object[] { departStageTrack.getName(),
								car.toString(), car.getBuilt(), _train.getName() }));
						return false;
					}
					// does the car have a destination serviced by this train?
					if (car.getDestination() != null) {
						log.debug("Car ({}) has a destination ({}, {})", car.toString(), car.getDestinationName(), car.getDestinationTrackName());
						if (!_train.services(car)) {
							addLine(_buildReport, THREE, MessageFormat.format(Bundle
									.getMessage("buildStagingDepartCarDestination"), new Object[] {
									departStageTrack.getName(), car.toString(), car.getDestinationName(),
									_train.getName() }));
							return false;
						}
					}
					// is this car a caboose with the correct road for this train?
					if (car.isCaboose()
							&& (_train.getCabooseRoad().equals("") || _train.getCabooseRoad().equals(car.getRoadName())))
						foundCaboose = true;
					// is this car have a FRED with the correct road for this train?
					if (car.hasFred()
							&& (_train.getCabooseRoad().equals("") || _train.getCabooseRoad().equals(car.getRoadName())))
						foundFRED = true;
				}
			}
		}
		// does the train require a caboose and did we find one from staging?
		if ((_train.getRequirements() & Train.CABOOSE) == Train.CABOOSE && !foundCaboose) {
			addLine(_buildReport, THREE, MessageFormat.format(Bundle.getMessage("buildStagingNoCaboose"), new Object[] {
					departStageTrack.getName(), _train.getCabooseRoad() }));
			return false;
		}
		// does the train require a car with FRED and did we find one from staging?
		if ((_train.getRequirements() & Train.FRED) == Train.FRED && !foundFRED) {
			addLine(_buildReport, THREE, MessageFormat.format(Bundle.getMessage("buildStagingNoCarFRED"), new Object[] {
					departStageTrack.getName(), _train.getCabooseRoad() }));
			return false;
		}
		addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildTrainCanDepartTrack"), new Object[] {
				_train.getName(), departStageTrack.getName() }));
		return true;
	}

	/**
	 * Checks to see if staging track can accept train.
	 * 
	 * @return true if staging track is empty, not reserved, and accepts car and engine types, roads, and loads.
	 */
	private boolean checkTerminateStagingTrack(Track terminateStageTrack) {
		// In normal mode, find a completely empty track. In aggressive mode, a track that scheduled to depart is okay
		if (((!Setup.isBuildAggressive() || !Setup.isStagingTrackImmediatelyAvail()) && terminateStageTrack
				.getNumberRS() != 0)
				|| terminateStageTrack.getNumberRS() != terminateStageTrack.getPickupRS()) {
			addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildStagingTrackOccupied"),
					new Object[] { terminateStageTrack.getName(), terminateStageTrack.getNumberEngines(),
							terminateStageTrack.getNumberCars() }));
			return false;
		}
		if (terminateStageTrack.getDropRS() != 0) {
			addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildStagingTrackReserved"),
					new Object[] { terminateStageTrack.getName(), terminateStageTrack.getDropRS() }));
			return false;
		}
		if (terminateStageTrack.getPickupRS() > 0) {
			addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildStagingTrackDepart"),
					new Object[] { terminateStageTrack.getName() }));
		}
		if (!terminateStageTrack.acceptsDropTrain(_train)) {
			addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildStagingNotTrain"),
					new Object[] { terminateStageTrack.getName() }));
			return false;
			// if track is setup to accept a specific train or route, then ignore other track restrictions
		} else if (terminateStageTrack.getDropOption().equals(Track.TRAINS)
				|| terminateStageTrack.getDropOption().equals(Track.ROUTES)) {
			addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildTrainCanTerminateTrack"),
					new Object[] { _train.getName(), terminateStageTrack.getName() }));
			return true; // train can drop to this track, ignore other track restrictions
		}
		if (!Setup.isTrainIntoStagingCheckEnabled()) {
			addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildTrainCanTerminateTrack"),
					new Object[] { _train.getName(), terminateStageTrack.getName() }));
			return true;
		}
		if (!checkTerminateStagingTrackRestrications(terminateStageTrack)) {
			addLine(_buildReport, SEVEN, Bundle.getMessage("buildOptionRestrictStaging"));
			return false;
		}
		return true;
	}

	private boolean checkTerminateStagingTrackRestrications(Track terminateStageTrack) {
		// check go see if location/track will accept the train's car and engine types
		for (String name : _train.getTypeNames()) {
			if (!_terminateLocation.acceptsTypeName(name)) {
				addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildDestinationType"),
						new Object[] { _terminateLocation.getName(), name }));
				return false;
			}
			if (!terminateStageTrack.acceptsTypeName(name)) {
				addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildStagingTrackType"),
						new Object[] { terminateStageTrack.getName(), name }));
				return false;
			}
		}
		// check go see if track will accept the train's car and engine roads
		if (_train.getRoadOption().equals(Train.ALL_ROADS)
				&& !terminateStageTrack.getRoadOption().equals(Track.ALL_ROADS)) {
			addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildStagingTrackAllRoads"),
					new Object[] { terminateStageTrack.getName() }));
			return false;

		} else if (_train.getRoadOption().equals(Train.INCLUDE_ROADS)) {
			for (String name : _train.getRoadNames()) {
				if (!terminateStageTrack.acceptsRoadName(name)) {
					addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildStagingTrackRoad"),
							new Object[] { terminateStageTrack.getName(), name }));
					return false;
				}
			}
		} else if (_train.getRoadOption().equals(Train.EXCLUDE_ROADS)) {
			List<String> roads = new ArrayList<String>();
			for (String road : CarRoads.instance().getNames()) {
				roads.add(road);
			}
			for (String road : _train.getRoadNames()) {
				roads.remove(road);
			}
			for (String road : roads) {
				if (!terminateStageTrack.acceptsRoadName(road)) {
					addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildStagingTrackRoad"),
							new Object[] { terminateStageTrack.getName(), road }));
					return false;
				}
			}
		}
		// check go see if track will accept the train's car loads
		if (_train.getLoadOption().equals(Train.ALL_LOADS)
				&& !terminateStageTrack.getLoadOption().equals(Track.ALL_LOADS)) {
			addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildStagingTrackAllLoads"),
					new Object[] { terminateStageTrack.getName() }));
			return false;

		} else if (_train.getLoadOption().equals(Train.INCLUDE_LOADS)) {
			for (String load : _train.getLoadNames()) {
				String loadParts[] = load.split(CarLoad.SPLIT_CHAR); // split load name
				if (loadParts.length > 1) {
					if (!terminateStageTrack.acceptsLoad(loadParts[1], loadParts[0])) {
						addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildStagingTrackLoad"),
								new Object[] { terminateStageTrack.getName(), load }));
						return false;
					}
				} else {
					if (!terminateStageTrack.acceptsLoadName(load)) {
						addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildStagingTrackLoad"),
								new Object[] { terminateStageTrack.getName(), load }));
						return false;
					}
				}
			}
		} else if (_train.getLoadOption().equals(Train.EXCLUDE_LOADS)) {
			// build a list of loads that the staging track must accept
			List<String> loads = new ArrayList<String>();
			for (String type : _train.getTypeNames()) {
				for (String load : CarLoads.instance().getNames(type)) {
					if (!loads.contains(load))
						loads.add(load);
				}
			}
			// remove the loads that the train won't carry
			for (String load : _train.getLoadNames()) {
				loads.remove(load);
			}
			for (String load : loads) {
				String loadParts[] = load.split(CarLoad.SPLIT_CHAR); // split load name
				if (loadParts.length > 1) {
					if (!terminateStageTrack.acceptsLoad(loadParts[1], loadParts[0])) {
						addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildStagingTrackLoad"),
								new Object[] { terminateStageTrack.getName(), load }));
						return false;
					}
				} else {
					if (!terminateStageTrack.acceptsLoadName(load)) {
						addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildStagingTrackLoad"),
								new Object[] { terminateStageTrack.getName(), load }));
						return false;
					}
				}
			}
		}
		addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildTrainCanTerminateTrack"),
				new Object[] { _train.getName(), terminateStageTrack.getName() }));
		return true;
	}

	/**
	 * Find the final destination and track for a car with a custom load. Car doesn't have a destination or final
	 * destination. There's a check to see if there's a spur/ schedule for this car. Returns true if a schedule was
	 * found.
	 * 
	 * @param car
	 *            the car with the load
	 * @return true if there's a schedule that can be routed to for this car and load
	 * @throws BuildFailedException
	 */
	private boolean findFinalDestinationForCarLoad(Car car) throws BuildFailedException {
		boolean routeToSpurFound = false;
		if (car.getLoadName().equals(CarLoads.instance().getDefaultEmptyName())
				|| car.getLoadName().equals(CarLoads.instance().getDefaultLoadName()) || car.getDestination() != null
				|| car.getFinalDestination() != null)
			return false; // no schedule found for this car
		addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildSearchForSpur"),
				new Object[] { car.toString(), car.getTypeName(), car.getLoadName(),
						car.getLocationName() + ", " + car.getTrackName() }));
		if (car.getKernel() != null) {
			addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildCarPartOfKernel"),
					new Object[] { car.toString(), car.getKernelName(), car.getKernel().getSize(),
							car.getKernel().getTotalLength(), Setup.getLengthUnit().toLowerCase() }));
		}
		List<Track> tracks = locationManager.getTracksByMoves(Track.SPUR);
		log.debug("Found {} spurs", tracks.size());
		List<Location> locations = new ArrayList<Location>(); // locations not reachable
		for (Track track : tracks) {
			if (car.getTrack() == track || track.getSchedule() == null)
				continue;
			if (locations.contains(track.getLocation()))
				continue;
			if (!car.getTrack().acceptsDestination(track.getLocation())) {
				addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildDestinationNotServiced"),
						new Object[] { track.getLocation().getName(), car.getTrackName() }));
				locations.add(track.getLocation());
				continue;
			}
			if (!_train.isAllowLocalMovesEnabled()
					&& splitString(car.getLocationName()).equals(splitString(track.getLocation().getName()))) {
				log.debug("Skipping track ({}), it would require a local move", track.getName()); // NOI18N
				continue;
			}
			if (!_train.isAllowThroughCarsEnabled() && !_train.isLocalSwitcher() && !car.isCaboose() && !car.hasFred()
					&& !car.isPassenger()
					&& splitString(car.getLocationName()).equals(splitString(_departLocation.getName()))
					&& splitString(track.getLocation().getName()).equals(splitString(_terminateLocation.getName()))
					&& !splitString(_departLocation.getName()).equals(splitString(_terminateLocation.getName()))) {
				log.debug("Skipping track ({}), through cars not allowed to terminal ({})",
						track.getName(), _terminateLocation.getName());
				continue;
			}
			String status = car.testDestination(track.getLocation(), track);
			if (!status.equals(Track.OKAY)) {
				if (track.getScheduleMode() == Track.SEQUENTIAL && status.startsWith(Track.SCHEDULE))
					addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildTrackSequentialMode"),
							new Object[] { track.getName(), track.getLocation().getName(), status }));
				// if the track has an alternate track don't abort if the issue was space
				if (!status.startsWith(Track.LENGTH) || !track.checkSchedule(car).equals(Track.OKAY))
					continue;
				if (track.getAlternateTrack() == null) {
					// report that the spur is full and no alternate
					addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildSpurFullNoAlternate"),
							new Object[] { track.getLocation().getName(), track.getName() }));
					continue;
				} else {
					addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildTrackHasAlternate"),
							new Object[] { track.getLocation().getName(), track.getName(),
									track.getAlternateTrack().getName() }));
					// check to see if alternate and track are configured properly
					if (!_train.isLocalSwitcher() && (track.getTrainDirections() & track.getAlternateTrack().getTrainDirections()) == 0) {
						addLine(_buildReport, SEVEN, MessageFormat.format(Bundle
								.getMessage("buildCanNotDropRsUsingTrain4"), new Object[] {
								track.getName(),
								formatStringToCommaSeparated(Setup.getDirectionStrings(track.getTrainDirections())),
								track.getAlternateTrack().getName(),
								formatStringToCommaSeparated(Setup.getDirectionStrings(track.getAlternateTrack()
										.getTrainDirections())), }));
						continue;
					}
				}
			}

			addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildSetFinalDestination"),
					new Object[] { car.toString(), car.getLoadName(), track.getLocation().getName(), track.getName() }));

			// check the number of in bound cars to this track
			if (!track.isSpaceAvailable(car)) {
				// Now determine if we should move the car or just leave it where it is
				String id = track.getScheduleItemId(); // save the tracks schedule item id
				// determine if this car can be routed to the spur
				car.setFinalDestination(track.getLocation());
				car.setFinalDestinationTrack(track);
				if (Router.instance().setDestination(car, _train, _buildReport))
					routeToSpurFound = true; // if we don't find another spur, keep the car here for now
				car.setDestination(null, null);
				car.setFinalDestination(null);
				car.setFinalDestinationTrack(null);
				track.setScheduleItemId(id); // restore id
				addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildNoDestTrackSpace"),
						new Object[] { car.toString(), track.getLocation().getName(), track.getName(),
								track.getNumberOfCarsInRoute(), track.getReservedInRoute(),
								Setup.getLengthUnit().toLowerCase(), track.getReservationFactor() }));
				continue;
			}
			// try to send car to this spur
			car.setFinalDestination(track.getLocation());
			car.setFinalDestinationTrack(track);
			// test to see if destination is reachable by this train
			if (Router.instance().setDestination(car, _train, _buildReport))
				routeToSpurFound = true; // found a route to the spur
			if (car.getDestination() != null) {
				// is car part of kernel?
				car.updateKernel();
				if (car.getDestination() != track.getLocation()) {
					car.setScheduleId(track.getCurrentScheduleItem().getId());
					track.bumpSchedule();
				}
				return routeToSpurFound; // done, car has a new destination
			}
			addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildNotAbleToSetDestination"),
					new Object[] { car.toString(), Router.instance().getStatus() }));
			car.setFinalDestination(null);
			car.setFinalDestinationTrack(null);
		}
		addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildCouldNotFindSpur"), new Object[] {
				car.toString(), car.getLoadName() }));
		if (routeToSpurFound && !_train.isSendCarsWithCustomLoadsToStagingEnabled()
				&& car.getLocation().getLocationOps() != Location.STAGING) {
			addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildHoldCarVaildRoute"),
					new Object[] { car.toString(), car.getLocationName(), car.getTrackName() }));
		} else {
			// try and send car to staging
			addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildTrySendCarToStaging"),
					new Object[] { car.toString(), car.getLoadName() }));
			tracks = locationManager.getTracks(Track.STAGING);
			log.debug("Found {} staging tracks", tracks.size());
			while (tracks.size() > 0) {
				// pick a track randomly
				int rnd = (int) (Math.random() * tracks.size());
				Track track = tracks.get(rnd);
				tracks.remove(track);
				log.debug("Staging track ({}, {})", track.getLocation().getName(), track.getName());
				if (track.getLocation() == car.getLocation())
					continue;
				if (locations.contains(track.getLocation()))
					continue;
				if (_terminateStageTrack != null && track.getLocation() == _terminateLocation && track != _terminateStageTrack)
					continue; // ignore other staging tracks at terminus 
				if (!car.getTrack().acceptsDestination(track.getLocation())) {
					addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildDestinationNotServiced"),
							new Object[] { track.getLocation().getName(), car.getTrackName() }));
					locations.add(track.getLocation());
					continue;
				}
				String status = track.accepts(car);
				if (!status.equals(Track.OKAY) && !status.startsWith(Track.LENGTH)) {
					log.debug("Staging track ({}) can't accept car ({})", track.getName(), car.toString());
					continue;
				}
				addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildStagingCanAcceptLoad"),
						new Object[] { track.getLocation(), track.getName(), car.getLoadName() }));
				// try to send car to staging
				car.setFinalDestination(track.getLocation());
				// test to see if destination is reachable by this train
				if (Router.instance().setDestination(car, _train, _buildReport))
					routeToSpurFound = true; // found a route to staging
				if (car.getDestination() != null) {
					// is car part of kernel?
					car.updateKernel();
					return true;
				}
				locations.add(track.getLocation()); // couldn't route to this staging location
				car.setFinalDestination(null);
			}
			if (locations.isEmpty())
				addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildNoStagingForCarLoad"),
						new Object[] { car.toString(), car.getLoadName() }));
		}
		return routeToSpurFound; // done
	}

	/**
	 * Used to generate a car's load from staging. Search for a spur with a schedule and load car if possible.
	 * 
	 * @param car
	 *            the car
	 * @throws BuildFailedException
	 */
	private boolean generateCarLoadFromStaging(Car car) throws BuildFailedException {
		if (car.getTrack() == null || !car.getTrack().getTrackType().equals(Track.STAGING)
				|| !car.getTrack().isAddCustomLoadsAnySpurEnabled()
				|| !car.getLoadName().equals(CarLoads.instance().getDefaultEmptyName()) || car.getDestination() != null
				|| car.getFinalDestination() != null) {
			log.debug("No load generation for car ({}) isAddLoadsAnySpurEnabled: " // NOI18N
							+ (car.getTrack().isAddCustomLoadsAnySpurEnabled() ? "true": "false") // NOI18N
							+ ", car load ({}) destination ({}) final destination ({})", // NOI18N
					car.toString(), car.getLoadName(), car.getDestinationName(), car.getFinalDestinationName()); // NOI18N
			// if car has a destination or final destination add "no load generated" message to report
			if (car.getTrack() != null && car.getTrack().getTrackType().equals(Track.STAGING)
					&& car.getTrack().isAddCustomLoadsAnySpurEnabled()
					&& car.getLoadName().equals(CarLoads.instance().getDefaultEmptyName()))
				addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildCarNoLoadGenerated"),
						new Object[] { car.toString(), car.getLoadName(), car.getDestinationName(),
								car.getFinalDestinationName() }));
			return false; // no load generated for this car
		}
		addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildSearchTrackNewLoad"), new Object[] {
				car.toString(), car.getTypeName(), car.getLoadName(), car.getLocationName(), car.getTrackName() }));
		if (car.getKernel() != null) {
			addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildCarPartOfKernel"),
					new Object[] { car.toString(), car.getKernelName(), car.getKernel().getSize(),
							car.getKernel().getTotalLength(), Setup.getLengthUnit().toLowerCase() }));
		}
		List<Track> tracks = locationManager.getTracksByMoves(Track.SPUR);
		log.debug("Found {} spurs", tracks.size());
		for (Track track : tracks) {
			ScheduleItem si = getScheduleItem(car, track);
			if (si == null)
				continue; // no match
			// need to set car load so testDestination will work properly
			String oldCarLoad = car.getLoadName();
			car.setLoadName(si.getReceiveLoadName());
			String status = car.testDestination(track.getLocation(), track);
			if (!status.equals(Track.OKAY) && !status.startsWith(Track.LENGTH)) {
				addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildNoDestTrackNewLoad"),
						new Object[] { track.getLocation().getName(), track.getName(), car.toString(),
								si.getReceiveLoadName(), status }));
				// restore car's load
				car.setLoadName(oldCarLoad);
				continue;
			}
			addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildTrySpurLoad"), new Object[] {
					track.getLocation().getName(), track.getName(), car.getLoadName() }));
			if (!track.isSpaceAvailable(car)) {
				addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildNoDestTrackSpace"),
						new Object[] { car.toString(), track.getLocation().getName(), track.getName(),
								track.getNumberOfCarsInRoute(), track.getReservedInRoute(),
								Setup.getLengthUnit().toLowerCase(), track.getReservationFactor() }));
				// restore car's load
				car.setLoadName(oldCarLoad);
				continue;
			}
			car.setFinalDestination(track.getLocation());
			car.setFinalDestinationTrack(track);
			// try routing car
			if (Router.instance().setDestination(car, _train, _buildReport) && car.getDestination() != null) {
				// return car with this custom load and destination
				addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildCreateNewLoadForCar"),
						new Object[] { car.toString(), si.getReceiveLoadName(), track.getLocation().getName(),
								track.getName() }));
				car.setScheduleId(si.getId());
				car.setLoadGeneratedFromStaging(true);
				// is car part of kernel?
				car.updateKernel();
//				track.bumpSchedule();
				return true; // done, car now has a custom load
			}
			addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildCanNotRouteCar"), new Object[] {
					car.toString(), si.getReceiveLoadName(), track.getLocation().getName(), track.getName() }));
			car.setDestination(null, null);
			// restore load and final destination and track
			car.setLoadName(oldCarLoad);
			car.setFinalDestination(null);
			car.setFinalDestinationTrack(null);
		}
		addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildUnableNewLoad"), new Object[] { car
				.toString() }));
		return false; // done, no load generated for this car
	}

	/**
	 * Tries to place a custom load in the car that is departing staging, and may terminate to staging. Tries to create
	 * a custom load that will be accepted by the train's terminal if the terminal is staging. Otherwise, any staging
	 * track is searched for that will accept this car and a custom load.
	 * 
	 * @param car
	 *            the car
	 * @throws BuildFailedException
	 */
	private boolean generateCarLoadStagingToStaging(Car car) throws BuildFailedException {
		if (car.getTrack() == null || !car.getTrack().getTrackType().equals(Track.STAGING)
				|| !car.getTrack().isAddCustomLoadsAnyStagingTrackEnabled()
				|| !car.getLoadName().equals(CarLoads.instance().getDefaultEmptyName()) || car.getDestination() != null
				|| car.getFinalDestination() != null) {
			log.debug("No load generation for car ({}) isAddCustomLoadsAnyStagingTrackEnabled: " // NOI18N
					+ (car.getTrack().isAddCustomLoadsAnySpurEnabled() ? "true" : "false") // NOI18N
					+ ", car load ({}) destination ({}) final destination ({})", // NOI18N
							car.toString(), car.getLoadName(), car.getDestinationName(), car.getFinalDestinationName()); 
			return false;
		}
		List<Track> tracks = locationManager.getTracks(Track.STAGING);
		log.debug("Found {} staging tracks for load generation", tracks.size());
		// list of locations that can't be reached by the router
		List<Location> locationsNotReachable = new ArrayList<Location>();
		while (tracks.size() > 0) {
			// pick a track randomly
			int rnd = (int) (Math.random() * tracks.size());
			Track track = tracks.get(rnd);
			tracks.remove(track);
			log.debug("Try staging track ({}, {})", track.getLocation().getName(), track.getName());
			// find a staging track that isn't at the departure
			if (track.getLocation() == _departLocation && track.getLocation() != _terminateLocation) {
				log.debug("Don't use departure location ({})", track.getLocation().getName());
				continue;
			}
			if (!_train.isAllowThroughCarsEnabled() && track.getLocation() == _terminateLocation) {
				log.debug("Through cars to location ({}) not allowed", track.getLocation().getName());
				continue;
			}
			if (locationsNotReachable.contains(track.getLocation())) {
				log.debug("Location ({}) not reachable", track.getLocation().getName());
				continue;
			}
			// the following method sets the load generated from staging boolean
			if (generateLoadCarDepartingAndTerminatingIntoStaging(car, track)) {
				// try sending car to this destination, but not staging track
				car.setFinalDestination(track.getLocation());
				car.setFinalDestinationTrack(null);
				// test to see if destination is reachable by this train
				if (Router.instance().setDestination(car, _train, _buildReport) && car.getDestination() != null) {
					return true; // done, car has a custom load and a final destination
				}
				addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildStagingTrackNotReachable"),
						new Object[] { track.getLocation().getName(), track.getName(), car.getLoadName() }));
				// return car to original state
				car.setLoadName(CarLoads.instance().getDefaultEmptyName());
				car.setLoadGeneratedFromStaging(false);
				car.setFinalDestination(null);
				car.updateKernel();
				locationsNotReachable.add(track.getLocation()); // couldn't route to this staging location
			}
		}
		// No staging tracks reachable, try the track the train is terminating to
		if (_train.isAllowThroughCarsEnabled()
				&& generateLoadCarDepartingAndTerminatingIntoStaging(car, _terminateStageTrack))
			return true;

		return false;
	}

	/**
	 * Used when generating a car load from staging.
	 * 
	 * @param car
	 *            the car.
	 * @param track
	 *            the car's destination track that has the schedule.
	 * @return ScheduleItem si if match found, null otherwise.
	 * @throws BuildFailedException
	 */
	private ScheduleItem getScheduleItem(Car car, Track track) throws BuildFailedException {
		if (track.getSchedule() == null)
			return null;
		ScheduleItem si = null;
		if (track.getScheduleMode() == Track.SEQUENTIAL) {
			si = track.getCurrentScheduleItem();
			if (si == null)
				throw new BuildFailedException(MessageFormat.format(Bundle.getMessage("buildErrorNoScheduleItem"),
						new Object[] { track.getScheduleItemId(), track.getScheduleName(), track.getName(),
								track.getLocation().getName() }));
			return checkScheduleItem(si, car, track);
		}
		log.debug("Track ({}) in match mode", track.getName());
		for (int i = 0; i < track.getSchedule().getSize(); i++) {
			si = track.getNextScheduleItem();
			if (si == null)
				throw new BuildFailedException(MessageFormat.format(Bundle.getMessage("buildErrorNoScheduleItem"),
						new Object[] { track.getScheduleItemId(), track.getScheduleName(), track.getName(),
								track.getLocation().getName() }));
			si = checkScheduleItem(si, car, track);
			if (si != null) {
				car.setScheduleId(si.getId());
				return si;
			}
		}
		return si;
	}

	/**
	 * Checks a schedule item to see if the car type matches, and the train and track can service the schedule item's
	 * load. This code doesn't check to see if the car's load can be serviced by the schedule. Instead a schedule item
	 * is returned that allows the program to assign a custom load to the car that matches a schedule item. Therefore,
	 * schedule items that request don't request a custom load are ignored.
	 * 
	 * @param si
	 *            the schedule item
	 * @param car
	 *            the car to check
	 * @param track
	 *            the destination track
	 * @return Schedule item si if okay, null otherwise.
	 */
	private ScheduleItem checkScheduleItem(ScheduleItem si, Car car, Track track) {
		if (!car.getTypeName().equals(si.getTypeName()) || si.getReceiveLoadName().equals("")
				|| si.getReceiveLoadName().equals(CarLoads.instance().getDefaultEmptyName())
				|| si.getReceiveLoadName().equals(CarLoads.instance().getDefaultLoadName())) {
			log.debug("Not using track ({}) schedule request type ({}) road ({}) load ({})", 
					track.getName(), si.getTypeName(), si.getRoadName(), si.getReceiveLoadName()); // NOI18N
			return null;
		}
		if (!si.getRoadName().equals("") && !car.getRoadName().equals(si.getRoadName())) {
			log.debug("Not using track ({}) schedule request type ({}) road ({}) load ({})",
					track.getName(), si.getTypeName(), si.getRoadName(), si.getReceiveLoadName()); // NOI18N
			return null;
		}
		if (!_train.acceptsLoad(si.getReceiveLoadName(), si.getTypeName())) {
			addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildTrainNotNewLoad"), new Object[] {
					_train.getName(), si.getReceiveLoadName(), track.getLocation().getName(), track.getName() }));
			return null;
		}
		// does the departure track allow this load?
		if (!car.getTrack().shipsLoad(si.getReceiveLoadName(), car.getTypeName())) {
			addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildTrackNotNewLoad"), new Object[] {
					car.getTrackName(), si.getReceiveLoadName(), track.getLocation().getName(), track.getName() }));
			return null;
		}
		if (!si.getTrainScheduleId().equals("")
				&& !TrainManager.instance().getTrainScheduleActiveId().equals(si.getTrainScheduleId())) {
			log.debug("Schedule item isn't active");
			// build the status message
			TrainSchedule aSch = TrainScheduleManager.instance().getScheduleById(
					TrainManager.instance().getTrainScheduleActiveId());
			TrainSchedule tSch = TrainScheduleManager.instance().getScheduleById(si.getTrainScheduleId());
			String aName = "";
			String tName = "";
			if (aSch != null)
				aName = aSch.getName();
			if (tSch != null)
				tName = tSch.getName();
			addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildScheduleNotActive"), new Object[] {
					track.getName(), si.getId(), tName, aName }));

			return null;
		}
		log.debug("Found a schedule item id ({}) for car ({})", si.getId(), car.toString());
		return si;
	}

	/**
	 * Checks all of the cars on an interchange track and returns the oldest (FIFO) or newest (LIFO) car residing on
	 * that track. Note high priority cars will be serviced first, then low.
	 * 
	 * @param car
	 *            the car being pulled from the interchange track
	 * @return The FIFO car at this interchange
	 */
	private Car getCarOrder(Car car) {
		if (car.getTrack().getServiceOrder().equals(Track.NORMAL))
			return car;
		log.debug("Get {} car ({}) from {} ({}), last moved date: {}", // NOI18N
				car.getTrack().getServiceOrder(), car.toString(), car.getTrack().getTrackType(), car.getTrackName(), 
				car.getLastDate());
		Car bestCar = car;
		for (int i = _carIndex + 1; i < _carList.size(); i++) {
			Car testCar = _carList.get(i);
			if (testCar.getTrack() == car.getTrack()) {
				log.debug("{} car ({}) last moved date: {}", 
						car.getTrack().getTrackType(), testCar.toString(), testCar.getLastDate()); // NOI18N
				if (car.getTrack().getServiceOrder().equals(Track.FIFO)
						&& convertStringDateToDouble(bestCar.getLastDate()) > convertStringDateToDouble(testCar
								.getLastDate()) && bestCar.getLoadPriority().equals(testCar.getLoadPriority())) {
					bestCar = testCar;
					log.debug("New best car ({})", bestCar.toString());
				}
				if (car.getTrack().getServiceOrder().equals(Track.LIFO)
						&& convertStringDateToDouble(bestCar.getLastDate()) < convertStringDateToDouble(testCar
								.getLastDate()) && bestCar.getLoadPriority().equals(testCar.getLoadPriority())) {
					bestCar = testCar;
					log.debug("New best car ({})", bestCar.toString());
				}
			}
		}
		if (car != bestCar)
			addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildTrackModeCarPriority"),
					new Object[] { car.getTrack().getTrackType(), car.getTrackName(), car.getTrack().getServiceOrder(),
							bestCar.toString(), bestCar.getLastDate(), car.toString(), car.getLastDate() }));
		return bestCar;
	}

	private boolean checkCarForDestinationAndTrack(Car car, RouteLocation rl, RouteLocation rld)
			throws BuildFailedException {
		int index;
		for (index = 0; index < _routeList.size(); index++) {
			if (rld == _routeList.get(index))
				break;
		}
		return checkCarForDestinationAndTrack(car, rl, index - 1);
	}

	/**
	 * Checks to see if car has a destination and tries to add car to train
	 * 
	 * @param car
	 * @param rl
	 *            the car's route location
	 * @param routeIndex
	 *            where in the route the car pick up is
	 * @return true if car has a destination.
	 * @throws BuildFailedException
	 *             if destination was staging and can't place car there
	 */
	private boolean checkCarForDestinationAndTrack(Car car, RouteLocation rl, int routeIndex)
			throws BuildFailedException {
		if (car.getDestination() == null)
			return false;
		addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildCarHasAssignedDest"), new Object[] {
				car.toString(), (car.getDestinationName() + ", " + car.getDestinationTrackName()) }));
		RouteLocation rld = _train.getRoute().getLastLocationByName(car.getDestinationName());
		if (rld == null) {
			// car has a destination that isn't serviced by this train (destination loaded by router)
			addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildExcludeCarDestNotPartRoute"),
					new Object[] { car.toString(), car.getDestinationName(), _train.getRoute().getName() }));
			return true; // done
		}
		if (car.getRouteLocation() != null) {
			// The following code should never execute if train was reset before a build!
			addLine(_buildReport, THREE, MessageFormat.format(Bundle.getMessage("buildCarAlreadyAssigned"),
					new Object[] { car.toString() }));
		}
		// now go through the route and try and find a location with
		// the correct destination name
		int locCount = 0;
		for (int k = routeIndex; k < _routeList.size(); k++) {
			rld = _routeList.get(k);
			// if car can be picked up later at same location, skip
			if (checkForLaterPickUp(rl, rld, car)) {
				break;
			}
			if (!rld.getName().equals(car.getDestinationName()))
				continue;
			// is the car's destination the terminal and is that allowed?
			if (!_train.isAllowThroughCarsEnabled() && !_train.isLocalSwitcher() && !car.isCaboose() && !car.hasFred()
					&& !car.isPassenger()
					&& splitString(car.getLocationName()).equals(splitString(_departLocation.getName()))
					&& splitString(car.getDestinationName()).equals(splitString(_terminateLocation.getName()))
					&& !splitString(_departLocation.getName()).equals(splitString(_terminateLocation.getName()))) {
				addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildCarHasDestination"),
						new Object[] { car.toString(), _departLocation.getName(), _terminateLocation.getName() }));
				addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildThroughTrafficNotAllow"),
						new Object[] { _departLocation.getName(), _terminateLocation.getName() }));
				addLine(_buildReport, FIVE, BLANK_LINE); // add line when in detailed report mode
				return true; // done
			}
			locCount++; // show when this car would be dropped at location
			log.debug("Car ({}) found a destination in train's route", car.toString());
			// are drops allows at this location?
			if (!rld.isDropAllowed()) {
				addLine(_buildReport, THREE, MessageFormat.format(Bundle.getMessage("buildRouteNoDropsStop"),
						new Object[] { _train.getRoute().getName(), rld.getName(), rld.getId(), locCount }));
				continue;
			}
			if (rld.getCarMoves() >= rld.getMaxCarMoves()) {
				addLine(_buildReport, THREE, MessageFormat.format(Bundle.getMessage("buildNoAvailableMovesStop"),
						new Object[] { _train.getRoute().getName(), rld.getId(), rld.getName(), locCount }));
				continue;
			}
			// is the train length okay?
			if (!checkTrainLength(car, rl, rld)) {
				continue;
			}
			// check for valid destination track
			if (car.getDestinationTrack() == null) {
				addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildCarDoesNotHaveDest"),
						new Object[] { car.toString() }));
				// is there a destination track assigned for staging cars?
				if (rld == _train.getTrainTerminatesRouteLocation() && _terminateStageTrack != null) {
					String status = car.testDestination(car.getDestination(), _terminateStageTrack);
					if (status.equals(Track.OKAY)) {
						addLine(_buildReport, THREE, MessageFormat.format(
								Bundle.getMessage("buildCarAssignedToStaging"), new Object[] { car.toString(),
										_terminateStageTrack.getName() }));
						addCarToTrain(car, rl, rld, _terminateStageTrack);
						return true;
					} else {
						addLine(_buildReport, SEVEN, MessageFormat.format(Bundle
								.getMessage("buildCanNotDropCarBecause"), new Object[] { car.toString(),
								_terminateStageTrack.getName(), status, _terminateStageTrack.getTrackTypeName() }));
						continue;
					}
					// no staging at this location, now find a destination track this this car
				} else {
					List<Track> tracks = car.getDestination().getTrackByMovesList(null);
					addLine(_buildReport, SEVEN, MessageFormat
							.format(Bundle.getMessage("buildSearchForTrack"), new Object[] { tracks.size(),
									car.getDestinationName(), car.toString(), car.getLoadName() }));
					for (Track testTrack : tracks) {
						// log.debug("track (" +testTrack.getName()+ ") has "+ testTrack.getMoves() + " moves");
						// dropping to the same track isn't allowed
						if (testTrack == car.getTrack()) {
							addLine(_buildReport, SEVEN, MessageFormat.format(Bundle
									.getMessage("buildCanNotDropCarSameTrack"), new Object[] { car.toString(),
									testTrack.getName() }));
							continue;
						}
						// is train direction correct?
						if (!checkDropTrainDirection(car, rld, testTrack))
							continue;
						// drop to interchange or spur?
						if (!checkTrainCanDrop(car, testTrack))
							continue;
						String status = car.testDestination(car.getDestination(), testTrack);
						// is the testTrack a spur with a schedule and alternate track?
						if (!status.equals(Track.OKAY) && status.startsWith(Track.LENGTH)
								&& testTrack.checkSchedule(car).equals(Track.OKAY)
								&& testTrack.getTrackType().equals(Track.SPUR) && testTrack.getAlternateTrack() != null) {
							addLine(_buildReport, SEVEN, MessageFormat.format(Bundle
									.getMessage("buildTrackHasAlternate"), new Object[] {
									testTrack.getLocation().getName(), testTrack.getName(),
									testTrack.getAlternateTrack().getName() }));
							String altStatus = car.testDestination(car.getDestination(), testTrack.getAlternateTrack());
							// A car with a custom load sent to a spur will get a status with "CUSTOM" and "LOAD" embedded,
							// if the spur doesn't have a schedule. Must use contains to determine if both are in the
							// status message.
							// The following code allows the alternate track to be a spur that doesn't have a schedule.
							// TODO most of the other code only allows a yard or interchange track to be the alternate.
							if (altStatus.equals(Track.OKAY)
									|| (altStatus.contains(Track.CUSTOM) && altStatus.contains(Track.LOAD))) {
								addLine(_buildReport, SEVEN, MessageFormat.format(Bundle
										.getMessage("buildUseAlternateTrack"), new Object[] { car.toString(),
										testTrack.getAlternateTrack().getName() }));
								// forward the car to the original destination
								car.setFinalDestination(car.getDestination());
								car.setFinalDestinationTrack(testTrack);
								car.setNextLoadName(car.getLoadName());
								addCarToTrain(car, rl, rld, testTrack.getAlternateTrack());
								testTrack.setMoves(testTrack.getMoves() + 1); // bump the number of moves
								return true;
							} else {
								addLine(_buildReport, SEVEN, MessageFormat.format(Bundle
										.getMessage("buildCanNotDropCarBecause"), new Object[] { car.toString(),
										testTrack.getAlternateTrack().getName(), altStatus,
										testTrack.getTrackTypeName() }));
							}
						}
						if (!status.equals(Track.OKAY)) {
							addLine(_buildReport, SEVEN, MessageFormat.format(Bundle
									.getMessage("buildCanNotDropCarBecause"), new Object[] { car.toString(),
									testTrack.getName(), status, testTrack.getTrackTypeName() }));
							continue;
						}
						addCarToTrain(car, rl, rld, testTrack);
						return true;
					}
				}
				// car has a destination track
			} else {
				log.debug("Car ({}) has a destination track ({})", car.toString(), car.getDestinationTrack().getName());
				// going into the correct staging track?
				if (rld.equals(_train.getTrainTerminatesRouteLocation()) && _terminateStageTrack != null
						&& _terminateStageTrack != car.getDestinationTrack()) {
					// car going to wrong track in staging, change track
					addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildCarDestinationStaging"),
							new Object[] { car.toString(), car.getDestinationName(), car.getDestinationTrackName() }));
					car.setDestination(_terminateStageTrack.getLocation(), _terminateStageTrack);
				}
				if (!rld.equals(_train.getTrainTerminatesRouteLocation()) || _terminateStageTrack == null
						|| _terminateStageTrack == car.getDestinationTrack()) {
					// is train direction correct?
					if (checkDropTrainDirection(car, rld, car.getDestinationTrack())) {
						// drop to interchange or spur?
						if (checkTrainCanDrop(car, car.getDestinationTrack())) {
							String status = car.testDestination(car.getDestination(), car.getDestinationTrack());
							if (status.equals(Track.OKAY)
									&& checkDropTrainDirection(car, rld, car.getDestinationTrack())) {
								addCarToTrain(car, rl, rld, car.getDestinationTrack());
								return true;
							} else {
								addLine(_buildReport, SEVEN, MessageFormat.format(Bundle
										.getMessage("buildCanNotDropCarBecause"), new Object[] { car.toString(),
										car.getDestinationTrackName(), status,
										car.getDestinationTrack().getTrackTypeName() }));
							}
						}
					}
				} else {
					throw new BuildFailedException(MessageFormat.format(
							Bundle.getMessage("buildCarDestinationStaging"), new Object[] { car.toString(),
									car.getDestinationName(), car.getDestinationTrackName() }));
				}
			}
			addLine(_buildReport, THREE, MessageFormat.format(Bundle.getMessage("buildCanNotDropCar"), new Object[] {
					car.toString(), car.getDestinationName(), rld.getId(), locCount }));
			if (car.getDestinationTrack() == null) {
				log.debug("Could not find a destination track for location ({})", car.getDestinationName());
			}
		}
		log.debug("car ({}) not added to train", car.toString());
		addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildDestinationNotReachable"),
				new Object[] { car.getDestinationName(), rl.getName(), rl.getId() }));
		// remove destination and revert to final destination
		if (car.getDestinationTrack() != null) {
			Track destTrack = car.getDestinationTrack();
			// TODO should we leave the car's destination? The spur expects this car!
			if (destTrack.getSchedule() != null && destTrack.getScheduleMode() == Track.SEQUENTIAL) {
				// log.debug("Scheduled delivery to ("+destTrack.getName()+") cancelled");
				addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildPickupCancelled"),
						new Object[] { destTrack.getLocation().getName(), destTrack.getName() }));
			}
		}
		car.setFinalDestination(car.getPreviousFinalDestination());
		car.setFinalDestinationTrack(car.getPreviousFinalDestinationTrack());
		car.setDestination(null, null);
		car.updateKernel();
		// is car sitting on a FIFO or LIFO track?
		if (car.getTrack() != null && !car.getTrack().getServiceOrder().equals(Track.NORMAL)) {
			addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildBypassCarServiceOrder"),
					new Object[] { car.toString(), car.getTrackName(), car.getTrack().getServiceOrder() }));
			// move car id in front of current pointer so car is no longer used on this pass
			_carList.remove(car);
			_carList.add(_carIndex, car);
		}
		addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildNoDestForCar"), new Object[] { car
				.toString() }));
		addLine(_buildReport, FIVE, BLANK_LINE); // add line when in detailed report mode
		return true; // car no longer has a destination, but it had one.
	}

	/**
	 * Find a destination for the car at a specified location.
	 * 
	 * @param car
	 *            the car!
	 * @param rl
	 *            The car's route location
	 * @param rld
	 *            The car's route destination
	 * @return true if successful.
	 * @throws BuildFailedException
	 */
	private boolean findDestinationAndTrack(Car car, RouteLocation rl, RouteLocation rld) throws BuildFailedException {
		int index;
		for (index = 0; index < _routeList.size(); index++) {
			if (rld == _routeList.get(index))
				break;
		}
		return findDestinationAndTrack(car, rl, index - 1, index + 1);
	}

	/**
	 * Find a destination and track for a car, and add the car to the train.
	 * 
	 * @param car
	 *            The car that is looking for a destination and destination track.
	 * @param rl
	 *            The current route location for this car.
	 * @param routeIndex
	 *            Where in the train's route to begin a search for a destination for this car.
	 * @param routeEnd
	 *            Where to stop looking for a destination.
	 * @return true if successful, car has destination, track and a train.
	 * @throws BuildFailedException
	 */
	private boolean findDestinationAndTrack(Car car, RouteLocation rl, int routeIndex, int routeEnd)
			throws BuildFailedException {
		addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildFindDestinationForCar"), new Object[] {
				car.toString(), car.getTypeName(), car.getLoadName(), (car.getLocationName() + ", " + car.getTrackName()) }));
		int start = routeIndex; // start looking after car's current location
		RouteLocation rld = null; // the route location destination being checked for the car
		RouteLocation rldSave = null; // holds the best route location destination for the car
		Track trackSave = null; // holds the best track at destination for the car
		boolean multiplePickup = false; // true when car can be picked up from two or more locations in the route

		// more than one location in this route?
		if (_routeList.size() > 1)
			start++; // yes!, no car drops at departure
		// all pick ups to terminal?
		if (_train.isSendCarsToTerminalEnabled()
				&& !splitString(rl.getName()).equals(splitString(_departLocation.getName()))
				&& routeEnd == _routeList.size()) {
			addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildSendToTerminal"),
					new Object[] { _terminateLocation.getName() }));
			start = routeEnd - 1;
			while (start > routeIndex) {
				if (!splitString(_routeList.get(start - 1).getName()).equals(splitString(_terminateLocation.getName())))
					break;
				start--;
			}
		}
		for (int k = start; k < routeEnd; k++) {
			rld = _routeList.get(k);
			// if car can be picked up later at same location, set flag
			if (checkForLaterPickUp(rl, rld, car)) {
				multiplePickup = true;
			}
			if (rld.isDropAllowed() || car.hasFred() || car.isCaboose()) {
				addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildSearchingLocation"),
						new Object[] { rld.getName(), }));
			} else {
				addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildRouteNoDropLocation"),
						new Object[] { _train.getRoute().getName(), rld.getId(), rld.getName() }));
				continue;
			}
			// get the destination
			Location testDestination = rld.getLocation();
			if (testDestination == null) {
				// The following should never throw, all locations in the route have been already checked
				throw new BuildFailedException(MessageFormat.format(Bundle.getMessage("buildErrorRouteLoc"),
						new Object[] { _train.getRoute().getName(), rld.getName() }));
			}
			// any moves left at this location?
			if (rld.getCarMoves() >= rld.getMaxCarMoves()) {
				addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildNoAvailableMovesDest"),
						new Object[] { _train.getRoute().getName(), rld.getId(), rld.getName() }));
				continue;
			}
			Location destinationTemp = null;
			Track trackTemp = null;

			// don't move car to same location unless the route only has one location (local moves) or is passenger,
			// caboose or car with FRED
			if (splitString(rl.getName()).equals(splitString(rld.getName())) && !_train.isLocalSwitcher()
					&& !car.isPassenger() && !car.isCaboose() && !car.hasFred()) {
				// allow cars to return to the same staging location if no other options (tracks) are available
				if ((_train.isAllowReturnToStagingEnabled() || Setup.isAllowReturnToStagingEnabled())
						&& testDestination.getLocationOps() == Location.STAGING && trackSave == null) {
					addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildReturnCarToStaging"),
							new Object[] { car.toString(), rld.getName() }));
				} else {
					addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildCarLocEqualDestination"),
							new Object[] { car.toString(), rld.getName() }));
					continue;
				}
			}

			// check to see if departure track has any restrictions
			if (!car.getTrack().acceptsDestination(testDestination)) {
				addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildDestinationNotServiced"),
						new Object[] { testDestination.getName(), car.getTrackName() }));
				continue;
			}

			if (!testDestination.acceptsTypeName(car.getTypeName())) {
				addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildCanNotDropLocation"),
						new Object[] { car.toString(), car.getTypeName(), testDestination.getName() }));
				continue;
			}
			// can this location service this train's direction
			if (!checkDropTrainDirection(rld))
				continue;
			// is the train length okay?
			if (!checkTrainLength(car, rl, rld)) {
				break; // done with this route
			}
			// no through traffic from origin to terminal?
			if (!_train.isAllowThroughCarsEnabled() && !_train.isLocalSwitcher() && !car.isCaboose() && !car.hasFred()
					&& !car.isPassenger()
					&& splitString(car.getLocationName()).equals(splitString(_departLocation.getName()))
					&& splitString(rld.getName()).equals(splitString(_terminateLocation.getName()))) {
				addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildThroughTrafficNotAllow"),
						new Object[] { _departLocation.getName(), _terminateLocation.getName() }));
				continue;
			}
			// is there a track assigned for staging cars?
			if (rld == _train.getTrainTerminatesRouteLocation() && _terminateStageTrack != null) {
				// no need to check train and track direction into staging, already done
				String status = car.testDestination(testDestination, _terminateStageTrack);
				if (status.equals(Track.OKAY)) {
					trackTemp = _terminateStageTrack;
					destinationTemp = testDestination;
				// only generate a new load if there aren't any other tracks available for this car
				} else if (status.startsWith(Track.LOAD)
						&& car.getTrack() == _departStageTrack
						&& car.getLoadName().equals(CarLoads.instance().getDefaultEmptyName())
						&& rldSave == null
						&& (_departStageTrack.isAddCustomLoadsAnyStagingTrackEnabled()
								|| _departStageTrack.isAddCustomLoadsEnabled() || _departStageTrack
									.isAddCustomLoadsAnySpurEnabled())) {
					// try and generate a load for this car into staging
					if (generateLoadCarDepartingAndTerminatingIntoStaging(car, _terminateStageTrack)) {
						trackTemp = _terminateStageTrack;
						destinationTemp = testDestination;
					} else {
						addLine(_buildReport, SEVEN, MessageFormat.format(Bundle
								.getMessage("buildCanNotDropCarBecause"), new Object[] { car.toString(),
								_terminateStageTrack.getName(), status, _terminateStageTrack.getTrackTypeName() }));
						continue; // failed to create load
					}
			} else {
					addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildCanNotDropCarBecause"),
							new Object[] { car.toString(), _terminateStageTrack.getName(), status,
									_terminateStageTrack.getTrackTypeName() }));
					continue;
				}
				// no staging track assigned, start track search
			} else {
				for (Track testTrack : testDestination.getTrackByMovesList(null)) {
					// log.debug("track (" +testTrack.getName()+ ") has "+ testTrack.getMoves() + " moves");
					// dropping to the same track isn't allowed
					if (testTrack == car.getTrack() && !car.isPassenger() && !car.isCaboose() && !car.hasFred()) {
						addLine(_buildReport, SEVEN, MessageFormat.format(Bundle
								.getMessage("buildCanNotDropCarSameTrack"), new Object[] { car.toString(),
								testTrack.getName() }));
						continue;
					}
					// Can the train service this track?
					if (!checkDropTrainDirection(car, rld, testTrack))
						continue;
					// drop to interchange or spur?
					if (!checkTrainCanDrop(car, testTrack))
						continue;
					String status = car.testDestination(testDestination, testTrack);
					// is the destination a spur with a schedule demanding this car's custom load?
					if (status.equals(Track.OKAY) && !testTrack.getScheduleId().equals("")
							&& !car.getLoadName().equals(CarLoads.instance().getDefaultEmptyName())
							&& !car.getLoadName().equals(CarLoads.instance().getDefaultLoadName())) {
						addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildSpurScheduleLoad"),
								new Object[] { testTrack.getName(), car.getLoadName() }));
						addCarToTrain(car, rl, rld, testTrack);
						return true;
					}
					// is the destination a spur with a Schedule?
					// And is car departing a staging track that can generate schedule loads?
					if (!status.equals(Track.OKAY)
							&& (!status.startsWith(Track.TYPE)) // can't generate load for spur that doesn't accept this
																// car type
							&& (!status.startsWith(Track.LENGTH)) // can't generate load for spur that is full
							&& testTrack.getTrackType().equals(Track.SPUR)
							&& !testTrack.getScheduleId().equals("")
							&& (car.getTrack().isAddCustomLoadsEnabled() || car.getTrack()
									.isAddCustomLoadsAnySpurEnabled())
							&& car.getLoadName().equals(CarLoads.instance().getDefaultEmptyName())) {
						// can we use this track?
						if (!testTrack.isSpaceAvailable(car)) {
							addLine(_buildReport, SEVEN, MessageFormat.format(
									Bundle.getMessage("buildNoDestTrackSpace"), new Object[] { car.toString(),
											testTrack.getLocation().getName(), testTrack.getName(),
											testTrack.getNumberOfCarsInRoute(), testTrack.getReservedInRoute(),
											Setup.getLengthUnit().toLowerCase(), testTrack.getReservationFactor() }));
							continue; // no
						}
						addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildGenerateLoad"),
								new Object[] { car.toString(), car.getTypeName(), testDestination.getName(),
										testTrack.getName() }));
						String carLoad = car.getLoadName(); // save the car's load
						ScheduleItem si = getScheduleItem(car, testTrack);
						if (si != null) {
							car.setLoadName(si.getReceiveLoadName());
							status = car.testDestination(testDestination, testTrack);
							if (status.equals(Track.OKAY)) {
								addLine(_buildReport, FIVE, MessageFormat.format(Bundle
										.getMessage("buildAddingScheduleLoad"), new Object[] { si.getReceiveLoadName(),
										car.toString() }));
								car.setLoadGeneratedFromStaging(true);
								addCarToTrain(car, rl, rld, testTrack);
								return true;
							}
						}
						car.setLoadName(carLoad); // restore car's load
					}
					// okay to drop car?
					if (!status.equals(Track.OKAY)) {
						addLine(_buildReport, SEVEN, MessageFormat.format(
								Bundle.getMessage("buildCanNotDropCarBecause"), new Object[] { car.toString(),
										testTrack.getName(), status, testTrack.getTrackTypeName() }));
						continue;
					}
					// No local moves from spur to spur
					if (_train.isLocalSwitcher() && !Setup.isLocalSpurMovesEnabled()
							&& testTrack.getTrackType().equals(Track.SPUR)
							&& car.getTrack().getTrackType().equals(Track.SPUR)) {
						addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildNoSpurToSpurMove"),
								new Object[] { car.getTrackName(), testTrack.getName() }));
						continue;
					}
					// No local moves from yard to yard
					if (_train.isLocalSwitcher() && !Setup.isLocalYardMovesEnabled()
							&& testTrack.getTrackType().equals(Track.YARD)
							&& car.getTrack().getTrackType().equals(Track.YARD)) {
						addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildNoYardToYardMove"),
								new Object[] { car.getTrackName(), testTrack.getName() }));
						continue;
					}
					// No local moves from interchange to interchange
					if (_train.isLocalSwitcher() && !Setup.isLocalInterchangeMovesEnabled()
							&& testTrack.getTrackType().equals(Track.INTERCHANGE)
							&& car.getTrack().getTrackType().equals(Track.INTERCHANGE)) {
						addLine(_buildReport, FIVE, MessageFormat.format(Bundle
								.getMessage("buildNoInterchangeToInterchangeMove"),
								new Object[] { car.getTrackName(), testTrack.getName() }));
						continue;
					}

					// not staging, then use
					if (!testTrack.getTrackType().equals(Track.STAGING)) {
						trackTemp = testTrack;
						destinationTemp = testDestination;
						break;
					}
				}
			}
			// did we find a new destination?
			if (destinationTemp != null) {
				// check for programming error
				if (trackTemp == null) {
					// The following code should never execute
					throw new BuildFailedException("Build Failure, trackTemp is null!"); // NOI18N
				}
				addLine(_buildReport, THREE, MessageFormat.format(Bundle.getMessage("buildCarCanDropMoves"),
						new Object[] { car.toString(), (destinationTemp.getName() + ", " + trackTemp.getName()),
								+rld.getCarMoves(), rld.getMaxCarMoves() }));
				if (rldSave == null && multiplePickup) {
					addLine(_buildReport, THREE, MessageFormat.format(Bundle.getMessage("buildCarHasSecond"),
							new Object[] { car.toString(), car.getLocationName() }));
					trackSave = null;
					break; // done
				}
				// if there's more than one available destination use the one with the least moves
				if (rldSave != null) {
					double saveCarMoves = rldSave.getCarMoves();
					double saveRatio = saveCarMoves / rldSave.getMaxCarMoves();
					double nextCarMoves = rld.getCarMoves();
					double nextRatio = nextCarMoves / rld.getMaxCarMoves();
					// bias cars to the terminal
					if (rld.getName().equals(_terminateLocation.getName())) {
						log.debug("Location ({}) is terminate location {}", rld.getName(), Double.toString(nextRatio));
						nextRatio = nextRatio * nextRatio;
					}
					// bias cars with default loads to a track with a schedule
					if (!trackTemp.getScheduleId().equals("")) {
						log.debug("Track ({}) has schedule ({}) adjust nextRatio = {}", // NOI18N
								trackTemp.getName(), trackTemp.getScheduleName(), Double.toString(nextRatio));
						nextRatio = nextRatio * nextRatio;
					}
					// check for an earlier drop in the route
					for (int m = start; m < routeEnd; m++) {
						RouteLocation rle = _routeList.get(m);
						if (rle == rld)
							break; // done
						if (rle.getName().equals(rld.getName()) && (rle.getMaxCarMoves() - rle.getCarMoves() > 0)
								&& rle.isDropAllowed() && checkDropTrainDirection(car, rle, trackTemp)) {
							log.debug("Found an earlier drop for car ({}) destination ({})", car.toString(), rle.getName()); // NOI18N
							nextCarMoves = rle.getCarMoves();
							nextRatio = nextCarMoves / rle.getMaxCarMoves();
							rld = rle; // set car drop to earlier stop
							break;
						}
					}
					log.debug("{} = {}, {} = {}",
							rldSave.getName(), Double.toString(saveRatio), rld.getName(), Double.toString(nextRatio));
					if (saveRatio < nextRatio) {
						rld = rldSave; // the saved is better than the last found
						trackTemp = trackSave;
					} else if (multiplePickup) {
						addLine(_buildReport, THREE, MessageFormat.format(Bundle.getMessage("buildCarHasSecond"),
								new Object[] { car.toString(), car.getLocationName() }));
						trackSave = null;
						break; // done
					}
				}
				// every time through, save the best route destination, and track
				rldSave = rld;
				trackSave = trackTemp;
			} else {
				addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildCouldNotFindDestForCar"),
						new Object[] { car.toString(), rld.getName() }));
			}
		}
		// did we find a destination?
		if (trackSave != null) {
			addCarToTrain(car, rl, rldSave, trackSave);
			return true;
		}
		// is car sitting on a FIFO or LIFO track?
		if (car.getTrack() != null && !car.getTrack().getServiceOrder().equals(Track.NORMAL)) {
			addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildBypassCarServiceOrder"),
					new Object[] { car.toString(), car.getTrackName(), car.getTrack().getServiceOrder() }));
			// move car id in front of current pointer so car is no longer used on this pass
			_carList.remove(car);
			_carList.add(_carIndex, car);
		}
		addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildNoDestForCar"), new Object[] { car
				.toString() }));
		addLine(_buildReport, FIVE, BLANK_LINE); // add line when in detailed report mode
		return false; // no build errors, but car not given destination
	}

	/**
	 * Returns true if car can be picked up later in a train's route
	 */
	private boolean checkForLaterPickUp(RouteLocation rl, RouteLocation rld, Car car) {
		if (rl != rld && rld.getName().equals(car.getLocationName())) {
			// don't delay adding a caboose, passenger car, or car with FRED
			if (car.isCaboose() || car.isPassenger() || car.hasFred())
				return false;
			// no later pick up if car is departing staging
			if (car.getLocation().getLocationOps() == Location.STAGING)
				return false;
			if (!checkPickUpTrainDirection(car, rld)) {
				addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildPickupLaterDirection"),
						new Object[] { car.toString(), rld.getName(), rld.getId() }));
				return false;
			}
			if (!rld.isPickUpAllowed()) {
				addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildPickupLater"), new Object[] {
						car.toString(), rld.getName(), rld.getId() }));
				return false;
			}
			if (rld.getMaxCarMoves() - rld.getCarMoves() <= 0) {
				addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildPickupLaterNoMoves"),
						new Object[] { car.toString(), rld.getName(), rld.getId() }));
				return false;
			}
//			log.debug("Car ({}) can be picked up later!", car.toString());
			addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildPickupLaterOkay"),
					new Object[] { car.toString(), rld.getName(), rld.getId() }));
			return true;
		}
		return false;
	}

	/**
	 * Creates a car load for a car departing staging and terminating into staging.
	 * 
	 * @param car
	 *            the car!
	 * @return true if a load was generated this this car.
	 */
	private boolean generateLoadCarDepartingAndTerminatingIntoStaging(Car car, Track stageTrack) {
		if (stageTrack == null || !stageTrack.getTrackType().equals(Track.STAGING)
				|| !stageTrack.acceptsTypeName(car.getTypeName()) || !stageTrack.acceptsRoadName(car.getRoadName())) {
			log.debug("Track doesn't service car");
			return false;
		}
		// Departing and returning to same location in staging?
		if (!_train.isAllowReturnToStagingEnabled() && !car.isCaboose() && !car.hasFred() && !car.isPassenger()
				&& splitString(car.getLocationName()).equals(splitString(stageTrack.getLocation().getName()))) {
			log.debug("Returning car to staging not allowed");
			return false;
		}
		// figure out which loads the car can use
		List<String> loads = CarLoads.instance().getNames(car.getTypeName());
		// remove the default names
		loads.remove(CarLoads.instance().getDefaultEmptyName());
		loads.remove(CarLoads.instance().getDefaultLoadName());
		if (loads.size() == 0) {
			log.debug("No custom loads for staging track {}", stageTrack.getName());
			return false;
		}
		addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildSearchTrackLoadStaging"),
				new Object[] { car.toString(), car.getTypeName(), car.getLoadName(), car.getLocationName(),
						car.getTrackName(), stageTrack.getLocation().getName(), stageTrack.getName() }));
		for (int i = loads.size() - 1; i >= 0; i--) {
			String load = loads.get(i);
			if (!car.getTrack().shipsLoad(load, car.getTypeName()) || !stageTrack.acceptsLoad(load, car.getTypeName())
					|| !_train.acceptsLoad(load, car.getTypeName())) {
				if (!car.getTrack().shipsLoad(load, car.getTypeName())) {
					addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildTrackNotNewLoad"),
							new Object[] { car.getTrackName(), load, stageTrack.getLocation().getName(),
									stageTrack.getName() }));
				}
				if (!stageTrack.acceptsLoad(load, car.getTypeName())) {
					addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("BuildDestTrackNoLoad"),
							new Object[] { stageTrack.getLocation().getName(), stageTrack.getName(), car.toString(),
									load, }));
				}
				if (!_train.acceptsLoad(load, car.getTypeName())) {
					addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildTrainNotNewLoad"),
							new Object[] { _train.getName(), load, stageTrack.getLocation().getName(),
									stageTrack.getName() }));

				}
				loads.remove(i);
			}
		}
		// Use random loads rather that the first one that works to create interesting loads
		if (loads.size() > 0) {
			String oldLoad = car.getLoadName(); // in case creating a new load still doesn't allow the car to be placed
												// into
			// staging
			int rnd = (int) (Math.random() * loads.size());
			car.setLoadName(loads.get(rnd));
			// check to see if car is now accepted by staging
			String status = car.testDestination(stageTrack.getLocation(), stageTrack); // will staging now accept this
																						// car?
			if (status.equals(Track.OKAY)) {
				car.setLoadGeneratedFromStaging(true);
				car.setFinalDestination(stageTrack.getLocation());
				car.setFinalDestinationTrack(stageTrack); // TODO should we be setting the track in staging?
				// is car part of kernel?
				car.updateKernel();
				addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildAddingScheduleLoad"),
						new Object[] { car.getLoadName(), car.toString() }));
				return true;
			}
			car.setLoadName(oldLoad); // restore load and report failure
			addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildCanNotDropCarBecause"),
					new Object[] { car.toString(), stageTrack.getName(), status, stageTrack.getTrackTypeName() }));
		}
		addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildUnableNewLoadStaging"), new Object[] {
				car.toString(), car.getTrackName(), stageTrack.getLocation().getName(), stageTrack.getName() }));
		return false;
	}

	/**
	 * Checks to see if cars that are already in the train can be redirected from the alternate track to the spur that
	 * really wants the car. Fixes the issue of having cars placed at the alternate when the spur's cars get pulled by
	 * this train, but cars were sent to the alternate because the spur was full at the time it was tested.
	 * 
	 * @param rl
	 * @return true if one or more cars were redirected
	 */
	private boolean redirectCarsFromAlternateTrack() {
		if (!Setup.isBuildAggressive())
			return false;
		boolean redirected = false;
		List<RollingStock> cars = carManager.getByTrainList(_train);
		for (int i = 0; i < cars.size(); i++) {
			Car car = (Car) cars.get(i);
			// does the car have a final destination and the destination is this one?
			if (car.getFinalDestination() == null || car.getFinalDestinationTrack() == null
					|| !car.getFinalDestinationName().equals(car.getDestinationName()))
				continue;
			log.debug("Car ({}) destination track ({}) has final destination track ({}) location ({})", 
					car.toString(), car.getDestinationTrackName(), car.getFinalDestinationTrackName(), car.getDestinationName()); // NOI18N
			if (car.testDestination(car.getFinalDestination(), car.getFinalDestinationTrack()).equals(Track.OKAY)) {
				Track alternate = car.getFinalDestinationTrack().getAlternateTrack();
				if (alternate != null
						&& car.getDestinationTrack() == alternate
						&& (alternate.getTrackType().equals(Track.YARD) || alternate.getTrackType().equals(
								Track.INTERCHANGE))
						&& checkDropTrainDirection(car, car.getRouteDestination(), car.getFinalDestinationTrack())
						&& checkTrainCanDrop(car, car.getFinalDestinationTrack())) {
					log.debug("Car ({}) alternate track ({}) can be redirected to final destination track ({})", 
							car.toString(), car.getDestinationTrackName(), car.getFinalDestinationTrackName());
					addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildRedirectFromAlternate"),
							new Object[] { car.getFinalDestinationName(), car.getFinalDestinationTrackName(),
									car.toString(), car.getDestinationTrackName() }));
					car.setDestination(car.getFinalDestination(), car.getFinalDestinationTrack());
					redirected = true;
				}
			}
		}
		return redirected;
	}

	// report any cars left at location
	private void reportCarsNotMoved(RouteLocation rl, int percent) {
		// only report if requested moves completed and final pass
		if (!_success || percent != 100)
			return;
		if (_carIndex < 0)
			_carIndex = 0;
		// cars up this point have build report messages, only show the cars that aren't in the build report
		int numberCars = 0;
		for (int i = _carIndex; i < _carList.size(); i++) {
			if (numberCars == DISPLAY_NO_MOVE_CAR_LIMIT) {
				addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildOnlyFirstXXXCars"),
						new Object[] { numberCars }));
				break;
			}

			Car car = _carList.get(i);
			// find a car at this location that hasn't been given a destination
			if (!car.getLocationName().equals(rl.getName()) || car.getRouteDestination() != null)
				continue;
			if (numberCars == 0) {
				addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildMovesCompleted"),
						new Object[] { rl.getName() }));
			}
			addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildCarIgnored"), new Object[] {
					car.toString(), car.getTypeName(), car.getLoadName(), car.getLocationName(), car.getTrackName() }));
			numberCars++;
		}
		addLine(_buildReport, SEVEN, BLANK_LINE);
	}

	/**
	 * Checks to see if additional engines are needed for the train based on the train's calculated tonnage. Minimum
	 * speed for the train is fixed at 36 MPH. The formula HPT x 12 / % Grade = Speed, is used to determine the
	 * horsepower needed. For example a 1% grade requires a minimum of 3 HPT.
	 * 
	 * @throws BuildFailedException
	 */
	private void checkNumnberOfEnginesNeeded() throws BuildFailedException {
		if (_reqNumEngines == 0 || !_train.isBuildConsistEnabled() || Setup.getHorsePowerPerTon() == 0)
			return;
		addLine(_buildReport, ONE, BLANK_LINE);
		addLine(_buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildDetermineNeeds"), new Object[] { Setup
				.getHorsePowerPerTon() }));
		Route route = _train.getRoute();
		int hpAvailable = 0;
		int extraHpNeeded = 0;
		RouteLocation rlNeedHp = null;
		RouteLocation rlStart = _train.getTrainDepartsRouteLocation();
		RouteLocation rlEnd = _train.getTrainTerminatesRouteLocation();
		if (route != null) {
			boolean helper = false;
			for (RouteLocation rl : route.getLocationsBySequenceList()) {
				if ((_train.getSecondLegOptions() == Train.HELPER_ENGINES && rl == _train.getSecondLegStartLocation())
						|| (_train.getThirdLegOptions() == Train.HELPER_ENGINES && rl == _train
								.getThirdLegStartLocation())) {
					addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("AddHelpersAt"),
							new Object[] { rl.getName() }));
					helper = true;
				}
				if ((_train.getSecondLegOptions() == Train.HELPER_ENGINES && rl == _train.getSecondLegEndLocation())
						|| (_train.getThirdLegOptions() == Train.HELPER_ENGINES && rl == _train.getThirdLegEndLocation())) {
					addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("RemoveHelpersAt"),
							new Object[] { rl.getName() }));
					helper = false;
				}
				if (helper)
					continue;
				// check for a change of engines in the train's route
				if (((_train.getSecondLegOptions() & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES && rl == _train
						.getSecondLegStartLocation())
						|| ((_train.getThirdLegOptions() & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES && rl == _train
								.getThirdLegStartLocation())) {
					log.debug("Loco change at ({})", rl.getName());
					addLocos(hpAvailable, extraHpNeeded, rlNeedHp, rlStart, rl);
					// reset for next leg of train's route
					rlStart = rl;
					rlNeedHp = null;
					extraHpNeeded = 0;
				}
				int weight = rl.getTrainWeight();
				if (weight > 0) {
					int hptMinimum = Setup.getHorsePowerPerTon();
					int hptGrade = (int) (36 * rl.getGrade() / 12);
					int hp = _train.getTrainHorsePower(rl);
					int hpt = hp / weight;
					if (hptGrade > hptMinimum)
						hptMinimum = hptGrade;
					if (hptMinimum > hpt) {
						int addHp = hptMinimum * weight - hp;
						if (addHp > extraHpNeeded) {
							hpAvailable = hp;
							extraHpNeeded = addHp;
							rlNeedHp = rl;
						}
						addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildAddLocosStatus"),
								new Object[] { weight, hp, rl.getGrade(), hpt, hptMinimum, rl.getName() }));
						addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildTrainRequiresAddHp"),
								new Object[] { addHp, rl.getName(), hptMinimum }));
					}
				}
			}
		}
		addLocos(hpAvailable, extraHpNeeded, rlNeedHp, rlStart, rlEnd);
		addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildDoneAssingEnginesTrain"),
				new Object[] { _train.getName() }));
		addLine(_buildReport, THREE, BLANK_LINE);
	}

	private void addLocos(int hpAvailable, int extraHpNeeded, RouteLocation rlNeedHp, RouteLocation rl,
			RouteLocation rld) throws BuildFailedException {
		if (rlNeedHp == null)
			return;
		int numberLocos = 0;
		// determine how many locos have already been assigned to the train
		List<RollingStock> engines = EngineManager.instance().getList(_train);
		for (int i = 0; engines.size() > i; i++) {
			Engine eng = (Engine) engines.get(i);
			if (eng.getRouteLocation() == rl)
				numberLocos++;
		}
		addLine(_buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildTrainReqExtraHp"), new Object[] {
				extraHpNeeded, rlNeedHp.getName(), rld.getName(), numberLocos }));
		while (numberLocos < Setup.getMaxNumberEngines()) {
			if (!getEngines(1, _train.getEngineModel(), _train.getEngineRoad(), rl, rld)) {
				throw new BuildFailedException(MessageFormat.format(Bundle.getMessage("buildErrorEngines"),
						new Object[] { Bundle.getMessage("additional"), rl.getName(), rld.getName() }));
			}
			numberLocos++;
			int currentHp = _train.getTrainHorsePower(rlNeedHp);
			if (currentHp > hpAvailable + extraHpNeeded) {
				break; // done
			}
			if (numberLocos < Setup.getMaxNumberEngines()) {
				addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildContinueAddLocos"),
						new Object[] { (hpAvailable + extraHpNeeded - currentHp), rlNeedHp.getName(), rld.getName(),
								numberLocos }));
			} else {
				addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildMaxNumberLocoAssigned"),
						new Object[] { Setup.getMaxNumberEngines() }));
			}
		}
	}

	private void buildFailed(BuildFailedException e) {
		String msg = e.getMessage();
		_train.setBuildFailedMessage(msg);
		_train.setStatus(Train.CODE_BUILD_FAILED);
		_train.setBuildFailed(true);
		if (log.isDebugEnabled())
			log.debug(msg);
		if (TrainManager.instance().isBuildMessagesEnabled()) {
			if (e.getExceptionType().equals(BuildFailedException.NORMAL)) {
				JOptionPane.showMessageDialog(null, msg, MessageFormat.format(Bundle.getMessage("buildErrorMsg"),
						new Object[] { _train.getName(), _train.getDescription() }), JOptionPane.ERROR_MESSAGE);
			} else {
				// build error, could not find destinations for cars departing staging
				Object[] options = { Bundle.getMessage("buttonRemoveCars"), "OK" };
				int results = JOptionPane.showOptionDialog(null, msg, MessageFormat.format(Bundle
						.getMessage("buildErrorMsg"), new Object[] { _train.getName(), _train.getDescription() }),
						JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[1]);
				if (results == 0) {
					log.debug("User requested that cars be removed from staging track");
					removeCarsFromStaging();
				}
			}
			int size = carManager.getList(_train).size();
			if (size > 0) {
				if (JOptionPane.showConfirmDialog(null, MessageFormat.format(Bundle.getMessage("buildCarsResetTrain"),
						new Object[] { size, _train.getName() }), Bundle.getMessage("buildResetTrain"),
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					_train.reset();
				}
			}
		}
		if (_buildReport != null) {
			addLine(_buildReport, ONE, msg);
			// Write to disk and close buildReport
			addLine(_buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildFailedMsg"), new Object[] { _train
					.getName() }));
			_buildReport.flush();
			_buildReport.close();
		}
	}

	/**
	 * build has failed due to cars in staging not having destinations this routine removes those cars from the staging
	 * track by user request.
	 */
	private void removeCarsFromStaging() {
		if (_departStageTrack == null)
			return;
		for (_carIndex = 0; _carIndex < _carList.size(); _carIndex++) {
			Car car = _carList.get(_carIndex);
			// remove cars from departure staging track that haven't been assigned to this train
			if (car.getTrack().equals(_departStageTrack) && car.getTrain() == null) {
				car.setLocation(car.getLocation(), null);
			}
		}
	}

	static Logger log = LoggerFactory.getLogger(TrainBuilder.class.getName());

}

class BuildFailedException extends Exception {

	public final static String NORMAL = "normal"; // NOI18N
	public final static String STAGING = "staging"; // NOI18N
	private String type = NORMAL;

	public BuildFailedException(String s, String type) {
		super(s);
		this.type = type;
	}

	public BuildFailedException(String s) {
		super(s);
	}

        public BuildFailedException(Exception ex) {
            super(ex);
        }
        
	public String getExceptionType() {
		return type;
	}

}
