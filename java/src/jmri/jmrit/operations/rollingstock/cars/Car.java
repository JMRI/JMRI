package jmri.jmrit.operations.rollingstock.cars;

import java.beans.PropertyChangeEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.operations.locations.*;
import jmri.jmrit.operations.locations.schedules.Schedule;
import jmri.jmrit.operations.locations.schedules.ScheduleItem;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.trains.TrainCommon;
import jmri.jmrit.operations.trains.schedules.TrainSchedule;
import jmri.jmrit.operations.trains.schedules.TrainScheduleManager;

/**
 * Represents a car on the layout
 *
 * @author Daniel Boudreau Copyright (C) 2008, 2009, 2010, 2012, 2013, 2014,
 *         2015, 2023
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

    protected Location _rwlDestination = null; // return when loaded destination
    protected Track _rwlDestTrack = null; // return when loaded track
    protected String _rwlLoadName = carLoads.getDefaultLoadName();

    // schedule items
    protected String _scheduleId = NONE; // the schedule id assigned to this car
    protected String _nextLoadName = NONE; // next load by schedule
    protected Location _finalDestination = null; 
    protected Track _finalDestTrack = null; // final track by schedule or router
    protected Location _previousFinalDestination = null;
    protected Track _previousFinalDestTrack = null;
    protected String _previousScheduleId = NONE;
    protected String _pickupScheduleId = NONE;

    protected String _routePath = NONE;

    public static final String EXTENSION_REGEX = " ";
    public static final String CABOOSE_EXTENSION = Bundle.getMessage("(C)");
    public static final String FRED_EXTENSION = Bundle.getMessage("(F)");
    public static final String PASSENGER_EXTENSION = Bundle.getMessage("(P)");
    public static final String UTILITY_EXTENSION = Bundle.getMessage("(U)");
    public static final String HAZARDOUS_EXTENSION = Bundle.getMessage("(H)");

    public static final String LOAD_CHANGED_PROPERTY = "Car load changed"; // NOI18N
    public static final String RWE_LOAD_CHANGED_PROPERTY = "Car RWE load changed"; // NOI18N
    public static final String RWL_LOAD_CHANGED_PROPERTY = "Car RWL load changed"; // NOI18N
    public static final String WAIT_CHANGED_PROPERTY = "Car wait changed"; // NOI18N
    public static final String FINAL_DESTINATION_CHANGED_PROPERTY = "Car final destination changed"; // NOI18N
    public static final String FINAL_DESTINATION_TRACK_CHANGED_PROPERTY = "Car final destination track changed"; // NOI18N
    public static final String RETURN_WHEN_EMPTY_CHANGED_PROPERTY = "Car return when empty changed"; // NOI18N
    public static final String RETURN_WHEN_LOADED_CHANGED_PROPERTY = "Car return when loaded changed"; // NOI18N
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
        car.setReturnWhenLoadedLoadName(getReturnWhenLoadedLoadName());
        car.setNumber(getNumber());
        car.setOwnerName(getOwnerName());
        car.setRoadName(getRoadName());
        car.setTypeName(getTypeName());
        car.setCaboose(isCaboose());
        car.setFred(hasFred());
        car.setPassenger(isPassenger());
        car.loaded = true;
        return car;
    }

    public void setCarHazardous(boolean hazardous) {
        boolean old = _hazardous;
        _hazardous = hazardous;
        if (!old == hazardous) {
            setDirtyAndFirePropertyChange("car hazardous", old ? "true" : "false", hazardous ? "true" : "false"); // NOI18N
        }
    }

    public boolean isCarHazardous() {
        return _hazardous;
    }

    public boolean isCarLoadHazardous() {
        return carLoads.isHazardous(getTypeName(), getLoadName());
    }

    /**
     * Used to determine if the car is hazardous or the car's load is hazardous.
     * 
     * @return true if the car or car's load is hazardous.
     */
    public boolean isHazardous() {
        return isCarHazardous() || isCarLoadHazardous();
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

    public void setReturnWhenEmptyLoadName(String load) {
        String old = _rweLoadName;
        _rweLoadName = load;
        if (!old.equals(load)) {
            setDirtyAndFirePropertyChange(RWE_LOAD_CHANGED_PROPERTY, old, load);
        }
    }

    public String getReturnWhenEmptyLoadName() {
        return _rweLoadName;
    }

    public void setReturnWhenLoadedLoadName(String load) {
        String old = _rwlLoadName;
        _rwlLoadName = load;
        if (!old.equals(load)) {
            setDirtyAndFirePropertyChange(RWL_LOAD_CHANGED_PROPERTY, old, load);
        }
    }

    public String getReturnWhenLoadedLoadName() {
        return _rwlLoadName;
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

    public ScheduleItem getScheduleItem(Track track) {
        ScheduleItem si = null;
        // arrived at spur?
        if (track != null && track.isSpur() && !getScheduleItemId().equals(NONE)) {
            Schedule sch = track.getSchedule();
            if (sch == null) {
                log.error("Schedule null for car ({}) at spur ({})", toString(), track.getName());
            } else {
                si = sch.getItemById(getScheduleItemId());
            }
        }
        return si;
    }

    /**
     * Only here for backwards compatibility before version 5.1.4. The next load
     * name for this car. Normally set by a schedule.
     * 
     * @param load the next load name.
     */
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
            setDirtyAndFirePropertyChange(WAIT_CHANGED_PROPERTY, old, count);
        }
    }

    public int getWait() {
        return _wait;
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

    public String getPickupScheduleName() {
        TrainSchedule sch = InstanceManager.getDefault(TrainScheduleManager.class)
                .getScheduleById(getPickupScheduleId());
        if (sch != null) {
            return sch.getName();
        }
        return NONE;
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
        if ((old != null && !old.equals(destination)) || (destination != null && !destination.equals(old))) {
            setRoutePath(NONE);
            setDirtyAndFirePropertyChange(FINAL_DESTINATION_CHANGED_PROPERTY, old, destination);
        }
    }

    public Location getFinalDestination() {
        return _finalDestination;
    }
    
    public String getFinalDestinationName() {
        if (getFinalDestination() != null) {
            return getFinalDestination().getName();
        }
        return NONE;
    }
    
    public String getSplitFinalDestinationName() {
        return TrainCommon.splitString(getFinalDestinationName());
    }

    public void setFinalDestinationTrack(Track track) {
        Track old = _finalDestTrack;
        _finalDestTrack = track;
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

    public Track getFinalDestinationTrack() {
        return _finalDestTrack;
    }

    public String getFinalDestinationTrackName() {
        if (getFinalDestinationTrack() != null) {
            return getFinalDestinationTrack().getName();
        }
        return NONE;
    }
    
    public String getSplitFinalDestinationTrackName() {
        return TrainCommon.splitString(getFinalDestinationTrackName());
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
        }
        return NONE;
    }
    
    public String getSplitReturnWhenEmptyDestinationName() {
        return TrainCommon.splitString(getReturnWhenEmptyDestinationName());
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
        }
        return NONE;
    }
    
    public String getSplitReturnWhenEmptyDestinationTrackName() {
        return TrainCommon.splitString(getReturnWhenEmptyDestTrackName());
    }

    public void setReturnWhenLoadedDestination(Location destination) {
        Location old = _rwlDestination;
        _rwlDestination = destination;
        if ((old != null && !old.equals(destination)) || (destination != null && !destination.equals(old))) {
            setDirtyAndFirePropertyChange(RETURN_WHEN_LOADED_CHANGED_PROPERTY, null, null);
        }
    }

    public Location getReturnWhenLoadedDestination() {
        return _rwlDestination;
    }

    public String getReturnWhenLoadedDestinationName() {
        if (getReturnWhenLoadedDestination() != null) {
            return getReturnWhenLoadedDestination().getName();
        }
        return NONE;
    }

    public void setReturnWhenLoadedDestTrack(Track track) {
        Track old = _rwlDestTrack;
        _rwlDestTrack = track;
        if ((old != null && !old.equals(track)) || (track != null && !track.equals(old))) {
            setDirtyAndFirePropertyChange(RETURN_WHEN_LOADED_CHANGED_PROPERTY, null, null);
        }
    }

    public Track getReturnWhenLoadedDestTrack() {
        return _rwlDestTrack;
    }

    public String getReturnWhenLoadedDestTrackName() {
        if (getReturnWhenLoadedDestTrack() != null) {
            return getReturnWhenLoadedDestTrack().getName();
        }
        return NONE;
    }

    /**
     * Used to determine is car has been given a Return When Loaded (RWL)
     * address or custom load
     * 
     * @return true if car has RWL
     */
    protected boolean isRwlEnabled() {
        if (!getReturnWhenLoadedLoadName().equals(carLoads.getDefaultLoadName()) ||
                getReturnWhenLoadedDestination() != null) {
            return true;
        }
        return false;
    }

    public void setRoutePath(String routePath) {
        String old = _routePath;
        _routePath = routePath;
        if (!old.equals(routePath)) {
            setDirtyAndFirePropertyChange("Route path change", old, routePath);
        }
    }

    public String getRoutePath() {
        return _routePath;
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

    /**
     * Used to determine if car is performing a local move. A local move is when
     * a car is moved to a different track at the same location. Car has to be
     * assigned to a train.
     * 
     * @return true if local move
     */
    public boolean isLocalMove() {
        if (getTrain() == null && getLocation() != null) {
            return getSplitLocationName().equals(getSplitDestinationName());
        }
        if (getRouteLocation() == null || getRouteDestination() == null) {
            return false;
        }
        if (getRouteLocation().equals(getRouteDestination()) && getTrack() != null) {
            return true;
        }
        if (getTrain().isLocalSwitcher() &&
                getRouteLocation().getSplitName()
                        .equals(getRouteDestination().getSplitName()) &&
                getTrack() != null) {
            return true;
        }
        // look for sequential locations with the "same" name
        if (getRouteLocation().getSplitName().equals(
                getRouteDestination().getSplitName()) && getTrain().getRoute() != null) {
            boolean foundRl = false;
            for (RouteLocation rl : getTrain().getRoute().getLocationsBySequenceList()) {
                if (foundRl) {
                    if (getRouteDestination().getSplitName()
                            .equals(rl.getSplitName())) {
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
     * 
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
                car.setScheduleItemId(getScheduleItemId());
                car.setFinalDestination(getFinalDestination());
                car.setFinalDestinationTrack(getFinalDestinationTrack());
                car.setLoadGeneratedFromStaging(isLoadGeneratedFromStaging());
                if (InstanceManager.getDefault(CarLoads.class).containsName(car.getTypeName(), getLoadName())) {
                    car.setLoadName(getLoadName());
                }
            }
        }
    }

    /**
     * Used to determine if a car can be set out at a destination (location).
     * Track is optional. In addition to all of the tests that checkDestination
     * performs, spurs with schedules are also checked.
     *
     * @return status OKAY, TYPE, ROAD, LENGTH, ERROR_TRACK, CAPACITY, SCHEDULE,
     *         CUSTOM
     */
    @Override
    public String checkDestination(Location destination, Track track) {
        String status = super.checkDestination(destination, track);
        if (!status.equals(Track.OKAY) && !status.startsWith(Track.LENGTH)) {
            return status;
        }
        // now check to see if the track has a schedule
        if (track == null) {
            return status;
        }
        String statusSchedule = track.checkSchedule(this);
        if (status.startsWith(Track.LENGTH) && statusSchedule.equals(Track.OKAY)) {
            return status;
        }
        return statusSchedule;
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
     * @param force when true ignore track length, type, and road when setting
     *              destination
     * @return "okay" if successful, "type" if the rolling stock's type isn't
     *         acceptable, or "length" if the rolling stock length didn't fit,
     *         or Schedule if the destination will not accept the car because
     *         the spur has a schedule and the car doesn't meet the schedule
     *         requirements. Also changes the car load status when the car
     *         reaches its destination.
     */
    @Override
    public String setDestination(Location destination, Track track, boolean force) {
        // save destination name and track in case car has reached its
        // destination
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
        // car was in a train and has been dropped off, update load, RWE could
        // set a new final destination
        loadNext(destinationTrack);
        return status;
    }

    /**
     * Called when setting a car's destination to this spur. Loads the car with
     * a final destination which is the ship address for the schedule item.
     * 
     * @param scheduleItem The schedule item to be applied this this car
     */
    public void loadNext(ScheduleItem scheduleItem) {
        if (scheduleItem == null) {
            return; // should never be null
        }
        // set the car's final destination and track
        setFinalDestination(scheduleItem.getDestination());
        setFinalDestinationTrack(scheduleItem.getDestinationTrack());
        // bump hit count for this schedule item
        scheduleItem.setHits(scheduleItem.getHits() + 1);
        // set all cars in kernel same final destination
        updateKernel();
    }

    /**
     * Called when car is delivered to track. Updates the car's wait, pickup
     * day, and load if spur. If staging, can swap default loads, force load to
     * default empty, or replace custom loads with the default empty load. Can
     * trigger RWE or RWL.
     * 
     * @param track the destination track for this car
     */
    public void loadNext(Track track) {
        setLoadGeneratedFromStaging(false);
        if (track != null) {
            if (track.isSpur()) {
                ScheduleItem si = getScheduleItem(track);
                if (si == null) {
                    log.debug("Schedule item ({}) is null for car ({}) at spur ({})", getScheduleItemId(), toString(),
                            track.getName());
                } else {
                    setWait(si.getWait());
                    setPickupScheduleId(si.getPickupTrainScheduleId());
                }
                updateLoad(track);
            }
            // update load optionally when car reaches staging
            else if (track.isStaging()) {
                if (track.isLoadSwapEnabled() && getLoadName().equals(carLoads.getDefaultEmptyName())) {
                    setLoadLoaded();
                } else if ((track.isLoadSwapEnabled() || track.isLoadEmptyEnabled()) &&
                        getLoadName().equals(carLoads.getDefaultLoadName())) {
                    setLoadEmpty();
                } else if (track.isRemoveCustomLoadsEnabled() &&
                        !getLoadName().equals(carLoads.getDefaultEmptyName()) &&
                        !getLoadName().equals(carLoads.getDefaultLoadName())) {
                    // remove this car's final destination if it has one
                    setFinalDestination(null);
                    setFinalDestinationTrack(null);
                    if (getLoadType().equals(CarLoad.LOAD_TYPE_EMPTY) && isRwlEnabled()) {
                        setLoadLoaded();
                        // car arriving into staging with the RWE load?
                    } else if (getLoadName().equals(getReturnWhenEmptyLoadName())) {
                        setLoadName(carLoads.getDefaultEmptyName());
                    } else {
                        setLoadEmpty(); // note that RWE sets the car's final
                                        // destination
                    }
                }
            }
        }
    }

    /**
     * Updates a car's load when placed at a spur. Load change delayed if wait
     * count is greater than zero. 
     * 
     * @param track The spur the car is sitting on
     */
    public void updateLoad(Track track) {
        if (track.isDisableLoadChangeEnabled()) {
            return;
        }
        if (getWait() > 0) {
            return; // change load name when wait count reaches 0
        }
        // arriving at spur with a schedule?
        String loadName = NONE;
        ScheduleItem si = getScheduleItem(track);
        if (si != null) {
            loadName = si.getShipLoadName(); // can be NONE
        } else {
            // for backwards compatibility before version 5.1.4
            log.debug("Schedule item ({}) is null for car ({}) at spur ({}), using next load name", getScheduleItemId(),
                    toString(), track.getName());
            loadName = getNextLoadName();
        }
        setNextLoadName(NONE);
        if (!loadName.equals(NONE)) {
            setLoadName(loadName);
            // RWE or RWL load and no destination?
            if (getLoadName().equals(getReturnWhenEmptyLoadName()) && getFinalDestination() == null) {
                setReturnWhenEmpty();
            } else if (getLoadName().equals(getReturnWhenLoadedLoadName()) && getFinalDestination() == null) {
                setReturnWhenLoaded();
            }
        } else {
            // flip load names
            if (getLoadType().equals(CarLoad.LOAD_TYPE_EMPTY)) {
                setLoadLoaded();
            } else {
                setLoadEmpty();
            }
        }
        setScheduleItemId(Car.NONE);
    }

    /**
     * Sets the car's load to empty, triggers RWE load and destination if
     * enabled.
     */
    private void setLoadEmpty() {
        if (!getLoadName().equals(getReturnWhenEmptyLoadName())) {
            setLoadName(getReturnWhenEmptyLoadName()); // default RWE load is
                                                       // the "E" load
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

    /**
     * Sets the car's load to loaded, triggers RWL load and destination if
     * enabled.
     */
    private void setLoadLoaded() {
        if (!getLoadName().equals(getReturnWhenLoadedLoadName())) {
            setLoadName(getReturnWhenLoadedLoadName()); // default RWL load is
                                                        // the "L" load
            setReturnWhenLoaded();
        }
    }

    /*
     * Don't set return address if in staging with the same RWL address and
     * don't set return address if at the RWL address
     */
    private void setReturnWhenLoaded() {
        if (getReturnWhenLoadedDestination() != null &&
                (getLocation() != getReturnWhenLoadedDestination() ||
                        (!getReturnWhenLoadedDestination().isStaging() &&
                                getTrack() != getReturnWhenLoadedDestTrack()))) {
            setFinalDestination(getReturnWhenLoadedDestination());
            if (getReturnWhenLoadedDestTrack() != null) {
                setFinalDestinationTrack(getReturnWhenLoadedDestTrack());
            }
            log.debug("Car ({}) has return when loaded destination ({}, {}) load {}", toString(),
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
        if (isCarHazardous()) {
            buf.append(EXTENSION_REGEX + HAZARDOUS_EXTENSION);
        }
        return buf.toString();
    }

    @Override
    public void reset() {
        setScheduleItemId(getPreviousScheduleId()); // revert to previous
        setNextLoadName(NONE);
        setFinalDestination(getPreviousFinalDestination());
        setFinalDestinationTrack(getPreviousFinalDestinationTrack());
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
            Kernel k = InstanceManager.getDefault(KernelManager.class).getKernelByName(a.getValue());
            if (k != null) {
                setKernel(k);
                if ((a = e.getAttribute(Xml.LEAD_KERNEL)) != null && a.getValue().equals(Xml.TRUE)) {
                    _kernel.setLead(this);
                }
            } else {
                log.error("Kernel {} does not exist", a.getValue());
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
        // for backwards compatibility before version 5.1.4
        if ((a = e.getAttribute(Xml.NEXT_LOAD)) != null) {
            _nextLoadName = a.getValue();
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
        if ((a = e.getAttribute(Xml.RWL_DEST_ID)) != null) {
            _rwlDestination = InstanceManager.getDefault(LocationManager.class).getLocationById(a.getValue());
        }
        if (_rwlDestination != null && (a = e.getAttribute(Xml.RWL_DEST_TRACK_ID)) != null) {
            _rwlDestTrack = _rwlDestination.getTrackById(a.getValue());
        }
        if ((a = e.getAttribute(Xml.RWL_LOAD)) != null) {
            _rwlLoadName = a.getValue();
        }
        if ((a = e.getAttribute(Xml.ROUTE_PATH)) != null) {
            _routePath = a.getValue();
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
        if (isCarHazardous()) {
            e.setAttribute(Xml.HAZARDOUS, isCarHazardous() ? Xml.TRUE : Xml.FALSE);
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

        // for backwards compatibility before version 5.1.4
        if (!getNextLoadName().equals(NONE)) {
            e.setAttribute(Xml.NEXT_LOAD, getNextLoadName());
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

        if (getReturnWhenLoadedDestination() != null) {
            e.setAttribute(Xml.RWL_DEST_ID, getReturnWhenLoadedDestination().getId());
            if (getReturnWhenLoadedDestTrack() != null) {
                e.setAttribute(Xml.RWL_DEST_TRACK_ID, getReturnWhenLoadedDestTrack().getId());
            }
        }
        if (!getReturnWhenLoadedLoadName().equals(carLoads.getDefaultLoadName())) {
            e.setAttribute(Xml.RWL_LOAD, getReturnWhenLoadedLoadName());
        }

        if (!getRoutePath().isEmpty()) {
            e.setAttribute(Xml.ROUTE_PATH, getRoutePath());
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
                log.debug("Car ({}) sees type name change old: ({}) new: ({})", toString(), e.getOldValue(),
                        e.getNewValue()); // NOI18N
                setTypeName((String) e.getNewValue());
            }
        }
        if (e.getPropertyName().equals(CarLengths.CARLENGTHS_NAME_CHANGED_PROPERTY)) {
            if (e.getOldValue().equals(getLength())) {
                log.debug("Car ({}) sees length name change old: ({}) new: ({})", toString(), e.getOldValue(),
                        e.getNewValue()); // NOI18N
                setLength((String) e.getNewValue());
            }
        }
        if (e.getPropertyName().equals(Location.DISPOSE_CHANGED_PROPERTY)) {
            if (e.getSource() == getFinalDestination()) {
                log.debug("delete final destination for car: ({})", toString());
                setFinalDestination(null);
            }
        }
        if (e.getPropertyName().equals(Track.DISPOSE_CHANGED_PROPERTY)) {
            if (e.getSource() == getFinalDestinationTrack()) {
                log.debug("delete final destination for car: ({})", toString());
                setFinalDestinationTrack(null);
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(Car.class);

}
