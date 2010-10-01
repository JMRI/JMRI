package jmri.jmrit.operations.rollingstock.cars;

import java.util.List;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.ScheduleManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.locations.Schedule;
import jmri.jmrit.operations.locations.ScheduleItem;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.router.Router;

/**
 * Represents a car on the layout
 * 
 * @author Daniel Boudreau Copyright (C) 2008, 2009, 2010
 * @version             $Revision: 1.43 $
 */
public class Car extends RollingStock implements java.beans.PropertyChangeListener{
	
	CarLoads carLoads = CarLoads.instance();
	//LocationManager locationManager = LocationManager.instance();
	
	protected boolean _hazardous = false;
	protected boolean _caboose = false;
	protected boolean _fred = false;
	protected boolean _locationUnknown = false;
	protected boolean _outOfService = false;
	protected Kernel _kernel = null;
	protected String _load = carLoads.getDefaultEmptyName();
	protected String _nextLoad = "";
	protected Location _nextDestination = null;
	protected Track _nextDestTrack = null;
	protected Location _rweDestination = null;	// return when empty
	protected Track _rweDestTrack = null;
	
	public static final String LOAD_CHANGED_PROPERTY = "Car load changed";  		// property change descriptions
	public static final String NEXTDESTINATION_CHANGED_PROPERTY = "Next destination changed";
	public static final String RETURN_WHEN_EMPTY_CHANGED_PROPERTY = "Return when empty changed";
	
	public Car(){
		
	}
	
	public Car(String road, String number) {
		super(road, number);
		log.debug("New car " + road + " " + number);
	}

	public void setHazardous(boolean hazardous){
		boolean old = _hazardous;
		_hazardous = hazardous;
		if (!old == hazardous)
			firePropertyChange("car hazardous", old?"true":"false", hazardous?"true":"false");
	}
	
	public boolean isHazardous(){
		return _hazardous;
	}
	
	public void setFred(boolean fred){
		boolean old = _fred;
		_fred = fred;
		if (!old == fred)
			firePropertyChange("car fred", old?"true":"false", fred?"true":"false");
	}
	
	public boolean hasFred(){
		return _fred;
	}
	
	public void setLoad(String load){
		String old = _load;
		_load = load;
		if (!old.equals(load))
			firePropertyChange(LOAD_CHANGED_PROPERTY, old, load);
	}
	
	public String getLoad(){
		return _load;
	}
	
	public void setNextLoad(String load){
		String old = _nextLoad;
		_nextLoad = load;
		if (!old.equals(load))
			firePropertyChange(LOAD_CHANGED_PROPERTY, old, load);
	}
	
	public String getNextLoad(){
		return _nextLoad;
	}
	
	public void setNextDestination(Location destination){
		Location old = _nextDestination;
		_nextDestination = destination;
		if ((old != null && !old.equals(destination)) || (destination != null && !destination.equals(old)))
			firePropertyChange(NEXTDESTINATION_CHANGED_PROPERTY, null, null);
	}
	
	public Location getNextDestination(){
		return _nextDestination;
	}
	
	public String getNextDestinationName() {
		if (_nextDestination != null)
			return _nextDestination.getName();
		return "";
	}
	
	public void setNextDestTrack(Track track){
		Track old = _nextDestTrack;
		_nextDestTrack = track;
		if ((old != null && !old.equals(track)) || (track != null && !track.equals(old)))
			firePropertyChange(NEXTDESTINATION_CHANGED_PROPERTY, null, null);
	}
	
	public Track getNextDestTrack(){
		return _nextDestTrack;
	}
	
	public void setReturnWhenEmptyDestination(Location destination){
		Location old = _rweDestination;
		_rweDestination = destination;
		if ((old != null && !old.equals(destination)) || (destination != null && !destination.equals(old)))
			firePropertyChange(RETURN_WHEN_EMPTY_CHANGED_PROPERTY, null, null);
	}
	
	public Location getReturnWhenEmptyDestination(){
		return _rweDestination;
	}
	
	public void setReturnWhenEmptyDestTrack(Track track){
		Track old = _rweDestTrack;
		_rweDestTrack = track;
		if ((old != null && !old.equals(track)) || (track != null && !track.equals(old)))
			firePropertyChange(RETURN_WHEN_EMPTY_CHANGED_PROPERTY, null, null);

	}
	
	public Track getReturnWhenEmptyDestTrack(){
		return _rweDestTrack;
	}
	
	public String getReturnWhenEmptyDestName(){
		if (getReturnWhenEmptyDestination() != null && getReturnWhenEmptyDestTrack() != null)
			return getReturnWhenEmptyDestination().getName()+"("+getReturnWhenEmptyDestTrack().getName()+")";
		else if (getReturnWhenEmptyDestination() != null)
			return getReturnWhenEmptyDestination().getName()+"()";
		else
			return "";
	}
	
