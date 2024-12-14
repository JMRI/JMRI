package jmri.jmrit.operations.trains;

import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.locations.schedules.ScheduleItem;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarLoad;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.router.Router;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Setup;

/**
 * Contains methods for cars when building a train.
 * 
 * @author Daniel Boudreau Copyright (C) 2022
 */
public class TrainBuilderCars extends TrainBuilderEngines {

    /**
     * Find a caboose if needed at the correct location and add it to the train.
     * If departing staging, all cabooses are added to the train. If there isn't
     * a road name required for the caboose, tries to find a caboose with the
     * same road name as the lead engine.
     *
     * @param roadCaboose     Optional road name for this car.
     * @param leadEngine      The lead engine for this train. Used to find a
     *                        caboose with the same road name as the engine.
     * @param rl              Where in the route to pick up this car.
     * @param rld             Where in the route to set out this car.
     * @param requiresCaboose When true, the train requires a caboose.
     * @throws BuildFailedException If car not found.
     */
    protected void getCaboose(String roadCaboose, Engine leadEngine, RouteLocation rl, RouteLocation rld,
            boolean requiresCaboose) throws BuildFailedException {
        // code check
        if (rl == null) {
            throw new BuildFailedException(Bundle.getMessage("buildErrorCabooseNoLocation", _train.getName()));
        }
        // code check
        if (rld == null) {
            throw new BuildFailedException(
                    Bundle.getMessage("buildErrorCabooseNoDestination", _train.getName(), rl.getName()));
        }
        // load departure track if staging
        Track departTrack = null;
        if (rl == _train.getTrainDepartsRouteLocation()) {
            departTrack = _departStageTrack; // can be null
        }
        if (!requiresCaboose) {
            addLine(_buildReport, FIVE,
                    Bundle.getMessage("buildTrainNoCaboose", rl.getName()));
            if (departTrack == null) {
                return;
            }
        } else {
            addLine(_buildReport, ONE, Bundle.getMessage("buildTrainReqCaboose", _train.getName(), roadCaboose,
                    rl.getName(), rld.getName()));
        }

        // Now go through the car list looking for cabooses
        boolean cabooseTip = true; // add a user tip to the build report about
                                   // cabooses if none found
        boolean cabooseAtDeparture = false; // set to true if caboose at
                                            // departure location is found
        boolean foundCaboose = false;
        for (_carIndex = 0; _carIndex < _carList.size(); _carIndex++) {
            Car car = _carList.get(_carIndex);
            if (!car.isCaboose()) {
                continue;
            }
            showCarServiceOrder(car);

            cabooseTip = false; // found at least one caboose, so they exist!
            addLine(_buildReport, SEVEN, Bundle.getMessage("buildCarIsCaboose", car.toString(), car.getRoadName(),
                    car.getLocationName(), car.getTrackName()));
            // car departing staging must leave with train
            if (car.getTrack() == departTrack) {
                foundCaboose = false;
                if (!generateCarLoadFromStaging(car, rld)) {
                    // departing and terminating into staging?
                    if (car.getTrack().isAddCustomLoadsAnyStagingTrackEnabled() &&
                            rld.getLocation() == _terminateLocation &&
                            _terminateStageTrack != null) {
                        // try and generate a custom load for this caboose
                        generateLoadCarDepartingAndTerminatingIntoStaging(car, _terminateStageTrack);
                    }
                }
                if (checkAndAddCarForDestinationAndTrack(car, rl, rld)) {
                    if (car.getTrain() == _train) {
                        foundCaboose = true;
                    }
                } else if (findDestinationAndTrack(car, rl, rld)) {
                    foundCaboose = true;
                }
                if (!foundCaboose) {
                    throw new BuildFailedException(Bundle.getMessage("buildErrorCarStageDest", car.toString()));
                }
                // is there a specific road requirement for the caboose?
            } else if (!roadCaboose.equals(Train.NONE) && !roadCaboose.equals(car.getRoadName())) {
                continue;
            } else if (!foundCaboose && car.getLocationName().equals(rl.getName())) {
                // remove cars that can't be picked up due to train and track
                // directions
                if (!checkPickUpTrainDirection(car, rl)) {
                    addLine(_buildReport, SEVEN,
                            Bundle.getMessage("buildExcludeCarTypeAtLoc", car.toString(), car.getTypeName(),
                                    car.getTypeExtensions(), car.getLocationName(), car.getTrackName()));
                    _carList.remove(car); // remove this car from the list
                    _carIndex--;
                    continue;
                }
                // first pass, find a caboose that matches the engine road
                if (leadEngine != null && car.getRoadName().equals(leadEngine.getRoadName())) {
                    addLine(_buildReport, SEVEN, Bundle.getMessage("buildCabooseRoadMatches", car.toString(),
                            car.getRoadName(), leadEngine.toString()));
                    if (checkAndAddCarForDestinationAndTrack(car, rl, rld)) {
                        if (car.getTrain() == _train) {
                            foundCaboose = true;
                        }
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
                if (foundCaboose && departTrack == null) {
                    break;
                }
            }
        }
        // second pass, take a caboose with a road name that is "similar"
        // (hyphen feature) to the engine road name
        if (requiresCaboose && !foundCaboose && roadCaboose.equals(Train.NONE)) {
            log.debug("Second pass looking for caboose");
            for (Car car : _carList) {
                if (car.isCaboose() && car.getLocationName().equals(rl.getName())) {
                    if (leadEngine != null &&
                            TrainCommon.splitString(car.getRoadName())
                                    .equals(TrainCommon.splitString(leadEngine.getRoadName()))) {
                        addLine(_buildReport, SEVEN, Bundle.getMessage("buildCabooseRoadMatches", car.toString(),
                                car.getRoadName(), leadEngine.toString()));
                        if (checkAndAddCarForDestinationAndTrack(car, rl, rld)) {
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
        }
        // third pass, take any caboose unless a caboose road name is specified
        if (requiresCaboose && !foundCaboose) {
            log.debug("Third pass looking for caboose");
            for (Car car : _carList) {
                if (!car.isCaboose()) {
                    continue;
                }
                if (car.getLocationName().equals(rl.getName())) {
                    // is there a specific road requirement for the caboose?
                    if (!roadCaboose.equals(Train.NONE) && !roadCaboose.equals(car.getRoadName())) {
                        continue; // yes
                    }
                    // okay, we found a caboose at the departure location
                    cabooseAtDeparture = true;
                    if (checkAndAddCarForDestinationAndTrack(car, rl, rld)) {
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
            if (!cabooseAtDeparture) {
                throw new BuildFailedException(Bundle.getMessage("buildErrorReqDepature", _train.getName(),
                        Bundle.getMessage("Caboose").toLowerCase(), rl.getName()));
            }
            // we did find a caboose at departure that meet requirements, but
            // couldn't place it at destination.
            throw new BuildFailedException(Bundle.getMessage("buildErrorReqDest", _train.getName(),
                    Bundle.getMessage("Caboose"), rld.getName()));
        }
    }

    /**
     * Find a car with FRED if needed at the correct location and adds the car
     * to the train. If departing staging, will make sure all cars with FRED are
     * added to the train.
     *
     * @param road Optional road name for this car.
     * @param rl   Where in the route to pick up this car.
     * @param rld  Where in the route to set out this car.
     * @throws BuildFailedException If car not found.
     */
    protected void getCarWithFred(String road, RouteLocation rl, RouteLocation rld) throws BuildFailedException {
        // load departure track if staging
        Track departTrack = null;
        if (rl == _train.getTrainDepartsRouteLocation()) {
            departTrack = _departStageTrack;
        }
        boolean foundCarWithFred = false;
        if (_train.isFredNeeded()) {
            addLine(_buildReport, ONE,
                    Bundle.getMessage("buildTrainReqFred", _train.getName(), road, rl.getName(), rld.getName()));
        } else {
            addLine(_buildReport, FIVE, Bundle.getMessage("buildTrainNoFred"));
            // if not departing staging we're done
            if (departTrack == null) {
                return;
            }
        }
        for (_carIndex = 0; _carIndex < _carList.size(); _carIndex++) {
            Car car = _carList.get(_carIndex);
            if (!car.hasFred()) {
                continue;
            }
            showCarServiceOrder(car);
            addLine(_buildReport, SEVEN,
                    Bundle.getMessage("buildCarHasFRED", car.toString(), car.getRoadName(), car.getLocationName(),
                            car.getTrackName()));
            // all cars with FRED departing staging must leave with train
            if (car.getTrack() == departTrack) {
                foundCarWithFred = false;
                if (!generateCarLoadFromStaging(car, rld)) {
                    // departing and terminating into staging?
                    if (car.getTrack().isAddCustomLoadsAnyStagingTrackEnabled() &&
                            rld.getLocation() == _terminateLocation &&
                            _terminateStageTrack != null) {
                        // try and generate a custom load for this car with FRED
                        generateLoadCarDepartingAndTerminatingIntoStaging(car, _terminateStageTrack);
                    }
                }
                if (checkAndAddCarForDestinationAndTrack(car, rl, rld)) {
                    if (car.getTrain() == _train) {
                        foundCarWithFred = true;
                    }
                } else if (findDestinationAndTrack(car, rl, rld)) {
                    foundCarWithFred = true;
                }
                if (!foundCarWithFred) {
                    throw new BuildFailedException(Bundle.getMessage("buildErrorCarStageDest", car.toString()));
                }
            } // is there a specific road requirement for the car with FRED?
            else if (!road.equals(Train.NONE) && !road.equals(car.getRoadName())) {
                addLine(_buildReport, SEVEN, Bundle.getMessage("buildExcludeCarWrongRoad", car.toString(),
                        car.getLocationName(), car.getTrackName(), car.getTypeName(), car.getRoadName()));
                _carList.remove(car); // remove this car from the list
                _carIndex--;
                continue;
            } else if (!foundCarWithFred && car.getLocationName().equals(rl.getName())) {
                // remove cars that can't be picked up due to train and track
                // directions
                if (!checkPickUpTrainDirection(car, rl)) {
                    addLine(_buildReport, SEVEN, Bundle.getMessage("buildExcludeCarTypeAtLoc", car.toString(),
                            car.getTypeName(), car.getTypeExtensions(), car.getLocationName(), car.getTrackName()));
                    _carList.remove(car); // remove this car from the list
                    _carIndex--;
                    continue;
                }
                if (checkAndAddCarForDestinationAndTrack(car, rl, rld)) {
                    if (car.getTrain() == _train) {
                        foundCarWithFred = true;
                    }
                } else if (findDestinationAndTrack(car, rl, rld)) {
                    foundCarWithFred = true;
                }
                if (foundCarWithFred && departTrack == null) {
                    break;
                }
            }
        }
        if (_train.isFredNeeded() && !foundCarWithFred) {
            throw new BuildFailedException(Bundle.getMessage("buildErrorRequirements", _train.getName(),
                    Bundle.getMessage("FRED"), rl.getName(), rld.getName()));
        }
    }

    /**
     * Determine if caboose or car with FRED was given a destination and track.
     * Need to check if there's been a train assignment.
     * 
     * @param car the car in question
     * @param rl  car's route location
     * @param rld car's route location destination
     * @return true if car has a destination. Need to check if there's been a
     *         train assignment.
     * @throws BuildFailedException if destination was staging and can't place
     *                              car there
     */
    private boolean checkAndAddCarForDestinationAndTrack(Car car, RouteLocation rl, RouteLocation rld)
            throws BuildFailedException {
        return checkCarForDestination(car, rl, _routeList.indexOf(rld));
    }

    /**
     * Optionally block cars departing staging. No guarantee that cars departing
     * staging can be blocked by destination. By using the pick up location id,
     * this routine tries to find destinations that are willing to accepts all
     * of the cars that were "blocked" together when they were picked up. Rules:
     * The route must allow set outs at the destination. The route must allow
     * the correct number of set outs. The destination must accept all cars in
     * the pick up block.
     *
     * @throws BuildFailedException if blocking fails
     */
    protected void blockCarsFromStaging() throws BuildFailedException {
        if (_departStageTrack == null || !_departStageTrack.isBlockCarsEnabled()) {
            return;
        }

        addLine(_buildReport, THREE, BLANK_LINE);
        addLine(_buildReport, THREE,
                Bundle.getMessage("blockDepartureHasBlocks", _departStageTrack.getName(), _numOfBlocks.size()));

        Enumeration<String> en = _numOfBlocks.keys();
        while (en.hasMoreElements()) {
            String locId = en.nextElement();
            int numCars = _numOfBlocks.get(locId);
            String locName = "";
            Location l = locationManager.getLocationById(locId);
            if (l != null) {
                locName = l.getName();
            }
            addLine(_buildReport, SEVEN, Bundle.getMessage("blockFromHasCars", locId, locName, numCars));
            if (_numOfBlocks.size() < 2) {
                addLine(_buildReport, SEVEN, Bundle.getMessage("blockUnable"));
                return;
            }
        }
        blockCarsByLocationMoves();
        addLine(_buildReport, SEVEN, Bundle.getMessage("blockDone", _departStageTrack.getName()));
    }

    /**
     * Blocks cars out of staging by assigning the largest blocks of cars to
     * locations requesting the most moves.
     * 
     * @throws BuildFailedException
     */
    private void blockCarsByLocationMoves() throws BuildFailedException {
        List<RouteLocation> blockRouteList = _train.getRoute().getLocationsBySequenceList();
        for (RouteLocation rl : blockRouteList) {
            // start at the second location in the route to begin blocking
            if (rl == _train.getTrainDepartsRouteLocation()) {
                continue;
            }
            int possibleMoves = rl.getMaxCarMoves() - rl.getCarMoves();
            if (rl.isDropAllowed() && possibleMoves > 0) {
                addLine(_buildReport, SEVEN, Bundle.getMessage("blockLocationHasMoves", rl.getName(), possibleMoves));
            }
        }
        // now block out cars, send the largest block of cars to the locations
        // requesting the greatest number of moves
        while (true) {
            String blockId = getLargestBlock(); // get the id of the largest
                                                // block of cars
            if (blockId.isEmpty() || _numOfBlocks.get(blockId) == 1) {
                break; // done
            }
            // get the remaining location with the greatest number of moves
            RouteLocation rld = getLocationWithMaximumMoves(blockRouteList, blockId);
            if (rld == null) {
                break; // done
            }
            // check to see if there are enough moves for all of the cars
            // departing staging
            if (rld.getMaxCarMoves() > _numOfBlocks.get(blockId)) {
                // remove the largest block and maximum moves RouteLocation from
                // the lists
                _numOfBlocks.remove(blockId);
                // block 0 cars have never left staging.
                if (blockId.equals(Car.LOCATION_UNKNOWN)) {
                    continue;
                }
                blockRouteList.remove(rld);
                Location loc = locationManager.getLocationById(blockId);
                Location setOutLoc = rld.getLocation();
                if (loc != null && setOutLoc != null && checkDropTrainDirection(rld)) {
                    for (_carIndex = 0; _carIndex < _carList.size(); _carIndex++) {
                        Car car = _carList.get(_carIndex);
                        if (car.getTrack() == _departStageTrack && car.getLastLocationId().equals(blockId)) {
                            if (car.getDestination() != null) {
                                addLine(_buildReport, SEVEN, Bundle.getMessage("blockNotAbleDest", car.toString(),
                                        car.getDestinationName()));
                                continue; // can't block this car
                            }
                            if (car.getFinalDestination() != null) {
                                addLine(_buildReport, SEVEN,
                                        Bundle.getMessage("blockNotAbleFinalDest", car.toString(),
                                                car.getFinalDestination().getName()));
                                continue; // can't block this car
                            }
                            if (!car.getLoadName().equals(carLoads.getDefaultEmptyName()) &&
                                    !car.getLoadName().equals(carLoads.getDefaultLoadName())) {
                                addLine(_buildReport, SEVEN,
                                        Bundle.getMessage("blockNotAbleCustomLoad", car.toString(), car.getLoadName()));
                                continue; // can't block this car
                            }
                            if (car.getLoadName().equals(carLoads.getDefaultEmptyName()) &&
                                    (_departStageTrack.isAddCustomLoadsEnabled() ||
                                            _departStageTrack.isAddCustomLoadsAnySpurEnabled() ||
                                            _departStageTrack.isAddCustomLoadsAnyStagingTrackEnabled())) {
                                addLine(_buildReport, SEVEN,
                                        Bundle.getMessage("blockNotAbleCarTypeGenerate", car.toString(),
                                                car.getLoadName()));
                                continue; // can't block this car
                            }
                            addLine(_buildReport, SEVEN,
                                    Bundle.getMessage("blockingCar", car.toString(), loc.getName(), rld.getName()));
                            if (!findDestinationAndTrack(car, _train.getTrainDepartsRouteLocation(), rld)) {
                                addLine(_buildReport, SEVEN,
                                        Bundle.getMessage("blockNotAbleCarType", car.toString(), rld.getName(),
                                                car.getTypeName()));
                            }
                        }
                    }
                }
            } else {
                addLine(_buildReport, SEVEN, Bundle.getMessage("blockDestNotEnoughMoves", rld.getName(), blockId));
                // block is too large for any stop along this train's route
                _numOfBlocks.remove(blockId);
            }
        }
    }

    /**
     * Attempts to find a destinations for cars departing a specific route
     * location.
     *
     * @param rl           The route location where cars need destinations.
     * @param isSecondPass When true this is the second time we've looked at
     *                     these cars. Used to perform local moves.
     * @throws BuildFailedException if failure
     */
    protected void findDestinationsForCarsFromLocation(RouteLocation rl, boolean isSecondPass)
            throws BuildFailedException {
        if (_reqNumOfMoves <= 0) {
            return;
        }
        boolean messageFlag = true;
        boolean foundCar = false;
        for (_carIndex = 0; _carIndex < _carList.size(); _carIndex++) {
            Car car = _carList.get(_carIndex);
            // second pass deals with cars that have a final destination equal
            // to this location.
            // therefore a local move can be made. This causes "off spots" to be
            // serviced.
            if (isSecondPass && !car.getFinalDestinationName().equals(rl.getName())) {
                continue;
            }
            // find a car at this location
            if (!car.getLocationName().equals(rl.getName())) {
                continue;
            }
            foundCar = true;
            // add message that we're on the second pass for this location
            if (isSecondPass && messageFlag) {
                messageFlag = false;
                addLine(_buildReport, FIVE, Bundle.getMessage("buildExtraPassForLocation", rl.getName()));
                addLine(_buildReport, SEVEN, BLANK_LINE);
            }
            // can this car be picked up?
            if (!checkPickUpTrainDirection(car, rl)) {
                addLine(_buildReport, FIVE, BLANK_LINE);
                continue; // no
            }

            showCarServiceOrder(car); // car on FIFO or LIFO track?

            // is car departing staging and generate custom load?
            if (!generateCarLoadFromStaging(car)) {
                if (!generateCarLoadStagingToStaging(car) &&
                        car.getTrack() == _departStageTrack &&
                        !_departStageTrack.isLoadNameAndCarTypeShipped(car.getLoadName(), car.getTypeName())) {
                    // report build failure car departing staging with a
                    // restricted load
                    addLine(_buildReport, ONE, Bundle.getMessage("buildErrorCarStageLoad", car.toString(),
                            car.getLoadName(), _departStageTrack.getName()));
                    addLine(_buildReport, FIVE, BLANK_LINE);
                    continue; // keep going and see if there are other cars with
                              // issues outs of staging
                }
            }
            // If car been given a home division follow division rules for car
            // movement.
            if (!findDestinationsForCarsWithHomeDivision(car)) {
                addLine(_buildReport, FIVE,
                        Bundle.getMessage("buildNoDestForCar", car.toString()));
                addLine(_buildReport, FIVE, BLANK_LINE);
                continue; // hold car at current location
            }
            // does car have a custom load without a destination?
            // if departing staging, a destination for this car is needed, so
            // keep going
            if (findFinalDestinationForCarLoad(car) &&
                    car.getDestination() == null &&
                    car.getTrack() != _departStageTrack) {
                // done with this car, it has a custom load, and there are
                // spurs/schedules, but no destination found
                addLine(_buildReport, FIVE,
                        Bundle.getMessage("buildNoDestForCar", car.toString()));
                addLine(_buildReport, FIVE, BLANK_LINE);
                continue;
            }
            // Check car for final destination, then an assigned destination, if
            // neither, find a destination for the car
            if (checkCarForFinalDestination(car)) {
                log.debug("Car ({}) has a final desination that can't be serviced by train", car.toString());
            } else if (checkCarForDestination(car, rl, _routeList.indexOf(rl))) {
                // car had a destination, could have been added to the train.
                log.debug("Car ({}) has desination ({}) using train ({})", car.toString(), car.getDestinationName(),
                        car.getTrainName());
            } else {
                findDestinationAndTrack(car, rl, _routeList.indexOf(rl), _routeList.size());
            }
            if (_reqNumOfMoves <= 0) {
                break; // done
            }
            // build failure if car departing staging without a destination and
            // a train we'll just put out a warning message here so we can find
            // out how many cars have issues
            if (car.getTrack() == _departStageTrack &&
                    (car.getDestination() == null || car.getDestinationTrack() == null || car.getTrain() == null)) {
                addLine(_buildReport, ONE, Bundle.getMessage("buildWarningCarStageDest", car.toString()));
                // does the car have a final destination to staging? If so we
                // need to reset this car
                if (car.getFinalDestinationTrack() != null && car.getFinalDestinationTrack() == _terminateStageTrack) {
                    addLine(_buildReport, THREE,
                            Bundle.getMessage("buildStagingCarHasFinal", car.toString(), car.getFinalDestinationName(),
                                    car.getFinalDestinationTrackName()));
                    car.reset();
                }
                addLine(_buildReport, SEVEN, BLANK_LINE);
            }
        }
        if (!foundCar && !isSecondPass) {
            addLine(_buildReport, FIVE, Bundle.getMessage("buildNoCarsAtLocation", rl.getName()));
            addLine(_buildReport, FIVE, BLANK_LINE);
        }
    }

    private boolean generateCarLoadFromStaging(Car car) throws BuildFailedException {
        return generateCarLoadFromStaging(car, null);
    }

    /**
     * Used to generate a car's load from staging. Search for a spur with a
     * schedule and load car if possible.
     *
     * @param car the car
     * @param rld The route location destination for this car. Can be null.
     * @return true if car given a custom load
     * @throws BuildFailedException If code check fails
     */
    private boolean generateCarLoadFromStaging(Car car, RouteLocation rld) throws BuildFailedException {
        // Code Check, car should have a track assignment
        if (car.getTrack() == null) {
            throw new BuildFailedException(
                    Bundle.getMessage("buildWarningRsNoTrack", car.toString(), car.getLocationName()));
        }
        if (!car.getTrack().isStaging() ||
                (!car.getTrack().isAddCustomLoadsAnySpurEnabled() && !car.getTrack().isAddCustomLoadsEnabled()) ||
                !car.getLoadName().equals(carLoads.getDefaultEmptyName()) ||
                car.getDestination() != null ||
                car.getFinalDestination() != null) {
            log.debug(
                    "No load generation for car ({}) isAddLoadsAnySpurEnabled: {}, car load ({}) destination ({}) final destination ({})",
                    car.toString(), car.getTrack().isAddCustomLoadsAnySpurEnabled() ? "true" : "false",
                    car.getLoadName(), car.getDestinationName(), car.getFinalDestinationName());
            // if car has a destination or final destination add "no load
            // generated" message to report
            if (car.getTrack().isStaging() &&
                    car.getTrack().isAddCustomLoadsAnySpurEnabled() &&
                    car.getLoadName().equals(carLoads.getDefaultEmptyName())) {
                addLine(_buildReport, FIVE,
                        Bundle.getMessage("buildCarNoLoadGenerated", car.toString(), car.getLoadName(),
                                car.getDestinationName(), car.getFinalDestinationName()));
            }
            return false; // no load generated for this car
        }
        addLine(_buildReport, FIVE,
                Bundle.getMessage("buildSearchTrackNewLoad", car.toString(), car.getTypeName(),
                        car.getLoadType().toLowerCase(), car.getLoadName(), car.getLocationName(), car.getTrackName(),
                        rld != null ? rld.getLocation().getName() : ""));
        // check to see if car type has custom loads
        if (carLoads.getNames(car.getTypeName()).size() == 2) {
            addLine(_buildReport, SEVEN, Bundle.getMessage("buildCarNoCustomLoad", car.toString(), car.getTypeName()));
            return false;
        }
        if (car.getKernel() != null) {
            addLine(_buildReport, SEVEN,
                    Bundle.getMessage("buildCarLeadKernel", car.toString(), car.getKernelName(),
                            car.getKernel().getSize(), car.getKernel().getTotalLength(),
                            Setup.getLengthUnit().toLowerCase()));
        }
        // save the car's load, should be the default empty
        String oldCarLoad = car.getLoadName();
        List<Track> tracks = locationManager.getTracksByMoves(Track.SPUR);
        log.debug("Found {} spurs", tracks.size());
        // show locations not serviced by departure track once
        List<Location> locationsNotServiced = new ArrayList<>();
        for (Track track : tracks) {
            if (locationsNotServiced.contains(track.getLocation())) {
                continue;
            }
            if (rld != null && track.getLocation() != rld.getLocation()) {
                locationsNotServiced.add(track.getLocation());
                continue;
            }
            if (!car.getTrack().isDestinationAccepted(track.getLocation())) {
                addLine(_buildReport, SEVEN, Bundle.getMessage("buildDestinationNotServiced",
                        track.getLocation().getName(), car.getTrackName()));
                locationsNotServiced.add(track.getLocation());
                continue;
            }
            // only use tracks serviced by this train?
            if (car.getTrack().isAddCustomLoadsEnabled() &&
                    !_train.getRoute().isLocationNameInRoute(track.getLocation().getName())) {
                continue;
            }
            // only the first match in a schedule is used for a spur
            ScheduleItem si = getScheduleItem(car, track);
            if (si == null) {
                continue; // no match
            }
            // need to set car load so testDestination will work properly
            car.setLoadName(si.getReceiveLoadName());
            car.setScheduleItemId(si.getId());
            String status = car.checkDestination(track.getLocation(), track);
            if (!status.equals(Track.OKAY) && !status.startsWith(Track.LENGTH)) {
                addLine(_buildReport, SEVEN,
                        Bundle.getMessage("buildNoDestTrackNewLoad", StringUtils.capitalize(track.getTrackTypeName()),
                                track.getLocation().getName(), track.getName(), car.toString(), si.getReceiveLoadName(),
                                status));
                continue;
            }
            addLine(_buildReport, SEVEN, Bundle.getMessage("buildTrySpurLoad", track.getLocation().getName(),
                    track.getName(), car.getLoadName()));
            // does the car have a home division?
            if (car.getDivision() != null) {
                addLine(_buildReport, SEVEN,
                        Bundle.getMessage("buildCarHasDivisionStaging", car.toString(), car.getTypeName(),
                                car.getLoadType().toLowerCase(), car.getLoadName(), car.getDivisionName(),
                                car.getLocationName(), car.getTrackName(), car.getTrack().getDivisionName()));
                // load type empty must return to car's home division
                // or load type load from foreign division must return to car's
                // home division
                if (car.getLoadType().equals(CarLoad.LOAD_TYPE_EMPTY) && car.getDivision() != track.getDivision() ||
                        car.getLoadType().equals(CarLoad.LOAD_TYPE_LOAD) &&
                                car.getTrack().getDivision() != car.getDivision() &&
                                car.getDivision() != track.getDivision()) {
                    addLine(_buildReport, SEVEN,
                            Bundle.getMessage("buildNoDivisionTrack", track.getTrackTypeName(),
                                    track.getLocation().getName(), track.getName(), track.getDivisionName(),
                                    car.toString(), car.getLoadType().toLowerCase(), car.getLoadName()));
                    continue;
                }
            }
            if (!track.isSpaceAvailable(car)) {
                addLine(_buildReport, SEVEN,
                        Bundle.getMessage("buildNoDestTrackSpace", car.toString(), track.getLocation().getName(),
                                track.getName(), track.getNumberOfCarsInRoute(), track.getReservedInRoute(),
                                Setup.getLengthUnit().toLowerCase(), track.getReservationFactor()));
                continue;
            }
            // try routing car
            car.setFinalDestination(track.getLocation());
            car.setFinalDestinationTrack(track);
            if (router.setDestination(car, _train, _buildReport) && car.getDestination() != null) {
                // return car with this custom load and destination
                addLine(_buildReport, FIVE,
                        Bundle.getMessage("buildCreateNewLoadForCar", car.toString(), si.getReceiveLoadName(),
                                track.getLocation().getName(), track.getName()));
                car.setLoadGeneratedFromStaging(true);
                // is car part of kernel?
                car.updateKernel();
                track.bumpMoves();
                track.bumpSchedule();
                return true; // done, car now has a custom load
            }
            addLine(_buildReport, SEVEN, Bundle.getMessage("buildCanNotRouteCar", car.toString(),
                    si.getReceiveLoadName(), track.getLocation().getName(), track.getName()));
            addLine(_buildReport, SEVEN, BLANK_LINE);
            car.setDestination(null, null);
            car.setFinalDestination(null);
            car.setFinalDestinationTrack(null);
        }
        // restore car's load
        car.setLoadName(oldCarLoad);
        car.setScheduleItemId(Car.NONE);
        addLine(_buildReport, FIVE, Bundle.getMessage("buildUnableNewLoad", car.toString()));
        return false; // done, no load generated for this car
    }

    /**
     * Tries to place a custom load in the car that is departing staging and
     * attempts to find a destination for the car that is also staging.
     *
     * @param car the car
     * @return True if custom load added to car
     * @throws BuildFailedException If code check fails
     */
    private boolean generateCarLoadStagingToStaging(Car car) throws BuildFailedException {
        // Code Check, car should have a track assignment
        if (car.getTrack() == null) {
            throw new BuildFailedException(
                    Bundle.getMessage("buildWarningRsNoTrack", car.toString(), car.getLocationName()));
        }
        if (!car.getTrack().isStaging() ||
                !car.getTrack().isAddCustomLoadsAnyStagingTrackEnabled() ||
                !car.getLoadName().equals(carLoads.getDefaultEmptyName()) ||
                car.getDestination() != null ||
                car.getFinalDestination() != null) {
            log.debug(
                    "No load generation for car ({}) isAddCustomLoadsAnyStagingTrackEnabled: {}, car load ({}) destination ({}) final destination ({})",
                    car.toString(), car.getTrack().isAddCustomLoadsAnyStagingTrackEnabled() ? "true" : "false",
                    car.getLoadName(), car.getDestinationName(), car.getFinalDestinationName());
            return false;
        }
        // check to see if car type has custom loads
        if (carLoads.getNames(car.getTypeName()).size() == 2) {
            return false;
        }
        List<Track> tracks = locationManager.getTracks(Track.STAGING);
        addLine(_buildReport, FIVE, Bundle.getMessage("buildTryStagingToStaging", car.toString(), tracks.size()));
        if (Setup.getRouterBuildReportLevel().equals(Setup.BUILD_REPORT_VERY_DETAILED)) {
            for (Track track : tracks) {
                addLine(_buildReport, SEVEN,
                        Bundle.getMessage("buildStagingLocationTrack", track.getLocation().getName(), track.getName()));
            }
        }
        // list of locations that can't be reached by the router
        List<Location> locationsNotServiced = new ArrayList<>();
        if (_terminateStageTrack != null) {
            addLine(_buildReport, SEVEN,
                    Bundle.getMessage("buildIgnoreStagingFirstPass", _terminateStageTrack.getLocation().getName()));
            locationsNotServiced.add(_terminateStageTrack.getLocation());
        }
        while (tracks.size() > 0) {
            // pick a track randomly
            int rnd = (int) (Math.random() * tracks.size());
            Track track = tracks.get(rnd);
            tracks.remove(track);
            log.debug("Try staging track ({}, {})", track.getLocation().getName(), track.getName());
            // find a staging track that isn't at the departure
            if (track.getLocation() == _departLocation) {
                log.debug("Can't use departure location ({})", track.getLocation().getName());
                continue;
            }
            if (!_train.isAllowThroughCarsEnabled() && track.getLocation() == _terminateLocation) {
                log.debug("Through cars to location ({}) not allowed", track.getLocation().getName());
                continue;
            }
            if (locationsNotServiced.contains(track.getLocation())) {
                log.debug("Location ({}) not reachable", track.getLocation().getName());
                continue;
            }
            if (!car.getTrack().isDestinationAccepted(track.getLocation())) {
                addLine(_buildReport, SEVEN, Bundle.getMessage("buildDestinationNotServiced",
                        track.getLocation().getName(), car.getTrackName()));
                locationsNotServiced.add(track.getLocation());
                continue;
            }
            // the following method sets the Car load generated from staging
            // boolean
            if (generateLoadCarDepartingAndTerminatingIntoStaging(car, track)) {
                // test to see if destination is reachable by this train
                if (router.setDestination(car, _train, _buildReport) && car.getDestination() != null) {
                    return true; // done, car has a custom load and a final
                                 // destination
                }
                addLine(_buildReport, SEVEN, Bundle.getMessage("buildStagingTrackNotReachable",
                        track.getLocation().getName(), track.getName(), car.getLoadName()));
                // return car to original state
                car.setLoadName(carLoads.getDefaultEmptyName());
                car.setLoadGeneratedFromStaging(false);
                car.setFinalDestination(null);
                car.updateKernel();
                // couldn't route to this staging location
                locationsNotServiced.add(track.getLocation());
            }
        }
        // No staging tracks reachable, try the track the train is terminating
        // to
        if (_train.isAllowThroughCarsEnabled() &&
                _terminateStageTrack != null &&
                car.getTrack().isDestinationAccepted(_terminateStageTrack.getLocation()) &&
                generateLoadCarDepartingAndTerminatingIntoStaging(car, _terminateStageTrack)) {
            return true;
        }

        addLine(_buildReport, SEVEN,
                Bundle.getMessage("buildNoStagingForCarCustom", car.toString()));
        addLine(_buildReport, SEVEN, BLANK_LINE);
        return false;
    }

    /**
     * Check to see if car has been assigned a home division. If car has a home
     * division the following rules are applied when assigning the car a
     * destination:
     * <p>
     * If car load is type empty not at car's home division yard: Car is sent to
     * a home division yard. If home division yard not available, then car is
     * sent to home division staging, then spur (industry).
     * <p>
     * If car load is type empty at a yard at the car's home division: Car is
     * sent to a home division spur, then home division staging.
     * <p>
     * If car load is type load not at car's home division: Car is sent to home
     * division spur, and if spur not available then home division staging.
     * <p>
     * If car load is type load at car's home division: Car is sent to any
     * division spur or staging.
     * 
     * @param car the car being checked for a home division
     * @return false if destination track not found for this car
     * @throws BuildFailedException
     */
    private boolean findDestinationsForCarsWithHomeDivision(Car car) throws BuildFailedException {
        if (car.getDivision() == null || car.getDestination() != null || car.getFinalDestination() != null) {
            return true;
        }
        if (car.getDivision() == car.getTrack().getDivision()) {
            addLine(_buildReport, FIVE,
                    Bundle.getMessage("buildCarDepartHomeDivision", car.toString(), car.getTypeName(),
                            car.getLoadType().toLowerCase(),
                            car.getLoadName(), car.getDivisionName(), car.getTrack().getTrackTypeName(),
                            car.getLocationName(), car.getTrackName(),
                            car.getTrack().getDivisionName()));
        } else {
            addLine(_buildReport, FIVE,
                    Bundle.getMessage("buildCarDepartForeignDivision", car.toString(), car.getTypeName(),
                            car.getLoadType().toLowerCase(),
                            car.getLoadName(), car.getDivisionName(), car.getTrack().getTrackTypeName(),
                            car.getLocationName(), car.getTrackName(),
                            car.getTrack().getDivisionName()));
        }
        if (car.getKernel() != null) {
            addLine(_buildReport, SEVEN,
                    Bundle.getMessage("buildCarLeadKernel", car.toString(), car.getKernelName(),
                            car.getKernel().getSize(),
                            car.getKernel().getTotalLength(), Setup.getLengthUnit().toLowerCase()));
        }
        // does train terminate into staging?
        if (_terminateStageTrack != null) {
            log.debug("Train terminates into staging track ({})", _terminateStageTrack.getName());
            // bias cars to staging
            if (car.getLoadType().equals(CarLoad.LOAD_TYPE_EMPTY)) {
                log.debug("Car ({}) has home division ({}) and load type empty", car.toString(), car.getDivisionName());
                if (car.getTrack().isYard() && car.getTrack().getDivision() == car.getDivision()) {
                    log.debug("Car ({}) at it's home division yard", car.toString());
                    if (!sendCarToHomeDivisionTrack(car, Track.STAGING, HOME_DIVISION)) {
                        return sendCarToHomeDivisionTrack(car, Track.SPUR, HOME_DIVISION);
                    }
                }
                // try to send to home division staging, then home division yard,
                // then home division spur
                else if (!sendCarToHomeDivisionTrack(car, Track.STAGING, HOME_DIVISION)) {
                    if (!sendCarToHomeDivisionTrack(car, Track.YARD, HOME_DIVISION)) {
                        return sendCarToHomeDivisionTrack(car, Track.SPUR, HOME_DIVISION);
                    }
                }
            } else {
                log.debug("Car ({}) has home division ({}) and load type load", car.toString(), car.getDivisionName());
                // 1st send car to staging dependent of shipping track division, then
                // try spur
                if (!sendCarToHomeDivisionTrack(car, Track.STAGING, car.getTrack().getDivision() != car.getDivision())) {
                    return sendCarToHomeDivisionTrack(car, Track.SPUR,
                            car.getTrack().getDivision() != car.getDivision());
                }
            }
        } else {
            // train doesn't terminate into staging
            if (car.getLoadType().equals(CarLoad.LOAD_TYPE_EMPTY)) {
                log.debug("Car ({}) has home division ({}) and load type empty", car.toString(), car.getDivisionName());
                if (car.getTrack().isYard() && car.getTrack().getDivision() == car.getDivision()) {
                    log.debug("Car ({}) at it's home division yard", car.toString());
                    if (!sendCarToHomeDivisionTrack(car, Track.SPUR, HOME_DIVISION)) {
                        return sendCarToHomeDivisionTrack(car, Track.STAGING, HOME_DIVISION);
                    }
                }
                // try to send to home division yard, then home division staging,
                // then home division spur
                else if (!sendCarToHomeDivisionTrack(car, Track.YARD, HOME_DIVISION)) {
                    if (!sendCarToHomeDivisionTrack(car, Track.STAGING, HOME_DIVISION)) {
                        return sendCarToHomeDivisionTrack(car, Track.SPUR, HOME_DIVISION);
                    }
                }
            } else {
                log.debug("Car ({}) has home division ({}) and load type load", car.toString(), car.getDivisionName());
                // 1st send car to spur dependent of shipping track division, then
                // try staging
                if (!sendCarToHomeDivisionTrack(car, Track.SPUR, car.getTrack().getDivision() != car.getDivision())) {
                    return sendCarToHomeDivisionTrack(car, Track.STAGING,
                            car.getTrack().getDivision() != car.getDivision());
                }
            }
        }
        return true;
    }

    private static final boolean HOME_DIVISION = true;

    /**
     * Tries to set a final destination for the car with a home division.
     * 
     * @param car           the car
     * @param trackType     One of three track types: Track.SPUR Track.YARD or
     *                      Track.STAGING
     * @param home_division If true track's division must match the car's
     * @return true if car was given a final destination
     */
    private boolean sendCarToHomeDivisionTrack(Car car, String trackType, boolean home_division) {
        // locations not reachable
        List<Location> locationsNotServiced = new ArrayList<>();
        List<Track> tracks = locationManager.getTracksByMoves(trackType);
        log.debug("Found {} {} tracks", tracks.size(), trackType);
        for (Track track : tracks) {
            if (home_division && car.getDivision() != track.getDivision()) {
                addLine(_buildReport, SEVEN,
                        Bundle.getMessage("buildNoDivisionTrack", track.getTrackTypeName(),
                                track.getLocation().getName(), track.getName(), track.getDivisionName(), car.toString(),
                                car.getLoadType().toLowerCase(),
                                car.getLoadName()));
                continue;
            }
            if (locationsNotServiced.contains(track.getLocation())) {
                continue;
            }
            if (!car.getTrack().isDestinationAccepted(track.getLocation())) {
                addLine(_buildReport, SEVEN, Bundle.getMessage("buildDestinationNotServiced",
                        track.getLocation().getName(), car.getTrackName()));
                // location not reachable
                locationsNotServiced.add(track.getLocation());
                continue;
            }
            // only use the termination staging track for this train
            if (trackType.equals(Track.STAGING) &&
                    _terminateStageTrack != null &&
                    track.getLocation() == _terminateLocation &&
                    track != _terminateStageTrack) {
                continue;
            }
            if (trackType.equals(Track.SPUR)) {
                if (sendCarToDestinationSpur(car, track)) {
                    return true;
                }
            } else {
                if (sendCarToDestinationTrack(car, track)) {
                    return true;
                }
            }
        }
        addLine(_buildReport, FIVE,
                Bundle.getMessage("buildCouldNotFindTrack", trackType.toLowerCase(), car.toString(),
                        car.getLoadType().toLowerCase(), car.getLoadName()));
        addLine(_buildReport, SEVEN, BLANK_LINE);
        return false;
    }

    /**
     * Set the final destination and track for a car with a custom load. Car
     * must not have a destination or final destination. There's a check to see
     * if there's a spur/schedule for this car. Returns true if a schedule was
     * found. Will hold car at current location if any of the spurs checked has
     * the the option to "Hold cars with custom loads" enabled and the spur has
     * an alternate track assigned. Tries to sent the car to staging if there
     * aren't any spurs with schedules available.
     *
     * @param car the car with the load
     * @return true if there's a schedule that can be routed to for this car and
     *         load
     * @throws BuildFailedException
     */
    private boolean findFinalDestinationForCarLoad(Car car) throws BuildFailedException {
        if (car.getLoadName().equals(carLoads.getDefaultEmptyName()) ||
                car.getLoadName().equals(carLoads.getDefaultLoadName()) ||
                car.getDestination() != null ||
                car.getFinalDestination() != null) {
            return false; // car doesn't have a custom load, or already has a
                          // destination set
        }
        addLine(_buildReport, FIVE,
                Bundle.getMessage("buildSearchForSpur", car.toString(), car.getTypeName(),
                        car.getLoadType().toLowerCase(), car.getLoadName(), car.getTrackType(), car.getLocationName(),
                        car.getTrackName()));
        if (car.getKernel() != null) {
            addLine(_buildReport, SEVEN,
                    Bundle.getMessage("buildCarLeadKernel", car.toString(), car.getKernelName(),
                            car.getKernel().getSize(), car.getKernel().getTotalLength(),
                            Setup.getLengthUnit().toLowerCase()));
        }
        _routeToTrackFound = false;
        List<Track> tracks = locationManager.getTracksByMoves(Track.SPUR);
        log.debug("Found {} spurs", tracks.size());
        // locations not reachable
        List<Location> locationsNotServiced = new ArrayList<>();
        for (Track track : tracks) {
            if (car.getTrack() == track) {
                continue;
            }
            if (track.getSchedule() == null) {
                addLine(_buildReport, SEVEN, Bundle.getMessage("buildSpurNoSchedule",
                        track.getLocation().getName(), track.getName()));
                continue;
            }
            if (locationsNotServiced.contains(track.getLocation())) {
                continue;
            }
            if (!car.getTrack().isDestinationAccepted(track.getLocation())) {
                addLine(_buildReport, SEVEN, Bundle.getMessage("buildDestinationNotServiced",
                        track.getLocation().getName(), car.getTrackName()));
                // location not reachable
                locationsNotServiced.add(track.getLocation());
                continue;
            }
            if (sendCarToDestinationSpur(car, track)) {
                return true;
            }
        }
        addLine(_buildReport, SEVEN, Bundle.getMessage("buildCouldNotFindTrack", Track.getTrackTypeName(Track.SPUR).toLowerCase(),
                car.toString(), car.getLoadType().toLowerCase(), car.getLoadName()));
        if (_routeToTrackFound &&
                !_train.isSendCarsWithCustomLoadsToStagingEnabled() &&
                !car.getLocation().isStaging()) {
            addLine(_buildReport, SEVEN, Bundle.getMessage("buildHoldCarValidRoute", car.toString(),
                    car.getLocationName(), car.getTrackName()));
        } else {
            // try and send car to staging
            addLine(_buildReport, FIVE,
                    Bundle.getMessage("buildTrySendCarToStaging", car.toString(), car.getLoadName()));
            tracks = locationManager.getTracks(Track.STAGING);
            log.debug("Found {} staging tracks", tracks.size());
            while (tracks.size() > 0) {
                // pick a track randomly
                int rnd = (int) (Math.random() * tracks.size());
                Track track = tracks.get(rnd);
                tracks.remove(track);
                log.debug("Staging track ({}, {})", track.getLocation().getName(), track.getName());
                if (track.getLocation() == car.getLocation()) {
                    continue;
                }
                if (locationsNotServiced.contains(track.getLocation())) {
                    continue;
                }
                if (_terminateStageTrack != null &&
                        track.getLocation() == _terminateLocation &&
                        track != _terminateStageTrack) {
                    continue; // ignore other staging tracks at terminus
                }
                if (!car.getTrack().isDestinationAccepted(track.getLocation())) {
                    addLine(_buildReport, SEVEN, Bundle.getMessage("buildDestinationNotServiced",
                            track.getLocation().getName(), car.getTrackName()));
                    locationsNotServiced.add(track.getLocation());
                    continue;
                }
                String status = track.isRollingStockAccepted(car);
                if (!status.equals(Track.OKAY) && !status.startsWith(Track.LENGTH)) {
                    log.debug("Staging track ({}) can't accept car ({})", track.getName(), car.toString());
                    continue;
                }
                addLine(_buildReport, SEVEN, Bundle.getMessage("buildStagingCanAcceptLoad", track.getLocation(),
                        track.getName(), car.getLoadName()));
                // try to send car to staging
                car.setFinalDestination(track.getLocation());
                // test to see if destination is reachable by this train
                if (router.setDestination(car, _train, _buildReport)) {
                    _routeToTrackFound = true; // found a route to staging
                }
                if (car.getDestination() != null) {
                    car.updateKernel(); // car part of kernel?
                    return true;
                }
                // couldn't route to this staging location
                locationsNotServiced.add(track.getLocation());
                car.setFinalDestination(null);
            }
            addLine(_buildReport, SEVEN,
                    Bundle.getMessage("buildNoStagingForCarLoad", car.toString(), car.getLoadName()));
        }
        log.debug("routeToSpurFound is {}", _routeToTrackFound);
        return _routeToTrackFound; // done
    }

    boolean _routeToTrackFound;

    /**
     * Used to determine if spur can accept car. Also will set routeToTrackFound
     * to true if there's a valid route available to the spur being tested. Sets
     * car's final destination to track if okay.
     * 
     * @param car   the car
     * @param track the spur
     * @return false if there's an issue with using the spur
     */
    private boolean sendCarToDestinationSpur(Car car, Track track) {
        if (!checkBasicMoves(car, track)) {
            addLine(_buildReport, SEVEN, Bundle.getMessage("trainCanNotDeliverToDestination", _train.getName(),
                    car.toString(), track.getLocation().getName(), track.getName()));
            return false;
        }
        String status = car.checkDestination(track.getLocation(), track);
        if (!status.equals(Track.OKAY)) {
            if (track.getScheduleMode() == Track.SEQUENTIAL && status.startsWith(Track.SCHEDULE)) {
                addLine(_buildReport, SEVEN, Bundle.getMessage("buildTrackSequentialMode",
                        track.getLocation().getName(), track.getName(), status));
            }
            // if the track has an alternate track don't abort if the issue was
            // space
            if (!status.startsWith(Track.LENGTH)) {
                addLine(_buildReport, SEVEN,
                        Bundle.getMessage("buildNoDestTrackNewLoad", StringUtils.capitalize(track.getTrackTypeName()),
                                track.getLocation().getName(), track.getName(), car.toString(), car.getLoadName(),
                                status));
                return false;
            }
            String scheduleStatus = track.checkSchedule(car);
            if (!scheduleStatus.equals(Track.OKAY)) {
                addLine(_buildReport, SEVEN,
                        Bundle.getMessage("buildNoDestTrackNewLoad", StringUtils.capitalize(track.getTrackTypeName()),
                                track.getLocation().getName(), track.getName(), car.toString(), car.getLoadName(),
                                scheduleStatus));
                return false;
            }
            if (track.getAlternateTrack() == null) {
                // report that the spur is full and no alternate
                addLine(_buildReport, SEVEN,
                        Bundle.getMessage("buildSpurFullNoAlternate", track.getLocation().getName(), track.getName()));
                return false;
            } else {
                addLine(_buildReport, SEVEN,
                        Bundle.getMessage("buildTrackFullHasAlternate", track.getLocation().getName(), track.getName(),
                                track.getAlternateTrack().getName()));
                // check to see if alternate and track are configured properly
                if (!_train.isLocalSwitcher() &&
                        (track.getTrainDirections() & track.getAlternateTrack().getTrainDirections()) == 0) {
                    addLine(_buildReport, SEVEN, Bundle.getMessage("buildCanNotDropRsUsingTrain4", track.getName(),
                            formatStringToCommaSeparated(Setup.getDirectionStrings(track.getTrainDirections())),
                            track.getAlternateTrack().getName(), formatStringToCommaSeparated(
                                    Setup.getDirectionStrings(track.getAlternateTrack().getTrainDirections()))));
                    return false;
                }
            }
        }
        addLine(_buildReport, SEVEN, BLANK_LINE);
        addLine(_buildReport, SEVEN,
                Bundle.getMessage("buildSetFinalDestDiv", track.getTrackTypeName(), track.getLocation().getName(),
                        track.getName(), track.getDivisionName(), car.toString(), car.getLoadType().toLowerCase(),
                        car.getLoadName()));

        // show if track is requesting cars with custom loads to only go to
        // spurs
        if (track.isHoldCarsWithCustomLoadsEnabled()) {
            addLine(_buildReport, SEVEN,
                    Bundle.getMessage("buildHoldCarsCustom", track.getLocation().getName(), track.getName()));
        }
        // check the number of in bound cars to this track
        if (!track.isSpaceAvailable(car)) {
            // Now determine if we should move the car or just leave it
            if (track.isHoldCarsWithCustomLoadsEnabled()) {
                // determine if this car can be routed to the spur
                String id = track.getScheduleItemId();
                if (router.isCarRouteable(car, _train, track, _buildReport)) {
                    // hold car if able to route to track
                    _routeToTrackFound = true;
                } else {
                    addLine(_buildReport, SEVEN, Bundle.getMessage("buildRouteNotFound", car.toString(),
                            car.getFinalDestinationName(), car.getFinalDestinationTrackName()));
                }
                track.setScheduleItemId(id); // restore id
            }
            if (car.getTrack().isStaging()) {
                addLine(_buildReport, SEVEN,
                        Bundle.getMessage("buildNoDestTrackSpace", car.toString(), track.getLocation().getName(),
                                track.getName(), track.getNumberOfCarsInRoute(), track.getReservedInRoute(),
                                Setup.getLengthUnit().toLowerCase(), track.getReservationFactor()));
            } else {
                addLine(_buildReport, SEVEN,
                        Bundle.getMessage("buildNoDestSpace", car.toString(), track.getTrackTypeName(),
                                track.getLocation().getName(), track.getName(), track.getNumberOfCarsInRoute(),
                                track.getReservedInRoute(), Setup.getLengthUnit().toLowerCase()));
            }
            return false;
        }
        // try to send car to this spur
        car.setFinalDestination(track.getLocation());
        car.setFinalDestinationTrack(track);
        // test to see if destination is reachable by this train
        if (router.setDestination(car, _train, _buildReport) && track.isHoldCarsWithCustomLoadsEnabled()) {
            _routeToTrackFound = true; // if we don't find another spur, don't
                                       // move car
        }
        if (car.getDestination() == null) {
            if (!router.getStatus().equals(Track.OKAY)) {
                addLine(_buildReport, SEVEN,
                        Bundle.getMessage("buildNotAbleToSetDestination", car.toString(), router.getStatus()));
            }
            car.setFinalDestination(null);
            car.setFinalDestinationTrack(null);
            // don't move car if another train can
            if (router.getStatus().startsWith(Router.STATUS_NOT_THIS_TRAIN_PREFIX)) {
                _routeToTrackFound = true;
            }
            return false;
        }
        if (car.getDestinationTrack() != track) {
            track.bumpMoves();
            // car is being routed to this track
            if (track.getSchedule() != null) {
                car.setScheduleItemId(track.getCurrentScheduleItem().getId());
                track.bumpSchedule();
            }
        }
        car.updateKernel();
        return true; // done, car has a new destination
    }

    /**
     * Destination track can be division yard or staging, NOT a spur.
     * 
     * @param car   the car
     * @param track the car's destination track
     * @return true if car given a new final destination
     */
    private boolean sendCarToDestinationTrack(Car car, Track track) {
        if (!checkBasicMoves(car, track)) {
            addLine(_buildReport, SEVEN, Bundle.getMessage("trainCanNotDeliverToDestination", _train.getName(),
                    car.toString(), track.getLocation().getName(), track.getName()));
            return false;
        }
        String status = car.checkDestination(track.getLocation(), track);
        if (!status.equals(Track.OKAY)) {
            addLine(_buildReport, SEVEN,
                    Bundle.getMessage("buildNoDestTrackNewLoad", StringUtils.capitalize(track.getTrackTypeName()),
                            track.getLocation().getName(), track.getName(), car.toString(), car.getLoadName(), status));
            return false;
        }
        if (!track.isSpaceAvailable(car)) {
            addLine(_buildReport, SEVEN,
                    Bundle.getMessage("buildNoDestSpace", car.toString(), track.getTrackTypeName(),
                            track.getLocation().getName(), track.getName(), track.getNumberOfCarsInRoute(),
                            track.getReservedInRoute(), Setup.getLengthUnit().toLowerCase()));
            return false;
        }
        // try to send car to this division track
        addLine(_buildReport, SEVEN,
                Bundle.getMessage("buildSetFinalDestDiv", track.getTrackTypeName(), track.getLocation().getName(),
                        track.getName(), track.getDivisionName(), car.toString(), car.getLoadType().toLowerCase(),
                        car.getLoadName()));
        car.setFinalDestination(track.getLocation());
        car.setFinalDestinationTrack(track);
        // test to see if destination is reachable by this train
        if (router.setDestination(car, _train, _buildReport)) {
            log.debug("Can route car to destination ({}, {})", track.getLocation().getName(), track.getName());
        }
        if (car.getDestination() == null) {
            addLine(_buildReport, SEVEN,
                    Bundle.getMessage("buildNotAbleToSetDestination", car.toString(), router.getStatus()));
            car.setFinalDestination(null);
            car.setFinalDestinationTrack(null);
            return false;
        }
        car.updateKernel();
        return true; // done, car has a new final destination
    }

    /**
     * Checks for a car's final destination, and then after checking, tries to
     * route the car to that destination. Normal return from this routine is
     * false, with the car returning with a set destination. Returns true if car
     * has a final destination, but can't be used for this train.
     *
     * @param car
     * @return false if car needs destination processing (normal).
     */
    private boolean checkCarForFinalDestination(Car car) {
        if (car.getFinalDestination() == null || car.getDestination() != null) {
            return false;
        }

        addLine(_buildReport, FIVE,
                Bundle.getMessage("buildCarRoutingBegins", car.toString(), car.getTypeName(),
                        car.getLoadType().toLowerCase(), car.getLoadName(), car.getTrackType(), car.getLocationName(),
                        car.getTrackName(), car.getFinalDestinationName(), car.getFinalDestinationTrackName()));

        // no local moves for this train?
        if (!_train.isLocalSwitcher() && !_train.isAllowLocalMovesEnabled() &&
                car.getSplitLocationName().equals(car.getSplitFinalDestinationName()) &&
                car.getTrack() != _departStageTrack) {
            addLine(_buildReport, FIVE,
                    Bundle.getMessage("buildCarHasFinalDestNoMove", car.toString(), car.getLocationName(),
                            car.getFinalDestinationName(), _train.getName()));
            addLine(_buildReport, FIVE, BLANK_LINE);
            log.debug("Removing car ({}) from list", car.toString());
            _carList.remove(car);
            _carIndex--;
            return true; // car has a final destination, but no local moves by
                         // this train
        }
        // is the car's destination the terminal and is that allowed?
        if (!checkThroughCarsAllowed(car, car.getFinalDestinationName())) {
            // don't remove car from list if departing staging
            if (car.getTrack() == _departStageTrack) {
                addLine(_buildReport, ONE, Bundle.getMessage("buildErrorCarStageDest", car.toString()));
            } else {
                log.debug("Removing car ({}) from list", car.toString());
                _carList.remove(car);
                _carIndex--;
            }
            return true; // car has a final destination, but through traffic not
                         // allowed by this train
        }
        // does the car have a final destination track that is willing to
        // service the car?
        // note the default mode for all track types is MATCH
        if (car.getFinalDestinationTrack() != null && car.getFinalDestinationTrack().getScheduleMode() == Track.MATCH) {
            String status = car.checkDestination(car.getFinalDestination(), car.getFinalDestinationTrack());
            // keep going if the only issue was track length and the track
            // accepts the car's load
            if (!status.equals(Track.OKAY) &&
                    !status.startsWith(Track.LENGTH) &&
                    !(status.contains(Track.CUSTOM) && status.contains(Track.LOAD))) {
                addLine(_buildReport, SEVEN,
                        Bundle.getMessage("buildNoDestTrackNewLoad",
                                StringUtils.capitalize(car.getFinalDestinationTrack().getTrackTypeName()),
                                car.getFinalDestination().getName(), car.getFinalDestinationTrack().getName(),
                                car.toString(), car.getLoadName(), status));
                // is this car or kernel being sent to a track that is too
                // short?
                if (status.startsWith(Track.CAPACITY)) {
                    // track is too short for this car or kernel
                    addLine(_buildReport, SEVEN,
                            Bundle.getMessage("buildTrackTooShort", car.getFinalDestination().getName(),
                                    car.getFinalDestinationTrack().getName(), car.toString()));
                }
                _warnings++;
                addLine(_buildReport, SEVEN,
                        Bundle.getMessage("buildWarningRemovingFinalDest", car.getFinalDestination().getName(),
                                car.getFinalDestinationTrack().getName(), car.toString()));
                car.setFinalDestination(null);
                car.setFinalDestinationTrack(null);
                return false; // car no longer has a final destination
            }
        }

        // now try and route the car
        if (!router.setDestination(car, _train, _buildReport)) {
            addLine(_buildReport, SEVEN,
                    Bundle.getMessage("buildNotAbleToSetDestination", car.toString(), router.getStatus()));
            // don't move car if routing issue was track space but not departing
            // staging
            if ((!router.getStatus().startsWith(Track.LENGTH) &&
                    !_train.isServiceAllCarsWithFinalDestinationsEnabled()) || (car.getTrack() == _departStageTrack)) {
                // add car to unable to route list
                if (!_notRoutable.contains(car)) {
                    _notRoutable.add(car);
                }
                addLine(_buildReport, FIVE, BLANK_LINE);
                addLine(_buildReport, FIVE,
                        Bundle.getMessage("buildWarningCarNotRoutable", car.toString(), car.getLocationName(),
                                car.getTrackName(), car.getFinalDestinationName(), car.getFinalDestinationTrackName()));
                addLine(_buildReport, FIVE, BLANK_LINE);
                return false; // move this car, routing failed!
            }
        } else {
            if (car.getDestination() != null) {
                return false; // routing successful process this car, normal
                              // exit from this routine
            }
            if (car.getTrack() == _departStageTrack) {
                log.debug("Car ({}) departing staging with final destination ({}) and no destination",
                        car.toString(), car.getFinalDestinationName());
                return false; // try and move this car out of staging
            }
        }
        addLine(_buildReport, FIVE, Bundle.getMessage("buildNoDestForCar", car.toString()));
        addLine(_buildReport, FIVE, BLANK_LINE);
        return true;
    }

    /**
     * Checks to see if car has a destination and tries to add car to train.
     * Will find a track for the car if needed. Returns false if car doesn't
     * have a destination.
     *
     * @param rl         the car's route location
     * @param routeIndex where in the route to start search
     * @return true if car has a destination. Need to check if car given a train
     *         assignment.
     * @throws BuildFailedException if destination was staging and can't place
     *                              car there
     */
    private boolean checkCarForDestination(Car car, RouteLocation rl, int routeIndex) throws BuildFailedException {
        if (car.getDestination() == null) {
            return false; // the only false return
        }
        addLine(_buildReport, SEVEN, Bundle.getMessage("buildCarHasAssignedDest", car.toString(), car.getLoadName(),
                car.getDestinationName(), car.getDestinationTrackName()));
        RouteLocation rld = _train.getRoute().getLastLocationByName(car.getDestinationName());
        if (rld == null) {
            // code check, router doesn't set a car's destination if not carried
            // by train being built. Car has a destination that isn't serviced
            // by this train. Find buildExcludeCarDestNotPartRoute in
            // loadRemoveAndListCars()
            throw new BuildFailedException(Bundle.getMessage("buildExcludeCarDestNotPartRoute", car.toString(),
                    car.getDestinationName(), car.getDestinationTrackName(), _train.getRoute().getName()));
        }
        // now go through the route and try and find a location with
        // the correct destination name
        for (int k = routeIndex; k < _routeList.size(); k++) {
            rld = _routeList.get(k);
            // if car can be picked up later at same location, skip
            if (checkForLaterPickUp(car, rl, rld)) {
                addLine(_buildReport, SEVEN, BLANK_LINE);
                return true;
            }
            if (!rld.getName().equals(car.getDestinationName())) {
                continue;
            }
            // is the car's destination the terminal and is that allowed?
            if (!checkThroughCarsAllowed(car, car.getDestinationName())) {
                return true;
            }
            log.debug("Car ({}) found a destination in train's route", car.toString());
            // are drops allows at this location?
            if (!rld.isDropAllowed()) {
                addLine(_buildReport, FIVE, Bundle.getMessage("buildRouteNoDropLocation", _train.getRoute().getName(),
                        rld.getId(), rld.getName()));
                continue;
            }
            if (_train.isLocationSkipped(rld.getId())) {
                addLine(_buildReport, FIVE,
                        Bundle.getMessage("buildLocSkipped", rld.getName(), rld.getId(), _train.getName()));
                continue;
            }
            // any moves left at this location?
            if (rld.getCarMoves() >= rld.getMaxCarMoves()) {
                addLine(_buildReport, FIVE,
                        Bundle.getMessage("buildNoAvailableMovesDest", rld.getCarMoves(), rld.getMaxCarMoves(),
                                _train.getRoute().getName(), rld.getId(), rld.getName()));
                continue;
            }
            // is the train length okay?
            if (!checkTrainLength(car, rl, rld)) {
                continue;
            }
            // check for valid destination track
            if (car.getDestinationTrack() == null) {
                addLine(_buildReport, FIVE, Bundle.getMessage("buildCarDoesNotHaveDest", car.toString()));
                // is car going into staging?
                if (rld == _train.getTrainTerminatesRouteLocation() && _terminateStageTrack != null) {
                    String status = car.checkDestination(car.getDestination(), _terminateStageTrack);
                    if (status.equals(Track.OKAY)) {
                        addLine(_buildReport, FIVE, Bundle.getMessage("buildCarAssignedToStaging", car.toString(),
                                _terminateStageTrack.getName()));
                        addCarToTrain(car, rl, rld, _terminateStageTrack);
                        return true;
                    } else {
                        addLine(_buildReport, SEVEN,
                                Bundle.getMessage("buildCanNotDropCarBecause", car.toString(),
                                        _terminateStageTrack.getTrackTypeName(),
                                        _terminateStageTrack.getLocation().getName(), _terminateStageTrack.getName(),
                                        status));
                        continue;
                    }
                } else {
                    // no staging at this location, now find a destination track
                    // for this car
                    List<Track> tracks = getTracksAtDestination(car, rld);
                    if (tracks.size() > 0) {
                        if (tracks.get(1) != null) {
                            car.setFinalDestination(car.getDestination());
                            car.setFinalDestinationTrack(tracks.get(1));
                            tracks.get(1).bumpMoves();
                        }
                        addCarToTrain(car, rl, rld, tracks.get(0));
                        return true;
                    }
                }
            } else {
                log.debug("Car ({}) has a destination track ({})", car.toString(), car.getDestinationTrack().getName());
                // going into the correct staging track?
                if (rld.equals(_train.getTrainTerminatesRouteLocation()) &&
                        _terminateStageTrack != null &&
                        _terminateStageTrack != car.getDestinationTrack()) {
                    // car going to wrong track in staging, change track
                    addLine(_buildReport, SEVEN, Bundle.getMessage("buildCarDestinationStaging", car.toString(),
                            car.getDestinationName(), car.getDestinationTrackName()));
                    car.setDestination(_terminateStageTrack.getLocation(), _terminateStageTrack);
                }
                if (!rld.equals(_train.getTrainTerminatesRouteLocation()) ||
                        _terminateStageTrack == null ||
                        _terminateStageTrack == car.getDestinationTrack()) {
                    // is train direction correct? and drop to interchange or
                    // spur?
                    if (checkDropTrainDirection(car, rld, car.getDestinationTrack()) &&
                            checkTrainCanDrop(car, car.getDestinationTrack())) {
                        String status = car.checkDestination(car.getDestination(), car.getDestinationTrack());
                        if (status.equals(Track.OKAY)) {
                            addCarToTrain(car, rl, rld, car.getDestinationTrack());
                            return true;
                        } else {
                            addLine(_buildReport, SEVEN,
                                    Bundle.getMessage("buildCanNotDropCarBecause", car.toString(),
                                            car.getDestinationTrack().getTrackTypeName(),
                                            car.getDestinationTrack().getLocation().getName(),
                                            car.getDestinationTrackName(), status));
                        }
                    }
                } else {
                    // code check
                    throw new BuildFailedException(Bundle.getMessage("buildCarDestinationStaging", car.toString(),
                            car.getDestinationName(), car.getDestinationTrackName()));
                }
            }
            addLine(_buildReport, FIVE,
                    Bundle.getMessage("buildCanNotDropCar", car.toString(), car.getDestinationName(), rld.getId()));
            if (car.getDestinationTrack() == null) {
                log.debug("Could not find a destination track for location ({})", car.getDestinationName());
            }
        }
        log.debug("car ({}) not added to train", car.toString());
        addLine(_buildReport, FIVE,
                Bundle.getMessage("buildDestinationNotReachable", car.getDestinationName(), rl.getName(), rl.getId()));
        // remove destination and revert to final destination
        if (car.getDestinationTrack() != null) {
            // going to remove this destination from car
            car.getDestinationTrack().setMoves(car.getDestinationTrack().getMoves() - 1);
            Track destTrack = car.getDestinationTrack();
            // TODO should we leave the car's destination? The spur expects this
            // car!
            if (destTrack.getSchedule() != null && destTrack.getScheduleMode() == Track.SEQUENTIAL) {
                addLine(_buildReport, SEVEN, Bundle.getMessage("buildPickupCancelled",
                        destTrack.getLocation().getName(), destTrack.getName()));
            }
        }
        car.setFinalDestination(car.getPreviousFinalDestination());
        car.setFinalDestinationTrack(car.getPreviousFinalDestinationTrack());
        car.setDestination(null, null);
        car.updateKernel();

        addLine(_buildReport, FIVE, Bundle.getMessage("buildNoDestForCar", car.toString()));
        addLine(_buildReport, FIVE, BLANK_LINE);
        return true; // car no longer has a destination, but it had one.
    }

    /**
     * Find a destination and track for a car at a route location.
     *
     * @param car the car!
     * @param rl  The car's route location
     * @param rld The car's route destination
     * @return true if successful.
     * @throws BuildFailedException if code check fails
     */
    private boolean findDestinationAndTrack(Car car, RouteLocation rl, RouteLocation rld) throws BuildFailedException {
        int index = _routeList.indexOf(rld);
        if (_train.isLocalSwitcher()) {
            return findDestinationAndTrack(car, rl, index, index + 1);
        }
        return findDestinationAndTrack(car, rl, index - 1, index + 1);
    }

    /**
     * Find a destination and track for a car, and add the car to the train.
     *
     * @param car        The car that is looking for a destination and
     *                   destination track.
     * @param rl         The route location for this car.
     * @param routeIndex Where in the train's route to begin a search for a
     *                   destination for this car.
     * @param routeEnd   Where to stop looking for a destination.
     * @return true if successful, car has destination, track and a train.
     * @throws BuildFailedException if code check fails
     */
    private boolean findDestinationAndTrack(Car car, RouteLocation rl, int routeIndex, int routeEnd)
            throws BuildFailedException {
        if (routeIndex + 1 == routeEnd) {
            log.debug("Car ({}) is at the last location in the train's route", car.toString());
        }
        addLine(_buildReport, FIVE, Bundle.getMessage("buildFindDestinationForCar", car.toString(), car.getTypeName(),
                car.getLoadType().toLowerCase(), car.getLoadName(), car.getTrackType(), car.getLocationName(),
                car.getTrackName()));
        if (car.getKernel() != null) {
            addLine(_buildReport, SEVEN, Bundle.getMessage("buildCarLeadKernel", car.toString(), car.getKernelName(),
                    car.getKernel().getSize(), car.getKernel().getTotalLength(), Setup.getLengthUnit().toLowerCase()));
        }

        // normally start looking after car's route location
        int start = routeIndex;
        // the route location destination being checked for the car
        RouteLocation rld = null;
        // holds the best route location destination for the car
        RouteLocation rldSave = null;
        // holds the best track at destination for the car
        Track trackSave = null;
        // used when a spur has an alternate track and no schedule
        Track finalDestinationTrackSave = null;
        // true when car can be picked up from two or more locations in the
        // route
        boolean multiplePickup = false;

        // more than one location in this route?
        if (!_train.isLocalSwitcher()) {
            start++; // begin looking for tracks at the next location
        }
        // all pick ups to terminal?
        if (_train.isSendCarsToTerminalEnabled() &&
                !rl.getSplitName().equals(_departLocation.getSplitName()) &&
                routeEnd == _routeList.size()) {
            addLine(_buildReport, FIVE, Bundle.getMessage("buildSendToTerminal", _terminateLocation.getName()));
            // user could have specified several terminal locations with the
            // "same" name
            start = routeEnd - 1;
            while (start > routeIndex) {
                if (!_routeList.get(start - 1).getSplitName()
                        .equals(_terminateLocation.getSplitName())) {
                    break;
                }
                start--;
            }
        }
        // now search for a destination for this car
        for (int k = start; k < routeEnd; k++) {
            rld = _routeList.get(k);
            // if car can be picked up later at same location, set flag
            if (checkForLaterPickUp(car, rl, rld)) {
                multiplePickup = true;
            }
            if (rld.isDropAllowed() || car.hasFred() || car.isCaboose()) {
                addLine(_buildReport, FIVE, Bundle.getMessage("buildSearchingLocation", rld.getName(), rld.getId()));
            } else {
                addLine(_buildReport, FIVE, Bundle.getMessage("buildRouteNoDropLocation", _train.getRoute().getName(),
                        rld.getId(), rld.getName()));
                continue;
            }
            if (_train.isLocationSkipped(rld.getId())) {
                addLine(_buildReport, FIVE,
                        Bundle.getMessage("buildLocSkipped", rld.getName(), rld.getId(), _train.getName()));
                continue;
            }
            // any moves left at this location?
            if (rld.getCarMoves() >= rld.getMaxCarMoves()) {
                addLine(_buildReport, FIVE,
                        Bundle.getMessage("buildNoAvailableMovesDest", rld.getCarMoves(), rld.getMaxCarMoves(),
                                _train.getRoute().getName(), rld.getId(), rld.getName()));
                continue;
            }
            // get the destination
            Location testDestination = rld.getLocation();
            // code check, all locations in the route have been already checked
            if (testDestination == null) {
                throw new BuildFailedException(
                        Bundle.getMessage("buildErrorRouteLoc", _train.getRoute().getName(), rld.getName()));
            }
            // don't move car to same location unless the train is a switcher
            // (local moves) or is passenger, caboose or car with FRED
            if (rl.getSplitName().equals(rld.getSplitName()) &&
                    !_train.isLocalSwitcher() &&
                    !car.isPassenger() &&
                    !car.isCaboose() &&
                    !car.hasFred()) {
                // allow cars to return to the same staging location if no other
                // options (tracks) are available
                if ((_train.isAllowReturnToStagingEnabled() || Setup.isStagingAllowReturnEnabled()) &&
                        testDestination.isStaging() &&
                        trackSave == null) {
                    addLine(_buildReport, SEVEN,
                            Bundle.getMessage("buildReturnCarToStaging", car.toString(), rld.getName()));
                } else {
                    addLine(_buildReport, SEVEN,
                            Bundle.getMessage("buildCarLocEqualDestination", car.toString(), rld.getName()));
                    continue;
                }
            }

            // check to see if departure track has any restrictions
            if (!car.getTrack().isDestinationAccepted(testDestination)) {
                addLine(_buildReport, SEVEN, Bundle.getMessage("buildDestinationNotServiced", testDestination.getName(),
                        car.getTrackName()));
                continue;
            }

            if (!testDestination.acceptsTypeName(car.getTypeName())) {
                addLine(_buildReport, SEVEN, Bundle.getMessage("buildCanNotDropLocation", car.toString(),
                        car.getTypeName(), testDestination.getName()));
                continue;
            }
            // can this location service this train's direction
            if (!checkDropTrainDirection(rld)) {
                continue;
            }
            // is the train length okay?
            if (!checkTrainLength(car, rl, rld)) {
                break; // no, done with this car
            }
            // is the car's destination the terminal and is that allowed?
            if (!checkThroughCarsAllowed(car, rld.getName())) {
                continue; // not allowed
            }

            Track trackTemp = null;
            // used when alternate track selected
            Track finalDestinationTrackTemp = null;

            // is there a track assigned for staging cars?
            if (rld == _train.getTrainTerminatesRouteLocation() && _terminateStageTrack != null) {
                trackTemp = tryStaging(car, rldSave);
                if (trackTemp == null) {
                    continue; // no
                }
            } else {
                // no staging, start track search
                List<Track> tracks = getTracksAtDestination(car, rld);
                if (tracks.size() > 0) {
                    trackTemp = tracks.get(0);
                    finalDestinationTrackTemp = tracks.get(1);
                }
            }
            // did we find a new destination?
            if (trackTemp == null) {
                addLine(_buildReport, FIVE,
                        Bundle.getMessage("buildCouldNotFindDestForCar", car.toString(), rld.getName()));
            } else {
                addLine(_buildReport, FIVE,
                        Bundle.getMessage("buildCarCanDropMoves", car.toString(), trackTemp.getTrackTypeName(),
                                trackTemp.getLocation().getName(), trackTemp.getName(), +rld.getCarMoves(),
                                rld.getMaxCarMoves()));
                if (multiplePickup) {
                    if (rldSave != null) {
                        addLine(_buildReport, FIVE,
                                Bundle.getMessage("buildTrackServicedLater", car.getLocationName(),
                                        trackTemp.getTrackTypeName(), trackTemp.getLocation().getName(),
                                        trackTemp.getName(), car.getLocationName()));
                    } else {
                        addLine(_buildReport, FIVE,
                                Bundle.getMessage("buildCarHasSecond", car.toString(), car.getLocationName()));
                        trackSave = null;
                    }
                    break; // done
                }
                // if there's more than one available destination use the lowest
                // ratio
                if (rldSave != null) {
                    // check for an earlier drop in the route
                    rld = checkForEarlierDrop(car, trackTemp, rld, start, routeEnd);
                    double saveCarMoves = rldSave.getCarMoves();
                    double saveRatio = saveCarMoves / rldSave.getMaxCarMoves();
                    double nextCarMoves = rld.getCarMoves();
                    double nextRatio = nextCarMoves / rld.getMaxCarMoves();

                    // bias cars to the terminal
                    if (rld == _train.getTrainTerminatesRouteLocation()) {
                        nextRatio = nextRatio * nextRatio;
                        log.debug("Location ({}) is terminate location, adjusted nextRatio {}", rld.getName(),
                                Double.toString(nextRatio));

                        // bias cars with default loads to a track with a
                        // schedule
                    } else if (!trackTemp.getScheduleId().equals(Track.NONE)) {
                        nextRatio = nextRatio * nextRatio;
                        log.debug("Track ({}) has schedule ({}), adjusted nextRatio {}", trackTemp.getName(),
                                trackTemp.getScheduleName(), Double.toString(nextRatio));
                    }
                    // bias cars with default loads to saved track with a
                    // schedule
                    if (trackSave != null && !trackSave.getScheduleId().equals(Track.NONE)) {
                        saveRatio = saveRatio * saveRatio;
                        log.debug("Saved track ({}) has schedule ({}), adjusted nextRatio {}", trackSave.getName(),
                                trackSave.getScheduleName(), Double.toString(saveRatio));
                    }
                    log.debug("Saved {} = {}, {} = {}", rldSave.getName(), Double.toString(saveRatio), rld.getName(),
                            Double.toString(nextRatio));
                    if (saveRatio < nextRatio) {
                        // the saved is better than the last found
                        rld = rldSave;
                        trackTemp = trackSave;
                        finalDestinationTrackTemp = finalDestinationTrackSave;
                    }
                }
                // every time through, save the best route destination, and
                // track
                rldSave = rld;
                trackSave = trackTemp;
                finalDestinationTrackSave = finalDestinationTrackTemp;
            }
        }
        // did we find a destination?
        if (trackSave != null && rldSave != null) {
            // determine if local staging move is allowed (leaves car in staging)
            if ((_train.isAllowReturnToStagingEnabled() || Setup.isStagingAllowReturnEnabled()) &&
                    rl.isDropAllowed() &&
                    rl.getLocation().isStaging() &&
                    trackSave.isStaging() &&
                    rl.getLocation() == rldSave.getLocation() &&
                    !_train.isLocalSwitcher() &&
                    !car.isPassenger() &&
                    !car.isCaboose() &&
                    !car.hasFred()) {
                addLine(_buildReport, SEVEN,
                        Bundle.getMessage("buildLeaveCarInStaging", car.toString(), car.getLocationName(),
                                car.getTrackName()));
                rldSave = rl; // make local move
            } else if (trackSave.isSpur()) {
                car.setScheduleItemId(trackSave.getScheduleItemId());
                trackSave.bumpSchedule();
                log.debug("Sending car to spur ({}, {}) with car schedule id ({}))", trackSave.getLocation().getName(),
                        trackSave.getName(), car.getScheduleItemId());
            } else {
                car.setScheduleItemId(Car.NONE);
            }
            if (finalDestinationTrackSave != null) {
                car.setFinalDestination(finalDestinationTrackSave.getLocation());
                car.setFinalDestinationTrack(finalDestinationTrackSave);
                if (trackSave.isAlternate()) {
                    finalDestinationTrackSave.bumpMoves(); // bump move count
                }
            }
            addCarToTrain(car, rl, rldSave, trackSave);
            return true;
        }
        addLine(_buildReport, FIVE, Bundle.getMessage("buildNoDestForCar", car.toString()));
        addLine(_buildReport, FIVE, BLANK_LINE);
        return false; // no build errors, but car not given destination
    }

    private final static Logger log = LoggerFactory.getLogger(TrainBuilderCars.class);
}
