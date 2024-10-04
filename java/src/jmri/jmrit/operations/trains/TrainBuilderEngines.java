package jmri.jmrit.operations.trains;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;

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

    private final static Logger log = LoggerFactory.getLogger(TrainBuilderEngines.class);
}
