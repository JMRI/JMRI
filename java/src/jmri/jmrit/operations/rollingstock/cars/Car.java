package jmri.jmrit.operations.rollingstock.cars;

import java.beans.PropertyChangeEvent;
import jmri.InstanceManager;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.trains.TrainCommon;
import jmri.jmrit.operations.trains.schedules.TrainSchedule;
import jmri.jmrit.operations.trains.schedules.TrainScheduleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a car on the layout
 *
 * @author Daniel Boudreau Copyright (C) 2008, 2009, 2010, 2012, 2013, 2014,
 *         2015
 */
public class Car extends RollingStock {

    CarLoads carLoads = InstanceManager.getDefault(CarLoads.class);

    protected boolean _passenger = false;
    protected boolean _hazardous = false;
    protected boolean _caboose = false;
    protected boolean _fred = false;
    protected boolean _utility = false;
    protected boolean _loadGeneratedByStaging = false;
    protected Kernel _kernel = null;
    protected String _loadName = carLoads.getDefaultEmptyName();
    protected int _wait = 0;

    protected Location _rweDestination = null; // return when empty destination
    protected Track _rweDestTrack = null; // return when empty track
    protected String _rweLoadName = carLoads.getDefaultEmptyName();

    // schedule items
    protected String _scheduleId = NONE; // the schedule id assigned to this car
    protected String _nextLoadName = NONE; // next load by schedule
    protected int _nextWait = 0; // next wait by schedule
    protected Location _finalDestination = null; // final destination by schedule or router
    protected Track _finalDestTrack = null; // final track by schedule or router
    protected Location _previousFinalDestination = null; // previous final destination (for train resets)
    protected Track _previousFinalDestTrack = null; // previous final track (for train resets)
    protected String _previousScheduleId = NONE; // previous schedule id (for train resets)
    protected String _pickupScheduleId = NONE;
    protected String _nextPickupScheduleId = NONE; // when the car needs to be pulled

    public static final String EXTENSION_REGEX = " ";
    public static final String CABOOSE_EXTENSION = Bundle.getMessage("(C)");
    public static final String FRED_EXTENSION = Bundle.getMessage("(F)");
    public static final String PASSENGER_EXTENSION = Bundle.getMessage("(P)");
    public static final String UTILITY_EXTENSION = Bundle.getMessage("(U)");
    public static final String HAZARDOUS_EXTENSION = Bundle.getMessage("(H)");

    public static final String LOAD_CHANGED_PROPERTY = "Car load changed"; // property change descriptions // NOI18N
    public static final String WAIT_CHANGED_PROPERTY = "Car wait changed"; // NOI18N
    public static final String NEXT_WAIT_CHANGED_PROPERTY = "Car next wait changed"; // NOI18N
    public static final String FINAL_DESTINATION_CHANGED_PROPERTY = "Car final destination changed"; // NOI18N
    public static final String FINAL_DESTINATION_TRACK_CHANGED_PROPERTY = "Car final destination track changed"; // NOI18N
    public static final String RETURN_WHEN_EMPTY_CHANGED_PROPERTY = "Car return when empty changed"; // NOI18N
    public static final String SCHEDULE_ID_CHANGED_PROPERTY = "car schedule id changed"; // NOI18N
    public static final String KERNEL_NAME_CHANGED_PROPERTY = "kernel name changed"; // NOI18N

    public Car() {
        super();
        loaded = true;
    }

    public Car(String road, String number) {
        super(road, number);
        loaded = true;
        log.debug("New car ({} {})", road, number);
        addPropertyChangeListeners();
    }

    public Car copy() {
        Car car = new Car();
        car.setBuilt(getBuilt());
        car.setColor(getColor());
        car.setLength(getLength());
        car.setLoadName(getLoadName());
        car.setReturnWhenEmptyLoadName(getReturnWhenEmptyLoadName());
        car.setNumber(getNumber());
        car.setOwner(getOwner());
        car.setRoadName(getRoadName());
        car.setTypeName(getTypeName());
        car.setCaboose(isCaboose());
        car.setFred(hasFred());
        car.setPassenger(isPassenger());
        car.loaded = true;
        return car;
    }

    public void setHazardous(boolean hazardous) {
        boolean old = _hazardous;
        _hazardous = hazardous;
        if (!old == hazardous) {
            setDirtyAndFirePropertyChange("car hazardous", old ? "true" : "false", hazardous ? "true" : "false"); // NOI18N
        }
    }

    public boolean isHazardous() {
        return _hazardous;
    }

    public void setPassenger(boolean passenger) {
        boolean old = _passenger;
        _passenger = passenger;
        if (!old == passenger) {
            setDirtyAndFirePropertyChange("car passenger", old ? "true" : "false", passenger ? "true" : "false"); // NOI18N
        }
    }

