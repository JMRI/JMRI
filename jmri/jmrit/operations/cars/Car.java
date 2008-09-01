package jmri.jmrit.operations.cars;

import java.beans.PropertyChangeEvent;
import java.util.List;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.SecondaryLocation;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.jmrit.operations.routes.RouteLocation;

import org.jdom.Element;

/**
 * Represents a car on the layout
 * 
 * @author Daniel Boudreau
 * @version             $Revision: 1.1 $
 */
public class Car implements java.beans.PropertyChangeListener{

	protected String _id = "";
	protected String _number = "";
	protected String _road = "";
	protected String _type = "";
	protected String _length = "";
	protected String _color = "";
	protected String _weight = "";
	protected String _built = "";
	protected String _owner = "";
	protected String _comment = "";
	protected String _routeId = "";  		// saved route for interchange tracks
	protected boolean _hazardous = false;
	protected boolean _caboose = false;
	protected boolean _fred = false;
	
	protected Location _location = null;
	protected SecondaryLocation _secondaryLocation = null;
	protected Location _destination = null;
	protected SecondaryLocation _secondaryDestination = null;
	protected Train _train = null;
	protected RouteLocation _routeLocation = null;
	protected RouteLocation _routeDestination = null;
	protected int _moves = 0;
	protected Kernel _kernel = null;
	
	public static final String OKAY = "Okay";			// return status when placing car at a location
	public static final String LENGTH = "length";
	public static final String TYPE = "type";
	public static final String ROAD = "road";
	
	public static final String LOCATION = "car location";  		// property change descriptions
	public static final String SECONDARYLOCATION = "car secondary location";
	public static final String DESTINATION = "car destination";
	public static final String SECONDARYDESTINATION = "car secondary destination";
	
	public static final int COUPLER = 4;		// draw bar length between cars

	LocationManager locationManager = LocationManager.instance();
	
	public Car(){
		
	}
	
	public Car(String road, String number) {
		log.debug("New car " + road + " " + number);
		_road = road;
		_number = number;
		_id = createId(road, number);
	}

	public static String createId(String road, String number) {
		String id = road + number;
		return id;
	}

	public String getId() {
		return _id;
	}

	public void setNumber(String number) {
		String old = _number;
		_number = number;
		if (!old.equals(number))
			firePropertyChange("car number", old, number);
	}

	public String getNumber() {
		return _number;
	}

	public void setRoad(String road) {
		String old = _road;
		_road = road;
		if (!old.equals(road))
			firePropertyChange("car road", old, road);
	}

	public String getRoad() {
		return _road;
	}

	public void setType(String type) {
		String old = _type;
		_type = type;
		if (!old.equals(type))
			firePropertyChange("car type", old, type);
	}

	public String getType() {
		return _type;
	}

	public void setLength(String length) {
		String old = _length;
		_length = length;
		// adjust used length if car is at a location
		if (_location != null && _secondaryLocation != null){
			_location.setUsedLength(_location.getUsedLength() + Integer.parseInt(length) - Integer.parseInt(old));
			_secondaryLocation.setUsedLength(_secondaryLocation.getUsedLength() + Integer.parseInt(length) - Integer.parseInt(old));
		}
		if (!old.equals(length))
			firePropertyChange("car length", old, length);
	}

	public String getLength() {
		return _length;
	}

	public void setColor(String color) {
		String old = _color;
		_color = color;
		if (!old.equals(color))
			firePropertyChange("car color", old, color);
	}

	public String getColor() {
		return _color;
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
			firePropertyChange("car hazardous", old?"true":"false", fred?"true":"false");
	}
	
	public boolean hasFred(){
		return _fred;
	}
	
	public void setCaboose(boolean caboose){
		boolean old = _caboose;
		_caboose = caboose;
		if (!old == caboose)
			firePropertyChange("car hazardous", old?"true":"false", caboose?"true":"false");
	}
	
	public boolean isCaboose(){
		return _caboose;
	}

	public void setWeight(String weight) {
		String old = _weight;
		_weight = weight;
		if (!old.equals(weight))
			firePropertyChange("car weight", old, weight);
	}

	public String getWeight() {
		return _weight;
	}

	public void setBuilt(String built) {
		String old = _built;
		_built = built;
		if (!old.equals(built))
			firePropertyChange("car built", old, built);
	}

	public String getBuilt() {
		return _built;
	}
	
	/**
	 * A kernel is a group of cars that are switched as
	 * a unit. 
	 * @param kernel
	 */
	public void setKernel(Kernel kernel) {
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
		else
			return "";
	}

	public Location getLocation() {
		return _location;
	}
	
