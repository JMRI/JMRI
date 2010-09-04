package jmri.jmrit.operations.locations;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.rollingstock.cars.CarLoads;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineTypes;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.setup.Setup;

/**
 * Represents a location (track) on the layout
 * Can be a siding, yard, staging, or interchange track.
 * 
 * @author Daniel Boudreau
 * @version             $Revision: 1.38 $
 */
public class Track {
	
	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.locations.JmritOperationsLocationsBundle");

	protected String _id = "";
	protected String _name = "";
	protected String _locType = "";					// yard, siding, interchange or staging
	protected String _roadOption = ALLROADS;		// controls which car roads are accepted 
	protected int _trainDir = EAST+WEST+NORTH+SOUTH; //train direction served by this location
	protected int _numberRS = 0;					// number of cars and engines
	protected int _numberCars = 0;					// number of cars
	protected int _numberEngines = 0;				// number of engines
	protected int _pickupRS = 0;					// number of pickups by trains
	protected int _dropRS = 0;						// number of drops by trains
	protected int _length = 0;						// length of tracks at this location
	protected int _reserved = 0;					// length of track reserved by trains
	protected int _usedLength = 0;					// length of track filled by cars and engines 
	protected int _moves = 0;						// count of the drops since creation
	protected String _comment = "";
	
	// schedule options
	protected String _scheduleName = "";			// Schedule name if there's one
	protected String _scheduleItemId = "";			// the current scheduled item id
	protected int _scheduleCount = 0;				// the number of times the item has been delivered
	
	// drop options
	protected String _dropOption = ANY;				// controls which route or train can drop
	protected String _pickupOption = ANY;			// controls which route or train can pickup
	public static final String ANY = "Any";			// track accepts any train or route
	public static final String TRAINS = "trains";	// track only accepts certain trains
	public static final String ROUTES = "routes";	// track only accepts certain routes

	// load options
	protected int _loadOptions = 0;
	private static final int SWAP_GENERIC_LOADS = 1;
	private static final int EMPTY_SCHEDULE_LOADS = 2;
	private static final int GENERATE_SCHEDULE_LOADS = 4;
	
	// the four types of tracks
	public static final String STAGING = "Staging";			
	public static final String INTERCHANGE = "Interchange";
	public static final String YARD = "Yard";
	public static final String SIDING = "Siding";
	
	// train directions serviced by this track
	public static final int EAST = 1;		
	public static final int WEST = 2;
	public static final int NORTH = 4;
	public static final int SOUTH = 8;
	
	// how roads are serviced by this track
	public static final String ALLROADS = "All";			// track accepts all roads
	public static final String INCLUDEROADS = "Include";	// track accepts only certain roads
	public static final String EXCLUDEROADS = "Exclude";	// track does not accept certain roads
	
	//	 For property change
	public static final String TYPES_CHANGED_PROPERTY = "types";
	public static final String ROADS_CHANGED_PROPERTY = "roads";
	public static final String NAME_CHANGED_PROPERTY = "name";
	public static final String LENGTH_CHANGED_PROPERTY = "length";
	public static final String SCHEDULE_CHANGED_PROPERTY = "schedule change";
	public static final String DISPOSE_CHANGED_PROPERTY = "dispose";
	public static final String TRAINDIRECTION_CHANGED_PROPERTY = "trainDirection";
	public static final String DROP_CHANGED_PROPERTY = "drop";
	public static final String PICKUP_CHANGED_PROPERTY = "pickup";
	
	public Track(String id, String name, String type) {
		log.debug("New track " + name + " " + id);
		_locType = type;
		_name = name;
		_id = id;
		// a new track accepts all types
		setTypeNames(CarTypes.instance().getNames());
		setTypeNames(EngineTypes.instance().getNames());
	}
	
	// for combo boxes
	public String toString(){
		return _name;
	}

	public String getId() {
		return _id;
	}

	public void setName(String name) {
		String old = _name;
		_name = name;
		if (!old.equals(name)){
			firePropertyChange(NAME_CHANGED_PROPERTY, old, name);
		}
	}