    public boolean isPassenger() {
        return _passenger;
    }

    public void setFred(boolean fred) {
        boolean old = _fred;
        _fred = fred;
        if (!old == fred) {
            setDirtyAndFirePropertyChange("car has fred", old ? "true" : "false", fred ? "true" : "false"); // NOI18N
        }
    }

    /**
     * Used to determine if car has FRED (Flashing Rear End Device).
     *
     * @return true if car has FRED.
     */
    public boolean hasFred() {
        return _fred;
    }

    public void setLoadName(String load) {
        String old = _loadName;
        _loadName = load;
        if (!old.equals(load)) {
            setDirtyAndFirePropertyChange(LOAD_CHANGED_PROPERTY, old, load);
        }
    }

    /**
     * The load name assigned to this car.
     *
     * @return The load name assigned to this car.
     */
    public String getLoadName() {
        return _loadName;
    }

    @Deprecated
    // saved for scripts
    public void setLoad(String load) {
        setLoadName(load);
    }

    @Deprecated
    // saved for scripts
    public String getLoad() {
        return getLoadName();
    }

    public void setReturnWhenEmptyLoadName(String load) {
        String old = _rweLoadName;
        _rweLoadName = load;
        if (!old.equals(load)) {
            setDirtyAndFirePropertyChange(LOAD_CHANGED_PROPERTY, old, load);
        }
    }

    public String getReturnWhenEmptyLoadName() {
        return _rweLoadName;
    }

    /**
     * Gets the car's load's priority.
     * 
     * @return The car's load priority.
     */
    public String getLoadPriority() {
        return (carLoads.getPriority(getTypeName(), getLoadName()));
    }

    /**
     * Gets the car load's type, empty or load.
     *
     * @return type empty or type load
     */
    public String getLoadType() {
        return (carLoads.getLoadType(getTypeName(), getLoadName()));
    }

    public String getPickupComment() {
        return carLoads.getPickupComment(getTypeName(), getLoadName());
    }

    public String getDropComment() {
        return carLoads.getDropComment(getTypeName(), getLoadName());
    }

    public void setLoadGeneratedFromStaging(boolean fromStaging) {
        _loadGeneratedByStaging = fromStaging;
    }

    public boolean isLoadGeneratedFromStaging() {
        return _loadGeneratedByStaging;
    }

    /**
     * Used to keep track of which item in a schedule was used for this car.
     * 
     * @param id The ScheduleItem id for this car.
     *
     */
    public void setScheduleItemId(String id) {
        log.debug("Set schedule item id ({}) for car ({})", id, toString());
        String old = _scheduleId;
        _scheduleId = id;
        if (!old.equals(id)) {
            setDirtyAndFirePropertyChange(SCHEDULE_ID_CHANGED_PROPERTY, old, id);
        }
    }

    public String getScheduleItemId() {
        return _scheduleId;
    }

    public void setNextLoadName(String load) {
        String old = _nextLoadName;
        _nextLoadName = load;
        if (!old.equals(load)) {
            setDirtyAndFirePropertyChange(LOAD_CHANGED_PROPERTY, old, load);
        }
    }

    public String getNextLoadName() {
        return _nextLoadName;
    }

    @Override
    public String getWeightTons() {
        String weight = super.getWeightTons();
        if (!_weightTons.equals(DEFAULT_WEIGHT)) {
            return weight;
        }
        if (!isCaboose() && !isPassenger()) {
            return weight;
        }
        // .9 tons/foot for caboose and passenger cars
        try {
            weight = Integer.toString((int) (Double.parseDouble(getLength()) * .9));
        } catch (Exception e) {
            log.debug("Car ({}) length not set for caboose or passenger car", toString());
        }
        return weight;
    }

    /**
     * Returns a car's weight adjusted for load. An empty car's weight is 1/3
     * the car's loaded weight.
     */
    @Override
    public int getAdjustedWeightTons() {
        int weightTons = 0;
        try {
            // get loaded weight
            weightTons = Integer.parseInt(getWeightTons());
            // adjust for empty weight if car is empty, 1/3 of loaded weight
            if (!isCaboose() && !isPassenger() && getLoadType().equals(CarLoad.LOAD_TYPE_EMPTY)) {
                weightTons = weightTons / 3;
            }
        } catch (NumberFormatException e) {
            log.debug("Car ({}) weight not set", toString());
        }
        return weightTons;
    }

    public void setWait(int count) {
        int old = _wait;
        _wait = count;
        if (old != count) {
            setDirtyAndFirePropertyChange(NEXT_WAIT_CHANGED_PROPERTY, old, count);
        }
    }