	public String getLocationName() {
		if (_location != null)
			return _location.getName();
		else
			return "";
	}
	
	public String getLocationId() {
		if (_location != null)
			return _location.getId();
		else
			return "";
	}
	
	public SecondaryLocation getSecondaryLocation() {
		return _secondaryLocation;
	}
	
	public String getSecondaryLocationName() {
		if (_secondaryLocation != null)
			return _secondaryLocation.getName();
		else
			return "";
	}
	
	public String getSecondaryLocationId() {
		if (_secondaryLocation != null)
			return _secondaryLocation.getId();
		else
			return "";
	}
	
	/**
	 * Sets a cars primary and secondary locations on the layout
	 * @param location 
	 * @param secondary (yard, siding, staging, or interchange track)
	 * 
	 * @return "okay" if successful, "type" if the car's type isn't 
	 * acceptable, or "length" if the car length didn't fit.
	 */
	public String setLocation(Location location, SecondaryLocation secondary) {
		// first determine if car can be move to the new location
		if (location != null && secondary != null && !location.acceptsTypeName(getType())){
			log.debug("Can't set (" + getId() + ") type (" +getType()+ ") at location: "+ location.getName() + " wrong type");
			return TYPE;
		}
		if (secondary != null && !secondary.acceptsTypeName(getType())){
			log.debug("Can't set (" + getId() + ") type (" +getType()+ ") at secondary location: "+ location.getName() + " " + secondary.getName() + " wrong type");
			return TYPE;
		}
		if (secondary != null && !secondary.acceptsRoadName(getRoad())){
			log.debug("Can't set (" + getId() + ") road (" +getRoad()+ ") at secondary location: "+ location.getName() + " " + secondary.getName() + " wrong road");
			return ROAD;
		}
		// now determine if there's enough space for the car
		if (secondary != null && _secondaryLocation != secondary &&
				(secondary.getUsedLength() - secondary.getReserved() + Integer.parseInt(getLength()) + COUPLER) > secondary.getLength()){
			log.debug("Can't set (" + getId() + ") at secondary location ("+ location.getName() + ", " + secondary.getName() + ") no room!");
			return LENGTH;	
		}
		// now update
		Location oldLocation = _location;
		_location = location;
		SecondaryLocation oldSecondary = _secondaryLocation;
		_secondaryLocation = secondary;

		if (oldLocation != location || oldSecondary != secondary) {
			// update car location on layout, maybe this should be a property change?
			// first remove car from existing location
			if (oldLocation != null){
				oldLocation.deleteCar(this);
				oldLocation.removePropertyChangeListener(this);
				// if secondary is null, then car is in a train
				if (oldSecondary != null){
					oldSecondary.deleteCar(this);
					oldSecondary.removePropertyChangeListener(this);
					//	if there's a destination then pickup complete
					if (_destination != null){
						oldLocation.deletePickupCar();
						oldSecondary.deletePickupCar(this);
					}
				}
			}
			if (_location != null) {
				_location.addCar(this);
				//	Need to know if location name changes so we can forward to listerners 
				_location.addPropertyChangeListener(this);
			} 
			if (_secondaryLocation != null){
				_secondaryLocation.addCar(this);
				//	Need to know if location name changes so we can forward to listerners 
				_secondaryLocation.addPropertyChangeListener(this);
				// if there's a destination then there's a pickup
				if (_destination != null){
					_location.addPickupCar();
					_secondaryLocation.addPickupCar(this);
				}
			} 
			firePropertyChange(LOCATION, oldLocation, location);
			firePropertyChange(SECONDARYLOCATION, oldSecondary, secondary);
		}
		return OKAY;
	}

