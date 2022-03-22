package jmri.jmrit.operations.rollingstock.cars;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.InstanceManagerAutoDefault;
import jmri.InstanceManagerAutoInitialize;
import jmri.jmrit.operations.rollingstock.RollingStockManager;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.OperationsSetupXml;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.Train;

/**
 * Manages the cars.
 *
 * @author Daniel Boudreau Copyright (C) 2008
 */
public class CarManager extends RollingStockManager<Car>
        implements InstanceManagerAutoDefault, InstanceManagerAutoInitialize {

    public CarManager() {
    }

    /**
     * Finds an existing Car or creates a new Car if needed requires car's road and
     * number
     *
     * @param road   car road
     * @param number car number
     * @return new car or existing Car
     */
    @Override
    public Car newRS(String road, String number) {
        Car car = getByRoadAndNumber(road, number);
        if (car == null) {
            car = new Car(road, number);
            register(car);
        }
        return car;
    }

    @Override
    public void deregister(Car car) {
        super.deregister(car);
        InstanceManager.getDefault(CarManagerXml.class).setDirty(true);
    }

    /**
     * Sort by rolling stock location
     *
     * @return list of cars ordered by the Car's location
     */
    @Override
    public List<Car> getByLocationList() {
        return getByList(getByKernelList(), BY_LOCATION);
    }

    /**
     * Sort by car kernel names
     *
     * @return list of cars ordered by car kernel
     */
    public List<Car> getByKernelList() {
        return getByList(getByList(getByNumberList(), BY_BLOCKING), BY_KERNEL);
    }

    /**
     * Sort by car loads
     *
     * @return list of cars ordered by car loads
     */
    public List<Car> getByLoadList() {
        return getByList(getByLocationList(), BY_LOAD);
    }

    /**
     * Sort by car return when empty location and track
     *
     * @return list of cars ordered by car return when empty
     */
    public List<Car> getByRweList() {
        return getByList(getByLocationList(), BY_RWE);
    }

    public List<Car> getByRwlList() {
        return getByList(getByLocationList(), BY_RWL);
    }

    public List<Car> getByDivisionList() {
        return getByList(getByLocationList(), BY_DIVISION);
    }

    public List<Car> getByFinalDestinationList() {
        return getByList(getByDestinationList(), BY_FINAL_DEST);
    }

    /**
     * Sort by car wait count
     *
     * @return list of cars ordered by wait count
     */
    public List<Car> getByWaitList() {
        return getByList(getByIdList(), BY_WAIT);
    }

    public List<Car> getByPickupList() {
        return getByList(getByIdList(), BY_PICKUP);
    }

    // The special sort options for cars
    private static final int BY_LOAD = 4;
    private static final int BY_KERNEL = 5;
    private static final int BY_RWE = 13; // Return When Empty
    private static final int BY_FINAL_DEST = 14;
    private static final int BY_WAIT = 16;
    private static final int BY_PICKUP = 19;
    private static final int BY_HAZARD = 21;
    private static final int BY_RWL = 22; // Return When loaded
    private static final int BY_DIVISION = 23;

    // add car options to sort comparator
    @Override
    protected java.util.Comparator<Car> getComparator(int attribute) {
        switch (attribute) {
            case BY_LOAD:
                return (c1, c2) -> (c1.getLoadName().compareToIgnoreCase(c2.getLoadName()));
            case BY_KERNEL:
                return (c1, c2) -> (c1.getKernelName().compareToIgnoreCase(c2.getKernelName()));
            case BY_RWE:
                return (c1,
                        c2) -> (c1.getReturnWhenEmptyDestName().compareToIgnoreCase(c2.getReturnWhenEmptyDestName()));
            case BY_RWL:
                return (c1,
                        c2) -> (c1.getReturnWhenLoadedDestName().compareToIgnoreCase(c2.getReturnWhenLoadedDestName()));
            case BY_FINAL_DEST:
                return (c1, c2) -> (c1.getFinalDestinationName().compareToIgnoreCase(c2.getFinalDestinationName()));
            case BY_DIVISION:
                return (c1, c2) -> (c1.getDivisionName().compareToIgnoreCase(c2.getDivisionName()));
            case BY_WAIT:
                return (c1, c2) -> (c1.getWait() - c2.getWait());
            case BY_PICKUP:
                return (c1, c2) -> (c1.getPickupScheduleName().compareToIgnoreCase(c2.getPickupScheduleName()));
            case BY_HAZARD:
                return (c1, c2) -> ((c1.isHazardous() ? 1 : 0) - (c2.isHazardous() ? 1 : 0));
            default:
                return super.getComparator(attribute);
        }
    }

    /**
     * Return a list available cars (no assigned train or car already assigned to
     * this train) on a route, cars are ordered least recently moved to most
     * recently moved.
     *
     * @param train The Train to use.
     *
     * @return List of cars with no assigned train on a route
     */
    public List<Car> getAvailableTrainList(Train train) {
        List<Car> out = new ArrayList<>();
        Route route = train.getRoute();
        if (route == null) {
            return out;
        }
        // get a list of locations served by this route
        List<RouteLocation> routeList = route.getLocationsBySequenceList();
        // don't include Car at route destination
        RouteLocation destination = null;
        if (routeList.size() > 1) {
            destination = routeList.get(routeList.size() - 1);
            // However, if the destination is visited more than once, must
            // include all cars
            for (int i = 0; i < routeList.size() - 1; i++) {
                if (destination.getName().equals(routeList.get(i).getName())) {
                    destination = null; // include cars at destination
                    break;
                }
            }
            // pickup allowed at destination? Don't include cars in staging
            if (destination != null &&
                    destination.isPickUpAllowed() &&
                    destination.getLocation() != null &&
                    !destination.getLocation().isStaging()) {
                destination = null; // include cars at destination
            }
        }
        // get rolling stock by priority and then by moves
        List<Car> sortByPriority = sortByPriority(getByMovesList());
        // now build list of available Car for this route
        for (Car car : sortByPriority) {
            // only use Car with a location
            if (car.getLocation() == null) {
                continue;
            }
            RouteLocation rl = route.getLastLocationByName(car.getLocationName());
            // get Car that don't have an assigned train, or the
            // assigned train is this one
            if (rl != null && rl != destination && (car.getTrain() == null || train.equals(car.getTrain()))) {
                out.add(car);
            }
        }
        return out;
    }

    // sorts the high priority cars to the start of the list
    protected List<Car> sortByPriority(List<Car> list) {
        List<Car> out = new ArrayList<>();
        // move high priority cars to the start
        for (Car car : list) {
            if (car.getLoadPriority().equals(CarLoad.PRIORITY_HIGH)) {
                out.add(car);
            }
        }
        for (Car car : list) {
            if (car.getLoadPriority().equals(CarLoad.PRIORITY_MEDIUM)) {
                out.add(car);
            }
        }
        // now load all of the remaining low priority cars
        for (Car car : list) {
            if (!out.contains(car)) {
                out.add(car);
            }
        }
        return out;
    }

    /**
     * Provides a very sorted list of cars assigned to the train. Note that this
     * isn't the final sort as the cars must be sorted by each location the train
     * visits.
     * <p>
     * The sort priority is as follows:
     * <ol>
     * <li>Caboose or car with FRED to the end of the list
     * <li>Passenger cars to the end of the list, but before cabooses or car with
     * FRED. Passenger cars have blocking numbers which places them relative to each
     * other.
     * <li>Car's destination (alphabetical by location and track name or by track
     * blocking order)
     * <li>Car's current location (alphabetical by location and track name)
     * <li>Car's final destination (alphabetical by location and track name)
     * <li>Car is hazardous (hazardous placed after a non-hazardous car)
     * </ol>
     * <p>
     * Cars in a kernel are placed together by their kernel blocking numbers. The
     * kernel's position in the list is based on the lead car in the kernel.
     * <p>
     * If the train is to be blocked by track blocking order, all of the tracks at
     * that location need a blocking number greater than 0.
     *
     * @param train The selected Train.
     *
     * @return Ordered list of cars assigned to the train
     */
    public List<Car> getByTrainDestinationList(Train train) {
        List<Car> byHazard = getByList(getList(train), BY_HAZARD);
        List<Car> byFinal = getByList(byHazard, BY_FINAL_DEST);
        List<Car> byLocation = getByList(byFinal, BY_LOCATION);
        List<Car> byDestination = getByList(byLocation, BY_DESTINATION);
        // now place cabooses, cars with FRED, and passenger cars at the rear of the
        // train
        List<Car> out = new ArrayList<>();
        int lastCarsIndex = 0; // incremented each time a car is added to the end of the list
        for (Car car : byDestination) {
            if (car.getKernel() != null && !car.isLead()) {
                continue; // not the lead car, skip for now.
            }
            if (!car.isCaboose() && !car.hasFred() && !car.isPassenger()) {
                // sort order based on train direction when serving track, low to high if West
                // or North bound trains
                if (car.getDestinationTrack() != null && car.getDestinationTrack().getBlockingOrder() > 0) {
                    for (int j = 0; j < out.size(); j++) {
                        if (out.get(j).getDestinationTrack() == null) {
                            continue;
                        }
                        if (car.getRouteDestination() != null &&
                                (car.getRouteDestination().getTrainDirectionString().equals(RouteLocation.WEST_DIR) ||
                                        car.getRouteDestination().getTrainDirectionString()
                                                .equals(RouteLocation.NORTH_DIR))) {
                            if (car.getDestinationTrack().getBlockingOrder() < out.get(j).getDestinationTrack()
                                    .getBlockingOrder()) {
                                out.add(j, car);
                                break;
                            }
                            // Train is traveling East or South when setting out the car
                        } else {
                            if (car.getDestinationTrack().getBlockingOrder() > out.get(j).getDestinationTrack()
                                    .getBlockingOrder()) {
                                out.add(j, car);
                                break;
                            }
                        }
                    }
                }
                if (!out.contains(car)) {
                    out.add(out.size() - lastCarsIndex, car);
                }
            } else if (car.isCaboose() || car.hasFred()) {
                out.add(car); // place at end of list
                lastCarsIndex++;
            } else if (car.isPassenger()) {
                // block passenger cars at end of list, but before cabooses or car with FRED
                int index;
                for (index = 0; index < lastCarsIndex; index++) {
                    Car carTest = out.get(out.size() - 1 - index);
                    log.debug("Car ({}) has blocking number: {}", carTest.toString(), carTest.getBlocking());
                    if (carTest.isPassenger() &&
                            !carTest.isCaboose() &&
                            !carTest.hasFred() &&
                            carTest.getBlocking() < car.getBlocking()) {
                        break;
                    }
                }
                out.add(out.size() - index, car);
                lastCarsIndex++;
            }
            // group the cars in the kernel together
            if (car.isLead()) {
                int index = out.indexOf(car);
                int numberOfCars = 1; // already added the lead car to the list
                for (Car kcar : car.getKernel().getCars()) {
                    if (car != kcar) {
                        // Block cars in kernel
                        for (int j = 0; j < numberOfCars; j++) {
                            if (kcar.getBlocking() < out.get(index + j).getBlocking()) {
                                out.add(index + j, kcar);
                                break;
                            }
                        }
                        if (!out.contains(kcar)) {
                            out.add(index + numberOfCars, kcar);
                        }
                        numberOfCars++;
                        if (car.hasFred() || car.isCaboose() || car.isPassenger()) {
                            lastCarsIndex++; // place entire kernel at the end of list
                        }
                    }
                }
            }
        }
        return out;
    }

    /**
     * Get a list of car road names where the car was flagged as a caboose.
     *
     * @return List of caboose road names.
     */
    public List<String> getCabooseRoadNames() {
        List<String> names = new ArrayList<>();
        Enumeration<String> en = _hashTable.keys();
        while (en.hasMoreElements()) {
            Car car = getById(en.nextElement());
            if (car.isCaboose() && !names.contains(car.getRoadName())) {
                names.add(car.getRoadName());
            }
        }
        java.util.Collections.sort(names);
        return names;
    }

    /**
     * Get a list of car road names where the car was flagged with FRED
     *
     * @return List of road names of cars with FREDs
     */
    public List<String> getFredRoadNames() {
        List<String> names = new ArrayList<>();
        Enumeration<String> en = _hashTable.keys();
        while (en.hasMoreElements()) {
            Car car = getById(en.nextElement());
            if (car.hasFred() && !names.contains(car.getRoadName())) {
                names.add(car.getRoadName());
            }
        }
        java.util.Collections.sort(names);
        return names;
    }

    /**
     * Replace car loads
     *
     * @param type        type of car
     * @param oldLoadName old load name
     * @param newLoadName new load name
     */
    public void replaceLoad(String type, String oldLoadName, String newLoadName) {
        List<Car> cars = getList();
        for (Car car : cars) {
            if (car.getTypeName().equals(type) && car.getLoadName().equals(oldLoadName)) {
                if (newLoadName != null) {
                    car.setLoadName(newLoadName);
                } else {
                    car.setLoadName(InstanceManager.getDefault(CarLoads.class).getDefaultEmptyName());
                }
            }
            if (car.getTypeName().equals(type) && car.getReturnWhenEmptyLoadName().equals(oldLoadName)) {
                if (newLoadName != null) {
                    car.setReturnWhenEmptyLoadName(newLoadName);
                } else {
                    car.setReturnWhenEmptyLoadName(InstanceManager.getDefault(CarLoads.class).getDefaultEmptyName());
                }
            }
            if (car.getTypeName().equals(type) && car.getReturnWhenLoadedLoadName().equals(oldLoadName)) {
                if (newLoadName != null) {
                    car.setReturnWhenLoadedLoadName(newLoadName);
                } else {
                    car.setReturnWhenLoadedLoadName(InstanceManager.getDefault(CarLoads.class).getDefaultLoadName());
                }
            }
        }
    }

    public List<Car> getCarsLocationUnknown() {
        List<Car> mias = new ArrayList<>();
        List<Car> cars = getByIdList();
        for (Car rs : cars) {
            Car car = rs;
            if (car.isLocationUnknown()) {
                mias.add(car); // return unknown location car
            }
        }
        return mias;
    }

    /**
     * Determines a car's weight in ounces based on car's scale length
     * 
     * @param carLength Car's scale length
     * @return car's weight in ounces
     * @throws NumberFormatException if length isn't a number
     */
    public static String calculateCarWeight(String carLength) throws NumberFormatException {
        double doubleCarLength = Double.parseDouble(carLength) * 12 / Setup.getScaleRatio();
        double doubleCarWeight = (Setup.getInitalWeight() + doubleCarLength * Setup.getAddWeight()) / 1000;
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(1);
        return nf.format(doubleCarWeight); // car weight in ounces.
    }

    public void load(Element root) {
        if (root.getChild(Xml.CARS) != null) {
            List<Element> eCars = root.getChild(Xml.CARS).getChildren(Xml.CAR);
            log.debug("readFile sees {} cars", eCars.size());
            for (Element eCar : eCars) {
                register(new Car(eCar));
            }
        }
    }

    /**
     * Create an XML element to represent this Entry. This member has to remain
     * synchronized with the detailed DTD in operations-cars.dtd.
     *
     * @param root The common Element for operations-cars.dtd.
     */
    public void store(Element root) {
        // nothing to save under options
        root.addContent(new Element(Xml.OPTIONS));
        
        Element values;
        root.addContent(values = new Element(Xml.CARS));
        // add entries
        List<Car> carList = getByIdList();
        for (Car rs : carList) {
            Car car = rs;
            values.addContent(car.store());
        }
    }

    protected void setDirtyAndFirePropertyChange(String p, Object old, Object n) {
        // Set dirty
        InstanceManager.getDefault(CarManagerXml.class).setDirty(true);
        super.firePropertyChange(p, old, n);
    }

    private final static Logger log = LoggerFactory.getLogger(CarManager.class);

    @Override
    public void initialize() {
        InstanceManager.getDefault(OperationsSetupXml.class); // load setup
        // create manager to load cars and their attributes
        InstanceManager.getDefault(CarManagerXml.class);
    }

}
