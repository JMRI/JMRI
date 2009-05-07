// TrainManager.java

package jmri.jmrit.operations.trains;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JComboBox;

import org.jdom.Element;

import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.rollingstock.engines.EngineTypes;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.OperationsXml;


/**
 * Manages trains.
 * @author      Bob Jacobsen Copyright (C) 2003
 * @author Daniel Boudreau Copyright (C) 2008
 * @version	$Revision: 1.19 $
 */
public class TrainManager implements java.beans.PropertyChangeListener {
	public static final String LISTLENGTH_CHANGED_PROPERTY = "listLength";
	// Train frame attributes
	protected String _sortBy = "";
	protected boolean _buildReport = false;
	protected boolean _printPreview = false;	// when true, preview train manifest
	protected TrainsTableFrame _trainFrame = null;
	protected Dimension _frameDimension = new Dimension(Control.panelWidth,Control.panelHeight);
	protected Point _framePosition = new Point();
	// Edit Train frame attributes
	protected TrainEditFrame _trainEditFrame = null;
	protected Dimension _editFrameDimension = null;
	protected Point _editFramePosition = null;
	
	
    
	public TrainManager() {
		CarTypes.instance().addPropertyChangeListener(this);
		CarRoads.instance().addPropertyChangeListener(this);
		EngineTypes.instance().addPropertyChangeListener(this);
    }
    
	/** record the single instance **/
	private static TrainManager _instance = null;
	private static int _id = 0;		// train ids
	private static boolean trainsloaded = false;

	public static synchronized TrainManager instance() {
		if (_instance == null) {
			if (log.isDebugEnabled()) log.debug("TrainManager creating instance");
			// create and load
			_instance = new TrainManager();
			OperationsXml.instance();				// load setup
			TrainManagerXml.instance();				// load trains
		}
		if (Control.showInstance && log.isDebugEnabled()) log.debug("TrainManager returns instance "+_instance);
		return _instance;
	}

	public void setTrainsLoaded(){
		trainsloaded = true;
		log.debug("Trains have been loaded!");
	}
 
    public boolean getBuildReport(){
    	return _buildReport;
    }
    
    public void setBuildReport(boolean report){
    	_buildReport = report;
    }
    
    public boolean getPrintPreview(){
    	return _printPreview;
    }
    
    public void setPrintPreview(boolean preview){
    	_printPreview = preview;
    }
    
    public void setTrainFrame(TrainsTableFrame frame){
    	_trainFrame = frame;
    }
    
    public Dimension getTrainFrameSize(){
    	return _frameDimension;
    }
    
    public Point getTrainFramePosition(){
    	return _framePosition;
    }
    
    public String getTrainFrameSortBy (){
    	return _sortBy;
    }
   
    public void setTrainFrameSortBy(String sortBy){
    	_sortBy = sortBy;
    }
    
    public void setTrainEditFrame(TrainEditFrame frame){
    	_trainEditFrame = frame;
    }
    
    public Dimension getTrainEditFrameSize(){
    	return _editFrameDimension;
    }
    
    public Point getTrainEditFramePosition(){
    	return _editFramePosition;
    }
	
	public void dispose() {
    	CarTypes.instance().removePropertyChangeListener(this);
    	CarRoads.instance().removePropertyChangeListener(this);
    	EngineTypes.instance().removePropertyChangeListener(this);
        _trainHashTable.clear();
        _instance = null;
    }
	
	//	 stores known Train instances by id
    protected Hashtable<String, Train> _trainHashTable = new Hashtable<String, Train>();   

    /**
     * @return requested Train object or null if none exists
     */
     
    public Train getTrainByName(String name) {
		if (!trainsloaded)
			log.error("TrainManager getTrainByName called before trains completely loaded!");
    	Train train;
    	Enumeration<Train> en =_trainHashTable.elements();
    	for (int i = 0; i < _trainHashTable.size(); i++){
    		train = en.nextElement();
    		if (train.getName().equals(name))
    			return train;
      	}
    	log.debug("train "+name+" doesn't exist");
        return null;
    }
    