	public String getName() {
		return _name;
	}
	
	public String getLocType() {
		return _locType;
	}

	public void setLength(int length) {
		int old = _length;
		_length = length;
		if (old != length)
			firePropertyChange(LENGTH_CHANGED_PROPERTY, Integer.toString(old), Integer.toString(length));
	}

	public int getLength() {
		return _length;
	}
	
	public void setReserved(int reserved) {
		int old = _reserved;
		_reserved = reserved;
		if (old != reserved)
			firePropertyChange("reserved", Integer.toString(old), Integer.toString(reserved));
	}

	public int getReserved() {
		return _reserved;
	}
	
	public void setUsedLength(int length) {
		int old = _usedLength;
		_usedLength = length;
		if (old != length)
			firePropertyChange("usedLength", Integer.toString(old), Integer.toString(length));
	}
	
	public int getUsedLength() {
		return _usedLength;
	}
	
	/**
	 * Sets the number of rolling stock (cars and or engines) on this track
	 * @param number
	 */
	private void setNumberRS(int number) {
		int old = _numberRS;
		_numberRS = number;
		if (old != number)
			firePropertyChange("numberRS", Integer.toString(old), Integer.toString(number));
	}
	
	/**
	 * Sets the number of cars on this track
	 * @param number
	 */
	private void setNumberCars(int number) {
		int old = _numberCars;
		_numberCars = number;
		if (old != number)
			firePropertyChange("numberCars", Integer.toString(old), Integer.toString(number));
	}
	
	/**
	 * Sets the number of engines on this track
	 * @param number
	 */
	private void setNumberEngines(int number) {
		int old = _numberEngines;
		_numberEngines = number;
		if (old != number)
			firePropertyChange("numberEngines", Integer.toString(old), Integer.toString(number));
	}
	
	/**
	 * 
	 * @return The number of rolling stock (cars and engines) on this track
	 */
	public int getNumberRS() {
		return _numberRS;
	}
	
	/**
	 * 
	 * @return The number of cars on this track
	 */
	public int getNumberCars() {
		return _numberCars;
	}
	
	/**
	 * 
	 * @return The number of engines on this track
	 */
	public int getNumberEngines() {
		return _numberEngines;
	}
	
	/**
	 * Adds rolling stock to a specific track.  
	 * @param rs
	 */	
	public void addRS(RollingStock rs){
 		setNumberRS(getNumberRS()+1);
 		if (rs.getClass() == Car.class)
 			setNumberCars(getNumberCars()+1);
 		else if (rs.getClass() == Engine.class)
 			setNumberEngines(getNumberEngines()+1);
		setUsedLength(getUsedLength() + Integer.parseInt(rs.getLength())+ RollingStock.COUPLER);
	}
	
	public void deleteRS(RollingStock rs){
		setNumberRS(getNumberRS()-1);
 		if (rs.getClass() == Car.class)
 			setNumberCars(getNumberCars()-1);
 		else if (rs.getClass() == Engine.class)
 			setNumberEngines(getNumberEngines()-1);
		setUsedLength(getUsedLength() - (Integer.parseInt(rs.getLength())+ RollingStock.COUPLER));
	}

	/**
	 * Increments the number of cars and or engines that will be picked up by a train
	 * at this location.
	 */
	public void addPickupRS(RollingStock rs) {
		int old = _pickupRS;
		_pickupRS++;
		if (Setup.isBuildAggressive())
			setReserved(getReserved() - (Integer.parseInt(rs.getLength()) + RollingStock.COUPLER));
		firePropertyChange("pickupRS", Integer.toString(old), Integer.toString(_pickupRS));
	}
	
	public void deletePickupRS(RollingStock rs) {
		int old = _pickupRS;
		if (Setup.isBuildAggressive())
			setReserved(getReserved() + (Integer.parseInt(rs.getLength()) + RollingStock.COUPLER));
		_pickupRS--;
		firePropertyChange("pickupRS", Integer.toString(old), Integer.toString(_pickupRS));
	}
	
