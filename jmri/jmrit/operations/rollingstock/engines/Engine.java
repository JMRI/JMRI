package jmri.jmrit.operations.rollingstock.engines;

import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.Kernel;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.trains.TrainManager;

/**
 * Represents an engine on the layout
 * 
 * @author Daniel Boudreau (C) Copyright 2008
 * @version $Revision: 1.6 $
 */
public class Engine extends RollingStock {
	
	private Consist _consist = null;
	private String _model = "";
	
	EngineModels engineModels = EngineModels.instance();
	
	public Engine(String road, String number) {
		super(road, number);
		log.debug("New engine " + road + " " + number);
	}
	
	/**
	 * Set the engine's model. Note a model has only one length, type, and
	 * horsepower rating.
	 * 
	 * @param model
	 */
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
	 * Set the engine type for this engine's model
	 * @param type Engine type: Steam, Diesel, Traction, etc.
	 */
	public void setType (String type){
		String old = getType();
		engineModels.setModelType(getModel(), type);
		if (!old.equals(type))
			firePropertyChange("type", old, type);	
	}
	
	public String getType(){
		String type = engineModels.getModelType(getModel());
		if(type == null)
			type = super.getType();
		return type;
	}
	
	/**
	 * Set the engine horsepower rating for this engine's model
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
	
	/**
	 * Set the engine length for this engine's model
	 * @param length engine length
	 */
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
	
	/**
	 * Place engine in a consist
	 * @param consist
	 */
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

	/**
	 * Get the consist for this engine
	 * @return null if engine isn't in a consist
	 */
	public Consist getConsist() {
		return _consist;
	}
	
	public String getConsistName() {
		if (_consist != null)
			return _consist.getName();
		else
			return "";
	}

	/**
	 * Used to check destination track to see if it will accept engine
	 * @return status, see RollingStock.java
	 */
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
		org.jdom.Attribute a;
		if ((a = e.getAttribute("model")) != null)
			_model = a.getValue();
		if ((a = e.getAttribute("hp")) != null)
			setHp(a.getValue());
		if ((a = e.getAttribute("length")) != null)
			setLength(a.getValue());
		if ((a = e.getAttribute("consist")) != null){
			setConsist(EngineManager.instance().getConsistByName(a.getValue()));
			if ((a = e.getAttribute("leadConsist")) != null){
				_consist.setLeadEngine(this);
			}
			if ((a = e.getAttribute("consistNum")) != null){
				_consist.setConsistNumber(Integer.parseInt(a.getValue()));
			}
		}
		super.rollingStock(e); // must set _model first so engine length is calculated properly
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
		super.store(e);
		e.setAttribute("model", getModel());
		e.setAttribute("hp", getHp());
		if (getConsist() != null){
			e.setAttribute("consist", getConsistName());
			if (getConsist().isLeadEngine(this)){
				e.setAttribute("leadConsist", "true");
				if (getConsist().getConsistNumber()>0)
					e.setAttribute("consistNum", Integer.toString(getConsist().getConsistNumber()));
			}
		}
		return e;
	}
	
	static org.apache.log4j.Category log = org.apache.log4j.Category
	.getInstance(Engine.class.getName());

}
