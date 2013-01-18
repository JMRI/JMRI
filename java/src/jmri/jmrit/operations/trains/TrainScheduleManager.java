// TrainScheduleManager.java

package jmri.jmrit.operations.trains;

import java.util.Enumeration;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import javax.swing.JComboBox;

import org.jdom.Element;

import jmri.jmrit.operations.setup.Control;


/**
 * Manages train schedules.
 * @author      Bob Jacobsen Copyright (C) 2003
 * @author Daniel Boudreau Copyright (C) 2010
 * @version	$Revision$
 */
public class TrainScheduleManager implements java.beans.PropertyChangeListener {
	
	public static final String LISTLENGTH_CHANGED_PROPERTY = "trainScheduleListLength";  // NOI18N
    
	public TrainScheduleManager() {
		
    }
    
	/** record the single instance **/
	private static TrainScheduleManager _instance = null;
	private static int _id = 0;

	public static synchronized TrainScheduleManager instance() {
		if (_instance == null) {
			if (log.isDebugEnabled()) log.debug("TrainScheduleManager creating instance");
			// create and load
			_instance = new TrainScheduleManager();
			TrainManagerXml.instance();				// load trains
		}
		if (Control.showInstance && log.isDebugEnabled()) log.debug("TrainScheduleManager returns instance "+_instance);
		return _instance;
	}

    public void dispose() {
        _scheduleHashTable.clear();
    }

    protected Hashtable<String, TrainSchedule> _scheduleHashTable = new Hashtable<String, TrainSchedule>();   // stores known Schedule instances by id

    /**
     * @return Number of schedules
     */
    public int numEntries() {
    	if (_scheduleHashTable.size() == 0)
    		createDefaultSchedules();
    	return _scheduleHashTable.size(); 
    }
    
    /**
     * @return requested TrainSchedule object or null if none exists
     */
    public TrainSchedule getScheduleByName(String name) {
    	TrainSchedule s;
    	Enumeration<TrainSchedule> en =_scheduleHashTable.elements();
    	for (int i = 0; i < _scheduleHashTable.size(); i++){
    		s = en.nextElement();
    		if (s.getName().equals(name))
    			return s;
      	}
        return null;
    }
    
    public TrainSchedule getScheduleById (String id){
    	return _scheduleHashTable.get(id);
    }
 