	/**
	 * Sets a cars primary and secondary destinations on the layout
	 * @param destination 
	 * @param secondary (yard, siding, staging, or interchange track)
	 * @return "okay" if successful, "type" if the car's type isn't 
	 * acceptable, or "length" if the car length didn't fit.
	 */
	public String setDestination(Location destination, SecondaryLocation secondary) {
		// first determine if car can be move to the new destination
		String status = testDestination(destination, secondary);
		if (!status.equals(OKAY)){
			// ignore length error if car is in kernel
			if (status.equals(LENGTH) && getKernel() != null )
				log.debug("Ignoring length error for car ("+getId()+")");
			else
				return status;
		}
		// now set the cars destination!	
		Location oldDestination = _destination;
		_destination = destination;
		SecondaryLocation oldSecondary = _secondaryDestination;
		_secondaryDestination = secondary;

		if (oldDestination != (destination) || oldSecondary != secondary) {
			if (oldDestination != null){
				oldDestination.deleteDropCar();
				oldDestination.removePropertyChangeListener(this);
				// delete pickup in case destination is null
				if(_location != null){
					_location.deletePickupCar();
					if (_secondaryLocation != null)
						_secondaryLocation.deletePickupCar(this);
				}
			}
			if (oldSecondary != null){
				oldSecondary.deleteDropCar(this);
				oldSecondary.removePropertyChangeListener(this);
			}
			if (_destination != null){
				_destination.addDropCar();
				if(_location != null){
					_location.addPickupCar();
					if (_secondaryLocation != null)
						_secondaryLocation.addPickupCar(this);
				}
				// Need to know if destination name changes so we can forward to listerners 
				_destination.addPropertyChangeListener(this);
			} 
			if (_secondaryDestination != null){
				_secondaryDestination.addDropCar(this);
				// Need to know if destination name changes so we can forward to listerners 
				_secondaryDestination.addPropertyChangeListener(this);
			} else {
				// car has been terminated bump car moves
				setMoves(++_moves);
				if (getTrain() != null && getTrain().getRoute() != null)
					setSavedRouteId(getTrain().getRoute().getId());
				setRouteLocation(null);
				setRouteDestination(null);
			}
			firePropertyChange(DESTINATION, oldDestination, destination);
			firePropertyChange(SECONDARYDESTINATION, oldSecondary, secondary);
		}
		return status;
	}
	
	public String testDestination(Location destination, SecondaryLocation secondary) {
		// first determine if car can be move to the new destination
		if (destination != null && !destination.acceptsTypeName(getType())){
			log.debug("Can't set (" + getId() + ") type (" +getType()+ ") at destination ("+ destination.getName() + ") wrong type");
			return TYPE;
		}
		if (secondary != null && !secondary.acceptsTypeName(getType())){
			log.debug("Can't set (" + getId() + ") type (" +getType()+ ") at secondary destination ("+ destination.getName() + ", " +secondary.getName() + ") wrong type");
			return TYPE;
		}
		if (secondary != null && !secondary.acceptsRoadName(getRoad())){
			log.debug("Can't set (" + getId() + ") road (" +getRoad()+ ") at secondary location ("+ destination.getName() + ", " + secondary.getName() + ") wrong road");
			return ROAD;
		}
		// now determine if there's enough space for the car
		int length = Integer.parseInt(getLength())+ COUPLER;
		if (getKernel() != null)
			length = getKernel().getLength();
		if (secondary != null &&
				secondary.getUsedLength() + secondary.getReserved()+ length > secondary.getLength()){
			log.debug("Can't set (" + getId() + ") at secondary destination ("+ destination.getName() + ", " + secondary.getName() + ") no room!");
			return LENGTH;	
		}
		return OKAY;
	}

	public Location getDestination() {
		return _destination;
	}
	
	public String getDestinationName() {
		if (_destination != null)
			return _destination.getName();
		else
			return "";
	}
	
	public String getDestinationId() {
		if (_destination != null)
			return _destination.getId();
		else
			return "";
	}

	public SecondaryLocation getSecondaryDestination() {
		return _secondaryDestination;
	}
	
	public String getSecondaryDestinationName() {
		if (_secondaryDestination != null)
			return _secondaryDestination.getName();
		else
			return "";
	}
	
	public String getSecondaryDestinationId() {
		if (_secondaryDestination != null)
			return _secondaryDestination.getId();
		else
			return "";
	}
	
	public void setMoves(int moves){
		int old = _moves;
		_moves = moves;
		if (old != moves)
			firePropertyChange("car moves", Integer.toString(old), Integer.toString(moves));
	}
	public int getMoves(){
		return _moves;
	}

	public void setTrain(Train train) {
		Train old = _train;
		_train = train;
		if ((old != null && !old.equals(train)) || old != train){
			if(old != null){
				old.removePropertyChangeListener(this);
			}
			if(train != null)
				train.addPropertyChangeListener(this);
			firePropertyChange("car train", old, train);
		}
	}

	public Train getTrain() {
		return _train;
	}
	
	public void setRouteLocation (RouteLocation routeLocation){
		if(_location == null){
			log.debug("WARNING car ("+getId()+") does not have an assigned location");
		}
		else if(routeLocation != null && _location != null && !routeLocation.getName().equals(_location.getName()))
			log.debug("WARNING route location name("+routeLocation.getName()+") not equal to location name ("+_location.getName()+") for car ("+getId()+")" );
		_routeLocation = routeLocation;
	}
	
	public RouteLocation getRouteLocation(){
		return _routeLocation;
	}
	
