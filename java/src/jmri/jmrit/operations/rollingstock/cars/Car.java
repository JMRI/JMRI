package jmri.jmrit.operations.rollingstock.cars;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.beans.PropertyChangeEvent;
import java.util.List;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.RollingStock;

/**
 * Represents a car on the layout
 * 
 * @author Daniel Boudreau Copyright (C) 2008, 2009, 2010, 2012, 2013
 * @version $Revision$
 */
public class Car extends RollingStock {

	CarLoads carLoads = CarLoads.instance();

	protected boolean _passenger = false;
	protected boolean _hazardous = false;
	protected boolean _caboose = false;
	protected boolean _fred = false;
	protected boolean _utility = false;
	protected boolean _loadGeneratedByStaging = false;
	protected Kernel _kernel = null;
	protected String _load = carLoads.getDefaultEmptyName();
	protected int _wait = 0;
	protected int _order = 0; // interchange service ordering

	protected Location _rweDestination = null; // return when empty destination
	protected Track _rweDestTrack = null; // return when empty track

	// schedule items
	protected String _scheduleId = ""; // the schedule id assigned to this car
	protected String _nextLoad = ""; // next load by schedule
	protected int _nextWait = 0; // next wait by schedule
	protected Location _finalDestination = null; // final destination by schedule or router
	protected Track _finalDestTrack = null; // final track by schedule or router
	protected Location _previousFinalDestination = null; // previous final destination (for train resets)
	protected Track _previousFinalDestTrack = null; // previous final track (for train resets)
	protected String _previousScheduleId = ""; // previous schedule id (for train resets)

	public static final String LOAD_CHANGED_PROPERTY = "Car load changed"; // NOI18N property change descriptions
	public static final String WAIT_CHANGED_PROPERTY = "Car wait changed"; // NOI18N
	public static final String NEXT_WAIT_CHANGED_PROPERTY = "Car next wait changed"; // NOI18N
	public static final String FINAL_DESTINATION_CHANGED_PROPERTY = "Car final destination changed"; // NOI18N
	public static final String FINAL_DESTINATION_TRACK_CHANGED_PROPERTY = "Car final destination track changed"; // NOI18N
	public static final String RETURN_WHEN_EMPTY_CHANGED_PROPERTY = "Car return when empty changed"; // NOI18N

	public Car() {

	}

	public Car(String road, String number) {
		super(road, number);
		log.debug("New car " + road + " " + number);
		addPropertyChangeListeners();
	}

	public void setHazardous(boolean hazardous) {
		boolean old = _hazardous;
		_hazardous = hazardous;
		if (!old == hazardous)
			firePropertyChange("car hazardous", old ? "true" : "false", hazardous ? "true" : "false"); // NOI18N
	}

	public boolean isHazardous() {
		return _hazardous;
	}

	public void setPassenger(boolean passenger) {
		boolean old = _passenger;
		_passenger = passenger;
		if (!old == passenger)
			firePropertyChange("car passenger", old ? "true" : "false", passenger ? "true" : "false"); // NOI18N
	}

	public boolean isPassenger() {
		return _passenger;
	}

	public void setFred(boolean fred) {
		boolean old = _fred;
		_fred = fred;
		if (!old == fred)
			firePropertyChange("car has fred", old ? "true" : "false", fred ? "true" : "false"); // NOI18N
	}

	public boolean hasFred() {
		return _fred;
	}

	public void setLoadName(String load) {
		String old = _load;
		_load = load;
		if (!old.equals(load))
			firePropertyChange(LOAD_CHANGED_PROPERTY, old, load);
	}

	public String getLoadName() {
		return _load;
	}