    public int getWait() {
        return _wait;
    }

    public void setNextWait(int count) {
        int old = _nextWait;
        _nextWait = count;
        if (old != count) {
            setDirtyAndFirePropertyChange(NEXT_WAIT_CHANGED_PROPERTY, old, count);
        }
    }

    public int getNextWait() {
        return _nextWait;
    }

    /**
     * Sets when this car will be picked up (day of the week)
     *
     * @param id See TrainSchedule.java
     */
    public void setPickupScheduleId(String id) {
        String old = _pickupScheduleId;
        _pickupScheduleId = id;
        if (!old.equals(id)) {
            setDirtyAndFirePropertyChange("car pickup schedule changes", old, id); // NOI18N
        }
    }

    public String getPickupScheduleId() {
        return _pickupScheduleId;
    }

    public void setNextPickupScheduleId(String id) {
        String old = _nextPickupScheduleId;
        _nextPickupScheduleId = id;
        if (!old.equals(id)) {
            setDirtyAndFirePropertyChange("next car pickup schedule changes", old, id); // NOI18N
        }
    }

    public String getNextPickupScheduleId() {
        return _nextPickupScheduleId;
    }

    public String getPickupScheduleName() {
        TrainSchedule sch =
                InstanceManager.getDefault(TrainScheduleManager.class).getScheduleById(getPickupScheduleId());
        String name = NONE;
        if (sch != null) {
            name = sch.getName();
        }
        return name;
    }

    /**
     * Sets the final destination for a car.
     *
     * @param destination The final destination for this car.
     */
    public void setFinalDestination(Location destination) {
        Location old = _finalDestination;
        if (old != null) {
            old.removePropertyChangeListener(this);
        }
        _finalDestination = destination;
        if (_finalDestination != null) {
            _finalDestination.addPropertyChangeListener(this);
        }
        // log.debug("Next destination for car ("+toString()+") old: "+old+" new: "+destination);
        if ((old != null && !old.equals(destination)) || (destination != null && !destination.equals(old))) {
            setDirtyAndFirePropertyChange(FINAL_DESTINATION_CHANGED_PROPERTY, old, destination);
        }
    }

    @Deprecated
    // available for old scripts
    public void setNextDestination(Location destination) {
        setFinalDestination(destination);
    }

    public Location getFinalDestination() {
        return _finalDestination;
    }

    public String getFinalDestinationName() {
        if (_finalDestination != null) {
            return _finalDestination.getName();
        }
        return NONE;
    }

    public void setFinalDestinationTrack(Track track) {
        Track old = _finalDestTrack;
        _finalDestTrack = track;
        if (track == null) {
            setScheduleItemId(NONE);
        }
        if ((old != null && !old.equals(track)) || (track != null && !track.equals(old))) {
            if (old != null) {
                old.removePropertyChangeListener(this);
                old.deleteReservedInRoute(this);
            }
            if (_finalDestTrack != null) {
                _finalDestTrack.addReservedInRoute(this);
                _finalDestTrack.addPropertyChangeListener(this);
            }
            setDirtyAndFirePropertyChange(FINAL_DESTINATION_TRACK_CHANGED_PROPERTY, old, track);
        }
    }

    @Deprecated
    // available for old scripts
    public void setNextDestinationTrack(Track track) {
        setFinalDestinationTrack(track);
    }

    public Track getFinalDestinationTrack() {
        return _finalDestTrack;
    }

    public String getFinalDestinationTrackName() {
        if (_finalDestTrack != null) {
            return _finalDestTrack.getName();
        }
        return NONE;
    }

    public void setPreviousFinalDestination(Location location) {
        _previousFinalDestination = location;
    }

    public Location getPreviousFinalDestination() {
        return _previousFinalDestination;
    }

    public void setPreviousFinalDestinationTrack(Track track) {
        _previousFinalDestTrack = track;
    }

    public Track getPreviousFinalDestinationTrack() {
        return _previousFinalDestTrack;
    }

    public void setPreviousScheduleId(String id) {
        _previousScheduleId = id;
    }

    public String getPreviousScheduleId() {
        return _previousScheduleId;
    }

    public void setReturnWhenEmptyDestination(Location destination) {
        Location old = _rweDestination;
        _rweDestination = destination;
        if ((old != null && !old.equals(destination)) || (destination != null && !destination.equals(old))) {
            setDirtyAndFirePropertyChange(RETURN_WHEN_EMPTY_CHANGED_PROPERTY, null, null);
        }
    }

    public Location getReturnWhenEmptyDestination() {
        return _rweDestination;
    }

