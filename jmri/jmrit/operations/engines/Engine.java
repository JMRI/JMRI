package jmri.jmrit.operations.engines;

import jmri.jmrit.operations.cars.Car;
import jmri.jmrit.operations.cars.CarManager;
import jmri.jmrit.operations.cars.Kernel;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.SecondaryLocation;
import jmri.jmrit.operations.trains.TrainManager;

public class Engine extends Car {
	
	private Consist _consist = null;
	private String _model = "";
	private String _hp = "";
	
	public Engine(String road, String number) {
		super(road, number);
		log.debug("New engine " + road + " " + number);
		setType("Engine");
	}
	
	public void setModel (String model){
		String old = _model;
		_model = model;
		if (!old.equals(model))
			firePropertyChange("engine model", old, model);
	}
	
	public String getModel(){
		return _model;
	}
	
	/**
	 * Set the engine horsepower rating
	 * @param hp engine horsepower
	 */
	public void setHp (String hp){
		String old = _hp;
		_hp = hp;
		if (!old.equals(hp))
			firePropertyChange("hp", old, hp);
	}
	
	public String getHp(){
		return _hp;
	}
	
	public void setConsist(Consist consist) {
		String old ="";
		if (_consist != null){
			old = _consist.getName();
			_consist.deleteEngine(this);
		}
		_consist = consist;
		String newName ="";
		if (_consist != null){
			_consist.addEngine(this);
			newName = _consist.getName();
		}
		
		if (!old.equals(newName))
			firePropertyChange("consist", old, newName);
	}

	public Consist getConsist() {
		return _consist;
	}
	
	public String getConsistName() {
		if (_consist != null)
			return _consist.getName();
		else
			return "";
	}

	
	LocationManager locationManager = LocationManager.instance();
	
	/**
	 * Construct this Entry from XML. This member has to remain synchronized with the
	 * detailed DTD in operations-config.xml
	 *
	 * @param e  Consist XML element
	 */
	public Engine(org.jdom.Element e) {
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
		if ((a = e.getAttribute("model")) != null)
			_model = a.getValue();
		if ((a = e.getAttribute("hp")) != null)
			_hp = a.getValue();
		if ((a = e.getAttribute("length")) != null)
			_length = a.getValue();
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
		if ((a = e.getAttribute("consist")) != null){
			setConsist(EngineManager.instance().getConsistByName(a.getValue()));
			if ((a = e.getAttribute("leadConsist")) != null){
				_consist.setLeadEngine(this);
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
		org.jdom.Element e = new org.jdom.Element("engine");
		e.setAttribute("id", getId());
		e.setAttribute("roadName", getRoad());
		e.setAttribute("roadNumber", getNumber());
		e.setAttribute("type", getType());
		e.setAttribute("model", getModel());
		e.setAttribute("hp", getHp());
		e.setAttribute("length", getLength());
		e.setAttribute("built", getBuilt());
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
		if (getConsist() != null){
			e.setAttribute("consist", getConsistName());
			if (getConsist().isLeadEngine(this))
				e.setAttribute("leadConsist", "true");
		}
		e.setAttribute("owner", getOwner());
		e.setAttribute("comment", getComment());

		return e;
	}
	
	static org.apache.log4j.Category log = org.apache.log4j.Category
	.getInstance(Engine.class.getName());

}
