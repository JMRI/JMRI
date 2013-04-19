package jmri.jmrit.operations.trains;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarLoad;
import jmri.jmrit.operations.rollingstock.cars.CarLoads;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.router.Router;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;

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

	// build variables shared between local routines
	Train train; // the train being built
	int numberCars = 0; // how many cars are moved by this train
	int reqNumEngines = 0; // the number of engines required for this train
	List<String> engineList; // list of engines available for this train
	Engine leadEngine; // last lead engine found from getEngine
	int carIndex; // index for carList
	List<String> carList; // list of cars available for this train
	List<String> routeList; // list of locations from departure to termination served by this train
	Hashtable<String, Integer> numOfBlocks; // Number of blocks of cars departing staging.
	int moves; // the number of pick up car moves for a location
	double maxWeight = 0; // the maximum weight of cars in train
	int reqNumOfMoves; // the requested number of car moves for a location
	Location departLocation; // train departs this location
	Track departStageTrack; // departure staging track (null if not staging)
	Location terminateLocation; // train terminate at this location
	Track terminateStageTrack; // terminate staging track (null if not staging)
	boolean success; // true when enough cars have been picked up from a location
	PrintWriter buildReport; // build report for this train
	boolean noMoreMoves; // when true there aren't any more moves for a location

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
		this.train = train;
		try {
			build();
		} catch (BuildFailedException e) {
			buildFailed(e);
		}
	}

	private void build() throws BuildFailedException {
		log.debug("Building train " + train.getName());

		train.setStatus(Train.BUILDING);
		train.setBuilt(false);
		train.setLeadEngine(null);

		// create build status file
		File file = TrainManagerXml.instance().createTrainBuildReportFile(train.getName());
		try {
			buildReport = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file), "UTF-8")), true);	// NOI18N
		} catch (IOException e) {
			log.error("can not open build status file");
			return;
		}
		Date startTime = new Date();
		addLine(buildReport, ONE, MessageFormat.format(Bundle.getMessage("BuildReportMsg"), new Object[] {
				train.getName(), startTime }));
		addLine(buildReport, ONE, MessageFormat.format(Bundle.getMessage("BuildReportVersion"),
				new Object[] { Version.name() }));
		// show the various build detail levels
		addLine(buildReport, THREE, Bundle.getMessage("buildReportLevelThree"));
		addLine(buildReport, FIVE, Bundle.getMessage("buildReportLevelFive"));
		addLine(buildReport, SEVEN, Bundle.getMessage("buildReportLevelSeven"));
		
		if (Setup.getRouterBuildReportLevel().equals(Setup.BUILD_REPORT_DETAILED))
			addLine(buildReport, SEVEN, Bundle.getMessage("buildRouterReportLevelDetailed"));
		else if (Setup.getRouterBuildReportLevel().equals(Setup.BUILD_REPORT_VERY_DETAILED))
			addLine(buildReport, SEVEN, Bundle.getMessage("buildRouterReportLevelVeryDetailed"));

		if (train.getRoute() == null) {
			throw new BuildFailedException(MessageFormat.format(Bundle.getMessage("buildErrorRoute"),
					new Object[] { train.getName() }));
		}
		// get the train's route
		routeList = train.getRoute().getLocationsBySequenceList();
		if (routeList.size() < 1) {
			throw new BuildFailedException(MessageFormat.format(Bundle.getMessage("buildErrorNeedRoute"),
					new Object[] { train.getName() }));
		}
		// train departs
		departLocation = locationManager.getLocationByName(train.getTrainDepartsName());
		if (departLocation == null) {
			throw new BuildFailedException(MessageFormat.format(Bundle.getMessage("buildErrorNeedDepLoc"),
					new Object[] { train.getName() }));
		}
		// train terminates
		terminateLocation = locationManager.getLocationByName(train.getTrainTerminatesName());
		if (terminateLocation == null) {
			throw new BuildFailedException(MessageFormat.format(Bundle.getMessage("buildErrorNeedTermLoc"),
					new Object[] { train.getName() }));
		}

		// show train build options in very detailed mode
		addLine(buildReport, SEVEN, Bundle.getMessage("MenuItemBuildOptions"));
		if (Setup.isBuildAggressive())
			addLine(buildReport, SEVEN, Bundle.getMessage("BuildModeAggressive"));
		else
			addLine(buildReport, SEVEN, Bundle.getMessage("BuildModeNormal"));
		if (train.isBuildTrainNormalEnabled())
			addLine(buildReport, SEVEN, Bundle.getMessage("NormalModeWhenBuilding"));
		if (train.isSendCarsToTerminalEnabled())
			addLine(buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("SendToTerminal"),
					new Object[] { terminateLocation.getName() }));
		if (train.isAllowReturnToStagingEnabled()  || Setup.isAllowReturnToStagingEnabled())
			addLine(buildReport, SEVEN, Bundle.getMessage("AllowCarsToReturn"));
		if (train.isAllowLocalMovesEnabled())
			addLine(buildReport, SEVEN, Bundle.getMessage("AllowLocalMoves"));
		if (train.isAllowThroughCarsEnabled())
			addLine(buildReport, SEVEN, Bundle.getMessage("AllowThroughCars"));

		// TODO: DAB control minimal build by each train
		if (train.getTrainDepartsRouteLocation().getMaxCarMoves() > departLocation.getNumberRS()
				&& Control.fullTrainOnly) {
			throw new BuildFailedException(MessageFormat.format(Bundle.getMessage("buildErrorCars"),
					new Object[] { Integer.toString(departLocation.getNumberRS()),
							train.getTrainDepartsName(), train.getName() }));
		}
		// get the number of requested car moves for this train
		int requested = 0;
		for (int i = 0; i < routeList.size(); i++) {
			RouteLocation rl = train.getRoute().getLocationById(routeList.get(i));
			// check to see if there's a location for each stop in the route
			Location l = locationManager.getLocationByName(rl.getName());
			if (l == null || rl.getLocation() == null) {
				throw new BuildFailedException(MessageFormat.format(
						Bundle.getMessage("buildErrorLocMissing"),
						new Object[] { train.getRoute().getName() }));
			}
			// train doesn't drop or pick up cars from staging locations found in middle of a route
			List<String> slStage = l.getTrackIdsByMovesList(Track.STAGING);
			if (slStage.size() > 0 && i != 0 && i != routeList.size() - 1) {
				addLine(buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildLocStaging"),
						new Object[] { rl.getName() }));
				rl.setCarMoves(rl.getMaxCarMoves()); // don't allow car moves for this location
			}
			// if a location is skipped, no car drops or pick ups
			else if (train.skipsLocation(rl.getId())) {
				addLine(buildReport, THREE, MessageFormat.format(Bundle.getMessage("buildLocSkipped"),
						new Object[] { rl.getName(), train.getName() }));
				rl.setCarMoves(rl.getMaxCarMoves()); // don't allow car moves for this location
			} else {
				// we're going to use this location, so initialize the location
				rl.setCarMoves(0); // clear the number of moves
				requested = requested + rl.getMaxCarMoves(); // add up the total number of car moves requested
				// show the type of moves allowed at this location
				if (!rl.canDrop() && !rl.canPickup())
					addLine(buildReport, THREE, MessageFormat.format(Bundle
							.getMessage("buildLocNoDropsOrPickups"), new Object[] { rl.getName() }));
				else if (rl.canDrop() && rl.canPickup())
					addLine(buildReport, THREE, MessageFormat.format(Bundle
							.getMessage("buildLocRequestMoves"), new Object[] { rl.getName(),
							rl.getMaxCarMoves() }));
				else if (!rl.canDrop())
					addLine(buildReport, THREE, MessageFormat.format(Bundle
							.getMessage("buildLocRequestPickups"), new Object[] { rl.getName(),
							rl.getMaxCarMoves() }));
				else
					addLine(buildReport, THREE, MessageFormat.format(Bundle
							.getMessage("buildLocRequestDrops"), new Object[] { rl.getName(),
							rl.getMaxCarMoves() }));
			}
			rl.setTrainWeight(0); // clear the total train weight
			rl.setTrainLength(0); // and length
		}
		int numMoves = requested; // number of car moves
		if (routeList.size() > 1)
			requested = requested / 2; // only need half as many cars to meet requests
		addLine(buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildRouteRequest"), new Object[] {
				train.getRoute().getName(), Integer.toString(requested), Integer.toString(numMoves) }));

		// show road names that this train will service
		if (!train.getRoadOption().equals(Train.ALLROADS)) {
			addLine(buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildTrainRoads"),
					new Object[] { train.getName(), train.getRoadOption(),
							formatStringToCommaSeparated(train.getRoadNames()) }));
		}
		// show load names that this train will service
		if (!train.getLoadOption().equals(Train.ALLLOADS)) {
			addLine(buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildTrainLoads"),
					new Object[] { train.getName(), train.getLoadOption(),
							formatStringToCommaSeparated(train.getLoadNames()) }));
		}
		// show owner names that this train will service
		if (!train.getOwnerOption().equals(Train.ALLOWNERS)) {
			addLine(buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildTrainOwners"),
					new Object[] { train.getName(), train.getOwnerOption(),
							formatStringToCommaSeparated(train.getOwnerNames()) }));
		}
		// show built date serviced
		if (!train.getBuiltStartYear().equals(""))
			addLine(buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildTrainBuiltAfter"),
					new Object[] { train.getName(), train.getBuiltStartYear() }));
		if (!train.getBuiltEndYear().equals(""))
			addLine(buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildTrainBuiltBefore"),
					new Object[] { train.getName(), train.getBuiltEndYear() }));

		// show engine info
		if (train.getNumberEngines().equals(Train.AUTO)) {
			reqNumEngines = getAutoEngines();
		} else {
			reqNumEngines = Integer.parseInt(train.getNumberEngines());
		}
		// show engine types that this train will service
		if (reqNumEngines > 0) {
			addLine(buildReport, FIVE, MessageFormat.format(Bundle
					.getMessage("buildTrainServicesEngineTypes"), new Object[] { train.getName() }));
			addLine(buildReport, FIVE, formatStringToCommaSeparated(train.getLocoTypeNames()));
		}

		// show engine requirements for this train
		if (reqNumEngines == 0)
			addLine(buildReport, ONE, Bundle.getMessage("buildTrainReq0Engine"));
		else if (reqNumEngines == 1)
			addLine(buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildTrainReq1Engine"),
					new Object[] { train.getEngineModel(), train.getEngineRoad() }));
		else
			addLine(buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildTrainReqEngine"),
					new Object[] { train.getNumberEngines(), train.getEngineModel(), train.getEngineRoad() }));

		// allow up to two engine and caboose swaps in the train's route
		RouteLocation engineTerminatesFirstLeg = train.getTrainTerminatesRouteLocation();
		RouteLocation cabooseOrFredTerminatesFirstLeg = train.getTrainTerminatesRouteLocation();
		RouteLocation engineTerminatesSecondLeg = train.getTrainTerminatesRouteLocation();
		RouteLocation cabooseOrFredTerminatesSecondLeg = train.getTrainTerminatesRouteLocation();
		RouteLocation engineTerminatesThirdLeg = train.getTrainTerminatesRouteLocation();
		RouteLocation cabooseOrFredTerminatesThirdLeg = train.getTrainTerminatesRouteLocation();

		// show any engine changes or helper services
		if ((train.getSecondLegOptions() & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES) {
			addLine(buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildTrainEngineChange"),
					new Object[] { train.getSecondLegStartLocationName(), train.getSecondLegNumberEngines(),
							train.getSecondLegEngineModel(), train.getSecondLegEngineRoad() }));
			if (train.getSecondLegStartLocation() != null) {
				engineTerminatesFirstLeg = train.getSecondLegStartLocation();
			}
		}
		if ((train.getSecondLegOptions() & Train.HELPER_ENGINES) == Train.HELPER_ENGINES) {
			addLine(buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildTrainHelperEngines"),
					new Object[] { train.getSecondLegNumberEngines(), train.getSecondLegStartLocationName(),
							train.getSecondLegEndLocationName(), train.getSecondLegEngineModel(),
							train.getSecondLegEngineRoad() }));
		}
		if ((train.getThirdLegOptions() & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES) {
			addLine(buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildTrainEngineChange"),
					new Object[] { train.getThirdLegStartLocationName(), train.getThirdLegNumberEngines(),
							train.getThirdLegEngineModel(), train.getThirdLegEngineRoad() }));
			if (train.getThirdLegStartLocation() != null) {
				engineTerminatesSecondLeg = train.getThirdLegStartLocation();
				// No engine or caboose change at first leg?
				if ((train.getSecondLegOptions() & Train.CHANGE_ENGINES) != Train.CHANGE_ENGINES) {
					engineTerminatesFirstLeg = train.getThirdLegStartLocation();
				}
			}
		}
		if ((train.getThirdLegOptions() & Train.HELPER_ENGINES) == Train.HELPER_ENGINES) {
			addLine(buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildTrainHelperEngines"),
					new Object[] { train.getThirdLegNumberEngines(), train.getThirdLegStartLocationName(),
							train.getThirdLegEndLocationName(), train.getThirdLegEngineModel(),
							train.getThirdLegEngineRoad() }));
		}
		// make any caboose changes
		if ((train.getSecondLegOptions() & Train.REMOVE_CABOOSE) == Train.REMOVE_CABOOSE
				|| (train.getSecondLegOptions() & Train.ADD_CABOOSE) == Train.ADD_CABOOSE)
			cabooseOrFredTerminatesFirstLeg = train.getSecondLegStartLocation();
		else if ((train.getThirdLegOptions() & Train.REMOVE_CABOOSE) == Train.REMOVE_CABOOSE
				|| (train.getThirdLegOptions() & Train.ADD_CABOOSE) == Train.ADD_CABOOSE)
			cabooseOrFredTerminatesFirstLeg = train.getThirdLegStartLocation();
		if ((train.getThirdLegOptions() & Train.REMOVE_CABOOSE) == Train.REMOVE_CABOOSE
				|| (train.getThirdLegOptions() & Train.ADD_CABOOSE) == Train.ADD_CABOOSE)
			cabooseOrFredTerminatesSecondLeg = train.getThirdLegStartLocation();

		// does train terminate into staging?
		terminateStageTrack = null;
		List<String> stagingTracksTerminate = terminateLocation.getTrackIdsByMovesList(Track.STAGING);
		if (stagingTracksTerminate.size() > 0) {
			addLine(buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildTerminateStaging"),
					new Object[] { terminateLocation.getName(),
							Integer.toString(stagingTracksTerminate.size()) }));
			if (stagingTracksTerminate.size() > 1 && Setup.isPromptToStagingEnabled()) {
				terminateStageTrack = PromptToStagingDialog();
				startTime = new Date(); // reset build time
			} else
				for (int i = 0; i < stagingTracksTerminate.size(); i++) {
					terminateStageTrack = terminateLocation.getTrackById(stagingTracksTerminate.get(i));
					if (checkTerminateStagingTrack(terminateStageTrack)) {
						addLine(buildReport, ONE, MessageFormat.format(
								Bundle.getMessage("buildStagingAvail"), new Object[] {
										terminateStageTrack.getName(), terminateLocation.getName() }));
						break;
					}
					terminateStageTrack = null;
				}
			if (terminateStageTrack == null) {
				// is this train returning to the same staging in aggressive mode?
				if (departLocation == terminateLocation && Setup.isBuildAggressive()
						&& Setup.isStagingTrackImmediatelyAvail()) {
					addLine(buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildStagingReturn"),
							new Object[] { terminateLocation.getName() }));
				} else {
					addLine(buildReport, ONE, Bundle.getMessage("buildErrorStagingFullNote"));
					throw new BuildFailedException(MessageFormat.format(Bundle
							.getMessage("buildErrorStagingFull"),
							new Object[] { terminateLocation.getName() }));
				}
			}
		}

		// get list of engines for this route
		engineList = engineManager.getAvailableTrainList(train);

		// determine if train is departing staging
		departStageTrack = null;
		List<String> stagingTracks = departLocation.getTrackIdsByMovesList(Track.STAGING);
		if (stagingTracks.size() > 0) {
			addLine(buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildDepartStaging"),
					new Object[] { departLocation.getName(), Integer.toString(stagingTracks.size()) }));
			if (stagingTracks.size() > 1 && Setup.isPromptFromStagingEnabled()) {
				departStageTrack = PromptFromStagingDialog();
				startTime = new Date(); // restart build timer
				if (departStageTrack == null)
					throw new BuildFailedException(MessageFormat.format(Bundle
							.getMessage("buildErrorStagingEmpty"), new Object[] { departLocation.getName() }));
				// load engines for this train
				if (!getEngines(reqNumEngines, train.getEngineModel(), train.getEngineRoad(), train
						.getTrainDepartsRouteLocation(), engineTerminatesFirstLeg)) {
					throw new BuildFailedException(MessageFormat.format(Bundle
							.getMessage("buildErrorEngines"), new Object[] { reqNumEngines,
							train.getTrainDepartsName(), engineTerminatesFirstLeg.getName() }));
				}
			} else
				for (int i = 0; i < stagingTracks.size(); i++) {
					departStageTrack = departLocation.getTrackById(stagingTracks.get(i));
					addLine(buildReport, THREE, MessageFormat.format(Bundle.getMessage("buildStagingHas"),
							new Object[] { departStageTrack.getName(),
									Integer.toString(departStageTrack.getNumberEngines()),
									Integer.toString(departStageTrack.getNumberCars()) }));
					// is the departure track available?
					if (!checkDepartureStagingTrack(departStageTrack)) {
						departStageTrack = null;
						continue;
					}
					// try each departure track for the required engines
					if (getEngines(reqNumEngines, train.getEngineModel(), train.getEngineRoad(), train
							.getTrainDepartsRouteLocation(), engineTerminatesFirstLeg)) {
						addLine(buildReport, SEVEN, Bundle.getMessage("buildDoneAssignEnginesStaging"));
						break; // done!
					}
					departStageTrack = null;
				}
			if (departStageTrack == null) {
				throw new BuildFailedException(MessageFormat.format(Bundle
						.getMessage("buildErrorStagingEmpty"), new Object[] { departLocation.getName() }));
				// departing staging and returning to same track?
			} else if (terminateStageTrack == null && departLocation == terminateLocation
					&& Setup.isBuildAggressive() && Setup.isStagingTrackImmediatelyAvail()) {
				terminateStageTrack = departStageTrack; // use the same track
			}
			// no staging tracks at this location, load engines for this train
		} else if (!getEngines(reqNumEngines, train.getEngineModel(), train.getEngineRoad(), train
				.getTrainDepartsRouteLocation(), engineTerminatesFirstLeg)) {
			throw new BuildFailedException(MessageFormat.format(Bundle.getMessage("buildErrorEngines"),
					new Object[] { reqNumEngines, train.getTrainDepartsName(),
							engineTerminatesFirstLeg.getName() }));
		}

		// Save termination and departure tracks
		train.setTerminationTrack(terminateStageTrack);
		train.setDepartureTrack(departStageTrack);

		// First engine change in route?
		Engine secondLeadEngine = null;
		if ((train.getSecondLegOptions() & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES) {
			addLine(buildReport, THREE, MessageFormat.format(Bundle.getMessage("buildTrainEngineChange"),
					new Object[] { train.getSecondLegStartLocationName(), train.getSecondLegNumberEngines(),
							train.getSecondLegEngineModel(), train.getSecondLegEngineRoad() }));
			if (getEngines(Integer.parseInt(train.getSecondLegNumberEngines()), train
					.getSecondLegEngineModel(), train.getSecondLegEngineRoad(), train
					.getSecondLegStartLocation(), engineTerminatesSecondLeg)) {
				secondLeadEngine = leadEngine;
			} else {
				throw new BuildFailedException(MessageFormat.format(Bundle.getMessage("buildErrorEngines"),
						new Object[] { Integer.parseInt(train.getSecondLegNumberEngines()),
								train.getSecondLegStartLocation(), engineTerminatesSecondLeg }));
			}
		}
		// Second engine change in route?
		Engine thirdLeadEngine = null;
		if ((train.getThirdLegOptions() & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES) {
			addLine(buildReport, THREE, MessageFormat.format(Bundle.getMessage("buildTrainEngineChange"),
					new Object[] { train.getThirdLegStartLocationName(), train.getThirdLegNumberEngines(),
							train.getThirdLegEngineModel(), train.getThirdLegEngineRoad() }));
			if (getEngines(Integer.parseInt(train.getThirdLegNumberEngines()),
					train.getThirdLegEngineModel(), train.getThirdLegEngineRoad(), train
							.getThirdLegStartLocation(), engineTerminatesThirdLeg)) {
				thirdLeadEngine = leadEngine;
			} else {
				throw new BuildFailedException(MessageFormat.format(Bundle.getMessage("buildErrorEngines"),
						new Object[] { Integer.parseInt(train.getThirdLegNumberEngines()),
								train.getThirdLegStartLocation(), engineTerminatesThirdLeg }));
			}
		}
		addLine(buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildDoneAssingEnginesTrain"),
				new Object[] { train.getName() }));

		// show car types that this train will service
		addLine(buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildTrainServicesCarTypes"),
				new Object[] { train.getName() }));
		addLine(buildReport, FIVE, formatStringToCommaSeparated(train.getCarTypeNames()));

		// get list of cars for this route
		carList = carManager.getAvailableTrainList(train);
		// TODO: DAB this needs to be controlled by each train
		if (requested > carList.size() && Control.fullTrainOnly) {
			throw new BuildFailedException(MessageFormat.format(Bundle.getMessage("buildErrorNumReq"),
					new Object[] { Integer.toString(requested), train.getName(),
							Integer.toString(carList.size()) }));
		}

		// remove unwanted cars
		removeCars();

		// Do caboose changes in reverse order in case there isn't enough track space

		// second caboose change?
		if ((train.getThirdLegOptions() & Train.ADD_CABOOSE) > 0 && train.getThirdLegStartLocation() != null
				&& cabooseOrFredTerminatesThirdLeg != null) {
			getCaboose(train.getThirdLegCabooseRoad(), thirdLeadEngine, train.getThirdLegStartLocation(),
					cabooseOrFredTerminatesThirdLeg, true);
		}
		
		// first caboose change?
		if ((train.getSecondLegOptions() & Train.ADD_CABOOSE) > 0
				&& train.getSecondLegStartLocation() != null && cabooseOrFredTerminatesSecondLeg != null) {
			getCaboose(train.getSecondLegCabooseRoad(), secondLeadEngine, train.getSecondLegStartLocation(),
					cabooseOrFredTerminatesSecondLeg, true);
		}
		
		// get caboose or car with FRED if needed for train
		getCaboose(train.getCabooseRoad(), train.getLeadEngine(), train.getTrainDepartsRouteLocation(),
				cabooseOrFredTerminatesFirstLeg, (train.getRequirements() & Train.CABOOSE) > 0);
		getCarWithFred(train.getCabooseRoad(), train.getTrainDepartsRouteLocation(),
				cabooseOrFredTerminatesFirstLeg);


		// done assigning cabooses and cars with FRED, remove the rest, and save final destination
		removeCaboosesAndCarsWithFredAndSaveFinalDestination();

		blockCarsFromStaging(); // block cars from staging

		// now find destinations for cars
		addLine(buildReport, SEVEN, BLANK_LINE); // add line when in very detailed report mode
		addLine(buildReport, THREE, MessageFormat.format(Bundle.getMessage("buildTrain"), new Object[] {
				requested, train.getName(), carList.size() }));

		if (Setup.isBuildAggressive() && !train.isBuildTrainNormalEnabled()) {
			// perform a two pass build for this train
			placeCars(50); // find destination for 50% of the available moves
		}
		placeCars(100); // done finding cars for this train!

		train.setCurrentLocation(train.getTrainDepartsRouteLocation());
		if (numberCars < requested) {
			train.setStatus(Train.PARTIALBUILT + " " + train.getNumberCarsWorked() + "/" + requested + " "
					+ Bundle.getMessage("cars"));
			addLine(buildReport, ONE, Train.PARTIALBUILT + " " + train.getNumberCarsWorked() + "/"
					+ requested + " " + Bundle.getMessage("cars"));
		} else {
			train.setStatus(Train.BUILT + " " + train.getNumberCarsWorked() + " " + Bundle.getMessage("cars"));
			addLine(buildReport, ONE, Train.BUILT + " " + train.getNumberCarsWorked() + " "
					+ Bundle.getMessage("cars"));
		}
		train.setBuilt(true);
		addLine(buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildTime"), new Object[] {
				train.getName(), new Date().getTime() - startTime.getTime() }));
		buildReport.flush();
		buildReport.close();

		// now make manifest
		new TrainManifest(train);
		if (Setup.isGenerateCsvManifestEnabled())
			new TrainCsvManifest(train);
		// now create and place train icon
		train.moveTrainIcon(train.getTrainDepartsRouteLocation());
		log.debug("Done building train " + train.getName());
	}

	/**
	 * Ask which staging track the train is to depart on.
	 * 
	 * @return The departure track the user selected.
	 */
	private Track PromptFromStagingDialog() {
		List<String> trackIds = departLocation.getTrackIdsByNameList(null);
		List<String> validTrackIds = new ArrayList<String>();
		// only show valid tracks
		for (int i = 0; i < trackIds.size(); i++) {
			Track track = departLocation.getTrackById(trackIds.get(i));
			if (checkDepartureStagingTrack(track))
				validTrackIds.add(trackIds.get(i));
		}
		Object[] tracks = new Object[validTrackIds.size()];
		for (int i = 0; i < validTrackIds.size(); i++)
			tracks[i] = departLocation.getTrackById(validTrackIds.get(i));
		if (validTrackIds.size() > 1) {
			Track selected = (Track) JOptionPane.showInputDialog(null, Bundle
					.getMessage("SelectDepartureTrack"), Bundle.getMessage("TrainDepartingStaging"),
					JOptionPane.QUESTION_MESSAGE, null, tracks, null);
			if (selected != null)
				addLine(buildReport, FIVE, MessageFormat.format(Bundle
						.getMessage("buildUserSelectedDeparture"), new Object[] { selected.getName(),
						selected.getLocation().getName() }));
			return selected;
		} else if (validTrackIds.size() == 1)
			return (Track) tracks[0];
		return null; // no tracks available
	}

	/**
	 * Ask which staging track the train is to terminate on.
	 * 
	 * @return The arrival track selected by the user.
	 */
	private Track PromptToStagingDialog() {
		List<String> trackIds = terminateLocation.getTrackIdsByNameList(null);
		List<String> validTrackIds = new ArrayList<String>();
		// only show valid tracks
		for (int i = 0; i < trackIds.size(); i++) {
			Track track = terminateLocation.getTrackById(trackIds.get(i));
			if (checkTerminateStagingTrack(track))
				validTrackIds.add(trackIds.get(i));
		}
		Object[] tracks = new Object[validTrackIds.size()];
		for (int i = 0; i < validTrackIds.size(); i++)
			tracks[i] = terminateLocation.getTrackById(validTrackIds.get(i));
		if (validTrackIds.size() > 1) {
			Track selected = (Track) JOptionPane.showInputDialog(null, Bundle
					.getMessage("SelectArrivalTrack"), Bundle.getMessage("TrainTerminatingStaging"),
					JOptionPane.QUESTION_MESSAGE, null, tracks, null);
			if (selected != null)
				addLine(buildReport, FIVE, MessageFormat.format(
						Bundle.getMessage("buildUserSelectedArrival"), new Object[] { selected.getName(),
								selected.getLocation().getName() }));
			return selected;
		} else if (validTrackIds.size() == 1)
			return (Track) tracks[0];
		return null; // no tracks available
	}

	/**
	 * Get the engines for this train. If departing from staging (departStageTrack != null) engines must come from that
	 * track.
	 * 
	 * @return true if correct number of engines found.
	 * @throws BuildFailedException
	 */
	private boolean getEngines(int numberOfEngines, String model, String road, RouteLocation rl,
			RouteLocation rld) throws BuildFailedException {
		// load departure track if staging
		Track departTrack = null;
		if (rl == train.getTrainDepartsRouteLocation())
			departTrack = departStageTrack; // get departure track from staging, could be null
		// load termination track if staging
		Track terminateTrack = null;
		if (rld == train.getTrainTerminatesRouteLocation())
			terminateTrack = terminateStageTrack; // get termination track to staging, could be null
		// departing staging and returning to same track?
		if (departStageTrack != null && terminateTrack == null
				&& rld == train.getTrainTerminatesRouteLocation() && departLocation == terminateLocation
				&& Setup.isBuildAggressive() && Setup.isStagingTrackImmediatelyAvail())
			terminateTrack = departStageTrack;

		// if not departing staging track and engines aren't required done!
		if (departTrack == null && numberOfEngines == 0)
			return true;

		// if departing staging and no engines required and none available, we're done
		if (departTrack != null && numberOfEngines == 0 && departTrack.getNumberEngines() == 0)
			return true;

		// if leaving staging, use any number of engines if required number is 0
		if (departTrack != null && numberOfEngines != 0 && departTrack.getNumberEngines() != numberOfEngines) {
			addLine(buildReport, THREE, MessageFormat.format(Bundle.getMessage("buildStagingNotEngines"),
					new Object[] { departTrack.getName() }));
			return false; // done, wrong number of engines on staging track
		}

		// code check
		if (rl == null || rld == null)
			throw new BuildFailedException(MessageFormat.format(Bundle.getMessage("buildErrorEngLocUnknown"),
					new Object[] {}));

		addLine(buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildBegineSearchEngines"),
				new Object[] { numberOfEngines, model, road, rl.getName(), rld.getName() }));
		boolean foundLoco = false;
		for (int indexEng = 0; indexEng < engineList.size(); indexEng++) {
			Engine engine = engineManager.getById(engineList.get(indexEng));
			log.debug("Engine (" + engine.toString() + ") at location (" + engine.getLocationName() + ")");

			// use engines that are departing from the selected staging track (departTrack != null if staging)
			if (departTrack != null && !departTrack.equals(engine.getTrack())) {
				continue;
			}

			// use engines that are departing from the correct location
			if (!engine.getLocationName().equals(rl.getName())) {
				log.debug("Skipping engine (" + engine.toString() + ") at location ("
						+ engine.getLocationName() + ")");
				continue;
			}

			// remove engines types that train does not service
			if (!train.acceptsTypeName(engine.getType())) {
				addLine(buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildExcludeEngineType"),
						new Object[] { engine.toString(), engine.getType() }));
				engineList.remove(indexEng);
				indexEng--;
				continue;
			}
			// skip engines models that train does not service
			if (!model.equals("") && !engine.getModel().equals(model)) {
				addLine(buildReport, SEVEN, MessageFormat.format(
						Bundle.getMessage("buildExcludeEngineModel"), new Object[] { engine.toString(),
								engine.getModel(), engine.getLocationName() }));
				continue;
			}
			// Does the train have a very specific engine road name requirement?
			if (!road.equals("") && !engine.getRoad().equals(road)) {
				addLine(buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildExcludeEngineRoad"),
						new Object[] { engine.toString(), engine.getRoad() }));
				continue;
			}
			// remove rolling stock with roads that train does not service
			if (road.equals("") && !train.acceptsRoadName(engine.getRoad())) {
				addLine(buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildExcludeEngineRoad"),
						new Object[] { engine.toString(), engine.getRoad() }));
				engineList.remove(indexEng);
				indexEng--;
				continue;
			}
			// remove engines with owners that train does not service
			if (!train.acceptsOwnerName(engine.getOwner())) {
				addLine(buildReport, SEVEN, MessageFormat.format(
						Bundle.getMessage("buildExcludeEngineOwner"), new Object[] { engine.toString(),
								engine.getOwner() }));
				engineList.remove(indexEng);
				indexEng--;
				continue;
			}
			// remove engines with built dates that train does not service
			if (!train.acceptsBuiltDate(engine.getBuilt())) {
				addLine(buildReport, SEVEN, MessageFormat.format(
						Bundle.getMessage("buildExcludeEngineBuilt"), new Object[] { engine.toString(),
								engine.getBuilt() }));
				engineList.remove(indexEng);
				indexEng--;
				continue;
			}
			// skip engines on tracks that don't service the train's departure direction
			if (!checkPickUpTrainDirection(engine, rl)) {
				continue;
			}
			// skip engines that have been assigned destinations that don't match the terminal
			if (engine.getDestination() != null && !engine.getDestinationName().equals(rl.getName())) {
				addLine(buildReport, SEVEN, MessageFormat.format(Bundle
						.getMessage("buildExcludeEngineDestination"), new Object[] { engine.toString(),
						engine.getDestinationName() }));
				continue;
			}
			// remove engines that are out of service
			if (engine.isOutOfService()) {
				addLine(buildReport, SEVEN, MessageFormat.format(Bundle
						.getMessage("buildExcludeEngineOutOfService"), new Object[] { engine.toString(),
						engine.getLocationName(), engine.getTrackName() }));
				engineList.remove(indexEng);
				indexEng--;
				continue;
			}
			// is this engine part of a consist?
			if (engine.getConsist() == null) {
				// single engine, but does the train require a consist?
				if (numberOfEngines > 1) {
					addLine(buildReport, SEVEN, MessageFormat.format(Bundle
							.getMessage("buildExcludeEngineSingle"), new Object[] { engine.toString(),
							numberOfEngines }));
					continue;
				}
				// engine is part of a consist
			} else {
				// Keep only lead engines in consist if required number is correct.
				if (!engine.getConsist().isLead(engine)) {
					addLine(buildReport, SEVEN, MessageFormat.format(Bundle
							.getMessage("buildEnginePartConsist"), new Object[] { engine.toString(),
							engine.getConsist().getName(), engine.getConsist().getEngines().size() }));
					if (engine.getConsist().getSize() != numberOfEngines && numberOfEngines != 0)
						addLine(buildReport, SEVEN, MessageFormat.format(Bundle
								.getMessage("buildExcludeEngConsistNumber"), new Object[] {
								engine.toString(), engine.getConsist().getName(),
								engine.getConsist().getSize() }));
					// remove non-lead engines
					engineList.remove(indexEng);
					indexEng--;
					continue;
					// lead engine in consist
				} else {
					addLine(buildReport, SEVEN, MessageFormat.format(Bundle
							.getMessage("buildEngineLeadConsist"), new Object[] { engine.toString(),
							engine.getConsist().getName(), engine.getConsist().getSize() }));
					if (engine.getConsist().getSize() == numberOfEngines) {
						log.debug("Consist (" + engine.getConsist().getName()
								+ ") has the required number of engines"); // NOI18N
					} else if (numberOfEngines != 0) {
						// log.debug("Consist ("+engine.getConsist().getName()+") doesn't have the required number of engines");
						addLine(buildReport, SEVEN, MessageFormat.format(Bundle
								.getMessage("buildExcludeEngConsistNumber"), new Object[] {
								engine.toString(), engine.getConsist().getName(),
								engine.getConsist().getSize() }));
						continue;
					}
				}
			}
			// found a loco!
			foundLoco = true;

			// now find terminal track for engine(s)
			addLine(buildReport, FIVE, MessageFormat
					.format(Bundle.getMessage("buildEngineRoadModelType"), new Object[] { engine.toString(),
							engine.getRoad(), engine.getModel(), engine.getType() }));
			addLine(buildReport, FIVE, MessageFormat
					.format(Bundle.getMessage("buildAtLocation"), new Object[] {
							(engine.getLocationName() + ", " + engine.getTrackName()), rld.getName() }));
			// is there a staging track?
			if (terminateTrack != null) {
				String status = engine.testDestination(terminateTrack.getLocation(), terminateTrack);
				if (status.equals(Track.OKAY)) {
					addEngineToTrain(engine, rl, rld, terminateTrack);
					engineList.remove(indexEng);
					indexEng--;
					return true; // done
				} else {
					addLine(buildReport, SEVEN, MessageFormat.format(Bundle
							.getMessage("buildCanNotDropEngineToTrack"), new Object[] { engine.toString(),
							terminateTrack.getName(), status }));
				}
				// find a destination track for this engine
			} else {
				Location destination = rld.getLocation();
				List<String> destTracks = destination.getTrackIdsByMovesList(null);
				for (int s = 0; s < destTracks.size(); s++) {
					Track track = destination.getTrackById(destTracks.get(s));
					if (!checkDropTrainDirection(engine, rld, track))
						continue;
					String status = engine.testDestination(destination, track);
					if (status.equals(Track.OKAY)) {
						addEngineToTrain(engine, rl, rld, track);
						engineList.remove(indexEng);
						indexEng--;
						return true; // done
					} else {
						addLine(buildReport, SEVEN, MessageFormat.format(Bundle
								.getMessage("buildCanNotDropEngineToTrack"), new Object[] {
								engine.toString(), track.getName(), status }));
					}
				}
				addLine(buildReport, FIVE, MessageFormat.format(
						Bundle.getMessage("buildCanNotDropEngToDest"), new Object[] { engine.toString(),
								rld.getName() }));
			}
		}
		if (!foundLoco)
			addLine(buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildNoLocosFoundAtLocation"),
					new Object[] { rl.getName() }));
		// not able to assign engines to train
		return false;
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

		for (int i = 0; i < routeList.size() - 1; i++) {
			RouteLocation rl = train.getRoute().getLocationById(routeList.get(i));
			moves += rl.getMaxCarMoves();
			double carDivisor = 16; // number of 40' cars per engine 1% grade
			// change engine requirements based on grade
			if (rl.getGrade() > 1) {
				double grade = rl.getGrade();
				carDivisor = carDivisor / grade;
			}
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
		addLine(buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildAutoBuildMsg"),
				new Object[] { Integer.toString(nE) }));
		if (nE > Setup.getEngineSize()) {
			addLine(buildReport, THREE, MessageFormat.format(Bundle.getMessage("buildMaximumNumberEngines"),
					new Object[] { Setup.getEngineSize() }));
			nE = Setup.getEngineSize();
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
		if (rl == train.getTrainDepartsRouteLocation())
			departTrack = departStageTrack;
		boolean foundCar = false;
		boolean requiresCar = false;
		// Does this train require a car with FRED?
		if ((train.getRequirements() & Train.FRED) == 0) {
			addLine(buildReport, FIVE, Bundle.getMessage("buildTrainNoFred"));
			if (departTrack == null) // if not departing staging we're done
				return;
		} else {
			addLine(buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildTrainReqFred"),
					new Object[] { train.getName(), road, rl.getName(), rld.getName() }));
			requiresCar = true;
		}
		for (carIndex = 0; carIndex < carList.size(); carIndex++) {
			Car car = carManager.getById(carList.get(carIndex));
			if (car.hasFred()) {
				addLine(buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildCarHasFRED"),
						new Object[] { car.toString(), car.getRoad(), car.getLocationName() }));
				// car departing staging must leave with train
				if (car.getTrack() == departTrack) {
					foundCar = false;
					if (checkCarForDestinationAndTrack(car, rl, rld)) {
						if (car.getTrain() == train)
							foundCar = true;
					} else if (findDestinationAndTrack(car, rl, rld)) {
						foundCar = true;
					}
					if (!foundCar)
						throw new BuildFailedException(MessageFormat.format(Bundle
								.getMessage("buildErrorCarStageDest"), new Object[] { car.toString() }));
				}
				// is there a specific road requirement for the car with FRED?
				else if (!road.equals("") && !road.equals(car.getRoad())) {
					addLine(buildReport, SEVEN, MessageFormat.format(Bundle
							.getMessage("buildExcludeCarWrongRoad"), new Object[] { car.toString(),
							car.getType(), car.getRoad() }));
					carList.remove(car.getId()); // remove this car from the list
					carIndex--;
					continue;
				} else if (!foundCar && car.getLocationName().equals(rl.getName())) {
					// remove cars that can't be picked up due to train and track directions
					if (!checkPickUpTrainDirection(car, rl)) {
						addLine(buildReport, SEVEN, MessageFormat.format(Bundle
								.getMessage("buildExcludeCarTypeAtLoc"), new Object[] { car.toString(),
								car.getType(), (car.getLocationName() + " " + car.getTrackName()) }));
						carList.remove(car.getId()); // remove this car from the list
						carIndex--;
						continue;
					}
					if (checkCarForDestinationAndTrack(car, rl, rld)) {
						if (car.getTrain() == train)
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
					new Object[] { train.getName(), Bundle.getMessage("FRED"), rl.getName(), rld.getName() }));
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
			log.error("Departure track for caboose is null");
			return;
		}
		if (rld == null) {
			log.error("Destination track for caboose is null");
			return;
		}
		// load departure track if staging
		Track departTrack = null;
		if (rl == train.getTrainDepartsRouteLocation())
			departTrack = departStageTrack;
		if (!requiresCaboose) {
			addLine(buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildTrainNoCaboose"),
					new Object[] { rl.getName() }));
			if (departTrack == null)
				return;
		} else {
			addLine(buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildTrainReqCaboose"),
					new Object[] { train.getName(), roadCaboose, rl.getName(), rld.getName() }));
		}
		// Does the route have enough moves?
		if (requiresCaboose && rl.getMaxCarMoves() - rl.getCarMoves() <= 0) {
			throw new BuildFailedException(MessageFormat.format(Bundle.getMessage("buildErrorNoMoves"),
					new Object[] {rl.getName(), Bundle.getMessage("Caboose")}));
		}
		// Now go through the car list looking for cabooses
		boolean cabooseTip = true; // add a user tip to the build report about cabooses if none found
		boolean cabooseAtDeparture = false; // set to true if caboose at departure location is found
		boolean foundCaboose = false;
		for (carIndex = 0; carIndex < carList.size(); carIndex++) {
			Car car = carManager.getById(carList.get(carIndex));
			if (car.isCaboose()) {
				cabooseTip = false; // found at least one caboose, so they exist!
				addLine(buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildCarIsCaboose"),
						new Object[] { car.toString(), car.getRoad(), car.getLocationName() }));
				// car departing staging must leave with train
				if (car.getTrack() == departTrack) {
					foundCaboose = false;
					if (checkCarForDestinationAndTrack(car, rl, rld)) {
						if (car.getTrain() == train)
							foundCaboose = true;
					} else if (findDestinationAndTrack(car, rl, rld)) {
						foundCaboose = true;
					}
					if (!foundCaboose)
						throw new BuildFailedException(MessageFormat.format(Bundle
								.getMessage("buildErrorCarStageDest"), new Object[] { car.toString() }));
				}
				// is there a specific road requirement for the caboose?
				else if (!roadCaboose.equals("") && !roadCaboose.equals(car.getRoad())) {
					continue;
				} else if (!foundCaboose && car.getLocationName().equals(rl.getName())) {
					// remove cars that can't be picked up due to train and track directions
					if (!checkPickUpTrainDirection(car, rl)) {
						addLine(buildReport, SEVEN, MessageFormat.format(Bundle
								.getMessage("buildExcludeCarTypeAtLoc"), new Object[] { car.toString(),
								car.getType(), (car.getLocationName() + " " + car.getTrackName()) }));
						carList.remove(car.getId()); // remove this car from the list
						carIndex--;
						continue;
					}
					// first pass, take a caboose that matches the engine
					if (leadEngine != null && car.getRoad().equals(leadEngine.getRoad())) {
						addLine(buildReport, SEVEN, MessageFormat.format(Bundle
								.getMessage("buildCabooseRoadMatches"), new Object[] { car.toString(),
								car.getRoad(), leadEngine.toString() }));
						if (checkCarForDestinationAndTrack(car, rl, rld)) {
							if (car.getTrain() == train)
								foundCaboose = true;
						} else if (findDestinationAndTrack(car, rl, rld)) {
							foundCaboose = true;
						}
						if (!foundCaboose) {
							carList.remove(car.getId()); // remove this car from the list
							carIndex--;
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
			for (carIndex = 0; carIndex < carList.size(); carIndex++) {
				Car car = carManager.getById(carList.get(carIndex));
				if (car.isCaboose() && car.getLocationName().equals(rl.getName())) {
					// is there a specific road requirement for the caboose?
					if (!roadCaboose.equals("") && !roadCaboose.equals(car.getRoad())) {
						continue; // yes
					}
					// okay, we found a caboose at the departure location
					cabooseAtDeparture = true;
					if (checkCarForDestinationAndTrack(car, rl, rld)) {
						if (car.getTrain() == train) {
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
				addLine(buildReport, ONE, Bundle.getMessage("buildNoteCaboose"));
				addLine(buildReport, ONE, Bundle.getMessage("buildNoteCaboose2"));
			}
			if (!cabooseAtDeparture)
				throw new BuildFailedException(MessageFormat.format(Bundle
						.getMessage("buildErrorReqDepature"), new Object[] { train.getName(),
						Bundle.getMessage("Caboose"), rl.getName() }));
			// we did find a caboose at departure that meet requirements, but couldn't place it at destination.
			throw new BuildFailedException(MessageFormat.format(Bundle.getMessage("buildErrorReqDest"),
					new Object[] { train.getName(), Bundle.getMessage("Caboose"), rld.getName() }));
		}
	}

	/**
	 * Removes the remaining cabooses and cars with FRED from consideration. Also saves a car's final destination
	 * in case of train reset.
	 */
	private void removeCaboosesAndCarsWithFredAndSaveFinalDestination() {
		for (carIndex = 0; carIndex < carList.size(); carIndex++) {
			Car car = carManager.getById(carList.get(carIndex));
			if (car.isCaboose() || car.hasFred()) {
				addLine(buildReport, SEVEN, MessageFormat.format(Bundle
						.getMessage("buildExcludeCarTypeAtLoc"), new Object[] { car.toString(),
						car.getType(), (car.getLocationName() + ", " + car.getTrackName()) }));
				carList.remove(car.getId()); // remove this car from the list
				carIndex--;
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
		addLine(buildReport, SEVEN, Bundle.getMessage("buildRemoveCars"));
		for (carIndex = 0; carIndex < carList.size(); carIndex++) {
			Car car = carManager.getById(carList.get(carIndex));
			// remove cars that don't have a track assignment
			if (car.getTrack() == null) {
				addLine(buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildErrorRsNoLoc"),
						new Object[] { car.toString(), car.getLocationName() }));
				carList.remove(car.getId());
				carIndex--;
				continue;
			}
			// remove cars that have been reported as missing
			if (car.isLocationUnknown()) {
				addLine(buildReport, SEVEN, MessageFormat.format(Bundle
						.getMessage("buildExcludeCarLocUnknown"), new Object[] { car.toString(),
						car.getLocationName(), car.getTrackName() }));
				if (car.getTrack().equals(departStageTrack))
					throw new BuildFailedException(MessageFormat.format(Bundle
							.getMessage("buildErrorLocationUnknown"), new Object[] { car.getLocationName(),
							car.getTrackName(), car.toString() }));
				carList.remove(car.getId());
				carIndex--;
				continue;
			}
			// remove cars that are out of service
			if (car.isOutOfService()) {
				addLine(buildReport, SEVEN, MessageFormat.format(Bundle
						.getMessage("buildExcludeCarOutOfService"), new Object[] { car.toString(),
						car.getLocationName(), car.getTrackName() }));
				if (car.getTrack().equals(departStageTrack))
					throw new BuildFailedException(MessageFormat.format(Bundle
							.getMessage("buildErrorLocationOutOfService"), new Object[] {
							car.getLocationName(), car.getTrackName(), car.toString() }));
				carList.remove(car.getId());
				carIndex--;
				continue;
			}

			// remove cars with FRED that have a destination that isn't the terminal
			if (car.hasFred() && car.getDestination() != null && car.getDestination() != terminateLocation) {
				addLine(buildReport, FIVE, MessageFormat.format(
						Bundle.getMessage("buildExcludeCarWrongDest"), new Object[] { car.toString(),
								car.getType(), car.getDestinationName() }));
				carList.remove(car.getId());
				carIndex--;
				continue;
			}

			// remove cabooses that have a destination that isn't the terminal, no caboose changes in the train's route
			if (car.isCaboose() && car.getDestination() != null && car.getDestination() != terminateLocation
					&& train.getSecondLegOptions() == Train.NONE && train.getThirdLegOptions() == Train.NONE) {
				addLine(buildReport, FIVE, MessageFormat.format(
						Bundle.getMessage("buildExcludeCarWrongDest"), new Object[] { car.toString(),
								car.getType(), car.getDestinationName() }));
				carList.remove(car.getId());
				carIndex--;
				continue;
			}

			// is car at interchange?
			if (car.getTrack().getLocType().equals(Track.INTERCHANGE)) {
				// don't service a car at interchange and has been dropped of by this train
				if (car.getTrack().getPickupOption().equals(Track.ANY)
						&& car.getSavedRouteId().equals(train.getRoute().getId())) {
					addLine(buildReport, SEVEN, MessageFormat.format(Bundle
							.getMessage("buildExcludeCarDropByTrain"), new Object[] { car.toString(),
							(car.getLocationName() + ", " + car.getTrackName()) }));
					carList.remove(car.getId());
					carIndex--;
					continue;
				}
			}
			if (car.getTrack().getLocType().equals(Track.INTERCHANGE)
					|| car.getTrack().getLocType().equals(Track.SPUR)) {
				if (car.getTrack().getPickupOption().equals(Track.TRAINS)
						|| car.getTrack().getPickupOption().equals(Track.EXCLUDE_TRAINS)) {
					if (car.getTrack().acceptsPickupTrain(train)) {
						log.debug("Car (" + car.toString() + ") can be picked up by this train");
					} else {
						addLine(buildReport, SEVEN, MessageFormat.format(Bundle
								.getMessage("buildExcludeCarByTrain"), new Object[] { car.toString(),
								(car.getLocationName() + ", " + car.getTrackName()) }));
						carList.remove(car.getId());
						carIndex--;
						continue;
					}
				} else if (car.getTrack().getPickupOption().equals(Track.ROUTES)
						|| car.getTrack().getPickupOption().equals(Track.EXCLUDE_ROUTES)) {
					if (car.getTrack().acceptsPickupRoute(train.getRoute())) {
						log.debug("Car (" + car.toString() + ") can be picked up by this route");
					} else {
						addLine(buildReport, SEVEN, MessageFormat.format(Bundle
								.getMessage("buildExcludeCarByRoute"), new Object[] { car.toString(),
								(car.getLocationName() + ", " + car.getTrackName()) }));
						carList.remove(car.getId());
						carIndex--;
						continue;
					}
				}
			}

			// all cars in staging must be accepted, so don't exclude if in staging
			// note that for trains departing staging the engine and car roads and types were
			// checked in the routine checkDepartureStagingTrack().
			if (departStageTrack == null || car.getTrack() != departStageTrack) {
				if (!train.acceptsRoadName(car.getRoad())) {
					addLine(buildReport, SEVEN, MessageFormat.format(Bundle
							.getMessage("buildExcludeCarWrongRoad"), new Object[] { car.toString(),
							car.getType(), car.getRoad() }));
					carList.remove(car.getId());
					carIndex--;
					continue;
				}
				if (!train.acceptsTypeName(car.getType())) {
					addLine(buildReport, SEVEN, MessageFormat.format(Bundle
							.getMessage("buildExcludeCarTypeAtLoc"), new Object[] { car.toString(),
							car.getType(), (car.getLocationName() + ", " + car.getTrackName()) }));
					carList.remove(car.getId());
					carIndex--;
					continue;
				}
				if (!car.isCaboose() && !car.isPassenger() && !train.acceptsLoad(car.getLoad(), car.getType())) {
					addLine(buildReport, SEVEN, MessageFormat.format(Bundle
							.getMessage("buildExcludeCarLoadAtLoc"), new Object[] { car.toString(),
							car.getLoad(), (car.getLocationName() + ", " + car.getTrackName()) }));
					carList.remove(car.getId());
					carIndex--;
					continue;
				}
				if (!train.acceptsOwnerName(car.getOwner())) {
					addLine(buildReport, SEVEN, MessageFormat.format(Bundle
							.getMessage("buildExcludeCarOwnerAtLoc"), new Object[] { car.toString(),
							car.getOwner(), (car.getLocationName() + ", " + car.getTrackName()) }));
					carList.remove(car.getId());
					carIndex--;
					continue;
				}
				if (!train.acceptsBuiltDate(car.getBuilt())) {
					addLine(buildReport, SEVEN, MessageFormat.format(Bundle
							.getMessage("buildExcludeCarBuiltAtLoc"), new Object[] { car.toString(),
							car.getBuilt(), (car.getLocationName() + ", " + car.getTrackName()) }));
					carList.remove(car.getId());
					carIndex--;
					continue;
				}
				// remove cars with FRED if not needed by train
				if (car.hasFred() && (train.getRequirements() & Train.FRED) == 0) {
					addLine(buildReport, SEVEN, MessageFormat.format(Bundle
							.getMessage("buildExcludeCarWithFredAtLoc"), new Object[] { car.toString(),
							car.getType(), (car.getLocationName() + ", " + car.getTrackName()) }));
					carList.remove(car.getId()); // remove this car from the list
					carIndex--;
					continue;
				}
				// does car have a wait count?
				if (car.getWait() > 0 && train.services(car)) {
					addLine(buildReport, SEVEN, MessageFormat.format(
							Bundle.getMessage("buildExcludeCarWait"),
							new Object[] { car.toString(), car.getType(),
									(car.getLocationName() + ", " + car.getTrackName()), car.getWait() }));
					car.setWait(car.getWait() - 1); // decrement wait count
					carList.remove(car.getId());
					carIndex--;
					continue;
				}
			}
		}
		// adjust car list to only have cars from one staging track
		if (departStageTrack != null) {
			// Make sure that all cars in staging are moved
			train.getTrainDepartsRouteLocation().setCarMoves(
					train.getTrainDepartsRouteLocation().getMaxCarMoves() - departStageTrack.getNumberCars());
			int numCarsFromStaging = 0;
			numOfBlocks = new Hashtable<String, Integer>();
			for (carIndex = 0; carIndex < carList.size(); carIndex++) {
				Car c = carManager.getById(carList.get(carIndex));
				if (c.getLocationName().equals(departLocation.getName())) {
					if (c.getTrackName().equals(departStageTrack.getName())) {
						numCarsFromStaging++;
						// populate car blocking hashtable
						// don't block cabooses, cars with FRED, or passenger. Only block lead cars in kernel
						if (!c.isCaboose() && !c.hasFred() && !c.isPassenger()
								&& (c.getKernel() == null || c.getKernel().isLead(c))) {
							log.debug("last location id: " + c.getLastLocationId());
							Integer number = 1;
							if (numOfBlocks.containsKey(c.getLastLocationId())) {
								number = numOfBlocks.get(c.getLastLocationId()) + 1;
								numOfBlocks.remove(c.getLastLocationId());
							}
							numOfBlocks.put(c.getLastLocationId(), number);
						}
					} else {
						addLine(buildReport, SEVEN, MessageFormat.format(Bundle
								.getMessage("buildExcludeCarAtLoc"), new Object[] { c.toString(),
								(c.getLocationName() + ", " + c.getTrackName()) }));
						carList.remove(c.getId());
						carIndex--;
					}
				}
			}
			// show how many cars are departing from staging
			addLine(buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildDepartingStagingCars"),
					new Object[] { departStageTrack.getLocation().getName(), departStageTrack.getName(),
							numCarsFromStaging }));
			// and list them
			for (carIndex = 0; carIndex < carList.size(); carIndex++) {
				Car c = carManager.getById(carList.get(carIndex));
				if (c.getTrack() == departStageTrack) {
					addLine(buildReport, SEVEN, MessageFormat.format(Bundle
							.getMessage("buildStagingCarAtLoc"), new Object[] { c.toString(), c.getType(),
							c.getLoad() }));
				}
			}
			// error if all of the cars and engines from staging aren't available
			if (numCarsFromStaging + departStageTrack.getNumberEngines() != departStageTrack.getNumberRS()) {
				throw new BuildFailedException(MessageFormat.format(Bundle.getMessage("buildErrorNotAll"),
						new Object[] { Integer.toString(departStageTrack.getNumberRS()
								- (numCarsFromStaging + departStageTrack.getNumberEngines())) }));
			}
			log.debug("Staging departure track (" + departStageTrack.getName() + ") has "
					+ numCarsFromStaging + " cars and " + numOfBlocks.size() + " blocks"); // NOI18N
		}

		// show how many cars were found
		addLine(buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildFoundCars"), new Object[] {
				Integer.toString(carList.size()), train.getName() }));

		// now go through the car list and remove non-lead cars in kernels, destinations that aren't part of this route
		for (carIndex = 0; carIndex < carList.size(); carIndex++) {
			Car c = carManager.getById(carList.get(carIndex));
			// only print out the first 500 cars
			if (carIndex < 500) {
				if (c.getLoadPriority().equals(CarLoad.PRIORITY_LOW))
					addLine(buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildCarAtLocWithMoves"),
							new Object[] { c.toString(), c.getType(), (c.getLocationName() + ", " + c.getTrackName()),
						c.getMoves() }));
				else
					addLine(buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildCarAtLocWithMovesPriority"),
							new Object[] { c.toString(), c.getType(), (c.getLocationName() + ", " + c.getTrackName()),
						c.getMoves(), c.getLoadPriority() }));
			}
			if (carIndex == 500)
				addLine(buildReport, FIVE, Bundle.getMessage("buildOnlyFirst500Cars"));
			// use only the lead car in a kernel for building trains
			if (c.getKernel() != null) {
				addLine(buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildCarPartOfKernel"),
						new Object[] { c.toString(), c.getKernelName(), c.getKernel().getSize() }));
				if (c.getKernel().isLead(c)) {
					checkKernel(c);
				} else {
					carList.remove(c.getId()); // remove this car from the list
					carIndex--;
					continue;
				}
			}
			if (train.equals(c.getTrain())) {
				addLine(buildReport, THREE, MessageFormat.format(
						Bundle.getMessage("buildCarAlreadyAssigned"), new Object[] { c.toString() }));
			}
			// does car have a destination that is part of this train's route?
			if (c.getDestination() != null) {
				addLine(buildReport, SEVEN, MessageFormat.format(
						Bundle.getMessage("buildCarHasAssignedDest"), new Object[] { c.toString(),
								(c.getDestinationName() + ", " + c.getDestinationTrackName()) }));
				RouteLocation rld = train.getRoute().getLastLocationByName(c.getDestinationName());
				if (rld == null) {
					addLine(buildReport, FIVE, MessageFormat.format(Bundle
							.getMessage("buildExcludeCarDestNotPartRoute"), new Object[] { c.toString(),
							c.getDestinationName(), train.getRoute().getName() }));
					// build failure if car departing staging
					if (c.getLocation().equals(departLocation) && departStageTrack != null) {
						// The following code should not be executed, departing staging tracks are checked before this
						// routine.
						throw new BuildFailedException(MessageFormat.format(Bundle
								.getMessage("buildErrorCarNotPartRoute"), new Object[] { c.toString() }));
					}
					carList.remove(c.getId()); // remove this car from the list
					carIndex--;
				}
			}
		}
		return;
	}

	/**
	 * Verifies that all cars in the kernel have the same departure track.
	 * 
	 * @param car
	 * @throws BuildFailedException
	 */
	private void checkKernel(Car car) throws BuildFailedException {
		List<Car> cars = car.getKernel().getCars();
		for (int i = 0; i < cars.size(); i++) {
			Car c = cars.get(i);
			if (car.getLocation() != c.getLocation() || car.getTrack() != c.getTrack())
				throw new BuildFailedException(MessageFormat.format(Bundle
						.getMessage("buildErrorCarKernelLocation"), new Object[] { c.toString(),
						car.getKernelName(), car.toString() }));
		}
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
		if (departStageTrack == null || !departStageTrack.isBlockCarsEnabled()) {
			return;
		}

		addLine(buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("blockDepartureHasBlocks"),
				new Object[] { departStageTrack.getName(), numOfBlocks.size() }));

		Enumeration<String> en = numOfBlocks.keys();
		while (en.hasMoreElements()) {
			String locId = en.nextElement();
			int numCars = numOfBlocks.get(locId);
			String locName = "";
			Location l = locationManager.getLocationById(locId);
			if (l != null)
				locName = l.getName();
			addLine(buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("blockFromHasCars"),
					new Object[] { locId, locName, numCars }));
			if (numOfBlocks.size() < 2) {
				addLine(buildReport, SEVEN, Bundle.getMessage("blockUnable"));
				return;
			}
		}
		blockByLocationMoves();
		addLine(buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("blockDone"),
				new Object[] { departStageTrack.getName() }));
	}

	/**
	 * Blocks cars out of staging by assigning the largest blocks of cars to locations requesting the most moves.
	 */
	private void blockByLocationMoves() throws BuildFailedException {
		// start at the second location in the route to begin blocking
		List<String> routeList = train.getRoute().getLocationsBySequenceList();
		for (int i = 1; i < routeList.size(); i++) {
			RouteLocation rl = train.getRoute().getLocationById(routeList.get(i));
			int possibleMoves = rl.getMaxCarMoves() - rl.getCarMoves();
			if (rl.canDrop() && possibleMoves > 0) {
				addLine(buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("blockLocationHasMoves"),
						new Object[] { rl.getName(), possibleMoves }));
			}
		}
		// now block out cars, send the largest block of cars to the locations requesting the greatest number of moves
		RouteLocation rl = train.getTrainDepartsRouteLocation();
		while (true) {
			String blockId = getLargestBlock(); // get the id of the largest block of cars
			if (blockId.equals("") || numOfBlocks.get(blockId) == 1)
				break; // done
			RouteLocation rld = getLocationWithMaximumMoves(routeList, blockId); // get the location with the greatest
																					// number of moves
			if (rld == null)
				break; // done
			// check to see if there are enough moves for all of the cars departing staging
			if (rld.getMaxCarMoves() > numOfBlocks.get(blockId)) {
				// remove the largest block and maximum moves RouteLocation from the lists
				numOfBlocks.remove(blockId);
				// block 0 cars have never left staging.
				if (blockId.equals("0"))
					continue;
				routeList.remove(rld.getId());
				Location loc = locationManager.getLocationById(blockId);
				Location setOutLoc = rld.getLocation();
				if (loc != null && setOutLoc != null && checkDropTrainDirection(rld)) {
					for (carIndex = 0; carIndex < carList.size(); carIndex++) {
						Car car = carManager.getById(carList.get(carIndex));
						if (car.getTrack() == departStageTrack && car.getLastLocationId().equals(blockId)) {
							if (car.getDestination() != null) {
								addLine(buildReport, SEVEN, MessageFormat.format(Bundle
										.getMessage("blockNotAbleDest"), new Object[] { car.toString(),
										car.getDestinationName() }));
								continue;
							}
							if (car.getFinalDestination() != null) {
								addLine(buildReport, SEVEN, MessageFormat.format(Bundle
										.getMessage("blockNotAbleFinalDest"), new Object[] { car.toString(),
										car.getFinalDestination().getName() }));
								continue;
							}
							if (!car.getLoad().equals(CarLoads.instance().getDefaultEmptyName())
									&& !car.getLoad().equals(CarLoads.instance().getDefaultLoadName())) {
								addLine(buildReport, SEVEN, MessageFormat.format(Bundle
										.getMessage("blockNotAbleCustomLoad"), new Object[] { car.toString(),
										car.getLoad() }));
								continue;
							}
							if (car.getLoad().equals(CarLoads.instance().getDefaultEmptyName())
									&& (departStageTrack.isAddLoadsEnabled()
											|| departStageTrack.isAddLoadsAnySpurEnabled() || departStageTrack
												.isAddCustomLoadsAnyStagingTrackEnabled())) {
								addLine(buildReport, SEVEN, MessageFormat.format(Bundle
										.getMessage("blockNotAbleCarTypeGenerate"), new Object[] {
										car.toString(), car.getLoad() }));
								continue;
							}
							addLine(buildReport, SEVEN, MessageFormat.format(
									Bundle.getMessage("blockingCar"), new Object[] { car.toString(),
											loc.getName(), rld.getName() }));
							if (!findDestinationAndTrack(car, rl, rld)) {
								addLine(buildReport, SEVEN, MessageFormat.format(Bundle
										.getMessage("blockNotAbleCarType"), new Object[] { car.toString(),
										rld.getName(), car.getType() }));
							}
						}
					}
				}
			} else {
				addLine(buildReport, SEVEN, MessageFormat.format(
						Bundle.getMessage("blockDestNotEnoughMoves"), new Object[] { rl.getName(), blockId }));
				numOfBlocks.remove(blockId); // block is too large for any stop along this train's route
			}
		}
	}

	private String getLargestBlock() {
		Enumeration<String> en = numOfBlocks.keys();
		String largestBlock = "";
		int maxCars = 0;
		while (en.hasMoreElements()) {
			String locId = en.nextElement();
			if (numOfBlocks.get(locId) > maxCars) {
				largestBlock = locId;
				maxCars = numOfBlocks.get(locId);
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
	private RouteLocation getLocationWithMaximumMoves(List<String> routeList, String blockId) {
		RouteLocation rlMax = null;
		int maxMoves = 0;
		for (int i = 1; i < routeList.size(); i++) {
			RouteLocation rl = train.getRoute().getLocationById(routeList.get(i));
			if (rl.getMaxCarMoves() - rl.getCarMoves() > maxMoves) {
				maxMoves = rl.getMaxCarMoves() - rl.getCarMoves();
				rlMax = rl;
			}
			// if two locations have the same number of moves, return the one that doesn't match the block id
			if (rl.getMaxCarMoves() - rl.getCarMoves() == maxMoves
					&& !rl.getLocation().getId().equals(blockId)) {
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
			addLine(buildReport, THREE, MessageFormat.format(Bundle.getMessage("buildMultiplePass"),
					new Object[] { percent }));
			multipass = true;
		}
		if (percent == 100 && multipass) {
			addLine(buildReport, SEVEN, BLANK_LINE); // add line when in very detailed report mode
			addLine(buildReport, THREE, Bundle.getMessage("buildFinalPass"));
		}
		noMoreMoves = false; // need to reset this in case noMoreMoves is true on first pass
		// determine how many locations are serviced by this train
		int numLocs = routeList.size();
		if (numLocs > 1) // don't find car destinations for the last location in the route
			numLocs--;
		// now go through each location starting at departure and place cars as requested
		for (int routeIndex = 0; routeIndex < numLocs; routeIndex++) {
			RouteLocation rl = train.getRoute().getLocationById(routeList.get(routeIndex));
			if (train.skipsLocation(rl.getId())) {
				addLine(buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildLocSkipped"),
						new Object[] { rl.getName(), train.getName() }));
				continue;
			}
			if (!rl.canPickup()) {
				addLine(buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildLocNoPickups"),
						new Object[] { train.getRoute().getName(), rl.getName() }));
				continue;
			}
			if (!checkPickUpTrainDirection(rl)) {
				continue;
			}
			moves = 0; // the number of moves for this location
			success = true; // true when done with this location
			reqNumOfMoves = rl.getMaxCarMoves() - rl.getCarMoves(); // the number of moves requested
			int saveReqMoves = reqNumOfMoves; // save a copy for status message
			// multiple pass build?
			if (percent < 100) {
				reqNumOfMoves = reqNumOfMoves * percent / 100;
				// Departing staging?
				if (routeIndex == 0 && departStageTrack != null) {
					reqNumOfMoves = 0; // Move cars out of staging after working other locations
					// if leaving and returning to staging on the same track temporary pull cars off the track
					if (departStageTrack == terminateStageTrack 
							&& !train.isAllowReturnToStagingEnabled() && !Setup.isAllowReturnToStagingEnabled()) {
						for (int i = 0; i < carList.size(); i++) {
							Car car = carManager.getById(carList.get(i));
							if (car.getTrack() == departStageTrack)
								car.setLocation(car.getLocation(), null);
						}
					}
					addLine(buildReport, THREE, MessageFormat.format(Bundle
							.getMessage("buildDepartStagingAggressive"), new Object[] { departStageTrack
							.getLocation().getName() }));
				}
				// restore departure track for cars departing staging
			} else if (departStageTrack != null && departStageTrack == terminateStageTrack
					&& !train.isAllowReturnToStagingEnabled() && !Setup.isAllowReturnToStagingEnabled()) {
				for (int i = 0; i < carList.size(); i++) {
					Car car = carManager.getById(carList.get(i));
					if (car.getTrack() == null)
						car.setLocation(car.getLocation(), departStageTrack, true); // force
				}
			}
			addLine(buildReport, THREE, MessageFormat.format(Bundle.getMessage("buildLocReqMoves"),
					new Object[] { rl.getName(), reqNumOfMoves, saveReqMoves, rl.getMaxCarMoves() }));
			addLine(buildReport, SEVEN, BLANK_LINE); // add line when in very detailed report mode
			findDestinationsForCarsFromLocation(rl, routeIndex, false);
			// perform a another pass if aggressive and there are requested moves
			// this will perform local moves at this location, services off spot tracks
			if (Setup.isBuildAggressive() && saveReqMoves != reqNumOfMoves) {
				findDestinationsForCarsFromLocation(rl, routeIndex, true);
			}

			// we might have freed up space at a spur that has an alternate track
			redirectCarsFromAlternateTrack();

			if (routeIndex == 0)
				checkDepartureForStaging(percent); // report ASAP that the build has failed
			
			addLine(buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildStatusMsg"), new Object[] {
					(success ? Bundle.getMessage("Success") : Bundle.getMessage("Partial")),
					Integer.toString(moves), Integer.toString(saveReqMoves), rl.getName(), train.getName() }));
			
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
	 * @param secondPass
	 *            When true this is the second time we've looked at these cars.
	 * @throws BuildFailedException
	 */
	private void findDestinationsForCarsFromLocation(RouteLocation rl, int routeIndex, boolean secondPass)
			throws BuildFailedException {
		if (reqNumOfMoves <= 0)
			return;
		boolean messageFlag = true;
		success = false;
		for (carIndex = 0; carIndex < carList.size(); carIndex++) {
			Car car = carManager.getById(carList.get(carIndex));
			// second pass only cares about cars that have a final destination equal to this location
			if (secondPass && !car.getFinalDestinationName().equals(rl.getName()))
				continue;
			// find a car at this location
			if (!car.getLocationName().equals(rl.getName()))
				continue;
			// can this car be picked up?
			if (!checkPickUpTrainDirection(car, rl))
				continue; // no
			// add message that we're on the second pass for this location
			if (secondPass && messageFlag) {
				messageFlag = false;
				noMoreMoves = false; // we're on a second pass, there might be moves now
				addLine(buildReport, THREE, MessageFormat.format(Bundle
						.getMessage("buildSecondPassForLocation"), new Object[] { rl.getName() }));
			}
			// check for car order?
			car = getCarOrder(car);
			// is car departing staging and generate custom load?
			if (!generateCarLoadFromStaging(car))
				generateCarLoadStagingToStaging(car);
			// does car have a custom load without a destination?
			// If departing staging, a destination for this car is needed.
			if (findFinalDestinationForCarLoad(car) && car.getDestination() == null
					&& car.getTrack() != departStageTrack) {
				// done with this car, it has a custom load, and there are spurs/schedules, but no destination found
				addLine(buildReport, SEVEN, BLANK_LINE); // add line when in very detailed report mode
				continue;
			}
			// does car have a final destination, but no destination
			if (car.getFinalDestination() != null && car.getDestination() == null) {
				// no local moves for this train?
				if (!train.isAllowLocalMovesEnabled()
						&& splitString(car.getLocationName()).equals(
								splitString(car.getFinalDestinationName()))) {
					addLine(buildReport, FIVE, MessageFormat.format(Bundle
							.getMessage("buildCarHasFinalDestNoMove"), new Object[] { car.toString(),
						car.getFinalDestinationName() }));
					addLine(buildReport, SEVEN, BLANK_LINE); // add line when in very detailed report mode
					log.debug("Removing car (" + car.toString() + ") from list");
					carList.remove(car.getId());
					carIndex--;
					continue;
				}
				// no through traffic from origin to terminal?
				if (!train.isAllowThroughCarsEnabled()
						&& !train.isLocalSwitcher()
						&& !car.isCaboose()
						&& !car.hasFred()
						&& !car.isPassenger()
						&& splitString(car.getLocationName()).equals(splitString(departLocation.getName()))
						&& splitString(car.getFinalDestinationName()).equals(splitString(terminateLocation.getName()))
						&& !splitString(car.getLocationName()).equals(splitString(car.getFinalDestinationName()))) {
					addLine(buildReport, FIVE, MessageFormat.format(Bundle
							.getMessage("buildCarHasFinalDestination"), new Object[] { car.toString(),
							departLocation.getName(), terminateLocation.getName() }));
					addLine(buildReport, FIVE, MessageFormat.format(Bundle
							.getMessage("buildThroughTrafficNotAllow"), new Object[] {
							departLocation.getName(), terminateLocation.getName() }));
					addLine(buildReport, SEVEN, BLANK_LINE); // add line when in very detailed report mode
					log.debug("Removing car (" + car.toString() + ") from list");
					carList.remove(car.getId());
					carIndex--;
					continue;
				}
				addLine(buildReport, FIVE, MessageFormat.format(Bundle
						.getMessage("buildCarRoutingBegins"), new Object[] { car.toString(), car.getType(),
					car.getLoad(), car.getLocationName(), car.getTrackName(), car.getFinalDestinationName(),
					car.getFinalDestinationTrackName() }));
				if (!Router.instance().setDestination(car, train, buildReport)) {
					addLine(buildReport, SEVEN, MessageFormat.format(Bundle
							.getMessage("buildNotAbleToSetDestination"), new Object[] { car.toString(),
						Router.instance().getStatus() }));
					// don't move car if routing issue was track space but not departing staging
					if ((!Router.instance().getStatus().startsWith(Track.LENGTH) && !Router.instance()
							.getStatus().startsWith(Track.CAPACITY))
							|| (car.getLocationName().equals(departLocation.getName()) && departStageTrack != null))
						// move this car, routing failed!
						findDestinationAndTrack(car, rl, routeIndex, routeList.size());
					else
						addLine(buildReport, SEVEN, BLANK_LINE); // add line when in very detailed report mode
				} else {
					// did the router assign a destination?
					if (!checkCarForDestinationAndTrack(car, rl, routeIndex)
							&& car.getTrack() != departStageTrack) {
						log.debug("Skipping car (" + car.toString() + ") no car destination"); // NOI18N
						addLine(buildReport, SEVEN, BLANK_LINE); // add line when in very detailed report mode
						continue;
					}
				}
			}
			// does car have a destination?
			else if (checkCarForDestinationAndTrack(car, rl, routeIndex)) {
				// car does not have a destination, search for the best one
			} else {
				findDestinationAndTrack(car, rl, routeIndex, routeList.size());
			}
			if (success) {
				// log.debug("done with location ("+destinationSave.getName()+")");
				break;
			}
			// build failure if car departing staging without a destination and a train
			// we'll just put out a warning message here so we can find out how many cars have issues
			if (car.getLocationName().equals(departLocation.getName())
					&& departStageTrack != null
					&& (car.getDestination() == null || car.getDestinationTrack() == null || car
					.getTrain() == null)) {
				addLine(buildReport, ONE, MessageFormat.format(Bundle
						.getMessage("buildErrorCarStageDest"), new Object[] { car.toString() }));
				addLine(buildReport, SEVEN, BLANK_LINE); // add line when in very detailed report mode
			}
			// are there still moves available?
			if (noMoreMoves) {
				addLine(buildReport, FIVE, Bundle.getMessage("buildNoAvailableDestinations"));
				break;
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
		if (departStageTrack == null)
			return; // no, so we're done
		int carCount = 0;
		StringBuffer buf = new StringBuffer();
		// confirm that all cars in staging are departing
		for (carIndex = 0; carIndex < carList.size(); carIndex++) {
			Car car = carManager.getById(carList.get(carIndex));
			// build failure if car departing staging without a destination and a train
			if (car.getLocationName().equals(departLocation.getName())
					&& (car.getDestination() == null || car.getDestinationTrack() == null || car.getTrain() == null)) {
				carCount++;
				if (carCount < 21)
					buf.append(NEW_LINE + " " + car.toString());
			}
		}
		if (carCount > 0) {
			log.debug(carCount + " cars stuck in staging");
			String msg = MessageFormat.format(Bundle.getMessage("buildStagingCouldNotFindDest"),
					new Object[] { carCount, departStageTrack.getLocation().getName(),
							departStageTrack.getName() });
			throw new BuildFailedException(msg + buf.toString(), BuildFailedException.STAGING);
		}
	}

	private void addEngineToTrain(Engine engine, RouteLocation rl, RouteLocation rld, Track track) {
		leadEngine = engine;
		if (train.getLeadEngine() == null)
			train.setLeadEngine(engine); // load lead engine
		addLine(buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildEngineAssigned"),
				new Object[] { engine.toString(), rld.getName(), track.getName() }));
		engine.setDestination(track.getLocation(), track);
		int length = Integer.parseInt(engine.getLength()) + Engine.COUPLER;
		int weightTons = engine.getAdjustedWeightTons();
		// engine in consist?
		if (engine.getConsist() != null) {
			List<Engine> cEngines = engine.getConsist().getEngines();
			length = engine.getConsist().getTotalLength();
			weightTons = engine.getConsist().getAdjustedWeightTons();
			for (int j = 0; j < cEngines.size(); j++) {
				Engine cEngine = cEngines.get(j);
				if (cEngine == engine)
					continue;
				addLine(buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildEngineAssigned"),
						new Object[] { cEngine.toString(), rld.getName(), track.getName() }));
				cEngine.setTrain(train);
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
		addLine(buildReport, THREE, MessageFormat.format(Bundle.getMessage("buildCarAssignedDest"),
				new Object[] { car.toString(), (rld.getName() + ", " + track.getName()) }));
		car.setDestination(track.getLocation(), track);
		// don't update car's previous location if just re-staging car
		if (routeList.size() > 2)
			car.setLastLocationId(car.getLocationId());
		int length = car.getTotalLength();
		int weightTons = car.getAdjustedWeightTons();
		// car could be part of a kernel
		if (car.getKernel() != null) {
			length = car.getKernel().getTotalLength(); // includes couplers
			weightTons = car.getKernel().getAdjustedWeightTons();
			List<Car> kCars = car.getKernel().getCars();
			addLine(buildReport, THREE, MessageFormat.format(Bundle.getMessage("buildCarPartOfKernel"),
					new Object[] { car.toString(), car.getKernelName(), kCars.size() }));
			for (int i = 0; i < kCars.size(); i++) {
				Car kCar = kCars.get(i);
				if (kCar == car)
					continue;
				addLine(buildReport, THREE, MessageFormat.format(Bundle
						.getMessage("buildCarKernelAssignedDest"), new Object[] { kCar.toString(),
						kCar.getKernelName(), (rld.getName() + ", " + track.getName()) }));
				kCar.setTrain(train);
				kCar.setRouteLocation(rl);
				kCar.setRouteDestination(rld);
				kCar.setDestination(track.getLocation(), track, true); // force destination
				// save final destination and track values in case of train reset
				kCar.setPreviousFinalDestination(car.getPreviousFinalDestination());
				kCar.setPreviousFinalDestinationTrack(car.getPreviousFinalDestinationTrack());
			}
		}
		// warn if car's load wasn't generated out of staging
		if (!train.acceptsLoad(car.getLoad(), car.getType())) {
			addLine(buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildWarnCarDepartStaging"),
					new Object[] { car.toString(), car.getLoad() }));
		}
		addLine(buildReport, SEVEN, BLANK_LINE); // add line when in very detailed report mode
		numberCars++; // bump number of cars moved by this train
		moves++; // bump number of car pick up moves for the location
		reqNumOfMoves--; // decrement number of moves left for the location
		if (reqNumOfMoves <= 0)
			success = true; // done with this location!
		carList.remove(car.getId());
		carIndex--; // removed car from list, so backup pointer

		rl.setCarMoves(rl.getCarMoves() + 1);
		if (rl != rld)
			rld.setCarMoves(rld.getCarMoves() + 1);
		// now adjust train length and weight for each location that car is in the train
		addRsToTrain(car, rl, rld, track, length, weightTons);
		return;
	}

	private void addRsToTrain(RollingStock rs, RouteLocation rl, RouteLocation rld, Track track, int length,
			int weightTons) {
		rl.getLocation().setStatus();
		rld.getLocation().setStatus();
		rs.setTrain(train);
		rs.setRouteLocation(rl);
		rs.setRouteDestination(rld);
		// now adjust train length and weight for each location that the rolling stock is in the train
		boolean inTrain = false;
		for (int i = 0; i < routeList.size(); i++) {
			RouteLocation r = train.getRoute().getLocationById(routeList.get(i));
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
			if (r.getTrainWeight() > maxWeight) {
				maxWeight = r.getTrainWeight(); // used for AUTO engines
			}
		}
	}

	private boolean checkPickUpTrainDirection(RollingStock rs, RouteLocation rl) {
		// check that car or engine is located on a track
		if (rs.getTrack() == null) {
			addLine(buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildErrorRsNoLoc"),
					new Object[] { rs.toString(), rs.getLocationName() }));
			return false;
		}
		if (train.isLocalSwitcher()) // ignore local switcher direction
			return true;
		if ((rl.getTrainDirection() & rs.getLocation().getTrainDirections() & rs.getTrack()
				.getTrainDirections()) > 0)
			return true;

		// Only track direction can cause the following message. Location direction has already been checked
		addLine(buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildRsCanNotPickupUsingTrain"),
				new Object[] { rs.toString(), rl.getTrainDirectionString(), rs.getTrack().getName() }));
		addLine(buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildRsCanNotPickupUsingTrain2"),
				new Object[] { rs.getLocation().getName() }));
		return false;
	}

	private boolean checkPickUpTrainDirection(RouteLocation rl) {
		if (train.isLocalSwitcher()) // ignore local switcher direction
			return true;
		if ((rl.getTrainDirection() & rl.getLocation().getTrainDirections()) > 0)
			return true;

		addLine(buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildLocDirection"), new Object[] {
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
		for (int i = 0; i < routeList.size(); i++) {
			RouteLocation rlt = train.getRoute().getLocationById(routeList.get(i));
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
				addLine(buildReport, FIVE, MessageFormat.format(Bundle
						.getMessage("buildCanNotPickupCarLength"), new Object[] { car.toString(), length }));
				addLine(buildReport, FIVE, MessageFormat.format(Bundle
						.getMessage("buildCanNotPickupCarLength2"), new Object[] { rlt.getMaxTrainLength(),
						rlt.getName() }));
				return false;
			}
		}
		return true;
	}

	private final boolean ignoreTrainDirectionIfLastLoc = false;

	private boolean checkDropTrainDirection(RollingStock rs, RouteLocation rld, Track track) {
		// local?
		if (train.isLocalSwitcher())
			return true;
		// is the destination the last location on the route?
		if (ignoreTrainDirectionIfLastLoc && rld == train.getTrainTerminatesRouteLocation())
			return true; // yes, ignore train direction

		// this location only services trains with these directions
		int serviceTrainDir = rld.getLocation().getTrainDirections();
		if (track != null)
			serviceTrainDir = serviceTrainDir & track.getTrainDirections();
		if ((rld.getTrainDirection() & serviceTrainDir) > 0) {
			return true;
		}
		if (rs == null) {
			addLine(buildReport, FIVE, MessageFormat.format(Bundle
					.getMessage("buildDestinationDoesNotService"), new Object[] {
					rld.getLocation().getName(), rld.getTrainDirectionString() }));
			return false;
		}
		addLine(buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildCanNotDropRsUsingTrain"),
				new Object[] { rs.toString(), rld.getTrainDirectionString() }));
		if (track != null)
			addLine(buildReport, FIVE, MessageFormat.format(
					Bundle.getMessage("buildCanNotDropRsUsingTrain2"), new Object[] { track.getName() }));
		else
			addLine(buildReport, FIVE, MessageFormat.format(
					Bundle.getMessage("buildCanNotDropRsUsingTrain3"), new Object[] { rld.getLocation()
							.getName() }));
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
		if (track.getLocType().equals(Track.INTERCHANGE) || track.getLocType().equals(Track.SPUR)) {
			if (track.getDropOption().equals(Track.TRAINS)
					|| track.getDropOption().equals(Track.EXCLUDE_TRAINS)) {
				if (track.acceptsDropTrain(train)) {
					log.debug("Car (" + car.toString() + ") can be droped by train to track ("
							+ track.getName() + ")");
				} else {
					addLine(buildReport, SEVEN, MessageFormat.format(Bundle
							.getMessage("buildCanNotDropCarTrain"), new Object[] { car.toString(),
							train.getName(), track.getName() }));
					return false;
				}
			}
			if (track.getDropOption().equals(Track.ROUTES)
					|| track.getDropOption().equals(Track.EXCLUDE_ROUTES)) {
				if (track.acceptsDropRoute(train.getRoute())) {
					log.debug("Car (" + car.toString() + ") can be droped by route to track ("
							+ track.getName() + ")");
				} else {
					addLine(buildReport, SEVEN, MessageFormat.format(Bundle
							.getMessage("buildCanNotDropCarRoute"), new Object[] { car.toString(),
							train.getRoute().getName(), track.getName() }));
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
			addLine(buildReport, THREE, MessageFormat.format(Bundle.getMessage("buildStagingEmpty"),
					new Object[] { departStageTrack.getName() }));
			return false;
		}
		// does the staging track have the right number of locomotives?
		if (reqNumEngines > 0 && reqNumEngines != departStageTrack.getNumberEngines()) {
			addLine(buildReport, THREE, MessageFormat.format(Bundle.getMessage("buildStagingNotEngines"),
					new Object[] { departStageTrack.getName() }));
			return false;
		}
		// is the staging track direction correct for this train?
		if ((departStageTrack.getTrainDirections() & train.getTrainDepartsRouteLocation().getTrainDirection()) == 0) {
			addLine(buildReport, THREE, MessageFormat.format(Bundle.getMessage("buildStagingNotDirection"),
					new Object[] { departStageTrack.getName() }));
			return false;
		}
		// does this staging track service this train?
		if (!departStageTrack.acceptsPickupTrain(train)) {
			addLine(buildReport, THREE, MessageFormat.format(Bundle.getMessage("buildStagingNotTrain"),
					new Object[] { departStageTrack.getName() }));
			return false;
		}
		if (departStageTrack.getNumberEngines() > 0) {
			List<String> engs = engineManager.getList();
			for (int i = 0; i < engs.size(); i++) {
				Engine eng = engineManager.getById(engs.get(i));
				if (eng.getTrack() == departStageTrack) {
					// has engine been assigned to another train?
					if (eng.getRouteLocation() != null) {
						addLine(buildReport, THREE, MessageFormat.format(Bundle
								.getMessage("buildStagingDepart"), new Object[] { departStageTrack.getName(),
								eng.getTrainName() }));
						return false;
					}
					// does the train accept the engine type from the staging track?
					if (!train.acceptsTypeName(eng.getType())) {
						addLine(buildReport, THREE, MessageFormat.format(Bundle
								.getMessage("buildStagingDepartEngineType"), new Object[] {
								departStageTrack.getName(), eng.toString(), eng.getType(), train.getName() }));
						return false;
					}
					// does the train accept the engine model from the staging track?
					if (!train.getEngineModel().equals("") && !train.getEngineModel().equals(eng.getModel())) {
						addLine(buildReport, THREE, MessageFormat
								.format(Bundle.getMessage("buildStagingDepartEngineModel"), new Object[] {
										departStageTrack.getName(), eng.toString(), eng.getModel(),
										train.getName() }));
						return false;
					}
					// does the engine road match the train requirements?
					if (!train.getRoadOption().equals(Train.ALLLOADS) && !train.getEngineRoad().equals("")
							&& !train.getEngineRoad().equals(eng.getRoad())) {
						addLine(buildReport, THREE, MessageFormat.format(Bundle
								.getMessage("buildStagingDepartEngineRoad"), new Object[] {
								departStageTrack.getName(), eng.toString(), eng.getRoad(), train.getName() }));
						return false;
					}
					// does the train accept the engine road from the staging track?
					if (train.getEngineRoad().equals("") && !train.acceptsRoadName(eng.getRoad())) {
						addLine(buildReport, THREE, MessageFormat.format(Bundle
								.getMessage("buildStagingDepartEngineRoad"), new Object[] {
								departStageTrack.getName(), eng.toString(), eng.getRoad(), train.getName() }));
						return false;
					}
					// does the train accept the engine owner from the staging track?
					if (!train.acceptsOwnerName(eng.getOwner())) {
						addLine(buildReport, THREE, MessageFormat
								.format(Bundle.getMessage("buildStagingDepartEngineOwner"), new Object[] {
										departStageTrack.getName(), eng.toString(), eng.getOwner(),
										train.getName() }));
						return false;
					}
					// does the train accept the engine built date from the staging track?
					if (!train.acceptsBuiltDate(eng.getBuilt())) {
						addLine(buildReport, THREE, MessageFormat
								.format(Bundle.getMessage("buildStagingDepartEngineBuilt"), new Object[] {
										departStageTrack.getName(), eng.toString(), eng.getBuilt(),
										train.getName() }));
						return false;
					}
				}
			}
		}
		boolean foundCaboose = false;
		boolean foundFRED = false;
		if (departStageTrack.getNumberCars() > 0) {
			List<String> cars = carManager.getList();
			for (int i = 0; i < cars.size(); i++) {
				Car car = carManager.getById(cars.get(i));
				if (car.getTrack() == departStageTrack) {
					// ignore non-lead cars in kernels
					if (car.getKernel() != null && !car.getKernel().isLead(car)) {
						continue; // ignore non-lead cars
					}
					// has car been assigned to another train?
					if (car.getRouteLocation() != null) {
						log.debug("Car " + car.toString() + " has route location "
								+ car.getRouteLocation().getName());
						addLine(buildReport, THREE, MessageFormat.format(Bundle
								.getMessage("buildStagingDepart"), new Object[] { departStageTrack.getName(),
								car.getTrainName() }));
						return false;
					}
					// does the train accept the car type from the staging track?
					if (!train.acceptsTypeName(car.getType())) {
						addLine(buildReport, THREE, MessageFormat.format(Bundle
								.getMessage("buildStagingDepartCarType"), new Object[] {
								departStageTrack.getName(), car.toString(), car.getType(), train.getName() }));
						return false;
					}
					// does the train accept the car road from the staging track?
					if (!train.acceptsRoadName(car.getRoad())) {
						addLine(buildReport, THREE, MessageFormat.format(Bundle
								.getMessage("buildStagingDepartCarRoad"), new Object[] {
								departStageTrack.getName(), car.toString(), car.getRoad(), train.getName() }));
						return false;
					}
					// does the train accept the car load from the staging track?
					if (!car.isCaboose()
							&& !car.isPassenger()
							&& (!car.getLoad().equals(CarLoads.instance().getDefaultEmptyName()) || !departStageTrack
									.isAddLoadsEnabled()
									&& !departStageTrack.isAddLoadsAnySpurEnabled()
									&& !departStageTrack.isAddCustomLoadsAnyStagingTrackEnabled())
							&& !train.acceptsLoad(car.getLoad(), car.getType())) {
						addLine(buildReport, THREE, MessageFormat.format(Bundle
								.getMessage("buildStagingDepartCarLoad"), new Object[] {
								departStageTrack.getName(), car.toString(), car.getLoad(), train.getName() }));
						return false;
					}
					// does the train accept the car owner from the staging track?
					if (!train.acceptsOwnerName(car.getOwner())) {
						addLine(buildReport, THREE, MessageFormat
								.format(Bundle.getMessage("buildStagingDepartCarOwner"), new Object[] {
										departStageTrack.getName(), car.toString(), car.getOwner(),
										train.getName() }));
						return false;
					}
					// does the train accept the car built date from the staging track?
					if (!train.acceptsBuiltDate(car.getBuilt())) {
						addLine(buildReport, THREE, MessageFormat
								.format(Bundle.getMessage("buildStagingDepartCarBuilt"), new Object[] {
										departStageTrack.getName(), car.toString(), car.getBuilt(),
										train.getName() }));
						return false;
					}
					// does the car have a destination serviced by this train?
					if (car.getDestination() != null) {
						log.debug("Car (" + car.toString() + ") has a destination ("
								+ car.getDestinationName() + ", " + car.getDestinationTrackName() + ")");
						if (!train.services(car)) {
							addLine(buildReport, THREE, MessageFormat.format(Bundle
									.getMessage("buildStagingDepartCarDestination"), new Object[] {
									departStageTrack.getName(), car.toString(), car.getDestinationName(),
									train.getName() }));
							return false;
						}
					}
					// is this car a caboose with the correct road for this train?
					if (car.isCaboose()
							&& (train.getCabooseRoad().equals("") || train.getCabooseRoad().equals(
									car.getRoad())))
						foundCaboose = true;
					// is this car have a FRED with the correct road for this train?
					if (car.hasFred()
							&& (train.getCabooseRoad().equals("") || train.getCabooseRoad().equals(
									car.getRoad())))
						foundFRED = true;
				}
			}
		}
		// does the train require a caboose and did we find one from staging?
		if ((train.getRequirements() & Train.CABOOSE) == Train.CABOOSE && !foundCaboose) {
			addLine(buildReport, THREE, MessageFormat.format(Bundle.getMessage("buildStagingNoCaboose"),
					new Object[] { departStageTrack.getName(), train.getCabooseRoad() }));
			return false;
		}
		// does the train require a car with FRED and did we find one from staging?
		if ((train.getRequirements() & Train.FRED) == Train.FRED && !foundFRED) {
			addLine(buildReport, THREE, MessageFormat.format(Bundle.getMessage("buildStagingNoCarFRED"),
					new Object[] { departStageTrack.getName(), train.getCabooseRoad() }));
			return false;
		}
		addLine(buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildTrainCanDepartTrack"),
				new Object[] { train.getName(), departStageTrack.getName() }));
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
			addLine(buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildStagingTrackOccupied"),
					new Object[] { terminateStageTrack.getName(), terminateStageTrack.getNumberEngines(),
							terminateStageTrack.getNumberCars() }));
			return false;
		}
		if (terminateStageTrack.getDropRS() != 0) {
			addLine(buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildStagingTrackReserved"),
					new Object[] { terminateStageTrack.getName(), terminateStageTrack.getDropRS() }));
			return false;
		}
		if (terminateStageTrack.getPickupRS() > 0) {
			addLine(buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildStagingTrackDepart"),
					new Object[] { terminateStageTrack.getName() }));
		}
		if (!terminateStageTrack.acceptsDropTrain(train)) {
			addLine(buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildStagingNotTrain"),
					new Object[] { terminateStageTrack.getName() }));
			return false;
		// if track is setup to accept a specific train or route, then ignore other track restrictions
		} else if (terminateStageTrack.getDropOption().equals(Track.TRAINS)
				|| terminateStageTrack.getDropOption().equals(Track.ROUTES)) {
			addLine(buildReport, SEVEN, MessageFormat.format(
					Bundle.getMessage("buildTrainCanTerminateTrack"), new Object[] { train.getName(),
							terminateStageTrack.getName() }));
			return true; // train can drop to this track, ignore other track restrictions
		}
		if (!Setup.isTrainIntoStagingCheckEnabled()) {
			addLine(buildReport, SEVEN, MessageFormat.format(
					Bundle.getMessage("buildTrainCanTerminateTrack"), new Object[] { train.getName(),
							terminateStageTrack.getName() }));
			return true;
		}
		if (!checkTerminateStagingTrackRestrications(terminateStageTrack)) {
			addLine(buildReport, SEVEN, Bundle.getMessage("buildOptionRestrictStaging"));
			return false;
		}
		return true;
	}

	private boolean checkTerminateStagingTrackRestrications(Track terminateStageTrack) {
		// check go see if location/track will accept the train's car and engine types
		String[] types = train.getTypeNames();
		for (int i = 0; i < types.length; i++) {
			if (!terminateLocation.acceptsTypeName(types[i])) {
				addLine(buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildDestinationType"),
						new Object[] { terminateLocation.getName(), types[i] }));
				return false;
			}
			if (!terminateStageTrack.acceptsTypeName(types[i])) {
				addLine(buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildStagingTrackType"),
						new Object[] { terminateStageTrack.getName(), types[i] }));
				return false;
			}
		}
		// check go see if track will accept the train's car and engine roads
		if (train.getRoadOption().equals(Train.ALLROADS)
				&& !terminateStageTrack.getRoadOption().equals(Track.ALLROADS)) {
			addLine(buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildStagingTrackAllRoads"),
					new Object[] { terminateStageTrack.getName() }));
			return false;

		} else if (train.getRoadOption().equals(Train.INCLUDEROADS)) {
			String[] roads = train.getRoadNames();
			for (int i = 0; i < roads.length; i++) {
				if (!terminateStageTrack.acceptsRoadName(roads[i])) {
					addLine(buildReport, FIVE, MessageFormat.format(Bundle
							.getMessage("buildStagingTrackRoad"), new Object[] {
							terminateStageTrack.getName(), roads[i] }));
					return false;
				}
			}
		} else if (train.getRoadOption().equals(Train.EXCLUDEROADS)) {
			String[] excludeRoads = train.getRoadNames();
			String[] allroads = CarRoads.instance().getNames();
			List<String> roads = new ArrayList<String>();
			for (int i = 0; i < allroads.length; i++) {
				roads.add(allroads[i]);
			}
			for (int i = 0; i < excludeRoads.length; i++) {
				roads.remove(excludeRoads[i]);
			}
			for (int i = 0; i < roads.size(); i++) {
				if (!terminateStageTrack.acceptsRoadName(roads.get(i))) {
					addLine(buildReport, FIVE, MessageFormat.format(Bundle
							.getMessage("buildStagingTrackRoad"), new Object[] {
							terminateStageTrack.getName(), roads.get(i) }));
					return false;
				}
			}
		}
		// check go see if track will accept the train's car loads
		if (train.getLoadOption().equals(Train.ALLLOADS)
				&& !terminateStageTrack.getLoadOption().equals(Track.ALLLOADS)) {
			addLine(buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildStagingTrackAllLoads"),
					new Object[] { terminateStageTrack.getName() }));
			return false;

		} else if (train.getLoadOption().equals(Train.INCLUDELOADS)) {
			String[] loads = train.getLoadNames();
			for (int i = 0; i < loads.length; i++) {
				String loadParts[] = loads[i].split(CarLoad.SPLIT_CHAR); // split load name
				if (loadParts.length > 1) {
					if (!terminateStageTrack.acceptsLoad(loadParts[1], loadParts[0])) {
						addLine(buildReport, FIVE, MessageFormat.format(Bundle
								.getMessage("buildStagingTrackLoad"), new Object[] {
								terminateStageTrack.getName(), loads[i] }));
						return false;
					}
				} else {
					if (!terminateStageTrack.acceptsLoadName(loads[i])) {
						addLine(buildReport, FIVE, MessageFormat.format(Bundle
								.getMessage("buildStagingTrackLoad"), new Object[] {
								terminateStageTrack.getName(), loads[i] }));
						return false;
					}
				}
			}
		} else if (train.getLoadOption().equals(Train.EXCLUDELOADS)) {
			// build a list of loads that the staging track must accept
			List<String> loads = new ArrayList<String>();
			for (int i = 0; i < types.length; i++) {
				List<String> allLoads = CarLoads.instance().getNames(types[i]);
				for (int j = 0; j < allLoads.size(); j++) {
					if (!loads.contains(allLoads.get(j)))
						loads.add(allLoads.get(j));
				}
			}
			// remove the loads that the train won't carry
			String[] excludeLoads = train.getLoadNames();
			for (int i = 0; i < excludeLoads.length; i++) {
				loads.remove(excludeLoads[i]);
			}
			for (int i = 0; i < loads.size(); i++) {
				String loadParts[] = loads.get(i).split(CarLoad.SPLIT_CHAR); // split load name
				if (loadParts.length > 1) {
					if (!terminateStageTrack.acceptsLoad(loadParts[1], loadParts[0])) {
						addLine(buildReport, FIVE, MessageFormat.format(Bundle
								.getMessage("buildStagingTrackLoad"), new Object[] {
								terminateStageTrack.getName(), loads.get(i) }));
						return false;
					}
				} else {
					if (!terminateStageTrack.acceptsLoadName(loads.get(i))) {
						addLine(buildReport, FIVE, MessageFormat.format(Bundle
								.getMessage("buildStagingTrackLoad"), new Object[] {
								terminateStageTrack.getName(), loads.get(i) }));
						return false;
					}
				}
			}
		}
		addLine(buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildTrainCanTerminateTrack"),
				new Object[] { train.getName(), terminateStageTrack.getName() }));
		return true;
	}

	/**
	 * Find the final destination and track for a car with a custom load. Car doesn't have a destination or final
	 * destination. There's a check to see if there's a spur/ schedule for this car. Returns true if a schedule was
	 * found.
	 * 
	 * @param car
	 *            the car with the load
	 * @return true if there's a schedule that can be route to for this car and load
	 * @throws BuildFailedException
	 */
	private boolean findFinalDestinationForCarLoad(Car car) throws BuildFailedException {
		boolean routeToSpurFound = false;
		if (car.getLoad().equals(CarLoads.instance().getDefaultEmptyName())
				|| car.getLoad().equals(CarLoads.instance().getDefaultLoadName())
				|| car.getDestination() != null || car.getFinalDestination() != null)
			return routeToSpurFound; // no schedule found for this car
		addLine(buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildSearchForSpur"),
				new Object[] { car.toString(), car.getLoad(),
						car.getLocationName() + ", " + car.getTrackName() }));
		List<Track> tracks = locationManager.getTracks(Track.SPUR);
		log.debug("Found " + tracks.size() + " spurs");
		for (int i = 0; i < tracks.size(); i++) {
			Track track = tracks.get(i);
			if (car.getTrack() == track || track.getSchedule() == null)
				continue;
			if (!train.isAllowLocalMovesEnabled()
					&& splitString(car.getLocationName()).equals(splitString(track.getLocation().getName()))) {
				log.debug("Skipping track (" + track.getName() + "), it would require a local move"); // NOI18N
				continue;
			}
			if (!train.isAllowThroughCarsEnabled()
					&& !train.isLocalSwitcher()
					&& !car.isCaboose()
					&& !car.hasFred()
					&& !car.isPassenger()
					&& splitString(car.getLocationName()).equals(splitString(departLocation.getName()))
					&& splitString(track.getLocation().getName()).equals(splitString(terminateLocation.getName()))
					&& !splitString(departLocation.getName()).equals(splitString(terminateLocation.getName()))) {
				log.debug("Skipping track (" + track.getName() + "), through cars not allowed to terminal (" // NOI18N
						+ terminateLocation.getName() + ")");
				continue;
			}
			String status = car.testDestination(track.getLocation(), track);
			if (!status.equals(Track.OKAY)) {
				// if the track has an alternate track don't abort if the issue was space
				if (!status.startsWith(Track.LENGTH) || track.getAlternativeTrack() == null
						|| !track.checkSchedule(car).equals(Track.OKAY))
					continue;
			}
			
			addLine(buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildSetFinalDestination"),
					new Object[] { car.toString(), car.getLoad(), track.getLocation().getName(),
							track.getName() }));

			// check the number of in bound cars to this track
			if (!track.isSpaceAvailable(car)) {
				// Now determine if we should move the car or just leave it where it is
				String id = track.getScheduleItemId();	// save the tracks schedule item id
				// determine if this car can be routed to the spur
				car.setFinalDestination(track.getLocation());
				car.setFinalDestinationTrack(track);
				if (Router.instance().setDestination(car, train, buildReport))
					routeToSpurFound = true; // if we don't find another spur, keep the car here for now
				car.setDestination(null, null); 
				car.setFinalDestination(null);
				car.setFinalDestinationTrack(null);
				track.setScheduleItemId(id);	// restore id
				addLine(buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildNoDestTrackSpace"),
						new Object[] { car.toString(), track.getLocation().getName(), track.getName(),
								track.getNumberOfCarsInRoute(), track.getReservedInRoute(),
								track.getReservationFactor() }));
				continue;
			}
			// try to send car to this spur
			car.setFinalDestination(track.getLocation());
			car.setFinalDestinationTrack(track);
			// test to see if destination is reachable by this train
			if (Router.instance().setDestination(car, train, buildReport))
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
			addLine(buildReport, SEVEN, MessageFormat.format(Bundle
					.getMessage("buildNotAbleToSetDestination"), new Object[] { car.toString(),
					Router.instance().getStatus() }));
			car.setFinalDestination(null);
			car.setFinalDestinationTrack(null);
		}
		addLine(buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildCouldNotFindSpur"),
				new Object[] { car.toString(), car.getLoad() }));
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
		if (car.getTrack() == null || !car.getTrack().getLocType().equals(Track.STAGING)
				|| !car.getTrack().isAddLoadsAnySpurEnabled()
				|| !car.getLoad().equals(CarLoads.instance().getDefaultEmptyName())
				|| car.getDestination() != null || car.getFinalDestination() != null) {
			log.debug("No load search for car (" + car.toString() + ") isAddLoadsAnySpurEnabled: " // NOI18N
					+ (car.getTrack().isAddLoadsAnySpurEnabled() ? "true" : "false") // NOI18N
					+ ", car load: (" + car.getLoad() + ")"); // NOI18N
			return false; // no load generated for this car
		}
		addLine(buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildSearchTrackNewLoad"),
				new Object[] { car.toString(), car.getType(), car.getLoad(), car.getTrackName() }));
		List<Track> tracks = locationManager.getTracks(Track.SPUR);
		log.debug("Found " + tracks.size() + " spurs");
		for (int i = 0; i < tracks.size(); i++) {
			Track track = tracks.get(i);
			ScheduleItem si = getScheduleItem(car, track);
			if (si == null)
				continue; // no match
			// need to set car load so testDestination will work properly
			String oldCarLoad = car.getLoad();
			car.setLoad(si.getLoad());
			String status = car.testDestination(track.getLocation(), track);
			if (!status.equals(Track.OKAY) && !status.startsWith(Track.LENGTH)) {
				addLine(buildReport, SEVEN, MessageFormat.format(
						Bundle.getMessage("buildNoDestTrackNewLoad"), new Object[] {
								track.getLocation().getName(), track.getName(), car.toString(), si.getLoad(),
								status }));
				// restore car's load
				car.setLoad(oldCarLoad);
				continue;
			}
			addLine(buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildTrySpurLoad"),
					new Object[] { track.getLocation().getName(), track.getName(), car.getLoad() }));
			if (!track.isSpaceAvailable(car)) {
				addLine(buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildNoDestTrackSpace"),
						new Object[] { car.toString(), track.getLocation().getName(), track.getName(),
								track.getNumberOfCarsInRoute(), track.getReservedInRoute(),
								track.getReservationFactor() }));
				// restore car's load
				car.setLoad(oldCarLoad);
				continue;
			}
			car.setFinalDestination(track.getLocation());
			car.setFinalDestinationTrack(track);
			// try routing car
			if (Router.instance().setDestination(car, train, buildReport) && car.getDestination() != null) {
				// return car with this custom load and destination
				addLine(buildReport, FIVE, MessageFormat.format(
						Bundle.getMessage("buildCreateNewLoadForCar"), new Object[] { car.toString(),
								si.getLoad(), track.getLocation().getName(), track.getName() }));
				car.setScheduleId(track.getCurrentScheduleItem().getId());
				car.setLoadGeneratedFromStaging(true);
				// is car part of kernel?
				car.updateKernel();
				track.bumpSchedule();
				return true; // done, car now has a custom load
			}
			addLine(buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildCanNotRouteCar"),
					new Object[] { car.toString(), si.getLoad(), track.getLocation().getName(),
							track.getName() }));
			car.setDestination(null, null);
			// restore load and final destination and track
			car.setLoad(oldCarLoad);
			car.setFinalDestination(null);
			car.setFinalDestinationTrack(null);
		}
		addLine(buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildUnableNewLoad"),
				new Object[] { car.toString() }));
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
		if (car.getTrack() == null || !car.getTrack().getLocType().equals(Track.STAGING)
				|| !car.getTrack().isAddCustomLoadsAnyStagingTrackEnabled()
				|| !car.getLoad().equals(CarLoads.instance().getDefaultEmptyName())
				|| car.getDestination() != null || car.getFinalDestination() != null) {
			log.debug("No load search for car (" + car.toString()
					+ ") isAddCustomLoadsAnyStagingTrackEnabled: " // NOI18N
					+ (car.getTrack().isAddLoadsAnySpurEnabled() ? "true" : "false") // NOI18N
					+ ", car load: (" + car.getLoad() + ")"); // NOI18N
			return false;
		}
		// first try this train's termination track if one exists
		if (train.isAllowThroughCarsEnabled()
				&& generateLoadCarDepartingAndTerminatingIntoStaging(car, terminateStageTrack))
			return true;
		List<Track> tracks = locationManager.getTracks(Track.STAGING);
		log.debug("Found " + tracks.size() + " staging tracks for load generation");
		for (int i = 0; i < tracks.size(); i++) {
			Track track = tracks.get(i);
			log.debug("Staging track (" + track.getLocation().getName() + ", " + track.getName() + ")");
			// find a staging track that isn't at the departure or terminal
			if (track.getLocation() == departLocation || track.getLocation() == terminateLocation)
				continue;
			// the following method sets the load generated from staging boolean
			if (generateLoadCarDepartingAndTerminatingIntoStaging(car, track)) {
				// try sending car to this destination, but not staging track
				car.setFinalDestination(track.getLocation());
				car.setFinalDestinationTrack(null);
				// test to see if destination is reachable by this train
				if (Router.instance().setDestination(car, train, buildReport) && car.getDestination() != null) {
					return true; // done, car has a custom load and a destination
				}
				addLine(buildReport, SEVEN, MessageFormat.format(Bundle
						.getMessage("buildStagingTrackNotReachable"), new Object[] {
						track.getLocation().getName(), track.getName(), car.getLoad() }));
				// return car to original state
				car.setLoad(CarLoads.instance().getDefaultEmptyName());
				car.setLoadGeneratedFromStaging(false);
				car.setFinalDestination(null);
				car.updateKernel();
			}
		}
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
				throw new BuildFailedException(MessageFormat.format(Bundle
						.getMessage("buildErrorNoScheduleItem"), new Object[] { track.getScheduleItemId(),
						track.getScheduleName(), track.getName(), track.getLocation().getName() }));
			return checkScheduleItem(si, car, track);
		}
		log.debug("Track (" + track.getName() + ") in match mode");
		for (int i = 0; i < track.getSchedule().getSize(); i++) {
			si = track.getNextScheduleItem();
			if (si == null)
				throw new BuildFailedException(MessageFormat.format(Bundle
						.getMessage("buildErrorNoScheduleItem"), new Object[] { track.getScheduleItemId(),
						track.getScheduleName(), track.getName(), track.getLocation().getName() }));
			si = checkScheduleItem(si, car, track);
			if (si != null)
				return si;
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
		if (!car.getType().equals(si.getType()) || si.getLoad().equals("")
				|| si.getLoad().equals(CarLoads.instance().getDefaultEmptyName())
				|| si.getLoad().equals(CarLoads.instance().getDefaultLoadName())) {
			log.debug("Not using track (" + track.getName() + ") schedule request type (" + si.getType()
					+ ") road (" + si.getRoad() + ") load (" + si.getLoad() + ")"); // NOI18N
			return null;
		}
		if (!si.getRoad().equals("") && !car.getRoad().equals(si.getRoad())) {
			log.debug("Not using track (" + track.getName() + ") schedule request type (" + si.getType()
					+ ") road (" + si.getRoad() + ") load (" + si.getLoad() + ")"); // NOI18N
			return null;
		}
		if (!train.acceptsLoad(si.getLoad(), si.getType())) {
			addLine(buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildTrainNotNewLoad"),
					new Object[] { train.getName(), si.getLoad(), track.getLocation().getName(),
							track.getName() }));
			return null;
		}
		// does the departure track allow this load?
		if (!car.getTrack().shipsLoad(si.getLoad(), car.getType())) {
			addLine(buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildTrackNotNewLoad"),
					new Object[] { car.getTrackName(), si.getLoad(), track.getLocation().getName(),
							track.getName() }));
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
			addLine(buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildScheduleNotActive"),
					new Object[] { track.getName(), si.getId(), tName, aName }));

			return null;
		}
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
		log.debug("Get " + car.getTrack().getServiceOrder() + " car (" + car.toString() + ") from "
				+ car.getTrack().getLocType() + " (" + car.getTrackName() + "), order: " // NOI18N
				+ car.getOrder());
		Car bestCar = car;
		for (int i = carIndex + 1; i < carList.size(); i++) {
			Car testCar = carManager.getById(carList.get(i));
			if (testCar.getTrack() == car.getTrack()) {
				log.debug(car.getTrack().getLocType() + " car (" + testCar.toString() + ") has order: "
						+ testCar.getOrder()); // NOI18N
				if (car.getTrack().getServiceOrder().equals(Track.FIFO)
						&& bestCar.getOrder() > testCar.getOrder()
						&& bestCar.getLoadPriority().equals(testCar.getLoadPriority()))
					bestCar = testCar;
				if (car.getTrack().getServiceOrder().equals(Track.LIFO)
						&& bestCar.getOrder() < testCar.getOrder()
						&& bestCar.getLoadPriority().equals(testCar.getLoadPriority()))
					bestCar = testCar;
			}
		}
		if (car != bestCar)
			addLine(buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildTrackModeCarPriority"),
					new Object[] { car.getTrack().getLocType(), car.getTrack().getName(),
							car.getTrack().getServiceOrder(), bestCar.toString(), car.toString() }));
		return bestCar;
	}

	private boolean checkCarForDestinationAndTrack(Car car, RouteLocation rl, RouteLocation rld)
			throws BuildFailedException {
		int index;
		for (index = 0; index < routeList.size(); index++) {
			if (rld == train.getRoute().getLocationById(routeList.get(index)))
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
		addLine(buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildCarHasAssignedDest"),
				new Object[] { car.toString(),
						(car.getDestinationName() + ", " + car.getDestinationTrackName()) }));
		RouteLocation rld = train.getRoute().getLastLocationByName(car.getDestinationName());
		if (rld == null) {
			// car has a destination that isn't serviced by this train (destination loaded by router)
			addLine(buildReport, FIVE, MessageFormat.format(Bundle
					.getMessage("buildExcludeCarDestNotPartRoute"), new Object[] { car.toString(),
					car.getDestinationName(), train.getRoute().getName() }));
			return true; // done
		}
		if (car.getRouteLocation() != null) {
			// The following code should not be executed, this should not occur if train was reset before a build!
			addLine(buildReport, THREE, MessageFormat.format(Bundle.getMessage("buildCarAlreadyAssigned"),
					new Object[] { car.toString() }));
		}
		// now go through the route and try and find a location with
		// the correct destination name
		int locCount = 0;
		for (int k = routeIndex; k < routeList.size(); k++) {
			rld = train.getRoute().getLocationById(routeList.get(k));
			// if car can be picked up later at same location, skip
			if (checkForLaterPickUp(rl, rld, car)) {
				addLine(buildReport, THREE, MessageFormat.format(Bundle.getMessage("buildCarHasSecond"),
						new Object[] { car.toString(), car.getLocationName() }));
				break;
			}
			if (!rld.getName().equals(car.getDestinationName()))
				continue;
			// is the car's destination the terminal and is that allowed?
			if (!train.isAllowThroughCarsEnabled()
					&& k == routeList.size()-1
					&& !train.isLocalSwitcher() 
					&& !car.isCaboose()
					&& !car.hasFred() 
					&& !car.isPassenger()
					&& splitString(car.getLocationName()).equals(splitString(departLocation.getName()))
					&& splitString(car.getDestinationName()).equals(splitString(terminateLocation.getName()))) {
				addLine(buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildCarHasDestination"),
						new Object[] { car.toString(), departLocation.getName(), terminateLocation.getName() }));
				addLine(buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildThroughTrafficNotAllow"),
						new Object[] { departLocation.getName(), terminateLocation.getName() }));
				addLine(buildReport, SEVEN, BLANK_LINE); // add line when in very detailed report mode
				return true; // done
			}
			locCount++; // show when this car would be dropped at location
			log.debug("Car (" + car.toString() + ") found a destination in train's route");
			// are drops allows at this location?
			if (!rld.canDrop()) {
				addLine(buildReport, THREE, MessageFormat.format(Bundle.getMessage("buildRouteNoDropsStop"),
						new Object[] { train.getRoute().getName(), rld.getName(), locCount }));
				continue;
			}
			if (rld.getCarMoves() >= rld.getMaxCarMoves()) {
				addLine(buildReport, THREE, MessageFormat.format(Bundle
						.getMessage("buildNoAvailableMovesStop"), new Object[] { rld.getName(), locCount }));
				continue;
			}
			// is the train length okay?
			if (!checkTrainLength(car, rl, rld)) {
				continue;
			}
			// check for valid destination track
			if (car.getDestinationTrack() == null) {
				addLine(buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildCarDoesNotHaveDest"),
						new Object[] { car.toString() }));
				// is there a destination track assigned for staging cars?
				if (rld == train.getTrainTerminatesRouteLocation() && terminateStageTrack != null) {
					String status = car.testDestination(car.getDestination(), terminateStageTrack);
					if (status.equals(Track.OKAY)) {
						addLine(buildReport, THREE, MessageFormat.format(Bundle
								.getMessage("buildCarAssignedToStaging"), new Object[] { car.toString(),
								terminateStageTrack.getName() }));
						addCarToTrain(car, rl, rld, terminateStageTrack);
						return true;
					} else {
						addLine(buildReport, SEVEN, MessageFormat.format(Bundle
								.getMessage("buildCanNotDropCarBecause"), new Object[] { car.toString(),
								terminateStageTrack.getName(), status }));
						continue;
					}
					// no staging at this location, now find a destination track this this car
				} else {
					List<String> tracks = car.getDestination().getTrackIdsByMovesList(null);
					addLine(buildReport, SEVEN, MessageFormat.format(
							Bundle.getMessage("buildSearchForTrack"), new Object[] { tracks.size(),
									car.getDestinationName(), car.toString(), car.getLoad() }));
					for (int s = 0; s < tracks.size(); s++) {
						Track testTrack = car.getDestination().getTrackById(tracks.get(s));
						// log.debug("track (" +testTrack.getName()+ ") has "+ testTrack.getMoves() + " moves");
						// dropping to the same track isn't allowed
						if (testTrack == car.getTrack()) {
							addLine(buildReport, SEVEN, MessageFormat.format(Bundle
									.getMessage("buildCanNotDropCarSameTrack"), new Object[] {
									car.toString(), testTrack.getName() }));
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
								&& testTrack.getLocType().equals(Track.SPUR)
								&& testTrack.getAlternativeTrack() != null) {
							addLine(buildReport, SEVEN, MessageFormat.format(Bundle
									.getMessage("buildTrackHasAlternate"), new Object[] {
									testTrack.getName(), testTrack.getAlternativeTrack().getName() }));
							String altStatus = car.testDestination(car.getDestination(), testTrack
									.getAlternativeTrack());
							if (altStatus.equals(Track.OKAY)
							// "CUSTOM" and "LOAD" are in the status message, must use contains
									|| (altStatus.contains(Track.CUSTOM) && altStatus.contains(Track.LOAD))) {
								addLine(buildReport, SEVEN, MessageFormat.format(Bundle
										.getMessage("buildUseAlternateTrack"), new Object[] { car.toString(),
										testTrack.getAlternativeTrack().getName() }));
								addCarToTrain(car, rl, rld, testTrack.getAlternativeTrack());
								// and forward the car to the original destination
								car.setFinalDestination(car.getDestination());
								car.setFinalDestinationTrack(testTrack);
								car.setNextLoad(car.getLoad());
								testTrack.setMoves(testTrack.getMoves() + 1); // bump the number of moves
								return true;
							} else {
								addLine(buildReport, SEVEN, MessageFormat.format(Bundle
										.getMessage("buildCanNotDropCarBecause"),
										new Object[] { car.toString(),
												testTrack.getAlternativeTrack().getName(), altStatus }));
							}
						}
						if (!status.equals(Track.OKAY)) {
							addLine(buildReport, SEVEN, MessageFormat.format(Bundle
									.getMessage("buildCanNotDropCarBecause"), new Object[] { car.toString(),
									testTrack.getName(), status }));
							continue;
						}
						addCarToTrain(car, rl, rld, testTrack);
						return true;
					}
				}
				// car has a destination track
			} else {
				log.debug("Car (" + car.toString() + ") has a destination track ("
						+ car.getDestinationTrack().getName() + ")");
				// going into the correct staging track?
				if (!rld.equals(train.getTrainTerminatesRouteLocation()) || terminateStageTrack == null
						|| terminateStageTrack == car.getDestinationTrack()) {
					// is train direction correct?
					if (checkDropTrainDirection(car, rld, car.getDestinationTrack())) {
						// drop to interchange or spur?
						if (checkTrainCanDrop(car, car.getDestinationTrack())) {
							String status = car.testDestination(car.getDestination(), car
									.getDestinationTrack());
							if (status.equals(Track.OKAY)
									&& checkDropTrainDirection(car, rld, car.getDestinationTrack())) {
								addCarToTrain(car, rl, rld, car.getDestinationTrack());
								return true;
							} else if (!status.equals(Track.OKAY)) {
								addLine(buildReport, SEVEN, MessageFormat.format(Bundle
										.getMessage("buildCanNotDropCarBecause"), new Object[] {
										car.toString(), car.getDestinationTrackName(), status }));
							}
						}
					}
				} else {
					throw new BuildFailedException(MessageFormat.format(Bundle
							.getMessage("buildCarDestinationStaging"), new Object[] { car.toString(),
							car.getDestinationName(), car.getDestinationTrackName() }));
				}
			}
			addLine(buildReport, THREE, MessageFormat.format(Bundle.getMessage("buildCanNotDropCar"),
					new Object[] { car.toString(), car.getDestinationName(), locCount }));
			if (car.getDestinationTrack() == null) {
				log.debug("Could not find a destination track for location " + car.getDestinationName());
			}
		}
		log.debug("car (" + car.toString() + ") not added to train");
		// remove destination and revert to final destination
		if (car.getDestinationTrack() != null) {
			Track destTrack = car.getDestinationTrack();
			// TODO should we leave the car's destination? The spur expects this car!
			if (destTrack.getSchedule() != null && destTrack.getScheduleMode() == Track.SEQUENTIAL) {
				// log.debug("Scheduled delivery to ("+destTrack.getName()+") cancelled");
				addLine(buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildPickupCancelled"),
						new Object[] { destTrack.getLocation().getName(), destTrack.getName() }));
			}
		}
		car.setFinalDestination(car.getPreviousFinalDestination());
		car.setFinalDestinationTrack(car.getPreviousFinalDestinationTrack());
		car.setDestination(null, null);
		// is car sitting on a FIFO or LIFO track?
		if (car.getTrack() != null && !car.getTrack().getServiceOrder().equals(Track.NORMAL)) {
			addLine(buildReport, SEVEN, MessageFormat
					.format(Bundle.getMessage("buildBypassCarServiceOrder"), new Object[] { car.toString(),
							car.getTrack().getName(), car.getTrack().getServiceOrder() }));
			// move car id in front of current pointer so car is no longer used on this pass
			carList.remove(car.getId());
			carList.add(carIndex, car.getId());
		}
		addLine(buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildNoDestForCar"),
				new Object[] { car.toString() }));
		addLine(buildReport, SEVEN, BLANK_LINE); // add line when in very detailed report mode
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
	private boolean findDestinationAndTrack(Car car, RouteLocation rl, RouteLocation rld)
			throws BuildFailedException {
		int index;
		for (index = 0; index < routeList.size(); index++) {
			if (rld == train.getRoute().getLocationById(routeList.get(index)))
				break;
		}
		return findDestinationAndTrack(car, rl, index - 1, index + 1);
	}

	/**
	 * Find a destination and track for a car.
	 * 
	 * @param car
	 *            The car that is looking for a destination and destination track.
	 * @param rl
	 *            The current route location for this car.
	 * @param routeIndex
	 *            Where in the train's route to begin a search for a destination for this car.
	 * @param routeEnd
	 *            Where to stop looking for a destination.
	 * @return true if successful.
	 * @throws BuildFailedException
	 */
	private boolean findDestinationAndTrack(Car car, RouteLocation rl, int routeIndex, int routeEnd)
			throws BuildFailedException {
		addLine(buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildFindDestinationForCar"),
				new Object[] { car.toString(), car.getType(),
						(car.getLocationName() + ", " + car.getTrackName()) }));
		int start = routeIndex; // start looking after car's current location
		RouteLocation rld = null; // the route location destination being checked for the car
		RouteLocation rldSave = null; // holds the best route location destination for the car
		Track trackSave = null; // holds the best track at destination for the car
		noMoreMoves = true; // false when there are are locations with moves
		boolean multiplePickup = false; // true when car can be picked up from two or more locations in the route

		// more than one location in this route?
		if (routeList.size() > 1)
			start++; // yes!, no car drops at departure
		// all pick ups to terminal?
		if (train.isSendCarsToTerminalEnabled() && routeIndex > 0 && routeEnd == routeList.size()) {
			addLine(buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildSendToTerminal"),
					new Object[] { terminateLocation.getName() }));
			start = routeEnd - 1;
		}
		for (int k = start; k < routeEnd; k++) {
			rld = train.getRoute().getLocationById(routeList.get(k));
			// if car can be picked up later at same location, set flag
			if (checkForLaterPickUp(rl, rld, car)) {
				multiplePickup = true;
			}
			if (rld.canDrop() || car.hasFred() || car.isCaboose()) {
				addLine(buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildSearchingLocation"),
						new Object[] { rld.getName(), }));
			} else {
				addLine(buildReport, SEVEN, MessageFormat.format(Bundle
						.getMessage("buildRouteNoDropLocation"), new Object[] { train.getRoute().getName(),
						rld.getName() }));
				continue;
			}
			// get the destination
			Location testDestination = rld.getLocation();
			if (testDestination == null) {
				// The following should never throw, all locations in the route have been already checked
				throw new BuildFailedException(MessageFormat.format(Bundle.getMessage("buildErrorRouteLoc"),
						new Object[] { train.getRoute().getName(), rld.getName() }));
			}
			

			// any moves left at this location?
			if (rld.getCarMoves() >= rld.getMaxCarMoves()) {
				addLine(buildReport, FIVE, MessageFormat.format(Bundle
						.getMessage("buildNoAvailableMovesDest"), new Object[] { rld.getName() }));
				continue;
			}

			noMoreMoves = false;
			Location destinationTemp = null;
			Track trackTemp = null;
			
			// don't move car to same location unless the route only has one location (local moves) or is passenger,
			// caboose or car with FRED
			if (splitString(rl.getName()).equals(splitString(rld.getName())) && !train.isLocalSwitcher()
					&& !car.isPassenger() && !car.isCaboose() && !car.hasFred()) {
				// allow cars to return to the same staging location if no other options (tracks) are available
				if ((train.isAllowReturnToStagingEnabled()  || Setup.isAllowReturnToStagingEnabled())
						&& testDestination.getLocationOps() == Location.STAGING && trackSave == null) {
					addLine(buildReport, SEVEN, MessageFormat.format(Bundle
							.getMessage("buildReturnCarToStaging"), new Object[] { car.toString(),
							rld.getName() }));
				} else {
					addLine(buildReport, SEVEN, MessageFormat.format(Bundle
							.getMessage("buildCarLocEqualDestination"), new Object[] { car.toString(),
							rld.getName() }));
					continue;
				}
			}
			
			// check to see if departure track has any restrictions
			if (car.getFinalDestination() != null && !car.getTrack().acceptsDestination(testDestination)) {
				addLine(buildReport, SEVEN, MessageFormat.format(Bundle
						.getMessage("buildDestinationNotServiced"), new Object[] { testDestination.getName(),
						car.getTrackName() }));
				continue;
			}

			if (!testDestination.acceptsTypeName(car.getType())) {
				addLine(buildReport, SEVEN, MessageFormat.format(
						Bundle.getMessage("buildCanNotDropLocation"), new Object[] { car.toString(),
								car.getType(), testDestination.getName() }));
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
			if (!train.isAllowThroughCarsEnabled() 
					&& !train.isLocalSwitcher() 
					&& !car.isCaboose()
					&& !car.hasFred() 
					&& !car.isPassenger()
					&& splitString(car.getLocationName()).equals(splitString(departLocation.getName()))
					&& splitString(rld.getName()).equals(splitString(terminateLocation.getName()))) {
				addLine(buildReport, SEVEN, MessageFormat.format(Bundle
						.getMessage("buildThroughTrafficNotAllow"), new Object[] { departLocation.getName(),
						terminateLocation.getName() }));
				continue;
			}
			// is there a track assigned for staging cars?
			if (rld == train.getTrainTerminatesRouteLocation() && terminateStageTrack != null) {
				// no need to check train and track direction into staging, already done
				String status = car.testDestination(testDestination, terminateStageTrack); // will staging accept this
																							// car?
				if (status.equals(Track.OKAY)) {
					trackTemp = terminateStageTrack;
					destinationTemp = testDestination;
					// only generate a new load if there aren't any other tracks available for this car
				} else if (status.startsWith(Track.LOAD)
						&& car.getTrack() == departStageTrack
						&& rldSave == null
						&& !departStageTrack.isAddCustomLoadsAnyStagingTrackEnabled()
						&& (departStageTrack.isAddLoadsEnabled() || departStageTrack
								.isAddLoadsAnySpurEnabled())) {
					// try and generate a load for this car into staging
					if (generateLoadCarDepartingAndTerminatingIntoStaging(car, terminateStageTrack)) {
						trackTemp = terminateStageTrack;
						destinationTemp = testDestination;
					} else {
						addLine(buildReport, SEVEN, MessageFormat.format(Bundle
								.getMessage("buildCanNotDropCarBecause"), new Object[] { car.toString(),
								terminateStageTrack.getName(), status }));
						continue; // failed to create load
					}
				} else {
					addLine(buildReport, SEVEN, MessageFormat.format(Bundle
							.getMessage("buildCanNotDropCarBecause"), new Object[] { car.toString(),
							terminateStageTrack.getName(), status }));
					continue;
				}
				// no staging track assigned, start track search
			} else {
				List<String> tracks = testDestination.getTrackIdsByMovesList(null);
				for (int s = 0; s < tracks.size(); s++) {
					Track testTrack = testDestination.getTrackById(tracks.get(s));
					// log.debug("track (" +testTrack.getName()+ ") has "+ testTrack.getMoves() + " moves");
					// dropping to the same track isn't allowed
					if (testTrack == car.getTrack() && !car.isPassenger() && !car.isCaboose()
							&& !car.hasFred()) {
						addLine(buildReport, SEVEN, MessageFormat.format(Bundle
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
							&& !car.getLoad().equals(CarLoads.instance().getDefaultEmptyName())
							&& !car.getLoad().equals(CarLoads.instance().getDefaultLoadName())) {
						addLine(buildReport, FIVE, MessageFormat.format(Bundle
								.getMessage("buildSpurScheduleLoad"), new Object[] { testTrack.getName(),
								car.getLoad() }));
						// is car part of kernel?
						car.updateKernel();
						addCarToTrain(car, rl, rld, testTrack);
						return true;
					}
					// is the destination a spur with a Schedule?
					// And is car departing a staging track that can generate schedule loads?
					if (!status.equals(Track.OKAY)
							&& (!status.startsWith(Track.TYPE)) // can't generate load for spur that doesn't accept this
																// car type
							&& (!status.startsWith(Track.LENGTH)) // can't generate load for spur that is full
							&& testTrack.getLocType().equals(Track.SPUR)
							&& !testTrack.getScheduleId().equals("")
							&& (car.getTrack().isAddLoadsEnabled() || car.getTrack()
									.isAddLoadsAnySpurEnabled())
							&& car.getLoad().equals(CarLoads.instance().getDefaultEmptyName())) {
						// can we use this track?
						if (!testTrack.isSpaceAvailable(car)) {
							addLine(buildReport, SEVEN, MessageFormat.format(Bundle
									.getMessage("buildNoDestTrackSpace"), new Object[] { car.toString(),
									testTrack.getLocation().getName(), testTrack.getName(),
									testTrack.getNumberOfCarsInRoute(), testTrack.getReservedInRoute(),
									testTrack.getReservationFactor() }));
							continue; // no
						}
						addLine(buildReport, SEVEN, MessageFormat.format(Bundle
								.getMessage("buildGenerateLoad"), new Object[] { car.toString(),
								car.getType(), testDestination.getName(), testTrack.getName() }));
						String carLoad = car.getLoad(); // save the car's load
						ScheduleItem si = getScheduleItem(car, testTrack);
						if (si != null) {
							car.setLoad(si.getLoad());
							status = car.testDestination(testDestination, testTrack);
							if (status.equals(Track.OKAY)) {
								addLine(buildReport, FIVE, MessageFormat.format(Bundle
										.getMessage("buildAddingScheduleLoad"), new Object[] { si.getLoad(),
										car.toString() }));
								car.setLoadGeneratedFromStaging(true);
								car.updateKernel();
								addCarToTrain(car, rl, rld, testTrack);
								return true;
							}
						}
						car.setLoad(carLoad); // restore car's load
					}
					// okay to drop car?
					if (!status.equals(Track.OKAY)) {
						addLine(buildReport, SEVEN, MessageFormat.format(Bundle
								.getMessage("buildCanNotDropCarBecause"), new Object[] { car.toString(),
								testTrack.getName(), status }));
						continue;
					}
					// No local moves from spur to spur
					if (train.isLocalSwitcher() && !Setup.isLocalSpurMovesEnabled()
							&& testTrack.getLocType().equals(Track.SPUR)
							&& car.getTrack().getLocType().equals(Track.SPUR)) {
						addLine(buildReport, FIVE, MessageFormat.format(Bundle
								.getMessage("buildNoSpurToSpurMove"),
								new Object[] { testTrack.getName() }));
						continue;
					}
					// No local moves from yard to yard
					if (train.isLocalSwitcher() && !Setup.isLocalYardMovesEnabled()
							&& testTrack.getLocType().equals(Track.YARD)
							&& car.getTrack().getLocType().equals(Track.YARD)) {
						addLine(buildReport, FIVE, MessageFormat.format(Bundle
								.getMessage("buildNoYardToYardMove"), new Object[] { testTrack.getName() }));
						continue;
					}
					// No local moves from interchange to interchange
					if (train.isLocalSwitcher() && !Setup.isLocalInterchangeMovesEnabled()
							&& testTrack.getLocType().equals(Track.INTERCHANGE)
							&& car.getTrack().getLocType().equals(Track.INTERCHANGE)) {
						addLine(buildReport, FIVE, MessageFormat.format(Bundle
								.getMessage("buildNoInterchangeToInterchangeMove"), new Object[] { testTrack
								.getName() }));
						continue;
					}

					// not staging, then use
					if (!testTrack.getLocType().equals(Track.STAGING)) {
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
				addLine(buildReport, THREE, MessageFormat.format(Bundle.getMessage("buildCarCanDropMoves"),
						new Object[] { car.toString(),
								(destinationTemp.getName() + ", " + trackTemp.getName()), +rld.getCarMoves(),
								rld.getMaxCarMoves() }));
				if (rldSave == null && multiplePickup) {
					addLine(buildReport, THREE, MessageFormat.format(Bundle.getMessage("buildCarHasSecond"),
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
					if (rld.getName().equals(terminateLocation.getName())) {
						log.debug("Location " + rld.getName() + " is terminate location "
								+ Double.toString(nextRatio));
						nextRatio = nextRatio * nextRatio;
					}
					// bias cars with default loads to a track with a schedule
					if (!trackTemp.getScheduleId().equals("")) {
						log.debug("Track (" + trackTemp.getName() + ") has schedule ("
								+ trackTemp.getScheduleName() + ") adjust nextRatio = " // NOI18N
								+ Double.toString(nextRatio));
						nextRatio = nextRatio * nextRatio;
					}
					// check for an earlier drop in the route
					for (int m = start; m < routeEnd; m++) {
						RouteLocation rle = train.getRoute().getLocationById(routeList.get(m));
						if (rle == rld)
							break; // done
						if (rle.getName().equals(rld.getName())
								&& (rle.getMaxCarMoves() - rle.getCarMoves() > 0) && rle.canDrop()
								&& checkDropTrainDirection(car, rle, trackTemp)) {
							log.debug("Found an earlier drop for car (" + car.toString() + ") destination ("
									+ rle.getName() + ")"); // NOI18N
							nextCarMoves = rle.getCarMoves();
							nextRatio = nextCarMoves / rle.getMaxCarMoves();
							rld = rle; // set car drop to earlier stop
							break;
						}
					}
					log.debug(rldSave.getName() + " = " + Double.toString(saveRatio) + " " + rld.getName()
							+ " = " + Double.toString(nextRatio));
					if (saveRatio < nextRatio) {
						rld = rldSave; // the saved is better than the last found
						trackTemp = trackSave;
					} else if (multiplePickup) {
						addLine(buildReport, THREE, MessageFormat.format(Bundle
								.getMessage("buildCarHasSecond"), new Object[] { car.toString(),
								car.getLocationName() }));
						trackSave = null;
						break; // done
					}
				}
				// every time through, save the best route destination, and track
				rldSave = rld;
				trackSave = trackTemp;
			} else {
				addLine(buildReport, FIVE, MessageFormat.format(Bundle
						.getMessage("buildCouldNotFindDestForCar"), new Object[] { car.toString(),
						rld.getName() }));
			}
		}
		// did we find a destination?
		if (trackSave != null) {
			addCarToTrain(car, rl, rldSave, trackSave);
			return true;
		}
		// is car sitting on a FIFO or LIFO track?
		if (car.getTrack() != null && !car.getTrack().getServiceOrder().equals(Track.NORMAL)) {
			addLine(buildReport, SEVEN, MessageFormat
					.format(Bundle.getMessage("buildBypassCarServiceOrder"), new Object[] { car.toString(),
							car.getTrack().getName(), car.getTrack().getServiceOrder() }));
			// move car id in front of current pointer so car is no longer used on this pass
			carList.remove(car.getId());
			carList.add(carIndex, car.getId());
		}
		addLine(buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildNoDestForCar"),
				new Object[] { car.toString() }));
		addLine(buildReport, SEVEN, BLANK_LINE); // add line when in very detailed report mode
		return false; // no build errors, but car not given destination
	}

	/**
	 * Returns true if car can be picked up later in a train's route
	 */
	private boolean checkForLaterPickUp(RouteLocation rl, RouteLocation rld, Car car) {
		if (rl != rld && rld.getName().equals(car.getLocationName())
				&& !rld.getName().equals(terminateLocation.getName()) && checkPickUpTrainDirection(car, rld)) {
			if (!rld.canPickup()) {
				addLine(buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildPickupLater"),
						new Object[] { car.toString(), rld.getName(), rld.getId() }));
				// log.debug("Later pick up for car ("+car.toString()+") from route location ("+rld.getName()+") id "+
				// rld.getId()+" not possible, no pick ups allowed!");
				return false;
			}
			if (rld.getMaxCarMoves() - rld.getCarMoves() <= 0) {
				addLine(buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildPickupLaterNoRoom"),
						new Object[] { car.toString(), rld.getName(), rld.getId() }));
				// log.debug("Later pick up for car ("+car.toString()+") from route location ("+rld.getName()+") id "+
				// rld.getId()+" not possible, no moves left!");
				return false;
			}
			log.debug("Car (" + car.toString() + ") can be picked up later!");
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
		if (stageTrack == null || !stageTrack.getLocType().equals(Track.STAGING)
				|| !stageTrack.acceptsTypeName(car.getType()) || !stageTrack.acceptsRoadName(car.getRoad()))
			return false;
		// figure out which loads the car can use
		List<String> loads = CarLoads.instance().getNames(car.getType());
		// remove the default names
		loads.remove(CarLoads.instance().getDefaultEmptyName());
		loads.remove(CarLoads.instance().getDefaultLoadName());
		if (loads.size() == 0)
			return false;
		addLine(buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildSearchTrackLoadStaging"),
				new Object[] { car.toString(), car.getType(), car.getLoad(), car.getTrackName(),
						stageTrack.getLocation().getName(), stageTrack.getName() }));
		for (int i = loads.size() - 1; i >= 0; i--) {
			String load = loads.get(i);
			if (!car.getTrack().shipsLoad(load, car.getType())
					|| !stageTrack.acceptsLoad(load, car.getType())
					|| !train.acceptsLoad(load, car.getType())) {
				if (!car.getTrack().shipsLoad(load, car.getType())) {
					addLine(buildReport, SEVEN, MessageFormat.format(Bundle
							.getMessage("buildTrackNotNewLoad"), new Object[] { car.getTrackName(), load,
							stageTrack.getLocation().getName(), stageTrack.getName() }));
				}
				if (!stageTrack.acceptsLoad(load, car.getType())) {
					addLine(buildReport, SEVEN, MessageFormat
							.format(Bundle.getMessage("BuildDestTrackNoLoad"), new Object[] {
									stageTrack.getLocation().getName(), stageTrack.getName(), car.toString(),
									load, }));
				}
				if (!train.acceptsLoad(load, car.getType())) {
					addLine(buildReport, SEVEN, MessageFormat.format(Bundle
							.getMessage("buildTrainNotNewLoad"), new Object[] { train.getName(), load,
							stageTrack.getLocation().getName(), stageTrack.getName() }));

				}
			loads.remove(i);
			}
		}
		// Use random loads rather that the first one that works to create interesting loads
		if (loads.size() > 0) {
			String oldLoad = car.getLoad(); // in case creating a new load still doesn't allow the car to be placed into
											// staging
			int rnd = (int) (Math.random() * loads.size());
			car.setLoad(loads.get(rnd));
			// check to see if car is now accepted by staging
			String status = car.testDestination(stageTrack.getLocation(), stageTrack); // will staging now accept this
																						// car?
			if (status.equals(Track.OKAY)) {
				car.setLoadGeneratedFromStaging(true);
				car.setFinalDestination(stageTrack.getLocation());
				car.setFinalDestinationTrack(stageTrack);
				// is car part of kernel?
				car.updateKernel();
				addLine(buildReport, SEVEN, MessageFormat.format(
						Bundle.getMessage("buildAddingScheduleLoad"), new Object[] { car.getLoad(),
								car.toString() }));
				return true;
			}
			car.setLoad(oldLoad); // restore load and report failure
			addLine(buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildCanNotDropCarBecause"),
					new Object[] { car.toString(), stageTrack.getName(), status }));
		}
		addLine(buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildUnableNewLoadStaging"),
				new Object[] { car.toString(), car.getTrackName(), stageTrack.getLocation().getName(),
						stageTrack.getName() }));
		return false;
	}

	/**
	 * Checks to see if cars that are already in the train can be redirected from the alternate track to the spur that
	 * really wants the car. Fixes the issue of having cars placed at the alternate when the spur's cars get pulled by
	 * this train, but cars were sent to the alternate because the spur was full at the time it was tested.
	 * 
	 * @param rl
	 */
	private void redirectCarsFromAlternateTrack() {
		if (!Setup.isBuildAggressive())
			return;
		List<String> cars = carManager.getByTrainDestinationList(train);
		for (int i = 0; i < cars.size(); i++) {
			Car car = carManager.getById(cars.get(i));
			// does the car have a final destination and the destination is this one?
			if (car.getFinalDestination() == null || car.getFinalDestinationTrack() == null
					|| !car.getFinalDestinationName().equals(car.getDestinationName()))
				continue;
			log.debug("Car (" + car.toString() + ") destination track (" + car.getDestinationTrackName()
					+ ") has final destination track (" // NOI18N
					+ car.getFinalDestinationTrackName() + ") location (" + car.getDestinationName() + ")"); // NOI18N
			if (car.testDestination(car.getFinalDestination(), car.getFinalDestinationTrack()).equals(Track.OKAY)) {
				Track alternate = car.getFinalDestinationTrack().getAlternativeTrack();
				if (alternate != null && alternate.getLocType().equals(Track.YARD)
						&& car.getDestinationTrack() == alternate) {
					log.debug("Car (" + car.toString() + ") alternate track ("
							+ car.getDestinationTrackName()
							+ ") can be redirected to final destination track (" // NOI18N
							+ car.getFinalDestinationTrackName() + ")");
					addLine(buildReport, FIVE, MessageFormat.format(Bundle
							.getMessage("buildRedirectFromAlternate"), new Object[] {
							car.getFinalDestinationTrackName(), car.toString(), car.getDestinationTrackName() }));
					car.setDestination(car.getFinalDestination(), car.getFinalDestinationTrack());
				}
			}
		}
	}
	
	// report any cars left at location
	private void reportCarsNotMoved(RouteLocation rl, int percent) {
		if (!success || percent != 100)
			return;
		if (carIndex < 0)
			carIndex = 0;
		for (int i = carIndex; i < carList.size(); i++) {
			Car car = carManager.getById(carList.get(i));
			// find a car at this location that hasn't been given a destination
			if (!car.getLocationName().equals(rl.getName()) || car.getRouteDestination() != null)
				continue;
			addLine(buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildCarIgnored"),
					new Object[] { car.toString(), car.getType(), car.getLoad(), car.getLocationName(),
							car.getTrackName() }));
		}
		addLine(buildReport, SEVEN, BLANK_LINE);
	}

	private void buildFailed(BuildFailedException e) {
		String msg = e.getMessage();
		train.setBuildFailedMessage(msg);
		train.setStatus(Train.BUILDFAILED);
		train.setBuildFailed(true);
		if (log.isDebugEnabled())
			log.debug(msg);
		if (TrainManager.instance().isBuildMessagesEnabled()) {
			if (e.getExceptionType().equals(BuildFailedException.NORMAL)) {
				JOptionPane.showMessageDialog(null, msg, MessageFormat.format(Bundle
						.getMessage("buildErrorMsg"),
						new Object[] { train.getName(), train.getDescription() }), JOptionPane.ERROR_MESSAGE);
			} else {
				// build error, could not find destinations for cars departing staging
				Object[] options = { Bundle.getMessage("buttonRemoveCars"), "OK" };
				int results = JOptionPane.showOptionDialog(null, msg, MessageFormat.format(Bundle
						.getMessage("buildErrorMsg"),
						new Object[] { train.getName(), train.getDescription() }),
						JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[1]);
				if (results == 0) {
					log.debug("User requested that cars be removed from staging track");
					removeCarsFromStaging();
				}
			}
			int size = carManager.getByTrainList(train).size();
			if (size > 0) {
				if (JOptionPane.showConfirmDialog(null, MessageFormat.format(Bundle
						.getMessage("buildCarsResetTrain"), new Object[] { size, train.getName() }), Bundle
						.getMessage("buildResetTrain"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					train.reset();
				}
			}
		}
		if (buildReport != null) {
			addLine(buildReport, ONE, msg);
			// Write to disk and close buildReport
			addLine(buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildFailedMsg"),
					new Object[] { train.getName() }));
			buildReport.flush();
			buildReport.close();
		}
	}

	/**
	 * build has failed due to cars in staging not having destinations this routine removes those cars from the staging
	 * track by user request.
	 */
	private void removeCarsFromStaging() {
		if (departStageTrack == null)
			return;
		for (carIndex = 0; carIndex < carList.size(); carIndex++) {
			Car car = carManager.getById(carList.get(carIndex));
			// remove cars from departure staging track that haven't been assigned to this train
			if (car.getTrack().equals(departStageTrack) && car.getTrain() == null) {
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

	public String getExceptionType() {
		return type;
	}

}