    public String getReturnWhenEmptyDestinationName() {
        if (getReturnWhenEmptyDestination() != null) {
            return getReturnWhenEmptyDestination().getName();
        } else {
            return NONE;
        }

    }

    public void setReturnWhenEmptyDestTrack(Track track) {
        Track old = _rweDestTrack;
        _rweDestTrack = track;
        if ((old != null && !old.equals(track)) || (track != null && !track.equals(old))) {
            setDirtyAndFirePropertyChange(RETURN_WHEN_EMPTY_CHANGED_PROPERTY, null, null);
        }

    }

    public Track getReturnWhenEmptyDestTrack() {
        return _rweDestTrack;
    }

    public String getReturnWhenEmptyDestTrackName() {
        if (getReturnWhenEmptyDestTrack() != null) {
            return getReturnWhenEmptyDestTrack().getName();
        } else {
            return NONE;
        }
    }

    public String getReturnWhenEmptyDestName() {
        if (getReturnWhenEmptyDestination() != null) {
            return getReturnWhenEmptyDestinationName() + "(" + getReturnWhenEmptyDestTrackName() + ")";
        } else {
            return NONE;
        }
    }

    public void setCaboose(boolean caboose) {
        boolean old = _caboose;
        _caboose = caboose;
        if (!old == caboose) {
            setDirtyAndFirePropertyChange("car is caboose", old ? "true" : "false", caboose ? "true" : "false"); // NOI18N
        }
    }

    public boolean isCaboose() {
        return _caboose;
    }

    public void setUtility(boolean utility) {
        boolean old = _utility;
        _utility = utility;
        if (!old == utility) {
            setDirtyAndFirePropertyChange("car is utility", old ? "true" : "false", utility ? "true" : "false"); // NOI18N
        }
    }

    public boolean isUtility() {
        return _utility;
    }
    
    public boolean isLocalMove() {
        if (getRouteLocation() == null || getRouteDestination() == null) {
            return false;
        }
        if (getRouteLocation().equals(getRouteDestination()) && getTrack() != null) {
            return true;
        }
        if (getTrain() == null) {
            return false;
        }
        if (getTrain().isLocalSwitcher() &&
                TrainCommon.splitString(getRouteLocation().getName())
                        .equals(TrainCommon.splitString(getRouteDestination().getName())) &&
                getTrack() != null) {
            return true;
        }
        // look for sequential locations with the "same" name
        if (TrainCommon.splitString(getRouteLocation().getName()).equals(TrainCommon.splitString(getRouteDestination().getName())) &&
                getTrain().getRoute() != null) {
            boolean foundRl = false;
            for (RouteLocation rl : getTrain().getRoute().getLocationsBySequenceList()) {
                if (foundRl) {
                    if (TrainCommon.splitString(getRouteDestination().getName()).equals(TrainCommon.splitString(rl.getName()))) {
                        // user can specify the "same" location two more more
                        // times in a row
                        if (getRouteDestination() != rl) {
                            continue;
                        } else {
                            return true;
                        }
                    } else {
                        return false;
                    }
                }
                if (getRouteLocation().equals(rl)) {
                    foundRl = true;
                }
            }
        }
        return false;
    }

    /**
     * A kernel is a group of cars that are switched as a unit.
     * 
     * @param kernel The assigned Kernel for this car.
     *
     */
    public void setKernel(Kernel kernel) {
        if (_kernel == kernel) {
            return;
        }
        String old = "";
        if (_kernel != null) {
            old = _kernel.getName();
            _kernel.delete(this);
        }
        _kernel = kernel;
        String newName = "";
        if (_kernel != null) {
            _kernel.add(this);
            newName = _kernel.getName();
        }
        if (!old.equals(newName)) {
            setDirtyAndFirePropertyChange(KERNEL_NAME_CHANGED_PROPERTY, old, newName); // NOI18N
        }
    }

    public Kernel getKernel() {
        return _kernel;
    }

    public String getKernelName() {
        if (_kernel != null) {
            return _kernel.getName();
        }
        return NONE;
    }
    
    /**
     * Used to determine if car is lead car in a kernel
     * @return true if lead car in a kernel
     */
    public boolean isLead() {
        if (getKernel() != null) {
           return getKernel().isLead(this);
        }
        return false;
    }

    /**
     * Updates all cars in a kernel. After the update, the cars will all have
     * the same final destination, load, and next load.
     */
    public void updateKernel() {
        if (isLead()) {
            for (Car car : getKernel().getCars()) {
                car.setFinalDestination(getFinalDestination());
                car.setFinalDestinationTrack(getFinalDestinationTrack());
                car.setLoadGeneratedFromStaging(isLoadGeneratedFromStaging());
                if (InstanceManager.getDefault(CarLoads.class).containsName(car.getTypeName(), getLoadName())) {
                    car.setLoadName(getLoadName());
                }
                if (InstanceManager.getDefault(CarLoads.class).containsName(car.getTypeName(), getNextLoadName())) {
                    car.setNextLoadName(getNextLoadName());
                }
            }
        }
    }

