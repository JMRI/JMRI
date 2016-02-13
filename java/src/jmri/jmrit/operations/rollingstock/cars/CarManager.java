// CarManager.java
package jmri.jmrit.operations.rollingstock.cars;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import javax.swing.JComboBox;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.RollingStockManager;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.OperationsSetupXml;
import jmri.jmrit.operations.trains.Train;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the cars.
 *
 * @author Daniel Boudreau Copyright (C) 2008
 * @version $Revision$
 */
public class CarManager extends RollingStockManager {

    // stores Kernels
    protected Hashtable<String, Kernel> _kernelHashTable = new Hashtable<String, Kernel>();

    public static final String KERNEL_LISTLENGTH_CHANGED_PROPERTY = "KernelListLength"; // NOI18N

    public CarManager() {
    }

    /**
     * record the single instance *
     */
    private static CarManager _instance = null;

    public static synchronized CarManager instance() {
        if (_instance == null) {
            if (log.isDebugEnabled()) {
                log.debug("CarManager creating instance");
            }
            // create and load
            _instance = new CarManager();
            OperationsSetupXml.instance(); // load setup
            // create manager to load cars and their attributes
            CarManagerXml.instance();
        }
        if (Control.showInstance) {
            log.debug("CarManager returns instance {}", _instance);
        }
        return _instance;
    }

    /**
     * Finds an existing Car or creates a new Car if needed requires car's road
     * and number
     *
     * @param road   car road
     * @param number car number
     * @return new car or existing Car
     */
    public Car newCar(String road, String number) {
        Car car = getByRoadAndNumber(road, number);
        if (car == null) {
            car = new Car(road, number);
            register(car);
        }
        return car;
    }

    /**
     * @return requested Car object or null if none exists
     */
    public Car getById(String id) {
        return (Car) super.getById(id);
    }

    /**
     * Get Car by road and number
     *
     * @param road   Car road
     * @param number Car number
     * @return requested Car object or null if none exists
     */
    public Car getByRoadAndNumber(String road, String number) {
        return (Car) super.getByRoadAndNumber(road, number);
    }

    /**
     * Get a Car by type and road. Used to test that a car with a specific type
     * and road exists.
     *
     * @param type car type.
     * @param road car road.
     * @return the first car found with the specified type and road.
     */
    public Car getByTypeAndRoad(String type, String road) {
        return (Car) super.getByTypeAndRoad(type, road);
    }

    /**
     * Create a new Kernel
     *
     * @param name
     * @return Kernel
     */
    public Kernel newKernel(String name) {
        Kernel kernel = getKernelByName(name);
        if (kernel == null) {
            kernel = new Kernel(name);
            Integer oldSize = Integer.valueOf(_kernelHashTable.size());
            _kernelHashTable.put(name, kernel);
            setDirtyAndFirePropertyChange(KERNEL_LISTLENGTH_CHANGED_PROPERTY, oldSize, Integer.valueOf(_kernelHashTable
                    .size()));
        }
        return kernel;
    }

    /**
     * Delete a Kernel by name
     *
     * @param name
     */
    public void deleteKernel(String name) {
        Kernel kernel = getKernelByName(name);
        if (kernel != null) {
            kernel.dispose();
            Integer oldSize = Integer.valueOf(_kernelHashTable.size());
            _kernelHashTable.remove(name);
            setDirtyAndFirePropertyChange(KERNEL_LISTLENGTH_CHANGED_PROPERTY, oldSize, Integer.valueOf(_kernelHashTable
                    .size()));
        }
    }

    /**
     * Get a Kernel by name
     *
     * @param name
     * @return named Kernel
     */
    public Kernel getKernelByName(String name) {
        return _kernelHashTable.get(name);
    }

    public void replaceKernelName(String oldName, String newName) {
        Kernel oldKernel = getKernelByName(oldName);
        if (oldKernel != null) {
            Kernel newKernel = newKernel(newName);
            // keep the lead car
            Car leadCar = (Car) oldKernel.getLead();
            if (leadCar != null) {
                leadCar.setKernel(newKernel);
            }
            for (Car car : oldKernel.getCars()) {
                car.setKernel(newKernel);
            }
        }
    }

