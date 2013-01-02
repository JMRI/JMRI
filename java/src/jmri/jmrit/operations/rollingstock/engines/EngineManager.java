// EngineManager.java

package jmri.jmrit.operations.rollingstock.engines;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JComboBox;

import org.jdom.Element;

import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.OperationsSetupXml;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.RollingStockManager;
import jmri.jmrit.operations.trains.Train;


/**
 * Manages the engines.
 * @author Daniel Boudreau Copyright (C) 2008
 * @version	$Revision$
 */
public class EngineManager extends RollingStockManager{
	
	// Engines frame table column widths (12), starts with Number column and ends with Edit
	private int[] _enginesTableColumnWidths = {60, 60, 65, 65, 35, 75, 190, 190, 65, 50, 65, 70};

	protected Hashtable<String, Consist> _consistHashTable = new Hashtable<String, Consist>();   	// stores Consists by number

	public static final String CONSISTLISTLENGTH_CHANGED_PROPERTY = "ConsistListLength"; // NOI18N

    public EngineManager() {
    }
    
	/** record the single instance **/
	private static EngineManager _instance = null;

	public static synchronized EngineManager instance() {
		if (_instance == null) {
			if (log.isDebugEnabled()) log.debug("EngineManager creating instance");
			// create and load
			_instance = new EngineManager();
			OperationsSetupXml.instance();					// load setup
	    	// create manager to load engines and their attributes
	    	EngineManagerXml.instance();
		}
		if (Control.showInstance && log.isDebugEnabled()) log.debug("EngineManager returns instance "+_instance);
		return _instance;
	}

    /**
     * @return requested Engine object or null if none exists
     */
    public Engine getById(String id) {
        return (Engine)super.getById(id);
    }
    
    public Engine getByRoadAndNumber (String engineRoad, String engineNumber){
    	String engineId = Engine.createId (engineRoad, engineNumber);
    	return getById (engineId);
    }
 
    /**
     * Finds an existing engine or creates a new engine if needed
     * requires engine's road and number
     * @param engineRoad
     * @param engineNumber
     * @return new engine or existing engine
     */
    public Engine newEngine (String engineRoad, String engineNumber){
    	Engine engine = getByRoadAndNumber(engineRoad, engineNumber);
    	if (engine == null){
    		engine = new Engine(engineRoad, engineNumber);
    		register(engine);
    	}
    	return engine;
    }

    /**
     * Creates a new consist if needed
     * @param name of the consist
     * @return consist
     */
    public Consist newConsist(String name){
    	Consist consist = getConsistByName(name);
    	if (consist == null){
    		consist = new Consist(name);
    		Integer oldSize = Integer.valueOf(_consistHashTable.size());
    		_consistHashTable.put(name, consist);
    		firePropertyChange(CONSISTLISTLENGTH_CHANGED_PROPERTY, oldSize, Integer.valueOf(_consistHashTable.size()));
    	}
    	return consist;
    }
    
    public void deleteConsist(String name){
    	Consist consist = getConsistByName(name);
    	if (consist != null){
    		consist.dispose();
    		Integer oldSize = Integer.valueOf(_consistHashTable.size());
    		_consistHashTable.remove(name);
    		firePropertyChange(CONSISTLISTLENGTH_CHANGED_PROPERTY, oldSize, Integer.valueOf(_consistHashTable.size()));
    	}
    }
    
    public Consist getConsistByName(String name){
    	Consist consist = _consistHashTable.get(name);
    	return consist;
    }
    
    /**
     * Creates a combo box containing all of the consist names
     * @return a combo box with all of the consist names
     */
    public JComboBox getConsistComboBox(){
    	JComboBox box = new JComboBox();
    	box.addItem("");
       	List<String> consistNames = getConsistNameList();
    	for (int i=0; i<consistNames.size(); i++) {
       		box.addItem(consistNames.get(i));
    	}
    	return box;
    }
    
    public void updateConsistComboBox(JComboBox box) {
    	box.removeAllItems();
    	box.addItem("");
    	List<String> consistNames = getConsistNameList();
    	for (int i=0; i<consistNames.size(); i++) {
       		box.addItem(consistNames.get(i));
    	}
    }
    