	public String getRouteLocationId(){
		if(_routeLocation != null)
			return _routeLocation.getId();
		else
			return "";
	}
	
	public String getSavedRouteId(){
		return _routeId;
	}
	
	public void setSavedRouteId(String id){
		_routeId = id;
	}
	
	public void setRouteDestination (RouteLocation routeDestination){
		if(routeDestination != null && _destination != null && !routeDestination.getName().equals(_destination.getName()))
			log.debug("WARNING route destination name ("+routeDestination.getName()+") not equal to destination name ("+_destination.getName()+") for car ("+getId()+")" );
		_routeDestination = routeDestination;
	}
	
	public RouteLocation getRouteDestination(){
		return _routeDestination;
	}
	
	public String getRouteDestinationId(){
		if(_routeDestination != null)
			return _routeDestination.getId();
		else
			return "";
	}

	public void setOwner(String owner) {
		String old = _owner;
		_owner = owner;
		if (!old.equals(owner))
			firePropertyChange("car owner", old, owner);
	}

	public String getOwner() {
		return _owner;
	}

	public void setComment(String comment) {
		_comment = comment;
	}

	public String getComment() {
		return _comment;
	}

	/**
	 * Construct this Entry from XML. This member has to remain synchronized with the
	 * detailed DTD in operations-config.xml
	 *
	 * @param e  Consist XML element
	 */
	public Car(org.jdom.Element e) {
		//		  if (log.isDebugEnabled()) log.debug("ctor from element "+e);
		org.jdom.Attribute a;
		if ((a = e.getAttribute("id")) != null)
			_id = a.getValue();
		else
			log.warn("no id attribute in car element when reading operations");
		if ((a = e.getAttribute("roadNumber")) != null)
			_number = a.getValue();
		if ((a = e.getAttribute("roadName")) != null)
			_road = a.getValue();
		if ((a = e.getAttribute("type")) != null)
			_type = a.getValue();
		if ((a = e.getAttribute("length")) != null)
			_length = a.getValue();
		if ((a = e.getAttribute("color")) != null)
			_color = a.getValue();
		if ((a = e.getAttribute("hazardous")) != null)
			_hazardous = a.getValue().equals("true");
		if ((a = e.getAttribute("caboose")) != null)
			_caboose = a.getValue().equals("true");
		if ((a = e.getAttribute("fred")) != null)
			_fred = a.getValue().equals("true");
		if ((a = e.getAttribute("weight")) != null)
			_weight = a.getValue();
		if ((a = e.getAttribute("built")) != null)
			_built = a.getValue();
		Location location = null;
		SecondaryLocation secondaryLocation = null;
		if ((a = e.getAttribute("locationId")) != null)
			location = locationManager.getLocationById(a.getValue());
		if ((a = e.getAttribute("secLocationId")) != null && location != null)
			secondaryLocation = location.getSecondaryLocationById(a.getValue());
		setLocation(location, secondaryLocation);
		Location destination = null;
		SecondaryLocation secondaryDestination = null;
		if ((a = e.getAttribute("destinationId")) != null)
			destination = locationManager.getLocationById(a.getValue());
		if ((a = e.getAttribute("secDestinationId")) != null  && destination != null)
			secondaryDestination = destination.getSecondaryLocationById(a.getValue());
		setDestination(destination, secondaryDestination);
		if ((a = e.getAttribute("moves")) != null)
			_moves = Integer.parseInt(a.getValue());
		if ((a = e.getAttribute("train")) != null){
			_train = TrainManager.instance().getTrainByName(a.getValue());
			if (_train != null && _train.getRoute() != null && (a = e.getAttribute("routeLocationId")) != null){
				_routeLocation = _train.getRoute().getLocationById(a.getValue());
				if((a = e.getAttribute("routeDestinationId")) != null)
					_routeDestination = _train.getRoute().getLocationById(a.getValue());
			}
		}
		if ((a = e.getAttribute("lastRouteId")) != null)
			_routeId = a.getValue();
		if ((a = e.getAttribute("kernel")) != null){
			setKernel(CarManager.instance().getKernelByName(a.getValue()));
			if ((a = e.getAttribute("leadKernel")) != null){
				_kernel.setLeadCar(this);
			}
		}
		if ((a = e.getAttribute("owner")) != null)
			_owner = a.getValue();
		if ((a = e.getAttribute("comment")) != null)
			_comment = a.getValue();
	}
	
	boolean verboseStore = false;