	/**
	 * 
	 * @return the number of rolling stock (cars and or engines) that are
	 *         scheduled for pickup at this location.
	 */
	public int getPickupRS() {
		return _pickupRS;
	}

	public int getDropRS() {
		return _dropRS;
	}
	
	public void addDropRS(RollingStock rs) {
		int old = _dropRS;
		_dropRS++;
		setMoves(getMoves()+1);
		setReserved(getReserved() + Integer.parseInt(rs.getLength()) + RollingStock.COUPLER);
		firePropertyChange("dropRS", Integer.toString(old), Integer.toString(_dropRS));
	}
	
	public void deleteDropRS(RollingStock rs) {
		int old = _dropRS;
		_dropRS--;
		setReserved(getReserved() - (Integer.parseInt(rs.getLength()) + RollingStock.COUPLER));
		firePropertyChange("dropRS", Integer.toString(old), Integer.toString(_dropRS));
	}

	public void setComment(String comment) {
		_comment = comment;
	}

	public String getComment() {
		return _comment;
	}
	
    List<String> _typeList = new ArrayList<String>();
    
    public String[] getTypeNames(){
      	String[] types = new String[_typeList.size()];
     	for (int i=0; i<_typeList.size(); i++)
     		types[i] = _typeList.get(i);
   		return types;
    }
    
    private void setTypeNames(String[] types){
    	if (types.length == 0) return;
    	jmri.util.StringUtil.sort(types);
 		for (int i=0; i<types.length; i++)
 			_typeList.add(types[i]);
    }
    
    public void addTypeName(String type){
    	// insert at start of list, sort later
    	if (_typeList.contains(type))
    		return;
    	_typeList.add(0,type);
    	log.debug("track (" +getName()+ ") add rolling stock type "+type);
    	firePropertyChange (TYPES_CHANGED_PROPERTY, _typeList.size()-1, _typeList.size());
    }
    
    public void deleteTypeName(String type){
       	if (!_typeList.contains(type))
    		return;
    	_typeList.remove(type);
    	log.debug("track (" +getName()+ ") delete rolling stock type "+type);
    	firePropertyChange (TYPES_CHANGED_PROPERTY, _typeList.size()+1, _typeList.size());
    }
    
    public boolean acceptsTypeName(String type){
       	if (!CarTypes.instance().containsName(type) && !EngineTypes.instance().containsName(type))
    		return false;
    	return _typeList.contains(type);
    }
    
    public String getRoadOption (){
    	return _roadOption;
    }
    
    /**
     * Set the road option for this track.
     * @param option ALLROADS, INCLUDEROADS, or EXCLUDEROADS
     */
    public void setRoadOption (String option){
    	String old = _roadOption;
    	_roadOption = option;
    	firePropertyChange (ROADS_CHANGED_PROPERTY, old, option);
    }
    
    /**
     * Sets the train directions that can service this track
     * @param direction EAST, WEST, NORTH, SOUTH
     */
 	public void setTrainDirections(int direction){
		int old = _trainDir;
		_trainDir = direction;
		if (old != direction)
			firePropertyChange(TRAINDIRECTION_CHANGED_PROPERTY, Integer.toString(old), Integer.toString(direction));
	}
	
	public int getTrainDirections(){
		return _trainDir;
	}
 
    List <String>_roadList = new ArrayList<String>();
    
    public String[] getRoadNames(){
      	String[] roads = new String[_roadList.size()];
     	for (int i=0; i<_roadList.size(); i++)
     		roads[i] = _roadList.get(i);
     	if (_roadList.size() == 0)
     		return roads;
     	jmri.util.StringUtil.sort(roads);
   		return roads;
    }
    
    private void setRoadNames(String[] roads){
    	if (roads.length == 0) return;
    	jmri.util.StringUtil.sort(roads);
    	for (int i=0; i<roads.length; i++){
    		if (!roads[i].equals(""))
    			_roadList.add(roads[i]);
    	}
    }
    