    public List<String> getConsistNameList(){
    	String[] arr = new String[_consistHashTable.size()];
    	List<String> out = new ArrayList<String>();
       	Enumeration<String> en = _consistHashTable.keys();
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
     * Sort by engine model
     * @return list of engine ids ordered by engine model
     */
    public List<String> getByModelList() {
    	return getByList(getByRoadNameList(), BY_MODEL);
    }
    
    /**
     * Sort by engine consist
     * @return list of engine ids ordered by engine consist
     */
    public List<String> getByConsistList() {
    	return getByList(getByRoadNameList(), BY_CONSIST);
    }
  
    // The special sort options for engines
    private static final int BY_MODEL = 4;
    private static final int BY_CONSIST = 5;
    
    // provide model and consist sorts
    protected Object getRsAttribute(RollingStock rs, int attribute){
    	Engine eng = (Engine)rs;
    	switch (attribute){
    	case BY_MODEL: return eng.getModel(); 
    	case BY_CONSIST: return eng.getConsistName();
    	default: return super.getRsAttribute(rs, attribute);	
    	}
    }
    
       /**
	 * return a list available engines (no assigned train) engines are
	 * ordered least recently moved to most recently moved.
	 * 
	 * @param train
	 * @return Ordered list of engine ids not assigned to a train
	 */
    public List<String> getAvailableTrainList(Train train) {
    	// get engines by moves list
    	List<String> enginesSortByMoves = getByMovesList();
    	// now build list of available engines for this route
    	List<String> out = new ArrayList<String>();
    	Engine engine;
 
    	for (int i = 0; i < enginesSortByMoves.size(); i++) {
    		engine = getById(enginesSortByMoves.get(i));
    		if(engine.getTrack() != null && (engine.getTrain()== null || engine.getTrain()==train))
    			out.add(enginesSortByMoves.get(i));
    	}
    	return out;
    }
    
    /**
     * Get a list of engine road names.
     * @return List of engine road names.
     */
    public List<String> getEngineRoadNames(String model){
    	List<String> names = new ArrayList<String>();
       	Enumeration<String> en = _hashTable.keys();
    	while (en.hasMoreElements()) { 
    		Engine engine = getById(en.nextElement());
    		if ((engine.getModel().equals(model) || model.equals(""))
    				&& !names.contains(engine.getRoad())){
    			names.add(engine.getRoad());
    		}
    	}
    	return sortList(names);
    }
    
	/**
    * 
    * @return get an array of table column widths for the trains frame
    */
   public int[] getEnginesFrameTableColumnWidths(){
   	return _enginesTableColumnWidths.clone();
   }

	public void options (org.jdom.Element values) {
		if (log.isDebugEnabled()) log.debug("ctor from element "+values);
		// get Engines Table Frame attributes
		Element e = values.getChild(Xml.ENGINES_OPTIONS);
		if (e != null){
			//TODO this code is here for backwards compatibility, remove after next major release
			org.jdom.Attribute a;
	  		if ((a = e.getAttribute(Xml.COLUMN_WIDTHS)) != null){
             	String[] widths = a.getValue().split(" ");
             	for (int i=0; i<widths.length; i++){
             		try{
             			_enginesTableColumnWidths[i] = Integer.parseInt(widths[i]);
             		} catch (NumberFormatException ee){
             			log.error("Number format exception when reading trains column widths");
             		}
             	}
    		}
		}
	}

	   /**
     * Create an XML element to represent this Entry. This member has to remain synchronized with the
     * detailed DTD in operations-engines.dtd.
     * @return Contents in a JDOM Element
     */
    public org.jdom.Element store() {
    	Element values = new Element(Xml.OPTIONS);
    	// nothing to store!
        return values;
    }
    
    protected void firePropertyChange(String p, Object old, Object n){
		// Set dirty
		EngineManagerXml.instance().setDirty(true);
    	super.firePropertyChange(p, old, n);
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EngineManager.class.getName());
}

