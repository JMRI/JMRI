package jmri.jmrit.operations.trains.trainbuilder;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.*;

/**
 * Contains methods for engines when building a train.
 * 
 * @author Daniel Boudreau Copyright (C) 2022
 */
public class TrainBuilderEngines extends TrainBuilderBase {

    /**
     * Builds a list of possible engines for this train.
     */
    protected void getAndRemoveEnginesFromList() {
        _engineList = engineManager.getAvailableTrainList(_train);

        // remove any locos that the train can't use
        for (int indexEng = 0; indexEng < _engineList.size(); indexEng++) {
            Engine engine = _engineList.get(indexEng);
            // remove engines types that train does not service
            if (!_train.isTypeNameAccepted(engine.getTypeName())) {
                addLine(_buildReport, SEVEN, Bundle.getMessage("buildExcludeEngineType", engine.toString(),
                        engine.getLocationName(), engine.getTrackName(), engine.getTypeName()));
                _engineList.remove(indexEng--);
                continue;
            }
            // remove engines with roads that train does not service
            if (!_train.isLocoRoadNameAccepted(engine.getRoadName())) {
                addLine(_buildReport, SEVEN, Bundle.getMessage("buildExcludeEngineRoad", engine.toString(),
                        engine.getLocationName(), engine.getTrackName(), engine.getRoadName()));
                _engineList.remove(indexEng--);
                continue;
            }
            // remove engines with owners that train does not service
            if (!_train.isOwnerNameAccepted(engine.getOwnerName())) {
                addLine(_buildReport, SEVEN, Bundle.getMessage("buildExcludeEngineOwner", engine.toString(),
                        engine.getLocationName(), engine.getTrackName(), engine.getOwnerName()));
                _engineList.remove(indexEng--);
                continue;
            }
            // remove engines with built dates that train does not service
            if (!_train.isBuiltDateAccepted(engine.getBuilt())) {
                addLine(_buildReport, SEVEN, Bundle.getMessage("buildExcludeEngineBuilt", engine.toString(),
                        engine.getLocationName(), engine.getTrackName(), engine.getBuilt()));
                _engineList.remove(indexEng--);
                continue;
            }
            // remove engines that are out of service
            if (engine.isOutOfService()) {
                addLine(_buildReport, SEVEN, Bundle.getMessage("buildExcludeEngineOutOfService", engine.toString(),
                        engine.getLocationName(), engine.getTrackName()));
                _engineList.remove(indexEng--);
                continue;
            }
            // remove engines that aren't on the train's route
            if (_train.getRoute().getLastLocationByName(engine.getLocationName()) == null) {
                log.debug("removing engine ({}) location ({}) not serviced by train", engine.toString(),
                        engine.getLocationName());
                _engineList.remove(indexEng--);
                continue;
            }
            // is engine at interchange?
            if (engine.getTrack().isInterchange()) {
                // don't service a engine at interchange and has been dropped off
                // by this train
                if (engine.getTrack().getPickupOption().equals(Track.ANY) &&
                        engine.getLastRouteId().equals(_train.getRoute().getId())) {
                    addLine(_buildReport, SEVEN, Bundle.getMessage("buildExcludeEngineDropByTrain", engine.toString(),
                            engine.getTypeName(), _train.getRoute().getName(), engine.getLocationName(), engine.getTrackName()));
                    _engineList.remove(indexEng--);
                    continue;
                }
            }
            // is engine at interchange or spur and is this train allowed to pull?
            if (engine.getTrack().isInterchange() || engine.getTrack().isSpur()) {
                if (engine.getTrack().getPickupOption().equals(Track.TRAINS) ||
                        engine.getTrack().getPickupOption().equals(Track.EXCLUDE_TRAINS)) {
                    if (engine.getTrack().isPickupTrainAccepted(_train)) {
                        log.debug("Engine ({}) can be picked up by this train", engine.toString());
                    } else {
                        addLine(_buildReport, SEVEN,
                                Bundle.getMessage("buildExcludeEngineByTrain", engine.toString(), engine.getTypeName(),
                                        engine.getTrack().getTrackTypeName(), engine.getLocationName(), engine.getTrackName()));
                        _engineList.remove(indexEng--);
                        continue;
                    }
                } else if (engine.getTrack().getPickupOption().equals(Track.ROUTES) ||
                        engine.getTrack().getPickupOption().equals(Track.EXCLUDE_ROUTES)) {
                    if (engine.getTrack().isPickupRouteAccepted(_train.getRoute())) {
                        log.debug("Engine ({}) can be picked up by this route", engine.toString());
                    } else {
                        addLine(_buildReport, SEVEN,
                                Bundle.getMessage("buildExcludeEngineByRoute", engine.toString(), engine.getTypeName(),
                                        engine.getTrack().getTrackTypeName(), engine.getLocationName(), engine.getTrackName()));
                        _engineList.remove(indexEng--);
                        continue;
                    }
                }
            }
        }
    }
    
    protected boolean getEngines(String requestedEngines, String model, String road, RouteLocation rl,
            RouteLocation rld) throws BuildFailedException {
        return getEngines(requestedEngines, model, road, rl, rld, !USE_BUNIT);
    }
    