	/**
	 * Gets the car load's priority.
	 */
	public String getLoadPriority() {
		return (carLoads.getPriority(_type, _load));
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

	public void setScheduleId(String id) {
		// log.debug("set schedule id ("+id+") for car ("+toString()+")");
		String old = _scheduleId;
		_scheduleId = id;
		if (!old.equals(id))
			firePropertyChange("car schedule id changed", old, id); // NOI18N
	}

	public String getScheduleId() {
		return _scheduleId;
	}

	public void setNextLoadName(String load) {
		String old = _nextLoad;
		_nextLoad = load;
		if (!old.equals(load))
			firePropertyChange(LOAD_CHANGED_PROPERTY, old, load);
	}

	public String getNextLoadName() {
		return _nextLoad;
	}

	public String getWeightTons() {
		String weight = super.getWeightTons();
		if (!_weightTons.equals(DEFAULT_WEIGHT))
			return weight;
		if (!isCaboose() && !isPassenger())
			return weight;
		// .9 tons/foot for caboose and passenger cars
		try {
			weight = Integer.toString((int) (Double.parseDouble(getLength()) * .9));
		} catch (Exception e) {
			log.debug("Car (" + toString() + ") length not set for caboose or passenger car");
		}
		return weight;
	}

	/**
	 * Returns a car's weight adjusted for load. An empty car's weight is 1/3 the car's loaded weight.
	 */
	public int getAdjustedWeightTons() {
		int weightTons = 0;
		try {
			// get loaded weight
			weightTons = Integer.parseInt(getWeightTons());
			// adjust for empty weight if car is empty, 1/3 of loaded weight
			if (!isCaboose() && !isPassenger()
					&& CarLoads.instance().getLoadType(getTypeName(), getLoadName()).equals(CarLoad.LOAD_TYPE_EMPTY))
				weightTons = weightTons / 3;
		} catch (Exception e) {
			log.debug("Car (" + toString() + ") weight not set");
		}
		return weightTons;
	}

	public void setWait(int count) {
		int old = _wait;
		_wait = count;
		if (old != count)
			firePropertyChange(NEXT_WAIT_CHANGED_PROPERTY, old, count);
	}

	public int getWait() {
		return _wait;
	}

	/**
	 * This car's service order when placed at a track that considers car order. There are two track orders, FIFO and
	 * LIFO. Car's with the lowest numbers are serviced first when placed at a track in FIFO mode. Car's with the
	 * highest numbers are serviced first when place at a track in LIFO mode.
	 * 
	 * @param number
	 *            The assigned service order for this car.
	 */
	public void setOrder(int number) {
		int old = _order;
		_order = number;
		if (old != number)
			firePropertyChange("car order changed", old, number); // NOI18N
	}

	public int getOrder() {
		return _order;
	}

	public void setNextWait(int count) {
		int old = _nextWait;
		_nextWait = count;
		if (old != count)
			firePropertyChange(NEXT_WAIT_CHANGED_PROPERTY, old, count);
	}

	public int getNextWait() {
		return _nextWait;
	}

	/**
	 * Sets the final destination for a car.
	 * 
	 * @param destination
	 *            The final destination for this car.
	 */
	public void setFinalDestination(Location destination) {
		Location old = _finalDestination;
		if (old != null)
			old.removePropertyChangeListener(this);
		_finalDestination = destination;
		if (_finalDestination != null)
			_finalDestination.addPropertyChangeListener(this);
		// log.debug("Next destination for car ("+toString()+") old: "+old+" new: "+destination);
		if ((old != null && !old.equals(destination)) || (destination != null && !destination.equals(old)))
			firePropertyChange(FINAL_DESTINATION_CHANGED_PROPERTY, old, destination);
	}

	public Location getFinalDestination() {
		return _finalDestination;
	}

	public String getFinalDestinationName() {
		if (_finalDestination != null)
			return _finalDestination.getName();
		return "";
	}

	public void setFinalDestinationTrack(Track track) {
		Track old = _finalDestTrack;
		_finalDestTrack = track;
		if (track == null)
			setScheduleId("");
		if ((old != null && !old.equals(track)) || (track != null && !track.equals(old))) {
			if (old != null) {
				old.removePropertyChangeListener(this);
				old.deleteReservedInRoute(this);
			}
			if (_finalDestTrack != null) {
				_finalDestTrack.addReservedInRoute(this);
				_finalDestTrack.addPropertyChangeListener(this);
			}
			firePropertyChange(FINAL_DESTINATION_TRACK_CHANGED_PROPERTY, old, track);
		}
	}

	public Track getFinalDestinationTrack() {
		return _finalDestTrack;
	}

	public String getFinalDestinationTrackName() {
		if (_finalDestTrack != null)
			return _finalDestTrack.getName();
		return "";
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
		if ((old != null && !old.equals(destination)) || (destination != null && !destination.equals(old)))
			firePropertyChange(RETURN_WHEN_EMPTY_CHANGED_PROPERTY, null, null);
	}

	public Location getReturnWhenEmptyDestination() {
		return _rweDestination;
	}

	public String getReturnWhenEmptyDestinationName() {
		if (getReturnWhenEmptyDestination() != null)
			return getReturnWhenEmptyDestination().getName();
		else
			return "";

	}

	public void setReturnWhenEmptyDestTrack(Track track) {
		Track old = _rweDestTrack;
		_rweDestTrack = track;
		if ((old != null && !old.equals(track)) || (track != null && !track.equals(old)))
			firePropertyChange(RETURN_WHEN_EMPTY_CHANGED_PROPERTY, null, null);

	}

	public Track getReturnWhenEmptyDestTrack() {
		return _rweDestTrack;
	}

	public String getReturnWhenEmptyDestTrackName() {
		if (getReturnWhenEmptyDestTrack() != null)
			return getReturnWhenEmptyDestTrack().getName();
		else
			return "";
	}

	public String getReturnWhenEmptyDestName() {
		if (getReturnWhenEmptyDestination() != null)
			return getReturnWhenEmptyDestinationName() + "(" + getReturnWhenEmptyDestTrackName() + ")";
		else
			return "";
	}

	public void setCaboose(boolean caboose) {
		boolean old = _caboose;
		_caboose = caboose;
		if (!old == caboose)
			firePropertyChange("car is caboose", old ? "true" : "false", caboose ? "true" : "false"); // NOI18N
	}

	public boolean isCaboose() {
		return _caboose;
	}

	public void setUtility(boolean utility) {
		boolean old = _utility;
		_utility = utility;
		if (!old == utility)
			firePropertyChange("car is utility", old ? "true" : "false", utility ? "true" : "false"); // NOI18N
	}

	public boolean isUtility() {
		return _utility;
	}

	/**
	 * A kernel is a group of cars that are switched as a unit.
	 * 
	 * @param kernel
	 */
	public void setKernel(Kernel kernel) {
		if (_kernel == kernel)
			return;
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
		if (!old.equals(newName))
			firePropertyChange("kernel name changed", old, newName); // NOI18N
	}

	public Kernel getKernel() {
		return _kernel;
	}

	public String getKernelName() {
		if (_kernel != null)
			return _kernel.getName();
		return "";
	}
	
	/**
	 * Updates all cars in a kernel. After the update, the cars will all have the same final destination, load, and next
	 * load.
	 */
	public void updateKernel() {
		if (getKernel() != null && getKernel().isLead(this)) {
			List<Car> cars = getKernel().getCars();
			for (int i = 0; i < cars.size(); i++) {
				Car c = cars.get(i);
				c.setFinalDestination(getFinalDestination());
				c.setFinalDestinationTrack(getFinalDestinationTrack());
				c.setLoadGeneratedFromStaging(isLoadGeneratedFromStaging());
				if (CarLoads.instance().containsName(c.getTypeName(), getLoadName()))
					c.setLoadName(getLoadName());
				if (CarLoads.instance().containsName(c.getTypeName(), getNextLoadName()))
					c.setNextLoadName(getNextLoadName());
			}
		}
	}

	/**
	 * Used to determine if a car can be set out at a destination (location). Track is optional. In addition to all of
	 * the tests that testLocation performs, spurs with schedules are also checked.
	 * 
	 * @return status OKAY, TYPE, ROAD, LENGTH, ERROR_TRACK, CAPACITY, SCHEDULE, CUSTOM
	 */
	public String testDestination(Location destination, Track track) {
		String status = super.testDestination(destination, track);
		if (!status.equals(Track.OKAY))
			return status;
		// now check to see if the track has a schedule
		if (track == null)
			return status;
		return track.checkSchedule(this);
	}

	/**
	 * Sets the car's destination on the layout
	 * 
	 * @param destination
	 * @param track
	 *            (yard, spur, staging, or interchange track)
	 * @return "okay" if successful, "type" if the rolling stock's type isn't acceptable, or "length" if the rolling
	 *         stock length didn't fit, or Schedule if the destination will not accept the car because the spur has a
	 *         schedule and the car doesn't meet the schedule requirements. Also changes the car load status when the
	 *         car reaches its destination.
	 */
	public String setDestination(Location destination, Track track) {
		return setDestination(destination, track, false);
	}

	/**
	 * Sets the car's destination on the layout
	 * 
	 * @param destination
	 * @param track
	 *            (yard, spur, staging, or interchange track)
	 * @param force
	 *            when true ignore track length, type, & road when setting destination
	 * @return "okay" if successful, "type" if the rolling stock's type isn't acceptable, or "length" if the rolling
	 *         stock length didn't fit, or Schedule if the destination will not accept the car because the spur has a
	 *         schedule and the car doesn't meet the schedule requirements. Also changes the car load status when the
	 *         car reaches its destination.
	 */
	public String setDestination(Location destination, Track track, boolean force) {
		// save destination name and track in case car has reached its destination
		String destinationName = getDestinationName();
		Track oldDestTrack = getDestinationTrack();
		String status = super.setDestination(destination, track, force);
		// return if not Okay
		if (!status.equals(Track.OKAY))
			return status;
		// now check to see if the track has a schedule
		if (track != null && oldDestTrack != track && !loading) {
			status = track.scheduleNext(this);
			if (!status.equals(Track.OKAY))
				return status;
		}
		// update final destination and load only when car reaches destination and was in train
		if (destinationName.equals("") || (destination != null) || getTrain() == null)
			return status;
		// set service order for LIFO and FIFO tracks
		if (oldDestTrack != null)
			setOrder(oldDestTrack.getMoves());
		// update load when car reaches a spur
		loadNext(oldDestTrack);
		return status;
	}

	private void loadNext(Track destTrack) {
		setLoadGeneratedFromStaging(false);
		// update wait count
		setWait(getNextWait());
		setNextWait(0);
		if (destTrack != null && destTrack.getLocType().equals(Track.SPUR)) {
			if (!getNextLoadName().equals("")) {
				setLoadName(getNextLoadName());
				setNextLoadName("");
				// is the next load default empty? Check for car return when empty
				if (getLoadName().equals(carLoads.getDefaultEmptyName()) && getFinalDestination() == null)
					setLoadEmpty();
				return;
			}
			// if car doesn't have a schedule load, flip load status
			if (getLoadName().equals(carLoads.getDefaultEmptyName()))
				setLoadName(carLoads.getDefaultLoadName());
			else
				setLoadEmpty();
		}
		// update load optionally when car reaches staging
		if (destTrack != null && destTrack.getLocType().equals(Track.STAGING)) {
			if (destTrack.isLoadSwapEnabled()) {
				if (getLoadName().equals(carLoads.getDefaultEmptyName())) {
					setLoadName(carLoads.getDefaultLoadName());
				} else if (getLoadName().equals(carLoads.getDefaultLoadName())) {
					setLoadEmpty();
				}
			}
			if (destTrack.isSetLoadEmptyEnabled() && getLoadName().equals(carLoads.getDefaultLoadName())) {
				setLoadEmpty();
			}
			// empty car if it has a schedule load
			if (destTrack.isRemoveLoadsEnabled() && !getLoadName().equals(carLoads.getDefaultEmptyName())
					&& !getLoadName().equals(carLoads.getDefaultLoadName())) {
				setLoadEmpty();
				// remove this car's final destination if it has one
				setFinalDestination(null);
				setFinalDestinationTrack(null);
			}
		}
	}

	private void setLoadEmpty() {
		setLoadName(carLoads.getDefaultEmptyName());
		if (getReturnWhenEmptyDestination() != null) {
			setFinalDestination(getReturnWhenEmptyDestination());
			if (getReturnWhenEmptyDestTrack() != null) {
				setFinalDestinationTrack(getReturnWhenEmptyDestTrack());
			}
			log.debug("Car (" + toString() + ") has return when empty destination ("
					+ getFinalDestinationName() + ", " + getFinalDestinationTrackName() + ")");
		}
	}

	protected void reset() {
		setScheduleId(getPreviousScheduleId());	// revert to previous
		setNextLoadName("");
		setNextWait(0);
		setFinalDestination(getPreviousFinalDestination()); // revert to previous
		setFinalDestinationTrack(getPreviousFinalDestinationTrack()); // revert to previous
		if (isLoadGeneratedFromStaging()) {
			setLoadGeneratedFromStaging(false);
			setLoadName(CarLoads.instance().getDefaultEmptyName());
		}

		super.reset();
	}

	public void dispose() {
		setKernel(null);
		setFinalDestination(null); // removes property change listener
		setFinalDestinationTrack(null); // removes property change listener
		CarTypes.instance().removePropertyChangeListener(this);
		CarLengths.instance().removePropertyChangeListener(this);
		super.dispose();
	}

	// used to stop a track's schedule from bumping when loading car database
	private boolean loading = false;

	/**
	 * Construct this Entry from XML. This member has to remain synchronized with the detailed DTD in
	 * operations-cars.dtd
	 * 
	 * @param e
	 *            Car XML element
	 */
	public Car(org.jdom.Element e) {
		loading = true; // stop track schedule from bumping
		super.rollingStock(e);
		loading = false;
		org.jdom.Attribute a;
		if ((a = e.getAttribute(Xml.PASSENGER)) != null)
			_passenger = a.getValue().equals(Xml.TRUE);
		if ((a = e.getAttribute(Xml.HAZARDOUS)) != null)
			_hazardous = a.getValue().equals(Xml.TRUE);
		if ((a = e.getAttribute(Xml.CABOOSE)) != null)
			_caboose = a.getValue().equals(Xml.TRUE);
		if ((a = e.getAttribute(Xml.FRED)) != null)
			_fred = a.getValue().equals(Xml.TRUE);
		if ((a = e.getAttribute(Xml.UTILITY)) != null)
			_utility = a.getValue().equals(Xml.TRUE);
		if ((a = e.getAttribute(Xml.KERNEL)) != null) {
			Kernel k = CarManager.instance().getKernelByName(a.getValue());
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
			_load = a.getValue();
		}
		if ((a = e.getAttribute(Xml.LOAD_FROM_STAGING)) != null && a.getValue().equals(Xml.TRUE)) {
			setLoadGeneratedFromStaging(true);
		}

		if ((a = e.getAttribute(Xml.WAIT)) != null) {
			_wait = Integer.parseInt(a.getValue());
		}
		if ((a = e.getAttribute(Xml.SCHEDULE_ID)) != null) {
			_scheduleId = a.getValue();
		}
		if ((a = e.getAttribute(Xml.NEXT_LOAD)) != null) {
			_nextLoad = a.getValue();
		}
		if ((a = e.getAttribute(Xml.NEXT_WAIT)) != null) {
			_nextWait = Integer.parseInt(a.getValue());
		}
		if ((a = e.getAttribute(Xml.NEXT_DEST_ID)) != null) {
			setFinalDestination(LocationManager.instance().getLocationById(a.getValue()));
		}
		if (getFinalDestination() != null && (a = e.getAttribute(Xml.NEXT_DEST_TRACK_ID)) != null) {
			setFinalDestinationTrack(getFinalDestination().getTrackById(a.getValue()));
		}
		if ((a = e.getAttribute(Xml.PREVIOUS_NEXT_DEST_ID)) != null) {
			setPreviousFinalDestination(LocationManager.instance().getLocationById(a.getValue()));
		}
		if (getPreviousFinalDestination() != null
				&& (a = e.getAttribute(Xml.PREVIOUS_NEXT_DEST_TRACK_ID)) != null) {
			setPreviousFinalDestinationTrack(getPreviousFinalDestination().getTrackById(a.getValue()));
		}
		if ((a = e.getAttribute(Xml.PREVIOUS_SCHEDULE_ID)) != null) {
			setPreviousScheduleId(a.getValue());
		}
		if ((a = e.getAttribute(Xml.RWE_DEST_ID)) != null) {
			_rweDestination = LocationManager.instance().getLocationById(a.getValue());
		}
		if (_rweDestination != null && (a = e.getAttribute(Xml.RWE_DEST_TRACK_ID)) != null) {
			_rweDestTrack = _rweDestination.getTrackById(a.getValue());
		}
		if ((a = e.getAttribute(Xml.ORDER)) != null) {
			_order = Integer.parseInt(a.getValue());
		}
		addPropertyChangeListeners();
	}

	/**
	 * Create an XML element to represent this Entry. This member has to remain synchronized with the detailed DTD in
	 * operations-cars.dtd.
	 * 
	 * @return Contents in a JDOM Element
	 */
	public org.jdom.Element store() {
		org.jdom.Element e = new org.jdom.Element(Xml.CAR);
		super.store(e);
		if (isPassenger())
			e.setAttribute(Xml.PASSENGER, isPassenger() ? Xml.TRUE : Xml.FALSE);
		if (isHazardous())
			e.setAttribute(Xml.HAZARDOUS, isHazardous() ? Xml.TRUE : Xml.FALSE);
		if (isCaboose())
			e.setAttribute(Xml.CABOOSE, isCaboose() ? Xml.TRUE : Xml.FALSE);
		if (hasFred())
			e.setAttribute(Xml.FRED, hasFred() ? Xml.TRUE : Xml.FALSE);
		if (isUtility())
			e.setAttribute(Xml.UTILITY, isUtility() ? Xml.TRUE : Xml.FALSE);
		if (getKernel() != null) {
			e.setAttribute(Xml.KERNEL, getKernelName());
			if (getKernel().isLead(this))
				e.setAttribute(Xml.LEAD_KERNEL, Xml.TRUE);
		}
		if (!getLoadName().equals("")) {
			e.setAttribute(Xml.LOAD, getLoadName());
		}
		if (isLoadGeneratedFromStaging())
			e.setAttribute(Xml.LOAD_FROM_STAGING, Xml.TRUE);

		if (getWait() != 0) {
			e.setAttribute(Xml.WAIT, Integer.toString(getWait()));
		}

		if (!getScheduleId().equals("")) {
			e.setAttribute(Xml.SCHEDULE_ID, getScheduleId());
		}

		if (!getNextLoadName().equals("")) {
			e.setAttribute(Xml.NEXT_LOAD, getNextLoadName());
		}

		if (getNextWait() != 0) {
			e.setAttribute(Xml.NEXT_WAIT, Integer.toString(getNextWait()));
		}
		if (getFinalDestination() != null) {
			e.setAttribute(Xml.NEXT_DEST_ID, getFinalDestination().getId());
			if (getFinalDestinationTrack() != null)
				e.setAttribute(Xml.NEXT_DEST_TRACK_ID, getFinalDestinationTrack().getId());
		}
		if (getPreviousFinalDestination() != null) {
			e.setAttribute(Xml.PREVIOUS_NEXT_DEST_ID, getPreviousFinalDestination().getId());
			if (getPreviousFinalDestinationTrack() != null)
				e.setAttribute(Xml.PREVIOUS_NEXT_DEST_TRACK_ID, getPreviousFinalDestinationTrack().getId());
		}
		
		if (!getPreviousScheduleId().equals("")) {
			e.setAttribute(Xml.PREVIOUS_SCHEDULE_ID, getPreviousScheduleId());
		}
		if (getReturnWhenEmptyDestination() != null) {
			e.setAttribute(Xml.RWE_DEST_ID, getReturnWhenEmptyDestination().getId());
			if (getReturnWhenEmptyDestTrack() != null)
				e.setAttribute(Xml.RWE_DEST_TRACK_ID, getReturnWhenEmptyDestTrack().getId());
		}

		e.setAttribute(Xml.ORDER, Integer.toString(getOrder()));

		return e;
	}

	protected void firePropertyChange(String p, Object old, Object n) {
		// Set dirty
		CarManagerXml.instance().setDirty(true);
		super.firePropertyChange(p, old, n);
	}

	private void addPropertyChangeListeners() {
		CarTypes.instance().addPropertyChangeListener(this);
		CarLengths.instance().addPropertyChangeListener(this);
	}

	public void propertyChange(PropertyChangeEvent e) {
		super.propertyChange(e);
		if (e.getPropertyName().equals(CarTypes.CARTYPES_NAME_CHANGED_PROPERTY)) {
			if (e.getOldValue().equals(getTypeName())) {
				if (log.isDebugEnabled())
					log.debug("Car (" + toString() + ") sees type name change old: " + e.getOldValue()
							+ " new: " + e.getNewValue());	// NOI18N
				setTypeName((String) e.getNewValue());
			}
		}
		if (e.getPropertyName().equals(CarLengths.CARLENGTHS_NAME_CHANGED_PROPERTY)) {
			if (e.getOldValue().equals(getLength())) {
				if (log.isDebugEnabled())
					log.debug("Car (" + toString() + ") sees length name change old: " + e.getOldValue()
							+ " new: " + e.getNewValue()); // NOI18N
				setLength((String) e.getNewValue());
			}
		}
		if (e.getPropertyName().equals(Location.DISPOSE_CHANGED_PROPERTY)) {
			if (e.getSource() == _finalDestination) {
				if (log.isDebugEnabled())
					log.debug("delete final destination for car: " + toString());
				setFinalDestination(null);
			}
		}
		if (e.getPropertyName().equals(Track.DISPOSE_CHANGED_PROPERTY)) {
			if (e.getSource() == _finalDestTrack) {
				if (log.isDebugEnabled())
					log.debug("delete final destination for car: " + toString());
				setFinalDestinationTrack(null);
			}
		}
	}

	static Logger log = LoggerFactory.getLogger(Car.class.getName());

}