    /**
     * Get a comboBox loaded with current Kernel names
     *
     * @return comboBox with Kernel names.
     */
    public JComboBox<String> getKernelComboBox() {
        JComboBox<String> box = new JComboBox<>();
        box.addItem(NONE);
        for (String kernelName : getKernelNameList()) {
            box.addItem(kernelName);
        }
        return box;
    }

    /**
     * Update an existing comboBox with the current kernel names
     *
     * @param box comboBox requesting update
     */
    public void updateKernelComboBox(JComboBox<String> box) {
        box.removeAllItems();
        box.addItem(NONE);
        for (String kernelName : getKernelNameList()) {
            box.addItem(kernelName);
        }
    }

    /**
     * Get a list of kernel names
     *
     * @return ordered list of kernel names
     */
    public List<String> getKernelNameList() {
        String[] names = new String[_kernelHashTable.size()];
        List<String> out = new ArrayList<String>();
        Enumeration<String> en = _kernelHashTable.keys();
        int i = 0;
        while (en.hasMoreElements()) {
            names[i++] = en.nextElement();
        }
        jmri.util.StringUtil.sort(names);
        for (String name : names) {
            out.add(name);
        }
        return out;
    }

    public int getKernelMaxNameLength() {
        int maxLength = 0;
        for (String name : getKernelNameList()) {
            if (name.length() > maxLength) {
                maxLength = name.length();
            }
        }
        return maxLength;
    }

    /**
     * Sort by rolling stock location
     *
     * @return list of cars ordered by the RollingStock's location
     */
    public List<RollingStock> getByLocationList() {
        return getByList(getByKernelList(), BY_LOCATION);
    }

    /**
     * Sort by car kernel names
     *
     * @return list of cars ordered by car kernel
     */
    public List<RollingStock> getByKernelList() {
        List<RollingStock> byBlocking = getByList(getByNumberList(), BY_BLOCKING);
        return getByList(byBlocking, BY_KERNEL);
    }

    /**
     * Sort by car loads
     *
     * @return list of cars ordered by car loads
     */
    public List<RollingStock> getByLoadList() {
        return getByList(getByLocationList(), BY_LOAD);
    }

    /**
     * Sort by car return when empty location and track
     *
     * @return list of cars ordered by car return when empty
     */
    public List<RollingStock> getByRweList() {
        return getByList(getByLocationList(), BY_RWE);
    }

    public List<RollingStock> getByFinalDestinationList() {
        return getByList(getByDestinationList(), BY_FINAL_DEST);
    }

    /**
     * Sort by car wait count
     *
     * @return list of cars ordered by wait count
     */
    public List<RollingStock> getByWaitList() {
        return getByList(getByIdList(), BY_WAIT);
    }

    public List<RollingStock> getByPickupList() {
        return getByList(getByIdList(), BY_PICKUP);
    }

    // The special sort options for cars
    private static final int BY_LOAD = 4;
    private static final int BY_KERNEL = 5;
    private static final int BY_RWE = 13; // Return When Empty
    private static final int BY_FINAL_DEST = 14;
    private static final int BY_WAIT = 16;
    private static final int BY_PICKUP = 19;

    // add car options to sort comparator
    @Override
    protected java.util.Comparator<RollingStock> getComparator(int attribute) {
        switch (attribute) {
            case BY_LOAD:
                return (c1,c2)->(((Car)c1).getLoadName().compareToIgnoreCase(((Car) c2).getLoadName()));
            case BY_KERNEL:
                return (c1,c2)->(((Car)c1).getKernelName().compareToIgnoreCase(((Car)c2).getKernelName()));
            case BY_RWE:
                return (c1,c2)->(((Car)c1).getReturnWhenEmptyDestName().compareToIgnoreCase(((Car)c2).getReturnWhenEmptyDestName()));
            case BY_FINAL_DEST:
                return (c1,c2)->(((Car)c1).getFinalDestinationName().compareToIgnoreCase(((Car)c2).getFinalDestinationName()));
            case BY_WAIT:
                return (c1,c2)->(((Car)c1).getWait() - ((Car)c2).getWait());
            case BY_PICKUP:
                return (c1,c2)->(((Car)c1).getPickupScheduleName().compareToIgnoreCase(((Car)c2).getPickupScheduleName()));
            default:
                return super.getComparator(attribute);
        }
    }