    public void addRoadName(String road){
     	if (_roadList.contains(road))
    		return;
    	_roadList.add(road);
    	log.debug("track " +getName()+ " add car road "+road);
    	firePropertyChange (ROADS_CHANGED_PROPERTY, _roadList.size()-1, _roadList.size());
    }
    
    public void deleteRoadName(String road){
    	_roadList.remove(road);
    	log.debug("track " +getName()+ " delete car road "+road);
    	firePropertyChange (ROADS_CHANGED_PROPERTY, _roadList.size()+1, _roadList.size());
     }
    
    public boolean acceptsRoadName(String road){
    	if (_roadOption.equals(ALLROADS)){
    		return true;
    	}
       	if (_roadOption.equals(INCLUDEROADS)){
       		return _roadList.contains(road);
       	}
       	// exclude!
       	return !_roadList.contains(road);
    }
    
    public boolean containsRoadName(String road){
      		return _roadList.contains(road);
    }  
    
    public String getDropOption (){
    	return _dropOption;
    }
    
    /**
     * Set the car drop option for this track.
     * @param option ANY, TRAINS, or ROUTES
     */
    public void setDropOption (String option){
    	String old = _dropOption;
    	_dropOption = option;
    	if (!old.equals(option))
    		_dropList.clear();
    	firePropertyChange (DROP_CHANGED_PROPERTY, old, option);
    }
    
    public String getPickupOption (){
    	return _pickupOption;
    }
    
    /**
     * Set the car pickup option for this track.
     * @param option ANY, TRAINS, or ROUTES
     */
    public void setPickupOption (String option){
       	String old = _pickupOption;
       	_pickupOption = option;
    	if (!old.equals(option))
    		_pickupList.clear();
    	firePropertyChange (PICKUP_CHANGED_PROPERTY, old, option);
     }
    
    List<String> _dropList = new ArrayList<String>();
    
    public String[] getDropIds(){
      	String[] ids = new String[_dropList.size()];
     	for (int i=0; i<_dropList.size(); i++)
     		ids[i] = _dropList.get(i);
   		return ids;
    }
    
    private void setDropIds(String[] ids){
    	if (ids.length == 0) return;
 		for (int i=0; i<ids.length; i++)
 			_dropList.add(ids[i]);
    }
    
    public void addDropId(String id){
     	if (_dropList.contains(id))
    		return;
    	_dropList.add(id);
    	log.debug("track " +getName()+ " add drop id "+id);
    	firePropertyChange (DROP_CHANGED_PROPERTY, null, id);
    }
    
    public void deleteDropId(String id){
    	_dropList.remove(id);
    	log.debug("track " +getName()+ " delete drop id "+id);
    	firePropertyChange (DROP_CHANGED_PROPERTY, id, null);
     }
    
    /**
     * Determine if train can drop cars to this track.  Based on the train's
     * id or train's route id.  See setDropOption(option).
     * @param train
     * @return true if the train can drop cars to this track.
     */
    public boolean acceptsDropTrain(Train train){
    	if (_dropOption.equals(ANY))
    		return true;
    	if (_dropOption.equals(TRAINS))
    		return containsDropId(train.getId());
    	else if (train.getRoute() == null)
    		return false;
   		return acceptsDropRoute(train.getRoute());
    }
    
    public boolean acceptsDropRoute(Route route){
      	if (_dropOption.equals(ANY))
    		return true;
      	if (_dropOption.equals(TRAINS))
      		return false;
      	return containsDropId(route.getId());
    }
    
    public boolean containsDropId(String id){
      		return _dropList.contains(id);
    }  
    
    List<String> _pickupList = new ArrayList<String>();
    
    public String[] getPickupIds(){
      	String[] ids = new String[_pickupList.size()];
     	for (int i=0; i<_pickupList.size(); i++)
     		ids[i] = _pickupList.get(i);
   		return ids;
    }
    