    /**
     * Get the engines for this train at a route location. If departing from
     * staging engines must come from that track. Finds the required number of
     * engines in a consist, or if the option to build from single locos, builds
     * a consist for the user. When true, engines successfully added to train
     * for the leg requested.
     *
     * @param requestedEngines Requested number of Engines, can be number, AUTO
     *                         or AUTO HPT
     * @param model            Optional model name for the engines
     * @param road             Optional road name for the engines
     * @param rl               Departure route location for the engines
     * @param rld              Destination route location for the engines
     * @param useBunit         true if B unit engine is allowed
     * @return true if correct number of engines found.
     * @throws BuildFailedException if coding issue
     */
    protected boolean getEngines(String requestedEngines, String model, String road, RouteLocation rl,
            RouteLocation rld, boolean useBunit) throws BuildFailedException {
        // load departure track if staging
        Track departStageTrack = null;
        if (rl == _train.getTrainDepartsRouteLocation()) {
            departStageTrack = _departStageTrack; // get departure track from
                                                  // staging, could be null
        }

        int reqNumberEngines = getNumberEngines(requestedEngines);

        // if not departing staging track and engines aren't required done!
        if (departStageTrack == null && reqNumberEngines == 0) {
            return true;
        }
        // if departing staging and no engines required and none available,
        // we're done
        if (departStageTrack != null && reqNumberEngines == 0 && departStageTrack.getNumberEngines() == 0) {
            return true;
        }

        // code check, staging track selection checks number of engines needed
        if (departStageTrack != null &&
                reqNumberEngines != 0 &&
                departStageTrack.getNumberEngines() != reqNumberEngines) {
            throw new BuildFailedException(Bundle.getMessage("buildStagingNotEngines", departStageTrack.getName(),
                    departStageTrack.getNumberEngines(), reqNumberEngines));
        }

        // code check
        if (rl == null || rld == null) {
            throw new BuildFailedException(
                    Bundle.getMessage("buildErrorEngLocUnknown"));
        }

        addLine(_buildReport, FIVE, Bundle.getMessage("buildBegineSearchEngines", reqNumberEngines, model, road,
                rl.getName(), rld.getName()));

        int assignedLocos = 0; // the number of locos assigned to this train
        List<Engine> singleLocos = new ArrayList<>();
        for (int indexEng = 0; indexEng < _engineList.size(); indexEng++) {
            Engine engine = _engineList.get(indexEng);
            log.debug("Engine ({}) at location ({}, {})", engine.toString(), engine.getLocationName(),
                    engine.getTrackName());

            // use engines that are departing from the selected staging track
            // (departTrack
            // != null if staging)
            if (departStageTrack != null && !departStageTrack.equals(engine.getTrack())) {
                continue;
            }
            // use engines that are departing from the correct location
            if (!engine.getLocationName().equals(rl.getName())) {
                log.debug("Skipping engine ({}) at location ({})", engine.toString(), engine.getLocationName());
                continue;
            }
            // skip engines models that train does not service
            if (!model.equals(Train.NONE) && !engine.getModel().equals(model)) {
                addLine(_buildReport, SEVEN, Bundle.getMessage("buildExcludeEngineModel", engine.toString(),
                        engine.getModel(), engine.getLocationName()));
                continue;
            }
            // Does the train have a very specific engine road name requirement?
            if (!road.equals(Train.NONE) && !engine.getRoadName().equals(road)) {
                addLine(_buildReport, SEVEN, Bundle.getMessage("buildExcludeEngineRoad", engine.toString(),
                        engine.getLocationName(), engine.getTrackName(), engine.getRoadName()));
                continue;
            }
            // skip engines on tracks that don't service the train's departure
            // direction
            if (!checkPickUpTrainDirection(engine, rl)) {
                continue;
            }
            // skip engines that have been assigned destinations that don't
            // match the requested destination
            if (engine.getDestination() != null && !engine.getDestinationName().equals(rld.getName())) {
                addLine(_buildReport, SEVEN, Bundle.getMessage("buildExcludeEngineDestination", engine.toString(),
                        engine.getDestinationName()));
                continue;
            }
            // don't use non lead locos in a consist
            if (engine.getConsist() != null) {
                if (engine.isLead()) {
                    addLine(_buildReport, SEVEN,
                            Bundle.getMessage("buildEngineLeadConsist", engine.toString(),
                                    engine.getConsist().getName(), engine.getConsist().getEngines().size()));
                } else {
                    continue;
                }
            }
            // departing staging, then all locos must go!
            if (departStageTrack != null) {
                if (!setEngineDestination(engine, rl, rld)) {
                    return false;
                }
                _engineList.remove(indexEng--);
                if (engine.getConsist() != null) {
                    assignedLocos = assignedLocos + engine.getConsist().getSize();
                } else {
                    assignedLocos++;
                }
                continue;
            }
            // can't use B units if requesting one loco
            if (!useBunit && reqNumberEngines == 1 && engine.isBunit()) {
                addLine(_buildReport, SEVEN,
                        Bundle.getMessage("buildExcludeEngineBunit", engine.toString(), engine.getModel()));
                continue;
            }
            // is this engine part of a consist?
            if (engine.getConsist() == null) {
                // single engine, but does the train require a consist?
                if (reqNumberEngines > 1) {
                    addLine(_buildReport, SEVEN,
                            Bundle.getMessage("buildExcludeEngineSingle", engine.toString(), reqNumberEngines));
                    singleLocos.add(engine);
                    continue;
                }
                // engine is part of a consist
            } else if (engine.getConsist().getSize() == reqNumberEngines) {
                log.debug("Consist ({}) has the required number of engines", engine.getConsist().getName()); // NOI18N
            } else if (reqNumberEngines != 0) {
                addLine(_buildReport, SEVEN,
                        Bundle.getMessage("buildExcludeEngConsistNumber", engine.toString(),
                                engine.getConsist().getName(), engine.getConsist().getSize()));
                continue;
            }
            // found a loco or consist!
            assignedLocos++;

            // now find terminal track for engine(s)
            addLine(_buildReport, FIVE,
                    Bundle.getMessage("buildEngineRoadModelType", engine.toString(), engine.getRoadName(),
                            engine.getModel(), engine.getTypeName(), engine.getLocationName(), engine.getTrackName(),
                            rld.getName()));
            if (setEngineDestination(engine, rl, rld)) {
                _engineList.remove(indexEng--);
                return true; // normal exit when not staging
            }
        }
        // build a consist out of non-consisted locos
        if (assignedLocos == 0 && reqNumberEngines > 1 && _train.isBuildConsistEnabled()) {
            if (buildConsistFromSingleLocos(reqNumberEngines, singleLocos, rl, rld)) {
                return true; // normal exit when building with single locos
            }
        }
        if (assignedLocos == 0) {
            String locationName = rl.getName();
            if (departStageTrack != null) {
                locationName = locationName + ", " + departStageTrack.getName();
            }
            addLine(_buildReport, FIVE, Bundle.getMessage("buildNoLocosFoundAtLocation", locationName));
        } else if (departStageTrack != null && (reqNumberEngines == 0 || reqNumberEngines == assignedLocos)) {
            return true; // normal exit assigning from staging
        }
        // not able to assign engines to train
        return false;
    }
    