    /**
     * Used to determine if a car can be set out at a destination (location).
     * Track is optional. In addition to all of the tests that testLocation
     * performs, spurs with schedules are also checked.
     *
     * @return status OKAY, TYPE, ROAD, LENGTH, ERROR_TRACK, CAPACITY, SCHEDULE,
     *         CUSTOM
     */
    @Override
    public String testDestination(Location destination, Track track) {
        String status = super.testDestination(destination, track);
        if (!status.equals(Track.OKAY)) {
            return status;
        }
        // now check to see if the track has a schedule
        if (track == null) {
            return status;
        }
        return track.checkSchedule(this);
    }

    /**
     * Sets the car's destination on the layout
     *
     * @param track (yard, spur, staging, or interchange track)
     * @return "okay" if successful, "type" if the rolling stock's type isn't
     *         acceptable, or "length" if the rolling stock length didn't fit,
     *         or Schedule if the destination will not accept the car because
     *         the spur has a schedule and the car doesn't meet the schedule
     *         requirements. Also changes the car load status when the car
     *         reaches its destination.
     */
    @Override
    public String setDestination(Location destination, Track track) {
        return setDestination(destination, track, false);
    }

    /**
     * Sets the car's destination on the layout
     *
     * @param track (yard, spur, staging, or interchange track)
     * @param force when true ignore track length, type, {@literal &} road when
     *            setting destination
     * @return "okay" if successful, "type" if the rolling stock's type isn't
     *         acceptable, or "length" if the rolling stock length didn't fit,
     *         or Schedule if the destination will not accept the car because
     *         the spur has a schedule and the car doesn't meet the schedule
     *         requirements. Also changes the car load status when the car
     *         reaches its destination.
     */
    @Override
    public String setDestination(Location destination, Track track, boolean force) {
        // save destination name and track in case car has reached its destination
        String destinationName = getDestinationName();
        Track destinationTrack = getDestinationTrack();
        String status = super.setDestination(destination, track, force);
        // return if not Okay
        if (!status.equals(Track.OKAY)) {
            return status;
        }
        // now check to see if the track has a schedule
        if (track != null && destinationTrack != track && loaded) {
            status = track.scheduleNext(this);
            if (!status.equals(Track.OKAY)) {
                return status;
            }
        }
        // done?
        if (destinationName.equals(NONE) || (destination != null) || getTrain() == null) {
            return status;
        }
        // car was in a train and has been dropped off, update load, RWE could set a new final destination
        loadNext(destinationTrack);
        return status;
    }

    public void loadNext(Track destTrack) {
        setLoadGeneratedFromStaging(false);
        // update wait count
        setWait(getNextWait());
        setNextWait(0);
        // and the pickup day
        setPickupScheduleId(getNextPickupScheduleId());
        setNextPickupScheduleId(NONE);
        if (destTrack != null) {
            // arrived at spur?
            if (destTrack.isSpur()) {
                updateLoad();
            } 
            // update load optionally when car reaches staging
            else if (destTrack.isStaging()) {
                if (destTrack.isLoadSwapEnabled() && getLoadName().equals(carLoads.getDefaultEmptyName())) {
                    setLoadName(carLoads.getDefaultLoadName());
                } else if (destTrack.isLoadSwapEnabled() && getLoadName().equals(carLoads.getDefaultLoadName())) {
                    setLoadEmpty();
                } else if (destTrack.isLoadEmptyEnabled() && getLoadName().equals(carLoads.getDefaultLoadName())) {
                    setLoadEmpty();
                } else if (destTrack.isRemoveCustomLoadsEnabled() &&
                        !getLoadName().equals(carLoads.getDefaultEmptyName()) &&
                        !getLoadName().equals(carLoads.getDefaultLoadName())) {
                    // remove this car's final destination if it has one
                    setFinalDestination(null);
                    setFinalDestinationTrack(null);
                    // car arriving into staging with the RWE load?
                    if (getLoadName().equals(getReturnWhenEmptyLoadName())) {
                        setLoadName(carLoads.getDefaultEmptyName());
                    } else {
                        setLoadEmpty(); // note that RWE sets the car's final destination
                    }
                }
            }
        }
    }