    public Train getTrainById (String id){
		if (!trainsloaded)
			log.error("TrainManager getTrainById called before trains completely loaded!");
    	return _trainHashTable.get(id);
    }
 
    /**
     * Finds an exsisting train or creates a new train if needed
     * requires train's name creates a unique id for this train
     * @param name
     * 
     * @return new train or existing train
     */
    public Train newTrain (String name){
    	Train train = getTrainByName(name);
    	if (train == null){
    		_id++;						
    		train = new Train(Integer.toString(_id), name);
    		Integer oldSize = new Integer(_trainHashTable.size());
    		_trainHashTable.put(train.getId(), train);
    		firePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize, new Integer(_trainHashTable.size()));
    	}
    	return train;
    }
    
    /**
     * Remember a NamedBean Object created outside the manager.
 	 */
    public void register(Train train) {
    	Integer oldSize = new Integer(_trainHashTable.size());
        _trainHashTable.put(train.getId(), train);
        // find last id created
        int id = Integer.parseInt(train.getId());
        if (id > _id)
        	_id = id;
        firePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize, new Integer(_trainHashTable.size()));
        // listen for name and state changes to forward
    }

    /**
     * Forget a NamedBean Object created outside the manager.
     */
    public void deregister(Train train) {
    	if (train == null)
    		return;
        train.dispose();
        Integer oldSize = new Integer(_trainHashTable.size());
    	_trainHashTable.remove(train.getId());
        firePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize, new Integer(_trainHashTable.size()));
    }
    
    public void replaceType(String oldType, String newType){
		List<String> trains = getTrainsByIdList();
		for (int i=0; i<trains.size(); i++){
			Train train = getTrainById(trains.get(i));
			if (train.acceptsTypeName(oldType)){
				train.deleteTypeName(oldType);
				train.addTypeName(newType);
			}
		}
    }
    
	public void replaceRoad(String oldRoad, String newRoad){
		List<String> trains = getTrainsByIdList();
		for (int i=0; i<trains.size(); i++){
			Train train = getTrainById(trains.get(i));
			String[] roadNames = train.getRoadNames();
			for (int j=0; j<roadNames.length; j++){
				if (roadNames[j].equals(oldRoad)){
					train.deleteRoadName(oldRoad);
					if (newRoad != null)
						train.addRoadName(newRoad);
				}
			}
			if (train.getEngineRoad().equals(oldRoad))
				train.setEngineRoad(newRoad);
			if (train.getCabooseRoad().equals(oldRoad))
				train.setCabooseRoad(newRoad);
		}
	}

   /**
     * Sort by train name
     * @return list of train ids ordered by name
     */
    public List<String> getTrainsByNameList() {
		// first get id list
		List<String> sortList = getList();
		// now re-sort
		List<String> out = new ArrayList<String>();
		String trainName = "";
		boolean trainAdded = false;
		Train train;

		for (int i = 0; i < sortList.size(); i++) {
			trainAdded = false;
			train = getTrainById(sortList.get(i));
			trainName = train.getName();
			for (int j = 0; j < out.size(); j++) {
				train = getTrainById(out.get(j));
				String outTrainName = train.getName();
				if (trainName.compareToIgnoreCase(outTrainName) < 0) {
					out.add(j,sortList.get(i));
					trainAdded = true;
					break;
				}
			}
			if (!trainAdded) {
				out.add(sortList.get(i));
			}
		}
		return out;

	}
    
    /**
     * Sort by train departure time
     * @return list of train ids ordered by departure time
     */
    public List<String> getTrainsByTimeList() {
		// first get train by name list
		List<String> sortList = getTrainsByNameList();
		// now re-sort
		List<String> out = new ArrayList<String>();
		int trainTime;
		int outTrainTime;
		boolean trainAdded = false;
		Train train;

		for (int i = 0; i < sortList.size(); i++) {
			trainAdded = false;
			train = getTrainById(sortList.get(i));
			trainTime = train.getDepartTimeMinutes();
			for (int j = 0; j < out.size(); j++) {
				train = getTrainById(out.get(j));
				outTrainTime = train.getDepartTimeMinutes();
				if (trainTime < outTrainTime) {
					out.add(j,sortList.get(i));
					trainAdded = true;
					break;
				}
			}
			if (!trainAdded) {
				out.add(sortList.get(i));
			}
		}
		return out;

	}
    
    /**
	 * Sort by train number, number can alpha numeric
	 * 
	 * @return list of train ids ordered by number
	 */
    public List<String> getTrainsByIdList() {
    	// first get id list
    	List<String> sortList = getList();
    	// now re-sort
    	List<String> out = new ArrayList<String>();
    	int trainNumber = 0;
    	boolean trainAdded = false;
    	Train train;
    	
    	for (int i=0; i<sortList.size(); i++){
    		trainAdded = false;
    		train = getTrainById (sortList.get(i));
    		try{
    			trainNumber = Integer.parseInt (train.getId());
    		}catch (NumberFormatException e) {
    			log.debug("train id number isn't a number");
    		}
    		for (int j=0; j<out.size(); j++ ){
    			train = getTrainById (out.get(j));
        		try{
        			int outTrainNumber = Integer.parseInt (train.getId());
        			if (trainNumber < outTrainNumber){
        				out.add(j,sortList.get(i));
        				trainAdded = true;
        				break;
        			}
        		}catch (NumberFormatException e) {
        			log.debug("list out id number isn't a number");
        		}
    		}
    		if (!trainAdded){
    			out.add(sortList.get(i));
    		}
    	}
        return out;
    }
    
    private List<String> getList() {
		if (!trainsloaded)
			log.error("TrainManager getList called before trains completely loaded!");
        String[] arr = new String[_trainHashTable.size()];
        List<String> out = new ArrayList<String>();
        Enumeration<String> en = _trainHashTable.keys();
        int i=0;
        while (en.hasMoreElements()) {
            arr[i] = en.nextElement();
            i++;
        }
        jmri.util.StringUtil.sort(arr);
        for (i=0; i<arr.length; i++) out.add(arr[i]);
        return out;
    }
    
    public JComboBox getComboBox (){
    	JComboBox box = new JComboBox();
    	box.addItem("");
		List<String> trains = getTrainsByNameList();
		for (int i = 0; i < trains.size(); i++){
			Train train = getTrainById(trains.get(i));
			box.addItem(train);
		}
    	return box;
    }
    
    public void updateComboBox(JComboBox box) {
    	box.removeAllItems();
    	box.addItem("");
		List<String> trains = getTrainsByNameList();
		for (int i = 0; i < trains.size(); i++){
			Train train = getTrainById(trains.get(i));
			box.addItem(train);
		}
    }
  
    /**
     * @return Number of trains
     */
    public int numEntries() { return _trainHashTable.size(); }
    
    public void options (org.jdom.Element values) {
        if (log.isDebugEnabled()) log.debug("ctor from element "+values);
        Element e = values.getChild("trainOptions");
        org.jdom.Attribute a;
        if ((a = e.getAttribute("sortBy")) != null)
        	_sortBy = a.getValue();
 		if ((a = e.getAttribute("buildReport")) != null)
			_buildReport = a.getValue().equals("true");
 		if ((a = e.getAttribute("printPreview")) != null)
			_printPreview = a.getValue().equals("true");
        int x = 0;
        int y = 0;
        int height = Control.panelHeight;
        int width = Control.panelWidth;
        try {
            x = e.getAttribute("x").getIntValue();
            y = e.getAttribute("y").getIntValue();
            height = e.getAttribute("height").getIntValue();
            width = e.getAttribute("width").getIntValue();
            _frameDimension = new Dimension(width, height);
            _framePosition = new Point(x,y);
        } catch ( org.jdom.DataConversionException ee) {
            log.debug("Did not find train frame attributes");
        } catch ( NullPointerException ne) {
        	log.debug("Did not find train frame attributes");
        }
        // get Train Edit attributes
        e = values.getChild("trainEditOptions");
        if (e != null){
            try {
                x = e.getAttribute("x").getIntValue();
                y = e.getAttribute("y").getIntValue();
                height = e.getAttribute("height").getIntValue();
                width = e.getAttribute("width").getIntValue();
                _editFrameDimension = new Dimension(width, height);
                _editFramePosition = new Point(x,y);
            } catch ( org.jdom.DataConversionException ee) {
                log.debug("Did not find train edit frame attributes");
            } catch ( NullPointerException ne) {
            	log.debug("Did not find train edit frame attributes");
            }
        }
    }

    
    /**
     * Create an XML element to represent this Entry. This member has to remain synchronized with the
     * detailed DTD in operations-trains.dtd.
     * @return Contents in a JDOM Element
     */
    public org.jdom.Element store() {
    	Element values = new Element("options");
        org.jdom.Element e = new org.jdom.Element("trainOptions");
        e.setAttribute("sortBy", getTrainFrameSortBy());
        e.setAttribute("buildReport", getBuildReport()?"true":"false");
        e.setAttribute("printPreview", getPrintPreview()?"true":"false");
        // get previous Train frame size and position
        Dimension size = getTrainFrameSize();
        Point posn = getTrainFramePosition();
        if (_trainFrame != null){
        	size = _trainFrame.getSize();
        	posn = _trainFrame.getLocation();
        	_frameDimension = size;
        	_framePosition = posn;
        }
        e.setAttribute("x", ""+posn.x);
        e.setAttribute("y", ""+posn.y);
        e.setAttribute("height", ""+size.height);
        e.setAttribute("width", ""+size.width); 
        values.addContent(e);
        // now save Train Edit frame size and position
        e = new org.jdom.Element("trainEditOptions");
        size = getTrainEditFrameSize();
        posn = getTrainEditFramePosition();
        if (_trainEditFrame != null){
        	size = _trainEditFrame.getSize();
        	posn = _trainEditFrame.getLocation();
        	_editFrameDimension = size;
        	_editFramePosition = posn;
        }
        if (posn != null){
        	e.setAttribute("x", ""+posn.x);
        	e.setAttribute("y", ""+posn.y);
        }
        if (size != null){
        	e.setAttribute("height", ""+size.height);
        	e.setAttribute("width", ""+size.width); 
        }
        values.addContent(e);
        return values;
    }
    
	/**
	 * Check for car type and road name replacements. Also check for engine type
	 * replacement.
	 * 
	 */
    public void propertyChange(java.beans.PropertyChangeEvent e) {
    	log.debug("TrainManager sees property change: " + e.getPropertyName() + " old: " + e.getOldValue() + " new " + e.getNewValue());
    	if (e.getPropertyName().equals(CarTypes.CARTYPES_NAME_CHANGED_PROPERTY) ||
    			e.getPropertyName().equals(EngineTypes.ENGINETYPES_NAME_CHANGED_PROPERTY)){
    		replaceType((String)e.getOldValue(), (String)e.getNewValue());
    	}
    	if (e.getPropertyName().equals(CarRoads.CARROADS_NAME_CHANGED_PROPERTY)){
    		replaceRoad((String)e.getOldValue(), (String)e.getNewValue());
    	}
    }
    
    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);
    
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    protected void firePropertyChange(String p, Object old, Object n) { pcs.firePropertyChange(p,old,n);}

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TrainManager.class.getName());

}

/* @(#)TrainManager.java */
