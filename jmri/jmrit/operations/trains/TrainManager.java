// TrainManager.java

package jmri.jmrit.operations.trains;

import java.util.Enumeration;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JComboBox;
import java.awt.Dimension;

import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteManager;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.OperationsXml;


/**
 *
 * @author      Bob Jacobsen Copyright (C) 2003
 * @author Daniel Boudreau Copyright (C) 2008
 * @version	$Revision: 1.2 $
 */
public class TrainManager implements java.beans.PropertyChangeListener {
	public static final String LISTLENGTH = "listLength";
	protected boolean _buildReport = false;
	protected boolean _printPreview = false;	// when true, preview train manifest
    
	public TrainManager() {
    }
    
	/** record the single instance **/
	private static TrainManager _instance = null;
	private static int _id = 0;

	public static synchronized TrainManager instance() {
		if (_instance == null) {
			if (log.isDebugEnabled()) log.debug("TrainManager creating instance");
			// create and load
			_instance = new TrainManager();
			OperationsXml.instance();				// load setup
			TrainManagerXml.instance();				// load trains
			log.debug("Trains have been loaded!");
		}
		if (Control.showInstance && log.isDebugEnabled()) log.debug("TrainManager returns instance "+_instance);
		return _instance;
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
	
	public void dispose() {
        _trainHashTable.clear();
    }

    protected Hashtable _trainHashTable = new Hashtable();   // stores known Train instances by id

    /**
     * @return requested Train object or null if none exists
     */
     
    public Train getTrainByName(String name) {
    	Train l;
    	Enumeration en =_trainHashTable.elements();
    	for (int i = 0; i < _trainHashTable.size(); i++){
    		l = (Train)en.nextElement();
    		if (l.getName().equals(name))
    			return l;
      	}
    	log.debug("train "+name+" doesn't exist");
        return null;
    }
    
    public Train getTrainById (String id){
    	return (Train)_trainHashTable.get(id);
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
    		firePropertyChange(LISTLENGTH, oldSize, new Integer(_trainHashTable.size()));
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
        firePropertyChange(LISTLENGTH, oldSize, new Integer(_trainHashTable.size()));
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
        firePropertyChange(LISTLENGTH, oldSize, new Integer(_trainHashTable.size()));
    }

   /**
     * Sort by train name
     * @return list of train ids ordered by name
     */
    public List getTrainsByNameList() {
		// first get id list
		List sortList = getList();
		// now re-sort
		List out = new ArrayList();
		String trainName = "";
		boolean trainAdded = false;
		Train train;

		for (int i = 0; i < sortList.size(); i++) {
			trainAdded = false;
			train = getTrainById((String) sortList.get(i));
			trainName = train.getName();
			for (int j = 0; j < out.size(); j++) {
				train = getTrainById((String) out.get(j));
				String outTrainName = train.getName();
				if (trainName.compareToIgnoreCase(outTrainName) < 0) {
					out.add(j, sortList.get(i));
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
    public List getTrainsByIdList() {
    	// first get id list
    	List sortList = getList();
    	// now re-sort
    	List out = new ArrayList();
    	int trainNumber = 0;
    	boolean trainAdded = false;
    	Train train;
    	
    	for (int i=0; i<sortList.size(); i++){
    		trainAdded = false;
    		train = getTrainById ((String)sortList.get(i));
    		try{
    			trainNumber = Integer.parseInt (train.getId());
    		}catch (NumberFormatException e) {
    			log.debug("train id number isn't a number");
    		}
    		for (int j=0; j<out.size(); j++ ){
    			train = getTrainById ((String)out.get(j));
        		try{
        			int outTrainNumber = Integer.parseInt (train.getId());
        			if (trainNumber < outTrainNumber){
        				out.add(j, sortList.get(i));
        				trainAdded = true;
        				break;
        			}
        		}catch (NumberFormatException e) {
        			log.debug("list out id number isn't a number");
        		}
    		}
    		if (!trainAdded){
    			out.add( sortList.get(i));
    		}
    	}
        return out;
    }
    
    private List getList() {
        String[] arr = new String[_trainHashTable.size()];
        List out = new ArrayList();
        Enumeration en = _trainHashTable.keys();
        int i=0;
        while (en.hasMoreElements()) {
            arr[i] = (String)en.nextElement();
            i++;
        }
        jmri.util.StringUtil.sort(arr);
        for (i=0; i<arr.length; i++) out.add(arr[i]);
        return out;
    }
    
    public JComboBox getComboBox (){
    	JComboBox box = new JComboBox();
    	box.addItem("");
		List trains = getTrainsByNameList();
		for (int i = 0; i < trains.size(); i++){
			Train train = getTrainById((String)trains.get(i));
			box.addItem(train);
		}
    	return box;
    }
    
    public void updateComboBox(JComboBox box) {
    	box.removeAllItems();
    	box.addItem("");
		List trains = getTrainsByNameList();
		for (int i = 0; i < trains.size(); i++){
			Train train = getTrainById((String)trains.get(i));
			box.addItem(train);
		}
    }
  
    /**
     * @return Number of trains
     */
    public int numEntries() { return _trainHashTable.size(); }
    
    public void options (org.jdom.Element e) {
        if (log.isDebugEnabled()) log.debug("ctor from element "+e);
        org.jdom.Attribute a;
 		if ((a = e.getAttribute("buildReport")) != null)
			_buildReport = a.getValue().equals("true");
 		if ((a = e.getAttribute("printPreview")) != null)
			_printPreview = a.getValue().equals("true");
    }

    
    /**
     * Create an XML element to represent this Entry. This member has to remain synchronized with the
     * detailed DTD in operations-config.xml.
     * @return Contents in a JDOM Element
     */
    public org.jdom.Element store() {
        org.jdom.Element e = new org.jdom.Element("trainOptions");
        e.setAttribute("buildReport", getBuildReport()?"true":"false");
        e.setAttribute("printPreview", getPrintPreview()?"true":"false");
        return e;
    }
    
    /**
     * The PropertyChangeListener interface in this class is
     * intended to keep track of user name changes to individual NamedBeans.
     */
    public void propertyChange(java.beans.PropertyChangeEvent e) {

    }

  
    
    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);
    
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    protected void firePropertyChange(String p, Object old, Object n) { pcs.firePropertyChange(p,old,n);}

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(TrainManager.class.getName());

}

/* @(#)TrainManager.java */