    /**
     * Updates a car's load when placed at a spur. Load change delayed if wait
     * count is greater than zero.
     */
    public void updateLoad() {
        if (getWait() > 0) {
            return; // change load when wait count reaches 0
        } // arriving at spur with a schedule?
        if (!getNextLoadName().equals(NONE)) {
            setLoadName(getNextLoadName());
            setNextLoadName(NONE);
            // RWE load and no destination?
            if (getLoadName().equals(getReturnWhenEmptyLoadName()) && getFinalDestination() == null) {
                setReturnWhenEmpty();
            }
            return;
        }
        // flip load names
        if (getLoadType().equals(CarLoad.LOAD_TYPE_EMPTY)) {
            setLoadName(carLoads.getDefaultLoadName());
        } else {
            setLoadEmpty();
        }
    }

    /**
     * Sets the car's load to empty, triggers RWE load and destination if
     * enabled.
     */
    private void setLoadEmpty() {
        if (!getLoadName().equals(getReturnWhenEmptyLoadName())) {
            setLoadName(getReturnWhenEmptyLoadName()); // default RWE load is the "E" load
            setReturnWhenEmpty();
        }
    }

    /*
     * Don't set return address if in staging with the same RWE address and
     * don't set return address if at the RWE address
     */
    private void setReturnWhenEmpty() {
        if (getReturnWhenEmptyDestination() != null &&
                (getLocation() != getReturnWhenEmptyDestination() ||
                        (!getReturnWhenEmptyDestination().isStaging() &&
                                getTrack() != getReturnWhenEmptyDestTrack()))) {
            setFinalDestination(getReturnWhenEmptyDestination());
            if (getReturnWhenEmptyDestTrack() != null) {
                setFinalDestinationTrack(getReturnWhenEmptyDestTrack());
            }
            log.debug("Car ({}) has return when empty destination ({}, {}) load {}", toString(),
                    getFinalDestinationName(), getFinalDestinationTrackName(), getLoadName());
        }
    }

    public String getTypeExtensions() {
        StringBuffer buf = new StringBuffer();
        if (isCaboose()) {
            buf.append(EXTENSION_REGEX + CABOOSE_EXTENSION);
        }
        if (hasFred()) {
            buf.append(EXTENSION_REGEX + FRED_EXTENSION);
        }
        if (isPassenger()) {
            buf.append(EXTENSION_REGEX + PASSENGER_EXTENSION + EXTENSION_REGEX + getBlocking());
        }
        if (isUtility()) {
            buf.append(EXTENSION_REGEX + UTILITY_EXTENSION);
        }
        if (isHazardous()) {
            buf.append(EXTENSION_REGEX + HAZARDOUS_EXTENSION);
        }
        return buf.toString();
    }

    @Override
    public void reset() {
        setScheduleItemId(getPreviousScheduleId()); // revert to previous
        setNextLoadName(NONE);
        setNextWait(0);
        setFinalDestination(getPreviousFinalDestination()); // revert to previous
        setFinalDestinationTrack(getPreviousFinalDestinationTrack()); // revert to previous
        if (isLoadGeneratedFromStaging()) {
            setLoadGeneratedFromStaging(false);
            setLoadName(InstanceManager.getDefault(CarLoads.class).getDefaultEmptyName());
        }

        super.reset();
    }

    @Override
    public void dispose() {
        setKernel(null);
        setFinalDestination(null); // removes property change listener
        setFinalDestinationTrack(null); // removes property change listener
        InstanceManager.getDefault(CarTypes.class).removePropertyChangeListener(this);
        InstanceManager.getDefault(CarLengths.class).removePropertyChangeListener(this);
        super.dispose();
    }

    // used to stop a track's schedule from bumping when loading car database
    private boolean loaded = false;