	public void setLocationUnknown(boolean unknown){
		boolean old = _locationUnknown;
		_locationUnknown = unknown;
		if (!old == unknown)
			firePropertyChange("car location known", old?"true":"false", unknown?"true":"false");
	}
	
	/**
	 * 
	 * @return true when car's location is unknown
	 */
	public boolean isLocationUnknown(){
		return _locationUnknown;
	}
	
	public void setOutOfService(boolean outOfService){
		boolean old = _outOfService;
		_outOfService = outOfService;
		if (!old == outOfService)
			firePropertyChange("car out of service", old?"true":"false", outOfService?"true":"false");
	}
	
	/**
	 * 
	 * @return true when car is out of service
	 */
	public boolean isOutOfService(){
		return _outOfService;
	}
	
	public String getLocationName(){
		return (isLocationUnknown()?"<?> ":(isOutOfService()?"<O> ":"")) + super.getLocationName();
	}
	
	public void setCaboose(boolean caboose){
		boolean old = _caboose;
		_caboose = caboose;
		if (!old == caboose)
			firePropertyChange("car caboose", old?"true":"false", caboose?"true":"false");
	}
	
	public boolean isCaboose(){
		return _caboose;
	}
	
	/**
	 * A kernel is a group of cars that are switched as
	 * a unit. 
	 * @param kernel
	 */
	public void setKernel(Kernel kernel) {
		if (_kernel == kernel)
			return;
		String old ="";
		if (_kernel != null){
			old = _kernel.getName();
			_kernel.deleteCar(this);
		}
		_kernel = kernel;
		String newName ="";
		if (_kernel != null){
			_kernel.addCar(this);
			newName = _kernel.getName();
		}
		if (!old.equals(newName))
			firePropertyChange("kernel", old, newName);
	}

	public Kernel getKernel() {
		return _kernel;
	}
	
	public String getKernelName() {
		if (_kernel != null)
			return _kernel.getName();
		return "";
	}
	
	public String testDestination(Location destination, Track track) {
		String status = super.testDestination(destination, track);
		if (!status.equals(OKAY))
			return status;
		// does car already have this destination?
		if (destination == getDestination() && track == getDestinationTrack())
			return OKAY;
		// is car returning to same track?
		if (track == getTrack())
			return OKAY;
		// now check to see if car is in a kernel and can fit 
		if (getKernel() != null && track != null &&
				track.getUsedLength() + track.getReserved()+ getKernel().getLength() > track.getLength()){
			log.debug("Can't set car (" + getId() + ") in kernel ("+getKernel().getName()+") at track destination ("+ destination.getName() + ", " + track.getName() + ") no room!");
			return LENGTH+ " kernel ("+getKernel().getLength()+")";	
		}
		// now check to see if the track has a schedule
		return testSchedule(track);
	}
	
	private String testSchedule(Track track){
		if (track == null)
			return SCHEDULE +" track is null";
		if (track.getScheduleName().equals("")){
			// does car have a scheduled load?
			if (getLoad().equals(carLoads.getDefaultEmptyName()) || getLoad().equals(carLoads.getDefaultLoadName()))
				return OKAY; //no
			// can't place a car with a scheduled load at a siding
			else if (!track.getLocType().equals(Track.SIDING))
				return OKAY;
			else
				return "Car has a " +SCHEDULE+ " " +LOAD+ " ("+getLoad()+")";
		}
		log.debug("Track ("+track.getName()+") has schedule ("+track.getScheduleName()+")");
		ScheduleManager scheduleManager = ScheduleManager.instance();
		Schedule sch = scheduleManager.getScheduleByName(track.getScheduleName());
		if (sch == null){
			log.warn("Could not find schedule ("+track.getScheduleName()+") for track ("+track.getName()+")");
			return OKAY;
		}
		ScheduleItem si = sch.getItemById(track.getScheduleItemId());
		if (si == null){
			log.warn("Could not find schedule item id ("+track.getScheduleItemId()+") for schedule ("+track.getScheduleName()+")");
			// reset and get first schedule item on the list
			si = sch.getItemById(sch.getItemsBySequenceList().get(0));
			track.setScheduleItemId(si.getId());
		}
		if (getType().equals(si.getType())) {
			if (si.getRoad().equals("") || getRoad().equals(si.getRoad()))
				if (si.getLoad().equals("") || getLoad().equals(si.getLoad()))
					return OKAY;
				else
					return SCHEDULE + " (" + track.getScheduleName()
							+ ") request car "+TYPE+" (" + si.getType()
							+ ") "+ROAD+" (" + si.getRoad() + ") "+LOAD+" ("
							+ si.getLoad() + ")";
			else
				return SCHEDULE + " (" + track.getScheduleName()
						+ ") request car "+TYPE+" (" + si.getType()
						+ ") "+ROAD+" (" + si.getRoad() + ")";
		} else
			return SCHEDULE + " (" + track.getScheduleName()
					+ ") request car "+TYPE+" (" + si.getType() + ")";
	}
	