    private boolean buildConsistFromSingleLocos(int reqNumberEngines, List<Engine> singleLocos, RouteLocation rl,
            RouteLocation rld) {
        addLine(_buildReport, FIVE, Bundle.getMessage("buildOptionBuildConsist", reqNumberEngines, rl.getName()));
        addLine(_buildReport, FIVE, Bundle.getMessage("buildOptionSingleLocos", singleLocos.size(), rl.getName()));
        if (singleLocos.size() >= reqNumberEngines) {
            int locos = 0;
            // first find an "A" unit
            for (Engine engine : singleLocos) {
                if (engine.isBunit()) {
                    continue;
                }
                if (setEngineDestination(engine, rl, rld)) {
                    _engineList.remove(engine);
                    singleLocos.remove(engine);
                    locos++;
                    break; // found "A" unit
                }
            }
            // did we find an "A" unit?
            if (locos > 0) {
                // now add the rest "A" or "B" units
                for (Engine engine : singleLocos) {
                    if (setEngineDestination(engine, rl, rld)) {
                        _engineList.remove(engine);
                        locos++;
                    }
                    if (locos == reqNumberEngines) {
                        return true; // done!
                    }
                }
            } else {
                // list the "B" units found
                for (Engine engine : singleLocos) {
                    if (engine.isBunit()) {
                        addLine(_buildReport, FIVE,
                                Bundle.getMessage("BuildEngineBunit", engine.toString(), engine.getLocationName(),
                                        engine.getTrackName()));
                    }
                }
            }
        }
        return false;
    }