    /**
     * Construct this Entry from XML. This member has to remain synchronized
     * with the detailed DTD in operations-cars.dtd
     *
     * @param e Car XML element
     */
    public Car(org.jdom2.Element e) {
        super(e);
        loaded = true;
        org.jdom2.Attribute a;
        if ((a = e.getAttribute(Xml.PASSENGER)) != null) {
            _passenger = a.getValue().equals(Xml.TRUE);
        }
        if ((a = e.getAttribute(Xml.HAZARDOUS)) != null) {
            _hazardous = a.getValue().equals(Xml.TRUE);
        }
        if ((a = e.getAttribute(Xml.CABOOSE)) != null) {
            _caboose = a.getValue().equals(Xml.TRUE);
        }
        if ((a = e.getAttribute(Xml.FRED)) != null) {
            _fred = a.getValue().equals(Xml.TRUE);
        }
        if ((a = e.getAttribute(Xml.UTILITY)) != null) {
            _utility = a.getValue().equals(Xml.TRUE);
        }
        if ((a = e.getAttribute(Xml.KERNEL)) != null) {
            Kernel k = InstanceManager.getDefault(CarManager.class).getKernelByName(a.getValue());
            if (k != null) {
                setKernel(k);
                if ((a = e.getAttribute(Xml.LEAD_KERNEL)) != null && a.getValue().equals(Xml.TRUE)) {
                    _kernel.setLead(this);
                }
            } else {
                log.error("Kernel " + a.getValue() + " does not exist");
            }
        }
        if ((a = e.getAttribute(Xml.LOAD)) != null) {
            _loadName = a.getValue();
        }
        if ((a = e.getAttribute(Xml.LOAD_FROM_STAGING)) != null && a.getValue().equals(Xml.TRUE)) {
            setLoadGeneratedFromStaging(true);
        }

        if ((a = e.getAttribute(Xml.WAIT)) != null) {
            try {
                _wait = Integer.parseInt(a.getValue());
            } catch (NumberFormatException nfe) {
                log.error("Wait count ({}) for car ({}) isn't a valid number!", a.getValue(), toString());
            }
        }
        if ((a = e.getAttribute(Xml.PICKUP_SCHEDULE_ID)) != null) {
            _pickupScheduleId = a.getValue();
        }
        if ((a = e.getAttribute(Xml.SCHEDULE_ID)) != null) {
            _scheduleId = a.getValue();
        }
        if ((a = e.getAttribute(Xml.NEXT_LOAD)) != null) {
            _nextLoadName = a.getValue();
        }
        if ((a = e.getAttribute(Xml.NEXT_WAIT)) != null) {
            try {
                _nextWait = Integer.parseInt(a.getValue());
            } catch (NumberFormatException nfe) {
                log.error("Next wait count ({}) for car ({}) isn't a valid number!", a.getValue(), toString());
            }
        }
        if ((a = e.getAttribute(Xml.NEXT_PICKUP_SCHEDULE_ID)) != null) {
            _nextPickupScheduleId = a.getValue();
        }
        if ((a = e.getAttribute(Xml.NEXT_DEST_ID)) != null) {
            setFinalDestination(InstanceManager.getDefault(LocationManager.class).getLocationById(a.getValue()));
        }
        if (getFinalDestination() != null && (a = e.getAttribute(Xml.NEXT_DEST_TRACK_ID)) != null) {
            setFinalDestinationTrack(getFinalDestination().getTrackById(a.getValue()));
        }
        if ((a = e.getAttribute(Xml.PREVIOUS_NEXT_DEST_ID)) != null) {
            setPreviousFinalDestination(
                    InstanceManager.getDefault(LocationManager.class).getLocationById(a.getValue()));
        }
        if (getPreviousFinalDestination() != null && (a = e.getAttribute(Xml.PREVIOUS_NEXT_DEST_TRACK_ID)) != null) {
            setPreviousFinalDestinationTrack(getPreviousFinalDestination().getTrackById(a.getValue()));
        }
        if ((a = e.getAttribute(Xml.PREVIOUS_SCHEDULE_ID)) != null) {
            setPreviousScheduleId(a.getValue());
        }
        if ((a = e.getAttribute(Xml.RWE_DEST_ID)) != null) {
            _rweDestination = InstanceManager.getDefault(LocationManager.class).getLocationById(a.getValue());
        }
        if (_rweDestination != null && (a = e.getAttribute(Xml.RWE_DEST_TRACK_ID)) != null) {
            _rweDestTrack = _rweDestination.getTrackById(a.getValue());
        }
        if ((a = e.getAttribute(Xml.RWE_LOAD)) != null) {
            _rweLoadName = a.getValue();
        }
        addPropertyChangeListeners();
    }

