// CarColors.java

package jmri.jmrit.operations.cars;

import java.util.Enumeration;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import javax.swing.JComboBox;


/**
 * Represents the colors that cars can have.
 * @author Daniel Boudreau Copyright (C) 2008
 * @version	$Revision: 1.1 $
 */
public class CarColors implements java.beans.PropertyChangeListener {
	
	private static final String COLORS = "Black%%Blue%%Brown%%Gray%%Yellow%%Green%%Orange%%Purple%%Red%%Silver%%Tuscan%%White";
	public static final String CARCOLORS = "CarColors";
	private static final String LENGTH = "Length";
	
    public CarColors() {
    }
    
	/** record the single instance **/
	private static CarColors _instance = null;

	public static synchronized CarColors instance() {
		if (_instance == null) {
			if (log.isDebugEnabled()) log.debug("CarColors creating instance");
			// create and load
			_instance = new CarColors();
		}
		if (log.isDebugEnabled()) log.debug("CarColors returns instance "+_instance);
		return _instance;
	}

    public void dispose() {
    	list.clear();
    }
    
    /**
     * The PropertyChangeListener interface in this class is
     * intended to keep track of user name changes to individual NamedBeans.
     * It is not completely implemented yet. In particular, listeners
     * are not added to newly registered objects.
     */
    public void propertyChange(java.beans.PropertyChangeEvent e) {
    }

    List list = new ArrayList();
    
    public String[] getNames(){
     	if (list.size() == 0){
     		String[] colors = COLORS.split("%%");
     		for (int i=0; i<colors.length; i++)
     			list.add(colors[i]);
    	}
     	String[] colors = new String[list.size()];
     	for (int i=0; i<list.size(); i++)
     		colors[i] = (String)list.get(i);
   		return colors;
    }
    
    public void setNames(String[] colors){
    	if (colors.length == 0) return;
    	jmri.util.StringUtil.sort(colors);
 		for (int i=0; i<colors.length; i++)
 			list.add(colors[i]);
    }
    
    public void addName(String color){
    	// insert at start of list, sort later
    	if (list.contains(color))
    		return;
    	list.add(0,color);
    	firePropertyChange (CARCOLORS, null, LENGTH);
    }
    
    public void deleteName(String color){
    	list.remove(color);
    	firePropertyChange (CARCOLORS, null, LENGTH);
    }
    
    public boolean containsName(String color){
    	return list.contains(color);
     }
    
    public JComboBox getComboBox (){
    	JComboBox box = new JComboBox();
		String[] colors = getNames();
		for (int i = 0; i < colors.length; i++)
			box.addItem(colors[i]);
    	return box;
    }
    
    public void updateComboBox(JComboBox box) {
    	box.removeAllItems();
		String[] colors = getNames();
		for (int i = 0; i < colors.length; i++)
			box.addItem(colors[i]);
    }
        
    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    protected void firePropertyChange(String p, Object old, Object n) { pcs.firePropertyChange(p,old,n);}

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(CarColors.class.getName());

}