    /**
     * Adds engines to the train starting at the first location in the train's
     * route. Note that engines from staging are already part of the train.
     * There can be up to two engine swaps in a train's route.
     * 
     * @throws BuildFailedException if required engines can't be added to train.
     */
    protected void addEnginesToTrain() throws BuildFailedException {
        // allow up to two engine and caboose swaps in the train's route
        RouteLocation engineTerminatesFirstLeg = _train.getTrainTerminatesRouteLocation();
        RouteLocation engineTerminatesSecondLeg = _train.getTrainTerminatesRouteLocation();

        // Adjust where the locos will terminate
        if ((_train.getSecondLegOptions() & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES &&
                _train.getSecondLegStartRouteLocation() != null) {
            engineTerminatesFirstLeg = _train.getSecondLegStartRouteLocation();
        }
        if ((_train.getThirdLegOptions() & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES &&
                _train.getThirdLegStartRouteLocation() != null) {
            engineTerminatesSecondLeg = _train.getThirdLegStartRouteLocation();
            // No engine or caboose change at first leg?
            if ((_train.getSecondLegOptions() & Train.CHANGE_ENGINES) != Train.CHANGE_ENGINES) {
                engineTerminatesFirstLeg = _train.getThirdLegStartRouteLocation();
            }
        }

        if (_train.getLeadEngine() == null) {
            // option to remove locos from the train
            if ((_train.getSecondLegOptions() & Train.REMOVE_ENGINES) == Train.REMOVE_ENGINES &&
                    _train.getSecondLegStartRouteLocation() != null) {
                addLine(_buildReport, THREE, BLANK_LINE);
                addLine(_buildReport, THREE,
                        Bundle.getMessage("buildTrainRemoveEngines", _train.getSecondLegNumberEngines(),
                                _train.getSecondLegStartLocationName(), _train.getSecondLegEngineModel(),
                                _train.getSecondLegEngineRoad()));
                if (getEngines(_train.getSecondLegNumberEngines(), _train.getSecondLegEngineModel(),
                        _train.getSecondLegEngineRoad(), _train.getTrainDepartsRouteLocation(),
                        _train.getSecondLegStartRouteLocation())) {
                } else if (getConsist(_train.getSecondLegNumberEngines(), _train.getSecondLegEngineModel(),
                        _train.getSecondLegEngineRoad(), _train.getTrainDepartsRouteLocation(),
                        _train.getSecondLegStartRouteLocation())) {
                } else {
                    throw new BuildFailedException(Bundle.getMessage("buildErrorEngines",
                            _train.getSecondLegNumberEngines(), _train.getTrainDepartsName(),
                            _train.getSecondLegStartRouteLocation().getLocation().getName()));
                }
            }
            if ((_train.getThirdLegOptions() & Train.REMOVE_ENGINES) == Train.REMOVE_ENGINES &&
                    _train.getThirdLegStartRouteLocation() != null) {
                addLine(_buildReport, THREE, BLANK_LINE);
                addLine(_buildReport, THREE,
                        Bundle.getMessage("buildTrainRemoveEngines", _train.getThirdLegNumberEngines(),
                                _train.getThirdLegStartLocationName(), _train.getThirdLegEngineModel(),
                                _train.getThirdLegEngineRoad()));
                if (getEngines(_train.getThirdLegNumberEngines(), _train.getThirdLegEngineModel(),
                        _train.getThirdLegEngineRoad(), _train.getTrainDepartsRouteLocation(),
                        _train.getThirdLegStartRouteLocation())) {
                } else if (getConsist(_train.getThirdLegNumberEngines(), _train.getThirdLegEngineModel(),
                        _train.getThirdLegEngineRoad(), _train.getTrainDepartsRouteLocation(),
                        _train.getThirdLegStartRouteLocation())) {
                } else {
                    throw new BuildFailedException(Bundle.getMessage("buildErrorEngines",
                            _train.getThirdLegNumberEngines(), _train.getTrainDepartsName(),
                            _train.getThirdLegStartRouteLocation().getLocation().getName()));
                }
            }
            // load engines at the start of the route for this train
            addLine(_buildReport, THREE, BLANK_LINE);
            if (getEngines(_train.getNumberEngines(), _train.getEngineModel(), _train.getEngineRoad(),
                    _train.getTrainDepartsRouteLocation(), engineTerminatesFirstLeg)) {
                // when adding a caboose later in the route, no engine change
                _secondLeadEngine = _lastEngine;
                _thirdLeadEngine = _lastEngine;
            } else if (getConsist(_train.getNumberEngines(), _train.getEngineModel(), _train.getEngineRoad(),
                    _train.getTrainDepartsRouteLocation(), engineTerminatesFirstLeg)) {
                // when adding a caboose later in the route, no engine change
                _secondLeadEngine = _lastEngine;
                _thirdLeadEngine = _lastEngine;
            } else {
                throw new BuildFailedException(Bundle.getMessage("buildErrorEngines", _train.getNumberEngines(),
                        _train.getTrainDepartsName(), engineTerminatesFirstLeg.getName()));
            }
        }

        // First engine change in route?
        if ((_train.getSecondLegOptions() & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES ||
                (_train.getSecondLegOptions() & Train.ADD_ENGINES) == Train.ADD_ENGINES) {
            addLine(_buildReport, THREE, BLANK_LINE);
            if ((_train.getSecondLegOptions() & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES) {
                addLine(_buildReport, THREE,
                        Bundle.getMessage("buildTrainEngineChange", _train.getSecondLegStartLocationName(),
                                _train.getSecondLegNumberEngines(), _train.getSecondLegEngineModel(),
                                _train.getSecondLegEngineRoad()));
            } else {
                addLine(_buildReport, THREE,
                        Bundle.getMessage("buildTrainAddEngines", _train.getSecondLegNumberEngines(),
                                _train.getSecondLegStartLocationName(), _train.getSecondLegEngineModel(),
                                _train.getSecondLegEngineRoad()));
            }
            if (getEngines(_train.getSecondLegNumberEngines(), _train.getSecondLegEngineModel(),
                    _train.getSecondLegEngineRoad(), _train.getSecondLegStartRouteLocation(),
                    engineTerminatesSecondLeg)) {
                _secondLeadEngine = _lastEngine;
                _thirdLeadEngine = _lastEngine;
            } else if (getConsist(_train.getSecondLegNumberEngines(), _train.getSecondLegEngineModel(),
                    _train.getSecondLegEngineRoad(), _train.getSecondLegStartRouteLocation(),
                    engineTerminatesSecondLeg)) {
                _secondLeadEngine = _lastEngine;
                _thirdLeadEngine = _lastEngine;
            } else {
                throw new BuildFailedException(
                        Bundle.getMessage("buildErrorEngines", _train.getSecondLegNumberEngines(),
                                _train.getSecondLegStartRouteLocation(), engineTerminatesSecondLeg));
            }
        }
        // Second engine change in route?
        if ((_train.getThirdLegOptions() & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES ||
                (_train.getThirdLegOptions() & Train.ADD_ENGINES) == Train.ADD_ENGINES) {
            addLine(_buildReport, THREE, BLANK_LINE);
            if ((_train.getThirdLegOptions() & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES) {
                addLine(_buildReport, THREE,
                        Bundle.getMessage("buildTrainEngineChange", _train.getThirdLegStartLocationName(),
                                _train.getThirdLegNumberEngines(), _train.getThirdLegEngineModel(),
                                _train.getThirdLegEngineRoad()));
            } else {
                addLine(_buildReport, THREE,
                        Bundle.getMessage("buildTrainAddEngines", _train.getThirdLegNumberEngines(),
                                _train.getThirdLegStartLocationName(), _train.getThirdLegEngineModel(),
                                _train.getThirdLegEngineRoad()));
            }
            if (getEngines(_train.getThirdLegNumberEngines(), _train.getThirdLegEngineModel(),
                    _train.getThirdLegEngineRoad(), _train.getThirdLegStartRouteLocation(),
                    _train.getTrainTerminatesRouteLocation())) {
                _thirdLeadEngine = _lastEngine;
            } else if (getConsist(_train.getThirdLegNumberEngines(), _train.getThirdLegEngineModel(),
                    _train.getThirdLegEngineRoad(), _train.getThirdLegStartRouteLocation(),
                    _train.getTrainTerminatesRouteLocation())) {
                _thirdLeadEngine = _lastEngine;
            } else {
                throw new BuildFailedException(
                        Bundle.getMessage("buildErrorEngines", Integer.parseInt(_train.getThirdLegNumberEngines()),
                                _train.getThirdLegStartRouteLocation(), _train.getTrainTerminatesRouteLocation()));
            }
        }
        if (!_train.getNumberEngines().equals("0") &&
                (!_train.isBuildConsistEnabled() || Setup.getHorsePowerPerTon() == 0)) {
            addLine(_buildReport, SEVEN, Bundle.getMessage("buildDoneAssingEnginesTrain", _train.getName()));
        }
    }
    
    protected boolean getConsist(String reqNumEngines, String model, String road, RouteLocation rl, RouteLocation rld)
            throws BuildFailedException {
        if (reqNumEngines.equals(Train.AUTO_HPT)) {
            for (int i = 2; i < Setup.getMaxNumberEngines(); i++) {
                if (getEngines(Integer.toString(i), model, road, rl, rld)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    protected void showEnginesByLocation() {
        // show how many engines were found
        addLine(_buildReport, SEVEN, BLANK_LINE);
        addLine(_buildReport, ONE,
                Bundle.getMessage("buildFoundLocos", Integer.toString(_engineList.size()), _train.getName()));

        // only show engines once using the train's route
        List<String> locationNames = new ArrayList<>();
        for (RouteLocation rl : _train.getRoute().getLocationsBySequenceList()) {
            if (locationNames.contains(rl.getName())) {
                continue;
            }
            locationNames.add(rl.getName());
            int count = countRollingStockAt(rl, new ArrayList<RollingStock>(_engineList));
            if (rl.getLocation().isStaging()) {
                addLine(_buildReport, FIVE,
                        Bundle.getMessage("buildLocosInStaging", count, rl.getName()));
            } else {
                addLine(_buildReport, FIVE,
                        Bundle.getMessage("buildLocosAtLocation", count, rl.getName()));
            }
            for (Engine engine : _engineList) {
                if (engine.getLocationName().equals(rl.getName())) {
                    addLine(_buildReport, SEVEN,
                            Bundle.getMessage("buildLocoAtLocWithMoves", engine.toString(), engine.getTypeName(),
                                    engine.getModel(), engine.getLocationName(), engine.getTrackName(),
                                    engine.getMoves()));
                }
            }
            addLine(_buildReport, SEVEN, BLANK_LINE);
        }
    }

    /**
     * Adds engines to the train if needed based on HPT. Note that the engine
     * additional weight isn't considered in this method so HP requirements can
     * be lower compared to the original calculation which did include the
     * weight of the engines.
     *
     * @param hpAvailable   the engine hp already assigned to the train for this
     *                      leg
     * @param extraHpNeeded the additional hp needed
     * @param rlNeedHp      where in the route the additional hp is needed
     * @param rl            the start of the leg
     * @param rld           the end of the leg
     * @throws BuildFailedException if unable to add engines to train
     */
    protected void addEnginesBasedHPT(int hpAvailable, int extraHpNeeded, RouteLocation rlNeedHp, RouteLocation rl,
            RouteLocation rld) throws BuildFailedException {
        if (rlNeedHp == null) {
            return;
        }
        int numberLocos = 0;
        // determine how many locos have already been assigned to the train
        List<Engine> engines = engineManager.getList(_train);
        for (Engine rs : engines) {
            if (rs.getRouteLocation() == rl) {
                numberLocos++;
            }
        }

        addLine(_buildReport, ONE, BLANK_LINE);
        addLine(_buildReport, ONE, Bundle.getMessage("buildTrainReqExtraHp", extraHpNeeded, rlNeedHp.getName(),
                rld.getName(), numberLocos));

        // determine engine model and road
        String model = _train.getEngineModel();
        String road = _train.getEngineRoad();
        if ((_train.getSecondLegOptions() & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES &&
                rl == _train.getSecondLegStartRouteLocation()) {
            model = _train.getSecondLegEngineModel();
            road = _train.getSecondLegEngineRoad();
        } else if ((_train.getThirdLegOptions() & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES &&
                rl == _train.getThirdLegStartRouteLocation()) {
            model = _train.getThirdLegEngineModel();
            road = _train.getThirdLegEngineRoad();
        }

        while (numberLocos < Setup.getMaxNumberEngines()) {
            // if no engines assigned, can't use B unit as first engine
            if (!getEngines("1", model, road, rl, rld, numberLocos > 0)) {
                throw new BuildFailedException(Bundle.getMessage("buildErrorEngines", Bundle.getMessage("additional"),
                        rl.getName(), rld.getName()));
            }
            numberLocos++;
            int currentHp = _train.getTrainHorsePower(rlNeedHp);
            if (currentHp > hpAvailable + extraHpNeeded) {
                break; // done
            }
            if (numberLocos < Setup.getMaxNumberEngines()) {
                addLine(_buildReport, FIVE, BLANK_LINE);
                addLine(_buildReport, THREE,
                        Bundle.getMessage("buildContinueAddLocos", (hpAvailable + extraHpNeeded - currentHp),
                                rlNeedHp.getName(), rld.getName(), numberLocos, currentHp));
            } else {
                addLine(_buildReport, FIVE,
                        Bundle.getMessage("buildMaxNumberLocoAssigned", Setup.getMaxNumberEngines()));
            }
        }
    }

    /**
     * Checks to see if the engine or consist assigned to the train has the
     * appropriate HP. If the train's HP requirements are significantly higher
     * or lower than the engine that was assigned, the program will search for a
     * more appropriate engine or consist, and assign that engine or consist to
     * the train. The HP calculation is based on a minimum train speed of 36
     * MPH. The formula HPT x 12 / % Grade = Speed, is used to determine the
     * horsepower required. Speed is fixed at 36 MPH. For example a 1% grade
     * requires a minimum of 3 HPT. Disabled for trains departing staging.
     * 
     * @throws BuildFailedException if coding error.
     */
    protected void checkEngineHP() throws BuildFailedException {
        if (Setup.getHorsePowerPerTon() != 0) {
            if (_train.getNumberEngines().equals(Train.AUTO_HPT)) {
                checkEngineHP(_train.getLeadEngine(), _train.getEngineModel(), _train.getEngineRoad()); // 1st
                                                                                                        // leg
            }
            if ((_train.getSecondLegOptions() & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES &&
                    _train.getSecondLegNumberEngines().equals(Train.AUTO_HPT)) {
                checkEngineHP(_secondLeadEngine, _train.getSecondLegEngineModel(), _train.getSecondLegEngineRoad());
            }
            if ((_train.getThirdLegOptions() & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES &&
                    _train.getThirdLegNumberEngines().equals(Train.AUTO_HPT)) {
                checkEngineHP(_thirdLeadEngine, _train.getThirdLegEngineModel(), _train.getThirdLegEngineRoad());
            }
        }
    }

    private void checkEngineHP(Engine leadEngine, String model, String road) throws BuildFailedException {
        // code check
        if (leadEngine == null) {
            throw new BuildFailedException("ERROR coding issue, engine missing from checkEngineHP()");
        }
        // departing staging?
        if (leadEngine.getRouteLocation() == _train.getTrainDepartsRouteLocation() && _train.isDepartingStaging()) {
            return;
        }
        addLine(_buildReport, ONE, BLANK_LINE);
        addLine(_buildReport, ONE,
                Bundle.getMessage("buildDetermineHpNeeded", leadEngine.toString(), leadEngine.getLocationName(),
                        leadEngine.getDestinationName(), _train.getTrainHorsePower(leadEngine.getRouteLocation()),
                        Setup.getHorsePowerPerTon()));
        // now determine the HP needed for this train
        double hpNeeded = 0;
        int hpAvailable = 0;
        Route route = _train.getRoute();
        if (route != null) {
            boolean helper = false;
            boolean foundStart = false;
            for (RouteLocation rl : route.getLocationsBySequenceList()) {
                if (!foundStart && rl != leadEngine.getRouteLocation()) {
                    continue;
                }
                foundStart = true;
                if ((_train.getSecondLegOptions() == Train.HELPER_ENGINES &&
                        rl == _train.getSecondLegStartRouteLocation()) ||
                        (_train.getThirdLegOptions() == Train.HELPER_ENGINES &&
                                rl == _train.getThirdLegStartRouteLocation())) {
                    addLine(_buildReport, FIVE,
                            Bundle.getMessage("AddHelpersAt", rl.getName()));
                    helper = true;
                }
                if ((_train.getSecondLegOptions() == Train.HELPER_ENGINES &&
                        rl == _train.getSecondLegEndRouteLocation()) ||
                        (_train.getThirdLegOptions() == Train.HELPER_ENGINES &&
                                rl == _train.getThirdLegEndRouteLocation())) {
                    addLine(_buildReport, FIVE,
                            Bundle.getMessage("RemoveHelpersAt", rl.getName()));
                    helper = false;
                }
                if (helper) {
                    continue; // ignore HP needed when helpers are assigned to
                              // the train
                }
                // check for a change of engines in the train's route
                if (rl == leadEngine.getRouteDestination()) {
                    log.debug("Remove loco ({}) at ({})", leadEngine.toString(), rl.getName());
                    break; // done
                }
                if (_train.getTrainHorsePower(rl) > hpAvailable)
                    hpAvailable = _train.getTrainHorsePower(rl);
                int weight = rl.getTrainWeight();
                double hpRequired = (Control.speedHpt * rl.getGrade() / 12) * weight;
                if (hpRequired < Setup.getHorsePowerPerTon() * weight)
                    hpRequired = Setup.getHorsePowerPerTon() * weight; // min HPT
                if (hpRequired > hpNeeded) {
                    addLine(_buildReport, SEVEN,
                            Bundle.getMessage("buildReportTrainHpNeeds", weight, _train.getNumberCarsInTrain(rl),
                                    rl.getGrade(), rl.getName(), rl.getId(), hpRequired));
                    hpNeeded = hpRequired;
                }
            }
        }
        if (hpNeeded > hpAvailable) {
            addLine(_buildReport, ONE,
                    Bundle.getMessage("buildAssignedHpNotEnough", leadEngine.toString(), hpAvailable, hpNeeded));
            getNewEngine((int) hpNeeded, leadEngine, model, road);
        } else if (hpAvailable > 2 * hpNeeded) {
            addLine(_buildReport, ONE,
                    Bundle.getMessage("buildAssignedHpTooMuch", leadEngine.toString(), hpAvailable, hpNeeded));
            getNewEngine((int) hpNeeded, leadEngine, model, road);
        } else {
            log.debug("Keeping engine ({}) it meets the train's HP requirement", leadEngine.toString());
        }
    }
    
    /**
     * Removes engine from train and attempts to replace it with engine or
     * consist that meets the HP requirements of the train.
     *
     * @param hpNeeded   How much hp is needed
     * @param leadEngine The lead engine for this leg
     * @param model      The engine's model
     * @param road       The engine's road
     * @throws BuildFailedException if new engine not found
     */
    protected void getNewEngine(int hpNeeded, Engine leadEngine, String model, String road)
            throws BuildFailedException {
        // save lead engine's rl, and rld
        RouteLocation rl = leadEngine.getRouteLocation();
        RouteLocation rld = leadEngine.getRouteDestination();
        removeEngineFromTrain(leadEngine);
        _engineList.add(0, leadEngine); // put engine back into the pool
        if (hpNeeded < 50) {
            hpNeeded = 50; // the minimum HP
        }
        int hpMax = hpNeeded;
        // largest single engine HP known today is less than 15,000.
        // high end modern diesel locos approximately 5000 HP.
        // 100 car train at 100 tons per car and 2 HPT requires 20,000 HP.
        // will assign consisted engines to train.
        boolean foundLoco = false;
        List<Engine> rejectedLocos = new ArrayList<>();
        hpLoop: while (hpMax < 20000) {
            hpMax += hpNeeded / 2; // start off looking for an engine with no
                                   // more than 50% extra HP
            log.debug("Max hp {}", hpMax);
            for (Engine engine : _engineList) {
                if (rejectedLocos.contains(engine)) {
                    continue;
                }
                // don't use non lead locos in a consist
                if (engine.getConsist() != null && !engine.isLead()) {
                    continue;
                }
                if (engine.getLocation() != rl.getLocation()) {
                    continue;
                }
                if (!model.equals(Train.NONE) && !engine.getModel().equals(model)) {
                    continue;
                }
                if (!road.equals(Train.NONE) && !engine.getRoadName().equals(road) ||
                        road.equals(Train.NONE) && !_train.isLocoRoadNameAccepted(engine.getRoadName())) {
                    continue;
                }
                int engineHp = engine.getHpInteger();
                if (engine.getConsist() != null) {
                    for (Engine e : engine.getConsist().getEngines()) {
                        if (e != engine) {
                            engineHp = engineHp + e.getHpInteger();
                        }
                    }
                }
                if (engineHp > hpNeeded && engineHp <= hpMax) {
                    addLine(_buildReport, FIVE,
                            Bundle.getMessage("buildLocoHasRequiredHp", engine.toString(), engineHp, hpNeeded));
                    if (setEngineDestination(engine, rl, rld)) {
                        foundLoco = true;
                        break hpLoop;
                    } else {
                        rejectedLocos.add(engine);
                    }
                }
            }
        }
        if (!foundLoco && !_train.isBuildConsistEnabled()) {
            throw new BuildFailedException(Bundle.getMessage("buildErrorEngHp", rl.getLocation().getName()));
        }
    }

    /**
     * Sets the destination track for an engine and assigns it to the train.
     *
     * @param engine The engine to be added to train
     * @param rl     Departure route location
     * @param rld    Destination route location
     * @return true if destination track found and set
     */
    protected boolean setEngineDestination(Engine engine, RouteLocation rl, RouteLocation rld) {
        // engine to staging?
        if (rld == _train.getTrainTerminatesRouteLocation() && _terminateStageTrack != null) {
            String status = engine.checkDestination(_terminateStageTrack.getLocation(), _terminateStageTrack);
            if (status.equals(Track.OKAY)) {
                addEngineToTrain(engine, rl, rld, _terminateStageTrack);
                return true; // done
            } else {
                addLine(_buildReport, SEVEN,
                        Bundle.getMessage("buildCanNotDropEngineToTrack", engine.toString(),
                                _terminateStageTrack.getTrackTypeName(),
                                _terminateStageTrack.getLocation().getName(), _terminateStageTrack.getName(), status));
            }
        } else {
            // find a destination track for this engine
            Location destination = rld.getLocation();
            List<Track> destTracks = destination.getTracksByMoves(null);
            if (destTracks.size() == 0) {
                addLine(_buildReport, THREE, Bundle.getMessage("buildNoTracksAtDestination", rld.getName()));
            }
            for (Track track : destTracks) {
                if (!checkDropTrainDirection(engine, rld, track)) {
                    continue;
                }
                if (!checkTrainCanDrop(engine, track)) {
                    continue;
                }
                String status = engine.checkDestination(destination, track);
                if (status.equals(Track.OKAY)) {
                    addLine(_buildReport, FIVE,
                            Bundle.getMessage("buildEngineCanDrop", engine.toString(),
                                    track.getTrackTypeName(),
                                    track.getLocation().getName(), track.getName()));
                    addEngineToTrain(engine, rl, rld, track);
                    return true;
                } else {
                    addLine(_buildReport, SEVEN,
                            Bundle.getMessage("buildCanNotDropEngineToTrack", engine.toString(),
                                    track.getTrackTypeName(),
                                    track.getLocation().getName(), track.getName(), status));
                }
            }
            addLine(_buildReport, FIVE,
                    Bundle.getMessage("buildCanNotDropEngToDest", engine.toString(), rld.getName()));
        }
        return false; // not able to set loco's destination
    }
    
    /**
    * Adds an engine to the train.
    *
    * @param engine the engine being added to the train
    * @param rl     where in the train's route to pick up the engine
    * @param rld    where in the train's route to set out the engine
    * @param track  the destination track for this engine
    */
   private void addEngineToTrain(Engine engine, RouteLocation rl, RouteLocation rld, Track track) {
       _lastEngine = engine; // needed in case there's a engine change in the
                             // train's route
       engine = checkQuickServiceArrival(engine, rld, track);
       if (_train.getLeadEngine() == null) {
           _train.setLeadEngine(engine); // load lead engine
       }
       addLine(_buildReport, ONE, Bundle.getMessage("buildEngineAssigned", engine.toString(), rl.getName(),
               rld.getName(), track.getName()));
       engine.setDestination(track.getLocation(), track, Engine.FORCE);
       int length = engine.getTotalLength();
       int weightTons = engine.getAdjustedWeightTons();
       // engine in consist?
       if (engine.getConsist() != null) {
           length = engine.getConsist().getTotalLength();
           weightTons = engine.getConsist().getAdjustedWeightTons();
           for (Engine cEngine : engine.getConsist().getEngines()) {
               if (cEngine != engine) {
                   addLine(_buildReport, ONE, Bundle.getMessage("buildEngineAssigned", cEngine.toString(),
                           rl.getName(), rld.getName(), track.getName()));
                   cEngine.setTrain(_train);
                   cEngine.setRouteLocation(rl);
                   cEngine.setRouteDestination(rld);
                   cEngine.setDestination(track.getLocation(), track, RollingStock.FORCE); // force
               }
           }
       }
       // now adjust train length and weight for each location that engines are
       // in the train
       finishAddRsToTrain(engine, rl, rld, length, weightTons);
   }
   
   /**
    * Checks to see if track is requesting a quick service. Since it isn't
    * possible for a engine to be pulled and set out twice, this code creates a
    * "clone" engine to create the requested Manifest. A engine could have multiple
    * clones, therefore each clone has a creation order number. The first clone
    * is used to restore a engine's location and load in the case of reset.
    * 
    * @param engine   the engine possibly needing quick service
    * @param track the destination track
    * @return the engine if not quick service, or a clone if quick service
    */
   private Engine checkQuickServiceArrival(Engine engine, RouteLocation rld, Track track) {
       if (!track.isQuickServiceEnabled()) {
           return engine;
       }
       addLine(_buildReport, FIVE,
               Bundle.getMessage("buildTrackQuickService", StringUtils.capitalize(track.getTrackTypeName()),
                       track.getLocation().getName(), track.getName()));
       // quick service enabled, create clones
       Engine cloneEng = engineManager.createClone(engine, track, _train, _startTime);
       // for timing, use arrival times for the train that is building
       // other trains will use their departure time, loaded when creating the Manifest
       String expectedArrivalTime = _train.getExpectedArrivalTime(rld, true);
       cloneEng.setSetoutTime(expectedArrivalTime);
       // remember where in the route the car was delivered
       engine.setRouteDestination(rld);
       return cloneEng; // return clone
   }
       

    /**
     * Checks to see if additional engines are needed for the train based on the
     * train's calculated tonnage. Minimum speed for the train is fixed at 36
     * MPH. The formula HPT x 12 / % Grade = Speed, is used to determine the
     * horsepower needed. For example a 1% grade requires a minimum of 3 HPT.
     * 
     * Ignored when departing staging
     *
     * @throws BuildFailedException if build failure
     */
    protected void checkNumnberOfEnginesNeededHPT() throws BuildFailedException {
        if (!_train.isBuildConsistEnabled() ||
                Setup.getHorsePowerPerTon() == 0) {
            return;
        }
        addLine(_buildReport, ONE, BLANK_LINE);
        addLine(_buildReport, ONE, Bundle.getMessage("buildDetermineNeeds", Setup.getHorsePowerPerTon()));
        Route route = _train.getRoute();
        int hpAvailable = 0;
        int extraHpNeeded = 0;
        RouteLocation rlNeedHp = null;
        RouteLocation rlStart = _train.getTrainDepartsRouteLocation();
        RouteLocation rlEnd = _train.getTrainTerminatesRouteLocation();
        boolean departingStaging = _train.isDepartingStaging();
        if (route != null) {
            boolean helper = false;
            for (RouteLocation rl : route.getLocationsBySequenceList()) {
                if ((_train.getSecondLegOptions() == Train.HELPER_ENGINES &&
                        rl == _train.getSecondLegStartRouteLocation()) ||
                        (_train.getThirdLegOptions() == Train.HELPER_ENGINES &&
                                rl == _train.getThirdLegStartRouteLocation())) {
                    addLine(_buildReport, FIVE, Bundle.getMessage("AddHelpersAt", rl.getName()));
                    helper = true;
                }
                if ((_train.getSecondLegOptions() == Train.HELPER_ENGINES &&
                        rl == _train.getSecondLegEndRouteLocation()) ||
                        (_train.getThirdLegOptions() == Train.HELPER_ENGINES &&
                                rl == _train.getThirdLegEndRouteLocation())) {
                    addLine(_buildReport, FIVE,
                            Bundle.getMessage("RemoveHelpersAt", rl.getName()));
                    helper = false;
                }
                if (helper) {
                    continue;
                }
                // check for a change of engines in the train's route
                if (((_train.getSecondLegOptions() & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES &&
                        rl == _train.getSecondLegStartRouteLocation()) ||
                        ((_train.getThirdLegOptions() & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES &&
                                rl == _train.getThirdLegStartRouteLocation())) {
                    log.debug("Loco change at ({})", rl.getName());
                    addEnginesBasedHPT(hpAvailable, extraHpNeeded, rlNeedHp, rlStart, rl);
                    addLine(_buildReport, THREE, BLANK_LINE);
                    // reset for next leg of train's route
                    rlStart = rl;
                    rlNeedHp = null;
                    extraHpNeeded = 0;
                    departingStaging = false;
                }
                if (departingStaging) {
                    continue;
                }
                double weight = rl.getTrainWeight();
                if (weight > 0) {
                    double hptMinimum = Setup.getHorsePowerPerTon();
                    double hptGrade = (Control.speedHpt * rl.getGrade() / 12);
                    double hp = _train.getTrainHorsePower(rl);
                    double hpt = hp / weight;
                    if (hptGrade > hptMinimum) {
                        hptMinimum = hptGrade;
                    }
                    if (hptMinimum > hpt) {
                        int addHp = (int) (hptMinimum * weight - hp);
                        if (addHp > extraHpNeeded) {
                            hpAvailable = (int) hp;
                            extraHpNeeded = addHp;
                            rlNeedHp = rl;
                        }
                        addLine(_buildReport, SEVEN,
                                Bundle.getMessage("buildAddLocosStatus", weight, hp, Control.speedHpt, rl.getGrade(),
                                hpt, hptMinimum, rl.getName(), rl.getId()));
                        addLine(_buildReport, FIVE,
                                Bundle.getMessage("buildTrainRequiresAddHp", addHp, rl.getName(), hptMinimum));
                    }
                }
            }
        }
        addEnginesBasedHPT(hpAvailable, extraHpNeeded, rlNeedHp, rlStart, rlEnd);
        addLine(_buildReport, SEVEN, Bundle.getMessage("buildDoneAssingEnginesTrain", _train.getName()));
        addLine(_buildReport, THREE, BLANK_LINE);
    }
    
    protected void removeEngineFromTrain(Engine engine) {
        // replace lead engine?
        if (_train.getLeadEngine() == engine) {
            _train.setLeadEngine(null);
        }
        if (engine.getConsist() != null) {
            for (Engine e : engine.getConsist().getEngines()) {
                removeRollingStockFromTrain(e);
            }
        } else {
            removeRollingStockFromTrain(engine);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(TrainBuilderEngines.class);
}