	/**
	 * Create an XML element to represent this Entry. This member has to remain synchronized with the
	 * detailed DTD in operations-config.xml.
	 * @return Contents in a JDOM Element
	 */
	public org.jdom.Element store() {
		org.jdom.Element e = new org.jdom.Element("car");
		e.setAttribute("id", getId());
		e.setAttribute("roadName", getRoad());
		e.setAttribute("roadNumber", getNumber());
		e.setAttribute("type", getType());
		e.setAttribute("length", getLength());
		e.setAttribute("color", getColor());
		e.setAttribute("weight", getWeight());
		e.setAttribute("built", getBuilt());
		if (isHazardous())
			e.setAttribute("hazardous", isHazardous()?"true":"false");
		if (isCaboose())
			e.setAttribute("caboose", isCaboose()?"true":"false");
		if (hasFred())
			e.setAttribute("fred", hasFred()?"true":"false");
		if (getLocationId() != "")
			e.setAttribute("locationId", getLocationId());
		if (getRouteLocationId() != "")
			e.setAttribute("routeLocationId", getRouteLocationId());
		if (getSecondaryLocationId() != "")
			e.setAttribute("secLocationId", getSecondaryLocationId());
		if (getDestinationId() != "")
			e.setAttribute("destinationId", getDestinationId());
		if (getRouteDestinationId() != "")
			e.setAttribute("routeDestinationId", getRouteDestinationId());
		if (getSecondaryDestinationId() != "")
			e.setAttribute("secDestinationId", getSecondaryDestinationId());
		if (getSavedRouteId() != "")
			e.setAttribute("lastRouteId", getSavedRouteId());
		if (verboseStore){
			e.setAttribute("location", getLocationName());
			e.setAttribute("secLocation", getSecondaryLocationName());
			e.setAttribute("destination", getDestinationName());
			e.setAttribute("secDestination", getSecondaryDestinationName());
		}
		e.setAttribute("moves", Integer.toString(getMoves()));
		if (getTrain() != null)
			e.setAttribute("train",	getTrain().getName());
		if (getKernel() != null){
			e.setAttribute("kernel", getKernelName());
			if (getKernel().isLeadCar(this))
				e.setAttribute("leadKernel", "true");
		}
		e.setAttribute("owner", getOwner());
		e.setAttribute("comment", getComment());

		return e;
	}
	
	// car listens for changes in a location name or if a location is deleted
    public void propertyChange(PropertyChangeEvent e) {
    	// if (log.isDebugEnabled()) log.debug("Property change for car: " + getId()+ " property name: " +e.getPropertyName()+ " old: "+e.getOldValue()+ " new: "+e.getNewValue());
    	// notify if secondary location name changes
    	if (e.getPropertyName().equals("name")){
        	if (log.isDebugEnabled()) log.debug("Property change for car: " + getId()+ " property name: " +e.getPropertyName()+ " old: "+e.getOldValue()+ " new: "+e.getNewValue());
    		firePropertyChange(e.getPropertyName(), e.getOldValue(), e.getNewValue());
    	}
    	if (e.getPropertyName().equals(Location.DISPOSE)){
    	    if (e.getSource() == _location){
        	   	if (log.isDebugEnabled()) log.debug("delete location for car: " + getId());
    	    	setLocation(null, null);
    	    }
    	    if (e.getSource() == _destination){
        	   	if (log.isDebugEnabled()) log.debug("delete destination for car: " + getId());
    	    	setDestination(null, null);
    	    }
     	}
    	if (e.getPropertyName().equals(SecondaryLocation.DISPOSE)){
    	    if (e.getSource() == _secondaryLocation){
        	   	if (log.isDebugEnabled()) log.debug("delete location for car: " + getId());
    	    	setLocation(_location, null);
    	    }
    	    if (e.getSource() == _secondaryDestination){
        	   	if (log.isDebugEnabled()) log.debug("delete destination for car: " + getId());
    	    	setDestination(_destination, null);
    	    }
    	    	
    	}
    	if (e.getPropertyName().equals(Train.DISPOSE)){
    		if (e.getSource() == _train){
        	   	if (log.isDebugEnabled()) log.debug("delete train for car: " + getId());
        	   	setTrain(null);
    		}
    	}
    }

	java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(
			this);

	public synchronized void addPropertyChangeListener(
			java.beans.PropertyChangeListener l) {
		pcs.addPropertyChangeListener(l);
	}

	public synchronized void removePropertyChangeListener(
			java.beans.PropertyChangeListener l) {
		pcs.removePropertyChangeListener(l);
	}

	protected void firePropertyChange(String p, Object old, Object n) {
		pcs.firePropertyChange(p, old, n);
	}

	static org.apache.log4j.Category log = org.apache.log4j.Category
			.getInstance(Car.class.getName());

}