    /**
     * Create an XML element to represent this Entry. This member has to remain
     * synchronized with the detailed DTD in operations-cars.dtd.
     *
     * @return Contents in a JDOM Element
     */
    public org.jdom2.Element store() {
        org.jdom2.Element e = new org.jdom2.Element(Xml.CAR);
        super.store(e);
        if (isPassenger()) {
            e.setAttribute(Xml.PASSENGER, isPassenger() ? Xml.TRUE : Xml.FALSE);
        }
        if (isHazardous()) {
            e.setAttribute(Xml.HAZARDOUS, isHazardous() ? Xml.TRUE : Xml.FALSE);
        }
        if (isCaboose()) {
            e.setAttribute(Xml.CABOOSE, isCaboose() ? Xml.TRUE : Xml.FALSE);
        }
        if (hasFred()) {
            e.setAttribute(Xml.FRED, hasFred() ? Xml.TRUE : Xml.FALSE);
        }
        if (isUtility()) {
            e.setAttribute(Xml.UTILITY, isUtility() ? Xml.TRUE : Xml.FALSE);
        }
        if (getKernel() != null) {
            e.setAttribute(Xml.KERNEL, getKernelName());
            if (isLead()) {
                e.setAttribute(Xml.LEAD_KERNEL, Xml.TRUE);
            }
        }

        e.setAttribute(Xml.LOAD, getLoadName());

        if (isLoadGeneratedFromStaging()) {
            e.setAttribute(Xml.LOAD_FROM_STAGING, Xml.TRUE);
        }

        if (getWait() != 0) {
            e.setAttribute(Xml.WAIT, Integer.toString(getWait()));
        }

        if (!getPickupScheduleId().equals(NONE)) {
            e.setAttribute(Xml.PICKUP_SCHEDULE_ID, getPickupScheduleId());
        }

        if (!getScheduleItemId().equals(NONE)) {
            e.setAttribute(Xml.SCHEDULE_ID, getScheduleItemId());
        }

        if (!getNextLoadName().equals(NONE)) {
            e.setAttribute(Xml.NEXT_LOAD, getNextLoadName());
        }

        if (getNextWait() != 0) {
            e.setAttribute(Xml.NEXT_WAIT, Integer.toString(getNextWait()));
        }

        if (!getNextPickupScheduleId().equals(NONE)) {
            e.setAttribute(Xml.NEXT_PICKUP_SCHEDULE_ID, getNextPickupScheduleId());
        }

        if (getFinalDestination() != null) {
            e.setAttribute(Xml.NEXT_DEST_ID, getFinalDestination().getId());
            if (getFinalDestinationTrack() != null) {
                e.setAttribute(Xml.NEXT_DEST_TRACK_ID, getFinalDestinationTrack().getId());
            }
        }
        if (getPreviousFinalDestination() != null) {
            e.setAttribute(Xml.PREVIOUS_NEXT_DEST_ID, getPreviousFinalDestination().getId());
            if (getPreviousFinalDestinationTrack() != null) {
                e.setAttribute(Xml.PREVIOUS_NEXT_DEST_TRACK_ID, getPreviousFinalDestinationTrack().getId());
            }
        }

        if (!getPreviousScheduleId().equals(NONE)) {
            e.setAttribute(Xml.PREVIOUS_SCHEDULE_ID, getPreviousScheduleId());
        }
        if (getReturnWhenEmptyDestination() != null) {
            e.setAttribute(Xml.RWE_DEST_ID, getReturnWhenEmptyDestination().getId());
            if (getReturnWhenEmptyDestTrack() != null) {
                e.setAttribute(Xml.RWE_DEST_TRACK_ID, getReturnWhenEmptyDestTrack().getId());
            }
        }
        if (!getReturnWhenEmptyLoadName().equals(carLoads.getDefaultEmptyName())) {
            e.setAttribute(Xml.RWE_LOAD, getReturnWhenEmptyLoadName());
        }

        return e;
    }

    @Override
    protected void setDirtyAndFirePropertyChange(String p, Object old, Object n) {
        // Set dirty
        InstanceManager.getDefault(CarManagerXml.class).setDirty(true);
        super.setDirtyAndFirePropertyChange(p, old, n);
    }

    private void addPropertyChangeListeners() {
        InstanceManager.getDefault(CarTypes.class).addPropertyChangeListener(this);
        InstanceManager.getDefault(CarLengths.class).addPropertyChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        super.propertyChange(e);
        if (e.getPropertyName().equals(CarTypes.CARTYPES_NAME_CHANGED_PROPERTY)) {
            if (e.getOldValue().equals(getTypeName())) {
                log.debug("Car ({}) sees type name change old: ({}) new: ({})", toString(), e.getOldValue(), e
                        .getNewValue()); // NOI18N
                setTypeName((String) e.getNewValue());
            }
        }
        if (e.getPropertyName().equals(CarLengths.CARLENGTHS_NAME_CHANGED_PROPERTY)) {
            if (e.getOldValue().equals(getLength())) {
                log.debug("Car ({}) sees length name change old: ({}) new: ({})", toString(), e.getOldValue(), e
                        .getNewValue()); // NOI18N
                setLength((String) e.getNewValue());
            }
        }
        if (e.getPropertyName().equals(Location.DISPOSE_CHANGED_PROPERTY)) {
            if (e.getSource() == _finalDestination) {
                log.debug("delete final destination for car: ({})", toString());
                setFinalDestination(null);
            }
        }
        if (e.getPropertyName().equals(Track.DISPOSE_CHANGED_PROPERTY)) {
            if (e.getSource() == _finalDestTrack) {
                log.debug("delete final destination for car: ({})", toString());
                setFinalDestinationTrack(null);
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(Car.class);

}
