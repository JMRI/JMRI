package jmri.jmrit.operations.rollingstock.engines;

import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.Kernel;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.trains.TrainManager;

public class Engine extends RollingStock {
	
	private Consist _consist = null;
	private String _model = "";
	
	EngineModels engineModels = EngineModels.instance();
	
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
		String old = getHp();
		engineModels.setModelHorsepower(getModel(), hp);
		if (!old.equals(hp))
			firePropertyChange("hp", old, hp);
	}
	
	public String getHp(){
		String hp = engineModels.getModelHorsepower(getModel());
		if(hp == null)
			hp = "";
		return hp;
	}
	
	public void setLength(String length){
		String old = getLength();
		engineModels.setModelLength(getModel(), length);
		if (!old.equals(length))
			firePropertyChange(LENGTH, old, length);
	}
	
	public String getLength(){
		String length = engineModels.getModelLength(getModel());
		if(length == null)
			length = "";
		return length;
	}
	
	public void setConsist(Consist consist) {
		if (_consist == consist)
			return;
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

	public String testDestination(Location destination, Track track) {
		String status = super.testDestination(destination, track);
		if (!status.equals(OKAY))
			return status;
		// now check to see if car is in a kernel and can fit 
		if (getConsist() != null && track != null &&
				track.getUsedLength() + track.getReserved()+ getConsist().getLength() > track.getLength()){
			log.debug("Can't set engine (" + getId() + ") at track destination ("+ destination.getName() + ", " + track.getName() + ") no room!");
			return LENGTH;	
		}
		return OKAY;
	}
	
	LocationManager locationManager = LocationManager.instance();
	
	/**
	 * Construct this Entry from XML. This member has to remain synchronized
	 * with the detailed DTD in operations-engine.dtd
	 * 
	 * @param e
	 *            Engine XML element
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
			setHp(a.getValue());
		if ((a = e.getAttribute("length")) != null)
			setLength(a.getValue());
		if ((a = e.getAttribute("built")) != null)
			_built = a.getValue();
		Location location = null;
		Track track = null;
		if ((a = e.getAttribute("locationId")) != null)
			location = locationManager.getLocationById(a.getValue());
		if ((a = e.getAttribute("secLocationId")) != null && location != null)
			track = location.getTrackById(a.getValue());
		setLocation(location, track);
		Location destination = null;
		Track trackDestination = null;
		if ((a = e.getAttribute("destinationId")) != null)
			destination = locationManager.getLocationById(a.getValue());
		if ((a = e.getAttribute("secDestinationId")) != null  && destination != null)
			trackDestination = destination.getTrackById(a.getValue());
		setDestination(destination, trackDestination);
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
	 * Create an XML element to represent this Entry. This member has to remain
	 * synchronized with the detailed DTD in operations-engine.dtd.
	 * 
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
		if (getTrackId() != "")
			e.setAttribute("secLocationId", getTrackId());
		if (getDestinationId() != "")
			e.setAttribute("destinationId", getDestinationId());
		if (getRouteDestinationId() != "")
			e.setAttribute("routeDestinationId", getRouteDestinationId());
		if (getDestinationTrackId() != "")
			e.setAttribute("secDestinationId", getDestinationTrackId());
		if (getSavedRouteId() != "")
			e.setAttribute("lastRouteId", getSavedRouteId());
		if (verboseStore){
			e.setAttribute("location", getLocationName());
			e.setAttribute("secLocation", getTrackName());
			e.setAttribute("destination", getDestinationName());
			e.setAttribute("secDestination", getDestinationTrackName());
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