    /**
     * Finds an existing schedule or creates a new schedule if needed
     * requires schedule's name creates a unique id for this schedule
     * @param name
     * 
     * @return new TrainSchedule or existing TrainSchedule
     */
    public TrainSchedule newSchedule (String name){
    	TrainSchedule schedule = getScheduleByName(name);
    	if (schedule == null){
    		_id++;						
    		schedule = new TrainSchedule(Integer.toString(_id), name);
    		Integer oldSize = Integer.valueOf(_scheduleHashTable.size());
    		_scheduleHashTable.put(schedule.getId(), schedule);
    		firePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize, Integer.valueOf(_scheduleHashTable.size()));
    	}
    	return schedule;
    }
    
    /**
     * Remember a NamedBean Object created outside the manager.
 	 */
    public void register(TrainSchedule schedule) {
    	Integer oldSize = Integer.valueOf(_scheduleHashTable.size());
        _scheduleHashTable.put(schedule.getId(), schedule);
        // find last id created
        int id = Integer.parseInt(schedule.getId());
        if (id > _id)
        	_id = id;
        firePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize, Integer.valueOf(_scheduleHashTable.size()));
    }

    /**
     * Forget a NamedBean Object created outside the manager.
     */
    public void deregister(TrainSchedule schedule) {
    	if (schedule == null)
    		return;
        Integer oldSize = Integer.valueOf(_scheduleHashTable.size());
    	_scheduleHashTable.remove(schedule.getId());
        firePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize, Integer.valueOf(_scheduleHashTable.size()));
    }

    /**
     * Sort by schedule name
     * @return list of schedule ids ordered by name
     */
    public List<String> getSchedulesByNameList() {
		// first get id list
		List<String> sortList = getList();
		// now re-sort
		List<String> out = new ArrayList<String>();
		String schName = "";
		boolean schAdded = false;
		TrainSchedule s;

		for (int i = 0; i < sortList.size(); i++) {
			schAdded = false;
			s = getScheduleById(sortList.get(i));
			schName = s.getName();
			for (int j = 0; j < out.size(); j++) {
				s = getScheduleById(out.get(j));
				String outLocName = s.getName();
				if (schName.compareToIgnoreCase(outLocName) < 0) {
					out.add(j, sortList.get(i));
					schAdded = true;
					break;
				}
			}
			if (!schAdded) {
				out.add(sortList.get(i));
			}
		}
		return out;
	}
    
    /**
	 * Sort by schedule number
	 * 
	 * @return list of schedule ids ordered by number
	 */
    public List<String> getSchedulesByIdList() {
    	// first get id list
    	List<String> sortList = getList();
    	// now re-sort
    	List<String> out = new ArrayList<String>();
    	int scheduleNumber = 0;
    	boolean scheduleAdded = false;
    	TrainSchedule s;
    	
    	for (int i=0; i<sortList.size(); i++){
    		scheduleAdded = false;
    		s = getScheduleById (sortList.get(i));
    		try{
    			scheduleNumber = Integer.parseInt (s.getId());
    		}catch (NumberFormatException e) {
    			log.debug("schedule id number isn't a number");
    		}
    		for (int j=0; j<out.size(); j++ ){
    			s = getScheduleById (out.get(j));
        		try{
        			int outScheduleNumber = Integer.parseInt (s.getId());
        			if (scheduleNumber < outScheduleNumber){
        				out.add(j, sortList.get(i));
        				scheduleAdded = true;
        				break;
        			}
        		}catch (NumberFormatException e) {
        			log.debug("list out id number isn't a number");
        		}
    		}
    		if (!scheduleAdded){
    			out.add( sortList.get(i));
    		}
    	}
        return out;
    }
    
    private List<String> getList() {
        String[] arr = new String[numEntries()];
        List<String> out = new ArrayList<String>();
        Enumeration<String> en = _scheduleHashTable.keys();
        int i=0;
        while (en.hasMoreElements()) {
            arr[i] = en.nextElement();
            i++;
        }
        jmri.util.StringUtil.sort(arr);
        for (i=0; i<arr.length; i++) out.add(arr[i]);
        return out;
    }
    
    /**
     * Gets a JComboBox loaded with schedules.
     * @return JComboBox with a list of schedules.
     */
    public JComboBox getComboBox (){
    	JComboBox box = new JComboBox();
		List<String> schs = getSchedulesByNameList();
		for (int i = 0; i < schs.size(); i++){
			box.addItem(getScheduleById(schs.get(i)));
		}
    	return box;
    }
    
	/**
	 * Gets a JComboBox loaded with schedules starting with an empty string.
	 * 
	 * @return JComboBox with a list of schedules starting with an empty
	 *         string.
	 */
	public JComboBox getSelectComboBox(){
    	JComboBox box = new JComboBox();
    	box.addItem("");
    	List<String> schs = getSchedulesByIdList();
		for (int i = 0; i < schs.size(); i++){
			box.addItem(getScheduleById(schs.get(i)));
		}
    	return box;
    }
    
    /**
     * Update a JComboBox with the latest schedules.
     * @param box the JComboBox needing an update.
     */
    public void updateComboBox(JComboBox box) {
    	box.removeAllItems();
		List<String> schs = getSchedulesByNameList();
		for (int i = 0; i < schs.size(); i++){
			String id = schs.get(i);
			box.addItem(getScheduleById(id));
		}
    }
    
    /**
     * Create an XML element to represent this Entry. This member has to remain synchronized with the
     * detailed DTD in operations-trains.dtd.
     *
     */
    public void store(Element root) {
    	Element values = new Element(Xml.SCHEDULES);
		// add entries
    	List<String> schedules = getSchedulesByIdList();
		for (int i=0; i<schedules.size(); i++) {
			String id = schedules.get(i);
			TrainSchedule sch = getScheduleById(id);
			values.addContent(sch.store());
		}
    	root.addContent(values);
    }
    
    public void load (Element root) {
    	Element e = root.getChild(Xml.SCHEDULES);
    	if (e != null){
           	@SuppressWarnings("unchecked")
            List<Element> l = root.getChild(Xml.SCHEDULES).getChildren(Xml.SCHEDULE);
            if (log.isDebugEnabled()) log.debug("TrainScheduleManager sees "+l.size()+" train schedules");
            for (int i=0; i<l.size(); i++) {
                register(new TrainSchedule(l.get(i)));
            }
    	}
    }
    
    private void createDefaultSchedules(){
    	log.debug("creating default schedules");
    	newSchedule(Bundle.getMessage("Sunday"));
    	newSchedule(Bundle.getMessage("Monday"));
    	newSchedule(Bundle.getMessage("Tuesday"));
    	newSchedule(Bundle.getMessage("Wednesday"));
    	newSchedule(Bundle.getMessage("Thursday"));
    	newSchedule(Bundle.getMessage("Friday"));
		newSchedule(Bundle.getMessage("Saturday"));
    }

    public void propertyChange(java.beans.PropertyChangeEvent e) {
    	log.debug("ScheduleManager sees property change: " + e.getPropertyName() + " old: " + e.getOldValue() + " new " + e.getNewValue());
    }
    
    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);
    
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    protected void firePropertyChange(String p, Object old, Object n) {
    	TrainManagerXml.instance().setDirty(true);
    	pcs.firePropertyChange(p,old,n);
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TrainScheduleManager.class.getName());

}

/* @(#)TrainScheduleManager.java */