    private void setPickupIds(String[] ids){
    	if (ids.length == 0) return;
 		for (int i=0; i<ids.length; i++)
 			_pickupList.add(ids[i]);
    }
    
    /**
     * Add train or route id to this track.
     * @param id
     */
    public void addPickupId(String id){
     	if (_pickupList.contains(id))
    		return;
    	_pickupList.add(id);
    	log.debug("track " +getName()+ " add pickup id "+id);
    	firePropertyChange (PICKUP_CHANGED_PROPERTY, null, id);
    }
    
    public void deletePickupId(String id){
    	_pickupList.remove(id);
    	log.debug("track " +getName()+ " delete pickup id "+id);
    	firePropertyChange (PICKUP_CHANGED_PROPERTY, id, null);
     }
    
    /**
     * Determine if train can pickup cars from this track.  Based on the train's
     * id or train's route id.  See setPickupOption(option).
     * @param train
     * @return true if the train can pickup cars from this track.
     */
    public boolean acceptsPickupTrain(Train train){
    	if (_pickupOption.equals(ANY))
    		return true;
    	if (_pickupOption.equals(TRAINS))
    		return containsPickupId(train.getId());
    	else if (train.getRoute() == null)
    		return false;
   		return acceptsPickupRoute(train.getRoute());
    }
    
    public boolean acceptsPickupRoute(Route route){
      	if (_pickupOption.equals(ANY))
    		return true;
      	if (_pickupOption.equals(TRAINS))
      		return false;
      	return containsPickupId(route.getId());
    }
    
    public boolean containsPickupId(String id){
    	return _pickupList.contains(id);
    }  
    
    public int getMoves(){
    	return _moves;
    }
    
    public void setMoves(int moves){
    	_moves = moves;
    	// set dirty
    	LocationManagerXml.instance().setDirty(true);
    }
    
    public String getScheduleName(){
    	return _scheduleName;
    }
    
    public void setScheduleName(String name){
    	String old = _scheduleName;
    	_scheduleName = name;
    	if(!old.equals(name)){
    		Schedule schedule = ScheduleManager.instance().getScheduleByName(name);
    		if (schedule == null)
    			return;
    		// set the id to the first item in the list
    		String id = schedule.getItemsBySequenceList().get(0);
    		setScheduleItemId(id);
    		setScheduleCount(0);
    		firePropertyChange (SCHEDULE_CHANGED_PROPERTY, old, name);
    	}
    }
    
    public String getScheduleItemId(){
    	return _scheduleItemId;
    }
    
    public void setScheduleItemId(String id){
    	String old = _scheduleItemId;
    	_scheduleItemId = id;
    	firePropertyChange (SCHEDULE_CHANGED_PROPERTY, old, id);
    }
    
    public int getScheduleCount(){
    	return _scheduleCount;
    }
    
    public void setScheduleCount(int count){
    	int old = _scheduleCount;
    	_scheduleCount = count;
    	firePropertyChange (SCHEDULE_CHANGED_PROPERTY, old, count);
    }
    
    /**
     * Check to see if schedule is valid for the track at this location.
     * @param location The location of this track.
     * @return "" if schedule okay, otherwise an error message.
     */
	public String checkScheduleValid(Location location){
		String status = "";
		Schedule schedule = ScheduleManager.instance().getScheduleByName(getScheduleName());
		if (schedule == null)
			return status;
		List<String> scheduleItems = schedule.getItemsBySequenceList();
		if (scheduleItems.size() == 0){
			status = rb.getString("empty");
			return status;
		}
		for (int i=0; i<scheduleItems.size(); i++){
			ScheduleItem si = schedule.getItemById(scheduleItems.get(i));
			if (!location.acceptsTypeName(si.getType())){
				status = MessageFormat.format(rb.getString("NotValid"),new Object[]{si.getType()});
				break;
			}
			if (!acceptsTypeName(si.getType())){
				status = MessageFormat.format(rb.getString("NotValid"),new Object[]{si.getType()});
				break;
			}
			if (!si.getRoad().equals("") && !acceptsRoadName(si.getRoad())){
				status = MessageFormat.format(rb.getString("NotValid"),new Object[]{si.getRoad()});
				break;
			}
			// check loads
			List<String> loads = CarLoads.instance().getNames(si.getType());
			if (!si.getLoad().equals("") && !loads.contains(si.getLoad())){
				status = MessageFormat.format(rb.getString("NotValid"),new Object[]{si.getLoad()});
				break;
			}
			if (!si.getShip().equals("") && !loads.contains(si.getShip())){
				status = MessageFormat.format(rb.getString("NotValid"),new Object[]{si.getShip()});
				break;
			}
		}
		return status;
	}
	