    /**
     * Return a list available cars (no assigned train or car already assigned
     * to this train) on a route, cars are ordered least recently moved to most
     * recently moved.
     *
     * @param train
     * @return List of cars with no assigned train on a route
     */
    public List<Car> getAvailableTrainList(Train train) {
        List<Car> out = new ArrayList<Car>();
        Route route = train.getRoute();
        if (route == null) {
            return out;
        }
        // get a list of locations served by this route
        List<RouteLocation> routeList = route.getLocationsBySequenceList();
        // don't include RollingStock at route destination
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
            if (destination != null && destination.isPickUpAllowed()
                    && !destination.getLocation().isStaging()) {
                destination = null; // include cars at destination
            }
        }
        // get rolling stock by priority and then by moves
        List<RollingStock> sortByPriority = sortByPriority(getByMovesList());
        // now build list of available RollingStock for this route
        for (RollingStock rs : sortByPriority) {
            // only use RollingStock with a location
            if (rs.getLocation() == null) {
                continue;
            }
            RouteLocation rl = route.getLastLocationByName(rs.getLocationName());
            // get RollingStock that don't have an assigned train, or the
            // assigned train is this one
            if (rl != null && rl != destination && (rs.getTrain() == null || train.equals(rs.getTrain()))) {
                out.add((Car) rs);
            }
        }
        return out;
    }

    // sorts the high priority cars to the start of the list
    protected List<RollingStock> sortByPriority(List<RollingStock> list) {
        List<RollingStock> out = new ArrayList<RollingStock>();
        // move high priority cars to the start
        for (int i = 0; i < list.size(); i++) {
            RollingStock rs = list.get(i);
            if (rs.getLoadPriority().equals(CarLoad.PRIORITY_HIGH)) {
                out.add(list.get(i));
                list.remove(i--);
            }
        }
        // now load all of the remaining low priority cars
        for (RollingStock rs : list) {
            out.add(rs);
        }
        return out;
    }

    /**
     * Get a list of Cars assigned to a train sorted by destination track blocking order
     * or by track names. If a train is to be blocked by track blocking order, all of
     * the tracks at that location need a blocking number greater than 0.
     * Passenger cars will be placed at the end of the list. Caboose or car with
     * FRED will be the last car(s) in the list. Kernels are placed together by
     * blocking number.
     *
     * @param train
     * @return Ordered list of Cars assigned to the train
     */
    public List<Car> getByTrainDestinationList(Train train) {
        List<RollingStock> byFinal = getByList(getList(train), BY_FINAL_DEST);
        List<RollingStock> byLocation = getByList(byFinal, BY_LOCATION);
        List<RollingStock> byDestination = getByList(byLocation, BY_DESTINATION);
        // now place cabooses, cars with FRED, and passenger cars at the rear of the train
        List<Car> out = new ArrayList<Car>();
        int lastCarsIndex = 0; // incremented each time a car is added to the end of the list
        for (RollingStock rs : byDestination) {
            Car car = (Car) rs;
            if (car.getKernel() != null && !car.getKernel().isLead(car)) {
                continue; // not the lead car, skip for now.
            }
            if (!car.isCaboose() && !car.hasFred() && !car.isPassenger()) {
                // sort order based on train direction when serving track, low to high if West or North bound trains
                if (car.getDestinationTrack() != null && car.getDestinationTrack().getBlockingOrder() > 0) {
                    for (int j = 0; j < out.size(); j++) {
                        if (car.getRouteDestination() != null &&
                                (car.getRouteDestination().getTrainDirectionString().equals(RouteLocation.WEST_DIR)
                                || car.getRouteDestination().getTrainDirectionString().equals(RouteLocation.NORTH_DIR))) {
                            if (car.getDestinationTrack().getBlockingOrder() < out.get(j).getDestinationTrack().getBlockingOrder()) {
                                out.add(j, car);
                                break;
                            }
                        } else {
                            if (car.getDestinationTrack().getBlockingOrder() > out.get(j).getDestinationTrack().getBlockingOrder()) {
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
                // block passenger cars at end of list
                int index;
                for (index = 0; index < lastCarsIndex; index++) {
                    Car carTest = out.get(out.size() - 1 - index);
                    log.debug("Car ({}) has blocking number: {}", carTest.toString(), carTest.getBlocking());
                    if (carTest.isPassenger() && !carTest.isCaboose() && !carTest.hasFred()
                            && carTest.getBlocking() < car.getBlocking()) {
                        break;
                    }
                }
                out.add(out.size() - index, car);
                lastCarsIndex++;
            }
            // group the cars in the kernel together
            if (car.getKernel() != null && car.getKernel().isLead(car)) {
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
        List<String> names = new ArrayList<String>();
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
        List<String> names = new ArrayList<String>();
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
        List<RollingStock> cars = getList();
        for (RollingStock rs : cars) {
            Car car = (Car) rs;
            if (car.getTypeName().equals(type) && car.getLoadName().equals(oldLoadName)) {
                if (newLoadName != null) {
                    car.setLoadName(newLoadName);
                } else {
                    car.setLoadName(CarLoads.instance().getDefaultEmptyName());
                }
            }
        }
    }

    public List<Car> getCarsLocationUnknown() {
        List<Car> mias = new ArrayList<Car>();
        List<RollingStock> cars = getByIdList();
        for (RollingStock rs : cars) {
            Car car = (Car) rs;
            if (car.isLocationUnknown()) {
                mias.add(car); // return unknown location car
            }
        }
        return mias;
    }

    public void load(Element root) {
        // new format using elements starting version 3.3.1
        if (root.getChild(Xml.NEW_KERNELS) != null) {
            @SuppressWarnings("unchecked")
            List<Element> eKernels = root.getChild(Xml.NEW_KERNELS).getChildren(Xml.KERNEL);
            if (log.isDebugEnabled()) {
                log.debug("Car manager sees {} kernels", eKernels.size());
            }
            Attribute a;
            for (Element eKernel : eKernels) {
                if ((a = eKernel.getAttribute(Xml.NAME)) != null) {
                    newKernel(a.getValue());
                }
            }
        } // old format
        else if (root.getChild(Xml.KERNELS) != null) {
            String names = root.getChildText(Xml.KERNELS);
            if (!names.equals("")) {
                String[] kernelNames = names.split("%%"); // NOI18N
                if (log.isDebugEnabled()) {
                    log.debug("kernels: {}", names);
                }
                for (String name : kernelNames) {
                    newKernel(name);
                }
            }
        }

        // if (root.getChild(Xml.OPTIONS) != null) {
        // Element options = root.getChild(Xml.OPTIONS);
        // if (log.isDebugEnabled())
        // log.debug("ctor from element " + options);
        // }
        if (root.getChild(Xml.CARS) != null) {
            @SuppressWarnings("unchecked")
            List<Element> eCars = root.getChild(Xml.CARS).getChildren(Xml.CAR);
            if (log.isDebugEnabled()) {
                log.debug("readFile sees {} cars", eCars.size());
            }
            for (Element eCar : eCars) {
                register(new Car(eCar));
            }
        }
    }

    /**
     * Create an XML element to represent this Entry. This member has to remain
     * synchronized with the detailed DTD in operations-cars.dtd.
     */
    public void store(Element root) {
        root.addContent(new Element(Xml.OPTIONS)); // nothing to save under
        // options

        Element values;
        List<String> names = getKernelNameList();
        if (Control.backwardCompatible) {
            root.addContent(values = new Element(Xml.KERNELS));
            for (String name : names) {
                String kernelNames = name + "%%"; // NOI18N
                values.addContent(kernelNames);
            }
        }
        // new format using elements
        Element kernels = new Element(Xml.NEW_KERNELS);
        for (String name : names) {
            Element kernel = new Element(Xml.KERNEL);
            kernel.setAttribute(new Attribute(Xml.NAME, name));
            kernels.addContent(kernel);
        }
        root.addContent(kernels);
        root.addContent(values = new Element(Xml.CARS));
        // add entries
        List<RollingStock> carList = getByIdList();
        for (RollingStock rs : carList) {
            Car car = (Car) rs;
            values.addContent(car.store());
        }
    }

    protected void setDirtyAndFirePropertyChange(String p, Object old, Object n) {
        // Set dirty
        CarManagerXml.instance().setDirty(true);
        super.firePropertyChange(p, old, n);
    }

    private final static Logger log = LoggerFactory.getLogger(CarManager.class.getName());

}