	/**
	 * Sets the car's destination on the layout
	 * @param destination 
	 * @param track (yard, siding, staging, or interchange track)
	 * @return "okay" if successful, "type" if the rolling stock's type isn't 
	 * acceptable, or "length" if the rolling stock length didn't fit, or 
	 * Schedule if the destination will not accept the car because the siding
	 * has a schedule and the car doesn't meet the schedule requirements.
	 * Also changes the car load status when the car reaches its destination.
	 */
	public String setDestination(Location destination, Track track) {
		// save destination name and track in case car has reached its destination
		String destinationName = getDestinationName();
		Track destTrack = getDestinationTrack();
		String status = super.setDestination(destination, track);
		// return if not Okay 
		if (!status.equals(OKAY))
			return status;
		// now check to see if the track has a schedule
		scheduleNext(track);
		// update next destination and load only when car reaches destination and was in train
		if (destinationName.equals("") || (destination != null) || getTrain() == null)
			return status;
		// update load when car reaches a siding
		loadNext(destTrack);
		// set next destination and destination track if available
		if (!Router.instance().setCarNextDestination(this))
			super.setDestination(null, null);	// couldn't route car, maybe next time
		return status;
	}
	

	
	/**
	 * Check to see if track has schedule and if it does will schedule the next
	 * item in the list.  Load the car with the next schedule load if one exists.
	 * 
	 * @param track
	 */
	private void scheduleNext(Track track){
		if (track == null || track.getScheduleName().equals(""))
			return;
		log.debug("destination track ("+track.getName()+") has schedule ("+track.getScheduleName()+")");
		ScheduleManager scheduleManager = ScheduleManager.instance();
		Schedule sch = scheduleManager.getScheduleByName(track.getScheduleName());
		if (sch == null){
			log.warn("can not find schedule ("+track.getScheduleName()+") assigned to track ("+track.getName()+")");
			return;
		}
		ScheduleItem currentSi = sch.getItemById(track.getScheduleItemId());
		if (currentSi == null){
			log.warn("can not find schedule item ("+track.getScheduleItemId()+") for schedule ("+track.getScheduleName()+")");
			// reset schedule
			track.setScheduleItemId((sch.getItemById(sch.getItemsBySequenceList().get(0)).getId()));
			return;
		}
		// is car part of a kernel?
		if (getKernel()!=null && !getKernel().isLeadCar(this)){
			log.debug("Car ("+getId()+") is part of kernel "+getKernelName());
			// not lead car so return
			return;
		}
		// get the car's next load
		setNextLoad(currentSi.getShip());
		// get the car's next destination and track
		setNextDestination(currentSi.getDestination());
		setNextDestTrack(currentSi.getDestinationTrack());
		
		log.debug("Car ("+toString()+") next load ("+getNextLoad()+")");
		// bump and check count
		track.setScheduleCount(track.getScheduleCount()+1);
		if (track.getScheduleCount() < currentSi.getCount())
			return;
		// go to the next item on the schedule
		track.setScheduleCount(0);
		List<String> l = sch.getItemsBySequenceList();
		for (int i=0; i<l.size(); i++){
			ScheduleItem nextSi = sch.getItemById(l.get(i));
			if (track.getScheduleItemId().equals(nextSi.getId())){
				if (++i < l.size()){
					nextSi = sch.getItemById(l.get(i));
				}else{
					nextSi = sch.getItemById(l.get(0));
				}
				track.setScheduleItemId(nextSi.getId());
				break;
			}
		}
	}
	
	private void loadNext(Track destTrack){
		if (destTrack.getLocType().equals(Track.SIDING)){
			if (!getNextLoad().equals("")){
				setLoad(getNextLoad());
				setNextLoad("");
				// is the next load default empty?  Check for car return when empty
				if (getLoad().equals(carLoads.getDefaultEmptyName()))
					setLoadEmpty();
				return;
			}
			// car doesn't have a schedule load, flip load status
			if (getLoad().equals(carLoads.getDefaultEmptyName()))
				setLoad(carLoads.getDefaultLoadName());
			else
				setLoadEmpty();
		}
		// update load optionally when car reaches staging
		if (destTrack.getLocType().equals(Track.STAGING)){
			if (destTrack.isLoadSwapEnabled()){
				if (getLoad().equals(carLoads.getDefaultEmptyName())){
					setLoad(carLoads.getDefaultLoadName());
				} else if (getLoad().equals(carLoads.getDefaultLoadName())){
					setLoadEmpty();
				}
			}
			// empty car if it has a schedule load
			if (destTrack.isRemoveLoadsEnabled() 
					&& !getLoad().equals(carLoads.getDefaultEmptyName()) 
					&& !getLoad().equals(carLoads.getDefaultLoadName()))
				setLoadEmpty();
		}
	}
	