	/**
	 * Enable changing the car generic load state when car arrives at the
	 * location.
	 * 
	 * @param enable
	 *            when true, swap generic car load state
	 */
	public void enableLoadSwaps(boolean enable){
		if (enable)
			_loadOptions = _loadOptions | SWAP_GENERIC_LOADS;
		else
			_loadOptions = _loadOptions & 0xFFFF-SWAP_GENERIC_LOADS;
	}
	
	public boolean isLoadSwapEnabled(){
		return (0 < (_loadOptions & SWAP_GENERIC_LOADS));
	}
	
	/**
	 * When enabled, remove Scheduled car loads.
	 * @param enable when true, remove Scheduled loads from cars
	 */
	public void enableRemoveLoads(boolean enable){
		if (enable)
			_loadOptions = _loadOptions | EMPTY_SCHEDULE_LOADS;
		else
			_loadOptions = _loadOptions & 0xFFFF-EMPTY_SCHEDULE_LOADS;
	}
	
	public boolean isRemoveLoadsEnabled(){
		return (0 < (_loadOptions & EMPTY_SCHEDULE_LOADS));
	}
	
	/**
	 * When enabled, add Scheduled car loads if there's a demand.
	 * @param enable when true, add Scheduled loads from cars
	 */
	public void enableAddLoads(boolean enable){
		if (enable)
			_loadOptions = _loadOptions | GENERATE_SCHEDULE_LOADS;
		else
			_loadOptions = _loadOptions & 0xFFFF-GENERATE_SCHEDULE_LOADS;
	}
	
	public boolean isAddLoadsEnabled(){
		return (0 < (_loadOptions & GENERATE_SCHEDULE_LOADS));
	}
    
    public void dispose(){
    	firePropertyChange (DISPOSE_CHANGED_PROPERTY, null, "Dispose");
    }
    
	
    /**
     * Construct this Entry from XML. This member has to remain synchronized with the
     * detailed DTD in operations-config.xml
     *
     * @param e  Consist XML element
     */
    private boolean debugFlag = false;
    public Track(org.jdom.Element e) {
        //if (log.isDebugEnabled()) log.debug("ctor from element "+e);
        org.jdom.Attribute a;
        if ((a = e.getAttribute("id")) != null )  _id = a.getValue();
        else log.warn("no id attribute in track element when reading operations");
        if ((a = e.getAttribute("name")) != null )  _name = a.getValue();
        if ((a = e.getAttribute("locType")) != null )  _locType = a.getValue();
        if ((a = e.getAttribute("length")) != null )  _length = Integer.parseInt(a.getValue());
        if ((a = e.getAttribute("moves")) != null )  _moves = Integer.parseInt(a.getValue());
        if ((a = e.getAttribute("dir")) != null )  _trainDir = Integer.parseInt(a.getValue());
        if ((a = e.getAttribute("comment")) != null )  _comment = a.getValue();
        if ((a = e.getAttribute("carTypes")) != null ) {
        	String names = a.getValue();
           	String[] types = names.split("%%");
        	if (log.isDebugEnabled() && debugFlag) log.debug("track (" +getName()+ ") accepts car types: "+ names);
        	setTypeNames(types);
        }
        if ((a = e.getAttribute("carRoadOperation")) != null )  _roadOption = a.getValue();
        if ((a = e.getAttribute("dropIds")) != null ) {
        	String names = a.getValue();
           	String[] ids = names.split("%%");
        	if (log.isDebugEnabled() && debugFlag) log.debug("track (" +getName()+ ") has drop ids : "+ names);
        	setDropIds(ids);
        }
        if ((a = e.getAttribute("dropOption")) != null )  _dropOption = a.getValue();
        if ((a = e.getAttribute("pickupIds")) != null ) {
        	String names = a.getValue();
           	String[] ids = names.split("%%");
        	if (log.isDebugEnabled() && debugFlag) log.debug("track (" +getName()+ ") has pickup ids : "+ names);
        	setPickupIds(ids);
        }
        if ((a = e.getAttribute("pickupOption")) != null )  _pickupOption = a.getValue();
        if ((a = e.getAttribute("carRoads")) != null ) {
        	String names = a.getValue();
           	String[] roads = names.split("%%");
        	if (log.isDebugEnabled() && debugFlag) log.debug("track (" +getName()+ ") " +getRoadOption()+  " car roads: "+ names);
        	setRoadNames(roads);
        }
        if ((a = e.getAttribute("schedule")) != null ) _scheduleName = a.getValue();
        if ((a = e.getAttribute("itemId")) != null ) _scheduleItemId = a.getValue();
        if ((a = e.getAttribute("itemCount")) != null ) _scheduleCount = Integer.parseInt(a.getValue());
        if ((a = e.getAttribute("loadOptions")) != null ) _loadOptions = Integer.parseInt(a.getValue());
    }

    /**
     * Create an XML element to represent this Entry. This member has to remain synchronized with the
     * detailed DTD in operations-location.dtd.
     * @return Contents in a JDOM Element
     */
    public org.jdom.Element store() {
    	org.jdom.Element e = new org.jdom.Element("track");
    	e.setAttribute("id", getId());
    	e.setAttribute("name", getName());
    	e.setAttribute("locType", getLocType());
    	e.setAttribute("dir", Integer.toString(getTrainDirections()));
    	e.setAttribute("length", Integer.toString(getLength()));
    	e.setAttribute("moves", Integer.toString(getMoves()-getDropRS()));
    	// build list of car types for this track
    	String[] types = getTypeNames();
    	String typeNames ="";
    	for (int i=0; i<types.length; i++){
    		// remove types that have been deleted by user
    		if (CarTypes.instance().containsName(types[i]) || EngineTypes.instance().containsName(types[i]))
    			typeNames = typeNames + types[i]+"%%";
    	}
    	e.setAttribute("carTypes", typeNames);
     	e.setAttribute("carRoadOperation", getRoadOption());
       	// build list of car roads for this track
    	String[] roads = getRoadNames();
    	String roadNames ="";
    	for (int i=0; i<roads.length; i++){
    		roadNames = roadNames + roads[i]+"%%";
    	}
    	e.setAttribute("carRoads", roadNames);
    	e.setAttribute("dropOption", getDropOption());
      	// build list of drop ids for this track
    	String[] dropIds = getDropIds();
    	String ids ="";
    	for (int i=0; i<dropIds.length; i++){
    		ids = ids + dropIds[i]+"%%";
    	}
    	e.setAttribute("dropIds", ids);
    	e.setAttribute("pickupOption", getPickupOption());
     	// build list of pickup ids for this track
    	String[] pickupIds = getPickupIds();
    	ids ="";
    	for (int i=0; i<pickupIds.length; i++){
    		ids = ids + pickupIds[i]+"%%";
    	}
    	e.setAttribute("pickupIds", ids);
    	if (!getScheduleName().equals("")){
    		e.setAttribute("schedule", getScheduleName());
    		e.setAttribute("itemId", getScheduleItemId());
    		e.setAttribute("itemCount", Integer.toString(getScheduleCount()));
    	}
    	if (_loadOptions != 0)
    		e.setAttribute("loadOptions", Integer.toString(_loadOptions));
    	e.setAttribute("comment", getComment());

    	return e;
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

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(Track.class.getName());

}