	private void setLoadEmpty(){
		setLoad(carLoads.getDefaultEmptyName());
		if (getReturnWhenEmptyDestination() != null){
			setNextDestination(getReturnWhenEmptyDestination());
			if (getReturnWhenEmptyDestTrack() != null){
				setNextDestTrack(getReturnWhenEmptyDestTrack());
				log.debug("Car ("+toString()+") has return when empty destination ("+getNextDestinationName()+", "+getNextDestTrack().getName()+")");
			}
		}
	}
	
	public void dispose(){
		setKernel(null);
		super.dispose();
	}

	/**
	 * Construct this Entry from XML. This member has to remain synchronized
	 * with the detailed DTD in operations-cars.dtd
	 * 
	 * @param e  Car XML element
	 */
	public Car(org.jdom.Element e) {
		super.rollingStock(e);
		org.jdom.Attribute a;
		if ((a = e.getAttribute("hazardous")) != null)
			_hazardous = a.getValue().equals("true");
		if ((a = e.getAttribute("caboose")) != null)
			_caboose = a.getValue().equals("true");
		if ((a = e.getAttribute("fred")) != null)
			_fred = a.getValue().equals("true");
		if ((a = e.getAttribute("locUnknown")) != null)
			_locationUnknown = a.getValue().equals("true");
		if ((a = e.getAttribute("outOfService")) != null)
			_outOfService = a.getValue().equals("true");
		if ((a = e.getAttribute("kernel")) != null){
			Kernel k = CarManager.instance().getKernelByName(a.getValue());
			if (k != null){
				setKernel(k);
				if ((a = e.getAttribute("leadKernel")) != null){
					_kernel.setLeadCar(this);
				}
			} else {
				log.error("Kernel "+a.getValue()+" does not exist");
			}
		}
		if ((a = e.getAttribute("load")) != null){
			_load = a.getValue();
		}
		if ((a = e.getAttribute("nextLoad")) != null){
			_nextLoad = a.getValue();
		}
		if ((a = e.getAttribute("nextDestId")) != null){
			_nextDestination = LocationManager.instance().getLocationById(a.getValue());
		}
		if (_nextDestination != null && (a = e.getAttribute("nextDestTrackId")) != null){
			_nextDestTrack = _nextDestination.getTrackById(a.getValue());
		}
		if ((a = e.getAttribute("rweDestId")) != null){
			_rweDestination = LocationManager.instance().getLocationById(a.getValue());
		}
		if (_rweDestination != null && (a = e.getAttribute("rweDestTrackId")) != null){
			_rweDestTrack = _rweDestination.getTrackById(a.getValue());
		}
	}
	
	/**
	 * Create an XML element to represent this Entry. This member has to remain
	 * synchronized with the detailed DTD in operations-cars.dtd.
	 * 
	 * @return Contents in a JDOM Element
	 */
	public org.jdom.Element store() {
		org.jdom.Element e = new org.jdom.Element("car");
		super.store(e);
		if (isHazardous())
			e.setAttribute("hazardous", isHazardous()?"true":"false");
		if (isCaboose())
			e.setAttribute("caboose", isCaboose()?"true":"false");
		if (hasFred())
			e.setAttribute("fred", hasFred()?"true":"false");
		if (isLocationUnknown())
			e.setAttribute("locUnknown", isLocationUnknown()?"true":"false");
		if (isOutOfService())
			e.setAttribute("outOfService", isOutOfService()?"true":"false");
		if (getKernel() != null){
			e.setAttribute("kernel", getKernelName());
			if (getKernel().isLeadCar(this))
				e.setAttribute("leadKernel", "true");
		}
		if (!getLoad().equals("")){
			e.setAttribute("load", getLoad());
		}
		if (!getNextLoad().equals("")){
			e.setAttribute("nextLoad", getNextLoad());
		}
		if (getNextDestination() != null){
			e.setAttribute("nextDestId", getNextDestination().getId());
			if (getNextDestTrack() != null)
				e.setAttribute("nextDestTrackId", getNextDestTrack().getId());
		}
		if (getReturnWhenEmptyDestination() != null){
			e.setAttribute("rweDestId", getReturnWhenEmptyDestination().getId());
			if (getReturnWhenEmptyDestTrack() != null)
				e.setAttribute("rweDestTrackId", getReturnWhenEmptyDestTrack().getId());
		}
		return e;
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(Car.class.getName());

}
